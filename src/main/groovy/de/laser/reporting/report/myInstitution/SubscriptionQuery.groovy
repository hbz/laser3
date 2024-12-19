package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.License
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap

class SubscriptionQuery extends BaseQuery {

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStore.getContextService()

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
        else if ( suffix in ['referenceYear']) {

            handleGenericQuery(
                    params.query,
                    'select s.referenceYear, s.referenceYear, count(*) from Subscription s where s.id in (:idList) and s.referenceYear != null group by s.referenceYear order by s.referenceYear',
                    'select s.id from Subscription s where s.id in (:idList) and s.referenceYear = :d order by s.name',
                    'select s.id from Subscription s where s.id in (:idList) and s.referenceYear is null order by s.name',
                    idList,
                    result
            )
            result.dataDetails.each { dd ->
                if (dd.id) {
                    dd.id = Long.valueOf(dd.id.toString()) // year -> long
                }
            }
        }
        else if ( suffix in ['x']) {

            if (params.query in ['subscription-x-annual']) {

                handleGenericAnnualXQuery(params.query, 'Subscription', idList, result)
            }
            else if (params.query in ['subscription-x-referenceYear']) {

                handleSubscriptionReferenceYearXQuery(params.query, idList, result)
            }
//            else if (params.query in ['subscription-x-memberProvider']) {
//
//                List<Long> memberSubIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params) ?: []
//
//                result.data = Org.executeQuery(
//                        'select o.id, o.name, count(*) from Org o join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:memberSubIdList) group by o.id order by o.name',
//                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), memberSubIdList: memberSubIdList]
//                )
//                result.data.each { d ->
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : d[0],
//                            label : d[1],
//                            idList: Subscription.executeQuery(
//                                    'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o where s.id in (:memberSubIdList) and o.id = :d order by s.name',
//                                    [memberSubIdList: memberSubIdList, d: d[0]]
//                            )
//                    ])
//                }
//
//                List<Long> nonMatchingIdList = memberSubIdList.minus(result.dataDetails.collect { it.idList }.flatten())
//                List<Long> nonList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []
//
//                if (nonList) {
//                    result.data.add([null, BaseQuery.getChartLabel(BaseQuery.NO_MATCH_LABEL), nonList.size()])
//
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : null,
//                            label : BaseQuery.getChartLabel(BaseQuery.NO_MATCH_LABEL),
//                            idList: nonList
//                    ])
//                }
//            }
//            else if (params.query in ['subscription-x-provider']) {
//
//                result.data = Org.executeQuery(
//                        'select o.id, o.name, count(*) from Org o join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:idList) group by o.id order by o.name',
//                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
//                )
//                result.data.each { d ->
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : d[0],
//                            label : d[1],
//                            idList: Subscription.executeQuery(
//                                    'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o where s.id in (:idList) and o.id = :d order by s.name',
//                                    [idList: idList, d: d[0]]
//                            )
//                    ])
//                }
//
//                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
//                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []
//
//                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
//            }
//            else if (params.query in ['subscription-x-platform']) {
//
//                result.data = Platform.executeQuery(
//                        'select p.id, p.name, count(*) from Org o join o.platforms p join o.links orgLink where o.id in (:providerIdList) and orgLink.sub.id in (:idList) group by p.id order by p.name',
//                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
//                )
//                result.data.eachWithIndex { d, i ->
//                    List<Long> subIdList = Subscription.executeQuery(
//                            'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o join o.platforms p where s.id in (:idList) and p.id = :d order by s.name',
//                            [idList: idList, d: d[0]]
//                    )
//                    List<Long> positiveList = []
//
//                    // Subscriptions with existing tipps -> platform
//                    subIdList.each { subId ->
//                        List<Long> pltIdList = TitleInstancePackagePlatform.executeQuery(
//                                'select distinct tipp.platform.id from IssueEntitlement ie join ie.tipp tipp where ie.subscription.id = :subId and ie.status = :status ',
//                                [subId: subId, status: RDStore.TIPP_STATUS_CURRENT]
//                        )
//                        if (pltIdList[0] == d[0]) {
//                            positiveList.add(subId)
//                        }
//                    }
//
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : d[0],
//                            label : d[1],
//                            idList: Subscription.executeQuery(
//                                    'select s.id from Subscription s join s.orgRelations orgRel join orgRel.org o join o.platforms p where s.id in (:idList) and p.id = :d order by s.name',
//                                    [idList: idList, d: d[0]]
//                            ),
//                            value2: positiveList.size(),
//                            value1: (subIdList - positiveList).size()
//                    ])
//                }
//
//                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
//                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []
//
//                handleGenericNonMatchingData2Values_TMP(params.query, BaseQuery.NO_PLATFORM_LABEL, noDataList, result)
//            }
            else if (params.query in ['subscription-x-platform'] && false) {

                // TODO
                // TODO subscription > subscriptionPackage > package > platform ?
                // TODO

                String query_sub_pr_prov_plt = '''select plt.id, plt.name, count(*) from ProviderRole pr 
                    join pr.provider pro 
                    join pro.platforms plt 
                    where pro.id in (:providerIdList) and pr.subscription.id in (:idList) group by plt.id order by plt.name'''

                String query_sub_subpkg_pkg_plt =  '''select plt.id, plt.name, count(*) from SubscriptionPackage subPkg 
                    join subPkg.subscription sub 
                    join subPkg.pkg pkg 
                    join pkg.nominalPlatform plt 
                    where (pkg.provider is null or pkg.provider.id in (:providerIdList)) and sub.id in (:idList) group by plt.id order by plt.name'''

                String query_sub_subpkg_pkg_prov_plt =  '''select plt.id, plt.name, count(*) from SubscriptionPackage subPkg 
                    join subPkg.subscription sub 
                    join subPkg.pkg pkg 
                    join pkg.provider pro
                    join pro.platforms plt 
                    where pro.id in (:providerIdList) and sub.id in (:idList) group by plt.id order by plt.name'''

                println '\n--------------> query_sub_pr_prov_plt'
                println Platform.executeQuery(query_sub_pr_prov_plt, [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList])

                println '\n--------------> query_sub_subpkg_pkg_plt'
                println Platform.executeQuery(query_sub_subpkg_pkg_plt, [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList])

                println '\n--------------> query_sub_subpkg_pkg_prov_plt'
                println Platform.executeQuery(query_sub_subpkg_pkg_prov_plt, [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList])

                result.data = Platform.executeQuery(
                        'select plt.id, plt.name, count(*) from ProviderRole pr join pr.provider pro join pro.platforms plt where pro.id in (:providerIdList) and pr.subscription.id in (:idList) group by plt.id order by plt.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                )
                result.data.eachWithIndex { d, i ->
                    List<Long> subIdList = Subscription.executeQuery(
                            'select s.id from ProviderRole pr join pr.subscription s join pr.provider pro join pro.platforms plt where s.id in (:idList) and plt.id = :d order by s.name',
                            [idList: idList, d: d[0]]
                    )
                    List<Long> positiveList = []

                    // Subscriptions with existing tipps -> platform
                    subIdList.each { subId ->
                        List<Long> pltIdList = TitleInstancePackagePlatform.executeQuery(
                                'select distinct tipp.platform.id from IssueEntitlement ie join ie.tipp tipp where ie.subscription.id = :subId and ie.status = :status ',
                                [subId: subId, status: RDStore.TIPP_STATUS_CURRENT]
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
                                    'select s.id from ProviderRole pr join pr.subscription s join pr.provider pro join pro.platforms plt where s.id in (:idList) and plt.id = :d order by s.name',
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
            else if (params.query in ['subscription-x-license']) {

                result.data = License.executeQuery(
                        'select lk.sourceLicense.id, lk.sourceLicense.reference, count(*) from Links lk where lk.destinationSubscription.id in (:idList) and lk.linkType = :type ' +
                                'and exists ( select ro from lk.sourceLicense.orgRelations ro where ro.org = :ctx and ro.roleType in (:roleTypes) ) ' +
                                'group by lk.sourceLicense.id, lk.sourceLicense.reference order by lk.sourceLicense.reference',
                        [idList: idList, type: RDStore.LINKTYPE_LICENSE, ctx: contextService.getOrg(), roleTypes: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]]
                )
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery(
                                    'select lk.destinationSubscription.id from Links lk where lk.destinationSubscription.id in (:idList) and lk.sourceLicense.id = :d order by lk.destinationSubscription.name',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_LICENSE_LABEL, noDataList, result)
            }
            else if (params.query in ['subscription-x-licenseCategory']) {

                result.data = License.executeQuery(
                        'select lk.sourceLicense.licenseCategory.id, lk.sourceLicense.licenseCategory.value_de, count(*) from Links lk where lk.destinationSubscription.id in (:idList) and lk.linkType = :type ' +
                                'and exists ( select ro from lk.sourceLicense.orgRelations ro where ro.org = :ctx and ro.roleType in (:roleTypes) ) ' +
                                'group by lk.sourceLicense.licenseCategory.id, lk.sourceLicense.licenseCategory.value_de order by lk.sourceLicense.licenseCategory.value_de',
                        [idList: idList, type: RDStore.LINKTYPE_LICENSE, ctx: contextService.getOrg(), roleTypes: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]]
                )
                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery(
                                    'select lk.destinationSubscription.id from Links lk where lk.destinationSubscription.id in (:idList) and lk.sourceLicense.licenseCategory.id = :d order by lk.destinationSubscription.name',
                                    [idList: idList, d: d[0]]
                            )
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select s.id from Subscription s where s.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_LICENSE_LABEL, noDataList, result)
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
            else if (params.query in ['subscription-x-memberReferenceYear']) {

                List<Long> memberSubscriptionIdList = BaseFilter.getCachedFilterIdList('memberSubscription', params) // filter is set

                handleSubscriptionReferenceYearXQuery(params.query, memberSubscriptionIdList, result)
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
            else if (params.query in ['subscription-x-provider']) {

                List providerIdList = BaseFilter.getCachedFilterIdList('provider', params)

                List aList = Provider.executeQuery(
                        'select pro.id, pro.name, count(*) from ProviderRole pr join pr.provider pro where pro.id in (:providerIdList) and pr.subscription.id in (:idList) group by pro.id order by pro.name',
                        [providerIdList: providerIdList, idList: idList]
                )
                List bList = Provider.executeQuery(
                        'select pro.id, pro.name, count(*) from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.provider pro where pro.id in (:providerIdList) and sub.id in (:idList) group by pro.id order by pro.name',
                        [providerIdList: providerIdList, idList: idList]
                )
                result.data = []

                // sorting
                Provider.executeQuery('select id, name from Provider where id in (:providerIdList) order by name', [providerIdList: providerIdList]).each{ p ->
                    List tmp = [null, null, 0, 0]
                    aList.each { a -> if (p[0] == a[0]) {
                        tmp[0] = a[0]
                        tmp[1] = a[1]
                        tmp[2] = a[2]
                    } }
                    bList.each { b -> if (p[0] == b[0]) {
                        tmp[0] = b[0]
                        tmp[1] = b[1]
                        tmp[3] = b[2]
                    } }

                    if (tmp[0]) { result.data << tmp }
                }

                result.data.each { d ->
                    List a = Subscription.executeQuery('select sub.id from ProviderRole pr join pr.subscription sub join pr.provider pro where sub.id in (:idList) and pro.id = :d', [idList: idList, d: d[0]])
                    List b = Subscription.executeQuery('select sub.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.provider pro where sub.id in (:idList) and pro.id = :d', [idList: idList, d: d[0]])

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery('select sub.id, sub.name from Subscription sub where sub.id in (:idList) order by sub.name', [idList: (a + b)]).collect{ it[0] }, // todo
                            value2: a.size(),
                            value1: b.size()
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select sub.id from Subscription sub where sub.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData2Values_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['subscription-x-vendor']) {

                List vendorIdList = BaseFilter.getCachedFilterIdList('vendor', params)

                List aList = Vendor.executeQuery(
                        'select v.id, v.name, count(*) from VendorRole vr join vr.vendor v where v.id in (:vendorIdList) and vr.subscription.id in (:idList) group by v.id order by v.name',
                        [vendorIdList: vendorIdList, idList: idList]
                )
                List bList = Vendor.executeQuery(
                        'select v.id, v.name, count(*) from SubscriptionPackage subPkg join subPkg.pkg pkg join pkg.vendors pv join pv.vendor v where v.id in (:vendorIdList) and subPkg.subscription.id in (:idList) group by v.id order by v.name',
                        [vendorIdList: vendorIdList, idList: idList]
                )
                result.data = []

                // sorting
                Vendor.executeQuery('select id, name from Vendor where id in (:vendorIdList) order by name', [vendorIdList: vendorIdList]).each{ p ->
                    List tmp = [null, null, 0, 0]
                    aList.each { a -> if (p[0] == a[0]) {
                        tmp[0] = a[0]
                        tmp[1] = a[1]
                        tmp[2] = a[2]
                    } }
                    bList.each { b -> if (p[0] == b[0]) {
                        tmp[0] = b[0]
                        tmp[1] = b[1]
                        tmp[3] = b[2]
                    } }

                    if (tmp[0]) { result.data << tmp }
                }

                result.data.each { d ->
                    List a = Subscription.executeQuery('select sub.id from VendorRole vr join vr.subscription sub join vr.vendor v where sub.id in (:idList) and v.id = :d', [idList: idList, d: d[0]])
                    List b = Subscription.executeQuery('select sub.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.vendors pv join pv.vendor v where sub.id in (:idList) and v.id = :d', [idList: idList, d: d[0]])

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Subscription.executeQuery('select sub.id, sub.name from Subscription sub where sub.id in (:idList) order by sub.name', [idList: (a + b)]).collect{ it[0] }, // todo
                            value2: a.size(),
                            value1: b.size()
                    ])
                }

                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select sub.id from Subscription sub where sub.id in (:idList)', [idList: nonMatchingIdList]) : []

                handleGenericNonMatchingData2Values_TMP(params.query, BaseQuery.NO_VENDOR_LABEL, noDataList, result)
            }
//            else if (params.query in ['subscription-x-provider']) {
//
//                // TODO
//                // TODO subscription > subscriptionPackage > package > provider ?
//                // TODO
//
//
//                result.data = Provider.executeQuery(
//                        'select pro.id, pro.name, count(*) from ProviderRole pr join pr.provider pro where pro.id in (:providerIdList) and pr.subscription.id in (:idList) group by pro.id order by pro.name',
//                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
//                )
//                result.data.each { d ->
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : d[0],
//                            label : d[1],
//                            idList: Subscription.executeQuery(
//                                    'select sub.id from ProviderRole pr join pr.subscription sub join pr.provider pro where sub.id in (:idList) and pro.id = :d order by sub.name',
//                                    [idList: idList, d: d[0]]
//                            ),
//                            value2: a.size(),
//                            value1: b.size()
//                    ])
//                }
//
//                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
//                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select sub.id from Subscription sub where sub.id in (:idList)', [idList: nonMatchingIdList]) : []
//
//                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_PROVIDER_LABEL, noDataList, result)
//            }
//            else if (params.query in ['subscription-x-vendor']) {
//
//                // TODO
//                // TODO subscription > subscriptionPackage > package > packageVendor > vendor ?
//                // TODO
//
//                result.data = Vendor.executeQuery(
//                        'select v.id, v.name, count(*) from VendorRole vr join vr.vendor v where v.id in (:vendorIdList) and vr.subscription.id in (:idList) group by v.id order by v.name',
//                        [vendorIdList: BaseFilter.getCachedFilterIdList('vendor', params), idList: idList]
//                )
//                result.data.each { d ->
//                    result.dataDetails.add([
//                            query : params.query,
//                            id    : d[0],
//                            label : d[1],
//                            idList: Subscription.executeQuery(
//                                    'select sub.id from VendorRole vr join vr.subscription sub join vr.vendor v where sub.id in (:idList) and v.id = :d order by sub.name',
//                                    [idList: idList, d: d[0]]
//                            )
//                    ])
//                }
//
//                List<Long> nonMatchingIdList = idList.minus(result.dataDetails.collect { it.idList }.flatten())
//                List<Long> noDataList = nonMatchingIdList ? Subscription.executeQuery('select sub.id from Subscription sub where sub.id in (:idList)', [idList: nonMatchingIdList]) : []
//
//                handleGenericNonMatchingData1Value_TMP(params.query, BaseQuery.NO_VENDOR_LABEL, noDataList, result)
//            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                REFDATA_QUERY[0] + 'from Subscription s join s.' + refdata + ' ref where s.id in (:idList)' + REFDATA_QUERY[1],
                'select s.id from Subscription s join s.' + refdata + ' ref where s.id in (:idList) and ref.id = :d order by s.name',
                'select distinct s.id from Subscription s where s.id in (:idList) and s.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
