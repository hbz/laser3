package de.laser.oai

import groovyx.net.http.HTTPBuilder


class OaiClientLaser {

    private String host;

    def getRecord(url, object, id)
    {
        try {
            def http = new HTTPBuilder( url )
            http.get( path: object, query:[verb: 'getRecord', metadataPrefix: 'gokb', identifier: id], contentType: "xml") { resp, xml ->
                        def response = new XmlSlurper().parseText(xml.text)
                       return response.GetRecord.record
                    }
        }
        catch(groovyx.net.http.HttpResponseException e)
        {
            return null
        }
    }
}
