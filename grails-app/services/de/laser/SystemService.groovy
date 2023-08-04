package de.laser

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

import java.text.SimpleDateFormat

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

    void sendSystemInsightMails() {
        if (SystemSetting.findByName('SystemInsight').value != 'true') {
            log.info '---> Sending system insight mails .. ignored'
        }
        else {
            log.info '---> Sending system insight mails ..'

            String recipients = ConfigMapper.getSystemInsightMails()

            Map<String, Object> seMap = [recipients: recipients, status: 'ok']
            SystemEvent se = SystemEvent.createEvent('SYSTEM_INSIGHT_MAILS_START', seMap)

            if (recipients) {
                SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMdd_HHmmss()
                List<SystemEvent> events = SystemEvent.executeQuery(
                        "select se from SystemEvent se where se.created > (CURRENT_DATE-1) and se.relevance in ('WARNING', 'ERROR') order by se.created desc"
                )

                Map<String, Object> output = [
                        system      : "${ConfigMapper.getLaserSystemId()}",
                        server      : "${AppUtils.getCurrentServer()}",
                        created     : "${sdf.format(new Date())}",
                        data_type   : "SystemEvent",
                        data_count  : events.size(),
                        data        : []
                ]

                if (events) {
                    events.each { e ->
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

                Map<String, String> mailsVerbose = [:]
                String mailsStatus = 'ok'

                recipients.split(',').each { mailTo ->
                    log.info 'to .. ' + mailTo

                    try {
                        mailService.sendMail {
                            to      mailTo
                            from    ConfigMapper.getNotificationsEmailFrom()
                            replyTo ConfigMapper.getNotificationsEmailReplyTo()
                            subject ConfigMapper.getLaserSystemId() + ' - (Insight)'
                            text    mailContent
                        }
                        mailsVerbose.put(mailTo, 'ok')
                    }
                    catch (Exception e) {
                        log.error "mailService.sendMail exception: ${e.message}"
                        mailsVerbose.put(mailTo, e.message)
                        mailsStatus = 'error'
                    }
                }

                seMap.put('status', mailsStatus)
                seMap.put('verbose', mailsVerbose)
            }
            se.changeTo('SYSTEM_INSIGHT_MAILS_COMPLETE', seMap)
        }
    }
}
