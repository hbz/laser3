package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class AnnouncementController extends AbstractDebugController {

    def springSecurityService
    def genericOIDService
    def contextService

    @Secured(['ROLE_ADMIN'])
    def index() {
        Map<String, Object> result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def announcement_type = RefdataValue.getByValueAndCategory('Announcement','Document Type')
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: 10, sort: 'dateCreated', order: 'desc'])

        result
    }

    @Secured(['ROLE_ADMIN'])
    def createAnnouncement() {
        Map<String, Object> result = [:]
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
