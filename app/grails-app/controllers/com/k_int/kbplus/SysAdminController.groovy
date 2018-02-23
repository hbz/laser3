package com.k_int.kbplus

import grails.plugin.springsecurity.annotation.Secured // 2.0
import grails.util.Holders

class SysAdminController {
    def zenDeskSyncService
    def statsSyncService
    def dataloadService
    def globalSourceSyncService

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
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

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
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
        result;
    }

    @Secured(['ROLE_ADMIN', 'IS_AUTHENTICATED_FULLY'])
    def logViewer() {
        def f = new File("${Holders.config.log_location}")

        return [file: "${f.canonicalPath}"]
    }
}
