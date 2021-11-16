package de.laser.reporting.report.myInstitution

import de.laser.*
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.report.EsIndexHelper
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

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + 'platform' + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put('base', [source: BaseConfig.getMessage(BaseConfig.KEY_PLATFORM + '.source.' + filterSource)])

        switch (filterSource) {
            case 'all-plt':
                queryParams.platformIdList = Package.executeQuery( 'select plt.id from Platform plt where plt.status != :status',
                        [status: RDStore.PLATFORM_STATUS_DELETED]
                )
                break
            case 'my-plt':
                // TODO
                // TODO
                // TODO
                queryParams.platformIdList = Package.executeQuery( 'select plt.id from Platform plt where plt.status != :status',
                        [status: RDStore.PLATFORM_STATUS_DELETED]
                )
//                List<Long> subIdList = Subscription.executeQuery(
//                        "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx)) and s.status.value != 'Deleted'",
//                        [roleTypes: [
//                                RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN
//                        ], ctx: contextService.getOrg()])
//
//                queryParams.packageIdList = Package.executeQuery(
//                        'select distinct subPkg.pkg.id from SubscriptionPackage subPkg where subPkg.subscription.id in (:idList) and subPkg.pkg.packageStatus != :pkgStatus',
//                        [idList: subIdList, status: RDStore.PLATFORM_STATUS_DELETED]
//                )
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'platform_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PLATFORM ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Org.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'plt.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES) {
                            whereParts.add( 'plt.' + p + ' is true' )
                        }
                        else if (rdv == RDStore.YN_NO) {
                            whereParts.add( 'plt.' + p + ' is false' )
                        }
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
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation != es_data
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    if ( ! PlatformXCfg.ES_DATA.contains( p )) {

                        if (p == BaseConfig.CUSTOM_IMPL_KEY_PLT_SERVICEPROVIDER) {
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

        Map<String, Object> esRecords = [:]
        List<Long> orphanedIdList = []
        boolean esFilterUsed = false

        if (idList) {
            Map<String, Object> esr = EsIndexHelper.getEsPlatformRecords( idList )
            esRecords = esr.records as Map<String, Object>
            orphanedIdList = esr.orphanedIds as List<Long>
            println idList.size()
            println esRecords.size()
            println orphanedIdList.size()
        }

        if (esFilterUsed) {
            idList = orphanedIdList + esRecords.keySet()?.collect{ Long.parseLong(it) } // ????
        }
        filterResult.data.put( BaseConfig.KEY_PLATFORM + 'IdList', idList)
        filterResult.data.put( BaseConfig.KEY_PLATFORM + 'ESRecords', esRecords)
        filterResult.data.put( BaseConfig.KEY_PLATFORM + 'OrphanedIdList', orphanedIdList)

        filterResult
    }
}
