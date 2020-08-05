package de.laser.oai

import java.text.SimpleDateFormat
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method

@Deprecated
public class OaiClient {

  String host

  public OaiClient() {
  }

  public getChangesSince(datestamp, syncJob, processing_closure) {
    def metadataPrefix = syncJob.fullPrefix
    println("Get latest changes");
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    def http = new HTTPBuilder( host )

    boolean more = true
    println("Attempt get...");
    def resumption=null

    // perform a GET request, expecting JSON response data
    while ( more ) {
      println("com.k_int.goai.OaiClient::getChangesSince - Make http request..");

      http.request( Method.GET, ContentType.XML ) {
        // Use the default path specified in the host string
        // uri.path =
        if ( resumption ) {
          uri.query = [ verb:'ListRecords', resumptionToken: resumption ]
        }
        else {
          def from_param = datestamp ? sdf.format(datestamp) : ''
          uri.query = [ verb:'ListRecords', metadataPrefix: metadataPrefix, from:from_param ]
        }

        // response handler for a success response code:
        response.success = { resp, xml ->
          println resp.statusLine

          xml.'ListRecords'.'record'.each { r ->
            // println("Record id...${r.'header'.'identifier'}");
            // println("Package Name: ${r.metadata.package.packageName}");
            // println("Package Id: ${r.metadata.package.packageId}");
            processing_closure.call(r,syncJob);
          }

          if ( xml.'ListRecords'.'resumptionToken'.size() == 1 && xml.'ListRecords'.'resumptionToken'.text()?.size() > 0) {
            resumption=xml.'ListRecords'.'resumptionToken'.text()
            println("Iterate with resumption : ${resumption}");
          }
          else {
            more = false
          }
        }

        // handler for any failure status code:
        response.failure = { resp ->
          println "Unexpected error: ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}. Set more=false"
          more=false
        }
      }
      println("Endloop");
    }

    println("All done");
  }

}
