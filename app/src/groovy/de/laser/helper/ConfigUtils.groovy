package de.laser.helper

import grails.util.Holders
import org.apache.commons.logging.LogFactory

class ConfigUtils {

    static final DEPLOY_BACKUP_LOCATION     = 'deployBackupLocation'
    static final DOCUMENT_STORAGE_LOCATION  = 'documentStorageLocation'
    static final LASER_SYSTEM_ID            = 'laserSystemId'

    // -- comfortable --

    static String getDeployBackupLocation() {
        getConfig(ConfigUtils.DEPLOY_BACKUP_LOCATION)
    }
    static String getDocumentStorageLocation() {
        getConfig(ConfigUtils.DOCUMENT_STORAGE_LOCATION)
    }
    static String getLaserSystemId() {
        getConfig(ConfigUtils.LASER_SYSTEM_ID)
    }

    // -- check --

    static void check() {
        getDeployBackupLocation()
        getDocumentStorageLocation()
        getLaserSystemId()
    }

    // -- raw --

    static def getConfig(String key) {
        def result

        if (key) {
            ConfigObject cfg = Holders.grailsApplication.config

            key.split('\\.').each { lvl ->
                result = result ? result.get(lvl) : cfg.get(lvl)
            }
            if (result == null) {
                println("WARNING: configuration '${key}' not found")
            }
        }
        result
    }
}
