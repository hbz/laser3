package de.laser

import com.k_int.kbplus.Fact
import de.laser.domain.StatsTripleCursor
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

@Secured(['IS_AUTHENTICATED_FULLY'])
class UsageController {

    def statsSyncService
    def factService

    static transactional = false

    @Secured(['ROLE_ADMIN'])
    def index() {
        def result = initResult()
        result
    }

    private initResult()
    {
        def result = [:]
        result.statsSyncService = [:]
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

        result.institutionList = factService.institutionsWithRequestorIDAndAPIKey()
        result.providerList = factService.providersWithStatssid()
        result.institutionsWithFacts = factService.getFactInstitutionList()
        result.providersWithFacts = factService.getFactProviderList()

        return result
    }

    @Secured(['ROLE_ADMIN'])
    def abort()
    {
        def result = initResult()
        statsSyncService.running = false
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_ADMIN'])
    def fetchSelection()
    {
        def result = initResult()
        statsSyncService.doSync()
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_ADMIN'])
    @Transactional
    def deleteAll()
    {
        def result = initResult()
        Fact.executeUpdate('delete from Fact')
        StatsTripleCursor.executeUpdate('delete from StatsTripleCursor ')
        flash.message = message(code: 'default.usage.delete.success')
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_ADMIN'])
    def deleteSelection()
    {
        def result = initResult()
        flash.message = 'Not yet implemented'
        redirect(view: "index", model: result)
    }

}
