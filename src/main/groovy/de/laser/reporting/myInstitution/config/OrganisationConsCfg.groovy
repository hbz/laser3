package de.laser.reporting.myInstitution.config

import de.laser.Org
import de.laser.reporting.myInstitution.base.BaseConfig

class OrganisationConsCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: Org
                    ],
                    source : [
                            'all-org'               : 'Alle Organisationen',
                            'all-inst'              : 'Alle Einrichtungen',
                            'all-provider'          : 'Alle Anbieter',
                            'all-agency'            : 'Alle Lieferanten',
                            'all-providerAndAgency' : 'Alle Anbieter und Lieferanten',
                            'my-inst'               : 'Meine Einrichtungen',
                            'my-provider'           : 'Meine Anbieter',
                            'my-agency'             : 'Meine Lieferanten',
                            'my-providerAndAgency'  : 'Meine Anbieter und Lieferanten'
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
                                    'Organisation' : [ // TODO ..
                                            'org-orgType'           : 'Organisationstyp',
                                            'org-customerType'      : 'Kundentyp',
                                            'org-libraryType'       : 'Bibliothekstyp',
                                            'org-region'            : 'Bundesländer',
                                            'org-subjectGroup'      : 'Fächergruppen',
                                            'org-libraryNetwork'    : 'Verbundzugehörigkeit',
                                            'org-funderType'        : 'Unterhaltsträger',
                                            'org-funderHskType'     : 'Trägerschaft',
                                            'org-*'                 : 'Alle'
                                    ]
                            ],
                            providerAndAgency : [
                                    'Organisation' : [
                                            'org-orgType' : 'Organisationstyp'
                                    ]
                            ],
                            provider : [
                                    'Organisation' : [
                                            'org-orgType' : 'Organisationstyp'
                                    ]
                            ],
                            agency : [
                                    'Organisation' : [
                                            'org-orgType' : 'Organisationstyp'
                                    ]
                            ]
                    ],
                    query2 : [
                            'Verteilung' : [ // TODO ..
                                    'org-x-property' : [
                                            label               : 'Organisation → Merkmale (eigene/allgemeine)',
                                            detailsTemplate     : 'organisation',
                                            chartTemplate       : '2axis2values',
                                            chartLabels         : [ 'Organisationen', 'Vergebene Merkmale (eigene/allgemeine)' ]
                                            ],
                                    'org-x-identifier' : [
                                            label               : 'Organisation → Identifikatoren',
                                            detailsTemplate     : 'organisation',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'Organisationen', 'Vergebene Identifikatoren' ]
                                    ]
                                    //'org-x-serverAccess' : 'Organisation nach Datenweitergabe',
                            ]
                    ]
            ]
    ]
}
