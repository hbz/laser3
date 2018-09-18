package de.laser

import com.k_int.kbplus.*
import com.k_int.properties.*
import de.laser.domain.StatsTripleCursor
import groovyx.net.http.*
import java.text.SimpleDateFormat
import static groovyx.net.http.ContentType.*
import groovyx.gpars.GParsPool

class StatsSyncService {

    static final THREAD_POOL_SIZE = 4
    static final COUNTER_REPORT_VERSION = 4
    static final SYNC_STATS_FROM = '2012-01'

    def grailsApplication
    def executorService
    def sessionFactory
    def factService
    def propertyInstanceMap = org.codehaus.groovy.grails.plugins.DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP
    def queryParams = [:]


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

            def statsApi = grailsApplication.config.statsApiUrl
            if ((statsApi == null) || (statsApi == '')) {
                log.error("Stats API URL not set in config")
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

    def processListItem(listItem, mostRecentClosedPeriod) {

        def stats_api_endpoint = new RESTClient(grailsApplication.config.statsApiUrl)
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

            def reports = RefdataValue.findAllByValueLikeAndOwner('STATS%', RefdataCategory.findByDesc('FactType'))

            def csr = StatsTripleCursor.findByTitleIdAndSupplierIdAndCustomerId(statsTitleIdentifier, platform, customer)
            if (csr == null) {
                csr = new StatsTripleCursor(titleId: statsTitleIdentifier, supplierId: platform, customerId: customer, haveUpTo: null)
            }
            if ((csr.haveUpTo == null) || (csr.haveUpTo < mostRecentClosedPeriod)) {

                reports.each { statsReport ->
                    def reportValues = statsReport.toString().split(':')
                    def report = reportValues[1]
                    def reportType = getReportType(report)
                    def titleId = title_io_inst.identifier.value

                    def fromPeriod = csr.haveUpTo ?: SYNC_STATS_FROM
                    try {
                        def beginDate = "${fromPeriod}-01"
                        def endDate = getLastDayOfMonth(mostRecentClosedPeriod)
                        log.debug("Calling STATS API:  ${report}, Title with ID ${titleId}")
                        log.debug("Period Begin: ${beginDate}, Period End: ${endDate}")
                        stats_api_endpoint.get(
                                path: 'Sushiservice/GetReport',
                                contentType: ANY, // We get no XmlSlurper Objects for value XML
                                query: [
                                        APIKey        : apiKey,
                                        RequestorID   : requestor,
                                        CustomerID    : customer,
                                        Report        : report,
                                        Release       : COUNTER_REPORT_VERSION,
                                        BeginDate     : beginDate,
                                        EndDate       : endDate,
                                        Platform      : platform,
                                        ItemIdentifier: "${reportType}:zdbid:" + titleId
                                ]) { response, xml ->
                            if (xml) {

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
                                            fact.to =timeStampFormat.parse(getLastDayOfMonth(key))
                                            cal.setTime(fact.to)
                                            fact.reportingYear=cal.get(Calendar.YEAR)
                                            fact.reportingMonth=cal.get(Calendar.MONTH)+1
                                            fact.type = statsReport.toString()
                                            fact.value = count
                                            fact.uid = "${titleId}:${platform}:${customer}:${key}:${metric}:${report}"
                                            fact.title = title_inst
                                            fact.supplier = supplier_inst
                                            fact.inst = org_inst
                                            fact.juspio = title_io_inst
                                            if (factService.registerFact(fact)) {
                                                ++newFactCount
                                            }
                                        }
                                    }
                                    log.debug("Title: ${title_inst.title}")
                                    log.debug("ID: ${titleId}")

                                }
                            } else {
                                log.error("No xml object returned, response status: ${response.statusLine}")
                            }
                        }

                    } catch (Exception e) {
                        log.error("Error fetching data")
                        log.error(e.message)
                    }
                }
                csr.haveUpTo = mostRecentClosedPeriod
                try {
                csr.save(flush: true)
                } catch (Exception e) {
                    log.error(e.message)
                    exceptionCount++
                }
            }

            csr.save(flush:true)
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

    private String getLastDayOfMonth(yearMonthString) {
        def sdf = new SimpleDateFormat("yyyy-MM")
        def cal = new GregorianCalendar()
        cal.setTime(sdf.parse(yearMonthString))
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        return "${cal.get(Calendar.YEAR)}-${String.format('%02d',cal.get(Calendar.MONTH)+1)}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }


    private Boolean responseHasUsageData(xml, titleId) {
        // TODO maybe better check for usage first
        // What if we get a 3030 Exception? We return false here and do not store facts for the queried period.
        // Do we need to handle the 3031 Exceptions and kind of flag periods which are mentioned in the XML data element?
        // Or store 0 values for 3030 Exceptions, which allows us to mark missing/errorneous months, but would increase
        // the number of facts significantly
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

