package de.laser

import com.k_int.kbplus.Fact
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgCustomProperty
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import de.laser.controller.AbstractDebugController
import de.laser.domain.StatsTripleCursor
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import org.hibernate.criterion.CriteriaSpecification

@Secured(['IS_AUTHENTICATED_FULLY'])
class UsageController extends AbstractDebugController {

    def statsSyncService
    def factService
    def contextService
    def springSecurityService

    static transactional = false

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def index() {
        def result = initResult()

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        // criteria and totalCount for PageResultList Object seems to be problematic with projections and aggregation
        // use extra hql query for now, TODO only use hql base query and move the query out of this method

        def hql = "select stc.supplierId, stc.customerId, max(stc.haveUpTo), stc.factType.id from StatsTripleCursor as stc"
        def groupCondition = " group by stc.supplierId, stc.customerId, stc.factType.id"
        def whereConditions = []
        def queryParams = [:]
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
        def totalResultIds = StatsTripleCursor.executeQuery(hql, queryParams)

        def criteria = StatsTripleCursor.createCriteria()
        def results = criteria.list(max: result.max, offset: result.offset) {
            projections {
                groupProperty('supplierId', 'supplierId')
                groupProperty('customerId', 'customerId')
                groupProperty('factType', 'factType')
                max('haveUpTo', 'haveUpTo')
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
        result.user = User.get(springSecurityService.principal.id)
        def providersWithStatssid = factService.providersWithStatssid()
        def providerList = []
        if (!result.institutionList.isEmpty()) {
            def joinedInstitutions = result.institutionList.id.join(',')
            providersWithStatssid.each {
                def hql = "select s.id from Subscription s join s.orgRelations as institution " +
                    "where institution.org.id in (${joinedInstitutions}) and s.status.value!=:status and exists (select 1 from IssueEntitlement as ie INNER JOIN ie.tipp.pkg.orgs as orgrel where ie.subscription=s and orgrel.org.id=:supplierId)"
                def subsWithIssueEntitlements = Subscription.executeQuery(hql, [supplierId: it.id, status: 'Deleted'])
                def listItem = [:]
                listItem.id = it.id
                listItem.name = it.name
                listItem.optionDisabled = (subsWithIssueEntitlements.size() == 0)
                providerList.add(listItem)
            }
        }
        result.providerList = providerList
        result.institutionsWithFacts = factService.getFactInstitutionList()
        result.providersWithFacts = factService.getFactProviderList()
        result.natstatProviders = StatsTripleCursor.withCriteria {
            projections {
                distinct("supplierId")
            }
        }
        result.natstatInstitutions = StatsTripleCursor.withCriteria {
            projections {
                distinct("customerId")
            }
        }
        result.cursorCount = factService.getSupplierCursorCount()
        result.apiKey = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("API Key"), result.institution)
        result.requestor = OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)

        if (statsSyncService.getErrors()) {
            flash.error = statsSyncService.errors.join('</br>')
        }
        return result
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def abort()
    {
        def result = initResult()
        statsSyncService.setErrors([])
        statsSyncService.running = false
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    def fetchSelection()
    {
        // TODO when we switch to global API Key / Requestor, query SUSHI Service status endpoint here
        // Do not continue if service is not active or there is an error with the API Credentials.
        statsSyncService.setErrors([])
        def result = initResult()
        statsSyncService.addFilters(params)
        statsSyncService.doSync()
        if (statsSyncService.getErrors()) {
            flash.error = statsSyncService.errors.join('</br>')
        }
        redirect(view: "index", model: result)
    }

    @Secured(['ROLE_STATISTICS_EDITOR','ROLE_ADMIN'])
    @Transactional
    def deleteAll()
    {
        statsSyncService.setErrors([])
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
        statsSyncService.setErrors([])
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
