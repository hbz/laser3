package de.laser.reporting.report.myInstitution.config

import de.laser.Platform
import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PlatformXCfg extends BaseConfig {

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
                            'counterCertified'          : FIELD_TYPE_ELASTICSEARCH,
                            'statisticsFormat'          : FIELD_TYPE_ELASTICSEARCH
                            //'type'                    : FIELD_TYPE_REFDATA,
                    ],
                    filter : [
                            default: [
                                    [ 'org', 'status'],
                                    [ 'serviceProvider', 'softwareProvider'],
                                    [ 'ipAuthentication', 'shibbolethAuthentication', 'counterCertified' ],
                                    [ 'passwordAuthentication', 'proxySupported', 'statisticsFormat' ]
                            ]
                    ],
                    query : [
                            default: [
                                    platform : [
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
                    distribution : [
                            default : [
                                    'platform-x-propertyLocal' : [
                                            detailsTemplate     : 'platform',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                    ],
                                    'platform-x-propertyWekb' : [
                                            detailsTemplate     : 'platform',
                                            chartTemplate       : 'generic',
                                            esProperties        : [
                                                    'platform-ipAuthentication',
                                                    'platform-shibbolethAuthentication',
                                                    'platform-passwordAuthentication',
                                                    'platform-proxySupported',
                                                    'platform-counterCertified',
                                                    'platform-counterR3Supported',
                                                    'platform-counterR4Supported',
                                                    'platform-counterR5Supported',
                                                    'platform-counterR4SushiApiSupported',
                                                    'platform-counterR5SushiApiSupported',
                                                    'platform-statisticsFormat',
                                                    'platform-statisticsUpdate'
                                            ]
                                    ]
                            ]
                    ]
            ]
    ]

    static Map<String, Map> ES_DATA = [

            'platform-altname'                      : [                 export: true,   label: 'package.show.altname'],
            'platform-x-propertyWekb'               : [ : ],

            'platform-ipAuthentication'             : [filter: true,    export: true,   label: 'platform.auth.ip.supported',            rdc: RDConstants.IP_AUTHENTICATION],
            'platform-shibbolethAuthentication'     : [filter: true,    export: true,   label: 'platform.auth.shibboleth.supported',    rdc: RDConstants.Y_N],
            'platform-passwordAuthentication'       : [filter: true,    export: true,   label: 'platform.auth.userPass.supported',      rdc: RDConstants.Y_N],
            'platform-proxySupported'               : [filter: true,    export: true,   label: 'platform.auth.proxy.supported',         rdc: RDConstants.Y_N],
            'platform-counterCertified'             : [filter: true,    export: true,   label: 'platform.stats.counter.certified',      rdc: RDConstants.Y_N],
            'platform-counterR3Supported'           : [                 export: true,   label: 'platform.stats.counter.r3supported',    rdc: RDConstants.Y_N],
            'platform-counterR4Supported'           : [                 export: true,   label: 'platform.stats.counter.r4supported',    rdc: RDConstants.Y_N],
            'platform-counterR5Supported'           : [                 export: true,   label: 'platform.stats.counter.r5supported',    rdc: RDConstants.Y_N],
            'platform-counterR4SushiApiSupported'   : [                 export: true,   label: 'platform.stats.counter.r4sushi',        rdc: RDConstants.Y_N],
            'platform-counterR5SushiApiSupported'   : [                 export: true,   label: 'platform.stats.counter.r5sushi',        rdc: RDConstants.Y_N],
            'platform-statisticsFormat'             : [filter: true,    export: true,   label: 'platform.stats.format',                 rdc: RDConstants.PLATFORM_STATISTICS_FORMAT],
            'platform-statisticsUpdate'             : [                 export: true,   label: 'platform.stats.update',                 rdc: RDConstants.PLATFORM_STATISTICS_FREQUENCY]
    ]

    static Map<String, Boolean> DETAILS_TABLE_CONFIG = [

            'name' : true,
            'org' : true,     // TODO - move to query2 !?
            'primaryUrl' : true,
            'serviceProvider' : false,
            'softwareProvider' : false,
            'status' : false,
            'platform-ipAuthentication' : false, // ES
            'platform-shibbolethAuthentication' : false, // ES
            'platform-passwordAuthentication' : false, // ES
            'platform-proxySupported' : false, // ES
            'platform-statisticsFormat' : false, // ES
            'platform-statisticsUpdate' : false, // ES
            'platform-counterCertified' : false, // ES
            'platform-counterR3Supported' : false, // ES
            'platform-counterR4Supported' : false, // ES
            'platform-counterR5Supported' : false, // ES
            'platform-counterR4SushiApiSupported' : false, // ES
            'platform-counterR5SushiApiSupported' : false, // ES
            // 'platform-x-property' : false,
            '___lastUpdated' : true, // virtual
            '___wekb' : true // virtual
    ]
}
