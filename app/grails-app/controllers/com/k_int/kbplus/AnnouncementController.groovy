package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import com.k_int.kbplus.auth.*;
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class AnnouncementController extends AbstractDebugController {

    def springSecurityService
    def genericOIDService

    @Secured(['ROLE_DATAMANAGER'])
    def index() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def announcement_type = RefdataValue.getByValueAndCategory('Announcement','Document Type')
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: 10, sort: 'dateCreated', order: 'desc'])

        result
    }

    @Secured(['ROLE_DATAMANAGER'])
    def createAnnouncement() {
        def result = [:]
        if (params.annTxt) {
            result.user = User.get(springSecurityService.principal.id)
            flash.message = message(code: 'announcement.created', default: "Announcement Created")
            def announcement_type = RefdataValue.getByValueAndCategory('Announcement','Document Type')

            def new_announcement = new Doc(title: params.subjectTxt,
                    content: params.annTxt,
                    user: result.user,
                    type: announcement_type,
                    contentType: Doc.CONTENT_TYPE_STRING).save(flush: true)
        }
        redirect(action: 'index')
    }

}
