package com.k_int.kbplus

import grails.converters.*
import grails.plugin.springsecurity.annotation.Secured // 2.0
import grails.converters.*
import org.elasticsearch.groovy.common.xcontent.*
import groovy.xml.MarkupBuilder
import com.k_int.kbplus.auth.*;
import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class AnnouncementController {

    def springSecurityService
    def alertsService
    def genericOIDService

    @Secured(['ROLE_DATAMANAGER'])
    def index() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def announcement_type = RefdataCategory.lookupOrCreate('Document Type', 'Announcement')
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: 10, sort: 'dateCreated', order: 'desc'])

        result
    }

    @Secured(['ROLE_DATAMANAGER'])
    def createAnnouncement() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        flash.message = "Announcement Created"
        def announcement_type = RefdataCategory.lookupOrCreate('Document Type', 'Announcement')

        def new_announcement = new Doc(title: params.subjectTxt,
                content: params.annTxt,
                user: result.user,
                type: announcement_type,
                contentType: Doc.CONTENT_TYPE_STRING).save(flush: true)

        redirect(action: 'index')
    }

}
