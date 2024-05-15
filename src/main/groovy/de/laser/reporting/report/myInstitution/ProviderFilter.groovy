package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.Platform
import de.laser.Provider
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.annotations.UnstableFeature
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@UnstableFeature
@Slf4j
class ProviderFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (pro.id) from Provider pro']
        List<String> whereParts         = [ 'where pro.id in (:providerIdList)']
        Map<String, Object> queryParams = [ providerIdList: [] ]

        ContextService contextService = BeanStore.getContextService()

        String filterSource = getCurrentFilterSource(params, 'provider')
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_PROVIDER, filterSource)])

        switch (filterSource) {
            case 'all-provider':
                queryParams.providerIdList = _getAllProviderIdList()
                break
            case 'my-provider':
                queryParams.providerIdList = _getMyProviderIdList()
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'provider_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_PROVIDER ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Provider.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'pro.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Provider.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'pro.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'pro.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'pro.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    log.info ' --- ' + pType +' not implemented --- '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {

                    if (p == BaseConfig.CI_GENERIC_INVOICING_FORMAT) {
                        queryParts.add('ElectronicBilling elb')
                        whereParts.add('elb.provider = pro and elb.invoicingFormat.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == BaseConfig.CI_GENERIC_INVOICING_DISPATCH) {
                        queryParts.add('InvoiceDispatch dsp')
                        whereParts.add('dsp.provider = pro and dsp.invoiceDispatch.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_PROVIDER ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'ProviderFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('providerIdList', queryParams.providerIdList ? Provider.executeQuery( query, queryParams ) : [])

//        println 'providers >> ' + result.providerIdList.size()

        filterResult
    }

    static List<Long> _getAllProviderIdList() {

        List<Long> idList = Provider.executeQuery(
                'select pro.id from Provider pro',
//                'select pro.id from Provider pro where (pro.status is null or pro.status != :providerStatus)',
//                [providerStatus: RDStore.PROVIDER_STATUS_DELETED]
        )

        idList
    }

    static List<Long> _getMyProviderIdList() {

        ContextService contextService = BeanStore.getContextService()

        List<Long> subIdList = Subscription.executeQuery(
                "select s.id from Subscription s join s.orgRelations ro where (ro.roleType in (:roleTypes) and ro.org = :ctx)",
                [roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS], ctx: contextService.getOrg()])

        List<Long> providerIdList1 = Platform.executeQuery(
                'select distinct pro.id from ProviderRole pr join pr.subscription sub join pr.provider pro where sub.id in (:subIdList)',
                [subIdList: subIdList]
        )
        List<Long> providerIdList2 = Platform.executeQuery(
                'select distinct pro.id from SubscriptionPackage subPkg join subPkg.subscription sub join subPkg.pkg pkg join pkg.provider pro where sub.id in (:subIdList)',
                [subIdList: subIdList]
        )

//        println 'providerIdList1 ' + providerIdList1.size()
//        println 'providerIdList2 ' + providerIdList2.size()
//        println '>>> ' + (providerIdList1 + providerIdList2).unique().size()
//
//        List<Long> idList = Provider.executeQuery( '''
//            select distinct pr.provider.id from ProviderRole pr
//                join pr.subscription sub
//                join sub.orgRelations subOr
//            where (sub = subOr.sub and subOr.org = :org and subOr.roleType in (:subRoleTypes))
//            ''', [
//                org: contextService.getOrg(),
////                providerStatus: RDStore.PROVIDER_STATUS_DELETED,
//                subRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]
//            ]
//        )
//        println 'idList ' + idList.size()
        // and (pr.provider.status is null or pr.provider.status != :providerStatus)
        List<Long> idList = (providerIdList1 + providerIdList2).unique() as List<Long>
        idList
    }
}
