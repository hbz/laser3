package com.k_int.kbplus

import de.laser.helper.ConfigUtils
import org.apache.http.HttpHost
import org.codehaus.groovy.grails.web.json.parser.JSONParser
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.elasticsearch.cluster.health.ClusterIndexHealth
import org.elasticsearch.rest.RestStatus


class ESWrapperService {

    final static ES_INDEX   = 'kbplus'
    final static ES_HOST    = 'localhost'
    final static ES_CLUSTER = 'elasticsearch'

    static transactional = false
    def grailsApplication
    RestHighLevelClient esclient

    def es_cluster_name
    def es_index_name
    def es_host


    @javax.annotation.PostConstruct
    def init() {
        log.debug("ESWrapperService::init");

        es_cluster_name = ConfigUtils.getAggrEsCluster()  ?: ESWrapperService.ES_CLUSTER
        es_index_name   = ConfigUtils.getAggrEsIndex()    ?: ESWrapperService.ES_INDEX
        es_host         = ConfigUtils.getAggrEsHostname() ?: ESWrapperService.ES_HOST

        log.debug("es_cluster = ${es_cluster_name}")
        log.debug("es_index_name = ${es_index_name}")
        log.debug("es_host = ${es_host}")

      /*  Settings settings = Settings.builder().put("cluster.name", es_cluster_name).build()

        //Nötig ?
        //Settings settings = Settings.builder()
        //        .put("client.transport.sniff", true).build();

        esclient = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings);
        esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_host), 9300));*/

        log.debug("ES Init completed");
    }

    def getClient() {
        esclient = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(es_host, 9200, "http"),
                        new HttpHost(es_host, 9201, "http")));

        return esclient
    }

    def closeClient() {
        return esclient.close()
    }

    Map getESSettings(){
        Map result = [:]

        result.clusterName = es_cluster_name
        result.host = es_host
        result.indexName = es_index_name

        return result

    }

    def getESMapping(){

        JSONParser jsonParser = new JSONParser(this.class.classLoader.getResourceAsStream("elasticsearch/es_mapping.json"))

        return jsonParser.parse()

    }

    def clusterHealth(){

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
        println("Close")

    }

}
