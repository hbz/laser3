package de.laser.reporting.report.myInstitution.config

import de.laser.Vendor
import de.laser.reporting.report.myInstitution.base.BaseConfig

class VendorXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Vendor,
                            cfgKey: BaseConfig.KEY_VENDOR
                    ],
                    source : [
                            'all-vendor',
                            'my-vendor',
                    ],
                    fields : [
                            'status'                            : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'electronicBillings'                : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImplRdv: BaseConfig.CI_GENERIC_INVOICING_FORMAT ],   // TODO custom_impl
                            'invoiceDispatchs'                  : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImplRdv: BaseConfig.CI_GENERIC_INVOICING_DISPATCH ],   // TODO custom_impl
                            'paperInvoice'                      : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'managementOfCredits'               : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'processingOfCompensationPayments'  : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'individualInvoiceDesign'           : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                    ],
                    filter : [
                            default : [
                                    [ 'electronicBillings', 'invoiceDispatchs', 'status' ],
                                    [ 'paperInvoice', 'managementOfCredits', 'processingOfCompensationPayments', 'individualInvoiceDesign' ]
                            ],
                            my : [
                                    [ 'electronicBillings', 'invoiceDispatchs', 'status' ],
                                    [ 'paperInvoice', 'managementOfCredits', 'processingOfCompensationPayments', 'individualInvoiceDesign' ]
                            ]
                    ],
                    query : [
                            default : [
                                    vendor : [
                                            'vendor-paperInvoice' :                     [ 'generic.vendor.paperInvoice' ],
                                            'vendor-managementOfCredits' :              [ 'generic.vendor.managementOfCredits' ],
                                            'vendor-processingOfCompensationPayments' : [ 'generic.vendor.processingOfCompensationPayments' ],
                                            'vendor-individualInvoiceDesign' :          [ 'generic.vendor.individualInvoiceDesign' ],
                                            'vendor-status' :           [ 'generic.vendor.status' ],
                                            'vendor-*' :                [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [:]
//                            default : [
//                                    'vendor-x-electronicBillings' : [
//                                            detailsTemplate     : 'vendor',
//                                            chartTemplate       : '2axis2values_nonMatches',
//                                            chartLabels         : [ 'base', 'x.electronicBillings' ]
//                                    ],
//                                    'vendor-x-invoiceDispatchs' : [
//                                            detailsTemplate     : 'vendor',
//                                            chartTemplate       : '2axis2values_nonMatches',
//                                            chartLabels         : [ 'base', 'x.invoiceDispatchs' ]
//                                    ],
//                            ]
                    ]
            ]
    ]
}
