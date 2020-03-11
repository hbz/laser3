package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import de.laser.helper.RDStore
import grails.transaction.Transactional
import grails.util.Holders

@Transactional
class SurveyService {

    def accessService
    def contextService
    def messageSource
    Locale locale

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
    }


    boolean isEditableSurvey(Org org, SurveyInfo surveyInfo) {

        if(accessService.checkPermAffiliationX('ORG_CONSORTIUM_SURVEY','INST_EDITOR','ROLE_ADMIN') && surveyInfo.owner?.id == contextService.getOrg()?.id)
        {
            return true
        }

        if(surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED)
        {
            return false
        }

        def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo?.surveyConfigs)

        if(surveyResults) {
           return surveyResults?.finishDate?.contains(null) ? true : false
        }else
        {
            return false
        }


    }

    boolean isEditableIssueEntitlementsSurvey(Org org, SurveyConfig surveyConfig) {

        if(accessService.checkPermAffiliationX('ORG_CONSORTIUM_SURVEY','INST_EDITOR','ROLE_ADMIN') && surveyConfig?.surveyInfo.owner?.id == contextService.getOrg()?.id)
        {
            return true
        }

        if(!surveyConfig?.pickAndChoose)
        {
            return false
        }

        if(surveyConfig?.surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED)
        {
            return false
        }

        if(accessService.checkPermAffiliationX('ORG_BASIC_MEMBER','INST_EDITOR','ROLE_ADMIN')) {

            if (SurveyOrg.findByOrgAndSurveyConfig(org, surveyConfig)?.finishDate) {
                return false
            } else {
                return true
            }
        }else{
            return false
        }

    }

    Map<String, Object> getParticipantConfigNavigation(Org org, SurveyInfo surveyInfo, SurveyConfig surveyConfig){
        Map<String, Object> result = [:]
        def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo?.surveyConfigs).sort {it.surveyConfig.configOrder}

        int currentOrder = surveyConfig?.configOrder
        List<Integer> configOrders = SurveyConfig.findAllByIdInList(surveyResults.findAll {it.surveyConfig.type == 'Subscription'}.groupBy {it.surveyConfig.id}.keySet()).configOrder
        int currentOrderIndex = configOrders.indexOf(currentOrder)

        if(currentOrderIndex > 0) {
            result.prev = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :prev and sc.surveyInfo = :surInfo',[prev:configOrders.get(currentOrderIndex-1), surInfo: surveyInfo])[0]
        }
        if(currentOrderIndex < configOrders.size()-1) {
            result.next = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :next and sc.surveyInfo = :surInfo',[next:configOrders.get(currentOrderIndex+1), surInfo: surveyInfo])[0]
        }

        result.total = configOrders.size()

        return result

    }

    Map<String, Object> getConfigNavigation(SurveyInfo surveyInfo, SurveyConfig surveyConfig){
        Map<String, Object> result = [:]
        int currentOrder = surveyConfig?.configOrder
        List<Integer> configOrders = surveyInfo?.surveyConfigs?.sort{it.configOrder}.configOrder
        int currentOrderIndex = configOrders.indexOf(currentOrder)

        if(currentOrderIndex > 0) {
            result.prev = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :prev and sc.surveyInfo = :surInfo',[prev:configOrders.get(currentOrderIndex-1), surInfo: surveyInfo])[0]
        }
        if(currentOrderIndex < configOrders.size()-1) {
            result.next = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :next and sc.surveyInfo = :surInfo',[next:configOrders.get(currentOrderIndex+1), surInfo: surveyInfo])[0]
        }

        result.total = configOrders.size()

        return result

    }

    boolean isContinueToParticipate(Org org, SurveyConfig surveyConfig) {
        def participationProperty = SurveyProperty.findByNameAndOwnerIsNull("Participation")

        def result = SurveyResult.findBySurveyConfigAndParticipantAndType(surveyConfig, org, participationProperty)?.getResult() == RDStore.YN_YES ? true : false

        return result
    }

    boolean copyProperties(List<AbstractProperty> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        Org contextOrg = contextService.getOrg()
        def targetProp


        properties?.each { sourceProp ->
            if (sourceProp instanceof CustomProperty) {
                targetProp = targetSub.customProperties.find { it.typeId == sourceProp.typeId }
            }
            if (sourceProp instanceof PrivateProperty && sourceProp.type?.tenant?.id == contextOrg?.id) {
                targetProp = targetSub.privateProperties.find { it.typeId == sourceProp.typeId }
            }
            boolean isAddNewProp = sourceProp.type?.multipleOccurrence
            if ( (! targetProp) || isAddNewProp) {
                if (sourceProp instanceof CustomProperty) {
                    targetProp = new SubscriptionCustomProperty(type: sourceProp.type, owner: targetSub)
                } else {
                    targetProp = new SubscriptionPrivateProperty(type: sourceProp.type, owner: targetSub)
                }
                targetProp = sourceProp.copyInto(targetProp)
                save(targetProp, flash)
                if ((sourceProp.id.toString() in auditProperties) && targetProp instanceof CustomProperty) {
                    //copy audit
                    if (!AuditConfig.getConfig(targetProp, AuditConfig.COMPLETE_OBJECT)) {
                        def auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(SubscriptionCustomProperty.class.name, sourceProp.id)
                        auditConfigs.each {
                            AuditConfig ac ->
                                //All ReferenceFields were copied!
                                AuditConfig.addConfig(targetProp, ac.referenceField)
                        }
                        if (!auditConfigs) {
                            AuditConfig.addConfig(targetProp, AuditConfig.COMPLETE_OBJECT)
                        }
                    }

                }
            } else {
                Object[] args = [sourceProp?.type?.getI10n("name") ?: sourceProp.class.getSimpleName()]
                flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
            }
        }
    }


    boolean deleteProperties(List<AbstractProperty> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){

            properties.each { AbstractProperty prop ->
                AuditConfig.removeAllConfigs(prop)
            }

        int anzCP = SubscriptionCustomProperty.executeUpdate("delete from SubscriptionCustomProperty p where p in (:properties)",[properties: properties])
        int anzPP = SubscriptionPrivateProperty.executeUpdate("delete from SubscriptionPrivateProperty p where p in (:properties)",[properties: properties])
    }

    private boolean save(obj, flash){
        if (obj.save(flush: true)){
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, locale)
            return false
        }
    }
}
