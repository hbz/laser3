package de.laser.reporting.export.myInstitution

import de.laser.reporting.export.base.BaseQueryExport

class QueryExport extends BaseQueryExport {

    String token

    QueryExport(String token) {
        this.token = token
    }

    Map<String, Object> getData() {

        Map<String, Object> queryCache = ExportGlobalHelper.getQueryCache( token )

        Map<String, Object> result = [
                cols: [ queryCache.labels.tooltip ],
                rows: []
        ]
        List<Map<String, Object>> data = queryCache.dataDetails as List
        List<String> chart = queryCache.labels.chart

        if ( ! chart) {
            result.cols.add( 'Anzahl' )
            result.rows = data.collect{ e ->
                [e.label.toString(), e.idList.size().toString()]
            }
        }
        else {
            result.cols.addAll( chart )
            result.rows = data.collect{ e ->
                [e.label.toString(), e.value2.toString(), e.value1.toString()] // changed order
            }
        }
        result
    }
}
