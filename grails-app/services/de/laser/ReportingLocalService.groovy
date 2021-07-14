package de.laser

import de.laser.reporting.ReportingCache
import de.laser.reporting.ReportingCacheHelper
import de.laser.reporting.myInstitution.base.BaseQuery
import de.laser.reporting.local.SubscriptionReporting
import grails.gorm.transactions.Transactional

import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class ReportingLocalService {

    def contextService
    def financeService
    def financeControllerService

    // ----- <X>Controller.reporting() -----

    // ----- 1 - filter

    // ----- 2 - chart

    void doChart (Map<String, Object> result, GrailsParameterMap params) throws Exception {

        if (params.query) {

            GrailsParameterMap clone = params.clone() as GrailsParameterMap // TODO: simplify
            String prefix = clone.query.split('-')[0]
            Subscription sub = Subscription.get( params.id )

            if (prefix in ['timeline']) {
                result.putAll( SubscriptionReporting.query(clone) )
                result.labels.chart = SubscriptionReporting.getCurrentQuery2Config( sub ).getAt('Entwicklung').getAt(clone.query).getAt('chartLabels')

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

            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION )
            if (! rCache.exists()) {
                ReportingCacheHelper.initSubscriptionCache(params.long('id'))
            }
            rCache.writeQueryCache(result)
        }
    }

    // ----- 3 - details

    void doChartDetails (Map<String, Object> result, GrailsParameterMap params) throws Exception {

        // TODO : SESSION TIMEOUT

        if (params.query) {
            ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION )

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

            if (params.query == 'timeline-cost') {
                result.labels = SubscriptionReporting.getTimelineQueryLabels(params)

                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('id', params.id)
                Map<String, Object> finance = financeService.getCostItemsForSubscription(clone, financeControllerService.getResultGenerics(clone))

                result.billingSums = finance.cons.sums?.billingSums ?: []
                result.localSums   = finance.cons.sums?.localSums ?: []
                result.tmpl        = '/subscription/reporting/details/timeline/cost'
            }
            else if (params.query in ['timeline-entitlement', 'timeline-member']) {
                result.labels = SubscriptionReporting.getTimelineQueryLabels(params)

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
                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('label', label) // todo

                result.labels = BaseQuery.getQueryLabels(SubscriptionReporting.CONFIG, clone)
                result.list   = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp where tipp.id in (:idList) order by tipp.sortName, tipp.name', [idList: idList])
                result.tmpl   = '/subscription/reporting/details/entitlement'
            }

            rCache.intoQueryCache( 'labels', [labels: result.labels] )

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
