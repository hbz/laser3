package de.laser.survey

/**
 * This domain class links two surveys with each other. A link may be uni- or bidirectional
 * @see SurveyInfo
 */
class SurveyLinks {

    SurveyInfo sourceSurvey
    SurveyInfo targetSurvey

    Date dateCreated
    Date lastUpdated

    boolean bothDirection = false

    static constraints = {
    }

    static mapping = {
        id column: 'surlin_id'
        version column: 'surlin_version'

        sourceSurvey column: 'surlin_source_survey'
        targetSurvey column: 'surlin_target_survey'

        dateCreated column: 'surlin_date_created'
        lastUpdated column: 'surlin_last_updated'

        bothDirection column: 'surlin_both_direction'
    }
}
