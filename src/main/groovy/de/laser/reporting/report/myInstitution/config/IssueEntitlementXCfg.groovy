package de.laser.reporting.report.myInstitution.config

import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.Platform
import de.laser.Subscription
import de.laser.reporting.report.myInstitution.base.BaseConfig

@Deprecated
class IssueEntitlementXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  IssueEntitlement,
                            cfgKey: KEY_ISSUEENTITLEMENT
                    ],
                    source : [
//                            'all-ie',
                            'my-ie'
                    ],
                    fields: [
                            'status'            : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'pkg'               : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'platform'          : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'provider'          : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'subscription'      : [ FIELD_TYPE_CUSTOM_IMPL ],

                            'iePackageStatus'      : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'ieSubscriptionStatus' : [ FIELD_TYPE_CUSTOM_IMPL ]
                    ],
                    filter : [
                            default: [
                                    [ 'iePackageStatus', 'ieSubscriptionStatus', 'status' ],
                                    [ 'pkg', 'subscription' ],
                                    [ 'provider', 'platform' ]
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
                                            'issueEntitlement-*' :              [ 'generic-*' ]
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
                            cfgKey: KEY_ISSUEENTITLEMENT
                    ],
                    source : [
                            'depending-subscription'
                    ],
                    fields : [ ],
                    filter : [
                            default : [
                                    [ 'status' ],
                            ]
                    ],
                    query : [
                            default: [
                                    subscription : [
                                            'subscription-status' :  [ '@' ],
                                            'subscription-*' :       [ 'generic-*' ]
                                    ]
                            ]
                    ]
            ],

            package : [
                    meta : [
                            class:  de.laser.Package,
                            cfgKey: KEY_PACKAGE
                    ],
                    source : [
                            'filter-restricting-package'
                    ],
                    fields: [
                            'packageStatus': [ FIELD_TYPE_REFDATA ]
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
                                            'package-*' :               [ 'generic-*' ]
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_ISSUEENTITLEMENT
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
                                    provider : [
                                            'provider-orgType' : [ 'generic-orgType' ],
                                            'provider-*' :       [ 'generic-*' ]
                                    ]
                            ]
                    ]
            ],

            platform: [
                    meta : [
                            class:  Platform,
                            cfgKey: KEY_ISSUEENTITLEMENT
                    ],
                    source : [
                            'filter-restricting-platform'
                    ],
                    fields : [ ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    platform : [
                                            'platform-x-org' :      [ '@' ],       // KEY_PLATFORM -> distribution
                                            'platform-status' :     [ '@' ],
                                    //        'nominalPlatform-serviceProvider' : [ '@' ],
                                    //        'nominalPlatform-softwareProvider' : [ '@' ],
                                            'platform-*' :          [ 'generic-*' ]
                                    ]
                            ]
                    ]
            ]
    ]
}
