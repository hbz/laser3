package com.k_int.kbplus

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.client.transport.TransportClient

class ESWrapperService {

    final static ES_INDEX   = 'kbplus'
    final static ES_HOST    = 'localhost'
    final static ES_CLUSTER = 'elasticsearch'

    static transactional = false
    def grailsApplication
    def esclient = null
    def esclientgokb = null

    @javax.annotation.PostConstruct
    def init() {
        log.debug("ESWrapperService::init");

        def es_cluster_name = grailsApplication.config.aggr_es_cluster  ?: ESWrapperService.ES_CLUSTER
        def es_index_name   = grailsApplication.config.aggr_es_index    ?: ESWrapperService.ES_INDEX
        def es_host         = grailsApplication.config.aggr_es_hostname ?: ESWrapperService.ES_HOST

        def es_gokb_cluster_name = grailsApplication.config.aggr_es_gokb_cluster  ?: "elasticsearch"
        def es_gokb_index_name   = grailsApplication.config.aggr_es_gokb_index    ?: "gokb"
        def es_gokb_host         = grailsApplication.config.aggr_es_gokb_hostname ?: "localhost"

        log.debug("es_cluster = ${es_cluster_name}");
        log.debug("es_index_name = ${es_index_name}");
        log.debug("es_host = ${es_host}");

        log.debug("es_gokb_cluster = ${es_gokb_cluster_name}");
        log.debug("es_gokb_index_name = ${es_gokb_index_name}");
        log.debug("es_gokb_host = ${es_gokb_host}");


        Settings settings = Settings.settingsBuilder()
                .put("client.transport.sniff", true)
                .put("cluster.name", es_cluster_name)
                .build();

        esclient = TransportClient.builder().settings(settings).build();

        // add transport addresses
        esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_host), 9300 as int))



        Settings settings_gokb = Settings.settingsBuilder()
                .put("client.transport.sniff", true)
                .put("cluster.name", es_gokb_cluster_name)
                .build();

        esclientgokb = TransportClient.builder().settings(settings_gokb).build();

        // add transport addresses
        esclientgokb.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_gokb_host), 9300 as int))

        log.debug("ES Init completed");
    }

    def getClient() {
        return esclient
    }

    def getGOKBClient() {
        return esclientgokb
    }

}
