package de.laser


import de.laser.config.ConfigMapper
import de.laser.remote.FTControl
import de.laser.system.SystemEvent
import grails.gorm.transactions.Transactional
import groovy.json.JsonOutput
import org.apache.http.HttpHost
import org.apache.http.conn.ConnectTimeoutException
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
import org.elasticsearch.action.support.master.AcknowledgedResponse
import org.elasticsearch.client.indices.CreateIndexRequest
import org.elasticsearch.client.indices.CreateIndexResponse
import org.elasticsearch.client.indices.GetIndexRequest
import org.elasticsearch.common.xcontent.XContentType
import org.grails.web.json.parser.JSONParser
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.elasticsearch.cluster.health.ClusterIndexHealth
import org.elasticsearch.rest.RestStatus

/**
 * This service wraps the ElasticSearch connection and configuration methods and
 * handles the core ElasticSearch functionality (apart from search)
 * @see ESSearchService
 * @see DataloadService
 */
@Transactional
class ESWrapperService {

    final static String DEFAULT_ES_HOST = 'localhost'
    final static String DEFAULT_ES_CLUSTER = 'elasticsearch'

    static transactional = false

    static String ES_Host
    static String ES_Cluster
    static Map ES_Indices = [:]

    /**
     * Initialises the ElasticSearch connection and mapping parameters
     * @return
     */
    @javax.annotation.PostConstruct
    def init() {
        log.info('ESWrapperService - init')

        ES_Host     = ConfigMapper.getAggrEsHostname() ?: ESWrapperService.DEFAULT_ES_HOST
        ES_Cluster  = ConfigMapper.getAggrEsCluster()  ?: ESWrapperService.DEFAULT_ES_CLUSTER
        ES_Indices  = ConfigMapper.getAggrEsIndices()  ?: [:]

        log.debug("-> ES_Host = ${ES_Host}")
        log.debug("-> ES_Cluster = ${ES_Cluster}")
        log.debug("-> ES_Indices = ${ES_Indices}")
    }

    /**
     * Establishes the REST client connection to the ElasticSearch host
     * @return
     */
    RestHighLevelClient getClient() {
        RestHighLevelClient esclient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(ES_Host, 9200, "http"),
                        new HttpHost(ES_Host, 9201, "http")));

        esclient
    }

    /*void closeClient() {
        esclient.close()
    }*/

/*    Map<String, String> getESSettings(){
        Map<String, String> result = [:]

        result.clusterName = es_cluster_name
        result.host = es_host
        result.indexName = es_indices

        result
    }

    Object getESMapping(){
        JSONParser jsonParser = new JSONParser(this.class.classLoader.getResourceAsStream("es_mapping.json"))

        jsonParser.parse()
    }*/

    /**
     * Gets the ElasticSearch setting configuration file
     * @return the parsed ElasticSearch settings
     */
    def getSettings(){
        _parseResource("${File.separator}elasticsearch${File.separator}es_settings.json")
    }

    /**
     * Gets the ElasticSearch mapping file
     * @return the parsed ElasticSearch mapping
     */
    def getMapping(){
        _parseResource("${File.separator}elasticsearch${File.separator}es_mapping.json")
    }

    /**
     * Parses the file at the given path and returns its content as a JSON map
     * @param resourcePath the path where the file is located
     * @return the parsed content of the file
     */
    private def _parseResource(String resourcePath){
        def resource = this.class.classLoader.getResourceAsStream(resourcePath)
        if (resource == null){
            resource = getClass().getResource(resourcePath)
        }
        JSONParser jsonParser
        if(resource instanceof URL)
            jsonParser = new JSONParser(resource.openStream())
        else if(resource instanceof InputStream)
            jsonParser = new JSONParser(resource)
        if(jsonParser)
            jsonParser.parse()
        else log.error("resource at path ${resourcePath} unable to locate!")
    }

    /**
     * Checks if the connection to the ElasticSearch index is established and if it works
     * @return true if the test was successful, false otherwise
     */
    boolean testConnection() {

        RestHighLevelClient esclient = getClient()

        try {
            boolean response = esclient.ping(RequestOptions.DEFAULT)

            if(!response){
                log.warn("Problem with ElasticSearch: Ping Fail")
                SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["Ping Fail": "Ping Fail"])
            }
            esclient.close()
            return response
        } catch (ConnectTimeoutException e) {
            log.warn("Problem with ElasticSearch: Connect Timeout")
            SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["Connect Timeout": "Connect Timeout"])
            esclient.close()
            return false
        }
        catch (ConnectException e) {
            log.warn("Problem with ElasticSearch: Connection Fail")
            SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["Connection Fail": "Connection Fail"])
            esclient.close()
            return false
        }

    }

    /**
     * Drops the given index
     * @param indexName the index to be deleted
     * @return true if the deletion request was successful, false otherwise
     */
    boolean deleteIndex(String indexName){
        log.info("deleteIndex ${indexName} ...")
        RestHighLevelClient esclient = this.getClient()
        GetIndexRequest request = new GetIndexRequest(indexName)

        if (esclient.indices().exists(request, RequestOptions.DEFAULT)) {
            DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName)
            AcknowledgedResponse deleteIndexResponse = esclient.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT)
            if (deleteIndexResponse.isAcknowledged()) {
                log.info("Index ${indexName} successfully deleted!")
                esclient.close()
                return true
            } else {
                log.info("Index deletetion failed: ${deleteIndexResponse}")
                esclient.close()
                return false
            }
        }else {
            log.info("Index ${indexName} not exists and successfully deleted!")
            esclient.close()
            return true
        }
    }

    /**
     * Creates the given index. The settings and mapping defined in the external resource files (see links to see where
     * they are located) are being used to construct
     * @param indexName the index to be built
     * @return true if the creation request was successful, false otherwise
     * @see #getSettings()
     * @see #getMapping()
     */
    boolean createIndex(String indexName){
        log.info("createIndex ${indexName}...")
        RestHighLevelClient esclient = this.getClient()
        GetIndexRequest request = new GetIndexRequest(indexName)

        if (!esclient.indices().exists(request, RequestOptions.DEFAULT)) {
            log.debug("ES index ${indexName} did not exist, creating..")

            CreateIndexRequest createRequest = new CreateIndexRequest(indexName)

            log.debug("Adding index settings..")
            createRequest.settings(JsonOutput.toJson(this.getSettings().get("settings")), XContentType.JSON)
            log.debug("Adding index mappings..")
            createRequest.mapping(JsonOutput.toJson(this.getMapping()), XContentType.JSON)

            CreateIndexResponse createIndexResponse = esclient.indices().create(createRequest, RequestOptions.DEFAULT)

            boolean acknowledged = createIndexResponse.isAcknowledged()


            if (acknowledged) {
                log.debug("Index ${indexName} successfully created!")
                String domainClassName = this.ES_Indices.find {it.value == indexName}.key

                FTControl.withTransaction {
                    int res = FTControl.executeUpdate("delete FTControl c where c.domainClassName = :deleteFT", [deleteFT: "de.laser.${domainClassName}"])
                    log.info("Result: ${res}")
                }
                esclient.close()
                return true

            } else {
                log.debug("Index creation failed: ${createIndexResponse}")
                esclient.close()
                return false
            }
        } else {
            log.debug("ES index ${indexName} already exists..")
            esclient.close()
            return true
        }
    }
}
