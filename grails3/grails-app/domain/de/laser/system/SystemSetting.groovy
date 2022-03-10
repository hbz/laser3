package de.laser.system

/**
 * This class lets configure some system-wide settings (e.g. MaintenanceMode, MailSending). Setting can be changed only by ROLE_YODA users
 */
class SystemSetting {

    static final int CONTENT_TYPE_STRING  = 0
    static final int CONTENT_TYPE_BOOLEAN = 1

    String name
    int tp = CONTENT_TYPE_STRING // 0=string, 1=boolean
    String value

    Date dateCreated
    Date lastUpdated

    static mapping = {
             id column:'set_id'
           name column:'set_name'
             tp column:'set_type'
          value column:'set_value'

        dateCreated column: 'set_date_created'
        lastUpdated column: 'set_last_updated'
    }

    static constraints = {
        name        (blank:false)
        tp          (blank:false)
        value       (nullable:true, blank:true, maxSize:1024)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
}
