package de.laser.reporting.myInstitution.config

import de.laser.finance.CostItem
import de.laser.reporting.myInstitution.base.BaseConfig

class CostItemXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            key : KEY_COSTITEM,

            base : [
                    meta : [
                            class: CostItem
                    ],
                    source : [
                            'consortia-cost'
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
                            default: [
                                    'Kosten' : [
                                            'costItem-type',
                                            'costItem-costItemStatus',
                                            'costItem-costItemCategory',
                                            'costItem-costItemElement',
                                            'costItem-costItemElementConfiguration',
                                            'costItem-billingCurrency',
                                            'costItem-*'
                                    ]
                            ]
                    ]
            ]
    ]
}
