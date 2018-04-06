package com.k_int.kbplus

class CreatorTitle {

    RefdataValue role
    Date dateCreated
    Date lastUpdated

    static mapping = {

        id column:'ct_id'
        version column:'ct_version'
        role column:'ct_role_rv_fk'
        title column:'ct_title_fk'
        creator column:'ct_creator_fk'
        lastUpdated column:'ct_lastUpdated'
        dateCreated column:'ct_dateCreated'

    }

    static belongsTo = [
            title:  TitleInstance,
            creator:    Creator
    ]

    static constraints = {

    }

}
