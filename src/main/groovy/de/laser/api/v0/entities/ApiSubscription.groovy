package de.laser.api.v0.entities


import de.laser.Identifier
import de.laser.Links
import de.laser.Org
import de.laser.OrgRole
import de.laser.Subscription
import de.laser.finance.CostItem
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDStore
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * An API representation of a {@link Subscription}
 */
@Slf4j
class ApiSubscription {

    /**
	 * Locates the given {@link Subscription} and returns the object (or null if not found) and the request status for further processing
	 * @param the field to look for the identifier, one of {id, globalUID, namespace:id}
	 * @param the identifier value with namespace, if needed
     * @return {@link ApiBox}(obj: Subscription | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND | OBJECT_STATUS_DELETED)
	 * @see ApiBox#validatePrecondition_1()
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

		/*if (result.obj instanceof Subscription) {
			result.validateDeletedStatus_2('status', RDStore.SUBSCRIPTION_DELETED)
		}*/
		result
    }

    /**
     * Checks if the requesting institution can access to the given subscription
	 * @param sub the {@link Subscription} to which access is being requested
	 * @param context the institution ({@link Org}) requesting access
	 * @return true if the access is granted, false otherwise
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
	 * Checks if the given institution can access the given subscription.
	 * The subscription is returned in case of success
	 * @param sub the {@link Subscription} whose details should be retrieved
	 * @param context the institution ({@link Org}) requesting the subscription
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
	 * Retrieves the list of subscriptions the requested institution has. Only those subscriptions appear in the result
	 * list which have been marked as public for API. This can be enabled for each subscription individually by
	 * setting the {@link Subscription#isPublicForApi} flag
	 * @param owner the institution ({@link Org}) whose subscriptions should be requested
	 * @param context the institution ({@link Org}) requesting
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
	 * Assembles the given subscription attributes into a {@link Map}. The schema of the map can be seen in schemas.gsp
	 * @param subscription the {@link Subscription} which should be output
	 * @param ignoreRelation currently unused
	 * @param context the institution ({@link Org}) requesting
	 * @param isInvoiceTool is the hbz invoice tool doing the request?
	 * @return Map<String, Object>
	 */
	static Map<String, Object> getSubscriptionMap(Subscription sub, def ignoreRelation, Org context, boolean isInvoiceTool){
		Map<String, Object> result = [:]

		sub = GrailsHibernateUtil.unwrapIfProxy(sub)

		result.globalUID            	= sub.globalUID
		result.dateCreated          	= ApiToolkit.formatInternalDate(sub.dateCreated)
		result.endDate              	= ApiToolkit.formatInternalDate(sub.endDate)
		result.lastUpdated          	= ApiToolkit.formatInternalDate(sub._getCalculatedLastUpdated())
		result.manualCancellationDate 	= ApiToolkit.formatInternalDate(sub.manualCancellationDate)
		result.name                 	= sub.name
		result.startDate            	= ApiToolkit.formatInternalDate(sub.startDate)
		result.calculatedType       	= sub._getCalculatedType()

		// RefdataValues

		result.form         			= sub.form?.value
        result.isMultiYear  			= sub.isMultiYear ? 'Yes' : 'No'
		result.isAutomaticRenewAnnually = sub.isAutomaticRenewAnnually ? 'Yes' : 'No'
		result.resource     			= sub.resource?.value
		result.status       			= sub.status?.value
		result.kind         			= sub.kind?.value
		result.isPublicForApi 			= sub.isPublicForApi ? 'Yes' : 'No'
		result.hasPerpetualAccess 		= sub.hasPerpetualAccess ? 'Yes' : 'No'
		result.hasPublishComponent 		= sub.hasPublishComponent ? 'Yes' : 'No'

		// References

		result.documents            = ApiCollectionReader.getDocumentCollection(sub.documents) // de.laser.DocContext
		//result.derivedSubscriptions = ApiStubReader.getStubCollection(sub.derivedSubscriptions, ApiReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
		result.identifiers          = ApiCollectionReader.getIdentifierCollection(sub.ids) // de.laser.Identifier
		result.instanceOf           = ApiStubReader.requestSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
		//result.organisations        = ApiCollectionReader.resolveOrgLinks(sub.orgRelations, ApiCollectionReader.IGNORE_SUBSCRIPTION, context) // de.laser.OrgRole
		result.orgAccessPoints			= ApiCollectionReader.getOrgAccessPointCollection(sub.getOrgAccessPointsOfSubscriber())

		result.predecessors = []
		result.successors   = []
		sub._getCalculatedPrevious().each { Subscription prev ->
			result.predecessors.add(ApiStubReader.requestSubscriptionStub(prev, context)) // com.k_int.kbplus.Subscription
		}
		sub._getCalculatedSuccessor().each { Subscription succ ->
			result.successors.add(ApiStubReader.requestSubscriptionStub(succ, context)) // com.k_int.kbplus.Subscription
		}
		result.properties  = ApiCollectionReader.getPropertyCollection(sub, context, ApiReader.IGNORE_NONE) // com.k_int.kbplus.(SubscriptionCustomProperty, SubscriptionPrivateProperty)

		result.linkedSubscriptions = []

		List<Links> otherLinks = Links.executeQuery("select li from Links li where (li.sourceSubscription = :subscription or li.destinationSubscription = :subscription) and li.linkType not in (:excludes)", [subscription: sub, excludes: [RDStore.LINKTYPE_FOLLOWS, RDStore.LINKTYPE_LICENSE]])
		otherLinks.each { Links li ->
			result.linkedSubscriptions.add([linktype: li.linkType.value, subscription: ApiStubReader.requestSubscriptionStub((Subscription) li.getOther(sub), context)])
		}

		def allOrgRoles = []

		// add derived subscriptions org roles
		if (sub.derivedSubscriptions) {
			allOrgRoles = OrgRole.executeQuery(
					"select oo from OrgRole oo where oo.sub in (:derived) and oo.roleType in (:roles)",
					[derived: sub.derivedSubscriptions, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
			)
		}
		allOrgRoles.addAll(sub.orgRelations)

		result.organisations = ApiCollectionReader.getOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.OrgRole

		// TODO refactoring with issueEntitlementService
		result.packages = ApiCollectionReader.getPackageWithIssueEntitlementsCollection(sub.packages, context) // de.laser.SubscriptionPackage

		// Ignored

		//result.packages = exportHelperService.resolvePackagesWithIssueEntitlements(sub.packages, context) // de.laser.SubscriptionPackage
		//result.issueEntitlements = exportHelperService.resolveIssueEntitlements(sub.issueEntitlements, context) // de.laser.IssueEntitlement
		//result.packages = exportHelperService.resolveSubscriptionPackageStubs(sub.packages, exportHelperService.IGNORE_SUBSCRIPTION, context) // de.laser.SubscriptionPackage
		/*
		result.persons      = exportHelperService.resolvePrsLinks(
				sub.prsLinks,  true, true, context
		) // de.laser.PersonRole
		*/

		//result.license = ApiStubReader.requestLicenseStub(sub.owner, context) // com.k_int.kbplus.License
		result.licenses = []
		sub.getLicenses().each { lic ->
			result.licenses.add( ApiStubReader.requestLicenseStub(lic, context) )
		}

		if (isInvoiceTool) {
			result.costItems = ApiCollectionReader.getCostItemCollection(sub.costItems, context)
		}
		else {
			Collection<CostItem> filtered = sub.costItems.findAll{ it.owner == context || it.isVisibleForSubscriber }

			result.costItems = ApiCollectionReader.getCostItemCollection(filtered, context)
		}

		ApiToolkit.cleanUp(result, true, true)
	}
}
