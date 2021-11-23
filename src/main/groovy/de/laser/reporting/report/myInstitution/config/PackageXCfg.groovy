package de.laser.reporting.report.myInstitution.config

import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PackageXCfg extends BaseConfig {

    static Map<String, String> ES_DATA = [
            'package-breakable'         : RDConstants.PACKAGE_BREAKABLE,
            'package-consistent'        : RDConstants.PACKAGE_CONSISTENT,
            'package-openAccess'        : RDConstants.LICENSE_OA_TYPE,
            'package-paymentType'       : RDConstants.PAYMENT_TYPE,
            'package-scope'             : RDConstants.PACKAGE_SCOPE,
            'package-x-curatoryGroup'   : null,
            'package-x-nationalRange'   : null,
            'package-x-regionalRange'   : null,
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
                            'breakable'         : FIELD_TYPE_ELASTICSEARCH,
                            'consistent'        : FIELD_TYPE_ELASTICSEARCH,
                            'contentType'       : FIELD_TYPE_REFDATA,
                            'file'              : FIELD_TYPE_REFDATA,
                            'openAccess'        : FIELD_TYPE_ELASTICSEARCH,
                            'packageStatus'     : FIELD_TYPE_REFDATA,
                            'paymentType'       : FIELD_TYPE_ELASTICSEARCH,
                            'scope'             : FIELD_TYPE_ELASTICSEARCH
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
                                            'package-contentType',
                                            'package-packageStatus',
                                            'package-file',
                                            'package-breakable',        // ES
                                            'package-paymentType',      // ES
                                            'package-openAccess',       // ES
                                            'package-consistent',       // ES
                                            'package-scope',            // ES
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
                                    'package-x-language' : [
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
