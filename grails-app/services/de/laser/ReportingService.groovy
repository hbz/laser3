package de.laser

import de.laser.finance.CostItem
import de.laser.helper.SessionCacheWrapper
import de.laser.reporting.myInstitution.CostItemFilter
import de.laser.reporting.myInstitution.CostItemQuery
import de.laser.reporting.myInstitution.LicenseFilter
import de.laser.reporting.myInstitution.LicenseQuery
import de.laser.reporting.myInstitution.OrganisationFilter
import de.laser.reporting.myInstitution.OrganisationQuery
import de.laser.reporting.myInstitution.SubscriptionFilter
import de.laser.reporting.myInstitution.SubscriptionQuery
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseQuery
import de.laser.reporting.subscription.SubscriptionReporting
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class ReportingService {

    def contextService

    // ----- MyInstitutionController.reporting() -----

    void doFilter (Map<String, Object> result, GrailsParameterMap params) {

        result.filter = params.filter
        result.token  = params.token ?: RandomStringUtils.randomAlphanumeric(16)

        result.cfgQueryList = [:]
        result.cfgQuery2List = [:]

        if (params.filter == BaseConfig.KEY_COSTITEM) {
            doFilterCostItem(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_LICENSE) {
            doFilterLicense(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_ORGANISATION) {
            doFilterOrganisation(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_SUBSCRIPTION) {
            doFilterSubscription(result, params.clone() as GrailsParameterMap)
        }
    }

    // ----- 1 - filter

    void doFilterCostItem (Map<String, Object> result, GrailsParameterMap params) {

        CostItemFilter filter = new CostItemFilter()
        result.filterResult = filter.filter(params)

        result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base.query.default )
    }

    void doFilterLicense (Map<String, Object> result, GrailsParameterMap params) {

        LicenseFilter filter = new LicenseFilter()
        result.filterResult = filter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).get( pk ).query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.query2 ) // Verteilung
    }

    void doFilterOrganisation (Map<String, Object> result, GrailsParameterMap params) {

        OrganisationFilter filter = new OrganisationFilter()
        result.filterResult = filter.filter(params)

        if (params.get('filter:org_source').contains('providerAndAgency')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.providerAndAgency )
        }
        else if (params.get('filter:org_source').contains('provider')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.provider )
        }
        else if (params.get('filter:org_source').contains('agency')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.agency )
        }
        else {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query2 ) // Verteilung
    }

    void doFilterSubscription (Map<String, Object> result, GrailsParameterMap params) {

        SubscriptionFilter filter = new SubscriptionFilter()
        result.filterResult = filter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).get( pk ).query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base.query2 ) // Verteilung
    }

    // ----- 2 - chart

    void doChart (Map<String, Object> result, GrailsParameterMap params) {

        if (params.context == BaseConfig.KEY_MYINST && params.query) {

            Closure getTooltipLabels = { GrailsParameterMap pm ->
                if (pm.filter == 'license') {
                    BaseQuery.getQueryLabels(BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ), pm).get(1)
                }
                else if (pm.filter == 'organisation') {
                    BaseQuery.getQueryLabels(BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ), pm).get(1)
                }
                else if (pm.filter == 'subscription') {
                    BaseQuery.getQueryLabels(BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ), pm).get(1)
                }
            }

            GrailsParameterMap clone = params.clone() as GrailsParameterMap // clone.put() ..

            SessionCacheWrapper sessionCache = contextService.getSessionCache()
            Map<String, Object> cacheMap = sessionCache.get("MyInstitutionController/reporting/" + params.token)
            if (cacheMap) {
                clone.put('filterCache', cacheMap.filterCache)
            }

            String prefix = params.query.split('-')[0]

            if (prefix in ['license']) {
                result.putAll( LicenseQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = '/myInstitution/reporting/chart/generic'

                if (clone.query.endsWith('assignment')) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.query2.getAt('Verteilung').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels')
                    result.tmpl = '/myInstitution/reporting/chart/' + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['org', 'member', 'consortium', 'provider', 'agency', 'licensor']) {
                result.putAll( OrganisationQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = '/myInstitution/reporting/chart/generic'

                if (clone.query.endsWith('assignment')) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query2.getAt('Verteilung').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels')
                    result.tmpl = '/myInstitution/reporting/chart/' + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['subscription']) {
                result.putAll(SubscriptionQuery.query(clone))
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = '/myInstitution/reporting/chart/generic'

                if (clone.query.endsWith('assignment')) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig(BaseConfig.KEY_SUBSCRIPTION).base.query2.getAt('Verteilung').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels')
                    result.tmpl = '/myInstitution/reporting/chart/' + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['costItem']) {
                result.putAll( CostItemQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = '/myInstitution/reporting/chart/generic'
            }

            // TODO
            cacheMap.remove('detailsCache')
            cacheMap.queryCache = [:]
            cacheMap.queryCache.putAll(result)

            sessionCache.put("MyInstitutionController/reporting/" + params.token, cacheMap)
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION && params.query) {
            GrailsParameterMap clone = params.clone() as GrailsParameterMap // TODO: simplify
            String prefix = clone.query.split('-')[0]

            if (prefix in ['timeline']) {
                result.putAll( SubscriptionReporting.query(clone) )
                result.labels.chart = SubscriptionReporting.CONFIG.base.query2.getAt('Entwicklung').getAt(clone.query).getAt('chartLabels')

                if (clone.query in ['timeline-cost']) {
                    result.tmpl = '/subscription/reporting/chart/timeline-cost'
                }
                else {
                    result.tmpl = '/subscription/reporting/chart/generic-timeline'
                }
            }
            else {
                result.putAll( SubscriptionReporting.query(clone) )
                result.labels.tooltip = BaseQuery.getQueryLabels(SubscriptionReporting.CONFIG, clone).get(1)

                result.tmpl = '/subscription/reporting/chart/generic-bar'
            }
        }
    }

    // ----- 3 - details

    void doChartDetails (Map<String, Object> result, GrailsParameterMap params) {

        if (params.context == BaseConfig.KEY_MYINST && params.query) {

            String prefix = params.query.split('-')[0]
            List idList = []

            SessionCacheWrapper sessionCache = contextService.getSessionCache()
            Map<String, Object> cacheMap = sessionCache.get("MyInstitutionController/reporting/" + params.token)

            //println 'AjaxHtmlController.chartDetails()'
            cacheMap.queryCache.dataDetails.each{ it ->
                if (it.get('id') == params.long('id')) {
                    idList = it.get('idList')
                    return
                }
            }

            Map<String, Object> cfg

            if (prefix in ['license', 'licensor']) {
                cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE )
            }
            else if (prefix in ['org']) {
                cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION )
            }
            else if (prefix in ['subscription', 'member', 'consortium', 'provider', 'agency']) {
                cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION )
            }
            else if (prefix in ['costItem']) {
                cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM )
            }

            if (cfg) {
                result.labels = BaseQuery.getQueryLabels( cfg, params )

                if (params.query.endsWith('assignment')) {

                    String tmpl = cfg.base.query2.get('Verteilung').get(params.query).detailsTemplate
                    result.tmpl = '/myInstitution/reporting/details/' + tmpl

                    if (! idList) {
                        result.list = []
                    }
                    else if (tmpl == BaseConfig.KEY_LICENSE) {
                        result.list = License.executeQuery('select l from License l where l.id in (:idList) order by l.sortableReference, l.reference', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_ORGANISATION) {
                        result.list = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_SUBSCRIPTION) {
                        result.list = Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_COSTITEM) {
                        result.list = CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.costTitle', [idList: idList])
                    }
                }
                else {

                    if (prefix in ['license']) {
                        result.list = idList ? License.executeQuery('select l from License l where l.id in (:idList) order by l.sortableReference, l.reference', [idList: idList]) : []
                        result.tmpl = '/myInstitution/reporting/details/license'
                    }
                    else if (prefix in ['licensor', 'org', 'member', 'consortium', 'provider', 'agency']) {
                        result.list = idList ? Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList]) : []
                        result.tmpl = '/myInstitution/reporting/details/organisation'
                    }
                    else if (prefix in ['subscription']) {
                        result.list = idList ? Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList]) : []
                        result.tmpl = '/myInstitution/reporting/details/subscription'
                    }
                    else if (prefix in ['costItem']) {
                        result.list = idList ? CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.costTitle', [idList: idList]) : []
                        result.tmpl = '/myInstitution/reporting/details/costItem'
                    }
                }
            }
            cacheMap.queryCache.labels.put('labels', result.labels)

            cacheMap.detailsCache = [
                    prefix : prefix,
                    id :     params.long('id'),
                    idList : result.list.collect{ it.id }, // only existing ids
                    tmpl :   result.tmpl
            ]

            sessionCache.put("MyInstitutionController/reporting/" + params.token, cacheMap)
        }
        else if (params.context == BaseConfig.KEY_SUBSCRIPTION && params.query) {

            if (params.query == 'timeline-cost') {
                result.labels = SubscriptionReporting.getTimelineQueryLabels(params)

                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('id', params.id)
                Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, financeControllerService.getResultGenerics(clone))

                result.billingSums = finance.cons.sums.billingSums ?: []
                result.localSums   = finance.cons.sums.localSums ?: []
                result.tmpl        = '/subscription/reporting/details/timeline/cost'
            }
            else if (params.query in ['timeline-entitlement', 'timeline-member']) {
                result.labels = SubscriptionReporting.getTimelineQueryLabels(params)

                List idList      = params.list('idList[]').collect { it as Long }
                List plusIdList  = params.list('plusIdList[]').collect { it as Long }
                List minusIdList = params.list('minusIdList[]').collect { it as Long }

                if (params.query == 'timeline-entitlement') {
                    String hql = 'select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortName, tipp.name'

                    result.list      = idList      ? TitleInstancePackagePlatform.executeQuery( hql, [idList: idList] ) : []
                    result.plusList  = plusIdList  ? TitleInstancePackagePlatform.executeQuery( hql, [idList: plusIdList] ) : []
                    result.minusList = minusIdList ? TitleInstancePackagePlatform.executeQuery( hql, [idList: minusIdList] ) : []
                    result.tmpl      = '/subscription/reporting/details/timeline/entitlement'
                }
                else {
                    String hql = 'select o from Org o where o.id in (:idList) order by o.sortname, o.name'

                    result.list      = idList      ? Org.executeQuery( hql, [idList: idList] ) : []
                    result.plusList  = plusIdList  ? Org.executeQuery( hql, [idList: plusIdList] ) : []
                    result.minusList = minusIdList ? Org.executeQuery( hql, [idList: minusIdList] ) : []
                    result.tmpl      = '/subscription/reporting/details/timeline/organisation'
                }
            }
            else {
                List idList = params.list('idList[]').collect { it as Long }

                result.labels = BaseQuery.getQueryLabels(SubscriptionReporting.CONFIG, params)
                result.list   = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortName, tipp.name', [idList: idList])
                result.tmpl   = '/subscription/reporting/details/entitlement'
            }
        }
    }

    // ----- helper

    List getCachedFilterIdList(String prefix, GrailsParameterMap params) {

        List<Long> idList = params?.filterCache?.data?.get(prefix + 'IdList')?.collect { it as Long }
        return idList ?: []
    }
}
