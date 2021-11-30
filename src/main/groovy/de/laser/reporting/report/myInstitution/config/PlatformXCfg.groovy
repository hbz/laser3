package de.laser.reporting.report.myInstitution.config

import de.laser.Platform
import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PlatformXCfg extends BaseConfig {

    static Map<String, Map> ES_DATA = [

            'platform-ipAuthentication'             : [filter: true,    export: true,   label: 'platform.auth.ip.supported',            rdc: RDConstants.IP_AUTHENTICATION],
            'platform-shibbolethAuthentication'     : [filter: true,    export: true,   label: 'platform.auth.shibboleth.supported',    rdc: RDConstants.Y_N],
            'platform-passwordAuthentication'       : [filter: true,    export: true,   label: 'platform.auth.userPass.supported',      rdc: RDConstants.Y_N],
            'platform-proxySupported'               : [filter: true,    export: true,   label: 'platform.auth.proxy.supported',         rdc: RDConstants.Y_N],
            'platform-counterCertified'             : [                 export: true,   label: 'platform.stats.counter.certified',      rdc: RDConstants.Y_N],
            'platform-counterR3Supported'           : [                 export: true,   label: 'platform.stats.counter.r3supported',    rdc: RDConstants.Y_N],
            'platform-counterR4Supported'           : [                 export: true,   label: 'platform.stats.counter.r4supported',    rdc: RDConstants.Y_N],
            'platform-counterR5Supported'           : [                 export: true,   label: 'platform.stats.counter.r5supported',    rdc: RDConstants.Y_N],
            'platform-counterR4SushiApiSupported'   : [                 export: true,   label: 'platform.stats.counter.r4sushi',        rdc: RDConstants.Y_N],
            'platform-counterR5SushiApiSupported'   : [                 export: true,   label: 'platform.stats.counter.r5sushi',        rdc: RDConstants.Y_N],
            'platform-statisticsFormat'             : [                 export: true,   label: 'platform.stats.format',                 rdc: RDConstants.PLATFORM_STATISTICS_FORMAT],
            'platform-statisticsUpdate'             : [                 export: true,   label: 'platform.stats.update',                 rdc: RDConstants.PLATFORM_STATISTICS_FREQUENCY]
    ]

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Platform,
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
