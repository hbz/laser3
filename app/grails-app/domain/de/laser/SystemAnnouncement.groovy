package de.laser

import com.k_int.kbplus.UserSettings
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import de.laser.SystemEvent

class SystemAnnouncement {

    User    user
    String  title
    String  content

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
        isPublished     column: 'sa_is_published'
        lastPublishingDate column: 'sa_last_publishing_date'
        dateCreated     column: 'sa_date_created'
        lastUpdated     column: 'sa_last_updated'
    }

    static constraints = {
        user        (nullable:false, blank:false)
        title       (nullable:false, blank:false)
        content     (nullable:false, blank:false)
        isPublished (nullable:false, blank:false)
        lastPublishingDate (nullable:true, blank:false)
        dateCreated (nullable:true, blank:false)
        lastUpdated (nullable:true, blank:false)
    }

    static List<SystemAnnouncement> getPublished(int max) {

        SystemAnnouncement.executeQuery(
                'select sa from SystemAnnouncement sa where sa.isPublished = true order by sa.lastPublishingDate desc',
                [max: max]
        )
    }

    static List<User> getRecipients() {

        User.executeQuery(
                'select u from UserSettings uss join uss.user u where uss.key = :ussKey and uss.rdValue = :ussValue',
                [ussKey: UserSettings.KEYS.IS_NOTIFICATION_FOR_SYSTEM_MESSAGES, ussValue: RDStore.YN_YES]
        )
    }

    boolean publish() {
        List<User> reps = SystemAnnouncement.getRecipients()

        try {
            lastPublishingDate = new Date()
            isPublished = true
            save()
        }
        catch (Exception e) {
            SystemEvent.createEvent('SYSANN_SENDING_ERROR', ['count': reps.size()])
            return false
        }

        SystemEvent.createEvent('SYSANN_SENDING_OK', ['count': reps.size()])
        return true
    }
}
