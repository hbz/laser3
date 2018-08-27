package de.laser

import com.k_int.kbplus.Fact
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgCustomProperty
import com.k_int.properties.PropertyDefinition
import de.laser.domain.StatsTripleCursor
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional

@Secured(['IS_AUTHENTICATED_FULLY'])
class UsageController {

    def statsSyncService
    def factService
    def contextService

    static transactional = false

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
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

        result.institution = contextService.getOrg()
        result.institutionList = factService.institutionsWithRequestorIDAndAPIKey()
        result.providerList = factService.providersWithStatssid()
        result.institutionsWithFacts = factService.getFactInstitutionList()
        result.providersWithFacts = factService.getFactProviderList()
        result.cursorCount = factService.getSupplierCursorCount()
        result.apiKey = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("API Key"), result.institution)
        result.requestor = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)

        return result
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def abort()
    {
        def result = initResult()
        statsSyncService.running = false
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def fetchSelection()
    {
        def result = initResult()
        statsSyncService.addFilters(params)
        statsSyncService.doSync()
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    @Transactional
    def deleteAll()
    {
        def result = initResult()
        Fact.executeUpdate('delete from Fact')
        StatsTripleCursor.executeUpdate('delete from StatsTripleCursor ')
        flash.message = message(code: 'default.usage.delete.success')
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    @Transactional
    def deleteSelection()
    {
        def result = initResult()
        def wibid, supplier, supplierOrg, instOrg

        if (params.supplier != 'null'){
            supplierOrg = Org.get(params.supplier)
            supplier = supplierOrg?.getIdentifierByType('statssid')?.value

        }
        if (params.institution != 'null'){
            instOrg = Org.get(params.institution)
            wibid = instOrg?.getIdentifierByType('wibid')?.value
        }
        def factAndWhereCondition = ''
        def cursorAndWhereCondition = ''
        def factParams = [:]
        def cursorParams = [:]

        if (supplier) {
            factAndWhereCondition += " and t1.supplier = :supplier_id"
            cursorAndWhereCondition += " and t1.supplierId =:supplierName"
            factParams.supplier_id = supplierOrg
            cursorParams.supplierName = supplier
        }
        if (wibid) {
            factAndWhereCondition += " and t1.inst = :customer_id"
            cursorAndWhereCondition += " and t1.customerId = :customerName"
            factParams.customer_id = instOrg
            cursorParams.customerName = wibid
        }
        def deletedCursorCount = StatsTripleCursor.executeUpdate('delete from StatsTripleCursor t1 where 1=1' + cursorAndWhereCondition,
            cursorParams)
        def deletedFactCount = Fact.executeUpdate('delete from Fact t1 where 1=1' + factAndWhereCondition,
            factParams)
        log.debug("Deleted ${deletedCursorCount} entries from StatsTripleCursor table and ${deletedFactCount} entries from fact table")
        flash.message = message(code: 'default.usage.delete.success')
        redirect(view: "index", model: result)
    }

}
