package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.report.myInstitution.base.BaseConfig

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
                            'annual'                : [ FIELD_TYPE_CUSTOM_IMPL, FIELD_IS_MULTIPLE ],    // TODO custom_impl
                            'endDateLimit'          : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'form'                  : [ FIELD_TYPE_REFDATA ],
                            'hasPerpetualAccess'    : [ FIELD_TYPE_PROPERTY ],
                            'hasPublishComponent'   : [ FIELD_TYPE_PROPERTY ],
                            'isPublicForApi'        : [ FIELD_TYPE_PROPERTY ],
                            'isMultiYear'           : [ FIELD_TYPE_PROPERTY ],
                            'kind'                  : [ FIELD_TYPE_REFDATA ],
                            'propertyKey'           : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'propertyValue'         : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'resource'              : [ FIELD_TYPE_REFDATA ],
                            'startDateLimit'        : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'status'                : [ FIELD_TYPE_REFDATA ]
                            //'type'                : [ FIELD_TYPE_REFDATA ],
                            //'manualRenewalDate'       : [ FIELD_TYPE_PROPERTY ],
                            //'manualCancellationDate'  : FIELD_TYPE_PROPERTY
                    ],
                    filter : [
                            default : [
                                    [ 'form', 'kind', 'status' ],
                                    [ 'resource', 'hasPublishComponent', 'hasPerpetualAccess' ],
                                    [ 'isPublicForApi', 'isMultiYear' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' ],
                                    [ 'propertyKey', 'propertyValue' ]
                            ]
                    ],
                    query : [
                            default : [
                                    subscription : [
                                            'subscription-form' :                   [ 'generic.sub.form' ],
                                            'subscription-kind' :                   [ 'generic.sub.kind' ],
                                            'subscription-resource' :               [ 'generic.sub.resource' ],
                                            'subscription-status' :                 [ 'generic.sub.status' ],
                                            'subscription-isMultiYear' :            [ 'generic.sub.isMultiYear' ],
                                            'subscription-manualCancellationDate' : [ 'generic.sub.manualCancellationDate' ],
                                            'subscription-*' :                      [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                     'subscription-x-identifier' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'base', 'x.identifiers' ]
                                     ],
                                     'subscription-x-property' : [
                                            detailsTemplate     : 'subscription',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                     ],
                                     'subscription-x-memberSubscriptionProperty' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : '2axis3values',
                                             chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                     ],
                                     'subscription-x-annual' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'annual',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-memberAnnual' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'annual',
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
                                             chartLabels         : [ 'x.platforms.1', 'x.platforms.2' ]
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
                            'annual'                : [ FIELD_TYPE_CUSTOM_IMPL, FIELD_IS_MULTIPLE ],    // TODO custom_impl
                            'endDateLimit'          : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'form'                  : [ FIELD_TYPE_REFDATA ],
                            'hasPerpetualAccess'    : [ FIELD_TYPE_PROPERTY ],
                            'hasPublishComponent'   : [ FIELD_TYPE_PROPERTY ],
                            'isPublicForApi'        : [ FIELD_TYPE_PROPERTY ],
                            'isMultiYear'           : [ FIELD_TYPE_PROPERTY ],
                            'kind'                  : [ FIELD_TYPE_REFDATA ],
                            'propertyKey'           : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'propertyValue'         : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'resource'              : [ FIELD_TYPE_REFDATA ],
                            'startDateLimit'        : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'status'                : [ FIELD_TYPE_REFDATA ]
                    ],
                    filter : [
                            default : [
                                    [ 'form', 'kind', 'status' ],
                                    [ 'resource', 'hasPublishComponent', 'hasPerpetualAccess' ],
                                    [ 'isPublicForApi', 'isMultiYear' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' ],
                                    [ 'propertyKey', 'propertyValue' ]
                            ]
                    ],
                    query : [
                            default: [
                                    memberSubscription : [
                                                 'memberSubscription-form' :                    [ 'generic.sub.form' ],
                                                 'memberSubscription-kind' :                    [ 'generic.sub.kind' ],
                                                 'memberSubscription-resource' :                [ 'generic.sub.resource' ],
                                                 'memberSubscription-status' :                  [ 'generic.sub.status' ],
                                                 'memberSubscription-isMultiYear' :             [ 'generic.sub.isMultiYear' ],
                                                 'memberSubscription-manualCancellationDate' :  [ 'generic.sub.manualCancellationDate' ],
                                                 'memberSubscription-*' :                       [ 'generic.all' ]
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
                            'country'           : [ FIELD_TYPE_REFDATA ],
                            'region'            : [ FIELD_TYPE_REFDATA, FIELD_IS_VIRTUAL ],
                            'customerType'      : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'eInvoice'          : [ FIELD_TYPE_PROPERTY ],
                            'funderHskType'     : [ FIELD_TYPE_REFDATA ],
                            'funderType'        : [ FIELD_TYPE_REFDATA ],
                            'legalInfo'         : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'libraryNetwork'    : [ FIELD_TYPE_REFDATA ],
                            'libraryType'       : [ FIELD_TYPE_REFDATA ],
                            'orgType'           : [ FIELD_TYPE_REFDATA_JOINTABLE ],
                            'propertyKey'       : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'propertyValue'     : [ FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'subjectGroup'      : [ FIELD_TYPE_CUSTOM_IMPL ]    // TODO custom_impl
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ],
                                    [ 'orgType', 'eInvoice' ],
                                    [ 'customerType', 'legalInfo' ],
                                    [ 'propertyKey', 'propertyValue' ]
                            ]
                    ],
                    query : [
                            default : [
                                    member : [
                                            'member-orgType' :          [ 'generic.org.orgType' ],
                                            'member-customerType' :     [ 'generic.org.customerType' ],
                                            'member-libraryType' :      [ 'generic.org.libraryType' ],
                                            'member-region' :           [ 'generic.org.region' ],
                                            'member-subjectGroup' :     [ 'generic.org.subjectGroup' ],
                                            'member-libraryNetwork' :   [ 'generic.org.libraryNetwork' ],
                                            'member-funderType' :       [ 'generic.org.funderType' ],
                                            'member-funderHskType' :    [ 'generic.org.funderHskType' ],
                                            'member-*' :                [ 'generic.all' ]
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
                            'country'   : [ FIELD_TYPE_REFDATA ],
                            'region'    : [ FIELD_TYPE_REFDATA, FIELD_IS_VIRTUAL ],
                            'orgType'   : [ FIELD_TYPE_REFDATA_JOINTABLE ]
                    ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    provider : [
                                            'provider-orgType' : [ 'generic.org.orgType' ],
                                            'provider-*' :       [ 'generic.all' ]
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
                            'country'   : [ FIELD_TYPE_REFDATA ],
                            'region'    : [ FIELD_TYPE_REFDATA, FIELD_IS_VIRTUAL ],
                            'orgType'   : [ FIELD_TYPE_REFDATA_JOINTABLE ]
                    ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    agency : [
                                            'agency-orgType' : [ 'generic.org.orgType' ],
                                            'agency-*' :       [ 'generic.all' ],
                                    ]
                            ]
                    ]
            ]
    ]
}
