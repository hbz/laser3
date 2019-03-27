package com.k_int.kbplus

class SurveyResult {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        id column: 'surre_id'
        version column: 'surre_version'

        dateCreated column: 'surre_date_created'
        lastUpdated column: 'surre_last_updated'

    }
}
