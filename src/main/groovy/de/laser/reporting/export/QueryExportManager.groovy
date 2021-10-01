package de.laser.reporting.export

import de.laser.reporting.export.base.BaseExport
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.myInstitution.base.BaseConfig
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
        Map<String, Object> data = export.getData()

        println '-------------------------'
        println export
        println data

        if (format == 'csv') {
            rows.add( data.cols.join( BaseExport.CSV_FIELD_SEPARATOR ) )
            data.rows.each { row ->
                rows.add( buildRowCSV( row ) )
            }
        }
        else if (format == 'pdf') {
            rows.add( data.cols )
            data.rows.each { row ->
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

    static String buildRowCSV(List<String> content) {

        content.collect{ it ->
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

    static List<String> buildRowPDF(List<String> content) {

        content.collect{ it ->
            if (! it) {
                return ''
            }
            return it.trim()
        }
    }

    static Workbook buildXLSX(BaseQueryExport export) {

        Map<String, Object> data = export.getData()

        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet( export.token )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        data.rows.eachWithIndex { row, idx ->

            Row entry = sheet.createRow(idx+1)
            row.eachWithIndex{ str, i ->
                Cell cell = entry.createCell(i)
                cell.setCellStyle(cellStyle)

                if (str == null) {
                    cell.setCellValue('')
                }
                else {
                    cell.setCellValue( str.trim() )
                }
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
