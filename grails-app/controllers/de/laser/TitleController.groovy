package de.laser

import de.laser.annotations.Check404
import de.laser.titles.TitleHistoryEvent

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls for title listing
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleController  {

    ContextService contextService
    ESSearchService ESSearchService

    //-----

    final static Map<String, String> CHECK404_ALTERNATIVES = [
            'title/list': 'menu.public.all_titles',
            'myInstitution/currentTitles': 'myinst.currentTitles.label'
    ]

    //-----

    /**
     * Call to the list of all title instances recorded in the system
     * @return the result of {@link #list()}
     */
    @Secured(['ROLE_USER'])
    def index() {
        redirect controller: 'title', action: 'list', params: params
    }

    /**
     * Lists all recorded title in the app; the result may be filtered
     * @return a list of {@link TitleInstancePackagePlatform}s
     */
    @Secured(['ROLE_USER'])
    def list() {
        log.debug("titleSearch : ${params}")

        Map<String, Object> result = [:]

            params.rectype = "TitleInstancePackagePlatform" // Tells ESSearchService what to look for
            //params.showAllTitles = true
            result.user = contextService.getUser()
            params.max = params.max ?: result.user.getPageSizeOrDefault()

            if (params.search.equals("yes")) {
                params.offset = params.offset ? params.int('offset') : 0
                params.remove("search")
            }

            def old_q = params.q
            def old_sort = params.sort

            params.sort = params.sort ?: "name.keyword"

            if (params.filter != null && params.filter != '') {
                params.put(params.filter, params.q)
            }else{
                params.q = params.q ?: null
            }

            result =  ESSearchService.search(params)
            //Double-Quoted search strings wont display without this
            params.q = old_q

            if(! old_q ) {
                params.remove('q')
            }
            if(! old_sort ) {
                params.remove('sort')
            }

        if (old_q) {
            result.filterSet = true
        }

        result.flagContentElasticsearch = true // ESSearchService.search
        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')
        result
    }

    @Deprecated
    @Secured(['ROLE_USER'])
    @Check404(domain = TitleInstancePackagePlatform)
    Map<String,Object> show() {
        Map<String, Object> result = [:]

        result.editable = SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        result.titleHistory = TitleHistoryEvent.executeQuery("select distinct thep.event from TitleHistoryEventParticipant as thep where thep.participant = :participant", [participant: result.tipp] )

        result
    }
}
