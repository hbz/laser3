package de.laser.reporting.report.myInstitution.config

import de.laser.Org
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
                            'name'                      : [ type: FIELD_TYPE_PROPERTY /* blind */ ],
                            'primaryUrl'                : [ type: FIELD_TYPE_PROPERTY ],
                            'org'                       : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PLATFORM_ORG, spec: FIELD_IS_MULTIPLE ],
                            'ipAuthentication'          : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'passwordAuthentication'    : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'proxySupported'            : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'serviceProvider'           : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PLATFORM_SERVICEPROVIDER ],
                            'shibbolethAuthentication'  : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'softwareProvider'          : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PLATFORM_SOFTWAREPROVIDER ],
                            'status'                    : [ type: FIELD_TYPE_REFDATA ],
                            'packageStatus'             : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PACKAGE_STATUS ],
                            'subscriptionStatus'        : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_SUBSCRIPTION_STATUS, spec: FIELD_IS_MULTIPLE ],
                            'counterCertified'          : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'statisticsFormat'          : [ type: FIELD_TYPE_ELASTICSEARCH ]
                            //'type'                    : [ type: FIELD_TYPE_REFDATA ],
                    ],
                    filter : [
                            default: [
                                    [ 'packageStatus', 'status'],
                                    [ 'serviceProvider', 'softwareProvider', 'org'],
                                    [ 'ipAuthentication', 'shibbolethAuthentication', 'counterCertified' ],
                                    [ 'passwordAuthentication', 'proxySupported', 'statisticsFormat' ]
                            ],
                            my: [
                                    [ 'packageStatus', 'subscriptionStatus', 'status'],
                                    [ 'serviceProvider', 'softwareProvider', 'org'],
                                    [ 'ipAuthentication', 'shibbolethAuthentication', 'counterCertified' ],
                                    [ 'passwordAuthentication', 'proxySupported', 'statisticsFormat' ]
                            ]
                    ],
                    query : [
                            default: [
                                    platform : [
                                           // 'platform-org',   // TODO - moved to distribution !
                                            'platform-serviceProvider' :            [ '@' ],
                                            'platform-softwareProvider' :           [ '@' ],
                                            'platform-status' :                     [ '@' ],
//                                            'platform-primaryUrl' :                 [ '@' ],   // TODO - moved to distribution !
                                            'platform-ipAuthentication' :           [ '@' ],    // ES
                                            'platform-shibbolethAuthentication' :   [ '@' ],    // ES
                                            'platform-passwordAuthentication' :     [ '@' ],    // ES
                                            'platform-proxySupported' :             [ '@' ],    // ES
                                            'platform-statisticsFormat' :           [ '@' ],    // ES
                                            'platform-statisticsUpdate' :           [ '@' ],    // ES
                                            'platform-counterCertified' :           [ '@' ],    // ES
                                            'platform-counterR3Supported' :         [ '@' ],    // ES
                                            'platform-counterR4Supported' :         [ '@' ],    // ES
                                            'platform-counterR5Supported' :         [ '@' ],    // ES
                                            'platform-counterR4SushiApiSupported' : [ '@' ],    // ES
                                            'platform-counterR5SushiApiSupported' : [ '@' ],    // ES
                                            'platform-*' :                          [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                    'platform-x-property' : [
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
                                    ],
                                    'platform-x-org' : [       // TODO - moved from query !
                                                               detailsTemplate     : 'platform',
                                                               chartTemplate       : 'generic',
                                                               chartLabels         : []
                                    ],
                                    'platform-x-primaryUrl' : [       // TODO - moved from query !
                                            detailsTemplate     : 'platform',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_PLATFORM
                    ],
                    source : [
                            'filter-restricting-provider'
                    ],
                    fields : [ ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    platformOrg : [ // label
                                            'provider-orgType' : [ 'generic.org.orgType' ],
                                            'provider-*' :       [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],
    ]

    static Map<String, Map> CMB_ES_DT_CONFIG = [

            'name'                                  : [ dtc: true   ],
            'org'                                   : [ dtc: true   ],    // TODO - move to query2 !?
            'primaryUrl'                            : [ dtc: true   ],    // TODO - move to query2 !?
            'serviceProvider'                       : [ dtc: false  ],
            'softwareProvider'                      : [ dtc: false  ],

            'status'                                : [ dtc: false  ],
            'platform-altname'                      : [             es: true,               export: true, label: 'package.show.altname'],
            'platform-x-propertyWekb'               : [             es: true  ],
            'platform-ipAuthentication'             : [ dtc: false, es: true, filter: true, export: true, label: 'platform.auth.ip.supported',            rdc: RDConstants.IP_AUTHENTICATION ],
            'platform-shibbolethAuthentication'     : [ dtc: false, es: true, filter: true, export: true, label: 'platform.auth.shibboleth.supported',    rdc: RDConstants.Y_N ],

            'platform-passwordAuthentication'       : [ dtc: false, es: true, filter: true, export: true, label: 'platform.auth.userPass.supported',      rdc: RDConstants.Y_N ],
            'platform-proxySupported'               : [ dtc: false, es: true, filter: true, export: true, label: 'platform.auth.proxy.supported',         rdc: RDConstants.Y_N ],
            'platform-statisticsFormat'             : [ dtc: false, es: true, filter: true, export: true, label: 'platform.stats.format',                 rdc: RDConstants.PLATFORM_STATISTICS_FORMAT ],
            'platform-statisticsUpdate'             : [ dtc: false, es: true,               export: true, label: 'platform.stats.update',                 rdc: RDConstants.PLATFORM_STATISTICS_FREQUENCY ],
            'platform-counterCertified'             : [ dtc: false, es: true, filter: true, export: true, label: 'platform.stats.counter.certified',      rdc: RDConstants.Y_N ],

            'platform-counterR3Supported'           : [ dtc: false, es: true,               export: true, label: 'platform.stats.counter.r3supported',    rdc: RDConstants.Y_N ],
            'platform-counterR4Supported'           : [ dtc: false, es: true,               export: true, label: 'platform.stats.counter.r4supported',    rdc: RDConstants.Y_N ],
            'platform-counterR5Supported'           : [ dtc: false, es: true,               export: true, label: 'platform.stats.counter.r5supported',    rdc: RDConstants.Y_N ],
            'platform-counterR4SushiApiSupported'   : [ dtc: false, es: true,               export: true, label: 'platform.stats.counter.r4sushi',        rdc: RDConstants.Y_N ],
            'platform-counterR5SushiApiSupported'   : [ dtc: false, es: true,               export: true, label: 'platform.stats.counter.r5sushi',        rdc: RDConstants.Y_N ],

            '_?_propertyLocal'                      : [ dtc: true   ],  // virtual, optional, fixed position
            '_+_lastUpdated'                        : [ dtc: true   ],  // virtual
            '_+_wekb'                               : [ dtc: true   ],  // virtual
    ]
}
