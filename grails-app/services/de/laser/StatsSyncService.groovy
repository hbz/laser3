package de.laser


import de.laser.base.AbstractCounterApiSource
import de.laser.http.BasicHttpClient
import de.laser.config.ConfigMapper
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.utils.DateUtils
import de.laser.remote.ApiSource
import de.laser.stats.Fact
import de.laser.stats.StatsTripleCursor
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.system.SystemEvent
import de.laser.usage.StatsSyncServiceOptions
import de.laser.usage.SushiClient
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.json.JsonOutput
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.xml.slurpersupport.GPathResult
import groovy.xml.StreamingMarkupBuilder
import groovyx.gpars.GParsPool
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClientConfiguration
import org.grails.web.json.JSONArray

import java.security.MessageDigest
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import static java.time.temporal.TemporalAdjusters.firstDayOfYear
import java.util.concurrent.ExecutorService

/**
 * This service manages the synchronisation of usage statistics, both for the Nationaler Statistikserver and for the
 * internal statistic component
 */
@Transactional
class StatsSyncService {

    static final THREAD_POOL_SIZE = 8
    static final SYNC_STATS_FROM = '2012-01-01'
    static final MONTH_DUE_DATE = 28 //default is 28, do not commit other days!
    static final YEARLY_MONTH = Calendar.DECEMBER
    static final HALF_YEARLY_MONTHS = [Calendar.JUNE, YEARLY_MONTH]
    static final QUARTERLY_MONTHS = HALF_YEARLY_MONTHS+[Calendar.MARCH, Calendar.SEPTEMBER]

    ExecutorService executorService
    FactService factService
    GlobalService globalService
    Map queryParams = [:]
    List errors = []
    Map<String,List> availableReportCache = [:]

    SimpleDateFormat yyyyMMdd = DateUtils.getSDF_yyyyMMdd()

    static int submitCount=0
    static int completedCount=0
    static int newFactCount=0
    static int totalTime=0
    static int queryTime=0
    static int exceptionCount=0
    static long syncStartTime=0
    static int syncElapsed=0
    static Map activityHistogram = [:]

    static boolean running = false
    static transactional = false

    /**
     * Initialises synchronisation process
     */
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

    /**
     * Generates a query for the title instances
     * @return the title instance query
     */
    private String _getTitleInstancesForUsageQuery() {
        // Distinct list of titles ids, the platform, subscribing organisation and the zdbid
        //TODO change from string comparison to ID comparison
        String hql =  "select distinct ie.tipp.id, pf.id, orgrel.org.id, titleIdentifier.id from IssueEntitlement as ie " +
            "join ie.tipp.platform as pf " +
            "join ie.tipp.pkg.orgs as po " +
            "join ie.subscription.orgRelations as orgrel "+
            "join ie.tipp.ids as titleIdentifier "+
            "where titleIdentifier.ns.ns in ('zdb','doi') "+
            "and ie.status.value <> '${RDStore.TIPP_STATUS_REMOVED}' " +
            "and po.roleType.value in ('Provider','Content Provider') "+
            "and exists (select cp from pf.propertySet as cp where cp.type.name = 'NatStat Supplier ID')" +
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

    /**
     * Adds the platform and reporting institution IDs to the query parameter map
     * @param params the request parameter map
     */
    void addFilters(params) {
        queryParams = [:]
        if (params.supplier != 'null'){
            queryParams['supplier'] = params.supplier as long
        }
        if (params.institution != 'null'){
            queryParams['institution'] = params.institution as long
        }
    }

    /**
     * Starts the Nationaler Statistikserver synchronisation process and puts the process on a new thread
     */
    void doSync() {
        initSync()
        executorService.execute({ internalDoSync() })
    }

    /**
     * Starts the internal statistics synchronisation process, i.e. loading usage data directly from the providers
     * and puts the process on a new thread
     * @param incremental should only new data being loaded or a full data reload done?
     */
    void doFetch(boolean incremental, String platformUUID = '', String source = '', String revision = '') {
        log.debug("fetching data from providers started")
        if (running) {
            log.debug("Skipping sync ... fetching task already running")
            return
        }
        running = true
        executorService.execute({ internalDoFetch(incremental, platformUUID, source, revision) })
    }

    /**
     * Nationaler Statistikserver
     * Initialises the multithreading process performing the data load
     */
    void internalDoSync() {
        try {
            log.debug("create thread pool")
            String statsApi = ConfigMapper.getStatsApiUrl() ?: ''
            if (statsApi == '') {
                log.error("Stats API URL not set in config")
                errors.add("Stats API URL not set in config")
                return
            }
            String mostRecentClosedPeriod = _getMostRecentClosedPeriod()
            Long start_time = System.currentTimeMillis()
            log.debug("STATS Sync Task - Running query ${_getTitleInstancesForUsageQuery()}")
            List titleList = IssueEntitlement.executeQuery(_getTitleInstancesForUsageQuery(), queryParams)
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

    /**
     * Internal
     * Performs the loading of the SUSHI sources from the we:kb instance and loads the data from the SUSHI endpoints defined there.
     * Both COUNTER 4 and COUNTER 5 are being processed here
     * @param incremental should only newest data being fetched or a full data reload done?
     */
    void internalDoFetch(boolean incremental, String platformUUID = '', String source = '', String revision = '') {
        ApiSource apiSource = ApiSource.findByActive(true)
        List<List> c4SushiSources = [], c5SushiSources = []
        //process each platform with a SUSHI API
        BasicHttpClient http
        try {
            if(source && revision) {
                if(revision == 'r4') {
                    c4SushiSources.add([platformUUID, source])
                }
                else if(revision == 'r5') {
                    c5SushiSources.add([platformUUID, source])
                }
            }
            else {
                http = new BasicHttpClient(apiSource.baseUrl+apiSource.fixToken+'/sushiSources')
                Closure success = { resp, json ->
                    if(resp.code() == 200) {
                        if(incremental) {
                            Calendar now = GregorianCalendar.getInstance()
                            json.counter4ApiSources.each { c4as ->
                                boolean add = false
                                switch(c4as[2]) {
                                    case 'Daily': add = true
                                        break
                                    case 'Weekly': add = now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                                        break
                                    case 'Monthly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE
                                        break
                                    case 'Quarterly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE && now.get(Calendar.MONTH) in QUARTERLY_MONTHS
                                        break
                                    case 'Half-Yearly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE && now.get(Calendar.MONTH) in HALF_YEARLY_MONTHS
                                        break
                                    case 'Yearly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE && now.get(Calendar.MONTH) == YEARLY_MONTH
                                        break
                                }
                                if(add) {
                                    //c4SushiSources.add(c4as)
                                }
                            }
                            json.counter5ApiSources.each { c5as ->
                                boolean add = false
                                switch(c5as[2]) {
                                    case 'Daily': add = true
                                        break
                                    case 'Weekly': add = now.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY
                                        break
                                    case 'Monthly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE
                                        break
                                    case 'Quarterly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE && now.get(Calendar.MONTH) in QUARTERLY_MONTHS
                                        break
                                    case 'Half-Yearly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE && now.get(Calendar.MONTH) in HALF_YEARLY_MONTHS
                                        break
                                    case 'Yearly': add = now.get(Calendar.DAY_OF_MONTH) == MONTH_DUE_DATE && now.get(Calendar.MONTH) == YEARLY_MONTH
                                        break
                                }
                                if(add) {
                                    c5SushiSources.add(c5as)
                                }
                            }
                        }
                        else {
                            c4SushiSources.addAll(json.counter4ApiSources)
                            c5SushiSources.addAll(json.counter5ApiSources)
                        }
                    }
                    else {
                        log.error("server response: ${resp.status()}")
                    }
                }
                Closure failure = { resp, reader ->
                    log.error("server response: ${resp.status()} - ${reader}")
                }
                http.get(BasicHttpClient.ResponseType.JSON, success, failure)
            }
        }
        catch (Exception ignored) {
            log.error("we:kb unavailable ... postpone next run!")
        }
        finally {
            if (http) { http.close() }
        }

        Set<Long> namespaces = [IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, TitleInstancePackagePlatform.class.name).id, IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, TitleInstancePackagePlatform.class.name).id, IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, TitleInstancePackagePlatform.class.name).id, IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PISBN, TitleInstancePackagePlatform.class.name).id, IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, TitleInstancePackagePlatform.class.name).id]
            c4SushiSources.each { List c4as ->
                /*Set titles = TitleInstancePackagePlatform.executeQuery('select new map(id.value as identifier, tipp.id as title) from Identifier id  join id.tipp tipp where tipp.platform = :plat and tipp.status != :removed',
                        [plat: c4as.platform, removed: RDStore.TIPP_STATUS_REMOVED])*/
                Platform c4asPlatform = Platform.findByGokbId(c4as[0] as String)
                if(c4as[1] != null) {
                    String statsUrl = c4as[1] //.endsWith('/') ? c4as[1] : c4as[1]+'/' does not work with every platform!
                    List keyPairs = CustomerIdentifier.executeQuery('select new map(cust.id as customerId, cust.sortname as customerName, ci.value as value, ci.requestorKey as requestorKey) from CustomerIdentifier ci join ci.customer cust where ci.platform = :plat and ci.value != null and ci.requestorKey != null', [plat: c4asPlatform])
                    if(keyPairs) {
                        GParsPool.withPool(THREAD_POOL_SIZE) { pool ->
                            keyPairs.eachWithIndexParallel { Map keyPair, int i ->
                                Sql sql = GlobalService.obtainSqlConnection(), statsSql = GlobalService.obtainStorageSqlConnection()
                                //TitleInstancePackagePlatform.withNewSession {
                                sql.withTransaction {
                                    List laserStatsCursor = sql.rows("select lsc_latest_from_date, lsc_latest_to_date, lsc_report_id from laser_stats_cursor where lsc_platform_fk = :platform and lsc_customer_fk = :customer", [platform: c4asPlatform.id, customer: keyPair.customerId])
                                    boolean onlyNewest = laserStatsCursor ? incremental : false
                                    //List<GroovyRowResult> titles = sql.rows("select id_value as identifier, id_tipp_fk as title from identifier join title_instance_package_platform on id_tipp_fk = tipp_id where tipp_plat_fk = :plat and exists (select or_id from org_role join issue_entitlement on or_sub_fk = ie_subscription_fk where ie_tipp_fk = tipp_id and or_org_fk = :customer and ie_status_rv_fk != :removed)",
                                    //        [plat: c4as.platform.id, customer: keyPair.customer.id, removed: RDStore.TIPP_STATUS_REMOVED.id]
                                    Map<String, Object> calendarConfig = initCalendarConfig(onlyNewest)
                                    //DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                    Calendar startTime = GregorianCalendar.getInstance(), currentYearEnd = GregorianCalendar.getInstance()
                                    log.debug("${Thread.currentThread().getName()} is now processing key pair ${i}, requesting data for ${keyPair.customerName}:${keyPair.value}:${keyPair.requestorKey}")
                                    Counter4Report.COUNTER_4_REPORTS.each { String reportID ->
                                        startTime.setTime(calendarConfig.startDate)
                                        currentYearEnd.setTime(calendarConfig.endNextRun)
                                        if(onlyNewest) {
                                            GroovyRowResult row = laserStatsCursor.find { GroovyRowResult rr -> rr.get("lsc_report_id") == reportID }
                                            if(row) {
                                                startTime.setTimeInMillis(row.get("lsc_latest_from_date").getTime())
                                                startTime.add(Calendar.MONTH, 1)
                                                currentYearEnd.setTimeInMillis(row.get("lsc_latest_to_date").getTime())
                                                currentYearEnd.add(Calendar.MONTH, 1)
                                            }
                                        }
                                        log.debug("${Thread.currentThread().getName()} is starting ${reportID} for ${keyPair.customerName} at ${yyyyMMdd.format(startTime.getTime())}-${yyyyMMdd.format(currentYearEnd.getTime())}")
                                        //LaserStatsCursor.withTransaction {
                                        //LaserStatsCursor lsc = LaserStatsCursor.construct([platform: c4as.platform, customer: keyPair.customer, reportID: reportID, latestFrom: calendarConfig.startDate, latestTo: calendarConfig.endNextRun])
                                        boolean more = true
                                        while (more) {
                                            log.debug("${Thread.currentThread().getName()} is getting ${reportID} for ${keyPair.customerName} from ${yyyyMMdd.format(startTime.getTime())}-${yyyyMMdd.format(currentYearEnd.getTime())}")
                                            Map<String, Object> result = performCounter4Request(sql, statsSql, statsUrl, reportID, calendarConfig.now, startTime, currentYearEnd, c4asPlatform, keyPair, namespaces)
                                            if(result.error && result.error != true) {
                                                notifyError(sql, [platform: c4asPlatform.name, uuid: c4asPlatform.gokbId, url: statsUrl, error: result, customer: keyPair.customerName, keyPair: "${keyPair.value}:${keyPair.requestorKey}"])
                                                sql.executeInsert('insert into stats_missing_period (smp_version, smp_from_date, smp_to_date, smp_customer_fk, smp_platform_fk, smp_report_id) values (0, :from, :to, :customer, :platform, :reportID)',
                                                        [platform: c4asPlatform.id, customer: keyPair.customerId, reportID: reportID, from: new Timestamp(startTime.getTimeInMillis()), to: new Timestamp(currentYearEnd.getTimeInMillis())])
                                            }
                                            /*
                                            if(incremental) {
                                                lsc.missingPeriods.each { StatsMissingPeriod period ->
                                                    startTime.setTime(period.from)
                                                    currentYearEnd.setTime(period.to)
                                                }
                                                more = false
                                            }
                                            else {*/
                                            startTime.add(Calendar.YEAR, 1)
                                            currentYearEnd.add(Calendar.YEAR, 1)
                                            log.debug("${Thread.currentThread().getName()} is getting to ${yyyyMMdd.format(startTime.getTime())}-${yyyyMMdd.format(currentYearEnd.getTime())} for report ${reportID}")
                                            if (calendarConfig.now.before(startTime)) {
                                                more = false
                                                log.debug("${Thread.currentThread().getName()} has finished current data fetching for report ${reportID}. Processing missing periods for ${keyPair.customerName}")
                                                Map<String, Object> missingParams = [customer: keyPair.customerId, platform: c4asPlatform.id, report: reportID]
                                                List<GroovyRowResult> currentMissingPeriods = sql.rows('select smp_id, smp_from_date, smp_to_date from stats_missing_period where smp_customer_fk = :customer and smp_platform_fk = :platform and smp_report_id = :report', missingParams)
                                                List<Object> donePeriods = []
                                                currentMissingPeriods.each { GroovyRowResult row ->
                                                    Calendar from = GregorianCalendar.getInstance(), to = GregorianCalendar.getInstance()
                                                    from.setTimeInMillis(row.get("smp_from_date").getTime())
                                                    to.setTimeInMillis(row.get("smp_to_date").getTime())
                                                    result = performCounter4Request(sql, statsSql, statsUrl, reportID, calendarConfig.now, from, to, c4asPlatform, keyPair, namespaces)
                                                    if(result.success) {
                                                        donePeriods << row.get("smp_id")
                                                    }
                                                    else if(result.error) {
                                                        notifyError(sql, [platform: c4asPlatform.name, uuid: c4asPlatform.gokbId, url: statsUrl, error: result.error, customer: keyPair.customerName, keyPair: "${keyPair.value}:${keyPair.requestorKey}"])
                                                    }
                                                }
                                                if(donePeriods.size() > 0) {
                                                    log.debug("${Thread.currentThread().getName()} has fetched missing data, removing rows ${donePeriods.toListString()}")
                                                    sql.execute('delete from stats_missing_period where smp_id = any(:periodIds)', [periodIds: sql.connection.createArrayOf('bigint', donePeriods.toArray())])
                                                }
                                                log.debug("${Thread.currentThread().getName()} has finished report ${reportID} and gets next report for ${keyPair.customerName}")
                                            }
                                        }
                                        Calendar lastMonth = GregorianCalendar.getInstance()
                                        lastMonth.add(Calendar.MONTH, -1)
                                        lastMonth.set(Calendar.DAY_OF_MONTH, 1)
                                        Timestamp startOfMonth = new Timestamp(lastMonth.getTimeInMillis())
                                        sql.execute("insert into laser_stats_cursor(lsc_version, lsc_report_id, lsc_platform_fk, lsc_customer_fk, lsc_latest_from_date, lsc_latest_to_date) values " +
                                                "(0, :reportID, :platform, :customer, :latestFrom, :latestTo) " +
                                                "on conflict on constraint lsc_unique_report_per_customer do " +
                                                "update set lsc_latest_from_date = :latestFrom, lsc_latest_to_date = :latestTo",
                                                [platform: c4asPlatform.id, customer: keyPair.customerId, reportID: reportID, latestFrom: startOfMonth, latestTo: new Timestamp(calendarConfig.now.getTimeInMillis())])
                                        /*
                                        lsc.latestFrom = startTime.getTime()
                                        lsc.latestTo = calendarConfig.now.getTime()
                                        lsc.save()*/
                                        //}
                                    }
                                    //}
                                }
                            }
                        }
                    }
                    else {
                        log.info("no valid customer value / requestor key pairs recorded for COUNTER 4 source ${statsUrl}")
                    }
                }
                else {
                    log.error("Report error for ${c4asPlatform} / ${c4asPlatform.gokbId}! They said that their platform would support COUNTER 4 but entered no SUSHI URL!")
                }
            }
            c5SushiSources.each { List c5as ->
                //grasp all customer numbers with requestor keys
                Platform c5asPlatform = Platform.findByGokbId(c5as[0])
                if(c5as[1] != null) {
                    String statsUrl = c5as[1] //.endsWith('/') ? c5as[1] : c5as[1]+'/' does not work with every platform!
                    List keyPairs = CustomerIdentifier.executeQuery('select new map(cust.id as customerId, cust.sortname as customerName, ci.value as value, ci.requestorKey as requestorKey) from CustomerIdentifier ci join ci.customer cust where ci.platform = :plat and ci.value != null and ci.requestorKey != null', [plat: c5asPlatform])
                    if(keyPairs) {
                        GParsPool.withPool(THREAD_POOL_SIZE) { pool ->
                            keyPairs.eachWithIndexParallel { Map<String, Object> keyPair, int i ->
                                Sql sql = GlobalService.obtainSqlConnection(), statsSql = GlobalService.obtainStorageSqlConnection()
                                sql.withTransaction {
                                    List laserStatsCursor = sql.rows("select lsc_latest_from_date, lsc_latest_to_date,lsc_report_id from laser_stats_cursor where lsc_platform_fk = :platform and lsc_customer_fk = :customer", [platform: c5asPlatform.id, customer: keyPair.customerId])
                                    boolean onlyNewest = laserStatsCursor ? incremental : false
                                    //is done here because no thread-safe date classes are available and this is a workaround
                                    Map<String, Object> calendarConfig = initCalendarConfig(onlyNewest)
                                    //DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                    Calendar startTime = GregorianCalendar.getInstance(), currentYearEnd = GregorianCalendar.getInstance()
                                    SimpleDateFormat monthFormatter = DateUtils.getSDF_yyyyMM()
                                    //TitleInstancePackagePlatform.withNewTransaction {
                                    log.debug("${Thread.currentThread().getName()} now processing key pair ${i}, requesting data for ${keyPair.customerName}:${keyPair.value}:${keyPair.requestorKey}")
                                    String params = "?customer_id=${keyPair.value}&requestor_id=${keyPair.requestorKey}&api_key=${keyPair.requestorKey}"
                                    /*
                                    Map<String, Map<String, Object>> arguments = (Map<String, Map<String, Object>>) JSON.parse(c5as.arguments)
                                    arguments.eachWithIndex { String arg, Map<String, Object> value, int argIndex ->
                                        def obj
                                        String val
                                        switch (value.object) {
                                            case "platform": obj = c5asPlatform
                                                val = obj[value.field]
                                                break
                                            default: val = value.value
                                                break
                                        }
                                        params += "${arg}=${val}"
                                        if(argIndex < arguments.size()-1)
                                            params += "&"
                                    }
                                    */
                                    Map<String, Object> availableReports = fetchJSONData(statsUrl+params, true)
                                    if(availableReports && availableReports.list) {
                                        List<String> reportList = availableReports.list.collect { listEntry -> listEntry["Report_ID"].toLowerCase() }
                                        reportList.each { String reportId ->
                                            startTime.setTime(calendarConfig.startDate)
                                            currentYearEnd.setTime(calendarConfig.endNextRun)
                                            if(onlyNewest) {
                                                GroovyRowResult row = laserStatsCursor.find { GroovyRowResult rr -> rr.get("lsc_report_id") == reportId }
                                                if(row) {
                                                    startTime.setTimeInMillis(row.get("lsc_latest_from_date").getTime())
                                                    startTime.add(Calendar.MONTH, 1)
                                                    currentYearEnd.setTimeInMillis(row.get("lsc_latest_to_date").getTime())
                                                    currentYearEnd.add(Calendar.MONTH, 1)
                                                }
                                            }
                                            //LaserStatsCursor lsc = LaserStatsCursor.construct([platform: c5asPlatform, customer: keyPair.customer, reportID: reportId, latestFrom: calendarConfig.startDate, latestTo: calendarConfig.endNextRun])
                                            boolean more = true
                                            while(more) {
                                                String reportReqId = statsUrl.endsWith("/") ? reportId : '/'+reportId, url = statsUrl+reportReqId+params+"&begin_date=${monthFormatter.format(startTime.getTime())}&end_date=${monthFormatter.format(currentYearEnd.getTime())}"
                                                Map report = performCounter5Request(sql, statsSql, url, reportId, c5asPlatform, keyPair, namespaces)
                                                if(report.error) {
                                                    notifyError(sql, [platform: c5asPlatform.name, uuid: c5asPlatform.gokbId, url: url, error: report.error, customer: keyPair.customerName, keyPair: "${keyPair.value}:${keyPair.requestorKey}"])
                                                    sql.executeInsert('insert into stats_missing_period (smp_version, smp_from_date, smp_to_date, smp_customer_fk, smp_platform_fk, smp_report_id) values (0, :from, :to, :customer, :platform, :reportID)',
                                                            [platform: c5asPlatform.id, customer: keyPair.customerId, reportID: reportId, from: new Timestamp(startTime.getTimeInMillis()), to: new Timestamp(currentYearEnd.getTimeInMillis())])
                                                }
                                                startTime.add(Calendar.YEAR, 1)
                                                currentYearEnd.add(Calendar.YEAR, 1)
                                                log.debug("${Thread.currentThread().getName()} is getting to ${yyyyMMdd.format(startTime.getTime())}-${yyyyMMdd.format(currentYearEnd.getTime())} for report ${reportId}")
                                                if(calendarConfig.now.before(startTime)) {
                                                    more = false
                                                    log.debug("${Thread.currentThread().getName()} has finished fetching running data for report ${reportId}. Processing missing periods for ${keyPair.customerName} ...")
                                                    Map<String, Object> missingParams = [customer: keyPair.customerId, platform: c5asPlatform.id, report: reportId]
                                                    List<GroovyRowResult> currentMissingPeriods = sql.rows('select smp_id, smp_from_date, smp_to_date from stats_missing_period where smp_customer_fk = :customer and smp_platform_fk = :platform and smp_report_id = :report', missingParams)
                                                    List<Object> donePeriods = []
                                                    currentMissingPeriods.each { GroovyRowResult row ->
                                                        Date from = row.get("smp_from_date"), to = row.get("smp_to_date")
                                                        report = performCounter5Request(sql, statsSql, statsUrl+reportReqId+params+"&begin_date=${monthFormatter.format(from)}&end_date=${monthFormatter.format(to)}", reportId, c5asPlatform, keyPair, namespaces)
                                                        if(report.success) {
                                                            donePeriods << row.get("smp_id")
                                                        }
                                                        else if(report.error) {
                                                            notifyError(sql, [platform: c5asPlatform.name, uuid: c5asPlatform.gokbId, url: url, error: report.error, customer: keyPair.customerName, keyPair: "${keyPair.value}:${keyPair.requestorKey}"])
                                                        }
                                                    }
                                                    if(donePeriods.size() > 0) {
                                                        log.debug("${Thread.currentThread().getName()} has fetched missing data, removing rows ${donePeriods.toListString()}")
                                                        sql.execute('delete from stats_missing_period where smp_id = any(:periodIds)', [periodIds: sql.connection.createArrayOf('bigint', donePeriods.toArray())])
                                                    }
                                                    log.debug("${Thread.currentThread().getName()} has finished report ${reportId} and gets next report for ${keyPair.customerName}")
                                                }
                                                //}
                                            }
                                            Calendar lastMonth = GregorianCalendar.getInstance()
                                            lastMonth.add(Calendar.MONTH, -1)
                                            lastMonth.set(Calendar.DAY_OF_MONTH, 1)
                                            Timestamp startOfMonth = new Timestamp(lastMonth.getTimeInMillis())
                                            sql.execute("insert into laser_stats_cursor(lsc_version, lsc_report_id, lsc_platform_fk, lsc_customer_fk, lsc_latest_from_date, lsc_latest_to_date) values " +
                                                    "(0, :reportID, :platform, :customer, :latestFrom, :latestTo) " +
                                                    "on conflict on constraint lsc_unique_report_per_customer do " +
                                                    "update set lsc_latest_from_date = :latestFrom, lsc_latest_to_date = :latestTo",
                                                    [platform: c5asPlatform.id, customer: keyPair.customerId, reportID: reportId, latestFrom: startOfMonth, latestTo: new Timestamp(calendarConfig.now.getTimeInMillis())])
                                            /*
                                            lsc.latestFrom = startTime.getTime()
                                            lsc.latestTo = calendarConfig.now.getTime()
                                            lsc.save()
                                             */
                                        }
                                    }
                                    else {
                                        notifyError(sql, [platform: c5asPlatform.name, uuid: c5asPlatform.gokbId ,url: statsUrl ,error: availableReports.error, customer: keyPair.customerName, keyPair: "${keyPair.value}:${keyPair.requestorKey}"])
                                    }
                                }
                            }
                        }
                    }
                    else {
                        log.info("no valid customer value / key pairs recorded for this COUNTER 5 source: ${statsUrl}")
                    }
                }
                else {
                    log.error("Report error for ${c5asPlatform} / ${c5asPlatform.gokbId}! They said that their platform would support COUNTER 5 but entered no SUSHI URL!")
                }
            }
            running = false
            log.debug("fetch stats finished")
    }

    /**
     * Internal
     * Marks an error which occurred during a sync run as {@link SystemEvent} for that administrators get notified
     * @param sql the SQL connection to use
     * @param result the request response containing details of the error circumstances
     */
    void notifyError(Sql sql, Map result) {
//        Map<String, Object> event = SystemEvent.DEFINED_EVENTS.STATS_SYNC_JOB_WARNING
//        sql.executeInsert('insert into system_event (se_category, se_created, se_payload, se_relevance, se_token) values (:cat, now(), :error, :rel, :token)', [cat: event.category.value, error: new JSON(result).toString(false), rel: event.relevance.value, token: 'STATS_SYNC_JOB_WARNING'])

        log.warn 'notifyError: ' + result.error?.toString()

        Map payload = result.clone() as Map
        payload.remove('error')
        SystemEvent.createEvent('STATS_SYNC_JOB_WARNING', payload)
    }

    /**
     * Internal
     * Executes a request for statistics data from a provider's server according to COUNTER 4 and enters the statistics data read off from the given server.
     * The usage data is then being inserted for each title context
     * @param sql the SQL connection to use for data flow
     * @param statsUrl the URL from where usage should be retrieved
     * @param reportID the report which should be fetched
     * @param now the current time
     * @param startTime the start of reporting time span
     * @param endTime the end of reporting time span
     * @param c4asPlatform the platform for which usage data should be retrieved
     * @param keyPair the customer number-requestor ID/API key pair used to authentify the customer
     * @param namespaces the identifier namespaces within which a matching identifier may be contained
     * @return a map containing the request status - success or error on failure
     */
    Map<String, Object> performCounter4Request(Sql sql, Sql statsSql, String statsUrl, String reportID, Calendar now, Calendar startTime, Calendar endTime, Platform c4asPlatform, Map keyPair, Set<Long> namespaces) {
        StreamingMarkupBuilder requestBuilder = new StreamingMarkupBuilder()
        def requestBody = requestBuilder.bind {
            mkp.xmlDeclaration()
            mkp.declareNamespace(x: "http://schemas.xmlsoap.org/soap/envelope/")
            mkp.declareNamespace(cou: "http://www.niso.org/schemas/sushi/counter")
            mkp.declareNamespace(sus: "http://www.niso.org/schemas/sushi")
            x.Envelope {
                x.Header {}
                x.Body {
                    cou.ReportRequest(Created: DateUtils.getSDF_yyyyMMddTHHmmss().format(now.getTime()), ID: '?') {
                        sus.Requestor {
                            sus.ID(keyPair.requestorKey)
                            sus.Name('?')
                            sus.Email('?')
                        }
                        sus.CustomerReference {
                            sus.ID(keyPair.value)
                            sus.Name('?')
                        }
                        sus.ReportDefinition(Name: reportID, Release: 4) {
                            sus.Filters {
                                sus.UsageDateRange {
                                    sus.Begin(yyyyMMdd.format(startTime.getTime()))
                                    //if (currentYearEnd.before(calendarConfig.now))
                                    sus.End(yyyyMMdd.format(endTime.getTime()))
                                    /*else {
                                        sus.End(calendarConfig.now.format("yyyy-MM-dd"))
                                    }*/
                                }
                            }
                        }
                    }
                }
            }
        }
        //log.debug(requestBody.toString())
        //def because on success, I return the GPathResult, a map otherwise
        def result = fetchXMLData(statsUrl, requestBody)
        if (result && result instanceof GPathResult) {
            GPathResult xml = result
            xml.declareNamespace(["SOAP-ENV": "http://schemas.xmlsoap.org/soap/envelope/",
                                  ns1       : "http://www.niso.org/schemas/sushi",
                                  ns2       : "http://www.niso.org/schemas/counter",
                                  ns3       : "http://www.niso.org/schemas/sushi/counter"])
            if (['3000', '3020'].any { String errorCode -> errorCode == xml.'SOAP-ENV:Body'.'ReportResponse'?.'ns1:Exception'?.'ns1:Number'?.text() }) {
                log.warn(xml.'SOAP-ENV:Body'.'ReportResponse'.'ns1:Exception'.'ns1:Message'.text())
                log.debug(requestBody.toString())
                [error: true]
            }
            else if (xml.'SOAP-ENV:Body'.'ReportResponse'?.'ns1:Exception'?.'ns1:Number'?.text() == '3030') {
                log.info("no data for given period")
                //StatsMissingPeriod.construct([from: startTime.getTime(), to: currentYearEnd.getTime(), cursor: lsc])
                [error: true]
            }
            else {
                GPathResult reportData = xml.'SOAP-ENV:Body'.'ns3:ReportResponse'.'ns3:Report'
                //StatsMissingPeriod wasMissing = lsc.missingPeriods.find{ StatsMissingPeriod period -> period.from == startTime.getTime() && period.to == currentYearEnd.getTime() }
                //if(wasMissing)
                //lsc.missingPeriods.remove(wasMissing)
                GPathResult reportItems = reportData.'ns2:Report'.'ns2:Customer'.'ns2:ReportItems'
                if(reportID == Counter4Report.PLATFORM_REPORT_1) {
                    int[] resultCount = statsSql.withBatch( "insert into counter4report (c4r_version, c4r_platform_fk, c4r_publisher, c4r_report_institution_fk, c4r_report_type, c4r_category, c4r_metric_type, c4r_report_from, c4r_report_to, c4r_report_count) " +
                            "values (:version, :platform, :publisher, :reportInstitution, :reportType, :category, :metricType, :reportFrom, :reportTo, :reportCount) " +
                            "on conflict on constraint unique_counter_4_report do " +
                            "update set c4r_report_count = :reportCount") { stmt ->
                        int t = 0
                        //titles.each { row ->
                        reportItems.each { reportItem ->
                            Set<String> identifiers = []
                            reportItem.'ns2:ItemIdentifier'.'ns2:Value'.each { identifier ->
                                identifiers << identifier.text()
                            }
                            int ctr = 0
                            reportItem.'ns2:ItemPerformance'.each { performance ->
                                performance.'ns2:Instance'.each { instance ->
                                    //findAll seems to be less performant than loop processing
                                    //if (instance.'ns2:MetricType'.text() == "ft_total") {
                                    log.debug("${Thread.currentThread().getName()} processes performance ${ctr} for platform")
                                    String category = performance.'ns2:Category'.text()
                                    String metricType = instance.'ns2:MetricType'.text()
                                    Integer count = Integer.parseInt(instance.'ns2:Count'.text())
                                    Map<String, Object> configMap = [reportType: reportData.'ns2:Report'.'@Name'.text(), version: 0]
                                    configMap.reportInstitution = keyPair.customerId
                                    configMap.platform = c4asPlatform.id
                                    configMap.publisher = reportItem.'ns2:ItemPublisher'.text()
                                    configMap.reportFrom = new Timestamp(DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text()).getTime())
                                    configMap.reportTo = new Timestamp(DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:End'.text()).getTime())
                                    configMap.category = category
                                    configMap.metricType = metricType
                                    configMap.reportCount = count
                                    //c4r_platform_fk, c4r_report_institution_fk, c4r_report_type, c4r_category, c4r_metric_type, c4r_report_from, c4r_report_to, c4r_report_count
                                    Map<String, Object> selMap = configMap.clone() as Map<String, Object> //simple assignment makes call by reference so modifies the actual object
                                    selMap.remove('version')
                                    selMap.remove('reportCount')
                                    List<GroovyRowResult> existingKey = statsSql.rows('select c4r_id from counter4report where c4r_platform_fk = :platform ' +
                                            'and c4r_report_institution_fk = :reportInstitution ' +
                                            'and c4r_report_type = :reportType ' +
                                            'and c4r_metric_type = :metricType ' +
                                            'and c4r_report_from = :reportFrom ' +
                                            'and c4r_report_to = :reportTo', selMap)
                                    if(existingKey) {
                                        statsSql.execute('update counter4report set c4r_report_count = :reportCount, c4r_publisher = :publisher where c4r_id = :reportId', [reportCount: count, publisher: reportItem.'ns2:ItemPublisher'.text(), reportId: existingKey[0].get('c4r_id')])
                                    }
                                    else
                                        stmt.addBatch(configMap)
                                    ctr++
                                    //}
                                }
                            }
                        }
                    }
                    log.debug("${Thread.currentThread().getName()} reports success: ${resultCount.length}")
                }
                else {
                    int[] resultCount = statsSql.withBatch( "insert into counter4report (c4r_version, c4r_title_fk, c4r_publisher, c4r_platform_fk, c4r_report_institution_fk, c4r_report_type, c4r_category, c4r_metric_type, c4r_report_from, c4r_report_to, c4r_report_count) " +
                            "values (:version, :title, :publisher, :platform, :reportInstitution, :reportType, :category, :metricType, :reportFrom, :reportTo, :reportCount) " +
                            "on conflict on constraint unique_counter_4_report do " +
                            "update set c4r_report_count = :reportCount") { stmt ->
                        int t = 0
                        //titles.each { row ->
                        reportItems.each { reportItem ->
                            Set<String> identifiers = []
                            reportItem.'ns2:ItemIdentifier'.'ns2:Value'.each { identifier ->
                                identifiers << identifier.text()
                            }
                            int ctr = 0
                            List<GroovyRowResult> rows = sql.rows("select tipp_id from title_instance_package_platform join identifier on id_tipp_fk = tipp_id where id_value = any(:identifiers) and id_ns_fk = any(:namespaces) and tipp_plat_fk = :platform and tipp_status_rv_fk != :removed", [identifiers: sql.connection.createArrayOf('varchar', identifiers as Object[]), namespaces: sql.connection.createArrayOf('bigint', namespaces as Object[]), platform: c4asPlatform.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                            //Map row = titles.find { rowMap -> rowMap.identifier == reportItem.'ns2:ItemIdentifier'.'ns2:Value'.text() }
                            //GPathResult reportItem = reportItems.findAll { reportItem -> identifier == reportItem.'ns2:ItemIdentifier'.'ns2:Value'.text() }
                            if(rows) {
                                //GroovyRowResult row = rows[0] //this was necessary because the same title may be available in different packages and we do not want duplicates! - ERROR! Unfortunately, I lose package context by filtering that out and Preselect let us pay that expensively ...
                                t++
                                rows.eachWithIndex { GroovyRowResult row, int ctx ->
                                    Long title = row.get('tipp_id') as Long
                                    reportItem.'ns2:ItemPerformance'.each { performance ->
                                        performance.'ns2:Instance'.each { instance ->
                                            //findAll seems to be less performant than loop processing
                                            //if (instance.'ns2:MetricType'.text() == "ft_total") {
                                            log.debug("${Thread.currentThread().getName()} processes performance ${ctr} for title ${t} in context ${ctx}")
                                            String category = performance.'ns2:Category'.text()
                                            String metricType = instance.'ns2:MetricType'.text()
                                            String publisher = reportItem.'ns2:ItemPublisher'.text()
                                            Integer count = Integer.parseInt(instance.'ns2:Count'.text())
                                            Map<String, Object> configMap = [reportType: reportData.'ns2:Report'.'@Name'.text(), version: 0]
                                            configMap.title = title
                                            configMap.reportInstitution = keyPair.customerId
                                            configMap.platform = c4asPlatform.id
                                            configMap.reportFrom = new Timestamp(DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:Begin'.text()).getTime())
                                            configMap.reportTo = new Timestamp(DateUtils.parseDateGeneric(performance.'ns2:Period'.'ns2:End'.text()).getTime())
                                            configMap.category = category
                                            configMap.metricType = metricType
                                            configMap.publisher = publisher
                                            configMap.reportCount = count
                                            //c4r_title_fk, c4r_publisher, c4r_platform_fk, c4r_report_institution_fk, c4r_report_type, c4r_category, c4r_metric_type, c4r_report_from, c4r_report_to, c4r_report_count
                                            stmt.addBatch(configMap)
                                            /*
                                            try {
                                                Counter4Report c4report = Counter4Report.construct(configMap)
                                                if (c4report)
                                                log.debug("${Thread.currentThread().getName()} report ${c4report} successfully saved")
                                            }
                                            catch (CreationException e) {
                                                log.error(e.message)
                                            }
                                            */
                                            ctr++
                                            //}
                                        }
                                    }
                                }
                            }
                            if(!rows) log.debug("no title found for ${reportItem.'ns2:ItemIdentifier'.'ns2:Value'.text()}")
                        }
                    }
                    log.debug("${Thread.currentThread().getName()} reports success: ${resultCount.length}")
                }
                [success: true]
            }
        }
        else if(result && result instanceof Map)
            result
        else [error: true]
    }

    /**
     * Internal
     * Executes a request for statistics data from a provider's server according to COUNTER 5 and enters the statistics data read off from the given server.
     * The usage data is then being inserted for each title context
     * @param sql the SQL connection to use for data flow
     * @param url the URL from where usage should be retrieved
     * @param reportId the report which should be fetched
     * @param c5asPlatform the platform for which usage data should be retrieved
     * @param keyPair the customer number-requestor ID/API key pair used to authentify the customer
     * @param namespaces the identifier namespaces within which a matching identifier may be contained
     * @return a map containing the request status - success or error on failure
     */
    Map<String, Object> performCounter5Request(Sql sql, Sql statsSql, String url, String reportId, Platform c5asPlatform, Map keyPair, Set<Long> namespaces) {
        Map<String, Object> report = fetchJSONData(url)
        if(report.header) {
            List<Map> exceptions = report.header.Exceptions
            if([3000, 3020].any { Integer errorCode -> errorCode in exceptions.collect { Map exception -> exception.Code } }) {
                exceptions.each { Map exception ->
                    log.warn(exception.Message)
                }
            }
            else if(exceptions.find { Map exception -> exception.Code == 3030}) {
                //StatsMissingPeriod.construct([from: startTime.getTime(), to: currentYearEnd.getTime(), cursor: lsc])
                log.info("no data available for given period")
            }
            else {
                //StatsMissingPeriod wasMissing = lsc.missingPeriods.find{ StatsMissingPeriod period -> period.from == startTime.getTime() && period.to == currentYearEnd.getTime() }
                //if(wasMissing)
                //lsc.missingPeriods.remove(wasMissing)
                if(reportId in [Counter5Report.PLATFORM_MASTER_REPORT, Counter5Report.PLATFORM_USAGE]) {
                    int[] resultCount = statsSql.withBatch( "insert into counter5report (c5r_version, c5r_publisher, c5r_platform_fk, c5r_report_institution_fk, c5r_report_type, c5r_metric_type, c5r_report_from, c5r_report_to, c5r_report_count) " +
                            "values (:version, :publisher, :platform, :reportInstitution, :reportType, :metricType, :reportFrom, :reportTo, :reportCount)") { stmt ->
                        report.items.each { Map reportItem ->
                            int ctr = 0
                            reportItem.Performance.each { Map performance ->
                                performance.Instance.each { Map instance ->
                                    log.debug("${Thread.currentThread().getName()} processes performance ${ctr} for platform")
                                    Map<String, Object> configMap = [reportType: report.header.Report_ID, version: 0]
                                    configMap.reportInstitution = keyPair.customerId
                                    configMap.platform = c5asPlatform.id
                                    configMap.publisher = reportItem.Publisher
                                    configMap.reportFrom = new Timestamp(DateUtils.parseDateGeneric(performance.Period.Begin_Date).getTime())
                                    configMap.reportTo = new Timestamp(DateUtils.parseDateGeneric(performance.Period.End_Date).getTime())
                                    configMap.metricType = instance.Metric_Type
                                    configMap.reportCount = instance.Count as int
                                    Map<String, Object> selMap = configMap.clone() as Map<String, Object> //simple assignment makes call by reference so modifies the actual object
                                    selMap.remove('version')
                                    selMap.remove('reportCount')
                                    List<GroovyRowResult> existingKey = statsSql.rows('select c5r_id from counter5report where c5r_publisher = :publisher ' +
                                            'and c5r_platform_fk = :platform ' +
                                            'and c5r_report_institution_fk = :reportInstitution ' +
                                            'and c5r_report_type = :reportType ' +
                                            'and c5r_metric_type = :metricType ' +
                                            'and c5r_report_from = :reportFrom ' +
                                            'and c5r_report_to = :reportTo', selMap)
                                    if(existingKey) {
                                        statsSql.execute('update counter5report set c5r_report_count = :reportCount where c5r_id = :reportId', [reportCount: instance.Count as int, reportId: existingKey[0].get('c5r_id')])
                                    }
                                    else
                                        stmt.addBatch(configMap)
                                    /*
                                    try {
                                        Counter5Report c5report = Counter5Report.construct(configMap)
                                        if(c5report)
                                            log.debug("${Thread.currentThread().getName()} report ${c5report} successfully saved")
                                    }
                                    catch(CreationException e) {
                                        log.error(e.message)
                                    }
                                     */
                                }
                            }
                        }
                    }
                    log.debug("${Thread.currentThread().getName()} reports success: ${resultCount.length}")
                }
                else {
                    int[] resultCount = statsSql.withBatch( "insert into counter5report (c5r_version, c5r_title_fk, c5r_publisher, c5r_platform_fk, c5r_report_institution_fk, c5r_report_type, c5r_metric_type, c5r_access_type, c5r_access_method, c5r_report_from, c5r_report_to, c5r_report_count) " +
                            "values (:version, :title, :publisher, :platform, :reportInstitution, :reportType, :metricType, :accessType, :accessMethod, :reportFrom, :reportTo, :reportCount) " +
                            "on conflict on constraint unique_counter_5_report do " +
                            "update set c5r_report_count = :reportCount, c5r_access_type = :accessType, c5r_access_method = :accessMethod") { stmt ->
                        int t = 0
                        report.items.each { Map reportItem ->
                            int ctr = 0
                            Set<String> identifiers = []
                            reportItem["Item_ID"].each { idData ->
                                identifiers << idData.Value
                            }
                            /*
                            String doi = reportItem["Item_ID"].find { idData -> idData["Type"] == "DOI" }?.Value,
                                   isbn = reportItem["Item_ID"].find { idData -> idData["Type"] == "ISBN" }?.Value,
                                   issn = reportItem["Item_ID"].find { idData -> idData["Type"] == "ISSN" }?.Value
                             */
                            //Set<TitleInstancePackagePlatform> titles = TitleInstancePackagePlatform.executeQuery('select tipp from Identifier id join id.tipp tipp where ((id.value = :doi and id.ns.ns = :doiNs) or (id.value = :isbn and id.ns.ns = :isbnNs) or (id.value = :issn and id.ns.ns = :issnNs)) and tipp.platform = :plat and exists (select ie.id from IssueEntitlement ie join ie.subscription sub join sub.orgRelations oo where oo.org = :customer and ie.tipp = tipp)',[doi: doi, doiNs: IdentifierNamespace.DOI, isbn: isbn, isbnNs: IdentifierNamespace.ISBN, issn: issn, issnNs: IdentifierNamespace.EISSN, plat: c5as.platform, customer: keyPair.customer])
                            List<GroovyRowResult> rows = sql.rows("select tipp_id from title_instance_package_platform join identifier on id_tipp_fk = tipp_id where id_value = any(:identifiers) and id_ns_fk = any(:namespaces) and tipp_plat_fk = :platform and tipp_status_rv_fk != :removed", [identifiers: sql.connection.createArrayOf('varchar', identifiers as Object[]), namespaces: sql.connection.createArrayOf('bigint', namespaces as Object[]), platform: c5asPlatform.id, removed: RDStore.TIPP_STATUS_REMOVED.id])
                            List<Map> performances = reportItem.Performance as List<Map>
                            if(rows) {
                                //GroovyRowResult row = rows[0] //this is necessary because the same title may be available in different packages and we do not want duplicates! ERROR! See COUNTER 4 - the package context is too important; I must save the usage data for each context
                                t++
                                rows.eachWithIndex { GroovyRowResult row, int ctx ->
                                    Long title = row.get('tipp_id') as Long
                                    performances.each { Map performance ->
                                        performance.Instance.each { Map instance ->
                                            log.debug("${Thread.currentThread().getName()} processes performance ${ctr} for title ${t} in context ${ctx}")
                                            Map<String, Object> configMap = [reportType: report.header.Report_ID, version: 0]
                                            configMap.title = title
                                            configMap.reportInstitution = keyPair.customerId
                                            configMap.platform = c5asPlatform.id
                                            configMap.publisher = reportItem.Publisher
                                            configMap.reportFrom = new Timestamp(DateUtils.parseDateGeneric(performance.Period.Begin_Date).getTime())
                                            configMap.reportTo = new Timestamp(DateUtils.parseDateGeneric(performance.Period.End_Date).getTime())
                                            configMap.accessType = reportItem.Access_Type
                                            configMap.accessMethod = reportItem.Access_Method
                                            configMap.metricType = instance.Metric_Type
                                            configMap.reportCount = instance.Count as int
                                            stmt.addBatch(configMap)
                                            /*
                                            try {
                                                Counter5Report c5report = Counter5Report.construct(configMap)
                                                if(c5report)
                                                    log.debug("${Thread.currentThread().getName()} report ${c5report} successfully saved")
                                            }
                                            catch(CreationException e) {
                                                log.error(e.message)
                                            }
                                            */
                                            ctr++
                                        }
                                    }
                                }
                            }
                            else log.error("no matching titles determined for ${identifiers}")
                        }
                    }
                    log.debug("${Thread.currentThread().getName()} reports success: ${resultCount.length}")
                }
            }
            [success: true]
        }
        else {
            log.error("report header is missing for some reason??? request data: ${url}, response data: ${report.toMapString()}")
            [error: report.error]
        }
        /*if(incremental) {
            lsc.missingPeriods.each { StatsMissingPeriod period ->
                startTime.setTime(period.from)
                currentYearEnd.setTime(period.to)
            }
            more = false
        }
        else {*/
    }

    @Deprecated
    boolean createSushiSource(Map<String, Object> configMap) {
        AbstractCounterApiSource source = AbstractCounterApiSource.construct(configMap)
        if(source) {
            log.debug("source ${source} created")
            true
        }
        else false
    }

    /**
     * Internal
     * Fetches the given COUNTER 5 report from the given URL and returns the JSON response
     * @param url the URL to fetch data from
     * @param requestList is the list of available reports fetched?
     * @return the JSON response map
     */
    Map<String, Object> fetchJSONData(String url, boolean requestList = false) {
        Map<String, Object> result = [:]
        try {
            Closure success = { resp, json ->
                if(resp.code() == 200) {
                    if(json instanceof JSONArray) {
                        result.list = json
                    }
                    else if(json != null && !json.containsKey("Exception") && !requestList) {
                        result.header = json["Report_Header"]
                        result.items = json["Report_Items"]
                    }
                    else if(json != null) {
                        result.error = json["Exception"]["Message"]
                    }
                    else {
                        result.error = "invalid JSON returned, retry call"
                    }
                }
                else if(json.containsKey("Report_Header")) {
                    result.header = json["Report_Header"]
                }
                else {
                    result.error = "server response: ${resp.status()}"
                }
            }
            Closure failure = { resp, reader ->
                if(reader.containsKey("Report_Header"))
                    result.header = reader["Report_Header"]
                else
                    result.error = "server response: ${resp.status()} - ${reader}"
            }
            HttpClientConfiguration config = new DefaultHttpClientConfiguration()
            config.readTimeout = Duration.ofMinutes(1)
            BasicHttpClient http = new BasicHttpClient(url, config)
            http.get(BasicHttpClient.ResponseType.JSON, success, failure)
            http.close()
        }
        catch (Exception e) {
            result.error = "invalid response returned for ${url} - ${e.getMessage()}!"
            log.error("stack trace: ", e)
        }
        result
    }

    /**
     * Internal
     * Fetches the given COUNTER 4 report from the given URL and returns the XML node
     * @param url the URL to fetch data from
     * @param requestBody is the list of available reports fetched?
     * @return the response body or an error map upon failure
     */
    def fetchXMLData(String url, requestBody) {
        def result = null

        BasicHttpClient http
        try  {
            HttpClientConfiguration config = new DefaultHttpClientConfiguration()
            config.readTimeout = Duration.ofMinutes(1)
            http = new BasicHttpClient(url, config)
            Closure success = { resp, GPathResult xml ->
                if(resp.code() == 200) {
                    result = xml
                }
                else {
                    result = [error: "server response: ${resp.status()}"]
                }
            }
            Closure failure = { resp, reader ->
                result = [error: "server response: ${resp.status()} - ${reader}"]
            }
            http.post(["Accept": "application/soap+xml; charset=utf-8"], BasicHttpClient.ResponseType.XML, BasicHttpClient.PostType.SOAP, requestBody.toString(), success, failure)
        }
        catch (Exception e) {
            result = [error: "invalid response returned for ${url} - ${e.getMessage()}!"]
            log.error("stack trace: ", e)
            log.error("Request body was: ${requestBody}")
        }
        finally {
            if (http) { http.close() }
        }
        result
    }

    /**
     * Internal
     * Initialises the calendar map for the next bunch of requests
     * @param onlyNewest should only the most recent data be fetched?
     * @return the parameter map containing the time span and the breakpoints for the request loop
     */
    Map<String, Object> initCalendarConfig(boolean onlyNewest) {
        Calendar now = GregorianCalendar.getInstance()
        Date startDate, endNextRun, endTime
        now.add(Calendar.MONTH, -1)
        now.set(Calendar.DATE, now.getActualMaximum(Calendar.DATE))
        if(onlyNewest) {
            LocalDate startOfCurrentYear = LocalDate.now()
            startDate = Date.from(startOfCurrentYear.with(firstDayOfYear()).atStartOfDay(ZoneId.systemDefault()).toInstant()) //unfortunately, it does not work easier
            endTime = now.getTime()
            endNextRun = endTime
        }
        else {
            Calendar currentYear = GregorianCalendar.getInstance()
            //initially set to January 1st, '18; set flexibly to start of current year
            currentYear.set(Calendar.DAY_OF_YEAR, 1)
            startDate = currentYear.getTime()
            endNextRun = now.getTime()
            endTime = now.getTime()
        }
        [startDate: startDate, endTime: endTime, endNextRun: endNextRun, now: now]
    }

    /**
     * Nationaler Statistikserver
     * @param s generates an MD5 hash of the given string
     * @return a MD5 integer sum
     */
    String generateMD5(String s) {
        MessageDigest digest = MessageDigest.getInstance("MD5")
        digest.update(s.bytes)
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

    /**
     * Nationaler Statistikserver
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

        BasicHttpClient http
        try {
            List reportList = []
            http = new BasicHttpClient(ConfigMapper.getStatsApiUrl() + "Sushiservice/GetReport?apikey=${queryParams.apiKey}&requestor_id=${queryParams.requestor.toString()}&customer_id=${queryParams.customer}&platform=${queryParams.platform}")
            Closure success = { resp, reader ->
                if (resp.Report_ID && resp.Release) {
                    reportList.add(resp.Report_ID + 'R' + resp.Release)
                }
            }
            Closure failure = { resp, reader ->
                errors.add("SUSHI Error for ${queryParams.customer}|${queryParams.requestor}|${queryParams.platform}: ${resp.code()}-${reader}\n")
            }
            http.get(BasicHttpClient.ResponseType.JSON, success, failure)
            availableReportCache[queryParamsHash] = reportList
        } catch (Exception e) {
            String message = "Error getting available Reports from NatStat API"
            log.error(message)
            errors.add(message)
            log.error(e.message)
        }
        finally {
            if (http) { http.close() }
        }
    }

    /**
     * Nationaler Statistikserver
     * Resolves the objects in the given list item
     * @param listItem the list with the object keys
     * @return the list with the resolved objects
     */
    ArrayList getObjectsForItem(listItem) {
       [
            TitleInstancePackagePlatform.get(listItem[0]),
            Platform.get(listItem[1]),
            Org.get(listItem[2]),
            Identifier.get(listItem[3])
        ]
    }

    /**
     * Nationaler Statistikserver
     * Gets the available report list for the given platform
     * @param queryParams the basic query parameter map
     * @return a list of relevant report types
     */
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

    /**
     * Nationaler Statistikserver
     * Initialises the loading options for the given list item
     * @param listItem the list item to process
     * @param mostRecentClosedPeriod the most recent period for the sync run
     * @return the sync options for the given list
     */
    StatsSyncServiceOptions initializeStatsSyncServiceOptions(listItem, mostRecentClosedPeriod) {
        StatsSyncServiceOptions options = new StatsSyncServiceOptions()
        List itemObjects = getObjectsForItem(listItem)
        options.setItemObjects(itemObjects)
        options.setBasicQueryParams()
        options.mostRecentClosedPeriod = mostRecentClosedPeriod
        return options
    }

    /**
     * Nationaler Statistikserver
     * Gets the cursor for the given configuration row
     * @param options the row containing the report institution, the platform, the identifier and the title
     * @return the {@link StatsTripleCursor} for the given row
     */
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
                availFrom: DateUtils.getSDF_yyyyMMdd().parse(SYNC_STATS_FROM),
                availTo: null,
                factType: options.factType
            )
            csr.numFacts = 0
        }
        return csr
    }

    /**
     * Nationaler Statistikserver
     * Processes the given list item
     * @param listItem the list item to fetch data for
     * @param mostRecentClosedPeriod the most recent period for the sync run
     */
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
                Date mostRecentClosedDate = DateUtils.getSDF_yyyyMMdd().parse(options.mostRecentClosedPeriod)
                if (options.identifierTypeAllowedForAPICall() &&
                    ((csr.availTo == null) || (csr.availTo < mostRecentClosedDate))) {
                    options.from = getNextFromPeriod(csr)
                    sushiClient.clientOptions = options
                    try {
                        sushiClient.query()
                        def xml = sushiClient.getResult()
                        def authenticationError = _getSushiErrorMessage(xml)
                        if (authenticationError) {
                            jsonErrors.add(authenticationError)
                            csr.jerror = JsonOutput.toJson(jsonErrors)
                        }
                        if (_responseHasUsageData(xml, options.statsTitleIdentifier)) {
                            writeUsageRecords(xml, options, csr)
                        }
                    } catch (Exception e) {
                        log.error("Error fetching data")
                        log.error(e.message)
                        jsonErrors.add(e.message)
                        String jsonError = JsonOutput.toJson(jsonErrors)
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

    /**
     * Nationaler Statistikserver
     * Reads the usage records from the given XML body and updates the row and the cursor
     * @param xml the XML body
     * @param options the query options used
     * @param csr the stats triple cursor to update
     */
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
            csr.availTo = DateUtils.getSDF_yyyyMMdd().parse(options.mostRecentClosedPeriod)
            // We get a new month with no usage for a single title
            if (! _isNoUsageAvailableException(xml)) {
                List notProcessedMonths = getNotProcessedMonths(xml)
                if (! notProcessedMonths.empty) {
                    List followingRanges = _actualRangePlusFollowingNoUsageRanges(options, notProcessedMonths, DateUtils.getSDF_yyyyMM().format( csr.availFrom ))
                    followingRanges.each {
                        if (it == followingRanges.first()){
                            csr.availTo = DateUtils.getSDF_yyyyMMdd().parse(_getDateForLastDayOfMonth(it['end']))
                            csr.save()
                        } else {
                            _writeNewCsr(0, it['begin'],it['end'],options)
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
                csr = _writeNewCsr(factCount, it['begin'],it['end'],options)
            } else {
                Map usageMap = _getPeriodUsageMap(itemPerformancesForRange)
                usageMap.each { key, countPerMetric ->
                    Map fact = [:]
                    countPerMetric.each { metric, count ->
                        fact.from = DateUtils.getSDF_yyyyMMdd().parse(key)
                        fact.to = DateUtils.getSDF_yyyyMMdd().parse(_getDateForLastDayOfMonth(key))
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
                    csr.availFrom = DateUtils.getSDF_yyyyMM().parse(it['begin'])
                    csr.availTo = DateUtils.getSDF_yyyyMMdd().parse(_getDateForLastDayOfMonth(it.end))
                    csr.numFacts = factCount
                    csr.save()

                } else {
                    String newFromPeriod = getNextFromPeriod(csr).substring(0,7)
                    if (newFromPeriod != it.begin) { // gap for new range, create new csr
                        log.warn("usage data gap found before ${it.begin}")
                        csr = _writeNewCsr(factCount, it['begin'], it['end'], options)
                    } else {
                        // There is no gap, just update csr with new availTo value
                        csr.availTo = DateUtils.getSDF_yyyyMMdd().parse(_getDateForLastDayOfMonth(it.end))
                        csr.numFacts = csr.numFacts + factCount
                        csr.save()
                    }

                }
            }
        }
    }

    /**
     * Nationaler Statistikserver
     * Writes a new stats triple cursor with the given arguments
     * @param factCount the count of facts to be recorded
     * @param begin the report month start
     * @param end the report month end
     * @param options the options containing the title, platform, identifier and report type to record
     * @return the new stats triple cursor
     */
    private StatsTripleCursor _writeNewCsr(factCount, begin, end, options){
        StatsTripleCursor csr = new StatsTripleCursor()
        csr.availFrom = DateUtils.getSDF_yyyyMM().parse(begin)
        csr.availTo = DateUtils.getSDF_yyyyMMdd().parse(_getDateForLastDayOfMonth(end))
        csr.customerId = options.customer
        csr.numFacts = factCount
        csr.titleId = options.statsTitleIdentifier
        csr.supplierId = options.platform
        csr.factType = options.factType
        csr.identifierType = options.identifier.ns
        csr.save()
        return csr
    }

    /**
     * Nationaler Statistikserver
     * Reads the item performances for the given range
     * @param itemPerformances the list of performances to check
     * @param range the start and end date to query
     * @return all performances in the given range
     */
    List getItemPerformancesForRange(itemPerformances, range) {
        itemPerformances.findAll {
            it.Period.Begin.text().substring(0,7) >= range["begin"] &&
                it.Period.End.text().substring(0,7) <= range["end"]
        }
    }

    /**
     * Nationaler Statistikserver
     * Determines the range before the first item performance
     * @param itemPerformances the available performances
     * @param options unused
     * @param notProcessedMonths missing months
     * @return the range map containing the month before the first available usage period
     */
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

    /**
     * Nationaler Statistikserver
     * Lists the usage ranges in the given list of performances
     * @param itemPerformances the available performances
     * @param options the request parameters
     * @param notProcessedMonths missing months
     * @return a list of usage range starts and ends
     */
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
        List followingRanges = _actualRangePlusFollowingNoUsageRanges(options, notProcessedMonths, rangeMap['begin'])
        return ranges + followingRanges
    }

    /**
     * Nationaler Statistikserver
     * Fills the range map with gaps
     * @param options the request options
     * @param notProcessedMonths the missing months
     * @param begin the start of the range map
     * @return the filled list of month ranges
     */
    private List<Map> _actualRangePlusFollowingNoUsageRanges(StatsSyncServiceOptions options, List<String> notProcessedMonths, String begin)
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
            rangeMap['end'] = _minusMonths(notProcessedMonths.first(),1)
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
                rangeMap['begin'] = _plusMonths(it[0],1)
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
                        rangeMap['end'] = _minusMonths(it[1], 1)
                        ranges.add(rangeMap)
                        rangeMap = [:]
                    }
                }
            }
        }
        return ranges
    }

    /**
     * Nationaler Statistikserver
     * Adds the given count of months to the given base
     * @param baseMonth the starting month
     * @param count the count of months to add
     * @return the calculated time point
     */
    private String _plusMonths(CharSequence baseMonth, Long count) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
        YearMonth localDate = YearMonth.parse(baseMonth, formatter)
        return localDate.plusMonths(count).toString()
    }

    /**
     * Nationaler Statistikserver
     * Subtracts the given count of months to the given base
     * @param baseMonth the starting month
     * @param count the count of months to subtract
     * @return the calculated time point
     */
    private String _minusMonths(CharSequence baseMonth, Long count) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern('yyyy-MM')
        YearMonth localDate = YearMonth.parse(baseMonth, formatter)
        return localDate.minusMonths(count).toString()
    }

    /**
     * Nationaler Statistikserver
     * Not processed months when getting a 3031 Exception
     * @param xml the XML body
     * @return a list of months not being processed
     */
    List getNotProcessedMonths(xml) {
        if (xml.Exception.isEmpty() == false && xml.Exception.Number == '3031') {
            def exceptionData = xml.Exception.Data
            def matcher = exceptionData =~ /\d{4}-\d{2}/
            List list = []
            matcher.each {
                list.add(it)
            }
            return list
        }
        return []
    }

    /**
     * Nationaler Statistikserver
     * Gets the count of the titles in the given XML body
     * @param xml the XML repsonse body with the usage data
     */
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

    /**
     * Nationaler Statistikserver
     * Gets the next period from the given cursor
     * @param csr the cursor from which the next month should be calculated
     * @return the next period start
     */
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

    /**
     * Nationaler Statistikserver
     * Calculates the month start two months backwards from now
     * @return the start of the month two months behind now
     */
    private String _getMostRecentClosedPeriod() {
        Calendar cal = Calendar.getInstance()
        cal.setTime(new Date())
        cal.add(Calendar.MONTH, -2)
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return yyyyMMdd.format(cal.getTime())
    }

    /**
     * Nationaler Statistikserver
     * Checks if the given metric key is among the supported ones
     * @param metric the metric to check
     * @return true if the metric is among the supported ones, false otherwise
     */
    private Boolean _isAllowedMetric(metric) {
        if (metric in ['ft_total', 'search_reg', 'search_fed', 'record_view', 'result_click']) {
            return true
        }
        return false
    }

    /**
     * Nationaler Statistikserver
     * Gets the period usage map of the given list of item performances
     * @param itemPerformances the performances to output as map
     * @return a map of structure period=>[metric1=>value,metric2=>value...]
     */
    private Map<String,Map> _getPeriodUsageMap(ArrayList itemPerformances) {
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
                if (_isAllowedMetric(metric)) {
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

    /**
     * Nationaler Statistikserver
     * Gets the last day of the given month
     * @param yearMonthString the month to get the last day of
     * @return the full string of the last month of the day
     */
    private String _getDateForLastDayOfMonth(yearMonthString) {
        SimpleDateFormat sdf = DateUtils.getSDF_yyyyMM()
        GregorianCalendar cal = new GregorianCalendar()
        cal.setTime(sdf.parse(yearMonthString))
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return "${cal.get(Calendar.YEAR)}-${String.format('%02d',cal.get(Calendar.MONTH)+1)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }

    /**
     * Nationaler Statistikserver
     * Gets the SUSHI error message from the given XML response body
     * @param xml
     * @return
     */
    private _getSushiErrorMessage(xml) {
        if (xml.Exception.isEmpty() == false) {
            def errorNumber = xml.Exception.Number
            def sushiErrorList = ['2000', '2020', '3000', '3062']
            if (errorNumber in sushiErrorList) {
                return xml.Exception.Message.toString()
            }
        }
        return false
    }

    /**
     * Nationaler Statistikserver
     * Checks if the given XML response contains the SUSHI 3030 error
     * @param xml the XML response body
     * @return true if the Exception element is filled with the code 3030, false otherwise
     */
    private Boolean _isNoUsageAvailableException(xml)
    {
        return (xml.Exception.isEmpty() == false && xml.Exception.Number == '3030')
    }

    /**
     * Nationaler Statistikserver
     * Checks if the given XML response is empty
     * @param xml the XML response body
     * @return true if the Exception element is empty, false otherwise
     */
    private Boolean _isEmptyReport(xml)
    {
        return (xml.Report.Report.isEmpty() == true || xml.Report.isEmpty() == true)
    }

    /**
     * Nationaler Statistikserver
     * Checks if the given XML response contains a different SUSHI error than code 3031
     * @param xml the XML response body
     * @return true if the Exception element is filled with the code other than 3031, false otherwise
     */
    private Boolean _isOtherExceptionWithoutUsageData(xml)
    {
        return (xml.Exception.isEmpty() == false && xml.Exception.Number != '3031')
    }

    /**
     * Nationaler Statistikserver
     * Checks if the given XML response contains no customer
     * @param xml the XML response body
     * @return true if there is no customer, false otherwise
     */
    private Boolean _isEmptyReportWithoutCustomer(xml)
    {
        return (xml.Report.Report.Customer.isEmpty() == true)
    }

    /**
     * Checks if the given XML body contains usage data for the given title
     * @param xml the XML response
     * @param titleId the title to check
     * @return true if there is a usage for the given title, false otherwise
     */
    private Boolean _responseHasUsageData(xml, titleId) {
        // 3030 Exception-> Zero usage
        if (_isNoUsageAvailableException(xml)){
            return true
        }
        // SUSHI Exceptions which prevent from further processing and storing records in usage tables
        if (_isOtherExceptionWithoutUsageData(xml)) {
            log.debug('SUSHI Exception Number ' + xml.Exception.Number + ' : ' + xml.Exception.Message)
            return false
        } else if (_isEmptyReport(xml)) {
            // 3031 Exception but no usage data, e.g. all fetched Months are not available, or we call the last month which is
            // not yet available
            log.debug('XML response has 3031 Exception with no usage data')
            return false
        } else if (_isEmptyReportWithoutCustomer(xml)) {
            // there are processed months but no usage data. This can happen with queries for single titles. If we had no usage
            // for all titles we would get a 3030 Exception for all titles.
            log.debug('No result found for title with ID ' + titleId)
            return true
        } else {
            return true
        }
    }

    /**
     * Increments the activity histogram by a completed process
     */
    static synchronized void incrementActivityHistogram() {
        SimpleDateFormat sdf = new SimpleDateFormat('yyyy/MM/dd HH:mm')
        String col_identifier = sdf.format(new Date())

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
