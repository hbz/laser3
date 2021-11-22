package de.laser

import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation

/**
 * This domain defines the general attributes of a survey and completes the {@link SurveyConfig} definition which in turn connects the survey to its target.
 * Here, general attributes of the survey are being defined (survey information!) which are technically independent from the object being in the focus. This
 * class holds also the information whether it is a subscription survey (this enables further, specific functionality) or if there is any kind of reply mandatory or
 * if the renewal has been sent already. The survey information follows a generalistical approach; although not used in production for LAS:eR, it is possible to link
 * multiple targets to the same survey (i.e. several subscriptions with the same survey information); each subscription is then connected by an independent {@link SurveyConfig}.
 * The participants of a survey are tracked for each surveyed target individually; that is why the {@link SurveyOrg} connector class points from {@link SurveyConfig}
 * to the participant institution (of {@link Org} type) and not from here
 * @see SurveyConfig
 * @see SurveyOrg
 */
class SurveyInfo {

    def contextService
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
    boolean isRenewalSent = false

    static hasMany = [
            surveyConfigs: SurveyConfig
    ]

    static constraints = {
        startDate (nullable:true)
        endDate (nullable:true)
        surveyConfigs (nullable:true, blank:false)
        comment (nullable:true, blank:true)
        isRenewalSent (nullable:true)
    }

    static transients = ['editable', 'completedforOwner'] // mark read-only accessor methods

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
        isRenewalSent column: 'surin_is_renewal_sent'
    }

    /**
     * Used in survey/_actions
     * Checks if this survey is ready to be opened
     * @return true if there are survey configs (i.e. objects in focus of this survey) and if in each configuration, there are survey properties (i.e. questions being asked) (unless it is a pick and choose title survey)
     * and participants in the survey, false otherwise
     */
    boolean checkOpenSurvey() {
        boolean check = this.surveyConfigs.size() > 0 ? true : false

        this.surveyConfigs.each {

            if(it.subscription)
            {
                if(!it.pickAndChoose && !(it.surveyProperties.size() > 0)) {
                    check = false
                }
            }

            if(!it.subscription)
            {
                if(!(it.surveyProperties.size() > 0)) {
                    check = false
                }
            }

            if(!(it.orgs.org.size > 0)){
                check = false
            }
        }

        return check
    }

    @Deprecated
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
            //println(result)
            return result
        }else {
            return null
        }
    }

    /**
     * Checks edit permissions for this survey
     * @return true if the user belongs to the institution which created (= owns) this survey and if it is at least an editor or general admin, false otherwise
     */
    boolean isEditable() {
        if(accessService.checkPermAffiliationX('ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN') && this.owner?.id == contextService.getOrg().id)
        {
            return true
        }

        return false
    }

    /**
     * Checks if this survey has been completed
     * @return true if the survey status is in one of {@link RDStore#SURVEY_SURVEY_COMPLETED}, {@link RDStore#SURVEY_IN_EVALUATION} or {@link RDStore#SURVEY_COMPLETED}
     * and the viewing user belongs to the survey tenant institution, false otherwise
     */
    boolean isCompletedforOwner() {
        if(this.status in [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED] && this.owner?.id == contextService.getOrg().id)
        {
            return true
        }else{
            return false
        }
    }
}
