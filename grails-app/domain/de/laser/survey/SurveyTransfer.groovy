package de.laser.survey

import de.laser.Org
import de.laser.Subscription

class SurveyTransfer {

    SurveyConfig surveyConfig
    Org org
    Subscription subscription

    Date dateCreated
    Date lastUpdated

    Date transferDate

    static constraints = {
    }

    static mapping = {
        id column: 'surtrans_id'
        version column: 'surtrans_version'

        surveyConfig column: 'surtrans_surveyconfig_fk', index: 'surtrans_surveyconfig_idx'
        org column: 'surtrans_org_fk', index: 'surtrans_org_idx'
        subscription column: 'surtrans_subscription_fk', index: 'surtrans_subscription_idx'
        dateCreated column: 'surtrans_date_created'
        lastUpdated column: 'surtrans_last_updated'
        transferDate  column: 'surtrans_transfer_date'


    }

}
