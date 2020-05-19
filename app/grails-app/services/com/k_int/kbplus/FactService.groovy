package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import de.laser.domain.StatsTripleCursor
import de.laser.helper.RDConstants
import org.apache.commons.net.ntp.TimeStamp
import org.hibernate.criterion.CriteriaSpecification

class FactService {

  def sessionFactory
  def contextService


    // TODO make this configurable
    static final Map preferedCostPerUseMetrics = [
        'Database' : ['result_click', 'record_view', 'search_reg'],
        'Journal' : ['ft_total']
    ]

    static final Map costPerUseReportForDatatype = [
        'Database' : 'DB1R4',
        'Journal' : 'JR1R4'
    ]

  private static String TOTAL_USAGE_FOR_SUB_IN_PERIOD =
      'select sum(factValue) ' +
          'from Fact as f ' +
          'where f.factFrom >= :start and f.factTo <= :end and f.factType.value=:factType and f.factMetric.value=:metric and exists ' +
          '(select 1 from IssueEntitlement as ie INNER JOIN ie.tipp as tipp ' +
          'where ie.subscription= :sub  and tipp.title = f.relatedTitle and ie.status.value!=:status) and f.inst = :inst'

  static transactional = false

    def registerFact(fact) {
      // log.debug("Enter registerFact");
      boolean result = false

      if ( ( fact.type == null ) ||
           ( fact.type == '' ) ) 
        return result

      try {
          // ERMS-2016: def fact_type_refdata_value = RefdataCategory.lookupOrCreate('FactType',fact.type)
          // if value exists --> RefdataValue.getByValueAndCategory()

          RefdataValue fact_type_refdata_value = RefdataValue.construct([
                  token   : fact.type,
                  rdc     : RDConstants.FACT_TYPE,
                  hardData: false,
                  i10n    : [value_de: fact.type, value_en: fact.type]
          ])

          // Are we updating an existing fact?
          if ( fact.uid != null ) {
            def current_fact = Fact.findByFactUid(fact.uid)

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
      return result
    }

  def getTotalCostPerUse(subscription, type, existingMetrics) {
    if (!subscription.costItems){
      log.debug('No Costitems found for for this subscription')
      return null
    }
    // temp solution
    if (type.value == 'Book'){
      log.debug('CostPerUse not supported for EBooks')
      return null
    }
    def preferedMetrics = preferedCostPerUseMetrics[type.value]
    def report = costPerUseReportForDatatype[type.value]
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


  def generateMonthlyUsageGrid(title_id, org_id, supplier_id) {

    Map<String, Object> result = [:]

    if ( title_id != null &&
         org_id != null &&
         supplier_id != null ) {

      def q = "select sum(f.factValue),f.reportingYear,f.reportingMonth,f.factType from Fact as f where f.relatedTitle.id=? and f.supplier.id=? and f.inst.id=? group by f.factType, f.reportingYear, f.reportingMonth order by f.reportingYear desc,f.reportingMonth desc,f.factType.value desc"
      def l1 = Fact.executeQuery(q,[title_id, supplier_id, org_id])

      def y_axis_labels = []
      def x_axis_labels = []

      l1.each { f ->
        def y_label = "${f[1]}-${String.format('%02d',f[2])}"
        def x_label = f[3].value
        if ( ! y_axis_labels.contains(y_label) )
          y_axis_labels.add(y_label)
        if ( ! x_axis_labels.contains(x_label) )
          x_axis_labels.add(x_label)
      }

      x_axis_labels.sort()
      y_axis_labels.sort()

      // log.debug("X Labels: ${x_axis_labels}");
      // log.debug("Y Labels: ${y_axis_labels}");

      result.usage = new long[y_axis_labels.size()][x_axis_labels.size()]

      l1.each { f ->
        def y_label = "${f[1]}-${String.format('%02d',f[2])}"
        def x_label = f[3].value
        result.usage[y_axis_labels.indexOf(y_label)][x_axis_labels.indexOf(x_label)] += Long.parseLong(f[0])
      }

      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
    }
    result
  }

  def generateYearlyUsageGrid(title_id, org_id, supplier_id) {

    Map<String, Object> result = [:]

    if ( title_id != null &&
         org_id != null &&
         supplier_id != null ) {

      def q = "select sum(f.factValue),f.reportingYear,f.factType from Fact as f where f.relatedTitle.id=? and f.supplier.id=? and f.inst.id=? group by f.factType, f.reportingYear  order by f.reportingYear,f.factType.value"
      def l1 = Fact.executeQuery(q,[title_id, supplier_id, org_id])

      def y_axis_labels = []
      def x_axis_labels = []

      l1.each { f ->
        def y_label = "${f[1]}"
        def x_label = f[2].value
        if ( ! y_axis_labels.contains(y_label) )
          y_axis_labels.add(y_label)
        if ( ! x_axis_labels.contains(x_label) )
          x_axis_labels.add(x_label)
      }

      x_axis_labels.sort()
      y_axis_labels.sort()

      // log.debug("X Labels: ${x_axis_labels}");
      // log.debug("Y Labels: ${y_axis_labels}");

      result.usage = new long[y_axis_labels.size()][x_axis_labels.size()]

      l1.each { f ->
        def y_label = "${f[1]}"
        def x_label = f[2].value
        result.usage[y_axis_labels.indexOf(y_label)][x_axis_labels.indexOf(x_label)] += Long.parseLong(f[0])
      }

      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
    }

    result
  }


  /**
   *  Return an array of size n where array[0] = total for year, array[1]=year-1, array[2]=year=2 etc
   *  Array is zero padded for blank years
   */
  def lastNYearsByType(title_id, org_id, supplier_id, report_type, n, year) {

    def result = new String[n+1]

    // def c = new GregorianCalendar()
    // c.setTime(new Date());
    // def current_year = c.get(Calendar.YEAR)

    if ( title_id != null &&
         org_id != null &&
         supplier_id != null ) {

      def q = "select sum(f.factValue),f.reportingYear,f.factType from Fact as f where f.relatedTitle.id=? and f.supplier.id=? and f.inst.id=? and f.factType.value = ? and f.reportingYear >= ? group by f.factType, f.reportingYear  order by f.reportingYear desc,f.factType.value"
      def l1 = Fact.executeQuery(q,[title_id, supplier_id, org_id, report_type, (long)(year-n)])

      l1.each{ y ->
        if ( y[1] >= (year - n) ) {
          int idx = year - y[1]
          // log.debug("IDX = ${idx} year = ${y[1]} value=${y[0]}");
          result[idx] = y[0].toString()
        }
      }
    }

    // result.each{r->
    //   log.debug(r)
    // }
    result
  }

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

  private def generateUsageMDList(factList, firstAxis, secondAxis) {
    def usage = new long[firstAxis.size()][secondAxis.size()]
    factList.each { f ->
      def x_label = f.get('reportingYear').intValue()
      def y_label = f.get('factType') + ':' + f.get('factMetric')
      usage[firstAxis.indexOf(y_label)][secondAxis.indexOf(x_label)] += f.get('factValue')
    }
    usage
  }

  private def getTotalUsageFactsForSub(org_id, supplier_id, sub, title_id=null, restrictToSubscriptionPeriod=false)  {
    def params = [:]
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
              'where ie.subscription= :sub  and tipp.title = f.relatedTitle and ie.status.value!=:status)'
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

  private def transformToListOfMaps(queryResult) {
    def list = []
    queryResult.each { li ->
      def map = [:]
      map['factValue'] = li[0]
      map['reportingYear'] = li[1]
      map['reportingMonth'] = li[2]
      map['factType'] = li[3]
      map['factMetric'] = li[4]
      list.add(map)
    }
    list
  }


  /**
   * @param title_id
   * @param org_id
   * @param supplier_id
   * @param license if given use license start/end as filter
   * @return ArrayList List of Fact Maps
   */
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

  Map<String,List> getMissingMonths(supplier_id, org_id, Subscription subscription = null) {
    Org customerOrg = Org.get(org_id)
    return getUsageRanges(supplier_id, customerOrg, subscription)
  }

  private Map<String,List> getUsageRanges(supplier_id, Org org, Subscription subscription) {
    String customer = org.getIdentifierByType('wibid')?.value
    String supplierId = PlatformCustomProperty.findByOwnerAndType(Platform.get(supplier_id), PropertyDefinition.getByNameAndDescr('NatStat Supplier ID', PropertyDefinition.PLA_PROP))
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
            it < java.time.YearMonth.parse(subscription.startDate.toString().substring(0,7))
          }
        }
      }
      Date max = ranges*.availTo.max()
      if (subscription?.endDate){
        if (max > subscription.endDate){
          max = subscription.endDate
          months.removeAll { it ->
            it > java.time.YearMonth.parse(subscription.endDate.toString().substring(0,7))
          }
        }
      }
      List completeList = getMonthsOfDateRange(min, max)
      missingMonthsPerFactType[ft.value] = ((completeList - months) + (months - completeList))
    }
    return missingMonthsPerFactType
  }

  List getMonthsOfDateRange(Date begin, Date end){
    List result = []
    java.time.YearMonth from = java.time.YearMonth.parse(begin.toString().substring(0,7))
    java.time.YearMonth to = java.time.YearMonth.parse(end.toString().substring(0,7))
    while (from<=to)
    {
      result.add(from)
      from = from.plusMonths(1)
    }
    result
  }

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

  def generateExpandableMonthlyUsageGrid(title_id, org_id, supplier_id) {

    Map<String, Object> result = [:]

    if ( title_id != null &&
         org_id != null &&
         supplier_id != null ) {

      def q = "select sum(f.factValue),f.reportingYear,f.reportingMonth,f.factType from Fact as f where f.relatedTitle.id=? and f.supplier.id=? and f.inst.id=? group by f.factType, f.reportingYear, f.reportingMonth order by f.reportingYear desc,f.reportingMonth desc,f.factType.value desc"
      def factList = Fact.executeQuery(q,[title_id, supplier_id, org_id])

      def y_axis_labels = []
      def x_axis_labels = []

      factList.each { f ->
        def y_label = "${f[1]}-${String.format('%02d',f[2])}"
        def x_label = f[3].value
        if ( ! y_axis_labels.contains(y_label) ) {
          // log.debug("Adding y axis label: ${y_label}");
          y_axis_labels.add(y_label)
        }
        if ( ! x_axis_labels.contains(x_label) ) {
          // log.debug("Adding x axis label: ${x_label}");
          x_axis_labels.add(x_label)
        }
      }

      x_axis_labels.sort()

      // log.debug("X Labels: ${x_axis_labels}");
      // log.debug("Y Labels: ${y_axis_labels}");

      result.usage = new long[y_axis_labels.size()][x_axis_labels.size()]

      factList.each { f ->
        def y_label = "${f[1]}-${String.format('%02d',f[2])}"
        def x_label = f[3].value
        result.usage[y_axis_labels.indexOf(y_label)][x_axis_labels.indexOf(x_label)] += Long.parseLong(f[0])
      }

      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
    }

    result
  }

  List<Org> institutionsWithRequestorIDAndAPIKey()
  {
    String hql = "select os.org from OrgSettings as os" +
        " where os.key='${OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID}'" +
        " and exists (select 1 from OrgSettings as inneros where inneros.key = '${OrgSettings.KEYS.NATSTAT_SERVER_API_KEY}' and inneros.org=os.org) order by os.org.name"
    return OrgSettings.executeQuery(hql)
  }

  List<Platform> platformsWithNatstatId()
  {
    String hql = "select platform from Platform as platform" +
        " where exists (select 1 from platform.customProperties as pcp where pcp.owner = platform.id and pcp.type.name = 'NatStat Supplier ID') order by platform.name"
    return Platform.executeQuery(hql)
  }

  List<Org> getFactInstitutionList()
  {
    Fact.withCriteria {
      projections {
        distinct("inst")
      }
    }
  }

  List<Platform> getFactProviderList()
  {
    Fact.withCriteria {
      projections {
        distinct("supplier")
      }
    }
  }

  def getSupplierCursorCount()
  {
    String hql = 'select supplierId, count(*) from StatsTripleCursor group by supplierId'
    return StatsTripleCursor.executeQuery(hql)
  }

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