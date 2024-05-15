package de.laser.reporting.report.myInstitution.config

import de.laser.Provider
import de.laser.reporting.report.myInstitution.base.BaseConfig

class ProviderXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Provider,
                            cfgKey: BaseConfig.KEY_PROVIDER
                    ],
                    source : [
                            'all-provider',
                            'my-provider',
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
                            default : [
                                    provider : [
                                            'provider-paperInvoice' :                       [ 'generic.provider.paperInvoice' ],
                                            'provider-managementOfCredits' :                [ 'generic.provider.managementOfCredits' ],
                                            'provider-processingOfCompensationPayments' :   [ 'generic.provider.processingOfCompensationPayments' ],
                                            'provider-individualInvoiceDesign' :            [ 'generic.provider.individualInvoiceDesign' ],
                                            'provider-status' :                             [ 'generic.provider.status' ],
                                            'provider-*' :                                  [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                    'provider-x-property' : [
                                            detailsTemplate     : 'provider',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                    ],
                            ]
                    ]
            ]
    ]
}
