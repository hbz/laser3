package de.laser.api.v0.special

import com.k_int.kbplus.*
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiOAMonitor {

    /**
     * checks OAMONITOR_SERVER_ACCESS
     */
    static boolean calculateAccess(Org org) {

        def resultSetting = OrgSettings.get(org, OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS)
        if (resultSetting != OrgSettings.SETTING_NOT_FOUND && resultSetting.getValue()?.value == 'Yes') {
            return true
        }
        else {
            return false
        }
    }

    /**
     * checks implicit OAMONITOR_SERVER_ACCESS
     */
    static boolean calculateAccess(Subscription sub) {

        boolean hasAccess = false

        if (! sub.isPublicForApi) {
            hasAccess = false
        }
        else {
            List<Org> orgs = ApiOAMonitor.getAccessibleOrgs()

            if (orgs) {
                List<OrgRole> valid = OrgRole.executeQuery(
                        "select oo from OrgRole oo join oo.sub sub join oo.org org " +
                        "where sub = :sub and org in (:orgs) and oo.roleType in (:roles) ", [
                            sub  : sub,
                            orgs : orgs,
                            roles: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]
                        ]
                )
                hasAccess = ! valid.isEmpty()
            }
        }

        hasAccess
    }

    /**
     * checks OAMONITOR_SERVER_ACCESS
     */
    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                key    : OrgSettings.KEYS.OAMONITOR_SERVER_ACCESS,
                rdValue: RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N),
                deleted: RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS)
        ])

        orgs
    }

    /**
     * checks implicit OAMONITOR_SERVER_ACCESS
     *
     * @return JSON
     */
    static JSON getAllOrgs() {
        Collection<Object> result = []

        List<Org> orgs = getAccessibleOrgs()
        orgs.each { o ->
            result << ApiUnsecuredMapReader.getOrganisationStubMap(o)
        }

        return result ? new JSON(result) : null
        //return new JSON(result)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static requestOrganisation(Org org, Org context) {
        Map<String, Object> result = [:]

        boolean hasAccess = calculateAccess(org)
        if (hasAccess) {

            org = GrailsHibernateUtil.unwrapIfProxy(org)

            //def context = org // TODO

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
            result.lastUpdated  = ApiToolkit.formatInternalDate(org.lastUpdated)

            //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
            //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

            // RefdataValues

            result.sector       = org.sector?.value
            result.type         = org.orgType?.collect{ it.value }
            result.status       = org.status?.value

            // References

            //result.addresses    = ApiCollectionReader.retrieveAddressCollection(org.addresses, ApiReader.NO_CONSTRAINT) // com.k_int.kbplus.Address
            //result.contacts     = ApiCollectionReader.retrieveContactCollection(org.contacts, ApiReader.NO_CONSTRAINT) // com.k_int.kbplus.Contact
            result.identifiers  = ApiCollectionReader.getIdentifierCollection(org.ids) // com.k_int.kbplus.Identifier
            //result.persons      = ApiCollectionReader.retrievePrsLinkCollection(
            //        org.prsLinks, ApiCollectionReader.NO_CONSTRAINT, ApiCollectionReader.NO_CONSTRAINT, context
            //) // com.k_int.kbplus.PersonRole

            result.properties    = ApiCollectionReader.getPropertyCollection(org, context, ApiReader.IGNORE_PRIVATE_PROPERTIES) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)
            result.subscriptions = getSubscriptionCollection(org)

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static requestSubscription(Subscription sub, Org context) {
        Map<String, Object> result = [:]

        boolean hasAccess = calculateAccess(sub)
        if (hasAccess) {

            sub = GrailsHibernateUtil.unwrapIfProxy(sub)

            result.globalUID            	= sub.globalUID
            result.cancellationAllowances 	= sub.cancellationAllowances
            result.dateCreated          	= ApiToolkit.formatInternalDate(sub.dateCreated)
            result.endDate              	= ApiToolkit.formatInternalDate(sub.endDate)
            result.lastUpdated          	= ApiToolkit.formatInternalDate(sub.lastUpdated)
            result.manualCancellationDate 	= ApiToolkit.formatInternalDate(sub.manualCancellationDate)
            result.manualRenewalDate    	= ApiToolkit.formatInternalDate(sub.manualRenewalDate)
            result.name                 	= sub.name
            result.noticePeriod         	= sub.noticePeriod
            result.startDate            	= ApiToolkit.formatInternalDate(sub.startDate)
            result.calculatedType           = sub.getCalculatedType()

            // RefdataValues

            result.form                 = sub.form?.value
            result.isSlaved             = sub.isSlaved ? 'Yes' : 'No'
            result.isMultiYear          = sub.isMultiYear ? 'Yes' : 'No'
            result.resource             = sub.resource?.value
            result.status               = sub.status?.value
            result.type                 = sub.type?.value
            result.kind                 = sub.kind?.value
            result.isPublicForApi 		= sub.isPublicForApi ? 'Yes' : 'No'
            result.hasPerpetualAccess 	= sub.hasPerpetualAccess ? 'Yes' : 'No'

            // References

            result.instanceOf           = ApiUnsecuredMapReader.getSubscriptionStubMap(sub.instanceOf)
            //result.documents            = ApiCollectionReader.getDocumentCollection(sub.documents) // com.k_int.kbplus.DocContext
            //result.derivedSubscriptions = ApiStubReader.getStubCollection(sub.derivedSubscriptions, ApiReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            //result.identifiers          = ApiCollectionReader.getIdentifierCollection(sub.ids) // com.k_int.kbplus.Identifier
            //result.instanceOf           = ApiStubReader.requestSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
            //result.license              = ApiStubReader.requestLicenseStub(sub.owner, context) // com.k_int.kbplus.License
            //removed: result.license          = ApiCollectionReader.resolveLicense(sub.owner, ApiCollectionReader.IGNORE_ALL, context) // com.k_int.kbplus.License

            //result.organisations        = ApiCollectionReader.resolveOrgLinks(sub.orgRelations, ApiCollectionReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

            //result.predecessor = ApiStubReader.requestSubscriptionStub(sub.getCalculatedPrevious(), context) // com.k_int.kbplus.Subscription
            //result.successor   = ApiStubReader.requestSubscriptionStub(sub.getCalculatedSuccessor(), context) // com.k_int.kbplus.Subscription
            //result.properties  = ApiCollectionReader.getPropertyCollection(sub, context, ApiReader.IGNORE_NONE) // com.k_int.kbplus.(SubscriptionCustomProperty, SubscriptionPrivateProperty)

            def allOrgRoles = []

            // add derived subscriptions org roles
            if (sub.derivedSubscriptions) {
                allOrgRoles = OrgRole.executeQuery(
                        "select oo from OrgRole oo where oo.sub in (:derived) and oo.roleType in (:roles)",
                        [derived: sub.derivedSubscriptions, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
                )
            }
            allOrgRoles.addAll(sub.orgRelations)

            // TODO:0.93 @ result.organisations = ApiCollectionReader.getOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

            // TODO refactoring with issueEntitlementService
            //result.packages = ApiCollectionReader.getPackageWithIssueEntitlementsCollection(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

            // Ignored

            //result.packages = exportHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage
            //result.issueEntitlements = exportHelperService.resolveIssueEntitlements(sub.issueEntitlements, context) // com.k_int.kbplus.IssueEntitlement
            //result.packages = exportHelperService.resolveSubscriptionPackageStubs(sub.packages, exportHelperService.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
            /*
            result.persons      = exportHelperService.resolvePrsLinks(
                    sub.prsLinks,  true, true, context
            ) // com.k_int.kbplus.PersonRole
            */

            // TODO: oaMonitor
            //result.costItems    = ApiCollectionReader.getCostItemCollection(sub.costItems) // com.k_int.kbplus.CostItem

            ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    static private Collection<Object> getSubscriptionCollection(Org org) {
        if (!org ) {
            return null
        }
        if (org.status?.value == 'Deleted') {
            return []
        }

        Collection<Object> result = []

        List<Subscription> tmp = OrgRole.executeQuery(
                'select distinct(oo.sub) from OrgRole oo where oo.roleType in (:roleTypes)',
                [roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
        log.debug ("found ${tmp.size()} subscriptions .. processing")

        tmp.each { sub ->
            result.add(ApiStubReader.requestSubscriptionStub(sub, org))
        }

        if (! ApiToolkit.isDebugMode()) {
            result.removeAll(Constants.HTTP_FORBIDDEN)
        }

        ApiToolkit.cleanUp(result, true, true)
    }
}
