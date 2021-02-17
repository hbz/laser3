package de.laser.reporting

import de.laser.Org
import grails.web.servlet.mvc.GrailsParameterMap

class OrganisationConfig extends GenericConfig {

    static String KEY = 'Organisation'

    static Map<String, Object> CONFIG = [

            base : [
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
                            ],
                            [
                                    'eInvoice'          : FORM_TYPE_PROPERTY,
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
                                    'org-libraryType'       : 'Bibliothekstyp',
                                    'org-region'            : 'Bundesländer',
                                    'org-subjectGroup'      : 'Fächergruppen',
                                    'org-libraryNetwork'    : 'Verbundzugehörigkeit',
                                    'org-funderType'        : 'Unterhaltsträger',
                                    'org-funderHskType'     : 'Trägerschaft'
                            ]
                    ]
            ]
    ]

    static String getQueryLabel(GrailsParameterMap params) {
        getQueryLabel(CONFIG, params)
    }
}
