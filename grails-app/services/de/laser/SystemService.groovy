package de.laser

import de.laser.auth.User
import de.laser.config.ConfigMapper
import de.laser.remote.ApiSource
import de.laser.remote.GlobalRecordSource
import de.laser.system.SystemEvent
import de.laser.system.SystemMessage
import de.laser.system.SystemSetting
import de.laser.utils.AppUtils
import de.laser.utils.DateUtils
import grails.gorm.transactions.Transactional
import grails.plugins.mail.MailService
import groovy.json.JsonOutput
import org.springframework.mail.MailMessage

import java.text.SimpleDateFormat
import java.time.LocalDate

/**
 * This service checks the system health
 */
@Transactional
class SystemService {

    ContextService contextService
    MailService mailService

    /**
     * Dumps the state of currently active services
     * @return a map with the services marked as active by configuration or with sources marked as active and their running state
     */
    Map<String, Object> serviceCheck() {
        Map<String, Object> checks = [:]

            if (GlobalRecordSource.findAll().size() in [0, GlobalRecordSource.findAllByActive(false).size()]) {
                checks.globalSync = "NOT active"
            }
            if (ApiSource.findAll().size() in [0, ApiSource.findAllByActive(false).size()]) {
                checks.apiSource = "NOT active"
            }

            if (! ConfigMapper.getNotificationsJobActive()) {
                checks.notificationsJobActive = "NOT active"
            }
            if (! ConfigMapper.getGlobalDataSyncJobActive()) {
                checks.globalDataSyncJob = "NOT active"
            }
            if (! ConfigMapper.getIsUpdateDashboardTableInDatabase()) {
                checks.UpdateDashboardTableInDatabase = "NOT active"
            }
            if (! ConfigMapper.getIsSendEmailsForDueDatesOfAllUsers()) {
                checks.SendEmailsForDueDatesOfAllUsers = "NOT active"
            }
            if (! ConfigMapper.getReporting()) {
                checks.Reporting = "ElasticSearch Config for Reporting not found"
            }
            if (ConfigMapper.getConfig('grails.mail.disabled', Boolean)) {
                checks.MailService = "NOT active"
            }

        return checks
    }

    /**
     * Gets the system status message
     * @param messageId the message ID to display
     * @return a {@link Map} with the status, state of maintenance and the messages themselves
     */
    Map getStatusMessage(long messageId = 0) {
        Map result = [ status: 'error' ]

        try {
            result = [
                    status:      'ok',
                    maintenance: SystemSetting.findByName('MaintenanceMode').value == 'true',
                    messages:    SystemMessage.getActiveMessages(SystemMessage.TYPE_ATTENTION) ? true : false
            ]
        } catch(Exception e) {
            log.error( e.getMessage() )
        }
        if (messageId) {
            result.putAt('id', messageId)
        }

        result
    }

    /**
     * Sends system status / events mails to registered email addresses. The email registry is a setting in the
     * local configuration file; see the configuration setting at {@link ConfigMapper#SYSTEM_INSIGHT_EMAILS)}
     */
    void sendSystemInsightMails() {
        if (SystemSetting.findByName('SystemInsight').value != 'true') {
            log.info '---> Sending system insight mails .. ignored'
        }
        else {
            log.info '---> Sending system insight mails ..'

            String recipients = ConfigMapper.getSystemInsightEmails()

            Map<String, Object> seMap = [recipients: recipients, status: 'ok']
            SystemEvent se = SystemEvent.createEvent('SYSTEM_INSIGHT_MAILS_START', seMap)

            if (recipients) {
                SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMdd_HHmmss()

                String query             = "from SystemEvent se where se.created > (CURRENT_DATE-1) and se.relevance in ('WARNING', 'ERROR')"
                List<SystemEvent> seList = SystemEvent.executeQuery( "select se " + query + " order by se.created desc" )
                List relCounts           = SystemEvent.executeQuery( "select se.relevance, count(*) " + query + " group by se.relevance order by se.relevance desc" )

                Map<String, Object> eCounts = [:]
                seList.each { e ->
                    String key   = "${e.getSource()} -> ${e.getEvent()}"
                    eCounts[key] = eCounts[key] ? eCounts[key] + 1 : 1
                }

                Map<String, Object> output = [
                        system      : "${ConfigMapper.getLaserSystemId()}",
                        server      : "${AppUtils.getCurrentServer()}",
                        created     : "${sdf.format(new Date())}",
                        meta        :  [
                                data_type   : "SystemEvent",
                                level_count : relCounts.collectEntries{ it },
                                event_count : eCounts
                        ],
                        data        : []
                ]

                if (seList) {
                    seList.each { e ->
                        Map<String, Object> data = [
                                created : "${sdf.format(e.created)}",
                                level   : "${e.relevance.value}",
                                event   : "${e.getSource()} -> ${e.getEvent()}"
                        ]
                        if (e.payload) {
                            data.putAt('payload', "${e.payload}")
                        }
                        output.data.add( data )
                    }
                }

                String mailContent = JsonOutput.prettyPrint(JsonOutput.toJson(output))
                // println mailContent

                Map<String, String> mailsFailed = [:]
                String mailsStatus = 'ok'

                recipients.split(',').each { mailTo ->
                    log.info 'to .. ' + mailTo

                    try {
                        MailMessage mail = mailService.sendMail {
                            to      mailTo
                            from    ConfigMapper.getNotificationsEmailFrom()
                            replyTo ConfigMapper.getNotificationsEmailReplyTo()
                            subject ConfigMapper.getLaserSystemId() + ' - (Insight)'
                            text    mailContent
                        }
                        if (!mail) {
                            mailsFailed.put(mailTo, 'unkown error')
                            mailsStatus = 'error'
                        }
                    }
                    catch (Exception e) {
                        log.error "mailService.sendMail exception: ${e.message}"
                        mailsFailed.put(mailTo, e.message.split(';').first())
                        mailsStatus = 'error'
                    }
                }
                seMap.put('status', mailsStatus)

                if (mailsFailed) {
                    seMap.put('failed', mailsFailed)
                }
            }

            se.changeTo(seMap.status == 'ok' ? 'SYSTEM_INSIGHT_MAILS_COMPLETE' : 'SYSTEM_INSIGHT_MAILS_ERROR', seMap)
        }
    }

    void flagExpiredUserAccounts() {
        log.info "--> flagExpiredUserAccounts"

        List expiredAccounts = []
        LocalDate now = LocalDate.now()

        User.executeQuery("select u from User u where u.accountExpired != true and u.username != 'anonymous' order by u.username").each{ User usr ->
            LocalDate lastLogin = usr.lastLogin ? DateUtils.dateToLocalDate(usr.lastLogin) : DateUtils.dateToLocalDate(usr.dateCreated)
            if (lastLogin.isBefore(now.minusMonths(6))) {
                usr.accountExpired = true
                usr.save()

                expiredAccounts.add([usr.id, usr.username, usr.lastLogin])
            }
        }

        if (expiredAccounts) {
            SystemEvent.createEvent('SYSTEM_FLAG_EXPIRED_ACCOUNTS', [expiredAccounts: expiredAccounts])
        }
    }
}
