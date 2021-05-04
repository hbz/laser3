package de.laser.reporting.myInstitution

import de.laser.reporting.myInstitution.base.BaseConfig

class LicenseConfig extends BaseConfig {

    static Map<String, Object> getCurrentConfig() {
        getCurrentConfig( KEY_LICENSE )
    }
}
