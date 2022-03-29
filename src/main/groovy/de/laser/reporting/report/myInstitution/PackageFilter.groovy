package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.Org
import de.laser.Package
import de.laser.Platform
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.BeanStore
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class PackageFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (pkg.id) from Package pkg']
        List<String> whereParts         = [ 'where pkg.id in (:packageIdList)']
        Map<String, Object> queryParams = [ packageIdList: [] ]

        ContextService contextService = BeanStore.getContextService()

        String filterSource = getCurrentFilterSource(params, BaseConfig.KEY_PACKAGE)
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_PACKAGE, filterSource)])

        switch (filterSource) {
            case 'all-pkg':
                queryParams.packageIdList = Package.executeQuery( 'select pkg.id from Package pkg' )
//                queryParams.packageIdList = Package.executeQuery( 'select pkg.id from Package pkg where pkg.packageStatus != :pkgStatus',
//                        [pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
//                )
                break
            case 'my-pkg':
                List<Long> subIdList = Subscription.executeQuery(
                        "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx))",
                        [roleTypes: [ RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS ], ctx: contextService.getOrg()])

                queryParams.packageIdList = Package.executeQuery(
                        'select distinct subPkg.pkg.id from SubscriptionPackage subPkg where subPkg.subscription.id in (:subIdList)', [subIdList: subIdList]
                )
//                queryParams.packageIdList = Package.executeQuery(
//                        'select distinct subPkg.pkg.id from SubscriptionPackage subPkg where subPkg.subscription.id in (:subIdList) and subPkg.pkg.packageStatus != :pkgStatus',
//                        [subIdList: subIdList, pkgStatus: RDStore.PACKAGE_STATUS_DELETED]
//                )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + BaseConfig.KEY_PACKAGE + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Package.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'pkg.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Package.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'pkg.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'pkg.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'pkg.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' --- ' + pType +' not implemented --- '
                }
                // --> custom implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == 'nominalPlatform') {
                        Long[] pList = params.list(key).collect{ Long.parseLong(it) }

                        queryParts.add('Platform plt')
                        whereParts.add('pkg.nominalPlatform = plt and plt.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        filterLabelValue = Platform.getAll(pList).collect{ it.name }
                    }
                    else if (p == 'orProvider') {
                        Long[] pList = params.list(key).collect{ Long.parseLong(it) }

                        queryParts.add('OrgRole ro')
                        whereParts.add('ro.pkg = pkg and ro.org.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)
                        whereParts.add('ro.roleType in (:p'  + (++pCount) + ')')
                        queryParams.put('p' + pCount, [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER])

                        filterLabelValue = Org.getAll(pList).collect{ it.name }
                    }
                    else if (p == 'subscriptionStatus') {
                        Long[] pList = params.list(key).collect{ Long.parseLong(it) }

                        queryParts.add('Subscription sub')
                        whereParts.add('sub.status.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        queryParts.add('SubscriptionPackage subPkg')
                        whereParts.add('subPkg.subscription = sub and subPkg.pkg = pkg')

                        queryParts.add('OrgRole ro')
                        whereParts.add('ro.roleType in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS ])
                        whereParts.add('ro.org = :p' + (++pCount) + ' and ro.sub = sub')
                        queryParams.put('p' + pCount, contextService.getOrg())

                        filterLabelValue = RefdataValue.getAll(pList).collect{ it.getI10n('value') }
                    }
                    else {
                        println ' --- ' + pType + ' not implemented --- '
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'PackageFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        List<Long> packageIdList = queryParams.packageIdList ? Package.executeQuery( query, queryParams ) : []
        filterResult.data.put(BaseConfig.KEY_PACKAGE + 'IdList', packageIdList)

        // -- SUB --

        // println filterResult.data.get('packageIdList')

        BaseConfig.getCurrentConfig( BaseConfig.KEY_PACKAGE ).keySet().each{ pk ->
            if (pk != 'base') {
                if (pk == 'provider') {
                    _handleInternalOrgFilter(pk, filterResult)
                }
                else if (pk == 'platform') {
                    _handleInternalPlatformFilter(pk, filterResult)
                }
            }
        }

        // -- ES --

        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PACKAGE, cmbKey, packageIdList, filterResult, params )

        List<Long> platformIdList = GenericHelper.getFilterResultDataIdList( filterResult, BaseConfig.KEY_PLATFORM )
        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PLATFORM, ElasticSearchHelper.IGNORE_FILTER, platformIdList, filterResult, params )

        filterResult
    }

    static void _handleInternalOrgFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (org.id) from OrgRole ro join ro.pkg pkg join ro.org org'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)', 'ro.roleType in (:roleTypes)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList, roleTypes: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER] ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? Org.executeQuery(query, queryParams) : [] )
    }

    static void _handleInternalPlatformFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (plt.id) from Package pkg join pkg.nominalPlatform plt'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? Platform.executeQuery(query, queryParams) : [] )
    }
}
