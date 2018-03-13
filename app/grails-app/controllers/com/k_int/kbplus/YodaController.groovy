package com.k_int.kbplus

import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured // 2.0
import grails.util.Holders
import grails.web.Action

import java.lang.reflect.Method
import java.lang.reflect.Modifier

@Secured(['IS_AUTHENTICATED_FULLY'])
class YodaController {

    def springSecurityService
    def statsSyncService
    def dataloadService
    def globalSourceSyncService

    static boolean ftupdate_running = false

    @DebugAnnotation(test='hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser().hasAffiliation("INST_ADM") })
    def demo() {
        def result = [:]

        result.user = springSecurityService.getCurrentUser()
        result.roles = result.user.roles
        result.affiliations = result.user.affiliations

        result.check1 = "INST_ADMx: " + result.user.hasAffiliation("INST_ADMx")
        result.check2 = "INST_ADM: " + result.user.hasAffiliation("INST_ADM")
        result.check3 = "INST_EDITOR: " + result.user.hasAffiliation("INST_EDITOR")
        result.check4 = "INST_USER: " + result.user.hasAffiliation("INST_USER")
        result
    }

    @DebugAnnotation(test='hasMinimumAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser().hasMinimumAffiliation("INST_USER") })
    def demo2() {
        redirect action: 'demo'
    }

    @Secured(['ROLE_YODA'])
    def appConfig() {
        def result = [:]
        //SystemAdmin should only be created once in BootStrap
        result.adminObj = SystemAdmin.list().first()
        result.editable = true
        if (request.method == "POST") {
            result.adminObj.refresh()
        }
        result.currentconf = grails.util.Holders.config

        result
    }

    @Secured(['ROLE_ADMIN'])
    def appInfo() {
        def result = [:]

        result.statsSyncService = [:]
        result.dataloadService = [:]
        result.globalSourceSyncService = [:]
        result.globalSourceSyncService.running = globalSourceSyncService.running
        result.statsSyncService.running = statsSyncService.running
        result.statsSyncService.submitCount = statsSyncService.submitCount
        result.statsSyncService.completedCount = statsSyncService.completedCount
        result.statsSyncService.newFactCount = statsSyncService.newFactCount
        result.statsSyncService.totalTime = statsSyncService.totalTime
        result.statsSyncService.threads = statsSyncService.FIXED_THREAD_POOL_SIZE
        result.statsSyncService.queryTime = statsSyncService.queryTime
        result.statsSyncService.activityHistogram = statsSyncService.activityHistogram
        result.statsSyncService.syncStartTime = statsSyncService.syncStartTime
        result.statsSyncService.syncElapsed = statsSyncService.syncElapsed
        result.dataloadService.update_running = dataloadService.update_running
        result.dataloadService.lastIndexUpdate = dataloadService.lastIndexUpdate

        result
    }

    @Secured(['ROLE_YODA'])
    def appSecurity() {
        def result = [:]
        def cList = [:]

        grailsApplication.controllerClasses.toList().each { controller ->
            Class controllerClass = controller.clazz

            if (controllerClass.name.startsWith('com.k_int.kbplus')) {
                def mList = [:]
                controllerClass.methods.each { Method method ->
                    if (method.getAnnotation(Action) && method.getModifiers() == Modifier.PUBLIC) {

                        def da = method.getAnnotation(de.laser.helper.DebugAnnotation)
                        if (da) {
                            mList << ["${method.name}": [da.test()]]
                        }
                        else {
                            mList << ["${method.name}": method.getAnnotation(grails.plugin.springsecurity.annotation.Secured)?.value()]
                        }
                    }
                }
                cList<< ["${controllerClass.name}": [
                        'secured': controllerClass.getAnnotation(grails.plugin.springsecurity.annotation.Secured)?.value(),
                        'methods': mList.sort{it.key}
                        ]
                ]
            }
        }
        result.controller = cList.sort{it.key}
        result
    }

    @Secured(['ROLE_ADMIN'])
    def appLogfile() {
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
                new EventLog(event:'kbplus.fullReset',message:'Full Reset',tstp:new Date(System.currentTimeMillis())).save(flush:true)
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
        def result = [:]
        log.debug("manageGlobalSources ..")
        result.sources = GlobalRecordSource.list()

        result
    }

    @Secured(['ROLE_YODA'])
    def deleteGlobalSource() {
        GlobalRecordSource.removeSource(params.long('id'))

        redirect(action:'manageGlobalSources')
    }

    @Secured(['ROLE_YODA'])
    def newGlobalSource() {
        def result=[:]
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
        def result = [:]
        result.settings = Setting.list();
        result
    }

    @Secured(['ROLE_YODA'])
    def toggleBoolSetting() {
        def result = [:]
        def s = Setting.findByName(params.setting)
        if (s) {
            if (s.tp == Setting.CONTENT_TYPE_BOOLEAN) {
                if (s.value == 'true')
                    s.value = 'false'
                else
                    s.value = 'true'
            }
            s.save(flush:true)
        }

        redirect action:'settings'
    }
}
