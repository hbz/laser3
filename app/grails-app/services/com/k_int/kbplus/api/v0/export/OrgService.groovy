package com.k_int.kbplus.api.v0.export

import com.k_int.kbplus.*
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class OrgService {

    ExportHelperService exportHelperService

    static def findOrganisation(String query, String value) {
        def obj
        if('id'.equalsIgnoreCase(query)) {
            obj = Org.findWhere(id: Long.parseLong(value))
        }
        else if('impId'.equalsIgnoreCase(query)) {
            obj = Org.findWhere(impId: value)
        }
        else if('shortcode'.equalsIgnoreCase(query)) {
            obj = Org.findWhere(shortcode: value)
        }
        else {
            obj = ApiService.BAD_REQUEST
        }
        obj
    }

    /**
     * @param com.k_int.kbplus.Org org
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def getOrganisation(Org org, Org context) {
        def result = [:]

        result.id           = org.id
        result.comment      = org.comment
        result.name         = org.name
        result.scope        = org.scope
        result.shortcode    = org.shortcode

        // RefdataValues

        result.sector       = org.sector?.value
        result.type         = org.orgType?.value
        result.status       = org.status?.value

        // References

        result.addresses    = exportHelperService.resolveAddresses(org.addresses, exportHelperService.NO_CONSTRAINT) // com.k_int.kbplus.Address
        result.contacts     = exportHelperService.resolveContacts(org.contacts, exportHelperService.NO_CONSTRAINT) // com.k_int.kbplus.Contact
        result.identifiers  = exportHelperService.resolveIdentifiers(org.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.persons      = exportHelperService.resolvePrsLinks(
                org.prsLinks, exportHelperService.NO_CONSTRAINT, exportHelperService.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole

        result.properties   = exportHelperService.resolveProperties(org, context) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)

        // Ignored

        //result.affiliations         = org.affiliations // com.k_int.kblpus.UserOrg
        //result.incomingCombos       = org.incomingCombos // com.k_int.kbplus.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // com.k_int.kbplus.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // com.k_int.kbplus.Combo

        return exportHelperService.cleanUp(result, true, true)
    }
}
