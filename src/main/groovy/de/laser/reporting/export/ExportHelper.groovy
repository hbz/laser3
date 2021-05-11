package de.laser.reporting.export

import de.laser.IdentifierNamespace
import de.laser.License
import de.laser.Org
import de.laser.Subscription
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

    static String getFieldLabel(AbstractExport export, String fieldName) {

        if (fieldName == 'x-identifier') {
            List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
            return 'Identifikatoren' + (selList ? ': ' + selList.collect{it -> IdentifierNamespace.get(it).ns }.join(', ') : '') // TODO - export
        }
        else if (fieldName == 'x-property') {
            return 'Merkmal: ' + BaseQuery.getQueryCache( export.token ).labels.labels[2] // TODO - modal
        }
        else if (AbstractExport.CUSTOM_LABEL.containsKey(fieldName)) {
            return AbstractExport.CUSTOM_LABEL.get(fieldName)
        }

        // --- adapter ---

        String cfg = getCachedConfigStrategy( export.token )
        Map<String, Object> objConfig = export.getCurrentConfig( export.KEY ).base

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

    static List getIdentifiersForDropdown(Map<String, Object> cfg) {

        List<IdentifierNamespace> idnsList = []

        if (cfg.base.meta.class == Org) {
            idnsList = Org.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type',  [type: Org.class.name] )
        }
        else if (cfg.base.meta.class == License) {
            idnsList = License.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: License.class.name] )
        }
        else if (cfg.base.meta.class == Subscription) {
            idnsList = Subscription.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Subscription.class.name] )
        }

        idnsList.collect{ it ->
            [ it.id, it.ns ]
            //[ it.class.name + ':' + it.id, it.ns ]
        }
    }
}
