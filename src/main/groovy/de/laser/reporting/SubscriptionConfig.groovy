package de.laser.reporting

import de.laser.Org
import de.laser.Subscription

class SubscriptionConfig extends GenericConfig {

    static String KEY = 'subscription'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    source : [
                            //'all-sub' : 'Alle Lizenzen',
                            'my-sub' : 'Meine Lizenzen'
                    ],
                    fields: [
                            'endDate'               : FIELD_TYPE_PROPERTY,
                            'form'                  : FIELD_TYPE_REFDATA,
                            'hasPerpetualAccess'    : FIELD_TYPE_PROPERTY,
                            'isPublicForApi'        : FIELD_TYPE_PROPERTY,
                            'kind'                  : FIELD_TYPE_REFDATA,
                            'resource'              : FIELD_TYPE_REFDATA,
                            'startDate'             : FIELD_TYPE_PROPERTY,
                            'status'                : FIELD_TYPE_REFDATA,
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
                            'Verteilung' : [
                                    'subscription-form'         : 'Lizenzform',
                                    'subscription-kind'         : 'Lizenztyp',
                                    'subscription-resource'     : 'Ressourcentyp',
                                    'subscription-status'       : 'Lizenzstatus',
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
                            'country'           : FIELD_TYPE_REFDATA,
                            'customerType'      : FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : FIELD_TYPE_PROPERTY,
                            'funderHskType'     : FIELD_TYPE_REFDATA,
                            'funderType'        : FIELD_TYPE_REFDATA,
                            'legalInfo'         : FIELD_TYPE_CUSTOM_IMPL,
                            'libraryNetwork'    : FIELD_TYPE_REFDATA,
                            'libraryType'       : FIELD_TYPE_REFDATA,
                            'orgType'           : FIELD_TYPE_REFDATA_RELTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : FIELD_TYPE_REFDATA_RELTABLE
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
                            'country'   : FIELD_TYPE_REFDATA,
                            'orgType'   : FIELD_TYPE_REFDATA_RELTABLE,
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
