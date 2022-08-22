package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.auth.Role
import de.laser.auth.UserRole
import de.laser.base.AbstractJob
import de.laser.config.ConfigDefaults
import de.laser.finance.CostItem
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.remote.ApiSource
import de.laser.remote.ElasticsearchSource
import de.laser.remote.FTControl
import de.laser.remote.GlobalRecordSource
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.stats.LaserStatsCursor
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyResult
import de.laser.system.SystemActivityProfiler
import de.laser.system.SystemProfiler
import de.laser.system.SystemSetting
import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.web.Action
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.core.CountRequest
import org.elasticsearch.client.core.CountResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.hibernate.SessionFactory
import org.quartz.JobKey
import org.quartz.impl.matchers.GroupMatcher

import javax.servlet.ServletOutputStream
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.sql.Timestamp
import java.time.LocalDate

/**
 * This controller is a buddy-for-everything controller and handles thus all sorts of global management
 * calls. Many methods are dangerous because they affect many parts of the system (global cleanup methods
 * for example which may affect many title holdings) or are under subject of testing before they get
 * generally released.
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class YodaController {

    SessionFactory sessionFactory

    CacheService cacheService
    ContextService contextService
    DashboardDueDatesService dashboardDueDatesService
    DataloadService dataloadService
    DeletionService deletionService
    ESWrapperService ESWrapperService
    FinanceService financeService
    FormService formService
    GokbService gokbService
    GlobalSourceSyncService globalSourceSyncService
    def quartzScheduler
    StatsSyncService statsSyncService
    StatusUpdateService statusUpdateService
    SubscriptionService subscriptionService
    SurveyUpdateService surveyUpdateService
    YodaService yodaService

    /**
     * Shows the Yoda-dashboard
     */
    @Secured(['ROLE_YODA'])
    @Transactional
    def index() {
        Map<String, Object> result = [
                docStore: AppUtils.getDocumentStorageInfo()
        ]
        result
    }

    /**
     * Shows the configuration settings of the application; this is useful for remote deploys
     * because configuration may be checked during runtime
     * @return the list of configurations done on grails.util.Holders.config and the local config files
     */
    @Secured(['ROLE_YODA'])
    def systemConfiguration() {
        Map result = [:]

        result.blacklist = [
                'jira', 'dataSource', ConfigDefaults.DATASOURCE_DEFAULT + '.password', ConfigDefaults.DATASOURCE_STORAGE + '.password'
        ]
        result.editable = true
        result.currentConfig = grails.util.Holders.config.findAll { ! it.key.matches("[A-Z|_]*") }
        result
    }

    /**
     * Shows the a list of the current cronjob configurations
     * @return a list of the currently defined cronjobs, including their:
     * <ul>
     *     <li>execution schedule</li>
     *     <li>next scheduled execution</li>
     *     <li>service configuration setting availability (is the job present in the config?)</li>
     *     <li>service activity state (is the service active?)</li>
     * </ul>
     */
    @Secured(['ROLE_YODA'])
    def systemQuartz() {
        Map result = [:]

        // DEBUG ONLY: changeNotificationService.aggregateAndNotifyChanges()

        result.currentConfig   = Holders.grailsApplication.config
        result.quartzScheduler = quartzScheduler

        Map<String, Object> groups = [:]
        for (String groupName : quartzScheduler.getJobGroupNames()) {
            List group = []

            for (JobKey key : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                def clazz = Class.forName(key.getName())
                List<List> cp  = clazz.getAt('configurationProperties')

                def triggers = quartzScheduler.getTriggersOfJob(key)
                List nft = triggers.collect{ it.nextFireTime ?: null }

                Closure getUsedServices = { clz ->
                    clz.getDeclaredFields().findAll{ it.getName().endsWith('Service')}.collect{ it.getName().capitalize() }
                }
                def services = getUsedServices(clazz)

                Map map = [
                        name: clazz.simpleName,
                        configurationProperties: cp,
                        services: services,
                        nextFireTime: nft ? nft.get(0)?.toTimestamp() : '',
                        running: (applicationContext.getBean(key.getName()) as AbstractJob).isRunning(),
                        available: (applicationContext.getBean(key.getName()) as AbstractJob).isAvailable()
                ]

                List crx = triggers.collect{ it.hasProperty('cronEx') ? it.cronEx : null }

                if (crx) {
                    map << ['cronEx': crx.get(0).cronExpression]
                }
                group << map
            }

            groups.putAt( groupName, group.sort{ a, b -> (a.name < b.name ? -1 : 1)} )
        }
        result.quartz = groups
        result
    }

    /**
     * Dumps the current cache state, i.e. which caches are currently set and what is stored in them
     * @return a list of the current cache entries, grouped by user / global / session caches
     */
    @Secured(['ROLE_YODA'])
    def systemCache() {
        Map result = [:]

        result.grailsApp = grailsApplication
        result.appContext = getApplicationContext()

        result.hibernateSession = sessionFactory

        result.ehcacheManager = cacheService.getEhcacheManager()

        if (params.key) {
            JSON entry = contextService.getSessionCache().get(params.key) as JSON

            entry.prettyPrint = true
            response.setContentType("application/json")
            response.outputStream << entry

            return
        }
        else if (params.cmd?.equals('clearCache')) {
            def cache
            if (params.type?.equals('session')) {
                cache = contextService.getSessionCache()
                cache.clear()
            }
            else if (params.type?.equals('ehcache')) {
                cache = cacheService.getCache(result.ehcacheManager, params.cache)
                cacheService.clear(cache)
            }

            params.remove('cmd')
            params.remove('type')
            params.remove('cache')

            redirect controller: 'yoda', action: 'systemCache', params: params
            return
        }

        result
    }

    /**
     * Dumps the registered counts of users over time
     * @return a list of graphs showing when how many users were recorded
     */
    @Secured(['ROLE_YODA'])
    def profilerActivity() {
        Map result = [:]

        Map<String, Object> activity = [:]

        // gathering data

        List<Timestamp> dayDates = SystemActivityProfiler.executeQuery(
                "select date_trunc('day', dateCreated) as day from SystemActivityProfiler group by date_trunc('day', dateCreated), dateCreated order by dateCreated desc"
        )
        dayDates.unique().take(30).each { it ->
//            List<Timestamp, Timestamp, Timestamp, Integer, Integer, Double> slots = SystemActivityProfiler.executeQuery(
            List slots = SystemActivityProfiler.executeQuery(
                    "select date_trunc('hour', dateCreated), min(dateCreated), max(dateCreated), min(userCount), max(userCount), avg(userCount) " +
                            "  from SystemActivityProfiler where date_trunc('day', dateCreated) = :day " +
                            " group by date_trunc('hour', dateCreated) order by min(dateCreated), max(dateCreated)",
                    [day: it])

            String dayKey = (DateUtils.getLocalizedSDF_noTime()).format(new Date(it.getTime()))
            activity.put(dayKey, [])

            slots.each { hour ->
                activity[dayKey].add([
                        (DateUtils.getSDF_onlyTime()).format(new Date(hour[0].getTime())),   // time.start
                        (DateUtils.getSDF_onlyTime()).format(new Date(hour[1].getTime())),   // time.min
                        (DateUtils.getSDF_onlyTime()).format(new Date(hour[2].getTime())),   // time.max
                        hour[3],    // user.min
                        hour[4],    // user.max
                        hour[5]     // user.avg
                ])
            }
        }

        // precalc

        Map<String, Object> activityMatrix = [:]
        activityMatrix.put('Ø', null)

        List averages = (0..23).collect{ 0 }
        List labels   = (0..23).collect{ "${it < 10 ? '0' + it : it}:00:00" }

        activity.each{ dayKey, values ->
            List series1 = (0..23).collect{ 0 }
            List series2 = (0..23).collect{ 0 }

            values.each { val ->
                int indexOf = labels.findIndexOf{it == val[0]}
                if (indexOf >= 0) {
                    series1.putAt(indexOf, val[3])          // [0] = min
                    //series2.putAt(indexOf, val[4])          // [1] = max
                    series2.putAt(indexOf, val[4]- val[3])  // stackBars: true - max-min -> [1] = diff
                    averages[indexOf] = averages[indexOf] + val[5]
                }
            }
            activityMatrix.put(dayKey, [series1, series2])
        }

        // averages

        for(int i=0; i<averages.size(); i++) {
            averages[i] = (averages[i]/activity.size())
        }

        activityMatrix.putAt('Ø', [averages, averages])

        result.labels = labels
        result.activity = activityMatrix

        result
    }

    /**
     * Call to list the currently available threads
     * @return the view calling the currently available threads, their running state (or daemon), CPU time, thread group, priority
     */
    @Secured(['ROLE_YODA'])
    def systemThreads() {
        return [:]
    }

    /**
     * Dumps the average loading times for the app's routes during certain time points
     * @return a table showing when which call needed how much time in average
     * @see SystemActivityProfiler
     */
    @Secured(['ROLE_YODA'])
    def profilerLoadtime() {
        Map<String, Object> result = [:]

        result.globalMatrix = [:]
        result.globalMatrixSteps = [0, 2000, 4000, 8000, 12000, 20000, 30000, 45000, 60000]

        result.archive = params.archive ?: SystemProfiler.getCurrentArchive()
        result.allArchives = SystemProfiler.executeQuery('select distinct(archive) from SystemProfiler').collect{ it }

        List<String> allUri = SystemProfiler.executeQuery('select distinct(uri) from SystemProfiler')

        allUri.each { uri ->
            result.globalMatrix["${uri}"] = [:]
            result.globalMatrixSteps.eachWithIndex { step, i ->
                String sql = 'select count(sp.uri) from SystemProfiler sp where sp.archive = :arc and sp.uri =:uri and sp.ms > :currStep'
                Map sqlParams = [uri: uri, currStep: step, arc: result.archive]

                if (i < result.globalMatrixSteps.size() - 1) {
                    sql += ' and sp.ms < :nextStep'
                    sqlParams = [uri: uri, currStep: step, nextStep: result.globalMatrixSteps[i+1], arc: result.archive]
                }
                result.globalMatrix["${uri}"]["${step}"] = SystemProfiler.executeQuery(sql, sqlParams).get(0)
            }
        }

        result.globalStats = SystemProfiler.executeQuery(
                "select sp.uri, max(sp.ms) as max, avg(sp.ms) as avg, count(sp.ms) as counter from SystemProfiler sp where sp.archive = :arc group by sp.uri order by counter desc",
                [arc: result.archive]
        )

        result.contextStats = SystemProfiler.executeQuery(
                "select sp.uri, max(sp.ms) as max, avg(sp.ms) as avg, ctx.id, count(ctx.id) as counter from SystemProfiler sp join sp.context as ctx where sp.archive = :arc and ctx is not null group by sp.uri, ctx.id order by counter desc",
                [arc: result.archive]
        )


        List<BigDecimal> hmw = [ -0.05, 0.1, 0.2, 0.4, 0.6, 0.8, 1.0, 1.5, 2.0 ]
        Map<String, List> heatMap = [:]

        result.globalStats.each{ gs ->
            String uri = gs[0]
            Map<String, Long> counts = result.globalMatrix[uri]
            BigDecimal heat = 0.0

            result.globalMatrixSteps.eachWithIndex { c, idx ->
                heat += (counts.get(c.toString()) * hmw[idx])
            }
            heatMap.putAt(uri, [ heat.doubleValue() * (Math.sqrt(gs[3]) / gs[3]), gs[1], gs[2], gs[3] ]) // max, avg, count
        }
        result.globalHeatMap = heatMap.findAll {it.value[0] > 0 }.sort {e, f -> f.value[0] <=> e.value[0] }.take(20)

        result
    }

    /**
     * Dumps the call counts on the app's different routes over time
     * @return a listing of graphs when which page has been called how many times
     */
    @Secured(['ROLE_YODA'])
    def profilerTimeline() {
        Map<String, Object> result = [:]

        List<String> allUri = SystemProfiler.executeQuery('select distinct(uri) from SystemProfiler')

        result.globalTimeline           = [:]
        result.globalTimelineStartDate  = DateUtils.localDateToSqlDate( LocalDate.now().minusDays(30) )
        result.globalTimelineDates      = (25..0).collect{ (DateUtils.getLocalizedSDF_noTime()).format( DateUtils.localDateToSqlDate( LocalDate.now().minusDays(it) ) )}

        Map<String, Integer> ordered = [:]

        allUri.each { uri ->
            result.globalTimeline[uri] = (25..0).collect { 0 }

            String sql = "select to_char(sp.dateCreated, 'dd.mm.yyyy'), count(*) from SystemProfiler sp where sp.uri = :uri and sp.dateCreated >= :dCheck group by to_char(sp.dateCreated, 'dd.mm.yyyy')"
            List hits = SystemProfiler.executeQuery(sql, [uri: uri, dCheck: result.globalTimelineStartDate])

            int count = 0
            hits.each { hit ->
                int indexOf = result.globalTimelineDates.findIndexOf { it == hit[0] }
                if (indexOf >= 0) {
                    result.globalTimeline[uri][indexOf] = hit[1]
                    count = count + hit[1]
                }
            }

            if (count > 0) {
                ordered[uri] = count
            }
        }
        result.globalTimelineOrder = ordered.sort{ e,f -> f.value <=> e.value }

        result
    }

    /**
     * Dumps the current method securing for each controller call
     * @return a list of calls with their security level, grouped by controller
     */
    @Secured(['ROLE_YODA'])
    def appControllers() {
        Map<String, Object> result = [:]
        Map<String, Object> cList = [:]

        // filter dynamic getter/setter
        List<String> blacklist = [
                'getAllowedMethods',
                'setAllowedMethods',
                'getStaticApplicationContext'
        ]

        CodeUtils.getAllControllerClasses().each { controller ->
            if (controller.name.startsWith('de.laser')) {
                Map<String, Object> mList = [public:[:], others:[:]]

                controller.declaredMethods.each { Method method ->
                    int mods = method.getModifiers()
//                  if (! Modifier.isSynthetic(mods) && (   // Access to 'SYNTHETIC' exceeds its access rights
                    if ( ((mods & 4096) == 0) && (          // isSynthetic(int mod) { (mod & 4096) != 0; }
                            (method.getAnnotation(Action) || Modifier.isPrivate(mods)) ||
                            (Modifier.isStatic(mods) && ! (method.name in blacklist))
                    ) ) {
                        String mKey = method.name
                        Map<String, Object> mInfo = [:]

                        Annotation da = method.getAnnotation(DebugInfo)
                        if (da) {
                            mInfo.debug = [
                                    perm     : da.perm(),
                                    type     : da.type(),
                                    affil    : da.affil(),
                                    specRole : da.specRole(),
                                    test     : da.test()
                            ]
                            if (da.ctrlService()) {
                                mInfo.ctrlService = da.ctrlService()
                                if (da.ctrlService() == DebugInfo.WITH_TRANSACTION) {
                                    mInfo.refactoring = 'done'
                                }
                            }
                            if (da.wtc()) {
                                mInfo.wtc = da.wtc()
                                if (da.wtc() == DebugInfo.WITH_TRANSACTION) {
                                    mInfo.refactoring = 'done'
                                }
                            }
                        }

                        if (method.getAnnotation(Check404)) {
                            mInfo.check404 = Check404.KEY
                        }

                        if (method.getAnnotation(Secured)) {
                            mInfo.secured = method.getAnnotation(Secured)?.value()
                        }
                        else {
                            mInfo.warning = 'not secured'
                        }

                        if (method.getAnnotation(Transactional)) {
                            mInfo.transactional = 'transactional'
                            mInfo.refactoring = 'done'
                        }
                        if (method.getAnnotation(Deprecated)) {
                            mInfo.deprecated = 'deprecated'
                        }

                        if (Modifier.isPrivate(mods) || Modifier.isStatic(mods)) {

                            // filter dynamic getter/setter
                            String check = method.name.replaceFirst('get', '').replaceFirst('set', '').replace('_', '')
                            if (check.toUpperCase() != check) {
                                mInfo.modifiers = [
                                        private: Modifier.isPrivate(mods),
                                        static: Modifier.isStatic(mods)
                                ]
                                mList.others.putAt(mKey, mInfo)
                            }
                        }
                        else {
                            mList.public.putAt(mKey, mInfo)
                        }
                    }
                }

                cList.putAt( controller.simpleName, [
                        'secured': controller.getAnnotation(Secured)?.value(),
                        'methods': [
                                public: mList.public.sort{it.key},
                                others: mList.others.sort{it.key}
                        ],
                        'deprecated': controller.getAnnotation(Deprecated) ? true : false
                ])
            }
        }
        result.controller = cList.sort{it.key}
        result
    }

    /**
     * Dumps all current global role assignments
     * @return a list of global roles and to whom they have been granted
     */
    @Secured(['ROLE_YODA'])
    def userRoleMatrix() {
        Map<String, Object> result = [:]

        result.matrix = [:]

        Role.findAll("from Role order by authority").each { role -> result.matrix[role.authority] = [] }

        UserRole.executeQuery(
                "select u, urro from User u join u.roles ur join ur.role urro group by urro, u"
        ).each { usRo ->
            result.matrix[usRo[1].authority].add(usRo[0])
        }
        result
    }

    /**
     * Is one of the dangerous methods: retriggers the change processing on title level and hands eventual differences
     * to the local holdings; if necessary, pending changes are being generated
     * @see PendingChange
     */
    @Secured(['ROLE_YODA'])
    def retriggerPendingChanges() {
        log.debug("match IssueEntitlements to TIPPs ...")
        flash.message = "Pakete werden nachgehalten ..."
        statusUpdateService.retriggerPendingChanges(params.packageUUID)
        redirect controller: 'home', action: 'index'
    }

    /**
     * Another one of the dangerous calls: creates missing titles for packages where auto-accept has been
     * configured for new titles. Concerned are only those packages where the setting for new titles is set to "accept"
     */
    @Secured(['ROLE_YODA'])
    def matchPackageHoldings() {
        log.debug("match package holdings to issue entitlement holdings ...")
        flash.message = "Bestände werden korrigiert ..."
        yodaService.matchPackageHoldings()
        redirect controller: 'home', action: 'index'
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def getTIPPsWithoutGOKBId() {
        log.debug("delete TIPPs without GOKb-ID")
        yodaService.getTIPPsWithoutGOKBId()
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def purgeTIPPsWithoutGOKBId() {
        def toDelete = JSON.parse(params.toDelete)
        def toUUIDfy = JSON.parse(params.toUUIDfy)
        if(params.doIt == "true") {
            yodaService.purgeTIPPsWihtoutGOKBId(toDelete,toUUIDfy)
            redirect(url: request.getHeader('referer'))
            return
        }
        else {
            flash.message = "Betroffene TIPP-IDs wären vereinigt worden: ${toDelete} und folgende hätten einen fehlenden Wert erhalten: ${toUUIDfy}"
            redirect action: 'getTIPPsWithoutGOKBId'
            return
        }
    }

    /**
     * Call to delete titles without we:kb reference and marked as removed
     */
    @Secured(['ROLE_YODA'])
    Map<String, Object> expungeRemovedTIPPs() {
        yodaService.expungeRemovedTIPPs(Boolean.valueOf(params.doIt))
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    @Transactional
    def remapOriginEditUrl() {
        List<Identifier> originEditUrls = Identifier.executeQuery("select ident from Identifier ident where lower(ident.ns.ns) = 'originediturl'")
        originEditUrls.each { originEditUrl ->
            def obj
            if(originEditUrl.tipp) {
                obj = originEditUrl.tipp
            }
            else if(originEditUrl.ti) {
                obj = originEditUrl.ti
            }
            else if(originEditUrl.pkg) {
                obj = originEditUrl.pkg
            }
            else if(originEditUrl.org) {
                obj = originEditUrl.org
            }
            if(!obj.originEditUrl) {
                obj.originEditUrl = new URL(originEditUrl.identifier.value)
                obj.save()
            }
        }
        redirect controller: 'home'
    }

    /**
     * Shows platforms which have a cursor recorded, i.e. usage data has been loaded already
     * @return a list of platforms with cursors
     */
    @Secured(['ROLE_YODA'])
    Map<String, Object> manageStatsSources() {
        Set<Platform> platforms = Platform.executeQuery('select p from LaserStatsCursor lsc join lsc.platform p join p.org o where p.org is not null order by o.name, o.sortname, p.name') as Set<Platform>
        Map<String, Object> result = [
                platforms: platforms,
                platformInstanceRecords: [:],
                flagContentGokb : true // gokbService.queryElasticsearch
        ]
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        platforms.each { Platform platformInstance ->
            Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${platformInstance.gokbId}")
            if (queryResult.error && queryResult.error == 404) {
                result.wekbServerUnavailable = message(code: 'wekb.error.404')
            }
            else if (queryResult.warning) {
                List records = queryResult.warning.records
                result.platformInstanceRecords[platformInstance.gokbId] = records ? records[0] : [:]
            }
        }
        result
    }

    /**
     * This is an already confirmed call; resets the usage data to a given platform. If specified,
     * existing usage data will be deleted as well
     */
    @Secured(['ROLE_YODA'])
    def resetStatsData() {
        boolean fullReset = Boolean.valueOf(params.fullReset)
        Platform platform = Platform.get(params.platform)
        LaserStatsCursor.executeUpdate('delete from LaserStatsCursor lsc where lsc.platform = :plat', [plat: platform])
        if(fullReset) {
            if(params.counterRevision == 'r4') {
                Counter4Report.withTransaction {
                    Counter4Report.executeUpdate('delete from Counter4Report c4r where c4r.platformUID = :plat', [plat: platform.globalUID])
                }
            }
            else if(params.counterRevision == 'r5') {
                Counter5Report.withTransaction {
                    Counter5Report.executeUpdate('delete from Counter5Report c5r where c5r.platformUID = :plat', [plat: platform.globalUID])
                }
            }
            statsSyncService.doFetch(false, platform.gokbId, params.sushiURL, params.counterRevision)
        }
        redirect(action: 'manageStatsSources')
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def editStatsSource() {
        statsSyncService.updateStatsSource(params)
        redirect(action: 'manageStatsSources')
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def deleteStatsSource() {
        statsSyncService.deleteStatsSource(params)
        redirect(action: 'manageStatsSources')
    }

    /**
     * Triggers the loading of usage data from the Nationaler Statistikserver
     */
    @Secured(['ROLE_YODA'])
    def statsSync() {
        log.debug("statsSync()")
        statsSyncService.doSync()
        redirect(controller:'home')
    }

    /**
     * Triggers the loading of usage data from the provider's SUSHI sources
     * directly, using the SUSHI sources entered in the respective we:kb platform page
     */
    @Secured(['ROLE_YODA'])
    def fetchStats() {
        if(formService.validateToken(params) && !StatsSyncService.running) {
            log.debug("fetchStats()")
            statsSyncService.doFetch(params.incremental == "true")
        }
        else if(StatsSyncService.running)
            log.info("sync is already running, not starting again ...")
        else log.info("form token expired, doing nothing ...")
        redirect(controller: 'yoda', action: 'systemThreads')
    }

    /**
     * Triggers the app's ElasticSearch index update
     */
    @Secured(['ROLE_YODA'])
    def esIndexUpdate() {
        log.debug("manual start full text index")
        dataloadService.updateFTIndices()
        log.debug("redirect ..")

        redirect controller: 'yoda', action: 'index'
    }

    /**
     * Clears the ElasticSearch index and reloads all data
     */
    @Secured(['ROLE_YODA'])
    def fullReset() {
       log.debug("Clear ES")
       dataloadService.resetESIndices()
        log.debug("redirect ..")

        redirect controller: 'yoda', action: 'index'
    }

    /**
     * Stops the current ElasticSearch index update process
     */
    @Secured(['ROLE_YODA'])
    def killDataloadService() {
        log.debug("kill DataloadService")
        dataloadService.killDataloadService()
        log.debug("redirect ..")

        redirect controller: 'yoda', action: 'index'
    }

    /**
     * Compares the counts of database entries against ElasticSearch index entries
     * @return a table of each instance count for each domain class
     */
    @Secured(['ROLE_YODA'])
    def checkESElementswithDBElements() {
        log.debug("checkESElementswithDBElements")
        dataloadService.checkESElementswithDBElements()
        log.debug("redirect ..")

        redirect controller: 'yoda', action: 'index'
    }

    /**
     * Very dangerous method. This calls the global records update, i.e. triggers data update
     * from all registered we:kb sources and updates the app's mirrored data
     * @see de.laser.remote.GlobalRecordSource
     */
    @Secured(['ROLE_YODA'])
    def globalSync() {
        log.debug("start global sync ...")
        globalSourceSyncService.startSync()
        //log.debug("done global sync ...")

        redirect controller: 'package'
    }

    /**
     * Triggers the bulk update of title data. This method has to be used when a new we:kb field has been
     * implemented in the app or because of a bug, data has not been transmitted. Beware that issue entitlements
     * (unless in case medium) are NOT affected by this reload and only global title fields should be fed by
     * this method!
     */
    @Secured(['ROLE_YODA'])
    def updateData() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            if(params.dataToLoad == "iemedium")
                yodaService.fillIEMedium()
            else
                globalSourceSyncService.updateData(params.dataToLoad)
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect controller: 'package'
    }

    /**
     * Call to reload all title instance data from the specified we:kb instance.
     * Beware that no local holdigs will be triggered, only the global level is going to be updated!
     * @see de.laser.remote.GlobalRecordSource
     */
    @Secured(['ROLE_YODA'])
    def reloadPackages() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            globalSourceSyncService.reloadData('TitleInstancePackagePlatform')
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect controller: 'package'
    }

    /**
     * Call to reload all provider data from the speicified we:kb instance.
     * Note that the organisations whose data should be updated need a we:kb ID for match;
     * if no match is being found for the given we:kb ID, a new record will be created!
     */
    @Secured(['ROLE_YODA'])
    def reloadWekbOrg() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            globalSourceSyncService.reloadData('Org')
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect controller: 'organisation', action: 'listProvider'
    }

    /**
     * Call to list all global record sources in the system
     * @see de.laser.remote.GlobalRecordSource
     */
    @Secured(['ROLE_YODA'])
    def manageGlobalSources() {
        Map<String, Object> result = [:]
        log.debug("manageGlobalSources ..")
        result.sources = GlobalRecordSource.list()

        result
    }

    /**
     * Call to list all ElasticSearch sources. Those indices are storing data of this app's database;
     * not to confound with APISources which establish connection to we:kb indices!
     */
    @Secured(['ROLE_YODA'])
    def manageESSources() {
        Map<String, Object> result = [:]
        log.debug("manageESSources ..")
        result.sources = ElasticsearchSource.list()
        result.editable = true

        result
    }

    /**
     * Dumps the current situation of the ElasticSearch domain indices
     * @return a list of the domain indices, including last record timestamp, entry counts (in index and in database), active flag,
     * and index reset buttons
     */
    @Secured(['ROLE_YODA'])
    def manageFTControl() {
        Map<String, Object> result = [:]
        log.debug("manageFTControle ..")
        result.ftControls = FTControl.list([sort: 'domainClassName'])
        result.dataload = [
            running : dataloadService.update_running,
            lastFTIndexUpdateInfo : dataloadService.getLastFTIndexUpdateInfo()
        ]
        result.editable = true

        RestHighLevelClient esclient = ESWrapperService.getClient()

        result.indices = []
        Map es_indices = ESWrapperService.ES_Indices
        es_indices.each{ indice ->
            Map indexInfo = [:]
            indexInfo.name = indice.value
            indexInfo.type = indice.key

            GetIndexRequest request = new GetIndexRequest(indice.value)

            if (esclient.indices().exists(request, RequestOptions.DEFAULT)) {
                CountRequest countRequest = new CountRequest(indice.value)
                CountResponse countResponse = esclient.count(countRequest, RequestOptions.DEFAULT)
                indexInfo.countIndex = countResponse ? countResponse.getCount().toInteger() : 0
            }else {
                indexInfo.countIndex = "n/a"
            }

            String query = "select count(id) from ${indice.key}"
            indexInfo.countDB = FTControl.executeQuery(query)[0]
            result.indices << indexInfo
        }

            try {
                esclient.close()
            }
            catch ( Exception e ) {
                log.error("Problem by Close ES Client",e)
            }

        result
    }

    /**
     * Sets up the ElasticSearch indices for the domain classes
     * @return a redirect to the index status list
     * @see ESWrapperService#ES_Indices
     */
    @Secured(['ROLE_YODA'])
    def createESIndices() {
        Collection esIndicesNames = ESWrapperService.ES_Indices.values() ?: []

        esIndicesNames.each { String indexName ->
            ESWrapperService.createIndex(indexName)
        }
        dataloadService.updateFTIndices()
        redirect action: 'manageFTControl'
    }

    /**
     * Deletes and rebuilds the given index and refills that with updated data
     */
    @Secured(['ROLE_YODA'])
    def deleteAndRefillIndex() {
        String indexName = params.name
        if (indexName) {
           ESWrapperService.deleteIndex(indexName)
           ESWrapperService.createIndex(indexName)
           dataloadService.updateFTIndices()
        }

        redirect(action: 'manageFTControl')
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def newESSource() {
        log.debug("manageGlobalSources ..")
        redirect action:'manageGlobalSources'
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def deleteGlobalSource() {
        GlobalRecordSource.removeSource(params.long('id'))
        redirect(action:'manageGlobalSources')
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    @Transactional
    def newGlobalSource() {
        Map<String, Object> result=[:]
        log.debug("manageGlobalSources ..")

        result.newSource = GlobalRecordSource.findByIdentifier(params.identifier) ?: new GlobalRecordSource(
                identifier:params.identifier,
                name:params.name,
                type:params.type,
                haveUpTo:null,
                uri:params.uri,
                listPrefix:params.listPrefix,
                fullPrefix:params.fullPrefix,
                principal:params.principal,
                credentials:params.credentials,
                rectype:params.int('rectype'))
        result.newSource.save()

        redirect action:'manageGlobalSources'
    }

    /**
     * Moves the Nationaler Statistikserver credentials from the OrgProperty into the OrgSettings structure
     */
    @Secured(['ROLE_YODA'])
    @Transactional
    def migrateNatStatSettings() {
        Org contextOrg = contextService.getOrg()

        List<OrgProperty> opList = OrgProperty.executeQuery(
                'select op from OrgProperty op join op.type pd where pd.descr = :orgConf and op.tenant = :context and op.isPublic = false', [
                orgConf: PropertyDefinition.ORG_CONF,
                context: contextOrg
        ])

        opList.each { OrgProperty op ->
            if (op.type.name == 'API Key') {
                def oss = OrgSetting.get(op.owner, OrgSetting.KEYS.NATSTAT_SERVER_API_KEY)

                if (oss == OrgSetting.SETTING_NOT_FOUND) {
                    OrgSetting.add(op.owner, OrgSetting.KEYS.NATSTAT_SERVER_API_KEY, op.getValue())
                }
                else {
                    oss.setValue(op)
                }
            }
            else if (op.type.name == 'RequestorID') {
                def oss = OrgSetting.get(op.owner, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)

                if (oss == OrgSetting.SETTING_NOT_FOUND) {
                    OrgSetting.add(op.owner, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID, op.getValue())
                }
                else {
                    oss.setValue(op)
                }
            }
        }

        OrgProperty.executeQuery(
                'select op from OrgProperty op join op.type pd where pd.descr = :orgConf '+
                'and ( pd.name = \'API Key\' or pd.name = \'RequestorID\' ) and op.tenant = :context and op.isPublic = false',
                [orgConf: PropertyDefinition.ORG_CONF, context: contextOrg]).each{
            it.delete()
        }

        redirect action:'dashboard'
    }

    /**
     * Lists all system settings and their state
     * @see SystemSetting
     */
    @Secured(['ROLE_YODA'])
    def systemSettings() {
        Map<String, Object> result = [:]
        result.settings = SystemSetting.executeQuery('select s from SystemSetting s where s.name != \'MaintenanceMode\' order by s.name asc')
        result
    }

    /**
     * Enables/disables a boolean setting flag
     */
    @Secured(['ROLE_YODA'])
    @Transactional
    def toggleBoolSetting() {
        SystemSetting s = SystemSetting.findByName(params.setting)
        if (s) {
            if (s.tp == SystemSetting.CONTENT_TYPE_BOOLEAN) {
                if (s.value == 'true')
                    s.value = 'false'
                else
                    s.value = 'true'
            }
            s.save()
        }

        redirect action:'systemSettings'
    }

    /**
     * Enables/disables mail sending from the current server instance
     */
    @Secured(['ROLE_YODA'])
    @Transactional
    def toggleMailSent() {
        if (params.mailSent) {
            if (params.mailSent == 'true')
                grailsApplication.config.grails.mail.disabled = false
            else
                grailsApplication.config.grails.mail.disabled = true
        }

        redirect action:'systemSettings'
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def costItemsApi(String owner) {
        def result = []

        if(owner) {
            def costItems

            //Für später zur besseren Absicherung
            //costItems = CostItem.findAllByOwner(contextService.getOrg())
            if(owner == 'all')
            {
                costItems = CostItem.getAll()
            }
            else{
                costItems = CostItem.findAllByOwnerAndCostItemStatusNotEqual(Org.get(owner), RDStore.COST_ITEM_DELETED)
            }

            costItems.each {
                Map costItem = [:]
                costItem.globalUID = it.globalUID

                costItem.costItemStatus = it.costItemStatus?.value
                costItem.costItemTyp = it.costItemCategory?.value
                costItem.billingCurrency = it.billingCurrency?.value
                costItem.costItemElement = it.costItemElement?.value
                costItem.taxCode = it.taxCode?.value

                costItem.costInBillingCurrency = it.costInBillingCurrency
                costItem.costInLocalCurrency = it.costInLocalCurrency
                costItem.currencyRate = it.currencyRate

                costItem.costTitle = it.costTitle
                costItem.costDescription = it.costDescription
                costItem.reference = it.reference

                costItem.startDate = it.startDate
                costItem.endDate = it.endDate

                costItem.owner = [:]
                it.owner?.each{
                    costItem.owner.globalUID = it.globalUID ?:''
                    costItem.owner.name = it.name
                    costItem.owner.shortname = it.shortname
                    costItem.owner.sortname = it.sortname
                    costItem.owner.libraryType = it.libraryType?.value
                }

                costItem.sub = [:]
                it.sub?.each {
                    costItem.sub.name = it.name
                    costItem.sub.globalUID = it.globalUID ?: ''
                    costItem.sub.startDate = it.startDate
                    costItem.sub.endDate = it.endDate
                }

                costItem.subPkg = it.subPkg
                costItem.issueEntitlement = it.issueEntitlement
                costItem.order = it.order
                costItem.invoice = it.invoice

                result.add(costItem)

            }
        }else {
            result=[result:'You must enter an organization!']
        }

        render result as JSON
    }

    /**
     * Triggers the database update of due reminders without email notification
     */
    @Secured(['ROLE_YODA'])
    def dueDates_updateDashboardDB(){
        flash.message = "DB wird upgedatet...<br/>"
        dashboardDueDatesService.takeCareOfDueDates(true, false, flash)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Triggers the email notification of currently due reminders
     */
    @Secured(['ROLE_YODA'])
    def dueDates_sendAllEmails() {
        flash.message = "Emails mit fälligen Terminen werden vesandt...<br/>"
        dashboardDueDatesService.takeCareOfDueDates(false, true, flash)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Manually triggers the status update of subscriptions and licenses if their due date is reached
     */
    @Secured(['ROLE_YODA'])
    def subscriptionCheck(){
        flash.message = "Lizenzen und Verträge werden upgedatet"
        statusUpdateService.subscriptionCheck()
        statusUpdateService.licenseCheck()
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Manually triggers the status update of surveys if their due date is reached
     */
    @Secured(['ROLE_YODA'])
    def surveyCheck(){
        flash.message = "Umfragen werden upgedatet"
        surveyUpdateService.surveyCheck()
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Manually triggers the subscription holding freezing
     */
    @Secured(['ROLE_YODA'])
    def freezeSubscriptionHoldings(){
        if(subscriptionService.freezeSubscriptionHoldings())
            flash.message = "Bestände werden festgefroren ..."
        else
            flash.message = "Bestände sind bereits festgefroren!"
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Checks the subscription-license linkings on member level and reveals those where the participant is not linked directly to the member license
     */
    @Secured(['ROLE_YODA'])
    Map checkOrgLicRoles() {
        Map<String,Set> result = [:]
        Set licenseSubscriptionLinks = Links.executeQuery("select li, ooo from Links li join li.sourceLicense l join li.destinationSubscription s join s.orgRelations ooo where li.linkType = :license and l.instanceOf != null and s.instanceOf != null and not exists(select oo from OrgRole oo where oo.lic = l and oo.roleType = :licCons and oo.org = (select ooo.org from ooo where ooo.sub = s and ooo.roleType = :subCons)) and ooo.roleType = :subCons", [license: RDStore.LINKTYPE_LICENSE, licCons: RDStore.OR_LICENSEE_CONS, subCons: RDStore.OR_SUBSCRIBER_CONS])
        result.links = licenseSubscriptionLinks
        result
    }

    /**
     * Synchronises the linkings between members and member licenses so that members can access the underlying licenses
     * @return
     */
    @Secured(['ROLE_YODA'])
    @Transactional
    def updateOrgLicRoles() {
        Set licenseSubscriptionLinks = Links.executeQuery("select li, ooo from Links li join li.sourceLicense l join li.destinationSubscription s join s.orgRelations ooo where li.linkType = :license and l.instanceOf != null and s.instanceOf != null and not exists(select oo from OrgRole oo where oo.lic = l and oo.roleType = :licCons and oo.org = (select ooo.org from ooo where ooo.sub = s and ooo.roleType = :subCons)) and ooo.roleType = :subCons", [license: RDStore.LINKTYPE_LICENSE, licCons: RDStore.OR_LICENSEE_CONS, subCons: RDStore.OR_SUBSCRIBER_CONS])
        licenseSubscriptionLinks.eachWithIndex { row, int i ->
            log.debug("now processing record ${i} out of ${licenseSubscriptionLinks.size()} entries")
            License l = row[0].sourceLicense
            Org o = row[1].org
            OrgRole oo = OrgRole.findByLicAndOrg(l, o)
            if(oo && oo.roleType == null) {
                oo.roleType = RDStore.OR_LICENSEE_CONS
                log.debug("faulty record corrected")
            }
            else {
                oo = new OrgRole(lic: l, org: o, roleType: RDStore.OR_LICENSEE_CONS)
                log.debug("new record created")
            }
            oo.save()
        }
        redirect(action: 'checkOrgLicRoles')
    }

    /**
     * Executes deletion of objects marked as deleted
     */
    @Secured(['ROLE_YODA'])
    def dropDeletedObjects() {
        Map<String, Object> result = [:]

        String order = (params.option?.equals('reverse')) ? 'asc' : 'desc'

        if (params.cmd == 'subscription') {

            List<Subscription> subList =
                    Subscription.executeQuery("select s from Subscription s join s.status ss where ss.value = 'Deleted' order by s.id " + order, [max:10])

            subList.each { sub ->
                try {
                    if (!sub.derivedSubscriptions) {
                        log.info '-----> deleting subscription: ' + sub.id
                        deletionService.deleteSubscription(sub, false)
                    }
                } catch (Exception e) {
                    log.error e.getMessage()
                }
                sleep(1000)
            }
        }
        else if (params.cmd == 'license') {

            List<License> licList =
                    License.executeQuery("select l from License l join l.status ls where ls.value = 'Deleted' order by l.id "  + order, [max:10])

            licList.each { lic ->
                try {
                    if (!lic.derivedLicenses) {
                        log.info '-----> deleting license: ' + lic.id
                        deletionService.deleteLicense(lic, false)
                    }
                } catch (Exception e) {
                    log.error e.getMessage()
                }
                sleep(1000)
            }
        }

        List<Subscription> subList =
                Subscription.executeQuery("select s from Subscription s join s.status ss where ss.value = 'Deleted' order by s.id desc")

        List<License> licList =
                License.executeQuery("select l from License l join l.status ls where ls.value = 'Deleted' order by l.id desc")

        result.subscriptions = subList
        result.licenses = licList

        result
    }

    /**
     * Currently unused; call to backwards-correct costs in local currency
     */
    @Secured(['ROLE_YODA'])
    def correctCostsInLocalCurrency() {
        Map<String, Object> result = ["costItems":financeService.correctCostsInLocalCurrency(Boolean.valueOf(params.dryRun))]
        result
    }

    /**
     * Purges the database from empty private properties
     */
    @Secured(['ROLE_YODA'])
    def dbmFixPrivateProperties() {
        Map<String, Object> result = [:]

        List opp = OrgProperty.executeQuery(
                "SELECT pp FROM OrgProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        List spp = SubscriptionProperty.executeQuery(
                "SELECT pp FROM SubscriptionProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                "AND pp.isPublic = false " +
                "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                "AND (pp.note IS null OR pp.note = '') "
        )

        List lpp = LicenseProperty.executeQuery(
                "SELECT pp FROM LicenseProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') " +
                        "AND (pp.paragraph IS null OR pp.paragraph = '') "
        )

        List ppp = PersonProperty.executeQuery(
                "SELECT pp FROM PersonProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        if (params.cmd == 'doIt') {
            //println opp.collect{ it.id }
            if (opp.size() > 0) {
                OrgProperty.executeUpdate('DELETE FROM OrgProperty opp WHERE opp.id in :idList',
                        [idList: opp.collect { it.id }]
                )
            }

            //println spp.collect{ it.id }
            if (spp.size() > 0) {
                SubscriptionProperty.executeUpdate('DELETE FROM SubscriptionProperty spp WHERE spp.id in :idList',
                        [idList: spp.collect { it.id }]
                )
            }

            //println lpp.collect{ it.id }
            if (lpp.size() > 0) {
                LicenseProperty.executeUpdate('DELETE FROM LicenseProperty lpp WHERE lpp.id in :idList',
                        [idList: lpp.collect { it.id }]
                )
            }

            //println ppp.collect{ it.id }
            if (ppp.size() > 0) {
                PersonProperty.executeUpdate('DELETE FROM PersonProperty ppp WHERE ppp.id in :idList',
                        [idList: ppp.collect { it.id }]
                )
            }
        }

        result.candidates = [OrgProperty: opp, SubscriptionProperty: spp, LicenseProperty: lpp, PersonProperty: ppp]

        render view: 'databaseMigration', model: result
    }

    @Deprecated
    @Secured(['ROLE_YODA'])
    def cleanUpSurveys() {
        Map<String, Object> result = [:]

        List<SurveyConfig> subSurveys = SurveyConfig.findAllBySubscriptionIsNotNull()
        int count = 0
        List resultMap = []
        subSurveys.each { surConfig ->


            def parentSubscription = surConfig?.subscription
            def parentSubChilds = subscriptionService.getCurrentValidSubChilds(parentSubscription)
            def parentSuccessorSubscription = surConfig?.subscription?._getCalculatedSuccessorForSurvey()
            //def property = PropertyDefinition.getByNameAndDescr("Perennial term checked", PropertyDefinition.SUB_PROP)
            parentSubChilds.each { sub ->
                if (sub._getCalculatedSuccessorForSurvey()) {
                    sub.getAllSubscribers().each { org1 ->

                        def surveyResult = SurveyResult.findAllBySurveyConfigAndParticipant(surConfig, org1)
                        if(surveyResult?.size() > 0) {
                            count++
                            def newMap = [:]
                            //println(count + ": ${sub.name} (${sub.id}) [${org1.name}]" + surveyResult)
                            newMap.surveyResult = surveyResult?.id ?: ""
                            newMap.subName = sub.name
                            newMap.subId = sub.id
                            newMap.orgName = org1.name
                            newMap.sortName = org1?.sortname
                            newMap.propertiesSize = surveyResult?.size()
                            newMap.info = 'Nachfolger Lizenz vorhanden'
                            //println("")
                            resultMap << newMap
                        }else{
                            count++
                            def newMap = [:]
                            //println(count + ": LEER : ${sub.name} (${sub.id}) [${org1.name}]" + surConfig)
                            newMap.surveyResult = 'Kein Umfrage'
                            newMap.subName = sub.name
                            newMap.subId = sub.id
                            newMap.orgName = org1.name
                            newMap.sortName = org1?.sortname
                            newMap.propertiesSize = 0
                            newMap.info = 'Nachfolger Lizenz vorhanden'
                            //println("")
                            resultMap << newMap
                        }

                    }

                } else {
                   /* if (property?.isRefdataValueType()) {
                        if (sub?.propertySet?.find {
                            it?.type?.id == property?.id
                        }?.refValue == RefdataValue.getByValueAndCategory('Yes', property?.refdataCategory)) {

                            sub?.getAllSubscribers().each { org ->
                                def surveyResult = SurveyResult.findAllBySurveyConfigAndParticipant(surConfig, org)
                                if(surveyResult?.size() > 0) {
                                    count++
                                    def newMap = [:]
                                    println(count + ":Merkmal: ${sub.name} (${sub.id}) [${org.name}]" + surveyResult)
                                    newMap.surveyResult = surveyResult?.id ?: ""
                                    newMap.subName = sub.name
                                    newMap.subId = sub.id
                                    newMap.orgName = org.name
                                    newMap.sortName = org?.sortname
                                    newMap.propertiesSize = surveyResult?.size()
                                    newMap.info = 'Merkmal vorhanden'
                                    println("")
                                    resultMap << newMap
                                }else{
                                    count++
                                    def newMap = [:]
                                    println(count + ": LEER : ${sub.name} (${sub.id}) [${org.name}]" + surConfig)
                                    newMap.surveyResult = 'Kein Umfrage'
                                    newMap.subName = sub.name
                                    newMap.subId = sub.id
                                    newMap.orgName = org.name
                                    newMap.sortName = org?.sortname
                                    newMap.propertiesSize = 0
                                    newMap.info = 'Merkmal vorhanden'
                                    println("")
                                    resultMap << newMap
                                }

                            }
                        }
                    }*/
                }
            }

        }

        response.setHeader("Content-disposition", "attachment; filename=\"Moe.csv\"")
        response.contentType = "text/csv"
        ServletOutputStream out = response.outputStream
        List titles = [

        ]
        List rows = []
        resultMap.each { newMap ->
            List row = []

            row.add(newMap.subName)
            row.add(newMap.subId)
            row.add(newMap.sortName)
            row.add(newMap.orgName)
            row.add(newMap.propertiesSize)
            row.add(newMap.info)
            row.add(newMap.surveyResult)

            rows.add(row)
        }
        out.withWriter { writer ->
            writer.write(exportService.generateSeparatorTableString(titles,rows,','))
        }
        out.close()

        result

        redirect action: 'dashboard'
    }

}
