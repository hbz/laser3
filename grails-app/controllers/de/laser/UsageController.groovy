package de.laser

import de.laser.auth.User
import de.laser.ctrl.UsageControllerService
import de.laser.helper.SwissKnife
import grails.plugin.springsecurity.annotation.Secured
import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification

/**
 * This controller handles calls fot Nationaler Statistikserver data management.
 * Not to confound with the LAS:eR statistics component whose global management
 * methods are defined in {@link YodaController}
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class UsageController  {

    StatsSyncService statsSyncService
    UsageControllerService usageControllerService

    /**
     * Landing page of the statistics overview
     */
    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def index() {
        Map<String, Object> result = usageControllerService.initResult(this, params)
        if (result.tmpFlashError) {
            flash.error = result.tmpFlashError
            result.remove('tmpFlashError')
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)

        // criteria and totalCount for PageResultList Object seems to be problematic with projections and aggregation
        // use extra hql query for now, TODO only use hql base query and move the query out of this method

        String hql = "select stc.supplierId, stc.customerId, min(stc.availFrom), max(stc.availTo), stc.factType.id from StatsTripleCursor as stc"
        String groupCondition = " group by stc.supplierId, stc.customerId, stc.factType.id"
        ArrayList whereConditions = []
        LinkedHashMap<String,Object> queryParams = [:]
        if (params.supplier){
            whereConditions.add('supplierId=:supplierId')
            queryParams += [supplierId: params.supplier]
        }
        if (params.institution) {
            whereConditions.add('customerId=:customerId')
            queryParams += [customerId: params.institution]
        }
        if (!whereConditions.empty) {
            hql += " where " + whereConditions.join(' and ')
        }
        hql += groupCondition
        /* needed if we remove the criteria
        if ((params.sort != null) && (params.sort.length() > 0)) {
           //numFact has to be addressed seperatly (todo)
            hql += " order by stc.${params.sort} ${params.order}"
        } else {
            hql += " order by stc.supplierId asc"
        }*/
        ArrayList totalResultIds = StatsTripleCursor.executeQuery(hql, queryParams)

        List<HashMap> results = StatsTripleCursor.createCriteria().list(max: result.max, offset: result.offset) {
            projections {
                groupProperty('supplierId', 'supplierId')
                groupProperty('customerId', 'customerId')
                groupProperty('factType', 'factType')
                min('availFrom', 'availFrom')
                max('availTo', 'availTo')
                sum('numFacts', 'numFacts')
            }
            if (params.supplier) {
                eq("supplierId", params.supplier)
            }
            if (params.institution) {
                eq("customerId", params.institution)
            }
            if ((params.sort != null) && (params.sort.length() > 0)) {
                order(params.sort, params.order)
            } else {
                order("supplierId", "asc")
            }

            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
        }

        result.availStatsRanges = results
        result.num_stc_rows = totalResultIds.size()
        result
    }

    /**
     * Aborts an eventually running loading procedure
     */
    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def abort()
    {
        Map<String, Object> result = usageControllerService.initResult(this, params)
        if (result.tmpFlashError) {
            flash.error = result.tmpFlashError
            result.remove('tmpFlashError')
        }

        statsSyncService.setErrors([])
        statsSyncService.running = false
        redirect(view: "index", model: result)
    }

    /**
     * Fetches for the given institution and provider the usage data from the Nationaler Statistikserver
     * SUSHI server
     */
    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def fetchSelection()
    {
        // TODO when we switch to global API Key / Requestor, query SUSHI Service status endpoint here
        // Do not continue if service is not active or there is an error with the API Credentials.
        statsSyncService.setErrors([])
        Map<String, Object> result = usageControllerService.initResult(this, params)
        if (result.tmpFlashError) {
            flash.error = result.tmpFlashError
            result.remove('tmpFlashError')
        }

        statsSyncService.addFilters(params)
        statsSyncService.doSync()
        if (statsSyncService.errors) {
            flash.error = statsSyncService.errors.join('</br>')
        }
        redirect(view: "index", model: result)
    }

    /**
     * Clears the database from current usage data
     */
    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    @Transactional
    def deleteAll()
    {
        statsSyncService.setErrors([])
        Map<String, Object> result = usageControllerService.initResult(this, params)
        if (result.tmpFlashError) {
            flash.error = result.tmpFlashError
            result.remove('tmpFlashError')
        }

        Fact.executeUpdate('delete from Fact')
        StatsTripleCursor.executeUpdate('delete from StatsTripleCursor ')
        flash.message = message(code: 'default.usage.delete.success')
        redirect(view: "index", model: result)
    }

    /**
     * Clears the usage data for the given institution and provider
     */
    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    @Transactional
    def deleteSelection()
    {
        statsSyncService.setErrors([])
        Map<String, Object> result = usageControllerService.initResult(this, params)
        if (result.tmpFlashError) {
            flash.error = result.tmpFlashError
            result.remove('tmpFlashError')
        }

        def wibid, supplier, platform, instOrg

        if (params.supplier != 'null'){
            platform = Platform.get(params.supplier)
            def cp = platform.propertySet.find(){
                it.type.name = "NatStat Supplier ID"
            }
            supplier = cp.stringValue
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
            factParams.supplier_id = platform
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
