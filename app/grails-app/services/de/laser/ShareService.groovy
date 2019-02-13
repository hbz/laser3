package de.laser

import com.k_int.kbplus.DocContext
import com.k_int.kbplus.License
import com.k_int.kbplus.Links
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import de.laser.interfaces.ShareSupport
import de.laser.traits.ShareableTrait
import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.transform.CompileStatic
import org.codehaus.groovy.grails.commons.GrailsApplication

//@CompileStatic
class ShareService {

    GrailsApplication grailsApplication

    boolean addDocShareForTarget(DocContext share, ShareSupport target) {

        if (share.sharedFrom) {
            return false
        }
        // todo check existence

        DocContext clonedShare = new DocContext(
                owner:          share.owner ,
                license:        (target instanceof License) ? target : share.license,
                subscription:   (target instanceof Subscription) ? target : share.subscription,
                pkg:            (target instanceof Package) ? target : share.pkg,
                link:           (target instanceof Links) ? target : share.link,
                doctype:        share.doctype,
                domain:         share.domain,
                globannounce:   share.globannounce,
                sharedFrom:     share,
                isShared:       false
        )
        if (clonedShare.save(flush: true)) {
            if (! share.isShared) {
                share.isShared = true
                if (share.save(flush: true)) {
                    return true
                }
            }
        }
        return false
    }

    boolean deleteDocShareForTarget(DocContext share, ShareSupport target) {

        String tp =
                (target instanceof License) ? 'license' :
                        (target instanceof Subscription) ? 'subscription' :
                                (target instanceof Package) ? 'pkg' :
                                        (target instanceof Links) ? 'link' :
                                                null

        if (tp) {
            DocContext.executeUpdate('delete from DocContext dc where dc.sharedFrom = :sf and ' + tp + ' = :target', [sf: this, target: target])
            return true
        }
        return false
    }
}
