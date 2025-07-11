package de.laser.reporting.export

import de.laser.IdentifierNamespace
import de.laser.RefdataValue
import de.laser.reporting.export.myInstitution.LicenseExport
import de.laser.reporting.export.myInstitution.OrgExport
import de.laser.reporting.export.myInstitution.PackageExport
import de.laser.reporting.export.myInstitution.PlatformExport
import de.laser.reporting.export.myInstitution.ProviderExport
import de.laser.reporting.export.myInstitution.SubscriptionExport
import de.laser.reporting.export.myInstitution.VendorExport
import de.laser.reporting.report.ReportingCache
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseQuery

/**
 * Class containing helper methods valid for global report exports, i.e. such which are regarding the institution as a whole
 */
class GlobalExportHelper extends BaseExportHelper {

    /**
     * Creates a new export with the selected fields for the given object
     * @param token the token indicating the object type and its configuration to use; this token is also used for storage
     * @param selectedFields the fields selected for the report
     * @return the export of the given report
     */
    static BaseDetailsExport createExport(String token, Map<String, Object> selectedFields) {

        String tmpl = getCachedExportStrategy( token )

        if (tmpl == LicenseExport.KEY) {
            return new LicenseExport( token, selectedFields )
        }
        else if (tmpl == OrgExport.KEY) {
            return new OrgExport( token, selectedFields )
        }
        else if (tmpl == PackageExport.KEY) {
            return new PackageExport( token, selectedFields )
        }
        else if (tmpl == PlatformExport.KEY) {
            return new PlatformExport( token, selectedFields )
        }
        else if (tmpl == ProviderExport.KEY) {
            return new ProviderExport( token, selectedFields )
        }
        else if (tmpl == SubscriptionExport.KEY) {
            return new SubscriptionExport( token, selectedFields )
        }
        else if (tmpl == VendorExport.KEY) {
            return new VendorExport( token, selectedFields )
        }
    }

    // ----- Cache -----

    /**
     * Retrieves the reporting metadata from the cache
     * @param token the cache token where the reporting data is being stored
     * @return the basic report configuration map
     * @see ReportingCache#readMeta()
     */
    static Map<String, Object> getMeta(String token) {

        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, token )
        rCache.readMeta()
    }

    /**
     * Gets the cached filter stored under the given token
     * @param token the token to load
     * @return the cached filter
     */
    static Map<String, Object> getFilterCache(String token) {

        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, token )
        rCache.readFilterCache()
    }

    /**
     * Gets the cached query storage under the given token
     * @param token the token to load
     * @return the cached query
     */
    static Map<String, Object> getQueryCache(String token) {

        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, token )
        rCache.readQueryCache()
    }

    /**
     * Gets the details cache stored under the given token
     * @param token the token to load
     * @return the cached object details
     */
    static Map<String, Object> getDetailsCache(String token) {

        ReportingCache rCache = new ReportingCache( ReportingCache.CTX_GLOBAL, token )
        rCache.readDetailsCache()
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
    static String getCachedFieldStrategy(String token) {

        Map<String, Object> detailsCache = getDetailsCache(token)
        detailsCache.query.substring( detailsCache.query.indexOf('-') + 1 )
    }

    // -----

    /**
     * Gets the cached filter labels stored under the given token
     * @param token the token to load
     * @return the cached filter labels
     */
    static Map<String, Object> getCachedFilterLabels(String token) {

        Map<String, Object> filterCache = getFilterCache(token)
        filterCache.labels as Map<String, Object>
    }

    /**
     * Gets the cached filter result stored under the given token
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
     * Gets the query labels for filtering
     * @param token the token to load
     * @return the filter query labels
     */
    static List<String> getIncompleteQueryLabels(String token) {

        Map<String, Object> queryCache = getQueryCache( token )
        //String prefix = queryCache.query.split('-')[0]
        Map<String, Object> cfg = BaseConfig.getCurrentConfigByFilter( getMeta(token).filter )
        BaseQuery.getQueryLabels(cfg, queryCache.query as String) // TODO
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
            //String label = BaseDetailsExport.CUSTOM_LABEL.get(fieldName)
            String label = BaseDetailsExport.getExportLabel(fieldName)

            if (fieldName == 'x-identifier') {
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
            else if (fieldName in ['@-org-contact', '@-provider-contact', '@-vendor-contact']) {
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
        else if (fieldName in ['x-property', 'x-memberSubscriptionProperty']) {
            return BaseDetailsExport.getExportLabel('x-property') + ': ' + getQueryCache( export.token ).labels.labels[2] // TODO - modal
        }
        else if (fieldName in ['laserID', 'x-provider'] || fieldName.startsWith('@')) {
            return BaseDetailsExport.getExportLabel(fieldName)
        }

        // --- adapter - label from config ---
        // println 'GlobalExportHelper.getFieldLabel() - adapter: ' + fieldName

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
