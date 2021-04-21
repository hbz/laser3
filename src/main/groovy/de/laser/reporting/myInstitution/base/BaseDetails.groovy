package de.laser.reporting.myInstitution.base

import de.laser.ContextService
import de.laser.helper.SessionCacheWrapper
import grails.util.Holders

class BaseDetails {

    static Map<String, Object> getDetailsCache(String token) {
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String, Object> cacheMap = sessionCache.get("MyInstitutionController/reporting/" + token)

        cacheMap.detailsCache as Map<String, Object>
    }
}
