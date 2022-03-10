package de.laser.reporting.export.myInstitution

import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class GlobalQueryExport extends BaseQueryExport {

    String token

    GlobalQueryExport(String token) {
        this.token = token
    }

    @Override
    Map<String, Object> getQueriedData() {
        Map<String, Object> queryCache = GlobalExportHelper.getQueryCache( token )

        Map<String, Object> result = [
                cols: [ queryCache.labels.tooltip ],
                rows: []
        ]
        List<Map<String, Object>> data = queryCache.dataDetails as List
        List<String> chart = queryCache.labels.chart

        if ( ! chart) {
            MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')

            result.cols.add( messageSource.getMessage('default.count.label', null, LocaleContextHolder.getLocale()) )
            result.rows = data.collect{ e ->
                [e.label.toString(), e.idList.size()]
            }
        }
        else {
            result.cols.addAll( chart )
            result.rows = data.collect{ e ->
                if (e.keySet().contains('value3')) {
                    [e.label.toString(), e.value1, e.value2, e.value3] // changed order - properties - TODO
                }
                else {
                    if (queryCache.tmpl == '/myInstitution/reporting/chart/generic_signOrphaned') {  // unchanged order - TODO
                        [e.label.toString(), e.value1, e.value2]
                    }
                    else {
                        [e.label.toString(), e.value2, e.value1] // changed order - default - TODO
                    }
                }
            }
        }
        result
    }
}
