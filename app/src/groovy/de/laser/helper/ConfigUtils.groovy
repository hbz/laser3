package de.laser.helper

import grails.util.Holders

class ConfigUtils {

    static final ACTIVATE_TEST_JOB                          = 'activateTestJob'
    static final APP_DEFAULT_PREFS                          = 'appDefaultPrefs'

    static final BASIC_DATA_FILE_NAME                       = 'basicDataFileName'
    static final BASIC_DATA_PATH                            = 'basicDataPath'

    static final DEPLOY_BACKUP_LOCATION                     = 'deployBackupLocation'
    static final DOCUMENT_STORAGE_LOCATION                  = 'documentStorageLocation'

    static final FINANCIALS_CURRENCY                        = 'financials.currency'

    static final GLOBAL_DATA_SYNC_JOB_ACTIV                 = 'globalDataSyncJobActiv'

    static final QUARTZ_HEARTBEAT                           = 'quartzHeartbeat'

    static final IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS  = 'isSendEmailsForDueDatesOfAllUsers'

    static final IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE      = 'isUpdateDashboardTableInDatabase'
    static final LASER_SYSTEM_ID                            = 'laserSystemId'

    static final NOTIFICATIONS_EMAIL_FROM                   = 'notifications.email.from'

    static final NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE       = 'notifications.email.genericTemplate'
    static final NOTIFICATIONS_EMAIL_REPLY_TO               = 'notifications.email.replyTo'
    static final NOTIFICATIONS_JOB_ACTIVE                   = 'notificationsJobActive'

    static final ORG_DUMP_FILE_NAME_PATTERN                 = 'orgDumpFileNamePattern'

    static final ORG_DUMP_FILE_EXTENSION                    = 'orgDumpFileExtension'

    static final SHOW_DEBUG_INFO                            = 'showDebugInfo'
    static final SYSTEM_EMAIL                               = 'systemEmail'
    static final SHOW_SYSTEM_INFO                           = 'showSystemInfo'

    // -- comfortable --

    static boolean getActivateTestJob() {
        getConfig(ConfigUtils.ACTIVATE_TEST_JOB)
    }
    static Object getAppDefaultPrefs() {
        getConfig(ConfigUtils.APP_DEFAULT_PREFS)
    }
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
    static String getFinancialsCurrency() {
        getConfig(ConfigUtils.FINANCIALS_CURRENCY)
    }
    static boolean getGlobalDataSyncJobActiv() {
        getConfig(ConfigUtils.GLOBAL_DATA_SYNC_JOB_ACTIV)
    }
    static String getQuartzHeartbeat() {
        getConfig(ConfigUtils.QUARTZ_HEARTBEAT)
    }
    static boolean getIsSendEmailsForDueDatesOfAllUsers() {
        getConfig(ConfigUtils.IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS)
    }
    static boolean getIsUpdateDashboardTableInDatabase() {
        getConfig(ConfigUtils.IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE)
    }
    static String getLaserSystemId() {
        getConfig(ConfigUtils.LASER_SYSTEM_ID)
    }
    static String getNotificationsEmailFrom() {
        getConfig(ConfigUtils.NOTIFICATIONS_EMAIL_FROM)
    }
    static boolean getNotificationsEmailGenericTemplate() {
        getConfig(ConfigUtils.NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE)
    }
    static String getNotificationsEmailReplyTo() {
        getConfig(ConfigUtils.NOTIFICATIONS_EMAIL_REPLY_TO)
    }
    static boolean getNotificationsJobActive() {
        getConfig(ConfigUtils.NOTIFICATIONS_JOB_ACTIVE)
    }
    static String getOrgDumpFileExtension() {
        getConfig(ConfigUtils.ORG_DUMP_FILE_EXTENSION)
    }
    static String getOrgDumpFileNamePattern() {
        getConfig(ConfigUtils.ORG_DUMP_FILE_NAME_PATTERN)
    }
    static boolean getShowDebugInfo() {
        getConfig(ConfigUtils.SHOW_DEBUG_INFO)
    }
    static String getSystemEmail() {
        getConfig(ConfigUtils.SYSTEM_EMAIL)
    }
    static boolean getShowSystemInfo() {
        getConfig(ConfigUtils.SHOW_SYSTEM_INFO)
    }

    // -- raw --

    static void validate() {
        getAppDefaultPrefs()
        getActivateTestJob()
        getBasicDataFileName()
        getBasicDataPath()
        getDeployBackupLocation()
        getDocumentStorageLocation()
        getFinancialsCurrency()
        getGlobalDataSyncJobActiv()
        getQuartzHeartbeat()
        getIsSendEmailsForDueDatesOfAllUsers()
        getIsUpdateDashboardTableInDatabase()
        getLaserSystemId()
        getNotificationsEmailFrom()
        getNotificationsEmailGenericTemplate()
        getNotificationsEmailReplyTo()
        getNotificationsJobActive()
        getOrgDumpFileExtension()
        getOrgDumpFileNamePattern()
        getShowDebugInfo()
        getSystemEmail()
        getShowSystemInfo()
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
        //println "ConfigUtils -> ${key} : ${result} > ${result?.getClass()}"

        result
    }
}
