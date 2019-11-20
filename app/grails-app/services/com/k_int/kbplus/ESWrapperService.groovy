package com.k_int.kbplus

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.cluster.health.ClusterHealthStatus
import org.elasticsearch.cluster.health.ClusterIndexHealth
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

class ESWrapperService {

    final static ES_INDEX   = 'kbplus'
    final static ES_HOST    = 'localhost'
    final static ES_CLUSTER = 'elasticsearch'

    static transactional = false
    def grailsApplication
    def esclient = null


    @javax.annotation.PostConstruct
    def init() {
        log.debug("ESWrapperService::init");

        def es_cluster_name = grailsApplication.config.aggr_es_cluster  ?: ESWrapperService.ES_CLUSTER
        def es_index_name   = grailsApplication.config.aggr_es_index    ?: ESWrapperService.ES_INDEX
        def es_host         = grailsApplication.config.aggr_es_hostname ?: ESWrapperService.ES_HOST

        log.debug("es_cluster = ${es_cluster_name}")
        log.debug("es_index_name = ${es_index_name}")
        log.debug("es_host = ${es_host}")


        Settings settings = Settings.builder().put("cluster.name", es_cluster_name).build()

        //Nötig ?
        //Settings settings = Settings.builder()
        //        .put("client.transport.sniff", true).build();

        esclient = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings);
        esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_host), 9300));

        log.debug("ES Init completed");
    }

    def getClient() {
        return esclient
    }

    //grails-app/config
    private def inputFile = this.class.classLoader.getResourceAsStream(
            "elasticsearch/es_mapping.txt"
    )
    def es_mapping = new JsonSlurper().parseText(inputFile.text)

    def clusterHealth(){
        ClusterHealthResponse healths = client.admin().cluster().prepareHealth().get();
        String clusterName = healths.getClusterName();
        int numberOfDataNodes = healths.getNumberOfDataNodes();
        int numberOfNodes = healths.getNumberOfNodes();

        println("ESInfo: clusterName: ${clusterName}, numberOfDataNodes: ${numberOfDataNodes}, numberOfNodes: ${numberOfNodes}")

        for (ClusterIndexHealth health : healths.getIndices().values()) {
            String index = health.getIndex();
            int numberOfShards = health.getNumberOfShards();
            int numberOfReplicas = health.getNumberOfReplicas();
            ClusterHealthStatus status = health.getStatus();

            println("ESInfo: index: ${index}, numberOfShards: ${numberOfShards}, numberOfReplicas: ${numberOfReplicas}, status: ${status}")
        }
    }

}
