package de.laser

import com.k_int.kbplus.PendingChangeService
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.finance.CostItem
import de.laser.storage.BeanStorage
import de.laser.helper.MigrationHelper
import de.laser.properties.PropertyDefinitionGroup
import de.laser.oap.OrgAccessPoint
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.helper.DateUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.annotations.RefdataAnnotation
import grails.plugins.orm.auditable.Auditable
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.Permissions
import de.laser.interfaces.ShareSupport
import de.laser.properties.SubscriptionProperty
import de.laser.traits.ShareableTrait
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.SimpleDateFormat
import java.time.LocalDate

/**
 * This is the most central of all the domains and the turning point of everything: the subscription.
 * A subscription describes the permission of an institution to use electronic resources; that is what the institution actually
 * purchases from a provider when licensing titles to the institution itself (local subscription, see below) or to a group of institutions
 * participating in a bulk order. This bulk ordering is called consortial subscription. In the German-speaking area, there are (currently, November 11th, 2021) 18
 * so called consortial institutions (in short consortia) (Konsortialstellen) which are organising such bulk licensings with the providers
 * (see <a href="https://laser.hbz-nrw.de/gasco/">the GASCO monitor</a> for the up-to-date list of consortia in the German-speaking area; check the dropdown for
 * the respective institutions and load the title list to see which products may be purchased via a consortium). An institution (library, research organisation, see
 * {@link Org} for the definition of institution) may then purchase the license for cheaper when participating in such a consortial subscription.
 * The licensed resources ((e-)books, (e-)journals, title databases but also films and other audiovisual media, see {@link RDConstants#TITLE_MEDIUM} for the
 * full list of entitlement types which may be handled) are represented by title instances which are delivered in packages. So, a subscription aims usually a
 * package of titles, provided on a certain platform. The subscribed titles consist the local holding; they are the so called issue entitlements. Subscriptions do
 * usually have but not necessarily need to be bound to a time ranging, spanned between {@link #startDate} and {@link #endDate};
 * it should become standard to organise subscriptions in rings of year, ranging between January 1st and December 31st of a year.
 * Nonetheless, it is possible to set different dates (of course they should not overlap) or to define a subscription ranging over
 * several years; they are then called multi-year subscriptions. Structure may then be:
 * It is even possible to omit the dates completely. Test access subscriptions for example have no subscription range defined since
 * an institution takes a subscription test-wise to see how the titles are being appreciated among the end users.
 * There are three types of subscriptions:
 * <ol>
 *     <li>consortial (parent) subscriptions</li>
 *     <li>consortial (member) subscriptions</li>
 *     <li>local subscriptions</li>
 * </ol>
 * (above them, there are administrative subscriptions as well, but they are for hbz-internal purposes only and technically consortial subscriptions)
 * Consortial parent subscriptions can be maintained only by consortia, local subscriptions only by single users. Consortia may add member institutions
 * to consortial subscriptions; they then get member subscriptions. Basic members and single users may have consortia member subscriptions, but they have
 * reading rights only. Members may add notes to the subscriptions; single users may add above that own documents, tasks, notes and properties to the
 * consortial member subscription. Consortia have full writing rights for the parent and the member subscriptions as well. Consortia may also share attributes
 * with their members; properties may be inherited just as identifiers and documents and cost items may be shared with the consortia subscription members. Shared
 * items are thus visible on both levels - on parent and on member level. The inhertiance may be configured to take effect automatically or only after accepting it -
 * this is controlled by the {@link #isSlaved} flag
 * Single users may manage their local subscriptions independently. Subscriptions have a wide range of functionality; costs and statistics may be managed via the
 * subscription or its holding and reporting is mainly fed by data from and around subscriptions
 * @see SubscriptionProperty
 * @see Platform
 * @see Package
 * @see SubscriptionPackage
 * @see TitleInstancePackagePlatform
 * @see IssueEntitlement
 * @see CostItem
 * @see License
 */
class Subscription extends AbstractBaseWithCalculatedLastUpdated
        implements Auditable, CalculatedType, Permissions, ShareSupport {

    @RefdataAnnotation(cat = RDConstants.SUBSCRIPTION_STATUS)
    RefdataValue status

    @RefdataAnnotation(cat = RDConstants.SUBSCRIPTION_TYPE)
    RefdataValue type

    @RefdataAnnotation(cat = RDConstants.SUBSCRIPTION_KIND)
    RefdataValue kind

    @RefdataAnnotation(cat = RDConstants.SUBSCRIPTION_FORM)
    RefdataValue form

    @RefdataAnnotation(cat = RDConstants.SUBSCRIPTION_RESOURCE)
    RefdataValue resource

    // If a subscription is slaved then any changes to instanceOf will automatically be applied to this subscription
    boolean isSlaved = false
	boolean isPublicForApi = false

    //explicitely demanded as of ERMS-2503 - but demand has been revoked! Keep in u.f.n. until discussions on orderer side are terminated!
    //@RefdataAnnotation(cat = RDConstants.Y_N)
    //RefdataValue hasPerpetualAccess
    boolean hasPerpetualAccess = false
    boolean hasPublishComponent = false
    boolean isMultiYear = false

    //Only for Subscription with Type = Local
    boolean isAutomaticRenewAnnually = false

  String name
  String identifier
  Date startDate
  Date endDate
  Date manualRenewalDate
  Date manualCancellationDate
  String cancellationAllowances

  //Only for Consortia: ERMS-2098
  String comment

  Subscription instanceOf
  // If a subscription is administrative, subscription members will not see it resp. there is a toggle which en-/disables visibility
  boolean administrative = false

    String noticePeriod

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

  SortedSet issueEntitlements
  SortedSet packages
  SortedSet ids

  static hasMany = [
          ids                 : Identifier,
          packages            : SubscriptionPackage,
          issueEntitlements   : IssueEntitlement,
          documents           : DocContext,
          orgRelations        : OrgRole,
          prsLinks            : PersonRole,
          derivedSubscriptions: Subscription,
          pendingChanges      : PendingChange,
          propertySet         : SubscriptionProperty,
          //privateProperties: SubscriptionPrivateProperty,
          costItems           : CostItem,
          ieGroups            : IssueEntitlementGroup
  ]

  static mappedBy = [
                      ids: 'sub',
                      packages: 'subscription',
                      issueEntitlements: 'subscription',
                      documents: 'subscription',
                      orgRelations: 'sub',
                      prsLinks: 'sub',
                      derivedSubscriptions: 'instanceOf',
                      pendingChanges: 'subscription',
                      costItems: 'sub',
                      propertySet: 'owner',
                      //privateProperties: 'owner',
                      ]

    static transients = [
            'nameConcatenated', 'isSlavedAsString', 'provider', 'multiYearSubscription',
            'currentMultiYearSubscription', 'currentMultiYearSubscriptionNew', 'renewalDate',
            'commaSeperatedPackagesIsilList', 'calculatedPropDefGroups', 'allocationTerm',
            'subscriber', 'providers', 'agencies', 'consortia'
    ] // mark read-only accessor methods

    static mapping = {
        sort name: 'asc'
        id          column:'sub_id'
        version     column:'sub_version'
        globalUID   column:'sub_guid'
        status      column:'sub_status_rv_fk'
        type        column:'sub_type_rv_fk',        index: 'sub_type_idx'
        kind        column:'sub_kind_rv_fk'
        //owner       column:'sub_owner_license_fk',  index: 'sub_owner_idx'
        form        column:'sub_form_fk'
        resource    column:'sub_resource_fk'
        name        column:'sub_name'
        comment     column: 'sub_comment', type: 'text'
        identifier  column:'sub_identifier'
        startDate   column:'sub_start_date',        index: 'sub_dates_idx'
        endDate     column:'sub_end_date',          index: 'sub_dates_idx'
        manualRenewalDate       column:'sub_manual_renewal_date'
        manualCancellationDate  column:'sub_manual_cancellation_date'
        instanceOf              column:'sub_parent_sub_fk', index:'sub_parent_idx'
        administrative          column:'sub_is_administrative'
        isSlaved        column:'sub_is_slaved'
        hasPerpetualAccess column: 'sub_has_perpetual_access', defaultValue: false
        //hasPerpetualAccess column: 'sub_has_perpetual_access_rv_fk'
        hasPublishComponent column: 'sub_has_publish_component', defaultValue: false
        isPublicForApi  column:'sub_is_public_for_api', defaultValue: false
        lastUpdatedCascading column: 'sub_last_updated_cascading'

        noticePeriod    column:'sub_notice_period'
        isMultiYear column: 'sub_is_multi_year'
        isAutomaticRenewAnnually column: 'sub_is_automatic_renew_annually'
        pendingChanges  sort: 'ts', order: 'asc', batchSize: 10

        ids             sort: 'ns', batchSize: 10
        packages            batchSize: 10
        issueEntitlements   batchSize: 10
        documents           batchSize: 10
        orgRelations        batchSize: 10
        prsLinks            batchSize: 10
        derivedSubscriptions    batchSize: 10
        propertySet    batchSize: 10
        costItems           batchSize: 10
    }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        type        (nullable:true)
        kind        (nullable:true)
        //owner(nullable:true, blank:false)
        form        (nullable:true)
        resource    (nullable:true)
        startDate(nullable:true, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['startDateAfterEndDate']
            }
        })
        endDate(nullable:true, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['endDateBeforeStartDate']
            }
        })
        manualRenewalDate       (nullable:true)
        manualCancellationDate  (nullable:true)
        instanceOf              (nullable:true)
        comment(nullable: true, blank: true)
        //hasPerpetualAccess(nullable: true) keep in case has perpetual access becomes nullable
        noticePeriod(nullable:true, blank:true)
        cancellationAllowances(nullable:true, blank:true)
        lastUpdated(nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    @Override
    Collection<String> getLogIncluded() {
        [ 'name', 'startDate', 'endDate', 'manualCancellationDate', 'status', 'type', 'kind', 'form', 'resource', 'isPublicForApi', 'hasPerpetualAccess', 'hasPublishComponent' ]
    }
    @Override
    Collection<String> getLogExcluded() {
        [ 'version', 'lastUpdated', 'lastUpdatedCascading', 'pendingChanges' ]
    }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        BeanStorage.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()



    }
    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }

    /**
     * When updating a subscription instance, some changes may be reflected to member objects or issue entitlements as well:
     * <ul>
     *     <li>inherited attributes are passed to the member subscriptions</li>
     *     <li>if the perpetual access flag is being modified, this affects eventual issue entitlements as well</li>
     * </ul>
     */
    @Override
    def beforeUpdate() {
        Map<String, Object> changes = super.beforeUpdateHandler()
        log.debug ("beforeUpdate() " + changes.toMapString())

        if ((this._getCalculatedType() in [CalculatedType.TYPE_LOCAL, CalculatedType.TYPE_PARTICIPATION])
                && changes.oldMap.containsKey('hasPerpetualAccess')
                && changes.newMap.containsKey('hasPerpetualAccess')
                && changes.oldMap.hasPerpetualAccess != changes.newMap.hasPerpetualAccess) {
            if(changes.newMap.hasPerpetualAccess == true) {
                List<Long> ieIDs = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus and ie.perpetualAccessBySub is null', [sub: this, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED])
                if (ieIDs.size() > 0) {
                    log.debug("beforeUpdate() set perpetualAccessBySub of ${ieIDs.size()} IssueEntitlements to sub:" + this)
                    ieIDs.collate(32767).each {
                        IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.perpetualAccessBySub = :sub where ie.id in (:idList)", [sub: this, idList: it])
                    }
                }
            }else {
                List<Long> ieIDs = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus and ie.perpetualAccessBySub is not null', [sub: this, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED])
                if (ieIDs.size() > 0) {
                    log.debug("beforeUpdate() set perpetualAccessBySub of ${ieIDs.size()} IssueEntitlements to null:" + this)
                    ieIDs.collate(32767).each {
                        IssueEntitlement.executeUpdate("update IssueEntitlement ie set ie.perpetualAccessBySub = null where ie.id in (:idList)", [idList: it])
                    }


                }
            }
        }

        BeanStorage.getAuditService().beforeUpdateHandler(this, changes.oldMap, changes.newMap)
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Method to check if the correct shareable is being processed.
     * Is needed to differentiate OrgRoles
     * @param sharedObject the object to be shared
     * @return true if the object is an {@link OrgRole} and the share toggling was successful, false otherwise
     */
    @Override
    boolean checkSharePreconditions(ShareableTrait sharedObject) {
        if (sharedObject instanceof OrgRole) {
            if (showUIShareButton() && sharedObject.roleType.value in ['Provider', 'Agency']) {
                return true
            }
        }
        false
    }

    @Override
    boolean showUIShareButton() {
        _getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL]
    }

    /**
     * Toggles sharing for the given object
     * @param sharedObject the object whose sharing should be toggled
     */
    @Override
    void updateShare(ShareableTrait sharedObject) {
        log.debug('updateShare: ' + sharedObject)

        if (sharedObject instanceof DocContext) {
            if (sharedObject.isShared) {
                List<Subscription> newTargets = Subscription.findAllByInstanceOf(this)
                log.debug('found targets: ' + newTargets)

                newTargets.each{ sub ->
                    log.debug('adding for: ' + sub)
                    sharedObject.addShareForTarget_trait(sub)
                }
            }
            else {
                sharedObject.deleteShare_trait()
            }
        }
        if (sharedObject instanceof OrgRole) {
            if (sharedObject.isShared) {
                List<Subscription> newTargets = Subscription.findAllByInstanceOf(this)
                log.debug('found targets: ' + newTargets)

                newTargets.each{ sub ->
                    log.debug('adding for: ' + sub)

                    // ERMS-1185
                    if (sharedObject.roleType in [RDStore.OR_AGENCY, RDStore.OR_PROVIDER]) {
                        List<OrgRole> existingOrgRoles = OrgRole.findAll{
                            sub == sub && roleType == sharedObject.roleType && org == sharedObject.org
                        }
                        if (existingOrgRoles) {
                            log.debug('found existing orgRoles, deleting: ' + existingOrgRoles)
                            existingOrgRoles.each{ OrgRole tmp -> tmp.delete() }
                        }
                    }
                    sharedObject.addShareForTarget_trait(sub)
                }
            }
            else {
                sharedObject.deleteShare_trait()
            }
        }
    }

    /**
     * Processes each shareable object of this subscription and toggles sharing on each of them
     * @param targets the member objects on which new objects should be attached
     */
    @Override
    void syncAllShares(List<ShareSupport> targets) {
        log.debug('synAllShares: ' + targets)

        documents.each{ sharedObject ->
            targets.each{ sub ->
                if (sharedObject.isShared) {
                    log.debug('adding for: ' + sub)
                    sharedObject.addShareForTarget_trait(sub)
                }
                else {
                    log.debug('deleting all shares')
                    sharedObject.deleteShare_trait()
                }
            }
        }

        orgRelations.each{ sharedObject ->
            targets.each{ sub ->
                if (sharedObject.isShared) {
                    log.debug('adding for: ' + sub)
                    sharedObject.addShareForTarget_trait(sub)
                }
                else {
                    log.debug('deleting all shares')
                    sharedObject.deleteShare_trait()
                }
            }
        }
    }

    /**
     * Determines the actual type of this subscription:
     * <ul>
     *     <li>if there is an {@link OrgRole} with the type 'Subscription Consortia' and no {@link OrgRole}s with one of the
     *     types 'Subscriber', 'Subscriber_Consortial' or 'Subscriber_Consortial_Hidden' and no parent subscription ({@link #instanceOf}),
     *     then it may be administrative or consortial (parent), depending on the {@link #administrative} flag</li>
     *     <li>if there is an {@link OrgRole} with the type 'Subscription Consortia' and a parent subscription, then it is a consortial member subscription</li>
     *     <li>if there is an {@link OrgRole} with the type 'Subscriber' and no parent subscription, then it is a local subscription</li>
     * </ul>
     * The type controls many of the functions and grants linked to a subscription
     * @return the subscription type, depending on the specifications described above
     */
    @Override
    String _getCalculatedType() {
        String result = TYPE_UNKOWN

        if(getConsortia() && !getAllSubscribers() && !instanceOf) {
            if(administrative) {
                result = TYPE_ADMINISTRATIVE
            }
            else result = TYPE_CONSORTIAL
        }
        else if(getConsortia() && instanceOf) {
            result = TYPE_PARTICIPATION
        }
        // TODO remove type_local
        else if(getAllSubscribers() && !instanceOf) {
            result = TYPE_LOCAL
        }
        result
    }

    /**
     * Retrieves all organisation linked as providers to this subscription
     * @return a {@link List} of {@link Org}s linked as provider
     */
    List<Org> getProviders() {
        Org.executeQuery("select og.org from OrgRole og where og.sub =:sub and og.roleType = :provider",
            [sub: this, provider: RDStore.OR_PROVIDER])
    }

    /**
     * Retrieves all organisation linked as agencies to this subscription
     * @return a {@link List} of {@link Org}s linked as agency
     */
    List<Org> getAgencies() {
        Org.executeQuery("select og.org from OrgRole og where og.sub =:sub and og.roleType = :agency",
                [sub: this, agency: RDStore.OR_AGENCY])
    }

    // used for views and dropdowns
    @Deprecated
    String getNameConcatenated() {
        Org cons = getConsortia()
        List<Org> subscr = getAllSubscribers()
        if (subscr) {
            "${name} (" + subscr.join(', ') + ")"
        }
        else if (cons){
            "${name} (${cons})"
        }
        else {
            name
        }
    }

    /**
     * Outputs the {@link #isSlaved} flag as string; isSlaved means whether changes on a consortial parent subscription should be
     * passed directly to the members or if they need to be accepted before applying them
     * @return 'Yes' if isSlaved is true, 'No' otherwise
     */
    String getIsSlavedAsString() {
        isSlaved ? "Yes" : "No"
    }

    /**
     * Retrieves all linked licenses to this subscription
     * @return a {@link Set} of {@link License}s linked to this subscription
     */
    Set<License> getLicenses() {
        Set<License> result = []
        Links.findAllByDestinationSubscriptionAndLinkType(this,RDStore.LINKTYPE_LICENSE).each { l ->
            result << l.sourceLicense
        }
        result
    }

    /**
     * Gets the principal subscriber to this subscription
     * @return <ul>
     *     <li>if it is a local or consortial member license, the subscriber</li>
     *     <li>else if it is a consortial parent license, the consortium</li>
     * </ul>
     */
  Org getSubscriber() {
    Org result
    Org cons
    
    orgRelations.each { or ->
      if ( or.roleType.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id] )
        result = or.org
        
      if ( or.roleType.id == RDStore.OR_SUBSCRIPTION_CONSORTIA.id )
        cons = or.org
    }
    
    if ( !result && cons ) {
      result = cons
    }
    
    result
  }

    /**
     * Retrieves all subscribers to this subscription
     * @return a {@link List} of institutions ({@link Org}) subscribing this subscription
     */
    List<Org> getAllSubscribers() {
        List<Org> result = []
        orgRelations.each { OrgRole or ->
            if ( or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN] )
                result.add(or.org)
            }
        result
    }

    /**
     * Gets the content provider of this subscription
     * @return the {@link Org} linked to this subscription as 'Content Provider'; if several orgs are linked that way, the last one is being returned
     */
    Org getProvider() {
        Org result
        orgRelations.each { OrgRole or ->
            if ( or.roleType == RDStore.OR_CONTENT_PROVIDER )
                result = or.org
            }
        result
    }

    /**
     * Gets the consortium of this subscription
     * @return the {@link Org} linked as 'Subscription Consortia' to this subscription or null if none exists (this is the case for local subscriptions)
     */
    Org getConsortia() { // TODO getConsortium()
        Org result = orgRelations.find { OrgRole oo -> oo.roleType == RDStore.OR_SUBSCRIPTION_CONSORTIA }?.org //null check necessary because of single users!
        result
    }

    /**
     * Gets all members of this consortial subscription
     * @return a {@link List} of {@link Org}s which are linked to child instances of this subscription by 'Subscriber' or 'Subscriber_Consortial'
     */
    List<Org> getDerivedSubscribers() {
        List<Subscription> subs = Subscription.findAllByInstanceOf(this)
        //OR_SUBSCRIBER is legacy; the org role types are distinct!
        subs.isEmpty() ? [] : OrgRole.findAllBySubInListAndRoleTypeInList(subs, [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS], [sort: 'org.name']).collect{it.org}
    }

    /**
     * Gets the subscription marked as preceding to this subscription; usually the one of the last year ring
     * @return the subscription linked to this subscription with type 'Follows'
     */
    Subscription _getCalculatedPrevious() {
        Links match = Links.findBySourceSubscriptionAndLinkType(this, RDStore.LINKTYPE_FOLLOWS)
        return match ? match.destinationSubscription : null
    }

    /**
     * Gets the subscription marked as following to this subscription; usually the one of the next year ring
     * @return the subscription linking to this subscription with type 'Follows'
     */
    Subscription _getCalculatedSuccessor() {
        Links match = Links.findByDestinationSubscriptionAndLinkType(this,RDStore.LINKTYPE_FOLLOWS)
        return match ? match.sourceSubscription : null
    }

    /**
     * Checks if the subscription has a running time beyond a year ring
     * @return true if the running time is beyond 366 days spanning thus more than one year, false otherwise
     */
    boolean isMultiYearSubscription() {
        return (this.startDate && this.endDate && (this.endDate.minus(this.startDate) > 366))
    }

    @Deprecated
    boolean isCurrentMultiYearSubscription() {
        //Date currentDate = new Date()
        //println(this.endDate.minus(currentDate))
        //return (this.isMultiYearSubscription() && this.endDate && (this.endDate.minus(currentDate) > 366))

        LocalDate endDate = MigrationHelper.dateToLocalDate(this.endDate)
        return (this.isMultiYearSubscription() && endDate && (endDate.minus(LocalDate.now()) > 366))
    }

    /**
     * Checks if this subscription is a multi-year subscription and if we are in the time range spanned by the subscription
     * @return true if we are within the given multi-year range, false otherwise
     */
    boolean isCurrentMultiYearSubscriptionNew() {
        //Date currentDate = new Date()
        //println(this.endDate.minus(currentDate))
        //return (this.isMultiYear && this.endDate && (this.endDate.minus(currentDate) > 366))

        LocalDate endDate = MigrationHelper.dateToLocalDate(this.endDate)
        return (this.isMultiYear && endDate && (endDate.minus(LocalDate.now()) > 366))
    }

    boolean isCurrentMultiYearSubscriptionToParentSub() {
        //return (this.isMultiYear && this.endDate && this.instanceOf && (this.endDate.minus(this.instanceOf.startDate) > 366))

        LocalDate endDate = MigrationHelper.dateToLocalDate(this.endDate)
        return (this.isMultiYear && endDate && this.instanceOf && (endDate.minus(MigrationHelper.dateToLocalDate(this.instanceOf.startDate)) > 366))
    }

    @Deprecated
    boolean islateCommer() {
        //return (this.endDate && (this.endDate.minus(this.startDate) > 366 && this.endDate.minus(this.startDate) < 728))

        LocalDate endDate = MigrationHelper.dateToLocalDate(this.endDate)
        LocalDate startDate = MigrationHelper.dateToLocalDate(this.startDate)
        return (endDate && (endDate.minus(startDate) > 366 && endDate.minus(startDate) < 728))
    }

    /**
     * Checks if the local subscription is configured to be renewed annually
     * @return true if this subscription is a local subscription and the running time is between 364 and 366 days (to include leap years as well)
     */
    boolean isAllowToAutomaticRenewAnnually() {
        //return (this.type == RDStore.SUBSCRIPTION_TYPE_LOCAL && this.startDate && this.endDate && (this.endDate.minus(this.startDate) > 363) && (this.endDate.minus(this.startDate) < 367))

        LocalDate endDate = MigrationHelper.dateToLocalDate(this.endDate)
        LocalDate startDate = MigrationHelper.dateToLocalDate(this.startDate)
        return (this.type == RDStore.SUBSCRIPTION_TYPE_LOCAL && startDate && endDate && (endDate.minus(startDate) > 363) && (endDate.minus(startDate) < 367))
    }

    /**
     * Checks if this subscription is editable by the given user
     * @param user the {@link User} whose grants should be checked
     * @return true if this subscription is editable, false otherwise
     */
    boolean isEditableBy(user) {
        hasPerm('edit', user)
    }

    /**
     * Checks if this subscription is viewable by the given user
     * @param user the {@link User} whose grants should be checked
     * @return true if this subscription is viewable, false otherwise
     */
    boolean isVisibleBy(user) {
        hasPerm('view', user)
    }

    /**
     * Checks if the given permission has been granted to the given user for this subscription
     * @param perm the permission string to check the grant of
     * @param user the {@link User} whose right should be checked
     * @return true if the given permission has been granted to the given user for this subscription, false otherwise
     */
    boolean hasPerm(perm, user) {
        Role adm = Role.findByAuthority('ROLE_ADMIN')
        Role yda = Role.findByAuthority('ROLE_YODA')

        if (user.getAuthorities().contains(adm) || user.getAuthorities().contains(yda)) {
            return true
        }

        Org contextOrg = BeanStorage.getContextService().getOrg()
        if (user.getAuthorizedOrgsIds().contains(contextOrg?.id)) {

            OrgRole cons = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextOrg, RDStore.OR_SUBSCRIPTION_CONSORTIA
            )
            OrgRole subscrCons = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextOrg, RDStore.OR_SUBSCRIBER_CONS
            )
            OrgRole subscr = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextOrg, RDStore.OR_SUBSCRIBER
            )

            if (perm == 'view') {
                return cons || subscrCons || subscr
            }
            if (perm == 'edit') {
                if (BeanStorage.getAccessService().checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN'))
                    return cons || subscr
            }
        }

        return false
    }

    /**
     * Method used by generic call. Propagates changes to a consortial subscription to member objects, i.e. triggers inheritance
     * @param changeDocument the change map as JSON document which will be passed to the child objects
     */
    @Transient
    def notifyDependencies(changeDocument) {
        log.debug("notifyDependencies(${changeDocument})")

        List<PendingChange> slavedPendingChanges = []
        List<Subscription> derived_subscriptions = getNonDeletedDerivedSubscriptions()

        derived_subscriptions.each { ds ->

            log.debug("Send pending change to ${ds.id}")

            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
            String description = BeanStorage.getMessageSource().getMessage('default.accept.placeholder',null, locale)
            String definedType = 'text'

            if (this."${changeDocument.prop}" instanceof RefdataValue) {
                definedType = 'rdv'
            }
            else if (this."${changeDocument.prop}" instanceof Date) {
                definedType = 'date'
            }

            def msgParams = [
                    definedType,
                    "${changeDocument.prop}",
                    "${changeDocument.old}",
                    "${changeDocument.new}",
                    "${description}"
            ]

            PendingChange newPendingChange = BeanStorage.getChangeNotificationService().registerPendingChange(
                    PendingChange.PROP_SUBSCRIPTION,
                    ds,
                    ds.getSubscriber(),
                    [
                            changeTarget:"${Subscription.class.name}:${ds.id}",
                            changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                            changeDoc:changeDocument
                    ],
                    PendingChange.MSG_SU01,
                    msgParams,
                    "<strong>${changeDocument.prop}</strong> hat sich von <strong>\"${changeDocument.oldLabel?:changeDocument.old}\"</strong> zu <strong>\"${changeDocument.newLabel?:changeDocument.new}\"</strong> von der Lizenzvorlage geändert. " + description
            )

            if (newPendingChange && ds.isSlaved) {
                slavedPendingChanges << newPendingChange
            }
        }

        slavedPendingChanges.each { spc ->
            log.debug('autoAccept! performing: ' + spc)
            BeanStorage.getPendingChangeService().performAccept(spc)
        }
    }

    /**
     * Retrieves all member objects of this subscription, i.e. subscriptions which are instance of this subscription
     * @return a {@link List} of member subscriptions
     */
    List<Subscription> getNonDeletedDerivedSubscriptions() {

        Subscription.where { instanceOf == this }.findAll()
    }

    /**
     * Retrieves the property definition groups defined by the given institution for this subscription
     * @param contextOrg the institution whose property groups should be retrieved
     * @return the {@link PropertyDefinitionGroup}s for this subscription, defined by the given institution
     */
    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        BeanStorage.getPropertyService().getCalculatedPropDefGroups(this, contextOrg)
    }

    /**
     * Outputs this subscription's name and core data for labelling
     * @return the concatenated label of this subscription
     */
    String getLabel() {
        SimpleDateFormat sdf = DateUtils.getSDF_dmy()
        name + ' (' + (startDate ? sdf.format(startDate) : '') + ' - ' + (endDate ? sdf.format(endDate) : '') + ')'
    }

    /**
     * Returns a simple string representation of this subscription
     * @return if a name exists: the name, otherwise 'Subscription {database id}'
     */
  String toString() {
      name ? "${name}" : "Subscription ${id}"
  }

  /**
   * JSON definition of the subscription object
   * see http://manbuildswebsite.com/2010/02/15/rendering-json-in-grails-part-3-customise-your-json-with-object-marshallers/
   * Also http://jwicz.wordpress.com/2011/07/11/grails-custom-xml-marshaller/
   */
  static {
    grails.converters.JSON.registerObjectMarshaller(User) {
      // you can filter here the key-value pairs to output:
      return it?.properties.findAll {k,v -> k != 'passwd'}
    }
  }

    /**
     * Returns the renewal date of this subscription
     * @return the manual renewal date
     */
  Date getRenewalDate() {
    manualRenewalDate
  }

  /**
   * Retrieves a list of subscriptions for dropdown display. The display can be parametrised, possible options are:
   * startDate, endDate, hideIdent, inclSubStartDate, hideDeleted, accessibleToUser, inst_shortcode
   * @param the display and filter parameter map
   * @return a {@link List} of {@link Map}s of structure [id: oid, text: subscription text] with the query results
   */
  @Transient
  static def refdataFind(GrailsParameterMap params) {
      List<Map<String, Object>> result = []

      String hqlString = "select sub from Subscription sub where lower(sub.name) like :name "
    def hqlParams = [name: ((params.q ? params.q.toLowerCase() : '' ) + "%")]
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
      RefdataValue cons_role        = RDStore.OR_SUBSCRIPTION_CONSORTIA
      RefdataValue subscr_role      = RDStore.OR_SUBSCRIBER
      RefdataValue subscr_cons_role = RDStore.OR_SUBSCRIBER_CONS
    def viableRoles = [cons_role, subscr_role, subscr_cons_role]
    
    hqlParams.put('viableRoles', viableRoles)

    if(params.hasDate ){

      def startDate = params.startDate.length() > 1 ? sdf.parse(params.startDate) : null
      def endDate = params.endDate.length() > 1 ? sdf.parse(params.endDate)  : null

      if(startDate){
          hqlString += " AND sub.startDate >= :startDate "
          hqlParams.put('startDate', startDate)
      }
      if(endDate){
        hqlString += " AND sub.endDate <= :endDate "
        hqlParams.put('endDate', endDate)
        }
    }

    if(params.inst_shortcode && params.inst_shortcode.length() > 1){
      hqlString += " AND exists ( select orgs from sub.orgRelations orgs where orgs.org.shortcode = :inst AND orgs.roleType IN (:viableRoles) ) "
      hqlParams.put('inst', params.inst_shortcode)
    }


    def results = Subscription.executeQuery(hqlString,hqlParams)

    if(params.accessibleToUser){
      for(int i=0;i<results.size();i++){
        if(! results.get(i).checkPermissionsNew("view",User.get(params.accessibleToUser))){
          results.remove(i)
        }
      }
    }

    results?.each { t ->
      String resultText = t.name
      def date = t.startDate ? " (${sdf.format(t.startDate)})" : ""
      resultText = params.inclSubStartDate == "true"? resultText + date : resultText
      resultText = params.hideIdent == "true"? resultText : resultText + " (${t.identifier})"
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result
  }

    @Deprecated
  def setInstitution(Org inst) {
      log.debug("Set institution ${inst}")

      OrgRole or = new OrgRole(org:inst, roleType:RDStore.OR_SUBSCRIBER, sub:this)
      if (this.orgRelations == null) {
        this.orgRelations = []
      }
     this.orgRelations.add(or)
  }

    /**
     * Retrieves a list ISILs of packages linked to this subscription.
     * Called from issueEntitlement/show and subscription/show, is part of the Nationaler Statistikserver statistics component
     * @return a {@link List} of ISIL identifier strings
     * @see SubscriptionPackage
     * @see Package
     */
  String getCommaSeperatedPackagesIsilList() {
      List<String> result = []
      packages.each { it ->
          String identifierValue = it.pkg.getIdentifierByType('isil')?.value ?: null
          if (identifierValue) {
              result += identifierValue
          }
      }
      result.join(',')
  }

    /**
     * Checks if there is a platform linked to this subscription which contains a Nationaler Statistikserver supplier ID
     * Is part of the Nationaler Statistikserver statistics component
     * @return true if there is a platform with a NatStat supplier ID linked to this subscription, false otherwise
     */
  boolean hasPlatformWithUsageSupplierId() {
      boolean hasUsageSupplier = false
      packages.each { it ->
          String hql="select count(distinct sp) from SubscriptionPackage sp "+
              "join sp.subscription.orgRelations as or "+
              "join sp.pkg.tipps as tipps "+
              "where sp.id=:sp_id "
              "and exists (select 1 from CustomProperties as cp where cp.owner = tipps.platform.id and cp.type.name = 'NatStat Supplier ID')"
          def queryResult = OrgRole.executeQuery(hql, ['sp_id':it.id])
          if (queryResult[0] > 0){
              hasUsageSupplier = true
          }
      }
      return hasUsageSupplier
  }

    /**
     * Retrieves a set of access points linked to this subscription and attached to the given institution and platform
     * @param org the institution ({@link Org}) who created the access point
     * @param platform the {@link Platform} to which the access point link is attached to
     * @return a set (as {@link List} with distinct in query) of access point links
     * @see OrgAccessPoint
     */
  def deduplicatedAccessPointsForOrgAndPlatform(org, platform) {
      String hql = """
select distinct oap from OrgAccessPoint oap 
    join oap.oapp as oapl
    join oapl.subPkg as subPkg
    join subPkg.subscription as sub
    where sub=:sub
    and oap.org=:org
    and oapl.active=true
    and oapl.platform=:platform
    
"""
      return OrgAccessPoint.executeQuery(hql, [sub:this, org:org, platform:platform])
  }

    /**
     * Substitution call for {@link #dropdownNamingConvention(java.lang.Object)} with the context institution
     * @return this subscription's name according to the dropdown naming convention (<a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">see here</a>)
     */
  String dropdownNamingConvention() {
      dropdownNamingConvention(BeanStorage.getContextService().getOrg())
  }

    /**
     * Displays this subscription's name according to the dropdown naming convention as specified <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @param contextOrg the institution whose perspective should be taken
     * @return this subscription's name according to the dropdown naming convention
     */
  String dropdownNamingConvention(contextOrg){
       SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
       String period = startDate ? sdf.format(startDate)  : ''

       period = endDate ? period + ' - ' + sdf.format(endDate)  : ''

       period = period ? '('+period+')' : ''

       String statusString = status ? status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

       if(instanceOf) {
           def additionalInfo
           Map<Long,Org> orgRelationsMap = [:]
           orgRelations.each { or ->
               orgRelationsMap.put(or.roleType.id,or.org)
           }
           if(orgRelationsMap.get(RDStore.OR_SUBSCRIPTION_CONSORTIA.id)?.id == contextOrg.id) {
               if(orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS.id))
                   additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS.id)?.sortname
               else if(orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id))
                   additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id)?.sortname
           }
           else{
               additionalInfo = BeanStorage.getMessageSource().getMessage('gasco.filter.consortialLicence',null, LocaleContextHolder.getLocale())
           }


           return name + ' - ' + statusString + ' ' +period + ' - ' + additionalInfo

       } else {

           return name + ' - ' + statusString + ' ' +period
       }
  }

    @Deprecated
    String dropdownNamingConventionWithoutOrg() {
        dropdownNamingConventionWithoutOrg(BeanStorage.getContextService().getOrg())
    }

    @Deprecated
    String dropdownNamingConventionWithoutOrg(Org contextOrg){
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        String period = startDate ? sdf.format(startDate)  : ''

        period = endDate ? period + ' - ' + sdf.format(endDate)  : ''
        period = period ? '('+period+')' : ''

        String statusString = status ? status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        return name + ' - ' + statusString + ' ' +period
    }

    /**
     * Gets the subscription which is instance of this subscription (i.e. member subscriptions of this consortial parent subscription)
     * and where the given institution is the subscriber
     * @param org the member {@link Org} whose subscription should be retrieved
     * @return the member subscription of the subscriber
     */
    Subscription getDerivedSubscriptionBySubscribers(Org org) {
        Subscription result

        Subscription.findAllByInstanceOf(this).each { s ->
            List<OrgRole> ors = OrgRole.findAllWhere( sub: s )
            ors.each { OrgRole or ->
                if (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS] && or.org.id == org.id) {
                    result = or.sub
                }
            }
        }
        result
    }

    /**
     * Retrieves the running time (= allocation term) of this subscription
     * @return the time of span covered by this subscription; if it is a multi year subscription, the parent subscription is being consulted which covers the whole allocation term
     */
    def getAllocationTerm() {
        def result = [:]

        if(isMultiYear) {
            result.startDate = startDate
            result.endDate = instanceOf ? ((endDate == instanceOf.endDate) ? endDate : instanceOf.endDate) : endDate
        }
        else {
            result.startDate = startDate
            result.endDate = endDate
        }

        result
    }

    /**
     * Retrieves all access points of this subscription's subscriber
     * @return a {@link Collection} of {@link OrgAccessPoint}s linked to the subscriber {@link Org}
     */
    Collection<OrgAccessPoint> getOrgAccessPointsOfSubscriber() {
        Collection<OrgAccessPoint> result = []

        result = this.getSubscriber()?.accessPoints

        result
    }

}
