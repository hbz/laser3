package de.laser

import de.laser.helper.ConfigUtils
import de.laser.helper.DateUtils
import de.laser.system.SystemEvent
import grails.gorm.transactions.Transactional
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.HttpHost
import org.apache.http.conn.ConnectTimeoutException
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.sort.FieldSortBuilder
import org.elasticsearch.search.sort.SortOrder
import org.grails.web.json.parser.JSONParser

@Transactional
class GokbService {

    final static String ESGOKB_INDEX   = 'gokbg3'
    final static String ESGOKB_HOST    = '193.30.112.33'
    final static String ESGOKB_CLUSTER = 'cFdD9qL'

    RestHighLevelClient esgokb_client

    String esgokb_cluster_name
    String esgokb_index_name
    String esgokb_host

    // Map the parameter names we use in the webapp with the ES fields
    def reversemap = [
            'type':'rectype',
            'curatoryGroups':'curatoryGroups',
            'cpname':'cpname',
            'provider':'provider',
            'componentType':'componentType',
            'lastUpdatedDisplay':'lastUpdatedDisplay']


    @javax.annotation.PostConstruct
    def init() {
        log.debug("GokbService::init");

        esgokb_cluster_name = ConfigUtils.getAggrEsGOKBCluster()  ?: ESGOKB_CLUSTER
        esgokb_index_name   = ConfigUtils.getAggrEsGOKBIndex()    ?: ESGOKB_INDEX
        esgokb_host         = ConfigUtils.getAggrEsGOKBHostname() ?: ESGOKB_HOST

        log.debug("esgokb_cluster = ${esgokb_cluster_name}")
        log.debug("esgokb_index_name = ${esgokb_index_name}")
        log.debug("esgokb_host = ${esgokb_host}")

        log.debug("GokbService Init completed");
    }

    Map<String, Object> getPackagesMap(ApiSource apiSource, def qterm = null, def suggest = true, def max = 2000) {

        log.info("getting Package map from gokb ..")

        Map<String, Object> result = [:]

        try {
            String esQuery = qterm ? URLEncoder.encode(qterm) : ""

            def json = null

            if(suggest) {
                //Get only Package with Status= Current
                json = geElasticsearchSuggests(apiSource.baseUrl+apiSource.fixToken, esQuery, "Package", null) // 10000 is maximum value allowed by now
            }else {
                //Get only Package with Status= Current
                json = geElasticsearchFindings(apiSource.baseUrl+apiSource.fixToken, esQuery, "Package", null, max)
            }
            log.info("getting Package map from gokb (${apiSource.baseUrl+apiSource.fixToken})")
            result.records = []

            if (json?.info?.records) {

                json.info.records.each { r ->
                    def pkg = [:]

                    pkg.id = r.id
                    pkg.uuid = r.uuid
                    pkg.componentType = r.componentType

                    pkg.identifiers = []
                    r.identifiers?.each{ id ->
                        pkg.identifiers.add([namespace:id.namespace, value:id.value]);
                    }

                    pkg.altnames = []
                    r.altname?.each{ name ->
                        pkg.altnames.add(name);
                    }

                    pkg.variantNames = []
                    r.variantNames?.each{ name ->
                        pkg.variantNames.add(name);
                    }

                    pkg.updater = r.updater
                    pkg.listStatus = r.listStatus
                    pkg.listVerifiedDate = (r.listVerifiedDate != "null") ? r.listVerifiedDate : ''
                    pkg.contentType = (r.contentType != "null") ? r.contentType : ''
                    //pkg.consistent = r.consistent
                    //pkg.global = r.global

                    pkg.curatoryGroups = []
                    r.curatoryGroups.each{ curatoryGroup ->
                        pkg.curatoryGroups.add(curatoryGroup)
                    }

                    pkg.source = [:]

                    if(r.source) {
                        pkg.source.lastRun = r.source.lastRun
                        pkg.source.name = r.source.name
                        pkg.source.automaticUpdates = Boolean.valueOf(r.source.automaticUpdates)
                        if(r.source.url)
                            pkg.source.url = new URL(r.source.url)
                        pkg.source.frequency = r.source.frequency
                    }

                    pkg.titleCount = r.titleCount
                    pkg.scope = (r.scope != "null") ? r.scope : ''
                    pkg.name = (r.name != "null") ? r.name: ''
                    pkg.sortname = (r.sortname != "null") ? r.sortname: ''
                    //pkg.fixed = r.fixed
                    pkg.platformName = (r.nominalPlatformName != "null") ? r.nominalPlatformName : ''
                    pkg.platformUuid = r.nominalPlatformUuid ?: ''
                    //pkg.breakable = r.breakable
                    pkg.providerName = (r.cpname != "null") ? r.cpname : ''
                    pkg.provider = r.provider
                    pkg.providerUuid = r.providerUuid ?: ''
                    pkg.status = r.status ?: ''
                    pkg.description = (r.description != "null") ? r.description: ''
                    pkg.descriptionURL = r.descriptionURL ?: ''

                    pkg.lastUpdatedDisplay = r.lastUpdatedDisplay

                    pkg.url = apiSource.baseUrl
                    pkg.editUrl = apiSource.editUrl

                    if(r.uuid && r.uuid != "null") {
                        result.records << pkg
                    }
                }
            }
            if (json?.warning?.records) {

                json.warning.records.each { r ->
                    def pkg = [:]

                    pkg.id = r.id
                    pkg.uuid = r.uuid
                    pkg.componentType = r.componentType

                    pkg.identifiers = []
                    r.identifiers?.each{ id ->
                        pkg.identifiers.add([namespace:id.namespace, value:id.value]);
                    }

                    pkg.altnames = []
                    r.altname?.each{ name ->
                        pkg.altnames.add(name);
                    }

                    pkg.variantNames = []
                    r.variantNames?.each{ name ->
                        pkg.variantNames.add(name);
                    }

                    pkg.updater = r.updater
                    pkg.listStatus = r.listStatus
                    pkg.listVerifiedDate = (r.listVerifiedDate != "null") ? r.listVerifiedDate : ''
                    pkg.contentType = (r.contentType != "null") ? r.contentType : ''
                    //pkg.consistent = r.consistent
                    //pkg.global = r.global
                    pkg.listVerifiedDate = r.listVerifiedDate

                    pkg.curatoryGroups = []
                    r.curatoryGroups?.each{ curatoryGroup ->
                        pkg.curatoryGroups.add(curatoryGroup);
                    }

                    pkg.source = [:]
                    if(r.source) {
                        pkg.source.lastRun = r.source.lastRun
                        pkg.source.name = r.source.name
                        pkg.source.automaticUpdates = Boolean.valueOf(r.source.automaticUpdates)
                        if(r.source.url)
                            pkg.source.url = new URL(r.source.url)
                        pkg.source.frequency = r.source.frequency
                    }

                    pkg.titleCount = r.titleCount
                    pkg.scope = (r.scope != "null") ? r.scope : ''
                    pkg.name = (r.name != "null") ? r.name: ''
                    pkg.sortname = (r.sortname != "null") ? r.sortname: ''
                    //pkg.fixed = r.fixed
                    pkg.platformName = (r.nominalPlatformName != "null") ? r.nominalPlatformName : ''
                    pkg.platformUuid = r.nominalPlatformUuid ?: ''
                    //pkg.breakable = r.breakable
                    pkg.providerName = (r.cpname != "null") ? r.cpname : ''
                    pkg.provider = r.provider
                    pkg.providerUuid = r.providerUuid ?: ''
                    pkg.status = r.status ?: ''
                    pkg.description = (r.description != "null") ? r.description: ''
                    pkg.descriptionURL = r.descriptionURL ?: ''



                    pkg.lastUpdatedDisplay = r.lastUpdatedDisplay

                    pkg.url = apiSource.baseUrl
                    pkg.editUrl = apiSource.editUrl

                    if(r.uuid && r.uuid != "null") {
                        result.records << pkg
                    }
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage())
        }

        result
    }


    Map geElasticsearchSuggests(final String apiUrl, final String query, final String type, final String role) {
        String url = buildUri(apiUrl+'/suggest', query, type, role, null)
        queryElasticsearch(url)
    }

    Map geElasticsearchFindings(final String apiUrl, final String query, final String type,
                                final String role, final Integer max) {
        String url = buildUri(apiUrl+'/find', query, type, role, max)
        queryElasticsearch(url)
    }

    Map queryElasticsearch(String url){
        log.info("querying: " + url)
        Map result = [:]
        try {
            def http = new HTTPBuilder(url)
//         http.auth.basic user, pwd
            http.request(Method.GET) { req ->
                headers.'User-Agent' = 'laser'
                response.success = { resp, html ->
                    log.info("server response: ${resp.statusLine}")
                    log.debug("server:          ${resp.headers.'Server'}")
                    log.debug("content length:  ${resp.headers.'Content-Length'}")
                    if (resp.status < 400) {
                        result = ['warning': html]
                    } else {
                        result = ['info': html]
                    }
                }
                response.failure = { resp ->
                    log.error("server response: ${resp.statusLine}")
                    result = ['error': resp.statusLine]
                }
            }
            http.shutdown()
        } catch (Exception e) {
        log.error("Problem with queryElasticsearch by GokbService: "+ e)
        }
        result
    }

    private String buildUri(final String stub, final String query, final String type, final String role, final Integer max) {
        String url = stub + "?status=Current&"
        if (query) {
            url += "q=" + query + "&"
        }
        if (type){
            url += "componentType=" + type + "&"
        }
        if (role){
            url += "role=" + role + "&"
        }
        if (max){
            url += "max=" + max + "&"
        }
        url.substring(0, url.length() - 1)
    }

    Map getPackageMapWithUUID(ApiSource apiSource, String identifier) {

        log.info("getting Package map from gokb ..")

        def result

        if(apiSource && identifier) {

            try {

                String apiUrl = apiSource.baseUrl + apiSource.fixToken

                String url = apiUrl + '/find?uuid=' + identifier

                def json = queryElasticsearch(url)

                log.info("getting Package map from gokb (${apiSource.baseUrl + apiSource.fixToken})")

                if (json?.info?.records) {

                    json.info.records.each { r ->
                        def pkg = [:]

                        pkg.id = r.id
                        pkg.uuid = r.uuid
                        pkg.componentType = r.componentType

                        pkg.identifiers = []
                        r.identifiers?.each { id ->
                            pkg.identifiers.add([namespace: id.namespace, value: id.value]);
                        }

                        pkg.altnames = []
                        r.altname?.each { name ->
                            pkg.altnames.add(name);
                        }

                        pkg.variantNames = []
                        r.variantNames?.each { name ->
                            pkg.variantNames.add(name);
                        }

                        pkg.updater = r.updater
                        pkg.listStatus = r.listStatus
                        pkg.listVerifiedDate = (r.listVerifiedDate != "null") ? r.listVerifiedDate : ''
                        pkg.contentType = (r.contentType != "null") ? r.contentType : ''
                        //pkg.consistent = r.consistent
                        //pkg.global = r.global

                        pkg.curatoryGroups = []
                        r.curatoryGroups?.each { curatoryGroup ->
                            pkg.curatoryGroups.add(curatoryGroup);
                        }

                        pkg.titleCount = r.titleCount
                        pkg.scope = (r.scope != "null") ? r.scope : ''
                        pkg.name = (r.name != "null") ? r.name : ''
                        pkg.sortname = (r.sortname != "null") ? r.sortname : ''
                        //pkg.fixed = r.fixed
                        pkg.platformName = (r.nominalPlatformName != "null") ? r.nominalPlatformName : ''
                        pkg.platformUuid = r.nominalPlatformUuid ?: ''
                        //pkg.breakable = r.breakable
                        pkg.providerName = (r.cpname != "null") ? r.cpname : ''
                        pkg.provider = r.provider
                        pkg.providerUuid = r.providerUuid ?: ''
                        pkg.status = r.status ?: ''
                        pkg.description = (r.description != "null") ? r.description : ''
                        pkg.descriptionURL = r.descriptionURL ?: ''

                        pkg.lastUpdatedDisplay = r.lastUpdatedDisplay

                        pkg.url = apiSource.baseUrl
                        pkg.editUrl = apiSource.editUrl

                        if (r.uuid && r.uuid != "null") {
                            result = pkg
                        }
                    }
                }
                if (json?.warning?.records) {

                    json.warning.records.each { r ->
                        def pkg = [:]

                        pkg.id = r.id
                        pkg.uuid = r.uuid
                        pkg.componentType = r.componentType

                        pkg.identifiers = []
                        r.identifiers?.each { id ->
                            pkg.identifiers.add([namespace: id.namespace, value: id.value]);
                        }

                        pkg.altnames = []
                        r.altname?.each { name ->
                            pkg.altnames.add(name);
                        }

                        pkg.variantNames = []
                        r.variantNames?.each { name ->
                            pkg.variantNames.add(name);
                        }

                        pkg.updater = r.updater
                        pkg.listStatus = r.listStatus
                        pkg.listVerifiedDate = (r.listVerifiedDate != "null") ? r.listVerifiedDate : ''
                        pkg.contentType = (r.contentType != "null") ? r.contentType : ''
                        //pkg.consistent = r.consistent
                        //pkg.global = r.global

                        pkg.curatoryGroups = []
                        r.curatoryGroups?.each { curatoryGroup ->
                            pkg.curatoryGroups.add(curatoryGroup);
                        }

                        pkg.titleCount = r.titleCount
                        pkg.scope = (r.scope != "null") ? r.scope : ''
                        pkg.name = (r.name != "null") ? r.name : ''
                        pkg.sortname = (r.sortname != "null") ? r.sortname : ''
                        //pkg.fixed = r.fixed
                        pkg.platformName = (r.nominalPlatformName != "null") ? r.nominalPlatformName : ''
                        pkg.platformUuid = r.nominalPlatformUuid ?: ''
                        //pkg.breakable = r.breakable
                        pkg.providerName = (r.cpname != "null") ? r.cpname : ''
                        pkg.provider = r.provider
                        pkg.providerUuid = r.providerUuid ?: ''
                        pkg.status = r.status ?: ''
                        pkg.description = (r.description != "null") ? r.description : ''
                        pkg.descriptionURL = r.descriptionURL ?: ''

                        pkg.lastUpdatedDisplay = r.lastUpdatedDisplay

                        pkg.url = apiSource.baseUrl
                        pkg.editUrl = apiSource.editUrl

                        if (r.uuid && r.uuid != "null") {
                            result = pkg
                        }
                    }
                }


            } catch (Exception e) {
                log.error(e.getMessage())
            }
        }

        result
    }

    def searchESGOKB(params){
        searchESGOKB(params, reversemap)
    }

    def searchESGOKB(params, field_map){

        log.debug("ESGOKBSearchService::search - ${params}")

        Map<String, Object> result = [:]

        RestHighLevelClient esclient = this.getClient()
        Map<String, String> esSettings =  this.getESGOKBSettings()

        try {
            if(this.testConnection()) {

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

                        SearchRequest searchRequest = new SearchRequest(esSettings.indexName)
                        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()

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

                        log.debug("index: ${esSettings.indexName} -> searchRequestBuilder: ${query_str}")

                        if (params.actionName == 'index') {

                            NestedAggregationBuilder nestedAggregationBuilder = new NestedAggregationBuilder('status', 'status')

                            searchSourceBuilder.query(QueryBuilders.queryStringQuery(query_str))
                            searchSourceBuilder.aggregation(AggregationBuilders.terms('rectype').size(25).field('rectype'))
                            searchSourceBuilder.aggregation(AggregationBuilders.terms('provider').size(50).field('provider'))
                            searchSourceBuilder.aggregation(nestedAggregationBuilder.subAggregation(AggregationBuilders.terms('status').size(50).field('status.value')))
                            searchSourceBuilder.aggregation(AggregationBuilders.terms('curatoryGroups').size(50).field('curatoryGroups'))

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
                    }
                    catch (Exception ex) {
                        log.error("Error processing ${esSettings.indexName} ${query_str}", ex)
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
                        result.resultsTotal = searchResponse.getHits().getTotalHits().value ?: "0"
                        result.index = esSettings.indexName

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
                log.error("Problem by Close ESGOKB Client",e)
            }
        }
        result
    }

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
                    //sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
                }
            }else {
                if(params.q.contains(":") || params.q.contains("-")) {
                    //params.q = params.q.replaceAll('\\*', '')
                    sw.write("\"${params.q}\"")
                }else if (params.q.count("\"") >= 2){
                    sw.write("${params.q}")
                    //sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
                }else{

                    if(DateUtils.isDate(params.q)){
                        params.q = DateUtils.parseDateGeneric(params.q).format("yyyy-MM-dd").toString()
                    }

                    params.q = params.q.replaceAll('\\"', '')
                    sw.write("${params.q}")
                    //sw.write(" AND ((NOT gokbId:'${params.q}') AND (NOT guid:'${params.q}')) ")
                }
            }
        }


        field_map.each { mapping ->

            if ( params[mapping.key] != null ) {
                if ( params[mapping.key].class == java.util.ArrayList) {
                    if(sw.toString()) sw.write(" AND ")

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

                    log.debug("Processing ${params[mapping.key]} ${mapping.key}")

                    try {
                        if ( params[mapping.key] ) {
                            if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                                if (sw.toString()) sw.write(" AND ")
                                sw.write(mapping.value)
                                sw.write(":")
                                if (params[mapping.key].startsWith("[") && params[mapping.key].endsWith("]")) {
                                    sw.write("${params[mapping.key]}")
                                } else if (params[mapping.key].count("\"") >= 2) {
                                    sw.write("${params[mapping.key]}")
                                } else {
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


        if(!params.showDeleted)
        {
            sw.write(  " AND ( NOT status:\"Deleted\" )")
        }

        if(params.showAllTitles) {
            sw.write(  " AND ((rectype: \"EBookInstance\") OR (rectype: \"JournalInstance\") OR (rectype: \"BookInstance\") OR (rectype: \"TitleInstance\") OR (rectype: \"DatabaseInstance\")) ")
        }

        sw.toString()
    }

    RestHighLevelClient getClient() {
        esgokb_client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esgokb_host, 9200, "http"),
                        new HttpHost(esgokb_host, 9201, "http")));

        esgokb_client
    }

    Map<String, String> getESGOKBSettings(){
        Map<String, String> result = [:]

        result.clusterName = esgokb_cluster_name
        result.host = esgokb_host
        result.indexName = esgokb_index_name

        result
    }

    void closeClient() {
        esgokb_client.close()
    }

    boolean testConnection() {

        try {
            boolean response = esgokb_client.ping(RequestOptions.DEFAULT)

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



}
