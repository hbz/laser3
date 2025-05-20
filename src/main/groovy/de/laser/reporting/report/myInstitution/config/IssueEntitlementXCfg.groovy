package de.laser.reporting.report.myInstitution.config

import de.laser.IssueEntitlement
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.annotations.UnstableFeature
import de.laser.reporting.report.myInstitution.base.BaseConfig

// not ready for use
@UnstableFeature
class IssueEntitlementXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  IssueEntitlement,
                            cfgKey: BaseConfig.KEY_ISSUEENTITLEMENT
                    ],
                    source : [
//                            'all-ie',
                            'my-ie'
                    ],
                    fields: [
                            'status'                        : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_IE_STATUS ],
                            'package'                       : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_CTX_IE_PACKAGE ],
                            'subscription'                  : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_CTX_IE_SUBSCRIPTION ],
                            'packageStatus'                 : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_PACKAGESTATUS ],
                            'packageNominalPlatform'        : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_PLATFORM ],
//                            'orProvider'                    : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_OR_PROVIDER ],
                            'subscriptionStatus'            : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_SUBSCRIPTION_STATUS ],
                    ],
                    filter : [
                            default: [
                                    [ 'packageStatus', 'subscriptionStatus', 'status' ],
                                    [ 'package', 'subscription' ],
                                    [ /*'orProvider',*/ 'packageNominalPlatform' ]
                            ]
                    ],
                    query : [
                            default: [
                                    issueEntitlement : [
                                            'issueEntitlement-status' :         [ '@' ],
//                                            'issueEntitlement-pkg' :          [ '@' ],
//                                            'issueEntitlement-org' :          [ '@' ],
//                                            'issueEntitlement-platform' :     [ '@' ],
//                                            'issueEntitlement-subscription' : [ '@' ],
                                            'issueEntitlement-*' :              [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                    'issueEntitlement-x-pkg' : [
                                            detailsTemplate     : 'issueEntitlement',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'issueEntitlement-x-provider' : [
                                            detailsTemplate     : 'issueEntitlement',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'issueEntitlement-x-platform' : [
                                            detailsTemplate     : 'issueEntitlement',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'issueEntitlement-x-platformProvider' : [
                                            detailsTemplate     : 'issueEntitlement',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'issueEntitlement-x-subscription' : [
                                            detailsTemplate     : 'issueEntitlement',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                            ]
                    ]
            ],

            subscription : [
                    meta : [
                            class:  Subscription,
                            cfgKey: BaseConfig.KEY_ISSUEENTITLEMENT
                    ],
                    source : [
                            'depending-subscription'
                    ],
                    fields : [],
                    filter : [
                            default : [
                                    [ 'status' ],
                            ]
                    ],
                    query : [
                            default: [
                                    subscription : [
                                            'subscription-status' :  [ '@' ],
                                            'subscription-*' :       [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],

            package : [
                    meta : [
                            class:  de.laser.wekb.Package,
                            cfgKey: BaseConfig.KEY_PACKAGE
                    ],
                    source : [
                            'filter-subset-package'
                    ],
                    fields: [
                            'packageStatus': [ type: BaseConfig.FIELD_TYPE_REFDATA ]
                    ],
                    filter : [
                            default: [
                                    [ 'packageStatus' ],
                            ]
                    ],
                    query : [
                            default: [
                                    package : [
                                            'package-x-platform' :      [ '@' ],   // KEY_PACKAGE -> distribution
                                            'package-packageStatus' :   [ '@' ],
                                            'package-*' :               [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Provider,
                            cfgKey: BaseConfig.KEY_ISSUEENTITLEMENT
                    ],
                    source : [
                            'filter-subset-provider'
                    ],
                    fields : [],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : BaseConfig.GENERIC_PROVIDER_QUERY_DEFAULT
                    ]
            ],

            platform: [
                    meta : [
                            class:  Platform,
                            cfgKey: BaseConfig.KEY_ISSUEENTITLEMENT
                    ],
                    source : [
                            'filter-subset-platform'
                    ],
                    fields : [],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    platform : [
                                            'platform-x-provider' : [ '@' ],       // KEY_PLATFORM -> distribution
//                                            'platform-x-org' :      [ '@' ],       // KEY_PLATFORM -> distribution
                                            'platform-status' :     [ 'generic.plt.status' ],
                                    //        'nominalPlatform-serviceProvider' : [ '@' ],
                                    //        'nominalPlatform-softwareProvider' : [ '@' ],
                                            'platform-*' :          [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ]
    ]
}
