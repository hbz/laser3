package com.k_int.kbplus.api.v0

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.out.OutHelperService
import com.k_int.kbplus.api.v0.out.OutService
import com.k_int.kbplus.auth.User
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
            case 'impId':
                result = License.findAllWhere(impId: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
        if (result) {
            result = result.size() == 1 ? result.get(0) : MainService.PRECONDITION_FAILED
        }
        result
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getLicense(License lic, User user, Org context){
        def hasAccess = false

        lic.orgLinks.each{ orgRole ->
            if(orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }

        def result = []
        if(hasAccess) {
            result = outService.exportLicense(lic, OutHelperService.IGNORE_NONE, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : MainService.FORBIDDEN)
    }
}
