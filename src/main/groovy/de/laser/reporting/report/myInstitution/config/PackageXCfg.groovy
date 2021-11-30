package de.laser.reporting.report.myInstitution.config

import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig

class PackageXCfg extends BaseConfig {

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
            'package-x-nationalRange'   : [:],
            'package-x-regionalRange'   : [:],
            'package-x-ddc'             : [                 export: true,   mapping: 'ddcs',            label: 'package.ddc.label', rdc: RDConstants.DDC]
    ]

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
                            'breakable'         : FIELD_TYPE_ELASTICSEARCH,
                            'consistent'        : FIELD_TYPE_ELASTICSEARCH,
                            'contentType'       : FIELD_TYPE_REFDATA,
                            'file'              : FIELD_TYPE_REFDATA,
                            'openAccess'        : FIELD_TYPE_ELASTICSEARCH,
                            'packageStatus'     : FIELD_TYPE_REFDATA,
                            'paymentType'       : FIELD_TYPE_ELASTICSEARCH,
                            'scope'             : FIELD_TYPE_ELASTICSEARCH
                    ],
                    filter : [
                            default: [
                                    [ 'contentType', 'scope', 'packageStatus' ],
                                    [ 'paymentType', 'breakable', 'consistent' ],
                                    [ 'openAccess', 'file' ]
                            ]
                    ],
                    query : [
                            default: [
                                    'package' : [
                                            'package-contentType',
                                            'package-packageStatus',
                                            'package-file',
                                            'package-breakable',        // ES
                                            'package-paymentType',      // ES
                                            'package-openAccess',       // ES
                                            'package-consistent',       // ES
                                            'package-scope',            // ES
                                            'package-*'
                                    ]
                            ]
                    ],
                    query2 : [
                            'distribution' : [
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
            ]
    ]
}
