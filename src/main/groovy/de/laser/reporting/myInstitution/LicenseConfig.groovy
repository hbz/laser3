package de.laser.reporting.myInstitution

import de.laser.License
import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class LicenseConfig extends BaseConfig {

    static String KEY = 'license'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: License
                    ],
                    source : [
                            //'all-lic' : 'Alle Verträge',
                            'consortia-lic' : 'Meine Verträge (Konsortium)',
                            'my-lic'        : 'Meine Verträge (Vollnutzer)'
                    ],
                    fields: [
                            'endDate'               : BaseConfig.FIELD_TYPE_PROPERTY,
                            'licenseCategory'       : BaseConfig.FIELD_TYPE_REFDATA,
                            //'openEnded'             : FIELD_TYPE_REFDATA,
                            'startDate'             : BaseConfig.FIELD_TYPE_PROPERTY,
                            'status'                : BaseConfig.FIELD_TYPE_REFDATA,
                            'type'                  : BaseConfig.FIELD_TYPE_REFDATA
                    ],
                    filter : [
                            default: [
                                    [ 'licenseCategory', 'type', 'status' ],
                                    [ 'startDate', 'endDate' /*, 'openEnded' */ ]
                            ]
                    ],
                    query : [
                            'Vertrag' : [ // TODO ..
                                    'license-licenseCategory'   : 'Lizenzkategorie',
                                    'license-type'              : 'Lizenztyp',
                                    //'license-openEnded'         : 'Unbefristet',
                                    'license-status'            : 'Lizenzstatus'
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                    'license-property-assignment' : [
                                            label : 'Vertrag → Merkmale (eigene/allgemeine)',
                                            template: '2axis2values',
                                            chartLabels : [ 'Verträge', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                    ],
                                    'license-identifier-assignment' : [
                                            label : 'Vertrag → Identifikatoren',
                                            template: '2axis2values_nonMatches',
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
                            'orgType'           : FIELD_TYPE_REFDATA_JOINTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : FIELD_TYPE_REFDATA_JOINTABLE
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
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE,
                    ],
                    filter : [
                            default: [
                                    [ 'country' ]
                            ]
                    ],
                    query : [
                            'Lizenzgeber' : [ // TODO ..
                                    'licensor-orgType'      : 'Organisationstyp',
                                    'licensor-country'      : 'Länder',
                                    'licensor-region'       : 'Bundesländer'
                            ]
                    ]
            ],
    ]
}
