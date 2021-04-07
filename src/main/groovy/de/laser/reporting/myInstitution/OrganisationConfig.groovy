package de.laser.reporting.myInstitution

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
                            'country'           : GenericConfig.FIELD_TYPE_REFDATA,
                            'customerType'      : GenericConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : GenericConfig.FIELD_TYPE_PROPERTY,
                            'funderHskType'     : GenericConfig.FIELD_TYPE_REFDATA,
                            'funderType'        : GenericConfig.FIELD_TYPE_REFDATA,
                            'legalInfo'         : GenericConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'libraryNetwork'    : GenericConfig.FIELD_TYPE_REFDATA,
                            'libraryType'       : GenericConfig.FIELD_TYPE_REFDATA,
                            'orgType'           : GenericConfig.FIELD_TYPE_REFDATA_JOINTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : GenericConfig.FIELD_TYPE_CUSTOM_IMPL,
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
                                    'org-property-assignment' : [
                                            label: 'Organisation → Merkmale (eigene/öffentliche)',
                                            template: '2axis2values',
                                            chartLabels: [ 'Organisationen', 'Vergebene Merkmale (eigene/öffentliche)' ]
                                            ],
                                    'org-identifier-assignment' : [
                                            label : 'Organisation → Identifikatoren',
                                            template: '2axis2values_nonMatches',
                                            chartLabels: [ 'Organisationen', 'Vergebene Identifikatoren' ]
                                    ]
                                    //'org-serverAccess-assignment' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
