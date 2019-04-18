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
        startDate column: 'surin_start_date'
        endDate column: 'surin_end_date'
        comment column: 'surin_comment', type: 'text'

        dateCreated column: 'surin_date_created'
        lastUpdated column: 'surin_last_updated'

        owner column: 'surin_owner_org_fk'
        type column: 'surin_type_rv_fk'



    }
}
