package de.laser

class SurveyLinks {

    SurveyInfo sourceSurvey
    SurveyInfo targetSurvey

    Date dateCreated
    Date lastUpdated

    static constraints = {
    }

    static mapping = {
        id column: 'surlin_id'
        version column: 'surlin_version'

        sourceSurvey column: 'surlin_source_survey'
        targetSurvey column: 'surlin_target_survey'

        dateCreated column: 'surlin_date_created'
        lastUpdated column: 'surlin_last_updated'
    }
}
