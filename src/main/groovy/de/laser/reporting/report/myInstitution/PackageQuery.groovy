package de.laser.reporting.report.myInstitution

import de.laser.DeweyDecimalClassification
import de.laser.Language
import de.laser.Org
import de.laser.Platform
import de.laser.Package
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.RDStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class PackageQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        String suffix = params.query.split('-')[1] // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select pkg.name, pkg.name, count(pkg.name) from Package pkg where pkg.id in (:idList) group by pkg.name order by pkg.name',
                    'select pkg.id from Package pkg where pkg.id in (:idList) and pkg.name = :d order by pkg.id',
                    idList,
                    result
            )
        }
        else if ( suffix in ['breakable']) {

            _processSimpleRefdataQuery(params.query, 'breakable', idList, result)
        }
        else if ( suffix in ['consistent']) {

            _processSimpleRefdataQuery(params.query, 'consistent', idList, result)
        }
        else if ( suffix in ['contentType']) {

            _processSimpleRefdataQuery(params.query, 'contentType', idList, result)
        }
        else if ( suffix in ['file']) {

            _processSimpleRefdataQuery(params.query, 'file', idList, result)
        }
        else if ( suffix in ['packageStatus']) {

            _processSimpleRefdataQuery(params.query, 'packageStatus', idList, result)
        }
        else if ( suffix in ['scope']) {

            _processSimpleRefdataQuery(params.query, 'scope', idList, result)
        }
        else if ( suffix in ['x']) {

            if (params.query in ['package-x-identifier']) {

                handleGenericIdentifierXQuery(
                        params.query,
                        'select ns.id, ns.ns, count(*) from Package pkg join pkg.ids ident join ident.ns ns where pkg.id in (:idList)',
                        'select pkg.id from Package pkg join pkg.ids ident join ident.ns ns where pkg.id in (:idList)',
                        'select pkg.id from Package pkg where pkg.id in (:idList)', // inversed idList
                        idList,
                        result
                )
            }
            else if (params.query in ['package-x-provider']) {

                result.data = idList ? Org.executeQuery(
                        'select o.id, o.name, count(*) from Package pkg join pkg.orgs ro join ro.org o where ro.roleType in (:prov) and pkg.id in (:idList) group by o.id order by o.name',
                        [idList: idList, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER]]
                ) : []
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ro.roleType in (:prov) and pkg.id in (:idList) and o.id = :d order by pkg.name',
                                    [idList: idList, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER], d: d[0]]
                            ),
                            value2: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ro.roleType = :prov and pkg.id in (:idList) and o.id = :d order by pkg.name',
                                    [idList: idList, prov: RDStore.OR_CONTENT_PROVIDER, d: d[0]]
                            ).size(),
                            value1: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ro.roleType = :prov and pkg.id in (:idList) and o.id = :d order by pkg.name',
                                    [idList: idList, prov: RDStore.OR_PROVIDER, d: d[0]] // !!!!
                            ).size()
                    ])
                }

                List<Long> noDataList = Package.executeQuery(
                        'select pkg.id from Package pkg where pkg.id in (:idList) and not exists (select ro from OrgRole ro where ro.roleType in (:prov) and ro.pkg.id = pkg.id) order by pkg.name',
                        [idList: idList, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER]]
                )

                if (noDataList) {
                    result.data.add([null, BaseQuery.getMessage(BaseQuery.NO_PROVIDER_LABEL), noDataList.size()])

                    result.dataDetails.add([
                            query : params.query,
                            id    : null,
                            label : BaseQuery.getMessage(BaseQuery.NO_PROVIDER_LABEL),
                            idList: noDataList,
                            value1: 0,
                            value2: noDataList.size()
                    ])
                }
            }
            else if (params.query in ['package-x-platform']) {

                result.data = idList ? Platform.executeQuery(
                        'select p.id, p.name, count(*) from Package pkg join pkg.nominalPlatform p where pkg.id in (:idList) group by p.id order by p.name',
                        [idList: idList]
                ) : []
                result.data.eachWithIndex { d, i ->
                    List<Long> pkgIdList = Package.executeQuery(
                            'select pkg.id from Package pkg join pkg.nominalPlatform p where pkg.id in (:idList) and p.id = :d order by pkg.name',
                            [idList: idList, d: d[0]]
                    )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: pkgIdList
                    ])
                }

                handleGenericNonMatchingData(
                        params.query,
                        'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.nominalPlatform is null',
                        idList,
                        result
                )
            }
            else if (params.query in ['package-x-language']) {

                result.data = idList ? Language.executeQuery(
                        'select lang.id, lang.language.id, count(*) from Package pkg join pkg.languages lang where pkg.id in (:idList) group by lang.id, lang.language.id order by lang.id',
                        [idList: idList]
                ) : []

                result.data.each { d ->
                    d[1] = RefdataValue.get(d[1]).getI10n('value')

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.languages lang where pkg.id in (:idList) and lang.id = :d order by pkg.name',
                                    [d: d[0], idList: idList]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select pkg.id from Package pkg where pkg.id in (:idList)', [idList: nonMatchingIdList]) : []

                if (noDataList) {
                    result.data.add([null, BaseQuery.getMessage(BaseQuery.NO_DATA_LABEL), noDataList.size()])

                    result.dataDetails.add([
                            query : params.query,
                            id    : null,
                            label : BaseQuery.getMessage(BaseQuery.NO_DATA_LABEL),
                            idList: noDataList,
                            value1: 0,
                            value2: noDataList.size()
                    ])
                }
            }
            else if (params.query in ['package-x-ddc']) {

                result.data = idList ? DeweyDecimalClassification.executeQuery(
                        'select ddc.id, ddc.ddc.id, count(*) from Package pkg join pkg.ddcs ddc where pkg.id in (:idList) group by ddc.id, ddc.ddc.id order by ddc.id',
                        [idList: idList]
                ) : []

                result.data.each { d ->
                    d[1] = RefdataValue.get(d[1]).getI10n('value')

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.ddcs ddc where pkg.id in (:idList) and ddc.id = :d order by pkg.name',
                                    [d: d[0], idList: idList]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select pkg.id from Package pkg where pkg.id in (:idList)', [idList: nonMatchingIdList]) : []

                if (noDataList) {
                    result.data.add([null, BaseQuery.getMessage(BaseQuery.NO_DATA_LABEL), noDataList.size()])

                    result.dataDetails.add([
                            query : params.query,
                            id    : null,
                            label : BaseQuery.getMessage(BaseQuery.NO_DATA_LABEL),
                            idList: noDataList,
                            value1: 0,
                            value2: noDataList.size()
                    ])
                }
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from Package pkg join pkg.' + refdata + ' p where pkg.id in (:idList)' + PROPERTY_QUERY[1],
                'select pkg.id from Package pkg join pkg.' + refdata + ' p where pkg.id in (:idList) and p.id = :d order by pkg.name',
                'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
