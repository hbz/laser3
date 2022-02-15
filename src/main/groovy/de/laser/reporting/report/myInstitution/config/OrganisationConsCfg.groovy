package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.reporting.report.myInstitution.base.BaseConfig

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
                            'country'           : [ FIELD_TYPE_REFDATA, '@' ],
                            'region'            : [ FIELD_TYPE_REFDATA, '@' ],
                            'customerType'      : [ FIELD_TYPE_CUSTOM_IMPL, 'org.customerType.label' ],
                            'eInvoice'          : [ FIELD_TYPE_PROPERTY, 'x' ],
                            'funderHskType'     : [ FIELD_TYPE_REFDATA, '@' ],
                            'funderType'        : [ FIELD_TYPE_REFDATA, '@' ],
                            'legalInfo'         : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'libraryNetwork'    : [ FIELD_TYPE_REFDATA, '@' ],
                            'libraryType'       : [ FIELD_TYPE_REFDATA, '@' ],
                            'orgType'           : [ FIELD_TYPE_REFDATA_JOINTABLE, 'x' ],
                            'propertyKey'       : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'propertyValue'     : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ],
                            'subjectGroup'      : [ FIELD_TYPE_CUSTOM_IMPL, 'x' ]
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region', 'subjectGroup', 'libraryType' ],
                                    [ 'libraryNetwork', 'funderType', 'funderHskType' ] ,
                                    [ 'orgType', 'eInvoice' ],
                                    [ 'customerType', 'legalInfo' ],
                                    [ 'propertyKey', 'propertyValue' ]
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
                                             detailsTemplate     : 'organisation',
                                             chartTemplate       : '2axis2values_nonMatches',
                                             chartLabels         : [ 'base', 'x.identifiers' ]
                                    ],
                                    'org-x-property' : [
                                            detailsTemplate     : 'organisation',
                                            chartTemplate       : '2axis3values',
                                            chartLabels         : [ 'base', 'x.properties.2', 'x.properties.3' ]
                                   ]
                                    //'org-x-serverAccess' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
