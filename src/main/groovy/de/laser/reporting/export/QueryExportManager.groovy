package de.laser.reporting.export

import de.laser.reporting.export.base.BaseExport
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.reporting.export.local.ExportLocalHelper
import de.laser.reporting.export.myInstitution.ExportGlobalHelper
import de.laser.reporting.myInstitution.base.BaseConfig

class QueryExportManager {

    static BaseQueryExport createExport(String token, String context) {
        if (context == BaseConfig.KEY_MYINST) {
            new de.laser.reporting.export.myInstitution.QueryExport( token )
        }
        else if (context in [ BaseConfig.KEY_SUBSCRIPTION ]) {
            new de.laser.reporting.export.local.QueryExport( token )
        }
    }

    static List export(BaseQueryExport export, String format) {

        List rows = []
        Map<String, Object> data = export.getData()

        if (format == 'csv') {
            rows.add( data.cols.join( BaseExport.CSV_FIELD_SEPARATOR ) )
            data.rows.each { row ->
                rows.add( buildCsvRow( row ) )
            }
        }
        else if (format == 'pdf') {
            rows.add( data.cols )
            data.rows.each { row ->
                rows.add( buildPdfRow( row ) )
            }
        }
        rows
    }

    static String buildCsvRow(List<String> content) {

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

    static List<String> buildPdfRow(List<String> content) {

        content.collect{ it ->
            if (! it) {
                return ''
            }
            return it.trim()
        }
    }
}
