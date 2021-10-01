package de.laser.reporting.export.local

import de.laser.reporting.export.base.BaseQueryExport
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

class QueryExport extends BaseQueryExport {

    String token

    QueryExport(String token) {
        this.token = token
    }

    Map<String, Object> getData() {

        Map<String, Object> queryCache = ExportLocalHelper.getQueryCache( token )

        Map<String, Object> result = [
                cols: [],
                rows: []
        ]
        List<Map<String, Object>> dd = queryCache.dataDetails as List<Map>
        List<String> chart = queryCache.labels.chart

        // todo --
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')

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
                entry.add((e.vnc ?: '').toString())
                entry.add((e.vnct ?: '').toString())
                entry.add((e.vc ?: '').toString())
                entry.add((e.vct ?: '').toString())
            }
            else {
                if (e.containsKey('minusIdList')) {
                    entry.add(e.minusIdList.size().toString())
                }
                if (e.containsKey('plusIdList')) {
                    entry.add(e.plusIdList.size().toString())
                }
                if (e.containsKey('idList')) {
                    entry.add(e.idList.size().toString())
                }
            }
            entry
        }

        result
    }
}
