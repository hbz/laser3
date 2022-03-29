package de.laser


import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class AnnouncementController  {

    ContextService contextService

    /**
     * Lists all current announcements
     */
    @Secured(['ROLE_ADMIN'])
    def index() {
        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.recentAnnouncements = Doc.findAllByType(RDStore.DOC_TYPE_ANNOUNCEMENT, [max: 10, sort: 'dateCreated', order: 'desc'])

        result
    }

    /**
     * Creates a new announcement with to the given parameters and returns to the list of announcements
     */
    @Secured(['ROLE_ADMIN'])
    @Transactional
    def createAnnouncement() {
        Map<String, Object> result = [:]
        if (params.annTxt) {
            result.user = contextService.getUser()
            flash.message = message(code: 'announcement.created')

            new Doc(title: params.subjectTxt,
                    content: params.annTxt,
                    user: result.user,
                    type: RDStore.DOC_TYPE_ANNOUNCEMENT,
                    contentType: Doc.CONTENT_TYPE_STRING).save()
        }
        redirect(action: 'index')
    }

}
