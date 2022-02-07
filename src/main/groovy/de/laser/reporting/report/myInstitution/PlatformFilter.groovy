package de.laser.reporting.report.myInstitution

import de.laser.*
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.report.ElasticSearchHelper
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.reporting.report.myInstitution.config.PlatformXCfg
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class PlatformFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (plt.id) from Platform plt']
        List<String> whereParts         = [ 'where plt.id in (:platformIdList)']
        Map<String, Object> queryParams = [ platformIdList: [] ]

        ApplicationContext mainContext = Holders.grailsApplication.mainContext
        ContextService contextService  = mainContext.getBean('contextService')

        String filterSource = getCurrentFilterSource(params, BaseConfig.KEY_PLATFORM)
        filterResult.labels.put('base', [source: BaseConfig.getMessage(BaseConfig.KEY_PLATFORM + '.source.' + filterSource)])

        switch (filterSource) {
            case 'all-plt':
                queryParams.platformIdList = Platform.executeQuery( 'select plt.id from Platform plt where plt.status != :status',
                        [status: RDStore.PLATFORM_STATUS_DELETED]
                )
                break
            case 'my-plt':
                List<Long> subIdList = Subscription.executeQuery(
                        "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx)) and s.status.value != 'Deleted'",
                        [roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN], ctx: contextService.getOrg()])

                queryParams.platformIdList = Platform.executeQuery(
                        "select distinct plt.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg, " +
                        "TitleInstancePackagePlatform tipp join tipp.platform plt where tipp.pkg = pkg and sub.id in (:subIdList) " +
                        "and (pkg.packageStatus is null or pkg.packageStatus != :pkgDeleted) " +
                        "and (tipp.status is null or tipp.status != :tippDeleted) " +
                        "and plt.status != :status",
                        [status: RDStore.PLATFORM_STATUS_DELETED, subIdList: subIdList, pkgDeleted: RDStore.PACKAGE_STATUS_DELETED, tippDeleted: RDStore.TIPP_STATUS_DELETED]
                )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + BaseConfig.KEY_PLATFORM + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base, p)
                String pEsData = BaseConfig.KEY_PLATFORM + '-' + p

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
                    println ' --- ' + pType +' not implemented --- '
                }
                // --> custom implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    if (p == BaseConfig.CUSTOM_IMPL_KEY_PLT_ORG) {
                        Long[] pList = params.list(key).collect{ Long.parseLong(it) }

                        whereParts.add( 'plt.' + p + '.id in (:p' + (++pCount) + ')')
                        queryParams.put( 'p' + pCount, pList )

                        filterLabelValue = Org.getAll(pList).collect{ it.name }
                    }
                    else if (p == BaseConfig.CUSTOM_IMPL_KEY_PLT_SERVICEPROVIDER) {
                        whereParts.add( 'plt.' + p + '.id = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.long(key) )

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == BaseConfig.CUSTOM_IMPL_KEY_PLT_SOFTWAREPROVIDER) {
                        whereParts.add( 'plt.' + p + '.id = :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, params.long(key) )

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
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

        List<Long> idList = queryParams.platformIdList ? Platform.executeQuery( query, queryParams ) : []
        // println 'local matches: ' + idList.size()

        // -- ES --

        ElasticSearchHelper.handleEsRecords( BaseConfig.KEY_PLATFORM, idList, cmbKey, filterResult, params )

        // -- SUB --

        BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).keySet().each{ pk ->
            if (pk == 'provider') {
                _handleInternalOrgFilter(params, pk, filterResult)
            }
        }

        filterResult
    }

    static void _handleInternalOrgFilter(GrailsParameterMap params, String partKey, Map<String, Object> filterResult) {

        String filterSource = getCurrentFilterSource(params, partKey)

        if (filterSource && ! filterSource.startsWith('filter-depending-')) {
            filterResult.labels.put(partKey, [source: BaseConfig.getMessage(BaseConfig.KEY_PACKAGE + '.source.' + filterSource )])
        }

        if (! filterResult.data.get('platformIdList')) {
            filterResult.data.put( partKey + 'IdList', [] )
        }

        String query = 'select distinct (plt.org.id) from Platform plt where plt.id in (:platformIdList)'
        Map<String, Object> queryParams = [ platformIdList: filterResult.data.platformIdList ]

        filterResult.data.put( partKey + 'IdList', queryParams.platformIdList ? Org.executeQuery(query, queryParams) : [] )
    }
}
