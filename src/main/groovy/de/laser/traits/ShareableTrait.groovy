package de.laser.traits

import de.laser.DocContext
import de.laser.OrgRole
import de.laser.interfaces.ShareSupport
import de.laser.storage.BeanStore

import javax.persistence.Transient

/**
 * This trait is being attached to each object class which can be inherited to member objects.
 * See {@link de.laser.Subscription} for the intellectual background of the inheritance concept
 */
trait ShareableTrait {

    // static belongsTo = [ sharedFrom: ShareableTrait_IMPL ]

    // ShareableTrait_IMPL sharedFrom
    // Boolean isShared

    // static mapping { sharedFrom column:'dc_shared_from_fk'; isShared column:'dc_is_shared' }

    // static constraints = { sharedFrom(nullable:true, blank:true); isShared(nullable:true, blank:false, default:false) }

    /**
     * Sets the sharing flag for the given target object
     * @param target the {@link DocContext} or {@link OrgRole} which should be shared with members
     */
    @Transient
    def addShareForTarget_trait(ShareSupport target) {
        log?.debug ("addShareForTarget " + this + " for " + target)

        if (this instanceof DocContext) {
            BeanStore.getShareService().addDocShareForTarget(this, target)
        }
        if (this instanceof OrgRole) {
            BeanStore.getShareService().addOrgRoleShareForTarget(this, target)
        }
    }

    /**
     * Unsets the sharing flag from the given target object
     * @param target the {@link DocContext} or {@link OrgRole} whose sharing with members should be abandoned
     */
    @Transient
    def deleteShareForTarget_trait(ShareSupport target) {
        log?.debug ("deleteShareForTarget " + this + " for " + target)

        if (this instanceof DocContext) {
            BeanStore.getShareService().deleteDocShareForTarget(this, target)
        }
        if (this instanceof OrgRole) {
            BeanStore.getShareService().deleteOrgRoleShareForTarget(this, target)
        }
    }

    /**
     * Removes sharing links from the given {@link DocContext} or {@link OrgRole}
     */
    @Transient
    def deleteShare_trait() {
        log?.debug ("deleteAllShares where x.sharedFrom = " + this)

        if (this instanceof DocContext) {
            DocContext.executeUpdate('delete from DocContext dc where dc.sharedFrom = :sf', [sf: this])
        }
        if (this instanceof OrgRole) {
            OrgRole.executeUpdate('delete from OrgRole oorr where oorr.sharedFrom = :sf', [sf: this])
        }
    }
}
