package de.laser.reporting.report

import com.fasterxml.jackson.databind.ObjectMapper
import de.laser.remote.Wekb
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.RefdataValue
import de.laser.http.BasicHttpClient
import de.laser.config.ConfigMapper
import de.laser.reporting.export.myInstitution.PackageExport
import de.laser.reporting.export.myInstitution.PlatformExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j
import org.grails.web.json.JSONObject

/**
 * This helper class is responsible for data retrieval from we:kb ElasticSearch API
 */
@Slf4j
class ElasticSearchHelper {

    public static final int ELASTICSEARCH_CHUNKSIZE = 1000
    public static final String ELASTICSEARCH_IS_NOT_REACHABLE = 'elasticSearchIsNotReachable'
    public static final String IGNORE_FILTER = 'ignoreFilter'

    /**
     * Manages the querying of data from we:kb ElasticSearch index.
     * Records are being grouped in IDs with matching local record, IDs without such and the records themselves
     * @param cfgKey the object type being queried
     * @param cmbKey the filter being queried
     * @param idList a list of LAS:eR database IDs
     * @param filterResult the filter result map being filled
     * @param params the request parameter map
     */
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
                filterResult.put(ElasticSearchHelper.ELASTICSEARCH_IS_NOT_REACHABLE, ElasticSearchHelper.ELASTICSEARCH_IS_NOT_REACHABLE)
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

    /**
     * Gets the full {@link de.laser.wekb.Package} objects for the given IDs
     * @param idList the list of database IDs to be fetched
     * @return a {@link Map} containing the records and the orphaned IDs, i.e. such remote records to which there is no local record counterpart yet
     */
    static Map<String, Object> getEsPackageRecords(List<Long> idList) {
        Map<String, Object> result = [records: [:], orphanedIds: [] ] as Map<String, Object>

        if (idList) {
            List<List> pkgList = Package.executeQuery('select pkg.gokbId, pkg.id from Package pkg where pkg.id in (:idList)', [idList: idList])

            BasicHttpClient client
            try {
                Map rConfig = ConfigMapper.getConfig('reporting', Map) as Map
                client = new BasicHttpClient( rConfig.elasticSearch.url + '/' + rConfig.elasticSearch.indices.packages + '/_search' )

                log.info 'Retrieving ' + pkgList.size() + ' items (chunksize ' + ELASTICSEARCH_CHUNKSIZE + ') from ' + client.url

                while (pkgList) {
                    // print ' ~' + pkgList.size()
                    List terms = pkgList.take(ELASTICSEARCH_CHUNKSIZE)
                    pkgList = pkgList.drop(ELASTICSEARCH_CHUNKSIZE) as List<List>

                    client.post(
                            BasicHttpClient.ResponseType.JSON,
                            BasicHttpClient.PostType.JSON,
                            [
                                query: [
                                        terms: [ uuid: terms.collect{ it[0] } ]
                                ],
                                from: 0,
                                size: 10000,
                                _source: PackageExport.ES_SOURCE_FIELDS
                            ],
                        { resp, data ->
                                ObjectMapper om = new ObjectMapper()
                                data.hits.hits.each {
                                    JSONObject source = it.get('_source')
                                    Map map = om.readValue( source.toString(), Map.class )
                                    String id = terms.find{ it[0] == source.uuid }[1] as String
                                    result.records.putAt( id, map )
                                }
                            },
                        { resp ->
                                log.warn ('Response: ' + resp.getStatus().getCode() + ' - ' + resp.getStatus().getReason())
                            }
                    )
                }
            }
            catch (Exception e) {
                log.error e.getMessage()
            }
            finally {
                if (client) { client.close() }
            }
            result.orphanedIds = idList - result.records.keySet().collect{ Long.parseLong(it) }
        }
        result
    }

    /**
     * Gets the full {@link Platform} objects for the given IDs
     * @param idList the list of database IDs to be fetched
     * @return a {@link Map} containing the records and the orphaned IDs, i.e. such remote records to which there is no local record counterpart yet
     */
    static Map<String, Object> getEsPlatformRecords(List<Long> idList) {
        Map<String, Object> result = [records: [:], orphanedIds: [] ] as Map<String, Object>

        if (idList) {
            List<List> pkgList = Platform.executeQuery('select plt.gokbId, plt.id from Platform plt where plt.id in (:idList)', [idList: idList])

            BasicHttpClient client
            try {
                Map rConfig = ConfigMapper.getConfig('reporting', Map) as Map
                client = new BasicHttpClient( rConfig.elasticSearch.url + '/' + rConfig.elasticSearch.indices.platforms + '/_search' )

                log.info 'Retrieving ' + pkgList.size() + ' items (chunksize ' + ELASTICSEARCH_CHUNKSIZE + ') from ' + client.url

                while (pkgList) {
                    // print ' ~' + pkgList.size()
                    List terms = pkgList.take(ELASTICSEARCH_CHUNKSIZE)
                    pkgList = pkgList.drop(ELASTICSEARCH_CHUNKSIZE) as List<List>

                    client.post(
                            BasicHttpClient.ResponseType.JSON,
                            BasicHttpClient.PostType.JSON,
                            [
                                query: [
                                        terms: [ uuid: terms.collect{ it[0] } ]
                                ],
                                from: 0,
                                size: 10000,
                                _source: PlatformExport.ES_SOURCE_FIELDS
                            ],
                            { resp, data ->
                                ObjectMapper om = new ObjectMapper()
                                data.hits.hits.each {
                                    JSONObject source = it.get('_source')
                                    Map map = om.readValue( source.toString(), Map.class )
                                    String id = terms.find{ it[0] == source.uuid }[1] as String
                                    result.records.putAt( id, map )
                                }
                            },
                            { resp ->
                                log.warn ('Response: ' + resp.getStatus().getCode() + ' - ' + resp.getStatus().getReason())
                            }
                    )
                }
            }
            catch (Exception e) {
                log.error e.getMessage()
            }
            finally {
                if (client) { client.close() }
            }

            result.orphanedIds = idList - result.records.keySet().collect{ Long.parseLong(it) }
        }
        result
    }

    /**
     * Checks whether the we:kb API is available, aborting the test after 7 seconds
     * @return true if the API responds within 7 seconds, false otherwise
     */
    static boolean isReachable() {
        boolean reachable = false

        try {
            Map rConfig = ConfigMapper.getConfig('reporting', Map) as Map
            URI uri = new URI( rConfig.elasticSearch.url )
            InetAddress es = InetAddress.getByName( uri.getHost() )
            reachable = es.isReachable( 7000 )
        } catch (Exception e) {
            println e.printStackTrace()
        }
        reachable
    }

    /**
     * Sorts the result data, putting the wildcards to the back
     * @param list the resorted result list
     */
    static void sortResultDataList(List<List> list) {
        list.sort{ a, b ->
            if (a[1].startsWith('*')) { return 1 } else if (b[1].startsWith('*')) { return -1 } else { (a[1] as String).toLowerCase() <=> (b[1] as String).toLowerCase() }
        }
    }

}
