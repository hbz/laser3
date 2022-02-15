package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.reporting.report.myInstitution.base.BaseConfig

class OrganisationInstCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_ORGANISATION
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
                            'country'           : [ FIELD_TYPE_REFDATA, '@' ],
                            'region'            : [ FIELD_TYPE_REFDATA, '@' ],
                            'customerType'      : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'eInvoice'          : [ FIELD_TYPE_PROPERTY, 'x' ],
                            'funderHskType'     : [ FIELD_TYPE_REFDATA, '@' ],
                            'funderType'        : [ FIELD_TYPE_REFDATA, '@' ],
                            'legalInfo'         : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'libraryNetwork'    : [ FIELD_TYPE_REFDATA, '@' ],
                            'libraryType'       : [ FIELD_TYPE_REFDATA, '@' ],
                            'orgType'           : [ FIELD_TYPE_REFDATA_JOINTABLE, 'x' ],
                            //'region'            : [ FIELD_TYPE_REFDATA, '@' ],
                            'subjectGroup'      : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ]
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
                                    org : [ 'org-orgType' ]
                            ],
                            provider : [
                                    org : [ 'org-orgType' ]
                            ],
                            agency : [
                                    org : [ 'org-orgType' ]
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
