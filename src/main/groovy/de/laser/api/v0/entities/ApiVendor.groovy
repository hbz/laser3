package de.laser.api.v0.entities


import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.Org
import de.laser.wekb.Vendor
import de.laser.api.v0.ApiBox
import de.laser.api.v0.ApiUnsecuredMapReader
import de.laser.storage.Constants
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import grails.converters.JSON

/**
 * An API representation of an {@link Vendor}
 */
class ApiVendor {

    /**
     * Locates the given {@link Vendor} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, laserID, gokbId, ns:identifier}
     * @param the identifier value
     * @return {@link ApiBox}(obj: Vendor | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findVendorBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'ezbId':
                result.obj = Vendor.executeQuery('select id.vendor from Identifier id where id.value = :id and id.ns.ns = :ezb', [id: value, ezb: IdentifierNamespace.EZB_ORG_ID])
                break
            case 'id':
                result.obj = Vendor.findAllById(Long.parseLong(value))
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldDatabaseIDAndOldObjectType(Long.parseLong(value), Vendor.class.name)
                    }
                }
                break
            case 'laserID':
                result.obj = Vendor.findAllByLaserID(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldLaserID(value)
                    }
                }
                break
            case 'gokbId':
                result.obj = Vendor.findAllByGokbId(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGokbID(value)
                    }
                }
                break
            case 'ns:identifier':
                result.obj = Identifier.lookupObjectsByIdentifierString(new Vendor(), value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        if (result.obj instanceof Vendor) {
            result.validateDeletedStatus_2('status', RDStore.PROVIDER_STATUS_DELETED)
        }
        result
    }

    /**
     * Retrieves a list of vendors recorded in the app
     * @return JSON
     */
    static JSON getVendorList() {
        Collection<Object> result = []

        List<Vendor> vendors = Vendor.executeQuery('select v from Vendor v')
        vendors.each { Vendor v ->
            result << ApiUnsecuredMapReader.getVendorStubMap(v)
        }

        return result ? new JSON(result) : null
    }

    /**
     * Retrieves details of the given provider for the requesting institution
     * @param vendor the {@link Vendor} whose details should be retrieved
     * @param context the institution ({@link Org})
     * @return JSON
     */
    static getVendor(Vendor vendor, Org context) {
        Map<String, Object> result = [:]

        result = ApiUnsecuredMapReader.getVendorMap(vendor, context)

        return result ? new JSON(result) : null
    }
}
