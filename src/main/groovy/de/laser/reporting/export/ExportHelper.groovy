package de.laser.reporting.export

import de.laser.IdentifierNamespace
import de.laser.License
import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.RDConstants
import de.laser.reporting.myInstitution.GenericHelper
import de.laser.reporting.myInstitution.base.BaseDetails
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery

import java.time.Year

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

    static String getCachedFilterResult(String token) {

        Map<String, Object> filterCache = BaseFilter.getFilterCache(token)
        filterCache.result
    }

    static List<String> getCachedQueryLabels(String token) {

        Map<String, Object> queryCache = BaseQuery.getQueryCache(token)
        queryCache.labels.labels as List<String>
    }

    static String getFieldLabel(AbstractExport export, String fieldName) {

        if ( isFieldMultiple(fieldName) ) {
            String label = AbstractExport.CUSTOM_LABEL.get(fieldName)

            if (fieldName == 'x-identifier') {
                List<Long> selList = export.getSelectedFields().get(fieldName) as List<Long>
                label += (selList ? ': ' + selList.collect{it -> IdentifierNamespace.get(it).ns }.join(', ') : '') // TODO - export
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

        labels.collect{ it.replaceAll('→', '_').replaceAll(' ', '') }.join('_')
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
            [ it.id, it.ns ]
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
}
