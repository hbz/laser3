package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.helper.BeanStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class LicenseQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        //println 'LicenseQuery.query() -> ' + params.query + ' : ' + suffix

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select l.reference, l.reference, count(l.reference) from License l where l.id in (:idList) group by l.reference order by l.reference',
                    'select l.id from License l where l.id in (:idList) and l.reference = :d order by l.id',
                    idList,
                    result
            )
        }
        else if ( suffix in ['licenseCategory', 'type', 'status']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['x']) {

            if (params.query in ['license-x-annual']) {

                handleGenericAnnualXQuery(params.query, 'License', idList, result)
            }
            else if (params.query in ['license-x-property']) {

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from License lic join lic.propertySet prop join prop.type pd where lic.id in (:idList)',
                        'select lic.id from License lic join lic.propertySet prop join prop.type pd where lic.id in (:idList)',
                        idList,
                        contextService.getOrg(),
                        result
                )
            }
            else if (params.query in ['license-x-identifier']) {

                handleGenericIdentifierXQuery(
                        params.query,
                        'select ns.id, ns.ns, count(*) from License lic join lic.ids ident join ident.ns ns where lic.id in (:idList)',
                        'select lic.id from License lic join lic.ids ident join ident.ns ns where lic.id in (:idList)',
                        'select lic.id from License lic where lic.id in (:idList)', // inversed idList
                        idList,
                        result
                )
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from License l join l.' + refdata + ' p where l.id in (:idList)' + PROPERTY_QUERY[1],
                'select l.id from License l join l.' + refdata + ' p where l.id in (:idList) and p.id = :d order by l.reference',
                'select distinct l.id from License l where l.id in (:idList) and l.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
