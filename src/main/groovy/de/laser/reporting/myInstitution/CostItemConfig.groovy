package de.laser.reporting.myInstitution

import de.laser.finance.CostItem

class CostItemConfig extends GenericConfig {

    static String KEY = 'costItem'

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class: CostItem
                    ],
                    source : [
                            'consortia-cost' : 'Meine Teilnehmerkosten - aktive Lizenzen'
                    ],
                    fields: [
                            'type'                          : GenericConfig.FIELD_TYPE_REFDATA,
                            'costItemStatus'                : GenericConfig.FIELD_TYPE_REFDATA,
                            'costItemCategory'              : GenericConfig.FIELD_TYPE_REFDATA,
                            'costItemElement'               : GenericConfig.FIELD_TYPE_REFDATA,
                            'costItemElementConfiguration'  : GenericConfig.FIELD_TYPE_REFDATA,
                            'billingCurrency'               : GenericConfig.FIELD_TYPE_REFDATA,
                    ],
                    filter : [
                            default: [
                                    [ 'type', 'costItemCategory', 'costItemStatus' ],
                                    [ 'costItemElement', 'costItemElementConfiguration', 'billingCurrency' ]
                            ]
                    ],
                    query : [
                            'Kosten' : [
                                    'costItem-type'                         : 'type',
                                    'costItem-costItemStatus'               : 'costItemStatus',
                                    'costItem-costItemCategory'             : 'costItemCategory',
                                    'costItem-costItemElement'              : 'costItemElement',
                                    'costItem-costItemElementConfiguration' : 'costItemElementConfiguration',
                                    'costItem-billingCurrency'              : 'billingCurrency',
                            ]
                    ]
            ]
    ]
}
