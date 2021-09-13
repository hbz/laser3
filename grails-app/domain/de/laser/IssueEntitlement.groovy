package de.laser


import de.laser.base.AbstractBase
import de.laser.finance.PriceItem
import de.laser.exceptions.EntitlementCreationException
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient
import java.text.Normalizer

class IssueEntitlement extends AbstractBase implements Comparable {

    def deletionService

    Date coreStatusStart
    Date coreStatusEnd
    Date accessStartDate
    Date accessEndDate

    String name
    String sortname

    String ieReason

    boolean hasPerpetualAccess = false

    //merged as the difference between an IssueEntitlement and a TIPP is mainly former's attachment to a subscription, otherwise, they are functionally identical, even dependent upon each other. So why keep different refdata categories?
    @RefdataAnnotation(cat = RDConstants.TIPP_STATUS)
    RefdataValue status

    @RefdataAnnotation(cat = RDConstants.CORE_STATUS)
    RefdataValue coreStatus // core Status is really core Medium.. dont ask

    @RefdataAnnotation(cat = RDConstants.TITLE_MEDIUM)
    RefdataValue medium // legacy; was distinguished back then; I see no reason why I should still do so. Is legacy.

    @RefdataAnnotation(cat = RDConstants.IE_ACCEPT_STATUS)
    RefdataValue acceptStatus

    Date dateCreated
    Date lastUpdated

    static Log static_logger = LogFactory.getLog(IssueEntitlement)

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
            status column:'ie_status_rv_fk'
      subscription column:'ie_subscription_fk', index: 'ie_sub_idx'
              tipp column:'ie_tipp_fk',         index: 'ie_tipp_idx'
          ieReason column:'ie_reason'
          hasPerpetualAccess column:'ie_has_perpetual_access'
            medium column:'ie_medium_rv_fk'
    accessStartDate column:'ie_access_start_date'
     accessEndDate column:'ie_access_end_date'
         coverages sort: 'startDate', order: 'asc'
      acceptStatus column:'ie_accept_status_rv_fk'

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
    }

  static IssueEntitlement construct(Map<String,Object> configMap) throws EntitlementCreationException {
    if(configMap.subscription instanceof Subscription && configMap.tipp instanceof TitleInstancePackagePlatform) {
        static_logger.debug("creating new issue entitlement for ${configMap.tipp} and ${configMap.subscription}")
      Subscription subscription = (Subscription) configMap.subscription
      TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) configMap.tipp
      IssueEntitlement ie = findBySubscriptionAndTipp(subscription,tipp)
      if(!ie) {
          ie = new IssueEntitlement(subscription: subscription, tipp: tipp, medium: tipp.medium, status:tipp.status, acceptStatus: configMap.acceptStatus, name: tipp.name)
          ie.generateSortTitle()
      }
      if(ie.save()) {
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
          static_logger.debug("creating price items for ${tipp}")
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
    deletionService.deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
  }

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
}
