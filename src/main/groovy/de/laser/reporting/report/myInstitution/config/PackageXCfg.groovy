package de.laser.reporting.report.myInstitution.config

import de.laser.reporting.report.myInstitution.base.BaseConfig

class PackageXCfg extends BaseConfig {

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
                            'breakable'         : FIELD_TYPE_REFDATA,
                            'consistent'        : FIELD_TYPE_REFDATA,
                            'contentType'       : FIELD_TYPE_REFDATA,
                            'file'              : FIELD_TYPE_REFDATA,
                            'packageStatus'     : FIELD_TYPE_REFDATA,
                            'scope'             : FIELD_TYPE_REFDATA
                    ],
                    filter : [
                            default: [
                                    [ 'contentType', 'scope', 'packageStatus' ],
                                    [ 'consistent', 'breakable', 'file' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'package' : [
                                            'package-breakable',     // ES
                                            'package-consistent',
                                            'package-contentType',
                                            'package-file',
                                            'package-packageStatus',
                                            'package-scope',     // ES
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
                                    'package-x-openAccess' : [   // ES
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
                                    'package-x-ddc' : [  // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ]
                            ]
                    ]
            ]
    ]
}
