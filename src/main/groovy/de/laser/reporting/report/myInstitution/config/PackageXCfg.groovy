package de.laser.reporting.report.myInstitution.config

import de.laser.Org
import de.laser.Platform
import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PackageXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  de.laser.Package,
                            cfgKey: KEY_PACKAGE
                    ],
                    source : [
                            'all-pkg',
                            'my-pkg'
                    ],
                    fields: [
                            'name'                      : [ type: FIELD_TYPE_PROPERTY /* blind */ ],
                            'sortname'                  : [ type: FIELD_TYPE_PROPERTY /* blind */ ],
                            'breakable'                 : [ type: FIELD_TYPE_ELASTICSEARCH ],
                                //'consistent'        : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'contentType'               : [ type: FIELD_TYPE_REFDATA ],
                            'file'                      : [ type: FIELD_TYPE_REFDATA ],
                            'openAccess'                : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'packageStatus'             : [ type: FIELD_TYPE_REFDATA ],
                            'subscriptionStatus'        : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_SUBSCRIPTION_STATUS ],
                            'paymentType'               : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'nominalPlatform'           : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PACKAGE_PLATFORM, spec: FIELD_IS_MULTIPLE ],
                            'orProvider'                : [ type: FIELD_TYPE_CUSTOM_IMPL, customImplRdv: CI_GENERIC_PACKAGE_OR_PROVIDER, spec: FIELD_IS_MULTIPLE ],
                            'scope'                     : [ type: FIELD_TYPE_ELASTICSEARCH ]
                    ],
                    filter : [
                            default: [
                                    [ 'contentType', 'packageStatus' ],
                                    [ 'file', 'orProvider', 'nominalPlatform' ],
                                    [ 'breakable', 'scope' ],
                                    [ 'paymentType', 'openAccess']
                            ],
                            my: [
                                    [ 'contentType',  'subscriptionStatus', 'packageStatus' ],
                                    [ 'file', 'orProvider', 'nominalPlatform' ],
                                    [ 'breakable', 'scope' ],
                                    [ 'paymentType', 'openAccess']
                            ]
                    ],
                    query : [
                            default: [
                                    package : [
                                            'package-contentType' :     [ '@' ],
                                            'package-packageStatus' :   [ '@' ],
                                            'package-file' :            [ '@' ],
                                            'package-breakable' :       [ '@' ],    // ES
                                            'package-paymentType' :     [ '@' ],    // ES
                                            'package-openAccess' :      [ '@' ],    // ES
                                            'package-consistent' :      [ '@' ],    // ES
                                            'package-scope' :           [ '@' ],    // ES
                                            'package-*' :               [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [
                                    /* 'package-x-identifier' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'base', 'x.identifiers' ]
                                    ], */
                                    'package-x-id' : [ // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-provider' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : '2axis2values_nonMatches',
                                            chartLabels         : [ 'x.providers.1', 'x.providers.2' ]
                                    ],
                                    'package-x-platform' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-platformProvider' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-curatoryGroup' : [ // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-ddc' : [  // ES
                                             detailsTemplate     : 'package',
                                             chartTemplate       : 'generic',
                                             chartLabels         : []
                                    ],
                                    'package-x-language' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-nationalRange' : [   // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-regionalRange' : [   // ES
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Org,
                            cfgKey: KEY_PACKAGE
                    ],
                    source : [
                            'filter-restricting-provider'
                    ],
                    fields : [ ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    provider : [
                                            'provider-orgType' : [ 'generic.org.orgType' ],
                                            'provider-*' :       [ 'generic.all' ],
                                    ]
                            ]
                    ]
            ],

            platform : [
                    meta : [
                            class:  Platform,
                            cfgKey: KEY_PACKAGE
                    ],
                    source : [
                            'filter-restricting-platform'
                    ],
                    fields : [ ],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    platform : [
                                            'platform-x-org' :              [ '@' ],    // KEY_PLATFORM -> distribution
                                            'platform-serviceProvider' :    [ '@' ],
                                            'platform-softwareProvider' :   [ '@' ],
                                            'platform-*' :                  [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ]
    ]

    static Map<String, Map> ES_DATA = [

            'package-altname'           : [                 export: true,   label: 'package.show.altname'],

            'package-breakable'         : [filter: true,    export: true,   label: 'package.breakable',          rdc: RDConstants.PACKAGE_BREAKABLE],
            'package-consistent'        : [filter: true,    export: true,   label: 'package.consistent',         rdc: RDConstants.PACKAGE_CONSISTENT],
            'package-description'       : [                 export: true,   label: 'package.description.label'],
            'package-descriptionURL'    : [                 export: true,   label: 'package.descriptionURL.label'],
            'package-openAccess'        : [filter: true,    export: true,   label: 'package.openAccess.label',   rdc: RDConstants.LICENSE_OA_TYPE],
            'package-paymentType'       : [filter: true,    export: true,   label: 'package.paymentType.label',  rdc: RDConstants.PAYMENT_TYPE],
            'package-scope'             : [filter: true,    export: true,   label: 'package.scope.label',        rdc: RDConstants.PACKAGE_SCOPE],

            'package-x-curatoryGroup'   : [                 export: true,   mapping: 'curatoryGroups',  label: 'package.curatoryGroup.label'],
            'package-x-id'              : [                 export: true,   mapping: 'identifiers',     label: 'identifier.label'],
            'package-x-nationalRange'   : [                                 mapping: 'nationalRanges',  label: 'package.nationalRange.label'],
            'package-x-regionalRange'   : [                                 mapping: 'regionalRanges',  label: 'package.regionalRange.label'],
            'package-x-language'        : [                 export: true,   mapping: 'languages',       label: 'package.language.label', rdc: RDConstants.LANGUAGE],
            'package-x-ddc'             : [                 export: true,   mapping: 'ddcs',            label: 'package.ddc.label', rdc: RDConstants.DDC]
    ]

    static Map<String, Map> ES_DT_CONFIG = [

            'name'                      : [ dtc: true  ],
            'sortname'                  : [ dtc: false ],
            'package-altname'           : [ dtc: false, es: true,                export: true, label: 'package.show.altname' ],
            'package-x-id'              : [ dtc: false, es: true,                export: true, label: 'identifier.label',              mapping: 'identifiers' ],
            'contentType'               : [ dtc: false ],

            'packageStatus'             : [ dtc: false ],
            'orProvider'                : [ dtc: true  ],
            'nominalPlatform'           : [ dtc: true  ],
            'file'                      : [ dtc: false ],
            '_+_currentTitles'          : [ dtc: true  ],    // virtual

            'package-breakable'         : [ dtc: false, es: true, filter: true, export: true, label: 'package.breakable',             rdc: RDConstants.PACKAGE_BREAKABLE ],
            'package-paymentType'       : [ dtc: false, es: true, filter: true, export: true, label: 'package.paymentType.label',     rdc: RDConstants.PAYMENT_TYPE ],
            'package-openAccess'        : [ dtc: false, es: true, filter: true, export: true, label: 'package.openAccess.label',      rdc: RDConstants.LICENSE_OA_TYPE ],
            'package-consistent'        : [ dtc: false, es: true, filter: true, export: true, label: 'package.consistent',            rdc: RDConstants.PACKAGE_CONSISTENT ],
            'package-scope'             : [ dtc: false, es: true, filter: true, export: true, label: 'package.scope.label',           rdc: RDConstants.PACKAGE_SCOPE ],

            'package-x-ddc'             : [ dtc: false, es: true,               export: true, label: 'package.ddc.label',             mapping: 'ddcs',        rdc: RDConstants.DDC ],
            'package-x-nationalRange'   : [ dtc: false, es: true,                             label: 'package.nationalRange.label',   mapping: 'nationalRanges' ],
            'package-x-regionalRange'   : [ dtc: false, es: true,                             label: 'package.regionalRange.label',   mapping: 'regionalRanges' ],
            'package-x-language'        : [ dtc: false, es: true,               export: true, label: 'package.language.label',        mapping: 'languages',   rdc: RDConstants.LANGUAGE ],
            'package-description'       : [ dtc: false, es: true,               export: true, label: 'package.description.label' ],

            'package-descriptionURL'    : [ dtc: false, es: true,               export: true, label: 'package.descriptionURL.label' ],
            'package-x-curatoryGroup'   : [ dtc: false, es: true,               export: true, label: 'package.curatoryGroup.label',   mapping: 'curatoryGroups' ],
            '_+_lastUpdated'            : [ dtc: true  ],    // virtual
            '_+_wekb'                   : [ dtc: true  ],    // virtual
    ]
}
