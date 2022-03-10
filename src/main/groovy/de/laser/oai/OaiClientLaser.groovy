package de.laser.oai

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseException

@Deprecated
class OaiClientLaser {

    def getRecord(url, object, id)
    {
        try {
            HTTPBuilder http = new HTTPBuilder( url )
            http.get( path: object, query:[verb: 'getRecord', metadataPrefix: 'gokb', identifier: id], contentType: "xml") { resp, xml ->
                def response = new XmlSlurper().parseText(xml.text)
                return response.GetRecord.record
            }
        }
        catch(HttpResponseException e) {
            e.printStackTrace()
            return null
        }
    }
}
