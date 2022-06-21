package de.laser.reporting.export.local

import de.laser.OrgRole
import de.laser.Subscription
import de.laser.finance.CostItem
import de.laser.finance.Invoice
import de.laser.finance.Order
import de.laser.utils.DateUtils
import de.laser.storage.RDStore
import de.laser.reporting.export.LocalExportHelper
import de.laser.reporting.export.base.BaseDetailsExport

import java.text.SimpleDateFormat

class CostItemExport extends BaseDetailsExport {

    static String KEY = 'cost'

    static Map<String, Object> CONFIG_ORG_CONSORTIUM = [

            base : [
                    meta : [
                            class: CostItem
                    ],
                    fields : [
                            default: [
                                    '@-cost-member+sortname+name' : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-cost-subscription'         : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-cost-package'              : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-cost-order'                : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    '@-cost-invoice'              : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],

                                    'costTitle'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'costDescription'   : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'reference'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],

                                    'costInBillingCurrency'         : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'costInBillingCurrencyAfterTax' : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'billingCurrency'               : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'costInLocalCurrency'           : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'costInLocalCurrencyAfterTax'   : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    '@-cost-taxKey'                 : [ type: BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL ],
                                    'costItemElementConfiguration'  : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'costItemStatus'                : [ type: BaseDetailsExport.FIELD_TYPE_REFDATA ],
                                    'startDate'                     : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'endDate'                       : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'datePaid'                      : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ],
                                    'financialYear'                 : [ type: BaseDetailsExport.FIELD_TYPE_PROPERTY ]
                            ]
                    ],
            ]
    ]

    CostItemExport(String token, Map<String, Object> fields) {
        init(token, fields)
    }

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

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        CostItem ci = obj as CostItem
        List content = []

        fields.each{ f ->
            String key = f.key
            String type = getAllFields().get(f.key)?.type

            // --> generic properties
            if (type == BaseDetailsExport.FIELD_TYPE_PROPERTY) {
                content.add( getPropertyContent(ci, key, CostItem.getDeclaredField(key).getType()) )
            }
            // --> generic refdata
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA) {
                content.add( getRefdataContent(ci, key) )
            }
            // --> refdata join tables
            else if (type == BaseDetailsExport.FIELD_TYPE_REFDATA_JOINTABLE) {
                content.add( getJointableRefdataContent(ci, key) )
            }
            // --> custom filter implementation
            else if (type == BaseDetailsExport.FIELD_TYPE_CUSTOM_IMPL) {

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
//                else if (key == '@-cost-member') {
//                    Set<OrgRole> subscrOr = ci.sub.orgRelations.findAll{it.roleType.id in [RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id]}
//                    content.add( subscrOr.collect{ it.org.name + ( it.org.sortname ? ' (' + it.org.sortname +')' : '')}.join( CSV_VALUE_SEPARATOR ) )
//                }
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
            // --> combined properties : TODO
            else if (key in ['@-cost-member+sortname', '@-cost-member+name']) {
                String prop = key.split('\\+')[1]
                Set<OrgRole> subscrOr = ci.sub.orgRelations.findAll{it.roleType.id in [RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id]}
                content.add( subscrOr.collect{  it.org.getProperty(prop) ?: '' }.join( BaseDetailsExport.CSV_VALUE_SEPARATOR ) )
            }
            else {
                content.add( '- ' + key + ' not implemented -' )
            }
        }

        content
    }
}
