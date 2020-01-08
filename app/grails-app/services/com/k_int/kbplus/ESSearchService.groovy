package com.k_int.kbplus

import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.*
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;


class ESSearchService{
// Map the parameter names we use in the webapp with the ES fields
  def reversemap = ['rectype':'rectype',
                    'endYear':'endYear',
                    'startYear':'startYear',
                    'consortiaName':'consortiaName',
                    'providerName':'providerName',
                    'availableToOrgs':'availableToOrgs',
                    'isPublic':'isPublic',
                    'status':'status']

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
   RestHighLevelClient esclient = ESWrapperService.getClient()
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
        //def search
        SearchResponse searchResponse
        try {

          SearchRequest searchRequest = new SearchRequest(esSettings.indexName)
          SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()


          //SearchRequestBuilder searchRequestBuilder  = esclient.prepareSearch(esSettings.indexName)
          if (params.sort) {
            SortOrder order = SortOrder.ASC
            if (params.order) {
              order = SortOrder.valueOf(params.order?.toUpperCase())
            }

            //searchRequestBuilder = searchRequestBuilder.addSort("${params.sort}".toString()+".keyword", order)
            searchSourceBuilder.sort(new FieldSortBuilder("${params.sort}").order(order))
          }

          //searchRequestBuilder = searchRequestBuilder.addSort("priority", SortOrder.DESC)
          searchSourceBuilder.sort(new FieldSortBuilder("priority").order(SortOrder.DESC))

          log.debug("index: ${esSettings.indexName} -> searchRequestBuilder start to add query and aggregration query string is ${query_str}")

          if(params.actionName == 'index') {

            searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))
            searchSourceBuilder.aggregation(AggregationBuilders.terms('rectype').size(25).field('rectype.keyword'))
            searchSourceBuilder.aggregation(AggregationBuilders.terms('providerName').size(50).field('providerName.keyword'))
            searchSourceBuilder.aggregation(AggregationBuilders.terms('status').size(50).field('status.keyword'))
            searchSourceBuilder.aggregation(AggregationBuilders.terms('startYear').size(50).field('startYear.keyword'))
            searchSourceBuilder.aggregation(AggregationBuilders.terms('endYear').size(50).field('endYear.keyword'))
            searchSourceBuilder.aggregation(AggregationBuilders.terms('consortiaName').size(50).field('consortiaName.keyword'))

            searchSourceBuilder.from(params.offset)
            searchSourceBuilder.size(params.max)
            searchRequest.source(searchSourceBuilder)
          }else{

            searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))
            searchSourceBuilder.from(params.offset)
            searchSourceBuilder.size(params.max)
            searchRequest.source(searchSourceBuilder)

          }
          searchResponse = esclient.search(searchRequest, RequestOptions.DEFAULT)
          //search = searchRequestBuilder.get()
        }
        catch (Exception ex) {
          log.error("Error processing ${esSettings.indexName} ${query_str}",ex);
        }

        if ( searchResponse ) {

          if (searchResponse.getAggregations()) {
            result.facets = [:]
            searchResponse.getAggregations().each { entry ->
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

          result.hits = searchResponse.getHits()
          result.resultsTotal = searchResponse.getHits().getTotalHits().value ?: "0"
          result.index = esSettings.indexName

        }

      }
      else {
        log.debug("No query.. Show search page")
      }
    }
    finally {
      try {
        esclient.close()
      }
      catch ( Exception e ) {
        log.error("Problem by Close ES Client",e);
      }
    }
    result
  }

  def buildQuery(params,field_map) {
    //log.debug("BuildQuery... with params ${params}. ReverseMap: ${field_map}");

    StringWriter sw = new StringWriter()

    if ( params?.q != null ){
      params.query = "${params.query}"
      //GOKBID, GUUID
      if(params.q.length() >= 37){
        if(params.q.contains(":") || params.q.contains("-")){
          //params.q = params.q.replaceAll('\\*', '')
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
          params.q = params.q.replaceAll('\\"', '')
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
          sw.write(" ( (( NOT rectype:\"Subscription\" ) AND ( NOT rectype:\"License\" ) " +
                  "AND ( NOT rectype:\"ParticipantSurvey\" ) AND ( NOT rectype:\"Survey\" ) " +
                  "AND ( NOT rectype:\"Task\" ) AND ( NOT rectype:\"Note\" ) AND ( NOT rectype:\"Document\" ) " +
                  "AND ( NOT rectype:\"IssueEntitlement\" )" +
                  "AND ( NOT rectype:\"SubscriptionCustomProperty\" ) AND ( NOT rectype:\"SubscriptionPrivateProperty\" )" +
                  "AND ( NOT rectype:\"LicenseCustomProperty\" ) AND ( NOT rectype:\"LicensePrivateProperty\" )" +
                  ") ")

          params[mapping.key].each { p ->
            if(p == params[mapping.key].first())
            {
              sw.write(" OR ( ")
            }
            sw.write(" ( ")
            sw.write(mapping.value)
            sw.write(":")
            sw.write("\"${p}\"")

            if(mapping.value == 'availableToOrgs')
            {
              if(params?.consortiaGUID){
                if(sw.toString()) sw.write(" OR ");

                sw.write(" consortiaGUID:\"${params?.consortiaGUID}\"")
              }
            }
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

    if(params?.searchObjects && params?.searchObjects != 'allObjects'){
      if(sw.toString()) sw.write(" AND ");

        sw.write(" visible:'Private' ")
    }

    if(!params.showDeleted)
    {
      sw.write(  " AND ( NOT status:\"Deleted\" )")
    }

    def result = sw.toString();
    result;
  }

  /*def getClient(index) {
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
  }*/

}
