package de.laser

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IdentifierNamespace
import com.k_int.kbplus.Subscription
import grails.transaction.Transactional

@Transactional
class CascadingUpdateService {

    def grailsApplication

    def cascadingUpdate(IdentifierNamespace obj, Date clu) {
        logDebug(obj, clu)

        IdentifierNamespace.executeUpdate("update IdentifierNamespace ns set ns.cascadingLastUpdated = :clu where ns = :obj", [
                clu: clu, obj: obj
        ])

        Identifier.findAllByNs(obj).each{ i -> cascadingUpdate(i, clu) }
    }

    def cascadingUpdate(Identifier obj, Date clu) {
        logDebug(obj, clu)

        Identifier.executeUpdate("update Identifier i set i.cascadingLastUpdated = :clu where i = :obj", [
                clu: clu, obj: obj
        ])

        if (obj.sub) { cascadingUpdate(obj.sub, clu) }

//        lic:    License,
//        org:    Org,
//        pkg:    Package,
//        ti:     TitleInstance,
//        tipp:   TitleInstancePackagePlatform,
//        cre:    Creator
    }

    def cascadingUpdate(Subscription obj, Date clu) {
        logDebug(obj, clu)

        Subscription.executeUpdate("update Subscription sub set sub.cascadingLastUpdated = :clu where sub = :obj", [
                clu: clu, obj: obj
        ])
    }

    private void logDebug(Object obj, Date clu) {
        log.debug ('cascadingUpdate for ' + obj + ' = ' + obj.cascadingLastUpdated + ' <- ' + clu)
    }
}
