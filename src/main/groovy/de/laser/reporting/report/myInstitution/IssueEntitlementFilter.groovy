package de.laser.reporting.report.myInstitution

import de.laser.*
import de.laser.storage.BeanStorage
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap

// not ready for use
@Deprecated
class IssueEntitlementFilter extends BaseFilter {

    static int TMP_QUERY_CONSTRAINT = 30000

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (ie.id) from IssueEntitlement ie']
        List<String> whereParts         = [ 'where ie.id in (:issueEntitlementIdList)']
        Map<String, Object> queryParams = [ issueEntitlementIdList: [] ]

        ContextService contextService = BeanStorage.getContextService()
        SubscriptionsQueryService subscriptionsQueryService = BeanStorage.getSubscriptionsQueryService()

        String filterSource = getCurrentFilterSource(params, BaseConfig.KEY_ISSUEENTITLEMENT)
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_ISSUEENTITLEMENT, filterSource)])

        switch (filterSource) {
//            case 'all-ie':
//                queryParams.issueEntitlementIdList = IssueEntitlement.executeQuery( 'select ie.id from IssueEntitlement ie' )
//                break
            case 'my-ie':
                List tmp = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([validOn: null], contextService.getOrg())
                List<Long> subIdList = Subscription.executeQuery( 'select s.id ' + tmp[0], tmp[1])
                subIdList = Subscription.executeQuery( "select s.id from Subscription s where s.id in (:subIdList)", [subIdList: subIdList])

                queryParams.issueEntitlementIdList = IssueEntitlement.executeQuery(
                        'select distinct(ie.id) from IssueEntitlement ie join ie.subscription sub where sub.id in (:subscriptionIdList) ' +
                                'and ie.status in (:ieStatus) and ie.acceptStatus = :ieAcceptStatus',
                        [subscriptionIdList: subIdList, ieAcceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: [
                                RDStore.TIPP_STATUS_CURRENT, RDStore.TIPP_STATUS_EXPECTED, RDStore.TIPP_STATUS_RETIRED ]
                        ]
                )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + BaseConfig.KEY_ISSUEENTITLEMENT + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (IssueEntitlement.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'ie.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (IssueEntitlement.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'ie.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'ie.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'ie.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' --- ' + pType +' not implemented --- '
                }
                // --> custom implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    if (p == 'package') {
                        queryParts.add('TitleInstancePackagePlatform tipp')
                        whereParts.add('ie.tipp = tipp and tipp.pkg.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key) )

                        filterLabelValue = de.laser.Package.get(params.long(key)).name
                    }
                    else if (p == 'packageNominalPlatform') {
                        queryParts.add('TitleInstancePackagePlatform tipp')
                        queryParts.add('Package pkg')  // status !!
                        whereParts.add('ie.tipp = tipp and tipp.pkg = pkg and pkg.nominalPlatform.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key) )

                        filterLabelValue = Platform.get(params.long(key)).name
                    }
                    else if (p == 'orProvider') {
                        queryParts.add('TitleInstancePackagePlatform tipp')
                        queryParts.add('Package pkg')  // status !!
                        queryParts.add('OrgRole ro')

                        whereParts.add('ie.tipp = tipp and tipp.pkg = ro.pkg and ro.org.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        whereParts.add('ro.roleType in (:p'  + (++pCount) + ')')
                        queryParams.put('p' + pCount, [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER])

                        filterLabelValue = Org.get(params.long(key)).name
                    }
                    else if (p == 'status') {
                        whereParts.add( 'ie.status.id = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.long(key) )

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == 'subscription') {
                        whereParts.add('ie.subscription.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key) )

                        filterLabelValue = Subscription.get(params.long(key)).getLabel()
                    }
                    else if (p == 'packageStatus') {
                        queryParts.add('TitleInstancePackagePlatform tipp')
                        queryParts.add('Package pkg')

                        whereParts.add('ie.tipp = tipp and tipp.pkg = pkg and pkg.packageStatus.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == 'subscriptionStatus') {
                        queryParts.add('Subscription sub')
                        whereParts.add('ie.subscription = sub and sub.status.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else {
                        println ' --- ' + pType + ' not implemented --- '
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

        //println 'IssueEntitlementFilter.filter() -->'
        //println query
        //println queryParams
        //println whereParts

        filterResult.data.put(BaseConfig.KEY_ISSUEENTITLEMENT + 'IdList', _handleLargeQuery(query, queryParams, 'issueEntitlementIdList').take( TMP_QUERY_CONSTRAINT ))

        BaseConfig.getCurrentConfig( BaseConfig.KEY_ISSUEENTITLEMENT ).keySet().each{ pk ->
            if (pk != 'base') {
                if (pk == 'subscription') {
                    _handleInternalSubscriptionFilter(pk, filterResult)
                }
                else if (pk == 'package') {
                    _handleInternalPackageFilter(pk, filterResult)
                }
                else if (pk == 'provider') {
                    _handleInternalOrgFilter(pk, filterResult)
                }
                else if (pk == 'platform') {
                    _handleInternalPlatformFilter(pk, filterResult)
                }
            }
        }

        // -- ES --

        List<Long> packageIdList = GenericHelper.getFilterResultDataIdList( filterResult, BaseConfig.KEY_PACKAGE )
        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PACKAGE, ElasticSearchHelper.IGNORE_FILTER, packageIdList, filterResult, params )

        List<Long> platformIdList = GenericHelper.getFilterResultDataIdList( filterResult, BaseConfig.KEY_PLATFORM )
        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PLATFORM, ElasticSearchHelper.IGNORE_FILTER, platformIdList, filterResult, params )

        filterResult
    }

    static void _handleInternalSubscriptionFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct(ie.subscription.id) from IssueEntitlement ie'
        List<String> whereParts = [ 'ie.id in (:issueEntitlementIdList)' ]

        Map<String, Object> queryParams = [ issueEntitlementIdList: filterResult.data.issueEntitlementIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.issueEntitlementIdList ? _handleLargeQuery(query, queryParams, 'issueEntitlementIdList') : [] )
    }

    static void _handleInternalPackageFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (pkg.id) from SubscriptionPackage subPkg join subPkg.pkg pkg join subPkg.subscription sub join sub.issueEntitlements ie'
        List<String> whereParts = [ 'ie.id in (:issueEntitlementIdList)' ]

        Map<String, Object> queryParams = [ issueEntitlementIdList: filterResult.data.issueEntitlementIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.issueEntitlementIdList ? _handleLargeQuery(query, queryParams, 'issueEntitlementIdList') : [] )
    }

    static void _handleInternalOrgFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (org.id) from OrgRole ro join ro.pkg pkg join ro.org org'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)', 'ro.roleType in (:roleTypes)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList, roleTypes: [RDStore.OR_PROVIDER, RDStore.OR_CONTENT_PROVIDER] ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? _handleLargeQuery(query, queryParams, 'packageIdList') : [] )
    }

    static void _handleInternalPlatformFilter(String partKey, Map<String, Object> filterResult) {
        String queryBase = 'select distinct (plt.id) from Package pkg join pkg.nominalPlatform plt'
        List<String> whereParts = [ 'pkg.id in (:packageIdList)' ]

        Map<String, Object> queryParams = [ packageIdList: filterResult.data.packageIdList ]

        String query = queryBase + ' where ' + whereParts.join(' and ')
        filterResult.data.put( partKey + 'IdList', queryParams.packageIdList ? _handleLargeQuery(query, queryParams, 'packageIdList') : [] )
    }

    static def _handleLargeQuery(String query, Map queryParams, String idListName) {
        Set<Long> tmpIdSet = []

        IssueEntitlement.withTransaction {
            List<Long> tmpIdList = queryParams.getAt( idListName ).clone() as List<Long>
            while (tmpIdList) {
                //println '--- ' + query + ' : ' + idListName + ' ---> ' + tmpIdList.size()

                queryParams.putAt( idListName, tmpIdList.take( 10000 ) )
                tmpIdList = tmpIdList.drop( 10000 ) as List<Long>
                tmpIdSet.addAll( IssueEntitlement.executeQuery(query, queryParams) )
            }
        }
        tmpIdSet.sort().toList()
    }
}
