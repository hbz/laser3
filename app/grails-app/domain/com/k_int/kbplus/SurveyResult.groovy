package com.k_int.kbplus

class SurveyResult {




    Date dateCreated
    Date lastUpdated

    static constraints = {
        id column: 'surRe_id'
        version column: 'surRe_version'

        dateCreated column: 'surRe_dateCreated'
        lastUpdated column: 'surRe_lastUpdated'

    }
}
