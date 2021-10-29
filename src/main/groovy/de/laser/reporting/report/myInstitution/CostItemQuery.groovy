package de.laser.reporting.report.myInstitution


import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class CostItemQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        String suffix = params.query.split('-')[1] // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            println '--- TODO ---'
        }
        else if ( suffix in ['type']) {

            _processSimpleRefdataQuery(params.query,'type', idList, result)
        }
        else if ( suffix in ['costItemStatus']) {

            _processSimpleRefdataQuery(params.query,'costItemStatus', idList, result)
        }
        else if ( suffix in ['costItemCategory']) {

            _processSimpleRefdataQuery(params.query,'costItemCategory', idList, result)
        }
        else if ( suffix in ['costItemElement']) {

            _processSimpleRefdataQuery(params.query,'costItemElement', idList, result)
        }
        else if ( suffix in ['costItemElementConfiguration']) {

            _processSimpleRefdataQuery(params.query,'costItemElementConfiguration', idList, result)
        }
        else if ( suffix in ['billingCurrency']) {

            _processSimpleRefdataQuery(params.query,'billingCurrency', idList, result)
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from CostItem ci join ci.' + refdata + ' p where ci.id in (:idList)' + PROPERTY_QUERY[1],
                'select ci.id from CostItem ci join ci.' + refdata + ' p where ci.id in (:idList) and p.id = :d order by ci.costTitle',
                'select distinct ci.id from CostItem ci where ci.id in (:idList) and ci.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
