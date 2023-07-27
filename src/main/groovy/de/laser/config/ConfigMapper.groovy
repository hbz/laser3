package de.laser.config

import grails.util.Holders
import groovy.util.logging.Slf4j
import org.springframework.core.env.Environment

@Slf4j
class ConfigMapper {

    static final int NO_OUTPUT = 0
    static final int LOGGER = 1
    static final int PRINTLN = 2
    
    // -- controlled configuration

    static final List AGGR_ES_CLUSTER               = ['aggr_es_cluster',   String]
    static final List AGGR_ES_HOSTNAME              = ['aggr_es_hostname',  String]
    static final List AGGR_ES_INDICES               = ['aggr_es_indices',   Map]
    static final List AGGR_ES_GOKB_CLUSTER          = ['aggr_es_gokb_cluster',  String]
    static final List AGGR_ES_GOKB_HOSTNAME         = ['aggr_es_gokb_hostname', String]
    static final List AGGR_ES_GOKB_INDEX            = ['aggr_es_gokb_index',    String]

    static final List DEPLOY_BACKUP_LOCATION        = ['deployBackupLocation',      String]
    static final List DOCUMENT_STORAGE_LOCATION     = ['documentStorageLocation',   String]

    static final List FINANCIALS_CURRENCY           = ['financials.currency', String]

    static final List GLOBAL_DATA_SYNC_JOB_ACTIVE                   = ['globalDataSyncJobActive', Boolean]
    static final List GRAILS_MAIL_DISABLED                          = ['grails.mail.disabled',   Boolean]
    static final List GRAILS_PLUGIN_WKHTMLTOPDF_BINARY              = ['grails.plugin.wkhtmltopdf.binary',     String]
    static final List GRAILS_PLUGIN_WKHTMLTOPDF_XVFBRUNNER          = ['grails.plugin.wkhtmltopdf.xvfbRunner', String]
    static final List GRAILS_SERVER_URL                             = ['grails.serverURL', String]

    static final List INDEX_UPDATE_JOB_ACTIVE                       = ['indexUpdateJobActive', Boolean]
    static final List IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS     = ['isSendEmailsForDueDatesOfAllUsers', Boolean]
    static final List IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE         = ['isUpdateDashboardTableInDatabase',  Boolean]

    static final List LASER_SYSTEM_ID                               = ['laserSystemId', String]

    static final List MULE_JOB_ACTIVE                               = ['muleJobActive', Boolean]

    static final List NOTIFICATIONS_EMAIL_FROM                = ['notifications.email.from', String]
    static final List NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE    = ['notifications.email.genericTemplate', Boolean]
    static final List NOTIFICATIONS_EMAIL_REPLY_TO            = ['notifications.email.replyTo', String]
    static final List NOTIFICATIONS_JOB_ACTIVE                = ['notificationsJobActive', Boolean]

    static final List PGDUMP_PATH           = ['pgDumpPath', String]
    static final List QUARTZ_HEARTBEAT      = ['quartzHeartbeat', Date]
    static final List REPORTING             = ['reporting', Map]

    static final List SHOW_DEBUG_INFO            = ['showDebugInfo',  Boolean]
    static final List SHOW_SYSTEM_INFO           = ['showSystemInfo', Boolean]
    static final List SHOW_STATS_INFO            = ['showStatsInfo',  Boolean]
    static final List STATS_API_URL              = ['statsApiUrl', String]
    static final List STATS_REPORT_SAVE_LOCATION = ['statsReportSaveLocation', String]
    static final List STATS_SYNC_JOB_ACTIVE      = ['statsSyncJobActive', Boolean]
    static final List SYSTEM_EMAIL               = ['systemEmail', String]
    static final List SYSTEM_INSIGHT_INDEX       = ['systemInsightIndex', String]
    static final List SYSTEM_INSIGHT_JOB_ACTIVE  = ['systemInsightJobActive', Boolean]

    static final List WEKB_API_USERNAME     = ['wekbApiUsername', String]
    static final List WEKB_API_PASSWORD     = ['wekbApiPassword', String]

    static final List<List> CONTROLLED_CONFIGURATION_LIST = [

            AGGR_ES_CLUSTER, AGGR_ES_HOSTNAME, AGGR_ES_INDICES, AGGR_ES_GOKB_CLUSTER, AGGR_ES_GOKB_HOSTNAME, AGGR_ES_GOKB_INDEX,
            DEPLOY_BACKUP_LOCATION, DOCUMENT_STORAGE_LOCATION,
            FINANCIALS_CURRENCY,
            GLOBAL_DATA_SYNC_JOB_ACTIVE, GRAILS_MAIL_DISABLED, GRAILS_PLUGIN_WKHTMLTOPDF_BINARY, GRAILS_PLUGIN_WKHTMLTOPDF_XVFBRUNNER, GRAILS_SERVER_URL,
            INDEX_UPDATE_JOB_ACTIVE, IS_SEND_EMAILS_FOR_DUE_DATES_OF_ALL_USERS, IS_UPDATE_DASHBOARD_TABLE_IN_DATABASE,
            LASER_SYSTEM_ID,
            MULE_JOB_ACTIVE,
            NOTIFICATIONS_EMAIL_FROM, NOTIFICATIONS_EMAIL_GENERIC_TEMPLATE, NOTIFICATIONS_EMAIL_REPLY_TO, NOTIFICATIONS_JOB_ACTIVE,
            PGDUMP_PATH,
            QUARTZ_HEARTBEAT,
            REPORTING,
            SHOW_DEBUG_INFO, SHOW_SYSTEM_INFO, SHOW_STATS_INFO, STATS_API_URL, STATS_SYNC_JOB_ACTIVE, SYSTEM_EMAIL, SYSTEM_INSIGHT_INDEX, SYSTEM_INSIGHT_JOB_ACTIVE,
            WEKB_API_USERNAME, WEKB_API_PASSWORD
    ]

    static File getCurrentConfigFile(Environment environment) {
        Map<String, Object> sysProps = environment.properties.get('systemProperties') as Map

        String cfgFile = (sysProps.get('info.app.name') ?: environment.getProperty('info.app.name')) + '-config.groovy' // TODO : fallback - database migration plugin

//        if (grails.util.Environment.isDevelopmentMode()) {
//            String override = environment.getProperty('LASER_CONFIG_FILE')
//            if (override) { cfgFile = override }
//        }

        new File("${System.getProperty('user.home')}/.grails/${cfgFile}")
    }

    static void checkCurrentConfig() {
        log.info('ConfigMapper - checkCurrentConfig')

        println ": ------------------------------------------------------------------------------------------>"
        CONTROLLED_CONFIGURATION_LIST.each { cc ->
            readConfig(cc, PRINTLN)
        }
        println ": ------------------------------------------------------------------------------------------>"
    }

    // -- basic getter/setter --

    static def getConfig(String token, Class cls) {
        readConfig( [token, cls] )
    }
    static def getPluginConfig(String token, Class cls) {
        readConfig( ['grails.plugin.' + token, cls] )
    }

    static def setConfig(String token, def value) {
        log.warn 'Changing grailsApplication.config -> ' + token + ' = ' + value

        Holders.grailsApplication.config.put(token, value)
    }
    static def setConfig(List cfg, def value) {
        setConfig(cfg[0] as String, value)
    }

    // -- comfortable --

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
    static boolean getGlobalDataSyncJobActive(int output = LOGGER) {
        readConfig( GLOBAL_DATA_SYNC_JOB_ACTIVE, output )
    }
    static boolean getGrailsMailDisabled(int output = LOGGER) {
        readConfig( GRAILS_MAIL_DISABLED, output )
    }
    static boolean getIndexUpdateJobActive(int output = LOGGER) {
        readConfig( INDEX_UPDATE_JOB_ACTIVE, output )
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
    static String getLaserSystemId(int output = LOGGER) {
        readConfig( LASER_SYSTEM_ID, output )
    }
    static boolean getMuleJobActive(int output = LOGGER) {
        readConfig( MULE_JOB_ACTIVE, output )
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
    static Date getQuartzHeartbeat(int output = LOGGER) {
        readConfig( QUARTZ_HEARTBEAT, output ) as Date
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
    static String getStatsReportSaveLocation(int output = LOGGER) {
        readConfig( STATS_REPORT_SAVE_LOCATION, output )
    }
    static boolean getStatsSyncJobActive(int output = LOGGER) {
        readConfig( STATS_SYNC_JOB_ACTIVE, output )
    }
    static String getSystemEmail(int output = LOGGER) {
        readConfig( SYSTEM_EMAIL, output )
    }
    static String getSystemInsightIndex(int output = LOGGER) {
        readConfig( SYSTEM_INSIGHT_INDEX, output )
    }
    static boolean getSystemInsightJobActive(int output = LOGGER) {
        readConfig( SYSTEM_INSIGHT_JOB_ACTIVE, output )
    }
    static String getWekbApiUsername(int output = LOGGER) {
        readConfig( WEKB_API_USERNAME, output )
    }
    static String getWekbApiPassword(int output = LOGGER) {
        readConfig( WEKB_API_PASSWORD, output )
    }

    // -- raw --

    static def readConfig(List cfg, int output = NO_OUTPUT) {
        def result

        if (cfg) {
            result = Holders.grailsApplication.config.getProperty( cfg[0] as String, cfg[1] as Class )

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
                        log.warn 'Configuration not found: ' + cfg + ' ; ' + ste.toString()
                    }
                    else {
                        log.warn 'Configuration found, but no value: ' + cfg + ' ; ' + ste.toString()
                    }
                }
            }
            else if (output == PRINTLN) {
                if (result == null) {
                    println(": NOT found              ${cfg}")
                }
                else if (result instanceof org.grails.config.NavigableMap.NullSafeNavigator) {
                    println(": found, value EMPTY     ${cfg}")
                }
                else {
                    println(": OK                     ${cfg}  ->  " + result + '  (' + result.getClass() + ')')
                }
            }
        }
        result
    }
}