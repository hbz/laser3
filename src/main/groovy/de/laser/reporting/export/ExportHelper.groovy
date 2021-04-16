package de.laser.reporting.export

import de.laser.ContextService
import de.laser.helper.SessionCacheWrapper
import de.laser.reporting.myInstitution.GenericHelper
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class ExportHelper {

    static String getFieldLabel(Map<String, Object> objConfig, String fieldName) {
        if (fieldName == 'globalUID') {
            return 'Link (Global UID)'
        }
        if (fieldName == 'identifier-assignment') {
            return 'Identifikatoren'
        }
        else if (fieldName == 'property-assignment') {
            return 'Merkmalswert * TODO'
        }

        GenericHelper.getFieldLabel( objConfig, fieldName )
    }

    static String getFileName(List<String> labels) {
        labels.collect{ it.replaceAll('→', '').replaceAll(' ', '') }.join('_')
    }

    static Map<String, Object> getFilterLabels(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String, Object> cacheMap = sessionCache.get("MyInstitutionController/reporting/" + params.token)

        cacheMap.filterCache.labels
    }

    static List<String> getQueryLabels(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        SessionCacheWrapper sessionCache = contextService.getSessionCache()
        Map<String, Object> cacheMap = sessionCache.get("MyInstitutionController/reporting/" + params.token)

        cacheMap.queryCache.labels.labels
    }
}
