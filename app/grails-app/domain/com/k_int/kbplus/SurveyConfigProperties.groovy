package com.k_int.kbplus

class SurveyConfigProperties {


    SurveyConfig surveyConfig
    SurveyProperty surveyProperty

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }
    static mapping = {
        id column: 'surconpro_id'
        version column: 'surconpro_version'

        dateCreated column: 'surconpro_dateCreated'
        lastUpdated column: 'surconpro_lastUpdated'

        surveyConfig column: 'surconpro_survey_config_fk'
        surveyProperty column: 'surconpro_survey_property_fk'
    }
}
