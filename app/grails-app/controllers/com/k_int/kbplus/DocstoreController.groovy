package com.k_int.kbplus

import grails.converters.*
import grails.plugins.springsecurity.Secured

import org.apache.poi.hslf.model.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.ss.usermodel.*
import org.elasticsearch.groovy.common.xcontent.*

import com.k_int.kbplus.auth.*

class DocstoreController {

    def docstoreService

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        def doc = Doc.findByUuid(params.id)
        if (doc) {
            switch (doc.contentType) {
                case Doc.CONTENT_TYPE_STRING:
                    break
                case Doc.CONTENT_TYPE_DOCSTORE:
                    docstoreService.retrieve(params.id, response, doc.mimeType, doc.filename)
                    break
                case Doc.CONTENT_TYPE_BLOB:
                    doc.render(response)
                    break
            }
        }
    }
}
