package de.laser.reporting.report.myInstitution.config

import de.laser.Provider
import de.laser.reporting.report.myInstitution.base.BaseConfig

class ProviderXCfg extends BaseConfig {

    static Map<String, Object> CONFIG = [

            base : [
                    meta : [
                            class:  Provider, // TODO
                            cfgKey: BaseConfig.KEY_PROVIDER
                    ],
                    source : [
                            'all-provider',
                            'my-provider',
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
                                    provider : [
                                            'provider-status' :         [ 'generic.provider.status' ],
                                            'provider-*' :              [ 'generic.all' ]
                                    ]
                            ]
                    ],
                    distribution : [
                            default : [:]
                    ]
            ]
    ]
}
