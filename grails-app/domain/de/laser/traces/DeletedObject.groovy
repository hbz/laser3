package de.laser.traces

import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.exceptions.CreationException

/**
 * This is a placeholder for an object completely removed from the LAS:eR database. It serves for communication to
 * systems using the API to notify them about a deletion of a certain object. Access rights are being handled via
 * {@link DelCombo} linking
 * @see DelCombo
 */
class DeletedObject {

    Long oldDatabaseID
    String oldName
    String oldCalculatedType
    String oldObjectType
    String oldGlobalUID
    String oldGokbID
    Date oldStartDate
    Date oldEndDate
    String referenceSubscriptionUID
    String referencePackageWekbID
    String referenceTitleWekbID
    Date dateCreated
    Date lastUpdated
    Date oldDateCreated
    Date oldLastUpdated
    Set combos

    static mapping = {
        datasource                          'storage'
        id                                  column: 'do_id'
        version                             column: 'do_version'
        oldDatabaseID                       column: 'do_old_database_id', index: 'do_old_database_idx, do_old_db_id_obj_idx'
        oldName                             column: 'do_old_name', type: 'text'
        oldCalculatedType                   column: 'do_old_calculated_type'
        oldObjectType                       column: 'do_old_object_type', index: 'do_old_object_type_idx, do_old_db_id_obj_idx'
        oldGlobalUID                        column: 'do_old_global_uid', index: 'do_old_global_idx'
        oldGokbID                           column: 'do_old_gokb_id', index: 'do_old_gokb_idx'
        oldStartDate                        column: 'do_old_start_date'
        oldEndDate                          column: 'do_old_end_date'
        referenceSubscriptionUID            column: 'do_ref_subscription_uid', index: 'do_ref_subscription_idx'
        referencePackageWekbID              column: 'do_ref_package_wekb_id', index: 'do_ref_package_wekb_idx'
        referenceTitleWekbID                column: 'do_ref_title_wekb_id', index: 'do_ref_title_wekb_idx'
        dateCreated                         column: 'do_date_created'
        oldDateCreated                      column: 'do_old_date_created'
        lastUpdated                         column: 'do_last_updated'
        oldLastUpdated                      column: 'do_old_last_updated'
    }

    static hasMany = [
        combos: DelCombo
    ]

    static mappedBy = [
        combos: 'delObjTrace'
    ]

    static constraints = {
        oldGlobalUID (nullable: true)
        oldName (nullable: true, blank: false)
        oldCalculatedType (nullable: true, blank: false)
        oldStartDate (nullable: true)
        oldEndDate (nullable: true)
        oldGokbID (nullable: true)
        referenceSubscriptionUID (nullable: true)
        referencePackageWekbID (nullable: true)
        referenceTitleWekbID (nullable: true)
    }

    static Map<String, String> nonMandatoryFields = [name: 'oldName',
                                                     reference: 'oldName',
                                                     startDate: 'oldStartDate',
                                                     endDate: 'oldEndDate',
                                                     globalUID: 'oldGlobalUID',
                                                     gokbId: 'oldGokbID']

    /**
     * Sets up a new trace incl. the institutions having read rights
     * @param delObj the object being deleted, containing parameters
     * @param delRelations the institutions (global UIDs of {@link de.laser.Org}s) with granted access rights
     * @return the object trace
     * @throws CreationException
     */
    static DeletedObject construct(delObj, Set<String> delRelations) throws CreationException {
        DeletedObject trace = construct(delObj)
        delRelations.each { String globalUID ->
            DelCombo delCombo = new DelCombo(delObjTrace: trace, accessibleOrg: globalUID)
            if(!delCombo.save())
                throw new CreationException(delCombo.getErrors().getAllErrors())
        }
        trace
    }

    /**
     * Sets up a new trace, inserting basic information
     * @param delObj the object parameters being stored, those are:
     * <ul>
     *     <li>old database ID</li>
     *     <li>old object type</li>
     *     <li>creation date of the deleted object</li>
     *     <li>date when the object has been last updated</li>
     * </ul>
     * Moreover, depending on the object type:
     * <ul>
     *     <li>if {@link Subscription} or {@link License}: the {@link de.laser.interfaces.CalculatedType}</li>
     *     <li>if {@link TitleInstancePackagePlatform}: the we:kb ID</li>
     *     <li>if {@link IssueEntitlement}: the we:kb ID of the title and of the package and the subscription global UID</li>
     * </ul>
     * @return the object trace
     * @throws CreationException
     */
    static DeletedObject construct(delObj) throws CreationException {
        DeletedObject trace = new DeletedObject(oldDatabaseID: delObj.id, oldObjectType: delObj.class.name, oldDateCreated: delObj.dateCreated, oldLastUpdated: delObj.lastUpdated)
        nonMandatoryFields.each { String traceField, String mappingField ->
            if(delObj.hasProperty(traceField))
                trace[mappingField] = delObj[traceField]
        }
        if(delObj instanceof Subscription || delObj instanceof License) {
            trace.oldCalculatedType = delObj._getCalculatedType()
        }
        if(delObj instanceof TitleInstancePackagePlatform)
            trace.referencePackageWekbID = delObj.pkg.gokbId
        else if(delObj instanceof IssueEntitlement) {
            trace.referencePackageWekbID = delObj.tipp.pkg.gokbId
            trace.referenceSubscriptionUID = delObj.subscription.globalUID
            trace.referenceTitleWekbID = delObj.tipp.gokbId
        }

        if(!trace.save()) {
            throw new CreationException(trace.getErrors().getAllErrors())
        }
        else trace
    }
}
