package de.laser


import de.laser.helper.ConfigUtils
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.titles.TitleInstance
import de.laser.usage.StatsSyncServiceOptions
import de.laser.usage.SushiClient
import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import groovyx.gpars.GParsPool
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.ExecutorService

@Transactional
class StatsSyncService {

    static final THREAD_POOL_SIZE = 1
    static final SYNC_STATS_FROM = '2012-01-01'

    def grailsApplication
    ExecutorService executorService
    def factService
    def globalService
     //def propertyInstanceMap = DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def queryParams = [:]
    def errors = []
    Map<String,List> availableReportCache = [:]


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
        if (running) {
            log.debug("Skipping sync.. task already running")
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

    private String getTitleInstancesForUsageQuery() {
        // Distinct list of titles ids, the platform, subscribing organisation and the zdbid
        String hql =  "select distinct ie.tipp.title.id, pf.id, orgrel.org.id, titleIdentifier.id from IssueEntitlement as ie " +
            "join ie.tipp.platform as pf " +
            "join ie.tipp.pkg.orgs as po " +
            "join ie.subscription.orgRelations as orgrel "+
            "join ie.tipp.title.ids as titleIdentifier "+
            "where titleIdentifier.ns.ns in ('zdb','doi') "+
            "and ie.status.value <> '${RDStore.TIPP_STATUS_DELETED}' " +
            "and po.roleType.value='Content Provider' "+
            "and exists (select cp from pf.customProperties as cp where cp.type.name = 'NatStat Supplier ID')" +
            "and (orgrel.roleType.value = 'Subscriber_Consortial' or orgrel.roleType.value = 'Subscriber') " +
            "and exists (select 1 from OrgSetting as os where os.org=orgrel.org and os.key='${OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID}' and os.strValue<>'') "
        if (queryParams['supplier'] != null){
            hql += "and pf.id =:supplier "
        }
        if (queryParams['institution'] != null){
            hql += "and orgrel.org.id =:institution"
        }
        return hql
    }

    void addFilters(params) {
        queryParams = [:]
        if (params.supplier != 'null'){
            queryParams['supplier'] = params.supplier as long
        }
        if (params.institution != 'null'){
            queryParams['institution'] = params.institution as long
        }
    }

    void doSync() {
        initSync()
        executorService.execute({ internalDoSync() })
    }

    void internalDoSync() {
        try {
            log.debug("create thread pool")
            String statsApi = ConfigUtils.getStatsApiUrl() ?: ''
            if (statsApi == '') {
                log.error("Stats API URL not set in config")
                errors.add("Stats API URL not set in config")
                return
            }
            String mostRecentClosedPeriod = getMostRecentClosedPeriod()
            Long start_time = System.currentTimeMillis()
            log.debug("STATS Sync Task - Running query ${getTitleInstancesForUsageQuery()}")
            List titleList = IssueEntitlement.executeQuery(getTitleInstancesForUsageQuery(), queryParams)
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

    String generateMD5(String s) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        digest.update(s.bytes)
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

    /**
     * Query NatStat v5 reports endpoint to get the available reports for a supplier
     * @param queryParams
     * @return List Available reports for supplier
     */
    List getAvailableReportsForPlatform(Map<String,String> queryParams) {

        String queryParamsHash = generateMD5(queryParams.apiKey.toString() + queryParams.requestor.toString() + queryParams.customer + queryParams.platform)
        if (availableReportCache[queryParamsHash]) {
            log.debug('Return available NatStat reports from cache')
            return availableReportCache[queryParamsHash]
        }
        try {
            URIBuilder uri = new URIBuilder(ConfigUtils.getStatsApiUrl())
            String baseUrl = uri.getScheme() + "://" + uri.getHost()
            String basePath = uri.getPath().endsWith('/') ? uri.getPath() : uri.getPath() + '/'
            String path = basePath + 'Sushiservice/reports'

            RESTClient v5Endpoint = new RESTClient(baseUrl)
            def result = v5Endpoint.get(
                path: path,
                headers: ["Accept": "application/json"],
                query: [
                    apikey      : queryParams.apiKey,
                    requestor_id: queryParams.requestor.toString(),
                    customer_id : queryParams.customer,
                    platform    : queryParams.platform,
                ])
            List reportList = []
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
            String message = "Error getting available Reports from NatStat API"
            log.error(message)
            errors.add(message)
            log.error(e.message)
        }
    }

    ArrayList getObjectsForItem(listItem) {
       [
            TitleInstance.get(listItem[0]),
            Platform.get(listItem[1]),
            Org.get(listItem[2]),
            Identifier.get(listItem[3])
        ]
    }

    List<RefdataValue> getRelevantReportList(Map<String,String> queryParams) {
        List<RefdataValue> reports = RefdataCategory.getAllRefdataValues(RDConstants.FACT_TYPE)
        List availableReports = getAvailableReportsForPlatform(queryParams)
        reports.removeAll {
            if (it.value.startsWith('STATS') || (it.value.startsWith('JUSP'))){
                //log.warn('STATS/JUSP prefix deprecated please remove Refdatavalues')
            }
            Boolean reportInAvailableReport = it.value in availableReports
            (it.value.startsWith('STATS') || it.value.startsWith('JUSP') || !reportInAvailableReport)
        }
        return reports
    }

    StatsSyncServiceOptions initializeStatsSyncServiceOptions(listItem, mostRecentClosedPeriod) {
        StatsSyncServiceOptions options = new StatsSyncServiceOptions()
        List itemObjects = getObjectsForItem(listItem)
        options.setItemObjects(itemObjects)
        options.setBasicQueryParams()
        options.mostRecentClosedPeriod = mostRecentClosedPeriod
        return options
    }

    StatsTripleCursor getCursor(StatsSyncServiceOptions options) {
        // There could be more than one (if we have gaps in usage), get the newest one
        StatsTripleCursor csr = StatsTripleCursor.findByTitleIdAndSupplierIdAndCustomerIdAndFactType(
            options.statsTitleIdentifier, options.platform, options.customer, options.factType,
            [sort: "availTo", order: "desc"])
        if (csr == null) {
            csr = new StatsTripleCursor(
                titleId: options.statsTitleIdentifier,
                identifierType: options.identifier.ns,
                supplierId: options.platform,
                customerId: options.customer,
                availFrom: new SimpleDateFormat("yyyy-MM-dd").parse(SYNC_STATS_FROM),
                availTo: null,
                factType: options.factType
            )
            csr.numFacts = 0
        }
        return csr
    }

    void processListItem(Object listItem, String mostRecentClosedPeriod) {
        SushiClient sushiClient = new SushiClient()
        Long start_time = System.currentTimeMillis()

        Fact.withNewTransaction { status ->
            StatsSyncServiceOptions options = initializeStatsSyncServiceOptions(listItem, mostRecentClosedPeriod)
            List<RefdataValue> reports = getRelevantReportList(options.getBasicQueryParams())
            StatsTripleCursor csr = null

            reports.each { statsReport ->
                options.setReportSpecificQueryParams(statsReport)
                // we could use a more complex structure, e.g. to try to seperate the SUSHI Exceptions from API
                // for now use a list of error messages
                List jsonErrors = []
                csr = getCursor(options)
                Date mostRecentClosedDate = new SimpleDateFormat("yyyy-MM-dd").parse(options.mostRecentClosedPeriod)
                if (options.identifierTypeAllowedForAPICall() &&
                    ((csr.availTo == null) || (csr.availTo < mostRecentClosedDate))) {
                    options.from = getNextFromPeriod(csr)
                    sushiClient.clientOptions = options
                    try {
                        sushiClient.query()
                        def xml = sushiClient.getResult()
                        def authenticationError = getSushiErrorMessage(xml)
                        if (authenticationError) {
                            jsonErrors.add(authenticationError)
                            csr.jerror = JsonOutput.toJson(jsonErrors)
                        }
                        if (responseHasUsageData(xml, options.statsTitleIdentifier)) {
                            writeUsageRecords(xml, options, csr)
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
            globalService.cleanUpGorm()
            def elapsed = System.currentTimeMillis() - start_time;
            totalTime+=elapsed
            incrementActivityHistogram()
        }
    }

    void writeUsageRecords(xml, options, csr) {
        checkStatsTitleCount(xml)
        List itemPerformances = xml.depthFirst().findAll {
            it.name() == 'ItemPerformance'
        }
        if (itemPerformances.size()>0) {
            itemPerformances.sort() {
                it.Period.Begin.text()
            }
        }
        // 3030 Exception or single title query without ItemPerformance, no usage data for fetched report
        if (itemPerformances.empty) {
            csr.availTo = new SimpleDateFormat('yyyy-MM-dd').parse(options.mostRecentClosedPeriod)
            // We get a new month with no usage for a single title
            if (! isNoUsageAvailableException(xml)) {
                List notProcessedMonths = getNotProcessedMonths(xml)
                if (! notProcessedMonths.empty) {
                    List followingRanges = actualRangePlusFollowingNoUsageRanges(options, notProcessedMonths, csr.availFrom.format('yyyy-MM'))
                    followingRanges.each {
                        if (it == followingRanges.first()){
                            csr.availTo = DateUtils.SDF_ymd.parse(getDateForLastDayOfMonth(it['end']))
                            csr.save()
                        } else {
                            writeNewCsr(0, it['begin'],it['end'],options)
                        }
                    }
                }
            } else {
                // 3030 Exception
                csr.save()
            }
            return
        }
        List usageRanges = getUsageRanges(itemPerformances, options, getNotProcessedMonths(xml))
        GregorianCalendar cal = new GregorianCalendar()
        usageRanges.each {
            Integer factCount = 0
            List itemPerformancesForRange = getItemPerformancesForRange(itemPerformances, it)
            // should only happen if there is a range without usage before the first ItemPerformance or
            // if there is a usage range after the last ItemPerformance (zero usage)
            if (itemPerformancesForRange.empty) {
                csr = writeNewCsr(factCount, it['begin'],it['end'],options)
            } else {
                Map usageMap = getPeriodUsageMap(itemPerformancesForRange)
                usageMap.each { key, countPerMetric ->
                    Map fact = [:]
                    countPerMetric.each { metric, count ->
                        fact.from = new SimpleDateFormat('yyyy-MM-dd').parse(key)
                        fact.to = new SimpleDateFormat('yyyy-MM-dd').parse(getDateForLastDayOfMonth(key))
                        cal.setTime(fact.to)
                        fact.reportingYear = cal.get(Calendar.YEAR)
                        fact.reportingMonth = cal.get(Calendar.MONTH) + 1
                        fact.type = csr.factType.value
                        fact.value = count
                        fact.uid = "${options.statsTitleIdentifier}:${options.platform}:${options.customer}:${key}:${metric}:${csr.factType.value}"
                        fact.metric = RefdataValue.getByValueAndCategory(metric, RDConstants.FACT_METRIC)
                        fact.title = options.title_inst
                        fact.supplier = options.supplier_inst
                        fact.inst = options.org_inst
                        if (factService.registerFact(fact)) {
                            ++factCount
                            ++newFactCount
                        }
                    }
                }
                // First csr -> update
                if (csr.availTo == null){
                    csr.availFrom = new SimpleDateFormat('yyyy-MM').parse(it['begin'])
                    csr.availTo = new SimpleDateFormat('yyyy-MM-dd').parse(getDateForLastDayOfMonth(it.end))
                    csr.numFacts = factCount
                    csr.save()

                } else {
                    def newFromPeriod = getNextFromPeriod(csr).substring(0,7)
                    if (newFromPeriod != it.begin) { // gap for new range, create new csr
                        log.warn("usage data gap found before ${it.begin}")
                        csr = writeNewCsr(factCount, it['begin'], it['end'], options)
                    } else {
                        // There is no gap, just update csr with new availTo value
                        csr.availTo = DateUtils.SDF_ymd.parse(getDateForLastDayOfMonth(it.end))
                        csr.numFacts = csr.numFacts + factCount
                        csr.save()
                    }

                }
            }
        }
    }

    private StatsTripleCursor writeNewCsr(factCount, begin, end, options){
        StatsTripleCursor csr = new StatsTripleCursor()
        csr.availFrom = new SimpleDateFormat('yyyy-MM').parse(begin)
        csr.availTo = new SimpleDateFormat('yyyy-MM-dd').parse(getDateForLastDayOfMonth(end))
        csr.customerId = options.customer
        csr.numFacts = factCount
        csr.titleId = options.statsTitleIdentifier
        csr.supplierId = options.platform
        csr.factType = options.factType
        csr.identifierType = options.identifier.ns
        csr.save()
        return csr
    }

    List getItemPerformancesForRange(itemPerformances, range) {
        itemPerformances.findAll {
            it.Period.Begin.text().substring(0,7) >= range["begin"] &&
                it.Period.End.text().substring(0,7) <= range["end"]
        }
    }

    Map<String,String> getRangeBeforeFirstItemPerformanceElement(List itemPerformances, StatsSyncServiceOptions options, List<String> notProcessedMonths) {
        Map<String,String> rangeMap = [:]
        String firstItemPerformanceBeginPeriod = itemPerformances.first().Period.Begin.text().substring(0,7)
        String lastItemPerformanceBeginPeriod = itemPerformances.last().Period.Begin.text().substring(0,7)
        String firstProcessedMonth = null
        List<String> monthsWithoutNatStatTasksBeforeFirstItemPerformancePeriod = notProcessedMonths.findAll {
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

    List<Map> getUsageRanges(List itemPerformances, StatsSyncServiceOptions options, List<String> notProcessedMonths) {
        log.debug('Get Usage ranges for API call from/to Period')
        List ranges = []
        // Add begin and end period for zero usage before first ItemPerformance and filter out months not available before that.
        // At the moment we begin SYNC_STATS_FROM. We would have to extend the NatStat API to improve that (i.e.
        // get csr.availfrom for the first period via API call
        Map<String,String> rangeMap = [:]
        Map rangeBeforeFirstItemPerformanceElement = getRangeBeforeFirstItemPerformanceElement(itemPerformances,
            options, notProcessedMonths)

        if (rangeBeforeFirstItemPerformanceElement.size() != 0) {
            // There is zero usage for one or more months before the first ItemPerformance (range between last not processed month
            // and first ItemPerformance Period), add those months to range
            rangeMap['begin'] = rangeBeforeFirstItemPerformanceElement['begin']
            log.debug('Found months between not processed months (SUSHI 3031) and first ItemPerformance Period')
        } else {
            // use next from period which was used to query the SUSHI service
            rangeMap['begin'] = options.from.substring(0,7)
            // Case when we have only not processed (3031) Months before first ItemPerformance
            // we have to set rangeMap['begin'] to +1 month after last not processed month before first ItemPerformance month
            List<String> lastNotProcessedMonthBeforeItemPerformances = notProcessedMonths.findAll() {
                it < itemPerformances.first().Period.Begin.text().substring(0,7)
            }.sort()
            if (lastNotProcessedMonthBeforeItemPerformances.size() != 0) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
                YearMonth localDate = YearMonth.parse(lastNotProcessedMonthBeforeItemPerformances.last(), formatter)
                rangeMap['begin'] = localDate.plusMonths(1).toString()
            }

            if (itemPerformances.first().Period.Begin.text().substring(0,7) != options.from.substring(0,7)){
                log.debug('First ItemPerformance does not equal from period for SUSHI call')
            }
        }

        // remove not processed months which we already have used to check for gaps to be able to use the list to find newer gaps
        // also handle the case where we have only 3031 Months before
        notProcessedMonths.removeAll {
            it < rangeMap['begin']
        }
        List itemPerformanceRangeList = [] //temp list
        itemPerformances.each { performance ->
            String performanceMonth = performance.Period.Begin.text().substring(0,7)
            // we have a gap if there is a month in our list < the actual processed ItemPerformance month
             String gap = notProcessedMonths.find { month ->
                 performanceMonth > month
            }

            // if we have a gap, close range and remove all not processed months belonging together and also within that gap
            if (gap){
                if (itemPerformanceRangeList.size() == 0){
                    log.error('Gap but no Itemperformance Elements to calculate the end of range')
                    // throw exception?
                }
                rangeMap['end'] = itemPerformanceRangeList.last().Period.Begin.text().substring(0,7)
                ranges.add(rangeMap)
                notProcessedMonths.removeAll {
                    it < performanceMonth
                }
                rangeMap = [:]
                rangeMap['begin'] = performanceMonth
                itemPerformanceRangeList = []
                itemPerformanceRangeList.add(performance)

            } else {
                itemPerformanceRangeList.add(performance)
            }
        }
        List followingRanges = actualRangePlusFollowingNoUsageRanges(options, notProcessedMonths, rangeMap['begin'])
        return ranges + followingRanges
    }

    private List<Map> actualRangePlusFollowingNoUsageRanges(StatsSyncServiceOptions options, List<String> notProcessedMonths, String begin)
    {
        List<Map> ranges = []
        Map rangeMap = [:]
        rangeMap['begin'] = begin
        // if there are months between last ItemPerformance and end date of SUSHI Call, there has to be zero usage for
        // that title
        if (notProcessedMonths.empty){
            // not matter if there are further months without usage or not, mostRecentClosedPeriod should be correct here
            rangeMap['end'] = options.mostRecentClosedPeriod.substring(0,7)
            ranges.add(rangeMap)
        } else {
            // close old range: range end is notProcessed - 1 Month
            rangeMap['end'] = minusMonths(notProcessedMonths.first(),1)
            ranges.add(rangeMap)
            rangeMap = [:]
            if (notProcessedMonths.first() == options.mostRecentClosedPeriod.substring(0, 7)){
                // do nothing more if it's the end month of the SUSHI call
                notProcessedMonths = []
            }

            // Here we can have further gaps in between zero usage months without ItemPerformances
            // it gets even more complicated and we also have to exclude the case where the notProcessedMonths are at the end, that
            // can happen very often, if new months cannot yet be loaded in NatStat. We also would have no Facts to connect
            // to the csr.
            // Take the following example:
            // ItemPerformances without gaps from [2012-01] - [2019-04]
            // notProcessedMonths 2019-06, 2019-12=End months of API call (mostRecentClosedPeriod)
            // => Leads to rangeMap: 2012-01 - 2019-05, 2019-07 - 2019-11

            // collate example: [2019-06,2019-11,2019-12]
            // => [[2019-06,2019-11],[2019-11,2019-12],[2019-12]]

            // get Pairs of Elements, keep the remainder
            List<List<String>> notProcessedMonthPairs = notProcessedMonths.collate(2,1)
            notProcessedMonthPairs.each {
                rangeMap['begin'] = plusMonths(it[0],1)
                if (it.size() == 1) {
                    // remainder, last not processed month, but only add a range if the notProcessedMonth
                    // ist not the SUSHI call end month
                    if (rangeMap['begin'] < options.mostRecentClosedPeriod.substring(0, 7)) {
                        rangeMap['end'] = options.mostRecentClosedPeriod.substring(0, 7)
                        ranges.add(rangeMap)
                        rangeMap = [:]
                    }
                } else {
                    // do not processed Months directly following one another (would be =)
                    if (rangeMap['begin'] < it[1]) {
                        rangeMap['end'] = minusMonths(it[1], 1)
                        ranges.add(rangeMap)
                        rangeMap = [:]
                    }
                }
            }
        }
        return ranges
    }

    private String plusMonths(CharSequence baseMonth, Long count) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
        YearMonth localDate = YearMonth.parse(baseMonth, formatter)
        return localDate.plusMonths(count).toString()
    }

    private String minusMonths(CharSequence baseMonth, Long count) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
        YearMonth localDate = YearMonth.parse(baseMonth, formatter)
        return localDate.minusMonths(count).toString()
    }

    /**
     * Not processed months when getting a 3031 Exception
     * @param xml
     * @return
     */
    List getNotProcessedMonths(xml) {
        if (xml.Exception.isEmpty() == false && xml.Exception.Number == '3031') {
            def exceptionData = xml.Exception.Data
            def matcher = exceptionData =~ /\d{4}-\d{2}/
            def list = []
            matcher.each {
                list.add(it)
            }
            return list
        }
        return []
    }


    void checkStatsTitleCount(xml) {
        List statsTitles = xml.depthFirst().findAll {
            it.name() == 'ItemName'
        }
        if (statsTitles.size() > 1) {
            log.warn('Found more than one item for the given Identifier')
            log.warn('Titles delivered by API: ')
            log.warn( statsTitles.toString() )
        }
    }

    String getNextFromPeriod(StatsTripleCursor csr) {
        String acceptedFormat = "yyyy-MM-dd"
        Date fromPeriodForAPICall
        // If availTo is set, get first day of next month
        if (csr.availTo) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(csr.availTo);
            cal.add(Calendar.MONTH, 1);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            fromPeriodForAPICall = cal.getTime()
        } else {
            if (!csr.availFrom){
                return SYNC_STATS_FROM
            }
            fromPeriodForAPICall = csr.availFrom
        }
        return fromPeriodForAPICall.format(acceptedFormat)
    }

    private String getMostRecentClosedPeriod() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, -2)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return cal.format('yyyy-MM-dd')
    }

    private Boolean isAllowedMetric(metric) {
        if (metric in ['ft_total', 'search_reg', 'search_fed', 'record_view', 'result_click']) {
            return true
        }
        return false
    }

    // period=>[metric1=>value,metric2=>value...]
    private Map<String,Map> getPeriodUsageMap(ArrayList itemPerformances) {
        Map map = [:]
        // every ItemPerformance can have several Instances (DB/PR Reports up to 2, JR1 up to 3...)
        itemPerformances.each {
            String begin = it.Period.Begin.text()
            if (! map[begin]){
                map[begin] = [:]
            }
            List instances = it.depthFirst().findAll { node ->
                node.name() == 'Instance'
            }
            instances.each {
                String metric = it.MetricType.text()
                if (isAllowedMetric(metric)) {
                    Integer usage = it.Count.text().toInteger()
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM")
        GregorianCalendar cal = new GregorianCalendar()
        cal.setTime(sdf.parse(yearMonthString))
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return "${cal.get(Calendar.YEAR)}-${String.format('%02d',cal.get(Calendar.MONTH)+1)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    private getSushiErrorMessage(xml) {
        if (xml.Exception.isEmpty() == false) {
            def errorNumber = xml.Exception.Number
            def sushiErrorList = ['2000', '2020', '3000', '3062']
            if (errorNumber in sushiErrorList) {
                return xml.Exception.Message.toString()
            }
        }
        return false
    }

    private Boolean isNoUsageAvailableException(xml)
    {
        return (xml.Exception.isEmpty() == false && xml.Exception.Number == '3030')
    }

    private Boolean isEmptyReport(xml)
    {
        return (xml.Report.Report.isEmpty() == true || xml.Report.isEmpty() == true)
    }

    private Boolean isOtherExceptionWithoutUsageData(xml)
    {
        return (xml.Exception.isEmpty() == false && xml.Exception.Number != '3031')
    }

    private Boolean isEmptyReportWithoutCustomer(xml)
    {
        return (xml.Report.Report.Customer.isEmpty() == true)
    }

    private Boolean responseHasUsageData(xml, titleId) {
        // 3030 Exception-> Zero usage
        if (isNoUsageAvailableException(xml)){
            return true
        }
        // SUSHI Exceptions which prevent from further processing and storing records in usage tables
        if (isOtherExceptionWithoutUsageData(xml)) {
            log.debug('SUSHI Exception Number ' + xml.Exception.Number + ' : ' + xml.Exception.Message)
            return false
        } else if (isEmptyReport(xml)) {
            // 3031 Exception but no usage data, e.g. all fetched Months are not available, or we call the last month which is
            // not yet available
            log.debug('XML response has 3031 Exception with no usage data')
            return false
        } else if (isEmptyReportWithoutCustomer(xml)) {
            // there are processed months but no usage data. This can happen with queries for single titles. If we had no usage
            // for all titles we would get a 3030 Exception for all titles.
            log.debug('No result found for title with ID ' + titleId)
            return true
        } else {
            return true
        }
    }

    static synchronized void incrementActivityHistogram() {
        SimpleDateFormat sdf = new SimpleDateFormat('yyyy/MM/dd HH:mm')
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
