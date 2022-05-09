package de.laser.ctrl


import de.laser.*
import de.laser.stats.StatsTripleCursor
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This service is a mirror of the {@link UsageController} containing its data manipulation methods
 */
@Transactional
class UsageControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    FactService factService
    StatsSyncService statsSyncService

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Gets the current state of the Nationaler Statistikserver data
     * @param controller unused
     * @param params unused
     * @return a map containing the current overview of the Nationaler Statistikserver state, platforms and participants
     */
    Map<String, Object> initResult(UsageController controller, GrailsParameterMap params) {
        Map<String, Object> result = [:]

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
        result.user = contextService.getUser()

        ArrayList platformsWithNatstatId = factService.platformsWithNatstatId()

        ArrayList providerList = []
        if (!result.institutionList.isEmpty()) {
            String joinedInstitutions = result.institutionList.id.join(',')
            platformsWithNatstatId.each {
                String hql = "select s.id from Subscription s join s.orgRelations as institution " +
                        "where institution.org.id in (${joinedInstitutions}) and exists (select 1 from IssueEntitlement as ie INNER JOIN ie.tipp.platform  as platform where ie.subscription=s and platform.id=:platform_id)"
                ArrayList subsWithIssueEntitlements = Subscription.executeQuery(hql, [platform_id: it.id])
                LinkedHashMap<String,Object> listItem = [:]
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
            order("supplierId", "asc")
        }

        String institutionsForQuery = StatsTripleCursor.withCriteria {
            projections {
                distinct("customerId")
            }
            order("customerId", "asc")
        }.collect {"'$it'"}.join(',')

        String hql = "select ident.org, ident from Identifier as ident where ident.value in (${institutionsForQuery})"
        result.natstatInstitutions = institutionsForQuery ? Org.executeQuery(hql) : []
        result.cursorCount = factService.getSupplierCursorCount()

        if (statsSyncService.errors) {
            result.tmpFlashError = statsSyncService.errors.join('</br>')
        }
        statsSyncService.errors = []

        return result
    }
}