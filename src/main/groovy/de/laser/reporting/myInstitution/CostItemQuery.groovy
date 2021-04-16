package de.laser.reporting.myInstitution

import de.laser.ContextService
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class CostItemQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        List idList   = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( params.query in ['costItem-type']) {

            processSimpleRefdataQuery(params.query,'type', idList, result)
        }
        else if ( params.query in ['costItem-costItemStatus']) {

            processSimpleRefdataQuery(params.query,'costItemStatus', idList, result)
        }
        else if ( params.query in ['costItem-costItemCategory']) {

            processSimpleRefdataQuery(params.query,'costItemCategory', idList, result)
        }
        else if ( params.query in ['costItem-costItemElement']) {

            processSimpleRefdataQuery(params.query,'costItemElement', idList, result)
        }
        else if ( params.query in ['costItem-costItemElementConfiguration']) {

            processSimpleRefdataQuery(params.query,'costItemElementConfiguration', idList, result)
        }
        else if ( params.query in ['costItem-billingCurrency']) {

            processSimpleRefdataQuery(params.query,'billingCurrency', idList, result)
        }

        result
    }

    static void processSimpleRefdataQuery(String query, String refdata, List idList, Map<String, Object> result) {

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
