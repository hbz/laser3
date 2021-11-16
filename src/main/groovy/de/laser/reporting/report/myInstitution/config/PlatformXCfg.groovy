package de.laser.reporting.report.myInstitution.config

import de.laser.reporting.report.myInstitution.base.BaseConfig

class PlatformXCfg extends BaseConfig {

    static List<String> ES_DATA = [
    ]

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  de.laser.Platform,
                            cfgKey: KEY_PLATFORM
                    ],
                    source : [
                            'all-plt',
                            //'my-plt'
                    ],
                    fields: [
                            'org'               : FIELD_TYPE_CUSTOM_IMPL,
                            'status'            : FIELD_TYPE_REFDATA,
                            //'type'              : FIELD_TYPE_REFDATA,
                            'serviceProvider'   : FIELD_TYPE_CUSTOM_IMPL,
                            'softwareProvider'  : FIELD_TYPE_CUSTOM_IMPL,
                    ],
                    filter : [
                            default: [
                                    [ 'org', 'status'],
                                    [ 'serviceProvider', 'softwareProvider'],
                            ]
                    ],
                    query : [
                            default: [
                                    'package' : [
                                            'platform-org',
                                            'platform-status',
                                            'platform-serviceProvider',
                                            'platform-softwareProvider',
                                            'platform-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [
                                    'platform-x-property' : [
                                            detailsTemplate     : 'platform',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'base', 'x.properties' ]
                                    ]
                            ]
                    ]
            ]
    ]
}
