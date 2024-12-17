package de.laser

import de.laser.config.ConfigMapper
import de.laser.http.BasicHttpClient
import de.laser.remote.Wekb
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import io.micronaut.http.client.DefaultHttpClientConfiguration
import io.micronaut.http.client.HttpClientConfiguration
import org.springframework.context.MessageSource

import java.time.Duration

/**
 * Is actually a we:kb service. It contains methods to communicate with the we:kb ElasticSearch index
 * @see Wekb
 */
@Transactional
class GokbService {

    MessageSource messageSource

    /**
     * A wrapper for controller-fed filters using the ElasticSearch data
     * @param ctrlResult the base result of the controller
     * @param params the pagination setting data
     * @param queryParams the request parameter map
     * @return the ElasticSearch result map
     */
    Map doQuery(Map ctrlResult, Map params, Map queryParams) {
        Map result = [:]
        queryParams.putAll(setupPaginationParams(ctrlResult, params))

        Set records = []

        Map queryResult = executeQuery(Wekb.getSearchApiURL(), queryParams)
        if (queryResult && queryResult.result) {
            records.addAll(queryResult.result)
            result.recordsCount = queryResult.result_count_total
            result.records = records
        }
        else {
            if(queryResult) {
                if(queryResult.code == "error")
                    result.error = messageSource.getMessage('wekb.error.500', [queryResult.message] as Object[], LocaleUtils.getCurrentLocale())
            }
            else result.error = messageSource.getMessage('wekb.error.404', null, LocaleUtils.getCurrentLocale())
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
        String sort = params.sort ?: "sortname"
        String order = params.order ?: "asc"
        String max = params.max ?: ctrlResult.max
        String offset = (params.offset != null) ? params.offset : ctrlResult.offset
        Map<String, String> result = [sort: sort, order: order]
        if(max)
            result.max = max
        if(offset)
            result.offset = offset
        result
    }

    /**
     * Performs the given query on the we:kb ElasticSearch index API. Note that communication is not set to
     * the index directly but an API endpoint takes the query and generates more complex ElasticSearch
     * queries in order to limit external index access
     * @param url the query string to pass to the we:kb ElasticSearch API
     * @return the result map, reflecting the ElasticSearch response
     */
    Map executeQuery(String baseUrl, Map queryParams){
        Map result = [:]

        BasicHttpClient http
        try {
            //url = url.contains('?') ? url.replaceAll(" ", "+")+"&username=${ConfigMapper.getWekbApiUsername()}&password=${ConfigMapper.getWekbApiPassword()}" : url.replaceAll(" ", "+")+"?username=${ConfigMapper.getWekbApiUsername()}&password=${ConfigMapper.getWekbApiPassword()}"
            queryParams.username = ConfigMapper.getWekbApiUsername()
            queryParams.password = ConfigMapper.getWekbApiPassword()
            HttpClientConfiguration config = new DefaultHttpClientConfiguration()
            config.readTimeout = Duration.ofMinutes(5)
            http = new BasicHttpClient( baseUrl, config )

            Closure success = { resp, json ->
                log.debug ("server response: ${resp.getStatus().getReason()}, server: ${resp.getHeaders().get('Server')}, content length: ${resp.getHeaders().get('Content-Length')}")
                result = json
//                if (resp.getStatus().getCode() < 400) {
//                    result = ['warning': json]      // warning <-> info ?
//                } else {
//                    result = ['info': json]         // ???
//                }
            }
            Closure failure = { resp ->
                log.warn ('Response: ' + resp.getStatus().getCode() + ' - ' + resp.getStatus().getReason())
                result = ['error': resp.getStatus().getCode()]
            }

            http.post(['User-Agent' : 'laser'], BasicHttpClient.ResponseType.JSON, BasicHttpClient.PostType.URLENC, queryParams, success, failure)

        } catch (Exception e) {
            log.error e.getMessage()
        }
        finally {
            if (http) { http.close() }
        }
        result
    }
}
