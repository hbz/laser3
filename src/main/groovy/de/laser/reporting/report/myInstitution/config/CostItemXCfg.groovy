package de.laser.reporting.report.myInstitution.config

import de.laser.finance.CostItem
import de.laser.reporting.report.myInstitution.base.BaseConfig

class CostItemXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  CostItem,
                            cfgKey: KEY_COSTITEM
                    ],
                    source : [
                            'consortia-cost'
                    ],
                    fields: [
                            'type'                          : FIELD_TYPE_REFDATA,
                            'costItemStatus'                : FIELD_TYPE_REFDATA,
                            'costItemCategory'              : FIELD_TYPE_REFDATA,
                            'costItemElement'               : FIELD_TYPE_REFDATA,
                            'costItemElementConfiguration'  : FIELD_TYPE_REFDATA,
                            'billingCurrency'               : FIELD_TYPE_REFDATA,
                    ],
                    filter : [
                            default: [
                                    [ 'type', 'costItemCategory', 'costItemStatus' ],
                                    [ 'costItemElement', 'costItemElementConfiguration', 'billingCurrency' ]
                            ]
                    ],
                    query : [
                            default: [
                                    costItem : [
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
