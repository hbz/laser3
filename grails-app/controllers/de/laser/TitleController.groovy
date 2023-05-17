package de.laser

import de.laser.annotations.Check404
import de.laser.auth.User
import de.laser.storage.RDStore
import de.laser.titles.TitleHistoryEvent
import de.laser.utils.SwissKnife
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

/**
 * This controller manages calls for title listing
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleController  {

    ContextService contextService
    ESSearchService ESSearchService
    FilterService filterService

    //-----

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
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
        log.debug("list : ${params}")

        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        if(params.tab){
            if(params.tab == 'currentTipps'){
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }else if(params.tab == 'plannedTipps'){
                params.status = [RDStore.TIPP_STATUS_EXPECTED.id.toString()]
            }else if(params.tab == 'expiredTipps'){
                params.status = [RDStore.TIPP_STATUS_RETIRED.id.toString()]
            }else if(params.tab == 'deletedTipps'){
                params.status = [RDStore.TIPP_STATUS_DELETED.id.toString()]
            }else if(params.tab == 'allTipps'){
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString(), RDStore.TIPP_STATUS_EXPECTED.id.toString(), RDStore.TIPP_STATUS_RETIRED.id.toString(), RDStore.TIPP_STATUS_DELETED.id.toString()]
            }
        }
        else if(params.list('status').size() == 1) {
            if(params.list('status')[0] == RDStore.TIPP_STATUS_CURRENT.id.toString()){
                params.tab = 'currentTipps'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_RETIRED.id.toString()){
                params.tab = 'expiredTipps'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_EXPECTED.id.toString()){
                params.tab = 'plannedTipps'
            }else if(params.list('status')[0] == RDStore.TIPP_STATUS_DELETED.id.toString()){
                params.tab = 'deletedTipps'
            }
        }else{
            if(params.list('status').size() > 1){
                params.tab = 'allTipps'
            }else {
                params.tab = 'currentTipps'
                params.status = [RDStore.TIPP_STATUS_CURRENT.id.toString()]
            }
        }

        Map<String, Object> query = filterService.getTippQuery(params, [])
        result.filterSet = query.filterSet

        List<Long> titlesList = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)

        result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_CURRENT])[0]
        result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_EXPECTED])[0]
        result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_RETIRED])[0]
        result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(tipp) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_DELETED])[0]

        result.allTippsCounts = result.currentTippsCounts + result.plannedTippsCounts +  result.expiredTippsCounts + result.deletedTippsCounts

        result.titlesList = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList.drop(result.offset).take(result.max), [sort: 'sortname']) : []
        result.num_tipp_rows = titlesList.size()

        result
    }

    /**
     * Lists all recorded title in the app; the result may be filtered
     * @return a list of {@link TitleInstancePackagePlatform}s
     */
    @Secured(['ROLE_USER'])
    def listES() {
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
}
