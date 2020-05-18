package de.laser.api.v0.entities

import com.k_int.kbplus.CostItem
import com.k_int.kbplus.Identifier
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.api.v0.ApiBox
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
     * @return ApiBox(obj: Subscription | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
     */
    static ApiBox findSubscriptionBy(String query, String value) {
		ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
				result.obj = Subscription.findAllWhere(id: Long.parseLong(value))
                break
            case 'globalUID':
				result.obj = Subscription.findAllWhere(globalUID: value)
                break
            case 'ns:identifier':
				result.obj = Identifier.lookupObjectsByIdentifierString(new Subscription(), value)
                break
            default:
				result.status = Constants.HTTP_BAD_REQUEST
				return result
                break
        }
		result.validatePrecondition_1()

		if (result.obj instanceof Subscription) {
			result.validateDeletedStatus_2('status', RDStore.SUBSCRIPTION_DELETED)
		}
		result
    }

    /**
     * @return boolean
     */
    static boolean calculateAccess(Subscription sub, Org context) {

		boolean hasAccess = false

		if (! sub.isPublicForApi) {
			hasAccess = false
		}
		else if (OrgRole.findBySubAndRoleTypeAndOrg(sub, RDStore.OR_SUBSCRIPTION_CONSORTIA, context)) {
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
    static requestSubscription(Subscription sub, Org context, boolean isInvoiceTool){
        Map<String, Object> result = [:]

		boolean hasAccess = isInvoiceTool || calculateAccess(sub, context)
        if (hasAccess) {
            result = getSubscriptionMap(sub, ApiReader.IGNORE_NONE, context, isInvoiceTool)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return JSON
     */
    static JSON getSubscriptionList(Org owner, Org context){
        Collection<Object> result = []

        List<Subscription> available = Subscription.executeQuery(
                'SELECT DISTINCT(sub) FROM Subscription sub JOIN sub.orgRelations oo WHERE oo.org = :owner AND oo.roleType in (:roles )' ,
                [
                        owner: owner,
                        roles: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]
                ]
        )

		println "${available.size()} available subscriptions found .."

        available.each { sub ->
			result.add(ApiStubReader.requestSubscriptionStub(sub, context))
        }

		ApiToolkit.cleanUpDebugInfo(result)

		return (result ? new JSON(result) : null)
    }

	/**
	 * @return Map<String, Object>
	 */
	static Map<String, Object> getSubscriptionMap(Subscription sub, def ignoreRelation, Org context, boolean isInvoiceTool){
		Map<String, Object> result = [:]

		sub = GrailsHibernateUtil.unwrapIfProxy(sub)

		result.globalUID            	= sub.globalUID
		result.cancellationAllowances 	= sub.cancellationAllowances
		result.dateCreated          	= ApiToolkit.formatInternalDate(sub.dateCreated)
		result.endDate              	= ApiToolkit.formatInternalDate(sub.endDate)
		result.lastUpdated          	= ApiToolkit.formatInternalDate(sub.getCalculatedLastUpdated())
		result.manualCancellationDate 	= ApiToolkit.formatInternalDate(sub.manualCancellationDate)
		result.manualRenewalDate    	= ApiToolkit.formatInternalDate(sub.manualRenewalDate)
		result.name                 	= sub.name
		result.noticePeriod         	= sub.noticePeriod
		result.startDate            	= ApiToolkit.formatInternalDate(sub.startDate)
		result.calculatedType       	= sub.getCalculatedType()

		// RefdataValues

		result.form         		= sub.form?.value
		result.isSlaved     		= sub.isSlaved ? 'Yes' : 'No'
        result.isMultiYear  		= sub.isMultiYear ? 'Yes' : 'No'
		result.resource     		= sub.resource?.value
		result.status       		= sub.status?.value
		result.type         		= sub.type?.value
		result.kind         		= sub.kind?.value
		result.isPublicForApi 		= sub.isPublicForApi ? 'Yes' : 'No'
		result.hasPerpetualAccess 	= sub.hasPerpetualAccess ? 'Yes' : 'No'

		// References

		result.documents            = ApiCollectionReader.getDocumentCollection(sub.documents) // com.k_int.kbplus.DocContext
		//result.derivedSubscriptions = ApiStubReader.getStubCollection(sub.derivedSubscriptions, ApiReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
		result.identifiers          = ApiCollectionReader.getIdentifierCollection(sub.ids) // com.k_int.kbplus.Identifier
		result.instanceOf           = ApiStubReader.requestSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
		result.license              = ApiStubReader.requestLicenseStub(sub.owner, context) // com.k_int.kbplus.License
		//removed: result.license          = ApiCollectionReader.resolveLicense(sub.owner, ApiCollectionReader.IGNORE_ALL, context) // com.k_int.kbplus.License

		//result.organisations        = ApiCollectionReader.resolveOrgLinks(sub.orgRelations, ApiCollectionReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

		result.predecessor = ApiStubReader.requestSubscriptionStub(sub.getCalculatedPrevious(), context) // com.k_int.kbplus.Subscription
		result.successor   = ApiStubReader.requestSubscriptionStub(sub.getCalculatedSuccessor(), context) // com.k_int.kbplus.Subscription
		result.properties  = ApiCollectionReader.getPropertyCollection(sub, context, ApiReader.IGNORE_NONE) // com.k_int.kbplus.(SubscriptionCustomProperty, SubscriptionPrivateProperty)

		def allOrgRoles = []

		// add derived subscriptions org roles
		if (sub.derivedSubscriptions) {
			allOrgRoles = OrgRole.executeQuery(
					"select oo from OrgRole oo where oo.sub in (:derived) and oo.roleType in (:roles)",
					[derived: sub.derivedSubscriptions, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
			)
		}
		allOrgRoles.addAll(sub.orgRelations)

		result.organisations = ApiCollectionReader.getOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.OrgRole

		// TODO refactoring with issueEntitlementService
		result.packages = ApiCollectionReader.getPackageWithIssueEntitlementsCollection(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage

		// Ignored

		//result.packages = exportHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // com.k_int.kbplus.SubscriptionPackage
		//result.issueEntitlements = exportHelperService.resolveIssueEntitlements(sub.issueEntitlements, context) // com.k_int.kbplus.IssueEntitlement
		//result.packages = exportHelperService.resolveSubscriptionPackageStubs(sub.packages, exportHelperService.IGNORE_SUBSCRIPTION, context) // com.k_int.kbplus.SubscriptionPackage
		/*
		result.persons      = exportHelperService.resolvePrsLinks(
				sub.prsLinks,  true, true, context
		) // com.k_int.kbplus.PersonRole
		*/

		if (isInvoiceTool) {
			result.costItems = ApiCollectionReader.getCostItemCollection(sub.costItems) // com.k_int.kbplus.CostItem
		}
		else {
			Collection<CostItem> filtered= sub.costItems.findAll{ it.owner == context || it.isVisibleForSubscriber }

			result.costItems = ApiCollectionReader.getCostItemCollection(filtered) // com.k_int.kbplus.CostItem
		}

		ApiToolkit.cleanUp(result, true, true)
	}
}
