package com.k_int.kbplus

import de.laser.domain.BaseDomainComponent

class Creator extends BaseDomainComponent{

    String firstname
    String middlename
    String lastname
    IdentifierOccurrence gnd_id

    Date dateCreated
    Date lastUpdated

    static hasMany = [
            title:  CreatorTitle,

    ]

    static mapping = {

        id column: 'cre_id'
        version column: 'cre_version'
        firstname column: 'cre_firstname'
        middlename column:'cre_middlename'
        lastname column:'cre_lastname'
        gnd_id column:'cre_gnd_id_fk'
        globalUID column:'cre_guid'
        lastUpdated column:'cre_lastUpdated'
        dateCreated column:'cre_dateCreated'

    }

    static constraints = {

        firstname   (nullable:true, blank:false);
        middlename  (nullable:true, blank:false);
        gnd_id      (nullable:true, blank:false);
        globalUID   (nullable:true, blank:false, unique:true, maxSize:255)
        title       (nullable:true)

    }
}
