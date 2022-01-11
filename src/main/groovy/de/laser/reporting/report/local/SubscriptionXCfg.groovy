package de.laser.reporting.report.local

class SubscriptionXCfg {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            cfgKey: 'SubscriptionReport'
                    ],
                    query: [
                            default: [
                                    tipp : [
                                            'tipp-publisherName',
                                            'tipp-seriesName',
                                            'tipp-subjectReference',
                                            'tipp-titleType',
                                            'tipp-medium',
                                            'tipp-ddcs',
                                            'tipp-languages'
                                            //  'tipp-package',
                                            //  'tipp-platform'
                                    ]
                            ]
                    ],

                    timeline: [
                            default: [
                                    timeline : [
                                            'timeline-entitlement' : [
                                                    detailsTemplate : 'timeline/entitlement',
                                                    chartTemplate   : 'timeline/1axis3values',
                                                    chartLabels     : [ 'entitlement.1', 'entitlement.2', 'entitlement.3' ]
                                            ]
                                    ]
                            ]
                    ]
            ]
    ]

    static Map<String, Object> CONFIG_CONS_AT_CONS = [

            base : [
                    meta : [
                            cfgKey: 'SubscriptionReport'
                    ],
                    query: [
                            default: [
                                    member : [
                                            'member-customerType',
                                            'member-orgType',
                                            //'member-legalInfo',
                                            'member-libraryNetwork',
                                            'member-libraryType',
                                            'member-subjectGroup',
                                            'member-country',
                                            'member-region',
                                            'member-eInvoicePortal',
                                            'member-funderHskType',
                                            'member-funderType'
                                    ],
                                    tipp : [
                                            'tipp-publisherName',
                                            'tipp-seriesName',
                                            'tipp-subjectReference',
                                            'tipp-titleType',
                                            'tipp-medium',
                                            'tipp-ddcs',
                                            'tipp-languages'
                                            //  'tipp-package',
                                            //  'tipp-platform'
                                    ]
                            ]
                    ],

                    timeline: [
                            default: [
                                    timeline : [
                                            'timeline-member' : [
                                                    detailsTemplate : 'timeline/organisation',
                                                    chartTemplate   : 'timeline/1axis3values',
                                                    chartLabels     : [ 'member.1', 'member.2', 'member.3' ]
                                            ],
                                            'timeline-cost' : [
                                                    detailsTemplate : 'timeline/cost',
                                                    chartTemplate   : 'timeline/cost',
                                                    chartLabels     : [ 'cost.1', 'cost.2', 'cost.3', 'cost.4' ]
                                            ],
                                            'timeline-entitlement' : [
                                                    detailsTemplate : 'timeline/entitlement',
                                                    chartTemplate   : 'timeline/1axis3values',
                                                    chartLabels     : [ 'entitlement.1', 'entitlement.2', 'entitlement.3' ]
                                            ],
                                            'timeline-annualMember-subscription' : [
                                                    detailsTemplate : 'timeline/subscription',
                                                    chartTemplate   : 'timeline/annualMember',
                                                    chartLabels     : [ 'annualMember-subscription' ]
                                            ],
                                    ]
                            ],
                    ]
            ]
    ]
}
