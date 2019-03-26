package com.k_int.kbplus

class SurveyConfigProperties {


    SurveyConfig surveyConfig
    SurveyProperty surveyProperty

    Date dateCreated
    Date lastUpdated

    static constraints = {

    }
    static mapping = {
        id column: 'surConPro_id'
        version column: 'surConPro_version'

        dateCreated column: 'surConPro_dateCreated'
        lastUpdated column: 'surConPro_lastUpdated'

        surveyConfig column: 'surConPro_surveyConfig'
        surveyProperty column: 'surConPro_surveyProperty'
    }
}
