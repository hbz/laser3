package de.laser.reporting.report.myInstitution.config

import de.laser.License
import de.laser.Org
import de.laser.reporting.report.myInstitution.base.BaseConfig

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
                            'annual'                : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'endDateLimit'          : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'licenseCategory'       : [ FIELD_TYPE_REFDATA, '@' ],
                            //'openEnded'             : [ FIELD_TYPE_REFDATA, '@' ],
                            'propertyKey'           : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'propertyValue'         : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'startDateLimit'        : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'status'                : [ FIELD_TYPE_REFDATA, '@' ]
                            //'type'                  : BaseConfig.FIELD_TYPE_REFDATA
                    ],
                    filter : [
                            default : [
                                    [ 'licenseCategory', 'status' ],
                                    [ 'startDateLimit', 'endDateLimit', 'annual' /*, 'openEnded' */ ],
                                    [ 'propertyKey', 'propertyValue' ]
                            ]
                    ],
                    query : [
                            default : [
                                    license : [
                                            'license-licenseCategory',
                                            //'license-type',
                                            //'license-openEnded',
                                            'license-status',
                                            'license-*'
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                     'license-x-identifier' : [
                                             detailsTemplate     : 'license',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'base', 'x.identifiers' ]
                                     ],
                                    'license-x-property' : [
                                            detailsTemplate     : 'license',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                    ],
                                    'license-x-annual' : [
                                            detailsTemplate     : 'license',
                                            chartTemplate       : 'annual',
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
                            'country'   : [ FIELD_TYPE_REFDATA, '@' ],
                            'region'    : [ FIELD_TYPE_REFDATA, '@' ],
                            'orgType'   : [ FIELD_TYPE_REFDATA_JOINTABLE, 'x' ]
                    ],
                    filter : [
                            default: []
                    ],
                    query : [
                            default : [
                                    licensor : [
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
