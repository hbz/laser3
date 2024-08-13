package de.laser.survey

import de.laser.Org
import de.laser.Subscription
import de.laser.wekb.Vendor


class SurveyVendorResult {

    SurveyConfig surveyConfig
    Vendor vendor

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
        id column: 'survenre_id'
        version column: 'survenre_version'

        dateCreated column: 'survenre_date_created'
        lastUpdated column: 'survenre_last_updated'

        owner column: 'survenre_owner_fk', index: 'survenre_owner_idx'
        participant column: 'survenre_participant_fk', index: 'survenre_participant_idx'

        surveyConfig column: 'survenre_survey_config_fk', index: 'survenre_survey_config_idx'
        vendor column: 'survenre_vendor_fk', index: 'survenre_vendor_idx'

        comment column: 'survenre_comment', type: 'text'
        participantComment column: 'survenre_participant_comment', type: 'text'
        ownerComment column: 'survenre_owner_comment', type: 'text'

    }

    /**
     * Retrieves the subscription linked to this survey and the participant
     * @return the {@link de.laser.Subscription} object
     */
    Subscription getParticipantSubscription(){
        Subscription subscription
        if (surveyConfig.subscription){
            subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                    [parentSub  : surveyConfig.subscription,
                     participant: participant
                    ])[0]
        }

        return subscription
    }
}
