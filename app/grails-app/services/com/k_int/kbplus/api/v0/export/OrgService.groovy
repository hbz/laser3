package com.k_int.kbplus.api.v0.export

import com.k_int.kbplus.*
import groovy.util.logging.Log4j

@Log4j
class OrgService {

    ExportHelperService exportHelperService

    /**
     *
     * @param com.k_int.kbplus.Org org
     * @param allowedAddressTypes
     * @param allowedContactTypes
     * @return
     */
    def resolveOrganisation(Org org, allowedAddressTypes, allowedContactTypes) {
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
        result.addresses            = exportHelperService.resolveAddresses(org.addresses, allowedAddressTypes) // com.k_int.kbplus.Address
        result.contacts             = exportHelperService.resolveContacts(org.contacts, allowedContactTypes) // com.k_int.kbplus.Contact
        result.identifiers          = exportHelperService.resolveIdentifiers(org.ids) // com.k_int.kbplus.IdentifierOccurrence
        result.persons              = exportHelperService.resolvePrsLinks(
                org.prsLinks, allowedAddressTypes, allowedContactTypes, true, false
        ) // com.k_int.kbplus.PersonRole

        result.properties           = exportHelperService.resolveProperties(org) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)


        //result.affiliations         = org.affiliations // com.k_int.kblpus.UserOrg
        //result.incomingCombos       = org.incomingCombos // com.k_int.kbplus.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // com.k_int.kbplus.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // com.k_int.kbplus.Combo

        return exportHelperService.cleanUp(result, true, true)
    }
}
