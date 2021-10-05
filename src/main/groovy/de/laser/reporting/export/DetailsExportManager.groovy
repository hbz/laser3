package de.laser.reporting.export

import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.helper.DateUtils
import de.laser.reporting.export.base.BaseExport
import de.laser.reporting.export.local.ExportLocalHelper
import de.laser.reporting.export.local.IssueEntitlementExport
import de.laser.reporting.export.myInstitution.ExportGlobalHelper
import de.laser.reporting.export.myInstitution.LicenseExport
import de.laser.reporting.export.myInstitution.OrgExport
import de.laser.reporting.export.myInstitution.SubscriptionExport
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
            List<String> row = export.getObjectResult( obj, fields ).collect{ it ->
                if (it instanceof Date) {
                    SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                    return sdf.format(it)
                }
                return it as String
            } // TODO date, double, etc

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

        objList.eachWithIndex { obj, idx ->

            List<Object> values = export.getObjectResult( obj, fields )
            if (values) {
                Row entry = sheet.createRow(idx+1)
                values.eachWithIndex{ v, i ->
                    Cell cell = entry.createCell(i)
                    cell.setCellStyle(cellStyle)

                    if (v == null) {
                        cell.setCellValue('')
                    }
                    else {
                        if (v instanceof String) {
                            if (v.contains(BaseExport.CSV_VALUE_SEPARATOR)) {
                                cell.setCellStyle(wrapStyle)
                                cell.setCellValue(v.split(BaseExport.CSV_VALUE_SEPARATOR).collect { it.trim() }.join('\r\n'))
                            }
                            else {
                                cell.setCellValue(v.trim())
                            }
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
                            cell.setCellValue(v) // raw
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
            List<String> row = export.getObjectResult( obj, fields ).collect{ it ->
                if (it instanceof Date) {
                    SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                    return sdf.format(it)
                }
                return it as String
            } // TODO date, double, etc

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
        result
    }
}
