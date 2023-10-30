package de.laser

import de.laser.auth.User
import de.laser.finance.CostItem
import de.laser.config.ConfigDefaults
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.stats.Counter4ApiSource
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5ApiSource
import de.laser.stats.Counter5Report
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.survey.SurveyUrl
import de.laser.config.ConfigMapper
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.gsp.PageRenderer
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.text.SimpleDateFormat

/**
 * This service manages survey handling
 */
@Transactional
class SurveyService {

    AccessService accessService
    ContextService contextService
    EscapeService escapeService
    ExportService exportService
    FilterService filterService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    SubscriptionService subscriptionService
    MailSendService mailSendService

    PageRenderer groovyPageRenderer

    SimpleDateFormat formatter = DateUtils.getSDF_ddMMyyyy()
    String from

    /**
     * Constructor method
     */
    @javax.annotation.PostConstruct
    void init() {
        from = ConfigMapper.getNotificationsEmailFrom()
        messageSource = BeanStore.getMessageSource()
    }

    /**
     * Checks if the given survey information is editable by the given institution
     * @param org the institution to check
     * @param surveyInfo the survey information which should be accessed
     * @return true if the survey data can be manipulated, false otherwise
     */
    boolean isEditableSurvey(Org org, SurveyInfo surveyInfo) {

        if (contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO ) && surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )) {
            SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfigInList(org, surveyInfo.surveyConfigs)

            if (surveyOrg.finishDate) {
                return false
            } else {
                return true
            }
        }else{
            return false
        }
    }

    @Deprecated
    boolean isEditableIssueEntitlementsSurvey(Org org, SurveyConfig surveyConfig) {

        if (contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO ) && surveyConfig.surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (!surveyConfig.pickAndChoose) {
            return false
        }

        if (surveyConfig.surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (contextService.hasPermAsInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )) {

            if (SurveyOrg.findByOrgAndSurveyConfig(org, surveyConfig)?.finishDate) {
                return false
            } else {
                return true
            }
        } else {
            return false
        }

    }

    /**
     * Builds the navigation between the given surveys
     * @param surveyInfo the survey within which navigation should be possible
     * @param surveyConfig the surveys to be linked
     * @return the map containing the previous and the next objects
     */
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

    @Deprecated
    boolean isContinueToParticipate(Org org, SurveyConfig surveyConfig) {
        PropertyDefinition participationProperty = PropertyStore.SURVEY_PROPERTY_PARTICIPATION

        def result = SurveyResult.findBySurveyConfigAndParticipantAndType(surveyConfig, org, participationProperty)?.getResult() == RDStore.YN_YES ? true : false

        return result
    }

    @Deprecated
    private boolean _save(obj, flash) {
        if (obj.save()) {
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, LocaleUtils.getCurrentLocale())
            return false
        }
    }

    /**
     * Exports the given surveys
     * @param surveyConfigs the surveys to export
     * @param contextOrg the institution whose perspective should be taken
     * @return an Excel worksheet containing the survey data
     */
    def exportSurveys(List<SurveyConfig> surveyConfigs, Org contextOrg) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()
        Map sheetData = [:]

        surveyConfigs.each { surveyConfig ->
            List titles = []
            List surveyData = []

            boolean exportForSurveyOwner = (surveyConfig.surveyInfo.owner.id == contextOrg.id)

            if (exportForSurveyOwner) {
                titles.addAll([messageSource.getMessage('org.sortname.label', null, locale),
                               messageSource.getMessage('surveyParticipants.label', null, locale),
                               messageSource.getMessage('surveyOrg.ownerComment.label', null, locale)])
                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                    titles.add(messageSource.getMessage('surveyProperty.subName', null, locale))
                }
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    titles.addAll([messageSource.getMessage('surveyInfo.name.label', null, locale)])
                }

                surveyConfig.surveyUrls.eachWithIndex { SurveyUrl surveyUrl, int i ->
                    Object[] args = ["${i+1}"]
                    titles.addAll([
                            messageSource.getMessage('surveyconfig.url.label', args, locale),
                            messageSource.getMessage('surveyconfig.urlComment.label', args, locale)])
                }
                titles.addAll([messageSource.getMessage('surveyConfigsInfo.comment', null, locale)])

                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                    titles.addAll([messageSource.getMessage('subscription.comment.label', null, locale),
                                   messageSource.getMessage('surveyProperty.subProvider', null, locale),
                                   messageSource.getMessage('surveyProperty.subAgency', null, locale),
                                   messageSource.getMessage('license.label', null, locale),
                                   messageSource.getMessage('subscription.packages.label', null, locale),
                                   messageSource.getMessage('default.status.label', null, locale),
                                   messageSource.getMessage('subscription.kind.label', null, locale),
                                   messageSource.getMessage('subscription.form.label', null, locale),
                                   messageSource.getMessage('subscription.resource.label', null, locale),
                                   messageSource.getMessage('subscription.isPublicForApi.label', null, locale),
                                   messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale)])

                    if (surveyConfig.subSurveyUseForTransfer) {
                        titles.addAll([messageSource.getMessage('surveyConfigsInfo.newPrice', null, locale),
                                       messageSource.getMessage('default.currency.label', null, locale),
                                       messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale)])
                    }
                }

                surveyConfig.surveyProperties.sort { it.surveyProperty.getI10n('name') }.each {
                    titles.addAll([messageSource.getMessage('surveyProperty.label', null, locale),
                                   messageSource.getMessage('default.type.label', null, locale),
                                   messageSource.getMessage('surveyResult.result', null, locale),
                                   messageSource.getMessage('surveyResult.comment', null, locale),
                                   messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale),
                                   messageSource.getMessage('surveyOrg.finishDate', null, locale)])
                }

            } else {
                titles.add(messageSource.getMessage('surveyInfo.owner.label', null, locale))
                titles.add(messageSource.getMessage('surveyConfigsInfo.comment', null, locale))
                titles.add(messageSource.getMessage('surveyInfo.endDate.label', null, locale))
                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                    surveyConfig.surveyUrls.eachWithIndex { SurveyUrl surveyUrl, int i ->
                        Object[] args = ["${i+1}"]
                        titles.addAll([
                                messageSource.getMessage('surveyconfig.url.label', args, locale),
                            messageSource.getMessage('surveyconfig.urlComment.label', args, locale)])
                    }
                    titles.addAll([messageSource.getMessage('surveyProperty.subName', null, locale),
                                   messageSource.getMessage('surveyProperty.subProvider', null, locale),
                                   messageSource.getMessage('surveyProperty.subAgency', null, locale),
                                   messageSource.getMessage('license.label', null, locale),
                                   messageSource.getMessage('subscription.packages.label', null, locale),
                                   messageSource.getMessage('default.status.label', null, locale),
                                   messageSource.getMessage('subscription.kind.label', null, locale),
                                   messageSource.getMessage('subscription.form.label', null, locale),
                                   messageSource.getMessage('subscription.resource.label', null, locale),
                                   messageSource.getMessage('subscription.isPublicForApi.label', null, locale),
                                   messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale)])
                    if (surveyConfig.subSurveyUseForTransfer) {
                        titles.addAll([messageSource.getMessage('surveyconfig.scheduledStartDate.label', null, locale),
                                       messageSource.getMessage('surveyconfig.scheduledEndDate.label', null, locale),
                                       messageSource.getMessage('surveyConfigsInfo.newPrice', null, locale),
                                       messageSource.getMessage('default.currency.label', null, locale),
                                       messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale)])
                    }
                }
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    titles.add(messageSource.getMessage('surveyInfo.name.label', null, locale))
                    surveyConfig.surveyUrls.eachWithIndex { SurveyUrl surveyUrl, int i ->
                        Object[] args = ["${i+1}"]
                        titles.add(messageSource.getMessage('surveyconfig.url.label', args, locale))
                        titles.add(messageSource.getMessage('surveyconfig.urlComment.label', args, locale))
                    }
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

                    if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {

                        //OrgRole orgRole = Subscription.findAllByInstanceOf(surveyConfig.subscription) ? OrgRole.findByOrgAndRoleTypeAndSubInList(surveyOrg.org, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyConfig.subscription)) : null
                        //subscription =  orgRole ? orgRole.sub : null
                        row.add([field: surveyName ?: '', style: null])
                    }
                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                        row.add([field: surveyName ?: '', style: null])
                    }
                    surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                        row.add([field: surveyUrl.url ?: '', style: null])
                        row.add([field: surveyUrl.urlComment ?: '', style: null])
                    }

                    row.add([field: surveyConfig.comment ?: '', style: null])

                    if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                        row.add([field: subscription?.comment ? subscription.comment : '', style: null])
                        //Performance lastig providers und agencies
                        row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                        row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                        List licenseNames = []
                        Links.findAllByDestinationSubscriptionAndLinkType(subscription,RDStore.LINKTYPE_LICENSE).each { Links li ->
                            License l = li.sourceLicense
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

                    SurveyResult.findAllBySurveyConfigAndParticipant(surveyConfig, surveyOrg.org).sort{it.type.getI10n('name')}.each { surResult ->
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
                        row.add([field: surveyOrg.finishDate ? sdf.format(surveyOrg.finishDate) : '', style: null])


                    }
                    surveyData.add(row)
                }
            } else {

                List row = []

                row.add([field: surveyConfig.surveyInfo.owner.name ?: '', style: null])
                row.add([field: surveyConfig.comment ?: '', style: null])
                row.add([field: surveyConfig.surveyInfo.endDate ? DateUtils.getSDF_ddMMyyy().format( DateUtils.getSDF_yyyyMMdd_hhmmSSS().parse(surveyConfig.surveyInfo.endDate.toString()) ) : '', style: null])

                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                    surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                        row.add([field: surveyUrl.url ?: '', style: null])
                        row.add([field: surveyUrl.urlComment ?: '', style: null])
                    }

                    subscription = surveyConfig.subscription.getDerivedSubscriptionBySubscribers(contextOrg) ?: null
                    row.add([field: surveyConfig.getConfigNameShort() ?: "", style: null])
                    row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                    row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                    List licenseNames = []
                    Links.findAllByDestinationSubscriptionAndLinkType(subscription, RDStore.LINKTYPE_LICENSE).each { Links li ->
                        License l = li.sourceLicense
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
                        row.add([field: surveyConfig.scheduledStartDate ? DateUtils.getSDF_ddMMyyy().format( DateUtils.getSDF_yyyyMMdd_hhmmSSS().parse(surveyConfig.scheduledStartDate.toString()) ): '', style: null])
                        row.add([field: surveyConfig.scheduledEndDate ? DateUtils.getSDF_ddMMyyy().format( DateUtils.getSDF_yyyyMMdd_hhmmSSS().parse(surveyConfig.scheduledEndDate.toString()) ): '', style: null])
                        row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                        row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                    }
                }

                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    row.add([field: surveyConfig.getConfigNameShort() ?: '', style: null])
                    surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                        row.add([field: surveyUrl.url ?: '', style: null])
                        row.add([field: surveyUrl.urlComment ?: '', style: null])
                    }
                }

                surveyData.add(row)
                surveyData.add([])
                surveyData.add([])
                surveyData.add([])
                List row2 = [[field: messageSource.getMessage('surveyProperty.label', null, locale), style: 'bold'],
                             [field: messageSource.getMessage('default.type.label', null, locale), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.result', null, locale), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.comment', null, locale), style: 'bold'],
                             [field: messageSource.getMessage('surveyResult.commentOnlyForParticipant', null, locale), style: 'bold'],
                             [field: messageSource.getMessage('surveyOrg.finishDate', null, locale), style: 'bold']]
                surveyData.add(row2)

                SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(contextOrg, surveyConfig)
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
                    row3.add([field: surveyOrg.finishDate ? sdf.format(surveyOrg.finishDate) : '', style: null])

                    surveyData.add(row3)
                }
            }
            sheetData.put(escapeService.escapeString(surveyConfig.getConfigNameShort()), [titleRow: titles, columnData: surveyData])
        }

        if(sheetData.isEmpty()){
            sheetData.put(messageSource.getMessage('survey.plural', null, locale), [titleRow: [], columnData: []])
        }

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the cost items of the given surveys
     * @param surveyConfigs the surveys whose cost items should be exported
     * @param contextOrg the institution whose perspective should be taken
     * @return an Excel worksheet containing the cost item data
     */
    def exportSurveyCostItems(List<SurveyConfig> surveyConfigs, Org contextOrg) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()

        Map sheetData = [:]

        if (contextOrg.isCustomerType_Consortium_Pro()) {
            surveyConfigs.each { surveyConfig ->
                List titles = []
                List surveyData = []

                titles.addAll([messageSource.getMessage('org.sortname.label', null, locale),
                               messageSource.getMessage('surveyParticipants.label', null, locale)])
                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT] ) {
                    titles.add(messageSource.getMessage('surveyProperty.subName', null, locale))
                }
                if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                    titles.addAll([messageSource.getMessage('surveyInfo.name.label', null, locale)])
                }

                surveyConfig.surveyUrls.eachWithIndex { SurveyUrl surveyUrl, int i ->
                    Object[] args = ["${i+1}"]
                    titles.addAll([
                            messageSource.getMessage('surveyconfig.url.label', args, locale),
                            messageSource.getMessage('surveyconfig.urlComment.label', args, locale)])
                }

                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT] ) {
                    titles.addAll([messageSource.getMessage('surveyProperty.subProvider', null, locale),
                                   messageSource.getMessage('surveyProperty.subAgency', null, locale),
                                   messageSource.getMessage('default.status.label', null, locale),
                                   messageSource.getMessage('financials.costItemElement', null, locale),
                                   messageSource.getMessage('financials.costInBillingCurrency', null, locale),
                                   messageSource.getMessage('default.currency.label', null, locale),
                                   messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, locale),
                                   messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, locale),
                                   messageSource.getMessage('default.startDate.export.label', null, locale),
                                   messageSource.getMessage('default.endDate.export.label', null, locale),
                                   messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale)])
                }

                Subscription subscription

                String surveyName = surveyConfig.getConfigNameShort()
                surveyConfig.orgs.sort { it.org.sortname }.each { surveyOrg ->
                    List row = []

                    row.add([field: surveyOrg.org.sortname ?: '', style: null])
                    row.add([field: surveyOrg.org.name ?: '', style: null])

                    if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {

                        //OrgRole orgRole = Subscription.findAllByInstanceOf(surveyConfig.subscription) ? OrgRole.findByOrgAndRoleTypeAndSubInList(surveyOrg.org, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyConfig.subscription)) : null
                        //subscription =  orgRole ? orgRole.sub : null
                        row.add([field: surveyName ?: '', style: null])
                    }
                    if (surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                        row.add([field: surveyName ?: '', style: null])
                    }

                    surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                        row.add([field: surveyUrl.url ?: '', style: null])
                        row.add([field: surveyUrl.urlComment ?: '', style: null])
                    }

                    if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                        row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                        row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                        row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])

                        CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, surveyOrg.org), RDStore.COST_ITEM_DELETED)

                        if (surveyCostItem) {
                            row.add([field: surveyCostItem?.costItemElement?.getI10n('value') ?: '', style: null])
                            row.add([field: surveyCostItem?.costInBillingCurrency ?: '', style: null])
                            row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                            String surveyCostTax
                            if(surveyCostItem?.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                            else if(surveyCostItem?.taxKey)
                                surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                            else
                                surveyCostTax = ''
                            row.add([field: surveyCostTax, style: null])
                            if(surveyCostItem?.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                row.add([field: '', style: null])
                            else
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

            titles.addAll([messageSource.getMessage('surveyInfo.owner.label', null, locale)])

            surveyConfigs[0].surveyUrls.eachWithIndex { SurveyUrl surveyUrl, int i ->
                Object[] args = ["${i+1}"]
                titles.addAll([
                        messageSource.getMessage('surveyconfig.url.label', args, locale),
                        messageSource.getMessage('surveyconfig.urlComment.label', args, locale)])
            }
            titles.addAll([messageSource.getMessage('surveyInfo.name.label', null, locale),
                           messageSource.getMessage('surveyInfo.type.label', null, locale),
                           messageSource.getMessage('surveyProperty.subProvider', null, locale),
                           messageSource.getMessage('surveyProperty.subAgency', null, locale),
                           messageSource.getMessage('default.status.label', null, locale),
                           messageSource.getMessage('financials.costItemElement', null, locale),
                           messageSource.getMessage('financials.costInBillingCurrency', null, locale),
                           messageSource.getMessage('default.currency.label', null, locale),
                           messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, locale),
                           messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, locale),
                           messageSource.getMessage('default.startDate.export.label', null, locale),
                           messageSource.getMessage('default.endDate.export.label', null, locale),
                           messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale)])


            surveyConfigs.each { surveyConfig ->

                List row = []
                Subscription subscription

                String surveyName = surveyConfig.getConfigNameShort()

                row.add([field: surveyConfig.surveyInfo.owner.name ?: '', style: null])

                surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                    row.add([field: surveyUrl.url ?: '', style: null])
                    row.add([field: surveyUrl.urlComment ?: '', style: null])
                }

                row.add([field: surveyName ?: '', style: null])
                row.add([field: surveyConfig.surveyInfo.type?.getI10n('value') ?: '', style: null])

                if (surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                    subscription = surveyConfig.subscription.getDerivedSubscriptionBySubscribers(contextOrg) ?: null
                    row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                    row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                    row.add([field: subscription?.status?.getI10n("value") ?: '', style: null])

                    CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)

                    if (surveyCostItem) {
                        row.add([field: surveyCostItem?.costItemElement?.getI10n('value') ?: '', style: null])
                        row.add([field: surveyCostItem?.costInBillingCurrency ?: '', style: null])
                        row.add([field: surveyCostItem?.billingCurrency?.value ?: '', style: null])
                        String surveyCostTax
                        if(surveyCostItem?.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                            surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                        else if(surveyCostItem?.taxKey)
                            surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                        else
                            surveyCostTax = ''
                        row.add([field: surveyCostTax, style: null])
                        if(surveyCostItem?.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                            row.add([field: '', style: null])
                        else
                            row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem?.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                        row.add([field: surveyCostItem?.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                        row.add([field: surveyCostItem?.costDescription ?: '', style: null])
                    }
                }
                surveyData.add(row)
            }

            sheetData.put(escapeService.escapeString(messageSource.getMessage('survey.exportSurveyCostItems', null, locale)), [titleRow: titles, columnData: surveyData])

        }
        return exportService.generateXLSXWorkbook(sheetData)
    }





    /**
     * Exports the surveys of the given participant
     * @param surveyConfigs the surveys in which the given institution takes part
     * @param participant the participant institution
     * @return an Excel worksheet containing the export
     */
    def exportSurveysOfParticipant(List surveyConfigs, Org participant) {
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()

        Map sheetData = [:]
            List titles = []
            List surveyData = []

            titles.addAll([messageSource.getMessage('org.sortname.label', null, locale),
                           messageSource.getMessage('surveyParticipants.label', null, locale),
                           messageSource.getMessage('surveyInfo.name.label', null, locale)])

        surveyConfigs[0].surveyUrls.eachWithIndex { SurveyUrl surveyUrl, int i ->
            Object[] args = ["${i+1}"]
            titles.addAll([
                    messageSource.getMessage('surveyconfig.url.label', args, locale),
                    messageSource.getMessage('surveyconfig.urlComment.label', args, locale)])
        }

        titles.addAll([messageSource.getMessage('surveyConfigsInfo.comment', null, locale),
                           messageSource.getMessage('surveyProperty.subProvider', null, locale),
                           messageSource.getMessage('surveyProperty.subAgency', null, locale),
                           messageSource.getMessage('license.label', null, locale),
                           messageSource.getMessage('subscription.packages.label', null, locale),
                           messageSource.getMessage('default.status.label', null, locale),
                           messageSource.getMessage('subscription.kind.label', null, locale),
                           messageSource.getMessage('subscription.form.label', null, locale),
                           messageSource.getMessage('subscription.resource.label', null, locale),
                           messageSource.getMessage('subscription.isPublicForApi.label', null, locale),
                           messageSource.getMessage('subscription.hasPerpetualAccess.label', null, locale),
                           messageSource.getMessage('surveyConfigsInfo.newPrice', null, locale),
                           messageSource.getMessage('default.currency.label', null, locale),
                           messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale),
                           messageSource.getMessage('surveyProperty.label', null, locale),
                           messageSource.getMessage('default.type.label', null, locale),
                           messageSource.getMessage('surveyResult.result', null, locale),
                           messageSource.getMessage('surveyResult.comment', null, locale),
                           messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale),
                           messageSource.getMessage('surveyOrg.finishDate', null, locale)])

            List<SurveyResult> surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(participant, surveyConfigs)

            surveyResults.each { surveyResult ->
                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyResult.surveyConfig, participant)
                    Subscription subscription
                    String surveyName = surveyResult.surveyConfig.getConfigNameShort()
                        List row = []

                        row.add([field: surveyResult.participant.sortname ?: '', style: null])
                        row.add([field: surveyResult.participant.name ?: '', style: null])

                        if (surveyResult.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {

                            //OrgRole orgRole = Subscription.findAllByInstanceOf(surveyResult.surveyConfig.subscription) ? OrgRole.findByOrgAndRoleTypeAndSubInList(participant, RDStore.OR_SUBSCRIBER_CONS, Subscription.findAllByInstanceOf(surveyResult.surveyConfig.subscription)) : null
                            //subscription =  orgRole ? orgRole.sub : null
                            row.add([field: surveyName ?: '', style: null])
                        }
                        if (surveyResult.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY) {
                            row.add([field: surveyName ?: '', style: null])

                        }

                        surveyResult.surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                            row.add([field: surveyUrl.url ?: '', style: null])
                            row.add([field: surveyUrl.urlComment ?: '', style: null])
                        }

                        row.add([field: surveyResult.surveyConfig.comment ?: '', style: null])

                        if (surveyResult.surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]) {
                            //Performance lastig providers und agencies
                            row.add([field: subscription?.providers ? subscription?.providers?.join(", ") : '', style: null])
                            row.add([field: subscription?.agencies ? subscription?.agencies?.join(", ") : '', style: null])

                            List licenseNames = []
                            Links.findAllByDestinationSubscriptionAndLinkType(subscription,RDStore.LINKTYPE_LICENSE).each { Links li ->
                                licenseNames << li.sourceLicense.reference
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

                                CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqual(surveyOrg, RDStore.COST_ITEM_DELETED)

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
                            row.add([field: surveyOrg.finishDate ? sdf.format(surveyOrg.finishDate) : '', style: null])

                        surveyData.add(row)

                sheetData.put(escapeService.escapeString(messageSource.getMessage('surveyInfo.members', null, locale)), [titleRow: titles, columnData: surveyData])
            }
        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Sends an email to the given survey participants
     * @param surveyInfoIds the IDs of the survey participations
     */
    void emailsToSurveyUsers(List surveyInfoIds){

        def surveys = SurveyInfo.findAllByIdInList(surveyInfoIds)
        def orgs = surveys?.surveyConfigs?.orgs?.org?.flatten()

        if(orgs)
        {
            //Only User that approved
            List<User> formalUserList = orgs ? User.findAllByFormalOrgInList(orgs) : []

            //Only User with Notification by Email and for Surveys Start
            formalUserList.each { fu ->
                if (fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                        fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
                {
                    List<SurveyInfo> orgSurveys = SurveyInfo.executeQuery("SELECT s FROM SurveyInfo s " +
                            "LEFT JOIN s.surveyConfigs surConf " +
                            "LEFT JOIN surConf.orgs surOrg  " +
                            "WHERE surOrg.org IN (:org) " +
                            "AND s.id IN (:survey)", [org: fu.formalOrg, survey: surveys?.id])

                    mailSendService.sendSurveyEmail(fu, fu.formalOrg, orgSurveys, false)
                }
            }
        }
    }

    /**
     * Sends mails to the users of the given institution
     * @param surveyInfo the survey information to be sent
     * @param org the institution whose users should be notified
     * @param reminderMail is it a reminder about the survey completion?
     */
    void emailsToSurveyUsersOfOrg(SurveyInfo surveyInfo, Org org, boolean reminderMail){

        //Only User that approved
        List<User> formalUserList = org ? User.findAllByFormalOrg(org) : []

        //Only User with Notification by Email and for Surveys Start
        formalUserList.each { fu ->
            if (fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_FOR_SURVEYS_START) == RDStore.YN_YES &&
                    fu.getSettingsValue(UserSetting.KEYS.IS_NOTIFICATION_BY_EMAIL) == RDStore.YN_YES)
            {
                mailSendService.sendSurveyEmail(fu, fu.formalOrg, [surveyInfo], reminderMail)
            }
        }
    }

    /**
     * Limits the given institution query to the set of institution IDs
     * @param orgIDs the institution IDs to fetch
     * @param query the query string
     * @param queryParams the query parameters
     * @param params the request parameter map
     * @return a list of institutions matching the filter
     */
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

    /**
     * Retrieves the counts of surveys in the different stages
     * @param parameterMap the filter parameter map
     * @return the counts for each survey stage
     */
    Map<String,Object> getSurveyConfigCounts(GrailsParameterMap parameterMap) {
        Map<String, Object> result = [:]

        Org contextOrg = contextService.getOrg()

        GrailsParameterMap tmpParams = (GrailsParameterMap) parameterMap.clone()

        result = _setSurveyConfigCounts(result, 'created', tmpParams, contextOrg)

        result = _setSurveyConfigCounts(result, 'active', tmpParams, contextOrg)

        result = _setSurveyConfigCounts(result, 'finish', tmpParams, contextOrg)

        result = _setSurveyConfigCounts(result, 'inEvaluation', tmpParams, contextOrg)

        result = _setSurveyConfigCounts(result, 'completed', tmpParams, contextOrg)

        return result
    }

    /**
     * Sets the count of surveys for the given tab
     * @param result the result map
     * @param tab the tab for which the count should be set
     * @param parameterMap the request parameter map
     * @param owner the context consortium
     * @return the map enriched with information
     */
    private Map _setSurveyConfigCounts(Map result, String tab, GrailsParameterMap parameterMap, Org owner){
        SimpleDateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()
        Map<String,Object> fsq = [:]

        def cloneParameterMap = parameterMap.clone()

        cloneParameterMap.tab = tab
        cloneParameterMap.remove('max')

        fsq = filterService.getSurveyConfigQueryConsortia(cloneParameterMap, sdFormat, owner)
        result."${tab}" =  SurveyInfo.executeQuery(fsq.query, fsq.queryParams, cloneParameterMap).size()

        return result
    }

    /**
     * Gets the survey properties for the given institution
     * @param contextOrg the institution whose survey properties should be retrieved
     * @return a sorted list of property definitions
     */
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

    /**
     * Adds the given survey property (= question) to the survey
     * @param surveyConfig the survey to which the property should be added
     * @param surveyProperty the survey property (= question) to add
     * @return true if the adding was successful, false otherwise
     */
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

    /**
     * Adds the members of the underlying subscription to the given survey
     * @param surveyConfig the survey config to which members should be added
     */
    void addSubMembers(SurveyConfig surveyConfig) {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()

        List currentMembersSubs = subscriptionService.getValidSurveySubChilds(surveyConfig.subscription)

        currentMembersSubs.each { Subscription subChild ->
                Org org = subChild.getSubscriber()

                if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org))) {

                    boolean existsMultiYearTerm = false

                    if (!surveyConfig.pickAndChoose && surveyConfig.subSurveyUseForTransfer) {


                        if (subChild.isCurrentMultiYearSubscriptionNew()) {
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

                                    if(!SurveyResult.findBySurveyConfigAndParticipantAndTypeAndOwner(surveyConfig, org, property.surveyProperty, result.institution)) {
                                        SurveyResult surveyResult = new SurveyResult(
                                                owner: result.institution,
                                                participant: org ?: null,
                                                startDate: surveyConfig.surveyInfo.startDate,
                                                endDate: surveyConfig.surveyInfo.endDate ?: null,
                                                type: property.surveyProperty,
                                                surveyConfig: surveyConfig
                                        )

                                        if (surveyResult.save()) {
                                            //log.debug(surveyResult.toString())
                                        } else {
                                            log.error("Not create surveyResult: " + surveyResult)
                                        }
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

    /**
     * Copies the documents, notes, tasks, participations and properties related to the given survey into the given new survey
     * @param oldSurveyConfig the survey from which data should be taken
     * @param newSurveyConfig the survey into which data should be copied
     * @param params the request parameter map
     */
    void copySurveyConfigCharacteristic(SurveyConfig oldSurveyConfig, SurveyConfig newSurveyConfig, params){
        oldSurveyConfig.documents.each { dctx ->
            //Copy Docs
            if (params.copySurvey.copyDocs) {
                if (dctx.isDocAFile() && (dctx.status != RDStore.DOC_CTX_STATUS_DELETED)) {
                    Doc clonedContents = new Doc(
                            type: dctx.getDocType(),
                            confidentiality: dctx.getDocConfid(),
                            content: dctx.owner.content,
                            uuid: dctx.owner.uuid,
                            contentType: dctx.owner.contentType,
                            title: dctx.owner.title,
                            filename: dctx.owner.filename,
                            mimeType: dctx.owner.mimeType,
                            migrated: dctx.owner.migrated,
                            owner: dctx.owner.owner,
                            server: dctx.owner.server
                    ).save()
                    String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                    Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                    Path target = new File("${fPath}/${clonedContents.uuid}").toPath()
                    Files.copy(source, target)
                    new DocContext(
                            owner: clonedContents,
                            surveyConfig: newSurveyConfig,
                            domain: dctx.domain,
                            status: dctx.status
                    ).save()
                }
            }
            //Copy Announcements
            if (params.copySurvey.copyAnnouncements) {
                if (dctx.isDocANote() && !(dctx.domain) && (dctx.status != RDStore.DOC_CTX_STATUS_DELETED)) {
                    Doc clonedContents = new Doc(
                            type: dctx.getDocType(),
                            confidentiality: dctx.getDocConfid(),
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
                            status: dctx.status
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

    /**
     * Gets the count map of survey participations for the given participant
     * @param participant the institution whose participations should be counted
     * @param parameterMap the request parameter map
     * @return the counts for each tab
     */
    private def _getSurveyParticipantCounts_New(Org participant, GrailsParameterMap parameterMap){
        Map<String, Object> result = [:]

        Org contextOrg = contextService.getOrg()

        GrailsParameterMap tmpParams = (GrailsParameterMap) parameterMap.clone()
        if (contextOrg.isCustomerType_Consortium_Pro()) {

            result = _setSurveyParticipantCounts(result, 'new', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'processed', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'finish', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'notFinish', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'termination', tmpParams, participant, contextOrg)


        }else {

            result = _setSurveyParticipantCounts(result, 'new', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'processed', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'finish', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'notFinish', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'termination', tmpParams, participant, null)
        }
        return result
    }

    /**
     * Sets the count of survey participations for the given tab
     * @param result the result map
     * @param tab the tab for which the count should be set
     * @param parameterMap the request parameter map
     * @param participant the participant whose participations should be counted
     * @param owner the context consortium
     * @return the map enriched with information
     */
    private Map _setSurveyParticipantCounts(Map result, String tab, GrailsParameterMap parameterMap, Org participant, Org owner = null){
        SimpleDateFormat sdFormat = DateUtils.getLocalizedSDF_noTime()
        Map fsq = [:]

        def cloneParameterMap = parameterMap.clone()

        if(owner){
            cloneParameterMap.owner = owner
        }

        cloneParameterMap.tab = tab
        cloneParameterMap.remove('max')

        fsq = filterService.getParticipantSurveyQuery_New(cloneParameterMap, sdFormat, participant)
        result."${tab}" = SurveyResult.executeQuery(fsq.query, fsq.queryParams, cloneParameterMap).groupBy { it.id[1] }.size()

        return result

    }

    @Deprecated
    private def _getSurveyParticipantCounts(Org participant){
        Map<String, Object> result = [:]

        result.new = SurveyInfo.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg left join surConfig.propertySet surResult where surOrg.org = :participant and (surResult.surveyConfig.surveyInfo.status = :status and surResult.id in (select sr.id from SurveyResult sr where sr.surveyConfig  = surveyConfig and sr.dateCreated = sr.lastUpdated and surOrg.finishDate is null))",
                [status: RDStore.SURVEY_SURVEY_STARTED,
                 participant: participant]).groupBy {it.id[1]}.size()

        result.processed = SurveyInfo.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg left join surConfig.propertySet surResult where surOrg.org = :participant and (surResult.surveyConfig.surveyInfo.status = :status and surResult.id in (select sr.id from SurveyResult sr where sr.surveyConfig  = surveyConfig and sr.dateCreated < sr.lastUpdated and surOrg.finishDate is null))",
                [status: RDStore.SURVEY_SURVEY_STARTED,
                 participant: participant]).groupBy {it.id[1]}.size()

        result.finish = SurveyInfo.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg where surOrg.org = :participant and (surOrg.finishDate is not null)",
                [participant: participant]).groupBy {it.id[1]}.size()

        result.notFinish = SurveyInfo.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig left join surConfig.orgs surOrg  where surOrg.org = :participant and surOrg.finishDate is null and (surInfo.status in (:status))",
                [status: [RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED],
                 participant: participant]).groupBy {it.id[1]}.size()
        return result
    }

    /**
     * Get the usage statistics for the given participant
     * @param result the result map with the base data
     * @param params the request parameter map
     * @param subscription the subscription to which usage details should be retrieved
     * @param participant the participant whose data should be retrieved
     * @param titles the title IDs upon which usage data may be restricted
     * @return the enriched result map with the usage data
     */
    Map<String, Object> getStatsForParticipant(Map<String, Object> result, GrailsParameterMap params, Subscription subscription, Org participant, List<Long> titles){
        Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: subscription])

        if(!subscribedPlatforms) {
            subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: subscription.instanceOf])
        }

        List count4check = [], count5check = [], monthsInRing = []
        if(!params.tabStat)
            params.tabStat = 'total'
        if(subscribedPlatforms && titles) {
            String sort, dateRange
            Map<String, Object> queryParams = [customer: participant, platforms: subscribedPlatforms]

            if(params.tabStat == 'total'){
                if (params.sort) {
                    sort = "${params.sort} ${params.order}"
                } else {
                    sort = "reportCount ${params.order ?: 'asc'}"
                }
            }else {
                if (params.sort) {
                    String secondarySort
                    switch (params.sort) {
                        case 'reportType': secondarySort = ", title.name asc, r.reportFrom desc"
                            break
                        case 'title.name': secondarySort = ", r.reportType asc, r.reportFrom desc"
                            break
                        case 'reportFrom': secondarySort = ", title.name asc, r.reportType asc"
                            break
                        default: secondarySort = ", title.name asc, r.reportType asc, r.reportFrom desc"
                            break
                    }
                    sort = "${params.sort} ${params.order} ${secondarySort}"
                } else {
                    sort = "title.name asc, r.reportType asc, r.reportFrom desc"
                }
            }
            Calendar startTime = GregorianCalendar.getInstance(), endTime = GregorianCalendar.getInstance(), now = GregorianCalendar.getInstance()

            Date newStartDate
            Date newEndDate

            use(TimeCategory) {
                newStartDate = new Date()-12.months
                newEndDate = new Date()+1.months
            }

            dateRange = " and r.reportFrom >= :startDate and r.reportTo <= :endDate "
            if (params.tabStat != 'total') {
                Calendar filterTime = GregorianCalendar.getInstance()
                Date filterDate = DateUtils.getSDF_yyyyMM().parse(params.tabStat)
                filterTime.setTime(filterDate)
                queryParams.startDate = filterDate
                filterTime.set(Calendar.DATE, filterTime.getActualMaximum(Calendar.DAY_OF_MONTH))
                queryParams.endDate = filterTime.getTime()
            } else {
                queryParams.startDate = newStartDate
                queryParams.endDate = newEndDate
            }
            startTime.setTime(newStartDate)
            endTime.setTime(newEndDate)


            while(startTime.before(endTime)) {
                monthsInRing << startTime.getTime()
                startTime.add(Calendar.MONTH, 1)
            }
            String filter = ""
            if(params.series_names) {
                filter += " and title.seriesName in (:seriesName) "
                queryParams.seriesName = params.list("series_names")
            }
            if(params.subject_references) {
                Set<String> subjectQuery = []
                params.list('subject_references').each { String subReference ->
                    subjectQuery << "genfunc_filter_matcher(title.subjectReference, '${subReference.toLowerCase()}') = true"
                }
                filter += " and (${subjectQuery.join(" or ")}) "
            }
            if(params.ddcs && params.list("ddcs").size() > 0) {
                filter += " and exists (select ddc.id from title.ddcs ddc where ddc.ddc.id in (:ddcs)) "
                queryParams.ddcs = []
                params.list("ddcs").each { String ddc ->
                    queryParams.ddcs << Long.parseLong(ddc)
                }
            }
            if(params.languages && params.list("languages").size() > 0) {
                filter += " and exists (select lang.id from title.languages lang where lang.language.id in (:languages)) "
                queryParams.languages = []
                params.list("languages").each { String lang ->
                    queryParams.languages << Long.parseLong(lang)
                }
            }

            if (params.filter) {
                filter += "and ( ( lower(title.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = title.id and ident.value like :identifier ) ) or ((lower(title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(title.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
                queryParams.title = "%${params.filter.trim().toLowerCase()}%"
                queryParams.identifier = "%${params.filter}%"
                queryParams.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            }

            if (params.pkgfilter && (params.pkgfilter != '')) {
                filter += " and title.pkg.id = :pkgId "
                queryParams.pkgId = Long.parseLong(params.pkgfilter)
            }

            if(params.summaryOfContent) {
                filter += " and lower(title.summaryOfContent) like :summaryOfContent "
                queryParams.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            }

            if(params.ebookFirstAutorOrFirstEditor) {
                filter += " and (lower(title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(title.firstEditor) like :ebookFirstAutorOrFirstEditor) "
                queryParams.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            }

            if(params.yearsFirstOnline) {
                filter += " and (Year(title.dateFirstOnline) in (:yearsFirstOnline)) "
                queryParams.yearsFirstOnline = params.list('yearsFirstOnline').collect { Integer.parseInt(it) }
            }

            if (params.identifier) {
                filter += "and ( exists ( from Identifier ident where ident.tipp.id = title.id and ident.value like :identifier ) ) "
                queryParams.identifier = "${params.identifier}"
            }

            if (params.publishers) {
                filter += "and lower(title.publisherName) in (:publishers) "
                queryParams.publishers = params.list('publishers').collect { it.toLowerCase() }
            }


            if (params.title_types && params.title_types != "" && params.list('title_types')) {
                filter += " and lower(title.titleType) in (:title_types)"
                queryParams.title_types = params.list('title_types').collect { ""+it.toLowerCase()+"" }
            }


            if(params.metricType && params.list("metricType").size() > 0) {
                filter += " and r.metricType in (:metricType) "
                queryParams.metricType = params.metricType
            }

            if(titles.size() > 0) {
                filter += " and title.id in (:titles) "
                queryParams.titles = titles
            }

            Map<String, Object> c5CheckParams = [customer: queryParams.customer, platforms: queryParams.platforms]
            if(dateRange) {
                c5CheckParams.startDate = queryParams.startDate
                c5CheckParams.endDate = queryParams.endDate
            }
            count5check.addAll(Counter5Report.executeQuery('select count(r.id) from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms)'+dateRange, c5CheckParams))
            if(count5check.get(0) == 0) {
                Set availableReportTypes = Counter4Report.executeQuery('select r.reportType from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) order by r.reportFrom asc', [customer: queryParams.customer, platforms: queryParams.platforms])
                result.reportTypes = availableReportTypes
                if(!params.reportType) {
                    if(availableReportTypes)
                        params.reportType = availableReportTypes[0]
                    else params.reportType = Counter4ApiSource.BOOK_REPORT_1
                }
                filter += " and r.reportType in (:reportType) "
                queryParams.reportType = params.reportType
                Set availableMetricTypes = Counter4Report.executeQuery('select r.metricType from Counter4Report r where r.reportInstitution = :customer and r.platform in (:platforms) and r.reportType in (:reportType)', [customer: queryParams.customer, platforms: queryParams.platforms, reportType: params.reportType])
                result.metricTypes = availableMetricTypes
                if(!params.metricType) {
                    if(availableMetricTypes)
                        params.metricType = availableMetricTypes[0]
                    else params.metricType = 'ft_total'
                }
                filter += " and r.metricType = :metricType "
                queryParams.metricType = params.metricType
                if(params.tabStat == 'total') {
                    result.total = Counter4Report.executeQuery('select new map(r.title as title, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange + ' group by r.title, r.metricType, r.reportType, r.reportCount order by ' + sort, queryParams).size()
                    result.usages = Counter4Report.executeQuery('select new map(r.title as title, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange + ' group by r.title, r.metricType, r.reportType, r.reportCount order by ' + sort, queryParams, [max: result.max, offset: result.offset])
                }
                else {
                    result.usages = Counter4Report.executeQuery('select r from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange + ' order by ' + sort, queryParams, [max: result.max, offset: result.offset])

                    count4check.addAll(Counter4Report.executeQuery('select count(r.id) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange, queryParams))
                    result.total = count4check.size() > 0 ? count4check[0] as int : 0
                }

            }
            else {
                Set availableReportTypes = Counter5Report.executeQuery('select r.reportType from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) order by r.reportFrom asc', [customer: queryParams.customer, platforms: queryParams.platforms])
                result.reportTypes = availableReportTypes
                if(!params.reportType) {
                    if(availableReportTypes)
                        params.reportType = availableReportTypes[0].toLowerCase()
                    else params.reportType = Counter5ApiSource.TITLE_MASTER_REPORT.toLowerCase()
                }
                filter += " and lower(r.reportType) in (:reportType) "
                queryParams.reportType = params.reportType
                Set availableMetricTypes = Counter5Report.executeQuery('select r.metricType from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms) and lower(r.reportType) in (:reportType)', [customer: queryParams.customer, platforms: queryParams.platforms, reportType: params.reportType])
                result.metricTypes = availableMetricTypes
                if(!params.metricType) {
                    if(availableMetricTypes)
                        params.metricType = availableMetricTypes[0]
                    else params.metricType = 'Total_Item_Investigations'
                }
                filter += " and r.metricType = :metricType "
                queryParams.metricType = params.metricType
                if(params.tabStat == 'total') {
                    result.total = Counter5Report.executeQuery('select new map(r.title as title, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange + ' group by r.title, r.metricType, r.reportType, r.reportCount order by ' + sort, queryParams).size()
                    result.usages = Counter5Report.executeQuery('select new map(r.title as title, r.metricType as metricType, sum(r.reportCount) as reportCount) from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange + ' group by r.title, r.metricType, r.reportType, r.reportCount order by ' + sort, queryParams, [max: result.max, offset: result.offset])
                }else {
                    result.usages = Counter5Report.executeQuery('select r from Counter5Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange + ' order by ' + sort, queryParams, [max: result.max, offset: result.offset])
                    result.total = count5check.size() > 0 ? count5check[0] as int : 0
                }

            }
        }
        result.monthsInRing = monthsInRing

        result
    }

    boolean hasParticipantPerpetualAccessToTitle(List<Subscription> subscriptions, TitleInstancePackagePlatform tipp) {
        if(subscriptions){
            List<Long> subscriptionIDs = subscriptions.id
            return hasParticipantPerpetualAccessToTitle2(subscriptionIDs, tipp)
        }else {
            return false
        }
    }

    /**
     * Called from views
     * Checks if the participant has perpetual access to the given title
     * @param subscriptionIDs the subscription ids of the participant
     * @param tipp the title whose access should be checked
     * @return true if there is a title with perpetual access, false otherwise
     */
    boolean hasParticipantPerpetualAccessToTitle2(List<Long> subscriptionIDs, TitleInstancePackagePlatform tipp){
        if(subscriptionIDs){
            Integer countIes = IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie join ie.tipp tipp where ' +
                    'ie.perpetualAccessBySub is not null and ' +
                    'tipp.hostPlatformURL = :hostPlatformURL and ' +
                    'tipp.status != :tippStatus and ' +
                    'ie.status != :tippStatus and ' +
                    'ie.subscription.id in (:subscriptionIDs)',
                    [hostPlatformURL: tipp.hostPlatformURL,
                     tippStatus: RDStore.TIPP_STATUS_REMOVED,
                     subscriptionIDs: subscriptionIDs])[0]

            if(countIes > 0){
                return true
            }else {
                return false
            }
        }else {
            return false
        }
    }

    boolean hasParticipantPerpetualAccessToTitle3(Org org, TitleInstancePackagePlatform tipp){
            Integer countPermanentTitles = PermanentTitle.executeQuery('select count(*) from PermanentTitle pt join pt.tipp tipp where ' +
                    '(tipp = :tipp or tipp.hostPlatformURL = :hostPlatformURL) and ' +
                    'tipp.status != :tippStatus AND ' +
                    'pt.owner = :org',
                    [hostPlatformURL: tipp.hostPlatformURL,
                     tippStatus: RDStore.TIPP_STATUS_REMOVED,
                     tipp: tipp,
                     org: org])[0]

            if(countPermanentTitles > 0){
                return true
            }else {
                return false
            }
    }

    List<PermanentTitle> listParticipantPerpetualAccessToTitle(Org org, TitleInstancePackagePlatform tipp){
        List<PermanentTitle> permanentTitles = PermanentTitle.executeQuery('select pt from PermanentTitle pt join pt.tipp tipp where ' +
                '(tipp = :tipp or tipp.hostPlatformURL = :hostPlatformURL) and ' +
                'tipp.status != :tippStatus AND ' +
                'pt.owner = :org',
                [hostPlatformURL: tipp.hostPlatformURL,
                 tippStatus: RDStore.TIPP_STATUS_REMOVED,
                 tipp: tipp,
                 org: org])

        permanentTitles
    }

    /**
     * Called from views
     * Checks if the org has perpetual access to the given title
     * @param org
     * @param tipp the title whose access should be checked
     * @return true if there is a title with perpetual access, false otherwise
     */
    boolean hasOrgPerpetualAccessToTitle(Org org, TitleInstancePackagePlatform tipp){
        List<Long> subscriptionIDs = subscriptionsOfOrg(org)
        return hasParticipantPerpetualAccessToTitle2(subscriptionIDs, tipp)
    }

    List<Long> subscriptionsOfOrg(Org org){
        String base_qry = ''
        Map qry_params = [:]
        RefdataValue role_sub = RDStore.OR_SUBSCRIBER
        RefdataValue role_subCons = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_consortia = RDStore.OR_SUBSCRIPTION_CONSORTIA

        if (accessService.otherOrgPerm(org, 'ORG_CONSORTIUM_PRO')) {
            //nur Parents
            base_qry = " from Subscription as s where ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) " +
                    " AND s.instanceOf is null "
            qry_params << ['roleType': role_sub_consortia, 'activeInst': org]

        } else {
            base_qry = " from Subscription as s where (exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType in (:roleType2) ) AND o.org = :activeInst ) ) AND (( not exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) or ( ( exists ( select o from s.orgRelations as o where o.roleType in (:scRoleType) ) ) AND ( s.instanceOf is not null) ) ) )"
            qry_params << ['roleType1': role_sub, 'roleType2': [role_subCons], 'activeInst': org, 'scRoleType': [role_sub_consortia]]
        }

        List<Long> subscriptionIDs = Subscription.executeQuery( "select s.id " + base_qry, qry_params)

        return subscriptionIDs
    }


    /**
     * Called from views
     * Checks if the given title is contained by the given subscription
     * @param subscription the subscription whose holding should be checked
     * @param tipp the title whose presence should be checked
     * @return true if the given subscription contains the title, false otherwise
     */
    IssueEntitlement titleContainedBySubscription(Subscription subscription, TitleInstancePackagePlatform tipp) {
        IssueEntitlement ie
        if(subscription.packages && tipp.pkg in subscription.packages.pkg) {
            ie = IssueEntitlement.findBySubscriptionAndStatusAndTipp(subscription, RDStore.TIPP_STATUS_CURRENT, tipp)
        }else {
            TitleInstancePackagePlatform.findAllByHostPlatformURL(tipp.hostPlatformURL).each {TitleInstancePackagePlatform titleInstancePackagePlatform ->
                ie = IssueEntitlement.findBySubscriptionAndStatusAndTipp(subscription, RDStore.TIPP_STATUS_CURRENT, titleInstancePackagePlatform) ?: ie
            }
        }
        return ie
    }

    /**
     * Checks if there is a customer number recorded to the subscribed platform of the participant
     * @param subscription the subscription whose nominal platform should be retrieved
     * @param org the participant whose customer number should be checked
     * @return true if there is a customer number recorded, false otherwise
     */
    boolean showStatisticByParticipant(Subscription subscription, Org org) {
        Map<String, Object> result = [:]

        Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: subscription])
        if (subscribedPlatforms) {
            List<CustomerIdentifier> customerIdentifiers = CustomerIdentifier.findAllByCustomerAndPlatformInList(org, subscribedPlatforms)
            customerIdentifiers.size() > 0
        }else {
            return false
        }
    }

    def exportPropertiesChanged(SurveyConfig surveyConfig, def participants, Org contextOrg) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map sheetData = [:]
        List titles = [messageSource.getMessage('org.sortname.label', null, locale),
                       messageSource.getMessage('subscription.details.consortiaMembers.label', null, locale),
                       messageSource.getMessage('propertyDefinition.label', null, locale),
                       messageSource.getMessage('subscription', null, locale) + ' - ' + messageSource.getMessage('propertyDefinition.label', null, locale),
                       messageSource.getMessage('survey.label', null, locale) + ' - ' + messageSource.getMessage('propertyDefinition.label', null, locale),
        ]
        List changedProperties = []
        List propList = surveyConfig.surveyProperties.surveyProperty

        propList.each { PropertyDefinition propertyDefinition ->
            PropertyDefinition subPropDef = PropertyDefinition.getByNameAndDescr(propertyDefinition.name, PropertyDefinition.SUB_PROP)
            if (subPropDef) {
                List row = []
                participants.each { SurveyOrg surveyOrg ->
                    Subscription subscription = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                            [parentSub  : surveyConfig.subscription,
                             participant: surveyOrg.org
                            ])[0]
                    SurveyResult surveyResult = SurveyResult.findByParticipantAndTypeAndSurveyConfigAndOwner(surveyOrg.org, propertyDefinition, surveyConfig, contextOrg)
                    SubscriptionProperty subscriptionProperty = SubscriptionProperty.findByTypeAndOwnerAndTenant(subPropDef, subscription, contextOrg)

                    if (surveyResult && subscriptionProperty) {
                        String surveyValue = surveyResult.getValue()
                        String subValue = subscriptionProperty.getValue()
                        if (surveyValue != subValue) {
                            row = []
                            row.add([field: surveyOrg.org.sortname ?: '', style: null])
                            row.add([field: surveyOrg.org.name ?: '', style: null])
                            row.add([field: propertyDefinition.getI10n('name') ?: '', style: null])
                            row.add([field: subscriptionProperty.type.isRefdataValueType() ? subscriptionProperty.refValue?.getI10n("value") : '', style: null])
                            row.add([field: surveyResult.getResult() ?: '', style: null])

                            changedProperties.add(row)
                        }
                    }

                }
                if (row.size() > 0) {
                    changedProperties.add([])
                    changedProperties.add([])
                }
            }
        }
        sheetData.put(escapeService.escapeString(surveyConfig.getConfigNameShort()), [titleRow: titles, columnData: changedProperties])

        return exportService.generateXLSXWorkbook(sheetData)
    }

    void transferPerpetualAccessTitlesOfOldSubs(List<Long> entitlementsToTake, Subscription participantSub) {
        Sql sql = GlobalService.obtainSqlConnection()
        Connection connection = sql.dataSource.getConnection()

        List newIes = sql.executeInsert("insert into issue_entitlement (ie_version, ie_guid, ie_date_created, ie_last_updated, ie_subscription_fk, ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_name, ie_perpetual_access_by_sub_fk) " +
                "select 0, concat('issueentitlement:',gen_random_uuid()), now(), now(), ${participantSub.id},  ie_tipp_fk, ie_access_start_date, ie_access_end_date, ie_status_rv_fk, ie_name, ie_perpetual_access_by_sub_fk from issue_entitlement where ie_tipp_fk not in (select ie_tipp_fk from issue_entitlement where ie_subscription_fk = ${participantSub.id}) and ie_id = any(:ieIds)", [ieIds: connection.createArrayOf('bigint', entitlementsToTake.toArray())])

        if(newIes.size() > 0){

            List newIesIds = newIes.collect {it[0]}

           sql.executeInsert('''insert into issue_entitlement_coverage (ic_version, ic_ie_fk, ic_date_created, ic_last_updated, ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo)
            select 0, i.ie_id, now(), now(), ic_start_date, ic_start_volume, ic_start_issue, ic_end_date, ic_end_volume, ic_end_issue, ic_coverage_depth, ic_coverage_note, ic_embargo from issue_entitlement_coverage
            join issue_entitlement ie on ie.ie_id = issue_entitlement_coverage.ic_ie_fk
            join title_instance_package_platform tipp on tipp.tipp_id = ie.ie_tipp_fk
            join issue_entitlement i on tipp.tipp_id = i.ie_tipp_fk
            where i.ie_id = any(:newIeIds)
            and ie.ie_id = any(:ieIds) order  by ic_last_updated DESC''', [newIeIds: connection.createArrayOf('bigint', newIesIds.toArray()), ieIds: connection.createArrayOf('bigint', entitlementsToTake.toArray())])


            sql.executeInsert('''insert into price_item (pi_version, pi_ie_fk, pi_date_created, pi_last_updated, pi_guid, pi_list_currency_rv_fk, pi_list_price)
                                select 0, i.ie_id, now(), now(), concat('priceitem:',gen_random_uuid()), pi_list_currency_rv_fk, pi_list_price from price_item
                                join issue_entitlement ie on ie.ie_id = price_item.pi_ie_fk
                                join title_instance_package_platform tipp on tipp.tipp_id = ie.ie_tipp_fk
                                join issue_entitlement i on tipp.tipp_id = i.ie_tipp_fk
                                where i.ie_id = any(:newIeIds)
                                and ie.ie_id = any(:ieIds) order  by pi_last_updated DESC''', [newIeIds: connection.createArrayOf('bigint', newIesIds.toArray()), ieIds: connection.createArrayOf('bigint', entitlementsToTake.toArray())])

        }
    }

    List<IssueEntitlement> getPerpetualAccessIesBySub(Subscription subscription) {
        Set<Subscription> subscriptions = linksGenerationService.getSuccessionChain(subscription, 'sourceSubscription')
        subscriptions << subscription
        List<IssueEntitlement> issueEntitlements = []

        if(subscriptions.size() > 0) {
            List<Object> subIds = []
            subIds.addAll(subscriptions.id)
            Sql sql = GlobalService.obtainSqlConnection()
            Connection connection = sql.dataSource.getConnection()
            def ieIds = sql.rows("select ie.ie_id from issue_entitlement ie join title_instance_package_platform tipp on tipp.tipp_id = ie.ie_tipp_fk " +
                    "where ie.ie_subscription_fk = any(:subs) " +
                    "and tipp.tipp_status_rv_fk = :tippStatus and ie.ie_status_rv_fk = :tippStatus " +
                    "and tipp.tipp_host_platform_url in " +
                    "(select tipp2.tipp_host_platform_url from issue_entitlement ie2 join title_instance_package_platform tipp2 on tipp2.tipp_id = ie2.ie_tipp_fk " +
                    "where ie2.ie_subscription_fk = any(:subs) " +
                    "and ie2.ie_perpetual_access_by_sub_fk = any(:subs) " +
                    "and tipp2.tipp_status_rv_fk = :tippStatus and ie2.ie_status_rv_fk = :tippStatus)", [subs: connection.createArrayOf('bigint', subIds.toArray()), tippStatus: RDStore.TIPP_STATUS_CURRENT.id])

            issueEntitlements = ieIds.size() > 0 ? IssueEntitlement.executeQuery("select ie from IssueEntitlement ie where ie.id in (:ieIDs)", [ieIDs: ieIds.ie_id]) : []
        }
        return issueEntitlements
    }

    List<Long> getPerpetualAccessIeIDsBySub(Subscription subscription) {
        Set<Subscription> subscriptions = linksGenerationService.getSuccessionChain(subscription, 'sourceSubscription')
        subscriptions << subscription
        List<Long> issueEntitlementIds = []

        if(subscriptions.size() > 0) {
            List<Object> subIds = []
            subIds.addAll(subscriptions.id)
            Sql sql = GlobalService.obtainSqlConnection()
            Connection connection = sql.dataSource.getConnection()
            def ieIds = sql.rows("select ie.ie_id from issue_entitlement ie join title_instance_package_platform tipp on tipp.tipp_id = ie.ie_tipp_fk " +
                    "where ie.ie_subscription_fk = any(:subs) " +
                    " and ie.ie_status_rv_fk = :tippStatus " +
                    "and tipp.tipp_host_platform_url in " +
                    "(select tipp2.tipp_host_platform_url from issue_entitlement ie2 join title_instance_package_platform tipp2 on tipp2.tipp_id = ie2.ie_tipp_fk " +
                    "where ie2.ie_subscription_fk = any(:subs) " +
                    "and ie2.ie_perpetual_access_by_sub_fk = any(:subs) " +
                    " and ie2.ie_status_rv_fk = :tippStatus)", [subs: connection.createArrayOf('bigint', subIds.toArray()), tippStatus: RDStore.TIPP_STATUS_CURRENT.id])

            issueEntitlementIds = ieIds.ie_id
        }
        return issueEntitlementIds
    }

    Integer countPerpetualAccessTitlesBySub(Subscription subscription) {
        Integer count = 0
        Set<Subscription> subscriptions = linksGenerationService.getSuccessionChain(subscription, 'sourceSubscription')
        subscriptions << subscription

        if(subscriptions.size() > 0) {
            List<Object> subIds = []
            subIds.addAll(subscriptions.id)
            Sql sql = GlobalService.obtainSqlConnection()
            Connection connection = sql.dataSource.getConnection()
            /*def titles = sql.rows("select count(*) from issue_entitlement ie join title_instance_package_platform tipp on tipp.tipp_id = ie.ie_tipp_fk " +
                    "where ie.ie_subscription_fk = any(:subs)  " +
                    "and tipp.tipp_status_rv_fk = :tippStatus and ie.ie_status_rv_fk = :tippStatus " +
                    "and tipp.tipp_host_platform_url in " +
                    "(select tipp2.tipp_host_platform_url from issue_entitlement ie2 join title_instance_package_platform tipp2 on tipp2.tipp_id = ie2.ie_tipp_fk " +
                    " where ie2.ie_perpetual_access_by_sub_fk = any(:subs)" +
                    " and tipp2.tipp_status_rv_fk = :tippStatus and ie2.ie_status_rv_fk = :tippStatus) group by tipp.tipp_id", [subs: connection.createArrayOf('bigint', subIds.toArray()), tippStatus: RDStore.TIPP_STATUS_CURRENT.id])*/

            def titles = sql.rows("select count(*) from issue_entitlement ie2 join title_instance_package_platform tipp2 on tipp2.tipp_id = ie2.ie_tipp_fk " +
                    " where ie2.ie_subscription_fk = any(:subs) and ie2.ie_perpetual_access_by_sub_fk = any(:subs)" +
                    " and ie2.ie_status_rv_fk = :tippStatus group by tipp2.tipp_host_platform_url", [subs: connection.createArrayOf('bigint', subIds.toArray()), tippStatus: RDStore.TIPP_STATUS_CURRENT.id])

            count = titles.size()
        }
        return count
    }

    Integer countIssueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        Integer countIes = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select count(igi) from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup",
                        [ieGroup: issueEntitlementGroup])[0]
                : 0
        countIes
    }

    /**
     * Calculates the sum of list prices of titles in the group attached to the given survey and subscription
     * @param subscription the {@link Subscription} to whose titles the issue entitlement group has been defined
     * @param surveyConfig the {@link SurveyConfig} for which the issue entitlement group has been defined
     * @return the sum of {@link de.laser.finance.PriceItem#listPrice}s of the titles belonging to the given {@link IssueEntitlementGroup}
     */
    BigDecimal sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig, RefdataValue currency) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        BigDecimal sumListPrice = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency and p.issueEntitlement in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup)",
                        [ieGroup: issueEntitlementGroup, currency: currency])[0]
                : 0.0
        sumListPrice
    }

    /**
     * Calculates the sum of list prices of titles in the group attached to the given survey and subscription.
     * Regarded are only titles with status = Current
     * @param subscription the {@link Subscription} to whose titles the issue entitlement group has been defined
     * @param surveyConfig the {@link SurveyConfig} for which the issue entitlement group has been defined
     * @return the sum of {@link de.laser.finance.PriceItem#listPrice}s of the titles belonging to the given {@link IssueEntitlementGroup}
     */
    BigDecimal sumListPriceInCurrencyOfCurrentIssueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig, RefdataValue currency) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        BigDecimal sumListPrice = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency and p.issueEntitlement in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup) and p.issueEntitlement.status = :status",
                        [ieGroup: issueEntitlementGroup, status: RDStore.TIPP_STATUS_CURRENT, currency: currency])[0]
                : 0.0
        sumListPrice
    }

    List<IssueEntitlement> issueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        List<IssueEntitlement> ies = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup",
                        [ieGroup: issueEntitlementGroup])
                : []
        ies
    }

    String surveyMailHtmlAsString(SurveyInfo surveyInfo, boolean reminder = false) {
        Locale language = new Locale("de")
        groovyPageRenderer.render view: '/mailTemplates/html/notificationSurveyForMailClient', model: [language: language, survey: surveyInfo, reminder: false]
    }

    String surveyMailTextAsString(SurveyInfo surveyInfo, boolean reminder = false) {
        Locale language = new Locale("de")
        groovyPageRenderer.render view: '/mailTemplates/text/notificationSurvey', model: [language: language, survey: surveyInfo, reminder: reminder]
    }

}
