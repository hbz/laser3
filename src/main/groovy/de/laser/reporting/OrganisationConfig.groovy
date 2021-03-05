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
                    fields: [
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
                            'subjectGroup'      : FIELD_TYPE_REFDATA_RELTABLE,
                    ],
                    filter : [
                            default: [
                                    [ 'country',  'subjectGroup', 'orgType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ] ,
                                    [ 'libraryType', 'eInvoice' ],
                                    [ 'customerType', 'legalInfo' ]
                            ],
                            provider: [
                                    [ 'country', 'orgType'],
                                    [ 'customerType', 'legalInfo' ]
                            ]
                    ],
                    query : [
                            'Organisation' : [
                                    'org-orgType'           : 'Organisationstyp',
                                    'org-customerType'      : 'Kundentyp',
                                    'org-libraryType'       : 'Bibliothekstyp',
                                    'org-region'            : 'Bundesländer',
                                    'org-subjectGroup'      : 'Fächergruppen',
                                    'org-libraryNetwork'    : 'Verbundzugehörigkeit',
                                    'org-funderType'        : 'Unterhaltsträger',
                                    'org-funderHskType'     : 'Trägerschaft'
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [
                                    'org-property-assignment'   : 'Organisation → Merkmale (eigene/öffentliche)',
                                    'org-identifier-assignment' : 'Organisation → Identifikatoren',
                                    //'org-serverAccess-assignment' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
