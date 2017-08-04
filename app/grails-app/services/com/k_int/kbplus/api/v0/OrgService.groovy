package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Org
import com.k_int.kbplus.api.v0.out.OutService
import com.k_int.kbplus.auth.User
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class OrgService {

    OutService outService

    /**
     * @return Org | BAD_REQUEST
     */
    def findOrganisationBy(String query, String value) {

        switch(query) {
            case 'id':
                return Org.findWhere(id: Long.parseLong(value))
                break
            case 'impId':
                return Org.findWhere(impId: value)
                break
            case 'ns:identifier':
                return Org.lookupByIdentifierString(value)
                break
            case 'shortcode':
                return Org.findWhere(shortcode: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
    }

    /**
     * @return grails.converters.JSON | FORBIDDEN
     */
    def getOrganisation(Org org, User user, Org context) {
        def hasAccess = true
        def result = outService.exportOrganisation(org, context)

        return (hasAccess ? new JSON(result) : MainService.FORBIDDEN)
    }
}
