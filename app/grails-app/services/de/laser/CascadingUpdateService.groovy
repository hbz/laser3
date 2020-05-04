package de.laser

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IdentifierNamespace
import com.k_int.kbplus.Subscription
import grails.transaction.Transactional

@Transactional
class CascadingUpdateService {

    def grailsApplication

    def update(IdentifierNamespace obj, Date clu) {
        toLog(obj, clu)

        IdentifierNamespace.executeUpdate("update IdentifierNamespace ns set ns.lastUpdatedCascading = :clu where ns = :obj", [
                clu: clu, obj: obj
        ])

        Identifier.findAllByNs(obj).each{ i -> update(i, clu) }
    }

    def update(Identifier obj, Date clu) {
        toLog(obj, clu)

        Identifier.executeUpdate("update Identifier i set i.lastUpdatedCascading = :clu where i = :obj", [
                clu: clu, obj: obj
        ])

        if (obj.sub) { update(obj.sub, clu) }

//        lic:    License,
//        org:    Org,
//        pkg:    Package,
//        ti:     TitleInstance,
//        tipp:   TitleInstancePackagePlatform,
//        cre:    Creator
    }

    def update(Subscription obj, Date clu) {
        toLog(obj, clu)

        Subscription.executeUpdate("update Subscription sub set sub.lastUpdatedCascading = :clu where sub = :obj", [
                clu: clu, obj: obj
        ])
    }

    private void toLog(Object obj, Date clu) {
        log.debug ('cascadingUpdate for ' + obj + ' = ' + obj.cascadingLastUpdated + ' <- ' + clu)
    }
}
