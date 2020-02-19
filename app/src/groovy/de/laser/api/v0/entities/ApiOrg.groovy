package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiOrg {

    /**
     * @return Org | BAD_REQUEST | PRECONDITION_FAILED | OBJECT_STATUS_DELETED
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
            case 'gokbId':
                result = Org.findAllWhere(gokbId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Org(), value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }
        result = ApiToolkit.checkPreconditionFailed(result)

        if (result instanceof Org && result.status == RDStore.ORG_STATUS_DELETED) {
            result = Constants.OBJECT_STATUS_DELETED
        }
        result
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static requestOrganisation(Org org, Org context, boolean isInvoiceTool) {
        Map<String, Object> result = [:]

        boolean hasAccess = isInvoiceTool || (org.id == context.id)
        if (hasAccess) {
            result = getOrganisationMap(org, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Map<String, Object>
     */
    static Map<String, Object> getOrganisationMap(Org org, Org context) {
        Map<String, Object> result = [:]

        org = GrailsHibernateUtil.unwrapIfProxy(org)

        result.globalUID    = org.globalUID
        result.gokbId       = org.gokbId
        result.comment      = org.comment
        result.name         = org.name
        result.scope        = org.scope
        result.shortname    = org.shortname
        result.sortname     = org.sortname
        result.federalState = org.federalState?.value
        result.country      = org.country?.value
        result.libraryType  = org.libraryType?.value
        result.lastUpdated  = org.lastUpdated

        //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
        //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

        // RefdataValues

        result.sector       = org.sector?.value
        result.type         = org.orgType?.collect{ it.value }
        result.status       = org.status?.value

        // References

        result.addresses    = ApiCollectionReader.getAddressCollection(org.addresses, ApiReader.NO_CONSTRAINT) // com.k_int.kbplus.Address
        result.contacts     = ApiCollectionReader.getContactCollection(org.contacts, ApiReader.NO_CONSTRAINT) // com.k_int.kbplus.Contact
        result.identifiers  = ApiCollectionReader.getIdentifierCollection(org.ids) // com.k_int.kbplus.Identifier
        result.persons      = ApiCollectionReader.getPrsLinkCollection(
                org.prsLinks, ApiReader.NO_CONSTRAINT, ApiReader.NO_CONSTRAINT, context
        ) // com.k_int.kbplus.PersonRole

        result.properties   = ApiCollectionReader.getPropertyCollection(org, context, ApiReader.IGNORE_NONE) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)

        // Ignored

        //result.affiliations         = org.affiliations // com.k_int.kblpus.UserOrg
        //result.incomingCombos       = org.incomingCombos // com.k_int.kbplus.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // com.k_int.kbplus.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // com.k_int.kbplus.Combo

        ApiToolkit.cleanUp(result, true, true)
    }
}
