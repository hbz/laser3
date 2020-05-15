package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.Platform
import de.laser.api.v0.ApiBox
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.api.v0.ApiUnsecuredMapReader
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiPlatform {

    /**
     * @return ApiBox(obj: Org | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
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
     *
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
     * @return JSON
     */
    static getPlatform(Platform pform, Org context) {
        Map<String, Object> result = [:]

        result = ApiUnsecuredMapReader.getPlatformMap(pform, context)

        return result ? new JSON(result) : null
    }
}
