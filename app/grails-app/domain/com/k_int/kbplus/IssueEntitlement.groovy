package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomain
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.PriceItem
import de.laser.exceptions.CreationException
import de.laser.exceptions.EntitlementCreationException
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation

import javax.persistence.Transient

class IssueEntitlement extends AbstractBaseDomain implements Comparable {

    @Transient
    def deletionService
    /*
    Date startDate
    String startVolume
    String startIssue
    Date endDate
    String endVolume
    String endIssue
    String embargo
    String coverageDepth
    String coverageNote
     */
  Date coreStatusStart
  Date coreStatusEnd
  Date accessStartDate
  Date accessEndDate

  String ieReason

  //merged as the difference between an IssueEntitlement and a TIPP is mainly former's attachment to a subscription, otherwise, they are functionally identical, even dependent upon each other. So why keep different refdata categories?
  @RefdataAnnotation(cat = RDConstants.TIPP_STATUS)
  RefdataValue status

  @RefdataAnnotation(cat = RDConstants.CORE_STATUS)
  RefdataValue coreStatus // core Status is really core Medium.. dont ask

  @RefdataAnnotation(cat = RDConstants.IE_MEDIUM)
  RefdataValue medium
    
  @RefdataAnnotation(cat = RDConstants.IE_ACCEPT_STATUS)
  RefdataValue acceptStatus

  Date dateCreated
  Date lastUpdated

  static belongsTo = [subscription: Subscription, tipp: TitleInstancePackagePlatform]

  static hasOne =    [priceItem: PriceItem]

  static hasMany = [coverages: IssueEntitlementCoverage]

  @Transient
  def comparisonProps = ['derivedAccessStartDate', 'derivedAccessEndDate',
'coverageNote','coverageDepth','embargo','startVolume','startIssue','startDate','endDate','endIssue','endVolume']

  int compareTo(obj) {
    int cmp = tipp?.title?.title.compareTo(obj.tipp?.title?.title)
    if(cmp == 0)
      return tipp?.id?.compareTo(obj.tipp?.id)
    return cmp
  }

  static mapping = {
                id column:'ie_id'
         globalUID column:'ie_guid'
           version column:'ie_version'
            status column:'ie_status_rv_fk'
      subscription column:'ie_subscription_fk', index: 'ie_sub_idx'
              tipp column:'ie_tipp_fk',         index: 'ie_tipp_idx'
          ieReason column:'ie_reason'
            medium column:'ie_medium_rv_fk'
   accessStartDate column:'ie_access_start_date'
     accessEndDate column:'ie_access_end_date'
         coverages sort: 'startDate', order: 'asc'
      acceptStatus column:'ie_accept_status_rv_fk'
      /*
      startDate column:'ie_start_date',      index: 'ie_dates_idx'
      startVolume column:'ie_start_volume'
      startIssue column:'ie_start_issue'
      endDate column:'ie_end_date',        index: 'ie_dates_idx'
      endVolume column:'ie_end_volume'
      endIssue column:'ie_end_issue'
      embargo column:'ie_embargo'
      coverageDepth column:'ie_coverage_depth'
      coverageNote column:'ie_coverage_note',type: 'text'
      */

    dateCreated column: 'ie_date_created'
    lastUpdated column: 'ie_last_updated'

  }

  static constraints = {
    globalUID      (nullable:true, blank:false, unique:true, maxSize:255)
    status         (nullable:true, blank:false)
    subscription   (nullable:false, blank:false)
    tipp           (nullable:false, blank:false)
    ieReason       (nullable:true, blank:true)
    medium         (nullable:true, blank:true)
    priceItem      (nullable:true, blank:true)
    accessStartDate(nullable:true, blank:true)
    accessEndDate  (nullable:true, blank:true)
    coreStatus     (nullable:true, blank:true)
    coreStatusStart(nullable:true, blank:true)
    coreStatusEnd  (nullable:true, blank:true)
    acceptStatus  (nullable:true, blank:true)
      /*
      startDate     (nullable:true, blank:true)
      startVolume   (nullable:true, blank:true)
      startIssue    (nullable:true, blank:true)
      endDate       (nullable:true, blank:true)
      endVolume     (nullable:true, blank:true)
      endIssue      (nullable:true, blank:true)
      embargo       (nullable:true, blank:true)
      coverageDepth (nullable:true, blank:true)
      coverageNote  (nullable:true, blank:true)
       */

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true, blank: false)
    dateCreated (nullable: true, blank: false)
  }

  static IssueEntitlement construct(Map<String,Object> configMap) throws EntitlementCreationException {
    if(configMap.subscription instanceof Subscription && configMap.tipp instanceof TitleInstancePackagePlatform) {
      Subscription subscription = (Subscription) configMap.subscription
      TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) configMap.tipp
      IssueEntitlement ie = findBySubscriptionAndTipp(subscription,tipp)
      if(!ie) {
        ie = new IssueEntitlement(subscription: subscription,tipp: tipp, status:tipp.status)
      }
      if(!ie.save())
        throw new EntitlementCreationException(ie.errors)
      ie
    }
    else throw new EntitlementCreationException("Issue entitlement creation attempt without valid subscription and TIPP references! This is not allowed!")
  }

  void afterDelete() {
    deletionService.deleteDocumentFromIndex(this.globalUID)
  }

  Date getDerivedAccessStartDate() {
    accessStartDate ? accessStartDate : subscription?.startDate
  }

  Date getDerivedAccessEndDate() {
    accessEndDate ? accessEndDate : subscription?.endDate
  }

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

  RefdataValue getAvailabilityStatus(Date as_at) {
    RefdataValue result
    // If StartDate <= as_at <= EndDate - Current
    // if Date < StartDate - Expected
    // if Date > EndDate - Expired
    Date ie_access_start_date = getDerivedAccessStartDate()
    Date ie_access_end_date = getDerivedAccessEndDate()

    result = RefdataValue.getByValueAndCategory('Current', RDConstants.IE_ACCESS_STATUS)

    if (ie_access_start_date && as_at < ie_access_start_date ) {
      result = RefdataValue.getByValueAndCategory('Expected', RDConstants.IE_ACCESS_STATUS)
    }
    else if (ie_access_end_date && as_at > ie_access_end_date ) {
      result = RefdataValue.getByValueAndCategory('Expired', RDConstants.IE_ACCESS_STATUS)
    }

    result
  }

  /*
  @Transient
  @Deprecated
  TitleInstitutionProvider getTIP(){
    Org inst = subscription?.getSubscriber()
    def title = tipp?.title
    Org provider = tipp?.pkg?.getContentProvider()

    if ( inst && title && provider ) {
      TitleInstitutionProvider tip = TitleInstitutionProvider.findByTitleAndInstitutionAndprovider(title, inst, provider)
      if(!tip){
        tip = new TitleInstitutionProvider(title:title,institution:inst,provider:provider)
        tip.save()
      }
      return tip
    }
    return null
  }
  
  @Transient
  def coreStatusOn(as_at) {
    // Use the new core system to determine if this title really is core
    TitleInstitutionProvider tip = getTIP()
    if(tip) return tip.coreStatus(as_at);
    return false
  }
  
  @Transient
  def extendCoreDates(startDate, endDate){
    def tip = getTIP()
      tip?.extendCoreExtent(startDate,endDate)
  }

  @Transient
  static def refdataFind(params) {

    def result = [];
    def hqlParams = []
    String hqlString = "select ie from IssueEntitlement as ie"

    if ( params.subFilter ) {
      hqlString += ' where ie.subscription.id = ?'
      hqlParams.add(params.long('subFilter'))
    }

    def results = IssueEntitlement.executeQuery(hqlString,hqlParams)

    results?.each { t ->
      def resultText = t.tipp.title.title
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result

  }
  */
}
