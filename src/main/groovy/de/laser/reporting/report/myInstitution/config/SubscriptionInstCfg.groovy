package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.report.myInstitution.base.BaseConfig

class SubscriptionInstCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Subscription,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'inst-sub',
                            'inst-sub-consortia',
                            'inst-sub-local'
                    ],
                    fields : [
                            'annual'                : [ FIELD_TYPE_CUSTOM_IMPL, FIELD_IS_MULTIPLE ],
                            'endDateLimit'          : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'form'                  : [ FIELD_TYPE_REFDATA ],
                            'hasPerpetualAccess'    : [ FIELD_TYPE_PROPERTY ],
                            'hasPublishComponent'   : [ FIELD_TYPE_PROPERTY ],
                            'isPublicForApi'        : [ FIELD_TYPE_PROPERTY ],
                            'isMultiYear'           : [ FIELD_TYPE_PROPERTY ],
                            'kind'                  : [ FIELD_TYPE_REFDATA ],
                            'propertyKey'           : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'propertyValue'         : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'resource'              : [ FIELD_TYPE_REFDATA ],
                            'startDateLimit'        : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'status'                : [ FIELD_TYPE_REFDATA ],
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
                            default: [
                                    subscription : [
                                             'subscription-form' :                   [ '@' ],
                                             'subscription-kind' :                   [ '@' ],
                                             'subscription-resource' :               [ '@' ],
                                             'subscription-status' :                 [ '@' ],
                                             'subscription-isMultiYear' :            [ '@' ],
                                             'subscription-manualCancellationDate' : [ '@' ],
                                             'subscription-*' :                      [ 'generic-*' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                     'subscription-x-identifier' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis2values_nonMatches',
                                             chartLabels        : [ 'base', 'x.identifiers' ]
                                     ],
                                     'subscription-x-property' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis3values',
                                             chartLabels        : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                     ],
                                     'subscription-x-annual' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'annual',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-provider' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-platform' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis2values_nonMatches',
                                             chartLabels        : [ 'x.platforms.1', 'x.platforms.2' ]
                                     ]
                            ]
                    ]
            ],

            consortium : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-consortium'
                    ],
                    fields : [
                            'country'           : [ FIELD_TYPE_REFDATA ],
                            'region'            : [ FIELD_TYPE_REFDATA, FIELD_IS_VIRTUAL ],
                            //'customerType'      : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'eInvoice'          : [ FIELD_TYPE_PROPERTY ],
                            'funderHskType'     : [ FIELD_TYPE_REFDATA ],
                            'funderType'        : [ FIELD_TYPE_REFDATA ],
                            'legalInfo'         : [ FIELD_TYPE_CUSTOM_IMPL ],
                            'libraryNetwork'    : [ FIELD_TYPE_REFDATA ],
                            'libraryType'       : [ FIELD_TYPE_REFDATA ],
                            //'orgType'           : [ FIELD_TYPE_REFDATA_JOINTABLE ],
                            'subjectGroup'      : [ FIELD_TYPE_CUSTOM_IMPL ]
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ],
                                    [ 'eInvoice' ]
                            ]
                    ],
                    query : [
                            default : [
                                    consortium : [
                                             //'consortium-orgType :        [ 'generic-orgType' ],
                                             //'consortium-customerType' :  [ 'generic-customerType' ],
                                             'consortium-libraryType' :     [ 'generic-libraryType' ],
                                             'consortium-region' :          [ 'generic-region' ],
                                             'consortium-subjectGroup' :    [ 'generic-subjectGroup' ],
                                             'consortium-libraryNetwork' :  [ 'generic-libraryNetwork' ],
                                             'consortium-funderType' :      [ 'generic-funderType' ],
                                             'consortium-funderHskType' :   [ 'generic-funderHskType' ],
                                             'consortium-*' :               [ 'generic-*' ]
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
                                               'provider-orgType' : [ 'generic-orgType'],
                                               'provider-*' :       [ 'generic-*']
                                              // 'provider-country'
                                              // 'provider-region'
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
                                           'agency-orgType' : [ 'generic-orgType' ],
                                           'agency-*' :       [ 'generic-*' ]
                                    ]
                            ]
                    ]
            ]
    ]
}
