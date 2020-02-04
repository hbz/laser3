package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class SearchController extends AbstractDebugController {
    def ESSearchService
    def springSecurityService
    def contextService


    @Secured(['ROLE_USER'])
    def index() {
        log.debug("searchController: index");
        Map<String, Object> result = [:]

        result.user = springSecurityService.getCurrentUser()
        params.max = params.max ?: result.user.getDefaultPageSizeTMP()
        params.offset = params.offset ? params.int('offset') : 0

        params.searchObjects = params.searchObjects ?: 'allObjects'
        result.contextOrg = contextService.getOrg()

        def query = params.q ?: null
        if (!query) {
            return result
        }

        if (springSecurityService.isLoggedIn()) {
            params.q = query

            if(params.advancedSearchText){
                params.q += " ${params.advancedSearchOption} ${params.advancedSearchText} "
            }
            if(params.advancedSearchText2){
                params.q += " ${params.advancedSearchOption2} ${params.advancedSearchText2} "
            }

            if(params.advancedSearchText3){
                params.q += " ${params.advancedSearchOption3} ${params.advancedSearchText3} "
            }

            if(params.advancedSearchText || params.advancedSearchText2 || params.advancedSearchText3)
            {
                params.q = "( ${params.q} )"
            }

            if(params.showMembersObjects && result.contextOrg.getCustomerType() in ['ORG_CONSORTIUM', 'ORG_CONSORTIUM_SURVEY']){
                params.consortiaGUID = result.contextOrg.globalUID
            }

            params.actionName = actionName

            params.availableToOrgs = [contextService.org.id]
            params.availableToUser = [result.user.id]

            result = ESSearchService.search(params)

            params.q = query

        }
        result.contextOrg = contextService.getOrg()
        result
    }

    @Secured(['ROLE_USER'])
    def spotlightSearch() {
        log.debug("searchController: spotlightSearch");
        Map<String, Object> result = [:]
        def filtered
        def query = "${params.query}"
        result.user = springSecurityService.getCurrentUser()
        //params.max = result.user.getDefaultPageSizeTMP() ?: 15
        params.max = 50

        if (!query) {
            return result
        }

        if (springSecurityService.isLoggedIn()) {
            if (query.startsWith("\$") && query.length() > 2 && query.indexOf(" ") != -1) {
                def filter = query.substring(0, query.indexOf(" "))
                switch (filter) {
                    case "\$t":
                        params.type = "title"
                        query = query.replace("\$t  ", "")
                        filtered = "Title Instance"
                        break
                    case "\$pa":
                        params.type = "package"
                        query = query.replace("\$pa ", "")
                        filtered = "Package"
                        break
                    case "\$p":
                        params.type = "package"
                        query = query.replace("\$p ", "")
                        filtered = "Package"
                        break
                    case "\$pl":
                        params.type = "platform"
                        query = query.replace("\$pl ", "")
                        filtered = "Platform"
                        break;
                    case "\$s":
                        params.type = "subscription"
                        query = query.replace("\$s ", "")
                        filtered = "Subscription"
                        break
                    case "\$o":
                        params.type = "organisation"
                        query = query.replace("\$o ", "")
                        filtered = "Organisation"
                        break
                    case "\$l":
                        params.type = "license"
                        query = query.replace("\$l ", "")
                        filtered = "License"
                        break
                }

            }
            params.q = query
            //From the available orgs, see if any belongs to a consortium, and add consortium ID too
            //TMP Bugfix, restrict for now to context org! A proper solution has to be found later!
            params.availableToOrgs = [contextService.org.id]

            if (query.startsWith("\$")) {
                if (query.length() > 2) {
                    result = ESSearchService.search(params)
                }
            } else {
                result = ESSearchService.search(params)
            }
            result.filtered = filtered
            // result?.facets?.type?.pop()?.term
        }
        result
    }
}
