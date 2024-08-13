package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.License
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import de.laser.storage.BeanStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class LicenseQuery extends BaseQuery {

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
            else if (params.query in ['license-x-provider']) {

                result.data = Provider.executeQuery(
                        'select pro.id, pro.name, count(*) from ProviderRole pr join pr.provider pro where pro.id in (:providerIdList) and pr.license.id in (:idList) group by pro.id order by pro.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                )
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: License.executeQuery(
                                    'select lic.id from ProviderRole pr join pr.license lic join pr.provider pro where lic.id in (:idList) and pro.id = :d order by lic.reference',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? License.executeQuery('select lic.id from License lic where lic.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['license-x-vendor']) {

                result.data = Vendor.executeQuery(
                        'select v.id, v.name, count(*) from VendorRole vr join vr.vendor v where v.id in (:vendorIdList) and vr.license.id in (:idList) group by v.id order by v.name',
                        [vendorIdList: BaseFilter.getCachedFilterIdList('vendor', params), idList: idList]
                )
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: License.executeQuery(
                                    'select lic.id from VendorRole vr join vr.license lic join vr.vendor v where lic.id in (:idList) and v.id = :d order by lic.reference',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? License.executeQuery('select lic.id from License lic where lic.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_VENDOR_LABEL, noDataList, result)
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                REFDATA_QUERY[0] + 'from License l join l.' + refdata + ' ref where l.id in (:idList)' + REFDATA_QUERY[1],
                'select l.id from License l join l.' + refdata + ' ref where l.id in (:idList) and ref.id = :d order by l.reference',
                'select distinct l.id from License l where l.id in (:idList) and l.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
