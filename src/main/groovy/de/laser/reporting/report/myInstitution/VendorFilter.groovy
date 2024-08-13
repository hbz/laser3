package de.laser.reporting.report.myInstitution

import de.laser.ContextService
import de.laser.RefdataValue
import de.laser.wekb.Vendor
import de.laser.annotations.UnstableFeature
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.FilterQueries
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.myInstitution.base.BaseFilter
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.util.logging.Slf4j

@UnstableFeature
@Slf4j
class VendorFilter extends BaseFilter {

    static Map<String, Object> filter(GrailsParameterMap params) {
        // notice: params is cloned
        Map<String, Object> filterResult = [ labels: [:], data: [:] ]

        List<String> queryParts         = [ 'select distinct (ven.id) from Vendor ven']
        List<String> whereParts         = [ 'where ven.id in (:vendorIdList)']
        Map<String, Object> queryParams = [ vendorIdList: [] ]

        ContextService contextService = BeanStore.getContextService()

        String filterSource = getCurrentFilterSource(params, 'vendor')
        filterResult.labels.put('base', [source: BaseConfig.getSourceLabel(BaseConfig.KEY_VENDOR, filterSource)])

        switch (filterSource) {
            case 'all-vendor':
                queryParams.vendorIdList = FilterQueries.getAllVendorIdList()
                break
            case 'my-vendor':
                queryParams.vendorIdList = FilterQueries.getMyVendorIdList()
                break
        }

        String cmbKey = BaseConfig.FILTER_PREFIX + 'vendor_'
        int pCount = 0

        getCurrentFilterKeys(params, cmbKey).each { key ->
            //println key + " >> " + params.get(key)

            if (params.get(key)) {
                String p = key.replaceFirst(cmbKey,'')
                String pType = GenericHelper.getFieldType(BaseConfig.getCurrentConfig( BaseConfig.KEY_VENDOR ).base, p)

                def filterLabelValue

                // --> properties generic
                if (pType == BaseConfig.FIELD_TYPE_PROPERTY) {
                    if (Vendor.getDeclaredField(p).getType() == Date) {

                        String modifier = getDateModifier( params.get(key + '_modifier') )

                        whereParts.add( 'ven.' + p + ' ' + modifier + ' :p' + (++pCount) )
                        queryParams.put( 'p' + pCount, DateUtils.parseDateGeneric(params.get(key)) )

                        filterLabelValue = getDateModifier(params.get(key + '_modifier')) + ' ' + params.get(key)
                    }
                    else if (Vendor.getDeclaredField(p).getType() in [boolean, Boolean]) {
                        RefdataValue rdv = RefdataValue.get(params.long(key))

                        if (rdv == RDStore.YN_YES)     { whereParts.add( 'ven.' + p + ' is true' ) }
                        else if (rdv == RDStore.YN_NO) { whereParts.add( 'ven.' + p + ' is false' ) }

                        filterLabelValue = rdv.getI10n('value')
                    }
                    else {
                        queryParams.put( 'p' + pCount, params.get(key) )
                        filterLabelValue = params.get(key)
                    }
                }
                // --> refdata generic
                else if (pType == BaseConfig.FIELD_TYPE_REFDATA) {
                    whereParts.add( 'ven.' + p + '.id = :p' + (++pCount) )
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
                        whereParts.add('elb.vendor = ven and elb.invoicingFormat.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
                    else if (p == BaseConfig.CI_GENERIC_INVOICING_DISPATCH) {
                        queryParts.add('InvoiceDispatch dsp')
                        whereParts.add('dsp.vendor = ven and dsp.invoiceDispatch.id = :p' + (++pCount))
                        queryParams.put('p' + pCount, params.long(key))

                        filterLabelValue = RefdataValue.get(params.long(key)).getI10n('value')
                    }
//                    if (p == BaseConfig.CI_CTX_PROPERTY_KEY) {
//                        Long pValue = params.long('filter:vendor_propertyValue')
//
//                        String pq = getPropertyFilterSubQuery(
//                                'VendorProperty', 'ven',
//                                params.long(key),
//                                pValue,
//                                queryParams
//                        )
//                        whereParts.add( '(exists (' + pq + '))' )
//                        filterLabelValue = PropertyDefinition.get(params.long(key)).getI10n('name') + ( pValue ? ': ' + RefdataValue.get( pValue ).getI10n('value') : '')
//                    }
                }

                if (filterLabelValue) {
                    filterResult.labels.get('base').put(p, [label: GenericHelper.getFieldLabel(BaseConfig.getCurrentConfig( BaseConfig.KEY_VENDOR ).base, p), value: filterLabelValue])
                }
            }
        }

        String query = queryParts.unique().join(' , ') + ' ' + whereParts.join(' and ')

//        println 'VendorFilter.filter() -->'
//        println query
//        println queryParams
//        println whereParts

        filterResult.data.put('vendorIdList', queryParams.vendorIdList ? Vendor.executeQuery( query, queryParams ) : [])

//        println 'vendors >> ' + result.vendorIdList.size()

        filterResult
    }
}
