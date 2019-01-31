package com.k_int.kbplus

import com.k_int.kbplus.auth.User

class SystemTicket {

    User author
    String title

    String described
    String expected
    String info
    String meta

    RefdataValue status             // RefdataCategory 'Ticket.Status'
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
        author      (nullable:false, blank:false)
        title       (nullable:false, blank:false)
        described   (nullable:true, blank:true)
        expected    (nullable:true, blank:true)
        info        (nullable:true, blank:true)
        meta        (nullable:false, blank:true)
        status      (nullable:false, blank:false)
        category    (nullable:false, blank:false)
        dateCreated (nullable:false, blank:false)
        lastUpdated (nullable:false, blank:false)
        jiraReference(nullable:true, blank:true)
    }

    static getNew() {

        SystemTicket.where{ status == RefdataValue.getByValueAndCategory('New', 'Ticket.Status') }.list(sort:'dateCreated', order:'desc')
    }
}
