package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserRole
import de.laser.SystemEvent
import de.laser.domain.SystemProfiler
import de.laser.helper.DebugAnnotation
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.web.Action
import org.hibernate.SessionFactory
import org.quartz.JobKey
import org.quartz.impl.matchers.GroupMatcher
import org.quartz.impl.triggers.SimpleTriggerImpl

import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Secured(['IS_AUTHENTICATED_FULLY'])
class YodaController {

    SessionFactory sessionFactory

    def cacheService
    def springSecurityService
    def statsSyncService
    def dataloadService
    def globalSourceSyncService
    def contextService
    def dashboardDueDatesService
    def subscriptionUpdateService
    def executorService
    def costItemUpdateService
    def quartzScheduler

    static boolean ftupdate_running = false

    @Secured(['ROLE_YODA'])
    def index() {
        redirect action: 'dashboard'
    }

    @Secured(['ROLE_YODA'])
    def dashboard() {
        Map result = [:]

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def demo() {
        Map result = [:]

        result.user = springSecurityService.getCurrentUser()
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

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def demo2() {
        redirect action: 'demo'
    }
    @DebugAnnotation(test='hasAffiliationOR("INST_USER", "ROLE_XY")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliationOR("INST_USER", "ROLE_XY") })
    def demo3() {
        redirect action: 'demo'
    }
    @DebugAnnotation(test='hasAffiliationAND("INST_USER", "ROLE_XY")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliationAND("INST_USER", "ROLE_XY") })
    def demo4() {
        redirect action: 'demo'
    }

    @Secured(['ROLE_YODA'])
    def appConfig() {
        Map result = [:]
        //SystemAdmin should only be created once in BootStrap
        result.adminObj = SystemAdmin.list().first()
        result.editable = true
        if (request.method == "POST") {
            result.adminObj.refresh()
        }
        result.currentconf = grails.util.Holders.config

        result
    }

    @Secured(['ROLE_YODA'])
    def quartzInfo() {
        Map result = [:]

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

                Map map = [
                        name: key.getName(),
                        configFlags: cf.join(', '),
                        nextFireTime: nft ? nft.get(0)?.toTimestamp() : ''
                ]

                def crx = triggers.collect{ it.cronEx ?: null }

                if (crx) {
                    map << ['cronEx': crx.get(0).cronExpression]
                }
                group << map
            }

            groups << ["${groupName}" : group.sort{ it.nextFireTime }]
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

        result.ehcacheManager = cacheService.getCacheManager(cacheService.EHCACHE)
        result.plugincacheManager = cacheService.getCacheManager(cacheService.PLUGINCACHE)

        if (params.cmd?.equals('clearCache')) {
            def cache
            if (params.type?.equals('ehcache')) {
                cache = cacheService.getCache(result.ehcacheManager, params.cache)
                cacheService.clear(cache)
            } else {
                cache = cacheService.getCache(result.plugincacheManager, params.cache)
                cacheService.clear(cache)
            }
        }

        result
    }

    @Secured(['ROLE_YODA'])
    def profiler() {
        Map result = [:]

        result.globalCountByUri = [:]

        SystemProfiler.executeQuery(
        "select sp.uri, sp.ms as count from SystemProfiler sp where sp.context is null and sp.params is null"
        ).each { it ->
            result.globalCountByUri["${it[0]}"] = it[1]
        }

        result.byUri =
                SystemProfiler.executeQuery("select sp.uri, avg(sp.ms) as ms, count(sp.id) as count from SystemProfiler sp where sp.context is not null group by sp.uri").sort{it[1]}.reverse()
        result.byUriAndContext =
                SystemProfiler.executeQuery("select sp.uri, org.id, avg(sp.ms) as ms, count(org.id) as count from SystemProfiler sp join sp.context as org group by sp.uri, org.id").sort{it[2]}.reverse()

        result
    }

    //@Cacheable('message')
    @Secured(['ROLE_ADMIN'])
    def appInfo() {
        Map result = [:]

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
        Map result = [:]
        Map cList = [:]

        grailsApplication.controllerClasses.toList().each { controller ->
            Class controllerClass = controller.clazz
            if (controllerClass.name.startsWith('com.k_int.kbplus') || controllerClass.name.startsWith('de.laser')) {
                def mList = [:]

                controllerClass.methods.each { Method method ->
                    if (method.getAnnotation(Action) && method.getModifiers() == Modifier.PUBLIC) {
                        def mKey = "${method.name}"
                        if (method.getAnnotation(Deprecated)) {
                            mKey = "${method.name} <em>*</em>"
                        }

                        def da = method.getAnnotation(DebugAnnotation)
                        if (da) {
                            mList << ["${mKey}": [da.test()]]
                        }
                        else {
                            mList << ["${mKey}": method.getAnnotation(Secured)?.value()]
                        }
                    }
                }

                def cKey = "${controllerClass.name}"
                if (controllerClass.getAnnotation(Deprecated)) {
                    cKey ="${controllerClass.name} <em>*</em>"
                }
                cList<< ["${cKey}": [
                        'secured': controllerClass.getAnnotation(Secured)?.value(),
                        'methods': mList.sort{it.key}
                ]
                ]
            }
        }
        result.controller = cList.sort{it.key}
        result
    }

    @Secured(['ROLE_YODA'])
    def userMatrix() {
        Map result = [:]

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
        Map result = [:]
        result.matrix = [:]
        result
    }

    @Secured(['ROLE_YODA'])
    def pendingChanges() {

        // TODO: DEBUG ONLY
        Map result = [:]

        result.pending = PendingChange.executeQuery(
                "SELECT pc FROM PendingChange pc WHERE pc.status IS NULL ORDER BY pc.id DESC",
        )
        result
    }

    @Secured(['ROLE_ADMIN'])
    def appLogfile() {
        return // TODO

        def f = new File("${Holders.config.log_location}")
        return [file: "${f.canonicalPath}"]
    }

    @Secured(['ROLE_YODA'])
    def esIndexUpdate() {
        log.debug("manual start full text index")
        dataloadService.updateSiteMapping()
        dataloadService.updateFTIndexes()
        log.debug("redirecting to home ..")

        redirect controller: 'home'
    }

    @Secured(['ROLE_YODA'])
    def fullReset() {

        if (ftupdate_running == false) {
            try {
                ftupdate_running = true
                // TODO: remove due SystemEvent
                new EventLog(event:'kbplus.fullReset',message:'Full Reset ES Start',tstp:new Date(System.currentTimeMillis())).save(flush:true)

                SystemEvent.createEvent('YODA_ES_RESET_START')

                log.debug("Delete all existing FT Control entries");
                FTControl.withTransaction {
                    FTControl.executeUpdate("delete FTControl c")
                }

                log.debug("Clear ES")
                dataloadService.clearDownAndInitES()

                log.debug("manual start full text index")
                dataloadService.updateFTIndexes()
            }
            finally {
                ftupdate_running = false
                log.debug("fullReset complete ..")
            }
        }
        else {
            log.debug("FT update already running")
        }
        log.debug("redirecting to home ..")

        redirect controller:'home'
    }

    @Secured(['ROLE_YODA'])
    def globalSync() {
        log.debug("start global sync ..")
        globalSourceSyncService.runAllActiveSyncTasks()
        log.debug("done global sync ..")

        redirect(controller: 'globalDataSync')
    }

    @Secured(['ROLE_YODA'])
    def manageGlobalSources() {
        Map result = [:]
        log.debug("manageGlobalSources ..")
        result.sources = GlobalRecordSource.list()

        result
    }

    @Secured(['ROLE_YODA'])
    def manageESSources() {
        Map result = [:]
        log.debug("manageESSources ..")
        result.sources = ElasticsearchSource.list()

        result
    }

    @Secured(['ROLE_YODA'])
    def newESSource() {
        Map result=[:]
        log.debug("manageGlobalSources ..")

        /*result.newSource = ElasticsearchSource.findByIdentifier(params.identifier) ?: new ElasticsearchSource(
                identifier:params.identifier,
                name:params.name,
                host:params.uri)

        result.newSource.save()*/

        redirect action:'manageGlobalSources'
    }

    @Secured(['ROLE_YODA'])
    def deleteGlobalSource() {
        GlobalRecordSource.removeSource(params.long('id'))

        redirect(action:'manageGlobalSources')
    }

    @Secured(['ROLE_YODA'])
    def newGlobalSource() {
        Map result=[:]
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
    def settings() {
        Map result = [:]
        result.settings = Setting.list();
        result
    }

    @Secured(['ROLE_YODA'])
    def toggleBoolSetting() {
        Map result = [:]
        def s = Setting.findByName(params.setting)
        if (s) {
            if (s.tp == Setting.CONTENT_TYPE_BOOLEAN) {
                if (s.value == 'true')
                    s.value = 'false'
                else
                    s.value = 'true'
            }

            if (s.name == "MailSentDisabled") {
                if (s.value == 'true')
                    grailsApplication.config.grails.mail.disabled = false
                else
                    grailsApplication.config.grails.mail.disabled = true
            }

            s.save(flush:true)
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
                costItems = CostItem.findAllByOwner(Org.get(owner))
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
    def manageSystemMessage() {
        Map result = [:]
        result.user = springSecurityService.currentUser

        if(params.create)
        {

            if(!SystemMessage.findAllByText(params.text)) {

                def systemMessage = new SystemMessage(office: params.office ?: null,
                        text: params.text ?: '',
                        showNow: params.showNow ?: 0)

                if (systemMessage.save(flush: true)) {
                    flash.message = 'System Nachricht erstellt'
                } else {
                    flash.error = 'System Nachricht wurde nicht erstellt!!'
                }
            }else {
                flash.error = 'System Nachricht schon im System!!'
            }
        }


        result.systemMessages = SystemMessage.findAll()
        result.editable = true
        result
    }

    @Secured(['ROLE_YODA'])
    def deleteSystemMessage(Long id) {
        if(SystemMessage.get(id)) {
            SystemMessage.get(id).delete(flush: true)
            flash.message = 'System Nachricht wurde gelöscht!!'
        }

        redirect(action: 'manageSystemMessage')
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
        flash.message = "Lizenzen werden upgedatet"
        subscriptionUpdateService.subscriptionCheck()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def updateLinks(){
        int affected = subscriptionUpdateService.updateLinks()
        flash.message = "Es wurden ${affected} Vor-/Nachfolgebeziehungen neu verknüpft"
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def startDateCheck(){
        if(subscriptionUpdateService.startDateCheck())
            flash.message = "Lizenzen ohne Startdatum verlieren ihren Status ..."
        else
            flash.message = "Lizenzen ohne Startdatum haben bereits ihren Status verloren!"
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def updateTaxRates(){
        flash.message = "Kosten werden in das neue Steuermodell überführt ..."
        costItemUpdateService.updateTaxRates()
        redirect(url: request.getHeader('referer'))
    }

}
