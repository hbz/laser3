package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.titles.TitleInstance
import grails.gorm.transactions.Transactional

@Transactional
class CascadingUpdateService {

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
    }

    void update(AbstractPropertyWithCalculatedLastUpdated obj, Date luc) {
        log(obj, luc)

        obj.class.executeUpdate("update " + obj.class.simpleName + " prop set prop.lastUpdatedCascading = :luc where prop = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.owner) { update(obj.owner, luc) }
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
