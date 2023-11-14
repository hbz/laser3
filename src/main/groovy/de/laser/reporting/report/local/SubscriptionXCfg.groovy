package de.laser.reporting.report.local

import de.laser.reporting.report.myInstitution.base.BaseConfig

class SubscriptionXCfg {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            cfgKey: BaseConfig.KEY_LOCAL_SUBSCRIPTION
                    ],
                    query: [
                            default: [
                                    tipp : [
                                            'tipp-publisherName' :      [ 'generic.tipp.publisherName' ],
                                            'tipp-seriesName' :         [ 'generic.tipp.seriesName' ],
                                            'tipp-subjectReference' :   [ 'generic.tipp.subjectReference' ],
                                            'tipp-titleType' :          [ 'generic.tipp.titleType' ],
                                            'tipp-medium' :             [ 'generic.tipp.medium' ],
                                            'tipp-dateFirstOnline' :    [ 'generic.tipp.dateFirstOnline' ],
                                            'tipp-ddcs' :               [ 'generic.tipp.ddcs' ],
                                            'tipp-languages' :          [ 'generic.tipp.languages' ],
                                            //  'tipp-package' :        [ 'generic.tipp.package' ],
                                            //  'tipp-platform' :       [ 'generic.tipp.platform' ]
                                    ]
                            ]
                    ],

                    timeline: [
                            default: [
//                                    'timeline-cost' : [ // todo
//                                            detailsTemplate : 'timeline/cost',
//                                            chartTemplate   : 'timeline/cost',
//                                            chartLabels     : [ 'cost.1', 'cost.2', 'cost.3', 'cost.4' ]
//                                    ],
                                    'timeline-entitlement' : [
                                            detailsTemplate : 'timeline/entitlement',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'entitlement.1', 'entitlement.2', 'entitlement.3' ]
                                    ],
                                    'timeline-package' : [
                                            detailsTemplate : 'timeline/package',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'package.1', 'package.2', 'package.3' ]
                                    ]
                            ]
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_PARTICIPATION = [

            base : [
                    meta : [
                            cfgKey: BaseConfig.KEY_LOCAL_SUBSCRIPTION
                    ],
                    query: [
                            default: [
                                    tipp : [
                                            'tipp-publisherName' :      [ 'generic.tipp.publisherName' ],
                                            'tipp-seriesName' :         [ 'generic.tipp.seriesName' ],
                                            'tipp-subjectReference' :   [ 'generic.tipp.subjectReference' ],
                                            'tipp-titleType' :          [ 'generic.tipp.titleType' ],
                                            'tipp-medium' :             [ 'generic.tipp.medium' ],
                                            'tipp-dateFirstOnline' :    [ 'generic.tipp.dateFirstOnline' ],
                                            'tipp-ddcs' :               [ 'generic.tipp.ddcs' ],
                                            'tipp-languages' :          [ 'generic.tipp.languages' ],
                                            //  'tipp-package' :        [ 'generic.tipp.package' ],
                                            //  'tipp-platform' :       [ 'generic.tipp.platform' ]
                                    ]
                            ]
                    ],

                    timeline: [
                            default: [
                                    'timeline-participant-cost' : [
                                            detailsTemplate : 'timeline/cost',
                                            chartTemplate   : 'timeline/cost',
                                            chartLabels     : [ 'cost.1', 'cost.2', 'cost.3', 'cost.4' ]
                                    ],
                                    'timeline-entitlement' : [
                                            detailsTemplate : 'timeline/entitlement',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'entitlement.1', 'entitlement.2', 'entitlement.3' ]
                                    ],
                                    'timeline-package' : [
                                            detailsTemplate : 'timeline/package',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'package.1', 'package.2', 'package.3' ]
                                    ]
                            ]
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_CONS_AT_CONS = [

            base : [
                    meta : [
                            cfgKey: BaseConfig.KEY_LOCAL_SUBSCRIPTION
                    ],
                    query: [
                            default: [
                                    member : [
                                            'member-customerType' :     [ 'generic.org.customerType' ],
                                            'member-orgType' :          [ 'generic.org.orgType' ],
                                            //'member-legalInfo' : [ '@' ],
                                            'member-libraryNetwork' :   [ 'generic.org.libraryNetwork' ],
                                            'member-libraryType' :      [ 'generic.org.libraryType' ],
                                            'member-subjectGroup' :     [ 'generic.org.subjectGroup' ],
                                            'member-country' :          [ 'generic.org.country' ],
                                            'member-region' :           [ 'generic.org.region' ],
                                            'member-eInvoicePortal' :   [ '@' ],
                                            'member-funderHskType' :    [ 'generic.org.funderHskType' ],
                                            'member-funderType' :       [ 'generic.org.funderType' ]
                                    ],
                                    tipp : [
                                            'tipp-publisherName' :      [ 'generic.tipp.publisherName' ],
                                            'tipp-seriesName' :         [ 'generic.tipp.seriesName' ],
                                            'tipp-subjectReference' :   [ 'generic.tipp.subjectReference' ],
                                            'tipp-titleType' :          [ 'generic.tipp.titleType' ],
                                            'tipp-medium' :             [ 'generic.tipp.medium' ],
                                            'tipp-dateFirstOnline' :    [ 'generic.tipp.dateFirstOnline' ],
                                            'tipp-ddcs' :               [ 'generic.tipp.ddcs' ],
                                            'tipp-languages' :          [ 'generic.tipp.languages' ],
                                            //  'tipp-package' :        [ 'generic.tipp.package' ],
                                            //  'tipp-platform' :       [ 'generic.tipp.platform' ]
                                    ]
                            ]
                    ],

                    timeline: [
                            default: [
                                    'timeline-member' : [
                                            detailsTemplate : 'timeline/organisation',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'member.1', 'member.2', 'member.3' ]
                                    ],
                                    'timeline-member-cost' : [
                                            detailsTemplate : 'timeline/cost',
                                            chartTemplate   : 'timeline/cost',
                                            chartLabels     : [ 'cost.1', 'cost.2', 'cost.3', 'cost.4' ]
                                    ],
                                    'timeline-entitlement' : [
                                            detailsTemplate : 'timeline/entitlement',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'entitlement.1', 'entitlement.2', 'entitlement.3' ]
                                    ],
                                    'timeline-package' : [
                                            detailsTemplate : 'timeline/package',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'package.1', 'package.2', 'package.3' ]
                                    ],
                                    'timeline-annualMember-subscription' : [
                                            detailsTemplate : 'timeline/subscription',
                                            chartTemplate   : 'timeline/annualMember',
                                            chartLabels     : [ 'annualMember-subscription' ]
                                    ],
                            ],
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_CONS_AT_SUBSCR = [

            base : [
                    meta : [
                            cfgKey: BaseConfig.KEY_LOCAL_SUBSCRIPTION
                    ],
                    query: [
                            default: [
                                    tipp : [
                                            'tipp-publisherName' :      [ 'generic.tipp.publisherName' ],
                                            'tipp-seriesName' :         [ 'generic.tipp.seriesName' ],
                                            'tipp-subjectReference' :   [ 'generic.tipp.subjectReference' ],
                                            'tipp-titleType' :          [ 'generic.tipp.titleType' ],
                                            'tipp-medium' :             [ 'generic.tipp.medium' ],
                                            'tipp-dateFirstOnline' :    [ 'generic.tipp.dateFirstOnline' ],
                                            'tipp-ddcs' :               [ 'generic.tipp.ddcs' ],
                                            'tipp-languages' :          [ 'generic.tipp.languages' ],
                                            //  'tipp-package' :        [ 'generic.tipp.package' ],
                                            //  'tipp-platform' :       [ 'generic.tipp.platform' ]
                                    ]
                            ]
                    ],

                    timeline: [
                            default: [
                                    'timeline-member-cost' : [
                                            detailsTemplate : 'timeline/cost',
                                            chartTemplate   : 'timeline/cost',
                                            chartLabels     : [ 'cost.1', 'cost.2', 'cost.3', 'cost.4' ]
                                    ],
                                    'timeline-entitlement' : [
                                            detailsTemplate : 'timeline/entitlement',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'entitlement.1', 'entitlement.2', 'entitlement.3' ]
                                    ],
                                    'timeline-package' : [
                                            detailsTemplate : 'timeline/package',
                                            chartTemplate   : 'timeline/1axis3values',
                                            chartLabels     : [ 'package.1', 'package.2', 'package.3' ]
                                    ]
                            ],
                    ]
            ]
    ]
}
