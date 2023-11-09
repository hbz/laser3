package de.laser.http

import de.laser.utils.AppUtils
import grails.converters.JSON
import groovy.json.JsonOutput
import groovy.util.logging.Slf4j
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import io.micronaut.http.client.HttpClient
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClientConfiguration
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.grails.web.json.JSONObject

/**
 * Class responsible for requests of the app to other servers. It should collect
 * every request done by the app in order to use the same client library in the whole code
 */
@Slf4j
class BasicHttpClient {

    /**
     * Supported are GET and POST methods
     */
    static enum Method {
        GET,
        POST
    }

    /**
     * Supported are plain text, JSON and XML
     */
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

    /**
     * Supported are plain text, JSON, SOAP and URL-encoded
     */
    static enum PostType {
        TEXT    ('text/plain'),
        JSON    ('application/json'),
        SOAP    ('application/soap+xml'),
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
    boolean silentMode

    /**
     * Constructor for a client with the basic settings
     * @param url the URL which should be requested
     * @param silentMode should the request output be suppressed or not?
     */
    BasicHttpClient(String url, boolean silentMode = true) {
        try {
            this.url = url.toURL()
            this.silentMode = silentMode
            client = HttpClient.create(this.url)

            if (silentMode) {
                // log.debug 'Using BasicHttpClient(' + url + ') in silentMode'
            }
        }
        catch(Exception e) {
            log.error e.getMessage()
        }
        this
    }

    /**
     * Constructor for a client with customized configuration settings
     * @param url the URL which should be requested
     * @param config the configuration settings for this client
     * @param silentMode should the request output be suppressed or not?
     * @see HttpClient#create(java.net.URL, HttpClientConfiguration)
     */
    BasicHttpClient(String url, HttpClientConfiguration config, boolean silentMode = true) {
        try {
            this.url = url.toURL()
            this.silentMode = silentMode
            client = HttpClient.create(this.url, config)

            if (silentMode) {
                // log.debug 'Using BasicHttpClient(' + url + ', config) in silentMode'
            }
        }
        catch(Exception e) {
            log.error e.getMessage()
        }
        this
    }

    /**
     * Closes the current client
     */
    void close() {
        try {
            client.close()
        }
        catch(Exception e) {
            log.error e.toString()
        }
    }

    // --->

    /**
     * Performs a GET request, expecting the given response type
     * @param responseType the {@link ResponseType} expected
     * @return the result of {@link #get(java.util.Map, de.laser.http.BasicHttpClient.ResponseType, groovy.lang.Closure, groovy.lang.Closure)}
     */
    HttpResponse get(ResponseType responseType) {
        get(null, responseType, null, null)
    }

    /**
     * Performs a GET request, expecting the given response type, calling back the given closures
     * @param responseType the {@link ResponseType} expected
     * @param onSuccess the closure to call on success
     * @param onFailure the closure to call on failure
     * @return the result of {@link #get(java.util.Map, de.laser.http.BasicHttpClient.ResponseType, groovy.lang.Closure, groovy.lang.Closure)}
     */
    HttpResponse get(ResponseType responseType, Closure onSuccess, Closure onFailure) {
        get(null, responseType, onSuccess, onFailure)
    }

    /**
     * Performs a GET request, using the given request headers and expecting the given response type, calling back the given closures
     * @param customHeaders headers specified for this request
     * @param responseType the {@link ResponseType} expected
     * @param onSuccess the closure to call on success
     * @param onFailure the closure to call on failure
     * @return the result of {@link #request(de.laser.http.BasicHttpClient.Method, java.util.Map, de.laser.http.BasicHttpClient.ResponseType, de.laser.http.BasicHttpClient.PostType, java.lang.Object, groovy.lang.Closure, groovy.lang.Closure)}
     */
    HttpResponse get(Map<String, String> customHeaders, ResponseType responseType, Closure onSuccess, Closure onFailure) {
        request(Method.GET, customHeaders, responseType, null, null, onSuccess, onFailure)
    }

    /**
     * Performs a POST request with the given body in the specified post type, expecting the given response type
     * @param responseType the {@link ResponseType} expected
     * @param postType the type of data used for request
     * @param body the request body, containing the data
     * @return the result of {@link #post(java.util.Map, de.laser.http.BasicHttpClient.ResponseType, de.laser.http.BasicHttpClient.PostType, java.lang.Object, groovy.lang.Closure, groovy.lang.Closure)}
     */
    HttpResponse post(ResponseType responseType, PostType postType, def body) {
        post(null, responseType, postType, body, null, null)
    }

    /**
     * Performs a POST request with the given body in the specified post type, expecting the given response type, calling back the given closures
     * @param responseType the {@link ResponseType} expected
     * @param postType the type of data used for request
     * @param body the request body, containing the data
     * @param onSuccess the callback for successful requests
     * @param onFailure the callback for failed requests
     * @return the result of {@link #post(java.util.Map, de.laser.http.BasicHttpClient.ResponseType, de.laser.http.BasicHttpClient.PostType, java.lang.Object, groovy.lang.Closure, groovy.lang.Closure)}
     */
    HttpResponse post(ResponseType responseType, PostType postType, def body, Closure onSuccess, Closure onFailure) {
        post(null, responseType, postType, body, onSuccess, onFailure)
    }

    /**
     * Performs a POST request with the given body in the specified post type, expecting the given response type, calling back the given closures, using the specified request headers
     * @param customHeaders the request headers specified for the current request
     * @param responseType the {@link ResponseType} expected
     * @param postType the type of data used for request
     * @param body the request body, containing the data
     * @param onSuccess the callback for successful requests
     * @param onFailure the callback for failed requests
     * @return the result of {@link #request(de.laser.http.BasicHttpClient.Method, java.util.Map, de.laser.http.BasicHttpClient.ResponseType, de.laser.http.BasicHttpClient.PostType, java.lang.Object, groovy.lang.Closure, groovy.lang.Closure)}
     */
    HttpResponse post(Map<String, String> customHeaders, ResponseType responseType, PostType postType, def body, Closure onSuccess, Closure onFailure) {
        request(Method.POST, customHeaders, responseType, postType, body, onSuccess, onFailure)
    }

    // --->

    /**
     * Delegates the request to the inner methods performing the actual requests and handles the callback calls depending on the response status
     * @param method the method - one of {@link Method#GET}, {@link Method#POST} - used for the request
     * @param customHeaders the headers specified for the current request
     * @param responseType the type of response expected - one of {@link ResponseType}
     * @param postType the type of POST request body - one of {@link PostType}
     * @param body the request body (for POST requests only)
     * @param onSuccess the closure to be called on success, i.e. HTTP status 200
     * @param onFailure the closure to be called on failure, i.e. HTTP status != 200
     * @return the response object
     */
    HttpResponse request(Method method, Map<String, String> customHeaders, ResponseType responseType, PostType postType, def body, Closure onSuccess, Closure onFailure) {
        HttpResponse response = null

        if (!silentMode) {
            log.debug '[ request ] ' + url + ' (Method: ' + method + ', Accept: ' + responseType + ', Content-Type: ' + postType + ')'
        }

        if (method == Method.GET) { 
            response = innerGET(responseType, customHeaders) 
        }
        else if (method == Method.POST) { 
            response = innerPOST(responseType, postType, body, customHeaders) 
        }

        int sc = response ? response.getStatus().getCode() : -1
        if (!silentMode) {
            log.debug '[ request ] httpStatusCode: ' + sc
        }

        if (sc >= 200 && sc < 300) {
            if (onSuccess) {
                onSuccess.call(response, getParsedResponse(response, responseType))
            }
        }
        else if (sc >= 300 && sc < 400) {
            if (onSuccess) {
                log.warn '[ request ] ' + url + ' > '+ sc + response.getStatus().getReason()
                onFailure.call(response, getParsedResponse(response, responseType))
            }
        }
        else if (sc >= 400) {
            if (onFailure) {
                log.warn '[ request ] ' + url + ' > '+ sc + response.getStatus().getReason()
                onFailure.call(response, getParsedResponse(response, responseType))
            }
        }
        else if (sc < 0) {
            if (onFailure) {
                log.warn '[ request ] ' + url + ' > '+ sc
                onFailure.call(response, response?.getBody())
            }
        }
        response
    }

    // --->

    /**
     * Performs the given GET request and returns the response
     * @param responseType the type of response expected, one of {@link ResponseType}
     * @param customHeaders the headers for this request
     * @return the response of the request
     */
    HttpResponse innerGET(ResponseType responseType, Map<String, String> customHeaders) {
        HttpResponse response = null

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
            if(e instanceof HttpClientResponseException) {
                log.error '[ innerGET ] ' + e.getStatus()
                response = e.getResponse()
            }
            else {
                log.error '[ innerGET ] ' + e.getMessage()
                e.printStackTrace()
            }
        }
        response
    }

    /**
     * Performs the given POST request and returns the response
     * @param responseType the type of response expected, one of {@link ResponseType}
     * @param postType the type of data submitted with the request body, one of {@link PostType}
     * @param body the request body
     * @param customHeaders the headers for this request
     * @return the response of the request
     */
    HttpResponse innerPOST(ResponseType responseType, PostType postType, def body = '', Map<String, String> customHeaders) {
        HttpResponse response = null

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
                        log.warn '[ innerPOST ] too complex URLENC found! Check payload to avoid possible problems'
                    }
                }
                if (!silentMode && AppUtils.getCurrentServer() == AppUtils.LOCAL) {
                    log.debug '[ innerPOST ] payload: ' + body.toString()
                }
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
            if(e instanceof HttpClientResponseException) {
                log.error '[ innerGET ] ' + e.getStatus()
                response = e.getResponse()
            }
            else {
                log.error '[ innerPOST ] ' + e.getMessage()
                e.printStackTrace()
            }
        }
        response
    }

    // --->

    /**
     * Parses the raw response according to the given response type, returning the appropriate object
     * @param response the raw {@link HttpResponse} object
     * @param responseType the {@link ResponseType} expected for this request
     * @return the parsed response object for further processing:
     * <ul>
     *     <li>if plain text: a plain text response string</li>
     *     <li>if {@link ResponseType#JSON}: a JSON object</li>
     *     <li>if {@link ResponseType#XML}: a {@link GPathResult} object</li>
     * </ul>
     */
    static def getParsedResponse(HttpResponse response, ResponseType responseType) {
        def result = null

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
                log.warn '[ getParsedResponse ] return type ' + result.getClass() + ' != ' + responseType.returnType
            }
        }
        catch (Exception e) {
            log.error '[ getParsedResponse ] ' + e.getMessage()
            e.printStackTrace()
        }
        result
    }
}