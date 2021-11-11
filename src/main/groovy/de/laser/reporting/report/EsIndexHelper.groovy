package de.laser.reporting.report

import de.laser.ElasticsearchSource
import groovyx.net.http.HTTPBuilder

class EsIndexHelper {

    static HTTPBuilder getHttpBuilder(String uriPart) {
        ElasticsearchSource esSource = ElasticsearchSource.findByGokb_esAndActive(true, true)
        HTTPBuilder builder = new HTTPBuilder( 'http://' + esSource.host + ':' + esSource.port + uriPart )

        println 'EsIndexHelper.getHttpBuilder() - ' + builder.getUri()
        return builder
    }
}
