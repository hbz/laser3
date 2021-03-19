package de.laser.reporting.myInstitution

import de.laser.Org
import de.laser.Subscription
import de.laser.reporting.myInstitution.GenericConfig

class SubscriptionConfig extends GenericConfig {

    static String KEY = 'subscription'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    source : [
                            //'all-sub' : 'Alle Lizenzen',
                            'consortia-sub' : 'Meine Lizenzen'
                    ],
                    fields: [
                            'endDate'               : GenericConfig.FIELD_TYPE_PROPERTY,
                            'form'                  : GenericConfig.FIELD_TYPE_REFDATA,
                            'hasPerpetualAccess'    : GenericConfig.FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : GenericConfig.FIELD_TYPE_PROPERTY,
                            'kind'                  : GenericConfig.FIELD_TYPE_REFDATA,
                            'resource'              : GenericConfig.FIELD_TYPE_REFDATA,
                            'startDate'             : GenericConfig.FIELD_TYPE_PROPERTY,
                            'status'                : GenericConfig.FIELD_TYPE_REFDATA,
                            //'type'                : FIELD_TYPE_REFDATA,
                            //'isMultiYear'         : FIELD_TYPE_PROPERTY,
                            //'manualRenewalDate'       : FIELD_TYPE_PROPERTY,
                            //'manualCancellationDate'  : FIELD_TYPE_PROPERTY
                    ],
                    filter : [
                            default: [
                                    [ 'form', 'kind', 'status' ],
                                    [ 'resource', 'hasPerpetualAccess', 'isPublicForApi' ],
                                    [ 'startDate', 'endDate' ]
                            ]
                    ],
                    query : [
                            'Lizenz' : [
                                    'subscription-form'         : 'Lizenzform',
                                    'subscription-kind'         : 'Lizenztyp',
                                    'subscription-resource'     : 'Ressourcentyp',
                                    'subscription-status'       : 'Lizenzstatus'
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [
                                    'subscription-provider-assignment' : [
                                            label : 'Lizenz → Anbieter',
                                            chartLabels : []
                                    ],
                                    'subscription-platform-assignment' : [
                                            label : 'Lizenz → Anbieter → Plattform',
                                            chartLabels : [ 'Ermittelt durch Bestand', 'Zuordnung über Anbieter' ]
                                    ],
                                    'subscription-property-assignment' : [
                                            label : 'Lizenz → Merkmale (eigene/öffentliche)',
                                            chartLabels : [ 'Lizenzen', 'Vergebene Merkmale (eigene/öffentliche)' ]
                                    ],
                                    'subscription-identifier-assignment' : [
                                            label : 'Lizenz → Identifikatoren',
                                            chartLabels : [ 'Lizenzen', 'Vergebene Identifikatoren' ]
                                    ]
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
                            'country'           : GenericConfig.FIELD_TYPE_REFDATA,
                            'customerType'      : GenericConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : GenericConfig.FIELD_TYPE_PROPERTY,
                            'funderHskType'     : GenericConfig.FIELD_TYPE_REFDATA,
                            'funderType'        : GenericConfig.FIELD_TYPE_REFDATA,
                            'legalInfo'         : GenericConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'libraryNetwork'    : GenericConfig.FIELD_TYPE_REFDATA,
                            'libraryType'       : GenericConfig.FIELD_TYPE_REFDATA,
                            'orgType'           : GenericConfig.FIELD_TYPE_REFDATA_RELTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : GenericConfig.FIELD_TYPE_REFDATA_RELTABLE
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
                            'Teilnehmer' : [
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
            ],

            provider : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-provider' : 'Alle betroffenen Anbieter'
                    ],
                    fields : [
                            'country'   : GenericConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : GenericConfig.FIELD_TYPE_REFDATA_RELTABLE
                    ],
                    filter : [
                            default: [
                                    [ 'country' ]
                            ]
                    ],
                    query : [
                            'Anbieter' : [
                                    'provider-orgType'      : 'Organisationstyp',
                                    'provider-country'      : 'Länder',
                                    'provider-region'       : 'Bundesländer'
                            ]
                    ]
            ],
    ]
}
