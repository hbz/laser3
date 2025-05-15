package de.laser.survey

import de.laser.Subscription

class SurveyConfigSubscription {

    static belongsTo = [
            surveyConfig:   SurveyConfig,
            subscription:   Subscription
    ]

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }

    static mapping = {
        id column: 'surconsub_id'
        version column: 'surconsub_version'

        dateCreated column: 'surconsub_date_created'
        lastUpdated column: 'surconsub_last_updated'

        surveyConfig column: 'surconsub_survey_config_fk', index: 'surconsub_survey_config_idx'
        subscription column: 'surconsub_subscription_fk', index: 'surconsub_subscription_idx'

    }
}
