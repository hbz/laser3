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
        showNow column: 'sm_showNow'
        dateCreated column: 'sm_dateCreated'
        lastUpdated column: 'sm_lastUpdated'
    }

    static constraints = {
        org (nullable:true,  blank:true)
    }
}
