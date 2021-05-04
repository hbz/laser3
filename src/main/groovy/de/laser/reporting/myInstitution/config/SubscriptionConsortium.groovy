package de.laser.reporting.myInstitution.config

import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.myInstitution.base.BaseConfig

class SubscriptionConsortium extends BaseConfig {

    static String KEY = 'subscription'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    source : [
                            'consortia-sub' : 'Meine Lizenzen'
                    ],
                    fields: [
                            'annual'                : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'endDate'               : BaseConfig.FIELD_TYPE_PROPERTY,
                            'form'                  : BaseConfig.FIELD_TYPE_REFDATA,
                            'hasPerpetualAccess'    : BaseConfig.FIELD_TYPE_PROPERTY,
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
                            default: [
                                    [ 'form', 'kind', 'status', 'annual' ],
                                    [ 'hasPerpetualAccess', 'isMultiYear', 'isPublicForApi', 'resource' ],
                                    [ 'startDate', 'endDate' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'Lizenz' : [ // TODO ..
                                            'subscription-form'         : 'Lizenzform',
                                            'subscription-kind'         : 'Lizenztyp',
                                            'subscription-resource'     : 'Ressourcentyp',
                                            'subscription-status'       : 'Lizenzstatus',
                                            'subscription-isMultiYear'  : 'Mehrjahreslaufzeit',
                                            'subscription-manualCancellationDate'  : 'Kündigungsdatum'
                                    ]
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                    'subscription-provider-assignment' : [
                                            label : 'Lizenz → Anbieter',
                                            template: 'generic',
                                            chartLabels : []
                                    ],
                                    'subscription-platform-assignment' : [
                                            label : 'Lizenz → Anbieter → Plattform',
                                            template: '2axis2values_nonMatches',
                                            chartLabels : [ 'Ermittelt durch Bestand', 'Zuordnung über Anbieter' ]
                                    ],
                                    'subscription-property-assignment' : [
                                            label : 'Lizenz → Merkmale (eigene/allgemeine)',
                                            template: '2axis2values',
                                            chartLabels : [ 'Lizenzen', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                    ],
                                    'subscription-identifier-assignment' : [
                                            label : 'Lizenz → Identifikatoren',
                                            template: '2axis2values_nonMatches',
                                            chartLabels : [ 'Lizenzen', 'Vergebene Identifikatoren' ]
                                    ],
                                     'subscription-annual-assignment' : [
                                             label : 'Lizenz → Jahresring',
                                             template: 'generic',
                                             chartLabels : []
                                     ],
                                     'subscription-subscription-assignment' : [
                                             label : 'Teilnehmerlizenz → Lizenz',
                                             template: 'generic',
                                             chartLabels : []
                                     ],
                            ]
                    ]
            ],

            member : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-member' : 'Alle betroffenen Teilnehmer'
                    ],
                    fields : [
                            'country'           : BaseConfig.FIELD_TYPE_REFDATA,
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
                            default: [
                                    [ 'country', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ],
                                    [ 'orgType', 'eInvoice' ],
                                    [ 'customerType', 'legalInfo' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'Teilnehmer' : [ // TODO ..
                                            'member-orgType'            : 'Organisationstyp',
                                            'member-customerType'       : 'Kundentyp',
                                            'member-libraryType'        : 'Bibliothekstyp',
                                            'member-region'             : 'Bundesländer',
                                            'member-subjectGroup'       : 'Fächergruppen',
                                            'member-libraryNetwork'     : 'Verbundzugehörigkeit',
                                            'member-funderType'         : 'Unterhaltsträger',
                                            'member-funderHskType'      : 'Trägerschaft'
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-provider' : 'Alle betroffenen Anbieter'
                    ],
                    fields : [
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE
                    ],
                    filter : [
                            default: []
                    ],
                    query : [
                            default: [
                                    'Anbieter' : [ // TODO ..
                                            'provider-orgType'      : 'Organisationstyp',
                                            //'provider-country'      : 'Länder',
                                            //'provider-region'       : 'Bundesländer'
                                    ]
                            ]
                    ]
            ],

            agency : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-agency' : 'Alle betroffenen Lieferanten'
                    ],
                    fields : [
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE
                    ],
                    filter : [
                            default: []
                    ],
                    query : [
                            default: [
                                    'Lieferant' : [ // TODO ..
                                            'agency-orgType'      : 'Organisationstyp',
                                            // 'provider-country'      : 'Länder',
                                            // 'provider-region'       : 'Bundesländer'
                                    ]
                            ]
                    ]
            ]
    ]
}
