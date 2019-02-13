package de.laser.traits

import com.k_int.kbplus.DocContext
import com.k_int.kbplus.License
import com.k_int.kbplus.Links
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import de.laser.interfaces.ShareSupport

import javax.persistence.Transient

trait ShareableTrait {

    // static belongsTo = [ sharedFrom: DocContext ]

    // DocContext sharedFrom
    // Boolean isShared

    // static mapping { sharedFrom column:'dc_shared_from_fk'; isShared column:'dc_is_shared' }

    // static constraints = { sharedFrom(nullable:true, blank:true); isShared(nullable:true, blank:false, default:false) }

    @Transient
    def addShareForTarget_trait(ShareSupport target) {
        log?.debug ("addShareForTarget " + this + " for " + target)

        if (this instanceof DocContext) {
            shareService.addDocShareForTarget(this, target)
        }
    }

    @Transient
    def deleteShareForTarget_trait(ShareSupport target) {
        log?.debug ("deleteShareForTarget " + this + " for " + target)

        if (this instanceof DocContext) {
            shareService.deleteDocShareForTarget(this, target)
        }
    }

    @Transient
    def deleteShare_trait() {
        log?.debug ("deleteAllShares where x.sharedFrom = " + this)

        if (this instanceof DocContext) {
            DocContext.executeUpdate('delete from DocContext dc where dc.sharedFrom = :sf', [sf: this])
        }
    }
}
