package de.laser.survey

import de.laser.CustomerTypeService
import de.laser.License
import de.laser.Org
import de.laser.wekb.Provider
import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore

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

    String name
    Date startDate
    Date endDate
    String comment

    Org owner

    @RefdataInfo(cat = RDConstants.SURVEY_TYPE)
    RefdataValue type

    @RefdataInfo(cat = RDConstants.SURVEY_STATUS)
    RefdataValue status

    Date dateCreated
    Date lastUpdated

    boolean isSubscriptionSurvey = false
    boolean isMandatory = false
    boolean isRenewalSent = false

    License license

    @Deprecated
    Org providerOld
    Provider provider

    static hasMany = [
            surveyConfigs: SurveyConfig
    ]

    static constraints = {
        startDate (nullable:true)
        endDate (nullable:true)
        surveyConfigs (nullable:true)
        comment (nullable:true, blank:true)
        license (nullable:true)
        provider (nullable:true)
        providerOld (nullable:true)
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

        owner column: 'surin_owner_org_fk',     index: 'surin_owner_org_idx'
        type column: 'surin_type_rv_fk',        index: 'surin_type_idx'
        status column: 'surin_status_rv_fk',    index: 'surin_status_idx'

        isSubscriptionSurvey column: 'surin_is_subscription_survey'
        isMandatory column: 'surin_is_mandatory'
        isRenewalSent column: 'surin_is_renewal_sent'

        license column: 'surin_license'
        providerOld column: 'surin_provider'
        provider column: 'surin_provider_fk'
    }

    /**
     * Used in survey/_actions
     * Checks if this survey is ready to be opened
     * @return true if there are survey configs (i.e. objects in focus of this survey) and if in each configuration, there are survey properties (i.e. questions being asked) (unless it is a pick and choose title survey)
     * and participants in the survey, false otherwise
     */
    boolean checkOpenSurvey() {
        boolean check = this.surveyConfigs.size() > 0

        this.surveyConfigs.each {

            if(it.subscription)
            {
                if(!it.pickAndChoose && SurveyConfigProperties.countBySurveyConfig(it)  == 0) {
                    check = false
                }
            }

            if(!it.subscription)
            {
                if(SurveyConfigProperties.countBySurveyConfig(it)  == 0) {
                    check = false
                }
            }

            if(SurveyOrg.countBySurveyConfig(it) == 0){
                check = false
            }

            if(it.vendorSurvey && (SurveyConfigVendor.countBySurveyConfig(it) == 0)){
                check = false
            }

            if(it.subscriptionSurvey && (SurveyConfigSubscription.countBySurveyConfig(it) == 0)){
                check = false
            }

            if(it.packageSurvey && (SurveyConfigPackage.countBySurveyConfig(it) == 0)){
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

        //print(result)
        //print(count)


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
        if(BeanStore.getContextService().isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO ) && this.owner?.id == BeanStore.getContextService().getOrg().id)
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
        if(this.status in [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED] && this.owner?.id == BeanStore.getContextService().getOrg().id)
        {
            return true
        }else{
            return false
        }
    }
}
