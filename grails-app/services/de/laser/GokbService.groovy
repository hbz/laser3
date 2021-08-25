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

    Map doQuery(Map ctrlResult, Map params, String esQuery) {
        Map result = [:]
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        Map<String, String> pagination = setupPaginationParams(ctrlResult, params)

        Set records = []
        Map queryResult = queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + '/find' + esQuery + pagination.sort + pagination.order + pagination.max + pagination.offset)
        if (queryResult.warning) {
            records.addAll(queryResult.warning.records)
            result.recordsCount = queryResult.warning.count
            result.records = records
        }
        else {
            result.recordsCount = 0
            result.records = records
        }
        result
    }

    Map<String, String> setupPaginationParams(Map ctrlResult, Map params) {
        String sort = params.sort ? "&sort=" + params.sort : "&sort=sortname"
        String order = params.order ? "&order=" + params.order : "&order=asc"
        String max = params.max ? "&max=${params.max}" : "&max=${ctrlResult.max}"
        String offset = params.offset ? "&offset=${params.offset}" : "&offset=${ctrlResult.offset}"
        [sort: sort, order: order, max: max, offset: offset]
    }

    Map queryElasticsearch(String url){
        String compatibleUrl = url.replaceAll(" ", "+")
        log.info("querying: " + compatibleUrl)
        Map result = [:]
        try {
            def http = new HTTPBuilder(compatibleUrl)
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
                    result = ['error': resp.status]
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

}
