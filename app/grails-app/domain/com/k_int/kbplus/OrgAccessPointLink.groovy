package com.k_int.kbplus

import de.laser.domain.BaseDomainComponent

class OrgAccessPointLink extends BaseDomainComponent{

    OrgAccessPoint oap
    Platform platform
    License license
    Boolean active
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        oap:OrgAccessPoint,
        platform:Platform,
        license:License
    ]

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        license(nullable:true)
        platform(nullable:true)
        oap(nullable:false, blank:false)
    }

}
