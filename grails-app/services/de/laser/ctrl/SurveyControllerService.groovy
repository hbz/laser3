package de.laser.ctrl

import de.laser.AccessService
import de.laser.ContextService
import de.laser.DocstoreService
import de.laser.License
import de.laser.Org
import de.laser.Subscription
import de.laser.SubscriptionService
import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.SurveyController
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.TaskService
import de.laser.auth.User
import de.laser.utils.LocaleUtils
import de.laser.storage.RDStore
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.time.TimeCategory

@Transactional
class SurveyControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    ContextService contextService
    DocstoreService docstoreService
    SubscriptionService subscriptionService
    TaskService taskService

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Sets generic parameters common to many controller calls and checks permission grants
     * @param params the request parameter map
     * @return the result map with generic parameters
     */
    Map<String, Object> getResultGenericsAndCheckAccess(GrailsParameterMap params) {

        Map<String, Object> result = [:]

        result.institution = contextService.getOrg()
        result.contextOrg = contextService.getOrg()
        result.user = contextService.getUser()

        result.surveyInfo = SurveyInfo.get(params.id)
        result.surveyConfig = params.surveyConfigID ? SurveyConfig.get(Long.parseLong(params.surveyConfigID.toString())) : result.surveyInfo.surveyConfigs[0]
        result.surveyWithManyConfigs = (result.surveyInfo.surveyConfigs?.size() > 1)

        result.editable = result.surveyInfo.isEditable() ?: false

        if(!(result.user.isAdmin() || result.user.isYoda() || result.surveyInfo.owner == contextService.getOrg().id))
        {
            return [result:null,status:STATUS_ERROR]
        }

        if (result.surveyConfig) {
            result.transferWorkflow = result.surveyConfig.transferWorkflow ? JSON.parse(result.surveyConfig.transferWorkflow) : null
        }

        int tc1 = taskService.getTasksByResponsiblesAndObject(result.user, result.contextOrg, result.surveyConfig).size()
        int tc2 = taskService.getTasksByCreatorAndObject(result.user, result.surveyConfig).size()
        result.tasksCount = (tc1 || tc2) ? "${tc1}/${tc2}" : ''

        result.notesCount = docstoreService.getNotes(result.surveyConfig, result.contextOrg).size()

        result.subscription = result.surveyConfig.subscription ?: null

        result
    }

    /**
     * Gets the tasks to the given survey
     * @param controller unused
     * @param params the request parameter map
     * @return OK with the tasks in case of success, ERROR otherwise
     */
    Map<String,Object> tasks(SurveyController controller, GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            int offset = params.offset ? Integer.parseInt(params.offset) : 0
            result.putAll(taskService.getTasks(offset, (User) result.user, (Org) result.institution, result.surveyConfig))
            [result:result,status:STATUS_OK]
        }
    }

    /**
     * Collects the data for the given renewal survey for the evaluation view
     * @param params the request parameter map
     * @return OK with the retrieved data in case of success, ERROR otherwise
     */
    Map<String,Object> renewalEvaluation(GrailsParameterMap params) {
        Map<String,Object> result = getResultGenericsAndCheckAccess(params)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {

            result.parentSubscription = result.surveyConfig.subscription
            result.parentSubChilds = subscriptionService.getValidSubChilds(result.parentSubscription)
            result.parentSuccessorSubscription = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
            result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null


            result.participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION
            if (result.parentSuccessorSubscription) {
                String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
                result.memberLicenses = License.executeQuery(query, [subscription: result.parentSuccessorSubscription, linkType: RDStore.LINKTYPE_LICENSE])
            }


            /* if(result.parentSubChilds) {
             Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select distinct(sp.type) from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :ctx and sp.instanceOf = null",[subscriptionSet:validSubChildren,ctx:result.institution])
             propList.addAll(result.parentSubscription.propertySet.type)
             result.propList = propList
             result.filteredSubChilds = validSubChildren
             List<Subscription> childSubs = result.parentSubscription.getNonDeletedDerivedSubscriptions()
             if(childSubs) {
                 String localizedName
                 switch(LocaleUtils.getCurrentLocale()) {
                     case Locale.GERMANY:
                     case Locale.GERMAN: localizedName = "name_de"
                         break
                     default: localizedName = "name_en"
                         break
                 }
                 String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                 Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet:childSubs, context:result.institution] )
                 result.memberProperties = memberProperties
             }
         }*/

            result.properties = []
            result.properties.addAll(SurveyConfigProperties.findAllBySurveyPropertyNotEqualAndSurveyConfig(result.participationProperty, result.surveyConfig)?.surveyProperty.sort {
                it.getI10n('name')
            })


            result.multiYearTermFiveSurvey = null
            result.multiYearTermFourSurvey = null
            result.multiYearTermThreeSurvey = null
            result.multiYearTermTwoSurvey = null

            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in result.properties.id) {
                result.multiYearTermFiveSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5
                result.properties.remove(result.multiYearTermFiveSurvey)
            }

            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in result.properties.id) {
                result.multiYearTermFourSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4
                result.properties.remove(result.multiYearTermFourSurvey)
            }

            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in result.properties.id) {
                result.multiYearTermThreeSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3
                result.properties.remove(result.multiYearTermThreeSurvey)
            }
            if (PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in result.properties.id) {
                result.multiYearTermTwoSurvey = PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2
                result.properties.remove(result.multiYearTermTwoSurvey)

            }

            List currentParticipantIDs = []
            result.orgsWithMultiYearTermSub = []
            //result.orgsLateCommers = []
            List orgsWithMultiYearTermOrgsID = []
            List orgsLateCommersOrgsID = []
            result.parentSubChilds.each { Subscription sub ->
                if (sub.isCurrentMultiYearSubscriptionToParentSub()) {
                    result.orgsWithMultiYearTermSub << sub
                    sub.getAllSubscribers().each { org ->
                        orgsWithMultiYearTermOrgsID << org.id
                    }
                } else {
                    sub.getAllSubscribers().each { org ->
                        currentParticipantIDs << org.id
                    }
                }
            }


            result.orgsWithParticipationInParentSuccessor = []
            result.parentSuccessorSubChilds.each { sub ->
                sub.getAllSubscribers().each { org ->
                    if (!(org.id in orgsWithMultiYearTermOrgsID) || !(org.id in currentParticipantIDs)) {
                        result.orgsWithParticipationInParentSuccessor << sub
                    }
                }
            }


            result.orgInsertedItself = []

            List<Org> orgInsertedItselfList = SurveyOrg.executeQuery("select surOrg.org from SurveyOrg as surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.orgInsertedItself = true", [surveyConfig: result.surveyConfig])

            List<Org> orgNotInsertedItselfList = SurveyOrg.executeQuery("select surOrg.org from SurveyOrg as surOrg where surOrg.surveyConfig = :surveyConfig and surOrg.orgInsertedItself = false", [surveyConfig: result.surveyConfig])


            //Orgs with inserted it self to the survey
            SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and (refValue = :refValue OR refValue is null) and participant in (:orgInsertedItselfList) order by participant.sortname",
                    [
                            owner      : result.institution.id,
                            surProperty: result.participationProperty.id,
                            surConfig  : result.surveyConfig.id,
                            refValue   : RDStore.YN_NO,
                            orgInsertedItselfList: orgInsertedItselfList]).each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.sub = surveyResult.participantSubscription
                if (result.properties) {
                        String lang = LocaleUtils.getCurrentLang()
                        //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(surveyResult.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                        //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                        String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                        newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }

                result.orgInsertedItself << newSurveyResult

            }

            result.orgsWithTermination = []
            String queryOrgsWithTermination = 'from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue  '
            Map queryMapOrgsWithTermination = [
                    owner      : result.institution.id,
                    surProperty: result.participationProperty.id,
                    surConfig  : result.surveyConfig.id,
                    refValue   : RDStore.YN_NO]

            if(orgNotInsertedItselfList.size() > 0){
                queryOrgsWithTermination += ' and participant in (:orgNotInsertedItselfList) '
                queryMapOrgsWithTermination.orgNotInsertedItselfList = orgNotInsertedItselfList
            }

            //Orgs with termination there sub
            SurveyResult.executeQuery(queryOrgsWithTermination + " order by participant.sortname", queryMapOrgsWithTermination).each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.sub = surveyResult.participantSubscription
                if (result.properties) {
                        String lang = LocaleUtils.getCurrentLang()
                        //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(surveyResult.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                        //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                        String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                        newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }

                result.orgsWithTermination << newSurveyResult

            }


            // Orgs that renew or new to Sub
            result.orgsContinuetoSubscription = []
            result.newOrgsContinuetoSubscription = []

            List<SurveyResult> surveyResults = SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue order by participant.sortname",
                    [
                            owner      : result.institution.id,
                            surProperty: result.participationProperty.id,
                            surConfig  : result.surveyConfig.id,
                            refValue   : RDStore.YN_YES])
            surveyResults.each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                if (result.properties) {
                    String lang = LocaleUtils.getCurrentLang()
                    //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(surveyResult.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                    //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                    String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                    newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }

                if (surveyResult.participant.id in currentParticipantIDs) {

                    newSurveyResult.sub = surveyResult.participantSubscription

                    //newSurveyResult.sub = result.parentSubscription.getDerivedSubscriptionBySubscribers(surveyResult.participant)

                    if (result.multiYearTermTwoSurvey) {

                        newSurveyResult.newSubPeriodTwoStartDate = null
                        newSurveyResult.newSubPeriodTwoEndDate = null

                        SurveyResult participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                        if (participantPropertyTwo && participantPropertyTwo.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodTwoStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodTwoEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 2.year) : null
                                newSurveyResult.participantPropertyTwoComment = participantPropertyTwo.comment
                            }
                        }

                    }
                    if (result.multiYearTermThreeSurvey) {
                        newSurveyResult.newSubPeriodThreeStartDate = null
                        newSurveyResult.newSubPeriodThreeEndDate = null

                        SurveyResult participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                        if (participantPropertyThree && participantPropertyThree.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodThreeStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodThreeEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 3.year) : null
                                newSurveyResult.participantPropertyThreeComment = participantPropertyThree.comment
                            }
                        }
                    }

                    if (result.multiYearTermFourSurvey) {
                        newSurveyResult.newSubPeriodFoureStartDate = null
                        newSurveyResult.newSubPeriodFourEndDate = null

                        SurveyResult participantPropertyFour = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFourSurvey)
                        if (participantPropertyFour && participantPropertyFour.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFourStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFourEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 4.year) : null
                                newSurveyResult.participantPropertyFourComment = participantPropertyFour.comment
                            }
                        }
                    }

                    if (result.multiYearTermFiveSurvey) {
                        newSurveyResult.newSubPeriodFiveStartDate = null
                        newSurveyResult.newSubPeriodFiveEndDate = null

                        SurveyResult participantPropertyFive = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFiveSurvey)
                        if (participantPropertyFive && participantPropertyFive.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFiveStartDate = newSurveyResult.sub.startDate ? (newSurveyResult.sub.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFiveEndDate = newSurveyResult.sub.endDate ? (newSurveyResult.sub.endDate + 5.year) : null
                                newSurveyResult.participantPropertyFiveComment = participantPropertyFive.comment
                            }
                        }
                    }

                    result.orgsContinuetoSubscription << newSurveyResult
                }
                if (!(surveyResult.participant.id in currentParticipantIDs) && !(surveyResult.participant.id in orgsLateCommersOrgsID) && !(surveyResult.participant.id in orgsWithMultiYearTermOrgsID)) {


                    if (result.multiYearTermTwoSurvey) {

                        newSurveyResult.newSubPeriodTwoStartDate = null
                        newSurveyResult.newSubPeriodTwoEndDate = null

                        SurveyResult participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                        if (participantPropertyTwo && participantPropertyTwo.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodTwoStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodTwoEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 2.year) : null
                                newSurveyResult.participantPropertyTwoComment = participantPropertyTwo.comment
                            }
                        }

                    }
                    if (result.multiYearTermThreeSurvey) {
                        newSurveyResult.newSubPeriodThreeStartDate = null
                        newSurveyResult.newSubPeriodThreeEndDate = null

                        SurveyResult participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                        if (participantPropertyThree && participantPropertyThree.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodThreeStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodThreeEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 3.year) : null
                                newSurveyResult.participantPropertyThreeComment = participantPropertyThree.comment
                            }
                        }
                    }

                    if (result.multiYearTermFourSurvey) {
                        newSurveyResult.newSubPeriodFourStartDate = null
                        newSurveyResult.newSubPeriodFourEndDate = null

                        SurveyResult participantPropertyFour = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFourSurvey)
                        if (participantPropertyFour && participantPropertyFour.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFourStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFourEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 4.year) : null
                                newSurveyResult.participantPropertyFourComment = participantPropertyFour.comment
                            }
                        }
                    }

                    if (result.multiYearTermFiveSurvey) {
                        newSurveyResult.newSubPeriodFiveStartDate = null
                        newSurveyResult.newSubPeriodFiveEndDate = null

                        SurveyResult participantPropertyFive = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(surveyResult.participant, result.institution, result.surveyConfig, result.multiYearTermFiveSurvey)
                        if (participantPropertyFive && participantPropertyFive.refValue?.id == RDStore.YN_YES.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodFiveStartDate = result.parentSubscription.startDate ? (result.parentSubscription.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodFiveEndDate = result.parentSubscription.endDate ? (result.parentSubscription.endDate + 5.year) : null
                                newSurveyResult.participantPropertyFiveComment = participantPropertyFive.comment
                            }
                        }
                    }

                    result.newOrgsContinuetoSubscription << newSurveyResult
                }

            }


            //Orgs without really result
            result.orgsWithoutResult = []

            String queryOrgsWithoutResult = 'from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue is null '
            Map queryMapOrgsWithoutResult = [
                    owner      : result.institution.id,
                    surProperty: result.participationProperty.id,
                    surConfig  : result.surveyConfig.id]

            if(orgNotInsertedItselfList.size() > 0){
                queryOrgsWithoutResult += ' and participant in (:orgNotInsertedItselfList) '
                queryMapOrgsWithoutResult.orgNotInsertedItselfList = orgNotInsertedItselfList
            }


            SurveyResult.executeQuery(queryOrgsWithoutResult + " order by participant.sortname", queryMapOrgsWithoutResult).each { SurveyResult surveyResult ->
                Map newSurveyResult = [:]
                newSurveyResult.participant = surveyResult.participant
                newSurveyResult.resultOfParticipation = surveyResult
                newSurveyResult.surveyConfig = result.surveyConfig
                if (result.properties) {
                    String lang = LocaleUtils.getCurrentLang()
                    //newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(it.participant, result.institution, result.surveyConfig, result.properties,[sort:type["value${locale}"],order:'asc'])
                    //in (:properties) throws for some unexplaniable reason a HQL syntax error whereas it is used in many other places without issues ... TODO
                    String query = "select sr from SurveyResult sr join sr.type pd where pd in (:surveyProperties) and sr.participant = :participant and sr.owner = :context and sr.surveyConfig = :cfg order by pd.name_${lang} asc"
                    newSurveyResult.properties = SurveyResult.executeQuery(query, [participant: surveyResult.participant, context: result.institution, cfg: result.surveyConfig, surveyProperties: result.properties])
                }


                if (surveyResult.participant.id in currentParticipantIDs) {
                    newSurveyResult.sub = surveyResult.participantSubscription
                    //newSurveyResult.sub = result.parentSubscription.getDerivedSubscriptionBySubscribers(surveyResult.participant)
                } else {
                    newSurveyResult.sub = null
                }
                result.orgsWithoutResult << newSurveyResult
            }


            //MultiYearTerm Subs
            Integer sumParticipantWithSub = ((result.orgsContinuetoSubscription.groupBy {
                it.participant.id
            }.size()) + (result.orgsWithTermination.groupBy { it.participant.id }.size()) + (result.orgsWithMultiYearTermSub.size()))

            if (sumParticipantWithSub < result.parentSubChilds.size()) {
                /*def property = PropertyDefinition.getByNameAndDescr("Perennial term checked", PropertyDefinition.SUB_PROP)

            def removeSurveyResultOfOrg = []
            result.orgsWithoutResult.each { surveyResult ->
                if (surveyResult.participant.id in currentParticipantIDs && surveyResult.sub) {

                    if (property.isRefdataValueType()) {
                        if (surveyResult.sub.propertySet.find {
                            it.type.id == property.id
                        }?.refValue == RefdataValue.getByValueAndCategory('Yes', property.refdataCategory)) {

                            result.orgsWithMultiYearTermSub << surveyResult.sub
                            removeSurveyResultOfOrg << surveyResult
                        }
                    }
                }
            }
            removeSurveyResultOfOrg.each{ it
                result.orgsWithoutResult?.remove(it)
            }*/

                result.orgsWithMultiYearTermSub = result.orgsWithMultiYearTermSub.sort { it.getAllSubscribers().sortname }

            }

            result.propertiesChanged = [:]
            result.propertiesChangedByParticipant = []
            result.properties.sort{it.getI10n('name')}.each { PropertyDefinition propertyDefinition ->

                PropertyDefinition subPropDef = PropertyDefinition.getByNameAndDescr(propertyDefinition.name, PropertyDefinition.SUB_PROP)
                if(subPropDef){
                    result.surveyConfig.orgs.each{ SurveyOrg surveyOrg ->
                        Subscription subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                                [parentSub  : result.surveyConfig.subscription,
                                 participant: surveyOrg.org
                                ])[0]
                        SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, result.surveyConfig, result.contextOrg)
                        SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, result.contextOrg)

                        if(surveyResult && subscriptionProperty){
                            String surveyValue = surveyResult.getValue()
                            String subValue = subscriptionProperty.getValue()
                            if (surveyValue && surveyValue != subValue) {
                                Map changedMap = [:]
                                //changedMap.surveyResult = surveyResult
                                //changedMap.subscriptionProperty = subscriptionProperty
                                //changedMap.surveyValue = surveyValue
                                //changedMap.subValue = subValue
                                changedMap.participant = surveyOrg.org

                                result.propertiesChanged."${propertyDefinition.id}" = result.propertiesChanged."${propertyDefinition.id}" ?: []
                                result.propertiesChanged."${propertyDefinition.id}" << changedMap

                                result.propertiesChangedByParticipant << surveyOrg.org
                            }
                        }

                    }

                }
            }

            result.totalOrgs = result.orgsContinuetoSubscription.size() + result.newOrgsContinuetoSubscription.size() + result.orgsWithMultiYearTermSub.size()  + result.orgsWithTermination.size() + result.orgsWithParticipationInParentSuccessor.size() + result.orgsWithoutResult.size()

            result
        }
        [result:result,status:STATUS_OK]

    }

}