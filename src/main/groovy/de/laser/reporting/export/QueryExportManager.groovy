package de.laser.reporting.export

class QueryExportManager {

    static QueryExport createExport(String token) {
        return new QueryExport( token )
    }

    static List export(QueryExport export, String format) {

        List rows = []

        if (format == 'csv') {
            export.getDataDetails().each { dd ->
                rows.add( buildCsvRow( dd ) )
            }
        }
        else if (format == 'pdf') {
            export.getDataDetails().each { dd ->
                rows.add( buildPdfRow( dd ) )
            }
        }

        rows
    }

    static String buildCsvRow(Map<String, Object> content) {

        [ content.label, content.idList.size().toString() ].collect{ it ->
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

    static List<String> buildPdfRow(Map<String, Object> content) {

        [ content.label, content.idList.size().toString() ].collect{ it ->
            if (! it) {
                return ''
            }
            return it.trim() // ????
        }
    }
}
