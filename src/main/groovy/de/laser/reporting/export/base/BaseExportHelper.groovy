package de.laser.reporting.export.base

import de.laser.IdentifierNamespace
import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore

import java.text.SimpleDateFormat
import java.time.Year

abstract class BaseExportHelper {

    static def getPropertyFieldContent(Object obj, String field, Class type) {

        def content = obj.getProperty(field)

        if (type in [boolean, Boolean]) {
            if (obj.getProperty(field) == true) {
                content = RDStore.YN_YES.getI10n('value')
            }
            else if (obj.getProperty(field) == false) {
                content = RDStore.YN_NO.getI10n('value')
            }
            else {
                content = ''
            }
        }
        // println field + ' >> ' + content + ' : ' + content?.class
        content
    }

    static String getFileName(List<String> labels = ['Reporting']) {

        SimpleDateFormat sdf = DateUtils.getSDF_forFilename()

        String filename = sdf.format(new Date()) + '_' + labels.collect{
            it.replaceAll('[→/]', '-')
                    .replaceAll('[^\\wäöüÄÖÜ!"§$%&()=?\'{},.\\-+~#;:]', '')
                    .replaceAll(' ', '')
        }.join('_')

        filename
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

    // -----

    static boolean isFieldMultiple(String fieldName) {

        if (fieldName in [ 'x-identifier', '@ae-org-accessPoint', '@ae-org-contact', '@ae-org-readerNumber', '@ae-entitlement-tippIdentifier']) {
            return true
        }
        return false
    }

    static void normalizeSelectedMultipleFields(BaseExport export) {

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

    static List getMultipleFieldListForDropdown(String key, Map<String, Object> cfg) {

        if (key == 'x-identifier') {
            getIdentifierNamespacesForDropdown( cfg )
        }
        else if (key == '@ae-org-accessPoint') {
            getAccessPointMethodsforDropdown()
        }
        else if (key == '@ae-org-contact') {
            getContactOptionsforDropdown()
        }
        else if (key == '@ae-org-readerNumber') {
            getReaderNumberSemesterAndDueDatesForDropdown()
        }
        else if (key == '@ae-entitlement-tippIdentifier') {
            getIdentifierNamespacesForDropdown( cfg )
        }
    }

    static List getIdentifierNamespacesForDropdown(Map<String, Object> cfg) {
        List<IdentifierNamespace> idnsList = []

        if (cfg.base.meta.class == Org) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Org.class.name] )
        }
        else if (cfg.base.meta.class == License) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: License.class.name] )
        }
        else if (cfg.base.meta.class == Subscription) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: Subscription.class.name] )
        }
        else if (cfg.base.meta.class == IssueEntitlement) {
            idnsList = IdentifierNamespace.executeQuery( 'select idns from IdentifierNamespace idns where idns.nsType = :type', [type: TitleInstancePackagePlatform.class.name] )
        }

        idnsList.collect{ it ->
            [ it.id, it.getI10n('name') ?: it.ns + ' *' ]
        }.sort { a,b -> a[1] <=> b[1] }
    }

    static List getAccessPointMethodsforDropdown() {
        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.ACCESS_POINT_TYPE )

        aptList.collect{ it ->
            [ it.id, it.getI10n('value') ]
        }
    }
    static List getContactOptionsforDropdown() {
        List<RefdataValue> aptList = RefdataCategory.getAllRefdataValues( RDConstants.REPORTING_CONTACT_TYPE )

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

    // -----

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
