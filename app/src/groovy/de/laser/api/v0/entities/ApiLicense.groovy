package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiLicense {

    /**
     * @return License | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findLicenseBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = License.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = License.findAllWhere(globalUID: value)
                break
            case 'impId':
                result = License.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new License(), value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }
        if (result) {
            result = result.size() == 1 ? result.get(0) : Constants.HTTP_PRECONDITION_FAILED
        }
        result
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    static getLicense(License lic, User user, Org context){
        def result = []
        def hasAccess = ApiReader.isDataManager(user)

        if (! hasAccess) {
            lic.orgLinks.each { orgRole ->
                if (orgRole.getOrg().id == context?.id) {
                    hasAccess = true
                }
            }
        }

        if (hasAccess) {
            result = ApiReader.exportLicense(lic, ApiReaderHelper.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
