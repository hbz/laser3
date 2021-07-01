package de.laser.reporting.export

import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.export.local.ExportLocalHelper
import de.laser.reporting.export.myInstitution.ExportGlobalHelper
import de.laser.reporting.export.myInstitution.LicenseExport
import de.laser.reporting.export.myInstitution.OrgExport
import de.laser.reporting.export.myInstitution.SubscriptionExport
import de.laser.reporting.myInstitution.base.BaseConfig

class DetailsExportManager {

    static AbstractExport createExport(String token, String context) {
        if (context == BaseConfig.KEY_MYINST) {
            createGlobalExport(token, [:])
        }
        else if (context in [ BaseConfig.KEY_SUBSCRIPTION ]) {
            createLocalExport(token, [:])
        }
    }

    static AbstractExport createGlobalExport(String token, Map<String, Object> selectedFields) {
        ExportGlobalHelper.createExport( token, selectedFields )
    }

    static AbstractExport createLocalExport(String token, Map<String, Object> selectedFields) {
        ExportLocalHelper.createExport( token, selectedFields )
    }

    static List export(AbstractExport export, String format, List<Long> idList) {

        List rows = []

        if (format == 'csv') {
            rows.add( buildCsvRow(
                    export.getSelectedFields().collect{it -> export.getFieldLabel(it.key as String) }
            ) )
            rows.addAll( buildCsv(export, idList) )
        }
        else if (format == 'pdf') {
            rows.add( buildPdfRow(
                    export.getSelectedFields().collect{it -> export.getFieldLabel(it.key as String) }
            ) )
            rows.addAll( buildPdf(export, idList) )
        }

        rows
    }

    static List<String> buildCsv(AbstractExport export, List<Long> idList) {

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
                rows.add( buildCsvRow( row ) )
            }
        }

        rows
    }

    static String buildCsvRow(List<String> content) {

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

    static List<List<List<String>>> buildPdf(AbstractExport export, List<Long> idList) {

        List<List<List<String>>> rows = []
        Map<String, Object> fields = export.getSelectedFields()

        if (export.KEY == LicenseExport.KEY) {
            idList = License.executeQuery('select l.id from License l where l.id in (:idList) order by l.reference', [idList: idList])
        }
        else if (export.KEY == OrgExport.KEY) {
            idList = Org.executeQuery('select o.id from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
        }
        else if (export.KEY == SubscriptionExport.KEY) {
            idList = Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList) order by s.name', [idList: idList])
        }

        idList.each { id ->
            List<String> row = export.getObject(id as Long, fields)
            if (row) {
                rows.add( buildPdfRow( row ) )
            }
        }

        rows
    }

    static List<List<String>> buildPdfRow(List<String> content) {

        content.collect{it ->
            if (it == null) {
                return ['']
            }
            return it.split(AbstractExport.CSV_VALUE_SEPARATOR).collect{ it.trim() }
        }
    }
}
