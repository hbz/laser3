package de.laser.reporting.export.local

import de.laser.helper.BeanStore
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseQueryExport
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class LocalQueryExport extends BaseQueryExport {

    String token

    LocalQueryExport(String token) {
        this.token = token
    }

    @Override
    Map<String, Object> getQueriedData() {

        Map<String, Object> queryCache = LocalExportHelper.getQueryCache( token )

        Map<String, Object> result = [
                cols: [],
                rows: []
        ]
        List<Map<String, Object>> dd = queryCache.dataDetails as List<Map>
        List<String> chart = queryCache.labels.chart

        // todo --
        MessageSource messageSource = BeanStore.getMessageSource()

        if ( queryCache.labels.tooltip ) {
            result.cols.add( queryCache.labels.tooltip ) // simple
        }
        else {
            result.cols.add( messageSource.getMessage('default.period.label', null, LocaleContextHolder.getLocale()) )
        }
        if ( ! chart) {
            result.cols.add( messageSource.getMessage('default.count.label', null, LocaleContextHolder.getLocale()) ) // simple
        }
        else {
            result.cols.addAll( chart )
        }
        // -- todo

        result.rows = dd.collect{ Map e ->
            List entry = [e.label.toString()]
            if (queryCache.query == 'timeline-cost') { // -- todo // -- todo // -- todo
                entry.add((e.vnc ?: ''))
                entry.add((e.vnct ?: ''))
                entry.add((e.vc ?: ''))
                entry.add((e.vct ?: ''))
            }
            else {
                if (e.containsKey('minusIdList')) {
                    entry.add(e.minusIdList.size())
                }
                if (e.containsKey('plusIdList')) {
                    entry.add(e.plusIdList.size())
                }
                if (e.containsKey('idList')) {
                    entry.add(e.idList.size())
                }
            }
            entry
        }

        result
    }
}
