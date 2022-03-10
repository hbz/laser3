package de.laser.reporting.report.myInstitution.config

import de.laser.License
import de.laser.Org
import de.laser.reporting.report.myInstitution.base.BaseConfig

class LicenseConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  License,
                            cfgKey: BaseConfig.KEY_LICENSE
                    ],
                    source : [
                            'consortia-lic'
                    ],
                    fields : [
                            'annual'                : [type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, spec: BaseConfig.FIELD_IS_MULTIPLE ],   // TODO custom_impl
                            'endDateLimit'          : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'licenseCategory'       : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            //'openEnded'             : [ type: FIELD_TYPE_REFDATA ],
                            'propertyKey'           : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'propertyValue'         : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'startDateLimit'        : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'status'                : [ type: BaseConfig.FIELD_TYPE_REFDATA ]
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
                                            'license-licenseCategory' : [ '@' ],
                                            //'license-type' :          [ '@' ],
                                            //'license-openEnded' :     [ '@' ],
                                            'license-status' :          [ '@' ],
                                            'license-*' :               [ 'generic.all' ]
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
                            cfgKey: BaseConfig.KEY_LICENSE
                    ],
                    source : [
                            'depending-licensor'
                    ],
                    fields : [
                            'country'   : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'region'    : [type: BaseConfig.FIELD_TYPE_REFDATA, spec: BaseConfig.FIELD_IS_VIRTUAL ],
                            'orgType'   : [ type: BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE ]
                    ],
                    filter : [
                            default: []
                    ],
                    query : [
                            default : [
                                    licensor : [
                                            'licensor-orgType' :    [ 'generic.org.orgType' ],
                                            'licensor-country' :    [ 'generic.org.country' ],
                                            'licensor-region' :     [ 'generic.org.region' ],
                                            'licensor-*' :          [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],
    ]
}
