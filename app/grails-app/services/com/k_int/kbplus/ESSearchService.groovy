package com.k_int.kbplus


import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.*
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;


class ESSearchService{
// Map the parameter names we use in the webapp with the ES fields
  def reversemap = ['subject':'subject', 
                    'provider':'provid',
                    'type':'rectype',
                    'endYear':'endYear',
                    'startYear':'startYear',
                    'consortiaName':'consortiaName',
                    'cpname':'cpname',
                    'availableToOrgs':'availableToOrgs',
                    'isPublic':'isPublic',
                    'lastModified':'lastModified']

  def ESWrapperService
  def grailsApplication

  def search(params){
    search(params,reversemap)
  }

  def search(params, field_map){
    // log.debug("Search Index, params.coursetitle=${params.coursetitle}, params.coursedescription=${params.coursedescription}, params.freetext=${params.freetext}")
    log.debug("ESSearchService::search - ${params}")

   def result = [:]

   //List client = getClient()
   Client esclient = ESWrapperService.getClient()
   Map esSettings =  ESWrapperService.getESSettings()

    try {
      if ( (params.q && params.q.length() > 0) || params.rectype) {
  
        params.max = Math.min(params.max ? params.int('max') : 15, 100)
        params.offset = params.offset ? params.int('offset') : 0

        def query_str = buildQuery(params,field_map)
        if (params.tempFQ) //add filtered query
        {
            query_str = query_str + " AND ( " + params.tempFQ + " ) "
            params.remove("tempFQ") //remove from GSP access
        }

        //log.debug("index:${esSettings.indexName} query: ${query_str}");
        def search
        try {
          SearchRequestBuilder searchRequestBuilder  = esclient.prepareSearch(esSettings.indexName)
          if (params.sort) {
            SortOrder order = SortOrder.ASC
            if (params.order) {
              order = SortOrder.valueOf(params.order?.toUpperCase())
            }
            searchRequestBuilder = searchRequestBuilder.addSort("${params.sort}".toString()+".keyword", order)
          }

          searchRequestBuilder = searchRequestBuilder.addSort("priority", SortOrder.DESC)

          log.debug("index:${esSettings.indexName} -> searchRequestBuilder start to add query and aggregration query string is ${query_str}")

          if(params.actionName == 'index') {

            searchRequestBuilder.setQuery(QueryBuilders.queryStringQuery(query_str))
                    .addAggregation(AggregationBuilders.terms('type').size(25).field('rectype.keyword'))
                    .addAggregation(AggregationBuilders.terms('providerName').size(50).field('providerName.keyword'))
                    .addAggregation(AggregationBuilders.terms('startYear').size(50).field('startYear.keyword'))
                    .addAggregation(AggregationBuilders.terms('endYear').size(50).field('endYear.keyword'))
                    .setFrom(params.offset)
                    .setSize(params.max)
          }else{
            searchRequestBuilder.setQuery(QueryBuilders.queryStringQuery(query_str))
                    .setFrom(params.offset)
                    .setSize(params.max)
          }
          search = searchRequestBuilder.get()
        }
        catch (Exception ex) {
          log.error("Error processing ${esSettings.indexName} ${query_str}",ex);
        }

        //Old search with ES 2.4.6
        /*def search = esclient.search{
                  indices esSettings.indexName
                  source {
                    from = params.offset
                    size = params.max
                    sort = params.sort?[
                      ("${params.sort}".toString()) : [ 'order' : (params.order?:'asc') ]
                    ] : []

                    query {
                      query_string (query: query_str)
                    }
                    aggregations {
                      consortiaName {
                        terms {
                          field = 'consortiaName'
                          size = 25
                        }
                      }
                      cpname {
                        terms {
                          field = 'cpname'
                          size = 25
                        }
                      }
                      type {
                        terms {
                          field = 'rectype'
                          size = 25
                        }
                      }
                      startYear {
                        terms {
                          field = 'startYear'
                          size = 25
                        }
                      }
                      endYear {
                        terms {
                          field = 'endYear'
                          size = 25
                        }
                      }
                    }

                  }

                }.actionGet()

        if ( search ) {
          def search_hits = search.hits
          result.hits = search_hits.hits
          result.resultsTotal = search_hits.totalHits
          result.index = esSettings.indexName

          // We pre-process the facet response to work around some translation issues in ES
          if (search.getAggregations()) {
            result.facets = [:]
            search.getAggregations().each { entry ->
              def facet_values = []
              entry.buckets.each { bucket ->
                log.debug("Bucket: ${bucket}");
                bucket.each { bi ->
                  log.debug("Bucket item: ${bi} ${bi.getKey()} ${bi.getDocCount()}");
                  facet_values.add([term: bi.getKey(), display: bi.getKey(), count: bi.getDocCount()])
                }
              }
              result.facets[entry.getName()] = facet_values

            }
          }
        }*/
        if ( search ) {
          def search_hits = search.getHits()

          if (search.getAggregations()) {
            result.facets = [:]
            search.getAggregations().each { entry ->
              def facet_values = []
              entry.buckets.each { bucket ->
                //log.debug("Bucket: ${bucket}");
                bucket.each { bi ->
                  //log.debug("Bucket item: ${bi} ${bi.getKey()} ${bi.getDocCount()}");
                  facet_values.add([term: bi.getKey(), display: bi.getKey(), count: bi.getDocCount()])
                }
              }
              result.facets[entry.getName()] = facet_values

            }
          }

          result.hits = search.getHits()
          result.resultsTotal = search_hits.totalHits
          result.index = esSettings.indexName
        }

      }
      else {
        log.debug("No query.. Show search page")
      }
    }
    finally {
      try {
      }
      catch ( Exception e ) {
        log.error("problem",e);
      }
    }
    result
  }

  def buildQuery(params,field_map) {
    log.debug("BuildQuery... with params ${params}. ReverseMap: ${field_map}");

    StringWriter sw = new StringWriter()

    if ( params?.q != null ){
      params.query = "*${params.query}*"
      //GOKBID, GUUID
      if(params.q.length() >= 37){
        if(params.q.contains(":") || params.q.contains("-")){
          params.q = params.q.replaceAll('\\*', '')
          sw.write("\"${params.q}\"")
        }else {
          sw.write("${params.q}")
          sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
        }
      }else {
        if((params.q.charAt(1) == '"' && params.q.charAt(params.q.length()-2) == '"') || params.q.contains(":") || params.q.contains("-")) {
          params.q = params.q.replaceAll('\\*', '')
          sw.write("\"${params.q}\"")
        }else{
          sw.write("${params.q}")
          sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
        }
      }
    }
      
    if(params?.rectype){
      if(sw.toString()) sw.write(" AND ");
      sw.write(" rectype:'${params.rectype}' ")
    }

      field_map.each { mapping ->

      if ( params[mapping.key] != null ) {
        if ( params[mapping.key].class == java.util.ArrayList) {
          if(sw.toString()) sw.write(" AND ");
          sw.write(" ( (( NOT rectype:\"Subscription\" ) AND ( NOT rectype:\"License\" ) AND ( NOT rectype:\"ParticipantSurvey\" ) AND ( NOT rectype:\"Survey\" )) ")

          params[mapping.key].each { p ->
            if(p == params[mapping.key].first())
            {
              sw.write(" OR ( ")
            }
            sw.write(" ( ")
            sw.write(mapping.value)
            sw.write(":")
            sw.write("\"${p}\"")
            sw.write(" ) ")
            if(p == params[mapping.key].last()) {
              sw.write(" ) ")
            }else{
              sw.write(" OR ")
            }

          }
          sw.write(" ) ")
        }
        else {
          // Only add the param if it's length is > 0 or we end up with really ugly URLs
          // II : Changed to only do this if the value is NOT an *

          log.debug("Processing ${params[mapping.key]} ${mapping.key}");

          try {
            if ( params[mapping.key] ) {
              if ( params[mapping.key].length() > 0 && ! ( params[mapping.key].equalsIgnoreCase('*') ) ) {
                if(sw.toString()) sw.write(" AND ");
                sw.write(mapping.value)
                sw.write(":")
                if(params[mapping.key].startsWith("[") && params[mapping.key].endsWith("]")){
                  sw.write("${params[mapping.key]}")
                }else{
                  sw.write("\"${params[mapping.key]}\"")
                }
              }
            }
          }
          catch ( Exception e ) {
            log.error("Problem procesing mapping, key is ${mapping.key} value is ${params[mapping.key]}",e);
          }
        }
      }
    }

    if(!params.showDeleted)
    {
      sw.write(  " AND ( NOT status:\"Deleted\" )")
    }

    def result = sw.toString();
    result;
  }

  def getClient(index) {
    def esclient = null
    def es_cluster_name = null
    def es_index_name = null
    def es_host = null
    def es_url = ''

    if(index == 'esgokb') {
      es_cluster_name = grailsApplication.config.aggr_es_gokb_cluster ?: (ElasticsearchSource.findByIdentifier('gokb')?.cluster ?: "elasticsearch")
      es_index_name = grailsApplication.config.aggr_es_gokb_index ?: (ElasticsearchSource.findByIdentifier('gokb')?.index ?: "gokb")
      es_host = grailsApplication.config.aggr_es_gokb_hostname ?: (ElasticsearchSource.findByIdentifier('gokb')?.host ?: "localhost")
      es_url = grailsApplication.config.aggr_es_gokb_url ?: (ElasticsearchSource.findByIdentifier('gokb')?.url ?: "")
    }else
    {
      es_cluster_name = grailsApplication.config.aggr_es_cluster  ?: 'elasticsearch'
      es_index_name   = grailsApplication.config.aggr_es_index    ?: 'kbplus'
      es_host         = grailsApplication.config.aggr_es_hostname ?: 'localhost'
    }
    log.debug("es_cluster = ${es_cluster_name}");
    log.debug("es_index_name = ${es_index_name}");
    log.debug("es_host = ${es_host}");
    log.debug("es_url = ${es_url}");

    Settings settings = Settings.builder()
            .put("client.transport.sniff", true)
            .put("cluster.name", es_cluster_name)
            .put("network.host", es_host)
            .put("es_url", es_url)
            .build();

    esclient = new org.elasticsearch.transport.client.PreBuiltTransportClient(settings);
    esclient.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(es_host), 9300));

    return [esclient, es_index_name]
  }

}
