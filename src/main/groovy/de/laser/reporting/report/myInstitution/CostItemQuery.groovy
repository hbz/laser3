package de.laser.reporting.report.myInstitution


import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class CostItemQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            println '--- TODO ---'
        }
        else if ( suffix in ['billingCurrency', 'costItemCategory', 'costItemElement', 'costItemStatus', 'costItemElementConfiguration', 'type']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                REFDATA_QUERY[0] + 'from CostItem ci join ci.' + refdata + ' ref where ci.id in (:idList)' + REFDATA_QUERY[1],
                'select ci.id from CostItem ci join ci.' + refdata + ' ref where ci.id in (:idList) and ref.id = :d order by ci.costTitle',
                'select distinct ci.id from CostItem ci where ci.id in (:idList) and ci.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
