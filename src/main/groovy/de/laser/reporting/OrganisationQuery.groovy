package de.laser.reporting

import de.laser.Org
import de.laser.RefdataValue
import de.laser.auth.Role
import grails.web.servlet.mvc.GrailsParameterMap

class OrganisationQuery extends GenericQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        Map<String, Object> result = [
                chart    : params.chart,
                query    : params.query,
                data     : [],
                dataDetails : []
        ]

        String prefix = params.query.split('-')[0]
        List idList = params.list(prefix + 'IdList').collect { it as Long }

        if (! idList) {
        }
        else if ( params.query in ['org-libraryType', 'member-libraryType']) {

            processSimpleRefdataQuery(params.query, 'libraryType', idList, result)
        }
        else if ( params.query in ['org-region', 'member-region', 'provider-region']) {

            processSimpleRefdataQuery(params.query,'region', idList, result)
        }
        else if ( params.query in ['provider-country']) {

            processSimpleRefdataQuery(params.query,'country', idList, result)
        }
        else if ( params.query in ['org-libraryNetwork', 'member-libraryNetwork']) {

            processSimpleRefdataQuery(params.query, 'libraryNetwork', idList, result)
        }
        else if ( params.query in ['org-funderType', 'member-funderType']) {

            processSimpleRefdataQuery(params.query, 'funderType', idList, result)
        }
        else if ( params.query in ['org-funderHskType', 'member-funderHskType']) {

            processSimpleRefdataQuery(params.query, 'funderHskType', idList, result)
        }
        else if ( params.query in ['org-orgType', 'member-orgType', 'provider-orgType']) {

            result.data = Org.executeQuery(
                    PROPERTY_QUERY[0] + 'from Org o join o.orgType p where o.id in (:idList)' + PROPERTY_QUERY[1],
                    [idList: idList]
            )
            result.data.each { d ->
                d[1] = RefdataValue.get(d[0]).getI10n('value')

                result.dataDetails.add( [
                        query:  params.query,
                        id:     d[0],
                        label:  d[1],
                        idList: Org.executeQuery(
                                'select o.id from Org o join o.orgType p where o.id in (:idList) and p.id = :d order by o.name',
                                [idList: idList, d: d[0]]
                        )
                ])
            }
            handleNonMatchingData(
                    params.query,
                    'select distinct o.id from Org o where o.id in (:idList) and not exists (select ot from o.orgType ot)',
                    idList,
                    result
            )
        }
        else if ( params.query in ['org-customerType', 'member-customerType']) {

            result.data = Org.executeQuery(
                    'select r.id, r.authority, count(*) from Org o, OrgSetting oss, Role r where oss.org = o and oss.key = \'CUSTOMER_TYPE\' and o.id in (:idList) and oss.roleValue = r group by r.id',
                    [idList: idList]
            )

            result.data.each { d ->
                d[1] = Role.get(d[0]).getI10n('authority')

                result.dataDetails.add([
                        query : params.query,
                        id    : d[0],
                        label : d[1],
                        idList: Org.executeQuery(
                                'select o.id from Org o, OrgSetting oss where oss.org = o and oss.key = \'CUSTOMER_TYPE\' and o.id in (:idList) and oss.roleValue.id = :d order by o.name',
                                [idList: idList, d: d[0]]
                        )
                ])
            }
            handleNonMatchingData(
                    params.query,
                    'select distinct o.id from Org o where o.id in (:idList) and not exists (select oss from OrgSetting oss where oss.org = o and oss.key = \'CUSTOMER_TYPE\')',
                    idList,
                    result
            )
        }
        else if ( params.query in ['org-subjectGroup', 'member-subjectGroup']) {

            result.data = Org.executeQuery(
                    PROPERTY_QUERY[0] + 'from Org o join o.subjectGroup rt join rt.subjectGroup p where o.id in (:idList)' + PROPERTY_QUERY[1],
                    [idList: idList]
            )
            result.data.each { d ->
                d[1] = RefdataValue.get(d[0]).getI10n('value')

                result.dataDetails.add([
                        query : params.query,
                        id    : d[0],
                        label : d[1],
                        idList: Org.executeQuery(
                                'select o.id from Org o join o.subjectGroup rt join rt.subjectGroup p where o.id in (:idList) and p.id = :d order by o.name',
                                [idList: idList, d: d[0]]
                        )
                ])
            }
            handleNonMatchingData(
                    params.query,
                    'select distinct o.id from Org o where o.id in (:idList) and not exists (select osg from OrgSubjectGroup osg where osg.org = o)',
                    idList,
                    result
            )
        }

        result
    }

    static void processSimpleRefdataQuery(String query, String refdata, List idList, Map<String, Object> result) {

        result.data = Org.executeQuery(
                PROPERTY_QUERY[0] + 'from Org o join o.' + refdata + ' p where o.id in (:idList)' + PROPERTY_QUERY[1], [idList: idList]
        )
        result.data.each { d ->
            d[1] = RefdataValue.get(d[0]).getI10n('value')

            result.dataDetails.add( [
                    query:  query,
                    id:     d[0],
                    label:  d[1],
                    idList: Org.executeQuery(
                        'select o.id from Org o join o.' + refdata + ' p where o.id in (:idList) and p.id = :d order by o.name',
                        [idList: idList, d: d[0]]
                    )
            ])
        }
        handleNonMatchingData(
                query,
                'select distinct o.id from Org o where o.id in (:idList) and o.' + refdata + ' is null',
                idList,
                result
        )
    }
}
