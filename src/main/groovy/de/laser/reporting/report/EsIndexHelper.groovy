package de.laser.reporting.report

import de.laser.ElasticsearchSource
import de.laser.Package
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class EsIndexHelper {

    static HTTPBuilder getHttpBuilder(String uriPart) {
        ElasticsearchSource esSource = ElasticsearchSource.findByGokb_esAndActive(true, true)
        HTTPBuilder builder = new HTTPBuilder( 'http://' + esSource.host + ':' + esSource.port + uriPart )

        println 'EsIndexHelper.getHttpBuilder() - ' + builder.getUri()
        return builder
    }

    static Map<String, Object> getEsRecords(List<Long> idList) {
        Map<String, Object> result = [records: [:], orphaned: [] ] as Map<String, Object>

        if (idList) {
            List<List> pkgList = Package.executeQuery('select pkg.gokbId, pkg.id from Package pkg where pkg.id in (:idList)', [idList: idList])

            try {
                HTTPBuilder hb = getHttpBuilder('/gokbpackages/_search')
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
            result.orphaned = idList - result.records.keySet().collect{ Long.parseLong(it) }
        }
        //println 'found:    ' + result.records.size()
        //println 'orphaned: ' + result.orphaned.size()
        result
    }
}
