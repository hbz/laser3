package de.laser.reporting.report.myInstitution.config

import de.laser.reporting.report.myInstitution.base.BaseConfig

class PackageXCfg extends BaseConfig {

    static List<String> ES_DATA = [
            'breakable',
            'consistent',
            'openAccess',
            'paymentType',
            'scope',
            'package-x-curatoryGroup',
            'package-x-nationalRange',
            'package-x-regionalRange',
            'package-x-language',
           // 'package-x-ddc'
    ]

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  de.laser.Package,
                            cfgKey: KEY_PACKAGE
                    ],
                    source : [
                            'all-pkg',
                            'my-pkg'
                    ],
                    fields: [
                            'breakable'         : FIELD_TYPE_CUSTOM_IMPL, // ES
                            'consistent'        : FIELD_TYPE_CUSTOM_IMPL, // ES
                            'contentType'       : FIELD_TYPE_REFDATA,
                            'file'              : FIELD_TYPE_REFDATA,
                            'openAccess'        : FIELD_TYPE_CUSTOM_IMPL, // ES
                            'packageStatus'     : FIELD_TYPE_REFDATA,
                            'paymentType'       : FIELD_TYPE_CUSTOM_IMPL, // ES
                            'scope'             : FIELD_TYPE_CUSTOM_IMPL  // ES
                    ],
                    filter : [
                            default: [
                                    [ 'contentType', 'scope', 'packageStatus' ],
                                    [ 'paymentType', 'breakable', 'consistent' ],
                                    [ 'openAccess', 'file' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'package' : [
                                            'package-breakable',     // ES
                                            'package-consistent',    // ES
                                            'package-contentType',
                                            'package-file',
                                            'package-openAccess',   // ES
                                            'package-packageStatus',
                                            'package-paymentType',  // ES
                                            'package-scope',        // ES
                                            'package-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [
                                    'package-x-identifier' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'base', 'x.identifiers' ]
                                    ],
                                    'package-x-provider' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'x.providers.1', 'x.providers.2' ]
                                    ],
                                    'package-x-platform' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-curatoryGroup' : [ // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-nationalRange' : [   // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-regionalRange' : [   // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-language' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
//                                    'package-x-ddc' : [  // ES
//                                            detailsTemplate     : 'package',
//                                            chartTemplate       : 'generic',
//                                            chartLabels         : []
//                                    ]
                            ]
                    ]
            ]
    ]
}
