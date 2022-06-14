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
                            'status'                        : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_IE_STATUS ],
                            'package'                       : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_CTX_IE_PACKAGE ],
                            'subscription'                  : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_CTX_IE_SUBSCRIPTION ],
                            'packageStatus'                 : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PACKAGE_STATUS ],
                            'packageNominalPlatform'        : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PACKAGE_PLATFORM ],
                            'orProvider'                    : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PACKAGE_OR_PROVIDER ],
                            'subscriptionStatus'            : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_SUBSCRIPTION_STATUS ],
                    ],
                    filter : [
                            default: [
                                    [ 'packageStatus', 'subscriptionStatus', 'status' ],
                                    [ 'package', 'subscription' ],
                                    [ 'orProvider', 'packageNominalPlatform' ]
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
                                            'subscription-*' :       [ 'generic.all' ]
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
                            'packageStatus': [ type: FIELD_TYPE_REFDATA ]
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
                                            'provider-orgType' : [ 'generic.org.orgType' ],
                                            'provider-*' :       [ 'generic.all' ]
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
