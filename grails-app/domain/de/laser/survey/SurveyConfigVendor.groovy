package de.laser.survey

import de.laser.wekb.Vendor

class SurveyConfigVendor {

    static belongsTo = [
            surveyConfig:            SurveyConfig,
            vendor:   Vendor
    ]

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }

    static mapping = {
        id column: 'surconven_id'
        version column: 'surconven_version'

        dateCreated column: 'surconven_date_created'
        lastUpdated column: 'surconven_last_updated'

        surveyConfig column: 'surconven_survey_config_fk', index: 'surconven_survey_config_idx'
        vendor column: 'surconven_vendor_fk', index: 'surconven_vendor_idx'

    }
}
