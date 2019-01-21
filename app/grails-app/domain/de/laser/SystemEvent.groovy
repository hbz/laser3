package de.laser

import grails.converters.JSON

class SystemEvent {

    String    token        // TODO

    String    source       // class, service, etc
    String    event        // specific type of event; e.g. sql updates, cleanup, sync, sending mails
    String    message      // short human friendly message
    String    payload      // json for object ids, etx
    CATEGORY  category
    RELEVANCE relevance
    Date      created

    static final ALLOWED_TOKENS = [
            'BOOTSTRAP_STARTUP',
            'FT_INDEX_UPDATE_START',
            'FT_INDEX_UPDATE_ERROR',
            'FT_INDEX_CLEANUP_ERROR',
            'GSSS_OAI_START',
            'GSSS_OAI_COMPLETE',
            'GSSS_OAI_ERROR',
            'YODA_ES_RESET_START',
            'DBDD_JOB_START',
            'DBDD_JOB_COMPLETE',
            'DBDD_JOB_IGNORE',
            'SUB_UPDATE_JOB_START',
            'SUB_UPDATE_JOB_COMPLETE',
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
        source      column:'se_source'
        event       column:'se_event'
        message     column:'se_message'
        payload     column:'se_payload', type: 'text'
        category    column:'se_category'
        relevance   column:'se_relevance'
        created     column:'se_created'
    }

    static constraints = {
        token       (nullable:true)
        source      (nullable:false, blank:false)
        event       (nullable:false, blank:false)
        message     (nullable:false, blank:false)
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
            result = new SystemEvent(
                    source:    token,
                    event:     token,
                    message:   token,
                    category:  CATEGORY.UNKNOWN,
                    relevance: RELEVANCE.UNKNOWN
            )
        }
        else if ('BOOTSTRAP_STARTUP' == token) {
            result = new SystemEvent(
                    source:    'BootStrap',
                    event:     'Startup',
                    message:   'Normal System Startup',
                    category:  CATEGORY.SYSTEM,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('FT_INDEX_UPDATE_START' == token) {
            result = new SystemEvent(
                    source:    'DataloadService',
                    event:     'Updating FTIndex',
                    message:   "Updating FT indexes",
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('FT_INDEX_UPDATE_ERROR' == token) {
            result = new SystemEvent(
                    source: 'DataloadService',
                    event: 'Updating FT Index',
                    message: "Problem while updating FT indexes",
                    category: CATEGORY.OTHER,
                    relevance: RELEVANCE.ERROR
            )
        }
        else if ('FT_INDEX_CLEANUP_ERROR' == token) {
            result = new SystemEvent(
                    source: 'DataloadService',
                    event: 'Deleting FT Index',
                    message: "Problem while deleting index",
                    category: CATEGORY.OTHER,
                    relevance: RELEVANCE.ERROR
            )
        }
        else if ('GSSS_OAI_START' == token) {
            result = new SystemEvent(
                    source: 'GlobalSourceSyncService',
                    event: 'OAI Sync',
                    message: "Processing internalOAI sync",
                    category: CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('GSSS_OAI_ERROR' == token) {
            result =  new SystemEvent(
                    source:    'GlobalSourceSyncService',
                    event:     'OAI Sync',
                    message:   "Problems while processing internalOAI sync",
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.ERROR
            )
        }
        else if ('GSSS_OAI_COMPLETE' == token) {
            result = new SystemEvent(
                    source: 'GlobalSourceSyncService',
                    event: 'OAI Sync',
                    message: "Completed processing internalOAI sync",
                    category: CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('YODA_ES_RESET_START' == token) {
            result = new SystemEvent(
                    source:    'YodaController',
                    event:     'ES Reset',
                    message:   'Started full reset of elasticsearch index',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_JOB_START' == token) {
            result = new SystemEvent(
                    source: 'DashboardDueDatesJob',
                    event: 'Starting',
                    message: 'Starting',
                    category: CATEGORY.CRONJOB,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_JOB_COMPLETE' == token) {
            result = new SystemEvent(
                    source: 'DashboardDueDatesJob',
                    event: 'Finished',
                    message: 'Finished',
                    category: CATEGORY.CRONJOB,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_JOB_IGNORE' == token) {
            result = new SystemEvent(
                    source: 'DashboardDueDatesJob',
                    event: 'Ignored',
                    message: 'isUpdateDashboardTableInDatabase and isSendEmailsForDueDatesOfAllUsers are switched off in grailsApplication.config file',
                    category: CATEGORY.CRONJOB,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('SUB_UPDATE_JOB_START' == token) {
            result = new SystemEvent(
                    source: 'SubscriptionUpdateJob',
                    event: 'Starting Status Updates',
                    message: 'Checking existing subscription status vs start date and end date. Changing status if needed',
                    category: CATEGORY.CRONJOB,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('SUB_UPDATE_JOB_COMPLETE' == token) {
            result = new SystemEvent(
                    source: 'SubscriptionUpdateJob',
                    event: 'Finished Status Updates',
                    message: 'Checking existing subscription status vs start date and end date. Changing status if needed',
                    category: CATEGORY.CRONJOB,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_START_1' == token) {
            result = new SystemEvent(
                    source: 'DashboardDueDatesService',
                    event: 'Starting takeCareOfDueDates',
                    message: 'Starting takeCareOfDueDates',
                    category: CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_COMPLETE_1' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Finished takeCareOfDueDates',
                    message:   'Finished takeCareOfDueDates',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_ERROR_1' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Error while processing takeCareOfDueDates',
                    message:   'Unable to perform email',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.ERROR
            )
        }
        else if ('DBDD_SERVICE_START_2' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Starting updateDashboardTableInDatabase',
                    message:   'Starting updateDashboardTableInDatabase',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_PROCESSING_2' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Starting updateDashboardTableInDatabase',
                    message:   'Inserting entries',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_COMPLETE_2' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Finished updateDashboardTableInDatabase',
                    message:   'Finished updateDashboardTableInDatabase',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_ERROR_2' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Starting updateDashboardTableInDatabase',
                    message:   'Processing rollback',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.ERROR
            )
        }
        else if ('DBDD_SERVICE_START_3' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Starting sendEmailsForDueDatesOfAllUsers',
                    message:   'Starting sendEmailsForDueDatesOfAllUsers',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_COMPLETE_3' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Finished sendEmailsForDueDatesOfAllUsers',
                    message:   'Finished sendEmailsForDueDatesOfAllUsers',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.INFO
            )
        }
        else if ('DBDD_SERVICE_ERROR_3' == token) {
            result = new SystemEvent(
                    source:    'DashboardDueDatesService',
                    event:     'Error while sendEmailsForDueDatesOfAllUsers',
                    message:   'Error while sending emails in sendEmailsForDueDatesOfAllUsers',
                    category:  CATEGORY.OTHER,
                    relevance: RELEVANCE.ERROR
            )
        }

        if (result) {
            result.token = token
            result.payload = payload

            result.save(flush:true)
        }

        result
    }
}
