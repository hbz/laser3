package de.laser.survey

import de.laser.Org
import de.laser.Subscription

class SurveySubscriptionResult {

    SurveyConfig surveyConfig
    Subscription subscription

    Org owner
    Org participant

    String comment
    String participantComment
    String ownerComment

    Date dateCreated
    Date lastUpdated

    static constraints = {
        comment (nullable:true, blank:false)
        participantComment (nullable:true, blank:false)
        ownerComment (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'sursubre_id'
        version column: 'sursubre_version'

        dateCreated column: 'sursubre_date_created'
        lastUpdated column: 'sursubre_last_updated'

        owner column: 'sursubre_owner_fk', index: 'sursubre_owner_idx'
        participant column: 'sursubre_participant_fk', index: 'sursubre_participant_idx'

        surveyConfig column: 'sursubre_survey_config_fk', index: 'sursubre_survey_config_idx'
        subscription column: 'sursubre_subscription_fk', index: 'sursubre_subscription_idx'

        comment column: 'sursubre_comment', type: 'text'
        participantComment column: 'sursubre_participant_comment', type: 'text'
        ownerComment column: 'sursubre_owner_comment', type: 'text'

    }
}
