package de.laser.reporting.myInstitution

import de.laser.reporting.myInstitution.base.BaseConfig

class SubscriptionConfig extends BaseConfig {

    static Map<String, Object> getCurrentConfig() {
        getCurrentConfig( KEY_SUBSCRIPTION )
    }
}
