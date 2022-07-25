package de.laser.reporting.export.base

import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.CreationHelper
import org.apache.poi.ss.usermodel.VerticalAlignment
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFHyperlink
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat
import java.time.Year

class BaseExportHelper {

    static final Map<String, List> PDF_OPTIONS = [
            'auto' : [ 'auto' ],
            'A4-P' : [ 'A4', 'Portrait' ], 'A4-L' : [ 'A4', 'Landscape' ],
            'A3-P' : [ 'A3', 'Portrait' ], 'A3-L' : [ 'A3', 'Landscape' ],
            'A2-P' : [ 'A2', 'Portrait' ], 'A2-L' : [ 'A2', 'Landscape' ],
            'A1-P' : [ 'A1', 'Portrait' ], 'A1-L' : [ 'A1', 'Landscape' ],
            'A0-P' : [ 'A0', 'Portrait' ], 'A0-L' : [ 'A0', 'Landscape' ]
    ]

    static int updateCell(Workbook workbook, Cell cell, def value, boolean inserNewLines, boolean useHyperlinks) {

        int lineCount = 1

        CreationHelper createHelper = workbook.getCreationHelper()
        Locale locale = LocaleContextHolder.getLocale()
        MessageSource messageSource = BeanStore.getMessageSource()

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

        cell.setCellStyle(cellStyle)

        // println 'BEH.updateCell() --> ' + value + ' ' + value?.class

        if (value == null) {
            cell.setCellValue('')
        }
        else {
            if (value instanceof String) {
                if (inserNewLines) {
                    cell.setCellStyle(wrapStyle)
                    value = value.split( BaseDetailsExport.CSV_VALUE_SEPARATOR )
                    lineCount = value.size()
                    value = value.join('\n')
                }
                if (value.startsWith('http://') || value.startsWith('https://')) {
                    List<String> parts = value.split('@') // masking globalUID and gokbId
                    value = parts[0]

                    if (useHyperlinks) {
                        XSSFHyperlink link = new XSSFHyperlink( XSSFHyperlink.LINK_URL ) // TODO
                        link.setAddress(parts[0])
                        value = parts[0]

                        if (parts.size()>1) {
                            value = parts[1]
                        }
                        cell.setHyperlink(link)
                    }
                }
                cell.setCellValue(value.trim())
            }
            else if (value instanceof Boolean) {
                cell.setCellValue(value ? '1' : '0')
            }
            else if (value instanceof Date) {
                cell.setCellStyle(dateStyle)
                cell.setCellValue(value)
            }
            else if (value instanceof Double) {
                cell.setCellStyle(currStyle)
                cell.setCellValue(value)
            }
            else if (value instanceof Integer) {
                cell.setCellValue(value)
            }
            else if (value instanceof Long) {
                if (value > Integer.MAX_VALUE) {
                    cell.setCellValue(value.toString())
                } else {
                    cell.setCellValue(value.toInteger())
                }
            }
            else if (value instanceof Year) {
                cell.setCellValue(value.getValue())
            }
            else {
                cell.setCellValue(value.toString())
            }
        }

        lineCount
    }

    static String getFileName(List<String> labels = ['Reporting']) {

        SimpleDateFormat sdf = DateUtils.getSDF_forFilename()

        String filename = sdf.format(new Date()) + '_' + labels.collect{
            it.replaceAll('[→/]', '-')
                    .replaceAll('[^\\wäöüÄÖÜ!"§$%&()=?\'{},.\\-+~#;:]', '')
                    .replaceAll(' ', '')
        }.join('_')

        filename
    }

    static def reorderFieldsForUI(Map<String, Object> formFields) {

        Map<String, Object> result = [:]
        List<Integer> reorder = []
        int max = formFields.keySet().size()

        for (int i=0; i<max; i++) {
            if (i%2==0) {
                reorder[i] = ( i - i/2 ) as Integer
            } else {
                reorder[i] = Math.round(Math.floor(i/2 + max/2)) as Integer
            }
        }
        reorder.each {i ->
            String key = formFields.keySet()[i]
            result.putAt(key, formFields.get(key))
        }

        result
    }

    // -----

    static Map<String, Object> calculatePdfPageStruct(List<List<String>> content, String pin) {

        Map<String, Object> struct = [
                width       : [],
                height      : [],
                pageSize    : '',
                orientation : 'Portrait'
        ]

        if (pin == 'chartDetailsExport') {

            content.eachWithIndex { List row, int i ->
                row.eachWithIndex { List cell, int j ->
                    if (!struct.height[i] || struct.height[i] < cell.size()) {
                        struct.height[i] = cell.size()
                    }
                    cell.eachWithIndex { String entry, int k ->
                        if (i == 0) {
                            struct.width[j] = entry.length() < 15 ? 15 : entry.length() > 35 ? 35 : entry.length()
                        }
                        else {
                            if (!struct.width[j] || struct.width[j] < entry.length()) {
                                struct.width[j] = entry.length()
                            }
                        }
                    }
                }
            }
        }
        else if (pin == 'chartQueryExport') {

            content.eachWithIndex { List row, int i ->
                row.eachWithIndex { String cell, int j ->
                    struct.height[i] = 1
                    struct.width[j] = cell.length() < 15 ? 15 : cell.length() > 35 ? 35 : cell.length()
                }
            }
        }
        else if (pin == 'chartQueryExport-image') {

            // TODO
            // TODO
        }
        else {
            println ' ----- TODO: calculatePdfPageStruct( ' + pin + ' ) ----- '
        }

        String[] sizes = [ 'A0', 'A1', 'A2', 'A3', 'A4' ]
        int pageSize = 4

        int wx = 85, w = struct.width.sum() as int
        int hx = 35, h = struct.height.sum() as int

        if (w > wx*4)       { pageSize = 0 }
        else if (w > wx*3)  { pageSize = 1 }
        else if (w > wx*2)  { pageSize = 2 }
        else if (w > wx)    { pageSize = 3 }

        struct.whr = (w * 0.75) / (h + 15)
        if (struct.whr > 5) {
            if (w < wx*7) {
                if (pageSize < sizes.length - 1) {
                    pageSize++
                }
            }
            struct.orientation = 'Landscape'
        }

        struct.width = struct.width.sum()
        struct.height = struct.height.sum()
        struct.pageSize = sizes[ pageSize ]

        struct
    }
}
