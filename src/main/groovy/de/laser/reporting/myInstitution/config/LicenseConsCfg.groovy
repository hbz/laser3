package de.laser.reporting.myInstitution.config

import de.laser.License
import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class LicenseConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            key : KEY_LICENSE,

            base : [
                    meta : [
                            class: License
                    ],
                    source : [
                            'consortia-lic'
                    ],
                    fields : [
                            'annual'                : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'endDateLimit'          : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'licenseCategory'       : BaseConfig.FIELD_TYPE_REFDATA,
                            //'openEnded'             : FIELD_TYPE_REFDATA,
                            'startDateLimit'        : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'status'                : BaseConfig.FIELD_TYPE_REFDATA,
                            //'type'                  : BaseConfig.FIELD_TYPE_REFDATA
                    ],
                    filter : [
                            default : [
                                    [ 'licenseCategory', 'status' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' /*, 'openEnded' */ ]
                            ]
                    ],
                    query : [
                            default : [
                                    'Vertrag' : [
                                            'license-licenseCategory',
                                            //'license-type',
                                            //'license-openEnded',
                                            'license-status',
                                            'license-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                     'license-x-identifier' : [
                                             label               : 'Vertrag → Identifikatoren',
                                             detailsTemplate     : 'license',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'Verträge', 'Vergebene Identifikatoren' ]
                                     ],
                                    'license-x-property' : [
                                            label               : 'Vertrag → Merkmale (eigene/allgemeine)',
                                            detailsTemplate     : 'license',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'Verträge', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                    ],
                                    'license-x-annual' : [
                                            label               : 'Vertrag → Jahresring',
                                            detailsTemplate     : 'license',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ]
                            ]
                    ]
            ],

            licensor : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'depending-licensor'
                    ],
                    fields : [
                            'country'   : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'   : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE,
                    ],
                    filter : [
                            default: []
                    ],
                    query : [
                            default : [
                                    'Lizenzgeber' : [
                                            'licensor-orgType',
                                            'licensor-country',
                                            'licensor-region',
                                            'licensor-*'
                                    ]
                            ]
                    ]
            ],
    ]
}
