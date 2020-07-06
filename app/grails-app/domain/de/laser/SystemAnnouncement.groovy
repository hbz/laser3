package de.laser

import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import de.laser.helper.ServerUtils
import grails.util.Holders
import net.sf.json.JSON

import javax.persistence.Transient

class SystemAnnouncement {

    def grailsApplication
    def mailService
    @Transient
    def escapeService

    User    user
    String  title
    String  content
    String  status

    boolean isPublished = false

    Date    lastPublishingDate
    Date    dateCreated
    Date    lastUpdated

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
        user        (blank:false)
        title       (blank:false)
        content     (blank:false)
        status      (nullable:true, blank:false)
        isPublished (blank:false)
        lastPublishingDate (nullable:true, blank:false)
        dateCreated (nullable:true, blank:false)
        lastUpdated (nullable:true, blank:false)
    }

    static List<SystemAnnouncement> getPublished(int periodInDays) {
        def dcCheck = (new Date()).minus(periodInDays)

        SystemAnnouncement.executeQuery(
                'select sa from SystemAnnouncement sa ' +
                'where sa.isPublished = true and sa.lastPublishingDate >= :dcCheck order by sa.lastPublishingDate desc',
                [dcCheck: dcCheck]
        )
    }

    static List<User> getRecipients() {
        User.executeQuery(
                'select u from UserSettings uss join uss.user u where uss.key = :ussKey and uss.rdValue = :ussValue order by u.id',
                [ussKey: UserSettings.KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, ussValue: RDStore.YN_YES]
        )
    }

    static String cleanUp(String s) {
        s.replaceAll("\\<.*?>","")
    }
    String getCleanTitle() {
        SystemAnnouncement.cleanUp(escapeService.replaceUmlaute(title))
    }

    String getCleanContent() {
        SystemAnnouncement.cleanUp(escapeService.replaceUmlaute(content))
    }

    boolean publish() {
        if (grailsApplication.config.grails.mail.disabled == true) {
            println 'SystemAnnouncement.publish() failed due grailsApplication.config.grails.mail.disabled = true'
            return false
        }

        List<User> reps = SystemAnnouncement.getRecipients()
        List validUserIds = []
        List failedUserIds = []

        lastPublishingDate = new Date()
        isPublished = true
        save()

        reps.each { u ->
            try {
                sendMail(u)
                validUserIds << u.id
            }
            catch (Exception e) {
                log.error(e.getMessage())
                log.error(e.getStackTrace())
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

    private void sendMail(User user) throws Exception {

        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()

        String currentServer = ServerUtils.getCurrentServer()
        String subjectSystemPraefix = (currentServer == ServerUtils.SERVER_PROD) ? "LAS:eR - " : (grailsApplication.config.laserSystemId + " - ")
        String mailSubject = subjectSystemPraefix + messageSource.getMessage('email.subject.sysAnnouncement', null, locale)

        boolean isRemindCCbyEmail = user.getSetting(UserSettings.KEYS.IS_REMIND_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
        String ccAddress

        if (isRemindCCbyEmail){
            ccAddress = user.getSetting(UserSettings.KEYS.REMIND_CC_EMAILADDRESS, null)?.getValue()
            // println user.toString() + " : " + isRemindCCbyEmail + " : " + ccAddress
        }

        if (isRemindCCbyEmail && ccAddress) {
            mailService.sendMail {
                to      user.getEmail()
                from    grailsApplication.config.notifications.email.from
                cc      ccAddress
                replyTo grailsApplication.config.notifications.email.replyTo
                subject mailSubject
                body    (view: "/mailTemplates/text/systemAnnouncement", model: [user: user, announcement: this])
            }
        }
        else {
            mailService.sendMail {
                to      user.getEmail()
                from    grailsApplication.config.notifications.email.from
                replyTo grailsApplication.config.notifications.email.replyTo
                subject mailSubject
                body    (view: "/mailTemplates/text/systemAnnouncement", model: [user: user, announcement: this])
            }
        }
    }
}
