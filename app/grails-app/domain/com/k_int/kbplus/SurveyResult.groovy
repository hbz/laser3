package com.k_int.kbplus

class SurveyResult {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        id column: 'surre_id'
        version column: 'surre_version'

        dateCreated column: 'surre_dateCreated'
        lastUpdated column: 'surre_lastUpdated'

    }
}
