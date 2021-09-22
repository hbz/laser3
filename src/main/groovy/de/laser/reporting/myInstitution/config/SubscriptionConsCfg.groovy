package de.laser.reporting.myInstitution.config

import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.myInstitution.base.BaseConfig

class SubscriptionConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Subscription,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'consortia-sub'
                    ],
                    fields : [
                            'annual'                : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'endDateLimit'          : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'form'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'hasPerpetualAccess'    : BaseConfig.FIELD_TYPE_PROPERTY,
                            'hasPublishComponent'   : BaseConfig.FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : BaseConfig.FIELD_TYPE_PROPERTY,
                            'isMultiYear'           : BaseConfig.FIELD_TYPE_PROPERTY,
                            'kind'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'resource'              : BaseConfig.FIELD_TYPE_REFDATA,
                            'startDateLimit'        : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'status'                : BaseConfig.FIELD_TYPE_REFDATA,
                            //'type'                : FIELD_TYPE_REFDATA,
                            //'manualRenewalDate'       : FIELD_TYPE_PROPERTY,
                            //'manualCancellationDate'  : FIELD_TYPE_PROPERTY
                    ],
                    filter : [
                            default : [
                                    [ 'form', 'kind', 'status' ],
                                    [ 'resource', 'hasPublishComponent', 'hasPerpetualAccess' ],
                                    [ 'isPublicForApi', 'isMultiYear' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' ]
                            ]
                    ],
                    query : [
                            default : [
                                    'subscription' : [
                                            'subscription-form',
                                            'subscription-kind',
                                            'subscription-resource',
                                            'subscription-status',
                                            'subscription-isMultiYear',
                                            'subscription-manualCancellationDate',
                                            'subscription-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [ // TODO ..
                                     'subscription-x-identifier' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'Lizenzen', 'Vergebene Identifikatoren' ]
                                     ],
                                     'subscription-x-property' : [
                                            detailsTemplate     : 'subscription',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'Lizenzen', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                     ],
                                     'subscription-x-annual' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-memberAnnual' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-provider' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : 'generic',
                                             chartLabels         : []
                                     ],
                                     'subscription-x-memberProvider' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ],
//                                     'subscription-x-memberCost-TODO' : [
//                                             label              : 'Anbieter → Lizenz → Teilnehmerkosten',
//                                             detailsTemplate    : 'TODO',
//                                             chartTemplate      : 'generic',
//                                             chartLabels        : []
//                                     ],
                                     'subscription-x-platform' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'Ermittelt durch Bestand', 'Zuordnung über Anbieter' ]
                                     ],
                                     'subscription-x-memberSubscription' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-member' : [
                                             detailsTemplate    : 'organisation',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ]
                            ]
                    ]
            ],

            memberSubscription : [
                    meta : [
                            class:  Subscription,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-memberSubscription'
                    ],
                    fields : [
                            'annual'                : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'endDateLimit'          : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'form'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'hasPerpetualAccess'    : BaseConfig.FIELD_TYPE_PROPERTY,
                            'hasPublishComponent'   : BaseConfig.FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : BaseConfig.FIELD_TYPE_PROPERTY,
                            'isMultiYear'           : BaseConfig.FIELD_TYPE_PROPERTY,
                            'kind'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'resource'              : BaseConfig.FIELD_TYPE_REFDATA,
                            'startDateLimit'        : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'status'                : BaseConfig.FIELD_TYPE_REFDATA,
                    ],
                    filter : [
                            default : [
                                    [ 'form', 'kind', 'status' ],
                                    [ 'resource', 'hasPublishComponent', 'hasPerpetualAccess' ],
                                    [ 'isPublicForApi', 'isMultiYear' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'memberSubscription' : [
                                                 'memberSubscription-form',
                                                 'memberSubscription-kind',
                                                 'memberSubscription-resource',
                                                 'memberSubscription-status',
                                                 'memberSubscription-isMultiYear',
                                                 'memberSubscription-manualCancellationDate',
                                                 'memberSubscription-*'
                                    ]
                            ]
                    ]
            ],

            member : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-member'
                    ],
                    fields : [
                            'country'           : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'            : BaseConfig.FIELD_TYPE_REFDATA,
                            'customerType'      : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : BaseConfig.FIELD_TYPE_PROPERTY,
                            'funderHskType'     : BaseConfig.FIELD_TYPE_REFDATA,
                            'funderType'        : BaseConfig.FIELD_TYPE_REFDATA,
                            'legalInfo'         : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'libraryNetwork'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'libraryType'       : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'           : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : BaseConfig.FIELD_TYPE_CUSTOM_IMPL
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ],
                                    [ 'orgType', 'eInvoice' ],
                                    [ 'customerType', 'legalInfo' ]
                            ]
                    ],
                    query : [
                            default : [
                                    'member' : [
                                            'member-orgType',
                                            'member-customerType',
                                            'member-libraryType',
                                            'member-region',
                                            'member-subjectGroup',
                                            'member-libraryNetwork',
                                            'member-funderType',
                                            'member-funderHskType',
                                            'member-*'
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-provider'
                    ],
                    fields : [
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE
                    ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    'provider' : [
                                            'provider-orgType',
                                            'provider-*'
                                            //'provider-country',
                                            //'provider-region'
                                    ]
                            ]
                    ]
            ],

            agency : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-agency'
                    ],
                    fields : [
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE
                    ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    'agency' : [
                                            'agency-orgType',
                                            'agency-*'
                                    ]
                            ]
                    ]
            ]
    ]
}
