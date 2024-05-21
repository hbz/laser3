package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.annotations.UnstableFeature
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.storage.BeanStore
import grails.web.servlet.mvc.GrailsParameterMap

@UnstableFeature
class ProviderQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        //println 'ProviderQuery.query() -> ' + params.query + ' : ' + suffix

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select pro.name, pro.name, count(pro.name) from Provider pro where pro.id in (:idList) group by pro.name order by pro.name',
                    'select pro.id from Provider pro where pro.id in (:idList) and pro.name = :d order by pro.id',
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
                    'select pro.' + suffix + ', pro.' + suffix + ', count(*) from Provider pro where pro.id in (:idList) group by pro.' + suffix,
                    'select pro.id from Provider pro where pro.id in (:idList) and pro.' + suffix + ' = :d order by pro.name',
                    idList,
                    result
            )
        }
        else if ( suffix in ['x']) {

            if (params.query in ['provider-x-identifier']) {

                handleGenericIdentifierXQuery(
                        params.query,
                        'select ns.id, ns.ns, count(*) from Provider pro join pro.ids ident join ident.ns ns where pro.id in (:idList)',
                        'select pro.id from Provider pro join pro.ids ident join ident.ns ns where pro.id in (:idList)',
                        'select pro.id from Provider pro where pro.id in (:idList)', // inversed idList
                        idList,
                        result
                )
            }
            else if (params.query in ['provider-x-property']) {

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from Provider pro join pro.propertySet prop join prop.type pd where pro.id in (:idList)',
                        'select pro.id from Provider pro join pro.propertySet prop join prop.type pd where pro.id in (:idList)',
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
                REFDATA_QUERY[0] + 'from Provider pro join pro.' + refdata + ' ref where pro.id in (:idList)' + REFDATA_QUERY[1],
                'select pro.id from Provider pro join pro.' + refdata + ' ref where pro.id in (:idList) and ref.id = :d order by pro.name',
                'select distinct pro.id from Provider pro where pro.id in (:idList) and pro.' + refdata + ' is null',
                idList,
                result
        )
    }
}
