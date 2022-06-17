package de.laser.oap

import de.laser.Platform
import de.laser.SubscriptionPackage
import de.laser.base.AbstractBase

/**
 * Represents the connection between an organisation access configuration and a platform or a subscription package and indicates that a platform or a subscription package may be conditioned by access conditions.
 * Thos access conditions are specified by the {@link OrgAccessPoint} class.
 */
class OrgAccessPointLink extends AbstractBase {

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
        oap     (nullable:true) //intentional, null used in program logic
        subPkg  (nullable:true) //intentional, null used in program logic
    }

    static mapping = {
        id              column:'oapl_id'
        version         column:'oapl_version'
        dateCreated     column:'oapl_date_created'
        lastUpdated     column:'oapl_last_updated'
    }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }
}
