package de.laser.system

import de.laser.auth.User
import de.laser.RefdataValue
import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation

/**
 * This class was used to record bug notifications and user remarks. The underlying workflow is currently not used
 */
class SystemTicket {

    User author
    String title

    String described
    String expected
    String info
    String meta

    @RefdataAnnotation(cat = RDConstants.TICKET_STATUS)
    RefdataValue status

    @RefdataAnnotation(cat = RDConstants.TICKET_CATEGORY)
    RefdataValue category

    Date dateCreated
    Date lastUpdated

    String jiraReference

    static mapping = {
        id          column:'sti_id'
        version     column:'sti_version'
        author      column:'sti_user_fk'
        title       column:'sti_title'
        described   column:'sti_described',     type: 'text'
        expected    column:'sti_expected',      type: 'text'
        info        column:'sti_info',          type: 'text'
        meta        column:'sti_meta',          type: 'text'
        status      column:'sti_status_rv_fk'
        category    column:'sti_category_rv_fk'
        dateCreated column:'sti_created'
        lastUpdated column:'sti_modified'
        jiraReference    column:'sti_jira'
    }

    static constraints = {
        author       (blank:false)
        title        (blank:false)
        described   (nullable:true, blank:true)
        expected    (nullable:true, blank:true)
        info        (nullable:true, blank:true)
        meta         (blank:true)
        status       (blank:false)
        category     (blank:false)
        jiraReference(nullable:true, blank:true)
    }

    /**
     * Lists the most recent tickets which have not been dealt with (= status is New)
     * @return a sorted {@link List} of tickets
     */
    static List<SystemTicket> getNew() {
        SystemTicket.where{ status == RefdataValue.getByValueAndCategory('New', RDConstants.TICKET_STATUS) }.list(sort:'dateCreated', order:'desc')
    }
}
