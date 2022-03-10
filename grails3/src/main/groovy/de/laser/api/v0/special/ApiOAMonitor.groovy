package de.laser.api.v0.special


import de.laser.Org
import de.laser.OrgRole
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.finance.CostItem
import de.laser.OrgSetting
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.titles.TitleInstance
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This class is an endpoint implemented for the OA monitor of the Jülich research centre (FZ Jülich)
 */
@Slf4j
class ApiOAMonitor {

    /**
     * Checks OAMONITOR_SERVER_ACCESS, i.e. if the given institution authorised access to its data for the OA monitor endpoint
     * @param org the institution ({@link Org}) whose data should be accessed
     * @return true if access is granted, false otherwise
     */
    static boolean calculateAccess(Org org) {

        def resultSetting = OrgSetting.get(org, OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS)
        if (resultSetting != OrgSetting.SETTING_NOT_FOUND && resultSetting.getValue()?.value == 'Yes') {
            return true
        }
        else {
            return false
        }
    }

    /**
     * Checks if the given subscription is accessible.
     * Checks implicitely OAMONITOR_SERVER_ACCESS, i.e. if the requested institution is among those which authorised access to the OA monitor endpoint
     * @param sub the {@link Subscription} to which access is requested
     * @return true if access is granted, false otherwise
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
     * Retrieves all institutions which have given access to the OA monitor.
     * Checks OAMONITOR_SERVER_ACCESS; here those which have granted access to their data for the OA monitor
     */
    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSetting.executeQuery(
                "select o from OrgSetting os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                key    : OrgSetting.KEYS.OAMONITOR_SERVER_ACCESS,
                rdValue: RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N),
                deleted: RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS)
        ])

        orgs
    }

    /**
     * Lists the details of all institutions which have granted access to the OA monitor endpoint.
     * Checks implicit OAMONITOR_SERVER_ACCESS, i.e. if the requested institution is among those which gave permission to
     * the OA monitor
     * @return a {@link JSON} containing a list of the organisation stubs
     */
    static JSON getAllOrgs() {
        Collection<Object> result = []

        List<Org> orgs = getAccessibleOrgs()
        orgs.each { o ->
            result << ApiUnsecuredMapReader.getOrganisationStubMap(o)
        }

        return result ? new JSON(result) : null
    }

    /**
     * Requests the given institution and returns a {@link Map} containing the requested institution's details if
     * the requesting institution has access to the details
     * @return JSON | FORBIDDEN
     * @see Org
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
            result.region       = org.region?.value
            result.country      = org.country?.value
            result.libraryType  = org.libraryType?.value
            result.lastUpdated  = ApiToolkit.formatInternalDate(org._getCalculatedLastUpdated())

            //result.fteStudents  = org.fteStudents // TODO dc/table readerNumber
            //result.fteStaff     = org.fteStaff // TODO dc/table readerNumber

            // RefdataValues

            result.sector       = org.sector?.value
            result.type         = org.orgType?.collect{ it.value }
            result.status       = org.status?.value

            // References

            //result.addresses    = ApiCollectionReader.retrieveAddressCollection(org.addresses, ApiReader.NO_CONSTRAINT) // de.laser.Address
            //result.contacts     = ApiCollectionReader.retrieveContactCollection(org.contacts, ApiReader.NO_CONSTRAINT)  // de.laser.Contact
            result.identifiers  = ApiCollectionReader.getIdentifierCollection(org.ids) // de.laser.Identifier
            //result.persons      = ApiCollectionReader.retrievePrsLinkCollection(
            //        org.prsLinks, ApiCollectionReader.NO_CONSTRAINT, ApiCollectionReader.NO_CONSTRAINT, context
            //) // de.laser.PersonRole

            result.properties    = ApiCollectionReader.getPropertyCollection(org, context, ApiReader.IGNORE_PRIVATE_PROPERTIES) // com.k_int.kbplus.(OrgCustomProperty, OrgPrivateProperty)
            result.subscriptions = getSubscriptionCollection(org)

            result = ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Requests the given subscription and returns a {@link Map} containing the requested subscription's details if
     * the requesting institution has access to the details
     * @return JSON | FORBIDDEN
     * @see Subscription
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
            result.lastUpdated          	= ApiToolkit.formatInternalDate(sub._getCalculatedLastUpdated())
            result.manualCancellationDate 	= ApiToolkit.formatInternalDate(sub.manualCancellationDate)
            result.manualRenewalDate    	= ApiToolkit.formatInternalDate(sub.manualRenewalDate)
            result.name                 	= sub.name
            result.noticePeriod         	= sub.noticePeriod
            result.startDate            	= ApiToolkit.formatInternalDate(sub.startDate)
            result.calculatedType           = sub._getCalculatedType()

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
            //result.documents            = ApiCollectionReader.getDocumentCollection(sub.documents) // de.laser.DocContext
            //result.derivedSubscriptions = ApiStubReader.getStubCollection(sub.derivedSubscriptions, ApiReader.SUBSCRIPTION_STUB, context) // com.k_int.kbplus.Subscription
            //result.identifiers          = ApiCollectionReader.getIdentifierCollection(sub.ids) // de.laser.Identifier
            //result.instanceOf           = ApiStubReader.requestSubscriptionStub(sub.instanceOf, context) // com.k_int.kbplus.Subscription
            //result.license              = ApiStubReader.requestLicenseStub(sub.owner, context) // com.k_int.kbplus.License
            //removed: result.license          = ApiCollectionReader.resolveLicense(sub.owner, ApiCollectionReader.IGNORE_ALL, context) // com.k_int.kbplus.License

            //result.predecessor = ApiStubReader.requestSubscriptionStub(sub._getCalculatedPrevious(), context) // com.k_int.kbplus.Subscription
            //result.successor   = ApiStubReader.requestSubscriptionStub(sub._getCalculatedSuccessor(), context) // com.k_int.kbplus.Subscription
            result.properties  = ApiCollectionReader.getPropertyCollection(sub, context, ApiReader.IGNORE_PRIVATE_PROPERTIES) // com.k_int.kbplus.(SubscriptionCustomProperty, SubscriptionPrivateProperty)

            List<OrgRole> allOrgRoles = []

            // add derived subscriptions org roles
            if (sub.derivedSubscriptions) {
                allOrgRoles = OrgRole.executeQuery(
                        "select oo from OrgRole oo where oo.sub in (:derived) and oo.roleType in (:roles)",
                        [derived: sub.derivedSubscriptions, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
                )
            }
            allOrgRoles.addAll(sub.orgRelations)

            result.organisations = ApiCollectionReader.getOrgLinkCollection(allOrgRoles, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.OrgRole

            result.packages = ApiOAMonitor.getPackageCollectionWithTitleStubMaps(sub.packages)

            Collection<CostItem> filtered = []

            if (sub._getCalculatedType() in [Subscription.TYPE_PARTICIPATION]) {
                List<Org> validOwners = getAccessibleOrgs()
                List<Org> subSubscribers = sub.getAllSubscribers()
                //filtered = sub.costItems.findAll{ it.owner in validOwners && it.owner in subSubscribers }
                filtered = sub.costItems.findAll{ it.owner in validOwners && (it.owner in subSubscribers || it.isVisibleForSubscriber) }
            }
            else {
                filtered = sub.costItems
            }
            result.costItems = ApiCollectionReader.getCostItemCollection(filtered)

            ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? new JSON(result) : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Retrieves a collection of subscriptions the given institution subscribed
     * @param org the institution whose subscriptions should be retrieved
     * @return a {@link Collection} of {@link Subscription}s
     */
    static private Collection<Object> getSubscriptionCollection(Org org) {
        if (!org ) {
            return null
        }
        if (org.status?.value == 'Deleted') {
            return []
        }

        Collection<Object> result = []

        List<Subscription> tmp = OrgRole.executeQuery(
                'select distinct(oo.sub) from OrgRole oo where oo.org = :org and oo.roleType in (:roleTypes)', [
                        org: org,
                        roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]
                ]
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

    /**
     * Retrieves a list of packages with their titles, those delivered in stubs with the essential information
     * @param list the list of subscribed packages whose titles should be enumerated
     * @return a {@link Collection} of packages with title stubs
     */
    static Collection<Object> getPackageCollectionWithTitleStubMaps(Collection<SubscriptionPackage> list) {
        Collection<Object> result = []

        list.each { subPkg ->
            Map<String, Object> pkg = ApiUnsecuredMapReader.getPackageStubMap(subPkg.pkg) // de.laser.Package

            pkg.organisations = ApiCollectionReader.getOrgLinkCollection(subPkg.pkg.orgs, ApiReader.IGNORE_PACKAGE, null) // de.laser.OrgRole
            result << pkg

            List tmp = []
            List<TitleInstancePackagePlatform> tiList = TitleInstance.executeQuery(
                    'select tipp from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                            ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status != :statusIe',
                    [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_DELETED, statusIe: RDStore.TIPP_STATUS_DELETED]
            )

            tiList.each{ ti ->
                tmp << ApiUnsecuredMapReader.getTitleStubMap(ti)
            }

            pkg.titles = ApiToolkit.cleanUp(tmp, true, true)
        }

        return ApiToolkit.cleanUp(result, true, false)
    }
}
