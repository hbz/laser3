package de.laser.usage

import de.laser.helper.ConfigMapper
import groovy.util.logging.Slf4j
import groovyx.net.http.RESTClient
import groovyx.net.http.URIBuilder
import groovyx.net.http.ContentType

@Slf4j
class SushiClient {

    def clientOptions
    def result

    URIBuilder getUri() {
        new URIBuilder(ConfigMapper.getStatsApiUrl())
    }

    String getBasePath() {
        URIBuilder uri = getUri()
        uri.getPath().endsWith('/') ? uri.getPath() : uri.getPath() + '/'
    }

    String getBaseUrl() {
        URIBuilder uri = getUri()
        uri.getScheme() + "://" + uri.getHost()
    }

    RESTClient getClient() {
        new RESTClient(getBaseUrl())
    }

    String getPath() {
        getBasePath() + 'Sushiservice/GetReport'
    }

    def query() {
        log.debug("Calling STATS API:  ${clientOptions.reportName}, Title with ID ${clientOptions.statsTitleIdentifier}")
        log.debug("Period Begin: ${clientOptions.from}, Period End: ${clientOptions.mostRecentClosedPeriod}")
        def iType = clientOptions.statsIdentifierType ?: 'zdbid'
        getClient().get(
            path: getPath(),
            contentType: ContentType.ANY, // We get no XmlSlurper Objects for value XML
            query: [
                APIKey        : clientOptions.apiKey,
                RequestorID   : clientOptions.requestor,
                CustomerID    : clientOptions.customer,
                Report        : clientOptions.reportName,
                Release       : clientOptions.reportVersion,
                BeginDate     : clientOptions.from,
                EndDate       : clientOptions.mostRecentClosedPeriod,
                Platform      : clientOptions.platform,
                ItemIdentifier: "${clientOptions.reportType}:${iType}:" + clientOptions.statsTitleIdentifier
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
