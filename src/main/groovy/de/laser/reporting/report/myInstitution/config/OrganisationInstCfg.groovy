package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.reporting.report.myInstitution.base.BaseConfig

class OrganisationInstCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Org,
                            cfgKey: BaseConfig.KEY_ORGANISATION
                    ],
                    source : [
                            'all-consortium',
                            'all-provider',
                            'all-agency',
                            'all-providerAndAgency',
                            'my-consortium',
                            'my-provider',
                            'my-agency',
                            'my-providerAndAgency'
                    ],
                    fields : [
                            'country'           : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'region'            : [type: BaseConfig.FIELD_TYPE_REFDATA, spec: BaseConfig.FIELD_IS_VIRTUAL ],
                            'customerType'      : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'eInvoice'          : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'funderHskType'     : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'funderType'        : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'legalInfo'         : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'libraryNetwork'    : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'libraryType'       : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'orgType'           : [ type: BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE ],
                            'subjectGroup'      : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ]    // TODO custom_impl
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region' ],
                                    [ 'libraryNetwork',  'orgType' ]
                            ],
                            provider : [ // TODO : provider != agency
                                    // all disabled
                            ]
                    ],
                    query : [
                            default : [
                                    org : [
                                           'org-orgType' :          [ 'generic.org.orgType' ],
                                           'org-customerType' :     [ 'generic.org.customerType' ],
                                           'org-libraryType' :      [ 'generic.org.libraryType' ],
                                           'org-region' :           [ 'generic.org.region' ],
                                           'org-subjectGroup' :     [ 'generic.org.subjectGroup' ],
                                           'org-libraryNetwork' :   [ 'generic.org.libraryNetwork' ],
                                           'org-funderType' :       [ 'generic.org.funderType' ],
                                           'org-funderHskType' :    [ 'generic.org.funderHskType' ],
                                           'org-*' :                [ 'generic.all' ]
                                    ]
                            ],
                            providerAndAgency : [
                                    org : [
                                            'org-orgType': [ 'generic.org.orgType' ]
                                    ]
                            ],
                            provider : [
                                    org : [
                                            'org-orgType': [ 'generic.org.orgType' ]
                                    ]
                            ],
                            agency : [
                                    org : [
                                            'org-orgType': [ 'generic.org.orgType' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                     'org-x-identifier' : [
                                             detailsTemplate    : 'organisation',
                                             chartTemplate      : '2axis2values_nonMatches',
                                             chartLabels        : [ 'base', 'x.identifiers' ]
                                     ],
                                     'org-x-property' : [
                                             detailsTemplate    : 'organisation',
                                             chartTemplate      : '2axis3values',
                                             chartLabels        : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                     ]
                                     //'org-x-serverAccess' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
