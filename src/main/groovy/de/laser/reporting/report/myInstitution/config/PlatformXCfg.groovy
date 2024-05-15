package de.laser.reporting.report.myInstitution.config

import de.laser.Platform
import de.laser.Provider
import de.laser.storage.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PlatformXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Platform,
                            cfgKey: BaseConfig.KEY_PLATFORM
                    ],
                    source : [
                            'all-plt',
                            'my-plt'
                    ],
                    fields: [
                            'name'                      : [ type: BaseConfig.FIELD_TYPE_PROPERTY /* blind */ ],
                            'primaryUrl'                : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'provider'                  : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PLATFORM_PROVIDER, spec: BaseConfig.FIELD_IS_MULTIPLE ],
//                            'org'                       : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PLATFORM_ORG, spec: BaseConfig.FIELD_IS_MULTIPLE ],
                            'ipAuthentication'          : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'passwordAuthentication'    : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'proxySupported'            : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'serviceProvider'           : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PLATFORM_SERVICEPROVIDER ],
                            'shibbolethAuthentication'  : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'softwareProvider'          : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PLATFORM_SOFTWAREPROVIDER ],
                            'status'                    : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'packageStatus'             : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_PACKAGESTATUS ],
                            'subscriptionStatus'        : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_SUBSCRIPTION_STATUS, spec: BaseConfig.FIELD_IS_MULTIPLE ],
                            'counterCertified'          : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'statisticsFormat'          : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ]
                            //'type'                    : [ type: FIELD_TYPE_REFDATA ],
                    ],
                    filter : [
                            default: [
                                    [ 'packageStatus', 'status'],
                                    [ 'serviceProvider', 'softwareProvider', 'provider' /*, 'org' */],
                                    [ 'ipAuthentication', 'shibbolethAuthentication', 'counterCertified' ],
                                    [ 'passwordAuthentication', 'proxySupported', 'statisticsFormat' ]
                            ],
                            my: [
                                    [ 'packageStatus', 'subscriptionStatus', 'status'],
                                    [ 'serviceProvider', 'softwareProvider', 'provider' /*, 'org' */],
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
//                                    'platform-x-org' : [       // TODO - moved from query !
//                                                               detailsTemplate     : 'platform',
//                                                               chartTemplate       : 'generic',
//                                                               chartLabels         : []
//                                    ],
                                    'platform-x-provider' : [
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
                            class:  Provider,
                            cfgKey: BaseConfig.KEY_PLATFORM
                    ],
                    source : [
                            'filter-subset-provider'
                    ],
                    fields : [ ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    provider : [
                                            'provider-status' :  [ 'generic.provider.status' ],
                                            'provider-*' :       [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],
    ]

    static Map<String, Map> CONFIG_DTC_ES = [

            'name'                                  : [ dtc: true   ],
//            'org'                                   : [ dtc: true   ],    // TODO - move to query2 !?
            'provider'                              : [ dtc: true   ],    // TODO - move to query2 !?
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

            '_dtField_?_propertyLocal'              : [ dtc: true   ],  // virtual, optional, fixed position
            '_dtField_lastUpdated'                  : [ dtc: true   ],  // virtual
            '_dtField_wekb'                         : [ dtc: true   ],  // virtual
    ]
}
