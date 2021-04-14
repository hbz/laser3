package de.laser.reporting.myInstitution

import de.laser.ContextService
import de.laser.Org
import de.laser.Platform
import de.laser.ReportingService
import de.laser.Subscription
import de.laser.TitleInstancePackagePlatform
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.base.BaseFilter
import de.laser.reporting.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap

class SubscriptionQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        Map<String, Object> result = [
                chart       : params.chart,
                query       : params.query,
                labels      : [:],
                data        : [],
                dataDetails : []
        ]

        String prefix = params.query.split('-')[0]
        List idList   = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( params.query in ['subscription-form']) {

            processSimpleRefdataQuery(params.query,'form', idList, result)
        }
        else if ( params.query in ['subscription-kind']) {

            processSimpleRefdataQuery(params.query,'kind', idList, result)
        }
        else if ( params.query in ['subscription-resource']) {

            processSimpleRefdataQuery(params.query,'resource', idList, result)
        }
        else if ( params.query in ['subscription-status']) {

            processSimpleRefdataQuery(params.query,'status', idList, result)
        }
        else if ( params.query in ['subscription-provider-assignment']) {

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

            List<Long> nonMatchingIdList = idList.minus( result.dataDetails.collect { it.idList }.flatten() )
            List noDataList = nonMatchingIdList ? Subscription.executeQuery( 'select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList] ) : []

            if (noDataList) {
                result.data.add( [null, BaseQuery.NO_PROVIDER_LABEL, noDataList.size()] )

                result.dataDetails.add( [
                        query:  params.query,
                        id:     null,
                        label:  BaseQuery.NO_PROVIDER_LABEL,
                        idList: noDataList
                ])
            }
        }
        else if ( params.query in ['subscription-platform-assignment']) {

            result.data = Platform.executeQuery(
                    'select p.id, p.name, count(*) from Org o join o.platforms p join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:idList) group by p.id order by p.name',
                    [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
            )

            result.data.eachWithIndex { d, i ->
                List<Long> subIdList = Platform.executeQuery(
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
                        idList: Platform.executeQuery(
                                'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o join o.platforms p where s.id in (:idList) and p.id = :d order by s.name',
                                [idList: idList, d: d[0]]
                        ),
                        value2: positiveList.size(),
                        value1: (subIdList - positiveList).size()
                ])
            }

            List<Long> nonMatchingIdList = idList.minus( result.dataDetails.collect { it.idList }.flatten() )
            List noDataList = nonMatchingIdList ? Subscription.executeQuery( 'select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList] ) : []

            if (noDataList) {
                result.data.add( [null, BaseQuery.NO_PLATFORM_LABEL, noDataList.size()] )

                result.dataDetails.add( [
                        query:  params.query,
                        id:     null,
                        label:  BaseQuery.NO_PLATFORM_LABEL,
                        idList: noDataList,
                        value1: 0,
                        value2: noDataList.size()
                ])
            }
        }
        else if ( params.query in ['subscription-property-assignment']) {

            handleGenericPropertyAssignmentQuery(
                    params.query,
                    'select pd.id, pd.name, count(*) from Subscription sub join sub.propertySet prop join prop.type pd where sub.id in (:idList)',
                    'select sub.id from Subscription sub join sub.propertySet prop join prop.type pd where sub.id in (:idList)',
                    idList,
                    contextService.getOrg(),
                    result
            )
        }
        else if ( params.query in ['subscription-identifier-assignment']) {

            handleGenericIdentifierAssignmentQuery(
                    params.query,
                    'select ns.id, ns.ns, count(*) from Subscription sub join sub.ids ident join ident.ns ns where sub.id in (:idList)',
                    'select sub.id from Subscription sub join sub.ids ident join ident.ns ns where sub.id in (:idList)',
                    'select sub.id from Subscription sub where sub.id in (:idList)', // modified idList
                    idList,
                    result
            )
        }

        result
    }

    static void processSimpleRefdataQuery(String query, String refdata, List idList, Map<String, Object> result) {

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
