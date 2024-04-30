package de.laser.reporting.report.myInstitution.config

import de.laser.Vendor
import de.laser.reporting.report.myInstitution.base.BaseConfig

class VendorXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Vendor,
                            cfgKey: BaseConfig.KEY_VENDOR
                    ],
                    source : [
                            'all-vendor',
                            'my-vendor',
                    ],
                    fields : [
                            'status' : [ type: BaseConfig.FIELD_TYPE_REFDATA ]
                    ],
                    filter : [
                            default : [
                                    [ 'status' ],
                            ],
                            my : [
                                    [ 'status' ],
                            ]
                    ],
                    query : [
                            default : [
                                    vendor : [
                                            'vendor-status' :           [ 'generic.vendor.status' ],
                                            'vendor-*' :                [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [:]
                    ]
            ]
    ]
}
