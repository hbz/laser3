package com.k_int.kbplus

import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.transform.Transformers

class FactService {

  def sessionFactory

  private static String TOTAL_USAGE_FOR_SUB_IN_PERIOD =
      'select sum(factValue) ' +
          'from Fact as f ' +
          'where f.factFrom >= :start and f.factTo <= :end and f.factType.value=:factType and exists ' +
          '(select 1 from IssueEntitlement as ie INNER JOIN ie.tipp as tipp ' +
          'where ie.subscription= :sub  and tipp.title = f.relatedTitle)'

  static transactional = false

    def registerFact(fact) {
      // log.debug("Enter registerFact");
      def result = false

      if ( ( fact.type == null ) ||
           ( fact.type == '' ) ) 
        return result

      try {
          def fact_type_refdata_value = RefdataCategory.lookupOrCreate('FactType',fact.type)

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


  def generateMonthlyUsageGrid(title_id, org_id, supplier_id) {

    def result=[:]

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

    def result=[:]

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

  def generateUsageDataForLicense(org_id, supplier_id, license, title_id=null) {
    def result = [:]

    if (org_id != null &&
        supplier_id != null) {

      Calendar cal = Calendar.getInstance()
      cal.setTimeInMillis(license.startDate.getTime())
      def (firstLicenseMonth, firstLicenseYear) = [cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)]
      cal.setTimeInMillis(license.endDate.getTime())
      def (lastLicenseMonth, lastLicenseYear) = [cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR)]

      //def factList = getUsageFacts(org_id, supplier_id, title_id, license)
      def factList = getTotalUsageFactsForSub(org_id,supplier_id,title_id,license)

      def y_axis_labels = factList.factType.value.unique(false).sort()
      def x_axis_labels = (firstLicenseYear..lastLicenseYear).toList()

      addFactsForLicensePeriodWithoutUsage(x_axis_labels,factList)

      result.usage = generateUsageMDList(factList, y_axis_labels, x_axis_labels)

      if (firstLicenseMonth > 1) {
        def firstYearIndex = x_axis_labels.indexOf(x_axis_labels.first())
        x_axis_labels[firstYearIndex] = "${firstLicenseMonth}-12/${firstLicenseYear}"
      }
      if (lastLicenseMonth < 12) {
        def lastYearIndex = x_axis_labels.indexOf(x_axis_labels.last())
        x_axis_labels[lastYearIndex] = "1-${lastLicenseMonth}/${lastLicenseYear}"
      }

      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
    }

    result
  }

  private def generateUsageMDList(factList, firstAxis, secondAxis) {
    def usage = new long[firstAxis.size()][secondAxis.size()]
    factList.each { f ->
      def x_label = f.get('reportingYear').intValue()
      def y_label = f.get('factType')
      usage[firstAxis.indexOf(y_label)][secondAxis.indexOf(x_label)] += Long.parseLong(f.get('factValue'))
    }
    usage
  }

  private def getTotalUsageFactsForSub(org_id, supplier_id, title_id=null, sub=null)  {
    def params = [:]
    def hql = 'select sum(f.factValue), f.reportingYear, f.reportingMonth, f.factType' +
        ' from Fact as f' +
        ' where f.supplier.id=:supplierid and f.inst.id=:orgid'
        if (sub) {
          hql += ' and f.factFrom >= :start and f.factTo <= :end'
          params['start'] = sub.startDate
          params['end'] = sub.endDate
        }
        if (title_id) {
          hql += ' and f.relatedTitle.id=:titleid'
          params['titleid'] = title_id
        } else {
          hql += ' and exists (select 1 from IssueEntitlement as ie INNER JOIN ie.tipp as tipp ' +
              'where ie.subscription= :sub  and tipp.title = f.relatedTitle)'
          params['sub'] = sub
        }
    hql += ' group by f.factType, f.reportingYear, f.reportingMonth'
    hql += ' order by f.reportingYear desc,f.reportingMonth desc'
    params['supplierid'] = supplier_id
    params['orgid'] = org_id
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

  private def addFactsForLicensePeriodWithoutUsage(licenseYears, factList) {
    def usageYears = factList.reportingYear.unique(false).sort()
    def licenseYearsWithoutUsage = licenseYears - usageYears

    if (! licenseYearsWithoutUsage.isEmpty()) {
      licenseYearsWithoutUsage.each { year ->
        licenseYears.each { ft ->
          def newMapElement = [reportingYear:(year),factValue:'0',factType:(ft)]
          factList.add(newMapElement)
        }
      }
      factList.sort { a,b-> b.reportingYear <=> a.reportingYear }
    }
  }

  def generateUsageData(org_id, supplier_id, title_id=null) {
    def result = [:]

    if (org_id != null &&
        supplier_id != null) {

      //def factList = getUsageFacts(org_id, supplier_id, title_id)
      def factList = getTotalUsageFactsForSub(org_id, supplier_id, title_id)
      def y_axis_labels = factList.factType.value.unique(false).sort()
      def x_axis_labels = factList.reportingYear.unique(false).sort()*.intValue()

      result.usage = generateUsageMDList(factList, y_axis_labels, x_axis_labels)
      result.x_axis_labels = x_axis_labels
      result.y_axis_labels = y_axis_labels
    }
    result
  }

  def totalUsageForSub(sub, factType) {
    Fact.executeQuery(TOTAL_USAGE_FOR_SUB_IN_PERIOD, [
        start: sub.startDate,
        end  : sub.endDate,
        sub  : sub,
        factType : factType])[0]
  }

  def generateExpandableMonthlyUsageGrid(title_id, org_id, supplier_id) {

    def result=[:]

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

}