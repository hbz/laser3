package de.laser.reporting.report.myInstitution.config

import de.laser.reporting.report.myInstitution.base.BaseConfig

class PlatformXCfg extends BaseConfig {

    static List<String> ES_DATA = [
            'platform-ipAuthentication',
            'platform-shibbolethAuthentication',
            'platform-passwordAuthentication',
            'platform-proxySupported',
            'platform-counterCertified',
            'platform-counterR3Supported',
            'platform-counterR4Supported',
            'platform-counterR4SushiApiSupported',
            'platform-counterR5Supported',
            'platform-counterR5SushiApiSupported',
            'platform-statisticsFormat',
            'platform-statisticsUpdate'
    ]

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  de.laser.Platform,
                            cfgKey: KEY_PLATFORM
                    ],
                    source : [
                            'all-plt',
                            'my-plt'
                    ],
                    fields: [
                            'org'               : FIELD_TYPE_CUSTOM_IMPL,
                            'status'            : FIELD_TYPE_REFDATA,
                            //'type'              : FIELD_TYPE_REFDATA,
                            'serviceProvider'   : FIELD_TYPE_CUSTOM_IMPL,
                            'softwareProvider'  : FIELD_TYPE_CUSTOM_IMPL,
                            'ipAuthentication'  : FIELD_TYPE_CUSTOM_IMPL,           // ES
                            'shibbolethAuthentication'  : FIELD_TYPE_CUSTOM_IMPL,   // ES
                            'passwordAuthentication'    : FIELD_TYPE_CUSTOM_IMPL,   // ES
                            'proxySupported'    : FIELD_TYPE_CUSTOM_IMPL            // ES
                    ],
                    filter : [
                            default: [
                                    [ 'org', 'status'],
                                    [ 'serviceProvider', 'softwareProvider'],
                            ]
                    ],
                    query : [
                            default: [
                                    'platform' : [
                                            'platform-org',     // TODO - move to query2 !?
                                            'platform-serviceProvider',
                                            'platform-softwareProvider',
                                            'platform-status',
                                            'platform-ipAuthentication',            // ES
                                            'platform-shibbolethAuthentication',    // ES
                                            'platform-passwordAuthentication',      // ES
                                            'platform-proxySupported',              // ES
                                            'platform-statisticsFormat',            // ES
                                            'platform-statisticsUpdate',            // ES
                                            'platform-counterCertified',            // ES
                                            'platform-counterR3Supported',          // ES
                                            'platform-counterR4Supported',          // ES
                                            'platform-counterR4SushiApiSupported',  // ES
                                            'platform-counterR5Supported',          // ES
                                            'platform-counterR5SushiApiSupported',  // ES
                                            'platform-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [
                                    'platform-x-property' : [
                                            detailsTemplate     : 'platform',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                    ]
                            ]
                    ]
            ]
    ]
}
