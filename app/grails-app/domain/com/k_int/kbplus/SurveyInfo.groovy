package com.k_int.kbplus


import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation

import javax.persistence.Transient

class SurveyInfo {

    @Transient
    def contextService
    @Transient
    def accessService

    String name
    Date startDate
    Date endDate
    String comment

    Org owner

    @RefdataAnnotation(cat = RDConstants.SURVEY_TYPE)
    RefdataValue type

    @RefdataAnnotation(cat = RDConstants.SURVEY_STATUS)
    RefdataValue status

    Date dateCreated
    Date lastUpdated

    boolean isSubscriptionSurvey = false
    boolean isMandatory = false

    static hasMany = [
            surveyConfigs: SurveyConfig
    ]

    static constraints = {
        startDate (nullable:true, blank:false)
        endDate (nullable:true, blank:false)
        surveyConfigs (nullable:true, blank:false)
        comment (nullable:true, blank:true)
        isSubscriptionSurvey  (nullable:false, blank:false)
        isMandatory           (nullable:false, blank:false)

    }

    static mapping = {
        id column: 'surin_id'
        version column: 'surin_version'

        name column: 'surin_name'
        startDate column: 'surin_start_date'
        endDate column: 'surin_end_date'
        comment column: 'surin_comment', type: 'text'

        dateCreated column: 'surin_date_created'
        lastUpdated column: 'surin_last_updated'

        owner column: 'surin_owner_org_fk'
        type column: 'surin_type_rv_fk'
        status column: 'surin_status_rv_fk'

        isSubscriptionSurvey column: 'surin_is_subscription_survey'
        isMandatory column: 'surin_is_mandatory'
    }


    boolean checkOpenSurvey()
    {
        boolean check = this.surveyConfigs?.size() > 0 ? true : false

        this.surveyConfigs?.each {

            if(it?.subscription)
            {
                if(!it.pickAndChoose && !(it?.surveyProperties?.size() > 0)) {
                    check = false
                }
            }

            if(!(it?.orgs.org?.size > 0)){
                check = false
            }
        }

        return check
    }

    def checkSurveyInfoFinishByOrg(Org org) {
        Map<String, Object> result = [:]

        int count = 0
        surveyConfigs.each {

            def checkResultsEditByOrg = result."${it.checkResultsEditByOrg(org)}"
            if(checkResultsEditByOrg){

                result."${it.checkResultsEditByOrg(org)}" = checkResultsEditByOrg+1


            }else {
                result."${it.checkResultsEditByOrg(org)}" = 1
            }
            count++
        }

        result.sort{it.value}

        print(result)
        print(count)


        result = result?.find{it.value == count} ? result?.find{it.value == count}.key : null

        if(result)
        {
            println(result)
            return result
        }else {
            return null
        }
    }
    boolean isEditable() {
        if(accessService.checkPermAffiliationX('ORG_CONSORTIUM_SURVEY','INST_EDITOR','ROLE_ADMIN') && this.owner?.id == contextService.getOrg()?.id)
        {
            return true
        }

        return false
    }

    boolean isCompletedforOwner() {
        if(this.status in [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED] && this.owner?.id == contextService.getOrg()?.id)
        {
            return true
        }else{
            return false
        }
    }
}
