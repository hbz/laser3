package de.laser

import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.survey.SurveyResult

/**
 * This service updates timestamps of depending objects if an object update has been triggered. This is essential for
 * API output since harvesters pay attention on the last update of an superordinate object - which is then updated, too
 */
//@Transactional needed to be deactivated, cf. https://stackoverflow.com/a/57656283 - two transactional annotated methods may enter in conflict
class CascadingUpdateService {

    /**
     * Updates all identifier time stamps belonging to the updated namespace and the namespace timestamp itself
     * @param obj the triggering identifier namespace
     * @param luc the timestamp of update
     */
    void update(IdentifierNamespace obj, Date luc) {
        _log(obj, luc)

        IdentifierNamespace.executeUpdate("update IdentifierNamespace ns set ns.lastUpdatedCascading = :luc where ns = :obj", [
                luc: luc, obj: obj
        ])

        Identifier.findAllByNs(obj).each{ i -> update(i, luc) }
    }

    /**
     * Updates the superordinate timestamp of the given identifier and the identifier timestamp itself
     * @param obj the triggering identifier
     * @param luc the timestamp of update
     */
    void update(Identifier obj, Date luc) {
        _log(obj, luc)

        Identifier.executeUpdate("update Identifier i set i.lastUpdatedCascading = :luc where i = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.lic) { update(obj.lic, luc) }
        if (obj.org) { update(obj.org, luc) }
        if (obj.pkg) { update(obj.pkg, luc) }
        if (obj.sub) { update(obj.sub, luc) }
        //if (obj.ti)  { update(obj.ti,  luc) }

        //        tipp:   TitleInstancePackagePlatform
    }

    /**
     * Updates the given Dewey decimal classification and the superordinate package timestamps
     * @param obj the updated Dewey decimal classification
     * @param luc the timestamp of update
     */
    void update(DeweyDecimalClassification obj, Date luc) {
        _log(obj, luc)

        DeweyDecimalClassification.executeUpdate("update DeweyDecimalClassification ddc set ddc.lastUpdatedCascading = :luc where ddc = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.pkg) { update(obj.pkg, luc) }
        //if (obj.ti)  { update(obj.ti,  luc) }

        //        tipp:   TitleInstancePackagePlatform
    }

    /**
     * Updates the given language record and the superordinate package timestamps
     * @param obj the updated language
     * @param luc the timestamp of update
     */
    void update(Language obj, Date luc) {
        _log(obj, luc)

        Language.executeUpdate("update Language lang set lang.lastUpdatedCascading = :luc where lang = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.pkg) { update(obj.pkg, luc) }
        //if (obj.ti)  { update(obj.ti,  luc) }

        //        tipp:   TitleInstancePackagePlatform
    }

    /**
     * Updates the given alternative name record and the superordinate object's timestamps
     * @param obj the updated alternative name
     * @param luc the timestamp of update
     */
    void update(AlternativeName obj, Date luc) {
        _log(obj, luc)

        AlternativeName.executeUpdate("update AlternativeName altName set altName.lastUpdatedCascading = :luc where altName = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.pkg)      { update(obj.pkg, luc) }
      //if (obj.tipp)     { update(obj.tipp, luc) }
        if (obj.platform) { update(obj.platform, luc) }
        if (obj.org)      { update(obj.org, luc) }
    }

    /**
     * Updates the given property and the owner object timestamps
     * @param obj the updated property
     * @param luc the timestamp of update
     */
    void update(AbstractPropertyWithCalculatedLastUpdated obj, Date luc) {
        _log(obj, luc)

        obj.class.executeUpdate("update " + obj.class.simpleName + " prop set prop.lastUpdatedCascading = :luc where prop = :obj", [
                luc: luc, obj: obj
        ])

        if (obj.owner) { update(obj.owner, luc) }
    }

    /**
     * Updates the given license timestamp
     * @param obj the updated license
     * @param luc the timestamp of update
     */
    void update(License obj, Date luc) {
        _log(obj, luc)

        License.executeUpdate("update License lic set lic.lastUpdatedCascading = :luc where lic = :obj", [
                luc: luc, obj: obj
        ])
    }

    /**
     * Updates the given organisation timestamp
     * @param obj the updated organisation
     * @param luc the timestamp of update
     */
    void update(Org obj, Date luc) {
        _log(obj, luc)

        Org.executeUpdate("update Org o set o.lastUpdatedCascading = :luc where o = :obj", [
                luc: luc, obj: obj
        ])
    }

    /**
     * Updates the given package timestamp
     * @param obj the updated package
     * @param luc the timestamp of update
     */
    void update(Package obj, Date luc) {
        _log(obj, luc)

        Package.executeUpdate("update Package pkg set pkg.lastUpdatedCascading = :luc where pkg = :obj", [
                luc: luc, obj: obj
        ])
    }

    /**
     * Updates the given person contact timestamp
     * @param obj the updated person contact
     * @param luc the timestamp of update
     */
    void update(Person obj, Date luc) {
        _log(obj, luc)

        Person.executeUpdate("update Person p set p.lastUpdatedCascading = :luc where p = :obj", [
                luc: luc, obj: obj
        ])
    }

    /**
     * Updates the given platform timestamp
     * @param obj the updated platform
     * @param luc the timestamp of update
     */
    void update(Platform obj, Date luc) {
        _log(obj, luc)

        Platform.executeUpdate("update Platform p set p.lastUpdatedCascading = :luc where p = :obj", [
                luc: luc, obj: obj
        ])
    }

    /**
     * Updates the given subscription timestamp
     * @param obj the updated subscription
     * @param luc the timestamp of update
     */
    void update(Subscription obj, Date luc) {
        _log(obj, luc)

        Subscription.executeUpdate("update Subscription sub set sub.lastUpdatedCascading = :luc where sub = :obj", [
                luc: luc, obj: obj
        ])
    }

    /**
     * Updates the given license survey property
     * @param obj the updated survey property
     * @param luc the timestamp of update
     */
    void update(SurveyResult obj, Date luc) {
        _log(obj, luc)

        SurveyResult.executeUpdate("update SurveyResult sr set sr.lastUpdatedCascading = :luc where sr = :obj", [
                luc: luc, obj: obj
        ])
    }

    /*void update(TitleInstancePackagePlatform obj, Date luc) {
        log(obj, luc)
        TitleInstancePackagePlatform.executeUpdate("update TitleInstancePackagePlatform tipp set tipp.lastUpdatedCascading = :luc where tipp = :obj", [
                luc: luc, obj: obj
        ])
    }*/

    /**
     * Logs the timestamp update
     * @param obj the object which has been updated
     * @param luc the timestamp of update
     */
    private void _log(Object obj, Date luc) {
        log.debug ('cascading update for ' + obj.class.simpleName + ':' + obj.id + ' -> [' + obj.lastUpdatedCascading + '] set to [' + luc + ']')
    }
}
