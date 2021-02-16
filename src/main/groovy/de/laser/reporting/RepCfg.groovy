package de.laser.reporting

import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.helper.RDConstants
import grails.web.servlet.mvc.GrailsParameterMap

class RepCfg {

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
                            'Verteilung' : [
                                'org-libraryType'   : 'Bibliothekstyp',
                                'org-region'        : 'Bundesländer',
                                'org-subjectGroup'  : 'Fächergruppen'
                            ]
                    ]
            ],

            Subscription : [
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
                                    'startDate'                 : FORM_TYPE_PROPERTY,
                                    'endDate'                   : FORM_TYPE_PROPERTY,
                                    //'manualRenewalDate'         : FORM_TYPE_PROPERTY,
                                    //'manualCancellationDate'    : FORM_TYPE_PROPERTY
                            ]
                    ],
                    filter : [
                            //'all-sub'   : 'Alle Lizenzen',
                            'my-sub'    : 'Meine Lizenzen'
                    ],
                    query : [
                            'Verteilung' : [
                                'subscription-form  '       : 'Lizenzform',
                                'subscription-kind'         : 'Lizenztyp',
                                'subscription-resource'     : 'Ressourcentyp',
                                'subscription-status'       : 'Lizenzstatus',
                            ],
                            'Teilnehmer' : [
                                'member-libraryType'        : 'Bibliothekstyp',
                                'member-region'             : 'Bundesländer',
                                'member-subjectGroup'       : 'Fächergruppen',
                            ],
                            'Anbieter' : [
                                'provider-libraryType'      : 'Bibliothekstyp',
                                'provider-region'           : 'Bundesländer',
                                'provider-country'          : 'Länder'
                            ]
                    ]
            ]
    ]

    static String getFormFieldType(Map<String, Object> objConfig, String fieldName) {

        String fieldType = '' // [ property, refdata ]

        objConfig.form.each {
            if (it.keySet().contains(fieldName)) {
                fieldType = it.get(fieldName)
            }
        }
        fieldType
    }

    static String getQueryLabel(GrailsParameterMap params) {

        String label = ''

        config.each {it ->
            if (it.value.containsKey('query')) {
                it.value.get('query').each { it2 ->
                    if (it2.value.containsKey(params.query)) {
                        label = it2.key + ' > ' + it2.value.get(params.query) + ' > ' + params.label
                    }
                }
            }
        }
        label
    }

    static Map<String, Object> getRefdataRelTableInfo(String key) {

        if (key == 'subjectGroup') {
            return [ label: 'Fächergruppe', from: RefdataCategory.getAllRefdataValues(RDConstants.SUBJECT_GROUP) ]
        }
    }
}
