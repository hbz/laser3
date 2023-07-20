package de.laser.api.v0.entities

import de.laser.Combo
import de.laser.DeletionService
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.Org
import de.laser.OrgSubjectGroup
import de.laser.api.v0.*
import de.laser.storage.Constants
import de.laser.storage.RDStore
import de.laser.traces.DeletedObject
import grails.converters.JSON
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of an {@link Org}
 */
class ApiOrg {

    /**
     * Locates the given {@link Org} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, globalUID, gokbId, ns:identifier}
     * @param the identifier value
     * @return {@link ApiBox}(obj: Org | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findOrganisationBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'ezbId':
                result.obj = Org.executeQuery('select id.org from Identifier id where id.value = :id and id.ns.ns = :ezb', [id: value, ezb: IdentifierNamespace.EZB_ORG_ID])
                break
            case 'id':
                result.obj = Org.findAllById(Long.parseLong(value))
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldDatabaseIDAndOldObjectType(Long.parseLong(value), Org.class.name)
                    }
                }
                break
            case 'globalUID':
                result.obj = Org.findAllByGlobalUID(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGlobalUID(value)
                    }
                }
                break
            case 'gokbId':
                result.obj = Org.findAllByGokbId(value)
                if(!result.obj) {
                    DeletedObject.withTransaction {
                        result.obj = DeletedObject.findAllByOldGokbID(value)
                    }
                }
                break
            case 'ns:identifier':
                result.obj = Identifier.lookupObjectsByIdentifierString(new Org(), value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        if (result.obj instanceof Org) {
            result.validateDeletedStatus_2('status', RDStore.ORG_STATUS_DELETED)
        }
        result
    }

    /**
     * Checks if the given institution can access the given target organisation. The organisation
     * is returned in case of success
     * @param org the {@link Org} whose details should be retrieved
     * @param context the institution ({@link Org}) requesting the organisation
     * @param isInvoiceTool is the request done by the hbz invoice tool?
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
     * Assembles the given organisation attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param org the {@link Org} which should be output
     * @param context the institution ({@link Org}) requesting
     * @return Map<String, Object>
     */
    static Map<String, Object> getOrganisationMap(Org org, Org context) {
        Map<String, Object> result = [:]

        org = GrailsHibernateUtil.unwrapIfProxy(org)

        result.globalUID           = org.globalUID
        result.gokbId              = org.gokbId
        result.name                = org.name
        result.altNames            = ApiCollectionReader.getAlternativeNameCollection(org.altnames)
        //result.shortname           = org.sortname //deprecated and to be removed for 3.2
        //result.sortname            = org.sortname
        result.lastUpdated         = ApiToolkit.formatInternalDate(org._getCalculatedLastUpdated())
        result.eInvoice            = org.eInvoice ? RDStore.YN_YES.value : RDStore.YN_NO.value
        result.url                 = org.url
        result.urlGov              = org.urlGov
        result.linkResolverBaseURL = org.linkResolverBaseURL
        result.legalPatronName     = org.legalPatronName

        result.retirementDate      = org.retirementDate ? ApiToolkit.formatInternalDate(org.retirementDate) : null
        result.links               = []

        Set<Combo> links = Combo.executeQuery('select c from Combo c where (c.fromOrg = :org or c.toOrg = :org) and c.type != :excludes', [org: org, excludes: RDStore.COMBO_TYPE_CONSORTIUM])
        links.each { Combo c ->
            if(c.fromOrg == org)
                result.links << [linktype: c.type.value, org: ApiUnsecuredMapReader.getOrganisationStubMap(c.toOrg)]
            else if(c.toOrg == org)
                result.links << [linktype: c.type.value, org: ApiUnsecuredMapReader.getOrganisationStubMap(c.fromOrg)]
        }

        //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
        //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

        // RefdataValues

        result.eInvoicePortal = org.eInvoicePortal?.value
        result.region         = org.region?.value
        result.country        = org.country?.value
        result.libraryType    = org.libraryType?.value
        result.funderType     = org.funderType?.value
        result.funderHskType  = org.funderHskType?.value
        result.subjectGroup   = org.subjectGroup?.collect { OrgSubjectGroup subjectGroup -> subjectGroup.subjectGroup.value }
        result.libraryNetwork = org.libraryNetwork?.value
        result.sector         = org.sector?.value
        result.type           = org.orgType?.collect{ it.value }
        result.status         = org.status?.value

        // References

        result.addresses    = ApiCollectionReader.getAddressCollection(org.addresses, ApiReader.NO_CONSTRAINT) // de.laser.Address
        result.contacts     = ApiCollectionReader.getContactCollection(org.contacts, ApiReader.NO_CONSTRAINT)  // de.laser.Contact
        result.identifiers  = ApiCollectionReader.getIdentifierCollection(org.ids) // de.laser.Identifier
        result.persons      = ApiCollectionReader.getPrsLinkCollection(
                org.prsLinks, ApiReader.NO_CONSTRAINT, ApiReader.NO_CONSTRAINT, context
        ) // de.laser.PersonRole

        result.orgAccessPoints	= ApiCollectionReader.getOrgAccessPointCollection(org.accessPoints)

        result.properties   = ApiCollectionReader.getPropertyCollection(org, context, ApiReader.IGNORE_NONE) // de.laser.(OrgCustomProperty, OrgPrivateProperty)

        // Ignored

        //result.incomingCombos       = org.incomingCombos // de.laser.Combo
        //result.links                = exportHelperService.resolveOrgLinks(org.links) // de.laser.OrgRole
        //result.membership           = org.membership?.value // RefdataValue
        //result.outgoingCombos       = org.outgoingCombos // de.laser.Combo

        ApiToolkit.cleanUp(result, true, true)
    }
}
