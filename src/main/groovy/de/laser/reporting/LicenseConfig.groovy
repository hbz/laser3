package de.laser.reporting

import de.laser.License
import de.laser.Org
import de.laser.Subscription

class LicenseConfig extends GenericConfig {

    static String KEY = 'license'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: License
                    ],
                    source : [
                            //'all-lic' : 'Alle Verträge',
                            'consortia-lic' : 'Meine Verträge'
                    ],
                    fields: [
                            'endDate'               : FIELD_TYPE_PROPERTY,
                            'licenseCategory'       : FIELD_TYPE_REFDATA,
                            //'openEnded'             : FIELD_TYPE_REFDATA,
                            'startDate'             : FIELD_TYPE_PROPERTY,
                            'status'                : FIELD_TYPE_REFDATA,
                            'type'                  : FIELD_TYPE_REFDATA
                    ],
                    filter : [
                            default: [
                                    [ 'licenseCategory', 'type', 'status' ],
                                    [ 'startDate', 'endDate' /*, 'openEnded' */ ]
                            ]
                    ],
                    query : [
                            'Vertrag' : [
                                    'license-licenseCategory'   : 'Lizenzkategorie',
                                    'license-type'              : 'Lizenztyp',
                                    //'license-openEnded'         : 'Unbefristet',
                                    'license-status'            : 'Lizenzstatus'
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [
                                    'license-property-assignment' : [
                                            label : 'Vertrag → Merkmale (eigene/öffentliche)',
                                            chartLabels : [ 'Verträge', 'Vergebene Merkmale (eigene/öffentliche)' ]
                                    ],
                                    'license-identifier-assignment' : [
                                            label : 'Vertrag → Identifikatoren',
                                            chartLabels: [ 'Verträge', 'Vergebene Identifikatoren' ]
                                    ],
                            ]
                    ]
            ],

            /*member : [
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
            ],*/

            licensor : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-licensor' : 'Alle betroffenen Lizenzgeber'
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
                            'Lizenzgeber' : [
                                    'licensor-orgType'      : 'Organisationstyp',
                                    'licensor-country'      : 'Länder',
                                    'licensor-region'       : 'Bundesländer'
                            ]
                    ]
            ],
    ]
}
