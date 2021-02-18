package de.laser.reporting

import de.laser.Org

class OrganisationConfig extends GenericConfig {

    static String KEY = 'organisation'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'all-org'       : 'Alle Organisationen',
                            'all-inst'      : 'Alle Einrichtungen',
                            'all-provider'  : 'Alle Anbieter und Lieferanten',
                            'my-inst'       : 'Meine Einrichtungen',
                            'my-provider'   : 'Meine Anbieter und Lieferanten'
                    ],
                    filter : [
                            [
                                    'country'           : FIELD_TYPE_REFDATA,
                                    //'region'            : FIELD_TYPE_REFDATA,
                                    'libraryType'       : FIELD_TYPE_REFDATA,
                                    'libraryNetwork'    : FIELD_TYPE_REFDATA,
                                    'funderType'        : FIELD_TYPE_REFDATA,
                                    'funderHskType'     : FIELD_TYPE_REFDATA
                            ],
                            [
                                    'eInvoice'          : FIELD_TYPE_PROPERTY,
                                    'subjectGroup'      : FIELD_TYPE_REFDATA_RELTABLE
                            ]
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
}
