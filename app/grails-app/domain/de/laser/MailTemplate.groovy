package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue

class MailTemplate {

    String name
    String text

    Org owner
    String subject

    boolean sentBySystem = false

    RefdataValue type
    RefdataValue language

    Date dateCreated
    Date lastUpdated

    static constraints = {
        owner (nullable: true, blank: false)
        text (nullable: true, blank: false)
        sentBySystem (blank: false)
    }

    static mapping = {
        id column: 'mt_id'
        version column: 'mt_version'

        name column: 'mt_name'
        text column: 'mt_text', type:'text'
        subject column: 'mt_subject'

        sentBySystem column: 'mt_sent_by_system'

        owner column: 'mt_owner_org_fk'

        type column: 'mt_type_rv_fk'
        language column: 'mt_language_rv_fk'

        dateCreated column: 'mt_date_created'
        lastUpdated column: 'mt_last_updated'

    }
}
