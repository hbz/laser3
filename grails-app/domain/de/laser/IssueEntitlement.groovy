package de.laser


import de.laser.base.AbstractBase
import de.laser.exceptions.EntitlementCreationException
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.annotations.RefdataInfo
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import groovy.util.logging.Slf4j

import javax.persistence.Transient
import java.text.Normalizer

/**
 * A title record within a local holding. Technically a {@link TitleInstancePackagePlatform} record entry with a {@link Subscription} foreign key. But there are a few more things to note:
 * The individually negotiated subscription holding may differ from what a provider offers usually. Those differences must be reflected in the issue entitlement record; that is why there are some
 * fields in both classes. In detail:
 * <ul>
 *     <li>acces start/end may be different</li>
 *     <li>name</li>
 *     <li>the subscribing institution may have a perpetual access negotiated to the title; this is of course no global property</li>
 *     <li>prices may differ from list prices on global level (the {@link PriceItem}s linked to the owning {@link TitleInstancePackagePlatform}; that is why issue entitlements and TIPPs have an individual set of price items)</li>
 *     <li>coverage entities may differ from global level ({@link IssueEntitlementCoverage} vs {@link TIPPCoverage})</li>
 * </ul>
 * Moreover, issue entitlements may be grouped for that the subscribing institution may organise them by certain criteria e.g. subscription phase, title group etc.
 * @see IssueEntitlementCoverage
 * @see IssueEntitlementGroup
 * @see IssueEntitlementGroupItem
 * @see PriceItem
 * @see TitleInstancePackagePlatform
 * @see Subscription
 */
@Slf4j
class IssueEntitlement extends AbstractBase implements Comparable {

    @Deprecated
    Date coreStatusStart
    @Deprecated
    Date coreStatusEnd

    Date accessStartDate
    Date accessEndDate

    String name
    String sortname

    @Deprecated
    String ieReason

    Subscription perpetualAccessBySub

    //merged as the difference between an IssueEntitlement and a TIPP is mainly former's attachment to a subscription, otherwise, they are functionally identical, even dependent upon each other. So why keep different refdata categories?
    @RefdataInfo(cat = RDConstants.TIPP_STATUS)
    RefdataValue status

    @Deprecated
    @RefdataInfo(cat = RDConstants.CORE_STATUS)
    RefdataValue coreStatus // core Status is really core Medium.. dont ask

    @Deprecated
    @RefdataInfo(cat = RDConstants.TITLE_MEDIUM)
    RefdataValue medium // legacy; was distinguished back then; I see no reason why I should still do so. Is legacy.

    @RefdataInfo(cat = RDConstants.IE_ACCEPT_STATUS)
    RefdataValue acceptStatus

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
        if(sortname && obj.sortname)
            cmp = sortname <=> obj.sortname
        else if(name && obj.name) cmp = name <=> obj.name
        if(cmp == 0)
            return id.compareTo(obj.id)
        return cmp
    }

    static transients = ['derivedAccessStartDate', 'derivedAccessEndDate', 'availabilityStatus'] // mark read-only accessor methods

    static mapping = {
                id column:'ie_id'
         globalUID column:'ie_guid'
           version column:'ie_version'
              name column:'ie_name', type: 'text'
          sortname column:'ie_sortname', type: 'text'
            status column:'ie_status_rv_fk', index: 'ie_status_idx'
      subscription column:'ie_subscription_fk', index: 'ie_sub_idx, ie_sub_tipp_idx'
              tipp column:'ie_tipp_fk',         index: 'ie_tipp_idx, ie_sub_tipp_idx'
          ieReason column:'ie_reason'
        perpetualAccessBySub column:'ie_perpetual_access_by_sub_fk'
            medium column:'ie_medium_rv_fk', index: 'ie_medium_idx'
    accessStartDate column:'ie_access_start_date'
     accessEndDate column:'ie_access_end_date'
         coverages sort: 'startDate', order: 'asc'
      acceptStatus column:'ie_accept_status_rv_fk', index: 'ie_accept_status_idx'

    dateCreated column: 'ie_date_created'
    lastUpdated column: 'ie_last_updated'

    }

    static constraints = {
        globalUID      (nullable:true, blank:false, unique:true, maxSize:255)
        status         (nullable:true)
        ieReason       (nullable:true, blank:true)
        name           (nullable:true)
        sortname       (nullable:true)
        medium         (nullable:true)
        accessStartDate(nullable:true)
        accessEndDate  (nullable:true)
        coreStatus     (nullable:true)
        coreStatusStart(nullable:true)
        coreStatusEnd  (nullable:true)
        acceptStatus   (nullable:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
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
      IssueEntitlement ie = findBySubscriptionAndTipp(subscription,tipp)
      if(!ie) {
          ie = new IssueEntitlement(subscription: subscription, tipp: tipp, medium: tipp.medium, status:tipp.status, acceptStatus: configMap.acceptStatus, name: tipp.name)
          ie.generateSortTitle()
      }
      if(ie.save()) {

          if(subscription.hasPerpetualAccess){
              ie.perpetualAccessBySub = subscription
          }

          Set<TIPPCoverage> tippCoverages = TIPPCoverage.findAllByTipp(tipp)
        if(tippCoverages) {
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
            if(!ic.save())
              throw new EntitlementCreationException(ic.errors)
          }
        }
          log.debug("creating price items for ${tipp}")
          Set<PriceItem> tippPriceItems = PriceItem.findAllByTipp(tipp)
        if(tippPriceItems) {
            tippPriceItems.each { PriceItem tp ->
                PriceItem ip = new PriceItem(issueEntitlement: ie)
                ip.startDate = tp.startDate
                ip.endDate = tp.endDate
                ip.listPrice = tp.listPrice
                ip.listCurrency = tp.listCurrency
                ip.setGlobalUID()
                if(!ip.save())
                    throw new EntitlementCreationException(ip.errors)
            }
        }
      }
      else
        throw new EntitlementCreationException(ie.errors)
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

  void afterDelete() {
      BeanStore.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
  }

    /**
     * Removes stopwords from the title and generates a sortable title string.
     * @see Normalizer.Form#NFKD
     */
    void generateSortTitle() {
        if ( name ) {
            sortname = Normalizer.normalize(name, Normalizer.Form.NFKD).trim().toLowerCase()
            sortname = sortname.replaceFirst('^copy of ', '')
            sortname = sortname.replaceFirst('^the ', '')
            sortname = sortname.replaceFirst('^a ', '')
            sortname = sortname.replaceFirst('^der ', '')
            sortname = sortname.replaceFirst('^die ', '')
            sortname = sortname.replaceFirst('^das ', '')
        }
    }

  @Deprecated
  Date getDerivedAccessStartDate() {
      if(accessStartDate)
          accessStartDate
      else if(subscription.startDate)
          subscription.startDate
      else if(tipp.accessStartDate)
          tipp.accessStartDate
  }
  @Deprecated
  Date getDerivedAccessEndDate() {
      if(accessEndDate)
          accessEndDate
      else if(subscription.endDate)
          subscription.endDate
      else if(tipp.accessEndDate)
          tipp.accessEndDate
  }
  @Deprecated
  RefdataValue getAvailabilityStatus() {
    getAvailabilityStatus(new Date())
  }

  @Transient
  int compare(IssueEntitlement ieB){
    if(ieB == null) return -1;

    def noChange =true 
    comparisonProps.each{ noChange &= this."${it}" == ieB."${it}" }

    if(noChange) return 0;
    return 1;
  }
  @Deprecated
  RefdataValue getAvailabilityStatus(Date as_at) {
      RefdataValue result
      // If StartDate <= as_at <= EndDate - Current
      // if Date < StartDate - Expected
      // if Date > EndDate - Expired
      Date ie_access_start_date = getDerivedAccessStartDate()
      Date ie_access_end_date = getDerivedAccessEndDate()

      result = RDStore.IE_ACCESS_CURRENT

      if (ie_access_start_date && as_at < ie_access_start_date ) {
        result = RefdataValue.getByValueAndCategory('Expected', RDConstants.IE_ACCESS_STATUS)
      }
      else if (ie_access_end_date && as_at > ie_access_end_date ) {
          if(!subscription.hasPerpetualAccess)
              result = RefdataValue.getByValueAndCategory('Expired', RDConstants.IE_ACCESS_STATUS)
      }

      result
  }

    /**
     * Currently unused, is subject of refactoring.
     * Retrieves usage details for the title to which this issue entitlement is linked
     * @param date the month for which usage should be retrieved
     * @param subscriber the subscriber institution ({@link Org}) whose report should be retrieved
     * @return the first available usage report, TODO: extend with metricType and reportType
     */
    def getCounterReport(Date date, Org subscriber){
        String sort = 'r.reportCount desc'

        String dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "

        Calendar filterTime = GregorianCalendar.getInstance()
        filterTime.setTime(date)
        filterTime.set(Calendar.DATE, filterTime.getActualMinimum(Calendar.DAY_OF_MONTH))
        Date startDate = filterTime.getTime()
        filterTime.set(Calendar.DATE, filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
        Date endDate = filterTime.getTime()

        Map<String, Object> queryParams = [customer: subscriber, platform: this.tipp.platform, startDate: startDate, endDate: endDate, title: this.tipp]

        List counterReports


        counterReports = Counter5Report.executeQuery('select r from Counter5Report r where r.reportInstitution = :customer and r.platform = :platform and r.title = :title'+dateRange+' order by '+sort, queryParams)

        if(counterReports.size() > 0){
            //println(counterReports.size())
            return counterReports[0]
        }

        counterReports =  Counter4Report.executeQuery('select r from Counter4Report r where r.reportInstitution = :customer and r.platform = :platform and r.title = :title'+dateRange+' order by '+sort, queryParams)

        if(counterReports.size() > 0){
            //println(counterReports.size())
            return counterReports[0]
        }

        if(!counterReports) {
            return null
        }

    }

}
