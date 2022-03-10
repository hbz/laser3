package de.laser.traits

import de.laser.DocContext
import de.laser.OrgRole
import de.laser.interfaces.ShareSupport

import javax.persistence.Transient

trait ShareableTrait {

    // static belongsTo = [ sharedFrom: ShareableTrait_IMPL ]

    // ShareableTrait_IMPL sharedFrom
    // Boolean isShared

    // static mapping { sharedFrom column:'dc_shared_from_fk'; isShared column:'dc_is_shared' }

    // static constraints = { sharedFrom(nullable:true, blank:true); isShared(nullable:true, blank:false, default:false) }

    @Transient
    def addShareForTarget_trait(ShareSupport target) {
        log?.debug ("addShareForTarget " + this + " for " + target)

        if (this instanceof DocContext) {
            shareService.addDocShareForTarget(this, target)
        }
        if (this instanceof OrgRole) {
            shareService.addOrgRoleShareForTarget(this, target)
        }
    }

    @Transient
    def deleteShareForTarget_trait(ShareSupport target) {
        log?.debug ("deleteShareForTarget " + this + " for " + target)

        if (this instanceof DocContext) {
            shareService.deleteDocShareForTarget(this, target)
        }
        if (this instanceof OrgRole) {
            shareService.deleteOrgRoleShareForTarget(this, target)
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
