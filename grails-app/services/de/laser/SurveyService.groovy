package de.laser


import com.k_int.kbplus.ExportService
import com.k_int.kbplus.GenericOIDService
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.finance.CostItem
import de.laser.helper.*
import de.laser.properties.PropertyDefinition
import de.laser.system.SystemEvent
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import grails.plugins.mail.MailService
import grails.util.Holders
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.i18n.LocaleContextHolder

import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat

@Transactional
class SurveyService {

    AccessService accessService
    ContextService contextService
    def messageSource
    ExportService exportService
    MailService mailService
    EscapeService escapeService
    GrailsApplication grailsApplication
    String replyTo
    GenericOIDService genericOIDService

    SimpleDateFormat formatter = DateUtils.getSDF_dmy()
    String from

    @javax.annotation.PostConstruct
    void init() {
        from = ConfigUtils.getNotificationsEmailFrom()
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
    }


    boolean isEditableSurvey(Org org, SurveyInfo surveyInfo) {

        if (accessService.checkPermAffiliationX('ORG_CONSORTIUM', 'INST_EDITOR', 'ROLE_ADMIN') && surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (accessService.checkPermAffiliationX('ORG_BASIC_MEMBER', 'INST_EDITOR', 'ROLE_ADMIN')) {
            def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo.surveyConfigs)

            if (surveyResults) {
                return surveyResults.finishDate.contains(null) ? true : false
            } else {
                return false
            }
        }else{
            return false
        }


    }

    boolean isEditableIssueEntitlementsSurvey(Org org, SurveyConfig surveyConfig) {

        if (accessService.checkPermAffiliationX('ORG_CONSORTIUM', 'INST_EDITOR', 'ROLE_ADMIN') && surveyConfig.surveyInfo.owner?.id == contextService.getOrg().id) {
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

    Map<String, Object> getConfigNavigation(SurveyInfo surveyInfo, SurveyConfig surveyConfig) {
        Map<String, Object> result = [:]
        int currentOrder = surveyConfig.configOrder
        List<Integer> configOrders = surveyInfo.surveyConfigs?.sort { it.configOrder }.configOrder
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

    private boolean save(obj, flash) {
        if (obj.save()) {
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, LocaleContextHolder.getLocale())
            return false
        }
    }

    def exportSurveys(List<SurveyConfig> surveyConfigs, Org contextOrg) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        Map sheetData = [:]

        surveyConfigs.each { surveyConfig ->
            List titles = []
            List surveyData = []

            boolean exportForSurveyOwner = (surveyConfig.surveyInfo.owner.id == contextOrg.id)

            if (exportForSurveyOwner) {
                titles.addAll([messageSource.getMessage('org.sortname.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyParticipants.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyOrg.ownerComment.label', null, LocaleContextHolder.getLocale())])
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                    titles.push(messageSource.getMessage('surveyProperty.subName', null, LocaleContextHolder.getLocale()))
                }
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    titles.addAll([messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale())])
                }

                titles.addAll([
                        messageSource.getMessage('surveyconfig.url.label', null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage('surveyconfig.urlComment.label', null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage('surveyconfig.url2.label', null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage('surveyconfig.urlComment2.label', null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage('surveyconfig.url3.label', null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage('surveyconfig.urlComment3.label', null, LocaleContextHolder.getLocale()),
                        messageSource.getMessage('surveyConfigsInfo.comment', null, LocaleContextHolder.getLocale())])

                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                    titles.addAll([messageSource.getMessage('subscription.comment.label', null, LocaleContextHolder.getLocale()),
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
                titles.push(messageSource.getMessage('surveyInfo.endDate.label', null, LocaleContextHolder.getLocale()))
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                    titles.addAll([messageSource.getMessage('surveyconfig.url.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyconfig.urlComment.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyconfig.url2.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyconfig.urlComment2.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyconfig.url3.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyconfig.urlComment3.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyProperty.subName', null, LocaleContextHolder.getLocale()),
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
                        titles.addAll([messageSource.getMessage('surveyconfig.scheduledStartDate.label', null, LocaleContextHolder.getLocale()),
                                       messageSource.getMessage('surveyconfig.scheduledEndDate.label', null, LocaleContextHolder.getLocale()),
                                       messageSource.getMessage('surveyConfigsInfo.newPrice', null, LocaleContextHolder.getLocale()),
                                       messageSource.getMessage('financials.billingCurrency', null, LocaleContextHolder.getLocale()),
                                       messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, LocaleContextHolder.getLocale())])
                    }
                }
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    titles.push(messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyconfig.url.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyconfig.urlComment.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyconfig.url2.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyconfig.urlComment2.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyconfig.url3.label', null, LocaleContextHolder.getLocale()))
                    titles.push(messageSource.getMessage('surveyconfig.urlComment3.label', null, LocaleContextHolder.getLocale()))
                }
            }

            Subscription subscription

            if (exportForSurveyOwner) {
                String surveyName = surveyConfig.getConfigNameShort()
                surveyConfig.orgs.sort{it.org.sortname}.each { surveyOrg ->
                    List row = []

                    row.add([field: surveyOrg.org.sortname ?: '', style: null])
                    row.add([field: surveyOrg.org.name ?: '', style: null])
                    row.add([field: surveyOrg.ownerComment ?: '', style: null])

                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                        OrgRole orgRole = Subscription.findAllByInstanceOf(surveyConfig.subscription) ? OrgRole.findByOrgAndRoleTypeAndSubInList(surveyOrg.org, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyConfig.subscription)) : null
                        subscription =  orgRole ? orgRole.sub : null
                        row.add([field: subscription?.name ?: surveyName ?: '', style: null])
                    }
                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                        row.add([field: surveyName ?: '', style: null])
                    }
                    row.add([field: surveyConfig.url ?: '', style: null])
                    row.add([field: surveyConfig.urlComment ?: '', style: null])
                    row.add([field: surveyConfig.url2 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment2 ?: '', style: null])
                    row.add([field: surveyConfig.url3 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment3 ?: '', style: null])
                    row.add([field: surveyConfig.comment ?: '', style: null])

                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                        row.add([field: subscription?.comment ? subscription.comment : '', style: null])
                        //Performance lastig providers und agencies
                        row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                        row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                        List licenseNames = []
                        Links.findAllByDestinationAndLinkType(genericOIDService.getOID(subscription),RDStore.LINKTYPE_LICENSE).each { Links li ->
                            License l = (License) genericOIDService.resolveOID(li.source)
                            licenseNames << l.reference
                        }
                        row.add([field: licenseNames ? licenseNames.join(", ") : '', style: null])
                        List packageNames = subscription?.packages?.collect {
                            it.pkg.name
                        }
                        row.add([field: packageNames ? packageNames.join(", ") : '', style: null])

                        row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription?.kind?.getI10n("value") ?: '', style: null])
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

                    SurveyResult.findAllBySurveyConfigAndParticipant(surveyConfig, surveyOrg.org).sort{it.type.name}.each { surResult ->
                        row.add([field: surResult.type?.getI10n('name') ?: '', style: null])
                        row.add([field: PropertyDefinition.getLocalizedValue(surResult.type.type) ?: '', style: null])

                        String value = ""

                        if (surResult.type.isIntegerType()) {
                            value = surResult?.intValue ? surResult.intValue.toString() : ""
                        } else if (surResult.type.isStringType()) {
                            value = surResult.stringValue ?: ""
                        } else if (surResult.type.isBigDecimalType()) {
                            value = surResult.decValue ? surResult.decValue.toString() : ""
                        } else if (surResult.type.isDateType()) {
                            value = surResult.dateValue ? sdf.format(surResult.dateValue) : ""
                        } else if (surResult.type.isURLType()) {
                            value = surResult.urlValue ? surResult.urlValue.toString() : ""
                        } else if (surResult.type.isRefdataValueType()) {
                            value = surResult.refValue ? surResult.refValue.getI10n('value') : ""
                        }

                        row.add([field: value ?: '', style: null])
                        row.add([field: surResult.comment ?: '', style: null])
                        row.add([field: surResult.ownerComment ?: '', style: null])
                        row.add([field: surResult.finishDate ? sdf.format(surResult.finishDate) : '', style: null])


                    }
                    surveyData.add(row)
                }
            } else {

                List row = []

                row.add([field: surveyConfig.surveyInfo.owner.name ?: '', style: null])
                row.add([field: surveyConfig.comment ?: '', style: null])
                row.add([field: surveyConfig.surveyInfo.endDate ? Date.parse('yyyy-MM-dd hh:mm:SS.S', surveyConfig.surveyInfo.endDate.toString()).format("dd.MM.yyy") : '', style: null])

                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                    row.add([field: surveyConfig.url ?: '', style: null])
                    row.add([field: surveyConfig.urlComment ?: '', style: null])
                    row.add([field: surveyConfig.url2 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment2 ?: '', style: null])
                    row.add([field: surveyConfig.url3 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment3 ?: '', style: null])
                    subscription = surveyConfig.subscription.getDerivedSubscriptionBySubscribers(contextOrg) ?: null
                    row.add([field: subscription?.name ?: surveyConfig.getConfigNameShort() ?: "", style: null])
                    row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                    row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                    List licenseNames = []
                    Links.findAllByDestinationAndLinkType(genericOIDService.getOID(subscription),RDStore.LINKTYPE_LICENSE).each { Links li ->
                        License l = (License) genericOIDService.resolveOID(li.source)
                        licenseNames << l.reference
                    }
                    row.add([field: licenseNames ? licenseNames.join(", ") : '', style: null])
                    List packageNames = subscription?.packages?.collect {
                        it.pkg.name
                    }
                    row.add([field: packageNames ? packageNames.join(", ") : '', style: null])
                    row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.kind?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.form?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.resource?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    row.add([field: subscription?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                    if (surveyConfig.subSurveyUseForTransfer) {
                        CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)

                        row.add([field: surveyConfig.scheduledStartDate ? Date.parse('yyyy-MM-dd hh:mm:SS.S', surveyConfig.scheduledStartDate.toString()).format("dd.MM.yyy"): '', style: null])
                        row.add([field: surveyConfig.scheduledEndDate ? Date.parse('yyyy-MM-dd hh:mm:SS.S', surveyConfig.scheduledEndDate.toString()).format("dd.MM.yyy"): '', style: null])
                        row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                        row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                    }
                }

                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    row.add([field: surveyConfig.getConfigNameShort() ?: '', style: null])
                    row.add([field: surveyConfig.url ?: '', style: null])
                    row.add([field: surveyConfig.urlComment ?: '', style: null])
                    row.add([field: surveyConfig.url2 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment2 ?: '', style: null])
                    row.add([field: surveyConfig.url3 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment3 ?: '', style: null])
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


                SurveyResult.findAllBySurveyConfigAndParticipant(surveyConfig, contextOrg).sort{it.type.name}.each { surResult ->
                    List row3 = []
                    row3.add([field: surResult.type?.getI10n('name') ?: '', style: null])
                    row3.add([field: PropertyDefinition.getLocalizedValue(surResult.type.type) ?: '', style: null])

                    String value = ""

                    if (surResult.type.isIntegerType()) {
                        value = surResult?.intValue ? surResult.intValue.toString() : ""
                    } else if (surResult.type.isStringType()) {
                        value = surResult.stringValue ?: ""
                    } else if (surResult.type.isBigDecimalType()) {
                        value = surResult.decValue ? surResult.decValue.toString() : ""
                    } else if (surResult.type.isDateType()) {
                        value = surResult.dateValue ? sdf.format(surResult.dateValue) : ""
                    } else if (surResult.type.isURLType()) {
                        value = surResult.urlValue ? surResult.urlValue.toString() : ""
                    } else if (surResult.type.isRefdataValueType()) {
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

    def exportSurveyCostItems(List<SurveyConfig> surveyConfigs, Org contextOrg) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        Map sheetData = [:]

        if (contextOrg.getCustomerType()  == 'ORG_CONSORTIUM') {
            surveyConfigs.each { surveyConfig ->
                List titles = []
                List surveyData = []

                titles.addAll([messageSource.getMessage('org.sortname.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyParticipants.label', null, LocaleContextHolder.getLocale())])
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT ) {
                    titles.push(messageSource.getMessage('surveyProperty.subName', null, LocaleContextHolder.getLocale()))
                }
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    titles.addAll([messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale())])
                }

                titles.addAll([messageSource.getMessage('surveyconfig.url.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyconfig.urlComment.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyconfig.url2.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyconfig.urlComment2.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyconfig.url3.label', null, LocaleContextHolder.getLocale()),
                               messageSource.getMessage('surveyconfig.urlComment3.label', null, LocaleContextHolder.getLocale())])

                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT ) {
                    titles.addAll([messageSource.getMessage('surveyProperty.subProvider', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyProperty.subAgency', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('default.status.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('financials.costItemElement', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('financials.costInBillingCurrency', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('financials.billingCurrency', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('default.startDate.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('default.endDate.label', null, LocaleContextHolder.getLocale()),
                                   messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, LocaleContextHolder.getLocale())])
                }

                Subscription subscription

                String surveyName = surveyConfig.getConfigNameShort()
                surveyConfig.orgs.sort { it.org.sortname }.each { surveyOrg ->
                    List row = []

                    row.add([field: surveyOrg.org.sortname ?: '', style: null])
                    row.add([field: surveyOrg.org.name ?: '', style: null])

                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                        OrgRole orgRole = Subscription.findAllByInstanceOf(surveyConfig.subscription) ? OrgRole.findByOrgAndRoleTypeAndSubInList(surveyOrg.org, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyConfig.subscription)) : null
                        subscription =  orgRole ? orgRole.sub : null
                        row.add([field: subscription?.name ?: surveyName ?: '', style: null])
                    }
                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                        row.add([field: surveyName ?: '', style: null])
                    }

                    row.add([field: surveyConfig.url ?: '', style: null])
                    row.add([field: surveyConfig.urlComment ?: '', style: null])
                    row.add([field: surveyConfig.url2 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment2 ?: '', style: null])
                    row.add([field: surveyConfig.url3 ?: '', style: null])
                    row.add([field: surveyConfig.urlComment3 ?: '', style: null])

                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                        row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                        row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                        row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])

                        CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, surveyOrg.org), RDStore.COST_ITEM_DELETED)

                        if (surveyCostItem) {
                            row.add([field: surveyCostItem?.costItemElement?.getI10n('value') ?: '', style: null])
                            row.add([field: surveyCostItem?.costInBillingCurrency ?: '', style: null])
                            row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                            row.add([field: surveyCostItem?.taxKey ? surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)" : '', style: null])
                            row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                            row.add([field: surveyCostItem?.startDate ? formatter.format(surveyCostItem.startDate): '', style: null])
                            row.add([field: surveyCostItem?.endDate ? formatter.format(surveyCostItem.endDate): '', style: null])
                            row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                        }
                    }

                    surveyData.add(row)
                    sheetData.put(escapeService.escapeString(surveyConfig.getConfigNameShort()), [titleRow: titles, columnData: surveyData])
                }
            }
        } else {
            List titles = []
            List surveyData = []

            titles.addAll([messageSource.getMessage('surveyInfo.owner.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.url.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.urlComment.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.url2.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.urlComment2.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.url3.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.urlComment3.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyInfo.type.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyProperty.subProvider', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyProperty.subAgency', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('default.status.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('financials.costItemElement', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('financials.costInBillingCurrency', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('financials.billingCurrency', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('default.startDate.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('default.endDate.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, LocaleContextHolder.getLocale())])


            surveyConfigs.each { surveyConfig ->

                List row = []
                Subscription subscription

                String surveyName = surveyConfig.getConfigNameShort()

                row.add([field: surveyConfig.surveyInfo.owner.name ?: '', style: null])
                row.add([field: surveyConfig.url ?: '', style: null])
                row.add([field: surveyConfig.urlComment ?: '', style: null])
                row.add([field: surveyConfig.url2 ?: '', style: null])
                row.add([field: surveyConfig.urlComment2 ?: '', style: null])
                row.add([field: surveyConfig.url3 ?: '', style: null])
                row.add([field: surveyConfig.urlComment3 ?: '', style: null])
                row.add([field: surveyName ?: '', style: null])
                row.add([field: surveyConfig.surveyInfo.type?.getI10n('value') ?: '', style: null])

                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                    subscription = surveyConfig.subscription.getDerivedSubscriptionBySubscribers(contextOrg) ?: null
                    row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                    row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                    row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])

                    CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)

                    if (surveyCostItem) {
                        row.add([field: surveyCostItem?.costItemElement?.getI10n('value') ?: '', style: null])
                        row.add([field: surveyCostItem?.costInBillingCurrency ?: '', style: null])
                        row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                        row.add([field: surveyCostItem?.taxKey ? surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)" : '', style: null])
                        row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem?.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                        row.add([field: surveyCostItem?.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                        row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                    }
                }
                surveyData.add(row)
            }

            sheetData.put(escapeService.escapeString(messageSource.getMessage('survey.exportSurveyCostItems', null, LocaleContextHolder.getLocale())), [titleRow: titles, columnData: surveyData])

        }

        return exportService.generateXLSXWorkbook(sheetData)
    }

    def emailToSurveyOwnerbyParticipationFinish(SurveyInfo surveyInfo, Org participationFinish){

        if (grailsApplication.config.grails.mail.disabled == true) {
            println 'surveyService.emailToSurveyOwnerbyParticipationFinish() failed due grailsApplication.config.grails.mail.disabled = true'
            return false
        }

        if(surveyInfo.owner)
        {
            //Only User that approved
            List<UserOrg> userOrgs = UserOrg.findAllByOrgAndStatus(surveyInfo.owner, 1)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if(userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_PARTICIPATION_FINISH) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {

                    User user = userOrg.user
                    Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())
                    String emailReceiver = user.getEmail()
                    String currentServer = ServerUtils.getCurrentServer()
                    String subjectSystemPraefix = (currentServer == ServerUtils.SERVER_PROD)? "" : (ConfigUtils.getLaserSystemId() + " - ")
                    String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + surveyInfo.type.getI10n('value', language) + ": " + surveyInfo.name +  " (" + participationFinish.sortname + ")")

                        try {
                            if (emailReceiver == null || emailReceiver.isEmpty()) {
                                log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                            } else {
                                boolean isNotificationCCbyEmail = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                                String ccAddress = null
                                if (isNotificationCCbyEmail){
                                    ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                                }

                                List surveyResults = SurveyResult.findAllByParticipantAndSurveyConfig(participationFinish, surveyInfo.surveyConfigs[0]).sort { it.surveyConfig.configOrder }

                                if (isNotificationCCbyEmail && ccAddress) {
                                    mailService.sendMail {
                                        to      emailReceiver
                                        from    from
                                        cc      ccAddress
                                        subject mailSubject
                                        html    (view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                    }
                                } else {
                                    mailService.sendMail {
                                        to      emailReceiver
                                        from from
                                        subject mailSubject
                                        html    (view: "/mailTemplates/html/notificationSurveyParticipationFinish", model: [user: user, org: participationFinish, survey: surveyInfo, surveyResults: surveyResults])
                                    }
                                }

                                log.debug("emailToSurveyOwnerbyParticipationFinish - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + surveyInfo.owner.name);
                            }
                        } catch (Exception e) {
                            String eMsg = e.message

                            log.error("emailToSurveyOwnerbyParticipationFinish - sendSurveyEmail() :: Unable to perform email due to exception ${eMsg}")
                            SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: participationFinish.name, survey: surveyInfo.name])
                        }
                }
            }

        }

    }

    def exportSurveysOfParticipant(List surveyConfigs, Org participant) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        Map sheetData = [:]
            List titles = []
            List surveyData = []

            titles.addAll([messageSource.getMessage('org.sortname.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyParticipants.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyInfo.name.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.url.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.urlComment.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.url2.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.urlComment2.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.url3.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyconfig.urlComment3.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyConfigsInfo.comment', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyProperty.subProvider', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyProperty.subAgency', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('license.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('subscription.packages.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('default.status.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('subscription.kind.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('subscription.form.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('subscription.resource.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('subscription.isPublicForApi.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('subscription.hasPerpetualAccess.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyConfigsInfo.newPrice', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('financials.billingCurrency', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyProperty.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('default.type.label', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyResult.result', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyResult.comment', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyResult.commentOnlyForOwner', null, LocaleContextHolder.getLocale()),
                           messageSource.getMessage('surveyResult.finishDate', null, LocaleContextHolder.getLocale())])

            List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(participant, surveyConfigs)
            surveyResults.each { surveyResult ->

                    Subscription subscription
                    String surveyName = surveyResult.surveyConfig.getConfigNameShort()
                        List row = []

                        row.add([field: surveyResult.participant.sortname ?: '', style: null])
                        row.add([field: surveyResult.participant.name ?: '', style: null])

                        if (surveyResult.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyResult.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                            OrgRole orgRole = Subscription.findAllByInstanceOf(surveyResult.surveyConfig.subscription) ? OrgRole.findByOrgAndRoleTypeAndSubInList(participant, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyResult.surveyConfig.subscription)) : null
                            subscription =  orgRole ? orgRole.sub : null
                            row.add([field: subscription?.name ?: surveyName ?: '', style: null])
                        }
                        if (surveyResult.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                            row.add([field: surveyName ?: '', style: null])

                        }
                        row.add([field: surveyResult.surveyConfig.url ?: '', style: null])
                        row.add([field: surveyResult.surveyConfig.urlComment ?: '', style: null])
                        row.add([field: surveyResult.surveyConfig.url2 ?: '', style: null])
                        row.add([field: surveyResult.surveyConfig.urlComment2 ?: '', style: null])
                        row.add([field: surveyResult.surveyConfig.url3 ?: '', style: null])
                        row.add([field: surveyResult.surveyConfig.urlComment3 ?: '', style: null])
                        row.add([field: surveyResult.surveyConfig.comment ?: '', style: null])

                        if (surveyResult.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION || surveyResult.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {
                            //Performance lastig providers und agencies
                            row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                            row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                            List licenseNames = []
                            Links.findAllByDestinationAndLinkType(genericOIDService.getOID(subscription),RDStore.LINKTYPE_LICENSE).each { Links li ->
                                License l = (License) genericOIDService.resolveOID(li.source)
                                licenseNames << l.reference
                            }
                            row.add([field: licenseNames ? licenseNames.join(", ") : '', style: null])

                            List packageNames = subscription?.packages?.collect {
                                it.pkg.name
                            }
                            row.add([field: packageNames ? packageNames.join(", ") : '', style: null])

                            row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription?.kind?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription?.form?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription?.resource?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription?.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                            row.add([field: subscription?.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                                CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyResult.surveyConfig, participant), RDStore.COST_ITEM_DELETED)

                                row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                                row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                                row.add([field: surveyCostItem?.costDescription ?: '', style: null])

                        }else {
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                        }

                            row.add([field: surveyResult.type?.getI10n('name') ?: '', style: null])
                            row.add([field: PropertyDefinition.getLocalizedValue(surveyResult.type.type) ?: '', style: null])

                            String value = ""

                            if (surveyResult.type.isIntegerType()) {
                                value = surveyResult?.intValue ? surveyResult.intValue.toString() : ""
                            } else if (surveyResult.type.isStringType()) {
                                value = surveyResult.stringValue ?: ""
                            } else if (surveyResult.type.isBigDecimalType()) {
                                value = surveyResult.decValue ? surveyResult.decValue.toString() : ""
                            } else if (surveyResult.type.isDateType()) {
                                value = surveyResult.dateValue ? sdf.format(surveyResult.dateValue) : ""
                            } else if (surveyResult.type.isURLType()) {
                                value = surveyResult.urlValue ? surveyResult.urlValue.toString() : ""
                            } else if (surveyResult.type.isRefdataValueType()) {
                                value = surveyResult.refValue ? surveyResult.refValue.getI10n('value') : ""
                            }

                            row.add([field: value ?: '', style: null])
                            row.add([field: surveyResult.comment ?: '', style: null])
                            row.add([field: surveyResult.ownerComment ?: '', style: null])
                            row.add([field: surveyResult.finishDate ? sdf.format(surveyResult.finishDate) : '', style: null])



                        surveyData.add(row)


                sheetData.put(escapeService.escapeString(messageSource.getMessage('surveyInfo.members', null, LocaleContextHolder.getLocale())), [titleRow: titles, columnData: surveyData])
            }


        return exportService.generateXLSXWorkbook(sheetData)
    }

    def emailsToSurveyUsers(List surveyInfoIds){

        def surveys = SurveyInfo.findAllByIdInList(surveyInfoIds)

        def orgs = surveys?.surveyConfigs?.orgs?.org?.flatten()

        if(orgs)
        {
            //Only User that approved
            List<UserOrg> userOrgs = UserOrg.findAllByOrgInListAndStatus(orgs, 1)

            //Only User with Notification by Email and for Surveys Start
            userOrgs.each { userOrg ->
                if(userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                        userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {

                    def orgSurveys = SurveyInfo.executeQuery("SELECT s FROM SurveyInfo s " +
                            "LEFT JOIN s.surveyConfigs surConf " +
                            "LEFT JOIN surConf.orgs surOrg  " +
                            "WHERE surOrg.org IN (:org) " +
                            "AND s.id IN (:survey)", [org: userOrg.org, survey: surveys?.id])

                    sendSurveyEmail(userOrg.user, userOrg.org, orgSurveys, false)
                }
            }

        }

    }

    def emailsToSurveyUsersOfOrg(SurveyInfo surveyInfo, Org org, boolean reminderMail){

        //Only User that approved
        List<UserOrg> userOrgs = UserOrg.findAllByOrgAndStatus(org, UserOrg.STATUS_APPROVED)

        //Only User with Notification by Email and for Surveys Start
        userOrgs.each { userOrg ->
            if(userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                    userOrg.user.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
            {
                sendSurveyEmail(userOrg.user, userOrg.org, [surveyInfo], reminderMail)
            }
        }
    }

    private void sendSurveyEmail(User user, Org org, List<SurveyInfo> surveyEntries, boolean reminderMail) {

        if (grailsApplication.config.grails.mail.disabled == true) {
            println 'SurveyService.sendSurveyEmail() failed due grailsApplication.config.grails.mail.disabled = true'
        }else {

            String emailReceiver = user.getEmail()
            String currentServer = ServerUtils.getCurrentServer()
            String subjectSystemPraefix = (currentServer == ServerUtils.SERVER_PROD) ? "LAS:eR - " : (ConfigUtils.getLaserSystemId() + " - ")

            surveyEntries.each { survey ->
                try {
                    if (emailReceiver == null || emailReceiver.isEmpty()) {
                        log.debug("The following user does not have an email address and can not be informed about surveys: " + user.username);
                    } else {
                        boolean isNotificationCCbyEmail = user.getSetting(UserSetting.KEYS.IS_NOTIFICATION_CC_BY_EMAIL, RDStore.YN_NO)?.rdValue == RDStore.YN_YES
                        String ccAddress = null
                        if (isNotificationCCbyEmail) {
                            ccAddress = user.getSetting(UserSetting.KEYS.NOTIFICATION_CC_EMAILADDRESS, null)?.getValue()
                        }

                        List generalContactsEMails = []

                        survey.owner.getGeneralContactPersons(true)?.each { person ->
                            person.contacts.each { contact ->
                                if (['Mail', 'E-Mail'].contains(contact.contentType?.value)) {
                                    generalContactsEMails << contact.content
                                }
                            }
                        }

                        replyTo = (generalContactsEMails.size() > 0) ? generalContactsEMails[0].toString() : null
                        Locale language = new Locale(user.getSetting(UserSetting.KEYS.LANGUAGE_OF_EMAILS, RefdataValue.getByValueAndCategory('de', RDConstants.LANGUAGE)).value.toString())
                        Object[] args = ["${survey.type.getI10n('value', language)}"]
                        String mailSubject = escapeService.replaceUmlaute(subjectSystemPraefix + (reminderMail ? messageSource.getMessage('email.subject.surveysReminder', args, language)  : messageSource.getMessage('email.subject.surveys', args, language)) + " " + survey.name + "")

                        if (isNotificationCCbyEmail && ccAddress) {
                            mailService.sendMail {
                                multipart true
                                to emailReceiver
                                from from
                                cc ccAddress
                                replyTo replyTo
                                subject mailSubject
                                text view: "/mailTemplates/text/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                                html view: "/mailTemplates/html/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                            }
                        } else {
                            mailService.sendMail {
                                multipart true
                                to emailReceiver
                                from from
                                replyTo replyTo
                                subject mailSubject
                                text view: "/mailTemplates/text/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                                html view: "/mailTemplates/html/notificationSurvey", model: [language: language, survey: survey, reminder: reminderMail]
                            }
                        }

                        log.debug("SurveyService - finished sendSurveyEmail() to " + user.displayName + " (" + user.email + ") " + org.name);
                    }
                } catch (Exception e) {
                    String eMsg = e.message

                    log.error("SurveyService - sendSurveyEmail() :: Unable to perform email due to exception ${eMsg}")
                    SystemEvent.createEvent('SUS_SEND_MAIL_ERROR', [user: user.getDisplayName(), org: org.name, survey: survey.name])
                }
            }
        }
    }

    List getfilteredSurveyOrgs(List orgIDs, String query, queryParams, params) {

        if (!(orgIDs?.size() > 0)) {
            return []
        }
        String tmpQuery = query
        tmpQuery = tmpQuery.replace("order by", "and o.id in (:orgIDs) order by")

        Map tmpQueryParams = queryParams
        tmpQueryParams.put("orgIDs", orgIDs)
        //println(tmpQueryParams)
        //println(tmpQuery)

        return Org.executeQuery(tmpQuery, tmpQueryParams, params)
    }

    Map<String,Object> getSurveyConfigCounts() {
        Map<String, Object> result = [:]

        Org contextOrg = contextService.getOrg()

        result.created = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and (surInfo.status = :status or surInfo.status = :status2)",
                [contextOrg: contextOrg, status: RDStore.SURVEY_READY, status2: RDStore.SURVEY_IN_PROCESSING]).size()

        result.active = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_SURVEY_STARTED]).size()

        result.finish = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_SURVEY_COMPLETED]).size()

        result.inEvaluation = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_IN_EVALUATION]).size()


        return result
    }

    List getSurveyProperties(Org contextOrg) {
        List props = []

        //private Property
        PropertyDefinition.getAllByDescrAndTenant(PropertyDefinition.SVY_PROP, contextOrg).each { it ->
            props << it

        }

        //global Property
        PropertyDefinition.getAllByDescr(PropertyDefinition.SVY_PROP).each { it ->
            props << it

        }

        props.sort { a, b -> a.getI10n('name').compareToIgnoreCase b.getI10n('name') }

        return props
    }

    boolean addSurPropToSurvey(SurveyConfig surveyConfig, PropertyDefinition surveyProperty) {

        if (!SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(surveyProperty, surveyConfig) && surveyProperty && surveyConfig) {
            SurveyConfigProperties propertytoSub = new SurveyConfigProperties(surveyConfig: surveyConfig, surveyProperty: surveyProperty)
            if(propertytoSub.save()){
                return true
            }else {
                return false
            }
        }else {
            return false
        }
    }

    def addSubMembers(SurveyConfig surveyConfig) {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()
        result.user = contextService.getUser()

        result.editable = (accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR') && surveyConfig.surveyInfo.owner.id == result.institution.id)

        if (!result.editable) {
            return
        }

        List orgs = []
        List currentMembersSubs = subscriptionService.getValidSurveySubChilds(surveyConfig.subscription)

        currentMembersSubs.each{ sub ->
            orgs.addAll(sub.getAllSubscribers())
        }

        if (orgs) {

            orgs.each { org ->

                if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org))) {

                    boolean existsMultiYearTerm = false
                    Subscription sub = surveyConfig.subscription
                    if (sub && !surveyConfig.pickAndChoose && surveyConfig.subSurveyUseForTransfer) {
                        Subscription subChild = sub.getDerivedSubscriptionBySubscribers(org)

                        if (subChild && subChild.isCurrentMultiYearSubscriptionNew()) {
                            existsMultiYearTerm = true
                        }

                    }
                    if (!existsMultiYearTerm) {
                        SurveyOrg surveyOrg = new SurveyOrg(
                                surveyConfig: surveyConfig,
                                org: org
                        )

                        if (!surveyOrg.save()) {
                            log.debug("Error by add Org to SurveyOrg ${surveyOrg.errors}");
                        }else{
                            if(surveyConfig.surveyInfo.status in [RDStore.SURVEY_READY, RDStore.SURVEY_SURVEY_STARTED]) {
                                surveyConfig.surveyProperties.each { property ->

                                    SurveyResult surveyResult = new SurveyResult(
                                            owner: result.institution,
                                            participant: org ?: null,
                                            startDate: surveyConfig.surveyInfo.startDate,
                                            endDate: surveyConfig.surveyInfo.endDate ?: null,
                                            type: property.surveyProperty,
                                            surveyConfig: surveyConfig
                                    )

                                    if (surveyResult.save()) {
                                        log.debug( surveyResult.toString() )
                                    } else {
                                        log.error("Not create surveyResult: " + surveyResult)
                                    }
                                }

                                if (surveyConfig.surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED) {
                                    emailsToSurveyUsersOfOrg(surveyConfig.surveyInfo, org, false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void copySurveyConfigCharacteristic(SurveyConfig oldSurveyConfig, SurveyConfig newSurveyConfig, params){
        oldSurveyConfig.documents.each { dctx ->
            //Copy Docs
            if (params.copySurvey.copyDocs) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (dctx.status != RDStore.DOC_CTX_STATUS_DELETED)) {
                    Doc clonedContents = new Doc(
                            status: dctx.owner.status,
                            type: dctx.owner.type,
                            content: dctx.owner.content,
                            uuid: dctx.owner.uuid,
                            contentType: dctx.owner.contentType,
                            title: dctx.owner.title,
                            filename: dctx.owner.filename,
                            mimeType: dctx.owner.mimeType,
                            migrated: dctx.owner.migrated,
                            owner: dctx.owner.owner
                    ).save()
                    String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'
                    Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                    Path target = new File("${fPath}/${clonedContents.uuid}").toPath()
                    Files.copy(source, target)
                    new DocContext(
                            owner: clonedContents,
                            surveyConfig: newSurveyConfig,
                            domain: dctx.domain,
                            status: dctx.status,
                            doctype: dctx.doctype
                    ).save()
                }
            }
            //Copy Announcements
            if (params.copySurvey.copyAnnouncements) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status != RDStore.DOC_CTX_STATUS_DELETED)) {
                    Doc clonedContents = new Doc(
                            status: dctx.owner.status,
                            type: dctx.owner.type,
                            content: dctx.owner.content,
                            uuid: dctx.owner.uuid,
                            contentType: dctx.owner.contentType,
                            title: dctx.owner.title,
                            filename: dctx.owner.filename,
                            mimeType: dctx.owner.mimeType,
                            migrated: dctx.owner.migrated
                    ).save()
                    new DocContext(
                            owner: clonedContents,
                            surveyConfig: newSurveyConfig,
                            domain: dctx.domain,
                            status: dctx.status,
                            doctype: dctx.doctype
                    ).save()
                }
            }
        }

        //Copy Tasks
        if (params.copySurvey.copyTasks) {
            Task.findAllBySurveyConfig(oldSurveyConfig).each { Task  task ->
                Task newTask = new Task()
                InvokerHelper.setProperties(newTask, task.properties)
                newTask.systemCreateDate = new Date()
                newTask.surveyConfig = newSurveyConfig
                newTask.save()
            }
        }
        //Copy Participants
        if (params.copySurvey.copyParticipants) {
            oldSurveyConfig.orgs.each { SurveyOrg surveyOrg ->
                new SurveyOrg(surveyConfig: newSurveyConfig, org: surveyOrg.org).save()
            }
        }
        //Copy Properties
        if (params.copySurvey.copySurveyProperties) {
            oldSurveyConfig.surveyProperties.each { SurveyConfigProperties surveyConfigProperty ->
                new SurveyConfigProperties(
                        surveyProperty: surveyConfigProperty.surveyProperty,
                        surveyConfig: newSurveyConfig).save()
            }
        }
    }

}
