package de.laser.reporting.report

import de.laser.Package
import de.laser.Platform
import de.laser.helper.ConfigUtils
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class ElasticSearchHelper {

    static final String ELASTIC_SEARCH_IS_NOT_REACHABLE = 'elasticSearchIsNotReachable'

    static Map<String, Object> getEsPackageRecords(List<Long> idList) {
        Map<String, Object> result = [records: [:], orphanedIds: [] ] as Map<String, Object>

        if (idList) {
            List<List> pkgList = Package.executeQuery('select pkg.gokbId, pkg.id from Package pkg where pkg.id in (:idList)', [idList: idList])

            try {
                Map rConfig = ConfigUtils.readConfig('reporting', false) as Map
                HTTPBuilder hb = new HTTPBuilder( rConfig.elasticSearch.url + '/' + rConfig.elasticSearch.indicies.packages + '/_search' )
                println 'Request: ' + hb.getUri()

                print 'Queue:'
                while (pkgList) {
                    print ' ~' + pkgList.size()
                    List terms = pkgList.take(500)
                    pkgList = pkgList.drop(500) as List<List>

                    hb.request(Method.POST, ContentType.JSON) {
                        body = [
                                query: [
                                        terms: [ uuid: terms.collect{ it[0] } ]
                                ],
                                from: 0,
                                size: 10000,
                                _source: [
                                        "uuid", "openAccess", "paymentType", "scope",
                                        "altname", "description", "descriptionURL",
                                        "curatoryGroups.*", "ddcs.*", "identifiers.*", "nationalRanges.*", "regionalRanges.*",
                                        "lastUpdatedDisplay"
                                ]
                        ]
                        response.success = { resp, data ->
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
            println()
            result.orphanedIds = idList - result.records.keySet().collect{ Long.parseLong(it) }
        }
        result
    }

    static Map<String, Object> getEsPlatformRecords(List<Long> idList) {
        Map<String, Object> result = [records: [:], orphanedIds: [] ] as Map<String, Object>

        if (idList) {
            List<List> pkgList = Platform.executeQuery('select plt.gokbId, plt.id from Platform plt where plt.id in (:idList)', [idList: idList])

            try {
                Map rConfig = ConfigUtils.readConfig('reporting', false) as Map
                HTTPBuilder hb = new HTTPBuilder( rConfig.elasticSearch.url + '/' + rConfig.elasticSearch.indicies.platforms + '/_search' )
                println 'Request: ' + hb.getUri()

                print 'Queue:'
                while (pkgList) {
                    print ' ~' + pkgList.size()
                    List terms = pkgList.take(500)
                    pkgList = pkgList.drop(500) as List<List>

                    hb.request(Method.POST, ContentType.JSON) {
                        body = [
                                query: [
                                        terms: [ uuid: terms.collect{ it[0] } ]
                                ],
                                from: 0,
                                size: 10000,
                                _source: [
                                        "uuid", "providerUuid",
                                        "altname",
                                        "ipAuthentication", "shibbolethAuthentication", "passwordAuthentication", "proxySupported",
                                        "statisticsFormat", "statisticsUpdate", "counterCertified",
                                        "counterR3Supported", "counterR4Supported", "counterR4SushiApiSupported", "counterR5Supported", "counterR5SushiApiSupported",
                                        "lastUpdatedDisplay"
                                ]
                        ]
                        response.success = { resp, data ->
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
            println()
            result.orphanedIds = idList - result.records.keySet().collect{ Long.parseLong(it) }
        }
        result
    }

    static boolean isReachable() {

        boolean reachable = false
        try {
            Map rConfig = ConfigUtils.readConfig('reporting', false) as Map
            URI uri = new URI( rConfig.elasticSearch.url )
            InetAddress es = InetAddress.getByName( uri.getHost() )
            reachable = es.isReachable( 7000 )
        } catch (Exception e) {
            println e.printStackTrace()
        }
        reachable
    }
}
