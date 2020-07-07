package de.laser

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.IdentifierNamespace
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.Person
import com.k_int.kbplus.Platform
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SurveyResult
import com.k_int.kbplus.TitleInstance
import com.k_int.kbplus.abstract_domain.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import grails.transaction.Transactional

@Transactional
class CascadingUpdateService {

    def grailsApplication

    def changeNotificationService
    def auditService

    void update(IdentifierNamespace obj, Date luc) {
        log(obj, luc)

        IdentifierNamespace.executeUpdate("update IdentifierNamespace ns set ns.lastUpdatedCascading = :luc where ns = :obj", [
                luc: luc, obj: obj
        ])

        Identifier.findAllByNs(obj).each{ i -> update(i, luc) }
    }

    void update(Identifier obj, Date luc) {
        log(obj, luc)

        Identifier.executeUpdate("update Identifier i set i.lastUpdatedCascading = :luc where i = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.lic) { update(obj.lic, luc) }
        if (obj.org) { update(obj.org, luc) }
        if (obj.pkg) { update(obj.pkg, luc) }
        if (obj.sub) { update(obj.sub, luc) }
        if (obj.ti)  { update(obj.ti,  luc) }

        //        tipp:   TitleInstancePackagePlatform
        //        cre:    Creator
    }

    void update(AbstractPropertyWithCalculatedLastUpdated obj, Date luc) {
        log(obj, luc)

        obj.class.executeUpdate("update ${obj.class.simpleName} cp set cp.lastUpdatedCascading = :luc where cp = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.owner) {
            update(obj.owner, luc)
        }
    }

    void update(CustomProperty obj, Date luc) {
        log(obj, luc)

        obj.class.executeUpdate("update ${obj.class.simpleName} cp set cp.lastUpdatedCascading = :luc where cp = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.owner) {
            update(obj.owner, luc)
        }
    }

    void update(PrivateProperty obj, Date luc) {
        log(obj, luc)

        obj.class.executeUpdate("update ${obj.class.simpleName} pp set pp.lastUpdatedCascading = :luc where pp = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.owner) {
            update(obj.owner, luc)
        }
    }

    void update(License obj, Date luc) {
        log(obj, luc)

        License.executeUpdate("update License lic set lic.lastUpdatedCascading = :luc where lic = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(Org obj, Date luc) {
        log(obj, luc)

        Org.executeUpdate("update Org o set o.lastUpdatedCascading = :luc where o = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(Package obj, Date luc) {
        log(obj, luc)

        Package.executeUpdate("update Package pkg set pkg.lastUpdatedCascading = :luc where pkg = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(Person obj, Date luc) {
        log(obj, luc)

        Person.executeUpdate("update Person p set p.lastUpdatedCascading = :luc where p = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(Platform obj, Date luc) {
        log(obj, luc)

        Platform.executeUpdate("update Platform p set p.lastUpdatedCascading = :luc where p = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(Subscription obj, Date luc) {
        log(obj, luc)

        Subscription.executeUpdate("update Subscription sub set sub.lastUpdatedCascading = :luc where sub = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(SurveyResult obj, Date luc) {
        log(obj, luc)

        SurveyResult.executeUpdate("update SurveyResult sr set sr.lastUpdatedCascading = :luc where sr = :obj", [
                luc: luc, obj: obj
        ])
    }

    void update(TitleInstance obj, Date luc) {
        log(obj, luc)

        TitleInstance.executeUpdate("update TitleInstance ti set ti.lastUpdatedCascading = :luc where ti = :obj", [
                luc: luc, obj: obj
        ])
    }

    private void log(Object obj, Date luc) {
        log.debug ('cascading update for ' + obj.class.simpleName + ':' + obj.id + ' -> [' + obj.lastUpdatedCascading + '] set to [' + luc + ']')
    }
}
