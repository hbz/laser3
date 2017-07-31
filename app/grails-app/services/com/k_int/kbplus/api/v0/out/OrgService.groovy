package com.k_int.kbplus.api.v0.out

import com.k_int.kbplus.*
import com.k_int.kbplus.api.v0.MainService
import com.k_int.kbplus.api.v0.in.ImportHelperService
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.json.JSONObject
import org.springframework.transaction.TransactionStatus

@Log4j
class OrgService {

    ExportHelperService exportHelperService
    ImportHelperService importHelperService

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
            case 'identifier':
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
     * @param com.k_int.kbplus.Org org
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def getOrganisation(Org org, Org context) {
        def result = [:]

        org = GrailsHibernateUtil.unwrapIfProxy(org)

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

    /**
     *
     * @param JSONObject data
     * @param com.k_int.kbplus.Org context
     * @return
     */
    def postOrganisation(JSONObject data, Org context) {

        def org
        Org.withTransaction { TransactionStatus status ->

            try {
                org = new Org(
                        name: data.name,
                        comment: data.comment,
                        scope: data.scope
                )

                // RefdataValues
                org.sector  = importHelperService.getRefdataValue(data.sector, "OrgSector")
                org.status  = importHelperService.getRefdataValue(data.status, "OrgStatus") // TODO unknown catagory !!!
                org.orgType = importHelperService.getRefdataValue(data.type, "OrgType")

                // References
                org.addresses = importHelperService.getAddresses(data.addresses, org, null)
                org.contacts  = importHelperService.getContacts(data.contacts, org, null)
                org.ids       = importHelperService.getIdentifiers(data.identifiers, org) // implicit creation of identifier and namespace

                def properties        = importHelperService.getProperties(data.properties, org, null, context)
                org.customProperties  = properties['custom']
                org.privateProperties = properties['private']

                def personsAndRoles = importHelperService.getPersonsAndRoles(data.persons, org, context)
                org.prsLinks        = personsAndRoles['personRoles']

                personsAndRoles['persons'].each { p ->
                    (Person) p.save() // save persons before saving prsLinks
                }

                org.save(flush: true)
            }
            catch (Exception e) {
                log.error("Error while importing ORG via API; rollback forced")
                log.error(e)
                status.setRollbackOnly()
                return ['result': MainService.INTERNAL_SERVER_ERROR, 'debug': e]
            }
        }

        return ['result': MainService.CREATED, 'debug': org]
    }
}
