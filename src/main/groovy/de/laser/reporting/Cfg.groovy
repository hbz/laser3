package de.laser.reporting

import de.laser.Org
import de.laser.Subscription

class Cfg {

    static String filterPrefix = 'filter:'

    static Map<String, Object> config = [

            filter:  [
                'organisation' : 'Organisationen',
                'subscription' : 'Lizenzen'
            ],

            charts: [
                'bar' : 'Balkendiagramm',
                'pie' : 'Tortendiagramm'
            ],

            // --- filter

            Organisation : [
                    meta : [
                            class: Org
                    ],
                    filter : [
                            'all-orgs'      : 'Alle Organisationen',
                            'all-inst'      : 'Alle Einrichtungen',
                            'all-provider'  : 'Alle Anbieter und Lieferanten',
                            'my-inst'       : 'Meine Einrichtungen',
                            'my-provider'   : 'Meine Anbieter und Lieferanten',

                    ],
                    refdata: [
                            'country', 'region', 'libraryType', 'libraryNetwork', 'funderType', 'funderHskType'
                    ]
            ],

            Subscription : [
                    meta : [
                            class: Subscription
                    ],
                    filter : [
                            'all-sub'   : 'Alle Lizenzen',
                            'my-sub'    : 'Meine Lizenzen',

                    ],
                    properties: [
                            'startDate', 'endDate', 'manualRenewalDate', 'manualCancellationDate'
                    ],
                    refdata: [
                            'form', 'kind', 'status', 'type'
                    ]
            ]
    ]
}
