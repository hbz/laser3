package de.laser.oap

import de.laser.wekb.Platform
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
        laserID (nullable:true, blank:false, unique:true, maxSize:255)
        platform(nullable:true)
        oap     (nullable:true) //intentional, null used in program logic
        subPkg  (nullable:true) //intentional, null used in program logic
    }

    static mapping = {
        id              column:'oapl_id'
        active          column:'oapl_active'
        laserID         column:'oapl_guid'
        version         column:'oapl_version'
        oap             column:'oapl_oap_fk'
        platform        column:'oapl_platform_fk'
        subPkg          column:'oapl_sub_pkg_fk'
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
