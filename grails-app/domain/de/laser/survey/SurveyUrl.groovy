package de.laser.survey

/**
 * A URL to a survey, providing further information, which can be commented with remarks
 * @see SurveyConfig
 */
class SurveyUrl {

    String url
    String urlComment

    Date dateCreated
    Date lastUpdated

    static hasOne = [
            surveyConfig: SurveyConfig
    ]

    static constraints = {
        url(nullable: true, blank: false, maxSize:512)
        urlComment(nullable: true, blank: false)
        surveyConfig(nullable: true)
    }

    static mapping = {
        id column: 'surur_id'
        version column: 'surur_version'

        url column: 'surur_url'
        urlComment column: 'surur_url_comment', type: 'text'

        dateCreated column: 'surur_date_created'
        lastUpdated column: 'surur_last_updated'

        surveyConfig column: 'surur_survey_config_fk'
    }
}
