package com.k_int.kbplus

import de.laser.helper.ConfigUtils
import de.laser.system.SystemEvent
import grails.gorm.transactions.Transactional
import org.apache.http.HttpHost
import org.apache.http.conn.ConnectTimeoutException
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

    final static String ES_INDEX   = 'kbplus'
    final static String ES_HOST    = 'localhost'
    final static String ES_CLUSTER = 'elasticsearch'

    static transactional = false

    RestHighLevelClient esclient

    String es_cluster_name
    String es_index_name
    String es_host


    @javax.annotation.PostConstruct
    def init() {
        log.debug("ESWrapperService::init");

        es_cluster_name = ConfigUtils.getAggrEsCluster()  ?: ESWrapperService.ES_CLUSTER
        es_index_name   = ConfigUtils.getAggrEsIndex()    ?: ESWrapperService.ES_INDEX
        es_host         = ConfigUtils.getAggrEsHostname() ?: ESWrapperService.ES_HOST

        log.debug("es_cluster = ${es_cluster_name}")
        log.debug("es_index_name = ${es_index_name}")
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

    Map<String, String> getESSettings(){
        Map<String, String> result = [:]

        result.clusterName = es_cluster_name
        result.host = es_host
        result.indexName = es_index_name

        result
    }

    Object getESMapping(){
        JSONParser jsonParser = new JSONParser(this.class.classLoader.getResourceAsStream("elasticsearch/es_mapping.json"))

        jsonParser.parse()
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
}
