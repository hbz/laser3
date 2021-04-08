package de.laser.exporting

import de.laser.License
import de.laser.Org
import de.laser.Subscription

class GenericExportManager {

    static AbstractExport getCurrentExport(String query) {
        GenericExportManager.getCurrentExport( query, [:] )
    }

    static AbstractExport getCurrentExport(String query, Map<String, Object> selectedFields) {

        String prefix = query.split('-')[0]

        if (prefix == 'license') {
            return new LicenseExport( selectedFields )
        }
        if (prefix in ['org', 'member', 'licensor', 'provider']) {
            return new OrgExport( selectedFields )
        }
        if (prefix == 'subscription') {
            return new SubscriptionExport( selectedFields )
        }
    }

    static List<String> doExport(AbstractExport export, List<Long> idList) {

        List<String> rows = []
        rows.add( buildRow( export.getSelectedFields().collect{it -> it.value.text } ) )
        rows.addAll( buildRows(export, idList) )

        rows
    }

    static List<String> buildRows(AbstractExport export, List<Long> idList) {

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

        idList.each { Long id ->
            List<String> row = export.getObject( id, fields )
            if (row) {
                rows.add( buildRow( row ) )
            }
        }

        rows
    }

    static String buildRow(List<String> content) {

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
