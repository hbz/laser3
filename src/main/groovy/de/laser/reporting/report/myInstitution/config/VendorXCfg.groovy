package de.laser.reporting.report.myInstitution.config

import de.laser.wekb.Vendor
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
                            'electronicBillings'                : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_INVOICING_FORMAT ],
                            'invoiceDispatchs'                  : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_INVOICING_DISPATCH ],
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
                            default : BaseConfig.GENERIC_VENDOR_QUERY_DEFAULT
                    ],
                    distribution : [
                            default : [
                                    'vendor-x-identifier' : [
                                            detailsTemplate     : 'vendor',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'base', 'x.identifiers' ]
                                    ],
                                    'vendor-x-property' : [
                                            detailsTemplate     : 'vendor',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                    ]
                            ]
                    ]
            ]
    ]
}
