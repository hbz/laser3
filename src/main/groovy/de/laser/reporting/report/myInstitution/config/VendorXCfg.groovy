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
                    ],
                    filter : [
                            default : [
                            ]
                    ],
                    query : [
                            default : [
                                    vendor : [
                                            'vendor-*' :               [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [:]
                    ]
            ]
    ]
}
