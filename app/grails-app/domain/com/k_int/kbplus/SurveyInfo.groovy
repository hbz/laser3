package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation

class SurveyInfo {

    String name
    Date startDate
    Date endDate
    String comment

    Org owner

    @RefdataAnnotation(cat = 'Survey Type')
    RefdataValue type

    @RefdataAnnotation(cat = 'Survey Status')
    RefdataValue status

    Date dateCreated
    Date lastUpdated

    List surveyConfigs

    static hasMany = [
            surveyConfigs: SurveyConfig
    ]

    static constraints = {
        endDate (nullable:true, blank:false)
        surveyConfigs (nullable:true, blank:false)
        comment (nullable:true, blank:true)

    }

    static mapping = {
        id column: 'surin_id'
        version column: 'surin_version'

        name column: 'surin_name'
        startDate column: 'surin_startDate'
        endDate column: 'surin_endDate'
        comment column: 'surin_comment', type: 'text'

        dateCreated column: 'surin_dateCreated'
        lastUpdated column: 'surin_lastUpdated'

        owner column: 'surin_owner_org_fk'
        type column: 'surin_type_rv_fk '



    }
}
