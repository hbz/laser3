package de.laser.reporting

import de.laser.Org
import grails.web.servlet.mvc.GrailsParameterMap

class QueryHandler {

    static Map<String, Object> processOrgQuery(GrailsParameterMap params) {
        println 'processOrgQuery()'

        Map<String, Object> result = [
            chart    : params.chart,
            query    : params.query,
            data     : []
        ]

        if ( params.query in ['org-libraryType', 'member-libraryType', 'provider-libraryType']) {
            String prefix = params.query.split('-')[0]
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            def data = Org.executeQuery(
                    'select p.value_de, count(*) from Org o join o.libraryType p where o.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            result.data = data
        }
        else if ( params.query in ['org-region', 'member-region', 'provider-region']) {
            String prefix = params.query.split('-')[0]
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            def data = Org.executeQuery(
                    'select p.value_de, count(*) from Org o join o.region p where o.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            result.data = data
        }
        else if ( params.query in ['provider-country']) {
            String prefix = params.query.split('-')[0]
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            def data = Org.executeQuery(
                    'select p.value_de, count(*) from Org o join o.country p where o.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            result.data = data
        }

        result
    }
}
