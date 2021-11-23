package de.laser.reporting.report.myInstitution.config

import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PlatformXCfg extends BaseConfig {

    static Map<String, String> ES_DATA = [
            'platform-ipAuthentication'             : RDConstants.IP_AUTHENTICATION,
            'platform-shibbolethAuthentication'     : RDConstants.Y_N,
            'platform-passwordAuthentication'       : RDConstants.Y_N,
            'platform-proxySupported'               : RDConstants.Y_N,
            'platform-counterCertified'             : RDConstants.Y_N,
            'platform-counterR3Supported'           : RDConstants.Y_N,
            'platform-counterR4Supported'           : RDConstants.Y_N,
            'platform-counterR4SushiApiSupported'   : RDConstants.Y_N,
            'platform-counterR5Supported'           : RDConstants.Y_N,
            'platform-counterR5SushiApiSupported'   : RDConstants.Y_N,
            'platform-statisticsFormat'             : RDConstants.PLATFORM_STATISTICS_FORMAT,
            'platform-statisticsUpdate'             : RDConstants.PLATFORM_STATISTICS_FREQUENCY
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
                            'org'                       : FIELD_TYPE_CUSTOM_IMPL,
                            'ipAuthentication'          : FIELD_TYPE_ELASTICSEARCH,
                            'passwordAuthentication'    : FIELD_TYPE_ELASTICSEARCH,
                            'proxySupported'            : FIELD_TYPE_ELASTICSEARCH,
                            'serviceProvider'           : FIELD_TYPE_CUSTOM_IMPL,
                            'shibbolethAuthentication'  : FIELD_TYPE_ELASTICSEARCH,
                            'softwareProvider'          : FIELD_TYPE_CUSTOM_IMPL,
                            'status'                    : FIELD_TYPE_REFDATA,
                            //'type'                    : FIELD_TYPE_REFDATA,
                    ],
                    filter : [
                            default: [
                                    [ 'org', 'status'],
                                    [ 'serviceProvider', 'softwareProvider'],
                                    [ 'ipAuthentication', 'shibbolethAuthentication', 'proxySupported' ],
                                    [ 'passwordAuthentication' ]
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
                                            'platform-counterR5Supported',          // ES
                                            'platform-counterR4SushiApiSupported',  // ES
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
