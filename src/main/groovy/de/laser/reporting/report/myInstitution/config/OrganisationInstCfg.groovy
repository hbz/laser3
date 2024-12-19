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
                            'my-consortium',
                    ],
                    fields : [
                            'country'           : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'region'            : [ type: BaseConfig.FIELD_TYPE_REFDATA, spec: BaseConfig.FIELD_IS_VIRTUAL ],
                            'customerType'      : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'eInvoice'          : [ type: BaseConfig.FIELD_TYPE_PROPERTY ],
                            'funderHskType'     : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'funderType'        : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'legalInfo'         : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ],   // TODO custom_impl
                            'libraryNetwork'    : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'libraryType'       : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'subjectGroup'      : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL ]    // TODO custom_impl
                    ],
                    filter : [
                            default : [
                                    [ 'country', 'region' ],
                                    [ 'libraryNetwork' ]
                            ]
                    ],
                    query : [
                            default : [
                                    org : [
                                           'org-customerType' :     [ 'generic.org.customerType' ],
                                           'org-libraryType' :      [ 'generic.org.libraryType' ],
                                           'org-country' :          [ 'generic.org.country' ],
                                           'org-region' :           [ 'generic.org.region' ],
                                           'org-subjectGroup' :     [ 'generic.org.subjectGroup' ],
                                           'org-libraryNetwork' :   [ 'generic.org.libraryNetwork' ],
                                           'org-funderType' :       [ 'generic.org.funderType' ],
                                           'org-funderHskType' :    [ 'generic.org.funderHskType' ],
                                           'org-*' :                [ 'generic.all' ]
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
