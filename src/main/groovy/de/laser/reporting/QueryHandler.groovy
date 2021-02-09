package de.laser.reporting

import de.laser.Org
import grails.web.servlet.mvc.GrailsParameterMap

class QueryHandler {

    static Map<String, Object> processOrgQuery(GrailsParameterMap params) {

        Map<String, Object> result = [
            chart    : params.chart,
            query    : params.query,
            data     : []
        ]

        String noDataLabel = '* keine Angabe'
        String prefix = params.query.split('-')[0]

        List data = []
        List noData

        if ( params.query in ['org-libraryType', 'member-libraryType', 'provider-libraryType']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Org o join o.libraryType p where o.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Org o where o.id in (:idList) and o.libraryType is null group by o.libraryType',
                    [idList: idList]
            )
        }
        else if ( params.query in ['org-region', 'member-region', 'provider-region']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Org o join o.region p where o.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Org o where o.id in (:idList) and o.region is null group by o.region',
                    [idList: idList]
            )
        }
        else if ( params.query in ['provider-country']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Org o join o.country p where o.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Org o where o.id in (:idList) and o.country is null group by o.country',
                    [idList: idList]
            )
        }

        result.data = data
        if (noData) {
            result.data.add([noDataLabel, noData.get(0)])
        }

        result
    }

    static Map<String, Object> processSubscriptionQuery(GrailsParameterMap params) {

        Map<String, Object> result = [
                chart    : params.chart,
                query    : params.query,
                data     : []
        ]

        String noDataLabel = '* keine Angabe'
        String prefix = params.query.split('-')[0]

        List data = []
        List noData

        if ( params.query in ['subscription-form']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Subscription s join s.form p where s.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Subscription s where s.id in (:idList) and s.form is null group by s.form',
                    [idList: idList]
            )
        }
        else if ( params.query in ['subscription-kind']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Subscription s join s.kind p where s.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Subscription s where s.id in (:idList) and s.kind is null group by s.kind',
                    [idList: idList]
            )
        }
        else if ( params.query in ['subscription-resource']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Subscription s join s.resource p where s.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Subscription s where s.id in (:idList) and s.resource is null group by s.resource',
                    [idList: idList]
            )
        }
        else if ( params.query in ['subscription-status']) {
            List idList = params.list(prefix + 'IdList[]').collect { it as Long }
            data = Org.executeQuery(
                    'select p.value_de, count(*) from Subscription s join s.status p where s.id in (:idList) group by p.value_de',
                    [idList: idList]
            )
            noData = Org.executeQuery(
                    'select count(*) from Subscription s where s.id in (:idList) and s.status is null group by s.status',
                    [idList: idList]
            )
        }

        result.data = data
        if (noData) {
            result.data.add([noDataLabel, noData.get(0)])
        }

        result
    }
}
