package de.laser.reporting.export

import de.laser.helper.DateUtils
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.myInstitution.base.BaseConfig
import grails.util.Holders
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

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

        println '-------------------------'
        println export
        println data

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
            else if (col instanceof Date) {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                return sdf.format(col)
            }
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
            else if (col instanceof Date) {
                SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                return sdf.format(col)
            }
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

        Locale locale = LocaleContextHolder.getLocale()
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')

        CreationHelper createHelper = workbook.getCreationHelper()
        short dateFormat = createHelper.createDataFormat().getFormat( messageSource.getMessage( DateUtils.DATE_FORMAT_NOTIME, null, locale ) )
        short currFormat = createHelper.createDataFormat().getFormat( messageSource.getMessage( 'default.decimal.format', null, locale ) ) // ? todo check format

        CellStyle wrapStyle = workbook.createCellStyle()
        wrapStyle.setWrapText(true)
        wrapStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        CellStyle dateStyle = workbook.createCellStyle()
        dateStyle.setVerticalAlignment( VerticalAlignment.CENTER )
        dateStyle.setDataFormat( dateFormat )

        CellStyle currStyle = workbook.createCellStyle()
        currStyle.setVerticalAlignment( VerticalAlignment.CENTER )
        currStyle.setDataFormat( currFormat )

        data.rows.eachWithIndex { row, idx ->

            Row entry = sheet.createRow(idx+1)
            row.eachWithIndex{ v, i ->
                Cell cell = entry.createCell(i)
                cell.setCellStyle(cellStyle)

                if (v == null) {
                    cell.setCellValue('')
                }
                else {
                    if (v instanceof String) {
                        cell.setCellValue(v.trim())
                    }
                    else if (v instanceof Date) {
                        cell.setCellStyle(dateStyle)
                        cell.setCellValue(v)
                    }
                    else if (v instanceof Double) {
                        cell.setCellStyle(currStyle)
                        cell.setCellValue(v)
                    }
                    else {
                        cell.setCellValue( v ) // raw
                    }
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
