package de.laser.reporting.myInstitution.config

import de.laser.License
import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class LicenseInstCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  License,
                            cfgKey: KEY_LICENSE
                    ],
                    source : [
                            'inst-lic',
                            'inst-lic-consortia',
                            'inst-lic-local'
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
                                             label              : 'Identifikatoren → Vertrag',
                                             detailsTemplate    : 'license',
                                             chartTemplate      : '2axis2values_nonMatches',
                                             chartLabels        : [ 'Verträge', 'Vergebene Identifikatoren' ]
                                     ],
                                     'license-x-property' : [
                                             label              : 'Merkmale (eigene/allgemeine) → Vertrag',
                                             detailsTemplate    : 'license',
                                             chartTemplate      : '2axis2values',
                                             chartLabels        : [ 'Verträge', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                     ],
                                     'license-x-annual' : [
                                             label              : 'Jahresring → Vertrag',
                                             detailsTemplate    : 'license',
                                             chartTemplate      : 'generic',
                                             chartLabels        : []
                                     ]
                            ]
                    ]
            ],

            licensor : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_LICENSE
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
                            default : []
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
