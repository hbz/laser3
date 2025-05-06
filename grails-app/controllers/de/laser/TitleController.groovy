package de.laser

import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.helper.FilterLogic
import de.laser.helper.Profiler
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
import de.laser.wekb.TitleInstancePackagePlatform
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

import java.security.MessageDigest

/**
 * This controller manages calls for title listing
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class TitleController  {

    CacheService cacheService
    ContextService contextService
    TitleService titleService
    FilterService filterService

    //-----

    /**
     * Map containing menu alternatives if an unexisting object has been called
     */
    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'title/index': 'menu.public.all_titles'
            //'myInstitution/currentTitles': 'myinst.currentTitles.label'
    ]

    //-----

    /**
     * Call to the list of all title instances recorded in the system
     * @return a list of {@link de.laser.wekb.TitleInstancePackagePlatform}s
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def index() {
        log.debug("index : ${params}")
        Profiler prf = new Profiler()
        prf.setBenchmark('init')
        Map<String, Object> result = [:], configMap = [:]
        Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'Tipps')
        if (ttParams.status) { params.status = ttParams.status }
        if (ttParams.tab)    { params.tab = ttParams.tab }
        SwissKnife.setPaginationParams(result, params, contextService.getUser())
        if(params.containsKey('filterSet')) {
            params.each { key, value ->
                if(value)
                    configMap.put(key, value)
            }
            log.debug("index : ${configMap}")
            prf.setBenchmark('getting keys')
            Set<Long> keys = titleService.getKeys(configMap)
            //prf.setBenchmark('getting counts')
            //result.putAll(titleService.getCounts(keys))
            prf.setBenchmark('get title list')
            result.titlesList = keys ? TitleInstancePackagePlatform.findAllByIdInList(keys.drop(result.offset).take(result.max), [sort: params.sort?: ['sortname', 'dateFirstOnline', 'dateFirstInPrint'], order: params.order]) : []
            result.num_tipp_rows = keys.size()
            result.editable = contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
        }
        result.benchMark = prf.stopBenchmark()
        result
    }

    /**
     * Lists all recorded title in the app; the result may be filtered
     * @return a list of {@link de.laser.wekb.TitleInstancePackagePlatform}s
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    @Deprecated
    def list() {
        log.debug("list : ${params}")
        Profiler prf = new Profiler()
        prf.setBenchmark('init')
        Map<String, Object> result = [:]

        result.user = contextService.getUser()
        SwissKnife.setPaginationParams(result, params, (User) result.user)

        Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'Tipps')
        if (ttParams.status) { params.status = ttParams.status }
        if (ttParams.tab)    { params.tab = ttParams.tab }
        prf.setBenchmark('before tipp query')
        Map<String, Object> query = filterService.getTippQuery(params, [])
        result.filterSet = query.filterSet
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256")
        Map<String, Object> cachingKeys = params.clone()
        cachingKeys.remove("offset")
        cachingKeys.remove("max")
        String checksum = "${result.user.id}_${cachingKeys.entrySet().join('_')}"
        messageDigest.update(checksum.getBytes())
        EhcacheWrapper subCache = cacheService.getTTL300Cache("/title/list/subCache/${messageDigest.digest().encodeHex()}")

        List<Long> titlesList = subCache.get('titleIDs') ?: []
        if(!titlesList) {
            prf.setBenchmark('load title IDs, no cache or cache changed')
            titlesList = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
            subCache.put('titleIDs', titlesList)
        }

        if(!params.containsKey('fileformat')) {
            //List counts = TitleInstancePackagePlatform.executeQuery('select new map(count(*) as count, tipp.status as status) '+countQueryString+' group by tipp.status', countQueryParams)
            List counts = subCache.get('counts') ?: []
            if(!counts) {
                prf.setBenchmark('get counts')
                counts = TitleInstancePackagePlatform.executeQuery('select new map(count(*) as count, tipp.status as status) from TitleInstancePackagePlatform tipp where tipp.status != :removed group by tipp.status', [removed: RDStore.TIPP_STATUS_REMOVED])
                subCache.put('counts', counts)
            }
            result.allTippsCounts = 0
            counts.each { row ->
                switch (row['status']) {
                    case RDStore.TIPP_STATUS_CURRENT: result.currentTippsCounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_EXPECTED: result.plannedTippsCounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_RETIRED: result.expiredTippsCounts = row['count']
                        break
                    case RDStore.TIPP_STATUS_DELETED: result.deletedTippsCounts = row['count']
                        break
                }
                result.allTippsCounts += row['count']
            }
            switch(params.tab) {
                case 'currentIEs': result.num_tipp_rows = result.currentIECounts
                    break
                case 'plannedIEs': result.num_tipp_rows = result.plannedIECounts
                    break
                case 'expiredIEs': result.num_tipp_rows = result.expredIECounts
                    break
                case 'deletedIEs': result.num_tipp_rows = result.deletedIECounts
                    break
                case 'allIEs': result.num_tipp_rows = result.allIECounts
                    break
            }
            /*
            result.currentTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_CURRENT])[0]
            result.plannedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_EXPECTED])[0]
            result.expiredTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_RETIRED])[0]
            result.deletedTippsCounts = TitleInstancePackagePlatform.executeQuery("select count(*) from TitleInstancePackagePlatform as tipp where tipp.status = :status", [status: RDStore.TIPP_STATUS_DELETED])[0]

            result.allTippsCounts = result.currentTippsCounts + result.plannedTippsCounts +  result.expiredTippsCounts + result.deletedTippsCounts
            */
        }
        prf.setBenchmark('before title objects')
        result.titlesList = titlesList ? TitleInstancePackagePlatform.findAllByIdInList(titlesList.drop(result.offset).take(result.max), [sort: params.sort?: 'sortname', order: params.order]) : []
        result.num_tipp_rows = titlesList.size()
        List bm = prf.stopBenchmark()
        result.benchMark = bm
        result
    }

    /**
     * Lists all recorded title in the app; the result may be filtered
     * @return a list of {@link TitleInstancePackagePlatform}s
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    @Deprecated
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
