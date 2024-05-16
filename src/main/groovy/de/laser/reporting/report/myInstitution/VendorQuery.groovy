package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.annotations.UnstableFeature
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.storage.BeanStore
import grails.web.servlet.mvc.GrailsParameterMap

@UnstableFeature
class VendorQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

//        println 'VendorQuery.query() -> ' + params.query + ' : ' + suffix

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select v.name, v.name, count(v.name) from Vendor v where v.id in (:idList) group by v.name order by v.name',
                    'select v.id from Vendor v where v.id in (:idList) and v.name = :d order by v.id',
                    idList,
                    result
            )
        }
        else if ( suffix in ['status']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['individualInvoiceDesign', 'managementOfCredits', 'paperInvoice', 'processingOfCompensationPayments' ]) {

            handleGenericBooleanQuery(
                    params.query,
                    'select v.' + suffix + ', v.' + suffix + ', count(*) from Vendor v where v.id in (:idList) group by v.' + suffix,
                    'select v.id from Vendor v where v.id in (:idList) and v.' + suffix + ' = :d order by v.name',
                    idList,
                    result
            )
        }
        else if ( suffix in ['x']) {

            if (params.query in ['vendor-x-property']) {

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from Vendor v join v.propertySet prop join prop.type pd where v.id in (:idList)',
                        'select v.id from Vendor v join v.propertySet prop join prop.type pd where v.id in (:idList)',
                        idList,
                        contextService.getOrg(),
                        result
                )
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                REFDATA_QUERY[0] + 'from Vendor v join v.' + refdata + ' ref where v.id in (:idList)' + REFDATA_QUERY[1],
                'select v.id from Vendor v join v.' + refdata + ' ref where v.id in (:idList) and ref.id = :d order by v.name',
                'select distinct v.id from Vendor v where v.id in (:idList) and v.' + refdata + ' is null',
                idList,
                result
        )
    }
}
