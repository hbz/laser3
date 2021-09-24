package de.laser.reporting.myInstitution.config

import de.laser.License
import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class LicenseConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  License,
                            cfgKey: KEY_LICENSE
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
                                    'license' : [
                                            'license-licenseCategory',
                                            //'license-type',
                                            //'license-openEnded',
                                            'license-status',
                                            'license-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [
                                     'license-x-identifier' : [
                                             detailsTemplate     : 'license',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'base', 'x.identifiers' ]
                                     ],
                                    'license-x-property' : [
                                            detailsTemplate     : 'license',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'base', 'x.properties' ]
                                    ],
                                    'license-x-annual' : [
                                            detailsTemplate     : 'license',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
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
                            default: []
                    ],
                    query : [
                            default : [
                                    'licensor' : [
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
