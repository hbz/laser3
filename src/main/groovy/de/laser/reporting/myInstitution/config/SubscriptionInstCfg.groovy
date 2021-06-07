package de.laser.reporting.myInstitution.config

import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.myInstitution.base.BaseConfig

class SubscriptionInstCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    source : [
                            'inst-sub'            : 'Meine Lizenzen (alle)',
                            'inst-sub-consortia'  : 'Meine zentral verwalteten Lizenzen',
                            'inst-sub-local'      : 'Meine lokalen Lizenzen',
                    ],
                    fields : [
                            'annual'                : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'endDate'               : BaseConfig.FIELD_TYPE_PROPERTY,
                            'form'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'hasPerpetualAccess'    : BaseConfig.FIELD_TYPE_PROPERTY,
                            'hasPublishComponent'   : BaseConfig.FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : BaseConfig.FIELD_TYPE_PROPERTY,
                            'isMultiYear'           : BaseConfig.FIELD_TYPE_PROPERTY,
                            'kind'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'resource'              : BaseConfig.FIELD_TYPE_REFDATA,
                            'startDate'             : BaseConfig.FIELD_TYPE_PROPERTY,
                            'status'                : BaseConfig.FIELD_TYPE_REFDATA,
                            //'type'                : FIELD_TYPE_REFDATA,
                            //'manualRenewalDate'       : FIELD_TYPE_PROPERTY,
                            //'manualCancellationDate'  : FIELD_TYPE_PROPERTY
                    ],
                    filter : [
                            default : [
                                    [ 'form', 'kind', 'status', 'annual' ],
                                    [ 'resource', 'hasPerpetualAccess', 'hasPublishComponent' ],
                                    [ 'isMultiYear', 'isPublicForApi' ],
                                    [ 'startDate', 'endDate' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'Lizenz' : [ // TODO ..
                                             'subscription-form'        : 'Lizenzform',
                                             'subscription-kind'        : 'Lizenztyp',
                                             'subscription-resource'    : 'Ressourcentyp',
                                             'subscription-status'      : 'Lizenzstatus',
                                             'subscription-isMultiYear' : 'Mehrjahreslaufzeit',
                                             'subscription-manualCancellationDate'  : 'Kündigungsdatum',
                                             'subscription-*'           : 'Alle'
                                    ]
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                     'subscription-x-provider' : [
                                             label              : 'Lizenz → Anbieter',
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ],
                                     'subscription-x-platform' : [
                                             label              : 'Lizenz → Anbieter → Plattform',
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis2values_nonMatches',
                                             chartLabels        : [ 'Ermittelt durch Bestand', 'Zuordnung über Anbieter' ]
                                     ],
                                     'subscription-x-property' : [
                                             label              : 'Lizenz → Merkmale (eigene/allgemeine)',
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis2values',
                                             chartLabels        : [ 'Lizenzen', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                     ],
                                     'subscription-x-identifier' : [
                                             label              : 'Lizenz → Identifikatoren',
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : '2axis2values_nonMatches',
                                             chartLabels        : [ 'Lizenzen', 'Vergebene Identifikatoren' ]
                                     ],
                                     'subscription-x-annual' : [
                                             label              : 'Lizenz → Jahresring',
                                             detailsTemplate    : 'subscription',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ]
                            ]
                    ]
            ],

            consortium : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-consortium' : 'Betreffende Konsortialstellen'
                    ],
                    fields : [
                            'country'           : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'            : BaseConfig.FIELD_TYPE_REFDATA,
                            //'customerType'      : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : BaseConfig.FIELD_TYPE_PROPERTY,
                            'funderHskType'     : BaseConfig.FIELD_TYPE_REFDATA,
                            'funderType'        : BaseConfig.FIELD_TYPE_REFDATA,
                            'legalInfo'         : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'libraryNetwork'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'libraryType'       : BaseConfig.FIELD_TYPE_REFDATA,
                            //'orgType'           : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : BaseConfig.FIELD_TYPE_CUSTOM_IMPL
                    ],
                    filter : [
                            default: [
                                    [ 'country', 'region', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ],
                                    [ 'eInvoice' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'Konsortialstelle' : [ // TODO ..
                                             //'consortium-orgType'            : 'Organisationstyp',
                                             //'consortium-customerType'       : 'Kundentyp',
                                             'consortium-libraryType'        : 'Bibliothekstyp',
                                             'consortium-region'             : 'Bundesländer',
                                             'consortium-subjectGroup'       : 'Fächergruppen',
                                             'consortium-libraryNetwork'     : 'Verbundzugehörigkeit',
                                             'consortium-funderType'         : 'Unterhaltsträger',
                                             'consortium-funderHskType'      : 'Trägerschaft',
                                             'consortium-*'                  : 'Alle'
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-provider' : 'Betreffende Anbieter'
                    ],
                    fields : [
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE
                    ],
                    filter : [
                            default: []
                    ],
                    query : [
                            default: [
                                    'Anbieter' : [ // TODO ..
                                               'provider-orgType'      : 'Organisationstyp',
                                               'provider-*'            : 'Alle'
                                              // 'provider-country'      : 'Länder',
                                              // 'provider-region'       : 'Bundesländer'
                                    ]
                            ]
                    ]
            ],

            agency : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-agency' : 'Betreffende Lieferanten'
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
                                    'Lieferant' : [ // TODO ..
                                           'agency-orgType'      : 'Organisationstyp',
                                           'agency-*'            : 'Alle'
                                           // 'provider-country'      : 'Länder',
                                           // 'provider-region'       : 'Bundesländer'
                                    ]
                            ]
                    ]
            ]
    ]
}
