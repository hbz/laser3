package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomain

class OrgAccessPointLink extends AbstractBaseDomain{

    OrgAccessPoint oap
    Platform platform
    Subscription subscription
    Boolean active
    Date dateCreated
    Date lastUpdated

    static belongsTo = [
        oap:OrgAccessPoint,
        platform:Platform,
        subscription:Subscription
    ]

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        subscription(nullable:true)
        platform(nullable:true)
        oap(nullable:false, blank:false)
    }

}
