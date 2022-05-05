package de.laser.reporting.report.myInstitution

import de.laser.FinanceService
import de.laser.Org
import de.laser.RefdataValue
import de.laser.ctrl.FinanceControllerService
import de.laser.finance.CostItem
import de.laser.storage.BeanStore
import de.laser.helper.DateUtils
import de.laser.storage.RDStore
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@Slf4j
class CostItemFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (ci.id) from CostItem ci']
        List<String> whereParts         = [ 'where ci.id in (:costItemIdList)']
        Map<String, Object> queryParams = [ costItemIdList: [] ]

        String filterSource = getCurrentFilterSource(params, BaseConfig.KEY_COSTITEM)
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_COSTITEM, filterSource)])

        switch (filterSource) {
            case 'consortia-cost':

                FinanceService financeService = BeanStore.getFinanceService()
                FinanceControllerService financeControllerService = BeanStore.getFinanceControllerService()

                GrailsParameterMap clone = params.clone() as GrailsParameterMap
                clone.setProperty('max', 1000000 as String)
                financeControllerService.getResultGenerics(clone) // [filterPresets:[filterSubStatus:Current], filterSet:true, ..

                Map<String, Object> financialData = financeService.getCostItems(params, financeControllerService.getResultGenerics(clone))
                //println financialData

                queryParams.costItemIdList = financialData.get('cons').get('costItems').collect { it.id as Long }
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + BaseConfig.KEY_COSTITEM + '_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base, p)

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
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES) {
                            whereParts.add( 'ci.' + p + ' is true' )
                        }
                        else if (rdv == RDStore.YN_NO) {
                            whereParts.add( 'ci.' + p + ' is false' )
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
                    whereParts.add( 'ci.' + p + '.id = :p' + (++pCount) )
                    queryParams.put( 'p' + pCount, params.long(key) )

                    filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                }
                // --> refdata join tables
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
                    log.info ' --- ' + pType +' not implemented --- '
                }
                // --> custom filter implementation
                else if (pType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
                    log.info ' --- ' + pType +' not implemented --- '
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_COSTITEM ).base, p), value: filterLabelValue])
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
