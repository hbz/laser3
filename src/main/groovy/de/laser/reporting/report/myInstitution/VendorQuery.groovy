package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.storage.BeanStore
import grails.web.servlet.mvc.GrailsParameterMap

class VendorQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()

//        println 'VendorQuery.query()'
//        println params

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)
//        List<Long> orphanedIdList = BaseFilter.getCachedFilterIdList(prefix + 'Orphaned', params)

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
//            handleGenericAllSignOrphanedQuery(
//                    params.query,
//                    'select v.id, v.name, 1, false from Vendor v where v.id in (:idList) order by v.name',
//                    'select v.id from Vendor v where v.id in (:idList) and v.name = :d order by v.id',
//                    idList,
//                    orphanedIdList,
//                    result
//            )
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
                PROPERTY_QUERY[0] + 'from Vendor v join v.' + refdata + ' p where v.id in (:idList)' + PROPERTY_QUERY[1],
                'select v.id from Vendor v join v.' + refdata + ' p where v.id in (:idList) and p.id = :d order by v.name',
                'select distinct v.id from Vendor v where v.id in (:idList) and v.' + refdata + ' is null',
                idList,
                result
        )
    }
}