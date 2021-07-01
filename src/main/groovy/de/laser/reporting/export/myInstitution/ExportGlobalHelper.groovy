package de.laser.reporting.export.myInstitution

import de.laser.IdentifierNamespace
import de.laser.RefdataValue
import de.laser.reporting.export.AbstractExport
import de.laser.reporting.export.AbstractExportHelper
import de.laser.reporting.myInstitution.GenericHelper
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseDetails
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery



class ExportGlobalHelper extends AbstractExportHelper {

    static AbstractExport createExport(String token, Map<String, Object> selectedFields) {

        String tmpl = getCachedExportStrategy( token )

        if (tmpl == LicenseExport.KEY) {
            return new LicenseExport( token, selectedFields )
        }
        else if (tmpl == OrgExport.KEY) {
            return new OrgExport( token, selectedFields )
        }
        else if (tmpl == SubscriptionExport.KEY) {
            return new SubscriptionExport( token, selectedFields )
        }
    }

    // ----- Cache -----

    static Map<String, Object> getDetailsCache(String token) {

        BaseDetails.getDetailsCache(token)
    }

    static String getCachedExportStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        List parts = detailsCache.tmpl.split('/')
        parts[parts.size()-1]
    }

    static String getCachedConfigStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        List<String> queryParts = detailsCache.query.split('-')
        queryParts.size() == 3 ? queryParts[2] : queryParts[0]
    }

    static String getCachedFieldStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        detailsCache.query.substring( detailsCache.query.indexOf('-') + 1 )
    }

    // -----

    static Map<String, Object> getCachedFilterLabels(String token) {

        Map<String, Object> filterCache = BaseFilter.getFilterCache(token)
        filterCache.labels as Map<String, Object>
    }

    static String getCachedFilterResult(String token) {

        Map<String, Object> filterCache = BaseFilter.getFilterCache(token)
        filterCache.result
    }

    static List<String> getCachedQueryLabels(String token) {

        Map<String, Object> queryCache = BaseQuery.getQueryCache(token)
        queryCache.labels.labels as List<String>
    }

    // -----

    static List<String> getIncompleteQueryLabels(String token) {

        Map<String, Object> queryCache = BaseQuery.getQueryCache( token )
        String prefix = queryCache.query.split('-')[0]
        Map<String, Object> cfg = BaseConfig.getCurrentConfigByPrefix( prefix )
        BaseQuery.getQueryLabels(cfg, queryCache.query as String)
    }

    // -----

    static String getFieldLabel(AbstractExport export, String fieldName) {

        if ( isFieldMultiple(fieldName) ) {
            String label = AbstractExport.CUSTOM_LABEL.get(fieldName)

            if (fieldName == 'x-identifier') {
                List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
                label += (selList ? ': ' + selList.collect{it ->
                    IdentifierNamespace idns = IdentifierNamespace.get(it)
                    idns.getI10n('name') ?: idns.ns + ' *'
                }.join(', ') : '')
            }
            else if (fieldName == '@ae-org-accessPoint') {
                List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
                label += (selList ? ': ' + selList.collect{it -> RefdataValue.get(it).getI10n('value') }.join(', ') : '') // TODO - export
            }
            else if (fieldName == '@ae-org-readerNumber') {
                List selList = export.getSelectedFields().get(fieldName)
                List semList = selList.findAll{ it.startsWith('sem-') }.collect{ RefdataValue.get( it.replace('sem-', '') ).getI10n('value') }
                List ddList  = selList.findAll{ it.startsWith('dd-') }.collect{ it.replace('dd-', 'Stichtage ') }
                label += (selList ? ': ' + (semList + ddList).join(', ') : '') // TODO - export
            }

            return label
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
}
