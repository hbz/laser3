package de.laser.survey

import de.laser.Org
import de.laser.addressbook.Person

class SurveyPersonResult {

    SurveyConfig surveyConfig
    Person person

    Org owner
    Org participant

    boolean surveyPerson = false
    boolean billingPerson = false

    Date dateCreated
    Date lastUpdated

    static constraints = {
    }

    static mapping = {
        id column: 'surprere_id'
        version column: 'surprere_version'

        dateCreated column: 'surprere_date_created'
        lastUpdated column: 'surprere_last_updated'

        owner column: 'surprere_owner_fk', index: 'surprere_owner_idx'
        participant column: 'surprere_participant_fk', index: 'surprere_participant_idx'

        surveyConfig column: 'surprere_survey_config_fk', index: 'surprere_survey_config_idx'
        person column: 'surprere_person_fk', index: 'surprere_person_idx'

        surveyPerson column: 'surprere_survey_person'
        billingPerson column: 'surprere_billing_person'

    }

}
