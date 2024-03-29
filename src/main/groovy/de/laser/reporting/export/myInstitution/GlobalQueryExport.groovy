package de.laser.reporting.export.myInstitution

import de.laser.storage.BeanStore
import de.laser.reporting.export.GlobalExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import de.laser.utils.LocaleUtils
import org.springframework.context.MessageSource

/**
 * Exports the reports generated for the institution-wide level
 */
class GlobalQueryExport extends BaseQueryExport {

    String token

    /**
     * Constructor call to instantiate the cache token for the given query
     * @param token the token under which the report is going to be stored in the cache
     */
    GlobalQueryExport(String token) {
        this.token = token
    }

    /**
     * Retrieves the queried report data from the cache
     * @return a {@link Map} containing the report headers with the respective data
     */
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
            MessageSource messageSource = BeanStore.getMessageSource()

            result.cols.add( messageSource.getMessage('default.count.label', null, LocaleUtils.getCurrentLocale()) )
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
