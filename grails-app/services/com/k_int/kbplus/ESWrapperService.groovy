package com.k_int.kbplus

import de.laser.FTControl
import de.laser.helper.ConfigUtils
import de.laser.system.SystemEvent
import grails.core.GrailsApplication
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

@Transactional
class ESWrapperService {

    final static String ES_HOST    = 'localhost'
    final static String ES_CLUSTER = 'elasticsearch'

    static transactional = false

    RestHighLevelClient esclient
    GrailsApplication grailsApplication

    String es_cluster_name
    Map es_indices = [:]
    String es_host


    @javax.annotation.PostConstruct
    def init() {
        log.debug("ESWrapperService::init");


        //With ConfigUtils.getAggrEsIndices() not possible.
        es_indices = grailsApplication.config.aggr_es_indices
        es_cluster_name = ConfigUtils.getAggrEsCluster()  ?: ESWrapperService.ES_CLUSTER
        es_host         = ConfigUtils.getAggrEsHostname() ?: ESWrapperService.ES_HOST

        log.debug("es_cluster = ${es_cluster_name}")
        log.debug("es_indices = ${es_indices}")
        log.debug("es_host = ${es_host}")

        log.debug("ES Init completed");
    }

    RestHighLevelClient getClient() {
        esclient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(es_host, 9200, "http"),
                        new HttpHost(es_host, 9201, "http")));

        esclient
    }

    void closeClient() {
        esclient.close()
    }

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

    def getSettings(){
        parseResource("${File.separator}elasticsearch${File.separator}es_settings.json")
    }


    def getMapping(){
        parseResource("${File.separator}elasticsearch${File.separator}es_mapping.json")
    }


    private def parseResource(String resourcePath){
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

    boolean testConnection() {

        try {
            boolean response = esclient.ping(RequestOptions.DEFAULT)

            if(!response){
                log.error("Problem with ElasticSearch: Ping Fail")
                SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["Ping Fail": "Ping Fail"])
            }
            return response
        } catch (ConnectTimeoutException e) {
            log.error("Problem with ElasticSearch: Connect Timeout")
            SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["Connect Timeout": "Connect Timeout"])
            esclient.close()
            return false
        }
        catch (ConnectException e) {
            log.error("Problem with ElasticSearch: Connection Fail")
            SystemEvent.createEvent('FT_INDEX_UPDATE_ERROR', ["Connection Fail": "Connection Fail"])
            return false
        }

    }

    void clusterHealth(){

        RestHighLevelClient esclient = this.getClient()

        ClusterHealthRequest request = new ClusterHealthRequest();
        ClusterHealthResponse response = esclient.cluster().health(request, RequestOptions.DEFAULT);

        String clusterName = response.getClusterName();
        //ClusterHealthStatus status = response.getStatus();

        boolean timedOut = response.isTimedOut();
        RestStatus restStatus = response.status();

        Map<String, ClusterIndexHealth> indices = response.getIndices();

        ClusterIndexHealth index = indices.get("index");
        ClusterHealthStatus indexStatus = index.getStatus();
        int numberOfShards = index.getNumberOfShards();
        int numberOfReplicas = index.getNumberOfReplicas();
        int activeShards = index.getActiveShards();
        int activePrimaryShards = index.getActivePrimaryShards();
        int initializingShards = index.getInitializingShards();
        int relocatingShards = index.getRelocatingShards();
        int unassignedShards = index.getUnassignedShards();

        println("ESInfo: clusterName: ${clusterName}, numberOfDataNodes: ${numberOfDataNodes}, numberOfNodes: ${numberOfNodes}")
        println("ESInfo: index: ${index}, numberOfShards: ${numberOfShards}, numberOfReplicas: ${numberOfReplicas}, indexStatus: ${indexStatus}")

        esclient.close()
    }

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
                String domainClassName = this.es_indices.find {it.value == indexName}.key

                FTControl.withTransaction {
                    def res = FTControl.executeUpdate("delete FTControl c where c.domainClassName = :deleteFT", [deleteFT: "de.laser.${domainClassName}"])
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
