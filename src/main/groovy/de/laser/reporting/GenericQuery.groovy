package de.laser.reporting

import de.laser.Org
import grails.web.servlet.mvc.GrailsParameterMap

class GenericQuery {

    static String NO_DATA_LABEL = '- keine Angabe -'

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

    static List<String> getQueryLabels(Map<String, Object> config, GrailsParameterMap params) {

        List<String> meta = []

        config.each {it ->
            if (it.value.containsKey('query')) {
                it.value.get('query').each { it2 ->
                    println it2
                    if (it2.value.containsKey(params.query)) {
                        meta = [ it2.key, it2.value.get(params.query), params.label ]
                    }
                }
            }
        }
        meta
    }
}
