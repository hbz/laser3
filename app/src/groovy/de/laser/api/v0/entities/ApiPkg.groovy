package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import de.laser.helper.Constants
import de.laser.api.v0.ApiReader
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiPkg {

    /**
     * @return Package | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findPackageBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Package.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Package.findAllWhere(globalUID: value)
                break
            case 'impId':
                result = Package.findAllWhere(impId: value)
                break
            case 'gokbId':
                result = Package.findAllWhere(gokbId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Package(), value)
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
     * @return JSON | FORBIDDEN
     */
    static getPackage(Package pkg, Org context, boolean hasAccess) {
        Collection<Object> result = []
        // TODO
        if (! hasAccess) {
            pkg.orgs.each { orgRole ->
                if (orgRole.getOrg().id == context?.id) {
                    hasAccess = true
                }
            }
        }

        if (hasAccess) {
            result = ApiReader.retrievePackageMap(pkg, context) // TODO check orgRole.roleType
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
