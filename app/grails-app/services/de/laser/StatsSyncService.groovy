package de.laser

import com.k_int.kbplus.*
import com.k_int.properties.*
import de.laser.domain.StatsTripleCursor
import de.laser.usage.StatsSyncServiceOptions
import groovy.json.JsonOutput
import groovyx.net.http.*

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter

import static groovyx.net.http.ContentType.*
import groovyx.gpars.GParsPool

class StatsSyncService {

    static final THREAD_POOL_SIZE = 1
    static final SYNC_STATS_FROM = '2012-01-01'

    def grailsApplication
    def executorService
    def sessionFactory
    def factService
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def queryParams = [:]
    def errors = []
    def availableReportCache = [:]


    static int submitCount=0
    static int completedCount=0
    static int newFactCount=0
    static int totalTime=0
    static int queryTime=0
    static int exceptionCount=0
    static long syncStartTime=0
    static int syncElapsed=0
    static def activityHistogram = [:]

    static boolean running = false
    static transactional = false

    def initSync() {
        log.debug("StatsSyncService::doSync ${this.hashCode()}")
        if ( this.running == true ) {
            log.debug("Skipping sync.. task already running")
            return
        }
        log.debug("Mark StatsSyncTask as running...")
        running = true

        submitCount=0
        completedCount=0
        newFactCount=0
        totalTime=0
        queryTime=0
        syncStartTime=System.currentTimeMillis()
        log.debug("Launch STATS sync at ${syncStartTime} ( ${System.currentTimeMillis()} )")
        syncElapsed=0
        activityHistogram = [:]
        availableReportCache = [:]
    }

    private String getTitleInstancesForUsageQuery()
    {
        // Distinct list of titles ids, the content provider, subscribing organisation and the zdbid
       def hql =  "select distinct ie.tipp.title.id, po.org.id, orgrel.org.id, zdbtitle.id from IssueEntitlement as ie " +
            "join ie.tipp.pkg.orgs as po " +
            "join ie.subscription.orgRelations as orgrel "+
            "join ie.tipp.title.ids as zdbtitle where zdbtitle.identifier.ns.ns = 'zdb' "+
            "and po.roleType.value='Content Provider' "+
            "and exists ( select oid from po.org.ids as oid where oid.identifier.ns.ns = 'statssid' ) " +
            "and (orgrel.roleType.value = 'Subscriber_Consortial' or orgrel.roleType.value = 'Subscriber') " +
            "and exists ( select rid from orgrel.org.customProperties as rid where rid.type.name = 'RequestorID' ) "
        if (queryParams['supplier'] != null){
            hql += "and po.org.id =:supplier "
        }
        if (queryParams['institution'] != null){
            hql += "and orgrel.org.id =:institution"
        }
        return hql
    }

    def addFilters(params)
    {
        queryParams = [:]
        if (params.supplier != 'null'){
            queryParams['supplier'] = params.supplier as long
        }
        if (params.institution != 'null'){
            queryParams['institution'] = params.institution as long
        }
    }

    private getReportType(report) {
        def result
        switch (report) {
            case "JR1":
                result = "journal"
            break
            case "JR1GOA":
                result = "journal"
            break
            case "DB1":
                result = "database"
            break
            default:
                result = "journal"
            break
        }
        return result
    }

    def doSync() {
        initSync()
        executorService.submit({ internalDoSync() } as java.util.concurrent.Callable)
    }

    def internalDoSync() {
        try {
            log.debug("create thread pool")
            def statsApi = grailsApplication.config.statsApiUrl ?: ''
            if (statsApi == '') {
                log.error("Stats API URL not set in config")
                errors.add("Stats API URL not set in config")
                return
            }
            def mostRecentClosedPeriod = getMostRecentClosedPeriod()
            def start_time = System.currentTimeMillis()
            log.debug("STATS Sync Task - Running query ${getTitleInstancesForUsageQuery()}")
            def titleList = IssueEntitlement.executeQuery(getTitleInstancesForUsageQuery(), queryParams)
            queryTime = System.currentTimeMillis() - start_time

            GParsPool.withPool(THREAD_POOL_SIZE) { pool ->
                titleList.anyParallel { to ->
                    processListItem(to, mostRecentClosedPeriod)
                    if (!running) {
                        return true  // break closure
                    }
                }
            }
        }
        catch ( Exception e ) {
            log.error("Error", e)
        }
        finally {
            log.debug("internalDoSync complete")
            log.debug("Mark StatsSyncTask as not running...")
            running = false
        }
    }

    def generateMD5(String s) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        digest.update(s.bytes)
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

    /**
     * Query NatStat v5 reports endpoint to get the available reports for a supplier
     * @param queryParams
     * @return Map Available reports for supplier
     */
    def getAvailableReportsForPlatform(queryParams) {

        def queryParamsHash = generateMD5(queryParams.apiKey.toString() + queryParams.requestor.toString() + queryParams.customer + queryParams.platform)
        if (availableReportCache[queryParamsHash]) {
            log.debug('Return available NatStat reports from cache')
            return availableReportCache[queryParamsHash]
        }
        try {
            def uri = new URIBuilder(grailsApplication.config.statsApiUrl)
            def baseUrl = uri.getScheme() + "://" + uri.getHost()
            def basePath = uri.getPath().endsWith('/') ? uri.getPath() : uri.getPath() + '/'
            def path = basePath + 'Sushiservice/reports'

            def v5Endpoint = new RESTClient(baseUrl)
            def result = v5Endpoint.get(
                path: path,
                headers: ["Accept": "application/json"],
                query: [
                    apikey      : queryParams.apiKey,
                    requestor_id: queryParams.requestor.toString(),
                    customer_id : queryParams.customer,
                    platform    : queryParams.platform,
                ])
            def reportList = []
            result.getData().each {it ->
                if (it.code) {
                    errors.add("SUSHI Error for ${queryParams.customer}|${queryParams.requestor}|${queryParams.platform}: ${it.code}-${it.message}\n")
                }
                if (it.Report_ID && it.Release) {
                    reportList.add(it.Report_ID + 'R' + it.Release)
                }

            }
            availableReportCache[queryParamsHash] = reportList
        } catch (Exception e) {
            def message = "Error getting available Reports from NatStat API"
            log.error(message)
            errors.add(message)
            log.error(e.message)
        }
    }

    private getObjectsForItem(listItem) {
       [
            TitleInstance.get(listItem[0]),
            Org.get(listItem[1]),
            Org.get(listItem[2]),
            IdentifierOccurrence.get(listItem[3])
        ]
    }

    def getRelevantReportList(queryParams)
    {
        def reports = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('FactType'))
        def availableReports = getAvailableReportsForPlatform(queryParams)
        reports.removeAll {
            if (it.value.startsWith('STATS') || (it.value.startsWith('JUSP'))){
                log.warn('STATS/JUSP prefix deprecated please remove Refdatavalues')
            }
            def reportInAvailableReport = it.value in availableReports
            (it.value.startsWith('STATS') || it.value.startsWith('JUSP') || !reportInAvailableReport)
        }
        return reports
    }

    def processListItem(listItem, mostRecentClosedPeriod) {
        def uri = new URIBuilder(grailsApplication.config.statsApiUrl)
        def baseUrl = uri.getScheme()+"://"+uri.getHost()
        def basePath = uri.getPath().endsWith('/') ? uri.getPath() : uri.getPath() + '/'
        def path = basePath + 'Sushiservice/GetReport'
        def stats_api_endpoint = new RESTClient(baseUrl)
        def start_time = System.currentTimeMillis()

        Fact.withNewTransaction { status ->
            def options = new StatsSyncServiceOptions()
            options.setItemObjects(getObjectsForItem(listItem))
            options.setQueryParams()
            options.mostRecentClosedPeriod = mostRecentClosedPeriod
            def reports = getRelevantReportList(options.getQueryParams())
            StatsTripleCursor csr = null

            reports.each { statsReport ->
                def matcher = statsReport.value =~ /^(.*).(\d)$/
                def report = matcher[0][1]
                def version = matcher[0][2]
                def reportType = getReportType(report)
                def factType = RefdataCategory.lookupOrCreate('FactType', statsReport.toString())
                // we could use a more complex structure, e.g. to try to seperate the SUSHI Exceptions from API
                // for now use a list of error messages
                def jsonErrors = []
                // There could be more than one (if we have gaps in usage), get the newest one
                csr = StatsTripleCursor.findByTitleIdAndSupplierIdAndCustomerIdAndFactType(
                    options.statsTitleIdentifier, options.platform, options.customer, factType,
                    [sort: "availTo", order: "desc"])
                if (csr == null) {
                    csr = new StatsTripleCursor(
                        titleId: options.statsTitleIdentifier,
                        supplierId: options.platform,
                        customerId: options.customer,
                        availFrom: SYNC_STATS_FROM,
                        availTo: null,
                        factType: factType
                    )
                    csr.numFacts = 0
                }
                // TODO use date representation of mostRecentClosedPeriod? Rename? Maybe
                if ((csr.availTo == null) || (csr.availTo < mostRecentClosedPeriod)) {
                    def fromPeriodForAPICall = getFromPeriodForAPICall(csr)
                    try {
                        def endDate = getDateForLastDayOfMonth(mostRecentClosedPeriod)
                        log.debug("Calling STATS API:  ${report}, Title with ID ${options.statsTitleIdentifier}")
                        log.debug("Period Begin: ${fromPeriodForAPICall}, Period End: ${endDate}")
                        stats_api_endpoint.get(
                            path: path,
                            contentType: ANY, // We get no XmlSlurper Objects for value XML
                            query: [
                                APIKey        : options.apiKey,
                                RequestorID   : options.requestor,
                                CustomerID    : options.customer,
                                Report        : report,
                                Release       : version,
                                BeginDate     : fromPeriodForAPICall,
                                EndDate       : endDate,
                                Platform      : options.platform,
                                ItemIdentifier: "${reportType}:zdbid:" + options.statsTitleIdentifier
                            ]) { response, xml ->
                            if (xml) {
                                def authenticationError = getSushiErrorMessage(xml)
                                if (authenticationError) {
                                    jsonErrors.add(authenticationError)
                                    csr.jerror = JsonOutput.toJson(jsonErrors)
                                }
                                if (responseHasUsageData(xml, options.statsTitleIdentifier)) {
                                  writeUsageRecords(xml, options, csr)
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("Error fetching data")
                        log.error(e.message)
                        jsonErrors.add(e.message)
                        def jsonError = JsonOutput.toJson(jsonErrors)
                        if (jsonError) {
                            csr.jerror = jsonError
                        }
                    }
                }
            }
            cleanUpGorm()
            def elapsed = System.currentTimeMillis() - start_time;
            totalTime+=elapsed
            incrementActivityHistogram()
        }
    }

    def writeUsageRecords(xml, options, csr)
    {
        checkStatsTitleCount(xml)
        def itemPerformances = xml.depthFirst().findAll {
            it.name() == 'ItemPerformance'
        }
        // 3030 Exception, no usage data for fetched report
        if (itemPerformances.empty) {
            csr.availTo = new SimpleDateFormat('yyyy-MM').parse(options.mostRecentClosedPeriod)
            csr.save(flush: true)
            return
        }
        def usageRanges = getUsageRanges(itemPerformances, getNotProcessedMonths(xml))
        def cal = new GregorianCalendar()
        usageRanges.each {
            def factCount = 0
            def itemPerformancesForRange = getItemPerformancesForRange(itemPerformances, it)

            // should only happen on first sync if there is a range without usage before the first ItemPerformance, e.g. if
            // we want to get usage for 2012ff from NatStat, but we cannot get usage this early
            if (itemPerformancesForRange.empty) {
                csr.availTo = new SimpleDateFormat('yyyy-MM').parse(it['end'])
                csr.save(flush: true)
            } else {
                def usageMap = getPeriodUsageMap(itemPerformancesForRange)
                usageMap.each { key, countPerMetric ->
                    def fact = [:]
                    countPerMetric.each { metric, count ->
                        fact.from = new SimpleDateFormat('yyyy-MM-dd').parse(key)
                        fact.to = new SimpleDateFormat('yyyy-MM-dd').parse(getDateForLastDayOfMonth(key))
                        cal.setTime(fact.to)
                        fact.reportingYear = cal.get(Calendar.YEAR)
                        fact.reportingMonth = cal.get(Calendar.MONTH) + 1
                        fact.type = csr.factType.value
                        fact.value = count
                        fact.uid = "${options.statsTitleIdentifier}:${options.platform}:${options.customer}:${key}:${metric}:${csr.factType.value}"
                        fact.metric = RefdataValue.getByValueAndCategory(metric, 'FactMetric')
                        fact.title = options.title_inst
                        fact.supplier = options.supplier_inst
                        fact.inst = options.org_inst
                        fact.juspio = options.title_io_inst
                        if (factService.registerFact(fact)) {
                            ++factCount
                            ++newFactCount
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd')
                            //csr.haveUpTo = YearMonth.parse(key, formatter).toString()
                        }
                    }
                }
                // First csr -> update
                if (csr.availTo == null){
                    csr.availTo = new SimpleDateFormat('yyyy-MM').parse(it.end)
                    csr.numFacts = factCount
                    csr.save(flush: true)

                } else {
                    def newYearMonth = getFromPeriodForAPICall(csr)
                    if (newYearMonth == it.begin) { // no gap for new range
                        csr.availTo = new SimpleDateFormat('yyyy-MM').parse(it.end) // update to last month for that range
                        csr.numFacts = factCount
                        csr.save(flush: true)
                    } else { // there is a gap, new csr
                        csr = new StatsTripleCursor()
                        // TODO set attributes
                    }
                }
            }
        }
    }

    def getItemPerformancesForRange(itemPerformances, range)
    {
        itemPerformances.findAll {
            it.Period.Begin.text().substring(0,7) >= range["begin"] &&
                it.Period.End.text().substring(0,7) <= range["end"]
        }
    }

    def getRangeBeforeFirstItemPerformanceElement(itemPerformances, notProcessedMonths)
    {
        def rangeMap = [:]
        def firstItemPerformanceBeginPeriod = itemPerformances.first().Period.Begin.text().substring(0,7)
        def lastItemPerformanceBeginPeriod = itemPerformances.last().Period.Begin.text().substring(0,7)
        def firstProcessedMonth = null
        def monthsWithoutNatStatTasksBeforeFirstItemPerformancePeriod = notProcessedMonths.findAll {
            firstItemPerformanceBeginPeriod > it
        }
        if (monthsWithoutNatStatTasksBeforeFirstItemPerformancePeriod) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
            YearMonth localDate = YearMonth.parse(monthsWithoutNatStatTasksBeforeFirstItemPerformancePeriod.last(),
                formatter)
            firstProcessedMonth = localDate.plusMonths(1).toString()
        }
        if (firstProcessedMonth && (firstProcessedMonth < firstItemPerformanceBeginPeriod)) {
            rangeMap['begin'] = firstProcessedMonth
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
            YearMonth localDate = YearMonth.parse(firstItemPerformanceBeginPeriod,formatter)
            rangeMap['end'] =  localDate.minusMonths(1).toString()
        }
        return rangeMap
    }

    def getUsageRanges(ArrayList itemPerformances, notProcessedMonths)
    {
        def ranges = []
        // Add begin end period for zero usage before first ItemPerformance and filter out months not available before that.
        // At the moment we begin SYNC_STATS_FROM. We would have to extend the NatStat API to improve that (i.e.
        // get csr.availfrom for the first period via API call
        def rangeBeforeFirstItemPerformanceElement = getRangeBeforeFirstItemPerformanceElement(itemPerformances, notProcessedMonths)
        if (! rangeBeforeFirstItemPerformanceElement.empty) {
            ranges.add(rangeBeforeFirstItemPerformanceElement)
            notProcessedMonths.removeAll {
                it < rangeBeforeFirstItemPerformanceElement['begin']
            }
        }
        def rangeList = []
        itemPerformances.each { performance ->
            def performanceMonth = performance.Period.Begin.text().substring(0,7)
            // we have a gap if there is a month in our list < the actual processed ItemPerformance month
            def gap = notProcessedMonths.find { month ->
                 performanceMonth > month
            }
            // if we have a gap, close range and remove all not processed months belonging together and also within that gap
            if (gap){
                def rangeMap = [:]
                rangeMap['begin'] = rangeList.first().Period.Begin.text().substring(0,7)
                rangeMap['end'] = rangeList.last().Period.Begin.text().substring(0,7)
                ranges.add(rangeMap)
                rangeList = []
                notProcessedMonths.removeAll {
                    it < performanceMonth
                }

            } else {
                rangeList.add(performance)
            }
        }

        def rangeMap = [:]
        rangeMap['begin'] = rangeList.first().Period.Begin.text().substring(0,7)
        rangeMap['end'] = rangeList.last().Period.Begin.text().substring(0,7)
        ranges.add(rangeMap)

        return ranges
    }

    /**
     * Not processed months when getting a 3031 Exception
     * @param xml
     * @return
     */
    def getNotProcessedMonths(xml)
    {
        if (xml.Exception.isEmpty() == false && xml.Exception.Number == '3031') {
            def exceptionData = xml.Exception.Data
            def matcher = exceptionData =~ /\d{4}-\d{2}/
            def list = []
            matcher.each {
                list.add(it)
            }
            return list
        }
        return false
    }


    def checkStatsTitleCount(xml)
    {
        def statsTitles = xml.depthFirst().findAll {
            it.name() == 'ItemName'
        }
        if (statsTitles.size() > 1) {
            log.warn('Found more than one item for the given Identifier')
            log.warn('Titles delivered by API: ')
            log.warn(statsTitles)
        }
    }

    def getFromPeriodForAPICall(csr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd')
        def fromPeriodForAPICall
        // set correct begin date for API call
        if (csr.availTo) {
            YearMonth localDate = YearMonth.parse(csr.availTo, formatter)
            fromPeriodForAPICall = localDate.plusMonths(1).toString()
        } else {
            if (!csr.availFrom){
                return SYNC_STATS_FROM
            }
            YearMonth fromPeriodLocalDate = YearMonth.parse(csr.availFrom, formatter)
            fromPeriodForAPICall = fromPeriodLocalDate.plusMonths(1).toString()
        }
        return fromPeriodForAPICall + '-01'
    }

    def processListItemOld(listItem, mostRecentClosedPeriod) {
        def uri = new URIBuilder(grailsApplication.config.statsApiUrl)
        def baseUrl = uri.getScheme()+"://"+uri.getHost()
        def basePath = uri.getPath().endsWith('/') ? uri.getPath() : uri.getPath() + '/'
        def path = basePath + 'Sushiservice/GetReport'
        def stats_api_endpoint = new RESTClient(baseUrl)
        def timeStampFormat = new SimpleDateFormat('yyyy-MM-dd')
        def start_time = System.currentTimeMillis()

        Fact.withNewTransaction { status ->

            def title_inst = TitleInstance.get(listItem[0])
            def supplier_inst = Org.get(listItem[1])
            def org_inst = Org.get(listItem[2])
            def title_io_inst = IdentifierOccurrence.get(listItem[3])
            def statsTitleIdentifier = title_io_inst.identifier.value

            def platform = supplier_inst.getIdentifierByType('statssid').value
            def customer = org_inst.getIdentifierByType('wibid').value
            def apiKey = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("API Key"), org_inst)
            def requestor = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), org_inst)
            def queryParams = [platform:platform, customer:customer, apiKey: apiKey, requestor:requestor]
            def availableReports = getAvailableReportsForPlatform(queryParams)
            def reports = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('FactType'))
            reports.removeAll {
                if (it.value.startsWith('STATS')){
                    log.warn('STATS prefix deprecated please remove Refdatavalues')
                }
                def reportInAvailableReport = it.value in availableReports
                (it.value.startsWith('STATS') || !reportInAvailableReport)
            }

            def csr = null

            reports.each { statsReport ->
                def factCount = 0
                def matcher = statsReport.value =~ /^(.*).(\d)$/
                def report = matcher[0][1]
                def version = matcher[0][2]
                def reportType = getReportType(report)
                def titleId = title_io_inst.identifier.value
                def factType = RefdataCategory.lookupOrCreate('FactType', statsReport.toString())

                // we could use a more complex structure, e.g. to try to seperate the SUSHI Exceptions from API
                // for now use a list of error messages
                def jsonErrors = []
                csr = StatsTripleCursor.findAllByTitleIdAndSupplierIdAndCustomerIdAndFactType(statsTitleIdentifier, platform, customer, factType, [sort: "availto", order: "desc"]).first()
                if (csr == null) {
                    csr = new StatsTripleCursor(
                        titleId: statsTitleIdentifier,
                        supplierId: platform,
                        customerId: customer,
                        availFrom: SYNC_STATS_FROM,
                        availTo: null,
                        factType: factType
                    )
                    csr.numFacts = 0
                }
                if ((csr.haveUpTo == null) || (csr.haveUpTo < mostRecentClosedPeriod)) {
                    def fromPeriod = SYNC_STATS_FROM
                    if (csr.haveUpTo){
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
                        YearMonth localDate = YearMonth.parse(csr.haveUpTo, formatter)
                        fromPeriod = localDate.plusMonths(1).toString()
                    }
                    try {
                        def beginDate = "${fromPeriod}-01"
                        def endDate = getDateForLastDayOfMonth(mostRecentClosedPeriod)
                        log.debug("Calling STATS API:  ${report}, Title with ID ${titleId}")
                        log.debug("Period Begin: ${beginDate}, Period End: ${endDate}")
                        stats_api_endpoint.get(
                                path: path,
                                contentType: ANY, // We get no XmlSlurper Objects for value XML
                                query: [
                                        APIKey        : apiKey,
                                        RequestorID   : requestor,
                                        CustomerID    : customer,
                                        Report        : report,
                                        Release       : version,
                                        BeginDate     : beginDate,
                                        EndDate       : endDate,
                                        Platform      : platform,
                                        ItemIdentifier: "${reportType}:zdbid:" + titleId
                                ]) { response, xml ->
                            if (xml) {
                                def authenticationError = getSushiErrorMessage(xml)
                                if (authenticationError){
                                    jsonErrors.add(authenticationError)
                                    csr.jerror = JsonOutput.toJson(jsonErrors)
                                }
                                if (responseHasUsageData(xml, titleId)) {
                                    def statsTitles = xml.depthFirst().findAll {
                                        it.name() == 'ItemName'
                                    }
                                    if (statsTitles.size() > 1) {
                                        log.warn('Found more than one item for the given Identifier')
                                        log.warn('Titles delivered by API: ')
                                        log.warn(statsTitles)
                                    }
                                    def itemPerformances = xml.depthFirst().findAll {
                                        it.name() == 'ItemPerformance'
                                    }
                                    def usageMap = getPeriodUsageMap(itemPerformances)
                                    def cal = new GregorianCalendar()

                                    usageMap.each { key, countPerMetric ->
                                        def fact = [:]
                                        countPerMetric.each { metric, count ->
                                            fact.from = timeStampFormat.parse(key)
                                            fact.to =timeStampFormat.parse(getDateForLastDayOfMonth(key))
                                            cal.setTime(fact.to)
                                            fact.reportingYear=cal.get(Calendar.YEAR)
                                            fact.reportingMonth=cal.get(Calendar.MONTH)+1
                                            fact.type = statsReport.toString()
                                            fact.value = count
                                            fact.uid = "${titleId}:${platform}:${customer}:${key}:${metric}:${statsReport.value}"
                                            fact.metric = RefdataValue.getByValueAndCategory(metric,'FactMetric')
                                            fact.title = title_inst
                                            fact.supplier = supplier_inst
                                            fact.inst = org_inst
                                            fact.juspio = title_io_inst
                                            if (factService.registerFact(fact)) {
                                                ++factCount
                                                ++newFactCount
                                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM-dd')
                                                csr.haveUpTo = YearMonth.parse(key, formatter).toString()
                                            }
                                        }
                                    }
                                    log.debug("Title: ${title_inst.title}")
                                    log.debug("ID: ${titleId}")

                                }
                            } else {
                                def errorMessage = "No xml object returned, response status: ${response.statusLine}"
                                log.error(errorMessage)
                                jsonErrors.add(errorMessage)
                                csr.jerror = JsonOutput.toJson(jsonErrors)
                            }
                        }

                    } catch (Exception e) {
                        log.error("Error fetching data")
                        log.error(e.message)
                        jsonErrors.add(e.message)
                        def jsonError = JsonOutput.toJson(jsonErrors)
                        if (jsonError) {
                            csr.jerror = jsonError
                        }
                    }
                    csr.numFacts = (csr.numFacts) ? csr.numFacts + factCount : factCount
                    try {
                        csr.save(flush: true)
                    } catch (Exception e) {
                        log.error(e.message)
                        jsonErrors.add(e.message)
                        def jsonError = JsonOutput.toJson(jsonErrors)
                        if (jsonError) {
                            csr.jerror = jsonError
                        }
                        exceptionCount++
                    }
                }
            }
            // TODO remove?
            // Exceptions are all catched, do we really want to save here when there were certain exceptions?
            // For now save the csr which should contain an error message
            if (csr != null) {
                csr.save(flush: true)
            }
            cleanUpGorm()
            def elapsed = System.currentTimeMillis() - start_time;
            totalTime+=elapsed
            incrementActivityHistogram()
        }
    }

    private String getMostRecentClosedPeriod()
    {
        def c = new GregorianCalendar()
        c.add(Calendar.MONTH,-2) // -2 TODO change back
        // Remember months are zero based - hence the +1 in this line!
        return "${c.get(Calendar.YEAR)}-${String.format('%02d',c.get(Calendar.MONTH)+1)}"
    }

    private isAllowedMetric(metric) {
        if (metric in ['ft_total', 'search_reg', 'search_fed', 'record_view', 'result_click']) {
            return true
        }
        return false
    }

    // period=>[metric1=>value,metric2=>value...]
    private Map getPeriodUsageMap(ArrayList itemPerformances) {
        def map = [:]
        // every ItemPerformance can have several Instances (DB/PR Reports up to 2, JR1 up to 3...)
        itemPerformances.each {
            def begin = it.Period.Begin.text()
            if (! map[begin]){
                map[begin] = [:]
            }
            def instances = it.depthFirst().findAll { node ->
                node.name() == 'Instance'
            }
            instances.each {
                def metric = it.MetricType.text()
                if (isAllowedMetric(metric)) {
                    def usage = it.Count.text()
                    if (!map[begin][metric]){
                        map[begin][metric] = usage
                    } else {
                        map[begin][metric] += usage
                    }
                } else {
                    //log.debug("Metric ${metric} is not supported")
                }
            }
        }
        return map
    }

    private String getDateForLastDayOfMonth(yearMonthString) {
        def sdf = new SimpleDateFormat("yyyy-MM")
        def cal = new GregorianCalendar()
        cal.setTime(sdf.parse(yearMonthString))
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return "${cal.get(Calendar.YEAR)}-${String.format('%02d',cal.get(Calendar.MONTH)+1)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private getSushiErrorMessage(xml){
        if (xml.Exception.isEmpty() == false) {
            def errorNumber = xml.Exception.Number
            def sushiErrorList = ['2000', '2020', '3000', '3062']
            if (errorNumber in sushiErrorList) {
                return xml.Exception.Message.toString()
            }
        }
        return false
    }

    private Boolean responseHasUsageData(xml, titleId) {
        // TODO maybe better check for usage first
        // What if we get a 3030 Exception? We return true here (report is processed in NatStat, usage data = 0)

        // Do we need to handle the 3031 Exceptions and kind of flag periods which are mentioned in the XML data element?
        // Or store 0 values for 3030 Exceptions, which allows us to mark missing/errorneous months, but would increase
        // the number of facts significantly
        if (xml.Exception.isEmpty() == false && xml.Exception.Number == '3030'){
            return true
        }
        if (xml.Exception.isEmpty() == false && xml.Exception.Number != '3031') {
            log.debug('SUSHI Exception Number ' + xml.Exception.Number + ' : ' + xml.Exception.Message)
            return false
        } else if (xml.Report.Report.isEmpty == true) {
            log.debug('XML response has 3031 Exception with no usage data')
            return false
        } else if (xml.Report.Report.Customer.isEmpty() == true) {
            log.debug('No result found for title with ID ' + titleId)
            return false
        } else {
            return true
        }
    }


    def cleanUpGorm() {
        log.debug("Clean up GORM")
        def session = sessionFactory.currentSession
        session.flush()
        session.clear()
        propertyInstanceMap.get().clear()
    }

    static synchronized void incrementActivityHistogram() {
        def sdf = new SimpleDateFormat('yyyy/MM/dd HH:mm')
        def col_identifier = sdf.format(new Date())

        completedCount++

        if ( activityHistogram[col_identifier] == null ) {
            activityHistogram[col_identifier] = new Long(1)
        }
        else {
            activityHistogram[col_identifier]++
        }

        syncElapsed = System.currentTimeMillis() - syncStartTime
    }

}

