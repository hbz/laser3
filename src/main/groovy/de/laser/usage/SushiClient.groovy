package de.laser.usage

import de.laser.config.ConfigMapper
import de.laser.http.BasicHttpClient
import groovy.util.logging.Slf4j
import groovy.xml.slurpersupport.GPathResult

@Slf4j
class SushiClient {

    def clientOptions
    def result

    String getPath() {
        ConfigMapper.getStatsApiUrl() + 'Sushiservice/GetReport'
    }

    void query() {
        log.debug("Calling STATS API:  ${clientOptions.reportName}, Title with ID ${clientOptions.statsTitleIdentifier}")
        log.debug("Period Begin: ${clientOptions.from}, Period End: ${clientOptions.mostRecentClosedPeriod}")
        String iType = clientOptions.statsIdentifierType ?: 'zdbid'
        String queryUrl = getPath()+"?APIKey=${clientOptions.apiKey}&RequestorID=${clientOptions.requestor}&CustomerID${clientOptions.customer}&Report=${clientOptions.reportName}&Release=${clientOptions.reportVersion}&BeginDate=${clientOptions.from}&EndDate=${clientOptions.mostRecentClosedPeriod}&Platform=${clientOptions.platform}&ItemIdentifier=${clientOptions.reportType}:${iType}:${clientOptions.statsTitleIdentifier}"
        //was initially a REST client; needs verification! Migration done in order to remove outdated dependency!
        BasicHttpClient http = new BasicHttpClient(queryUrl)
        Closure success = { response, GPathResult xml ->
            if(response.code() == 200) {
                result = xml
            }
        }
        Closure failure = { response, reader ->
            log.error("An error occurred!")
        }
        http.get(BasicHttpClient.ResponseType.XML, success, failure)
        http.close()
    }

    def getResult() {
        return result
    }

}
