package com.k_int.kbplus

import org.elasticsearch.common.settings.ImmutableSettings
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

        log.debug("ESWrapperService::init")

        def es_cluster_name = grailsApplication.config.aggr_es_cluster  ?: ESWrapperService.ES_CLUSTER
        def es_index_name   = grailsApplication.config.aggr_es_index    ?: ESWrapperService.ES_INDEX
        def es_host         = grailsApplication.config.aggr_es_hostname ?: ESWrapperService.ES_HOST

        log.debug("~ host: ${es_host} / cluster: ${es_cluster_name} / index: ${es_index_name}")

        esclient = new TransportClient(ImmutableSettings.settingsBuilder {
            cluster {
                name = es_cluster_name
            }
            client {
                transport {
                    sniff = true
                }
            }
        })

        esclient.addTransportAddress(new InetSocketTransportAddress(es_host, 9300))
    }

    def getClient() {
        return esclient
    }
}
