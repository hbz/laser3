package de.laser.reporting.export

import de.laser.reporting.myInstitution.GenericHelper
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery

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

    static Map<String, Object> getFilterLabels(String token) {

        Map<String, Object> filterCache = BaseFilter.getFilterCache(token)
        filterCache.labels as Map<String, Object>
    }

    static List<String> getQueryLabels(String token) {

        Map<String, Object> queryCache = BaseQuery.getQueryCache(token)
        queryCache.labels.labels as List<String>
    }
}
