package de.laser.api.v0.entities

import de.laser.Identifier
import de.laser.Org
import de.laser.Platform
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.storage.RDStore
import grails.converters.JSON
import groovy.util.logging.Slf4j

/**
 * An API representation of a {@link Platform}
 */
@Slf4j
class ApiPlatform {

    /**
     * Locates the given {@link de.laser.Platform} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID, gokbId, ns:identifier}
     * @param the identifier value
     * @return {@link ApiBox}(obj: Platform | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findPlatformBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
                result.obj = Platform.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result.obj = Platform.findAllWhere(globalUID: value)
                break
            case 'gokbId':
                result.obj = Platform.findAllWhere(gokbId: value)
                break
            case 'ns:identifier':
                result.obj = Identifier.lookupObjectsByIdentifierString(new Platform(), value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        println RDStore.PLATFORM_STATUS_DELETED.id
        if (result.obj instanceof Platform) {
            result.validateDeletedStatus_2('status', RDStore.PLATFORM_STATUS_DELETED)
        }
        result
    }

    /**
     * Retrieves a list of platforms recorded in the app.
     * NOTE: if the app is connected to a we:kb/GOKb source, use the we:kb/GOKb source instead as the data in this app
     * is mirrored and arrives only with delay!
     * @return JSON
     */
    static JSON getPlatformList() {
        Collection<Object> result = []

        List<Platform> pfs = Platform.executeQuery('select pf from Platform pf')
        pfs.each { pf ->
            result << ApiUnsecuredMapReader.getPlatformStubMap(pf)
        }

        return result ? new JSON(result) : null
    }

    /**
     * Retrieves details of the given platform for the requesting institution
     * @param pform the {@link Platform} whose details should be retrieved
     * @param context the institution ({@link Org})
     * @return JSON
     */
    static getPlatform(Platform pform, Org context) {
        Map<String, Object> result = [:]

        result = ApiUnsecuredMapReader.getPlatformMap(pform, context)

        return result ? new JSON(result) : null
    }
}
