package de.laser.system

import de.laser.MailSendService
import de.laser.UserSetting
import de.laser.auth.User
import de.laser.config.ConfigMapper
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.converters.JSON
import groovy.util.logging.Slf4j

import java.time.LocalDate

/**
 * This class reflects system-wide messages which can be shown on the dashboard.
 * The service message may be sent moreover to users who subscribed to these reminders. This reminder setting may be done in the user profile (and is stored as a {@link UserSetting}).
 */
@Slf4j
class ServiceMessage {

    User    user
    String  title
    String  content
    String  status

    boolean isPublished = false

    Date    lastPublishingDate
    Date    dateCreated
    Date    lastUpdated

    static transients = ['cleanTitle', 'cleanContent'] // mark read-only accessor methods

    static mapping = {
        id              column: 'sa_id'
        version         column: 'sa_version'
        user            column: 'sa_user_fk'
        title           column: 'sa_title'
        content         column: 'sa_content', type: 'text'
        status          column: 'sa_status', type: 'text'
        isPublished     column: 'sa_is_published'
        lastPublishingDate column: 'sa_last_publishing_date'
        dateCreated     column: 'sa_date_created'
        lastUpdated     column: 'sa_last_updated'
    }

    static constraints = {
        title       (blank:false)
        content     (blank:false)
        status      (nullable:true, blank:false)
        lastPublishingDate (nullable:true)
        lastUpdated (nullable:true)
    }

    /**
     * Retrieves a {@link List} of service messages which have been published in the given period of days
     * @param periodInDays the amount of days to look back for recently published messages
     * @return a {@link List} of service messages
     */
    static List<ServiceMessage> getPublished(int periodInDays) {
        Date dcCheck = DateUtils.localDateToSqlDate( LocalDate.now().minusDays(periodInDays) )

        ServiceMessage.executeQuery(
                'select msg from ServiceMessage msg ' +
                'where msg.isPublished = true and msg.lastPublishingDate >= :dcCheck order by msg.lastPublishingDate desc',
                [dcCheck: dcCheck]
        )
    }

    /**
     * Gets all users who should be notified about service messages. The criteria to be checked is the {@link UserSetting.KEYS#IS_NOTIFICATION_FOR_SYSTEM_MESSAGES} setting
     * @return a {@link List} of {@link User}s to be notified
     */
    static List<User> getRecipients() {
        User.executeQuery(
                'select u from UserSetting uss join uss.user u where uss.key = :ussKey and uss.rdValue = :ussValue order by u.id',
                [ussKey: UserSetting.KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, ussValue: RDStore.YN_YES]
        )
    }

    /**
     * Strips the chars '<' and '>' from a given string
     * @param s the string to be sanitized
     * @return the sanitized string
     */
    static String cleanUp(String s) {
        s.replaceAll("\\<.*?>","")
    }

    /**
     * Sanitizes the title of the system message
     * @return the sanitized title string
     */
    String getCleanTitle() {
        ServiceMessage.cleanUp(BeanStore.getEscapeService().replaceUmlaute(title))
    }

    /**
     * Sanitizes the content of the system message
     * @return the sanitized content string
     */
    String getCleanContent() {
        ServiceMessage.cleanUp(BeanStore.getEscapeService().replaceUmlaute(content))
    }

    /**
     * Publishes the service message via the given channels (display on pages, sending of reminder mails)
     * @return true if the publishing was successful, false otherwise
     */
    boolean publish() {
        if (ConfigMapper.getConfig('grails.mail.disabled', Boolean) == true) {
            log.debug 'ServiceMessage.publish() failed due grails.mail.disabled = true'
            return false
        }

        MailSendService mailSendService = BeanStore.getMailSendService()
        withTransaction {

            List<User> reps = ServiceMessage.getRecipients()
            List validUserIds = []
            List failedUserIds = []

            lastPublishingDate = new Date()
            isPublished = true
            save()

            reps.each { u ->
                try {
                    mailSendService.sendServiceMessageMail(u, this)
                    validUserIds << u.id
                }
                catch (Exception e) {
                    log.error(e.getMessage())
                    e.printStackTrace()
                    failedUserIds << u.id
                }
            }

            status = ([
                    validUserIds : validUserIds,
                    failedUserIds: failedUserIds
            ] as JSON).toString()

            save()

            if (validUserIds.size() > 0) {
                SystemEvent.createEvent('SYSANN_SENDING_OK', ['count': validUserIds.size()])
            }

            if (failedUserIds.size() > 0) {
                SystemEvent.createEvent('SYSANN_SENDING_ERROR', ['users': failedUserIds, 'count': failedUserIds.size()])
            }

            return failedUserIds.isEmpty()
        }
    }


}
