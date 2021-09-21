package de.laser.reporting.myInstitution.config

import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class OrganisationConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            key : KEY_ORGANISATION,

            base : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'all-org',
                            'all-inst',
                            'all-provider',
                            'all-agency',
                            'all-providerAndAgency',
                            'my-inst',
                            'my-provider',
                            'my-agency',
                            'my-providerAndAgency'
                    ],
                    fields : [
                            'country'           : BaseConfig.FIELD_TYPE_REFDATA,
                            'region'            : BaseConfig.FIELD_TYPE_REFDATA,
                            'customerType'      : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'eInvoice'          : BaseConfig.FIELD_TYPE_PROPERTY,
                            'funderHskType'     : BaseConfig.FIELD_TYPE_REFDATA,
                            'funderType'        : BaseConfig.FIELD_TYPE_REFDATA,
                            'legalInfo'         : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                            'libraryNetwork'    : BaseConfig.FIELD_TYPE_REFDATA,
                            'libraryType'       : BaseConfig.FIELD_TYPE_REFDATA,
                            'orgType'           : BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE,
                            //'region'            : FIELD_TYPE_REFDATA,
                            'subjectGroup'      : BaseConfig.FIELD_TYPE_CUSTOM_IMPL,
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ] ,
                                    [ 'orgType', 'eInvoice' ],
                                    [ 'customerType', 'legalInfo' ]
                            ],
                            provider : [ // TODO : provider != agency
                                    // all disabled
                            ]
                    ],
                    query : [
                            default : [
                                    'Organisation' : [
                                            'org-orgType',
                                            'org-customerType',
                                            'org-libraryType',
                                            'org-region',
                                            'org-subjectGroup',
                                            'org-libraryNetwork',
                                            'org-funderType',
                                            'org-funderHskType',
                                            'org-*'
                                    ]
                            ],
                            providerAndAgency : [
                                    'Organisation' : [ 'org-orgType' ]
                            ],
                            provider : [
                                    'Organisation' : [ 'org-orgType' ]
                            ],
                            agency : [
                                    'Organisation' : [ 'org-orgType' ]
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                     'org-x-identifier' : [
                                             label               : 'Identifikatoren → Organisation',
                                             detailsTemplate     : 'organisation',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'Organisationen', 'Vergebene Identifikatoren' ]
                                     ],
                                    'org-x-property' : [
                                            label               : 'Merkmale (eigene/allgemeine) → Organisation',
                                            detailsTemplate     : 'organisation',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'Organisationen', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                    ]
                                    //'org-x-serverAccess' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
