package de.laser.system

import de.laser.storage.BeanStore
import de.laser.helper.MigrationHelper
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.time.LocalDate

/**
 * This class reflects cronjob-related event records and serves to mark events. Depending on the relevance of the event, this event appears in a mail reminder sent to all developers (every morning at 7 o'clock AM;
 * this time may be defined by a cronjob script on a server instance directly). The system events may be reviewed in /admin/systemEvents where every event is listed and cronjob runnings may be checked
 */
@Slf4j
class SystemEvent {

    @Transient
    private String i18n

    String    token        // i18n and more
    String    payload      // json for object ids, etx
    CATEGORY  category
    RELEVANCE relevance
    Date      created

    /**
     * The event types which may be triggered
     */
    static final DEFINED_EVENTS = [
            'ADM_JOB_START'                 : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'BATCH_IMP_JOB_START'           : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'BATCH_TOUCH_JOB_START'         : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'BOOTSTRAP_STARTUP'             : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'CAJ_JOB_START'                 : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'CAJ_JOB_COMPLETE'              : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_START'                : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_COMPLETE'             : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_IGNORE'               : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.WARNING],
            'DBDD_SERVICE_START_1'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_1'       : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_1'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBDD_SERVICE_START_2'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_PROCESSING_2'     : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_START_COLLECT_DASHBOARD_DATA':    [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_END_COLLECT_DASHBOARD_DATA':      [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_START_TRANSACTION': [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_END_TRANSACTION'  : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_2'       : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_2'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBDD_SERVICE_START_3'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_3'       : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_3'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBM_SCRIPT_START'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBM_SCRIPT_INFO'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBM_SCRIPT_ERROR'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'FT_INDEX_UPDATE_START'         : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'FT_INDEX_UPDATE_END'           : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'FT_INDEX_UPDATE_ERROR'         : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'FT_INDEX_CLEANUP_ERROR'        : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GD_SYNC_JOB_START'             : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'GD_SYNC_JOB_COMPLETE'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'GSSS_OAI_START'                : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_OAI_COMPLETE'             : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_OAI_ERROR'                : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GSSS_OAI_WARNING'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.WARNING],
            'GSSS_JSON_START'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_JSON_COMPLETE'            : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_JSON_ERROR'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GSSS_JSON_WARNING'             : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.WARNING],
            'STATS_SYNC_JOB_START'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'STATS_SYNC_JOB_WARNING'        : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.WARNING],
            'STATS_SYNC_JOB_COMPLETE'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_JOB_START'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_JOB_COMPLETE'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_RENEW_JOB_START'           : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_RENEW_JOB_COMPLETE'        : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUS_SEND_MAIL_ERROR'           : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.ERROR],
            'SURVEY_UPDATE_JOB_START'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SURVEY_UPDATE_JOB_COMPLETE'    : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SURVEY_UPDATE_SERVICE_PROCESSING' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'SYSANN_SENDING_OK'             : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'SYSANN_SENDING_ERROR'          : [category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR],
            'YODA_ES_RESET_START'           : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_DROP_OK'         : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_CREATE_OK'       : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_END'             : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO]
    ]

    /**
     * The relevance of an event. It the relevance is WARNING oder ERROR, it appears in the reminder mail to the developers and usually requires action on behalf of them
     */
    static enum RELEVANCE {
        INFO    ("INFO"),
        WARNING ("WARNING"),
        ERROR   ("ERROR"),
        UNKNOWN ("UNKNOWN")

        RELEVANCE(String value) {
            this.value = value
        }
        public String value
    }

    /**
     * The category of an event
     */
    static enum CATEGORY {
        SYSTEM    ("SYSTEM"),
        CRONJOB   ("CRONJOB"),
        OTHER     ("OTHER"),
        UNKNOWN   ("UNKNOWN")

        CATEGORY(String value) {
            this.value = value
        }
        public String value
    }

    static transients = ['source', 'event', 'descr'] // mark read-only accessor methods

    static mapping = {
        id          column:'se_id'
        version     false
        token       column:'se_token'
        payload     column:'se_payload',   type: 'text'
        category    column:'se_category',   index: 'se_category_idx'
        relevance   column:'se_relevance',  index: 'se_relevance_idx'
        created     column:'se_created'
    }

    static constraints = {
        token       (nullable:true)
        payload     (nullable:true, blank:true)
        created     (nullable:true)
    }

    def beforeInsert() {
        created = created ?: new Date()
    }

    /**
     * Creates a new system event with the given token but without payload
     * @param token one of the {@link #DEFINED_EVENTS} constants
     * @return the new event
     */
    static SystemEvent createEvent(String token) {
        createEvent(token, null)
    }

    /**
     * Creates a new system event with the given event and JSON payload
     * @param token one of the {@link #DEFINED_EVENTS} constants
     * @param payload additional data which will be formatted in JSON
     * @return the new event
     */
    static SystemEvent createEvent(String token, def payload) {

        withTransaction {
            SystemEvent result

            if (SystemEvent.DEFINED_EVENTS.containsKey(token)) {
                result = new SystemEvent(
                        category: SystemEvent.DEFINED_EVENTS.get(token).category,
                        relevance: SystemEvent.DEFINED_EVENTS.get(token).relevance)
            } else {
                result = new SystemEvent(category: CATEGORY.UNKNOWN, relevance: RELEVANCE.UNKNOWN)
            }

            if (result) {
                result.token = token
                result.payload = payload ? (new JSON(payload)).toString(false) : null

                result.save()
            }
            result
        }
    }

    /**
     * Gets a list of distinct sources of all system events
     * @param list a list of system events whose source should be retrieved
     * @return a {@link List} of sources
     */
    static List<String> getAllSources(List<SystemEvent> list) {
        List<String> result = []

        list.each { it -> result.add( it.getSource() ) }
        result.unique().sort()
    }

    /**
     * Cleans up recorded system events which are older than three years
     * @return the count of deleted events
     */
    static int cleanUpOldEvents() {
        executeUpdate('delete from SystemEvent se where se.created <= :limit', [limit: MigrationHelper.localDateToSqlDate( LocalDate.now().minusYears(3) )])
    }

    // GETTER

    /**
     * This method is actually a getter. It gets the internationalised message by the system event token
     */
    private void setInfo() {
        if (!i18n) {
            i18n = BeanStore.getMessageSource().getMessage('se.' + (token ?: 'UNKNOWN'), null, LocaleContextHolder.locale)
        }
    }

    /**
     * Gets a certain part of the internationalised message string
     * @param index the index of the message string (array, split by '|') to be retrieved
     * @return the message part or an empty string if not found
     */
    private String getInfoPart(def index) {
        setInfo()
        List parts = i18n.split('\\|')

        if (parts.size() > index) {
            i18n.split('\\|')[index]
        }
        else {
            ''
        }
    }

    /**
     * Gets the source of the message
     * @return the source part of the internationalised message string
     */
    String getSource() {
        getInfoPart(0)
    }

    /**
     * Gets the event of the message
     * @return the event part of the internationalised message string
     */
    String getEvent() {
        getInfoPart(1)
    }

    /**
     * Gets the description of the message
     * @return the description part of the internationalised message string
     */
    String getDescr() {
        getInfoPart(2)
    }
}
