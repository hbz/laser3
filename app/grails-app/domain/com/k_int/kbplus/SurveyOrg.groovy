package com.k_int.kbplus

class SurveyOrg {


    SurveyConfig surveyConfig
    Org org
    CostItem costItem

    String priceComment


    static constraints = {
        priceComment (nullable:true, blank:false)
        costItem (nullable:true, blank:false)
    }

    static mapping = {
        id column: 'surorg_id'
        version column: 'surorg_version'

        surveyConfig column: 'surorg_surveyConfig_fk'
        org column: 'surorg_org_fk'
        costItem column: 'surorg_costItem_fk'
        priceComment column: 'surorg_priceComment', type: 'text'
    }
}
