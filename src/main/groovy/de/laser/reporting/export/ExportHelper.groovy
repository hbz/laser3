package de.laser.reporting.export

import de.laser.reporting.myInstitution.GenericHelper
import de.laser.reporting.myInstitution.base.BaseDetails
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery

class ExportHelper {

    // ----- Cache -----

    static String getCachedQuerySuffix(String token) {

        Map<String, Object> queryCache = BaseQuery.getQueryCache(token)
        String suffix = queryCache.query.replaceFirst( queryCache.query.split('-')[0] + '-', '' )
        suffix
    }

    static String getCachedTmplStrategy(String token) {

        Map<String, Object> detailsCache = BaseDetails.getDetailsCache(token)
        List parts = detailsCache.tmpl.split('/')
        parts[parts.size()-1]
    }

    // -----

    static Map<String, Object> getCachedFilterLabels(String token) {

        Map<String, Object> filterCache = BaseFilter.getFilterCache(token)
        filterCache.labels as Map<String, Object>
    }

    static List<String> getCachedQueryLabels(String token) {

        Map<String, Object> queryCache = BaseQuery.getQueryCache(token)
        queryCache.labels.labels as List<String>
    }

    static String getFieldLabel(String token, Map<String, Object> objConfig, String fieldName) {

        if (fieldName == 'property-assignment') {
            return 'Merkmal: ' + BaseQuery.getQueryCache(token).labels.labels[2] // TODO TODO TODO
        }
        else if (AbstractExport.CUSTOM_LABEL.containsKey(fieldName)) {
            return AbstractExport.CUSTOM_LABEL.get(fieldName)
        }

        GenericHelper.getFieldLabel( objConfig, fieldName )
    }

    static String getFileName(List<String> labels) {

        labels.collect{ it.replaceAll('→', '').replaceAll(' ', '') }.join('_')
    }
}
