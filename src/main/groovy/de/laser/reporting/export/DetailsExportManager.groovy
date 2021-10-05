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

    static List exportAsList(BaseExport export, List<Long> idList, String format, boolean hideEmptyResults) {

        List rows = []

        List objList = resolveObjectList( export, idList )
        Map<String, Object> fields = export.getSelectedFields()
        List<String> cols = fields.collect{it -> export.getFieldLabel(it.key as String) }

        if (format == 'csv') {
            List<List<String>> csv
            List<Integer> ici
            (csv, ici) = buildCSV(export, objList, fields)

            if (hideEmptyResults) {
                ici.each { i -> /* println 'Export CSV ignored: ' + cols[i]; */ cols.removeAt(i) }
            }
            rows.add( cols.join( BaseExport.CSV_FIELD_SEPARATOR ) )

            csv.each { row ->
                if (hideEmptyResults) {
                    ici.each { i -> row.removeAt(i) }
                }
                rows.add( row.join( BaseExport.CSV_FIELD_SEPARATOR ) )
            }
        }
        else if (format == 'pdf') {
            List<List<List<String>>> pdf
            List<Integer> ici
            (pdf, ici) = buildPDF(export, objList, fields)

            if (hideEmptyResults) {
                ici.each { i -> /* println 'Export PDF ignored: ' + cols[i]; */ cols.removeAt(i) }
            }
            rows.add( buildRowAsPDF(cols) )

            pdf.each { row ->
                if (hideEmptyResults) {
                    ici.each { i -> row.removeAt(i) }
                }
                rows.add( row )
            }
        }
        rows
    }

    static Workbook exportAsWorkbook(BaseExport export, List<Long> idList, String format, boolean hideEmptyResults) {

        List objList = resolveObjectList( export, idList )

        if (format == 'xlsx') {
            buildXLSX(export, objList, export.getSelectedFields(), hideEmptyResults)
        }
    }

    static List buildCSV(BaseExport export, List objList, Map<String, Object> fields) {

        List<List<String>> rows = []
        List<Integer> ici = []

        Integer[] cc = new Integer[fields.size()].collect{ 0 }

        objList.each{ obj ->
            List<String> row = export.getObject( obj, fields )
            if (row) {
                List<String> cols = buildRowAsCSV( row )
                cols.eachWithIndex{ c, i -> if (c) { cc[i]++ } }
                rows.add( cols )
            }
        }
        cc.eachWithIndex{ c, i -> if (c == 0) { ici.add(i) } }

        [rows, ici.reverse()]
    }

    static List<String> buildRowAsCSV(List<String> content) {

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
        }
    }

    static Workbook buildXLSX(BaseExport export, List objList, Map<String, Object> fields, boolean hideEmptyResults) {

        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet( export.token )

        CellStyle wrapStyle = workbook.createCellStyle()
        wrapStyle.setWrapText(true)
        wrapStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        List<List<String>> rows = []
        List<Integer> ici = []
        Integer[] cc = new Integer[fields.size()].collect{ 0 }

        objList.each{ obj ->
            List<String> row = export.getObject(obj, fields)
            if (row) {
                rows.add( row )
                row.eachWithIndex{ col, i -> if (col) { cc[i]++ } }
            }
        }
        cc.eachWithIndex{ c, i -> if (c == 0) { ici.add(i) } }

        List<String> cols = fields.collect{it -> export.getFieldLabel(it.key as String) }
        if (hideEmptyResults) {
            ici.each { i -> /* println 'Export XLSX ignored: ' + cols[i]; */ cols.remove(i) }
        }

        Row header = sheet.createRow(0)
        cols.eachWithIndex{ col, idx ->
            Cell headerCell = header.createCell(idx)
            headerCell.setCellStyle(cellStyle)
            headerCell.setCellValue(col)
            sheet.autoSizeColumn(idx)
        }

        rows.eachWithIndex { row, idx ->
            if (hideEmptyResults) {
                ici.each { i -> row.removeAt(i) }
            }
            if (row) {
                Row entry = sheet.createRow(idx + 1)
                row.eachWithIndex { col, i ->
                    Cell cell = entry.createCell(i)
                    cell.setCellStyle(cellStyle)

                    if (col == null) {
                        cell.setCellValue('')
                    }
                    else {
                        if (col.contains(BaseExport.CSV_VALUE_SEPARATOR)) {
                            cell.setCellValue(col.split(BaseExport.CSV_VALUE_SEPARATOR).collect { it.trim() }.join('\r\n'))
                            cell.setCellStyle(wrapStyle)
                        }
                        else {
                            cell.setCellValue(col.trim())
                        }
                    }
                    sheet.autoSizeColumn(i)
                }
            }
        }

        workbook
    }

    static List buildPDF(BaseExport export, List objList, Map<String, Object> fields) {

        List<List<List<String>>> rows = []
        List<Integer> ici = []

        Integer[] cc = new Integer[fields.size()].collect{ 0 }

        objList.each{ obj ->
            List<String> row = export.getObject(obj, fields)
            if (row) {
                List<List<String>> cols = buildRowAsPDF( row )
                cols.eachWithIndex{ c, i -> if (c.first()) { cc[i]++ } }
                rows.add( cols )
            }
        }
        cc.eachWithIndex{ c, i -> if (c == 0) { ici.add(i) } }

        [rows, ici.reverse()]
    }

    static List<List<String>> buildRowAsPDF(List<String> content) {

        content.collect{it ->
            if (it == null) {
                return ['']
            }
            return it.split(BaseExport.CSV_VALUE_SEPARATOR).collect{ it.trim() }
        }
    }

    static List<Object> resolveObjectList(BaseExport export, List<Long> idList) {

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
