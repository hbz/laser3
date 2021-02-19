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
                    filter : [
                            [
                                    'form'              : FIELD_TYPE_REFDATA,
                                    'kind'              : FIELD_TYPE_REFDATA,
                                    'resource'          : FIELD_TYPE_REFDATA,
                                    'status'            : FIELD_TYPE_REFDATA,
                                    //'type'              : FIELD_TYPE_REFDATA
                            ],
                            [
                                    //'isMultiYear'           : FIELD_TYPE_PROPERTY,
                                    'hasPerpetualAccess'    : FIELD_TYPE_PROPERTY,
                                    'isPublicForApi'        : FIELD_TYPE_PROPERTY
                            ],
                            [
                                    'startDate'             : FIELD_TYPE_PROPERTY,
                                    'endDate'               : FIELD_TYPE_PROPERTY,
                                    //'manualRenewalDate'         : FIELD_TYPE_PROPERTY,
                                    //'manualCancellationDate'    : FIELD_TYPE_PROPERTY
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
                    filter : [
                            [
                                    'country'           : FIELD_TYPE_REFDATA,
                                    //'region'            : FIELD_TYPE_REFDATA,
                                    'libraryType'       : FIELD_TYPE_REFDATA,
                                    'libraryNetwork'    : FIELD_TYPE_REFDATA,
                                    'funderType'        : FIELD_TYPE_REFDATA,
                                    'funderHskType'     : FIELD_TYPE_REFDATA,
                                    'subjectGroup'      : FIELD_TYPE_REFDATA_RELTABLE
                            ],
                            [
                                    'eInvoice'          : FIELD_TYPE_PROPERTY
                            ]
                    ],
                    query : [
                            'Teilnehmer' : [
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
                    filter : [
                            [
                                    'country' : FIELD_TYPE_REFDATA
                            ]
                    ],
                    query : [
                            'Anbieter' : [
                                    'provider-region'       : 'Bundesländer',
                                    'provider-country'      : 'Länder'
                            ]
                    ]
            ],
    ]
}
