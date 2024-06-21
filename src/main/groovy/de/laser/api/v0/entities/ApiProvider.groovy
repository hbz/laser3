package de.laser.api.v0.entities

import de.laser.*
import de.laser.api.v0.*
import de.laser.storage.Constants
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of an {@link Provider}
 */
class ApiProvider {

    /**
     * Locates the given {@link Provider} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID, gokbId, ns:identifier}
     * @param the identifier value
     * @return {@link ApiBox}(obj: Org | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findProviderBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'ezbId':
                result.obj = Provider.executeQuery('select id.provider from Identifier id where id.value = :id and id.ns.ns = :ezb', [id: value, ezb: IdentifierNamespace.EZB_ORG_ID])
                break
            case 'id':
                result.obj = Provider.findAllById(Long.parseLong(value))
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldDatabaseIDAndOldObjectType(Long.parseLong(value), Provider.class.name)
                    }
                }
                break
            case 'globalUID':
                result.obj = Provider.findAllByGlobalUID(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGlobalUID(value)
                    }
                }
                break
            case 'gokbId':
                result.obj = Provider.findAllByGokbId(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGokbID(value)
                    }
                }
                break
            case 'ns:identifier':
                result.obj = Identifier.lookupObjectsByIdentifierString(new Provider(), value)
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
}
