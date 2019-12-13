package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import de.laser.domain.SystemProfiler
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.web.Action
import groovy.xml.MarkupBuilder
import org.hibernate.SessionFactory
import org.quartz.JobKey
import org.quartz.impl.matchers.GroupMatcher
import org.springframework.transaction.TransactionStatus
import groovy.json.JsonOutput
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.servlet.ServletOutputStream
import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Secured(['IS_AUTHENTICATED_FULLY'])
class YodaController {

    SessionFactory sessionFactory

    def yodaService
    def cacheService
    def springSecurityService
    def statsSyncService
    def dataloadService
    def globalSourceSyncService
    def contextService
    def dashboardDueDatesService
    def subscriptionUpdateService
    def costItemUpdateService
    def documentUpdateService
    def quartzScheduler
    def identifierService
    def deletionService
    def surveyUpdateService
    def changeNotificationService
    def subscriptionService
    def exportService

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

    @DebugAnnotation(perm="ORG_CONSORTIUM", affil="INST_ADM", specRole="ROLE_ORG_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_ADM", "ROLE_ORG_EDITOR")
    })
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

        result.numberOfActiveUsers = yodaService.getNumberOfActiveUsers()

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
        //result.adminObj = SystemAdmin.list().first()
        result.editable = true
        //if (request.method == "POST") {
        //    result.adminObj.refresh()
        //}
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
            if (params.type?.equals('session')) {
                cache = contextService.getSessionCache()
                cache.clear()
            }
            else if (params.type?.equals('ehcache')) {
                cache = cacheService.getCache(result.ehcacheManager, params.cache)
                cacheService.clear(cache)
            }
            else {
                cache = cacheService.getCache(result.plugincacheManager, params.cache)
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
    def profiler() {
        Map result = [:]

        result.globalCountByUri = [:]

        SystemProfiler.executeQuery(
        "select sp.uri, sp.ms as count from SystemProfiler sp where sp.context is null and sp.params is null"
        ).each { it ->
            result.globalCountByUri["${it[0]}"] = it[1]
        }

        result.byUri =
                SystemProfiler.executeQuery("select sp.uri, max(sp.ms) as max, sum(sp.ms) as ms, count(sp.id) as count from SystemProfiler sp where sp.context is not null group by sp.uri")

        result.byUri.each{ it ->
            def tmp = ((it[2] + (de.laser.domain.SystemProfiler.THRESHOLD_MS * (result.globalCountByUri.get(it[0]) - it[3]))) / result.globalCountByUri.get(it[0]))
            it[2] = tmp
        }
        result.byUri.sort{a,b -> Double.compare(a[2], b[2]) * -1}

        result.byUriAndContext =
                SystemProfiler.executeQuery("select sp.uri, org.id, max(sp.ms) as max, avg(sp.ms) as ms, count(org.id) as count from SystemProfiler sp join sp.context as org group by sp.uri, org.id").sort{it[3]}.reverse()

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
                            mList << ["${mKey}": [perm: da.perm(),
                                                  type: da.type(),
                                                  affil: da.affil(),
                                                  specRoles: da.specRole(),
                                                  test: da.test()]]
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
        subscriptionUpdateService.retriggerPendingChanges()
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def getTIPPsWithoutGOKBId() {
        log.debug("delete TIPPs without GOKb-ID")
        List<TitleInstancePackagePlatform> tippsWithoutGOKbID = TitleInstancePackagePlatform.findAllByGokbIdIsNullAndStatusNotEqual(RDStore.TIPP_DELETED)
        List<IssueEntitlement> issueEntitlementsAffected = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp in :tipps',[tipps:tippsWithoutGOKbID])
        Map<TitleInstancePackagePlatform,Set<IssueEntitlement>> ieTippMap = [:]
        issueEntitlementsAffected.each { IssueEntitlement ie ->
            if (ieTippMap.get(ie.tipp)) {
                ieTippMap[ie.tipp] << ie
            } else {
                Set<IssueEntitlement> ies = new TreeSet<IssueEntitlement>()
                ies.add(ie)
                ieTippMap[ie.tipp] = ies
            }
        }
        [tipps: tippsWithoutGOKbID, issueEntitlements: ieTippMap]
    }

    @Secured(['ROLE_YODA'])
    def purgeTIPPsWithoutGOKBId() {
        def toDelete = JSON.parse(params.toDelete)
        if(params.doIt == "true") {
            toDelete.each { oldTippId, newTippId ->
                TitleInstancePackagePlatform oldTipp = TitleInstancePackagePlatform.get(oldTippId)
                TitleInstancePackagePlatform newTipp = TitleInstancePackagePlatform.get(newTippId)
                deletionService.deleteTIPP(oldTipp,newTipp)
            }
            redirect(url: request.getHeader('referer'))
        }
        else {
            flash.message = "Betroffene TIPP-IDs wären vereinigt worden: ${toDelete}"
            redirect action: 'getTIPPsWithoutGOKBId'
        }
    }

    @Secured(['ROLE_YODA'])
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

    @Secured(['ROLE_ADMIN'])
    def appLogfile() {
        return // TODO

        def f = new File("${Holders.config.log_location}")
        return [file: "${f.canonicalPath}"]
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

        if(dataloadService.update_running == false) {
            if (ftupdate_running == false) {
                try {
                    ftupdate_running = true
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
            } else {
                log.debug("FT update already running")
                flash.error = 'FT update already running'
            }
        }else{
            log.debug("Full Reset fail, because IndexUpdateJob running")
            flash.error = 'Full Reset fail, because IndexUpdateJob running'
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
    def migrateNatStatSettings() {
        Map<String, Object> result = [:]

        List<OrgCustomProperty> ocpList = OrgCustomProperty.executeQuery(
                'select ocp from OrgCustomProperty ocp join ocp.type pd where pd.descr = :orgConf', [
                orgConf: PropertyDefinition.ORG_CONF
        ])

        ocpList.each { ocp ->
            if (ocp.type.name == 'API Key') {
                def oss = OrgSettings.get(ocp.owner, OrgSettings.KEYS.NATSTAT_SERVER_API_KEY)

                if (oss == OrgSettings.SETTING_NOT_FOUND) {
                    OrgSettings.add(ocp.owner, OrgSettings.KEYS.NATSTAT_SERVER_API_KEY, ocp.getValue())
                }
                else {
                    oss.setValue(ocp)
                }
            }
            else if (ocp.type.name == 'RequestorID') {
                def oss = OrgSettings.get(ocp.owner, OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID)

                if (oss == OrgSettings.SETTING_NOT_FOUND) {
                    OrgSettings.add(ocp.owner, OrgSettings.KEYS.NATSTAT_SERVER_REQUESTOR_ID, ocp.getValue())
                }
                else {
                    oss.setValue(ocp)
                }
            }
        }

        OrgCustomProperty.executeQuery(
                'select ocp from OrgCustomProperty ocp join ocp.type pd where pd.descr = :orgConf '
                + 'and ( pd.name = \'API Key\' or pd.name = \'RequestorID\' )',
                [orgConf: PropertyDefinition.ORG_CONF]
        ).each{ it.delete() }

        redirect action:'dashboard'
    }

    @Secured(['ROLE_YODA'])
    def settings() {
        Map<String, Object> result = [:]
        result.settings = Setting.list();
        result
    }

    @Secured(['ROLE_YODA'])
    def migrateCollectiveSubscriptions() {
        Map<String, Object> result = [:]

        result.subRoles = Subscription.executeQuery(
            'select sub, role from Subscription sub join sub.orgRelations role, OrgSettings os ' +
            '  where os.org = role.org ' +
            '  and role.roleType.value like \'Subscriber\' ' +
            '  and os.key like \'CUSTOMER_TYPE\' and os.roleValue.authority like \'ORG_INST_COLLECTIVE\' '
        )

        result.subConsRoles = Subscription.executeQuery(
                'select sub, role from Subscription sub join sub.orgRelations role, OrgSettings os ' +
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

				if (sub.getCalculatedType() == Subscription.CALCULATED_TYPE_LOCAL) {
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

                if (sub.getCalculatedType() == Subscription.CALCULATED_TYPE_PARTICIPATION) {
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
        subscriptionUpdateService.assignNoteOwners()
        redirect controller: 'home'
    }

    @Secured(['ROLE_YODA'])
    def toggleBoolSetting() {
        Map<String, Object> result = [:]
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
        Map<String, Object> result = [:]
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
    def surveyCheck(){
        flash.message = "Umfragen werden upgedatet"
        surveyUpdateService.surveyCheck()
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

    @Secured(['ROLE_YODA'])
    Map checkIssueEntitlementPackages() {
        Map<String,List<IssueEntitlement>> result = [:]
        result.ieList = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie join ie.tipp.pkg tp where not exists (select sp.pkg from SubscriptionPackage sp where sp.subscription = ie.subscription and sp.pkg = tp)')
        result
    }

    @Secured(['ROLE_YODA'])
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
                    errorMsg << sp.getErrors()
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
        RefdataValue cons = RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')
        RefdataValue inst = RefdataValue.getByValueAndCategory('Institution', 'OrgRoleType')

        List<Org> consOrgs = Org.executeQuery("SELECT o from Org o join o.orgType ot where ot = :cons", [cons: cons])
        List<Org> instOrgs = Org.executeQuery("SELECT o from Org o join o.orgType ot where ot = :inst", [inst: inst])

        int consCount = 0
        consOrgs.each{ o ->
            def oss = OrgSettings.get(o, OrgSettings.KEYS.CUSTOMER_TYPE)
            if (oss == OrgSettings.SETTING_NOT_FOUND) {
                log.debug ('Setting customer type for org: ' + o.id)
                OrgSettings.add(o, OrgSettings.KEYS.CUSTOMER_TYPE, Role.findByAuthorityAndRoleType('ORG_CONSORTIUM_SURVEY', 'org'))
                consCount++
            }
        }

        int instCount = 0
        instOrgs.each{ o ->
            if (o.setDefaultCustomerType()) { instCount++ }
        }

        flash.message = "Kundentyp wurde für ${consCount} Konsortien und ${instCount} Konsorten gesetzt."
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_YODA'])
    def insertEditUris() {
        //ERMS-1758
        List<ApiSource> apiSources = ApiSource.findAllByEditUrlIsNull()
        List<GlobalRecordSource> globalRecordSources = GlobalRecordSource.findAllByEditUriIsNull()
        apiSources.each { ApiSource aps ->
            if(aps.baseUrl.contains('phaeton.hbz-nrw')) {
                aps.editUrl = 'https://gokb.org'
            }
            else {
                aps.editUrl = aps.baseUrl
            }
            aps.save()
        }
        globalRecordSources.each { GlobalRecordSource grs ->
            if(grs.uri.contains('phaeton.hbz-nrw')) {
                grs.editUri = 'https://gokb.org/gokb/oai/packages'
            }
            else {
                grs.editUri = grs.uri
            }
            grs.save()
        }
        flash.message = "${apiSources.size()} ApiSources und ${globalRecordSources.size()} GlobalRecordSources angepasst!"
        redirect controller: 'home', action: 'index'
    }

    @Secured(['ROLE_YODA'])
    def showOldDocumentOwners(){
        List currentDocuments = DocContext.executeQuery('select dc from DocContext dc where dc.owner.creator != null and dc.owner.owner = null and dc.sharedFrom = null order by dc.owner.creator.display asc')
        Map<String, Object> result = [currentDocuments:currentDocuments]
        result
    }

    @Secured(['ROLE_YODA'])
    def updateShareConfigurations(){
        flash.message = "Überarbeite Sichtbarkeitseinstellungen und Eigentümerverhältnisse ..."
        documentUpdateService.updateShareConfigurations()
        redirect(url: request.getHeader('referer'))
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
            File dir = new File(grailsApplication.config.basicDataPath)
            if(!dir.exists()) {
                dir.mkdir()
            }
            new File("${grailsApplication.config.basicDataPath}${grailsApplication.config.basicDataFileName}").withWriter { writer ->
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
                                    impId(o.impId)
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
                                    federalState {
                                        if(o.federalState) {
                                            rdc(o.federalState.owner.desc)
                                            rdv(o.federalState.value)
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
                                        List<OrgSettings> os = OrgSettings.findAllByOrg(o)
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
                                    List<UserSettings> us = UserSettings.findAllByUser(u)
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
                                if(a.state) {
                                    state {
                                        rdc(a.state.owner.desc)
                                        rdv(a.state.value)
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
                flash.message = 'Das Ersetzen des Usersettings DASHBOARD_REMINDER_PERIOD für alle Benutzer im System war erfolgreich.'
            } catch (Exception ex) {
                status.setRollbackOnly()
                flash.error = 'Es ist ein Fehler aufgetreten beim Ersetzen des Usersettings DASHBOARD_REMINDER_PERIOD: ' + ex.message
                flash.error += '<br /><br /><b>Es wurde ein Rollback durchgeführt!</b>'
            }
        }
        result
    }

    @Secured(['ROLE_YODA'])
    def correctCostsInLocalCurrency() {
        Map<String, Object> result = ["costItems":costItemUpdateService.correctCostsInLocalCurrency(Boolean.valueOf(params.dryRun))]
        result
    }

    @Secured(['ROLE_YODA'])
    def dbmFixPrivateProperties() {
        Map<String, Object> result = [:]

        def opp = OrgPrivateProperty.executeQuery(
                "SELECT pp FROM OrgPrivateProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        def spp = SubscriptionPrivateProperty.executeQuery(
                "SELECT pp FROM SubscriptionPrivateProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                "AND (pp.note IS null OR pp.note = '') "
        )

        def lpp = LicensePrivateProperty.executeQuery(
                "SELECT pp FROM LicensePrivateProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') " +
                        "AND (pp.paragraph IS null OR pp.paragraph = '') "
        )

        def ppp = PersonPrivateProperty.executeQuery(
                "SELECT pp FROM PersonPrivateProperty pp JOIN pp.type pd WHERE pd.mandatory = true " +
                        "AND pp.stringValue IS null AND pp.intValue IS null AND pp.decValue IS null " +
                        "AND pp.refValue IS null AND pp.urlValue IS null AND pp.dateValue IS null " +
                        "AND (pp.note IS null OR pp.note = '') "
        )

        if (params.cmd == 'doIt') {
            println opp.collect{ it.id }
            if (opp.size() > 0) {
                OrgPrivateProperty.executeUpdate('DELETE FROM OrgPrivateProperty opp WHERE opp.id in :idList',
                        [idList: opp.collect { it.id }]
                )
            }

            println spp.collect{ it.id }
            if (spp.size() > 0) {
                SubscriptionPrivateProperty.executeUpdate('DELETE FROM SubscriptionPrivateProperty spp WHERE spp.id in :idList',
                        [idList: spp.collect { it.id }]
                )
            }

            println lpp.collect{ it.id }
            if (lpp.size() > 0) {
                LicensePrivateProperty.executeUpdate('DELETE FROM LicensePrivateProperty lpp WHERE lpp.id in :idList',
                        [idList: lpp.collect { it.id }]
                )
            }

            println ppp.collect{ it.id }
            if (ppp.size() > 0) {
                PersonPrivateProperty.executeUpdate('DELETE FROM PersonPrivateProperty ppp WHERE ppp.id in :idList',
                        [idList: ppp.collect { it.id }]
                )
            }
        }

        result.candidates = [OrgPrivateProperty: opp, SubscriptionPrivateProperty: spp, LicensePrivateProperty: lpp, PersonPrivateProperty: ppp]

        render view: 'databaseMigration', model: result
    }

    @Secured(['ROLE_YODA'])
    def frontend() {
        Map<String, Object> result = [test:123]
        result
    }

    @Secured(['ROLE_YODA'])
    def cleanUpSurveys() {
        def result = [:]

        def subSurveys = SurveyConfig.findAllBySubscriptionIsNotNull()
        def count = 0
        def resultMap = []
        subSurveys.each { surConfig ->


            def parentSubscription = surConfig?.subscription
            def parentSubChilds = subscriptionService.getCurrentValidSubChilds(parentSubscription)
            def parentSuccessorSubscription = surConfig?.subscription?.getCalculatedSuccessor()
            def property = PropertyDefinition.findByName("Mehrjahreslaufzeit ausgewählt")
            parentSubChilds?.each { sub ->
                if (sub?.getCalculatedSuccessor()) {
                    sub?.getAllSubscribers().each { org1 ->

                        def surveyResult = SurveyResult.findAllBySurveyConfigAndParticipant(surConfig, org1)
                        if(surveyResult?.size() > 0) {
                            count++
                            def newMap = [:]
                            println(count + ": ${sub.name} (${sub.id}) [${org1.name}]" + surveyResult)
                            newMap.surveyResult = surveyResult?.id ?: ""
                            newMap.subName = sub.name
                            newMap.subId = sub.id
                            newMap.orgName = org1.name
                            newMap.sortName = org1?.sortname
                            newMap.propertiesSize = surveyResult?.size()
                            newMap.info = 'Nachfolger Lizenz vorhanden'
                            println("")
                            resultMap << newMap
                        }else{
                            count++
                            def newMap = [:]
                            println(count + ": LEER : ${sub.name} (${sub.id}) [${org1.name}]" + surConfig)
                            newMap.surveyResult = 'Kein Umfrage'
                            newMap.subName = sub.name
                            newMap.subId = sub.id
                            newMap.orgName = org1.name
                            newMap.sortName = org1?.sortname
                            newMap.propertiesSize = 0
                            newMap.info = 'Nachfolger Lizenz vorhanden'
                            println("")
                            resultMap << newMap
                        }

                    }

                } else {
                    if (property?.type == 'class com.k_int.kbplus.RefdataValue') {
                        if (sub?.customProperties?.find {
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
                    }
                }
            }

        }
        def output = JsonOutput.toJson(resultMap)

        println(output)

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
    def importSeriesToEBooks() {
        def result = [:]


        if(params.kbartPreselect) {
            CommonsMultipartFile kbartFile = params.kbartPreselect
            Integer count = 0
            Integer countChanges = 0
            InputStream stream = kbartFile.getInputStream()
            ArrayList<String> rows = stream.text.split('\n')
            Map<String,Integer> colMap = [publicationTitleCol:-1,zdbCol:-1, onlineIdentifierCol:-1, printIdentifierCol:-1, doiTitleCol:-1, seriesTitleCol:-1]
            //read off first line of KBART file
            rows[0].split('\t').eachWithIndex { headerCol, int c ->
                switch(headerCol.toLowerCase().trim()) {
                    case "zdb_id": colMap.zdbCol = c
                        break
                    case "print_identifier": colMap.printIdentifierCol = c
                        break
                    case "online_identifier": colMap.onlineIdentifierCol = c
                        break
                    case "publication_title": colMap.publicationTitleCol = c
                        break
                    case "series": colMap.seriesTitleCol = c
                        break
                    case "doi_identifier": colMap.doiTitleCol = c
                        break
                }
            }
            //after having read off the header row, pop the first row
            rows.remove(0)
            //now, assemble the identifiers available to highlight
            Map<String,IdentifierNamespace> namespaces = [zdb:IdentifierNamespace.findByNs('zdb'),
                                                          eissn:IdentifierNamespace.findByNs('eissn'),isbn:IdentifierNamespace.findByNs('isbn'),
                                                          issn:IdentifierNamespace.findByNs('issn'),pisbn:IdentifierNamespace.findByNs('pisbn'),
                                                            doi: IdentifierNamespace.findByNs('doi')]
            rows.eachWithIndex { row, int i ->
                log.debug("now processing entitlement ${i}")
                ArrayList<String> cols = row.split('\t')
                Map idCandidate
                if(colMap.zdbCol >= 0 && cols[colMap.zdbCol]) {
                    idCandidate = [namespaces:[namespaces.zdb],value:cols[colMap.zdbCol]]
                }
                if(colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol]) {
                    idCandidate = [namespaces:[namespaces.eissn,namespaces.isbn],value:cols[colMap.onlineIdentifierCol]]
                }
                if(colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol]) {
                    idCandidate = [namespaces:[namespaces.issn,namespaces.pisbn],value:cols[colMap.printIdentifierCol]]
                }
                if(colMap.doiTitleCol >= 0 && cols[colMap.doiTitleCol]) {
                    idCandidate = [namespaces:[namespaces.doi],value:cols[colMap.doiTitleCol]]
                }
                if(((colMap.zdbCol >= 0 && cols[colMap.zdbCol].trim().isEmpty()) || colMap.zdbCol < 0) &&
                        ((colMap.onlineIdentifierCol >= 0 && cols[colMap.onlineIdentifierCol].trim().isEmpty()) || colMap.onlineIdentifierCol < 0) &&
                        ((colMap.printIdentifierCol >= 0 && cols[colMap.printIdentifierCol].trim().isEmpty()) || colMap.printIdentifierCol < 0)) {
                }
                else {

                    // TODO [ticket=1789]
                    //def tiObj = TitleInstance.executeQuery('select ti from TitleInstance ti join ti.ids ids where ids in (select io from IdentifierOccurrence io join io.identifier id where id.ns in :namespaces and id.value = :value)',[namespaces:idCandidate.namespaces,value:idCandidate.value])
                    def tiObj = TitleInstance.executeQuery('select ti from TitleInstance ti join ti.ids ident where ident.ns in :namespaces and ident.value = :value', [namespaces:idCandidate.namespaces, value:idCandidate.value])
                    if(tiObj) {

                        tiObj?.each { titleInstance ->
                                if(titleInstance instanceof BookInstance) {
                                    count++

                                    if((cols[colMap.seriesTitleCol] != null || cols[colMap.seriesTitleCol] != "") && (titleInstance.summaryOfContent == null || titleInstance.summaryOfContent == "")){
                                        countChanges++
                                        titleInstance.summaryOfContent = cols[colMap.seriesTitleCol]
                                        titleInstance.save(flush: true)
                                    }
                                }
                        }
                    }
                }
            }

            flash.message = "Verbearbeitet: ${count} /Geändert ${countChanges}"
            println(count)
            println(countChanges)
            params.remove("kbartPreselct")
        }

    }

}
