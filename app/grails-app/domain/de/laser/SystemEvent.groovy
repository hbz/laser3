package de.laser

import grails.converters.JSON
import org.springframework.context.i18n.LocaleContextHolder
import javax.persistence.Transient

class SystemEvent {

    @Transient
    def messageSource

    private i18n

    String    token        // i18n and more
    String    payload      // json for object ids, etx
    CATEGORY  category
    RELEVANCE relevance
    Date      created

    static final DEFINED_EVENTS = [
            'ADM_JOB_START'             : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'BATCH_IMP_JOB_START'       : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'BATCH_TOUCH_JOB_START'     : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'BOOTSTRAP_STARTUP'         : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'CAJ_JOB_START'             : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'CAJ_JOB_COMPLETE'          : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_START'            : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_COMPLETE'         : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'DBDD_JOB_IGNORE'           : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.WARNING],
            'DBDD_SERVICE_START_1'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_1'   : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_1'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBDD_SERVICE_START_2'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_PROCESSING_2' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_2'   : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_2'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'DBDD_SERVICE_START_3'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_COMPLETE_3'   : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'DBDD_SERVICE_ERROR_3'      : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'FT_INDEX_UPDATE_START'     : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'FT_INDEX_UPDATE_ERROR'     : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'FT_INDEX_CLEANUP_ERROR'    : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'GD_SYNC_JOB_START'         : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'GD_SYNC_JOB_COMPLETE'      : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'GSSS_OAI_START'            : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_OAI_COMPLETE'         : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'GSSS_OAI_ERROR'            : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.ERROR],
            'STATS_SYNC_JOB_START'      : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'STATS_SYNC_JOB_COMPLETE'   : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_JOB_START'      : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_JOB_COMPLETE'   : [category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO],
            'SUB_UPDATE_SERVICE_PROCESSING' : [category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO],
            'YODA_ES_RESET_START'       : [category: CATEGORY.OTHER, relevance: RELEVANCE.INFO]
    ]

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

    static mapping = {
        id          column:'se_id'
        token       column:'se_token'
        payload     column:'se_payload', type: 'text'
        category    column:'se_category'
        relevance   column:'se_relevance'
        created     column:'se_created'
    }

    static constraints = {
        token       (nullable:true)
        payload     (nullable:true,  blank:true)
        category    (nullable:false, blank:false)
        relevance   (nullable:false, blank:false)
        created     (nullable:true)
    }

    def beforeInsert() {
        created = created ?: new Date()
    }

    static SystemEvent createEvent(String token) {
        createEvent(token, null)
    }

    static SystemEvent createEvent(String token, def payload) {

        SystemEvent result

        if (DEFINED_EVENTS.containsKey(token)) {
            result = new SystemEvent( category: DEFINED_EVENTS.get(token).category, relevance: DEFINED_EVENTS.get(token).relevance )
        }
        else {
            result = new SystemEvent( category: CATEGORY.UNKNOWN, relevance: RELEVANCE.UNKNOWN )
        }

        if (result) {
            result.token = token
            result.payload = payload ? (new JSON(payload)).toString(false) : null

            result.save(flush:true)
        }

        result
    }

    static getAllSources() {
        def result = []
        SystemEvent.findAll().each { it ->
            result.add( it.getSource() )
        }
        result.unique()
    }

    // GETTER

    private setInfo() {
        if (!i18n) {
            i18n = messageSource.getMessage('se.' + (token ?: 'UNKNOWN'), null, LocaleContextHolder.locale)
        }
        true
    }
    def getSource() {
        setInfo()
        i18n.split('\\|')[0]
    }
    def getEvent() {
        setInfo()
        i18n.split('\\|')[1]
    }
    def getDescr() {
        setInfo()
        i18n.split('\\|')[2]
    }
}
