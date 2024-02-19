package de.laser.reporting.export

import de.laser.storage.BeanStore
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.export.local.LocalQueryExport
import de.laser.reporting.export.myInstitution.GlobalQueryExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.taglib.ApplicationTagLib

/**
 * This class is responsible for creating the export of the assembled report
 */
class QueryExportManager {

    /**
     * Starts the exporting process, depending on the context
     * @param token the report to be exported
     * @param context the context – global or local – to be exported
     * @return the preprocessed data in one of {@link GlobalQueryExport} or {@link LocalQueryExport}
     */
    static BaseQueryExport createExport(String token, String context) {
        if (context == BaseConfig.KEY_MYINST) {
            new GlobalQueryExport( token )
        }
        else if (context in [ BaseConfig.KEY_SUBSCRIPTION ]) {
            new LocalQueryExport( token )
        }
    }

    /**
     * Renders the preprocessed data as a list
     * @param export the data to be exported
     * @param format the export format (one of csv or pdf) to be rendered
     * @return a list of rows
     */
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

    /**
     * Renders the preprocessed data as an Excel workbook
     * @param export the data to be exported
     * @param format the export format ('xlsx')
     * @return an Excel workbook containing the data
     * @see Workbook
     */
    static Workbook exportAsWorkbook(BaseQueryExport export, String format) {

        if (format == 'xlsx') {
            buildXLSX(export)
        }
    }

    /**
     * Builds a single row in character-separated value (CSV) format
     * @param row the raw data row
     * @return the concatenated row, character-separated
     */
    static String buildRowCSV(List<Object> row) {
        ApplicationTagLib g = BeanStore.getApplicationTagLib()

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
            else if (col instanceof Double) {
                return BaseDetailsExport.CSV_FIELD_QUOTATION + g.formatNumber( number: col, type: 'currency',  currencySymbol: '' ).trim() + BaseDetailsExport.CSV_FIELD_QUOTATION
            }
            else {
                col = col.toString()
            }

            return col.trim()
        }.join( BaseDetailsExport.CSV_FIELD_SEPARATOR )
    }

    /**
     * Builds a single row for a PDF table
     * @param row the raw data row
     * @return the parsed PDF row
     */
    static List<String> buildRowPDF(List<Object> row) {
        ApplicationTagLib g = BeanStore.getApplicationTagLib()

        row.collect{ col ->
            if (! col) {
                return ''
            }
            if (col instanceof String) {
                // ..
            }
            else if (col instanceof Double) {
                col = g.formatNumber( number: col, type: 'currency',  currencySymbol: '' ).trim()
            }
            else {
                col = col.toString()
            }
            return col.trim()
        }
    }

    /**
     * Builds the Excel worksheet from the given data
     * @param export the preprocessed data to be exported
     * @return an Excel {@link Workbook} containing the export data
     */
    static Workbook buildXLSX(BaseQueryExport export) {

        Map<String, Object> data = export.getQueriedData()

        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet( export.token )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        data.rows.eachWithIndex { row, idx ->

            Row entry = sheet.createRow(idx+1)
            row.eachWithIndex{ v, i ->
                int height = BaseExportHelper.updateCell(workbook, entry.createCell(i), v, false, false)
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
