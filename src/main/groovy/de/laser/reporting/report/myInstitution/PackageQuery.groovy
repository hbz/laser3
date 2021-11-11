package de.laser.reporting.report.myInstitution

import de.laser.Language
import de.laser.Org
import de.laser.Platform
import de.laser.Package
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.RDStore
import de.laser.reporting.report.EsIndexHelper
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class PackageQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static List<String> ES_QUERIES = [
            'package-breakable',
            'package-consistent',
            'package-scope',
            'package-x-curatoryGroup',
            'package-x-openAccess',
            'package-x-nationalRange',
            'package-x-regionalRange',
            'package-x-language',
            'package-x-ddc'
    ]

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        String prefix = params.query.split('-')[0]
        String suffix = params.query.split('-')[1] // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)
        Map<String, Object> esRecords = [:]

        println params.query
        if (idList && ES_QUERIES.contains( params.query )) {
            esRecords = getEsRecords( idList ).records
        }

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

            println '----------------> TODO'
            // _processSimpleRefdataQuery(params.query, 'breakable', idList, result)
        }
        else if ( suffix in ['consistent']) {

            println '----------------> TODO'
            // _processSimpleRefdataQuery(params.query, 'consistent', idList, result)
        }
        else if ( suffix in ['contentType']) {

            _processSimpleRefdataQuery(params.query, 'contentType', idList, result)
        }
        else if ( suffix in ['file']) {

            _processSimpleRefdataQuery(params.query, 'file', idList, result)
        }
        else if ( suffix in ['openAccess']) {

            println '----------------> TODO'
        }
        else if ( suffix in ['packageStatus']) {

            _processSimpleRefdataQuery(params.query, 'packageStatus', idList, result)
        }
        else if ( suffix in ['paymentType']) {

            println '----------------> TODO'
        }
        else if ( suffix in ['scope']) {

            println '----------------> TODO'
            // _processSimpleRefdataQuery(params.query, 'scope', idList, result)
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
                    handleGenericNonMatchingData2Values_TMP(params.query, NO_PROVIDER_LABEL, noDataList, result)
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
            else if (params.query in ['package-x-nationalRange']) {

                println '----------------> TODO'
            }
            else if (params.query in ['package-x-regionalRange']) {

                println '----------------> TODO'
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
                    handleGenericNonMatchingData2Values_TMP(params.query, NO_DATA_LABEL, noDataList, result)
                }
            }
            else if (params.query in ['package-x-ddc']) {

                println '----------------> TODO'
//                result.data = idList ? DeweyDecimalClassification.executeQuery(
//                        'select ddc.id, ddc.ddc.id, count(*) from Package pkg join pkg.ddcs ddc where pkg.id in (:idList) group by ddc.id, ddc.ddc.id order by ddc.id',
//                        [idList: idList]
//                ) : []
//
//                result.data.each { d ->
//                    d[1] = RefdataValue.get(d[1]).getI10n('value')
//
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : d[0],
//                            label : d[1],
//                            idList: Package.executeQuery(
//                                    'select pkg.id from Package pkg join pkg.ddcs ddc where pkg.id in (:idList) and ddc.id = :d order by pkg.name',
//                                    [d: d[0], idList: idList]
//                            )
//                    ])
//                }
//
//                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
//                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select pkg.id from Package pkg where pkg.id in (:idList)', [idList: nonMatchingIdList]) : []
//
//                if (noDataList) {
//                    handleGenericNonMatchingData2Values_TMP(params.query, NO_DATA_LABEL, noDataList, result)
//                }
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

    static Map<String, Map> getEsRecords(List<Long> idList) {
        Map<String, Map> result = [ records: [:] ]

        if (idList) {
            List<List> pkgList = Package.executeQuery('select pkg.gokbId, pkg.id from Package pkg where pkg.id in (:idList)', [idList: idList])

            try {
                HTTPBuilder hb = EsIndexHelper.getHttpBuilder('/gokbpackages/_search')
                println 'Requesting packages ..'

                while (pkgList) {
                    println '@' + pkgList.size()
                    List terms = pkgList.take(500)
                    pkgList = pkgList.drop(500) as List<List>

                    hb.request(Method.POST, ContentType.JSON) {
                        body = [
                                query: [
                                        terms: [ uuid: terms.collect{ it[0] } ]
                                ],
                                from: 0,
                                size: 10000,
                                _source: [ "uuid", "openAccess", "paymentType", "curatoryGroups.*", "scope", "nationalRanges.*", "regionalRanges.*" ]
                        ]

                        response.success = { resp, data ->
                            //println (resp.statusLine)
                            //println (resp.headers['content-length'])
                            data.hits.hits.each {
                                Map<String, Object> source = it.get('_source')
                                String id = terms.find{ it[0] == source.uuid }[1] as String
                                result.records.putAt( id, source )
                            }
                        }
                        response.failure = { resp ->
                            println (resp.statusLine)
                        }
                    }
                }
                hb.shutdown()
            }
            catch (Exception e) {
                println e.printStackTrace()
            }
            // println (idList.collect{ it.toString() } - result.records.keySet()).size()
        }
        println 'found: ' + result.records.size()
        result
    }
}
