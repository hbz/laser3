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

        surveyConfig column: 'surorg_surveyconfig_fk'
        org column: 'surorg_org_fk'
        priceComment column: 'surorg_pricecomment', type: 'text'
        dateCreated column: 'surorg_date_created'
        lastUpdated column: 'surorg_last_updated'
    }
}
