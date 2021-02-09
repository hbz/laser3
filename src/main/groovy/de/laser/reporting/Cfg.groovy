package de.laser.reporting

import de.laser.Org
import de.laser.Subscription

class Cfg {

    static String filterPrefix          = 'filter:'

    static String FORM_TYPE_PROPERTY    = 'property'
    static String FORM_TYPE_REFDATA     = 'refdata'

    static Map<String, Object> config = [

            filter:  [
                'organisation' : 'Organisationen',
                'subscription' : 'Lizenzen'
            ],

            charts: [
                'bar' : 'Balkendiagramm',
                'pie' : 'Tortendiagramm'
            ],

            // --- filter

            Organisation : [
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
                                    'funderHskType'     : FORM_TYPE_REFDATA
                            ]
                    ],
                    filter : [
                            'all-org'       : 'Alle Organisationen',
                            'all-inst'      : 'Alle Einrichtungen',
                            'all-provider'  : 'Alle Anbieter und Lieferanten',
                            'my-inst'       : 'Meine Einrichtungen',
                            'my-provider'   : 'Meine Anbieter und Lieferanten'
                    ],
                    query : [
                            'org-libraryType'   : 'Bibliothekstyp aller Organisationen',
                            'org-region'        : 'Bundesländer aller Organisationen'
                    ]
            ],

            Subscription : [
                    meta : [
                            class: Subscription
                    ],
                    form : [
                            /*[
                                    'startDate'                 : FORM_TYPE_PROPERTY,
                                    'endDate'                   : FORM_TYPE_PROPERTY,
                                    'manualRenewalDate'         : FORM_TYPE_PROPERTY,
                                    'manualCancellationDate'    : FORM_TYPE_PROPERTY
                            ],*/
                            [
                                    'form'              : FORM_TYPE_REFDATA,
                                    'kind'              : FORM_TYPE_REFDATA,
                                    'resource'          : FORM_TYPE_REFDATA,
                                    'status'            : FORM_TYPE_REFDATA,
                                    'type'              : FORM_TYPE_REFDATA
                            ]
                    ],
                    filter : [
                            //'all-sub'   : 'Alle Lizenzen',
                            'my-sub'    : 'Meine Lizenzen'
                    ],
                    query : [
                            'member-libraryType'        : 'Bibliothekstyp aller Teilnehmer',
                            'member-region'             : 'Bundesländer aller Teilnehmer',
                            'provider-libraryType'      : 'Bibliothekstyp aller Anbieter',
                            'provider-region'           : 'Bundesländer aller Anbieter',
                            'provider-country'          : 'Länder aller Anbieter',
                            'subscription-form  '       : 'Lizenzform',
                            'subscription-kind'         : 'Lizenztyp',
                            'subscription-resource'     : 'Ressourcentyp',
                            'subscription-status'       : 'Lizenzstatus'
                    ]
            ]
    ]
}
