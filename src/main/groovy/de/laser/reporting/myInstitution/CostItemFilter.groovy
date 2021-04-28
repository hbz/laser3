package de.laser.reporting.myInstitution

import de.laser.FinanceService
import de.laser.Org
import de.laser.RefdataValue
import de.laser.ctrl.FinanceControllerService
import de.laser.finance.CostItem
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.myInstitution.base.BaseConfig
import de.laser.reporting.myInstitution.base.BaseFilter
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.ApplicationContext

class CostItemFilter extends BaseFilter {

    def contextService
    def filterService
    def subscriptionsQueryService

    CostItemFilter() {
        ApplicationContext mainContext  = Holders.grailsApplication.mainContext
        contextService                  = mainContext.getBean('contextService')
        filterService                   = mainContext.getBean('filterService')
        subscriptionsQueryService       = mainContext.getBean('subscriptionsQueryService')
    }

    Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (ci.id) from CostItem ci']
        List<String> whereParts         = [ 'where ci.id in (:costItemIdList)']
        Map<String, Object> queryParams = [ costItemIdList: [] ]

        String filterSource = params.get(BaseConfig.FILTER_PREFIX + 'costItem' + BaseConfig.FILTER_SOURCE_POSTFIX)
        filterResult.labels.put('base', [source: getFilterSourceLabel(CostItemConfig.getCurrentConfig().base, filterSource)])

        switch (filterSource) {
            case 'consortia-cost':
                FinanceService financeService = (FinanceService) Holders.grailsApplication.mainContext.getBean('financeService')
                FinanceControllerService financeControllerService = (FinanceControllerService) Holders.grailsApplication.mainContext.getBean('financeControllerService')

                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('max', 1000000 as String)
                financeControllerService.getResultGenerics(clone) // [filterPresets:[filterSubStatus:Current], filterSet:true, ..

                Map<String, Object> financialData = financeService.getCostItems(params, financeControllerService.getResultGenerics(clone))
                //println financialData

                queryParams.costItemIdList = financialData.get('cons').get('costItems').collect { it.id as Long }
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'costItem_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(CostItemConfig.getCurrentConfig().base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Org.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'ci.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Org.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        if (RefdataValue.get(params.get(key)) == RDStore.YN_YES) {
                            whereParts.add( 'ci.' + p + ' is true' )
                        }
                        else if (RefdataValue.get(params.get(key)) == RDStore.YN_NO) {
                            whereParts.add( 'ci.' + p + ' is false' )
                        }
                        filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'ci.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.get(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    println ' ------------ not implemented ------------ '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    println ' ------------ not implemented ------------ '
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(CostItemConfig.getCurrentConfig().base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

        println 'CostItemFilter.filter() -->'

//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('costItemIdList', CostItem.executeQuery( query, queryParams ))

        println 'costItems (raw) >> ' + queryParams.costItemIdList.size()
        println 'costItems (queried) >> ' + filterResult.data.costItemIdList.size()

        filterResult
    }
}
