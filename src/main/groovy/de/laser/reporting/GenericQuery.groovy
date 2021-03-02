package de.laser.reporting

import de.laser.Org
import de.laser.RefdataValue
import grails.web.servlet.mvc.GrailsParameterMap

class GenericQuery {

    static String NO_DATA_LABEL = 'keine Angabe'

    static List<String> getQueryLabels(Map<String, Object> config, GrailsParameterMap params) {

        List<String> meta = []

        config.each {it ->
            it.value.get('query')?.each { it2 ->
                //println it2
                if (it2.value.containsKey(params.query)) {
                    meta = [ it2.key, it2.value.get(params.query), params.label ]
                }
            }
            it.value.get('query2')?.each { it2 ->
                //println it2
                if (it2.value.containsKey(params.query)) {
                    meta = [ it2.key, it2.value.get(params.query), params.label ]
                }
            }
        }
        meta
    }

    static void handleSimpleRefdataQuery(String query, String dataHql, String dataDetailsHql, String nonMatchingHql, List idList, Map<String, Object> result) {

        result.data = Org.executeQuery( dataHql, [idList: idList] )

        result.data.each { d ->
            d[1] = RefdataValue.get(d[0]).getI10n('value').replaceAll("'", '"')

            result.dataDetails.add( [
                    query:  query,
                    id:     d[0],
                    label:  d[1],
                    idList: Org.executeQuery( dataDetailsHql, [idList: idList, d: d[0]] )
            ])
        }
        handleNonMatchingData( query, nonMatchingHql, idList, result )
    }

    static void handleNonMatchingData(String query, String hql, List idList, Map<String, Object> result) {

        List noDataList = Org.executeQuery( hql, [idList: idList] )

        if (noDataList) {
            result.data.add( [null, NO_DATA_LABEL, noDataList.size()] )

            result.dataDetails.add( [
                    query:  query,
                    id:     null,
                    label:  NO_DATA_LABEL,
                    idList: noDataList
            ])
        }
    }
}
