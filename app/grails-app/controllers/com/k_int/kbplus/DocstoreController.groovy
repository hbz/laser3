package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.annotation.Secured

import org.springframework.context.i18n.LocaleContextHolder

@Secured(['IS_AUTHENTICATED_FULLY'])
class DocstoreController extends AbstractDebugController {

    def docstoreService
    def messageSource
    def contextService

    @Secured(['ROLE_USER'])
    def index() {
        def doc = Doc.findByUuid(params.id)
        if (doc) {

            def filename = doc.filename ?: messageSource.getMessage(
                    'template.documents.missing',
                    null,
                    LocaleContextHolder.getLocale()
            )

            switch (doc.contentType) {
                case Doc.CONTENT_TYPE_STRING:
                    break
                case Doc.CONTENT_TYPE_DOCSTORE:
                    docstoreService.retrieve(params.id, response, doc.mimeType, filename)
                    break
                case Doc.CONTENT_TYPE_BLOB:
                    // erms-790
                    doc.render(response, filename)
                    break
            }
        }
    }
}
