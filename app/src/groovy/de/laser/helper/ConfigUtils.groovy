package de.laser.helper

import grails.util.Holders

class ConfigUtils {

    // -- comfortable --

    static boolean getActivateTestJob() {
        readConfig('activateTestJob')
    }
    static String getAggrEsCluster() {
        readConfig('aggr_es_cluster')
    }
    static String getAggrEsHostname() {
        readConfig('aggr_es_hostname')
    }
    static String getAggrEsIndex() {
        readConfig('aggr_es_index')
    }
    static Object getAppDefaultPrefs() {
        readConfig('appDefaultPrefs')
    }
    static String getBasicDataFileName() {
        readConfig('basicDataFileName')
    }
    static String getBasicDataPath() {
        readConfig('basicDataPath')
    }
    static String getDeployBackupLocation() {
        readConfig('deployBackupLocation')
    }
    static String getDocumentStorageLocation() {
        readConfig('documentStorageLocation')
    }
    static String getFinancialsCurrency() {
        readConfig('financials.currency')
    }
    static boolean getGlobalDataSyncJobActiv() {
        readConfig('globalDataSyncJobActiv')
    }
    static String getQuartzHeartbeat() {
        readConfig('quartzHeartbeat')
    }
    static boolean getIsSendEmailsForDueDatesOfAllUsers() {
        readConfig('isSendEmailsForDueDatesOfAllUsers')
    }
    static boolean getIsUpdateDashboardTableInDatabase() {
        readConfig('isUpdateDashboardTableInDatabase')
    }
    static String getLaserSystemId() {
        readConfig('laserSystemId')
    }
    static String getNotificationsEmailFrom() {
        readConfig('notifications.email.from')
    }
    static boolean getNotificationsEmailGenericTemplate() {
        readConfig('notifications.email.genericTemplate')
    }
    static String getNotificationsEmailReplyTo() {
        readConfig('notifications.email.replyTo')
    }
    static boolean getNotificationsJobActive() {
        readConfig('notificationsJobActive')
    }
    static String getOrgDumpFileExtension() {
        readConfig('orgDumpFileExtension')
    }
    static String getOrgDumpFileNamePattern() {
        readConfig('orgDumpFileNamePattern')
    }
    static String getSchemaSpyScriptFile() {
        readConfig('schemaSpyScriptFile')
    }
    static boolean getShowDebugInfo() {
        readConfig('showDebugInfo')
    }
    static boolean getShowSystemInfo() {
        readConfig('showSystemInfo')
    }
    static String getStatsApiUrl() {
        readConfig('statsApiUrl')
    }
    static boolean getStatsSyncJobActiv() {
        readConfig('StatsSyncJobActiv')
    }
    static String getSystemEmail() {
        readConfig('systemEmail')
    }

    // -- check --

    static void checkConfig() {
        getActivateTestJob()
        getAggrEsCluster()
        getAggrEsHostname()
        getAggrEsIndex()
        getAppDefaultPrefs()
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
        getSchemaSpyScriptFile() // QA only
        getShowDebugInfo()
        getShowSystemInfo()
        getStatsApiUrl()
        getStatsSyncJobActiv()
        getSystemEmail()
    }

    // -- raw --

    static def readConfig(String key) {
        def result

        if (key) {
            ConfigObject cfg = Holders.grailsApplication.config

            key.split('\\.').each { lvl ->
                result = result ? result.get(lvl) : cfg.get(lvl)
            }
            if (result == null) {
                println("WARNING: ConfigUtils > configuration '${key}' not found")
            }
        }
        //println "ConfigUtils -> ${key} : ${result} > ${result?.getClass()}"

        result
    }
}