package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractBase
import de.laser.exceptions.EntitlementCreationException
import de.laser.finance.PriceItem
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.wekb.TIPPCoverage
import de.laser.wekb.TitleInstancePackagePlatform
import groovy.util.logging.Slf4j

import javax.persistence.Transient

/**
 * A title record within a local holding. Technically a {@link de.laser.wekb.TitleInstancePackagePlatform} record entry with a {@link Subscription} foreign key. But there are a few more things to note:
 * The individually negotiated subscription holding may differ from what a provider offers usually. Those differences must be reflected in the issue entitlement record; that is why there are some
 * fields in both classes. In detail:
 * <ul>
 *     <li>access start/end may be different</li>
 *     <li>the subscribing institution may have a perpetual access negotiated to the title; this is of course no global property</li>
 *     <li>there may be locally negotiated prices in addition to the global list price (the {@link de.laser.finance.PriceItem}s linked to the owning {@link de.laser.wekb.TitleInstancePackagePlatform}; that is why issue entitlements and TIPPs have an individual set of price items)</li>
 *     <li>there may be coverage entries differing from global level ({@link IssueEntitlementCoverage} vs {@link de.laser.wekb.TIPPCoverage})</li>
 * </ul>
 * Moreover, issue entitlements may be grouped for that the subscribing institution may organise them by certain criteria e.g. subscription phase, title group etc.
 * @see IssueEntitlementCoverage
 * @see IssueEntitlementGroup
 * @see IssueEntitlementGroupItem
 * @see de.laser.finance.PriceItem
 * @see de.laser.wekb.TitleInstancePackagePlatform
 * @see Subscription
 */
@Slf4j
class IssueEntitlement extends AbstractBase implements Comparable {

    Date accessStartDate
    Date accessEndDate

    String notes

    Subscription perpetualAccessBySub

    //merged as the difference between an IssueEntitlement and a TIPP is mainly former's attachment to a subscription, otherwise, they are functionally identical, even dependent upon each other. So why keep different refdata categories?
    @RefdataInfo(cat = RDConstants.TIPP_STATUS)
    RefdataValue status

    Date dateCreated
    Date lastUpdated

    static belongsTo = [subscription: Subscription, tipp: TitleInstancePackagePlatform]

    static hasMany = [coverages: IssueEntitlementCoverage,
                      ieGroups: IssueEntitlementGroupItem,
                      priceItems: PriceItem]

    static mappedBy = [priceItems: 'issueEntitlement']

    @Transient
    def comparisonProps = ['derivedAccessStartDate', 'derivedAccessEndDate',
    'coverageNote','coverageDepth','embargo','startVolume','startIssue','startDate','endDate','endIssue','endVolume']

    int compareTo(obj) {
        int cmp
        if(tipp.sortname && obj.tipp.sortname)
            cmp = tipp.sortname <=> obj.tipp.sortname
        else if(tipp.name && obj.tipp.name)
            cmp = tipp.name <=> obj.tipp.name
        if(cmp == 0)
            return id.compareTo(obj.id)
        return cmp
    }

    static transients = ['derivedAccessStartDate', 'derivedAccessEndDate', 'availabilityStatus'] // mark read-only accessor methods

    static mapping = {
                id column:'ie_id'
           laserID column:'ie_laser_id'
           version column:'ie_version'
             notes column:'ie_notes', type: 'text'
            status column:'ie_status_rv_fk', index: 'ie_status_idx, ie_sub_tipp_status_idx, ie_status_accept_status_idx, ie_tipp_status_accept_status_idx'
      subscription column:'ie_subscription_fk', index: 'ie_sub_idx, ie_sub_tipp_idx, ie_sub_tipp_status_idx, ie_status_accept_status_idx, ie_tipp_status_accept_status_idx'
              tipp column:'ie_tipp_fk',         index: 'ie_tipp_idx, ie_sub_tipp_idx, ie_sub_tipp_status_idx, ie_tipp_status_accept_status_idx'
        perpetualAccessBySub column:'ie_perpetual_access_by_sub_fk', index: 'ie_perpetual_access_by_sub_idx'
   accessStartDate column:'ie_access_start_date'
     accessEndDate column:'ie_access_end_date'
         coverages sort: 'startDate', order: 'asc'

    dateCreated column: 'ie_date_created'
    lastUpdated column: 'ie_last_updated'

    }

    static constraints = {
        laserID        (nullable: true, blank: false, unique:true, maxSize:255)

        notes          (nullable:true)
        accessStartDate(nullable:true)
        accessEndDate  (nullable:true)

        lastUpdated (nullable: true)
        perpetualAccessBySub (nullable: true)
    }

    /**
     * Constructs a new issue entitlement record with the given configuration map
     * @param configMap the parameter map containing the new holding's properties
     * @return a new or updated issue entitlement
     * @throws EntitlementCreationException
     */
  static IssueEntitlement construct(Map<String,Object> configMap) throws EntitlementCreationException {
    if(configMap.subscription instanceof Subscription && configMap.tipp instanceof TitleInstancePackagePlatform) {
        log.debug("creating new issue entitlement for ${configMap.tipp} and ${configMap.subscription}")
      Subscription subscription = (Subscription) configMap.subscription
      TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) configMap.tipp
      IssueEntitlement ie = findBySubscriptionAndTippAndStatusNotEqual(subscription,tipp, RDStore.TIPP_STATUS_REMOVED)
      if(!ie && !PermanentTitle.findByOwnerAndTipp(subscription.getSubscriberRespConsortia(), tipp)) {
          ie = new IssueEntitlement(subscription: subscription, tipp: tipp, medium: tipp.medium, status:tipp.status, accessType: tipp.accessType, openAccess: tipp.openAccess, name: tipp.name)
          //ie.generateSortTitle()
      }
        if(ie) {
            if (ie.save()) {

                if (subscription.hasPerpetualAccess && ie.status != RDStore.TIPP_STATUS_EXPECTED) {
                    ie.perpetualAccessBySub = subscription

                    if (!PermanentTitle.findByOwnerAndTipp(subscription.getSubscriberRespConsortia(), tipp)) {
                        PermanentTitle permanentTitle = new PermanentTitle(subscription: subscription,
                                issueEntitlement: ie,
                                tipp: tipp,
                                owner: subscription.getSubscriberRespConsortia()).save()
                    }
                }

                Set<TIPPCoverage> tippCoverages = TIPPCoverage.findAllByTipp(tipp)
                if (tippCoverages) {
                    tippCoverages.each { TIPPCoverage tc ->
                        IssueEntitlementCoverage ic = new IssueEntitlementCoverage(issueEntitlement: ie)
                        ic.startDate = tc.startDate
                        ic.startVolume = tc.startVolume
                        ic.startIssue = tc.startIssue
                        ic.endDate = tc.endDate
                        ic.endVolume = tc.endVolume
                        ic.endIssue = tc.endIssue
                        ic.coverageDepth = tc.coverageDepth
                        ic.coverageNote = tc.coverageNote
                        ic.embargo = tc.embargo
                        if (!ic.save())
                            throw new EntitlementCreationException(ic.errors)
                    }
                }
                log.debug("creating price items for ${tipp}")
                Set<PriceItem> tippPriceItems = PriceItem.findAllByTipp(tipp)
                if (tippPriceItems) {
                    tippPriceItems.each { PriceItem tp ->
                        PriceItem ip = new PriceItem(issueEntitlement: ie)
                        ip.startDate = tp.startDate
                        ip.endDate = tp.endDate
                        ip.listPrice = tp.listPrice
                        ip.listCurrency = tp.listCurrency
                        ip.setLaserID()
                        if (!ip.save())
                            throw new EntitlementCreationException(ip.errors)
                    }
                }
            } else
                throw new EntitlementCreationException(ie.errors)
        }
      ie
    }
    else throw new EntitlementCreationException("Issue entitlement creation attempt without valid subscription and TIPP references! This is not allowed!")
  }

    @Override
    def beforeInsert() {
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Call to delete the given issue entitlement from the ElasticSearch index
     */
  void afterDelete() {
      BeanStore.getDeletionService().deleteDocumentFromIndex(this.laserID, this.class.simpleName)
  }

    /**
     * Comparator method; compares two issue entitlements against a controlled set of properties
     * @param ieB the other issue entitlement to compare against
     * @return the comparison result
     * @deprecated the method itself may be useful, but neither the interface Comparable is being implemented nor is the set of properties up to date
     */
  @Deprecated
  @Transient
  int compare(IssueEntitlement ieB){
    if(ieB == null) return -1;

    def noChange =true 
    comparisonProps.each{ noChange &= this."${it}" == ieB."${it}" }

    if(noChange) return 0;
    return 1;
  }

}
