package de.laser

import de.laser.finance.CostItem
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.report.myInstitution.*
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.lang3.RandomStringUtils

@Transactional
class ReportingGlobalService {

    def contextService

    static final String TMPL_PATH = '/myInstitution/reporting/'

    // ----- MyInstitutionController.reporting() -----

    void doFilter(Map<String, Object> result, GrailsParameterMap params) {

        result.filter = params.filter
        result.token  = /* params.token ?: */ RandomStringUtils.randomAlphanumeric(16)

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

        result.filterResult = CostItemFilter.filter(params)

        result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base.query.default )
    }

    void doFilterLicense (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = LicenseFilter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).get( pk ).query.default )
        }

        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.query2 ) // Verteilung
    }

    void doFilterOrganisation (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = OrganisationFilter.filter(params)

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

        result.filterResult = SubscriptionFilter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).keySet().each{ pk ->
            //if (pk != 'memberSubscription') {
                result.cfgQueryList.putAll(BaseConfig.getCurrentConfig(BaseConfig.KEY_SUBSCRIPTION).get(pk).query.default)
            //}
        }

        if (! params.get('filter:consortium_source')) {
            result.cfgQueryList.remove('consortium') // ?
        }
        result.cfgQuery2List.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base.query2 ) // Verteilung
    }

    // ----- 2 - chart

    void doChart(Map<String, Object> result, GrailsParameterMap params) throws Exception {

        if (params.query) {

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

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )
            Map<String, Object> cacheMap = rCache.get()

            // TODO -- SESSION TIMEOUT
            if (cacheMap) {
                clone.put('filterCache', cacheMap.filterCache)
            }

            String prefix = params.query.split('-')[0]
            String suffix = params.query.split('-')[1]

            if (prefix in ['license']) {
                result.putAll( LicenseQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = TMPL_PATH + 'chart/generic'

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.query2.getAt('distribution').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getMessage(BaseConfig.KEY_LICENSE + '.dist.chartLabel.' + it) }
                    result.tmpl = TMPL_PATH + 'chart/' + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['org', 'member', 'consortium', 'provider', 'agency', 'licensor']) {
                result.putAll( OrganisationQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = TMPL_PATH + 'chart/generic'

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query2.getAt('distribution').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getMessage(BaseConfig.KEY_ORGANISATION + '.dist.chartLabel.' + it) }
                    result.tmpl = TMPL_PATH + 'chart/' + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['subscription', 'memberSubscription']) {
                result.putAll(SubscriptionQuery.query(clone))
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = TMPL_PATH + 'chart/generic'

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig(BaseConfig.KEY_SUBSCRIPTION).base.query2.getAt('distribution').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getMessage(BaseConfig.KEY_SUBSCRIPTION + '.dist.chartLabel.' + it)  }
                    result.tmpl = TMPL_PATH + 'chart/' + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['costItem']) {
                result.putAll( CostItemQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
                result.tmpl = TMPL_PATH + 'chart/generic'
            }

            // TODO
            cacheMap.remove('detailsCache')
            cacheMap.queryCache = [:]
            cacheMap.queryCache.putAll(result)

            rCache.put( cacheMap )
        }
    }

    // ----- 3 - details

    void doChartDetails(Map<String, Object> result, GrailsParameterMap params) throws Exception {

        if (params.query) {

            String prefix = params.query.split('-')[0]
            String suffix = params.query.split('-')[1]
            List idList = []

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )

            //println 'AjaxHtmlController.chartDetails()'
            rCache.readQueryCache().dataDetails.each{ it ->
                if (it.get('id') == params.long('id')) {
                    idList = it.get('idList')
                    return
                }
            }

            Map<String, Object> cfg = BaseConfig.getCurrentConfigByPrefix( prefix )

            if (cfg) {
                result.labels = BaseQuery.getQueryLabels( cfg, params )

                if (suffix in ['x']) {

                    String tmpl = cfg.base.query2.get('distribution').get(params.query).detailsTemplate
                    result.tmpl = TMPL_PATH + 'details/' + tmpl

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
                        result.tmpl = TMPL_PATH + 'details/license'
                    }
                    else if (prefix in ['licensor', 'org', 'member', 'consortium', 'provider', 'agency']) {
                        result.list = idList ? Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList]) : []
                        result.tmpl = TMPL_PATH + 'details/organisation'
                    }
                    else if (prefix in ['subscription', 'memberSubscription']) {
                        result.list = idList ? Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList]) : []
                        result.tmpl = TMPL_PATH + 'details/subscription'
                    }
                    else if (prefix in ['costItem']) {
                        result.list = idList ? CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.costTitle', [idList: idList]) : []
                        result.tmpl = TMPL_PATH + 'details/costItem'
                    }
                }
            }

            Map<String, Object> currentLabels = rCache.readQueryCache().labels as Map<String, Object>
            currentLabels.labels = result.labels
            rCache.intoQueryCache( 'labels', currentLabels )

            //rCache.intoQueryCache( 'labels', [labels: result.labels] )

            Map<String, Object> detailsCache = [
                    query   : params.query,
                    tmpl    : result.tmpl,
                    id      : params.long('id'),
                    idList  : result.list.collect{ it.id }, // only existing ids
            ]

            rCache.writeDetailsCache( detailsCache )
        }
    }
}
