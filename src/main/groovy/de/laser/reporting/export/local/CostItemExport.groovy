package de.laser.reporting.export.local

import de.laser.ContextService
import de.laser.OrgRole
import de.laser.Subscription
import de.laser.finance.CostItem
import de.laser.finance.Invoice
import de.laser.finance.Order
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.reporting.export.base.BaseDetailsExport
import grails.util.Holders
import org.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

class CostItemExport extends BaseDetailsExport {

    static String KEY = 'cost'

    CostItemExport(String token, Map<String, Object> fields) {
        this.token = token

        // keeping order ..
        getAllFields().keySet().each { k ->
            if (k in fields.keySet() ) {
                selectedExportFields.put(k, fields.get(k))
            }
        }
        normalizeSelectedMultipleFields( this )
    }

    static Map<String, Object> CONFIG_ORG_CONSORTIUM = [

            base : [
                    meta : [
                            class: CostItem
                    ],
                    fields : [
                            default: [
                                    'costTitle'         : FIELD_TYPE_PROPERTY,
                                    'costDescription'   : FIELD_TYPE_PROPERTY,
                                    'reference'         : FIELD_TYPE_PROPERTY,

                                    'costInBillingCurrency'         : FIELD_TYPE_PROPERTY,
                                    'costInBillingCurrencyAfterTax' : FIELD_TYPE_PROPERTY,
                                    'billingCurrency'               : FIELD_TYPE_REFDATA,
                                    'costInLocalCurrency'           : FIELD_TYPE_PROPERTY,
                                    'costInLocalCurrencyAfterTax'   : FIELD_TYPE_PROPERTY,
                                    '@-cost-taxKey'               : FIELD_TYPE_CUSTOM_IMPL,   // virtual
                                    'costItemElementConfiguration'  : FIELD_TYPE_REFDATA,
                                    'costItemStatus'                : FIELD_TYPE_REFDATA,
                                    'startDate'                     : FIELD_TYPE_PROPERTY,
                                    'endDate'                       : FIELD_TYPE_PROPERTY,
                                    'datePaid'                      : FIELD_TYPE_PROPERTY,
                                    'financialYear'                 : FIELD_TYPE_PROPERTY,

                                    '@-cost-member'       : FIELD_TYPE_CUSTOM_IMPL,       // virtual
                                    '@-cost-subscription' : FIELD_TYPE_CUSTOM_IMPL,
                                    '@-cost-package'      : FIELD_TYPE_CUSTOM_IMPL,
                                    '@-cost-order'        : FIELD_TYPE_CUSTOM_IMPL,
                                    '@-cost-invoice'      : FIELD_TYPE_CUSTOM_IMPL,
                            ]
                    ],
            ]
    ]

    @Override
    Map<String, Object> getSelectedFields() {
        selectedExportFields
    }

    @Override
    String getFieldLabel(String fieldName) {
        LocalExportHelper.getFieldLabel( this, fieldName )
    }

    @Override
    List<Object> getDetailedObject(Object obj, Map<String, Object> fields) {

        ApplicationTagLib g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        CostItem ci = obj as CostItem
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)

            // --> generic properties
            if (type == FIELD_TYPE_PROPERTY) {
                content.add( getPropertyContent(ci, key, CostItem.getDeclaredField(key).getType()) )
            }
            // --> generic refdata
            else if (type == FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(ci, key) )
            }
            // --> refdata join tables
            else if (type == FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(ci, key) )
            }
            // --> custom filter implementation
            else if (type == FIELD_TYPE_CUSTOM_IMPL) {

                if (key == '@-cost-taxKey') {
                    if (ci.taxKey) {
                        if (ci.taxKey.display) {
                            content.add( ci.taxKey.taxRate + '%' )
                        }
                        else if (ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE) {
                            content.add( RDStore.TAX_REVERSE_CHARGE.getI10n('value') )
                        }
                        else if (ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19]) {
                            content.add( ci.taxKey.taxType.getI10n('value') )
                        }
                    } else {
                        content.add('')
                    }
                }
                else if (key == '@-cost-subscription') {
                    Subscription sub = ci.sub
                    if (sub) {
                        content.add( sub.name + ' (' + (sub.startDate ? sdf.format(sub.startDate) : '') + '-' + (sub.endDate ? sdf.format(sub.endDate) : '') + ')' )
                    } else {
                        content.add('')
                    }
                }
                else if (key == '@-cost-member') {
                    Set<OrgRole> subscrOr = ci.sub.orgRelations.findAll{it.roleType.id in [RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id]}
                    content.add( subscrOr.collect{ it.org.name + ( it.org.sortname ? ' (' + it.org.sortname +')' : '')}.join( CSV_VALUE_SEPARATOR ) )
                }
                else if (key == '@-cost-package') {
                    de.laser.Package pkg = ci.subPkg?.pkg
                    if (pkg) {
                        content.add(pkg.toString())
                    } else {
                        content.add('')
                    }
                }
                else if (key == '@-cost-order') {
                    Order ord = ci.order
                    if (ord?.orderNumber) {
                        content.add( ord.orderNumber )
                    } else {
                        content.add('')
                    }
                }
                else if (key == '@-cost-invoice') {
                    Invoice inv = ci.invoice
                    if (inv?.invoiceNumber) {
                        content.add( inv.invoiceNumber )
                    } else {
                        content.add('')
                    }
                }
            }
            else {
                content.add( '- not implemented -' )
            }
        }

        content
    }
}
