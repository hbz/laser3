package com.k_int.kbplus

class SurveyOrg {


    SurveyConfig surveyConfig
    Org org

    String priceComment
    Date dateCreated
    Date lastUpdated


    static constraints = {
        priceComment (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surorg_id'
        version column: 'surorg_version'

        surveyConfig column: 'surorg_surveyConfig_fk'
        org column: 'surorg_org_fk'
        priceComment column: 'surorg_priceComment', type: 'text'
        dateCreated column: 'surorg_dateCreated'
        lastUpdated column: 'surorg_lastUpdated'
    }
}
