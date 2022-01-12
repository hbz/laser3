package de.laser

import de.laser.helper.RDStore
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.reporting.report.local.SubscriptionReport
import grails.gorm.transactions.Transactional

import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This service manages data retrieval for the object-related reporting
 */
@Transactional
class ReportingLocalService {

    def contextService
    def financeService
    def financeControllerService

    static final String TMPL_PATH_CHART = '/subscription/reporting/chart/'
    static final String TMPL_PATH_DETAILS = '/subscription/reporting/details/'

    // ----- <X>Controller.reporting() -----

    // ----- 1 - filter

    // ----- 2 - chart

    /**
     * Prepares the chart with the given input
     * @param result the result map containing the data for the chart output
     * @param params the request parameter map
     * @throws Exception
     */
    void doChart (Map<String, Object> result, GrailsParameterMap params) throws Exception {

        if (params.query) {

            GrailsParameterMap clone = params.clone() as GrailsParameterMap // TODO: simplify
            String prefix = clone.query.split('-')[0]
            Subscription sub = Subscription.get( params.id )

            if (prefix in ['timeline']) {
                Map<String, Object> queryCfg = SubscriptionReport.getCurrentTimelineConfig( sub ).getAt('default').getAt(clone.query) as Map
                result.putAll( SubscriptionReport.query(clone) )
                //result.labels.tooltip = queryCfg.getAt('label') // TODO - used for CSV-Export only
                result.labels.chart = queryCfg.getAt('chartLabels').collect{ SubscriptionReport.getMessage('timeline.chartLabel.' + it) } // TODO
                result.tmpl = TMPL_PATH_CHART + queryCfg.getAt('chartTemplate')
            }
            else {
                result.putAll( SubscriptionReport.query(clone) )
                result.labels.tooltip = BaseQuery.getQueryLabels(SubscriptionReport.getCurrentConfig(sub), clone).get(1)
                result.tmpl = TMPL_PATH_CHART + 'default'
            }

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, params.token )
            if (! rCache.exists()) {
                ReportingCache.initSubscriptionCache( params.long('id'), params.token )
            }
            rCache.writeDetailsCache( null )
            rCache.writeQueryCache(result)
        }
    }

    // ----- 3 - details

    /**
     * Prepares the chart details with the given input
     * @param result the result map containing the data for the chart output
     * @param params the request parameter map
     * @throws Exception
     */
    void doChartDetails (Map<String, Object> result, GrailsParameterMap params) throws Exception {

        // TODO : SESSION TIMEOUT

        if (params.query) {
            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, params.token )

            List<Long> idList = [], plusIdList = [], minusIdList = []
            String label

            rCache.readQueryCache().dataDetails.each{ it ->
                if ( it.get('id') == params.long('id') || it.get('id').toString() == params.idx ) { // TODO @ null
                    idList = it.get('idList')
                    plusIdList = it.get('plusIdList')
                    minusIdList = it.get('minusIdList')
                    label = it.get('label') // todo
                    return
                }
            }

            Map<String, Object> cfg = [:]
            Subscription sub = Subscription.get( params.token.split('#')[1] )
            Map<String, Object> subConf = SubscriptionReport.getCurrentConfig( sub )

            String prefix = params.query.split('-')[0]
            if (prefix == 'timeline') {
                cfg = subConf.base.timeline.getAt('default').get( params.query )
            }

            if (params.query == 'timeline-cost') {
                result.labels = SubscriptionReport.getTimelineQueryLabels(params)

                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('id', params.id)

                Map<String, Object> fsCifsMap = financeControllerService.getResultGenerics(clone)
                fsCifsMap.put('max', 5000)
                Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, fsCifsMap)

                result.list              = finance.cons.costItems ?: []
                result.relevantCostItems = result.list.findAll{ it.costItemElementConfiguration in [RDStore.CIEC_POSITIVE, RDStore.CIEC_NEGATIVE]}
                result.neutralCostItems  = result.list.minus( result.relevantCostItems )

                result.billingSums = finance.cons.sums?.billingSums ?: []
                result.localSums   = finance.cons.sums?.localSums ?: []
                result.tmpl        = TMPL_PATH_DETAILS + cfg.getAt('detailsTemplate')
            }
            else if (params.query in ['timeline-entitlement', 'timeline-member']) {
                result.labels = SubscriptionReport.getTimelineQueryLabels(params)

                if (params.query == 'timeline-entitlement') {
                    String hql = 'select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortname, tipp.name'

                    result.list      = idList      ? TitleInstancePackagePlatform.executeQuery( hql, [idList: idList] ) : []
                    result.plusList  = plusIdList  ? TitleInstancePackagePlatform.executeQuery( hql, [idList: plusIdList] ) : []
                    result.minusList = minusIdList ? TitleInstancePackagePlatform.executeQuery( hql, [idList: minusIdList] ) : []
                    result.tmpl      = TMPL_PATH_DETAILS + cfg.getAt('detailsTemplate')
                }
                else {
                    String hql = 'select o from Org o where o.id in (:idList) order by o.sortname, o.name'

                    result.list      = idList      ? Org.executeQuery( hql, [idList: idList] ) : []
                    result.plusList  = plusIdList  ? Org.executeQuery( hql, [idList: plusIdList] ) : []
                    result.minusList = minusIdList ? Org.executeQuery( hql, [idList: minusIdList] ) : []
                    result.tmpl      = TMPL_PATH_DETAILS + cfg.getAt('detailsTemplate')
                }
            }
            else if (params.query == 'timeline-annualMember-subscription') {
                result.labels = SubscriptionReport.getTimelineQueryLabelsForAnnual(params)

                result.list = Subscription.executeQuery( 'select sub from Subscription sub where sub.id in (:idList) order by sub.name, sub.startDate, sub.endDate', [idList: idList])
                result.tmpl = TMPL_PATH_DETAILS + cfg.getAt('detailsTemplate')
            }
            else {
                if (prefix in [ 'tipp' ]) {
                    GrailsParameterMap clone = params.clone() as GrailsParameterMap
                    clone.setProperty('label', label) // todo

                    result.labels = BaseQuery.getQueryLabels( subConf, clone ) // TODO
                    result.list   = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortname, tipp.name', [idList: idList])
                    result.tmpl   = TMPL_PATH_DETAILS + 'entitlement'
                }
                else if (prefix in [ 'member' ]) {
                    GrailsParameterMap clone = params.clone() as GrailsParameterMap
                    clone.setProperty('label', label) // todo

                    result.labels = BaseQuery.getQueryLabels( subConf, clone ) // TODO
                    result.list   = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
                    result.tmpl   = TMPL_PATH_DETAILS + 'organisation'
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
            ]
            if (result.list)      { detailsCache.putAt( 'idList', result.list.collect{ it.id } ) } // only existing ids
            if (result.plusList)  { detailsCache.putAt( 'plusIdList', result.plusList.collect{ it.id } ) } // only existing ids
            if (result.minusList) { detailsCache.putAt( 'minusIdList', result.minusList.collect{ it.id } ) } // only existing ids

            if (result.billingSums != null) { detailsCache.putAt( 'billingSums', result.billingSums ) }
            if (result.localSums != null)   { detailsCache.putAt( 'localSums', result.localSums ) }

            rCache.writeDetailsCache( detailsCache )
        }
    }
}
