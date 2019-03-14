package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition

class SurveyConfig {


    Integer order
    String question

    Subscription subscription

    SurveyInfo surveyInfo

    Date dateCreated
    Date lastUpdated


    static hasMany = [
            docs: Doc,
            properties: SurveyConfigProperties
    ]

    static constraints = {
        docs (nullable:true, blank:false)
        subscription (nullable:true, blank:false)
        question (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surConf_id'
        version column: 'surConf_version'


        dateCreated column: 'surConf_dateCreated'
        lastUpdated column: 'surConf_lastUpdated'

        surveyInfo column: 'surConf_surInfo_fk'


    }
}
