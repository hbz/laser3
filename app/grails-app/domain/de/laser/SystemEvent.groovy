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

    static final ALLOWED_TOKENS = [
            'BOOTSTRAP_STARTUP',
            'CAJ_JOB_START',
            'CAJ_JOB_COMPLETE',
            'DBDD_JOB_START',
            'DBDD_JOB_COMPLETE',
            'DBDD_JOB_IGNORE',
            'DBDD_SERVICE_START_1',
            'DBDD_SERVICE_COMPLETE_1',
            'DBDD_SERVICE_ERROR_1',
            'DBDD_SERVICE_START_2',
            'DBDD_SERVICE_PROCESSING_2',
            'DBDD_SERVICE_COMPLETE_2',
            'DBDD_SERVICE_ERROR_2',
            'DBDD_SERVICE_START_3',
            'DBDD_SERVICE_COMPLETE_3',
            'DBDD_SERVICE_ERROR_3',
            'FT_INDEX_UPDATE_START',
            'FT_INDEX_UPDATE_ERROR',
            'FT_INDEX_CLEANUP_ERROR',
            'GD_SYNC_JOB_START',
            'GD_SYNC_JOB_COMPLETE',
            'GSSS_OAI_START',
            'GSSS_OAI_COMPLETE',
            'GSSS_OAI_ERROR',
            'STATS_SYNC_JOB_START',
            'STATS_SYNC_JOB_COMPLETE',
            'SUB_UPDATE_JOB_START',
            'SUB_UPDATE_JOB_COMPLETE',
            'YODA_ES_RESET_START'
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
        payload = payload ? (new JSON(payload)).toString(false) : null
        created = created ?: new Date()
    }

    static SystemEvent createEvent(String token) {
        createEvent(token, null)
    }

    static SystemEvent createEvent(String token, def payload) {

        SystemEvent result

        if (! ALLOWED_TOKENS.contains(token)) {
            result = new SystemEvent( category: CATEGORY.UNKNOWN, relevance: RELEVANCE.UNKNOWN )
        }
        else if ('BOOTSTRAP_STARTUP' == token) {
            result = new SystemEvent( category: CATEGORY.SYSTEM, relevance: RELEVANCE.INFO )
        }
        else if ('CAJ_JOB_START' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('CAJ_JOB_COMPLETE' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_JOB_START' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_JOB_COMPLETE' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_JOB_IGNORE' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.WARNING )
        }
        else if ('DBDD_SERVICE_START_1' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_COMPLETE_1' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_ERROR_1' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR )
        }
        else if ('DBDD_SERVICE_START_2' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_PROCESSING_2' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_COMPLETE_2' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_ERROR_2' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR )
        }
        else if ('DBDD_SERVICE_START_3' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_COMPLETE_3' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('DBDD_SERVICE_ERROR_3' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR )
        }
        else if ('FT_INDEX_UPDATE_START' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('FT_INDEX_UPDATE_ERROR' == token) {
            result = new SystemEvent(  category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR )
        }
        else if ('FT_INDEX_CLEANUP_ERROR' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR )
        }
        else if ('GD_SYNC_JOB_START' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('GD_SYNC_JOB_COMPLETE' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('GSSS_OAI_START' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('GSSS_OAI_ERROR' == token) {
            result =  new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.ERROR )
        }
        else if ('GSSS_OAI_COMPLETE' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }
        else if ('STATS_SYNC_JOB_START' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('STATS_SYNC_JOB_COMPLETE' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('SUB_UPDATE_JOB_START' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('SUB_UPDATE_JOB_COMPLETE' == token) {
            result = new SystemEvent( category: CATEGORY.CRONJOB, relevance: RELEVANCE.INFO )
        }
        else if ('YODA_ES_RESET_START' == token) {
            result = new SystemEvent( category: CATEGORY.OTHER, relevance: RELEVANCE.INFO )
        }

        if (result) {
            result.token = token
            result.payload = payload

            result.save(flush:true)
        }

        result
    }

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
