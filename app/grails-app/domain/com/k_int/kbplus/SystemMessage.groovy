package com.k_int.kbplus

class SystemMessage {


    Org org
    String text
    boolean showNow = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id column: 'sm_id'
        version column: 'sm_version'

        org column: 'sm_org_fk'
        text column: 'sm_text'
        showNow column: 'sm_show_now'
        dateCreated column: 'sm_date_created'
        lastUpdated column: 'sm_last_updated'
    }

    static constraints = {
        org (nullable:true,  blank:true)
    }
}
