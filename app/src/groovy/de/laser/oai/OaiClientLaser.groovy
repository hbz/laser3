package de.laser.oai

import grails.converters.JSON
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method;

class OaiClientLaser {

    private String host;

    def getRecordTitle(url, object, id)
    {
        try {
            def http = new HTTPBuilder( url )

            http.get( path: object, query:[verb: 'getRecord', metadataPrefix: 'gokb', identifier: id], contentType: "xml") { resp, xml ->
                def response = new XmlSlurper().parseText(xml.text)
                        println resp.status
                        println " in London."
                        println "${response.GetRecord.record} degrees Kelvin"
                    return response.GetRecord.record
                    }
        }
        catch(groovyx.net.http.HttpResponseException e)
        {
            return null
        }
    }
}
