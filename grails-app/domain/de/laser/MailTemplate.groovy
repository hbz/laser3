package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants

/**
 * This class represents a mail template element for the (semi-)automatised sending of mails ex LAS:eR.
 * The sending procedure is controlled by the flag sentBySystem
 */
class MailTemplate {

    String name
    String text
    Org owner
    String subject

    boolean sentBySystem = false

    @RefdataInfo(cat = RDConstants.MAIL_TEMPLATE_TYPE)
    RefdataValue type
    @RefdataInfo(cat = RDConstants.MAIL_TEMPLATE_LANGUAGE)
    RefdataValue language

    Date dateCreated
    Date lastUpdated

    static constraints = {
        owner   (nullable: true)
        text    (nullable: true, blank: false)
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
