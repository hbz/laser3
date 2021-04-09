package de.laser.reporting.myInstitution

import de.laser.finance.CostItem
import de.laser.reporting.myInstitution.base.BaseConfig

class CostItemConfig extends BaseConfig {

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
                            'type'                          : BaseConfig.FIELD_TYPE_REFDATA,
                            'costItemStatus'                : BaseConfig.FIELD_TYPE_REFDATA,
                            'costItemCategory'              : BaseConfig.FIELD_TYPE_REFDATA,
                            'costItemElement'               : BaseConfig.FIELD_TYPE_REFDATA,
                            'costItemElementConfiguration'  : BaseConfig.FIELD_TYPE_REFDATA,
                            'billingCurrency'               : BaseConfig.FIELD_TYPE_REFDATA,
                    ],
                    filter : [
                            default: [
                                    [ 'type', 'costItemCategory', 'costItemStatus' ],
                                    [ 'costItemElement', 'costItemElementConfiguration', 'billingCurrency' ]
                            ]
                    ],
                    query : [
                            'Kosten' : [ // TODO ..
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
