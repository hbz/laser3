package de.laser.reporting.export

import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.finance.CostItem
import de.laser.reporting.export.base.BaseExport
import de.laser.reporting.export.local.CostItemExport
import de.laser.reporting.export.local.ExportLocalHelper
import de.laser.reporting.export.local.IssueEntitlementExport
import de.laser.reporting.export.myInstitution.ExportGlobalHelper
import de.laser.reporting.export.myInstitution.LicenseExport
import de.laser.reporting.export.myInstitution.OrgExport
import de.laser.reporting.export.myInstitution.SubscriptionExport
import de.laser.reporting.myInstitution.base.BaseConfig
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

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

    static List exportAsList(BaseExport export, String format, List<Long> idList) {

        List rows = []

        if (format == 'csv') {
            rows.add( buildRowCSV(
                    export.getSelectedFields().collect{it -> export.getFieldLabel(it.key as String) }
            ) )
            rows.addAll( buildCSV(export, idList) )
        }
        else if (format == 'pdf') {
            rows.add( buildRowPDF(
                    export.getSelectedFields().collect{it -> export.getFieldLabel(it.key as String) }
            ) )
            rows.addAll( buildPDF(export, idList) )
        }
        rows
    }

    static Workbook exportAsWorkbook(BaseExport export, String format, List<Long> idList) {

        if (format == 'xlsx') {
            buildXLSX(export, idList)
        }
    }

    static List<String> buildCSV(BaseExport export, List<Long> idList) {

        List<String> rows = []
        Map<String, Object> fields = export.getSelectedFields()

        List objList = resolveIdList( export, idList )

        objList.eachWithIndex { obj, i ->
            List<String> row = export.getObject( obj, fields )
            if (row) {
                rows.add( buildRowCSV( row ) )
            }
        }
        rows
    }

    static String buildRowCSV(List<String> content) {

        content.collect{it ->
            boolean enclose = false
            if (! it) {
                return ''
            }
            if (it.contains( BaseExport.CSV_FIELD_QUOTATION )) {
                it = it.replaceAll( BaseExport.CSV_FIELD_QUOTATION , BaseExport.CSV_FIELD_QUOTATION + BaseExport.CSV_FIELD_QUOTATION) // !
                enclose = true
            }
            if (enclose || it.contains( BaseExport.CSV_FIELD_SEPARATOR )) {
                return BaseExport.CSV_FIELD_QUOTATION + it.trim() + BaseExport.CSV_FIELD_QUOTATION
            }
            return it.trim()
        }.join( BaseExport.CSV_FIELD_SEPARATOR )
    }

    static Workbook buildXLSX(BaseExport export, List<Long> idList) {

        Map<String, Object> fields = export.getSelectedFields()
        List objList = resolveIdList( export, idList )

        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet( export.token )

        CellStyle wrapStyle = workbook.createCellStyle()
        wrapStyle.setWrapText(true)
        wrapStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        objList.eachWithIndex { obj, idx ->

            List<String> strList = export.getObject(obj, fields)
            if (strList) {
                Row entry = sheet.createRow(idx+1)
                strList.eachWithIndex{ str, i ->
                    Cell cell = entry.createCell(i)
                    cell.setCellStyle(cellStyle)

                    if (str == null) {
                        cell.setCellValue('')
                    }
                    else {
                        if (str.contains( BaseExport.CSV_VALUE_SEPARATOR )) {
                            cell.setCellValue( str.split( BaseExport.CSV_VALUE_SEPARATOR ).collect {it.trim() }.join('\r\n') )
                            cell.setCellStyle(wrapStyle)
                        } else {
                            cell.setCellValue( str.trim() )
                        }
                    }
                    sheet.autoSizeColumn(i)
                }
            }
        }

        Row header = sheet.createRow(0)

        fields.collect{it -> export.getFieldLabel(it.key as String) }.eachWithIndex{ row, idx ->
            Cell headerCell = header.createCell(idx)
            headerCell.setCellStyle(cellStyle)
            headerCell.setCellValue(row)
            sheet.autoSizeColumn(idx)
        }

        workbook
    }

    static List<List<List<String>>> buildPDF(BaseExport export, List<Long> idList) {

        List<List<List<String>>> rows = []
        Map<String, Object> fields = export.getSelectedFields()

        List objList = resolveIdList( export, idList )

        objList.eachWithIndex { obj, i ->
            List<String> row = export.getObject(obj, fields)
            if (row) {
                rows.add( buildRowPDF( row ) )
            }
        }
        rows
    }

    static List<List<String>> buildRowPDF(List<String> content) {

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
        else if (export.KEY == CostItemExport.KEY) {
//            Long subId = ExportLocalHelper.getDetailsCache( export.token ).id
//            result = CostItem.executeQuery(
//                    'select ci from CostItem ci where ci.sub.id = :subId and ci.id in (:idList) order by ci.id',
//                    [subId: subId, idList: idList]
//            )
            result = CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.id', [idList: idList])
        }
        result
    }
}
