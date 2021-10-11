package de.laser.reporting.export

import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class QueryExportManager {

    static BaseQueryExport createExport(String token, String context) {
        if (context == BaseConfig.KEY_MYINST) {
            new de.laser.reporting.export.myInstitution.QueryExport( token )
        }
        else if (context in [ BaseConfig.KEY_SUBSCRIPTION ]) {
            new de.laser.reporting.export.local.QueryExport( token )
        }
    }

    static List exportAsList(BaseQueryExport export, String format) {

        List rows = []
        Map<String, Object> data = export.getQueriedData()

        if (format == 'csv') {
            rows.add( data.cols.join( BaseDetailsExport.CSV_FIELD_SEPARATOR ) )
            data.rows.each { List<Object> row ->
                rows.add( buildRowCSV( row ) )
            }
        }
        else if (format == 'pdf') {
            rows.add( data.cols )
            data.rows.each { List<Object> row ->
                rows.add( buildRowPDF( row ) )
            }
        }
        rows
    }

    static Workbook exportAsWorkbook(BaseQueryExport export, String format) {

        if (format == 'xlsx') {
            buildXLSX(export)
        }
    }

    static String buildRowCSV(List<Object> row) {

        row.collect{ col ->
            boolean enclose = false
            if (! col) {
                return ''
            }
            if (col instanceof String) {
                if (col.contains(BaseDetailsExport.CSV_FIELD_QUOTATION)) {
                    col = col.replaceAll(BaseDetailsExport.CSV_FIELD_QUOTATION, BaseDetailsExport.CSV_FIELD_QUOTATION + BaseDetailsExport.CSV_FIELD_QUOTATION) // !
                    enclose = true
                }
                if (enclose || col.contains( BaseDetailsExport.CSV_FIELD_SEPARATOR )) {
                    return BaseDetailsExport.CSV_FIELD_QUOTATION + col.trim() + BaseDetailsExport.CSV_FIELD_QUOTATION
                }
            }
//            else if (col instanceof Date) {
//                println '!?? >>>>>>>>>>>>>>>>>>>>>>>>>>>> QueryExportManager.buildRowCSV() ' + col + ' instanceof Date'
//                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
//                return sdf.format(col)
//            }
            else {
                col = col.toString()
            }

            return col.trim()
        }.join( BaseDetailsExport.CSV_FIELD_SEPARATOR )
    }

    static List<String> buildRowPDF(List<Object> row) {

        row.collect{ col ->
            if (! col) {
                return ''
            }
            if (col instanceof String) {
                // ..
            }
//            else if (col instanceof Date) {
//                println '!?? >>>>>>>>>>>>>>>>>>>>>>>>>>>> QueryExportManager.buildRowPDF() ' + col + ' instanceof Date'
//                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
//                return sdf.format(col)
//            }
            else {
                col = col.toString()
            }
            return col.trim()
        }
    }

    static Workbook buildXLSX(BaseQueryExport export) {

        Map<String, Object> data = export.getQueriedData()

        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet( export.token )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        data.rows.eachWithIndex { row, idx ->

            Row entry = sheet.createRow(idx+1)
            row.eachWithIndex{ v, i ->

                Cell cell = BaseExportHelper.updateCell(workbook, entry.createCell(i), v)
                sheet.autoSizeColumn(i)
            }
        }

        Row header = sheet.createRow(0)

        data.cols.eachWithIndex{ col, idx ->
            Cell headerCell = header.createCell(idx)
            headerCell.setCellStyle(cellStyle)
            headerCell.setCellValue(col as String)
            sheet.autoSizeColumn(idx)
        }

        workbook
    }
}
