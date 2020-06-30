package com.k_int.kbplus

import de.laser.base.AbstractBaseDomain

class OrgAccessPointLink extends AbstractBaseDomain{

    OrgAccessPoint oap
    Platform platform
    SubscriptionPackage subPkg
    Boolean active = false
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        oap:OrgAccessPoint,
        platform:Platform,
        subPkg:SubscriptionPackage
    ]

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        platform(nullable:true)
        oap(nullable:true, blank:false) //intentional, null used in program logic
        subPkg(nullable:true, blank:false) //intentional, null used in program logic
    }

}
