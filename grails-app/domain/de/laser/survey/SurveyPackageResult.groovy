package de.laser.survey

import de.laser.Org
import de.laser.wekb.Package
import de.laser.Subscription


class SurveyPackageResult {

    SurveyConfig surveyConfig
    Package pkg

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
        id column: 'surpkgre_id'
        version column: 'surpkgre_version'

        dateCreated column: 'surpkgre_date_created'
        lastUpdated column: 'surpkgre_last_updated'

        owner column: 'surpkgre_owner_fk', index: 'surpkgre_owner_idx'
        participant column: 'surpkgre_participant_fk', index: 'surpkgre_participant_idx'

        surveyConfig column: 'surpkgre_survey_config_fk', index: 'surpkgre_survey_config_idx'
        pkg column: 'surpkgre_pkg_fk', index: 'surpkgre_pkg_idx'

        comment column: 'surpkgre_comment', type: 'text'
        participantComment column: 'surpkgre_participant_comment', type: 'text'
        ownerComment column: 'surpkgre_owner_comment', type: 'text'

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
