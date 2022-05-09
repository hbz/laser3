package de.laser

import de.laser.finance.CostItem
import de.laser.properties.PlatformProperty
import de.laser.properties.PropertyDefinition
import de.laser.stats.Fact
import de.laser.stats.StatsTripleCursor
import de.laser.storage.RDConstants
import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification

import java.time.YearMonth

/**
 * This service manages the Nationaler Statistikserver usage report workflows
 */
@Transactional
class FactService {

    ContextService contextService

    // TODO make this configurable
    Map preferedCostPerUseMetrics = [
        'Database' : ['result_click', 'record_view', 'search_reg'],
        'Journal' : ['ft_total']
    ]

    Map costPerUseReportForDatatype = [
        'Database' : 'DB1R4',
        'Journal' : 'JR1R4'
    ]

  private static String TOTAL_USAGE_FOR_SUB_IN_PERIOD =
      'select sum(factValue) ' +
          'from Fact as f ' +
          'where f.factFrom >= :start and f.factTo <= :end and f.factType.value=:factType and f.factMetric.value=:metric and exists ' +
          '(select 1 from IssueEntitlement as ie INNER JOIN ie.tipp as tipp ' +
          'where ie.subscription= :sub  and tipp = f.relatedTitle and ie.status.value!=:status) and f.inst = :inst'

  static transactional = false

  /**
   * Records a new usage report for a title
   * @param fact the usage report parameters
   * @return true if the registering was successful, false otherwise
   * @see Fact
   */
    boolean registerFact(fact) {
      // log.debug("Enter registerFact");
      boolean result = false

      if ( fact.type == null || fact.type == '' ) {
        return result
      }

      try {
          RefdataValue fact_type_refdata_value = RefdataValue.construct([
                  token   : fact.type,
                  rdc     : RDConstants.FACT_TYPE,
                  hardData: false,
                  i10n    : [value_de: fact.type, value_en: fact.type]
          ])

          // Are we updating an existing fact?
          if ( fact.uid != null ) {
            Fact current_fact = Fact.findByFactUid(fact.uid)

            if ( current_fact == null ) {
              // log.debug("Create new fact..");
              current_fact = new Fact(factType:fact_type_refdata_value, 
                                      factFrom:fact.from,
                                      factTo:fact.to,
                                      factValue:fact.value,
                                      factUid:fact.uid,
                                      factMetric:fact.metric,
                                      relatedTitle:fact.title,
                                      supplier:fact.supplier,
                                      inst:fact.inst,
                                      juspio:fact.juspio,
                                      reportingYear:fact.reportingYear,
                                      reportingMonth:fact.reportingMonth)
              if ( current_fact.save() ) {
                result=true
              }
              else {
                log.error("Problem saving fact: ${current_fact.errors}")
              }
            }
            else {
              log.debug("update existing fact ${current_fact.id} (${fact.uid} ${fact_type_refdata_value})")
            }
          }
      }
      catch ( Exception e ) {
        log.error("Problem registering fact",e)
      }
      finally {
        // log.debug("Leave registerFact");
      }
      result
    }

  /**
   * Calculates the total cost per use for the given subscription and title type. EBooks are not supported in this service
   * @param subscription the subscription whose title usages should be calculated and whose cost items should be considered
   * @param type the title instance type
   * @param existingMetrics the usage reports for the titles
   * @return an array containing the results, of structure:
   * <ol start="0">
   *     <li>the metric used for calculation</li>
   *     <li>the cost per use calculation result</li>
   * </ol>
   */
  def getTotalCostPerUse(Subscription subscription, String type, List existingMetrics) {
    if (!subscription.costItems){
      log.debug('No Costitems found for for this subscription')
      return null
    }
    // temp solution
    if (type == 'EBook'){
      log.debug('CostPerUse not supported for EBooks')
      return null
    }
    def preferedMetrics = preferedCostPerUseMetrics[type]
    def report = costPerUseReportForDatatype[type]
    def costPerUseMetric = preferedMetrics.findAll {
      existingMetrics.contains(it)
    }?.first()
    def costsQuery = 'select sum(co.costInLocalCurrency) as lccost, sum(co.costInBillingCurrency) as bccost, ' +
        'costItemElementConfiguration.value from CostItem co where co.sub=:sub and costItemElementConfiguration.value=:costType ' +
        'group by costItemElementConfiguration.value'
    def positiveCosts = CostItem.executeQuery(costsQuery, [sub: subscription, costType: 'positive'])
    if (positiveCosts.empty){
      log.debug('No positive CostItems found for this subscription')
      return null
    }
    def totalCosts = positiveCosts.first()[0]
    def negativeCosts = CostItem.executeQuery(costsQuery, [sub: subscription, costType: 'negative'])
    if (! negativeCosts.empty){
      totalCosts =  totalCosts - negativeCosts.first()[0]
      if (totalCosts < 0){
        log.debug('Total Costs < 0')
        return null
      }
    }
    def totalUsageForLicense = totalUsageForSub(subscription, report, costPerUseMetric)
    def totalCostPerUse = []
    if (totalCosts && totalUsageForLicense) {
      totalCostPerUse[0] = costPerUseMetric
      totalCostPerUse[1] = totalCosts / Double.valueOf(totalUsageForLicense)
    }
    totalCostPerUse
  }

  /**
   * Generates the usage data for the subscription period of the given subscription and the given title
   * @param org_id the subscriber whose usage should be used
   * @param supplier_id the provider whose data should be used
   * @param subscription the subscription whose period should count
   * @param title_id the title identifier whose usage should be regarded
   * @return a graph displaying the usage within the given time span
   */
  def generateUsageDataForSubscriptionPeriod(org_id, supplier_id, subscription, title_id=null) {
    Map<String, Object> result = [:]
    if (org_id != null &&
        supplier_id != null) {
      def factList = getTotalUsageFactsForSub(org_id, supplier_id, subscription, title_id, true)
      if (factList.size == 0){
        return result
      }
      Calendar cal = Calendar.getInstance()

      def (lastSubscriptionMonth, lastSubscriptionYear) = [cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)]
      def firstSubscriptionMonth, firstSubscriptionYear
      if (subscription.startDate) {
        cal.setTimeInMillis(subscription.startDate.getTime())
        (firstSubscriptionMonth, firstSubscriptionYear) = [cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)]
      } else {
        // use first year of factlist if there is no subscription begin
        def firstFactYear = factList.reportingYear.unique(false).sort()*.intValue().first()
        (firstSubscriptionMonth, firstSubscriptionYear) = [1, firstFactYear]
      }
      if (subscription.endDate) {
        cal.setTimeInMillis(subscription.endDate.getTime())
        (lastSubscriptionMonth, lastSubscriptionYear) = [cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)]
      }

      def y_axis_labels = []

      factList.each { li ->
        y_axis_labels.add(li.factType + ':' + li.factMetric)
      }
      y_axis_labels = y_axis_labels.unique().sort()

      def x_axis_labels = (firstSubscriptionYear..lastSubscriptionYear).toList()
      addFactsForSubscriptionPeriodWithoutUsage(x_axis_labels,factList)
      result.usage = generateUsageMDList(factList, y_axis_labels, x_axis_labels)
      if (firstSubscriptionMonth > 1) {
        def firstYearIndex = x_axis_labels.indexOf(x_axis_labels.first())
        x_axis_labels[firstYearIndex] = "${firstSubscriptionMonth}-12/${firstSubscriptionYear}"
      }
      if (lastSubscriptionMonth < 12) {
        def lastYearIndex = x_axis_labels.indexOf(x_axis_labels.last())
        x_axis_labels[lastYearIndex] = "1-${lastSubscriptionMonth}/${lastSubscriptionYear}"
      }

      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
      result.missingMonths = getMissingMonths(supplier_id, org_id, subscription)
    }

    result
  }

  /**
   * Assembles the usage data in a coordinate system
   * @param factList the list of usage reports to process
   * @param firstAxis the Y axis labels
   * @param secondAxis the X axis labels
   * @return a two-dimensional array containing usage data in plots
   */
  private def generateUsageMDList(factList, firstAxis, secondAxis) {
    def usage = new long[firstAxis.size()][secondAxis.size()]
    factList.each { f ->
      def x_label = f.get('reportingYear').intValue()
      def y_label = f.get('factType') + ':' + f.get('factMetric')
      usage[firstAxis.indexOf(y_label)][secondAxis.indexOf(x_label)] += f.get('factValue')
    }
    usage
  }

  /**
   * Collects all usage reports for the given subscription. The report timespan may be restricted to the subscription period
   * @param org_id the customer (institution) id whose usages should be retrieved
   * @param supplier_id the platform id
   * @param sub the subscription regarded for period
   * @param title_id the title instance identifier
   * @param restrictToSubscriptionPeriod should usage data be fetched only for the period of subscription?
   * @return a list of maps containing the usage data
   */
  private def getTotalUsageFactsForSub(org_id, supplier_id, sub, title_id=null, restrictToSubscriptionPeriod=false)  {
    Map params = [:]
    String hql = 'select sum(f.factValue), f.reportingYear, f.reportingMonth, f.factType.value, f.factMetric.value' +
        ' from Fact as f' +
        ' where f.supplier.id=:supplierid and f.inst.id=:orgid'
        if (restrictToSubscriptionPeriod) {
          if (sub.startDate) {
            hql += ' and f.factFrom >= :start'
            params['start'] = sub.startDate
          }
          if (sub.endDate) {
            hql += ' and f.factTo <= :end'
            params['end'] = sub.endDate
          }
        }
        if (title_id) {
          hql += ' and f.relatedTitle.id=:titleid'
          params['titleid'] = title_id
        } else {
          hql += ' and exists (select 1 from IssueEntitlement as ie INNER JOIN ie.tipp as tipp ' +
              'where ie.subscription= :sub  and tipp = f.relatedTitle and ie.status.value!=:status)'
          params['sub'] = sub
        }
    hql += ' group by f.factType.value, f.reportingYear, f.reportingMonth, f.factMetric.value'
    hql += ' order by f.reportingYear desc,f.reportingMonth desc'
    params['supplierid'] = supplier_id
    params['orgid'] = org_id
    if (! title_id) {
      params['status'] = 'Deleted'
    }
    def queryResult = Fact.executeQuery(hql, params)
    transformToListOfMaps(queryResult)
  }

  /**
   * Transforms the given query result into a list of maps
   * @param queryResult the result of a query
   * @return a map of structure
   * {
   *     factValue
   *     reportingYear
   *     reportingMonth
   *     factType
   *     factMetric
   * }
   */
  private def transformToListOfMaps(queryResult) {
    def list = []
    queryResult.each { li ->
      Map map = [:]
      map['factValue'] = li[0]
      map['reportingYear'] = li[1]
      map['reportingMonth'] = li[2]
      map['factType'] = li[3]
      map['factMetric'] = li[4]
      list.add(map)
    }
    list
  }

  @Deprecated
  private def getUsageFacts(org_id, supplier_id, title_id=null, license=null) {
    Fact.createCriteria().list {
      createAlias('factType', 'ft')
      resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
      projections {
        sum('factValue', 'factValue') // aliases needed for resultTransformer
        groupProperty('reportingYear', 'reportingYear')
        groupProperty('reportingMonth', 'reportingMonth')
        groupProperty('factType', 'factType')
      }
      if (license) {
        ge('factFrom', license.startDate)
        le('factTo', license.endDate)
      }
      if (title_id) {
        eq('relatedTitle.id', title_id)
      }
      eq('supplier.id', supplier_id)
      eq('inst.id', org_id)
      order('reportingYear', 'desc')
      order('reportingMonth', 'desc')
      order('ft.value', 'desc')
    }
  }

  /**
   * Fills the reporting gaps with zero values
   * @param licenseYears the years in which the subscription is running
   * @param factList the base list of usage reports
   * @return the list of usages with substituted values
   */
  private def addFactsForSubscriptionPeriodWithoutUsage(licenseYears, factList) {
    def usageYears = factList.reportingYear.unique(false).sort()
    def licenseYearsWithoutUsage = licenseYears - usageYears

    if (! licenseYearsWithoutUsage.isEmpty()) {
      licenseYearsWithoutUsage.each { year ->
        licenseYears.each { ft ->
          def newMapElement = [reportingYear:(year),factValue:0,factType:(ft)]
          factList.add(newMapElement)
        }
      }
      factList.sort { a,b-> b.reportingYear <=> a.reportingYear }
    }
  }

  /**
   * Collects the usage data for the given institution, platform, subscription and title instance
   * @param org_id the report institution
   * @param supplier_id the platform whose usage should be retrieved
   * @param subscription the subscription upon whose subscription range the queries may be restricted
   * @param title_id the title instance identifier
   * @return a map containing the data for a usage graph
   */
  def generateUsageData(org_id, supplier_id, subscription, title_id=null) {
    Map<String, Object> result = [:]

    if (org_id != null &&
        supplier_id != null) {

      def factList = getTotalUsageFactsForSub(org_id, supplier_id, subscription, title_id)
      // todo add column metric to table fact + data migration
      def y_axis_labels = []
      factList.each {
        y_axis_labels.add(it.factType + ':' + it.factMetric)
      }
      y_axis_labels = y_axis_labels.unique().sort()
      def x_axis_labels = factList.reportingYear.unique(false).sort()*.intValue()

      result.usage = generateUsageMDList(factList, y_axis_labels, x_axis_labels)
      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
      result.missingMonths = getMissingMonths(supplier_id, org_id)
    }
    result
  }

  /**
   * Gets the rages where no usage data is available for the given platform, institution and, if given, subscription
   * @param supplier_id the platform ID whose usages should be retrieved
   * @param org_id the report institution ID
   * @param subscription the subscription upon whose subscription range the query may be restricted
   * @return a map containing lists of missing months grouped per title type
   */
  Map<String,List> getMissingMonths(supplier_id, org_id, Subscription subscription = null) {
    Org customerOrg = Org.get(org_id)
    return getUsageRanges(supplier_id, customerOrg, subscription)
  }

  /**
   * Collects the usage ranges for the given platform, report institution and subscription
   * @param supplier_id the platform ID
   * @param org the report institution
   * @param subscription the subscription upon whose time span the ranges may be restricted
   * @return a map containing the missing usage ranges per title type
   */
  private Map<String,List> getUsageRanges(supplier_id, Org org, Subscription subscription) {
    String customer = org.getIdentifierByType('wibid')?.value
    String supplierId = PlatformProperty.findByOwnerAndType(Platform.get(supplier_id), PropertyDefinition.getByNameAndDescr('NatStat Supplier ID', PropertyDefinition.PLA_PROP))
    List factTypes = StatsTripleCursor.findAllByCustomerIdAndSupplierId(customer, supplierId).factType.unique()

    String titleRangesHql = "select stc from StatsTripleCursor as stc where " +
        "stc.factType=:factType and stc.supplierId=:supplierId and stc.customerId=:customerId " +
        "order by stc.titleId, stc.factType, stc.availFrom"
    Map<String,List> missingMonthsPerFactType = [:]
    factTypes.each { ft ->
      List rangeList = []
      List ranges = StatsTripleCursor.executeQuery(titleRangesHql, [
          supplierId : supplierId,
          customerId : customer,
          factType : ft
      ])
      List months = []
      ranges.each(){
        getMonthsOfDateRange(it.availFrom, it.availTo).each { m ->
          if (! months.contains(m))
            months.add(m)
        }
      }
      Date min = ranges*.availFrom.min()
      if (subscription?.startDate){
        if (min < subscription.startDate){
          min = subscription.startDate
          months.removeAll { it ->
            it < YearMonth.parse(subscription.startDate.toString().substring(0,7))
          }
        }
      }
      Date max = ranges*.availTo.max()
      if (subscription?.endDate){
        if (max > subscription.endDate){
          max = subscription.endDate
          months.removeAll { it ->
            it > YearMonth.parse(subscription.endDate.toString().substring(0,7))
          }
        }
      }
      List completeList = getMonthsOfDateRange(min, max)
      missingMonthsPerFactType[ft.value] = ((completeList - months) + (months - completeList))
    }
    return missingMonthsPerFactType
  }

  /**
   * Gets the last usage period for the given platform and institution
   * @param supplierId the platform ID whose last usage should be retrieved
   * @param customerId the institution ID for which usage should be queried
   * @return a map containing for each title type the last availability date
   */
  Map getLastUsagePeriodForReportType(supplierId, customerId){
    String hqlQuery = "select stc.factType.value, max(stc.availTo) as availTo from StatsTripleCursor stc where " +
        "stc.supplierId=:supplierId and stc.customerId=:customerId " +
        "group by stc.factType.value"
    Map<String,String> result = [:]
    List lastReportPeriods = StatsTripleCursor.executeQuery(hqlQuery, [
        supplierId : supplierId,
        customerId : customerId
    ])
    lastReportPeriods.each { it ->
      result[it[0]] = it[1].toString().substring(0,7)
    }
    result
  }

  /**
   * Collects a list of months in the given date range
   * @param begin the range start
   * @param end the range end
   * @return a list of {@link YearMonth}s
   */
  List getMonthsOfDateRange(Date begin, Date end){
    List result = []
    YearMonth from = YearMonth.parse(begin.toString().substring(0,7))
    YearMonth to = YearMonth.parse(end.toString().substring(0,7))
    while (from<=to)
    {
      result.add(from)
      from = from.plusMonths(1)
    }
    result
  }

  /**
   * Gets the sum of usages for the given subscription, title and metric types
   * @param sub the subscription to query after
   * @param factType the title instance type
   * @param metric the metric type
   * @return the total usage for the given subscription
   */
  def totalUsageForSub(sub, factType, metric) {
    Fact.executeQuery(TOTAL_USAGE_FOR_SUB_IN_PERIOD, [
        start: sub.startDate,
        end  : sub.endDate ?: new Date().parse('yyyy', '9999'), // TODO Fix that hack
        sub  : sub,
        factType : factType,
        status : 'Deleted',
        metric : metric,
        inst : sub.subscriber]
    )[0]
  }

  /**
   * Gets all institutions who have a Nationaler Statistikserver requestor ID and key
   * @return a list of institutions with a requestor ID and API key for the Nationaler Statstikserver
   * @see Org
   * @see OrgSetting
   */
  List<Org> institutionsWithRequestorIDAndAPIKey()
  {
    String hql = "select os.org from OrgSetting as os" +
        " where os.key='${OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID}'" +
        " and exists (select 1 from OrgSetting as inneros where inneros.key = '${OrgSetting.KEYS.NATSTAT_SERVER_API_KEY}' and inneros.org=os.org) order by os.org.name"
    return OrgSetting.executeQuery(hql)
  }

  /**
   * Gets all platforms where a Nationaler Statistikserver-ID has been registered
   * @return a list of platforms with a NatStat ID
   * @see Platform
   */
  List<Platform> platformsWithNatstatId()
  {
    String hql = "select platform from Platform as platform" +
        " where exists (select 1 from platform.propertySet as pcp where pcp.owner = platform.id and pcp.type.name = 'NatStat Supplier ID') order by platform.name"
    return Platform.executeQuery(hql)
  }

  /**
   * Gets a list of reporting institutions
   * @return a list of institutions ({@link Org})
   */
  List<Org> getFactInstitutionList()
  {
    Fact.withCriteria {
      projections {
        distinct("inst")
      }
    }
  }

  /**
   * Gets a list of statistics providers
   * @return a list of platforms
   */
  List<Platform> getFactProviderList()
  {
    Fact.withCriteria {
      projections {
        distinct("supplier")
      }
    }
  }

  /**
   * Gets the counts of statistics cursors for each platform
   * @return a list of cursor counts, grouped by platform
   */
  def getSupplierCursorCount()
  {
    String hql = 'select supplierId, count(*) from StatsTripleCursor group by supplierId'
    return StatsTripleCursor.executeQuery(hql)
  }

  /**
   * Called from view
   * Gets error reports for the given stats range
   * @param asr the given available statistics range to look for
   * @return error messages if any
   */
  def getStatsErrors(asr)
  {
    ArrayList results = StatsTripleCursor.createCriteria().list() {
      eq('supplierId', asr.supplierId)
      eq('customerId', asr.customerId)
      eq('factType', asr.factType)
      ne('jerror','')
    }
    results
  }

}