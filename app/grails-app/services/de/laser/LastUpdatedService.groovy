package de.laser

import com.k_int.kbplus.Creator
import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IdentifierNamespace
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.TitleInstance
import com.k_int.kbplus.TitleInstancePackagePlatform
import grails.transaction.Transactional

@Transactional
class LastUpdatedService {

    def grailsApplication
    def sessionFactory

    def cascadingUpdate(Identifier obj) {
        log.debug ('cascadingUpdate() for ' + obj)

        obj.sub.each { ref ->
            log.debug('setting ' + ref.calculatedLastUpdated + ' to ' + obj.lastUpdated + ' for ' + ref)
            ref.calculatedLastUpdated = obj.lastUpdated
            //ref.save()
        }
//        lic:    License,
//        org:    Org,
//        pkg:    Package,
//        sub:    Subscription,
//        ti:     TitleInstance,
//        tipp:   TitleInstancePackagePlatform,
//        cre:    Creator
    }

    def cascadingUpdate(IdentifierNamespace obj) {
        log.debug ('cascadingUpdate() for ' + obj)

        Identifier.executeUpdate("update Identifier i set i.calculatedLastUpdated = :lu where i.ns = :ns", [
                lu: obj.lastUpdated,
                ns: obj
        ])

//        Identifier.findAllByNs(obj).each{ ref ->
//            log.debug('setting ' + ref.calculatedLastUpdated + ' to ' + obj.lastUpdated + ' for ' + ref)
//            ref.calculatedLastUpdated = obj.lastUpdated
//            ref.save()
//        }
    }

    def cascadingUpdate(Subscription obj) {
        log.debug (obj)

    }
}
