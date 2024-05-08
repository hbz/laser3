package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.Org
import de.laser.auth.Role
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import de.laser.storage.BeanStore
import grails.web.servlet.mvc.GrailsParameterMap

class ProviderQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()

        //println 'ProviderQuery.query()'
        //println params

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)
//        List<Long> orphanedIdList = BaseFilter.getCachedFilterIdList(prefix + 'Orphaned', params)

        //println 'ProviderQuery.query() -> ' + params.query + ' : ' + suffix

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select pro.name, pro.name, count(pro.name) from Provider pro where pro.id in (:idList) group by pro.name order by pro.name',
                    'select pro.id from Provider pro where pro.id in (:idList) and pro.name = :d order by pro.id',
                    idList,
                    result
            )
//            handleGenericAllSignOrphanedQuery(
//                    params.query,
//                    'select pro.id, pro.name, 1, false from Provider pro where pro.id in (:idList) order by pro.name',
//                    'select pro.id from Provider pro where pro.id in (:idList) and pro.name = :d order by pro.id',
//                    idList,
//                    orphanedIdList,
//                    result
//            )
        }
        else if ( suffix in ['libraryType', 'region', 'country', 'libraryNetwork', 'funderType', 'funderHskType']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['orgType']) {

            handleGenericRefdataQuery(
                    params.query,
                    PROPERTY_QUERY[0] + 'from Org o join o.orgType p where o.id in (:idList)' + PROPERTY_QUERY[1],
                    'select o.id from Org o join o.orgType p where o.id in (:idList) and p.id = :d order by o.name',
                    'select distinct o.id from Org o where o.id in (:idList) and not exists (select ot from o.orgType ot)',
                    idList,
                    result
            )
        }
        else if ( suffix in ['customerType']) {

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
            handleGenericNonMatchingData(
                    params.query,
                    'select distinct o.id from Org o where o.id in (:idList) and not exists (select oss from OrgSetting oss where oss.org = o and oss.key = \'CUSTOMER_TYPE\')',
                    idList,
                    result
            )
        }
        else if ( suffix in ['subjectGroup']) {

            handleGenericRefdataQuery(
                    params.query,
                    PROPERTY_QUERY[0] + 'from Org o join o.subjectGroup rt join rt.subjectGroup p where o.id in (:idList)' + PROPERTY_QUERY[1],
                    'select o.id from Org o join o.subjectGroup rt join rt.subjectGroup p where o.id in (:idList) and p.id = :d order by o.name',
                    'select distinct o.id from Org o where o.id in (:idList) and not exists (select osg from OrgSubjectGroup osg where osg.org = o)',
                    idList,
                    result
            )
        }
        else if ( suffix in ['x']) {

            if (params.query in ['org-x-identifier']) {

                handleGenericIdentifierXQuery(
                        params.query,
                        'select ns.id, ns.ns, count(*) from Org o join o.ids ident join ident.ns ns where o.id in (:idList)',
                        'select o.id from Org o join o.ids ident join ident.ns ns where o.id in (:idList)',
                        'select o.id from Org o where o.id in (:idList)', // inversed idList
                        idList,
                        result
                )
            }
            else if (params.query in ['org-x-property']) {

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from Org o join o.propertySet prop join prop.type pd where o.id in (:idList)',
                        'select o.id from Org o join o.propertySet prop join prop.type pd where o.id in (:idList)',
                        idList,
                        contextService.getOrg(),
                        result
                )
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from Provider pro join pro.' + refdata + ' p where pro.id in (:idList)' + PROPERTY_QUERY[1],
                'select pro.id from Provider pro join pro.' + refdata + ' p where pro.id in (:idList) and p.id = :d order by pro.name',
                'select distinct pro.id from Provider pro where pro.id in (:idList) and pro.' + refdata + ' is null',
                idList,
                result
        )
    }
}