package com.k_int.kbplus.api.v0

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.base.OutHelperService
import com.k_int.kbplus.api.v0.base.OutService
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class LicenseService {

    OutService outService

    /**
     * @return License | BAD_REQUEST | PRECONDITION_FAILED
     */
    def findLicenseBy(String query, String value) {
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
    def getLicense(License lic, User user, Org context){
        def result = []
        def hasAccess = outService.isDataManager(user)

        if (! hasAccess) {
            lic.orgLinks.each { orgRole ->
                if (orgRole.getOrg().id == context?.id) {
                    hasAccess = true
                }
            }
        }

        if (hasAccess) {
            result = outService.exportLicense(lic, OutHelperService.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
