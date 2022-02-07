package de.laser.reporting.report.myInstitution


import de.laser.ContextService
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.Subscription
import de.laser.helper.RDStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Deprecated
class IssueEntitlementQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        MessageSource messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        if (! idList) {
        }
        else if ( suffix in ['*']) {

            // TODO
            handleGenericAllQuery(
                    params.query,
                    'select ie.name, ie.name, count(ie.name) from IssueEntitlement ie where ie.id in (:idList) group by ie.name order by ie.name',
                    'select ie.id from IssueEntitlement ie where ie.id in (:idList) and ie.name = :d order by ie.id',
                    idList,
                    result
            )
        }
        else if (suffix in ['status']) {
            _processSimpleRefdataQuery(params.query, suffix, idList, result)
        }
        else if ( suffix in ['x']) {

            if (params.query in ['issueEntitlement-x-pkg']) {
                // TODO
            }
            else if (params.query in ['issueEntitlement-x-provider']) {
                result.data = idList ? Org.executeQuery(
                        'select o.id, o.name, count(*) from Org o join o.links orgLink where o.id in (:providerIdList) and orgLink.pkg.id in (:idList) group by o.id order by o.name',
                        [providerIdList: BaseFilter.getCachedFilterIdList('provider', params), idList: idList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ro.roleType in (:prov) and pkg.id in (:idList) and o.id = :d order by pkg.name',
                                    [idList: idList, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER], d: d[0]]
                            ),
                            value2: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ro.roleType = :prov and pkg.id in (:idList) and o.id = :d order by pkg.name',
                                    [idList: idList, prov: RDStore.OR_CONTENT_PROVIDER, d: d[0]]
                            ).size(),
                            value1: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ro.roleType = :prov and pkg.id in (:idList) and o.id = :d order by pkg.name',
                                    [idList: idList, prov: RDStore.OR_PROVIDER, d: d[0]] // !!!!
                            ).size()
                    ])
                }

                List<Long> noDataList = Package.executeQuery(
                        'select pkg.id from Package pkg where pkg.id in (:idList) and not exists (select ro from OrgRole ro where ro.roleType in (:prov) and ro.pkg.id = pkg.id) order by pkg.name',
                        [idList: idList, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER]]
                )

                handleGenericNonMatchingData2Values_TMP(params.query, NO_PROVIDER_LABEL, noDataList, result)
            }
            else if (params.query in ['issueEntitlement-x-nominalPlatform']) {
                result.data = idList ? Platform.executeQuery(
                        'select p.id, p.name, count(*) from Package pkg join pkg.nominalPlatform p where p.id in (:platformIdList) and pkg.id in (:packageIdList) group by p.id order by p.name',
                        [platformIdList: BaseFilter.getCachedFilterIdList('platform', params), packageIdList: BaseFilter.getCachedFilterIdList('platform', params)]
                ) : []

                result.data.eachWithIndex { d, i ->
                    List<Long> pkgIdList = Package.executeQuery(
                            'select pkg.id from Package pkg join pkg.nominalPlatform p where pkg.id in (:idList) and p.id = :d order by pkg.name',
                            [idList: idList, d: d[0]]
                    )
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: pkgIdList
                    ])
                }

                List<Long> noDataList = idList ? Package.executeQuery(
                        'select distinct pkg.id from Package pkg where pkg.id in (:idList) and pkg.nominalPlatform is null', [idList: idList]
                ) : []
                handleGenericNonMatchingData1Value_TMP(params.query, NO_PLATFORM_LABEL, noDataList, result)
            }
            else if (params.query in ['issueEntitlement-x-subscription']) {
                List<Long> subscriptionIdList = BaseFilter.getCachedFilterIdList('subscription', params) // filter is set

                result.data = idList ? Subscription.executeQuery(
                        'select sub.id, concat(sub.name, \' (ID:\', sub.id,\')\'), count(mbr.id) from Subscription sub where sub.id in (:idList) order by sub.name, sub.id',
                        [idList: subscriptionIdList]) : []

                result.data.each { d ->

                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: subscriptionIdList
                    ])
                }
            }
        }

        result
    }

    static void _processSimpleRefdataQuery(String query, String refdata, List<Long> idList, Map<String, Object> result) {

        handleGenericRefdataQuery(
                query,
                PROPERTY_QUERY[0] + 'from IssueEntitlement ie join ie.' + refdata + ' p where ie.id in (:idList)' + PROPERTY_QUERY[1],
                'select ie.id from IssueEntitlement ie join ie.' + refdata + ' p where ie.id in (:idList) and p.id = :d order by ie.name',
                'select distinct ie.id from IssueEntitlement ie where ie.id in (:idList) and ie.'+ refdata + ' is null',
                idList,
                result
        )
    }
}
