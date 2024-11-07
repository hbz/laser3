package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.wekb.Vendor
import de.laser.reporting.report.myInstitution.base.BaseConfig

class SubscriptionInstCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Subscription,
                            cfgKey: BaseConfig.KEY_SUBSCRIPTION
                    ],
                    source : [
                            'inst-sub',
                            'inst-sub-consortia',
                            'inst-sub-local'
                    ],
                    fields : [
                            'annual'                : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, spec: BaseConfig.FIELD_IS_MULTIPLE ],    // TODO custom_impl
                            'endDateLimit'          : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'form'                  : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'hasPerpetualAccess'    : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'hasPublishComponent'   : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'isPublicForApi'        : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'isMultiYear'           : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'kind'                  : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'propertyKey'           : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'propertyValue'         : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'referenceYear'         : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'resource'              : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'startDateLimit'        : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'status'                : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            //'type'                : [ type: FIELD_TYPE_REFDATA ],
                    ],
                    filter : [
                            default : [
                                    [ 'form', 'kind', 'status' ],
                                    [ 'resource', 'hasPublishComponent', 'hasPerpetualAccess' ],
                                    [ 'isPublicForApi', 'isMultiYear', 'referenceYear' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' ],
                                    [ 'propertyKey', 'propertyValue' ]
                            ]
                    ],
                    query : [
                            default: [
                                    subscription : [
                                             'subscription-form' :                   [ 'generic.sub.form' ],
                                             'subscription-kind' :                   [ 'generic.sub.kind' ],
                                             'subscription-resource' :               [ 'generic.sub.resource' ],
                                             'subscription-status' :                 [ 'generic.sub.status' ],
                                             'subscription-referenceYear' :          [ 'generic.sub.referenceYear' ],
                                             'subscription-isMultiYear' :            [ 'generic.sub.isMultiYear' ],
                                             'subscription-manualCancellationDate' : [ 'generic.sub.manualCancellationDate' ],
                                             'subscription-*' :                      [ 'generic.all' ]
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
                                     'subscription-x-license' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : 'generic',
                                             chartLabels         : []
                                     ],
                                     'subscription-x-licenseCategory' : [
                                             detailsTemplate     : 'subscription',
                                             chartTemplate       : 'generic',
                                             chartLabels         : []
                                     ],
                                     'subscription-x-provider' : [
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis2values_nonMatches', // generic
                                             chartLabels        : [ 'x.providers.1', 'x.providers.2' ] // []
                                     ],
                                     'subscription-x-vendor' : [
                                            detailsTemplate     : 'subscription',
                                            chartTemplate       : '2axis2values_nonMatches', // generic
                                            chartLabels         : [ 'x.vendors.1', 'x.vendors.2' ] // []
                                    ],
//                                     'subscription-x-platform' : [ // TODO
//                                             detailsTemplate    : 'subscription',
//                                             chartTemplate      : '2axis2values_nonMatches',
//                                             chartLabels        : [ 'x.platforms.1', 'x.platforms.2' ]
//                                     ]
                            ]
                    ]
            ],

            consortium : [
                    meta : [
                            class:  Org,
                            cfgKey: BaseConfig.KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-consortium'
                    ],
                    fields : [
                            'country'           : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'region'            : [type: BaseConfig.FIELD_TYPE_REFDATA, spec: BaseConfig.FIELD_IS_VIRTUAL ],
                            //'customerType'      : [ type: FIELD_TYPE_CUSTOM_IMPL ],
                            'eInvoice'          : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'funderHskType'     : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'funderType'        : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'legalInfo'         : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'libraryNetwork'    : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'libraryType'       : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'subjectGroup'      : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ]    // TODO custom_impl
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
                                             //'consortium-customerType' :  [ 'generic.org.customerType' ],
                                             'consortium-libraryType' :     [ 'generic.org.libraryType' ],
                                             'consortium-region' :          [ 'generic.org.region' ],
                                             'consortium-subjectGroup' :    [ 'generic.org.subjectGroup' ],
                                             'consortium-libraryNetwork' :  [ 'generic.org.libraryNetwork' ],
                                             'consortium-funderType' :      [ 'generic.org.funderType' ],
                                             'consortium-funderHskType' :   [ 'generic.org.funderHskType' ],
                                             'consortium-*' :               [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Provider,
                            cfgKey: BaseConfig.KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-provider'
                    ],
                    fields : [
                            'status' : [ type: BaseConfig.FIELD_TYPE_REFDATA ]
                    ],
                    filter : [
                            default : [
                                    [ 'status' ],
                            ]
                    ],
                    query : [
                            default : BaseConfig.GENERIC_PROVIDER_QUERY_DEFAULT
                    ]
            ],

            vendor : [
                    meta : [
                            class:  Vendor,
                            cfgKey: BaseConfig.KEY_SUBSCRIPTION
                    ],
                    source : [
                            'depending-vendor'
                    ],
                    fields : [
                            'status' : [ type: BaseConfig.FIELD_TYPE_REFDATA ]
                    ],
                    filter : [
                            default : [
                                    [ 'status' ],
                            ]
                    ],
                    query : [
                            default : BaseConfig.GENERIC_VENDOR_QUERY_DEFAULT
                    ]
            ]
    ]
}
