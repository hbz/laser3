package de.laser.reporting.export

import de.laser.License
import de.laser.Org
import de.laser.Subscription

class GenericExportManager {

    static AbstractExport createExport(String token) {
        GenericExportManager.createExport( token, [:] )
    }

    static AbstractExport createExport(String token, Map<String, Object> selectedFields) {

        String prefix = ExportHelper.getCachedQueryPrefix( token )

        if (prefix == 'license') {
            return new LicenseExport( token, selectedFields )
        }
        if (prefix in ['org', 'member', 'licensor', 'provider']) {
            return new OrgExport( token, selectedFields )
        }
        if (prefix == 'subscription') {
            return new SubscriptionExport( token, selectedFields )
        }
    }

    static List<String> export(AbstractExport export, List<Long> idList) {

        List<String> rows = []
        rows.add( buildCSVRow(
                export.getSelectedFields().collect{it -> export.getFieldLabel(it.key as String) }
        ) )
        rows.addAll( buildCSV(export, idList) )

        rows
    }

    static List<String> buildCSV(AbstractExport export, List<Long> idList) {

        List<String> rows = []
        Map<String, Object> fields = export.getSelectedFields()

        if (export.KEY == LicenseExport.KEY) {
            idList = License.executeQuery( 'select l.id from License l where l.id in (:idList) order by l.reference', [idList: idList] )
        }
        else if (export.KEY == OrgExport.KEY) {
            idList = Org.executeQuery( 'select o.id from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList] )
        }
        else if (export.KEY == SubscriptionExport.KEY) {
            idList = Subscription.executeQuery( 'select s.id from Subscription s where s.id in (:idList) order by s.name', [idList: idList] )
        }

        idList.each { id ->
            List<String> row = export.getObject( id as Long, fields )
            if (row) {
                rows.add( buildCSVRow( row ) )
            }
        }

        rows
    }

    static String buildCSVRow(List<String> content) {

        content.collect{it ->
            if (it == null) {
                return ''
            }
            if (it.contains( AbstractExport.CSV_FIELD_QUOTATION )) {
                it = it.replaceAll( AbstractExport.CSV_FIELD_QUOTATION , AbstractExport.CSV_FIELD_QUOTATION + AbstractExport.CSV_FIELD_QUOTATION)
            }
            if (it.contains( AbstractExport.CSV_FIELD_SEPARATOR )) {
                return AbstractExport.CSV_FIELD_QUOTATION + it.trim() + AbstractExport.CSV_FIELD_QUOTATION
            }
            return it.trim()
        }.join( AbstractExport.CSV_FIELD_SEPARATOR )
    }
}
