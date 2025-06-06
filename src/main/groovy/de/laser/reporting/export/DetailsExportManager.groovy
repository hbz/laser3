package de.laser.reporting.export

import de.laser.IssueEntitlement
import de.laser.License
import de.laser.Org
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.wekb.Vendor
import de.laser.finance.CostItem
import de.laser.reporting.export.myInstitution.ProviderExport
import de.laser.reporting.export.myInstitution.VendorExport
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.reporting.export.base.BaseDetailsExport
import de.laser.reporting.export.base.BaseExportHelper
import de.laser.reporting.export.local.CostItemExport
import de.laser.reporting.export.local.IssueEntitlementExport
import de.laser.reporting.export.myInstitution.LicenseExport
import de.laser.reporting.export.myInstitution.OrgExport
import de.laser.reporting.export.myInstitution.PackageExport
import de.laser.reporting.export.myInstitution.PlatformExport
import de.laser.reporting.export.myInstitution.SubscriptionExport
import de.laser.reporting.report.myInstitution.base.BaseConfig
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

/**
 * Central class to manage report exports. It delegates also between local (= object-bound) and institution-wide exports
 * and takes care of the output into a file
 */
class DetailsExportManager {

    /**
     * Initialises a new export and handles the creation to the respective processing class
     * @param token which kind of report is being generated? The token is used for report identification and cache storage
     * @param context the switch between the global (= institution-wide) and local (= object-departing) report
     * @return the generated export; the result of either {@link #createGlobalExport(java.lang.String, java.util.Map)} or {@link #createLocalExport(java.lang.String, java.util.Map)}
     */
    static BaseDetailsExport createExport(String token, String context) {
        if (context == BaseConfig.KEY_MYINST) {
            createGlobalExport(token, [:])
        }
        else if (context in [ BaseConfig.KEY_SUBSCRIPTION ]) {
            createLocalExport(token, [:])
        }
    }

    /**
     * Initialises a new global (= institution-wide) export with the selected fields
     * @param token the type of report to export; later used as cache storage token
     * @param selectedFields the {@link Map} of fields which have been selected to be included in the report
     * @return the new global report export
     */
    static BaseDetailsExport createGlobalExport(String token, Map<String, Object> selectedFields) {
        GlobalExportHelper.createExport( token, selectedFields )
    }

    /**
     * Initialises a new local (= object-bound) export with the selected fields
     * @param token the type of report to export; later used as cache storage token
     * @param selectedFields the {@link Map} of fields which have been selected to be included in the report
     * @return the new local report export
     */
    static BaseDetailsExport createLocalExport(String token, Map<String, Object> selectedFields) {
        LocalExportHelper.createExport( token, selectedFields )
    }

    /**
     * Outputs the given export into a list; this is being used for PDF or CSV file outputs
     * @param export the export data to be processed
     * @param idList the list of object database IDs which are subject of export
     * @param format the desired output file format
     * @param options output configuration settings
     * @return a {@link List} containing the rows of entries for output
     */
    static List exportAsList(BaseDetailsExport export, List<Long> idList, String format, Map<String, Boolean> options) {

        List rows = []

        List objList = resolveObjectList( export, idList )
        Map<String, Object> fields = export.getSelectedFields()
        List<String> cols = fields.collect{it -> export.getFieldLabel(it.key as String) }

        if (format == 'csv') {
            List<List<String>> csv
            List<Integer> ici
            (csv, ici) = buildCSV(export, objList, fields)

            if (options.hideEmptyResults) {
                ici.each { i -> /* println 'Export CSV ignored: ' + cols[i]; */ cols.removeAt(i) }
            }
            rows.add( buildRowAsCSV( cols ).join( BaseDetailsExport.CSV_FIELD_SEPARATOR ) )

            csv.each { row ->
                if (options.hideEmptyResults) {
                    ici.each { i -> row.removeAt(i) }
                }
                rows.add( row.join( BaseDetailsExport.CSV_FIELD_SEPARATOR ) )
            }
        }
        else if (format == 'pdf') {
            List<List<List<String>>> pdf
            List<Integer> ici
            (pdf, ici) = buildPDF(export, objList, fields)

            if (options.hideEmptyResults) {
                ici.each { i -> /* println 'Export PDF ignored: ' + cols[i]; */ cols.removeAt(i) }
            }
            rows.add( buildRowAsPDF(cols) )

            pdf.each { row ->
                if (options.hideEmptyResults) {
                    ici.each { i -> row.removeAt(i) }
                }
                rows.add( row )
            }
        }
        rows
    }

    /**
     * Outputs the given export into an Excel workbook
     * @param export the export data to be processed
     * @param idList the list of object database IDs which are subject of export
     * @param format the desired output file format (only xlsx is being supported)
     * @param options output configuration settings
     * @return a {@link Workbook}
     */
    static Workbook exportAsWorkbook(BaseDetailsExport export, List<Long> idList, String format, Map<String, Boolean> options) {

        List objList = resolveObjectList( export, idList )

        if (format == 'xlsx') {
            buildXLSX(export, objList, export.getSelectedFields(), options)
        }
    }

    /**
     * Processes the export so that data is being formatted CSV-compatibly
     * @param export the export data to process
     * @param objList the list of database objects to be exported
     * @param fields the fields which should be included in the report
     * @return a {@link List} containing the formatted data
     */
    static List buildCSV(BaseDetailsExport export, List objList, Map<String, Object> fields) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()

        List<List<String>> rows = []
        List<Integer> ici = []

        Integer[] cc = new Integer[fields.size()].collect{ 0 }

        objList.each{ obj ->
            List<String> row = export.getDetailedObject( obj, fields ).collect{ it ->
                if (it instanceof Date) {
                    SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                    return sdf.format(it)
                }
                else if (it instanceof Double) {
                    return g.formatNumber( number: it, type: 'currency',  currencySymbol: '' ).trim()
                }
                else if (it instanceof String && (it.startsWith('http://') || it.startsWith('https://'))) {
                    // masking globalUID and gokbId
                    if (it.indexOf('@') > 0) {
                        it = it.split('@')[0]
                    }
                }
                return it as String
            } // TODO date, double, etc

            if (row) {
                List<String> cols = buildRowAsCSV( row )
                cols.eachWithIndex{ c, i -> if (c) { cc[i]++ } }
                rows.add( cols )
            }
        }
        cc.eachWithIndex{ c, i -> if (c == 0) { ici.add(i) } }

        [rows, ici.reverse()]
    }

    /**
     * Helper method to escape lists of strings
     * @param content the list to be formatted
     * @return a {@link List} containing the escaped list
     */
    static List<String> buildRowAsCSV(List<String> content) {

        content.collect{it ->
            boolean enclose = false
            if (! it) {
                return ''
            }
            if (it.contains( BaseDetailsExport.CSV_FIELD_QUOTATION )) {
                it = it.replaceAll( BaseDetailsExport.CSV_FIELD_QUOTATION , BaseDetailsExport.CSV_FIELD_QUOTATION + BaseDetailsExport.CSV_FIELD_QUOTATION) // !
                enclose = true
            }
            if (enclose || it.contains( BaseDetailsExport.CSV_FIELD_SEPARATOR )) {
                return BaseDetailsExport.CSV_FIELD_QUOTATION + it.trim() + BaseDetailsExport.CSV_FIELD_QUOTATION
            }
            return it.trim()
        }
    }

    /**
     * Processes the export so that data is being formatted for Excel cells
     * @param export the export data to process
     * @param objList the list of database objects to be exported
     * @param fields the fields which should be included in the report
     * @param options configuration settings for data output
     * @return a {@link Workbook} containing the processed data
     */
    static Workbook buildXLSX(BaseDetailsExport export, List objList, Map<String, Object> fields, Map<String, Boolean> options) {

        Workbook workbook = new XSSFWorkbook()
        Sheet sheet = workbook.createSheet( export.token )

        CellStyle cellStyle = workbook.createCellStyle()
        cellStyle.setVerticalAlignment( VerticalAlignment.CENTER )

        List<List<Object>> rows = []
        List<Integer> ici = []
        Integer[] cc = new Integer[fields.size()].collect{ 0 }

        objList.each{ obj ->
            List<Object> row = export.getDetailedObject(obj, fields)
            if (row) {
                rows.add( row )
                row.eachWithIndex{ col, i -> if (col) { cc[i]++ } }
            }
        }
        cc.eachWithIndex{ c, i -> if (c == 0) { ici.add(i) } }
        ici = ici.reverse()

        rows.eachWithIndex { row, idx ->
            if (options.hideEmptyResults) {
                ici.each { i -> row.removeAt(i) }
            }
            if (row) {
                Row entry = sheet.createRow(idx + 1)
                int cellHeight = 1
                row.eachWithIndex { val, i ->
                    int h = BaseExportHelper.updateCell(workbook, entry.createCell(i), val, options.insertNewLines, options.useHyperlinks)
                    cellHeight = h > cellHeight ? h : cellHeight
                }
                if (cellHeight > 1) {
                    entry.setHeight((short) (cellHeight * 0.8 * entry.getHeight()))
                }
            }
        }

        Row header = sheet.createRow(0)

        List<String> cols = fields.collect{it -> export.getFieldLabel(it.key as String) }
        if (options.hideEmptyResults) {
            ici.each { i -> /* println 'Export XLSX ignored: ' + cols[i]; */ cols.remove(i) }
        }

        cols.eachWithIndex{ col, idx ->
            Cell headerCell = header.createCell(idx)
            headerCell.setCellStyle(cellStyle)
            headerCell.setCellValue(col)
            sheet.autoSizeColumn(idx)
        }

        workbook
    }

    /**
     * Processes the export so that data is being formatted for a PDF map
     * @param export the export data to process
     * @param objList the list of database objects to be exported
     * @param fields the fields which should be included in the report
     * @return a {@link List} containing the formatted data
     */
    static List buildPDF(BaseDetailsExport export, List objList, Map<String, Object> fields) {

        ApplicationTagLib g = BeanStore.getApplicationTagLib()

        List<List<List<String>>> rows = []
        List<Integer> ici = []

        Integer[] cc = new Integer[fields.size()].collect{ 0 }

        objList.each{ obj ->
            List<String> row = export.getDetailedObject( obj, fields ).collect{ it ->
                if (it instanceof Date) {
                    SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                    return sdf.format(it)
                }
                else if (it instanceof Double) {
                    return g.formatNumber( number: it, type: 'currency',  currencySymbol: '' ).trim()
                }
                return it as String
            } // TODO date, double, etc

            if (row) {
                List<List<String>> cols = buildRowAsPDF( row )
                cols.eachWithIndex{ c, i -> if (c.first()) { cc[i]++ } }
                rows.add( cols )
            }
        }
        cc.eachWithIndex{ c, i -> if (c == 0) { ici.add(i) } }

        [rows, ici.reverse()]
    }

    /**
     * Formats the given string list into a PDF row
     * @param content the list to format
     * @return the list of formatted list entries
     */
    static List<List<String>> buildRowAsPDF(List<String> content) {

        content.collect{it ->
            if (it == null) {
                return ['']
            }
            return it.split(BaseDetailsExport.CSV_VALUE_SEPARATOR).collect{ it.trim() }
        }
    }

    /**
     * Resolves the given list of database identifiers and returns the matching objects
     * @param export the report for which the objects should be retrieved
     * @param idList the list of database identifiers requested for the report
     * @return a {@link List} of objects, depending on the KEY of the given export
     */
    static List<Object> resolveObjectList(BaseDetailsExport export, List<Long> idList) {

        List<Object> result = []

        if (export.KEY == CostItemExport.KEY) {
//            Long subId = LocalExportHelper.getDetailsCache( export.token ).id
//            result = CostItem.executeQuery(
//                    'select ci from CostItem ci where ci.sub.id = :subId and ci.id in (:idList) order by ci.id',
//                    [subId: subId, idList: idList]
//            )
            result = CostItem.executeQuery('select ci from CostItem ci where ci.id in (:idList) order by ci.id', [idList: idList])
        }
        else if (export.KEY == IssueEntitlementExport.KEY) {
            Long subId = LocalExportHelper.getDetailsCache( export.token ).id
            result = IssueEntitlement.executeQuery(
                    'select ie from IssueEntitlement ie where ie.subscription.id = :subId and ie.tipp.id in (:idList) order by ie.tipp.sortname',
                    [subId: subId, idList: idList]
            )
        }
        else if (export.KEY == LicenseExport.KEY) {
            result = License.executeQuery('select l from License l where l.id in (:idList) order by l.reference', [idList: idList])
        }
        else if (export.KEY == OrgExport.KEY) {
            result = Org.executeQuery('select o from Org o where o.id in (:idList) order by o.sortname, o.name', [idList: idList])
        }
        else if (export.KEY == PackageExport.KEY) {
            result = de.laser.wekb.Package.executeQuery('select p from Package p where p.id in (:idList) order by p.name', [idList: idList])
        }
        else if (export.KEY == PlatformExport.KEY) {
            result = Platform.executeQuery('select p from Platform p where p.id in (:idList) order by p.name', [idList: idList])
        }
        else if (export.KEY == ProviderExport.KEY) {
            result = Provider.executeQuery('select p from Provider p where p.id in (:idList) order by p.name', [idList: idList])
        }
        else if (export.KEY == SubscriptionExport.KEY) {
            result = Subscription.executeQuery('select s from Subscription s where s.id in (:idList) order by s.name', [idList: idList])
        }
        else if (export.KEY == VendorExport.KEY) {
            result = Vendor.executeQuery('select v from Vendor v where v.id in (:idList) order by v.name', [idList: idList])
        }

        result
    }
}
