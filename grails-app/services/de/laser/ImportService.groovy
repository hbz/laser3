package de.laser

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import grails.gorm.transactions.Transactional
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.web.multipart.MultipartFile

@Transactional
class ImportService {

    static final List<Map<String, String>> CSV_CHARS = [[charKey: ',', name: 'default.import.csv.comma'], [charKey: ';', name: 'default.import.csv.semicolon'], [charKey: '\t', name: 'default.import.csv.tab'], [charKey: '|', name: 'default.import.csv.pipe']]

    Map<String, Object> readCsvFile(MultipartFile csvFile, String encoding, char separator, boolean ignoreHeader = false) {
        InputStream fileContent = csvFile.getInputStream()
        List<String> headerRow = []
        List<List<String>> rows = []
        fileContent.withReader(encoding) { Reader reader ->
            ICSVParser csvp = new CSVParserBuilder().withSeparator(separator).build()
            CSVReader csvr = new CSVReaderBuilder(reader).withCSVParser(csvp).build()
            String[] readLine
            int l = 0
            while (readLine = csvr.readNext()) {
                List<String> line = []
                if(readLine[0]) {
                    if(l == 0 && !ignoreHeader) {
                        readLine.each { String s ->
                            String headerCol = s.trim()
                            //strip BOM
                            if(headerCol.startsWith("\uFEFF"))
                                headerCol = headerCol.substring(1)
                            headerRow << headerCol
                        }
                    }
                    else {
                        line.addAll(readLine.collect { String col -> col.trim() })
                        rows << line
                    }
                }
                l++
            }
        }
        //should replace
        //List<String> rows = fileContent.getText(encoding).split('\n')
        //List<String> headerRow = rows.remove(0).split('\t')
        [headerRow: headerRow, rows: rows]
    }

    Map<String, Object> readExcelFile(MultipartFile excelFile, boolean ignoreHeader = false) {
        //continue here with testing
        List<String> headerRow = []
        List<List<String>> rows = []
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream())
        XSSFSheet sheet = workbook.getSheetAt(0)
        boolean headerFlag = true
        int headerSize = sheet.getRow(0).getLastCellNum()
        if(ignoreHeader)
            headerFlag = false
        else {
            for(Cell cell in sheet.getRow(0).cellIterator()) {
                headerRow << cell.stringCellValue
            }
        }
        for(Row row in sheet.rowIterator()) {
            if(headerFlag) {
                headerFlag = false
                continue
            }
            def value
            List readRow = []
            for(int c = 0; c < headerSize; c++) {
                Cell cell = row.getCell(c)
                //log.debug(cell.getCellTypeEnum().toString())
                if(!cell || cell.getCellTypeEnum() == CellType.BLANK)
                    value = ""
                else {
                    switch(cell.getCellTypeEnum()) {
                        case CellType.NUMERIC: value = cell.numericCellValue.toDouble()
                            break
                        default: value = cell.stringCellValue
                            break
                    }
                }
                readRow << value
            }
            rows << readRow
        }
        [headerRow: headerRow, rows: rows]
    }
}
