package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition

class SurveyConfigProperties {


    SurveyConfig surveyConfig
    PropertyDefinition surveyProperty

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }
    static mapping = {
        id column: 'surconpro_id'
        version column: 'surconpro_version'

        dateCreated column: 'surconpro_date_created'
        lastUpdated column: 'surconpro_last_updated'

        surveyConfig column: 'surconpro_survey_config_fk'
        surveyProperty column: 'surconpro_survey_property_fk'
    }
}
