package de.laser.survey

import de.laser.Doc
import de.laser.DocContext
import de.laser.IssueEntitlementGroup
import de.laser.Org
import de.laser.Subscription
import de.laser.finance.CostItem
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import org.grails.web.json.JSONElement

import javax.persistence.Transient
import java.text.SimpleDateFormat

/**
 * A survey is (currently) used by consortia to ask certain subscription-related details among institutions. Those
 * institutions may but do not need necessarily linked to the consortium; it may also concern newcomers to a consortial subscription.
 * The main use cases of a survey are to ask for a subscription who wishes to continue the subscription under the given circumstances (which
 * are specified in {@link SurveyInfo}) and who wishes to join a new consortium.
 * This class represents a configuration between a survey and the object in focus of a survey. The object may be one of:
 * <ul>
 *     <li>entitlement survey</li>
 *     <li>general survey (also used for the interest of a new subscription in case of requests)</li>
 *     <li>subscription survey</li>
 * </ul>
 * whilst a subscription survey may also control the renewal of a survey. The survey type is represented by the constants listed below,
 * just as the state of the subscription completion; latter is an indicator for the consortium performing the survey.
 * The participants of a survey are tracked for each surveyed target individually; that is why the {@link SurveyOrg} connector class points from here
 * to the participant institution (of {@link Org} type) and not {@link SurveyInfo}
 * @see SurveyInfo
 * @see SurveyResult
 * @see SurveyOrg
 */
class SurveyConfig {

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
    String commentForNewParticipants
    String internalComment

    Date dateCreated
    Date lastUpdated

    boolean pickAndChoose
    boolean configFinish
    boolean costItemsFinish
    boolean evaluationFinish
    boolean subSurveyUseForTransfer


    boolean pickAndChoosePerpetualAccess = false

    String issueEntitlementGroupName

    String transferWorkflow

    static hasMany = [
            documents       : DocContext,
            surveyProperties: SurveyConfigProperties,
            orgs            : SurveyOrg,
            propertySet      : SurveyResult,
            surveyUrls         : SurveyUrl
    ]

    static constraints = {
        subscription        (nullable: true)
        surveyProperty      (nullable: true)

        issueEntitlementGroupName (nullable: true, blank: false)

        header(nullable: true, blank: false)
        comment(nullable: true, blank: true)
        commentForNewParticipants(nullable: true, blank: true)
        documents   (nullable: true)
        orgs        (nullable: true)
        scheduledStartDate  (nullable: true)
        scheduledEndDate    (nullable: true)
        internalComment(nullable: true, blank: true)
        propertySet (nullable: true)
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
        commentForNewParticipants column: 'surconf_comment_for_new_participants', type: 'text'
        internalComment column: 'surconf_internal_comment', type: 'text'
        pickAndChoose column: 'surconf_pickandchoose'
        configFinish column: 'surconf_config_finish'
        costItemsFinish column: 'surconf_costitems_finish'
        evaluationFinish column: 'surconf_evaluation_finish'
        subSurveyUseForTransfer column: 'surconf_is_subscription_survey_fix'

        pickAndChoosePerpetualAccess column: 'surconf_pac_perpetualaccess'

        scheduledStartDate column: 'surconf_scheduled_startdate'
        scheduledEndDate column: 'surconf_scheduled_enddate'

        dateCreated column: 'surconf_date_created'
        lastUpdated column: 'surconf_last_updated'

        surveyInfo column: 'surconf_surinfo_fk'
        subscription column: 'surconf_sub_fk'
        surveyProperty column: 'surconf_surprop_fk'

        configOrder column: 'surconf_config_order'

        transferWorkflow column: 'surconf_transfer_workflow', type:'text'

        issueEntitlementGroupName column: 'surconf_ie_group_name'
    }

    def afterDelete() {
        BeanStore.getDeletionService().deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
    }

    @Transient
    static def validTypes = [
            'Subscription'  : ['de': 'Lizenz', 'en': 'Subscription'],
            'SurveyProperty': ['de': 'Umfrage-Merkmal', 'en': 'Survey-Property']
    ]

    /**
     * Gets the localised value for a given survey type key
     * @param key the key to fetch its translation
     * @return the translation for the given survey type
     */
    static getLocalizedValue(key) {
        String lang = LocaleUtils.getCurrentLang()

        //println locale
        if (SurveyConfig.validTypes.containsKey(key)) {
            return (SurveyConfig.validTypes.get(key)."${lang}") ?: SurveyConfig.validTypes.get(key)
        } else {
            return null
        }
    }

    /**
     * Lists all documents attached to this survey
     * @return a {@link Collection} of documents ({@link DocContext}) which have not been marked as deleted and are of type file
     */
    def getCurrentDocs() {

        return documents.findAll { (it.status?.value != 'Deleted' && it.isDocAFile())}.sort()
    }

    /**
     * Returns the name of this survey (duplicate of getSurveyName())
     * @return the {@link Subscription}'s name if it is a subscription survey, the survey name (in {@link SurveyInfo}) otherwise
     */
    String getConfigNameShort() {

/*        if (type == 'Subscription') {
            return subscription?.name
        } else {
            return surveyInfo.name
        }*/

        return surveyInfo.name
    }

    /**
     * Returns the name of this survey (duplicate of getConfigNameShort())
     * @return the {@link Subscription}'s name if it is a subscription survey, the survey name (in {@link SurveyInfo}) otherwise
     */
    String getSurveyName() {

     /*   if (type == 'Subscription' && surveyInfo.isSubscriptionSurvey) {
            return subscription?.name
        } else {
            return surveyInfo.name
        }*/

        return surveyInfo.name
    }

    /**
     * Returns the name of this survey; if it is a subscription survey, the {@link Subscription}'s core data (status, running time) is also displayed
     * @return the {@link Subscription}'s name, status and running time if it is a subscription survey, the survey name (in {@link SurveyInfo}) otherwise
     */
    String getConfigName() {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

    /*    if (type == 'Subscription') {
            return subscription?.name + ' - ' + subscription?.status?.getI10n('value') + ' ' +
                    (subscription?.startDate ? '(' : '') + sdf.format(subscription?.startDate) +
                    (subscription?.endDate ? ' - ' : '') + sdf.format(subscription?.endDate) +
                    (subscription?.startDate ? ')' : '')

        } else {
            return surveyInfo.name
        }*/

        return surveyInfo.name
    }

    /**
     * Substitution call for getLocalizedValue(); outputs the survey type as translated/localised string
     * @return the translated type of this survey
     */
    def getTypeInLocaleI10n() {

        return this.getLocalizedValue(this?.type)
    }

    /**
     * Retrieves the database IDs of the survey participant {@link Org}s; works only when the survey is connected to a {@link Subscription}
     * @return a {@link Map} of {@link Org} ids; ordered by subscriber institutions and institutions not having a subscription
     */
    Map<String, Object> getSurveyOrgsIDs() {
        Map<String, Object> result = [:]

        result.orgsWithoutSubIDs = this.orgs?.org?.id?.minus(this?.subscription?.getDerivedSubscribers()?.id) ?: null

        result.orgsWithSubIDs = this.orgs.org.id.minus(result.orgsWithoutSubIDs) ?: null

        return result
    }

    /**
     * Checks whether the survey has been completed (but not if it is already submitted) by the participant
     * @param org the participant institution ({@link Org}) whose inputs should be checked
     * @return one of the constants how much the survey has been completed
     */
    def checkResultsEditByOrg(Org org) {

        if (this.subSurveyUseForTransfer && SurveyOrg.findBySurveyConfigAndOrg(this, org).existsMultiYearTerm()) {
            return ALL_RESULTS_PROCESSED_BY_ORG
        } else {

            int countFinish = SurveyResult.executeQuery("select count(sr.id) from SurveyResult sr where sr.surveyConfig = :surConf and sr.participant = :org and " +
                    "(sr.intValue != null or sr.stringValue != null or sr.decValue != null or sr.urlValue != null or sr.refValue != null or sr.dateValue != null)", [surConf: this, org: org])[0]
            int countNotFinish = SurveyResult.executeQuery("select count(sr.id) from SurveyResult sr where sr.surveyConfig = :surConf and sr.participant = :org and " +
                    "(sr.intValue = null and sr.stringValue = null and sr.decValue = null and sr.urlValue = null and sr.refValue = null and sr.dateValue = null)", [surConf: this, org: org])[0]

            /*List<SurveyResult> surveyResult = SurveyResult.findAllBySurveyConfigAndParticipant(this, org)

                surveyResult.each {
                    if (it.isResultProcessed()) {
                        countFinish++
                    } else {
                        countNotFinish++
                    }
                }*/

                if (countFinish >= 0 && countNotFinish == 0) {
                    return ALL_RESULTS_PROCESSED_BY_ORG
                } else if (countFinish > 0 && countNotFinish > 0) {
                    return ALL_RESULTS_HALF_PROCESSED_BY_ORG
                } else {
                    return ALL_RESULTS_NOT_PROCESSED_BY_ORG
                }
        }


    }

    /**
     * Checks if the survey has been submitted by the participant. If the underlying subscription has a multi year running time and if this is a renewal subscription, this method returns true as well (= no renewal has to be sent)
     * @param org the participant institution ({@link Org}) to check
     * @return true if:
     * <ul>
     *     <li>there is a finish date, i.e. the survey has been submitted</li>
     *     <li>if the survey is a renewal survey and the participant has a multi-year subscription,</li>
     * </ul>
     * false otherwise
     */
    boolean isResultsSetFinishByOrg(Org org) {

        SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(this, org)

        if (surveyOrg?.finishDate){
            return true
        }else if (this.subSurveyUseForTransfer && surveyOrg?.existsMultiYearTerm()) {
            return true
        } else {
           return false
        }


    }

    /**
     * Checks if the participant is subscribing the subscription which is target of this survey
     * @param org the participant institution ({@link Org}) whose subscription should be checked
     * @return true if there is a subscription for this member, false otherwise
     */
    boolean hasOrgSubscription(Org org) {
        if (this.subscription) {
            Subscription orgSub = Subscription.executeQuery("select sub" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.org = :org and orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub",
                    [org          : org,
                     roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: this.subscription])[0]
            if(orgSub){
                return true
            }
        }
        return false

    }

    /**
     * Get the subscriptions which is target of this survey
     * @return list of subscription
     */
    List<Subscription> orgSubscriptions() {
        List<Subscription> orgSubscriptions = []
        if (this.subscription) {
            orgSubscriptions = Subscription.executeQuery("select sub" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub",
                    [roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: this.subscription])
        }
        return orgSubscriptions

    }

    /**
     * Gets the following survey target to this one
     * @return the next config if it exists, null otherwise
     */
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

    /**
     * Gets the previous survey target to this one
     * @return the previous config if it exists, null otherwise
     */
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

    /**
     * Builds a navigation among the survey targets
     * @return a {@link Map} listing the adjacent survey configurations to this one
     */
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

    /**
     * Returns the string representation of this survey configuration
     * @return the {@link Subscription}'s name if it is a subscription survey, "Survey Element {database id}" otherwise
     */
    String toString() {
        surveyInfo.name
    }

    /**
     * Dummy method for the document listing
     * @return false
     */
    boolean showUIShareButton() {
        return false
    }

    /**
     * Retrieves all cost items linked to the survey participants
     * @return a {@link List} of {@link CostItem}s connected to this survey via the participants
     * @see SurveyOrg
     */
    List<CostItem> getSurveyConfigCostItems(){

        return this.orgs ? CostItem.findAllBySurveyOrgInListAndCostItemStatusNotEqual(this.orgs, RDStore.COST_ITEM_DELETED) : []
    }

    /**
     * Gets the steps already done during the subscription renewal procedure
     * @return a JSON object containing the steps already done; if nothing has been done, an empty object is being returned
     */
    JSONElement getTransferWorkflowAsJSON() {
        transferWorkflow ? grails.converters.JSON.parse(transferWorkflow) : grails.converters.JSON.parse('{}')
    }

    /**
     * Counts the participants in this survey
     * @return a {@link Map} containing the participants:
     * <ul>
     *     <li>how many institutions do participate at the survey in general?</li>
     *     <li>how many of them have a subscription?</li>
     *     <li>how many of them have a multi-year subscription?</li>
     * </ul>
     */
    Map countParticipants(){
        Map result = [:]

        result.surveyMembers = orgs.size()

        if(surveyInfo.isSubscriptionSurvey){
            List subChilds = subscription.getNonDeletedDerivedSubscriptions()

            result.subMembers = subChilds.size()

            Integer subMembersWithMultiYear = 0
            subChilds.each {
                if(it.isCurrentMultiYearSubscriptionToParentSub())
                {
                    subMembersWithMultiYear++
                }
            }

            result.subMembersWithMultiYear = subMembersWithMultiYear
        }
        result
    }

    /**
     * Outputs this survey according to the dropdown naming convention defined <a href="https://github.com/hbz/laser2/wiki/UI:-Naming-Conventions">here</a>
     * @return a concatenated string according to the dropdown naming convention linked above
     */
    String dropdownNamingConvention() {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String period = surveyInfo.startDate ? sdf.format(surveyInfo.startDate)  : ''

        period = surveyInfo.endDate ? period + ' - ' + sdf.format(surveyInfo.endDate)  : ''

        period = period ? '('+period+')' : ''

        String statusString = surveyInfo.status ? surveyInfo.status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

        return surveyInfo.name + ' - ' + statusString + ' ' +period + ' ' + surveyInfo.type.getI10n('value')
    }

    /**
     * Gets a sorted list of survey config properties. Sorting is done by the {@link #surveyProperty} ({@link PropertyDefinition}) name
     * @return a sorted {@link List} of {@link SurveyConfigProperties}
     */
    List<SurveyConfigProperties> getSortedSurveyConfigProperties() {
       List<SurveyConfigProperties> surveyConfigPropertiesList = []

        LinkedHashSet<SurveyConfigProperties> propertiesParticipation = []
        LinkedHashSet<SurveyConfigProperties> propertiesMandatory = []
        LinkedHashSet<SurveyConfigProperties> propertiesNoMandatory = []

        this.surveyProperties.each {
            if(it.surveyProperty == PropertyStore.SURVEY_PROPERTY_PARTICIPATION){
                propertiesParticipation << it
            }
            else if(it.mandatoryProperty == true && it.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION){
                propertiesMandatory << it
            }
            else if(it.mandatoryProperty == false && it.surveyProperty != PropertyStore.SURVEY_PROPERTY_PARTICIPATION){
                propertiesNoMandatory << it
            }
        }

        propertiesParticipation = propertiesParticipation.sort {it.surveyProperty.getI10n('name')}

        propertiesMandatory = propertiesMandatory.sort {it.surveyProperty.getI10n('name')}

        propertiesNoMandatory = propertiesNoMandatory.sort {it.surveyProperty.getI10n('name')}

        surveyConfigPropertiesList = [propertiesParticipation, propertiesMandatory, propertiesNoMandatory]

        return surveyConfigPropertiesList.flatten()

    }

    /**
     * Substitution call for {@link #getSortedSurveyConfigProperties()}
     * @return the list of {@link SurveyConfigProperties} or an empty list, if none are available
     */
    List<PropertyDefinition> getSortedSurveyProperties() {
        List<SurveyConfigProperties> surveyConfigPropertiesList = this.getSortedSurveyConfigProperties()
        return surveyConfigPropertiesList.size() > 0 ? surveyConfigPropertiesList.surveyProperty : []
    }

    /**
     * Retrieves the property definition groups defined by the given institution for this survey configuration
     * @param contextOrg the institution whose property groups should be retrieved
     * @see {@link Subscription#getCalculatedPropDefGroups(de.laser.Org)}
     * @return the {@link PropertyDefinitionGroup}s for this survey configuration, defined by the given institution
     */
    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        BeanStore.getPropertyService().getCalculatedPropDefGroups(this, contextOrg)
    }

    /**
     * Called from /templates/survey/_properties.gsp
     * Retrieves a set of survey config properties defined for the survey config belonging to the given property definition group
     * @param propertyDefinitionGroup the group to which the survey config properties to retrieve belong
     * @return a {@link Set} of {@link SurveyConfigProperties} of the given type defined for this survey config
     */
    LinkedHashSet<SurveyConfigProperties> getSurveyConfigPropertiesByPropDefGroup(PropertyDefinitionGroup propertyDefinitionGroup) {
        LinkedHashSet<SurveyConfigProperties> properties = []

        this.surveyProperties.each {
            if(propertyDefinitionGroup && propertyDefinitionGroup.items  && it.surveyProperty.id in propertyDefinitionGroup.items.propDef.id){
                properties << it
            }
        }

        properties = properties.sort {it.surveyProperty.getI10n('name')}

        return properties

    }

    /**
     * Called from /templates/survey/_properties_table.gsp
     * Gets the {@link PropertyDefinition}s in the given property definition group which can be added for the given survey config
     * @param propertyDefinitionGroup the {@link PropertyDefinitionGroup} whose items should be retrieved
     * @return a sorted {@link Set} of {@link PropertyDefinition}s
     */
    LinkedHashSet<PropertyDefinition> getSelectablePropertiesByPropDefGroup(PropertyDefinitionGroup propertyDefinitionGroup) {
        LinkedHashSet<PropertyDefinition> properties = []

        propertyDefinitionGroup.items.propDef.each {
            if(!(this.surveyProperties && it.id in this.surveyProperties.surveyProperty.id)){
                properties << it
            }
        }

        properties = properties.sort {it.getI10n('name')}

        return properties

    }

    /**
     * Gets all property definitions which are belonging to a group
     * @return a {@link List} of {@link PropertyDefinition}s which are belonging to a {@link PropertyDefinitionGroup}, sorted by name
     */
    List<PropertyDefinition> getPropertiesByPropDefGroups() {
        List<PropertyDefinition> properties = []

        List<PropertyDefinitionGroup> propertyDefinitionGroups = []

        Map<String, Object> allPropDefGroups = this.getCalculatedPropDefGroups(this.surveyInfo.owner)

        if(allPropDefGroups.sorted){
            allPropDefGroups.sorted.each {
                propertyDefinitionGroups << it[1]
            }
        }

        propertyDefinitionGroups.items.propDef.each {
                properties << it
        }

        properties = properties.sort {it.getI10n('name')}

        return properties

    }

    /**
     * Called from /templates/survey/_properties_table.gsp
     * Gets all property definitions which are not belonging to any group (= orphaned properties) and have not been assigned
     * to the survey configuration yet
     * @return a {@link List} of {@link PropertyDefinition}s which can be assigned to the survey configuration and do not belong to any group, sorted by name
     */
    List<PropertyDefinition> getOrphanedSelectableProperties() {
        List<PropertyDefinition> propertiesOfPropDefGroups = this.getPropertiesByPropDefGroups()?.flatten()
        List<PropertyDefinition> props = []

        Org ownerOrg = this.surveyInfo.owner

        //global Property
        PropertyDefinition.getAllByDescr(PropertyDefinition.SVY_PROP).each { it ->
            if((propertiesOfPropDefGroups.size() == 0 || (propertiesOfPropDefGroups && !(it.id in propertiesOfPropDefGroups.id))) && ((this.surveyProperties.size() == 0) || this.surveyProperties && !(it.id in  this.surveyProperties.surveyProperty.id))) {
                props << it
            }
        }

        props = props.sort {it.getI10n('name')}

        return props

    }

    /**
     * Called by /templates/survey/_properties.gsp
     * Gets the survey results (= answers on behalf of the submitting institution) of types of the given
     * property definition group, submitted by the given institution
     * @param propertyDefinitionGroup the {@link PropertyDefinitionGroup} whose result types ({@link PropertyDefinition}s) should be retrieved
     * @param org the institution ({@link Org}) whose results / answers are requested
     * @return a {@link Set} of {@link SurveyResult} answers, sorted by the name of the type ({@link PropertyDefinition})
     */
    LinkedHashSet<SurveyResult> getSurveyResultsByPropDefGroupAndOrg(PropertyDefinitionGroup propertyDefinitionGroup, Org org) {

        LinkedHashSet<SurveyResult> properties = []

        propertyDefinitionGroup.items.each {
            SurveyResult surveyResult = SurveyResult.findByParticipantAndSurveyConfigAndType(org, this, it.propDef)
            if(surveyResult) {
                properties << surveyResult
            }
        }

        properties = properties.sort {it.type.getI10n('name')}

        return properties

    }

    /**
     * Called by /templates/survey/_properties.gsp
     * Gets all survey config properties which are not belonging to any group. Optionally, a list of survey config
     * properties may be given to filter the available properties
     * @param containedProperties a filtering list of {@link SurveyConfigProperties} which excludes selectable properties
     * @return a {@link Set} of {@link SurveyConfigProperties} belonging to no group, sorted by name of {@link PropertyDefinition}
     */
    LinkedHashSet<SurveyConfigProperties> getOrphanedSurveyConfigProperties(LinkedHashSet containedProperties) {
        LinkedHashSet<SurveyConfigProperties> properties = []

        if(containedProperties.isEmpty()){
            this.surveyProperties.each {
                if ((!it.surveyProperty.tenant)) {
                    properties << it
                }
            }
        }else {
            this.surveyProperties.each {
                if (!(it.id in containedProperties.id.flatten()) && (!it.surveyProperty.tenant)) {
                    properties << it
                }
            }
        }

        properties = properties.sort {it.surveyProperty.getI10n('name')}

        return properties

    }

    /**
     * Called by /templates/survey/_properties.gsp
     * Gets all survey config properties which are not publicly visible, i.e. private
     * @return a {@link Set} of private {@link SurveyConfigProperties}, sorted by name of {@link PropertyDefinition}
     */
    LinkedHashSet<SurveyConfigProperties> getPrivateSurveyConfigProperties() {
        LinkedHashSet<SurveyConfigProperties> properties = []

        this.surveyProperties.each {
            if ((it.surveyProperty.tenant)) {
                properties << it
            }
        }

        properties = properties.sort {it.surveyProperty.getI10n('name')}

        return properties

    }

    /**
     * Called from /templates/survey/_properties_table.gsp
     * Gets the private survey properties of the institution submitting the survey for internal remarks
     * @return a {@link List} of {@link PropertyDefinition}s defined by the destination institution, sorted by name
     */
    List<PropertyDefinition> getPrivateSelectableProperties() {
        List<PropertyDefinition> props = []

        Org ownerOrg = this.surveyInfo.owner

        //private Property
        PropertyDefinition.getAllByDescrAndTenant(PropertyDefinition.SVY_PROP, ownerOrg).each { it ->
            if(((this.surveyProperties.size() == 0) || this.surveyProperties && !(it.id in  this.surveyProperties.surveyProperty.id))) {
                props << it
            }
        }

        props = props.sort {it.getI10n('name')}

        return props

    }

    /**
     * Called by /templates/survey/_properties.gsp
     * Gets the survey results (= answers) submitted by the given institution and belonging to no property definition group.
     * Optionally, a list of {@link SurveyConfigProperties} may be given to restrict the output to survey results of the given types
     * @param containedProperties a {@link Set} of {@link SurveyConfigProperties} filtering among the possible {@link SurveyResult}s
     * @param org the institution ({@link Org}) whose answers should be retrieved
     * @return a {@link Set}, sorted by the name of the {@link PropertyDefinition}, of {@link SurveyResult}s
     */
    LinkedHashSet<SurveyResult> getOrphanedSurveyResultsByOrg(LinkedHashSet containedProperties, Org org) {

        LinkedHashSet<SurveyResult> properties = []

        if(containedProperties.isEmpty()){
            this.surveyProperties.each {
                if ((!it.surveyProperty.tenant)) {
                    properties << SurveyResult.findByParticipantAndSurveyConfigAndType(org, this, it.surveyProperty)
                }
            }
        }else {
            this.surveyProperties.each {
                if (!(it.surveyProperty.id in containedProperties.type.id.flatten()) && (!it.surveyProperty.tenant)){
                    SurveyResult surveyResult = SurveyResult.findByParticipantAndSurveyConfigAndType(org, this, it.surveyProperty)
                    if(surveyResult) {
                        properties << surveyResult
                    }
                }
            }
        }

        properties = properties.sort {it.type.getI10n('name')}

        return properties

    }

    /**
     * Called by /templates/survey/_properties.gsp
     * Gets the responses of the institution to survey properties which have been defined by the submitting institutiton itself
     * @param org the institution ({@link Org}) whose {@link SurveyResult}s should be retrieved
     * @return a {@link List} of {@link SurveyResult}s which belong to types defined by the submitting institution
     */
    LinkedHashSet<SurveyResult> getPrivateSurveyResultsByOrg(Org org) {

        LinkedHashSet<SurveyResult> properties = []

            this.surveyProperties.each {
                if ((it.surveyProperty.tenant)) {
                    properties << SurveyResult.findByParticipantAndSurveyConfigAndType(org, this, it.surveyProperty)
                }
            }

        properties = properties.sort {it.type.getI10n('name')}

        return properties

    }

    /**
     * Returns the count of institutions who end their participation on the subscription subject of this survey configuration
     * @return the number of institutions terminating participation
     */
    Integer countOrgsWithTermination(){
        Integer countOrgsWithTermination = 0
        List<Org> orgNotInsertedItselfList = SurveyOrg.executeQuery("select surOrg.org from SurveyOrg as surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.orgInsertedItself = false", [surveyConfig: this])

        String queryOrgsWithTermination = 'select count(id) from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue  '
        Map queryMapOrgsWithTermination = [
                owner      : this.surveyInfo.owner.id,
                surProperty: PropertyStore.SURVEY_PROPERTY_PARTICIPATION.id,
                surConfig  : this.id,
                refValue   : RDStore.YN_NO]

        if(orgNotInsertedItselfList.size() > 0){
            queryOrgsWithTermination += ' and participant in (:orgNotInsertedItselfList) '
            queryMapOrgsWithTermination.orgNotInsertedItselfList = orgNotInsertedItselfList
        }

        //Orgs with termination there sub
        countOrgsWithTermination = SurveyResult.executeQuery(queryOrgsWithTermination, queryMapOrgsWithTermination)[0]

        return countOrgsWithTermination
    }


}
