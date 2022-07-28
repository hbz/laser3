package de.laser

import de.laser.http.BasicHttpClient
import de.laser.remote.ApiSource
import grails.gorm.transactions.Transactional

/**
 * Is actually a we:kb service. It contains methods to communicate with the we:kb ElasticSearch index
 * @see de.laser.remote.ApiSource
 */
@Transactional
class GokbService {

    @Deprecated
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

    @Deprecated
    Map geElasticsearchSuggests(final String apiUrl, final String query, final String type, final String role) {
        String url = _buildUri(apiUrl+'/suggest', query, type, role, null)
        queryElasticsearch(url)
    }

    /**
     * Builds the query for the ElasticSearch index and retrieves the findings of the API
     * @param apiUrl the URL for the we:kb ElasticSearch index
     * @param query the query string
     * @param type the component type to be fetched
     * @param role (only to be used for organisation queries) the role of the organisation
     * @param max the count of records to fetch
     * @return the ElasticSearch result map
     */
    Map geElasticsearchFindings(final String apiUrl, final String query, final String type,
                                final String role, final Integer max) {
        String url = _buildUri(apiUrl+'/find', query, type, role, max)
        queryElasticsearch(url)
    }

    /**
     * A wrapper for controller-fed filters using the ElasticSearch data
     * @param ctrlResult the base result of the controller
     * @param params the request parameter map
     * @param esQuery the query string
     * @return the ElasticSearch result map
     */
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

    /**
     * Sets up the parameters for the ElasticSearch result pagination
     * @param ctrlResult the generics from the controller
     * @param params the request parameter map
     * @return the query string parts for sort, order, max and offset, in a named map
     */
    Map<String, String> setupPaginationParams(Map ctrlResult, Map params) {
        String sort = params.sort ? "&sort=" + params.sort : "&sort=sortname"
        String order = params.order ? "&order=" + params.order : "&order=asc"
        String max = params.max ? "&max=${params.max}" : "&max=${ctrlResult.max}"
        String offset = params.offset ? "&offset=${params.offset}" : "&offset=${ctrlResult.offset}"
        [sort: sort, order: order, max: max, offset: offset]
    }

    /**
     * Performs the given query on the we:kb ElasticSearch index API. Note that communication is not set to
     * the index directly but an API endpoint takes the query and generates more complex ElasticSearch
     * queries in order to limit external index access
     * @param url the query string to pass to the we:kb ElasticSearch API
     * @return the result map (access either as result.warning or result.info), reflecting the ElasticSearch response
     */
    Map queryElasticsearch(String url){
        Map result = [:]

        try {
            BasicHttpClient http = new BasicHttpClient( url.replaceAll(" ", "+") )

            Closure success = { resp, json ->
                log.info ("server response: ${resp.getStatus().getReason()}")
                log.debug("server:          ${resp.getHeaders().get('Server')}")
                log.debug("content length:  ${resp.getHeaders().get('Content-Length')}")

//                if (resp.getStatus().getCode() < 400) {
                    result = ['warning': json]      // warning <-> info ?
//                } else {
//                    result = ['info': json]         // ???
//                }
            }
            Closure failure = { resp ->
                log.warn ('Response: ' + resp.getStatus().getCode() + ' - ' + resp.getStatus().getReason())
                result = ['error': resp.getStatus().getCode()]
            }

            http.get(['User-Agent' : 'laser'], BasicHttpClient.ResponseType.JSON, success, failure)

        } catch (Exception e) {
            log.error e.getMessage()
        }
        result
    }

    /**
     * Builds the query string for the ElasticSearch API query
     * @param stub a base stub containing generic component parameters
     * @param query a name query restriction
     * @param type the component type to use
     * @param role (use only for componentType=Org) the organisational role of the queried organisation
     * @param max the maximum count of entries to fetch
     * @return the query URL string
     */
    private String _buildUri(final String stub, final String query, final String type, final String role, final Integer max) {
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
}
