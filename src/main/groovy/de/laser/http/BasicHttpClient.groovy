package de.laser.http

import grails.converters.JSON
import groovy.util.slurpersupport.GPathResult
import io.micronaut.http.client.HttpClient
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.grails.web.json.JSONObject

class BasicHttpClient {

    static Log static_logger = LogFactory.getLog(BasicHttpClient)

    static enum ContentType {
        TEXT    ('TEXT',  ['text/plain'], String, String),
        JSON    ('JSON',  ['application/json', 'application/javascript', 'text/javascript'], String, JSONObject),
        XML     ('XML',   ['application/xml', 'text/xml', 'application/xhtml+xml', 'application/atom+xml'], String, GPathResult)
        // HTML    ('HTML',   ['text/html'], String, GPathResult),
        // URLENC  ('URLENC', ['application/x-www-form-urlencoded'], Object, Object),
        // BINARY  ('BINARY', ['application/octet-stream'], Object, Object),
        // ANY     ('ANY',   ['*/*'], Object, Object)

        final String id
        final List<String> acceptHeaders
        final Class bodyType
        final Class returnType

        ContentType(String id, List<String> acceptHeaders, Class bodyType, Class returnType) {
            this.id = id
            this.acceptHeaders = acceptHeaders
            this.bodyType = bodyType
            this.returnType = returnType
        }

        String getAcceptHeaders() {
            this.acceptHeaders.join(',')
        }
    }

    static enum Method {
        GET,
        POST
    }

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

    def request(Method method, ContentType contentType, Map<String, String> customHeaders, def body, Closure onSuccess, Closure onFailure) {
        HttpResponse response

        static_logger.debug '' + url + ' -> '+ method + ', ' + contentType

        if (method == Method.GET) {
            response = get(contentType, customHeaders)
        }
        else if (method == Method.POST) {
            response = post(contentType, customHeaders, body)
        }

        int sc = response ? response.code() : -1
        static_logger.debug 'HttpStatusCode: ' + sc

        if (sc >= 200 && sc < 300) {
            if (onSuccess) {
                onSuccess.call(response, getResponseBodyParsed(response, contentType))
            }
        }
        else if (sc >= 300 && sc < 400) {
            if (onSuccess) {
                static_logger.warn '' + url + ' > '+ sc + response.getStatus().getReason()
                onSuccess.call(response, getResponseBodyParsed(response, contentType))
            }
        }
        else if (sc >= 400) {
            if (onFailure) {
                onFailure.call(response, response.getBody())
            }
        }

        response
    }

    HttpResponse get(ContentType contentType = ContentType.TEXT, Map<String, String> customHeaders = [:]) {
        HttpResponse response

        try {
            HttpRequest request = HttpRequest.GET(url.toURI())

            request.header('Accept', contentType.getAcceptHeaders())
            customHeaders.each{ it ->
                request.header( it.key, it.value )
            }
            response = client.toBlocking().exchange(request, contentType.bodyType)
        }
        catch(Exception e) {
            static_logger.error e.getMessage()
        }
        response
    }

    HttpResponse post(ContentType contentType = ContentType.TEXT, Map<String, String> customHeaders = [:], def body = '') {
        HttpResponse response

        try {
            HttpRequest request = HttpRequest.POST(url.toURI(), body)

            request.header('Accept', contentType.getAcceptHeaders())
            customHeaders.each{ it ->
                request.header( it.key, it.value )
            }
            response = client.toBlocking().exchange(request, contentType.bodyType)
        }
        catch(Exception e) {
            static_logger.error e.getMessage()
        }
        response
    }

    // --- parser ---

    static def getResponseBodyParsed(HttpResponse response, ContentType contentType) {
        def result

        try {
            if (contentType == ContentType.TEXT) {
                result = response.body() as String
            }
            else if (contentType == ContentType.JSON) {
                result = JSON.parse( response.body() as String )
            }
            else if (contentType == ContentType.XML) {
                XmlSlurper xml = new XmlSlurper()
                result = xml.parseText( response.body() as String )
            }
            //        if (contentType == ContentType.ANY) {
            //            result = response.body()
            //        }
            //        else
            //        else if (contentType == ContentType.HTML) {
            //            XMLReader p = new SAXParser()
            //            result = (new XmlSlurper(p)).parseText( response.body() as String ) as GPathResult
            //        }
            else {
                result = response.body()
            }

            if (result?.getClass() != contentType.returnType) {
                static_logger.warn 'return type ' + result.getClass() + ' != ' + contentType.returnType
            }
        }
        catch (Exception e) {
            static_logger.error e.getMessage()
        }
        result
    }
}