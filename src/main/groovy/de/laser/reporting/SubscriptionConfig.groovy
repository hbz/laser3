package de.laser.reporting

import de.laser.Org
import de.laser.Subscription
import grails.web.servlet.mvc.GrailsParameterMap

class SubscriptionConfig extends GenericConfig {

    static String KEY = 'Subscription'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Subscription
                    ],
                    form : [
                            [
                                    'form'              : FORM_TYPE_REFDATA,
                                    'kind'              : FORM_TYPE_REFDATA,
                                    'resource'          : FORM_TYPE_REFDATA,
                                    'status'            : FORM_TYPE_REFDATA,
                                    'type'              : FORM_TYPE_REFDATA
                            ],
                            [
                                    //'isMultiYear'           : FORM_TYPE_PROPERTY,
                                    'hasPerpetualAccess'    : FORM_TYPE_PROPERTY,
                                    'isPublicForApi'        : FORM_TYPE_PROPERTY
                            ],
                            [
                                    'startDate'             : FORM_TYPE_PROPERTY,
                                    'endDate'               : FORM_TYPE_PROPERTY,
                                    //'manualRenewalDate'         : FORM_TYPE_PROPERTY,
                                    //'manualCancellationDate'    : FORM_TYPE_PROPERTY
                            ]
                    ],
                    filter : [
                            //'all-sub' : 'Alle Lizenzen',
                            'my-sub' : 'Meine Lizenzen'
                    ],
                    query : [
                            'Verteilung' : [
                                    'subscription-form  '       : 'Lizenzform',
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
                    form : [
                            [
                                    'country'           : FORM_TYPE_REFDATA,
                                    //'region'            : FORM_TYPE_REFDATA,
                                    'libraryType'       : FORM_TYPE_REFDATA,
                                    'libraryNetwork'    : FORM_TYPE_REFDATA,
                                    'funderType'        : FORM_TYPE_REFDATA,
                                    'funderHskType'     : FORM_TYPE_REFDATA,
                                    'subjectGroup'      : FORM_TYPE_REFDATA_RELTABLE
                            ],
                            [
                                    'eInvoice'          : FORM_TYPE_PROPERTY
                            ]
                    ],
                    filter : [
                            'depending-member' : 'Alle betroffenen Teilnehmer'
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
                    form : [
                            [
                                    'country' : FORM_TYPE_REFDATA
                            ]
                    ],
                    filter : [
                            'depending-provider' : 'Alle betroffenen Anbieter'
                    ],
                    query : [
                            'Anbieter' : [
                                    'provider-region'       : 'Bundesländer',
                                    'provider-country'      : 'Länder'
                            ]
                    ]
            ],
    ]

    static String getQueryLabel(GrailsParameterMap params) {
        getQueryLabel(CONFIG, params)
    }
}
