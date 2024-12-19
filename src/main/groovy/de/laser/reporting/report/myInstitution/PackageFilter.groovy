package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.RefdataValue
import de.laser.wekb.Vendor
import de.laser.helper.Params
import de.laser.reporting.report.FilterQueries
import de.laser.storage.BeanStore
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@Slf4j
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
                queryParams.packageIdList = FilterQueries.getAllPackageIdList()
                break
            case 'my-pkg':
                queryParams.packageIdList = FilterQueries.getMyPackageIdList()
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
                    log.info ' --- ' + pType +' not implemented --- '
                }
                // --> custom implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == 'nominalPlatform') {
                        Long[] pList = Params.getLongList(params, key)

                        whereParts.add('pkg.nominalPlatform.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        filterLabelValue = Platform.getAll(pList).collect{ it.name }
                    }
//                    else if (p == 'orProvider') {
//                        Long[] pList = Params.getLongList(params, key)
//
//                        queryParts.add('OrgRole ro')
//                        whereParts.add('ro.pkg = pkg and ro.org.id in (:p' + (++pCount) + ')')
//                        queryParams.put('p' + pCount, pList)
//                        whereParts.add('ro.roleType in (:p'  + (++pCount) + ')')
//                        queryParams.put('p' + pCount, [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER])
//
//                        filterLabelValue = Org.getAll(pList).collect{ it.name }
//                    }
                    else if (p == 'subscriptionStatus') {
                        Long[] pList = Params.getLongList(params, key)

                        queryParts.add('Subscription sub')
                        whereParts.add('sub.status.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        queryParts.add('SubscriptionPackage subPkg')
                        whereParts.add('subPkg.subscription = sub and subPkg.pkg = pkg')

                        queryParts.add('OrgRole ro')
                        whereParts.add('ro.roleType in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER_CONS ])
                        whereParts.add('ro.org = :p' + (++pCount) + ' and ro.sub = sub')
                        queryParams.put('p' + pCount, contextService.getOrg())

                        filterLabelValue = RefdataValue.getAll(pList).collect{ it.getI10n('value') }
                    }
                    else if (p == 'provider') {
                        Long[] pList = Params.getLongList(params, key)

                        whereParts.add('pkg.provider.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        filterLabelValue = Provider.getAll(pList).collect{ it.name }
                    }
                    else if (p == 'vendor') {
                        Long[] pList = Params.getLongList(params, key)

                        queryParts.add('PackageVendor pv')
                        whereParts.add('pv.pkg = pkg and pv.vendor.id in (:p' + (++pCount) + ')')
                        queryParams.put('p' + pCount, pList)

                        filterLabelValue = Vendor.getAll(pList).collect{ it.name }
                    }
                    else {
                        log.info ' --- ' + pType + ' not implemented --- '
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

        // --- subset ---

        // println filterResult.data.get('packageIdList')

        handleExpandoSubsetFilter(this, BaseConfig.KEY_PACKAGE, filterResult, null)

        // -- ES --

        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PACKAGE, cmbKey, packageIdList, filterResult, params )

        List<Long> platformIdList = GenericHelper.getFilterResultDataIdList( filterResult, BaseConfig.KEY_PLATFORM )
        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PLATFORM, ElasticSearchHelper.IGNORE_FILTER, platformIdList, filterResult, params )

        filterResult
    }

    static void _handleSubsetPlatformFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (plt.id) from Package pkg join pkg.nominalPlatform plt'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? Platform.executeQuery(query, queryParams) : [] )
    }

    static void _handleSubsetProviderFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (pro.id) from Package pkg join pkg.provider pro'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? Provider.executeQuery(query, queryParams) : [] )
    }

    static void _handleSubsetVendorFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (pv.vendor.id) from PackageVendor pv join pv.pkg pkg'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? Vendor.executeQuery(query, queryParams) : [] )
    }
}
