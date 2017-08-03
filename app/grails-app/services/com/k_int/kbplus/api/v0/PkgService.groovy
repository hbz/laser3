package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.api.v0.out.OutService
import com.k_int.kbplus.auth.User
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class PkgService {

    OutService outService

    /**
     * @return Package | BAD_REQUEST
     */
    def findPackageBy(String query, String value) {

        switch(query) {
            case 'id':
                return Package.findWhere(id: Long.parseLong(value))
                break
            case 'identifier':
                return Package.findWhere(identifier: value) // != identifiers
                break
            case 'impId':
                return Package.findWhere(impId: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getPackage(Package pkg, User user, Org context) {
        def hasAccess = true

        //pkg.orgs.each{ orgRole ->
        //    if(orgRole.getOrg().id == context?.id) {
        //        hasAccess = true
        //    }
        //}

        def result = []
        if (hasAccess) {
            result = outService.exportPackage(pkg, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : MainService.FORBIDDEN)
    }
}
