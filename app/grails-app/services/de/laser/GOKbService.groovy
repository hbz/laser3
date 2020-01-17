package de.laser

import com.k_int.kbplus.ApiSource
import grails.transaction.Transactional
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

@Transactional
class GOKbService {

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
                    r.curatoryGroups?.each{ curatoryGroup ->
                        pkg.curatoryGroups.add(curatoryGroup);
                    }

                    pkg.titleCount = r.titleCount
                    pkg.scope = (r.scope != "null") ? r.scope : ''
                    pkg.name = (r.name != "null") ? r.name: ''
                    pkg.sortname = (r.sortname != "null") ? r.sortname: ''
                    //pkg.fixed = r.fixed
                    pkg.platformName = (r.platformName != "null") ? r.platformName : ''
                    pkg.platformUuid = r.platformUuid ?: ''
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

                    pkg.titleCount = r.titleCount
                    pkg.scope = (r.scope != "null") ? r.scope : ''
                    pkg.name = (r.name != "null") ? r.name: ''
                    pkg.sortname = (r.sortname != "null") ? r.sortname: ''
                    //pkg.fixed = r.fixed
                    pkg.platformName = (r.platformName != "null") ? r.platformName : ''
                    pkg.platformUuid = r.platformUuid ?: ''
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
        def http = new HTTPBuilder(url)
//         http.auth.basic user, pwd
        http.request(Method.GET) { req ->
            headers.'User-Agent' = 'laser'
            response.success = { resp, html ->
                log.info("server response: ${resp.statusLine}")
                log.debug("server:          ${resp.headers.'Server'}")
                log.debug("content length:  ${resp.headers.'Content-Length'}")
                if(resp.status < 400){
                    result = ['warning':html]
                }
                else {
                    result = ['info':html]
                }
            }
            response.failure = { resp ->
                log.error("server response: ${resp.statusLine}")
                result = ['error':resp.statusLine]
            }
        }
        http.shutdown()
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
                        pkg.platformName = (r.platformName != "null") ? r.platformName : ''
                        pkg.platformUuid = r.platformUuid ?: ''
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
                        pkg.platformName = (r.platformName != "null") ? r.platformName : ''
                        pkg.platformUuid = r.platformUuid ?: ''
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
