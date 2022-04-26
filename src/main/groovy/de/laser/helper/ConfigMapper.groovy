package de.laser.helper

import grails.config.Config
import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.core.env.Environment

class ConfigMapper {

    static Log static_logger = LogFactory.getLog(ConfigMapper)
    
    static final int NO_OUTPUT = 0
    static final int LOGGER = 1
    static final int PRINTLN = 2
    
    // -- controlled configuration

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
            readConfig(cc, PRINTLN)
        }
        println ": --------------------------------------------->"
    }

    // -- basic getter --

    static def getConfig(String token) {
        readConfig( token )
    }
    static def getPluginConfig(String token) {
        readConfig( 'grails.plugin.' + token )
    }

    // -- comfortable --

    static boolean getActivateTestJob(int output = LOGGER) {
        readConfig( ACTIVATE_TEST_JOB, output )
    }
    static String getAggrEsCluster(int output = LOGGER) {
        readConfig( AGGR_ES_CLUSTER, output )
    }
    static String getAggrEsHostname(int output = LOGGER) {
        readConfig( AGGR_ES_HOSTNAME, output )
    }
    static Map getAggrEsIndices(int output = LOGGER) {
        readConfig( AGGR_ES_INDICES, output ) as Map
    }
    static String getAggrEsGOKBCluster(int output = LOGGER) {
        readConfig( AGGR_ES_GOKB_CLUSTER, output )
    }
    static String getAggrEsGOKBHostname(int output = LOGGER) {
        readConfig( AGGR_ES_GOKB_HOSTNAME, output )
    }
    static String getAggrEsGOKBIndex(int output = LOGGER) {
        readConfig( AGGR_ES_GOKB_INDEX, output )
    }
    static String getDeployBackupLocation(int output = LOGGER) {
        readConfig( DEPLOY_BACKUP_LOCATION, output )
    }
    static String getDocumentStorageLocation(int output = LOGGER) {
        readConfig( DOCUMENT_STORAGE_LOCATION, output )
    }
    static String getFinancialsCurrency(int output = LOGGER) {
        readConfig( FINANCIALS_CURRENCY, output )
    }
    static boolean getGlobalDataSyncJobActiv(int output = LOGGER) {
        readConfig( GLOBAL_DATA_SYNC_JOB_ACTIV, output )
    }
    static boolean getGrailsMailDisabled(int output = LOGGER) {
        readConfig( GRAILS_MAIL_DISABLED, output )
    }
    static String getWkhtmltopdfBinary(int output = LOGGER) {
        readConfig( GRAILS_PLUGIN_WKHTMLTOPDF_BINARY, output )
    }
    static String getWkhtmltopdfXvfbRunner(int output = LOGGER) {
        readConfig( GRAILS_PLUGIN_WKHTMLTOPDF_XVFBRUNNER, output )
    }
    static String getGrailsServerURL(int output = LOGGER) {
        readConfig( GRAILS_SERVER_URL, output )
    }
    static boolean getIsSendEmailsForDueDatesOfAllUsers(int output = LOGGER) {
        readConfig( IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS, output )
    }
    static boolean getIsUpdateDashboardTableInDatabase(int output = LOGGER) {
        readConfig( IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE, output )
    }
    static boolean getLaserStatsSyncJobActive(int output = LOGGER) {
        readConfig( LASER_STATS_SYNC_JOB_ACTIVE, output )
    }
    static String getLaserSystemId(int output = LOGGER) {
        readConfig( LASER_SYSTEM_ID, output )
    }
    static String getNotificationsEmailFrom(int output = LOGGER) {
        readConfig( NOTIFICATIONS_EMAIL_FROM, output )
    }
    static boolean getNotificationsEmailGenericTemplate(int output = LOGGER) {
        readConfig( NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE, output )
    }
    static String getNotificationsEmailReplyTo(int output = LOGGER) {
        readConfig( NOTIFICATIONS_EMAIL_REPLY_TO, output )
    }
    static boolean getNotificationsJobActive(int output = LOGGER) {
        readConfig( NOTIFICATIONS_JOB_ACTIVE, output )
    }
    static String getPgDumpPath(int output = LOGGER) {
        readConfig( PGDUMP_PATH, output )
    }
    static String getQuartzHeartbeat(int output = LOGGER) {
        readConfig( QUARTZ_HEARTBEAT, output )
    }
    static Map getReporting(int output = LOGGER) {
        readConfig( REPORTING, output ) as Map
    }
    static boolean getShowDebugInfo(int output = LOGGER) {
        readConfig( SHOW_DEBUG_INFO, output )
    }
    static boolean getShowSystemInfo(int output = LOGGER) {
        readConfig( SHOW_SYSTEM_INFO, output )
    }
    static boolean getShowStatsInfo(int output = LOGGER) {
        readConfig( SHOW_STATS_INFO, output )
    }
    static String getStatsApiUrl(int output = LOGGER) {
        readConfig( STATS_API_URL, output )
    }
    static String getSystemEmail(int output = LOGGER) {
        readConfig( SYSTEM_EMAIL, output )
    }

    // -- raw --

    static def readConfig(String key, int output = NO_OUTPUT) {
        def result

        if (key) {
            Config cfg = Holders.grailsApplication.config

            key.split('\\.').each { lvl ->
                result = result ? result.get(lvl) : cfg.get(lvl)
            }

            if (output == LOGGER) {
                if (result == null || result instanceof org.grails.config.NavigableMap.NullSafeNavigator) {
                    List stack = Thread.currentThread().getStackTrace()
                    StackTraceElement ste
                    String methodName = stack.find { it.declaringClass == ConfigMapper.class.name && it.methodName != 'readConfig' }.getMethodName()
                    stack.eachWithIndex { it, i ->
                        if (it.methodName == methodName ) {
                            int j = i+ 1
                            while (stack[j].methodName in ['call', 'defaultCall']) {
                                j++
                            }
                            ste = stack[j]
                        }
                    }
                    if (result == null) {
                        static_logger.warn 'Configuration key not found: ' + key + ' - called: ' + ste.toString()
                    }
                    else {
                        static_logger.warn 'Configuration key found, but no value: ' + key + ' - called: ' + ste.toString()
                    }
                }
            }
            else if (output == PRINTLN) {
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