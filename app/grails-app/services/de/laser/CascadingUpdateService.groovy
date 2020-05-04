package de.laser

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IdentifierNamespace
import com.k_int.kbplus.Subscription
import grails.transaction.Transactional

@Transactional
class CascadingUpdateService {

    def grailsApplication

    def update(IdentifierNamespace obj, Date luc) {
        toLog(obj, luc)

        IdentifierNamespace.executeUpdate("update IdentifierNamespace ns set ns.lastUpdatedCascading = :luc where ns = :obj", [
                luc: luc, obj: obj
        ])

        Identifier.findAllByNs(obj).each{ i -> update(i, luc) }
    }

    def update(Identifier obj, Date luc) {
        toLog(obj, luc)

        Identifier.executeUpdate("update Identifier i set i.lastUpdatedCascading = :luc where i = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.sub) { update(obj.sub, luc) }

//        lic:    License,
//        org:    Org,
//        pkg:    Package,
//        ti:     TitleInstance,
//        tipp:   TitleInstancePackagePlatform,
//        cre:    Creator
    }

    def update(Subscription obj, Date luc) {
        toLog(obj, luc)

        Subscription.executeUpdate("update Subscription sub set sub.lastUpdatedCascading = :luc where sub = :obj", [
                luc: luc, obj: obj
        ])
    }

    private void toLog(Object obj, Date luc) {
        log.debug ('cascadingUpdate for ' + obj + ' = [' + obj.cascadingLastUpdated + '] <- [' + luc + ']')
    }
}
