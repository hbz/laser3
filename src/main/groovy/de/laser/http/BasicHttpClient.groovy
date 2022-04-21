package de.laser.http

import com.k_int.kbplus.GlobalSourceSyncService
import de.laser.GlobalRecordSource
import de.laser.exceptions.SyncException
import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.HTTPBuilder
import io.micronaut.http.client.HttpClient
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.web.json.JSONObject

class BasicHttpClient {

    static Log static_logger = LogFactory.getLog(BasicHttpClient)

    // --->

    static enum Method {
        GET,
        POST
    }

    static enum ResponseType {
        TEXT    (['text/plain'], String, String),
        JSON    (['application/json', 'application/javascript', 'text/javascript'], String, JSONObject),
        XML     (['application/xml', 'text/xml', 'application/xhtml+xml', 'application/atom+xml'], String, GPathResult),
        // BINARY  (['application/octet-stream'], Object, Object),
        // ANY     (['*/*'], Object, Object)

        final List<String> mineTypes
        final Class parserType
        final Class returnType

        ResponseType(List<String> mineTypes, Class parserType, Class returnType) {
            this.mineTypes = mineTypes
            this.parserType = parserType
            this.returnType = returnType
        }

        String getAcceptHeader() {
            this.mineTypes.join(',')
        }
    }

    static enum PostType {
        TEXT    ('text/plain'),
        JSON    ('application/json'),
        URLENC  ('application/x-www-form-urlencoded')

        final String mineType

        PostType(String mineType) {
            this.mineType = mineType
        }

        String getContentTypeHeader() {
            this.mineType + '; charset=utf-8'
        }
    }

    // --->

    HttpClient client
    URL url

    BasicHttpClient(String url) {
        try {
            this.url = url.toURL()
            client = HttpClient.create(this.url)
        }
        catch(Exception e) {
            static_logger.error e.getMessage()
        }
        this
    }

    HttpResponse get(ResponseType responseType) {
        get(null, responseType, null, null)
    }
    HttpResponse get(ResponseType responseType, Closure onSuccess, Closure onFailure) {
        get(null, responseType, onSuccess, onFailure)
    }
    HttpResponse get(Map<String, String> customHeaders, ResponseType responseType, Closure onSuccess, Closure onFailure) {
        request(Method.GET, customHeaders, responseType, null, null, onSuccess, onFailure)
    }

    HttpResponse post(ResponseType responseType, PostType postType, def body) {
        post(null, responseType, postType, body, null, null)
    }
    HttpResponse post(ResponseType responseType, PostType postType, def body, Closure onSuccess, Closure onFailure) {
        post(null, responseType, postType, body, onSuccess, onFailure)
    }
    HttpResponse post(Map<String, String> customHeaders, ResponseType responseType, PostType postType, def body, Closure onSuccess, Closure onFailure) {
        request(Method.POST, customHeaders, responseType, postType, body, onSuccess, onFailure)
    }

    // --->

    HttpResponse request(Method method, Map<String, String> customHeaders, ResponseType responseType, PostType postType, def body, Closure onSuccess, Closure onFailure) {
        HttpResponse response

        static_logger.debug '[ request ] ' + url + ' (Method: ' + method + ', Accept: ' + responseType + ', Content-Type: ' + postType + ')'

        if (method == Method.GET) { 
            response = innerGET(responseType, customHeaders) 
        }
        else if (method == Method.POST) { 
            response = innerPOST(responseType, postType, body, customHeaders) 
        }

        int sc = response ? response.code() : -1
        static_logger.debug '[ request ] httpStatusCode: ' + sc

        if (sc >= 200 && sc < 300) {
            if (onSuccess) {
                onSuccess.call(response, getParsedResponse(response, responseType))
            }
        }
        else if (sc >= 300 && sc < 400) {
            if (onSuccess) {
                static_logger.warn '[ request ] ' + url + ' > '+ sc + response.getStatus().getReason()
                onFailure.call(response, response.getBody())
            }
        }
        else if (sc >= 400) {
            if (onFailure) {
                static_logger.warn '[ request ] ' + url + ' > '+ sc + response.getStatus().getReason()
                onFailure.call(response, response.getBody())
            }
        }
        response
    }

    // --->

    HttpResponse innerGET(ResponseType responseType, Map<String, String> customHeaders) {
        HttpResponse response

        responseType = responseType ?: ResponseType.TEXT
        customHeaders = customHeaders ?: [:]

        try {
            HttpRequest request = HttpRequest.GET(url.toURI())

            customHeaders.putAt('Accept', responseType.getAcceptHeader())
            customHeaders.each{ it ->
                request.header( it.key, it.value )
            }
            response = client.toBlocking().exchange(request, responseType.parserType)
        }
        catch(Exception e) {
            static_logger.error '[ innerGET ] ' + e.getMessage()
            e.printStackTrace()
        }
        response
    }

    HttpResponse innerPOST(ResponseType responseType, PostType postType, def body = '', Map<String, String> customHeaders) {
        HttpResponse response

        responseType = responseType ?: ResponseType.TEXT
        postType = postType ?: PostType.JSON
        body = body ?: ''
        customHeaders = customHeaders ?: [:]

        try {
            if (postType == PostType.TEXT) {
                body = body.toString()
            }
            else if (postType == PostType.JSON) {
                body = JsonOutput.toJson( body )
            }
            else if (postType == PostType.URLENC) {
                Closure encode = { value -> URLEncoder.encode("$value".toString(), 'UTF-8') }

                // simple parsing only ..
                if (body instanceof Map) {
                    boolean warning = false

                    body = body.collect { it ->
                        if (it.value instanceof Map) { warning = true }
                        if (it.value instanceof Collection) {
                            it.value.collect { val ->
                                if (val instanceof Map || val instanceof Collection) { warning = true }
                                encode(it.key) + '=' + encode(val)
                            }.join('&')
                        } else {
                            encode(it.key) + '=' + encode(it.value)
                        }
                    }.join('&')


                    if (warning) {
                        static_logger.warn '[ innerPOST ] too complex URLENC found! Check payload to avoid possible problems'
                    }
                }
                static_logger.debug '[ innerPOST ] payload: ' + body.toString()
            }
            HttpRequest request = HttpRequest.POST(url.toURI(), body)

            customHeaders.putAt('Accept', responseType.getAcceptHeader())
            customHeaders.putAt('Content-Type', postType.getContentTypeHeader())
            customHeaders.each{ it ->
                request.header( it.key, it.value )
            }
            response = client.toBlocking().exchange(request, responseType.parserType)
        }
        catch(Exception e) {
            static_logger.error '[ innerPOST ] ' + e.getMessage()
            e.printStackTrace()
        }
        response
    }

    // --->

    static def getParsedResponse(HttpResponse response, ResponseType responseType) {
        def result

        try {
            if (responseType == ResponseType.TEXT) {
                result = response.body() as String
            }
            else if (responseType == ResponseType.JSON) {
                result = JSON.parse( response.body() as String )
            }
            else if (responseType == ResponseType.XML) {
                XmlSlurper xml = new XmlSlurper()
                result = xml.parseText( response.body() as String )
            }
            else {
                result = response.body()
            }

            if (result?.getClass() != responseType.returnType) {
                static_logger.warn '[ getParsedResponse ] return type ' + result.getClass() + ' != ' + responseType.returnType
            }
        }
        catch (Exception e) {
            static_logger.error '[ getParsedResponse ] ' + e.getMessage()
            e.printStackTrace()
        }
        result
    }
}