package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.auth.Role
import de.laser.auth.UserRole
import de.laser.base.AbstractJob
import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.properties.SubscriptionProperty
import de.laser.remote.Wekb
import de.laser.remote.FTControl
import de.laser.remote.GlobalRecordSource
import de.laser.reporting.report.ReportingCache
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.stats.LaserStatsCursor
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.system.SystemActivityProfiler
import de.laser.system.SystemEvent
import de.laser.system.SystemProfiler
import de.laser.system.SystemSetting
import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
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
import org.springframework.security.web.FilterChainProxy
import org.springframework.security.web.SecurityFilterChain

import javax.servlet.Filter
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.sql.Timestamp
import java.time.LocalDate
import java.util.concurrent.ExecutorService

/**
 * This controller is a buddy-for-everything controller and handles thus all sorts of global management
 * calls. Many methods are dangerous because they affect many parts of the system (global cleanup methods
 * for example which may affect many title holdings) or are under subject of testing before they get
 * generally released.
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class YodaController {

    Filter springSecurityFilterChain
    SessionFactory sessionFactory

    CacheService cacheService
    ContextService contextService
    DashboardDueDatesService dashboardDueDatesService
    DataloadService dataloadService
    DeletionService deletionService
    ESWrapperService ESWrapperService
    ExecutorService executorService
    FinanceService financeService
    FormService formService
    GokbService gokbService
    GlobalSourceSyncService globalSourceSyncService
    PackageService packageService
    ProviderService providerService
    def quartzScheduler
    RenewSubscriptionService renewSubscriptionService
    StatsSyncService statsSyncService
    StatusUpdateService statusUpdateService
    SubscriptionService subscriptionService
    SurveyUpdateService surveyUpdateService
    VendorService vendorService
    YodaService yodaService
    WekbNewsService wekbNewsService

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
                'dataSource',
                ConfigDefaults.DATASOURCE_DEFAULT + '.password',
                ConfigDefaults.DATASOURCE_STORAGE + '.password',
                ConfigMapper.WEKB_API_PASSWORD[0],
                ConfigMapper.DOCUMENT_STORAGE_KEY[0]
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
                boolean isLaserJob = groupName == 'GRAILS_JOBS'

                List<List> cp  = isLaserJob ? clazz.getAt('configurationProperties') : []

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
                        running: '',
                        available: ''
                ]
                if (isLaserJob) {
                    map.running     = (applicationContext.getBean(key.getName()) as AbstractJob).isRunning()
                    map.available   = (applicationContext.getBean(key.getName()) as AbstractJob).isAvailable()
                }

                List crx = triggers.collect{ it.hasProperty('cronEx') ? it.cronEx : null }

                if (crx && isLaserJob) {
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
     * Call to open the list of ${@link FilterChainProxy}s
     */
    @Secured(['ROLE_YODA'])
    def systemOddments() {
        Map<String, Object> result = [:]

        FilterChainProxy filterChainProxy = (FilterChainProxy) springSecurityFilterChain
        List<SecurityFilterChain> list = filterChainProxy.getFilterChains()

        result.filters = list.stream().flatMap(chain -> chain.getFilters().stream()).toArray().toList()
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

        if (params.cmd && params.type) {

            if (params.cmd == 'clearCache') {
                if (params.type == 'session') {
                    contextService.getSessionCache().clear()
                }
                else if (params.type == 'ehcache' && params.cache) {
                    cacheService.clear(cacheService.getCache(result.ehcacheManager, params.cache))
                }

                params.remove('cmd')
                params.remove('type')
                params.remove('cache')

                redirect controller: 'yoda', action: 'systemCache', params: params
                return
            }
            else if (params.cmd == 'get') {
                JSON entry

                if (params.type == 'reporting' && params.token) {
                    ReportingCache rCache = new ReportingCache(ReportingCache.CTX_GLOBAL, params.token as String)
                    entry = rCache.get() as JSON
                }
                else if (params.type == 'session' && params.key) {
                    entry = contextService.getSessionCache().get(params.key) as JSON
                }
                if (entry) {
                    entry.prettyPrint = true
                    response.setContentType("application/json")
                    response.outputStream << entry

                    return
                }
            }
        }

        result
    }

    @Secured(['ROLE_YODA'])
    def profilerCurrent() {
        Map result = [:]

        EhcacheWrapper ttl1800 = BeanStore.getCacheService().getTTL1800Cache( SystemActivityProfiler.CACHE_KEY_ACTIVE_USER )
        result.users = SystemActivityProfiler.getActiveUsers(1000 * 60 * 10).collect { u -> ttl1800.get(u) }.sort { it[1] }

        render view: '/yoda/profiler/current', model: result
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

        render view: '/yoda/profiler/activity', model: result
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
        result.allArchives = SystemProfiler.executeQuery('select distinct(archive) from SystemProfiler').collect{ it ->
            [it,  SystemProfiler.executeQuery('select count(*) from SystemProfiler where archive =: archive', [archive: it])[0]]
        }

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

        render view: '/yoda/profiler/loadtime', model: result
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
        result.globalTimelineOrder = ordered.sort{ it.key }

        render view: '/yoda/profiler/timeline', model: result
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
                                    specRole : '',
                                    affil    : '',
                                    perm     : '',
                                    type     : '',
                                    test     : ''
                            ]

                            if (da.isInstUser() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstUser()' //  + da.isInstUser().toList()
                                mInfo.debug.perm        = da.isInstUser().toList()[0]
                                mInfo.debug.affil       = 'INST_USER'
                            }
                            if (da.isInstEditor() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstEditor()' //  + da.isInstEditor().toList()
                                mInfo.debug.perm        = da.isInstEditor().toList()[0]
                                mInfo.debug.affil       = 'INST_EDITOR'
                            }
                            if (da.isInstAdm() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstAdm()' //  + da.isInstAdm().toList()
                                mInfo.debug.perm        = da.isInstAdm().toList()[0]
                                mInfo.debug.affil       = 'INST_ADM'
                            }
                            if (da.isInstEditor_or_ROLEADMIN() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstEditor_or_ROLEADMIN()' //  + da.isInstEditor_or_ROLEADMIN().toList()
                                mInfo.debug.perm        = da.isInstEditor_or_ROLEADMIN().toList()[0]
                                mInfo.debug.affil       = 'INST_EDITOR'
                                mInfo.debug.specRole    = 'ROLE_ADMIN'
                            }
                            if (da.isInstAdm_or_ROLEADMIN() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstAdm_or_ROLEADMIN()' //  + da.isInstAdm_or_ROLEADMIN().toList()
                                mInfo.debug.perm        = da.isInstAdm_or_ROLEADMIN().toList()[0]
                                mInfo.debug.affil       = 'INST_ADM'
                                mInfo.debug.specRole    = 'ROLE_ADMIN'
                            }
                            if (da.isInstUser_denySupport() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstUser_denySupport()' //  + da.isInstUser_denySupport().toList()
                                mInfo.debug.perm        = da.isInstUser_denySupport().toList()[0]
                                mInfo.debug.affil       = 'INST_USER'
                            }
                            if (da.isInstEditor_denySupport() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstEditor_denySupport()' //  + da.isInstEditor_denySupport().toList()
                                mInfo.debug.perm        = da.isInstEditor_denySupport().toList()[0]
                                mInfo.debug.affil       = 'INST_EDITOR'
                            }
                            if (da.isInstAdm_denySupport() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstAdm_denySupport()' //  + da.isInstAdm_denySupport().toList()
                                mInfo.debug.perm        = da.isInstAdm_denySupport().toList()[0]
                                mInfo.debug.affil       = 'INST_ADM'
                            }
                            if (da.isInstAdm_denySupport_or_ROLEADMIN() != ([''] as String[])) {
                                mInfo.debug.test        = 'isInstAdm_denySupport_or_ROLEADMIN()' //  + da.isInstAdm_denySupport_or_ROLEADMIN().toList()
                                mInfo.debug.perm        = da.isInstAdm_denySupport_or_ROLEADMIN().toList()[0]
                                mInfo.debug.affil       = 'INST_ADM'
                                mInfo.debug.specRole    = 'ROLE_ADMIN'
                            }

                            if (da.ctrlService()) {
                                mInfo.ctrlService = da.ctrlService()
                                if (da.ctrlService() == 1) {
                                    mInfo.refactoring = 'done'
                                }
                            }
                            if (da.withTransaction()) {
                                mInfo.withTransaction = da.withTransaction()
                                if (da.withTransaction() == 1) {
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
     * Another one of the dangerous calls: creates missing titles for packages where auto-accept has been
     * configured for new titles. Concerned are only those packages where the setting for new titles is set to "accept"
     */
    @Secured(['ROLE_YODA'])
    def matchPackageHoldings() {
        log.debug("match package holdings to issue entitlement holdings ...")
        flash.message = "Bestände werden korrigiert ..."
        yodaService.matchPackageHoldings(params.long('pkgId'))
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def cleanupIssueEntitlements() {
        log.debug("purge unnecessary titles ...")
        executorService.execute ({
            yodaService.cleanupIssueEntitlements()
        })
        redirect action: "systemThreads"
    }

    /**
     * Matches the status of the title instance to the issue entitlement holdings. Should be used in case of
     * preceding errors in the title synchronisation where the
     * @return
     */
    @Secured(['ROLE_YODA'])
    def matchTitleStatus() {
        if(!yodaService.bulkOperationRunning) {
            yodaService.matchTitleStatus()
            log.debug("Titel-Status-Matching läuft ...")
        }
        else log.error("Prozess läuft schon, kein Neustart")
        redirect controller: 'home'
    }

    /**
     * Bulk retriggering of inheritance
     */
    @Secured(['ROLE_YODA'])
    Map<String, Object> retriggerInheritance() {
        yodaService.retriggerInheritance('holdingSelection') //TODO generalise
        redirect controller: 'home'
    }

    /**
     * Lists the currently created bulk files (= the export cache)
     */
    @Secured(['ROLE_YODA'])
    Map<String, Object> manageTempUsageFiles() {
        File dir = new File(GlobalService.obtainTmpFileLocation())
        List<File> tempFiles = dir.listFiles()
        //tempFiles.sort { File f -> Files.getAttribute(f.toPath(), 'creationTime') }
        [tempFiles: tempFiles]
    }

    /**
     * Deletes the given temp file; intended for debugging purposes (= file creation test)
     * @return redirects back to the list view @ {@link #manageTempUsageFiles()}
     */
    @Secured(['ROLE_YODA'])
    def deleteTempFile() {
        if(params.containsKey('filename')) {
            if(!params.filename.contains('..') && !params.filename.contains('\\') && !params.filename.contains('/')) {
                File f = new File(GlobalService.obtainTmpFileLocation() + '/' + params.filename)
                try {
                    f.delete()
                }
                catch (IOException e) {
                    log.error("unable to delete file: ${GlobalService.obtainTmpFileLocation() + '/' + params.filename}")
                }
            }
        }
        else if(params.containsKey('emptyDir')) {
            File f = new File(GlobalService.obtainTmpFileLocation())
            f.deleteDir()
            f.mkdir()
        }
        //tempFiles.sort { File f -> Files.getAttribute(f.toPath(), 'creationTime') }
        redirect action: 'manageTempUsageFiles'
    }

    /**
     * Shows platforms which have a cursor recorded, i.e. usage data has been loaded already
     * @return a list of platforms with cursors
     */
    @Secured(['ROLE_YODA'])
    Map<String, Object> manageStatsSources() {
        Map<String, Object> result = [
                platforms: [],
                platformInstanceRecords: [:],
                flagContentGokb : true // gokbService.executeQuery
        ]
        Map allPlatforms = gokbService.executeQuery(Wekb.getSushiSourcesURL(), [:])
        if (allPlatforms.error && allPlatforms.error == 404) {
            result.wekbServerUnavailable = message(code: 'wekb.error.404')
        }
        else if (allPlatforms) {
            Map records = allPlatforms
            List allRecords = []
            allRecords.addAll(records.counter4ApiSources.values())
            allRecords.addAll(records.counter5ApiSources.values())
            allRecords.each { platform ->
                result.platforms << Platform.findByGokbId(platform.uuid)
                result.platformInstanceRecords[platform.uuid] = platform
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
            String dataToLoad = params.dataToLoad, objType = params.objType
            executorService.execute({
                Thread.currentThread().setName("UpdateData")
                if (dataToLoad in ["medium", "openAccess", "accessType", "globalUID"] && objType == 'issueEntitlement')
                    yodaService.fillValue(dataToLoad)
                else
                    globalSourceSyncService.updateData(dataToLoad)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect action: 'systemThreads'
    }

    /**
     * Triggers the regeneration of sort titles for the given package
     */
    @Secured(['ROLE_YODA'])
    def regenerateSortTitles() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            de.laser.wekb.Package pkg = de.laser.wekb.Package.get(params.pkg)
            executorService.execute({
                Thread.currentThread().setName("RegenerateSortTitles")
                packageService.regenerateSortTitles(pkg)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect action: 'systemThreads'
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
            executorService.execute({
                Thread.currentThread().setName("GlobalDataUpdate_${componentType}")
                globalSourceSyncService.reloadData('TitleInstancePackagePlatform')
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Is one of the dangerous methods: retriggers the change processing on title level and hands eventual differences
     * to the local holdings
     */
    @Secured(['ROLE_YODA'])
    def reloadPackage() {
        if(!globalSourceSyncService.running) {
            log.debug("match IssueEntitlements to TIPPs ...")
            String uuid = params.packageUUID
            executorService.execute({
                globalSourceSyncService.doSingleSync(uuid)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        flash.message = "Pakete werden nachgehalten ..."

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Is another one of the dangerous methods: retriggers the change processing on subscription holding level and hands eventual differences
     * to the local holdings; if necessary, pending changes are being generated
     * @see PendingChange
     * @deprecated use case is not given any more; {@link #reloadPackage()} and {@link #matchTitleStatus()} do the purpose
     */
    @Deprecated
    @Secured(['ROLE_YODA'])
    def retriggerPendingChanges() {
        if(!globalSourceSyncService.running) {
            log.debug("match IssueEntitlements to TIPPs ...")
            String uuid = params.packageUUID
            executorService.execute({
                statusUpdateService.retriggerPendingChanges(uuid)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        flash.message = "Pakete werden nachgehalten ..."

        redirect controller: 'package'
    }

    /**
     * Call to reload all provider data from the specified we:kb instance.
     * Note that the organisations whose data should be updated need a we:kb ID for match;
     * if no match is being found for the given we:kb ID, a new record will be created!
     */
    @Secured(['ROLE_YODA'])
    def reloadWekbProvider() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            executorService.execute({
                Thread.currentThread().setName("GlobalDataUpdate_Provider")
                globalSourceSyncService.reloadData('Org')
                yodaService.expungeRemovedComponents(Provider.class.name)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect controller: 'organisation', action: 'listProvider'
    }

    /**
     * Call to reload all vendor (agency) data from the specified we:kb instance.
     * Note that the vendors whose data should be updated need a we:kb ID for match;
     * if no match is being found for the given we:kb ID, a new record will be created!
     */
    @Secured(['ROLE_YODA'])
    def reloadWekbVendor() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            executorService.execute({
                Thread.currentThread().setName("GlobalDataUpdate_Vendor")
                globalSourceSyncService.reloadData('Vendor')
                yodaService.expungeRemovedComponents(Vendor.class.name)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect controller: 'vendor', action: 'index'
    }

    /**
     * Call to reload all platform data from the speicified we:kb instance.
     * Note that the platforms whose data should be updated need a we:kb ID for match;
     * if no match is being found for the given we:kb ID, a new record will be created!
     */
    @Secured(['ROLE_YODA'])
    def reloadWekbPlatform() {
        if(!globalSourceSyncService.running) {
            log.debug("start reloading ...")
            executorService.execute({
                Thread.currentThread().setName("GlobalDataUpdate_Platform")
                globalSourceSyncService.reloadData('Platform')
                yodaService.expungeRemovedComponents(Platform.class.name)
            })
        }
        else {
            log.debug("process running, lock is set!")
        }
        redirect controller: 'platform', action: 'list'
    }

    @Secured(['ROLE_YODA'])
    def migrateProviders() {
        providerService.migrateProviders()
        redirect controller: 'myInstitution', action: 'dashboard'
    }

    @Secured(['ROLE_YODA'])
    def migrateVendors() {
        vendorService.migrateVendors()
        redirect controller: 'myInstitution', action: 'dashboard'
    }

    /**
     * Updates the recent we:kb changes cache
     * @return redirects back to the dashboard
     */
    @Secured(['ROLE_YODA'])
    def manageWekbNews() {
        log.info('--> manageWekbNews: ' + params.cmd)
        if (params.cmd == 'update') {
            wekbNewsService.updateCache()
        }
        else if (params.cmd == 'clear') {
            wekbNewsService.clearCache()
        }
        redirect controller: 'myInstitution', action: 'dashboard'
    }

    /**
     * Call to list all global record sources in the system
     * @see de.laser.remote.GlobalRecordSource
     */
    @Secured(['ROLE_YODA'])
    def manageGlobalSources() {
        Map<String, Object> result = [editable: true]
        log.debug("manageGlobalSources ..")
        result.sources = GlobalRecordSource.list([sort: 'id'])

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

        RestHighLevelClient esclient = ESWrapperService.getNewClient()

        result.indices = []
        Map es_indices = ESWrapperService.ES_Indices
        es_indices.each{ index ->
            Map indexInfo = [:]
            indexInfo.name = index.value
            indexInfo.type = index.key

            GetIndexRequest request = new GetIndexRequest(index.value)

            if (esclient.indices().exists(request, RequestOptions.DEFAULT)) {
                CountRequest countRequest = new CountRequest(index.value)
                CountResponse countResponse = esclient.count(countRequest, RequestOptions.DEFAULT)
                indexInfo.countIndex = countResponse ? countResponse.getCount().toInteger() : 0
            }else {
                indexInfo.countIndex = "n/a"
            }

            String query = "select count(*) from ${index.key}"
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
    def resetIndex() {
        if (params.name) {
            ESWrapperService.deleteIndex(params.name)
            ESWrapperService.createIndex(params.name)
            dataloadService.updateFTIndex(params.name)
        }
        redirect(action: 'manageFTControl')
    }

    /**
     * Call to trigger an index update
     */
    @Secured(['ROLE_YODA'])
    def continueIndex() {
        if (params.name) {
            dataloadService.updateFTIndex(params.name)
        }
        redirect(action: 'manageFTControl')
    }

    /**
     * Lists all system settings and their state
     * @see SystemSetting
     */
    @Secured(['ROLE_YODA'])
    def systemSettings() {
        Map<String, Object> result = [:]
        result.settings = []
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
     * Manually triggers the automatic subscription renewal
     */
    @Secured(['ROLE_YODA'])
    def subscriptionRenewCheck(){
        flash.message = "Lizenzen werden automatisch verlängert"
        renewSubscriptionService.subscriptionRenewCheck()
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
    /*
    @Secured(['ROLE_YODA'])
    def freezeSubscriptionHoldings(){
        if(subscriptionService.freezeSubscriptionHoldings())
            flash.message = "Bestände werden festgefroren ..."
        else
            flash.message = "Bestände sind bereits festgefroren!"
        redirect(url: request.getHeader('referer'))
    }
    */

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
                        "AND pp.stringValue IS null AND pp.longValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        List spp = SubscriptionProperty.executeQuery(
                "SELECT pp FROM SubscriptionProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                "AND pp.isPublic = false " +
                "AND pp.stringValue IS null AND pp.longValue IS null AND pp.decValue IS null " +
                "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                "AND (pp.note IS null OR pp.note = '') "
        )

        List lpp = LicenseProperty.executeQuery(
                "SELECT pp FROM LicenseProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.longValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') " +
                        "AND (pp.paragraph IS null OR pp.paragraph = '') "
        )

        List ppp = PersonProperty.executeQuery(
                "SELECT pp FROM PersonProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.longValue IS null AND pp.decValue IS null " +
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

    /**
     * Generates permanent title records for each issue entitlement which has a perpetual access ensured
     * @see PermanentTitle
     * @see Subscription#hasPerpetualAccess
     */
    @Secured(['ROLE_YODA'])
    def setPermanentTitle() {
        if (subscriptionService.checkThreadRunning('setPermanentTitle')) {
            flash.error = 'setPermanentTitle process still running!'
            redirect controller: 'yoda', action: 'index'
            return
        }
        executorService.execute({
            Thread.currentThread().setName("setPermanentTitle")
            int countProcess = 0, max = 100000
            Set<RefdataValue> status = [RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_RETIRED, RDStore.TIPP_STATUS_DELETED]
            int ieCount = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.status in (:status) and ie.subscription.hasPerpetualAccess = true', [status: status])[0]
            if (ieCount > 0) {
                for(int offset = 0; offset < ieCount; offset+=max) {
                    List<IssueEntitlement> ies = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.status in (:status) and ie.subscription.hasPerpetualAccess = true', [status: status], [max: max, offset: offset])
                    ies.eachWithIndex { IssueEntitlement issueEntitlement, int i ->
                        //log.debug("now processing record ${offset+i} out of ${ieCount}")
                        TitleInstancePackagePlatform titleInstancePackagePlatform = issueEntitlement.tipp
                        Org owner = issueEntitlement.subscription.getSubscriberRespConsortia()
                        issueEntitlement.perpetualAccessBySub = issueEntitlement.subscription
                        issueEntitlement.save()

                        if (!PermanentTitle.findByOwnerAndTipp(owner, titleInstancePackagePlatform)) {
                            PermanentTitle permanentTitle = new PermanentTitle(subscription: issueEntitlement.subscription,
                                    issueEntitlement: issueEntitlement,
                                    tipp: titleInstancePackagePlatform,
                                    owner: owner).save()
                            countProcess++
                            //log.debug("record ${offset+i} out of ${ieCount} had permanent title ${countProcess}")
                        }
                    }
                }
            }
            SystemEvent.createEvent('YODA_PROCESS', [yodaProcess: 'setPermanentTitle', countProcess: countProcess])
        })

        flash.message = 'setPermanentTitle process is now running. In SystemEvents you see when setPermanentTitle is finish!'
        redirect controller: 'yoda', action: 'index'
    }

    @Secured(['ROLE_YODA'])
    def removePerpetualAccessByIes() {
        if (subscriptionService.checkThreadRunning('removePerpetualAccessByIes')) {
            flash.error = 'removePerpetualAccessByIes process still running!'
            redirect controller: 'yoda', action: 'index'
            return
        }
        executorService.execute({
            Thread.currentThread().setName("removePerpetualAccessByIes")
            int countProcess = 0, max = 100000
            int ieCount = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.perpetualAccessBySub is not null and ie.subscription.hasPerpetualAccess = false')[0]
            if (ieCount > 0) {
                for(int offset = 0; offset < ieCount; offset+=max) {
                    List<IssueEntitlement> ies = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.perpetualAccessBySub is not null and ie.subscription.hasPerpetualAccess = false', [max: max, offset: offset])
                    ies.eachWithIndex { IssueEntitlement issueEntitlement, int i ->
                        //log.debug("now processing record ${offset+i} out of ${ieCount}")
                        issueEntitlement.perpetualAccessBySub = null
                        issueEntitlement.save()
                        PermanentTitle.findByOwnerAndIssueEntitlement(owner, issueEntitlement).delete()
                        countProcess++
                         //log.debug("record ${offset+i} out of ${ieCount} had permanent title ${countProcess}")
                    }
                }
            }
            SystemEvent.createEvent('YODA_PROCESS', [yodaProcess: 'removePerpetualAccessByIes', countProcess: countProcess])
        })

        flash.message = 'removePerpetualAccessByIes process is now running. In SystemEvents you see when removePerpetualAccessByIes is finish!'
        redirect controller: 'yoda', action: 'index'
    }

    @Secured(['ROLE_YODA'])
    def gdc_docContextTargetOrg() {
        log.debug 'gdc_docContextTargetOrg'

        List changes = [] // [shareConf, docContext, source, newTargetOrg]

        RefdataValue sc_all             = RDStore.SHARE_CONF_ALL
        RefdataValue sc_upOrg           = RDStore.SHARE_CONF_UPLOADER_ORG
        RefdataValue sc_upOrgAndTarget  = RDStore.SHARE_CONF_UPLOADER_AND_TARGET

        List<DocContext> dc_all             = DocContext.findAllByShareConfAndTargetOrgIsNull( sc_all )
        List<DocContext> dc_upOrg           = DocContext.findAllByShareConfAndTargetOrgIsNull( sc_upOrg )
        List<DocContext> dc_upOrgAndTarget  = DocContext.findAllByShareConfAndTargetOrgIsNull( sc_upOrgAndTarget )

        log.info ' (' + sc_all.id + ') SHARE_CONF_ALL : ' + dc_all.size()
        log.info ' (' + sc_upOrg.id + ') SHARE_CONF_UPLOADER_ORG : ' + dc_upOrg.size()
        log.info ' (' + sc_upOrgAndTarget.id + ') SHARE_CONF_UPLOADER_AND_TARGET : ' + dc_upOrgAndTarget.size()

//        log.info ' (' + sc_all.id + ') SHARE_CONF_ALL : ' + dc_all.collect { it.id }
//        log.info ' (' + sc_upOrg.id + ') SHARE_CONF_UPLOADER_ORG : ' + dc_upOrg.collect { it.id }
//        log.info ' (' + sc_upOrgAndTarget.id + ') SHARE_CONF_UPLOADER_AND_TARGET : ' + dc_upOrgAndTarget.collect { it.id }

        dc_all.each {dc ->
            dc.targetOrg = dc.owner.owner
            dc.save()
            changes << [dc.shareConf.id, dc.id, dc.targetOrg.id, 'dc.owner.owner']
        }

        dc_upOrg.each {dc ->
            dc.targetOrg = dc.owner.owner
            dc.save()
            changes << [dc.shareConf.id, dc.id, dc.targetOrg.id, 'dc.owner.owner']
        }

        dc_upOrgAndTarget.each {dc ->
            if (dc.license) {
                dc.targetOrg = dc.license.getLicensee()
                dc.save()
                changes << [dc.shareConf.id, dc.id, dc.targetOrg.id, 'dc.license.getLicensee()']
            }
            else if (dc.link) {
                log.debug 'TODO: link @ ' + dc.id
            }
            else if (dc.org) {
                dc.targetOrg = dc.org
                dc.save()
                changes << [dc.shareConf.id, dc.id, dc.targetOrg.id, 'dc.org']
            }
            else if (dc.provider) {
                log.debug 'TODO: provider @ ' + dc.id
            }
            else if (dc.subscription) {
                dc.targetOrg = dc.subscription.getSubscriber()
                dc.save()
                changes << [dc.shareConf.id, dc.id, dc.targetOrg.id, 'dc.subscription.getSubscriber()']
            }
            else if (dc.surveyConfig) {
                log.debug 'TODO: surveyConfig @ ' + dc.id
            }
            else if (dc.vendor) {
                log.debug 'TODO: vendor @ ' + dc.id
            }
        }

        if (changes) {
            log.info 'gdc_docContextTargetOrg: ' + changes.size()

            SystemEvent.createEvent('GDC_INFO', [
                    server: AppUtils.getCurrentServer(),
                    op: 'DocContext: fixed missing target org',
                    count: changes.size(),
                    changes: changes
            ])
        }

        log.debug changes.toListString()
    }
}
