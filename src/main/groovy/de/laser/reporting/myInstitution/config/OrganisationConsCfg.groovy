package de.laser.reporting.myInstitution.config

import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class OrganisationConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_ORGANISATION
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
                                    'org' : [
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
                                    'org' : [ 'org-orgType' ]
                            ],
                            provider : [
                                    'org' : [ 'org-orgType' ]
                            ],
                            agency : [
                                    'org' : [ 'org-orgType' ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [ // TODO ..
                                    'org-x-identifier' : [
                                             detailsTemplate     : 'organisation',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'base', 'x.identifiers' ]
                                    ],
                                    'org-x-property' : [
                                            detailsTemplate     : 'organisation',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'base', 'x.properties' ]
                                   ]
                                    //'org-x-serverAccess' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
