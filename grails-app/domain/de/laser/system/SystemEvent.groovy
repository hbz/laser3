package de.laser.system

import de.laser.storage.BeanStore
import de.laser.utils.LocaleUtils
import grails.converters.JSON
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

import javax.persistence.Transient

/**
 * This class reflects cronjob-related event records and serves to mark events. Depending on the relevance of the event, this event appears in a mail reminder sent to all developers (every morning at 7 o'clock AM;
 * this time may be defined by a cronjob script on a server instance directly). The system events may be reviewed in /admin/systemEvents where every event is listed and cronjob runnings may be checked
 */
@Slf4j
class SystemEvent {

    @Transient
    private String i18n
    @Transient
    private long startTime

    String    token                 // i18n and more
    String    payload               // json for object ids, etx
    CATEGORY  category
    RELEVANCE relevance
    Date      created
    boolean   hasChanged = false    // changeTo called

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
            'CRYPTO_DECRYPT_ERROR'          : [category: CATEGORY.OTHER,   relevance: RELEVANCE.WARNING],
            'CRYPTO_ENCRYPT_ERROR'          : [category: CATEGORY.OTHER,   relevance: RELEVANCE.WARNING],
            'DBDD_JOB_START'                : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_COMPLETE'             : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_IGNORE'               : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.WARNING],
            'DBDD_SERVICE_ERROR_1'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBDD_SERVICE_START_2'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_2'       : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_2'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBDD_SERVICE_START_3'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_3'       : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_3'          : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBM_SCRIPT_INFO'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBM_SCRIPT_ERROR'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DOCSTORE_ENC_RAW_FILES'        : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'DOCSTORE_MOV_OUTDATED_FILES'   : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'DOCSTORE_DEL_ORPHANED_DOCS'    : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'FT_INDEX_UPDATE_START'         : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'FT_INDEX_UPDATE_COMPLETE'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'FT_INDEX_UPDATE_ERROR'         : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'FT_INDEX_UPDATE_KILLED'        : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.WARNING],
            'FT_INDEX_CLEANUP_ERROR'        : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GD_SYNC_JOB_START'             : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'GD_SYNC_JOB_COMPLETE'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'GDC_INFO'                      : [category: CATEGORY.OTHER,  relevance: RELEVANCE.INFO],
            'GSSS_JSON_START'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_JSON_COMPLETE'            : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_JSON_ERROR'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GSSS_JSON_WARNING'             : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.WARNING],
            'GSSS_OAI_START'                : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_OAI_COMPLETE'             : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_OAI_ERROR'                : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GSSS_OAI_WARNING'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.WARNING],
            'LIC_UPDATE_SERVICE_PROCESSING' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'LOGIN_WARNING'                 : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.WARNING],
            'MULE_START'                    : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'MULE_COMPLETE'                 : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'REMOVE_TITLE_JOB_START'        : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'REMOVE_TITLE_JOB_COMPLETE'     : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'STATS_SYNC_JOB_START'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'STATS_SYNC_JOB_WARNING'        : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.WARNING],
            'STATS_SYNC_JOB_COMPLETE'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'STATS_CALL_ERROR'              : [category: CATEGORY.OTHER, relevance: RELEVANCE.WARNING],
            'SUB_RENEW_JOB_START'           : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_RENEW_JOB_COMPLETE'        : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_RENEW_SERVICE_PROCESSING'  : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_JOB_START'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_JOB_COMPLETE'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_SERVICE_PROCESSING' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'SURVEY_UPDATE_JOB_START'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SURVEY_UPDATE_JOB_COMPLETE'    : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SURVEY_UPDATE_SERVICE_PROCESSING' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'SUS_SEND_MAIL_ERROR'           : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'SYSANN_SENDING_OK'             : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'SYSANN_SENDING_ERROR'          : [category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR],
            'SYSTEM_INFO_JOB_START'         : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SYSTEM_INFO_JOB_STOP'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SYSTEM_INSIGHT_MAILS_START'    : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'SYSTEM_INSIGHT_MAILS_COMPLETE' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'SYSTEM_INSIGHT_MAILS_ERROR'    : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'UA_FLAG_DISABLED'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'UA_FLAG_EXPIRED'               : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'UA_FLAG_LOCKED'                : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'UA_FLAG_UNLOCKED'              : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'USER_INFO'                     : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_START'           : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_DELETED'         : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_CREATED'         : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO]
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
        payload     column:'se_payload',    type: 'text'
        category    column:'se_category',   index: 'se_category_idx'
        relevance   column:'se_relevance',  index: 'se_relevance_idx'
        hasChanged  column:'se_has_changed'
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

            if (DEFINED_EVENTS.containsKey(token)) {
                result = new SystemEvent(
                        category: DEFINED_EVENTS.get(token).category,
                        relevance: DEFINED_EVENTS.get(token).relevance)
            } else {
                result = new SystemEvent(category: CATEGORY.UNKNOWN, relevance: RELEVANCE.UNKNOWN)
            }

            if (result) {
                result.startTime = System.currentTimeMillis()

                result.token = token
                result.payload = payload ? (new JSON(payload)).toString(false) : null

                result.save()
            }
            result
        }
    }

    /**
     * Checks for the defined events whether there exists a explanatory message string in the translation resource file
     * @return true if there is one for at least one locale (DE, EN), false otherwise
     */
    static boolean checkDefinedEvents() {
        MessageSource messageSource = BeanStore.getMessageSource()
        boolean valid = true
        log.info 'SystemEvent - checkDefinedEvents'

        DEFINED_EVENTS.each { k, v ->
            try {
                messageSource.getMessage('se.' + k, null, LocaleUtils.getLocaleDE())
            } catch(Exception e) {
                log.warn '- locale DE not found for ' + k
                valid = false
            }
            try {
                messageSource.getMessage('se.' + k, null, LocaleUtils.getLocaleEN())
            } catch(Exception e) {
                log.warn '- locale EN not found for ' + k
                valid = false
            }
        }
        valid
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
     * Gets the last system event record of the given token
     * @param token the token of which the last record should be retrieved
     * @return the most recent system event record of the given token
     */
    static SystemEvent getLastByToken(String token) {
        find('from SystemEvent se where se.token = :token order by se.created desc', [token: token])
    }

    /**
     * Changes the current system event record to the given new token and sets the given payload
     * @param token the new token for this record
     * @param payload the new JSON payload
     */
    void changeTo(String token, Map<String, Object> payload = [:]) {
        withTransaction {
            if (DEFINED_EVENTS.containsKey(token)) {
                log.info '> changed given SystemEvent (ID:' + this.id + ') from ' + this.token + ' to ' + token

                this.token = token
                this.category = DEFINED_EVENTS.get(token).category
                this.relevance = DEFINED_EVENTS.get(token).relevance

                this.hasChanged = true
                payload.s = this.startTime ? ((System.currentTimeMillis() - this.startTime) / 1000).round(2) : 0

                this.payload = (new JSON(payload)).toString(false)
                this.save()
            }
        }
    }

    /**
     * This method is actually a getter. It gets the internationalised message by the system event token
     */
    private void _setInfo() {
        if (!i18n) {
            try {
                i18n = BeanStore.getMessageSource().getMessage('se.' + token, null, LocaleUtils.getCurrentLocale())
            } catch (Exception e) {
                log.warn '- missing locale for token: ' + token
                i18n = BeanStore.getMessageSource().getMessage('se.UNKNOWN', null, LocaleUtils.getCurrentLocale())
            }
        }
    }

    // GETTER

    /**
     * Gets a certain part of the internationalised message string
     * @param index the index of the message string (array, split by '|') to be retrieved
     * @return the message part or an empty string if not found
     */
    private String _getInfoPart(def index) {
        _setInfo()
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
        _getInfoPart(0)
    }

    /**
     * Gets the event of the message
     * @return the event part of the internationalised message string
     */
    String getEvent() {
        _getInfoPart(1)
    }

    /**
     * Gets the description of the message
     * @return the description part of the internationalised message string
     */
    String getDescr() {
        _getInfoPart(2)
    }
}
