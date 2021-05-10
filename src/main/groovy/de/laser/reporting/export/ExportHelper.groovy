package de.laser.reporting.export

import de.laser.reporting.myInstitution.GenericHelper
import de.laser.reporting.myInstitution.base.BaseDetails
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery

class ExportHelper {

    // ----- Cache -----

    static String getCachedExportStrategy(String token) {

        Map<String, Object> detailsCache = BaseDetails.getDetailsCache(token)
        List parts = detailsCache.tmpl.split('/')
        parts[parts.size()-1]
    }

    static String getCachedConfigStrategy(String token) {

        Map<String, Object> detailsCache = BaseDetails.getDetailsCache(token)
        List<String> queryParts = detailsCache.query.split('-')
        queryParts.size() == 3 ? queryParts[2] : queryParts[0]
    }

    static String getCachedFieldStrategy(String token) {

        Map<String, Object> detailsCache = BaseDetails.getDetailsCache(token)
        detailsCache.query.substring( detailsCache.query.indexOf('-') + 1 )
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

        if (fieldName == 'x-property') {
            return 'Merkmal: ' + BaseQuery.getQueryCache(token).labels.labels[2] // TODO TODO TODO
        }
        else if (AbstractExport.CUSTOM_LABEL.containsKey(fieldName)) {
            return AbstractExport.CUSTOM_LABEL.get(fieldName)
        }

        // --- adapter ---

        String cfg = getCachedConfigStrategy(token)
        if (! objConfig.fields.keySet().contains(cfg)) {
            cfg = 'default'
        }
        Map<String, Object> objConfig2 = [
                meta   : objConfig.meta,
                fields : objConfig.fields.get(cfg)
        ]

        GenericHelper.getFieldLabel( objConfig2, fieldName )
    }

    static String getFileName(List<String> labels) {

        labels.collect{ it.replaceAll('→', '_').replaceAll(' ', '') }.join('_')
    }
}
