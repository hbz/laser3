package de.laser.api.v0.entities

import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.api.v0.ApiCollectionReader
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiStubReader
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Log4j
class ApiSubscription {

    /**
     * @return Subscription | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findSubscriptionBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Subscription.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
                result = Subscription.findAllWhere(globalUID: value)
                break
            case 'impId':
                result = Subscription.findAllWhere(impId: value)
                break
            case 'ns:identifier':
                result = Identifier.lookupObjectsByIdentifierString(new Subscription(), value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }

        ApiToolkit.checkPreconditionFailed(result)
    }

    /**
     * @return boolean
     */
    static boolean calculateAccess(Subscription sub, Org context) {

		boolean hasAccess = false

		if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIPTION_CONSORTIA, context)) {
			hasAccess = true
		}
		else if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIBER, context)) {
			hasAccess = true
		}
		else if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIBER_CONS, context)) {
			hasAccess = true
		}

        hasAccess
    }

    /**
     * @return JSON | FORBIDDEN
     */
    static getSubscription(Subscription sub, Org context, boolean isInvoiceTool){
        Map<String, Object> result = [:]

		boolean hasAccess = isInvoiceTool || calculateAccess(sub, context)
        if (hasAccess) {
            result = retrieveSubscriptionMap(sub, ApiReader.IGNORE_NONE, context)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON
     */
    static JSON getSubscriptionList(Org owner, Org context){
        Collection<Object> result = []

        List<Subscription> available = Subscription.executeQuery(
                'SELECT sub FROM Subscription sub JOIN sub.orgRelations oo WHERE oo.org = :owner AND oo.roleType in (:roles )' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]
                ]
        )

        available.each { sub ->
            //if (calculateAccess(sub, context, hasAccess)) {
                println sub.id + ' ' + sub.name
                result.add(ApiStubReader.requestSubscriptionStub(sub, context, true))
                //result.add([globalUID: sub.globalUID])
            //}
        }

		return (result ? new JSON(result) : null)
    }

	/**
	 * @return Map<String, Object>
	 */
	static Map<String, Object> retrieveSubscriptionMap(Subscription sub, def ignoreRelation, Org context){
		Map<String, Object> result = [:]

		sub = GrailsHibernateUtil.unwrapIfProxy(sub)

		result.globalUID            	= sub.globalUID
		result.cancellationAllowances 	= sub.cancellationAllowances
		result.dateCreated          	= sub.dateCreated
		result.endDate              	= sub.endDate
		//result.identifier           	= sub.identifier // TODO: refactor legacy
		result.lastUpdated          	= sub.lastUpdated
		result.manualCancellationDate 	= sub.manualCancellationDate
		result.manualRenewalDate    	= sub.manualRenewalDate
		result.name                 	= sub.name
		result.noticePeriod         	= sub.noticePeriod
		result.startDate            	= sub.startDate

		// erms-888
		result.calculatedType       = sub.getCalculatedType()

		// RefdataValues

		result.form         = sub.form?.value
		result.isSlaved     = sub.isSlaved ? 'Yes' : 'No'
        result.isMultiYear  = sub.isMultiYear ? 'Yes' : 'No'
		//result.isPublic     = sub.isPublic ? 'Yes' : 'No'
		result.resource     = sub.resource?.value
		result.status       = sub.status?.value
		result.type         = sub.type?.value

		// References

		result.documents            = ApiCollectionReader.retrieveDocumentCollection(sub.documents) // com.k_int.kbplus.DocContext
		//result.derivedSubscriptions = ApiStubReader.resolveStubs(sub.derivedSubscriptions, ApiCollectionReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
		result.identifiers          = ApiCollectionReader.retrieveIdentifierCollection(sub.ids) // com.k_int.kbplus.Identifier
		result.instanceOf           = ApiStubReader.requestSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
		result.license              = ApiStubReader.requestLicenseStub(sub.owner, context) // com.k_int.kbplus.License
		//removed: result.license          = ApiCollectionReader.resolveLicense(sub.owner, ApiCollectionReader.IGNORE_ALL, context) // com.k_int.kbplus.License

		//result.organisations        = ApiCollectionReader.resolveOrgLinks(sub.orgRelations, ApiCollectionReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

		result.predecessor = ApiStubReader.requestSubscriptionStub(sub.getCalculatedPrevious(), context) // com.k_int.kbplus.Subscription
		result.successor   = ApiStubReader.requestSubscriptionStub(sub.getCalculatedSuccessor(), context) // com.k_int.kbplus.Subscription
		result.properties  = ApiCollectionReader.retrievePropertyCollection(sub, context, ApiReader.IGNORE_NONE) // com.k_int.kbplus.(SubscriptionCustomProperty, SubscriptionPrivateProperty)

		def allOrgRoles = []

		// add derived subscriptions org roles
		if (sub.derivedSubscriptions) {
			allOrgRoles = OrgRole.executeQuery(
					"select oo from OrgRole oo where oo.sub in (:derived) and oo.roleType in (:roles)",
					[derived: sub.derivedSubscriptions, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
			)
		}
		allOrgRoles.addAll(sub.orgRelations)

		result.organisations = ApiCollectionReader.retrieveOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

		// TODO refactoring with issueEntitlementService
		result.packages = ApiCollectionReader.retrievePackageWithIssueEntitlementsCollection(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

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
		result.costItems    = ApiCollectionReader.retrieveCostItemCollection(sub.costItems) // com.k_int.kbplus.CostItem

		return ApiToolkit.cleanUp(result, true, true)
	}
}
