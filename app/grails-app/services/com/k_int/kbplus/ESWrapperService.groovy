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

    @javax.annotation.PostConstruct
    def init() {
        log.debug("ESWrapperService::init");

        def es_cluster_name = grailsApplication.config.aggr_es_cluster  ?: ESWrapperService.ES_CLUSTER
        def es_index_name   = grailsApplication.config.aggr_es_index    ?: ESWrapperService.ES_INDEX
        def es_host         = grailsApplication.config.aggr_es_hostname ?: ESWrapperService.ES_HOST

        log.debug("es_cluster = ${es_cluster_name}");
        log.debug("es_index_name = ${es_index_name}");
        log.debug("es_host = ${es_host}");

        Settings settings = Settings.settingsBuilder()
                .put("client.transport.sniff", true)
                .put("cluster.name", es_cluster_name)
                .build();

        esclient = TransportClient.builder().settings(settings).build();

        // add transport addresses
        esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_host), 9300 as int))

        log.debug("ES Init completed");
    }

    def getClient() {
        return esclient
    }
}
