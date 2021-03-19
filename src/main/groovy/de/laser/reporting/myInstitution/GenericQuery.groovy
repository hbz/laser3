package de.laser.reporting.myInstitution

import de.laser.Org
import de.laser.RefdataValue
import de.laser.properties.PropertyDefinition
import grails.web.servlet.mvc.GrailsParameterMap

class GenericQuery {

    static String NO_DATA_LABEL = 'keine Angabe *'

    static List<String> getQueryLabels(Map<String, Object> config, GrailsParameterMap params) {

        List<String> meta = []

        config.each {it ->
            it.value.get('query')?.each { it2 ->
                if (it2.value.containsKey(params.query)) {
                    meta = [ it2.key, it2.value.get(params.query), params.label ]
                }
            }
            it.value.get('query2')?.each { it2 ->
                if (it2.value.containsKey(params.query)) {
                    meta = [ it2.key, it2.value.get(params.query).label, params.label ]
                }
            }
        }
        meta
    }

    static Object getDataDetailsByIdAndKey(Long id, String key, List<Map<String, Object>> idList) {
        def result

        idList.each{ it ->
            if (it.id == id) {
                result = it.get(key)
                return
            }
        }
        result
    }

    static void handleGenericRefdataQuery(String query, String dataHql, String dataDetailsHql, String nonMatchingHql, List idList, Map<String, Object> result) {

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
        handleGenericNonMatchingData( query, nonMatchingHql, idList, result )
    }

    static void handleGenericNonMatchingData(String query, String hql, List idList, Map<String, Object> result) {

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

    static void handleGenericIdentifierAssignmentQuery(String query, String dataHqlPart, String dataDetailsHqlPart, List idList, Map<String, Object> result) {

        result.data = Org.executeQuery(
                dataHqlPart + " and ident.value is not null and trim(ident.value) != '' group by ns.id order by ns.ns",
                [idList: idList]
        )
        result.data.each { d ->
            List<Long> objIdList = Org.executeQuery(
                    dataDetailsHqlPart + " and ns.id = :d and ident.value is not null and trim(ident.value) != ''",
                    [idList: idList, d: d[0]]
            )
            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: objIdList,
                    value1: objIdList.size(),
                    value2: objIdList.unique().size()
            ])
        }
    }

    static void handleGenericPropertyAssignmentQuery(String query, String dataHqlPart, String dataDetailsHqlPart, List idList, Org ctxOrg, Map<String, Object> result) {

        result.data = Org.executeQuery(
                dataHqlPart + " and (prop.tenant = :ctxOrg or prop.isPublic = true) and pd.descr like '%Property' group by pd.id order by pd.name",
                [idList: idList, ctxOrg: ctxOrg]
        )
        result.data.each { d ->
            d[1] = PropertyDefinition.get(d[0]).getI10n('name').replaceAll("'", '"')

            List<Long> objIdList =  Org.executeQuery(
                    dataDetailsHqlPart + ' and (prop.tenant = :ctxOrg or prop.isPublic = true) and pd.id = :d order by pd.name',
                    [idList: idList, d: d[0], ctxOrg: ctxOrg]
            )
            result.dataDetails.add([
                    query : query,
                    id    : d[0],
                    label : d[1],
                    idList: objIdList,
                    value1: objIdList.size(),
                    value2: objIdList.unique().size()
            ])
        }
    }

}
