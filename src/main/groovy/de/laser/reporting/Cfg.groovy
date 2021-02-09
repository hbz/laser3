package de.laser.reporting

import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.RDConstants

class Cfg {

    static String filterPrefix          = 'filter:'

    static String FORM_TYPE_PROPERTY            = 'property'
    static String FORM_TYPE_REFDATA             = 'refdata'
    static String FORM_TYPE_REFDATA_RELTABLE    = 'refdataRelationTable'

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
                                    'funderHskType'     : FORM_TYPE_REFDATA,
                                    'subjectGroup'      : FORM_TYPE_REFDATA_RELTABLE
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
                            'org-libraryType'   : 'Verteilung: Bibliothekstyp',
                            'org-region'        : 'Verteilung: Bundesländer',
                            'org-subjectGroup'  : 'Verteilung: Fächergruppen'
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
                            'subscription-form  '       : 'Verteilung: Lizenzform',
                            'subscription-kind'         : 'Verteilung: Lizenztyp',
                            'subscription-resource'     : 'Verteilung: Ressourcentyp',
                            'subscription-status'       : 'Verteilung: Lizenzstatus',
                            'member-libraryType'        : 'Bibliothekstyp aller Teilnehmer',
                            'member-region'             : 'Bundesländer aller Teilnehmer',
                            'member-subjectGroup'       : 'Fächergruppen aller Teilnehmer',
                            'provider-libraryType'      : 'Bibliothekstyp aller Anbieter',
                            'provider-region'           : 'Bundesländer aller Anbieter',
                            'provider-country'          : 'Länder aller Anbieter'
                    ]
            ]
    ]

    static Map<String, Object> getRefdataRelTableInfo(String key) {

        if (key == 'subjectGroup') {
            return [ label: 'Fächergruppe', from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP) ]
        }
    }
}
