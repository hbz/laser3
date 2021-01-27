package de.laser


import de.laser.finance.CostItem
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import org.grails.web.json.JSONElement
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.SimpleDateFormat

class SurveyConfig {

    def deletionService


    public static final ALL_RESULTS_PROCESSED_BY_ORG = "All Processed"
    public static final ALL_RESULTS_NOT_PROCESSED_BY_ORG = "Not Processed"
    public static final ALL_RESULTS_HALF_PROCESSED_BY_ORG = "Half Processed"
    public static final SURVEY_CONFIG_TYPE_SUBSCRIPTION = "Subscription"
    public static final SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT = "IssueEntitlementsSurvey"
    public static final SURVEY_CONFIG_TYPE_GENERAL_SURVEY = "GeneralSurvey"

    Integer configOrder

    Subscription subscription
    PropertyDefinition surveyProperty

    SurveyInfo surveyInfo

    Date scheduledStartDate
    Date scheduledEndDate

    String type
    String header
    String comment
    String internalComment
    String url
    String url2
    String url3
    String urlComment
    String urlComment2
    String urlComment3

    Date dateCreated
    Date lastUpdated

    boolean pickAndChoose
    boolean configFinish
    boolean costItemsFinish
    boolean evaluationFinish
    boolean subSurveyUseForTransfer
    //Nicht mehr nötig?
    boolean createTitleGroups = false

    String transferWorkflow

    static hasMany = [
            documents       : DocContext,
            surveyProperties: SurveyConfigProperties,
            orgs            : SurveyOrg,
            propertySet      : SurveyResult
    ]

    static constraints = {
        subscription        (nullable: true)
        surveyProperty      (nullable: true)

        header(nullable: true, blank: false)
        comment(nullable: true, blank: false)
        documents(nullable: true, blank: false)
        orgs(nullable: true, blank: false)
        scheduledStartDate  (nullable: true)
        scheduledEndDate    (nullable: true)
        internalComment(nullable: true, blank: false)
        url(nullable: true, blank: false, maxSize:512)
        url2(nullable: true, blank: false, maxSize:512)
        url3(nullable: true, blank: false, maxSize:512)
        urlComment(nullable: true, blank: false)
        urlComment2(nullable: true, blank: false)
        urlComment3(nullable: true, blank: false)
        propertySet(nullable: true, blank: false)
        transferWorkflow (nullable: true, blank: false)
    }

    static transients = [
            'currentDocs', 'configNameShort', 'surveyName', 'configName',
            'typeInLocaleI10n', 'surveyOrgsIDs', 'configNavigation', 'transferWorkflowAsJSON'
    ] // mark read-only accessor methods

    static mapping = {
        id column: 'surconf_id'
        version column: 'surconf_version'

        type column: 'surconf_type'
        header column: 'surconf_header'
        comment column: 'surconf_comment', type: 'text'
        internalComment column: 'surconf_internal_comment', type: 'text'
        url column: 'surconf_url'
        url2 column: 'surconf_url_2'
        url3 column: 'surconf_url_3'
        urlComment column: 'surconf_url_comment', type: 'text'
        urlComment2 column: 'surconf_url_comment_2', type: 'text'
        urlComment3 column: 'surconf_url_comment_3', type: 'text'
        pickAndChoose column: 'surconf_pickandchoose'
        createTitleGroups column: 'surconf_create_title_groups'
        configFinish column: 'surconf_config_finish'
        costItemsFinish column: 'surconf_costitems_finish'
        evaluationFinish column: 'surconf_evaluation_finish'
        subSurveyUseForTransfer column: 'surconf_is_subscription_survey_fix'

        scheduledStartDate column: 'surconf_scheduled_startdate'
        scheduledEndDate column: 'surconf_scheduled_enddate'

        dateCreated column: 'surconf_date_created'
        lastUpdated column: 'surconf_last_updated'

        surveyInfo column: 'surconf_surinfo_fk'
        subscription column: 'surconf_sub_fk'
        surveyProperty column: 'surconf_surprop_fk'

        configOrder column: 'surconf_config_order'

        transferWorkflow column: 'surconf_transfer_workflow', type:'text'
    }

    def afterDelete() {
        deletionService.deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id)
    }

    @Transient
    static def validTypes = [
            'Subscription'  : ['de': 'Lizenz', 'en': 'Subscription'],
            'SurveyProperty': ['de': 'Umfrage-Merkmal', 'en': 'Survey-Property']
    ]

    static getLocalizedValue(key) {
        String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())

        //println locale
        if (SurveyConfig.validTypes.containsKey(key)) {
            return (SurveyConfig.validTypes.get(key)."${locale}") ?: SurveyConfig.validTypes.get(key)
        } else {
            return null
        }
    }

    def getCurrentDocs() {

        return documents.findAll { (it.status?.value != 'Deleted' && (it.owner?.contentType == Doc.CONTENT_TYPE_FILE))}
    }

    String getConfigNameShort() {

        if (type == 'Subscription') {
            return subscription?.name
        } else {
            return surveyInfo.name
        }
    }

    String getSurveyName() {

        if (type == 'Subscription' && surveyInfo.isSubscriptionSurvey) {
            return subscription?.name
        } else {
            return surveyInfo.name
        }
    }

    String getConfigName() {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        if (type == 'Subscription') {
            return subscription?.name + ' - ' + subscription?.status?.getI10n('value') + ' ' +
                    (subscription?.startDate ? '(' : '') + sdf.format(subscription?.startDate) +
                    (subscription?.endDate ? ' - ' : '') + sdf.format(subscription?.endDate) +
                    (subscription?.startDate ? ')' : '')

        } else {
            return surveyInfo.name
        }
    }

    def getTypeInLocaleI10n() {

        return this.getLocalizedValue(this?.type)
    }


    Map<String, Object> getSurveyOrgsIDs() {
        Map<String, Object> result = [:]

        result.orgsWithoutSubIDs = this.orgs?.org?.id?.minus(this?.subscription?.getDerivedSubscribers()?.id) ?: null

        result.orgsWithSubIDs = this.orgs.org.id.minus(result.orgsWithoutSubIDs) ?: null

        return result
    }

    //Überprüft nur ob bearbeitet ist oder nicht, aber nicht ob abgeschickt wurde
    def checkResultsEditByOrg(Org org) {

        if (this.subSurveyUseForTransfer && SurveyOrg.findBySurveyConfigAndOrg(this, org)?.existsMultiYearTerm()) {
            return ALL_RESULTS_PROCESSED_BY_ORG
        } else {

            int countFinish = 0
            int countNotFinish = 0

            List<SurveyResult> surveyResult = SurveyResult.findAllBySurveyConfigAndParticipant(this, org)

                surveyResult.each {
                    if (it.isResultProcessed()) {
                        countFinish++
                    } else {
                        countNotFinish++
                    }
                }
                if (countFinish > 0 && countNotFinish == 0) {
                    return ALL_RESULTS_PROCESSED_BY_ORG
                } else if (countFinish > 0 && countNotFinish > 0) {
                    return ALL_RESULTS_HALF_PROCESSED_BY_ORG
                } else {
                    return ALL_RESULTS_NOT_PROCESSED_BY_ORG
                }
        }


    }

    boolean isResultsSetFinishByOrg(Org org) {

        SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(this, org)

        if (this.subSurveyUseForTransfer && surveyOrg && surveyOrg.existsMultiYearTerm()) {
            return true
        } else {

            int countFinish = 0
            int countNotFinish = 0

            boolean noParticipation = false
            if(subSurveyUseForTransfer) {
                noParticipation = (SurveyResult.findByParticipantAndSurveyConfigAndType(org, this, RDStore.SURVEY_PROPERTY_PARTICIPATION).refValue == RDStore.YN_NO)
            }

                /*if (!noParticipation) {*/

                    List surveyResults = SurveyResult.findAllBySurveyConfigAndParticipant(this, org)

                    if (pickAndChoose) {
                        boolean finish = false

                        if (surveyResults) {
                            finish = (surveyOrg.finishDate && surveyResults.finishDate.contains(null)) || surveyOrg.finishDate
                        } else {
                            finish = surveyOrg.finishDate ? true : false
                        }
                        return finish
                    }else{
                        if (surveyResults?.finishDate.contains(null)) {
                            return false
                        } else {
                            return true
                        }
                    }
                /*} else {
                    return true
                }*/
        }


    }

    def nextConfig()
    {
        def next

        for(int i = 0 ; i < this.surveyInfo.surveyConfigs?.size() ; i++ ) {
            def curr = this.surveyInfo.surveyConfigs[ i ]

            if(curr?.id == this.id)
            {
                next = i < this.surveyInfo.surveyConfigs.size() - 1 ? this.surveyInfo.surveyConfigs[ i + 1 ] : this
            }
        }
        return (next?.id == this?.id) ? null : next
    }

    def prevConfig()
    {
        def prev
        this.surveyInfo.surveyConfigs.sort {it.configOrder}.reverse(true).each { config ->
            if(prev)
            {
                prev = this
            }

            if(config.id == this.id)
            {
                prev = this
            }
        }
        return (prev?.id == this?.id) ? null : prev
    }

    Map<String, Object> getConfigNavigation(){

        Map<String, Object> result = [:]
        result.prev = prevConfig()
        result.next = nextConfig()
        result.total = this.surveyInfo.surveyConfigs?.size() ?: null

        if(!result.total && result.total < 1 && !result.prev && !result.next)
        {
            result = null
        }
        return result
    }

    String toString() {
        subscription ? "${subscription?.name}" : "Survey Element ${id}"
    }


    boolean showUIShareButton() {
        return false
    }

    List<CostItem> getSurveyConfigCostItems(){

        return this.orgs ? CostItem.findAllBySurveyOrgInListAndCostItemStatusNotEqual(this.orgs, RDStore.COST_ITEM_DELETED) : []
    }

    JSONElement getTransferWorkflowAsJSON() {
        transferWorkflow ? grails.converters.JSON.parse(transferWorkflow) : grails.converters.JSON.parse('{}')
    }

    Map countParticipants(){
        Map result = [:]

        result.surveyMembers = orgs.size()

        if(surveyInfo.isSubscriptionSurvey){
            List subChilds = subscription.getNonDeletedDerivedSubscriptions()

            result.subMembers = subChilds.size()

            Integer subMembersWithMultiYear = 0
            subChilds.each {
                if(it.isCurrentMultiYearSubscriptionNew())
                {
                    subMembersWithMultiYear++
                }
            }

            result.subMembersWithMultiYear = subMembersWithMultiYear
        }
        result
    }

    String dropdownNamingConvention() {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        String period = surveyInfo.startDate ? sdf.format(surveyInfo.startDate)  : ''

        period = surveyInfo.endDate ? period + ' - ' + sdf.format(surveyInfo.endDate)  : ''

        period = period ? '('+period+')' : ''

        String statusString = surveyInfo.status ? surveyInfo.status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        return surveyInfo.name + ' - ' + statusString + ' ' +period + ' ' + surveyInfo.type.getI10n('value')
    }



}
