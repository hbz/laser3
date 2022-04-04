package de.laser.reporting.report.myInstitution


import de.laser.ContextService
import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.Subscription
import de.laser.storage.BeanStorage
import de.laser.helper.RDStore
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.base.BaseQuery
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Deprecated
class IssueEntitlementQuery extends BaseQuery {

    static List<String> PROPERTY_QUERY = [ 'select p.id, p.value_de, count(*) ', ' group by p.id, p.value_de order by p.value_de' ]

    static Map<String, Object> query(GrailsParameterMap params) {

        ContextService contextService = BeanStorage.getContextService()
        MessageSource messageSource = BeanStorage.getMessageSource()
        Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> result = getEmptyResult( params.query, params.chart )

        def (String prefix, String suffix) = params.query.split('-') // only simply cfg.query
        List<Long> idList = BaseFilter.getCachedFilterIdList(prefix, params)

        //println 'IssueEntitlementQuery.query() -> ' + params.query + ' : ' + suffix

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

            List<Long> packageIdList      = BaseFilter.getCachedFilterIdList('package', params) // filter is set
            List<Long> platformIdList     = BaseFilter.getCachedFilterIdList('platform', params) // filter is set
            List<Long> providerIdList     = BaseFilter.getCachedFilterIdList('provider', params) // filter is set
            List<Long> subscriptionIdList = BaseFilter.getCachedFilterIdList('subscription', params) // filter is set

            if (params.query in ['issueEntitlement-x-pkg']) {

                result.data = idList ? Package.executeQuery(
                        'select distinct pkg.id, pkg.name, count(ie.id) from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg where ' +
                                'ie.id in (:idList) and pkg.id in (:packageIdList) group by pkg.id order by pkg.name',
                        [idList: idList, packageIdList: packageIdList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: IssueEntitlement.executeQuery(
                                    'select distinct ie.id from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg ' +
                                            'where ie.id in (:idList) and pkg.id in (:packageIdList) and pkg.id = :d',
                                    [idList: idList, packageIdList: packageIdList, d: d[0]]
                            )
                    ])
                }
            }
            else if (params.query in ['issueEntitlement-x-provider']) {
                result.data = idList ? Org.executeQuery(
                        'select distinct o.id, o.name, count(*) from Org o join o.links orgLink where ' +
                                'o.id in (:providerIdList) and orgLink.pkg.id in (:packageIdList) group by o.id order by o.name',
                        [providerIdList: providerIdList, packageIdList: packageIdList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ' +
                                            'ro.roleType in (:prov) and pkg.id in (:packageIdList) and o.id = :d order by pkg.name',
                                    [packageIdList: packageIdList, prov: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER], d: d[0]]
                            ),
                            value2: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ' +
                                            'ro.roleType = :prov and pkg.id in (:packageIdList) and o.id = :d order by pkg.name',
                                    [packageIdList: packageIdList, prov: RDStore.OR_CONTENT_PROVIDER, d: d[0]]
                            ).size(),
                            value1: Package.executeQuery(
                                    'select pkg.id from Package pkg join pkg.orgs ro join ro.org o where ' +
                                            'ro.roleType = :prov and pkg.id in (:packageIdList) and o.id = :d order by pkg.name',
                                    [packageIdList: packageIdList, prov: RDStore.OR_PROVIDER, d: d[0]] // !!!!
                            ).size()
                    ])
                }
            }
            else if (params.query in ['issueEntitlement-x-platform']) {

                result.data = idList ? Platform.executeQuery(
                        'select distinct plt.id, plt.name, count(ie.id) from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg join pkg.nominalPlatform plt where ' +
                                'ie.id in (:idList) and plt.id in (:platformIdList) and pkg.id in (:packageIdList) group by plt.id order by plt.name',
                        [idList: idList, platformIdList: platformIdList, packageIdList: packageIdList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: IssueEntitlement.executeQuery(
                                    'select distinct ie.id from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg join pkg.nominalPlatform plt ' +
                                            'where ie.id in (:idList) and pkg.id in (:packageIdList) and plt.id in (:platformIdList) and plt.id = :d',
                                    [idList: idList, platformIdList: platformIdList, packageIdList: packageIdList, d: d[0]]
                            )
                    ])
                }
            }
            else if (params.query in ['issueEntitlement-x-platformProvider']) {

                result.data = idList ? Platform.executeQuery(
                        'select distinct pltOrg.id, pltOrg.name, count(ie.id) from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg join pkg.nominalPlatform plt join plt.org pltOrg where ' +
                                'ie.id in (:idList) and plt.id in (:platformIdList) and pkg.id in (:packageIdList) group by pltOrg.id order by pltOrg.name',
                        [idList: idList, platformIdList: platformIdList, packageIdList: packageIdList]
                ) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: IssueEntitlement.executeQuery(
                                    'select distinct ie.id from IssueEntitlement ie join ie.tipp tipp join tipp.pkg pkg join pkg.nominalPlatform plt ' +
                                            'where ie.id in (:idList) and pkg.id in (:packageIdList) and plt.id in (:platformIdList) and plt.org.id = :d',
                                    [idList: idList, platformIdList: platformIdList, packageIdList: packageIdList, d: d[0]]
                            )
                    ])
                }
            }
            else if (params.query in ['issueEntitlement-x-subscription']) {

                result.data = idList ? Subscription.executeQuery(
                        'select sub.id, concat(sub.name, \' (ID:\', sub.id,\')\'), count(ie.id) from IssueEntitlement ie join ie.subscription sub ' +
                                'where ie.id in (:idList) and sub.id in (:subscriptionIdList) group by sub.id order by sub.name, sub.id',
                        [idList: idList, subscriptionIdList: subscriptionIdList]) : []

                result.data.each { d ->
                    result.dataDetails.add([
                            query : params.query,
                            id    : d[0],
                            label : d[1],
                            idList: IssueEntitlement.executeQuery(
                                    'select distinct ie.id from IssueEntitlement ie where ie.id in (:idList) and ie.subscription.id = :d',
                                    [idList: idList, d: d[0]]
                            )
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
