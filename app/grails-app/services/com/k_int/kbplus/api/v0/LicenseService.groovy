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
     * @return License | BAD_REQUEST
     */
    def findLicenseBy(String query, String value) {

        switch(query) {
            case 'id':
                return License.findWhere(id: Long.parseLong(value))
                break
            case 'impId':
                return License.findWhere(impId: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
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
