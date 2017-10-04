package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.api.v0.base.OutService
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class OrgService {

    OutService outService

    /**
     * @return Org | BAD_REQUEST | PRECONDITION_FAILED
     */
    def findOrganisationBy(String query, String value) {
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
    def getOrganisation(Org org, User user, Org context) {
        def hasAccess = true
        def result = outService.exportOrganisation(org, context)

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }
}
