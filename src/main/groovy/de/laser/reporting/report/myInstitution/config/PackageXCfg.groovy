package de.laser.reporting.report.myInstitution.config

import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import de.laser.storage.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PackageXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  de.laser.wekb.Package,
                            cfgKey: BaseConfig.KEY_PACKAGE
                    ],
                    source : [
                            'all-pkg',
                            'my-pkg'
                    ],
                    fields: [
                            'name'                      : [ type: BaseConfig.FIELD_TYPE_PROPERTY /* blind */ ],
                            'sortname'                  : [ type: BaseConfig.FIELD_TYPE_PROPERTY /* blind */ ],
                            'breakable'                 : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            //'consistent'        : [ type: FIELD_TYPE_ELASTICSEARCH ],
                            'contentType'               : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'file'                      : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'openAccess'                : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'packageStatus'             : [ type: BaseConfig.FIELD_TYPE_REFDATA ],
                            'subscriptionStatus'        : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_SUBSCRIPTION_STATUS, spec: BaseConfig.FIELD_IS_MULTIPLE ],
                            'paymentType'               : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ],
                            'nominalPlatform'           : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_PLATFORM, spec: BaseConfig.FIELD_IS_MULTIPLE ],
                            'provider'                  : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_PROVIDER, spec: BaseConfig.FIELD_IS_MULTIPLE ],
                            'vendor'                    : [ type: BaseConfig.FIELD_TYPE_CUSTOM_IMPL, customImpl: BaseConfig.CI_GENERIC_PACKAGE_VENDOR, spec: BaseConfig.FIELD_IS_MULTIPLE ],
                            'scope'                     : [ type: BaseConfig.FIELD_TYPE_ELASTICSEARCH ]
                    ],
                    filter : [
                            default: [
                                    [ 'contentType', 'file', 'packageStatus' ],
                                    [  'nominalPlatform', 'provider', 'vendor' ],
                                    [ 'breakable', 'scope' ],
                                    [ 'paymentType', 'openAccess' ]
                            ],
                            my: [
                                    [ 'contentType',  'file', 'subscriptionStatus', 'packageStatus' ],
                                    [ 'nominalPlatform', 'provider', 'vendor' ],
                                    [ 'breakable', 'scope' ],
                                    [ 'paymentType', 'openAccess' ]
                            ]
                    ],
                    query : [
                            default: [
                                    package : [
                                            //'package-platform',    // TODO - moved to distribution !
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
                                    'package-x-platform' : [    // TODO - moved from query !
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
                                    'package-x-provider' : [
                                            detailsTemplate     : 'package',
                                            chartTemplate       : 'generic',
                                            chartLabels         : []
                                    ],
//                                    'package-x-provider' : [
//                                            detailsTemplate     : 'package',
//                                            chartTemplate       : '2axis2values_nonMatches',
//                                            chartLabels         : [ 'x.providers.1', 'x.providers.2' ]
//                                    ],
                                    'package-x-vendor' : [
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
                                    'package-x-archivingAgency' : [  // ES
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

            platform : [
                    meta : [
                            class:  Platform,
                            cfgKey: BaseConfig.KEY_PACKAGE
                    ],
                    source : [
                            'filter-subset-platform'
                    ],
                    fields : [],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : [
                                    platform : [
                                            'platform-serviceProvider' :    [ '@' ],
                                            'platform-softwareProvider' :   [ '@' ],
//                                            'platform-org' :                [ '@' ],    // KEY_PLATFORM -> distribution
                                            'platform-primaryUrl' :         [ '@' ],    // KEY_PLATFORM -> distribution
                                            'platform-status' :             [ 'generic.plt.status' ],
                                            'platform-*' :                  [ 'generic.all' ]
                                    ]
                            ]
                    ]
            ],

            provider : [
                    meta : [
                            class:  Provider,
                            cfgKey: BaseConfig.KEY_PACKAGE
                    ],
                    source : [
                            'filter-subset-provider'
                    ],
                    fields : [],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : BaseConfig.GENERIC_PROVIDER_QUERY_DEFAULT
                    ]
            ],

            vendor : [
                    meta : [
                            class:  Vendor,
                            cfgKey: BaseConfig.KEY_PACKAGE
                    ],
                    source : [
                            'filter-subset-vendor'
                    ],
                    fields : [],
                    filter : [
                            default : []
                    ],
                    query : [
                            default : BaseConfig.GENERIC_VENDOR_QUERY_DEFAULT
                    ]
            ]
    ]

    static Map<String, Map> CONFIG_DTC_ES = [ // dynamic table config

            'name'                      : [ dtc: true  ],
            'sortname'                  : [ dtc: false ],
            'package-altname'           : [ dtc: false, es: true,                export: true, label: 'package.show.altname' ],
            'package-x-id'              : [ dtc: false, es: true,                export: true, label: 'identifier.label',              mapping: 'identifiers' ],
            'contentType'               : [ dtc: false ],

            'packageStatus'             : [ dtc: false ],
            'provider'                  : [ dtc: true ],
            'vendor'                    : [ dtc: true ],
            'nominalPlatform'           : [ dtc: true  ],
            'file'                      : [ dtc: false ],
            '_dtField_currentTitles'    : [ dtc: true  ],    // virtual

            'package-breakable'         : [ dtc: false, es: true, filter: true, export: true, label: 'package.breakable',             rdc: RDConstants.PACKAGE_BREAKABLE ],
            'package-paymentType'       : [ dtc: false, es: true, filter: true, export: true, label: 'package.paymentType.label',     rdc: RDConstants.PAYMENT_TYPE ],
            'package-openAccess'        : [ dtc: false, es: true, filter: true, export: true, label: 'package.openAccess.label',      rdc: RDConstants.LICENSE_OA_TYPE ],
            'package-consistent'        : [ dtc: false, es: true, filter: true, export: true, label: 'package.consistent',            rdc: RDConstants.PACKAGE_CONSISTENT ],
            'package-scope'             : [ dtc: false, es: true, filter: true, export: true, label: 'package.scope.label',           rdc: RDConstants.PACKAGE_SCOPE ],

            'package-x-ddc'             : [ dtc: false, es: true,               export: true, label: 'package.ddc.label',             mapping: 'ddcs',        rdc: RDConstants.DDC ],
            'package-x-nationalRange'   : [ dtc: false, es: true,                             label: 'package.nationalRange.label',   mapping: 'nationalRanges' ],
            'package-x-regionalRange'   : [ dtc: false, es: true,                             label: 'package.regionalRange.label',   mapping: 'regionalRanges' ],
            'package-x-language'        : [ dtc: false, es: true,               export: true, label: 'package.language.label',        mapping: 'languages',   rdc: RDConstants.LANGUAGE ],
            'package-description'       : [ dtc: false, es: true,               export: true, label: 'default.description.label' ],

            'package-descriptionURL'    : [ dtc: false, es: true,               export: true, label: 'default.url.label' ],
            'package-x-curatoryGroup'   : [ dtc: false, es: true,               export: true, label: 'package.curatoryGroup.label',   mapping: 'curatoryGroups' ],
            'package-x-archivingAgency' : [ dtc: false, es: true,               export: true, label: 'package.archivingAgency.label', mapping: 'packageArchivingAgencies' ],
            '_dtField_lastUpdated'      : [ dtc: true  ],    // virtual
            '_dtField_wekb'             : [ dtc: true  ],    // virtual
    ]
}
