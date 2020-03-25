package com.k_int.kbplus

import com.k_int.properties.PropertyDefinition
import de.laser.domain.I10nTranslation
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import grails.util.Holders
import org.codehaus.groovy.grails.web.json.JSONElement
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.SimpleDateFormat

class SurveyConfig {

    @Transient
    def deletionService

    @Transient
    public static final ALL_RESULTS_PROCESSED_BY_ORG = "All Processed"

    @Transient
    public static final ALL_RESULTS_NOT_PROCESSED_BY_ORG = "Not Processed"

    @Transient
    public static final ALL_RESULTS_HALF_PROCESSED_BY_ORG = "Half Processed"

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

    Date dateCreated
    Date lastUpdated

    boolean pickAndChoose
    boolean configFinish
    boolean costItemsFinish
    boolean evaluationFinish
    boolean subSurveyUseForTransfer

    String transferWorkflow

    static hasMany = [
            documents       : DocContext,
            surveyProperties: SurveyConfigProperties,
            orgs            : SurveyOrg,
            surResults      : SurveyResult
    ]

    static constraints = {
        subscription(nullable: true, blank: false)
        surveyProperty(nullable: true, blank: false)

        header(nullable: true, blank: false)
        comment(nullable: true, blank: false)
        pickAndChoose(nullable: true, blank: false)
        documents(nullable: true, blank: false)
        orgs(nullable: true, blank: false)
        configFinish(nullable: true, blank: false)
        costItemsFinish (nullable: true, blank: false)
        scheduledStartDate (nullable: true, blank: false)
        scheduledEndDate (nullable: true, blank: false)
        internalComment(nullable: true, blank: false)
        url(nullable: true, blank: false, maxSize:512)
        evaluationFinish (nullable: true, blank: false)
        subSurveyUseForTransfer (nullable: true, blank: false)
        surResults(nullable: true, blank: false)
        transferWorkflow (nullable: true, blank: false)

    }

    static mapping = {
        id column: 'surconf_id'
        version column: 'surconf_version'

        type column: 'surconf_type'
        header column: 'surconf_header'
        comment column: 'surconf_comment', type: 'text'
        internalComment column: 'surconf_internal_comment', type: 'text'
        url column: 'surconf_url'
        pickAndChoose column: 'surconf_pickandchoose'
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
        String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale().toString())

        //println locale
        if (SurveyConfig.validTypes.containsKey(key)) {
            return (SurveyConfig.validTypes.get(key)."${locale}") ?: SurveyConfig.validTypes.get(key)
        } else {
            return null
        }
    }

    def getCurrentDocs() {

        return documents.findAll { (it.status?.value != 'Deleted' && ((it.owner?.contentType == 1) || (it.owner?.contentType == 3)))}
    }

    String getConfigNameShort() {

        if (type == 'Subscription') {
            return subscription?.name
        } else {
            return surveyInfo?.name
        }
    }

    String getSurveyName() {

        if (type == 'Subscription' && surveyInfo.isSubscriptionSurvey) {
            return subscription?.name
        } else {
            return surveyInfo?.name
        }
    }

    String getConfigName() {

        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()

        if (type == 'Subscription') {
            return subscription?.name + ' - ' + subscription?.status?.getI10n('value') + ' ' +
                    (subscription?.startDate ? '(' : '') + sdf.format(subscription?.startDate) +
                    (subscription?.endDate ? ' - ' : '') + sdf.format(subscription?.endDate) +
                    (subscription?.startDate ? ')' : '')

        } else {
            return surveyInfo?.name
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

        if (SurveyOrg.findBySurveyConfigAndOrg(this, org)?.existsMultiYearTerm()) {
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

        if (SurveyOrg.findBySurveyConfigAndOrg(this, org)?.existsMultiYearTerm()) {
            return true
        } else {

            int countFinish = 0
            int countNotFinish = 0

            def surveyResults = SurveyResult.findAllBySurveyConfigAndParticipant(this, org)

            if(surveyResults?.finishDate.contains(null)){
                return false
            }else {
                return true
            }
        }


    }

    def nextConfig()
    {
        def next

        for(int i = 0 ; i < this.surveyInfo?.surveyConfigs?.size() ; i++ ) {
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

        return CostItem.findAllBySurveyOrgInListAndCostItemStatusNotEqual(this.orgs, RDStore.COST_ITEM_DELETED)

    }

    JSONElement getTransferWorkflowAsJSON() {
        transferWorkflow ? grails.converters.JSON.parse(transferWorkflow) : grails.converters.JSON.parse('{}')
    }

    Map countParticipants(){
        Map result = [:]

        result.surveyMembers = orgs.size()

        if(surveyInfo.isSubscriptionSurvey){
            List subChilds = Subscription.findAllByInstanceOf(subscription)

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



}
