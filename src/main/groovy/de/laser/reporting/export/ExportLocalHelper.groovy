package de.laser.reporting.export

import de.laser.*
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.reporting.myInstitution.GenericHelper
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseDetails
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery

import java.text.SimpleDateFormat
import java.time.Year

class ExportLocalHelper extends AbstractExportHelper {

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

    static String getFileName(List<String> labels) {

        SimpleDateFormat sdf = DateUtils.getSDF_forFilename()
        labels = labels ?: [ 'Reporting' ]

        String filename = sdf.format(new Date()) + '_' + labels.collect{
            it.replaceAll('[→/]', '-')
                .replaceAll('[^\\wäöüÄÖÜ!"§$%&()=?\'{},.\\-+~#;:]', '')
                .replaceAll(' ', '')
        }.join('_')

        filename
    }

    // -----  -----

    static void normalizeSelectedMultipleFields(AbstractExport export) {

        export.selectedExportFields.each {it ->
            if ( isFieldMultiple( it.key ) ) {
                if ( it.key == '@ae-org-readerNumber' ) {
                    export.selectedExportFields[it.key] = it.value instanceof String ? [ it.value ] : it.value.collect { it }
                }
                else {
                    export.selectedExportFields[it.key] = it.value instanceof String ? [Long.parseLong(it.value)] : it.value.collect { Long.parseLong(it) }
                }
            }
        }
    }

    static def reorderFieldsForUI(Map<String, Object> formFields) {

        Map<String, Object> result = [:]
        List<Integer> reorder = []
        int max = formFields.keySet().size()

        for (def i=0; i<max; i++) {
            if (i%2==0) {
                reorder[i] = ( i - i/2 ) as Integer
            } else {
                reorder[i] = Math.round(Math.floor(i/2 + max/2)) as Integer
            }
        }
        reorder.each {i ->
            String key = formFields.keySet()[i]
            result.putAt(key, formFields.get(key))
        }

        result
    }

    static boolean isFieldMultiple(String fieldName) {

        if (fieldName in [ 'x-identifier', '@ae-org-accessPoint', '@ae-org-readerNumber']) {
            return true
        }
        return false
    }

    static List getMultipleFieldListForDropdown(String key, Map<String, Object> cfg) {

        if (key == 'x-identifier') {
            getIdentifierNamespacesForDropdown( cfg )
        }
        else if (key == '@ae-org-accessPoint') {
            getAccessPointMethodsforDropdown()
        }
        else if (key == '@ae-org-readerNumber') {
            getReaderNumberSemesterAndDueDatesForDropdown()
        }
    }

    static List getIdentifierNamespacesForDropdown(Map<String, Object> cfg) {
        List<IdentifierNamespace> idnsList = []

        if (cfg.base.meta.class == Org) {
            idnsList = Org.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Org.class.name] )
        }
        else if (cfg.base.meta.class == License) {
            idnsList = License.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: License.class.name] )
        }
        else if (cfg.base.meta.class == Subscription) {
            idnsList = Subscription.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Subscription.class.name] )
        }

        idnsList.collect{ it ->
            [ it.id, it.getI10n('name') ?: it.ns + ' *' ]
        }
    }

    static List getAccessPointMethodsforDropdown() {
        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.ACCESS_POINT_TYPE )

        aptList.collect{ it ->
            [ it.id, it.getI10n('value') ]
        }
    }

    static List getReaderNumberSemesterAndDueDatesForDropdown() {
        List<RefdataValue> semList = RefdataCategory.getAllRefdataValuesWithOrder( RDConstants.SEMESTER )

        List result = semList.collect{ it ->
            [ 'sem-' + it.id, it.getI10n('value') ]
        }

        int y = Year.now().value
        result.addAll( (y+2..y-4).collect{[ 'dd-' + it, 'Stichtage für ' + it ]} )

        result
    }

    static Map<String, Object> calculatePdfPageStruct(List<List<String>> content, String pin) {

        Map<String, Object> struct = [
            width       : [],
            height      : [],
            pageSize    : '',
            orientation : 'Portrait'
        ]

        if (pin == 'chartDetailsExport') {

            content.eachWithIndex { List row, int i ->
                row.eachWithIndex { List cell, int j ->
                    if (!struct.height[i] || struct.height[i] < cell.size()) {
                        struct.height[i] = cell.size()
                    }
                    cell.eachWithIndex { String entry, int k ->
                        if (i == 0) {
                            struct.width[j] = entry.length() < 15 ? 15 : entry.length() > 35 ? 35 : entry.length()
                        }
                        else {
                            if (!struct.width[j] || struct.width[j] < entry.length()) {
                                struct.width[j] = entry.length()
                            }
                        }
                    }
                }
            }
        }
        else if (pin == 'chartQueryExport') {

            content.eachWithIndex { List row, int i ->
                row.eachWithIndex { String cell, int j ->
                    struct.height[i] = 1
                    struct.width[j] = cell.length() < 15 ? 15 : cell.length() > 35 ? 35 : cell.length()
                }
            }
        }
        else if (pin == 'chartQueryExport-image') {

            // TODO
            // TODO
        }
        else {
            println ' ----- TODO: calculatePdfPageStruct( ' + pin + ' ) ----- '
        }

        String[] sizes = [ 'A0', 'A1', 'A2', 'A3', 'A4' ]
        int pageSize = 4

        int wx = 85, w = struct.width.sum() as int
        int hx = 35, h = struct.height.sum() as int

        if (w > wx*4)       { pageSize = 0 }
        else if (w > wx*3)  { pageSize = 1 }
        else if (w > wx*2)  { pageSize = 2 }
        else if (w > wx)    { pageSize = 3 }

        struct.whr = (w * 0.75) / (h + 15)
        if (struct.whr > 5) {
            if (w < wx*7) {
                if (pageSize < sizes.length - 1) {
                    pageSize++
                }
            }
            struct.orientation = 'Landscape'
        }

        struct.width = struct.width.sum()
        struct.height = struct.height.sum()
        struct.pageSize = sizes[ pageSize ]

        struct
    }
}
