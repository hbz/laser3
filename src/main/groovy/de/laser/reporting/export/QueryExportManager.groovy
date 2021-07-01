package de.laser.reporting.export

import de.laser.reporting.export.myInstitution.QueryExport

class QueryExportManager {

    static QueryExport createExport(String token) {
        return new QueryExport( token )
    }

    static List export(QueryExport export, String format) {

        List rows = []
        Map<String, Object> data = export.getData()

        if (format == 'csv') {
            rows.add( data.cols.join( AbstractExport.CSV_FIELD_SEPARATOR ) )
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
            if (! it) {
                return ''
            }
            if (it.contains( AbstractExport.CSV_FIELD_QUOTATION )) {
                it = it.replaceAll( AbstractExport.CSV_FIELD_QUOTATION , AbstractExport.CSV_FIELD_QUOTATION + AbstractExport.CSV_FIELD_QUOTATION)
            }
            if (it.contains( AbstractExport.CSV_FIELD_SEPARATOR )) {
                return AbstractExport.CSV_FIELD_QUOTATION + it.trim() + AbstractExport.CSV_FIELD_QUOTATION
            }
            return it.trim()
        }.join( AbstractExport.CSV_FIELD_SEPARATOR )
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
