package de.laser.reporting.report.myInstitution

import de.laser.*
import de.laser.storage.BeanStore
import de.laser.helper.DateUtils
import de.laser.storage.RDStore
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@Slf4j
class PlatformFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (plt.id) from Platform plt']
        List<String> whereParts         = [ 'where plt.id in (:platformIdList)']
        Map<String, Object> queryParams = [ platformIdList: [] ]

        ContextService contextService = BeanStore.getContextService()

        String filterSource = getCurrentFilterSource(params, BaseConfig.KEY_PLATFORM)
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_PLATFORM, filterSource)])

        switch (filterSource) {
            case 'all-plt':
                queryParams.platformIdList = Platform.executeQuery( 'select plt.id from Platform plt')
//                queryParams.platformIdList = Platform.executeQuery( 'select plt.id from Platform plt where plt.status != :status',
//                        [status: RDStore.PLATFORM_STATUS_DELETED]
//                )
                break
            case 'my-plt':
                List<Long> subIdList = Subscription.executeQuery(
                        "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx))",
                        [roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS], ctx: contextService.getOrg()])

//                queryParams.platformIdList = Platform.executeQuery(
//                        "select distinct plt.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg, " +
//                        "TitleInstancePackagePlatform tipp join tipp.platform plt where tipp.pkg = pkg " +
//                        "and sub.id in (:subIdList) " +
//                        "and (pkg.packageStatus is null or pkg.packageStatus != :pkgDeleted) " +
//                        "and (tipp.status is null or tipp.status != :tippDeleted) " +
//                        "and plt.status != :pltStatus",
//                        [subIdList: subIdList, pkgDeleted: RDStore.PACKAGE_STATUS_DELETED, tippDeleted: RDStore.TIPP_STATUS_DELETED, pltStatus: RDStore.PLATFORM_STATUS_DELETED]
//                )
                queryParams.platformIdList = Platform.executeQuery(
                        "select distinct plt.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.nominalPlatform plt " +
                                "where sub.id in (:subIdList)",
                        [subIdList: subIdList]
                )
//                queryParams.platformIdList = Platform.executeQuery(
//                        "select distinct plt.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.nominalPlatform plt " +
//                        "where sub.id in (:subIdList) " +
//                        "and (pkg.packageStatus is null or pkg.packageStatus != :pkgDeleted) and plt.status != :pltStatus",
//                        [subIdList: subIdList, pkgDeleted: RDStore.PACKAGE_STATUS_DELETED, pltStatus: RDStore.PLATFORM_STATUS_DELETED]
//                )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + BaseConfig.KEY_PLATFORM + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                // println '* PlatformFilter.filter() ' + p
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Platform.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'plt.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Platform.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'plt.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'plt.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'plt.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    log.info ' --- ' + pType +' not implemented --- '
                }
                // --> custom implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    if (p == 'org') {
                        Long[] pList = params.list(key).collect{ Long.parseLong(it) }

                        whereParts.add( 'plt.org.id in (:p' + (++pCount) + ')')
                        queryParams.put( 'p' + pCount, pList )

                        filterLabelValue = Org.getAll(pList).collect{ it.name }
                    }
                    else if (p == 'serviceProvider') {
                        whereParts.add( 'plt.serviceProvider.id = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.long(key) )

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == 'softwareProvider') {
                        whereParts.add( 'plt.softwareProvider.id = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.long(key) )

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == 'packageStatus') {
                        queryParts.add('Subscription sub')

                        queryParts.add('Package pkg')
                        whereParts.add('pkg.nominalPlatform = plt')
                        whereParts.add('pkg.packageStatus.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        queryParts.add('SubscriptionPackage subPkg')
                        whereParts.add('subPkg.subscription = sub and subPkg.pkg = pkg')

                        queryParts.add('OrgRole ro')
                        whereParts.add('ro.roleType in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS ])
                        whereParts.add('ro.org = :p' + (++pCount) + ' and ro.sub = sub')
                        queryParams.put('p' + pCount, contextService.getOrg())

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == 'subscriptionStatus') {
                        Long[] pList = params.list(key).collect{ Long.parseLong(it) }

                        queryParts.add('Subscription sub')
                        whereParts.add('sub.status.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        queryParts.add('Package pkg')
                        whereParts.add('pkg.nominalPlatform = plt')

                        queryParts.add('SubscriptionPackage subPkg')
                        whereParts.add('subPkg.subscription = sub and subPkg.pkg = pkg')

                        queryParts.add('OrgRole ro')
                        whereParts.add('ro.roleType in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS ])
                        whereParts.add('ro.org = :p' + (++pCount) + ' and ro.sub = sub')
                        queryParams.put('p' + pCount, contextService.getOrg())

                        filterLabelValue = RefdataValue.getAll(pList).collect{ it.getI10n('value') }
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'PlatformFilter.filter() -->' // TODO
//        println query
//        println queryParams
//        println whereParts

        List<Long> platformIdList = queryParams.platformIdList ? Platform.executeQuery( query, queryParams ) : []
        filterResult.data.put(BaseConfig.KEY_PLATFORM + 'IdList', platformIdList)

        // -- SUB --

        BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).keySet().each{ pk ->
            if (pk == 'provider') {
                _handleInternalOrgFilter(pk, filterResult)
            }
        }

        // -- ES --

        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PLATFORM, cmbKey, platformIdList, filterResult, params )

        filterResult
    }

    static void _handleInternalOrgFilter(String partKey, Map<String, Object> filterResult) {
        String query = 'select distinct (plt.org.id) from Platform plt where plt.id in (:platformIdList)'
        Map<String, Object> queryParams = [ platformIdList: filterResult.data.platformIdList ]

        filterResult.data.put( partKey + 'IdList', queryParams.platformIdList ? Org.executeQuery(query, queryParams) : [] )
    }
}
