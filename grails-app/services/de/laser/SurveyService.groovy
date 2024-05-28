package de.laser

import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.ctrl.SubscriptionControllerService
import de.laser.finance.CostItem
import de.laser.config.ConfigDefaults
import de.laser.finance.PriceItem
import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.remote.ApiSource
import de.laser.stats.Counter4ApiSource
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5ApiSource
import de.laser.stats.Counter5Report
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigPackage
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyPackageResult
import de.laser.survey.SurveyResult
import de.laser.survey.SurveyUrl
import de.laser.config.ConfigMapper
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.converters.JSON
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
    AddressbookService addressbookService
    CompareService compareService
    ComparisonService comparisonService
    ContextService contextService
    EscapeService escapeService
    ExportService exportService
    GokbService gokbService
    FilterService filterService
    LinksGenerationService linksGenerationService
    MessageSource messageSource
    PackageService packageService
    SubscriptionService subscriptionService
    SubscriptionControllerService subscriptionControllerService
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

        if (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO ) && surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )) {
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

        if (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_CONSORTIUM_PRO ) && surveyConfig.surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (!surveyConfig.pickAndChoose) {
            return false
        }

        if (surveyConfig.surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )) {

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
                               messageSource.getMessage('surveyOrg.ownerComment.export.label', null, locale)])
                if (surveyConfig.subscription) {
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

                if (surveyConfig.subscription) {
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

                surveyConfig.surveyProperties.sort { it.propertyOrder }.each {
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
                if (surveyConfig.subscription) {
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

                    if (surveyConfig.subscription) {

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

                    if (surveyConfig.subscription) {
                        row.add([field: subscription.comment, style: null])
                        //Performance lastig providers und vendors
                        row.add([field: subscription.providers.join(", "), style: null])
                        row.add([field: subscription.vendors.join(", "), style: null])

                        List licenseNames = []
                        Links.findAllByDestinationSubscriptionAndLinkType(subscription,RDStore.LINKTYPE_LICENSE).each { Links li ->
                            License l = li.sourceLicense
                            licenseNames << l.reference
                        }
                        row.add([field: licenseNames ? licenseNames.join(", ") : '', style: null])
                        List packageNames = subscription.packages?.collect {
                            it.pkg.name
                        }
                        row.add([field: packageNames ? packageNames.join(", ") : '', style: null])

                        row.add([field: subscription.status?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription.kind?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription.form?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription.resource?.getI10n("value") ?: '', style: null])
                        row.add([field: subscription.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                        row.add([field: subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                        if (surveyConfig.subSurveyUseForTransfer) {
                            CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, surveyOrg.org), RDStore.COST_ITEM_DELETED)

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
                            value = surResult.intValue ? surResult.intValue.toString() : ""
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

                if (surveyConfig.subscription) {
                    surveyConfig.surveyUrls.each { SurveyUrl surveyUrl->
                        row.add([field: surveyUrl.url ?: '', style: null])
                        row.add([field: surveyUrl.urlComment ?: '', style: null])
                    }

                    subscription = surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(contextOrg) ?: null
                    row.add([field: surveyConfig.getConfigNameShort() ?: "", style: null])
                    row.add([field: subscription.providers.join(","), style: null])
                    row.add([field: subscription.vendors.join(", "), style: null])

                    List licenseNames = []
                    Links.findAllByDestinationSubscriptionAndLinkType(subscription, RDStore.LINKTYPE_LICENSE).each { Links li ->
                        License l = li.sourceLicense
                        licenseNames << l.reference
                    }
                    row.add([field: licenseNames ? licenseNames.join(", ") : '', style: null])
                    List packageNames = subscription.packages?.collect {
                        it.pkg.name
                    }
                    row.add([field: packageNames ? packageNames.join(", ") : '', style: null])
                    row.add([field: subscription.status?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription.kind?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription.form?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription.resource?.getI10n("value") ?: '', style: null])
                    row.add([field: subscription.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                    row.add([field: subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                    if (surveyConfig.subSurveyUseForTransfer) {
                        CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)
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
                if (surveyConfig.subscription ) {
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

                if (surveyConfig.subscription ) {
                    titles.addAll([messageSource.getMessage('surveyProperty.subProvider', null, locale),
                                   messageSource.getMessage('surveyProperty.subAgency', null, locale),
                                   messageSource.getMessage('default.status.label', null, locale),
                                   messageSource.getMessage('financials.costItemElement', null, locale),
                                   messageSource.getMessage('financials.costInBillingCurrency', null, locale),
                                   messageSource.getMessage('default.currency.label', null, locale),
                                   messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, locale),
                                   messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, locale),
                                   messageSource.getMessage('default.startDate.label', null, locale),
                                   messageSource.getMessage('default.endDate.label', null, locale),
                                   messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale)])
                }

                Subscription subscription

                String surveyName = surveyConfig.getConfigNameShort()
                surveyConfig.orgs.sort { it.org.sortname }.each { surveyOrg ->
                    List row = []

                    row.add([field: surveyOrg.org.sortname ?: '', style: null])
                    row.add([field: surveyOrg.org.name ?: '', style: null])

                    if (surveyConfig.subscription) {

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

                    if (surveyConfig.subscription) {
                        row.add([field: subscription.providers.join(", "), style: null])
                        row.add([field: subscription.vendors.join(", "), style: null])

                        row.add([field: subscription.status?.getI10n("value") ?: '', style: null])

                        CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, surveyOrg.org), RDStore.COST_ITEM_DELETED)

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
                           messageSource.getMessage('default.startDate.label', null, locale),
                           messageSource.getMessage('default.endDate.label', null, locale),
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

                if (surveyConfig.subscription) {
                    subscription = surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(contextOrg) ?: null
                    row.add([field: subscription.providers.join(", "), style: null])
                    row.add([field: subscription.vendors.join(", "), style: null])

                    row.add([field: subscription.status?.getI10n("value") ?: '', style: null])

                    CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)

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

                        if (surveyResult.surveyConfig.subscription) {

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

                        if (surveyResult.surveyConfig.subscription) {
                            //Performance lastig providers und vendors
                            row.add([field: subscription.providers.join(", "), style: null])
                            row.add([field: subscription.vendors.join(", "), style: null])

                            List licenseNames = []
                            Links.findAllByDestinationSubscriptionAndLinkType(subscription,RDStore.LINKTYPE_LICENSE).each { Links li ->
                                licenseNames << li.sourceLicense.reference
                            }
                            row.add([field: licenseNames ? licenseNames.join(", ") : '', style: null])

                            List packageNames = subscription.packages?.collect {
                                it.pkg.name
                            }
                            row.add([field: packageNames ? packageNames.join(", ") : '', style: null])

                            row.add([field: subscription.status?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription.kind?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription.form?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription.resource?.getI10n("value") ?: '', style: null])
                            row.add([field: subscription.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])
                            row.add([field: subscription.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value"), style: null])

                                CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED)

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
     * @param surveyInfo the survey concerned
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

        result = _setSurveyConfigCounts(result, 'created', parameterMap, contextOrg)

        result = _setSurveyConfigCounts(result, 'active', parameterMap, contextOrg)

        result = _setSurveyConfigCounts(result, 'finish', parameterMap, contextOrg)

        result = _setSurveyConfigCounts(result, 'inEvaluation', parameterMap, contextOrg)

        result = _setSurveyConfigCounts(result, 'completed', parameterMap, contextOrg)

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
        GrailsParameterMap cloneParameterMap = parameterMap.clone() as GrailsParameterMap

        cloneParameterMap.tab = tab
        cloneParameterMap.remove('max')
        cloneParameterMap.remove('offset')

        FilterService.Result fsr = filterService.getSurveyConfigQueryConsortia(cloneParameterMap, sdFormat, owner)
        if (fsr.isFilterSet) { cloneParameterMap.filterSet = true }

        String queryWithoutOrderBy = fsr.query.split('order by')[0]
        result."${tab}" = SurveyInfo.executeQuery("select count(*) "+queryWithoutOrderBy, fsr.queryParams, cloneParameterMap)[0]

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

            if(surveyConfig.subSurveyUseForTransfer && surveyProperty == PropertyStore.SURVEY_PROPERTY_PARTICIPATION){
                propertytoSub.propertyOrder = 1
            }else {
                propertytoSub.propertyOrder = surveyConfig.surveyProperties.size() + 1
            }

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
                Org org = subChild.getSubscriberRespConsortia()

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
                        surveyConfig: newSurveyConfig,
                        propertyOrder: surveyConfigProperty.propertyOrder).save()
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

            result = _setSurveyParticipantCounts(result, 'open', tmpParams, participant, contextOrg)

            //result = _setSurveyParticipantCounts(result, 'new', tmpParams, participant, contextOrg)

            //result = _setSurveyParticipantCounts(result, 'processed', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'finish', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'notFinish', tmpParams, participant, contextOrg)

            result = _setSurveyParticipantCounts(result, 'termination', tmpParams, participant, contextOrg)

            //result = _setSurveyParticipantCounts(result, 'all', tmpParams, participant, contextOrg)


        }else {

            result = _setSurveyParticipantCounts(result, 'open', tmpParams, participant, null)

            //result = _setSurveyParticipantCounts(result, 'new', tmpParams, participant, null)

            //result = _setSurveyParticipantCounts(result, 'processed', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'finish', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'notFinish', tmpParams, participant, null)

            result = _setSurveyParticipantCounts(result, 'termination', tmpParams, participant, null)

            //result = _setSurveyParticipantCounts(result, 'all', tmpParams, participant, contextOrg)
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

        GrailsParameterMap cloneParameterMap = parameterMap.clone() as GrailsParameterMap

        if(owner){
            cloneParameterMap.owner = owner
        }

        cloneParameterMap.tab = tab
        cloneParameterMap.remove('max')

        FilterService.Result fsr = filterService.getParticipantSurveyQuery_New(cloneParameterMap, sdFormat, participant)
        if (fsr.isFilterSet) { cloneParameterMap.filterSet = true }

        result."${tab}" = SurveyResult.executeQuery(fsr.query, fsr.queryParams, cloneParameterMap).groupBy { it.id[1] }.size()

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
     * @deprecated usage is being processed as part of {@link SubscriptionController#renewEntitlementsWithSurvey()}
     */
    @Deprecated
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
                queryParams.ddcs = Params.getLongList(params, 'ddcs')
            }
            if(params.languages && params.list("languages").size() > 0) {
                filter += " and exists (select lang.id from title.languages lang where lang.language.id in (:languages)) "
                queryParams.languages = Params.getLongList(params, 'languages')
            }

            if (params.filter) {
                filter += "and ( ( lower(title.name) like :title ) or ( exists ( from Identifier ident where ident.tipp.id = title.id and ident.value like :identifier ) ) or ((lower(title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(title.firstEditor) like :ebookFirstAutorOrFirstEditor)) ) "
                queryParams.title = "%${params.filter.trim().toLowerCase()}%"
                queryParams.identifier = "%${params.filter}%"
                queryParams.ebookFirstAutorOrFirstEditor = "%${params.filter.trim().toLowerCase()}%"
            }

            if (params.pkgfilter && (params.pkgfilter != '')) {
                filter += " and title.pkg.id = :pkgId "
                queryParams.pkgId = params.long('pkgfilter')
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

    /**
     * Substitution call to ${@link #hasParticipantPerpetualAccessToTitle2(java.util.List, de.laser.TitleInstancePackagePlatform)}
     * @param subscriptions the list of subscriptions eligible for the perpetual purchase
     * @param tipp the title to be checked
     * @return true if one of the given subscriptions grant access, false otherwise
     */
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

    /**
     * Checks if the given institution ({@link Org}) has perpetual access to the given title
     * @param org the institution whose access to check
     * @param tipp the title to which perpetual access may be granted
     * @return true if there is perpetual access (= at least one record in {@link PermanentTitle} for the given institution and title, false otherwise
     */
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

    /**
     * Lists the institution's subscriptions via which it has purchased perpetual access to the given title
     * @param org the institution ({@link Org}) whose subscriptions should be listed
     * @param tipp the title subject of the perpetual access
     * @return a {@link List} of {@link PermanentTitle} records containing the subscription(s) granting perpetual access to the given title
     */
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

    /**
     * Gets a list of subscription IDs of the given institution
     * @param org the institution ({@link Org}) whose subscriptions should be retrieved
     * @return a {@link List} of {@link Subscription} IDs which are subscribed by the given institution
     */
    List<Long> subscriptionsOfOrg(Org org){
        String base_qry = ''
        Map qry_params = [:]
        RefdataValue role_sub = RDStore.OR_SUBSCRIBER
        RefdataValue role_subCons = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_consortia = RDStore.OR_SUBSCRIPTION_CONSORTIA

        if (org.isCustomerType_Consortium_Pro()) {
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
    IssueEntitlement titleContainedBySubscription(Subscription subscription, TitleInstancePackagePlatform tipp, List<RefdataValue> status) {
        IssueEntitlement ie

        if(subscription.packages && tipp.pkg in subscription.packages.pkg) {
            ie = IssueEntitlement.findBySubscriptionAndStatusInListAndTipp(subscription, status, tipp)
        }else {
            TitleInstancePackagePlatform.findAllByHostPlatformURL(tipp.hostPlatformURL).each {TitleInstancePackagePlatform titleInstancePackagePlatform ->
                ie = IssueEntitlement.findBySubscriptionAndStatusInListAndTipp(subscription, status, titleInstancePackagePlatform) ?: ie
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

    /**
     * Exports those survey results which differ from their base subscription property, i.e.
     * where the member completing the survey submitted a different value as currently defined in the equivalent subscription property
     * @param surveyConfig the survey whose results should be exported
     * @param participants the members participating at the survey
     * @param contextOrg the context consortium
     * @return an Excel workbook containing the differences
     */
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

    @Deprecated
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

    @Deprecated
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

    @Deprecated
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

    /**
     * Counts the titles perpetually purchased via the given subscription. Regarded is the time span of all year rings, i.e.
     * subscriptions preceding the given one are counted as well
     * @param subscription the {@link Subscription} whose perpetually accessible titles should be counted
     * @return the count of {@link IssueEntitlement}s to which perpetual access have been granted over all years of the given subscription
     */
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

    /**
     * Called from _evaluationParticipantsView.gsp
     * Counts those perpetually purchased titles of the subscription's holding which are not part of the titles subject of the survey
     * @param subscription the {@link Subscription} containing the entire holding
     * @param surveyConfig the {@link SurveyConfig} in which the titles may be picked
     * @return the count of titles not figuring in the survey's selectable titles
     */
    Integer countPerpetualAccessTitlesBySubAndNotInIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        Integer count = 0
        Set<Subscription> subscriptions = linksGenerationService.getSuccessionChain(subscription, 'sourceSubscription')
        subscriptions << subscription

        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)

        if(subscriptions.size() > 0 && issueEntitlementGroup) {
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
                    " and ie2.ie_status_rv_fk = :tippStatus " +
                    " and ie2.ie_id not in (select igi_ie_fk from issue_entitlement_group_item where igi_ie_group_fk = :ieGroup) group by tipp2.tipp_host_platform_url", [subs: connection.createArrayOf('bigint', subIds.toArray()), tippStatus: RDStore.TIPP_STATUS_CURRENT.id, ieGroup: issueEntitlementGroup.id])

            count = titles.size()
        }
        return count
    }

    /**
     * Counts the titles in the group attached to the given survey and subscription
     * @param subscription the {@link Subscription} to whose titles the issue entitlement group has been defined
     * @param surveyConfig the {@link SurveyConfig} for which the issue entitlement group has been defined
     * @return the count of {@link IssueEntitlement}s belonging to the given {@link IssueEntitlementGroup}
     */
    Integer countIssueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        Integer countIes = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select count(igi) from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status != :status",
                        [ieGroup: issueEntitlementGroup, status: RDStore.TIPP_STATUS_REMOVED])[0]
                : 0
        countIes
    }

    /**
     *
     * @param subscription
     * @param surveyConfig
     * @param status
     * @return
     */
    Integer countIssueEntitlementsByIEGroupWithStatus(Subscription subscription, SurveyConfig surveyConfig, RefdataValue status) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        Integer countIes = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select count(igi) from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status = :status",
                        [ieGroup: issueEntitlementGroup, status: status])[0]
                : 0
        countIes
    }

    /**
     * currently unused
     * @param subscription
     * @param surveyConfig
     * @return
     */
    Integer countCurrentIssueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        Integer countIes = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select count(igi) from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status = :status",
                        [ieGroup: issueEntitlementGroup, status: RDStore.TIPP_STATUS_CURRENT])[0]
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
        if(issueEntitlementGroup) {
            //sql bridge
            Sql sql = GlobalService.obtainSqlConnection()
            BigDecimal sumListPrice = sql.rows("select sum(pi.pi_list_price) as sum_list_price from (select pi_tipp_fk, pi_list_price, row_number() over (partition by pi_tipp_fk order by pi_date_created desc) as rn, count(*) over (partition by pi_tipp_fk) as cn from price_item where pi_list_price is not null and pi_list_currency_rv_fk = :currency and pi_tipp_fk in (select ie_tipp_fk from issue_entitlement join issue_entitlement_group_item on ie_id = igi_ie_fk where igi_ie_group_fk = :ieGroup and ie_status_rv_fk <> :status)) as pi where pi.rn = 1", [ieGroup: issueEntitlementGroup.id, currency: currency.id, status: RDStore.TIPP_STATUS_REMOVED.id])[0]['sum_list_price']
            sumListPrice ?: 0.0
            /*
            BigDecimal sumListPrice = issueEntitlementGroup ?
                    IssueEntitlementGroupItem.executeQuery("select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency and p.tipp in (select igi.ie.tipp from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status != :status)",
                            [ieGroup: issueEntitlementGroup, currency: currency, status: RDStore.TIPP_STATUS_REMOVED])[0]
                    : 0.0
            sumListPrice
            */
        }
        else 0.0
    }

    /**
     * currently unused
     * @param subscription
     * @param surveyConfig
     * @param currency
     * @return
     */
    BigDecimal sumListPriceTippInCurrencyOfCurrentIssueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig, RefdataValue currency) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        BigDecimal sumListPrice = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency and p.tipp in (select igi.ie.tipp from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status = :status)",
                        [ieGroup: issueEntitlementGroup, status: RDStore.TIPP_STATUS_CURRENT, currency: currency])[0]
                : 0.0
        sumListPrice
    }

    /**
     * currently unused
     * @param subscription
     * @param currency
     * @return
     */
    BigDecimal sumListPriceTippInCurrencyOfCurrentIssueEntitlements(Subscription subscription, RefdataValue currency) {
        BigDecimal sumListPrice = 0.0
        sumListPrice = PriceItem.executeQuery("select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency and p.tipp in (select ie.tipp from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :status)",
                        [sub: subscription, status: RDStore.TIPP_STATUS_CURRENT, currency: currency])[0]
        sumListPrice
    }

    /**
     * Gets the titles in the group attached to the given survey and subscription
     * @param subscription the {@link Subscription} to whose titles the issue entitlement group has been defined
     * @param surveyConfig the {@link SurveyConfig} for which the issue entitlement group has been defined
     * @return a {@link List} of {@link IssueEntitlement}s belonging to the given {@link IssueEntitlementGroup}
     */
    List<IssueEntitlement> issueEntitlementsByIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)
        List<IssueEntitlement> ies = issueEntitlementGroup ?
                IssueEntitlementGroupItem.executeQuery("select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup",
                        [ieGroup: issueEntitlementGroup])
                : []
        ies
    }

    /**
     * Called from _evaluationParticipantsView.gsp
     * Generates a notification mail containing the details of the survey for the participants in the given {@link SurveyInfo}
     * @param surveyInfo the survey data containing the general parameters of the survey and the participants
     * @param reminder unused
     * @return the mail text as string with HTML markup
     */
    String surveyMailHtmlAsString(SurveyInfo surveyInfo, boolean reminder = false) {
        Locale language = new Locale("de")
        groovyPageRenderer.render view: '/mailTemplates/html/notificationSurveyForMailClient', model: [language: language, survey: surveyInfo, reminder: false]
    }

    /**
     * Generates a notification mail containing the details of the survey for the participants in the given {@link SurveyInfo}
     * @param surveyInfo the survey data containing the general parameters of the survey and the participants
     * @param reminder is the mail a reminder to participate at the given survey?
     * @return the mail text as plain string
     */
    String surveyMailTextAsString(SurveyInfo surveyInfo, boolean reminder = false) {
        Locale language = new Locale("de")
        groovyPageRenderer.render view: '/mailTemplates/text/notificationSurvey', model: [language: language, survey: surveyInfo, reminder: reminder]
    }

    /**
     * Generates a notification mail containing the details of all surveys for the participants in the given {@link SurveyInfo} list;
     * basically identical to {@link #surveyMailTextAsString(de.laser.survey.SurveyInfo, boolean)}, but for more than one survey
     * @param surveys the {@link SurveyInfo}s to process
     * @param reminder is the mail a reminder to participate at the given surveys?
     * @return the mail text as plain string
     */
    String surveysMailTextAsString(List<SurveyInfo> surveys, boolean reminder = false) {
        Locale language = new Locale("de")
        groovyPageRenderer.render view: '/mailTemplates/text/notificationSurveys', model: [language: language, surveys: surveys, reminder: reminder]
    }

    int countMultiYearResult(SurveyConfig surveyConfig, int multiYear){
        int countListMuliYearResult = 0
        String query = 'select count(*) from SurveyResult where surveyConfig = :surveyConfig and type in (:type) and refValue = :yes and (exists (select surResult from SurveyResult as surResult where surResult.surveyConfig = :surveyConfig and surResult.type = :participation and refValue = :yes))'
        if(multiYear == 1){
            countListMuliYearResult =  SurveyResult.executeQuery( query, [participation: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, type: [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5], surveyConfig: surveyConfig, yes: RDStore.YN_YES] )[0]
        } else if(multiYear == 2){
            countListMuliYearResult =  SurveyResult.executeQuery( query, [participation: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, type: [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5], surveyConfig: surveyConfig, yes: RDStore.YN_YES] )[0]
        } else if(multiYear == 3){
            countListMuliYearResult =  SurveyResult.executeQuery( query, [participation: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, type: [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5], surveyConfig: surveyConfig, yes: RDStore.YN_YES] )[0]
        }else if(multiYear == 4){
            countListMuliYearResult =  SurveyResult.executeQuery( query, [participation: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, type: [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5], surveyConfig: surveyConfig, yes: RDStore.YN_YES] )[0]
        }else if(multiYear == 5){
            countListMuliYearResult =  SurveyResult.executeQuery( query, [participation: PropertyStore.SURVEY_PROPERTY_PARTICIPATION, type: [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5], surveyConfig: surveyConfig, yes: RDStore.YN_YES] )[0]
        }

        return countListMuliYearResult
    }

    List<PropertyDefinition> getMultiYearResultProperties(SurveyConfig surveyConfig, Org org){
        return SurveyResult.executeQuery( 'select surResult.type from SurveyResult as surResult where surResult.surveyConfig = :surveyConfig and surResult.type in (:type) and surResult.refValue = :yes and surResult.participant = :participant', [participant: org, type: [PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4, PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5], surveyConfig: surveyConfig, yes: RDStore.YN_YES] )
    }

    int countSurveyPropertyWithValueByMembers(SurveyConfig surveyConfig, PropertyDefinition propertyDefinition, List<Org> orgs){
        return SurveyResult.executeQuery('select count(*) from SurveyResult as sr where sr.surveyConfig = :surveyConfig AND sr.type = :type AND sr.participant in (:orgs) AND ' +
                '(stringValue is not null ' +
                'OR intValue is not null ' +
                'OR decValue is not null ' +
                'OR refValue is not null ' +
                'OR urlValue is not null ' +
                'OR dateValue is not null)', [surveyConfig: surveyConfig, type: propertyDefinition, orgs: orgs])[0]
    }

    Map selectSurveyMembersWithImport(InputStream stream) {

        Integer processCount = 0
        Integer processRow = 0

        List orgList = []

        //now, assemble the identifiers available to highlight
        Map<String, IdentifierNamespace> namespaces = [gnd  : IdentifierNamespace.findByNsAndNsType('gnd_org_nr', Org.class.name),
                                                       isil: IdentifierNamespace.findByNsAndNsType('ISIL', Org.class.name),
                                                       ror: IdentifierNamespace.findByNsAndNsType('ROR ID',Org.class.name),
                                                       wib : IdentifierNamespace.findByNsAndNsType('wibid', Org.class.name),
                                                       dealId : IdentifierNamespace.findByNsAndNsType('deal_id', Org.class.name)]

        ArrayList<String> rows = stream.text.split('\n')
        Map<String, Integer> colMap = [gndCol: -1, isilCol: -1, rorCol: -1, wibCol: -1, dealCol: -1,
                                       startDateCol: -1, endDateCol: -1, ]

        //read off first line of KBART file
        List titleRow = rows.remove(0).split('\t'), wrongOrgs = [], truncatedRows = []
        titleRow.eachWithIndex { headerCol, int c ->
            switch (headerCol.toLowerCase().trim()) {
                case "gnd-nr": colMap.gndCol = c
                    break
                case "isil": colMap.isilCol = c
                    break
                case "ror-id": colMap.rorCol = c
                    break
                case "wib-id": colMap.wibCol = c
                    break
                case "deal-id": colMap.dealCol = c
                    break
            }
        }
        rows.eachWithIndex { row, int i ->
            processRow++
            log.debug("now processing rows ${i}")
            ArrayList<String> cols = row.split('\t', -1)
            if(cols.size() == titleRow.size()) {
                Org match = null
                if (colMap.wibCol >= 0 && cols[colMap.wibCol] != null && !cols[colMap.wibCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.status != :removed', [value: cols[colMap.wibCol].trim(), ns: namespaces.wib, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.isilCol >= 0 && cols[colMap.isilCol] != null && !cols[colMap.isilCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.status != :removed', [value: cols[colMap.isilCol].trim(), ns: namespaces.isil, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.gndCol >= 0 && cols[colMap.gndCol] != null && !cols[colMap.gndCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.status != :removed', [value: cols[colMap.gndCol].trim(), ns: namespaces.gnd, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.rorCol >= 0 && cols[colMap.rorCol] != null && !cols[colMap.rorCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.status != :removed', [value: cols[colMap.rorCol].trim(), ns: namespaces.ror, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (colMap.dealCol >= 0 && cols[colMap.dealCol] != null && !cols[colMap.dealCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.status != :removed', [value: cols[colMap.dealCol].trim(), ns: namespaces.dealId, removed: RDStore.TIPP_STATUS_REMOVED])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }

                if (match) {
                    processCount++
                    Map orgMap = [orgId: match.id]
                    orgList << orgMap

                } else {
                    wrongOrgs << i+2
                }
            }else{
                truncatedRows << i+2
            }
        }

        return [orgList: orgList, processCount: processCount, processRow: processRow, wrongOrgs: wrongOrgs.join(', '), truncatedRows: truncatedRows.join(', ')]
    }

    boolean modificationToCostInformation(SurveyOrg surveyOrg) {
        boolean modification = false
        Subscription subscription = surveyOrg.surveyConfig.subscription
        SurveyConfig surveyConfig = subscription ? SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(surveyOrg.surveyConfig.subscription, true) : null
        if(surveyConfig){
            int countModification = 0
            Date renewalDate = DocContext.executeQuery('select dateCreated from DocContext as dc where dc.subscription = :subscription and dc.owner.type = :docType and dc.owner.owner = :owner and dc.status is null order by dc.dateCreated desc', [subscription: subscription, docType: RDStore.DOC_TYPE_RENEWAL, owner: surveyConfig.surveyInfo.owner])[0]
            if(renewalDate)
                countModification = SurveyOrg.executeQuery('select count(*) from SurveyOrg as surOrg where surOrg = :surOrg ' +
                        'and (exists(select addr from Address as addr where addr = surOrg.address and addr.lastUpdated > :renewalDate) ' +
                        'or exists(select ct from Contact as ct where ct.prs = surOrg.person and ct.lastUpdated > :renewalDate) ' +
                        'or exists(select pr from Person as pr where pr = surOrg.person and pr.lastUpdated > :renewalDate))', [renewalDate: renewalDate, surOrg: surveyOrg])[0]
            modification = countModification > 0 ? true : false
        }

        return modification
    }

    int countModificationToCostInformationAfterRenewalDoc(Subscription subscription){
        int countModification = 0
        SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)
        if(surveyConfig){
            Date renewalDate = DocContext.executeQuery('select dateCreated from DocContext as dc where dc.subscription = :subscription and dc.owner.type = :docType and dc.owner.owner = :owner and dc.status is null order by dc.dateCreated desc', [subscription: subscription, docType: RDStore.DOC_TYPE_RENEWAL, owner: surveyConfig.surveyInfo.owner])[0]
            if(renewalDate)
            countModification = SurveyOrg.executeQuery('select count(*) from SurveyOrg as surOrg where surveyConfig = :surveyConfig ' +
                    'and (exists(select addr from Address as addr where addr = surOrg.address and addr.lastUpdated > :renewalDate) ' +
                    'or exists(select ct from Contact as ct where ct.prs = surOrg.person and ct.lastUpdated > :renewalDate) ' +
                    'or exists(select pr from Person as pr where pr = surOrg.person and pr.lastUpdated > :renewalDate))', [renewalDate: renewalDate, surveyConfig: surveyConfig])[0]

        }

        return countModification

    }

    List generatePropertyDataForCharts(SurveyConfig surveyConfig, List<Org> orgList){
        List chartSource = [['property', 'value']]

        List<PropertyDefinition> propList = SurveyConfigProperties.executeQuery("select scp.surveyProperty from SurveyConfigProperties scp where scp.surveyConfig = :surveyConfig", [surveyConfig: surveyConfig])
        propList.sort {it.getI10n('name')}.eachWithIndex {PropertyDefinition prop, int i ->
            if (prop.isRefdataValueType()) {
                def refDatas = SurveyResult.executeQuery("select sr.refValue from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and sr.refValue is not null and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList]).groupBy {it.getI10n('value')}
                refDatas.each {
                    chartSource << ["${prop.getI10n('name')}: ${it.key}", it.value.size()]
                }
            }
            if (prop.isIntegerType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.intValue is not null or sr.intValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isStringType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.stringValue is not null or sr.stringValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isBigDecimalType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.decValue is not null or sr.decValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isDateType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.dateValue is not null or sr.dateValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isURLType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.urlValue is not null or sr.urlValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
        }
        chartSource = chartSource.reverse()
        return chartSource.reverse()
    }

    List generateSurveyPackageDataForCharts(SurveyConfig surveyConfig, List<Org> orgList){
        List chartSource = [['property', 'value']]

        List<Package> packages = SurveyConfigPackage.executeQuery("select scp.pkg from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig order by scp.pkg.name", [surveyConfig: surveyConfig])

        packages.each {Package pkg ->
            chartSource << ["${pkg.name.replace('"', '')}", SurveyPackageResult.executeQuery("select count(*) from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant in (:participants) and spr.pkg = :pkg", [pkg: pkg, surveyConfig: surveyConfig, participants: orgList])[0]]
        }

        chartSource = chartSource.reverse()
        return chartSource.reverse()
    }

    Map getCostItemSumBySelectSurveyPackageOfParticipant(SurveyConfig surveyConfig, Org participant){
        Double sumCostInBillingCurrency = 0.0
        Double sumCostInBillingCurrencyAfterTax = 0.0

        List<SurveyPackageResult> surveyPackageResultList = SurveyPackageResult.findAllBySurveyConfigAndParticipant(surveyConfig, participant)

        if(surveyPackageResultList){
            List<CostItem> costItemList = CostItem.findAllBySurveyOrgAndPkgInListAndPkgIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant), surveyPackageResultList.pkg)
            costItemList.each { CostItem costItem ->

                sumCostInBillingCurrency = sumCostInBillingCurrency+ costItem.costInBillingCurrency
                sumCostInBillingCurrencyAfterTax = sumCostInBillingCurrencyAfterTax+ costItem.costInBillingCurrencyAfterTax
            }
        }

        [sumCostInBillingCurrency: sumCostInBillingCurrency, sumCostInBillingCurrencyAfterTax: sumCostInBillingCurrencyAfterTax]

    }


    Map participantResultGenerics(Map result, Org participant, GrailsParameterMap params){

        if (params.viewTab == 'overview') {
            if (result.surveyConfig.subscription) {
                result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)
                // restrict visible for templates/links/orgLinksAsList
                result.visibleOrgRelations = []
                if (result.subscription) {
                    result.subscription.orgRelations.each { OrgRole or ->
                        if (!(or.org.id == result.contextOrg.id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                            result.visibleOrgRelations << or
                        }
                    }
                    result.visibleOrgRelations.sort { it.org.sortname }

                    result.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                    result.links = linksGenerationService.getSourcesAndDestinations(result.subscription, result.user)

                    if (result.surveyConfig.subSurveyUseForTransfer) {
                        result.successorSubscriptionParent = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                        result.subscriptionParent = result.surveyConfig.subscription
                        Collection<AbstractPropertyWithCalculatedLastUpdated> props
                        props = result.subscriptionParent.propertySet.findAll { it.type.tenant == null && (it.tenant?.id == result.surveyInfo.owner.id || (it.tenant?.id != result.surveyInfo.owner.id && it.isPublic)) }
                        if (result.successorSubscriptionParent) {
                            props += result.successorSubscriptionParent.propertySet.findAll { it.type.tenant == null && (it.tenant?.id == result.surveyInfo.owner.id || (it.tenant?.id != result.surveyInfo.owner.id && it.isPublic)) }
                        }
                        result.customProperties = comparisonService.comparePropertiesWithAudit(props, true, true)
                    }

                    if (result.surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT) {

                        result.previousSubscription = result.subscription._getCalculatedPreviousForSurvey()

                        /*result.previousIesListPriceSum = 0
                       if(result.previousSubscription){
                           result.previousIesListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                                   'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                           [sub: result.previousSubscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0

                       }*/

                        result.sumListPriceSelectedIEsEUR = sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig, RDStore.CURRENCY_EUR)
                        result.sumListPriceSelectedIEsUSD = sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig, RDStore.CURRENCY_USD)
                        result.sumListPriceSelectedIEsGBP = sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig, RDStore.CURRENCY_GBP)


                        /* result.iesFixListPriceSum = PriceItem.executeQuery('select sum(p.listPrice) from PriceItem p join p.issueEntitlement ie ' +
                                 'where p.listPrice is not null and ie.subscription = :sub and ie.status = :ieStatus',
                                 [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT])[0] ?: 0 */

                        result.countSelectedIEs = countIssueEntitlementsByIEGroup(result.subscription, result.surveyConfig)
                        result.countCurrentPermanentTitles = subscriptionService.countCurrentPermanentTitles(result.subscription)
//                    if (result.surveyConfig.pickAndChoosePerpetualAccess) {
//                        result.countCurrentIEs = countPerpetualAccessTitlesBySub(result.subscription)
//                    } else {
//                        result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countCurrentIssueEntitlements(result.previousSubscription) : 0) + subscriptionService.countCurrentIssueEntitlements(result.subscription)
//                    }

                        result.subscriber = participant

                    }
                }

                if (!result.subscription) {
                    result.successorSubscriptionParent = result.surveyConfig.subscription._getCalculatedSuccessorForSurvey()
                    result.successorSubscription = result.successorSubscriptionParent ? result.successorSubscriptionParent.getDerivedSubscriptionForNonHiddenSubscriber(participant) : null
                } else {
                    result.successorSubscription = result.subscription._getCalculatedSuccessorForSurvey()
                }
                if (result.successorSubscription) {
                    List objects = []
                    if (result.subscription) {
                        objects << result.subscription
                    }
                    objects << result.successorSubscription
                    result = result + compareService.compareProperties(objects)
                }
            }
        }
        else if (params.viewTab == 'invoicingInformation') {
            if (result.editable) {
                if(params.setSurveyInvoicingInformation) {
                    if (params.personId) {
                        if (params.setConcact == 'true') {
                            result.surveyOrg.person = Person.get(Long.valueOf(params.personId))
                        }
                        if (params.setConcact == 'false') {
                            result.surveyOrg.person = null
                        }
                        result.surveyOrg.save()
                    }

                    if (params.addressId) {
                        if (params.setAddress == 'true') {
                            result.surveyOrg.address = Address.get(Long.valueOf(params.addressId))
                        }
                        if (params.setAddress == 'false') {
                            result.surveyOrg.address = null
                        }
                        result.surveyOrg.save()
                    }
                }
            }

            params.sort = params.sort ?: 'pr.org.sortname'
            params.org = participant
            result.visiblePersons = addressbookService.getVisiblePersons("contacts", params)
            result.addresses = addressbookService.getVisibleAddresses("contacts", params)

        }
        else if (params.viewTab == 'stats') {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)

            if(result.subscription){
                if (params.error)
                    result.error = params.error
                if (params.reportType)
                    result.putAll(subscriptionControllerService.loadFilterList(params))
                ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                result.flagContentGokb = true // gokbService.executeQuery
                Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription in (:subscriptions)", [subscriptions: [result.subscription, result.subscription.instanceOf]])
                result.platformInstanceRecords = [:]
                result.platforms = subscribedPlatforms
                result.platformsJSON = subscribedPlatforms.globalUID as JSON
                result.keyPairs = [:]
                if (!params.containsKey('tab'))
                    params.tab = subscribedPlatforms[0].id.toString()
                result.subscription.instanceOf.packages.each { SubscriptionPackage sp ->
                    Platform platformInstance = sp.pkg.nominalPlatform
                    if (result.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_LOCAL]) {
                        //create dummies for that they may be xEdited - OBSERVE BEHAVIOR for eventual performance loss!
                        CustomerIdentifier keyPair = CustomerIdentifier.findByPlatformAndCustomer(platformInstance, result.subscription.getSubscriberRespConsortia())
                        if (!keyPair) {
                            keyPair = new CustomerIdentifier(platform: platformInstance,
                                    customer: result.subscription.getSubscriberRespConsortia(),
                                    type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT,
                                    owner: contextService.getOrg(),
                                    isPublic: true)
                            if (!keyPair.save()) {
                                log.warn(keyPair.errors.getAllErrors().toListString())
                            }
                        }
                        result.keyPairs.put(platformInstance.gokbId, keyPair)
                    }
                    Map queryResult = gokbService.executeQuery(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [uuid: platformInstance.gokbId])
                    if (queryResult.error && queryResult.error == 404) {
                        result.wekbServerUnavailable = message(code: 'wekb.error.404')
                    } else if (queryResult) {
                        List records = queryResult.result
                        if (records[0]) {
                            records[0].lastRun = platformInstance.counter5LastRun ?: platformInstance.counter4LastRun
                            records[0].id = platformInstance.id
                            result.platformInstanceRecords[platformInstance.gokbId] = records[0]
                            result.platformInstanceRecords[platformInstance.gokbId].wekbUrl = apiSource.editUrl + "/resource/show/${platformInstance.gokbId}"
                            if (records[0].statisticsFormat == 'COUNTER' && records[0].counterR4SushiServerUrl == null && records[0].counterR5SushiServerUrl == null) {
                                result.error = 'noSushiSource'
                                ArrayList<Object> errorArgs = ["${apiSource.editUrl}/resource/show/${platformInstance.gokbId}", platformInstance.name]
                                result.errorArgs = errorArgs.toArray()
                            } else {
                                CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(result.subscription.getSubscriberRespConsortia(), platformInstance)
                                if (!ci?.value) {
                                    if (result.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_LOCAL])
                                        result.error = 'noCustomerId.local'
                                    else
                                        result.error = 'noCustomerId'
                                }
                            }
                        }
                    }
                    if (result.subscription._getCalculatedType() != CalculatedType.TYPE_CONSORTIAL) {
                        result.reportTypes = []
                        CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(result.subscription.getSubscriberRespConsortia(), platformInstance)
                        if (ci?.value) {
                            Set allAvailableReports = subscriptionControllerService.getAvailableReports(result)
                            if (allAvailableReports)
                                result.reportTypes.addAll(allAvailableReports)
                            else {
                                result.error = 'noReportAvailable'
                            }
                        } else if (!ci?.value) {
                            result.error = 'noCustomerId'
                        }
                    }
                }
            }
        }
        else if (params.viewTab == 'packageSurvey') {
            if (result.editable) {
                switch (params.actionsForSurveyPackages) {
                    case "addSurveyPackage":
                        Package pkg = Package.findByGokbId(params.pkgUUID)
                        if (SurveyConfigPackage.findBySurveyConfigAndPkg(result.surveyConfig, pkg) && !SurveyPackageResult.findBySurveyConfigAndParticipantAndPkg(result.surveyConfig, participant, pkg)) {
                            SurveyPackageResult surveyPackageResult = new SurveyPackageResult(surveyConfig: result.surveyConfig, participant: participant, pkg: pkg, owner: result.surveyInfo.owner)
                            surveyPackageResult.save()
                        }

                        break
                    case "removeSurveyPackage":
                        Package pkg = Package.findByGokbId(params.pkgUUID)
                        SurveyPackageResult surveyPackageResult = SurveyPackageResult.findBySurveyConfigAndParticipantAndPkg(result.surveyConfig, participant, pkg)
                        if (SurveyConfigPackage.findBySurveyConfigAndPkg(result.surveyConfig, pkg) && surveyPackageResult) {
                            surveyPackageResult.delete()
                        }
                        break
                }
            }

            if (result.surveyConfig.surveyPackages) {
                List uuidPkgs
                if (params.subTab == 'allPackages') {
                    result.uuidPkgs = SurveyPackageResult.executeQuery("select spr.pkg.gokbId from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant = :participant", [participant: participant, surveyConfig: result.surveyConfig])
                    uuidPkgs = SurveyConfigPackage.executeQuery("select scg.pkg.gokbId from SurveyConfigPackage scg where scg.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])
                } else if (params.subTab == 'selectPackages') {
                    List<Long> ids = SurveyPackageResult.executeQuery("select spr.pkg.gokbId from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant = :participant", [participant: participant, surveyConfig: result.surveyConfig])
                    if (ids.size() > 0) {
                        uuidPkgs = ids
                    } else {
                        uuidPkgs = ['fakeUuids']
                    }
                }

                params.uuids = uuidPkgs
                params.max = params.max ? Integer.parseInt(params.max) : result.user.getPageSizeOrDefault()
                params.offset = params.offset ? Integer.parseInt(params.offset) : 0
                result.putAll(packageService.getWekbPackages(params))
            } else {
                result.records = []
                result.recordsCount = 0
            }
        }

        return result

    }

}
