package de.laser.reporting.export

import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.export.base.BaseExport
import de.laser.reporting.export.local.ExportLocalHelper
import de.laser.reporting.export.local.IssueEntitlementExport
import de.laser.reporting.export.myInstitution.ExportGlobalHelper
import de.laser.reporting.export.myInstitution.LicenseExport
import de.laser.reporting.export.myInstitution.OrgExport
import de.laser.reporting.export.myInstitution.SubscriptionExport
import de.laser.reporting.myInstitution.base.BaseConfig

class DetailsExportManager {

    static BaseExport createExport(String token, String context) {
        if (context == BaseConfig.KEY_MYINST) {
            createGlobalExport(token, [:])
        }
        else if (context in [ BaseConfig.KEY_SUBSCRIPTION ]) {
            createLocalExport(token, [:])
        }
    }

    static BaseExport createGlobalExport(String token, Map<String, Object> selectedFields) {
        ExportGlobalHelper.createExport( token, selectedFields )
    }

    static BaseExport createLocalExport(String token, Map<String, Object> selectedFields) {
        ExportLocalHelper.createExport( token, selectedFields )
    }

    static List export(BaseExport export, String format, List<Long> idList) {

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

    static List<String> buildCsv(BaseExport export, List<Long> idList) {

        List<String> rows = []
        Map<String, Object> fields = export.getSelectedFields()

        List objList = resolveIdList( export, idList )

        objList.eachWithIndex { obj, i ->
            //println '- ' + i + ' : ' + obj

            List<String> row = export.getObject( obj, fields )
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
            if (it.contains( BaseExport.CSV_FIELD_QUOTATION )) {
                it = it.replaceAll( BaseExport.CSV_FIELD_QUOTATION , BaseExport.CSV_FIELD_QUOTATION + BaseExport.CSV_FIELD_QUOTATION)
            }
            if (it.contains( BaseExport.CSV_FIELD_SEPARATOR )) {
                return BaseExport.CSV_FIELD_QUOTATION + it.trim() + BaseExport.CSV_FIELD_QUOTATION
            }
            return it.trim()
        }.join( BaseExport.CSV_FIELD_SEPARATOR )
    }

    static List<List<List<String>>> buildPdf(BaseExport export, List<Long> idList) {

        List<List<List<String>>> rows = []
        Map<String, Object> fields = export.getSelectedFields()

        List objList = resolveIdList( export, idList )

        objList.eachWithIndex { obj, i ->
            //println '- ' + i + ' : ' + obj

            List<String> row = export.getObject(obj, fields)
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
            return it.split(BaseExport.CSV_VALUE_SEPARATOR).collect{ it.trim() }
        }
    }

    static List<Object> resolveIdList(BaseExport export, List<Long> idList) {

        List<Object> result = []

        if (export.KEY == LicenseExport.KEY) {
            result = License.executeQuery('select l from License l where l.id in (:idList) order by l.reference', [idList: idList])
        }
        else if (export.KEY == OrgExport.KEY) {
            result = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
        }
        else if (export.KEY == SubscriptionExport.KEY) {
            result = Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList])
        }
        else if (export.KEY == IssueEntitlementExport.KEY) {
            Long subId = ExportLocalHelper.getDetailsCache( export.token ).id
            result = IssueEntitlement.executeQuery(
                    'select ie from IssueEntitlement ie where ie.subscription.id = :subId and ie.tipp.id in (:idList) order by ie.name',
                    [subId: subId, idList: idList]
            )
        }
        result
    }
}
