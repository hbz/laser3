package de.laser.api.v0

import de.laser.GlobalSourceSyncService
import de.laser.Org
import de.laser.storage.Constants
import de.laser.traces.DelCombo
import de.laser.traces.DeletedObject
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

class ApiDeletedObject {

    static requestDeletedSubscription(DeletedObject delObj, Org context, boolean isInvoiceTool) {
        Map<String, Object> result = [:]

        boolean hasAccess = isInvoiceTool || calculateAccess(delObj, context)
        if(hasAccess) {
            result = getDeletedSubscriptionMap(delObj, context, isInvoiceTool)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    static boolean calculateAccess(DeletedObject delObj, Org context) {

        boolean hasAccess = false

        /*
        deleted trace are being only created for subscriptions with isPublicForApi, cf. DeletionService.deleteSubscription()
        if (! sub.isPublicForApi) {
            hasAccess = false
        }
        */
        DelCombo.withTransaction {
            if (DelCombo.findByDelObjTraceAndAccessibleOrg(delObj, context)) {
                hasAccess = true
            }
        }

        hasAccess
    }

    static Map<String, Object> getDeletedSubscriptionMap(DeletedObject delObj, Org context, boolean isInvoiceTool) {
        Map<String, Object> result = [:]

        DeletedObject.withTransaction {
            delObj = GrailsHibernateUtil.unwrapIfProxy(delObj)
            result.globalUID                = delObj.oldGlobalUID
            result.dateCreated          	= ApiToolkit.formatInternalDate(delObj.dateCreated)
            result.endDate              	= ApiToolkit.formatInternalDate(delObj.oldEndDate)
            result.lastUpdated          	= ApiToolkit.formatInternalDate(delObj.oldLastUpdated)
            result.name                 	= delObj.oldName
            result.startDate            	= ApiToolkit.formatInternalDate(delObj.oldStartDate)
            result.calculatedType       	= delObj.oldCalculatedType
            result.isPublicForApi           = "Yes"
            result.status                   = GlobalSourceSyncService.PERMANENTLY_DELETED
        }

        ApiToolkit.cleanUp(result, true, true)
    }
}
