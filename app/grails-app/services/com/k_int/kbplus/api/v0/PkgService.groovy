package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.api.v0.base.OutService
import com.k_int.kbplus.auth.User
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class PkgService {

    OutService outService

    /**
     * @return Package | BAD_REQUEST | PRECONDITION_FAILED
     */
    def findPackageBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Package.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Package.findAllWhere(globalUID: value)
                break
            case 'identifier':
                result = Package.findAllWhere(identifier: value)
                break
            case 'impId':
                result = Package.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Package(), value)
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
    def getPackage(Package pkg, User user, Org context) {
        def hasAccess = true

        // TODO
        pkg.orgs.each{ orgRole ->
            if(orgRole.getOrg().id == context?.id) {
                hasAccess = true
            }
        }

        def result = []
        if (hasAccess) {
            result = outService.exportPackage(pkg, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : MainService.FORBIDDEN)
    }
}
