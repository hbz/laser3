package de.laser.helper

import grails.config.Config
import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.core.env.Environment

class ConfigUtils {

    static Log static_logger = LogFactory.getLog(ConfigUtils)

    // -- config file --

    static File getConfigFile(Environment environment) {
        Map<String, Object> sysProps = environment.properties.get('systemProperties') as Map

        String appName = sysProps.get('info.app.name') ?: environment.getProperty('info.app.name') // TODO : fallback - database migration plugin
//        String lcf = sysProps.get('local.config.flag') ?: environment.getProperty('local.config.flag') ?: ''
//        if (lcf) { lcf = '-' + lcf }
//        new File("${System.getProperty('user.home')}/.grails/${ian}-config${lcf}.groovy")
        new File("${System.getProperty('user.home')}/.grails/${appName}-config.groovy")
    }

    // -- comfortable --

    static boolean getActivateTestJob(boolean validate = false) {
        readConfig('activateTestJob', validate)
    }
    static String getAggrEsCluster(boolean validate = false) {
        readConfig('aggr_es_cluster', validate)
    }
    static String getAggrEsHostname(boolean validate = false) {
        readConfig('aggr_es_hostname', validate)
    }
    static String getAggrEsIndices(boolean validate = false) {
        readConfig('aggr_es_indices', validate)
    }
    static String getAggrEsGOKBCluster(boolean validate = false) {
        readConfig('aggr_es_gokb_cluster', validate)
    }
    static String getAggrEsGOKBHostname(boolean validate = false) {
        readConfig('aggr_es_gokb_hostname', validate)
    }
    static String getAggrEsGOKBIndex(boolean validate = false) {
        readConfig('aggr_es_gokb_index', validate)
    }
    static String getBasicDataFileName(boolean validate = false) {
        readConfig('basicDataFileName', validate)
    }
    static String getBasicDataPath(boolean validate = false) {
        readConfig('basicDataPath', validate)
    }
    static String getDeployBackupLocation(boolean validate = false) {
        readConfig('deployBackupLocation', validate)
    }
    static String getDocumentStorageLocation(boolean validate = false) {
        readConfig('documentStorageLocation', validate)
    }
    static String getFinancialsCurrency(boolean validate = false) {
        readConfig('financials.currency', validate)
    }
    static boolean getGlobalDataSyncJobActiv(boolean validate = false) {
        readConfig('globalDataSyncJobActiv', validate)
    }
    static boolean getIsSendEmailsForDueDatesOfAllUsers(boolean validate = false) {
        readConfig('isSendEmailsForDueDatesOfAllUsers', validate)
    }
    static boolean getIsUpdateDashboardTableInDatabase(boolean validate = false) {
        readConfig('isUpdateDashboardTableInDatabase', validate)
    }
    static String getLaserStatsSyncJobActive(boolean validate = false) {
        readConfig('laserStatsSyncJobActive', validate)
    }
    static String getLaserSystemId(boolean validate = false) {
        readConfig('laserSystemId', validate)
    }
    static String getNotificationsEmailFrom(boolean validate = false) {
        readConfig('notifications.email.from', validate)
    }
    static boolean getNotificationsEmailGenericTemplate(boolean validate = false) {
        readConfig('notifications.email.genericTemplate', validate)
    }
    static String getNotificationsEmailReplyTo(boolean validate = false) {
        readConfig('notifications.email.replyTo', validate)
    }
    static boolean getNotificationsJobActive(boolean validate = false) {
        readConfig('notificationsJobActive', validate)
    }
    static String getOrgDumpFileExtension(boolean validate = false) {
        readConfig('orgDumpFileExtension', validate)
    }
    static String getOrgDumpFileNamePattern(boolean validate = false) {
        readConfig('orgDumpFileNamePattern', validate)
    }
    static String getPgDumpPath(boolean validate = false) {
        readConfig('pgDumpPath', validate)
    }
    static String getQuartzHeartbeat(boolean validate = false) {
        readConfig('quartzHeartbeat', validate)
    }
    static String getReporting(boolean validate = false) {
        readConfig('reporting', validate)
    }
    static boolean getShowDebugInfo(boolean validate = false) {
        readConfig('showDebugInfo', validate)
    }
    static boolean getShowSystemInfo(boolean validate = false) {
        readConfig('showSystemInfo', validate)
    }
    static boolean getShowStatsInfo(boolean validate = false) {
        readConfig('showStatsInfo', validate)
    }
    static String getStatsApiUrl(boolean validate = false) {
        readConfig('statsApiUrl', validate)
    }
    static String getSystemEmail(boolean validate = false) {
        readConfig('systemEmail', validate)
    }
    static String getWkhtmltopdfBinary(boolean validate = false) {
        readConfig('grails.plugin.wkhtmltopdf.binary', validate)
    }
    static String getWkhtmltopdfXvfbRunner(boolean validate = false) {
        readConfig('grails.plugin.wkhtmltopdf.xvfbRunner', validate)
    }

    // -- check --

    static void checkConfig() {
        static_logger.info('ConfigUtils - checkConfig')

        println ": --------------------------------------------->"

        getActivateTestJob(true)
        getAggrEsCluster(true)
        getAggrEsHostname(true)
        //getAggrEsIndices(true)
        getBasicDataFileName(true)
        getBasicDataPath(true)
        getDeployBackupLocation(true)
        getDocumentStorageLocation(true)
        getFinancialsCurrency(true)
        getGlobalDataSyncJobActiv(true)
        getIsSendEmailsForDueDatesOfAllUsers(true)
        getIsUpdateDashboardTableInDatabase(true)
        getLaserSystemId(true)
        getNotificationsEmailFrom(true)
        getNotificationsEmailGenericTemplate(true)
        getNotificationsEmailReplyTo(true)
        getNotificationsJobActive(true)
        getOrgDumpFileExtension(true)
        getOrgDumpFileNamePattern(true)
        getPgDumpPath(true)
        getQuartzHeartbeat(true)
        getReporting(true)
        getShowDebugInfo(true)
        getShowSystemInfo(true)
        getStatsApiUrl(true)
        getSystemEmail(true)
        getWkhtmltopdfBinary(true)
        getWkhtmltopdfXvfbRunner(true)

        println ": --------------------------------------------->"
    }

    // -- raw --

    static def readConfig(String key, boolean validate) {
        def result

        if (key) {
            Config cfg = Holders.grailsApplication.config

            key.split('\\.').each { lvl ->
                result = result ? result.get(lvl) : cfg.get(lvl)
            }

            if (validate) {
                if (result == null) {
                    println(": NOT found  ->  ${key}")
                }
                else {
                    println(": ok             ${key}  ->  " + result)
                }
            }
        }
        result
    }
}