package de.laser.reporting.report

import de.laser.ApiSource
import de.laser.Package
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.helper.ConfigUtils
import de.laser.reporting.export.myInstitution.PackageExport
import de.laser.reporting.export.myInstitution.PlatformExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

class ElasticSearchHelper {

    static final String ELASTIC_SEARCH_IS_NOT_REACHABLE = 'elasticSearchIsNotReachable'

    static final String IGNORE_FILTER = 'ignoreFilter'

    static void handleEsRecords(String cfgKey, String cmbKey, List<Long> idList, Map<String, Object> filterResult, GrailsParameterMap params) {

        Map<String, Object> esRecords = [:]
        List<Long> orphanedIdList = []
        boolean esFilterUsed = false

        if (idList) {
            if (isReachable()) {
                Map<String, Object> esr = [:]

                if (cfgKey == BaseConfig.KEY_PACKAGE)       { esr = getEsPackageRecords( idList ) }
                else if (cfgKey == BaseConfig.KEY_PLATFORM) { esr = getEsPlatformRecords( idList ) }

                esRecords = esr.records as Map<String, Object>
                orphanedIdList = esr.orphanedIds as List<Long>
            }
            else {
                filterResult.put(ElasticSearchHelper.ELASTIC_SEARCH_IS_NOT_REACHABLE, ElasticSearchHelper.ELASTIC_SEARCH_IS_NOT_REACHABLE)
                orphanedIdList = idList
            }
        }

        // println 'ElasticSearchHelper ---> ' + cfgKey + ' : ' + cmbKey + ' ---> ' + params

        if (cmbKey != ElasticSearchHelper.IGNORE_FILTER) {

            Map<String, Map> esDataMap = BaseConfig.getCurrentConfigElasticsearchData( cfgKey )

            BaseFilter.getCurrentFilterKeys(params, cmbKey).each { key ->
                if (params.get(key)) {
                    String p = key.replaceFirst(cmbKey,'')
                    // println 'ElasticSearchHelper - ' + cfgKey
                    String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( cfgKey ).base, p)
                    String pEsData = cfgKey + '-' + p

                    String filterLabelValue

                    if (pType == BaseConfig.FIELD_TYPE_ELASTICSEARCH && esDataMap.get( pEsData )?.filter) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        esRecords = esRecords.findAll{ it.value.get( p ) == rdv.value }
                        filterLabelValue = rdv.getI10n('value')
                    }
                    if (filterLabelValue) {
                        filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( cfgKey ).base, p), value: filterLabelValue])
                        esFilterUsed = true
                    }
                }
            }

            if (esFilterUsed) {
                idList = /* orphanedIdList + */ esRecords.keySet()?.collect{ Long.parseLong(it) }
                orphanedIdList = []
            }
        }

        filterResult.data.put( cfgKey + 'IdList', idList)
        filterResult.data.put( cfgKey + 'ESRecords', esRecords)
        filterResult.data.put( cfgKey + 'OrphanedIdList', orphanedIdList)
    }

    static Map<String, Object> getEsPackageRecords(List<Long> idList) {
        Map<String, Object> result = [records: [:], orphanedIds: [] ] as Map<String, Object>

        if (idList) {
            List<List> pkgList = Package.executeQuery('select pkg.gokbId, pkg.id from Package pkg where pkg.id in (:idList)', [idList: idList])

            try {
                Map rConfig = ConfigUtils.readConfig('reporting', false) as Map
                HTTPBuilder hb = new HTTPBuilder( rConfig.elasticSearch.url + '/' + rConfig.elasticSearch.indicies.packages + '/_search' )

                print ' Retrieving: ' + hb.getUri()
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
                                _source: PackageExport.ES_SOURCE_FIELDS
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

                print ' Retrieving: ' + hb.getUri()
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
                                _source: PlatformExport.ES_SOURCE_FIELDS
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

    static void sortResultDataList(List<List> list) {
        list.sort{ a, b ->
            if (a[1].startsWith('*')) { return 1 } else if (b[1].startsWith('*')) { return -1 } else { (a[1] as String).toLowerCase() <=> (b[1] as String).toLowerCase() }
        }
    }

    static getCurrentApiSource() {
        ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
    }
}
