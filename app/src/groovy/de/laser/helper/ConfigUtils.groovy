package de.laser.helper

import grails.util.Holders
import org.apache.commons.logging.LogFactory

class ConfigUtils {

    static final BASIC_DATA_FILE_NAME           = 'basicDataFileName'
    static final BASIC_DATA_PATH                = 'basicDataPath'

    static final DEPLOY_BACKUP_LOCATION         = 'deployBackupLocation'
    static final DOCUMENT_STORAGE_LOCATION      = 'documentStorageLocation'

    static final LASER_SYSTEM_ID                = 'laserSystemId'

    static final ORG_DUMP_FILE_NAME_PATTERN     = 'orgDumpFileNamePattern'
    static final ORG_DUMP_FILE_EXTENSION        = 'orgDumpFileExtension'

    // -- comfortable --

    static String getBasicDataFileName() {
        getConfig(ConfigUtils.BASIC_DATA_FILE_NAME)
    }
    static String getBasicDataPath() {
        getConfig(ConfigUtils.BASIC_DATA_PATH)
    }
    static String getDeployBackupLocation() {
        getConfig(ConfigUtils.DEPLOY_BACKUP_LOCATION)
    }
    static String getDocumentStorageLocation() {
        getConfig(ConfigUtils.DOCUMENT_STORAGE_LOCATION)
    }
    static String getLaserSystemId() {
        getConfig(ConfigUtils.LASER_SYSTEM_ID)
    }
    static String getOrgDumpFileExtension() {
        getConfig(ConfigUtils.ORG_DUMP_FILE_EXTENSION)
    }
    static String getOrgDumpFileNamePattern() {
        getConfig(ConfigUtils.ORG_DUMP_FILE_NAME_PATTERN)
    }

    // -- raw --

    static void validate() {

        getBasicDataFileName()
        getBasicDataPath()
        getDeployBackupLocation()
        getDocumentStorageLocation()
        getLaserSystemId()
        getOrgDumpFileExtension()
        getOrgDumpFileNamePattern()
    }

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
