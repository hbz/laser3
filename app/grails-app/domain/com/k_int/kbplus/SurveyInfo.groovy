package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class SurveyInfo {

    String name
    Date startDate
    Date endDate

    Org owner

    @RefdataAnnotation(cat = 'Survey Type')
    RefdataValue type

    Date dateCreated
    Date lastUpdated

    static hasMany = [
            surveyConfig: SurveyConfig
    ]

    static constraints = {
        endDate (nullable:true, blank:false)
        surveyConfig (nullable:true, blank:false)

    }

    static mapping = {
        id column: 'surIn_id'
        version column: 'surIn_version'

        name column: 'surIn_name'
        startDate column: 'surIn_startDate'
        endDate column: 'surIn_endDate'
        dateCreated column: 'surIn_dateCreated'
        lastUpdated column: 'surIn_lastUpdated'

        owner column: 'surIn_owner_org_fk'
        type column: 'surIn_type_rv_fk '


    }
}
