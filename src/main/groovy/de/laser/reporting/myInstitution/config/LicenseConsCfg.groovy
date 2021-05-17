package de.laser.reporting.myInstitution.config

import de.laser.License
import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class LicenseConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: License
                    ],
                    source : [
                            'consortia-lic' : 'Meine Verträge'
                    ],
                    fields : [
                            'annual'                : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'endDate'               : BaseConfig.FIELD_TYPE_PROPERTY,
                            'licenseCategory'       : BaseConfig.FIELD_TYPE_REFDATA,
                            //'openEnded'             : FIELD_TYPE_REFDATA,
                            'startDate'             : BaseConfig.FIELD_TYPE_PROPERTY,
                            'status'                : BaseConfig.FIELD_TYPE_REFDATA,
                            'type'                  : BaseConfig.FIELD_TYPE_REFDATA
                    ],
                    filter : [
                            default : [
                                    [ 'licenseCategory', 'type', 'status', 'annual' ],
                                    [ 'startDate', 'endDate' /*, 'openEnded' */ ]
                            ]
                    ],
                    query : [
                            default : [
                                    'Vertrag' : [ // TODO ..
                                            'license-*'                 : 'Alle',
                                            'license-licenseCategory'   : 'Lizenzkategorie',
                                            'license-type'              : 'Lizenztyp',
                                            //'license-openEnded'         : 'Unbefristet',
                                            'license-status'            : 'Lizenzstatus'
                                    ]
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                    'license-x-property' : [
                                            label               : 'Vertrag → Merkmale (eigene/allgemeine)',
                                            detailsTemplate     : 'license',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'Verträge', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                    ],
                                    'license-x-identifier' : [
                                            label               : 'Vertrag → Identifikatoren',
                                            detailsTemplate     : 'license',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'Verträge', 'Vergebene Identifikatoren' ]
                                    ],
                                    'license-x-annual' : [
                                            label               : 'Vertrag → Jahresring',
                                            detailsTemplate     : 'license',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                            ]
                    ]
            ],

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
                            default: []
                    ],
                    query : [
                            default : [
                                    'Lizenzgeber' : [ // TODO ..
                                            'licensor-*'            : 'Alle',
                                            'licensor-orgType'      : 'Organisationstyp',
                                            'licensor-country'      : 'Länder',
                                            'licensor-region'       : 'Bundesländer'
                                    ]
                            ]
                    ]
            ],
    ]
}
