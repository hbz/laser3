package de.laser.helper

import grails.config.Config
import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.core.env.Environment

class ConfigMapper {

    static Log static_logger = LogFactory.getLog(ConfigMapper)

    static final String ACTIVATE_TEST_JOB       = 'activateTestJob'
    static final String AGGR_ES_CLUSTER         = 'aggr_es_cluster'
    static final String AGGR_ES_HOSTNAME        = 'aggr_es_hostname'
    static final String AGGR_ES_INDICES         = 'aggr_es_indices'
    static final String AGGR_ES_GOKB_CLUSTER    = 'aggr_es_gokb_cluster'
    static final String AGGR_ES_GOKB_HOSTNAME   = 'aggr_es_gokb_hostname'
    static final String AGGR_ES_GOKB_INDEX      = 'aggr_es_gokb_index'

    static final String DEPLOY_BACKUP_LOCATION      = 'deployBackupLocation'
    static final String DOCUMENT_STORAGE_LOCATION   = 'documentStorageLocation'

    static final String FINANCIALS_CURRENCY         = 'financials.currency'

    static final String GLOBAL_DATA_SYNC_JOB_ACTIV                  = 'globalDataSyncJobActiv'
    static final String GRAILS_MAIL_DISABLED                        = 'grails.mail.disabled'
    static final String GRAILS_PLUGIN_WKHTMLTOPDF_BINARY            = 'grails.plugin.wkhtmltopdf.binary'
    static final String GRAILS_PLUGIN_WKHTMLTOPDF_XVFBRUNNER        = 'grails.plugin.wkhtmltopdf.xvfbRunner'
    static final String GRAILS_SERVER_URL                           = 'grails.serverURL'

    static final String IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS   = 'isSendEmailsForDueDatesOfAllUsers'
    static final String IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE       = 'isUpdateDashboardTableInDatabase'

    static final String LASER_STATS_SYNC_JOB_ACTIVE             = 'laserStatsSyncJobActive'
    static final String LASER_SYSTEM_ID                         = 'laserSystemId'

    static final String NOTIFICATIONS_EMAIL_FROM                = 'notifications.email.from'
    static final String NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE    = 'notifications.email.genericTemplate'
    static final String NOTIFICATIONS_EMAIL_REPLY_TO            = 'notifications.email.replyTo'
    static final String NOTIFICATIONS_JOB_ACTIVE                = 'notificationsJobActive'

    static final String PGDUMP_PATH         = 'pgDumpPath'
    static final String QUARTZ_HEARTBEAT    = 'quartzHeartbeat'
    static final String REPORTING           = 'reporting'

    static final String SHOW_DEBUG_INFO     = 'showDebugInfo'
    static final String SHOW_SYSTEM_INFO    = 'showSystemInfo'
    static final String SHOW_STATS_INFO     = 'showStatsInfo'
    static final String STATS_API_URL       = 'statsApiUrl'
    static final String SYSTEM_EMAIL        = 'systemEmail'

    static final List<String> CONTROLLED_CONFIGURATION_LIST = [

            ACTIVATE_TEST_JOB, AGGR_ES_CLUSTER, AGGR_ES_HOSTNAME, AGGR_ES_INDICES, AGGR_ES_GOKB_CLUSTER, AGGR_ES_GOKB_HOSTNAME, AGGR_ES_GOKB_INDEX,
            DEPLOY_BACKUP_LOCATION, DOCUMENT_STORAGE_LOCATION,
            FINANCIALS_CURRENCY,
            GLOBAL_DATA_SYNC_JOB_ACTIV, GRAILS_MAIL_DISABLED, GRAILS_PLUGIN_WKHTMLTOPDF_BINARY, GRAILS_PLUGIN_WKHTMLTOPDF_XVFBRUNNER, GRAILS_SERVER_URL,
            IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS, IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE,
            LASER_STATS_SYNC_JOB_ACTIVE, LASER_SYSTEM_ID,
            NOTIFICATIONS_EMAIL_FROM, NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE, NOTIFICATIONS_EMAIL_REPLY_TO, NOTIFICATIONS_JOB_ACTIVE,
            PGDUMP_PATH,
            QUARTZ_HEARTBEAT,
            REPORTING,
            SHOW_DEBUG_INFO, SHOW_SYSTEM_INFO, SHOW_STATS_INFO, STATS_API_URL, SYSTEM_EMAIL

    ]

    // -- current configuration --

    static File getCurrentConfigFile(Environment environment) {
        Map<String, Object> sysProps = environment.properties.get('systemProperties') as Map

        String appName = sysProps.get('info.app.name') ?: environment.getProperty('info.app.name') // TODO : fallback - database migration plugin
//        String lcf = sysProps.get('local.config.flag') ?: environment.getProperty('local.config.flag') ?: ''
//        if (lcf) { lcf = '-' + lcf }
//        new File("${System.getProperty('user.home')}/.grails/${ian}-config${lcf}.groovy")
        new File("${System.getProperty('user.home')}/.grails/${appName}-config.groovy")
    }

    static void checkCurrentConfig() {
        static_logger.info('ConfigMapper - checkCurrentConfig')

        println ": --------------------------------------------->"
        CONTROLLED_CONFIGURATION_LIST.each { cc ->
            readConfig(cc, true)
        }
        println ": --------------------------------------------->"
    }

    // -- basic getter --

    static def getConfig(String token) {
        readConfig( token, false )
    }
    static def getPluginConfig(String token) {
        readConfig( 'grails.plugin.' + token, false )
    }

    // -- comfortable --

    static boolean getActivateTestJob(boolean validate = false) {
        readConfig( ACTIVATE_TEST_JOB, validate )
    }
    static String getAggrEsCluster(boolean validate = false) {
        readConfig( AGGR_ES_CLUSTER, validate )
    }
    static String getAggrEsHostname(boolean validate = false) {
        readConfig( AGGR_ES_HOSTNAME, validate )
    }
    static Map getAggrEsIndices(boolean validate = false) {
        readConfig( AGGR_ES_INDICES, validate ) as Map
    }
    static String getAggrEsGOKBCluster(boolean validate = false) {
        readConfig( AGGR_ES_GOKB_CLUSTER, validate )
    }
    static String getAggrEsGOKBHostname(boolean validate = false) {
        readConfig( AGGR_ES_GOKB_HOSTNAME, validate )
    }
    static String getAggrEsGOKBIndex(boolean validate = false) {
        readConfig( AGGR_ES_GOKB_INDEX, validate )
    }
    static String getDeployBackupLocation(boolean validate = false) {
        readConfig( DEPLOY_BACKUP_LOCATION, validate )
    }
    static String getDocumentStorageLocation(boolean validate = false) {
        readConfig( DOCUMENT_STORAGE_LOCATION, validate )
    }
    static String getFinancialsCurrency(boolean validate = false) {
        readConfig( FINANCIALS_CURRENCY, validate )
    }
    static boolean getGlobalDataSyncJobActiv(boolean validate = false) {
        readConfig( GLOBAL_DATA_SYNC_JOB_ACTIV, validate )
    }
    static boolean getGrailsMailDisabled(boolean validate = false) {
        readConfig( GRAILS_MAIL_DISABLED, validate )
    }
    static String getWkhtmltopdfBinary(boolean validate = false) {
        readConfig( GRAILS_PLUGIN_WKHTMLTOPDF_BINARY, validate )
    }
    static String getWkhtmltopdfXvfbRunner(boolean validate = false) {
        readConfig( GRAILS_PLUGIN_WKHTMLTOPDF_XVFBRUNNER, validate )
    }
    static String getGrailsServerURL(boolean validate = false) {
        readConfig( GRAILS_SERVER_URL, validate )
    }
    static boolean getIsSendEmailsForDueDatesOfAllUsers(boolean validate = false) {
        readConfig( IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS, validate )
    }
    static boolean getIsUpdateDashboardTableInDatabase(boolean validate = false) {
        readConfig( IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE, validate )
    }
    static boolean getLaserStatsSyncJobActive(boolean validate = false) {
        readConfig( LASER_STATS_SYNC_JOB_ACTIVE, validate )
    }
    static String getLaserSystemId(boolean validate = false) {
        readConfig( LASER_SYSTEM_ID, validate )
    }
    static String getNotificationsEmailFrom(boolean validate = false) {
        readConfig( NOTIFICATIONS_EMAIL_FROM, validate )
    }
    static boolean getNotificationsEmailGenericTemplate(boolean validate = false) {
        readConfig( NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE, validate )
    }
    static String getNotificationsEmailReplyTo(boolean validate = false) {
        readConfig( NOTIFICATIONS_EMAIL_REPLY_TO, validate )
    }
    static boolean getNotificationsJobActive(boolean validate = false) {
        readConfig( NOTIFICATIONS_JOB_ACTIVE, validate )
    }
    static String getPgDumpPath(boolean validate = false) {
        readConfig( PGDUMP_PATH, validate )
    }
    static String getQuartzHeartbeat(boolean validate = false) {
        readConfig( QUARTZ_HEARTBEAT, validate )
    }
    static Map getReporting(boolean validate = false) {
        readConfig( REPORTING, validate ) as Map
    }
    static boolean getShowDebugInfo(boolean validate = false) {
        readConfig( SHOW_DEBUG_INFO, validate )
    }
    static boolean getShowSystemInfo(boolean validate = false) {
        readConfig( SHOW_SYSTEM_INFO, validate )
    }
    static boolean getShowStatsInfo(boolean validate = false) {
        readConfig( SHOW_STATS_INFO, validate )
    }
    static String getStatsApiUrl(boolean validate = false) {
        readConfig( STATS_API_URL, validate )
    }
    static String getSystemEmail(boolean validate = false) {
        readConfig( SYSTEM_EMAIL, validate )
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
                    println(": key NOT found              ${key}")
                }
                else if (result instanceof org.grails.config.NavigableMap.NullSafeNavigator) {
                    println(": key found, value EMPTY     ${key}")
                }
                else {
                    println(": OK                         ${key}  ->  " + result + '  (' + result.getClass() + ')')
                }
            }
        }
        result
    }
}