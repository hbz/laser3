package de.laser.reporting.export

import de.laser.*
import de.laser.reporting.export.local.CostItemExport
import de.laser.reporting.export.local.IssueEntitlementExport
import de.laser.reporting.export.local.OrgExport
import de.laser.reporting.export.local.SubscriptionExport
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.report.GenericHelper

/**
 * Class containing helper methods valid for local report exports, i.e. such which are departing from one certain object
 * and not regarding the institution as a whole
 */
class LocalExportHelper extends BaseExportHelper {

    /**
     * Creates a new export with the selected fields for the given object
     * @param token the token indicating the object type and its configuration to use; this token is also used for storage
     * @param selectedFields the fields selected for the report
     * @return the export of the given report
     */
    static BaseDetailsExport createExport(String token, Map<String, Object> selectedFields) {

        String tmpl = getCachedExportStrategy( token )

        if (tmpl == OrgExport.KEY) {
            return new OrgExport( token, selectedFields )
        }
        else if (tmpl == IssueEntitlementExport.KEY) {
            return new IssueEntitlementExport( token, selectedFields )
        }
        else if (tmpl == SubscriptionExport.KEY) {
            return new SubscriptionExport( token, selectedFields )
        }
        else if (tmpl == CostItemExport.KEY) {
            return new CostItemExport( token, selectedFields )
        }
    }

    // ----- Cache -----

    /**
     * Gets the cached filter stored under the given token
     * @param token the token to load
     * @return the cached filter
     */
    static Map<String, Object> getFilterCache(String token) {
        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, token )
        Map<String, Object> cacheMap = rCache.get()

        cacheMap.filterCache as Map<String, Object>
    }

    /**
     * Gets the cached query storage under the given token
     * @param token the token to load
     * @return the cached query
     */
    static Map<String, Object> getQueryCache(String token) {
        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, token )
        Map<String, Object> cacheMap = rCache.get()

        cacheMap.queryCache as Map<String, Object>
    }

    /**
     * Gets the details cache stored under the given token
     * @param token the token to load
     * @return the cached object details
     */
    static Map<String, Object> getDetailsCache(String token) {
        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_SUBSCRIPTION, token )
        Map<String, Object> cacheMap = rCache.get()

        cacheMap.detailsCache as Map<String, Object>
    }

    /**
     * Gets the export strategy stored under the given token
     * @param token the token to load
     * @return the export strategy from the details
     */
    static String getCachedExportStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        List parts = detailsCache.tmpl.split('/')
        parts[parts.size()-1]
    }

    /**
     * Gets the underlying config strategy of the export for the given token
     * @param token the token to load
     * @return the config strategy from the details
     */
    static String getCachedConfigStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        List<String> queryParts = detailsCache.query.split('-')
        queryParts.size() == 3 ? queryParts[2] : queryParts[0]
    }

    /**
     * Gets the underlying field strategy of the export for the given token
     * @param token the token to load
     * @return the field strategy from the details
     */
    static String getCachedFieldsStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        detailsCache.query.substring( detailsCache.query.indexOf('-') + 1 )
    }

    // -----

//    static Map<String, Object> getCachedFilterLabels(String token) {
//
//        Map<String, Object> filterCache = getFilterCache(token)
//        filterCache.labels as Map<String, Object>
//    }

    /**
     * Gets the cached field result stored under the given token
     * @param token the token to load
     * @return the cached filter result
     */
    static String getCachedFilterResult(String token) {

        Map<String, Object> filterCache = getFilterCache(token)
        filterCache.result
    }

    /**
     * Gets the filter labels cached under the given token
     * @param token the token to load
     * @return the cached query labels
     */
    static List<String> getCachedQueryLabels(String token) {

        Map<String, Object> queryCache = getQueryCache(token)
        queryCache.labels.labels as List<String>
    }

    // -----

    /**
     * Builds the export label for the given name in context of the given report
     * @param export the report to be exported resp. configurations to consider
     * @param fieldName the field key to which the label should be build
     * @return the field label appearing in the report export
     */
    static String getFieldLabel(BaseDetailsExport export, String fieldName) {

        if ( BaseDetailsExport.isFieldMultiple(fieldName) ) {
            // String label = BaseDetailsExport.CUSTOM_LABEL.get(fieldName)
            String label = BaseDetailsExport.getExportLabel(fieldName)

            if (fieldName == 'x-identifier' || fieldName == '@-entitlement-tippIdentifier') {
                List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
                label += (selList ? ': ' + selList.collect{it ->
                    IdentifierNamespace idns = IdentifierNamespace.get(it)
                    idns.getI10n('name') ?: GenericHelper.flagUnmatched( idns.ns )
                }.join(', ') : '')
            }
            else if (fieldName == '@-org-accessPoint') {
                List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
                label += (selList ? ': ' + selList.collect{it -> RefdataValue.get(it).getI10n('value') }.join(', ') : '') // TODO - export
            }
            else if (fieldName == '@-org-contact') {
                List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
                label += (selList ? ': ' + selList.collect{it -> RefdataValue.get(it).getI10n('value') }.join(', ') : '') // TODO - export
            }
            else if (fieldName == '@-org-readerNumber') {
                List selList = export.getSelectedFields().get(fieldName) as List
                List semList = selList.findAll{ it.startsWith('sem-') }.collect{ RefdataValue.get( it.replace('sem-', '') ).getI10n('value') }
                List ddList  = selList.findAll{ it.startsWith('dd-') }.collect{ it.replace('dd-', 'Stichtage ') }
                label += (selList ? ': ' + (semList + ddList).join(', ') : '') // TODO - export
            }

            return label
        }
        else if (fieldName in ['x-property']) {
            return BaseDetailsExport.getExportLabel('x-property') + ': ' + getQueryCache( export.token ).labels.labels[2] // TODO - modal
        }
        else if (fieldName in ['globalUID', 'x-provider'] || fieldName.startsWith('@')) {
            return BaseDetailsExport.getExportLabel(fieldName)
        }

        // --- adapter - label from config ---
        // println 'LocalExportHelper.getFieldLabel() - adapter: ' + fieldName

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
