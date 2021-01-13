package de.laser

import de.laser.annotations.DebugAnnotation
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.auth.UserRole
import de.laser.finance.CostItem
import de.laser.finance.CostItemElementConfiguration
import de.laser.helper.*
import de.laser.properties.LicenseProperty
import de.laser.properties.OrgProperty
import de.laser.properties.PersonProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.system.SystemActivityProfiler
import de.laser.system.SystemProfiler
import de.laser.system.SystemSetting
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import grails.web.Action
import groovy.json.JsonOutput
import groovy.xml.MarkupBuilder
import org.hibernate.SessionFactory
import org.quartz.JobKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.transaction.TransactionStatus

import javax.servlet.ServletOutputStream
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.sql.Timestamp

@Secured(['IS_AUTHENTICATED_FULLY'])
class YodaController {

    SessionFactory sessionFactory

    def yodaService
    def cacheService
    def statsSyncService
    def dataloadService
    def globalSourceSyncService
    def contextService
    def dashboardDueDatesService
    StatusUpdateService statusUpdateService
    SystemService systemService
    FinanceService financeService
    def quartzScheduler
    def identifierService
    def deletionService
    def surveyUpdateService
    def subscriptionService
    def exportService

    @Secured(['ROLE_YODA'])
    @Transactional
    def index() {
        Map result = [:]
        result
    }

    @Secured(['ROLE_YODA'])
    def erms2362() {
        redirect controller: 'migrations', action: 'erms2362', params: params
    }

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_ADM", "ROLE_ORG_EDITOR")
    })
    def demo() {
        Map result = [:]

        result.user = contextService.getUser()
        result.roles = result.user.roles
        result.affiliations = result.user.affiliations

        result.check1 = "INST_ADMx: " + result.user.hasAffiliation("INST_ADMx")
        result.check2 = "INST_ADM: " + result.user.hasAffiliation("INST_ADM")
        result.check3 = "INST_EDITOR: " + result.user.hasAffiliation("INST_EDITOR")
        result.check4 = "INST_USER: " + result.user.hasAffiliation("INST_USER")

        result.q1 = User.executeQuery('select u from User u where u.accountLocked = true and u.id < 4')
        result.q2 = User.executeQuery('select u from User u where u.accountLocked != true and u.id < 4')

        result.q3 = User.executeQuery('select u from User u where u.accountLocked = false and u.id < 4')
        result.q4 = User.executeQuery('select u from User u where u.accountLocked != false and u.id < 4')

        //result.q5 = User.executeQuery('select u from User u where u.accountLocked is null and u.id < 4')
        result.q6 = User.executeQuery('select u from User u where u.accountLocked is not null and u.id < 4')

        result.numberOfActiveUsers = systemService.getNumberOfActiveUsers()

        def userCache = contextService.getCache('yoda/test1', ContextService.USER_SCOPE)
        def orgCache  = contextService.getCache('yoda/test2', ContextService.ORG_SCOPE)

        userCache.put('X', 123)
        userCache.put('Y', 456)

        orgCache.put('A', 123)
        orgCache.put('B', 456)

        Map<String, Object> map1 = [
                token       : "Quellensteuer-Befreiung",
                category    : PropertyDefinition.SUB_PROP,
                type        : RefdataValue.CLASS,
                rdc         : RDConstants.Y_N_O,
                tenant      : contextService.getOrg().globalUID,
                i10n        : [
                    name_de: "Quellensteuer-Befreiung",
                    name_en: "Quellensteuer-Befreiung",
                    expl_de: "Hat der Anbieter für dieses Produkt eine Befreiung der Quellensteuer erwirkt?",
                    expl_en: "Hat der Anbieter für dieses Produkt eine Befreiung der Quellensteuer erwirkt?"
                ]
        ]

        Map<String, Object> map2 = [
                token       : "BGA",
                category    : PropertyDefinition.ORG_PROP,
                type        : RefdataValue.CLASS,
                rdc         : RDConstants.Y_N,
                tenant      : contextService.getOrg().globalUID,
                i10n        : [
                        name_de: "BGA",
                        name_en: "BGA",
                        expl_de: "Betrieb gewerblicher Art",
                        expl_en: "Betrieb gewerblicher Art"
                ]
        ]

        Map<String, Object> map3 = [
                token       : "EGP Nr.",
                category    : PropertyDefinition.ORG_PROP,
                type        : "java.lang.Integer",
                tenant      : contextService.getOrg().globalUID,
                i10n        : [
                        name_de: "EGP Nr.",
                        name_en: "EGP Nr.",
                        expl_de: "ID für das SAP System des rechtlichen Trägers",
                        expl_en: "ID für das SAP System des rechtlichen Trägers"
                ]
        ]

        Set<PropertyDefinition> privatePropertyDefMaps = [
                PropertyDefinition.construct(map1),
                PropertyDefinition.construct(map2),
                PropertyDefinition.construct(map3)
        ]

        println privatePropertyDefMaps


        println "-----------------------------------------------------------------------------"

        def t1 = System.currentTimeMillis()
        def test1 = Subscription.executeQuery("select s from Subscription s where s.status.value = 'Current'")

        println test1.size() + ' Matches > ' + (System.currentTimeMillis() - t1)

        def t2 = System.currentTimeMillis()
        def test2 = Subscription.executeQuery('select s from Subscription s where s.status = :state', [state: RDStore.SUBSCRIPTION_CURRENT])

        println test2.size() + ' Matches > ' + (System.currentTimeMillis() - t2)

        println "-----------------------------------------------------------------------------"

        def t3 = System.currentTimeMillis()
        def test3 = Subscription.executeQuery("select s from Subscription s where s.status.value = 'Current'")

        println test1.size() + ' Matches > ' + (System.currentTimeMillis() - t3)

        def t4 = System.currentTimeMillis()
        def test4 = Subscription.executeQuery('select s from Subscription s where s.status = :state', [state: RDStore.SUBSCRIPTION_CURRENT])

        println test4.size() + ' Matches > ' + (System.currentTimeMillis() - t4)

        println "-----------------------------------------------------------------------------"

        // dataConsistencyService.checkBooleanFields()

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def demo2() {
        redirect action: 'demo'
    }
    @DebugAnnotation(test='hasAffiliationOR("INST_USER", "ROLE_XY")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliationOR("INST_USER", "ROLE_XY") })
    def demo3() {
        redirect action: 'demo'
    }
    @DebugAnnotation(test='hasAffiliationAND("INST_USER", "ROLE_XY")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliationAND("INST_USER", "ROLE_XY") })
    def demo4() {
        redirect action: 'demo'
    }

    @Secured(['ROLE_YODA'])
    def appConfig() {
        Map result = [:]

        result.blacklist = [
                'jira', 'dataSource', 'dataSource.password'
        ]
        result.editable = true

        result.currentconf = grails.util.Holders.config

        result
    }

    @Secured(['ROLE_YODA'])
    def quartzInfo() {
        Map result = [:]

        // DEBUG ONLY: changeNotificationService.aggregateAndNotifyChanges()

        result.currentConfig   = grails.util.Holders.config
        result.quartzScheduler = quartzScheduler

        def groups = [:]
        for (String groupName : quartzScheduler.getJobGroupNames()) {
            def group = []

            for (JobKey key : quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                def clazz = Class.forName(key.getName())
                def cf  = clazz.configFlags

                def triggers = quartzScheduler.getTriggersOfJob(key)
                def nft = triggers.collect{ it.nextFireTime ?: null }

                def getUsedServices = { clz ->
                    clz.getDeclaredFields().findAll{ it.getName().endsWith('Service')}.collect{ it.getName().capitalize() }
                }
                def services = getUsedServices(clazz)

                Map map = [
                        name: clazz.simpleName,
                        configFlags: cf.join(', '),
                        services: services,
                        nextFireTime: nft ? nft.get(0)?.toTimestamp() : '',
                        running: applicationContext.getBean(key.getName()).isRunning(),
                        available: applicationContext.getBean(key.getName()).isAvailable()
                ]

                def crx = triggers.collect{ it.hasProperty('cronEx') ? it.cronEx : null }

                if (crx) {
                    map << ['cronEx': crx.get(0).cronExpression]
                }
                group << map
            }

            groups << ["${groupName}" : group.sort{ a, b -> (a.name < b.name ? -1 : 1)}]
        }
        result.quartz = groups
        result
    }

    @Secured(['ROLE_YODA'])
    def cacheInfo() {
        Map result = [:]

        result.grailsApp = grailsApplication
        result.appContext = getApplicationContext()

        result.hibernateSession = sessionFactory

        result.ehcacheManager = cacheService.getCacheManager()

        if (params.cmd?.equals('clearCache')) {
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

            redirect controller: 'yoda', action: 'cacheInfo', params: params
        }

        result
    }

    @Secured(['ROLE_YODA'])
    def activityProfiler() {
        Map result = [:]

        Map<String, Object> activity = [:]

        // gathering data

        List<Timestamp> dayDates = SystemActivityProfiler.executeQuery(
                "select date_trunc('day', dateCreated) as day from SystemActivityProfiler group by date_trunc('day', dateCreated), dateCreated order by dateCreated desc"
        )
        dayDates.unique().take(30).each { it ->
            List<Timestamp, Timestamp, Timestamp, Integer, Integer, Double> slots = SystemActivityProfiler.executeQuery(
                    "select date_trunc('hour', dateCreated), min(dateCreated), max(dateCreated), min(userCount), max(userCount), avg(userCount) " +
                            "  from SystemActivityProfiler where date_trunc('day', dateCreated) = :day " +
                            " group by date_trunc('hour', dateCreated) order by min(dateCreated), max(dateCreated)",
                    [day: it])

            String dayKey = (DateUtils.getSDF_NoTime()).format(new Date(it.getTime()))
            activity.put(dayKey, [])

            slots.each { hour ->
                activity[dayKey].add([
                        (DateUtils.getSDF_OnlyTime()).format(new Date(hour[0].getTime())),   // time.start
                        (DateUtils.getSDF_OnlyTime()).format(new Date(hour[1].getTime())),   // time.min
                        (DateUtils.getSDF_OnlyTime()).format(new Date(hour[2].getTime())),   // time.max
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
                    series1.putAt(indexOf, val[3])
                    series2.putAt(indexOf, val[4]- val[3]) // stackBars: true

                    averages[indexOf] = averages[indexOf] + val[5]
                }
            }
            activityMatrix.put(dayKey, [series1, series2])
        }

        // averages

        for(int i=0; i<averages.size(); i++) {
            averages[i] = (averages[i]/activity.size())
        }

        activityMatrix.putAt('Ø', [(0..23).collect{ 0 }, averages])

        result.labels = labels
        result.activity = activityMatrix

        result
    }

    @Secured(['ROLE_YODA'])
    def appThreads() {
        return [:]
    }

    @Secured(['ROLE_YODA'])
    def systemProfiler() {
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
                "select sp.uri, max(sp.ms) as max, avg(sp.ms) as avg, count(sp.ms) as counter from SystemProfiler sp where sp.archive = :arc group by sp.uri",
                [arc: result.archive]
        ).sort{it[2]}.reverse()

        result.contextStats = SystemProfiler.executeQuery(
                "select sp.uri, max(sp.ms) as max, avg(sp.ms) as avg, ctx.id, count(ctx.id) as counter from SystemProfiler sp join sp.context as ctx where sp.archive = :arc and ctx is not null group by sp.uri, ctx.id",
                [arc: result.archive]
        ).sort{it[2]}.reverse()

        result
    }

    @Secured(['ROLE_YODA'])
    def timelineProfiler() {
        Map<String, Object> result = [:]

        List<String> allUri = SystemProfiler.executeQuery('select distinct(uri) from SystemProfiler')

        result.globalTimeline           = [:]
        result.globalTimelineStartDate  = (new Date()).minus(30)
        result.globalTimelineDates      = (25..0).collect{ (DateUtils.getSDF_NoTime()).format( (new Date()).minus(it) ) }

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

            ordered[uri] = count
        }
        result.globalTimelineOrder = ordered.sort{ e,f -> f.value <=> e.value }

        result
    }

    //@Cacheable('message')
    @Secured(['ROLE_ADMIN'])
    def appInfo() {
        Map<String, Object> result = [:]

        result.statsSyncService = [:]
        result.dataloadService = [:]
        result.globalSourceSyncService = [:]
        result.globalSourceSyncService.running = globalSourceSyncService.running
        result.statsSyncService.running = statsSyncService.running
        result.statsSyncService.submitCount = statsSyncService.submitCount
        result.statsSyncService.completedCount = statsSyncService.completedCount
        result.statsSyncService.newFactCount = statsSyncService.newFactCount
        result.statsSyncService.totalTime = statsSyncService.totalTime
        result.statsSyncService.threads = statsSyncService.THREAD_POOL_SIZE
        result.statsSyncService.queryTime = statsSyncService.queryTime
        result.statsSyncService.activityHistogram = statsSyncService.activityHistogram
        result.statsSyncService.syncStartTime = statsSyncService.syncStartTime
        result.statsSyncService.syncElapsed = statsSyncService.syncElapsed
        result.dataloadService.update_running = dataloadService.update_running
        result.dataloadService.lastIndexUpdate = dataloadService.lastIndexUpdate
        result.esinfos = FTControl.list()

        def dbmQuery = (sessionFactory.currentSession.createSQLQuery(
                'SELECT filename, id, dateexecuted from databasechangelog order by orderexecuted desc limit 1'
        )).list()
        result.dbmVersion = dbmQuery.size() > 0 ? dbmQuery.first() : ['unkown', 'unkown', 'unkown']

        result
    }

    @Secured(['ROLE_YODA'])
    def appSecurity() {
        Map<String, Object> result = [:]
        Map<String, Object> cList = [:]

        // filter dynamic getter/setter
        List<String> blacklist = [
                'getAllowedMethods',
                'setAllowedMethods',
                'getStaticApplicationContext'
        ]

        grailsApplication.controllerClasses.toList().each { controller ->
            Class controllerClass = controller.clazz
            if (controllerClass.name.startsWith('de.laser')) {
                Map<String, Object> mList = [public:[:], others:[:]]

                controllerClass.declaredMethods.each { Method method ->
                    int mods = method.getModifiers()
                    if ( ! Modifier.isSynthetic(mods) && (
                            (method.getAnnotation(Action) || Modifier.isPrivate(mods)) ||
                            (Modifier.isStatic(mods) && ! (method.name in blacklist))
                    ) ) {
                        String mKey = method.name
                        Map<String, Object> mInfo = [:]

                        Annotation da = method.getAnnotation(DebugAnnotation)
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
                                if (da.ctrlService() == 2) {
                                    mInfo.refactoring = 'done'
                                }
                            }
                            if (da.wtc()) {
                                mInfo.wtc = da.wtc()
                                if (da.wtc() == 2) {
                                    mInfo.refactoring = 'done'
                                }
                            }
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
                                mList.others << ["${mKey}": mInfo]
                            }
                        }
                        else {
                            mList.public << ["${mKey}": mInfo]
                        }
                    }
                }

                String cKey = "${controllerClass.simpleName}"
                if (controllerClass.getAnnotation(Deprecated)) {
                    cKey ="${controllerClass.simpleName} <em>*</em>"
                }
                cList<< ["${cKey}": [
                        'secured': controllerClass.getAnnotation(Secured)?.value(),
                        'methods': [
                                public: mList.public.sort{it.key},
                                others: mList.others.sort{it.key}
                            ]
                        ]
                ]
            }
        }
        result.controller = cList.sort{it.key}
        result
    }

    @Secured(['ROLE_YODA'])
    def userMatrix() {
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

    @Secured(['ROLE_YODA'])
    def userRoleDefinitions() {
        Map<String, Object> result = [:]
        result.matrix = [:]
        result
    }

    @Secured(['ROLE_YODA'])
    def pendingChanges() {

        // TODO: DEBUG ONLY
        Map<String, Object> result = [:]

        result.pending = PendingChange.executeQuery(
                "SELECT pc FROM PendingChange pc WHERE pc.status IS NULL ORDER BY pc.id DESC",
        )
        result
    }

    @Secured(['ROLE_YODA'])
    def retriggerPendingChanges() {
        log.debug("match IssueEntitlements to TIPPs ...")
        flash.message = "Pakete werden nachgehalten ..."
        statusUpdateService.retriggerPendingChanges(params.packageUUID)
        redirect controller: 'home', action: 'index'
    }

    @Secured(['ROLE_YODA'])
    def getTIPPsWithoutGOKBId() {
        log.debug("delete TIPPs without GOKb-ID")
        yodaService.getTIPPsWithoutGOKBId()
    }

    @Secured(['ROLE_YODA'])
    def purgeTIPPsWithoutGOKBId() {
        def toDelete = JSON.parse(params.toDelete)
        def toUUIDfy = JSON.parse(params.toUUIDfy)
        if(params.doIt == "true") {
            yodaService.purgeTIPPsWihtoutGOKBId(toDelete,toUUIDfy)
            redirect(url: request.getHeader('referer'))
        }
        else {
            flash.message = "Betroffene TIPP-IDs wären vereinigt worden: ${toDelete} und folgende hätten einen fehlenden Wert erhalten: ${toUUIDfy}"
            redirect action: 'getTIPPsWithoutGOKBId'
        }
    }

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

    @Secured(['ROLE_YODA'])
    def esIndexUpdate() {
        log.debug("manual start full text index")
        dataloadService.updateFTIndexes()
        log.debug("redirecting to home ..")

        redirect controller: 'home'
    }

    @Secured(['ROLE_YODA'])
    def fullReset() {

       log.debug("Clear ES")
       dataloadService.clearDownAndInitES()

        log.debug("redirecting to home ..")

        redirect controller:'home'
    }

    @Secured(['ROLE_YODA'])
    def killDataloadService() {

        log.debug("kill DataloadService")
        dataloadService.killDataloadService()

        log.debug("redirecting to home ..")

        redirect controller:'home'
    }

    @Secured(['ROLE_YODA'])
    def checkESElementswithDBElements() {
        log.debug("checkESElementswithDBElements")
        dataloadService.checkESElementswithDBElements()
        log.debug("redirecting to home ..")

        redirect controller: 'home'
    }

    @Secured(['ROLE_YODA'])
    def globalSync() {
        log.debug("start global sync ..")
        globalSourceSyncService.startSync()
        log.debug("done global sync ..")

        redirect controller: 'package'
    }

    @Secured(['ROLE_YODA'])
    def globalMultithreadSync() {
        log.debug("start global sync ..")
        globalSourceSyncService.startMultithreadSync()
        log.debug("done global sync ..")

        redirect controller: 'package'
    }

    @Secured(['ROLE_YODA'])
    def manageGlobalSources() {
        Map<String, Object> result = [:]
        log.debug("manageGlobalSources ..")
        result.sources = GlobalRecordSource.list()

        result
    }

    @Secured(['ROLE_YODA'])
    def manageESSources() {
        Map<String, Object> result = [:]
        log.debug("manageESSources ..")
        result.sources = ElasticsearchSource.list()
        result.editable = true

        result
    }

    @Secured(['ROLE_YODA'])
    def manageFTControl() {
        Map<String, Object> result = [:]
        log.debug("manageFTControle ..")
        result.ftControls = FTControl.list()
        result.dataloadService = [:]
        result.dataloadService.lastIndexUpdate = dataloadService.lastIndexUpdate
        result.dataloadService.update_running = dataloadService.update_running
        result.dataloadService.lastIndexUpdate = dataloadService.lastIndexUpdate
        result.editable = true

        result
    }

    @Secured(['ROLE_YODA'])
    def newESSource() {
        Map<String, Object> result = [:]
        log.debug("manageGlobalSources ..")

        /*result.newSource = ElasticsearchSource.findByIdentifier(params.identifier) ?: new ElasticsearchSource(
                identifier:params.identifier,
                name:params.name,
                host:params.uri)

        result.newSource.save(flush:true)*/

        redirect action:'manageGlobalSources'
    }

    @Secured(['ROLE_YODA'])
    def deleteGlobalSource() {
        GlobalRecordSource.removeSource(params.long('id'))

        redirect(action:'manageGlobalSources')
    }

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

    @Secured(['ROLE_YODA'])
    @Transactional
    def migrateNatStatSettings() {
        Map<String, Object> result = [:]
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
                'select op from OrgProperty op join op.type pd where pd.descr = :orgConf '
                + 'and ( pd.name = \'API Key\' or pd.name = \'RequestorID\' ) and op.tenant = :context and op.isPublic = false',
                [orgConf: PropertyDefinition.ORG_CONF, context: contextOrg]).each{
            it.delete()
        }

        redirect action:'dashboard'
    }

    @Secured(['ROLE_YODA'])
    def settings() {
        Map<String, Object> result = [:]
        result.settings = SystemSetting.executeQuery('select s from SystemSetting s where s.name != \'MaintenanceMode\' order by s.name asc')
        result
    }

    @Secured(['ROLE_YODA'])
    @Transactional
    def migrateCollectiveSubscriptions() {
        Map<String, Object> result = [:]

        result.subRoles = Subscription.executeQuery(
            'select sub, role from Subscription sub join sub.orgRelations role, OrgSetting os ' +
            '  where os.org = role.org ' +
            '  and role.roleType.value like \'Subscriber\' ' +
            '  and os.key like \'CUSTOMER_TYPE\' and os.roleValue.authority like \'ORG_INST_COLLECTIVE\' '
        )

        result.subConsRoles = Subscription.executeQuery(
                'select sub, role from Subscription sub join sub.orgRelations role, OrgSetting os ' +
                        '  where os.org = role.org ' +
                        '  and role.roleType.value like \'Subscriber_Consortial\' ' +
                        '  and os.key like \'CUSTOMER_TYPE\' and os.roleValue.authority like \'ORG_INST_COLLECTIVE\' ' +
                        '    and not exists (select check from OrgRole check where check.org = role.org and check.sub = sub ' +
                        '    and check.roleType.value like \'Subscription Collective\' )'
        )

        if (params.cmd == 'migrate') {
            result.subRoles.each{ so ->
                Subscription sub = so[0]
                OrgRole role 	 = so[1]

				if (sub._getCalculatedType() == Subscription.TYPE_LOCAL) {
					role.setRoleType(RDStore.OR_SUBSCRIPTION_COLLECTIVE)
					role.save()

					sub.type = RDStore.SUBSCRIPTION_TYPE_LOCAL
					sub.save()
				}
            }

            /*
            todo: IGNORED for 0.20
            */
            result.subConsRoles.each{ so ->
                Subscription sub = so[0]
                OrgRole role 	 = so[1]

                if (sub._getCalculatedType() == Subscription.TYPE_PARTICIPATION) {
                    OrgRole newRole = new OrgRole(
                            org: role.org,
                            sub: sub,
                            roleType: RDStore.OR_SUBSCRIPTION_COLLECTIVE
                    )
                    newRole.save()
                }
            }

        }

        result
    }

    @Secured(['ROLE_YODA'])
    def migratePackageIdentifiers() {
        IdentifierNamespace isilPaketsigel = IdentifierNamespace.findByNs('ISIL_Paketsigel')
        Set<Identifier> idList = Identifier.executeQuery("select ident from Identifier ident where ident.pkg != null and lower(ident.ns.ns) = 'isil'")
        if(idList) {
            // TODO [ticket=1789]
            Identifier.executeUpdate("update Identifier ident set ident.ns = :isilPaketsigel where ident.pkg != null and lower(ident.ns.ns) = 'isil'",[isilPaketsigel: isilPaketsigel])
            //Identifier.executeUpdate("update IdentifierOccurrence io set io.identifier.ns = :isilPaketsigel where io.pkg != null and lower(io.identifier.ns.ns) = 'isil'",[isilPaketsigel: isilPaketsigel])
            flash.message = "Changes performed on ${idList.size()} package identifiers ..."
        }
        redirect controller: 'home'
    }

    @Secured(['ROLE_YODA'])
    def assignNoteOwners() {
        statusUpdateService.assignNoteOwners()
        redirect controller: 'home'
    }

    @Secured(['ROLE_YODA'])
    @Transactional
    def toggleBoolSetting() {
        def s = SystemSetting.findByName(params.setting)
        if (s) {
            if (s.tp == SystemSetting.CONTENT_TYPE_BOOLEAN) {
                if (s.value == 'true')
                    s.value = 'false'
                else
                    s.value = 'true'
            }
            s.save()
        }

        redirect action:'settings'
    }

    @Secured(['ROLE_YODA'])
    @Transactional
    def toggleMailSent() {
        Map<String, Object> result = [:]

        if (params.mailSent) {
            if (params.mailSent == 'true')
                grailsApplication.config.grails.mail.disabled = false
            else
                grailsApplication.config.grails.mail.disabled = true
        }

        redirect action:'settings'
    }

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
                costItems = CostItem.findAllByOwnerAndCostItemStatusNotEqual(Org.get(owner),RDStore.COST_ITEM_DELETED)
            }

            costItems.each {
                def costItem = [:]
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
                    //costItem.owner.ownerType = it.orgType?.value
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

    @Secured(['ROLE_YODA'])
    def dueDates_updateDashboardDB(){
        flash.message = "DB wird upgedatet...<br/>"
        dashboardDueDatesService.takeCareOfDueDates(true, false, flash)
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def dueDates_sendAllEmails() {
        flash.message = "Emails mit fälligen Terminen werden vesandt...<br/>"
        dashboardDueDatesService.takeCareOfDueDates(false, true, flash)
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def subscriptionCheck(){
        flash.message = "Lizenzen und Verträge werden upgedatet"
        statusUpdateService.subscriptionCheck()
        statusUpdateService.licenseCheck()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def surveyCheck(){
        flash.message = "Umfragen werden upgedatet"
        surveyUpdateService.surveyCheck()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def startDateCheck(){
        if(statusUpdateService.startDateCheck())
            flash.message = "Lizenzen ohne Startdatum verlieren ihren Status ..."
        else
            flash.message = "Lizenzen ohne Startdatum haben bereits ihren Status verloren!"
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def updateTaxRates(){
        flash.message = "Kosten werden in das neue Steuermodell überführt ..."
        financeService.updateTaxRates()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    Map checkIssueEntitlementPackages() {
        Map<String,List<IssueEntitlement>> result = [:]
        result.ieList = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp.pkg tp where not exists (select sp.pkg from SubscriptionPackage sp where sp.subscription = ie.subscription and sp.pkg = tp)')
        result
    }

    @Secured(['ROLE_YODA'])
    @Transactional
    def createSubscriptionPackagesFromIssueEntitlements() {
        List<IssueEntitlement> toLink = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp.pkg tp where not exists (select sp.pkg from SubscriptionPackage sp where sp.subscription = ie.subscription and sp.pkg = tp)')
        Set<Map> entries = []
        toLink.each { issueEntitlement ->
            entries << [subscription: issueEntitlement.subscription,pkg: issueEntitlement.tipp.pkg]
        }
        if(params.doIt == 'true') {
            List<String> errorMsg = []
            entries.each { entry ->
                SubscriptionPackage sp = new SubscriptionPackage(entry)
                if(!sp.save())
                    errorMsg << sp.errors
            }
            if(errorMsg)
                flash.error = "Folgende Fehler sind aufgetreten: <ul><li>${errorMsg.join('</li><li>')}</li></ul>"
            else flash.message = "Lizenzen wurden erfolgreich mit Paketen verknüpft"
        }
        else {
            flash.message = "Folgende Lizenzen und Pakete hätte es getroffen: <ul><li>${entries.join('</li><li>')}</li></ul>"
        }
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def updateCustomerType(){
        RefdataValue cons = RefdataValue.getByValueAndCategory('Consortium', RDConstants.ORG_TYPE)
        RefdataValue inst = RefdataValue.getByValueAndCategory('Institution', RDConstants.ORG_TYPE)

        List<Org> consOrgs = Org.executeQuery("SELECT o from Org o join o.orgType ot where ot = :cons", [cons: cons])
        List<Org> instOrgs = Org.executeQuery("SELECT o from Org o join o.orgType ot where ot = :inst", [inst: inst])

        int consCount = 0
        consOrgs.each{ o ->
            def oss = OrgSetting.get(o, OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss == OrgSetting.SETTING_NOT_FOUND) {
                log.debug ('Setting customer type for org: ' + o.id)
                OrgSetting.add(o, OrgSetting.KEYS.CUSTOMER_TYPE, Role.findByAuthorityAndRoleType('ORG_CONSORTIUM', 'org'))
                consCount++
            }
        }

        int instCount = 0
        instOrgs.each{ o ->
            if (o.setDefaultCustomerType()) { instCount++ }
        }

        flash.message = "Kundentyp wurde für ${consCount} Konsortien und ${instCount} Teilnehmer gesetzt."
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    @Transactional
    def insertEditUris() {
        //ERMS-1758
        List<ApiSource> apiSources = ApiSource.findAllByEditUrlIsNull()
        List<GlobalRecordSource> globalRecordSources = GlobalRecordSource.findAllByEditUriIsNull()
        apiSources.each { ApiSource aps ->
            aps.editUrl = aps.baseUrl
            aps.save()
        }
        globalRecordSources.each { GlobalRecordSource grs ->
            grs.editUri = grs.uri
            grs.save()
        }
        flash.message = "${apiSources.size()} ApiSources und ${globalRecordSources.size()} GlobalRecordSources angepasst!"
        redirect controller: 'home', action: 'index'
    }

    @Secured(['ROLE_YODA'])
    def generateBatchUID() {
        flash.message = "Setze UID für Domänen ..."
        identifierService.checkNullUIDs()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def makeshiftLaserOrgExport() {
        log.info("Export institutions in XML, structure follows LAS:eR-DB-structure")
        try {
            File dir = new File(ConfigUtils.getBasicDataPath())
            if(!dir.exists()) {
                dir.mkdir()
            }
            new File("${ConfigUtils.getBasicDataPath()}${ConfigUtils.getBasicDataFileName()}").withWriter { writer ->
                MarkupBuilder orgDataBuilder = new MarkupBuilder(writer)
                orgDataBuilder.data {
                    organisations {
                        Org.getAll().each { obj ->
                            try {
                                Org o = (Org) obj
                                org {
                                    globalUID(o.globalUID)
                                    name(o.name)
                                    shortname(o.shortname)
                                    shortcode(o.shortcode)
                                    sortname(o.sortname)
                                    url(o.url)
                                    urlGov(o.urlGov)
                                    importSource(o.importSource)
                                    lastImportDate(o.lastImportDate)
                                    gokbId(o.gokbId)
                                    comment(o.comment)
                                    ipRange(o.ipRange)
                                    scope(o.scope)
                                    dateCreated(o.dateCreated)
                                    lastUpdated(o.lastUpdated)
                                    categoryId(o.categoryId)
                                    sector {
                                        if(o.sector) {
                                            rdc(o.sector.owner.desc)
                                            rdv(o.sector.value)
                                        }
                                    }
                                    status {
                                        if(o.status) {
                                            rdc(o.status.owner.desc)
                                            rdv(o.status.value)
                                        }
                                    }
                                    membership {
                                        if(o.membership) {
                                            rdc(o.membership.owner.desc)
                                            rdv(o.membership.value)
                                        }
                                    }
                                    countryElem {
                                        if(o.country) {
                                            rdc(o.country.owner.desc)
                                            rdv(o.country.value)
                                        }
                                    }
                                    region {
                                        if(o.region) {
                                            rdc(o.region.owner.desc)
                                            rdv(o.region.value)
                                        }
                                    }
                                    libraryNetwork {
                                        if(o.libraryNetwork) {
                                            rdc(o.libraryNetwork.owner.desc)
                                            rdv(o.libraryNetwork.value)
                                        }
                                    }
                                    funderType {
                                        if(o.funderType) {
                                            rdc(o.funderType.owner.desc)
                                            rdv(o.funderType.value)
                                        }
                                    }
                                    libraryType {
                                        if(o.libraryType) {
                                            rdc(o.libraryType.owner.desc)
                                            rdv(o.libraryType.value)
                                        }
                                    }
                                    costConfigurations {
                                        CostItemElementConfiguration.findAllByForOrganisation(o).each { ciecObj ->
                                            CostItemElementConfiguration ciec = (CostItemElementConfiguration) ciecObj
                                            costConfiguration {
                                                rdc(ciec.costItemElement.owner.desc)
                                                rdv(ciec.costItemElement.value)
                                                elementSign {
                                                    rdc(ciec.elementSign.owner.desc)
                                                    rdv(ciec.elementSign.value)
                                                }
                                            }
                                        }
                                    }
                                    ids {
                                        o.ids.each { idObj ->
                                            // TODO [ticket=1789]
                                            //IdentifierOccurrence idOcc = (IdentifierOccurrence) idObj
                                            id (namespace: idObj.ns.ns, value: idObj.value)
                                        }
                                    }
                                    //outgoing/ingoingCombos: assembled in branch combos
                                    /*
                                    links {
                                        o.links.each { linkObj ->
                                            linkElem {
                                                OrgRole link = (OrgRole) linkObj
                                                org(link.org.globalUID)
                                                roleType {
                                                    rdc(link.roleType.owner.desc)
                                                    rdv(link.roleType.value)
                                                }
                                                if(link.title) title(link.title.globalUID)
                                                if(link.startDate) startDate(link.startDate)
                                                if(link.endDate) endDate(link.endDate)
                                            }
                                        }
                                    }
                                    */
                                    //prsLinks, affiliations, contacts and addresses done on own branches respectively
                                    /*
                                    customProperties {
                                        o.customProperties.each { cpObj ->
                                            OrgCustomProperty customProp = (OrgCustomProperty) cpObj
                                            customProperty {
                                                org(customProp.owner.globalUID)
                                                name(customProp.type.name)
                                                value(customProp.value)
                                            }
                                        }
                                    }
                                    privateProperties {
                                        o.privateProperties.each { ppObj ->
                                            OrgPrivateProperty privateProp = (OrgPrivateProperty) ppObj
                                            privateProperty {
                                                org(privateProp.owner.globalUID)
                                                name(privateProp.type.name)
                                                value(privateProp.value)
                                            }
                                        }
                                    }
                                    */
                                    orgTypes {
                                        o.orgType.each { ot ->
                                            orgType {
                                                rdc(ot.owner.desc)
                                                rdv(ot.value)
                                            }
                                        }
                                    }
                                    settings {
                                        List<OrgSetting> os = OrgSetting.findAllByOrg(o)
                                        os.each { st ->
                                            switch(st.key.type) {
                                                case RefdataValue:
                                                    if(st.rdValue) {
                                                        setting {
                                                            name(st.key)
                                                            rdValue {
                                                                rdc(st.rdValue.owner.desc)
                                                                rdv(st.rdValue.value)
                                                            }
                                                        }
                                                    }
                                                    break
                                                case Role:
                                                    if(st.roleValue) {
                                                        setting {
                                                            name(st.key)
                                                            roleValue(st.roleValue.authority)
                                                        }
                                                    }
                                                    break
                                                default: setting{
                                                    name(st.key)
                                                    value(st.getValue())
                                                    }
                                                    break
                                            }
                                        }
                                    }
                                }
                            }
                            catch (ClassCastException e) {
                                log.error("Help! ${obj} is a non-organisation object among orgs!!!")
                            }
                        }
                    }
                    affiliations {
                        List affiliations = UserOrg.getAll()
                        affiliations.each { affObj ->
                            UserOrg userOrg = (UserOrg) affObj
                            affiliation {
                                user(userOrg.user.username)
                                org(userOrg.org.globalUID)
                                status(userOrg.status)
                                if(userOrg.formalRole) {
                                    formalRole(userOrg.formalRole.authority)
                                }
                                if(userOrg.dateActioned) {
                                    dateActioned(userOrg.dateActioned)
                                }
                                if(userOrg.dateRequested) {
                                    dateRequested(userOrg.dateRequested)
                                }
                            }
                        }
                    }
                    combos {
                        Combo.getAll().each { obj ->
                            try {
                                Combo c = (Combo) obj
                                if(c.type) {
                                    combo {
                                        status {
                                            if (c.status) {
                                                rdc(c.status.owner.desc)
                                                rdv(c.status.value)
                                            }
                                        }
                                        type{
                                            rdc(c.type.owner.desc)
                                            rdv(c.type.value)
                                        }
                                        fromOrg(c.fromOrg.globalUID)
                                        toOrg(c.toOrg.globalUID)
                                    }
                                }
                            }
                            catch (ClassCastException e) {
                                log.error("Help! ${obj} is a non-combo object amoung combos!!!")
                            }
                        }
                    }
                    persons {
                        List orgPersons = PersonRole.findAllByOrgIsNotNull()
                        orgPersons.each { prsObj ->
                            person {
                                Person p = prsObj.prs
                                log.debug("now processing ${p.id}")
                                globalUID(p.globalUID)
                                title(p.title)
                                firstName(p.first_name)
                                middleName(p.middle_name)
                                lastName(p.last_name)
                                if(p.tenant)
                                    tenant(p.tenant.globalUID)
                                if(p.gender) {
                                    gender {
                                        rdc(p.gender.owner.desc)
                                        rdv(p.gender.value)
                                    }
                                }
                                if(p.isPublic) {
                                    isPublic { // ERMS-1562 ---> fixed in ERMS-1572
                                        'Yes'
                                    }
                                }
                                if(p.contactType) {
                                    contactType {
                                        rdc(p.contactType.owner.desc)
                                        rdv(p.contactType.value)
                                    }
                                }
                                if(p.roleType) {
                                    roleType {
                                        rdc(p.roleType.owner.desc)
                                        rdv(p.roleType.value)
                                    }
                                }
                            }
                        }
                    }
                    personRoles {
                        List orgPersonRoles = PersonRole.findAllByOrgIsNotNull()
                        orgPersonRoles.each { link ->
                            personRole {
                                org(link.org.globalUID)
                                prs(link.prs.globalUID)
                                if(link.positionType) {
                                    positionType {
                                        rdc(link.positionType.owner.desc)
                                        rdv(link.positionType.value)
                                    }
                                }
                                if(link.functionType) {
                                    functionType {
                                        rdc(link.functionType.owner.desc)
                                        rdv(link.functionType.value)
                                    }
                                }
                                if(link.responsibilityType) {
                                    responsibilityType {
                                        rdc(link.responsibilityType.owner.desc)
                                        rdv(link.responsibilityType.value)
                                    }
                                }
                            }
                        }
                    }
                    users {
                        User.getAll().each { userObj ->
                            user {
                                User u = (User) userObj
                                username(u.username)
                                display(u.display)
                                password(u.password)
                                email(u.email)
                                shibbScope(u.shibbScope)
                                enabled(u.enabled)
                                accountExpired(u.accountExpired)
                                accountLocked(u.accountLocked)
                                passwordExpired(u.passwordExpired)
                                dateCreated(u.dateCreated)
                                lastUpdated(u.lastUpdated)
                                //affiliations done already on organisations
                                roles {
                                    u.roles.each { rObj ->
                                        UserRole r = (UserRole) rObj
                                        role(r.role.authority)
                                    }
                                }
                                settings {
                                    List<UserSetting> us = UserSetting.findAllByUser(u)
                                    us.each { st ->
                                        switch(st.key.type) {
                                            case Org: setting{
                                                name(st.key)
                                                org(st.orgValue ? st.orgValue.globalUID : ' ')
                                            }
                                                break
                                            case RefdataValue:
                                                if(st.rdValue) {
                                                    setting {
                                                        name(st.key)
                                                        rdValue {
                                                            rdc(st.rdValue.owner.desc)
                                                            rdv(st.rdValue.value)
                                                        }
                                                    }
                                                }
                                                break
                                            default: setting{
                                                name(st.key)
                                                value(st.getValue())
                                            }
                                                break
                                        }
                                    }
                                }
                            }
                        }
                    }
                    addresses {
                        List addresses = Address.executeQuery('select a from Address a where a.prs != null or a.org != null')
                        addresses.each { a ->
                            address {
                                if(a.org) org(a.org.globalUID)
                                if(a.prs) prs(a.prs.globalUID)
                                street1(a.street_1)
                                street2(a.street_2)
                                zipcode(a.zipcode)
                                city(a.city)
                                pob(a.pob)
                                pobZipcode(a.pobZipcode)
                                pobCity(a.pobCity)
                                if(a.region) {
                                    region {
                                        rdc(a.region.owner.desc)
                                        rdv(a.region.value)
                                    }
                                }
                                if(a.country) {
                                    countryElem {
                                        rdc(a.country.owner.desc)
                                        rdv(a.country.value)
                                    }
                                }
                                type {
                                    rdc(a.type.owner.desc)
                                    rdv(a.type.value)
                                }
                                if(a.name) name(a.name)
                                if(a.additionFirst) additionFirst(a.additionFirst)
                                if(a.additionSecond) additionSecond(a.additionSecond)
                            }
                        }
                    }
                    contacts {
                        List contacts = Contact.executeQuery('select c from Contact c where c.org != null or c.prs != null')
                        contacts.each { c ->
                            contact {
                                if(c.org) org(c.org.globalUID)
                                if(c.prs) prs(c.prs.globalUID)
                                content(c.content)
                                contentType {
                                    rdc(c.contentType.owner.desc)
                                    rdv(c.contentType.value)
                                }
                                type {
                                    rdc(c.type.owner.desc)
                                    rdv(c.type.value)
                                }
                            }
                        }
                    }
                }
            }
            flash.message = "Export erfolgreich erstellt, bitte Datei prüfen! Anschließend weiter mit Schritt zwei!"
        }
        catch (IOException e) {
            flash.error = e.printStackTrace()
        }
        redirect(url: request.getHeader('referer'))
    }

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
                        println '-----> deleting subscription: ' + sub.id
                        deletionService.deleteSubscription(sub, false)
                    }
                } catch (Exception e) {
                    println e
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
                        println '-----> deleting license: ' + lic.id
                        deletionService.deleteLicense(lic, false)
                    }
                } catch (Exception e) {
                    println e
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
    @Secured(['ROLE_YODA'])
    def replaceUserSettingDashboardReminderPeriod() {
        Map<String, Object> result = [:]
        User.withTransaction { TransactionStatus status ->
            try {
                def users = User.findAll()
                print users
                users.each { user ->
                    int oldPeriod = 30
                    user.getSetting(REMIND_PERIOD_FOR_LICENSE_PRIVATE_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_LICENSE_CUSTOM_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_ORG_CUSTOM_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_ORG_PRIVATE_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_PERSON_PRIVATE_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_CUSTOM_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_PRIVATE_PROP, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_NOTICEPERIOD, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_SUBSCRIPTIONS_ENDDATE, oldPeriod)
                    user.getSetting(REMIND_PERIOD_FOR_TASKS, oldPeriod)
                }
                result.users = users
                flash.message = 'Das Ersetzen des Usersetting DASHBOARD_REMINDER_PERIOD für alle Benutzer im System war erfolgreich.'
            } catch (Exception ex) {
                status.setRollbackOnly()
                flash.error = 'Es ist ein Fehler aufgetreten beim Ersetzen des Usersetting DASHBOARD_REMINDER_PERIOD: ' + ex.message
                flash.error += '<br /><br /><strong>Es wurde ein Rollback durchgeführt!</strong>'
            }
        }
        result
    }

    @Secured(['ROLE_YODA'])
    def correctCostsInLocalCurrency() {
        Map<String, Object> result = ["costItems":financeService.correctCostsInLocalCurrency(Boolean.valueOf(params.dryRun))]
        result
    }

    @Secured(['ROLE_YODA'])
    def dbmFixPrivateProperties() {
        Map<String, Object> result = [:]

        def opp = OrgProperty.executeQuery(
                "SELECT pp FROM OrgProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        def spp = SubscriptionProperty.executeQuery(
                "SELECT pp FROM SubscriptionProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                "AND pp.isPublic = false " +
                "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                "AND (pp.note IS null OR pp.note = '') "
        )

        def lpp = LicenseProperty.executeQuery(
                "SELECT pp FROM LicenseProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') " +
                        "AND (pp.paragraph IS null OR pp.paragraph = '') "
        )

        def ppp = PersonProperty.executeQuery(
                "SELECT pp FROM PersonProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.isPublic = false " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        if (params.cmd == 'doIt') {
            println opp.collect{ it.id }
            if (opp.size() > 0) {
                OrgProperty.executeUpdate('DELETE FROM OrgProperty opp WHERE opp.id in :idList',
                        [idList: opp.collect { it.id }]
                )
            }

            println spp.collect{ it.id }
            if (spp.size() > 0) {
                SubscriptionProperty.executeUpdate('DELETE FROM SubscriptionProperty spp WHERE spp.id in :idList',
                        [idList: spp.collect { it.id }]
                )
            }

            println lpp.collect{ it.id }
            if (lpp.size() > 0) {
                LicenseProperty.executeUpdate('DELETE FROM LicenseProperty lpp WHERE lpp.id in :idList',
                        [idList: lpp.collect { it.id }]
                )
            }

            println ppp.collect{ it.id }
            if (ppp.size() > 0) {
                PersonProperty.executeUpdate('DELETE FROM PersonProperty ppp WHERE ppp.id in :idList',
                        [idList: ppp.collect { it.id }]
                )
            }
        }

        result.candidates = [OrgProperty: opp, SubscriptionProperty: spp, LicenseProperty: lpp, PersonProperty: ppp]

        render view: 'databaseMigration', model: result
    }

    @Secured(['ROLE_YODA'])
    def cleanUpSurveys() {
        Map<String, Object> result = [:]

        def subSurveys = SurveyConfig.findAllBySubscriptionIsNotNull()
        def count = 0
        def resultMap = []
        subSurveys.each { surConfig ->


            def parentSubscription = surConfig?.subscription
            def parentSubChilds = subscriptionService.getCurrentValidSubChilds(parentSubscription)
            def parentSuccessorSubscription = surConfig?.subscription?._getCalculatedSuccessor()
            //def property = PropertyDefinition.getByNameAndDescr("Perennial term checked", PropertyDefinition.SUB_PROP)
            parentSubChilds.each { sub ->
                if (sub._getCalculatedSuccessor()) {
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
                   /* if (property?.type == RefdataValue.CLASS) {
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
        def output = JsonOutput.toJson(resultMap)

        //println(output)

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

    @Secured(['ROLE_YODA'])
    @Transactional
    def cleanUpSurveyOrgFinishDate() {

        Integer changes = 0
        List<SurveyOrg> surveyOrgs = SurveyOrg.findAllByFinishDateIsNull()

        surveyOrgs.each { surveyOrg ->

            List<SurveyResult> surveyResults = SurveyResult.findAllBySurveyConfigAndParticipant(surveyOrg.surveyConfig, surveyOrg.org)
            List<SurveyResult> surveyResultsFinish = SurveyResult.findAllBySurveyConfigAndParticipantAndFinishDateIsNotNull(surveyOrg.surveyConfig, surveyOrg.org)

            if(surveyResults.size() == surveyResultsFinish.size()){
                surveyOrg.finishDate = surveyResultsFinish[0].finishDate
                surveyOrg.save()
                changes++
            }

        }
        flash.message = "Es wurden ${changes} FinishDate in SurveyOrg geändert!"
        redirect action: 'dashboard'
    }

}
