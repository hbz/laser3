package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import grails.transaction.Transactional
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class SurveyService {

    AccessService accessService
    ContextService contextService
    MessageSource messageSource
    ExportService exportService
    Locale locale
    EscapeService escapeService

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
    }


    boolean isEditableSurvey(Org org, SurveyInfo surveyInfo) {

        if (accessService.checkPermAffiliationX('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR', 'ROLE_ADMIN') && surveyInfo.owner?.id == contextService.getOrg()?.id) {
            return true
        }

        if (surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo?.surveyConfigs)

        if (surveyResults) {
            return surveyResults?.finishDate?.contains(null) ? true : false
        } else {
            return false
        }


    }

    boolean isEditableIssueEntitlementsSurvey(Org org, SurveyConfig surveyConfig) {

        if (accessService.checkPermAffiliationX('ORG_CONSORTIUM_SURVEY', 'INST_EDITOR', 'ROLE_ADMIN') && surveyConfig?.surveyInfo.owner?.id == contextService.getOrg()?.id) {
            return true
        }

        if (!surveyConfig.pickAndChoose) {
            return false
        }

        if (surveyConfig.surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (accessService.checkPermAffiliationX('ORG_BASIC_MEMBER', 'INST_EDITOR', 'ROLE_ADMIN')) {

            if (SurveyOrg.findByOrgAndSurveyConfig(org, surveyConfig)?.finishDate) {
                return false
            } else {
                return true
            }
        } else {
            return false
        }

    }

    Map<String, Object> getParticipantConfigNavigation(Org org, SurveyInfo surveyInfo, SurveyConfig surveyConfig) {
        Map<String, Object> result = [:]
        def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo?.surveyConfigs).sort { it.surveyConfig.configOrder }

        int currentOrder = surveyConfig?.configOrder
        List<Integer> configOrders = SurveyConfig.findAllByIdInList(surveyResults.findAll { it.surveyConfig.type == 'Subscription' }.groupBy { it.surveyConfig.id }.keySet()).configOrder
        int currentOrderIndex = configOrders.indexOf(currentOrder)

        if (currentOrderIndex > 0) {
            result.prev = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :prev and sc.surveyInfo = :surInfo', [prev: configOrders.get(currentOrderIndex - 1), surInfo: surveyInfo])[0]
        }
        if (currentOrderIndex < configOrders.size() - 1) {
            result.next = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :next and sc.surveyInfo = :surInfo', [next: configOrders.get(currentOrderIndex + 1), surInfo: surveyInfo])[0]
        }

        result.total = configOrders.size()

        return result

    }

    Map<String, Object> getConfigNavigation(SurveyInfo surveyInfo, SurveyConfig surveyConfig) {
        Map<String, Object> result = [:]
        int currentOrder = surveyConfig?.configOrder
        List<Integer> configOrders = surveyInfo?.surveyConfigs?.sort { it.configOrder }.configOrder
        int currentOrderIndex = configOrders.indexOf(currentOrder)

        if (currentOrderIndex > 0) {
            result.prev = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :prev and sc.surveyInfo = :surInfo', [prev: configOrders.get(currentOrderIndex - 1), surInfo: surveyInfo])[0]
        }
        if (currentOrderIndex < configOrders.size() - 1) {
            result.next = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :next and sc.surveyInfo = :surInfo', [next: configOrders.get(currentOrderIndex + 1), surInfo: surveyInfo])[0]
        }

        result.total = configOrders.size()

        return result

    }

    boolean isContinueToParticipate(Org org, SurveyConfig surveyConfig) {
        def participationProperty = RDStore.SURVEY_PROPERTY_PARTICIPATION

        def result = SurveyResult.findBySurveyConfigAndParticipantAndType(surveyConfig, org, participationProperty)?.getResult() == RDStore.YN_YES ? true : false

        return result
    }

    boolean copyProperties(List<AbstractProperty> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties) {
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
            if ((!targetProp) || isAddNewProp) {
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


    boolean deleteProperties(List<AbstractProperty> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties) {

        properties.each { AbstractProperty prop ->
            AuditConfig.removeAllConfigs(prop)
        }

        int anzCP = SubscriptionCustomProperty.executeUpdate("delete from SubscriptionCustomProperty p where p in (:properties)", [properties: properties])
        int anzPP = SubscriptionPrivateProperty.executeUpdate("delete from SubscriptionPrivateProperty p where p in (:properties)", [properties: properties])
    }

    private boolean save(obj, flash) {
        if (obj.save(flush: true)) {
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, locale)
            return false
        }
    }

    def exportSurveys(List<SurveyConfig> surveyConfigs, Org contextOrg) {
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()

        Map sheetData = [:]

        surveyConfigs.each { surveyConfig ->
            List titles = []
            List surveyData = []

            boolean exportForSurveyOwner = (surveyConfig.surveyInfo.owner.id == contextOrg.id)

            if (exportForSurveyOwner) {
                titles.addAll([messageSource.getMessage('surveyParticipants.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('org.sortname.label', null, LocaleContextHolder.getLocale())])
                if (surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose) {
                    titles.push(messageSource.getMessage('surveyProperty.subName', null, LocaleContextHolder.getLocale()))
                }
                if (surveyConfig.type == 'GeneralSurvey') {
                    titles.addAll([messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyConfig.url.label', null, LocaleContextHolder.getLocale())])
                }

                titles.add(messageSource.getMessage('surveyConfigsInfo.comment', null, LocaleContextHolder.getLocale()))

                if (surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose) {
                    titles.addAll([messageSource.getMessage('surveyProperty.subProvider', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyProperty.subAgency', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('license.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.packages.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('default.status.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.kind.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.form.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.resource.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.isPublicForApi.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.hasPerpetualAccess.label', null, LocaleContextHolder.getLocale())])

                    if (surveyConfig.subSurveyUseForTransfer) {
                        titles.addAll([messageSource.getMessage('surveyConfigsInfo.newPrice', null, LocaleContextHolder.getLocale()),
                                       messageSource.getMessage('financials.billingCurrency', null, LocaleContextHolder.getLocale()),
                                       messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, LocaleContextHolder.getLocale())])
                    }
                }

                surveyConfig.surveyProperties.each {
                    titles.addAll([messageSource.getMessage('surveyProperty.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('default.type.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyResult.result', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyResult.comment', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyResult.commentOnlyForOwner', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyResult.finishDate', null, LocaleContextHolder.getLocale())])
                }

            } else {
                titles.push(messageSource.getMessage('surveyInfo.owner.label', null, LocaleContextHolder.getLocale()))
                titles.push(messageSource.getMessage('surveyConfigsInfo.comment', null, LocaleContextHolder.getLocale()))
                if (surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose) {
                    titles.addAll([messageSource.getMessage('surveyProperty.subName', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyProperty.subProvider', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyProperty.subAgency', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('license.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.packages.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('default.status.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.kind.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.form.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.resource.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.isPublicForApi.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('subscription.hasPerpetualAccess.label', null, LocaleContextHolder.getLocale())])
                    if (surveyConfig.subSurveyUseForTransfer) {
                        titles.push(messageSource.getMessage('surveyConfigsInfo.newPrice', null, LocaleContextHolder.getLocale()))
                        titles.push(messageSource.getMessage('financials.billingCurrency', null, LocaleContextHolder.getLocale()))
                        titles.push(messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, LocaleContextHolder.getLocale()))
                    }
                }
                if (surveyConfig.type == 'GeneralSurvey') {
                    titles.push(messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyConfig.url.label', null, LocaleContextHolder.getLocale()))
                }
            }

            Subscription subscription

            if (exportForSurveyOwner) {
                String surveyName = surveyConfig.getConfigNameShort()
                surveyConfig.orgs.sort{it.org.sortname}.each { surveyOrg ->
                    List row = []

                    row.add([field: surveyOrg.org.name ?: '', style: null])
                    row.add([field: surveyOrg.org.sortname ?: '', style: null])
                    if (surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose) {

                        subscription = OrgRole.findByOrgAndRoleTypeAndSubInList(surveyOrg.org, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyConfig.subscription))?.sub ?: null
                        row.add([field: subscription?.name ?: surveyName ?: '', style: null])
                    }
                    if (surveyConfig.type == 'GeneralSurvey') {
                        row.add([field: surveyName ?: '', style: null])
                        row.add([field: surveyConfig.url ?: '', style: null])
                    }
                    row.add([field: surveyConfig.comment ?: '', style: null])

                    if (surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose) {
                        //Performance lastig providers und agencies
                        row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                        row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                        row.add([field: subscription?.owner?.reference ?: '', style: null])
                        List packageNames = subscription?.packages?.collect {
                            it.pkg.name
                        }
                        row.add([field: packageNames ? packageNames.join(", ") : '', style: null])

                        row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription?.type?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription?.form?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription?.resource?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                        row.add([field: subscription?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                        if (surveyConfig.subSurveyUseForTransfer) {
                            CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, surveyOrg.org), RDStore.COST_ITEM_DELETED)

                            row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                            row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                            row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                        }
                    }

                    SurveyResult.findAllBySurveyConfigAndParticipant(surveyConfig, surveyOrg.org).each { surResult ->
                        row.add([field: surResult.type?.getI10n('name') ?: '', style: null])
                        row.add([field: PropertyDefinition.getLocalizedValue(surResult.type.type) ?: '', style: null])

                        String value = ""

                        if (surResult.type.type == Integer.toString()) {
                            value = surResult?.intValue ? surResult.intValue.toString() : ""
                        } else if (surResult.type.type == String.toString()) {
                            value = surResult.stringValue ?: ""
                        } else if (surResult.type.type == BigDecimal.toString()) {
                            value = surResult.decValue ? surResult.decValue.toString() : ""
                        } else if (surResult.type.type == Date.toString()) {
                            value = surResult.dateValue ? sdf.format(surResult.dateValue) : ""
                        } else if (surResult.type.type == URL.toString()) {
                            value = surResult.urlValue ? surResult.urlValue.toString() : ""
                        } else if (surResult.type.type == RefdataValue.toString()) {
                            value = surResult.refValue ? surResult.refValue.getI10n('value') : ""
                        }

                        row.add([field: value ?: '', style: null])
                        row.add([field: surResult.comment ?: '', style: null])
                        row.add([field: surResult.participantComment ?: '', style: null])
                        row.add([field: surResult.finishDate ? sdf.format(surResult.finishDate) : '', style: null])


                    }
                    surveyData.add(row)
                }
            } else {

                List row = []

                row.add([field: surveyConfig.surveyInfo.owner.name ?: '', style: null])
                row.add([field: surveyConfig.comment ?: '', style: null])

                if (surveyConfig.type == 'Subscription' && !surveyConfig.pickAndChoose) {
                    subscription = surveyConfig.subscription.getDerivedSubscriptionBySubscribers(contextOrg) ?: null
                    row.add([field: subscription?.name ?: surveyConfig.getConfigNameShort() ?: "", style: null])
                    row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                    row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                    row.add([field: subscription?.owner?.reference ?: '', style: null])
                    List packageNames = subscription?.packages?.collect {
                        it.pkg.name
                    }
                    row.add([field: packageNames ? packageNames.join(", ") : '', style: null])
                    row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.type?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.form?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.resource?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    row.add([field: subscription?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                    if (surveyConfig.subSurveyUseForTransfer) {
                        CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)

                        row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                        row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                    }
                }

                if (surveyConfig.type == 'GeneralSurvey') {
                    row.add([field: surveyConfig.getConfigNameShort() ?: '', style: null])
                    row.add([field: surveyConfig.url ?: '', style: null])
                }

                surveyData.add(row)
                surveyData.add([])
                surveyData.add([])
                surveyData.add([])
                List row2 = [[field: messageSource.getMessage('surveyProperty.label', null, LocaleContextHolder.getLocale()), style: 'bold'],
                             [field: messageSource.getMessage('default.type.label', null, LocaleContextHolder.getLocale()), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.result', null, LocaleContextHolder.getLocale()), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.comment', null, LocaleContextHolder.getLocale()), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.commentOnlyForParticipant', null, LocaleContextHolder.getLocale()), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.finishDate', null, LocaleContextHolder.getLocale()), style: 'bold']]
                surveyData.add(row2)


                SurveyResult.findAllBySurveyConfigAndParticipant(surveyConfig, contextOrg).each { surResult ->
                    List row3 = []
                    row3.add([field: surResult.type?.getI10n('name') ?: '', style: null])
                    row3.add([field: PropertyDefinition.getLocalizedValue(surResult.type.type) ?: '', style: null])

                    String value = ""

                    if (surResult.type.type == Integer.toString()) {
                        value = surResult?.intValue ? surResult.intValue.toString() : ""
                    } else if (surResult.type.type == String.toString()) {
                        value = surResult.stringValue ?: ""
                    } else if (surResult.type.type == BigDecimal.toString()) {
                        value = surResult.decValue ? surResult.decValue.toString() : ""
                    } else if (surResult.type.type == Date.toString()) {
                        value = surResult.dateValue ? sdf.format(surResult.dateValue) : ""
                    } else if (surResult.type.type == URL.toString()) {
                        value = surResult.urlValue ? surResult.urlValue.toString() : ""
                    } else if (surResult.type.type == RefdataValue.toString()) {
                        value = surResult.refValue ? surResult.refValue.getI10n('value') : ""
                    }

                    row3.add([field: value ?: '', style: null])
                    row3.add([field: surResult.comment ?: '', style: null])
                    row3.add([field: surResult.participantComment ?: '', style: null])
                    row3.add([field: surResult.finishDate ? sdf.format(surResult.finishDate) : '', style: null])

                    surveyData.add(row3)
                }
            }
            sheetData.put(escapeService.escapeString(surveyConfig.getConfigNameShort()), [titleRow: titles, columnData: surveyData])
        }

        return exportService.generateXLSXWorkbook(sheetData)
    }
}
