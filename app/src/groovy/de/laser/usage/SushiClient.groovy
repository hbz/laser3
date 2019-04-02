package de.laser.usage

import groovy.json.JsonOutput
import groovy.util.logging.Log4j
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder

import static groovyx.net.http.ContentType.ANY

@Log4j
class SushiClient {

    def grailsApplication

    def clientOptions
    def result

    def getBasePath() {
        def uri = getUri()
        uri.getPath().endsWith('/') ? uri.getPath() : uri.getPath() + '/'
    }

    def getUri() {
        new URIBuilder(grails.util.Holders.grailsApplication.config.statsApiUrl)
    }

    def getBaseUrl() {
        def uri = getUri()
        uri.getScheme()+"://"+uri.getHost()
    }

    def getClient() {
        new RESTClient(getBaseUrl())
    }

    def getPath() {
        getBasePath() + 'Sushiservice/GetReport'
    }

    def query() {
        log.debug("Calling STATS API:  ${clientOptions.reportName}, Title with ID ${clientOptions.statsTitleIdentifier}")
        log.debug("Period Begin: ${clientOptions.from}, Period End: ${clientOptions.mostRecentClosedPeriod}")
        getClient().get(
            path: getPath(),
            contentType: ANY, // We get no XmlSlurper Objects for value XML
            query: [
                APIKey        : clientOptions.apiKey,
                RequestorID   : clientOptions.requestor,
                CustomerID    : clientOptions.customer,
                Report        : clientOptions.reportName,
                Release       : clientOptions.reportVersion,
                BeginDate     : clientOptions.from,
                EndDate       : clientOptions.mostRecentClosedPeriod,
                Platform      : clientOptions.platform,
                ItemIdentifier: "${clientOptions.reportType}:zdbid:" + clientOptions.statsTitleIdentifier
            ]) { response, xml ->
            if (xml) {
                result = xml
            }
        }
    }

    def getResult() {
        return result
    }

}
