package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.Org
import de.laser.Platform
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.helper.RDStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class SubscriptionQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        //println 'SubscriptionQuery.query() -> ' + params.query + ' : ' + suffix

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            handleGenericAllQuery(
                    params.query,
                    'select s.name, s.name, count(s.name) from Subscription s where s.id in (:idList) group by s.name order by s.name',
                    'select s.id from Subscription s where s.id in (:idList) and s.name = :d order by s.id',
                    idList,
                    result
            )
        }
        else if ( suffix in ['form', 'kind', 'resource', 'status']) {

            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['manualCancellationDate']) {

            handleGenericDateQuery(
                    params.query,
                    'select s.manualCancellationDate, s.manualCancellationDate, count(*) from Subscription s where s.id in (:idList) and s.manualCancellationDate != null group by s.manualCancellationDate order by s.manualCancellationDate',
                    'select s.id from Subscription s where s.id in (:idList) and s.manualCancellationDate = :d order by s.name',
                    'select s.id from Subscription s where s.id in (:idList) and s.manualCancellationDate is null order by s.name',
                    idList,
                    result
            )
        }
        else if ( suffix in ['isMultiYear']) {

            handleGenericBooleanQuery(
                    params.query,
                    'select s.isMultiYear, s.isMultiYear, count(*) from Subscription s where s.id in (:idList) group by s.isMultiYear',
                    'select s.id from Subscription s where s.id in (:idList) and s.isMultiYear = :d order by s.name',
                    idList,
                    result
            )
        }
        else if ( suffix in ['x']) {

            if (params.query in ['subscription-x-annual']) {

                handleGenericAnnualXQuery(params.query, 'Subscription', idList, result)
            }
            else if (params.query in ['subscription-x-memberProvider']) {

                List<Long> memberSubIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params) ?: []

                result.data = Org.executeQuery(
                        'select o.id, o.name, count(*) from Org o join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:memberSubIdList) group by o.id order by o.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), memberSubIdList: memberSubIdList]
                )
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery(
                                    'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o where s.id in (:memberSubIdList) and o.id = :d order by s.name',
                                    [memberSubIdList: memberSubIdList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = memberSubIdList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> nonList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []

                if (nonList) {
                    result.data.add([null, BaseQuery.getChartLabel(BaseQuery.NO_MATCH_LABEL), nonList.size()])

                    result.dataDetails.add([
                            query : params.query,
                            id    : null,
                            label : BaseQuery.getChartLabel(BaseQuery.NO_MATCH_LABEL),
                            idList: nonList
                    ])
                }
            }
            else if (params.query in ['subscription-x-provider']) {

                result.data = Org.executeQuery(
                        'select o.id, o.name, count(*) from Org o join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:idList) group by o.id order by o.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                )
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery(
                                    'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o where s.id in (:idList) and o.id = :d order by s.name',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['subscription-x-platform']) {

                result.data = Platform.executeQuery(
                        'select p.id, p.name, count(*) from Org o join o.platforms p join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:idList) group by p.id order by p.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                )
                result.data.eachWithIndex { d, i ->
                    List<Long> subIdList = Subscription.executeQuery(
                            'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o join o.platforms p where s.id in (:idList) and p.id = :d order by s.name',
                            [idList: idList, d: d[0]]
                    )
                    List<Long> positiveList = []

                    // Subscriptions with existing tipps -> platform
                    subIdList.each { subId ->
                        List<Long> pltIdList = TitleInstancePackagePlatform.executeQuery(
                                'select distinct tipp.platform.id from IssueEntitlement ie join ie.tipp tipp where ie.subscription.id = :subId and ie.status = :status and ie.acceptStatus = :acceptStatus',
                                [subId: subId, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED]
                        )
                        if (pltIdList[0] == d[0]) {
                            positiveList.add(subId)
                        }
                    }

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery(
                                    'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o join o.platforms p where s.id in (:idList) and p.id = :d order by s.name',
                                    [idList: idList, d: d[0]]
                            ),
                            value2: positiveList.size(),
                            value1: (subIdList - positiveList).size()
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData2Values_TMP(params.query, BaseQuery.NO_PLATFORM_LABEL, noDataList, result)
            }
            else if (params.query in ['subscription-x-property']) {

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from Subscription sub join sub.propertySet prop join prop.type pd where sub.id in (:idList)',
                        'select sub.id from Subscription sub join sub.propertySet prop join prop.type pd where sub.id in (:idList)',
                        idList,
                        contextService.getOrg(),
                        result
                )
            }
            else if (params.query in ['subscription-x-memberSubscriptionProperty']) {

                List<Long> memberSubscriptionIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params)

                handleGenericPropertyXQuery(
                        params.query,
                        'select pd.id, pd.name, count(*) from Subscription sub join sub.propertySet prop join prop.type pd where sub.id in (:idList)',
                        'select sub.id from Subscription sub join sub.propertySet prop join prop.type pd where sub.id in (:idList)',
                        memberSubscriptionIdList,
                        contextService.getOrg(),
                        result
                )
            }
            else if (params.query in ['subscription-x-identifier']) {

                handleGenericIdentifierXQuery(
                        params.query,
                        'select ns.id, ns.ns, count(*) from Subscription sub join sub.ids ident join ident.ns ns where sub.id in (:idList)',
                        'select sub.id from Subscription sub join sub.ids ident join ident.ns ns where sub.id in (:idList)',
                        'select sub.id from Subscription sub where sub.id in (:idList)', // inversed idList
                        idList,
                        result
                )
            }
            else if (params.query in ['subscription-x-memberSubscription']) {

                List<Long> memberSubscriptionIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params) // filter is set

                result.data = idList ? Subscription.executeQuery(
                        'select sub.id, concat(sub.name, \' (ID:\', sub.id,\')\'), count(mbr.id) from Subscription mbr join mbr.instanceOf sub ' +
                                'where sub.id in (:idList) and mbr.id in (:memberSubscriptionIdList) group by sub.id order by sub.name, sub.id',
                        [idList: idList, memberSubscriptionIdList: memberSubscriptionIdList]) : []

                result.data.each { d ->

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery(
                                    'select sub.id from Subscription sub where sub.instanceOf.id = :d and sub.id in (:memberSubscriptionIdList) order by sub.name',
                                    [d: d[0], memberSubscriptionIdList: memberSubscriptionIdList]
                            )
                    ])
                }
            }
            else if (params.query in ['subscription-x-memberAnnual']) {

                List<Long> memberSubscriptionIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params) // filter is set

                handleGenericAnnualXQuery(params.query, 'Subscription', memberSubscriptionIdList, result)
            }
            else if (params.query in ['subscription-x-member']) {

                List<Long> memberSubscriptionIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params) // filter is set

                result.data = idList ? Subscription.executeQuery(
                        'select sub.id, concat(sub.name, \' (ID:\', sub.id,\')\'), count(mbr.id) from Subscription mbr join mbr.instanceOf sub ' +
                                'where sub.id in (:idList) and mbr.id in (:memberSubscriptionIdList) group by sub.id order by sub.name, sub.id',
                        [idList: idList, memberSubscriptionIdList: memberSubscriptionIdList]) : []

                List<Long> memberIdList = BaseFilter.getCachedFilterIdList('member', params) // filter is set

                result.data.each { d ->
                    List<Long> validMemberIdList = memberIdList ? Subscription.executeQuery(
                            'select distinct orgRole.org.id from OrgRole orgRole join orgRole.sub mbr where orgRole.org.id in (:memberIdList) and ' +
                                    'mbr in (select mbr from Subscription mbr where mbr.instanceOf.id = :d and mbr.id in (:memberSubscriptionIdList)) and ' +
                                    'orgRole.roleType in (:roleTypes)',
                            [d: d[0], memberIdList: memberIdList, memberSubscriptionIdList: memberSubscriptionIdList, roleTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]

                    ) : []
                    d[2] = validMemberIdList.size()

                    if (! validMemberIdList.isEmpty()) {
                        result.dataDetails.add([
                                query : params.query,
                                id    : d[0],
                                label : d[1],
                                idList: validMemberIdList
                        ])
                    }
                }

                result.data = result.data.findAll { it[2] > 0 } // remove ms without matching m
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from Subscription s join s.' + refdata + ' p where s.id in (:idList)' + PROPERTY_QUERY[1],
                'select s.id from Subscription s join s.' + refdata + ' p where s.id in (:idList) and p.id = :d order by s.name',
                'select distinct s.id from Subscription s where s.id in (:idList) and s.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
