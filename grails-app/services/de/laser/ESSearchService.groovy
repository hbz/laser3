package de.laser

import de.laser.helper.DateUtils
import grails.gorm.transactions.Transactional
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder

/**
 * This service manages search requests for the app's ElasticSearch service
 */
@Transactional
class ESSearchService{

  ESWrapperService ESWrapperService


// Map the parameter names we use in the webapp with the ES fields
  def reversemap = ['rectype':'rectype',
                    'endYear':'endYear',
                    'startYear':'startYear',
                    'consortiaName':'consortiaName',
                    'providerName':'providerName',
                    'availableToOrgs':'availableToOrgs',
                    'isPublic':'isPublic',
                    'status':'status',
                    'publisher':'publisher',
                    'publishers':'publishers.name',
                    'name':'name']

  /**
   * Substitution call for requests, fetching the reverse map
   * @param params the search parameter map
   * @return the results of {@link #search(java.lang.Object, java.lang.Object)}
   */
  def search(params){
    search(params,reversemap)
  }

  /**
   * Prepares the index query, sets score weights of the query and performs the search against the ElasticSearch index
   * @param params the search request parameters
   * @param field_map the map containing which search parameter is represented by which index field
   * @return the search result hits of the query
   */
  def search(params, field_map){
    // log.debug("Search Index, params.coursetitle=${params.coursetitle}, params.coursedescription=${params.coursedescription}, params.freetext=${params.freetext}")
    log.debug("ESSearchService::search - ${params}")

   Map<String, Object> result = [:]

   //List client = getClient()
   RestHighLevelClient esclient = ESWrapperService.getClient()
   def es_indices =  ESWrapperService.es_indices

    try {
      if(ESWrapperService.testConnection()) {
        if ((params.q && params.q.length() > 0) || params.rectype) {

          params.max = Math.min(params.max ? params.int('max') : 15, 10000)
          params.offset = params.offset ? params.int('offset') : 0

          String query_str = buildQuery(params, field_map)
          if (params.tempFQ) //add filtered query
          {
            query_str = query_str + " AND ( " + params.tempFQ + " ) "
            params.remove("tempFQ") //remove from GSP access
          }

          SearchResponse searchResponse
          try {

            SearchRequest searchRequest = new SearchRequest(es_indices.values() as String[])
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()


            if (params.sort) {
              SortOrder order = SortOrder.ASC
              if (params.order) {
                order = SortOrder.valueOf(params.order?.toUpperCase())
              }

              //searchRequestBuilder = searchRequestBuilder.addSort("${params.sort}".toString()+".keyword", order)
              if(params.sort == "type.value"){
                searchSourceBuilder.sort(new FieldSortBuilder("${params.sort}").order(order).setNestedPath("type"))
              }
              else if(params.sort == "publishers.name"){
                searchSourceBuilder.sort(new FieldSortBuilder("${params.sort}").order(order).setNestedPath("publishers"))
              }
              else {
                searchSourceBuilder.sort(new FieldSortBuilder("${params.sort}").order(order))
              }
            }

            //searchRequestBuilder = searchRequestBuilder.addSort("priority", SortOrder.DESC)
            searchSourceBuilder.sort(new FieldSortBuilder("priority").order(SortOrder.DESC))

            log.debug("index: ${es_indices as String[]} -> searchRequestBuilder: ${query_str}")

            if (params.actionName == 'index') {

              NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder('status', 'status')

              searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))
              searchSourceBuilder.aggregation(AggregationBuilders.terms('rectype').size(25).field('rectype.keyword'))
              searchSourceBuilder.aggregation(AggregationBuilders.terms('providerName').size(50).field('providerName.keyword'))
              searchSourceBuilder.aggregation(nestedAggregationBuilder.subAggregation(AggregationBuilders.terms('status').size(50).field('status.value')))
              searchSourceBuilder.aggregation(AggregationBuilders.terms('startYear').size(50).field('startYear.keyword'))
              searchSourceBuilder.aggregation(AggregationBuilders.terms('endYear').size(50).field('endYear.keyword'))
              searchSourceBuilder.aggregation(AggregationBuilders.terms('consortiaName').size(50).field('consortiaName.keyword'))

              searchSourceBuilder.from(params.offset)
              searchSourceBuilder.size(params.max)
              searchRequest.source(searchSourceBuilder)
            } else {

              searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))
              searchSourceBuilder.from(params.offset)
              searchSourceBuilder.size(params.max)
              searchRequest.source(searchSourceBuilder)

            }
            searchResponse = esclient.search(searchRequest, RequestOptions.DEFAULT)

            //search = searchRequestBuilder.get()
          }
          catch (Exception ex) {
            log.error("Error processing ${es_indices as String[]} ${query_str}", ex)
          }

          if (searchResponse) {

            if (searchResponse.getAggregations()) {
              result.facets = [:]
              searchResponse.getAggregations().each { entry ->
                def facet_values = []
                //log.debug("Entry: ${entry.type}")

                if(entry.type == 'nested'){
                  entry.getAggregations().each { subEntry ->
                    //log.debug("metaData: ${subEntry.name}")
                    subEntry.buckets.each { bucket ->
                      //log.debug("Bucket: ${bucket}")
                      bucket.each { bi ->
                        //log.debug("Bucket item: ${bi} ${bi.getKey()} ${bi.getDocCount()}")
                        facet_values.add([term: bi.getKey(), display: bi.getKey(), count: bi.getDocCount()])
                      }
                    }
                  }
                }else {
                  entry.buckets.each { bucket ->
                    //log.debug("Bucket: ${bucket}")
                    bucket.each { bi ->
                      //log.debug("Bucket item: ${bi} ${bi.getKey()} ${bi.getDocCount()}")
                      facet_values.add([term: bi.getKey(), display: bi.getKey(), count: bi.getDocCount()])
                    }
                  }
                }
                result.facets[entry.getName()] = facet_values

              }
            }

            result.hits = searchResponse.getHits()
            result.resultsTotal = searchResponse.getHits().getTotalHits().value ?: 0
            result.index = es_indices as String[]

          }

        } else {
          log.debug("No query.. Show search page")
        }
      }
    }
    finally {
      try {
        esclient.close()
      }
      catch ( Exception e ) {
        log.error("Problem by Close ES Client",e)
      }
    }
    result
  }

  /**
   * Here is the ElasticSearch query (a query string query) actually being built.
   * The parameter map is being taken, the reverse map consulted to map the queried data on the ElasticSearch
   * index and the query string returned for execution
   * @param params the search parameter map
   * @param field_map the reverse map containing the mapping of fields in the index
   * @return the prepared query string
   * @see ESWrapperService#es_indices
   */
  String buildQuery(params,field_map) {
    //log.debug("BuildQuery... with params ${params}. ReverseMap: ${field_map}")

    StringWriter sw = new StringWriter()

    if ( params.q != null ){
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
        if(params.q.contains(":") || params.q.contains("-")) {
          //params.q = params.q.replaceAll('\\*', '')
          sw.write("\"${params.q}\"")
        }else if (params.q.count("\"") >= 2){
          sw.write("${params.q}")
          sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
        }else{

          if(DateUtils.isDate(params.q)){
            params.q = DateUtils.getSDF_yyyyMMdd().format( DateUtils.parseDateGeneric(params.q) )
          }

          params.q = params.q.replaceAll('\\"', '')
          sw.write("${params.q}")
          sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
        }
      }
    }
      
    /*if(params.rectype){
      if(sw.toString()) sw.write(" AND ")
      sw.write(" rectype:'${params.rectype}' ")
    }*/

      field_map.each { mapping ->

      if ( params[mapping.key] != null ) {
        if ( params[mapping.key].class == java.util.ArrayList) {
          if(sw.toString()) sw.write(" AND ")
          sw.write(" ( (( NOT rectype:\"Subscription\" ) AND ( NOT rectype:\"License\" ) " +
                  "AND ( NOT rectype:\"SurveyOrg\" ) AND ( NOT rectype:\"SurveyConfig\" ) " +
                  "AND ( NOT rectype:\"Task\" ) AND ( NOT rectype:\"Note\" ) AND ( NOT rectype:\"Document\" ) " +
                  "AND ( NOT rectype:\"IssueEntitlement\" ) " +
                  "AND ( NOT rectype:\"SubscriptionProperty\" ) " +
                  "AND ( NOT rectype:\"LicenseProperty\" ) " +
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
              if(params.consortiaID){
                if(sw.toString()) sw.write(" OR ")

                sw.write(" consortiaID:\"${params.consortiaID}\"")
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

          log.debug("Processing ${params[mapping.key]} ${mapping.key}")

          try {
            if ( params[mapping.key] ) {
                if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                  if (sw.toString())
                    sw.write(" AND ")
                  sw.write(mapping.value)
                  sw.write(":")
                  if (params[mapping.key].startsWith("[") && params[mapping.key].endsWith("]")) {
                    sw.write("${params[mapping.key]}")
                  } else if (params[mapping.key].count("\"") >= 2) {
                    sw.write("${params[mapping.key]}")
                  } else if (params[mapping.key].contains(" ")) {
                    sw.write(params[mapping.key].trim().replace(" "," AND "))
                  }
                  else {
                    sw.write("( ${params[mapping.key]} )")
                  }
                }
            }
          }
          catch ( Exception e ) {
            log.error("Problem procesing mapping, key is ${mapping.key} value is ${params[mapping.key]}",e)
          }
        }
      }
    }

    if(params.searchObjects && params.searchObjects != 'allObjects'){
      if(sw.toString()) sw.write(" AND ")

        sw.write(" visible:'Private' ")
    }

    if(!params.showDeleted)
    {
      sw.write(  " AND ( NOT status:\"Deleted\" )")
    }

    if(params.showAllTitles) {
      sw.write(  " AND (rectype: \"TitleInstancePackagePlatform\") ")
    }

    sw.toString()
  }
}
