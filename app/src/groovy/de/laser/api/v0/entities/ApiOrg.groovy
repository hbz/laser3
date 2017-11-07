package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import de.laser.api.v0.ApiReader
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiOrg {

    /**
     * @return Org | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findOrganisationBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Org.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Org.findAllWhere(globalUID: value)
                break
            case 'impId':
                result = Org.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Org(), value)
                break
            case 'shortcode':
                result = Org.findAllWhere(shortcode: value)
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
    static getOrganisation(Org org, User user, Org context) {
        def result = []
        def hasAccess = ApiReader.isDataManager(user)

        if (hasAccess) {
            result = ApiReader.exportOrganisation(org, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
