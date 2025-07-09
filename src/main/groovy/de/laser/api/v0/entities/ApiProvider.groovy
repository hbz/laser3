package de.laser.api.v0.entities

import de.laser.*
import de.laser.api.v0.*
import de.laser.storage.Constants
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import de.laser.wekb.Provider
import grails.converters.JSON

/**
 * An API representation of an {@link de.laser.wekb.Provider}
 */
class ApiProvider {

    /**
     * Locates the given {@link de.laser.wekb.Provider} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID, gokbId, ns:identifier}
     * @param the identifier value
     * @return {@link ApiBox}(obj: Provider | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findProviderBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'ezbId':
                result.obj = Provider.executeQuery('select id.provider from Identifier id where id.value = :id and id.ns.ns = :ezb', [id: value, ezb: IdentifierNamespace.EZB_ORG_ID])
                break
            case 'id':
                result.obj = Provider.get(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldDatabaseIDAndOldObjectType(Long.parseLong(value), Provider.class.name)
                    }
                }
                break
            case 'globalUID':
                result.obj = Provider.findByGlobalUID(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGlobalUID(value)
                    }
                }
                break
            case 'wekbId':
                result.obj = Provider.findByGokbId(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGokbID(value)
                    }
                }
                break
            case 'ns:identifier':
                result.obj = Identifier.lookupObjectsByIdentifierString(Provider.class.getSimpleName(), value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        if (result.obj instanceof Provider) {
            result.validateDeletedStatus_2('status', RDStore.PROVIDER_STATUS_DELETED)
        }
        result
    }

    /**
     * Retrieves a list of providers recorded in the app
     * @return JSON
     */
    static JSON getProviderList() {
        Collection<Object> result = []

        List<Provider> providers = Provider.executeQuery('select p from Provider p')
        providers.each { Provider p ->
            result << ApiUnsecuredMapReader.getProviderStubMap(p)
        }

        return result ? new JSON(result) : null
    }

    /**
     * Retrieves details of the given provider for the requesting institution
     * @param provider the {@link Provider} whose details should be retrieved
     * @param context the institution ({@link Org})
     * @return JSON
     */
    static getProvider(Provider provider, Org context) {
        Map<String, Object> result = [:]

        result = ApiUnsecuredMapReader.getProviderMap(provider, context)

        return result ? new JSON(result) : null
    }
}
