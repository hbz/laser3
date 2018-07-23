package de.laser

import groovy.json.JsonBuilder
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.util.EntityUtils
import org.springframework.http.HttpStatus
import sun.misc.BASE64Encoder

import java.text.SimpleDateFormat

class ErrorReportService {

    // jira.rest.url = 'https://jira'
    // jira.rest.user = ''
    // jira.rest.token = ''
    // jira.rest.errorReport.target = 'issue/ERMS-xy'
    // jira.rest.errorReport.type = 'attachments'

    def grailsApplication
    def springSecurityService

    def getConfig() {
        def config = [:]

        def rest = grailsApplication.config.jira.rest

        if (! rest) {
            log.error("no jira.rest config found >>> abort sending error report ")
            return null
        }

        if (rest.errorReport.type == 'attachments') {
            config << [url: "${rest.url}/${rest.errorReport.target}/${rest.errorReport.type}"]
            //config << [method: 'POST']

            BASE64Encoder b64 = new BASE64Encoder()

            config << [headers: [
                    'Authorization': 'Basic ' + b64.encode( "${rest.user}:${rest.token}".getBytes() ),
                    //'Content-Type': 'multipart/form-data', // causes FileUploadException: the request was rejected because no multipart boundary was found
                    'X-Atlassian-Token': 'nocheck'
            ]]

            //config << [body: [
            //            'file': null
            //        ]]
        }
    }

    def sendReportAsAttachement(data) {
        def config = getConfig()

        if (! config || ! data) {
            log.info("ignored sending error report - no config and/or no data")
            return
        }
        HttpPost post = new HttpPost(config.url)

        config.headers.each{ k, v ->
            post.setHeader(k, v)
        }

        def jb = new JsonBuilder(data)
        def sdf = new SimpleDateFormat('yMMdd:HHmmss')
        def dd  = sdf.format(new Date())

        def filename = (grailsApplication.config.laserSystemId ?: 'Quelle unbekannt') + " - ${springSecurityService.getCurrentUser().email} - ${dd}"

        MultipartEntityBuilder meb = MultipartEntityBuilder.create()
        meb.addPart('file', new ByteArrayBody( jb.toPrettyString().getBytes(), filename.replace('/', '') ))
        post.setEntity(meb.build())

        HttpClient client = HttpClientBuilder.create().build()
        HttpResponse response = client.execute(post)

        if (response.getStatusLine()?.getStatusCode()?.equals(HttpStatus.NO_CONTENT.value())) {

            log.info("successfully sent error report for " + jb.content.meta)
            return true
        }
        else {
            log.info(EntityUtils.toString(response.getEntity()))
            return false
        }
    }
}
