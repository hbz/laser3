package de.laser

import de.laser.finance.CostItem
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.report.myInstitution.*
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.commons.lang3.RandomStringUtils

/**
 * This service manages data retrieval for the global, context-free reporting
 */
@Transactional
class ReportingGlobalService {

    ContextService contextService

    public static final String TMPL_PATH_CHART   = '/myInstitution/reporting/chart/'
    public static final String TMPL_PATH_DETAILS = '/myInstitution/reporting/details/'

    // ----- MyInstitutionController.reporting() -----

    /**
     * Sets the filter for the given base config
     * @param result the result map containing the filter data
     * @param params the request parameter map
     */
    void doFilter(Map<String, Object> result, GrailsParameterMap params) {

        result.filter = params.filter
        result.token  = /* params.token ?: */ RandomStringUtils.randomAlphanumeric(16)

        result.cfgQueryList = [:]
        result.cfgDistributionList = [:]

        if (params.filter == BaseConfig.KEY_COSTITEM) {
            doFilterCostItem(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_ISSUEENTITLEMENT) {
            doFilterIssueEntitlement(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_LICENSE) {
            doFilterLicense(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_ORGANISATION) {
            doFilterOrganisation(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_PACKAGE) {
            doFilterPackage(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_PLATFORM) {
            doFilterPlatform(result, params.clone() as GrailsParameterMap)
        }
        else if (params.filter == BaseConfig.KEY_SUBSCRIPTION) {
            doFilterSubscription(result, params.clone() as GrailsParameterMap)
        }
    }

    // ----- 1 - filter

    /**
     * Prepares the cost item filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
    void doFilterCostItem (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = CostItemFilter.filter(params)

        result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base.query.default )
    }

    /**
     * Prepares the issue entitlement filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
    void doFilterIssueEntitlement (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = IssueEntitlementFilter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig(BaseConfig.KEY_ISSUEENTITLEMENT).get(pk).query.default)
        }
        result.cfgDistributionList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).base.distribution )
    }

    /**
     * Prepares the license filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
    void doFilterLicense (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = LicenseFilter.filter(params)

        BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).get( pk ).query.default )
        }
        result.cfgDistributionList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.distribution )
    }

    /**
     * Prepares the organisation filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
    void doFilterOrganisation (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = OrganisationFilter.filter(params)

        if (params.get('filter:org_source').endsWith('-providerAndAgency')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.providerAndAgency )
        }
        else if (params.get('filter:org_source').endsWith('-provider')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.provider )
        }
        else if (params.get('filter:org_source').endsWith('-agency')) {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.agency )
        }
        else {
            result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.query.default )
        }
        result.cfgDistributionList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.distribution )
    }

    /**
     * Prepares the package filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
    void doFilterPackage (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = PackageFilter.filter(params)

        //result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base.query.default )
        BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig(BaseConfig.KEY_PACKAGE).get(pk).query.default)
        }
        result.cfgDistributionList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base.distribution )
    }

    /**
     * Prepares the platform filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
    void doFilterPlatform (Map<String, Object> result, GrailsParameterMap params) {

        result.filterResult = PlatformFilter.filter(params)

        //result.cfgQueryList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base.query.default )
        BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).keySet().each{ pk ->
            result.cfgQueryList.putAll(BaseConfig.getCurrentConfig(BaseConfig.KEY_PLATFORM).get(pk).query.default)
        }
        result.cfgDistributionList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base.distribution )
    }

    /**
     * Prepares the subscription filter and writes the result to the result map
     * @param result the result map
     * @param params the request parameter map
     */
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
        result.cfgDistributionList.putAll( BaseConfig.getCurrentConfig( BaseConfig.KEY_SUBSCRIPTION ).base.distribution )
    }

    // ----- 2 - chart

    /**
     * Prepares the chart and writes the result to the given result map
     * @param result the result map containing the data and the chart parameters
     * @param params the request parameter map
     * @throws Exception
     */
    void doChart(Map<String, Object> result, GrailsParameterMap params) throws Exception {

        if (params.query) {
            Closure getTooltipLabels = { GrailsParameterMap pm ->
                BaseQuery.getQueryLabels(BaseConfig.getCurrentConfig( pm.filter ), pm).get(1)
            }

            GrailsParameterMap clone = params.clone() as GrailsParameterMap // clone.put() ..

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )
            Map<String, Object> cacheMap = rCache.get()

            // TODO -- SESSION TIMEOUT
            if (cacheMap) {
                clone.put('filterCache', cacheMap.filterCache)
            }

            def (String prefix, String suffix) = params.query.split('-')

            // println 'doChart() prefix : ' + prefix + ', suffix: ' + suffix
            result.tmpl = TMPL_PATH_CHART + 'generic'

            if (prefix in [ BaseConfig.KEY_COSTITEM ]) {
                result.putAll( CostItemQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)
            }
            else if (prefix in [ BaseConfig.KEY_ISSUEENTITLEMENT ]) {
                result.putAll( IssueEntitlementQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).base.distribution.getAt('default').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getDistributionLabel(BaseConfig.KEY_ISSUEENTITLEMENT, 'chartLabel.' + it) }
                    result.tmpl = TMPL_PATH_CHART + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in [ BaseConfig.KEY_LICENSE ]) {
                result.putAll( LicenseQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_LICENSE ).base.distribution.getAt('default').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getDistributionLabel(BaseConfig.KEY_LICENSE, 'chartLabel.' + it) }
                    result.tmpl = TMPL_PATH_CHART + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in ['org', 'member', 'consortium', 'provider', 'agency', 'licensor']) {
                result.putAll( OrganisationQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_ORGANISATION ).base.distribution.getAt('default').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getDistributionLabel(BaseConfig.KEY_ORGANISATION, 'chartLabel.' + it) }
                    result.tmpl = TMPL_PATH_CHART + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in [ BaseConfig.KEY_PACKAGE ]) {
                result.putAll( PackageQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)

                if (suffix in ['*']) {
                    //println 'ReportingGlobalService KEY_PACKAGE *'
                    result.labels.chart = [BaseConfig.getLabel('reporting.chart.chartLabel.counterpart.label'), BaseConfig.getLabel('reporting.chart.chartLabel.noCounterpart.label')]
                    result.tmpl = TMPL_PATH_CHART + 'generic_signOrphaned'
                }
                else if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base.distribution.getAt('default').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getDistributionLabel(BaseConfig.KEY_PACKAGE, 'chartLabel.' + it) }
                    result.tmpl = TMPL_PATH_CHART + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in [ BaseConfig.KEY_PLATFORM ]) {
                result.putAll( PlatformQuery.query(clone) )
                result.labels.tooltip = getTooltipLabels(clone)

                if (suffix in ['*']) {
                    //println 'ReportingGlobalService KEY_PLATFORM *'
                    result.labels.chart = [BaseConfig.getLabel('reporting.chart.chartLabel.counterpart.label'), BaseConfig.getLabel('reporting.chart.chartLabel.noCounterpart.label')]
                    result.tmpl = TMPL_PATH_CHART + 'generic_signOrphaned'
                }
                else if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base.distribution.getAt('default').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getDistributionLabel(BaseConfig.KEY_PLATFORM, 'chartLabel.' + it) }
                    result.tmpl = TMPL_PATH_CHART + cfg.getAt('chartTemplate')
                }
            }
            else if (prefix in [ BaseConfig.KEY_SUBSCRIPTION, 'memberSubscription' ]) {
                result.putAll(SubscriptionQuery.query(clone))
                result.labels.tooltip = getTooltipLabels(clone)

                if (suffix in ['x']) {
                    Map<String, Object> cfg = BaseConfig.getCurrentConfig(BaseConfig.KEY_SUBSCRIPTION).base.distribution.getAt('default').getAt(clone.query) as Map

                    result.labels.chart = cfg.getAt('chartLabels').collect{ BaseConfig.getDistributionLabel(BaseConfig.KEY_SUBSCRIPTION, 'chartLabel.' + it)  }
                    result.tmpl = TMPL_PATH_CHART + cfg.getAt('chartTemplate')
                }
            }

            // TODO
            cacheMap.remove('detailsCache')
            cacheMap.queryCache = [:]
            cacheMap.queryCache.putAll(result)

            rCache.put( cacheMap )
        }
    }

    // ----- 3 - details

    /**
     * Prepares the chart details and adds the result to the given result map
     * @param result the result map containing the data and the chart parameters
     * @param params the request parameter map
     * @throws Exception
     */
    void doChartDetails(Map<String, Object> result, GrailsParameterMap params) throws Exception {

        if (params.query) {

            def (String prefix, String suffix) = params.query.split('-')
            List idList = []

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, params.token )

            //println 'AjaxHtmlController.chartDetails()'
            rCache.readQueryCache().dataDetails.each{ it ->
                if (it.get('id') == params.long('id')) {
                    idList = it.get('idList')
                    return
                }
            }
            //println 'ReportingGlobalService.doChartDetails() -> filter: ' + params.filter + ', prefix:' + prefix + ', suffix:' + suffix
            Map<String, Object> cfg = BaseConfig.getCurrentConfigByFilter( params.filter )

            if (cfg) {
                result.labels = BaseQuery.getQueryLabels( cfg, params )
                //println 'BaseQuery.getQueryLabels( cfg, params ) ' + BaseQuery.getQueryLabels( cfg, params )
                if (suffix in ['x']) {

                    String tmpl = cfg.base.distribution.get('default').get(params.query).detailsTemplate
                    result.tmpl = TMPL_PATH_DETAILS + tmpl

                    if (! idList) {
                        result.list = []
                    }
                    else if (tmpl == BaseConfig.KEY_COSTITEM) {
                        result.list = CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.costTitle', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_ISSUEENTITLEMENT) {
                        result.list = License.executeQuery('select ie from IssueEntitlement ie where ie.id in (:idList) order by ie.name', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_LICENSE) {
                        result.list = License.executeQuery('select l from License l where l.id in (:idList) order by l.sortableReference, l.reference', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_ORGANISATION) {
                        result.list = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_PACKAGE) {
                        result.list = Package.executeQuery('select pkg from Package pkg where pkg.id in (:idList) order by pkg.name', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_PLATFORM) {
                        result.list = Platform.executeQuery('select plt from Platform plt where plt.id in (:idList) order by plt.name', [idList: idList])
                    }
                    else if (tmpl == BaseConfig.KEY_SUBSCRIPTION) {
                        result.list = Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList])
                    }
                }
                else {
                    result.tmpl = TMPL_PATH_DETAILS + prefix

                    if (prefix in [ BaseConfig.KEY_COSTITEM ]) {
                        result.list = idList ? CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.costTitle', [idList: idList]) : []
                    }
                    else if (prefix == BaseConfig.KEY_ISSUEENTITLEMENT) {
                        result.list = License.executeQuery('select ie from IssueEntitlement ie where ie.id in (:idList) order by ie.name', [idList: idList])
                    }
                    else if (prefix in [ BaseConfig.KEY_LICENSE ]) {
                        result.list = idList ? License.executeQuery('select l from License l where l.id in (:idList) order by l.sortableReference, l.reference', [idList: idList]) : []
                    }
                    else if (prefix in [ 'licensor', 'org', 'member', 'consortium', 'provider', 'agency' ]) {
                        result.list = idList ? Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList]) : []
                        result.tmpl = TMPL_PATH_DETAILS + BaseConfig.KEY_ORGANISATION
                    }
                    else if (prefix in [ BaseConfig.KEY_PACKAGE ]) {
                        result.list = idList ? Package.executeQuery('select pkg from Package pkg where pkg.id in (:idList) order by pkg.name', [idList: idList]) : []
                    }
                    else if (prefix in [ BaseConfig.KEY_PLATFORM ]) {
                        result.list = idList ? Platform.executeQuery('select plt from Platform plt where plt.id in (:idList) order by plt.name', [idList: idList]) : []
                    }
                    else if (prefix in [ BaseConfig.KEY_SUBSCRIPTION, 'memberSubscription' ]) {
                        result.list = idList ? Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList]) : []
                        result.tmpl = TMPL_PATH_DETAILS + BaseConfig.KEY_SUBSCRIPTION
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
