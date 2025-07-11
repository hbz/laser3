package de.laser

import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReader
import com.opencsv.CSVReaderBuilder
import com.opencsv.ICSVParser
import de.laser.addressbook.Address
import de.laser.addressbook.Person
import de.laser.auth.User
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.cache.EhcacheWrapper
import de.laser.ctrl.SubscriptionControllerService
import de.laser.finance.CostItem
import de.laser.config.ConfigDefaults
import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.remote.Wekb
import de.laser.stats.Counter4ApiSource
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5ApiSource
import de.laser.stats.Counter5Report
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigPackage
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyConfigSubscription
import de.laser.survey.SurveyConfigVendor
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyPackageResult
import de.laser.survey.SurveyPersonResult
import de.laser.survey.SurveyResult
import de.laser.survey.SurveySubscriptionResult
import de.laser.survey.SurveyUrl
import de.laser.config.ConfigMapper
import de.laser.survey.SurveyVendorResult
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.gsp.PageRenderer
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.time.TimeCategory
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource
import org.springframework.web.multipart.MultipartFile

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.text.SimpleDateFormat
import java.time.Year
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This service manages survey handling
 */
@Transactional
class SurveyService {

    AddressbookService addressbookService
    AuditService auditService
    BatchQueryService batchQueryService
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
    SubscriptionsQueryService subscriptionsQueryService
    MailSendService mailSendService
    VendorService vendorService

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

        if (contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO ) && surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (contextService.isInstEditor( CustomerTypeService.ORG_INST_BASIC )) {
            SurveyOrg surveyOrg = SurveyOrg.findByOrgAndSurveyConfigInList(org, surveyInfo.surveyConfigs)

            if (surveyOrg && surveyOrg.finishDate == null) {
                return true
            } else {
                return false
            }
        }else{
            return false
        }
    }

    @Deprecated
    boolean isEditableIssueEntitlementsSurvey(Org org, SurveyConfig surveyConfig) {

        if (contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_PRO ) && surveyConfig.surveyInfo.owner?.id == contextService.getOrg().id) {
            return true
        }

        if (!surveyConfig.pickAndChoose) {
            return false
        }

        if (surveyConfig.surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED) {
            return false
        }

        if (contextService.isInstEditor( CustomerTypeService.ORG_INST_BASIC )) {

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

                    }

                    SurveyResult.findAllBySurveyConfigAndParticipant(surveyConfig, surveyOrg.org).sort{it.type.getI10n('name')}.each { surResult ->
                        row.add([field: surResult.type?.getI10n('name') ?: '', style: null])
                        row.add([field: PropertyDefinition.getLocalizedValue(surResult.type.type) ?: '', style: null])

                        String value = ""

                        if (surResult.type.isLongType()) {
                            value = surResult.longValue ? surResult.longValue.toString() : ""
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

                    CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED).each { CostItem surveyCostItem ->
                        row.add([field: surveyCostItem.costItemElement?.getI10n('value') ?: '', style: null])
                        row.add([field: surveyCostItem.costInBillingCurrency ?: '', style: null])
                        row.add([field: surveyCostItem.billingCurrency?.value ?: '', style: null])
                        String surveyCostTax
                        if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                            surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                        else if(surveyCostItem.taxKey)
                            surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                        else
                            surveyCostTax = ''
                        row.add([field: surveyCostTax, style: null])
                        if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                            row.add([field: '', style: null])
                        else
                            row.add([field: surveyCostItem.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                        row.add([field: surveyCostItem.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                        row.add([field: surveyCostItem.costDescription ?: '', style: null])
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

                    if(subscription) {
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

                    CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED).each { CostItem surveyCostItem ->
                        row.add([field: surveyCostItem.costItemElement?.getI10n('value') ?: '', style: null])
                        row.add([field: surveyCostItem.costInBillingCurrency ?: '', style: null])
                        row.add([field: surveyCostItem.billingCurrency?.value ?: '', style: null])
                        String surveyCostTax
                        if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                            surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                        else if(surveyCostItem.taxKey)
                            surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                        else
                            surveyCostTax = ''
                        row.add([field: surveyCostTax, style: null])
                        if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                            row.add([field: '', style: null])
                        else
                            row.add([field: surveyCostItem.costInBillingCurrencyAfterTax ?: '', style: null])
                        row.add([field: surveyCostItem.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                        row.add([field: surveyCostItem.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                        row.add([field: surveyCostItem.costDescription ?: '', style: null])
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

                    if (surResult.type.isLongType()) {
                        value = surResult?.longValue ? surResult.longValue.toString() : ""
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


                //Library Supplier-Umfrage
                if(surveyConfig.vendorSurvey) {
                    surveyData.add([])
                    surveyData.add([])
                    surveyData.add([])
                    surveyData.add([[field: messageSource.getMessage('surveyconfig.vendorSurvey.label', null, locale), style: 'bold']])
                    List rowVendor = [[field: messageSource.getMessage('default.sortname.label', null, locale), style: 'bold'],
                                      [field: messageSource.getMessage('default.name.label', null, locale), style: 'bold'],
                                 [field: messageSource.getMessage('surveyResult.comment', null, locale), style: 'bold'],
                                 [field: messageSource.getMessage('surveyResult.commentOnlyForParticipant', null, locale), style: 'bold']]
                    surveyData.add(rowVendor)

                    SurveyVendorResult.findAllBySurveyConfigAndParticipant(surveyConfig, contextOrg).sort { it.vendor.name }.each { surveyVendorResult ->
                        List row3 = []
                        row3.add([field: surveyVendorResult.vendor.sortname, style: null])
                        row3.add([field: surveyVendorResult.vendor.name, style: null])
                        row3.add([field: surveyVendorResult.comment ?: '', style: null])
                        row3.add([field: surveyVendorResult.participantComment ?: '', style: null])
                        surveyData.add(row3)
                    }
                }


                //Paket-Umfrage
                if(surveyConfig.packageSurvey) {
                    surveyData.add([])
                    surveyData.add([])
                    surveyData.add([])
                    surveyData.add([[field: messageSource.getMessage('surveyconfig.packageSurvey.label', null, locale), style: 'bold']])
                    List rowPackage = [[field: messageSource.getMessage('package.show.pkg_name', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.status.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('package.compare.overview.tipps', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('provider.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('surveyResult.comment', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('surveyResult.commentOnlyForParticipant', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.costItemElement', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.costInBillingCurrency', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.currency.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.startDate.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.endDate.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale), style: 'bold']]
                    surveyData.add(rowPackage)

                    SurveyPackageResult.findAllBySurveyConfigAndParticipant(surveyConfig, contextOrg).sort { it.pkg.name }.each { surveyPackageResult ->
                        List row3 = []
                        row3.add([field: surveyPackageResult.pkg.name, style: null])
                        row3.add([field: surveyPackageResult.pkg.packageStatus?.getI10n('value'), style: null])
                        row3.add([field: surveyPackageResult.pkg.getCurrentTippsCount(), style: null])
                        row3.add([field: surveyPackageResult.pkg.provider.name, style: null])
                        row3.add([field: surveyPackageResult.comment ?: '', style: null])
                        row3.add([field: surveyPackageResult.participantComment ?: '', style: null])
                        CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndPkg(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED, surveyPackageResult.pkg).each { CostItem surveyCostItem ->
                            row3.add([field: surveyCostItem.costItemElement?.getI10n('value') ?: '', style: null])
                            row3.add([field: surveyCostItem.costInBillingCurrency ?: '', style: null])
                            row3.add([field: surveyCostItem.billingCurrency?.value ?: '', style: null])
                            String surveyCostTax
                            if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                            else if(surveyCostItem.taxKey)
                                surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                            else
                                surveyCostTax = ''
                            row3.add([field: surveyCostTax, style: null])
                            if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                row3.add([field: '', style: null])
                            else
                                row3.add([field: surveyCostItem.costInBillingCurrencyAfterTax ?: '', style: null])
                            row3.add([field: surveyCostItem.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                            row3.add([field: surveyCostItem.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                            row3.add([field: surveyCostItem.costDescription ?: '', style: null])
                        }

                        surveyData.add(row3)
                    }
                }

                //Lizenz-Umfrage
                if(surveyConfig.subscriptionSurvey) {
                    surveyData.add([])
                    surveyData.add([])
                    surveyData.add([])
                    surveyData.add([[field: messageSource.getMessage('surveyconfig.subscriptionSurvey.label', null, locale), style: 'bold']])
                    List rowPackage = [[field: messageSource.getMessage('default.name.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.status.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('provider.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('surveyResult.comment', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('surveyResult.commentOnlyForParticipant', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.costItemElement', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.costInBillingCurrency', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.currency.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.newCosts.taxTypeAndRate', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('financials.costInBillingCurrencyAfterTax', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.startDate.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('default.endDate.label', null, locale), style: 'bold'],
                                       [field: messageSource.getMessage('surveyConfigsInfo.newPrice.comment', null, locale), style: 'bold']]
                    surveyData.add(rowPackage)

                    SurveySubscriptionResult.findAllBySurveyConfigAndParticipant(surveyConfig, contextOrg).sort { it.subscription.name }.each { surveySubscriptionResult ->
                        List row3 = []
                        row3.add([field: surveySubscriptionResult.subscription.name, style: null])
                        row3.add([field: surveySubscriptionResult.subscription.status?.getI10n('value'), style: null])
                        row3.add([field: surveySubscriptionResult.subscription.providers?.collect {it.name}.join('; '), style: null])
                        row3.add([field: surveySubscriptionResult.comment ?: '', style: null])
                        row3.add([field: surveySubscriptionResult.participantComment ?: '', style: null])
                        CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndSurveyConfigSubscription(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED, de.laser.survey.SurveyConfigSubscription.findBySurveyConfigAndSubscription(surveySubscriptionResult.surveyConfig, surveySubscriptionResult.subscription)).each { CostItem surveyCostItem ->
                            row3.add([field: surveyCostItem.costItemElement?.getI10n('value') ?: '', style: null])
                            row3.add([field: surveyCostItem.costInBillingCurrency ?: '', style: null])
                            row3.add([field: surveyCostItem.billingCurrency?.value ?: '', style: null])
                            String surveyCostTax
                            if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                            else if(surveyCostItem.taxKey)
                                surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                            else
                                surveyCostTax = ''
                            row3.add([field: surveyCostTax, style: null])
                            if(surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                row3.add([field: '', style: null])
                            else
                                row3.add([field: surveyCostItem.costInBillingCurrencyAfterTax ?: '', style: null])
                            row3.add([field: surveyCostItem.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                            row3.add([field: surveyCostItem.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                            row3.add([field: surveyCostItem.costDescription ?: '', style: null])
                        }

                        surveyData.add(row3)
                    }
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

                        CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, surveyOrg.org), RDStore.COST_ITEM_DELETED).each { CostItem surveyCostItem ->
                                row.add([field: surveyCostItem.costItemElement?.getI10n('value') ?: '', style: null])
                                row.add([field: surveyCostItem.costInBillingCurrency ?: '', style: null])
                                row.add([field: surveyCostItem.billingCurrency?.value ?: '', style: null])
                                String surveyCostTax
                                if (surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                    surveyCostTax = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")
                                else if (surveyCostItem.taxKey)
                                    surveyCostTax = surveyCostItem.taxKey.taxType?.getI10n("value") + " (" + surveyCostItem.taxKey.taxRate + "%)"
                                else
                                    surveyCostTax = ''
                                row.add([field: surveyCostTax, style: null])
                                if (surveyCostItem.taxKey && surveyCostItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                    row.add([field: '', style: null])
                                else
                                    row.add([field: surveyCostItem.costInBillingCurrencyAfterTax ?: '', style: null])
                                row.add([field: surveyCostItem.startDate ? formatter.format(surveyCostItem.startDate) : '', style: null])
                                row.add([field: surveyCostItem.endDate ? formatter.format(surveyCostItem.endDate) : '', style: null])
                                row.add([field: surveyCostItem.costDescription ?: '', style: null])
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
                    if(subscription) {
                        row.add([field: subscription.providers.join(", "), style: null])
                        row.add([field: subscription.vendors.join(", "), style: null])

                        row.add([field: subscription.status?.getI10n("value") ?: '', style: null])
                    }else{
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                    }

                    CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, contextOrg), RDStore.COST_ITEM_DELETED)

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

                                CostItem surveyCostItem = CostItem.findBySurveyOrgAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(surveyOrg, RDStore.COST_ITEM_DELETED)

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

                            if (surveyResult.type.isLongType()) {
                                value = surveyResult?.longValue ? surveyResult.longValue.toString() : ""
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

            boolean selectable = selectableDespiteMultiYearTerm(surveyConfig, org)

            if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org)) && selectable) {
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


                                setDefaultPreferredConcatsForSurvey(surveyConfig, org)


                                if(surveyConfig.pickAndChoose){
                                    Subscription participantSub = surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)
                                    IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, participantSub)
                                    if (!issueEntitlementGroup && participantSub) {
                                        String groupName = IssueEntitlementGroup.countBySubAndName(participantSub, surveyConfig.issueEntitlementGroupName) > 0 ? (IssueEntitlementGroup.countBySubAndNameIlike(participantSub, surveyConfig.issueEntitlementGroupName) + 1) : surveyConfig.issueEntitlementGroupName
                                        new IssueEntitlementGroup(surveyConfig: surveyConfig, sub: participantSub, name: groupName).save()
                                    }
                                }
                            }
                        }
                }
            }

    }

    void addMultiYearSubMembers(SurveyConfig surveyConfig) {
        Map<String, Object> result = [:]
        result.institution = contextService.getOrg()

        List currentMembersSubs = subscriptionService.getValidSurveySubChilds(surveyConfig.subscription)

        currentMembersSubs.each { Subscription subChild ->
            Org org = subChild.getSubscriberRespConsortia()

            if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org))) {

                boolean existsMultiYearTerm = existsMultiYearTermBySurveyUseForTransfer(surveyConfig, org)

                if (existsMultiYearTerm) {
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


                            setDefaultPreferredConcatsForSurvey(surveyConfig, org)


                            if(surveyConfig.pickAndChoose){
                                Subscription participantSub = surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)
                                IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, participantSub)
                                if (!issueEntitlementGroup && participantSub) {
                                    String groupName = IssueEntitlementGroup.countBySubAndName(participantSub, surveyConfig.issueEntitlementGroupName) > 0 ? (IssueEntitlementGroup.countBySubAndNameIlike(participantSub, surveyConfig.issueEntitlementGroupName) + 1) : surveyConfig.issueEntitlementGroupName
                                    new IssueEntitlementGroup(surveyConfig: surveyConfig, sub: participantSub, name: groupName).save()
                                }
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
                            owner: dctx.owner.owner,
                            server: dctx.owner.server,
                            ckey: dctx.owner.ckey
                    ).save()
                    String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                    Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                    Path target = new File("${fPath}/${clonedContents.uuid}").toPath()
                    Files.copy(source, target)
                    new DocContext(
                            owner: clonedContents,
                            surveyConfig: newSurveyConfig,
                            status: dctx.status
                    ).save()
                }
            }
            //Copy Announcements
            if (params.copySurvey.copyAnnouncements) {
                if (dctx.isDocANote() && (dctx.status != RDStore.DOC_CTX_STATUS_DELETED)) {
                    Doc clonedContents = new Doc(
                            type: dctx.getDocType(),
                            confidentiality: dctx.getDocConfid(),
                            content: dctx.owner.content,
                            uuid: dctx.owner.uuid,
                            contentType: dctx.owner.contentType,
                            title: dctx.owner.title,
                            filename: dctx.owner.filename,
                            mimeType: dctx.owner.mimeType
                    ).save()
                    new DocContext(
                            owner: clonedContents,
                            surveyConfig: newSurveyConfig,
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
                        propertyOrder: surveyConfigProperty.propertyOrder,
                        mandatoryProperty: surveyConfigProperty.mandatoryProperty).save()
            }
        }
        if (params.copySurvey.copyPackages) {
            oldSurveyConfig.surveyPackages.each { SurveyConfigPackage surveyConfigPackage ->
                new SurveyConfigPackage(surveyConfig: newSurveyConfig, pkg: surveyConfigPackage.pkg).save()
            }
        }

        if (params.copySurvey.copySubscriptions) {
            oldSurveyConfig.surveySubscriptions.each { SurveyConfigSubscription surveyConfigSubscription ->
                new SurveyConfigSubscription(surveyConfig: newSurveyConfig, pkg: surveyConfigSubscription.subscription).save()
            }
        }

        if (params.copySurvey.copyVendors) {
            oldSurveyConfig.surveyVendors.each { SurveyConfigVendor surveyConfigVendor ->
                new SurveyConfigVendor(surveyConfig: newSurveyConfig, vendor: surveyConfigVendor.vendor).save()
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
            count5check.addAll(Counter5Report.executeQuery('select count(*) from Counter5Report r where r.reportInstitution = :customer and r.platform in (:platforms)'+dateRange, c5CheckParams))
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

                    count4check.addAll(Counter4Report.executeQuery('select count(*) from Counter4Report r left join r.title title where r.reportInstitution = :customer and r.platform in (:platforms)' + filter + dateRange, queryParams))
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
     * Substitution call to ${@link #hasParticipantPerpetualAccessToTitle2(java.util.List, de.laser.wekb.TitleInstancePackagePlatform)}
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
                "(pt.owner = :org or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :org and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))",
                [hostPlatformURL: tipp.hostPlatformURL,
                 tippStatus: RDStore.TIPP_STATUS_REMOVED,
                 tipp: tipp,
                 org: org,
                 subscriberCons: RDStore.OR_SUBSCRIBER_CONS])[0]
        /*countPermanentTitles += IssueEntitlement.executeQuery('select count(*) from IssueEntitlement ie where ie.tipp = :tipp and (' +
                'ie.perpetualAccessBySub in (select oo.sub from OrgRole oo where oo.org = :context and oo.roleType = :subscriber) or ' +
                "ie.perpetualAccessBySub in (select s from OrgRole oc join oc.sub s where oc.org = :context and oc.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection'))" +
                ')', [tipp: tipp, context: org, subscriber: RDStore.OR_SUBSCRIBER, subscriberCons: RDStore.OR_SUBSCRIBER_CONS])*/

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
                "(pt.owner = :org or pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :org and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')))",
                [hostPlatformURL: tipp.hostPlatformURL,
                 tippStatus: RDStore.TIPP_STATUS_REMOVED,
                 tipp: tipp,
                 org: org,
                 subscriberCons: RDStore.OR_SUBSCRIBER_CONS])

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
        RefdataValue role_sub_consortia = RDStore.OR_SUBSCRIPTION_CONSORTIUM

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
        try {
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
        finally {
            sql.close()
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
            try {
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
            finally {
                sql.close()
            }
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
            try {
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
            finally {
                sql.close()
            }
        }
        return issueEntitlementIds
    }

    /**
     * Counts the titles perpetually purchased via the given subscription. Regarded is the time span of all year rings, i.e.
     * subscriptions preceding the given one are counted as well
     * @param subscription the {@link Subscription} whose perpetually accessible titles should be counted
     * @return the count of {@link IssueEntitlement}s to which perpetual access have been granted over all years of the given subscription
     */
    @Deprecated
    Integer countPerpetualAccessTitlesBySub(Subscription subscription) {
        Integer count = 0
        Set<Subscription> subscriptions = linksGenerationService.getSuccessionChain(subscription, 'sourceSubscription')
        subscriptions << subscription

        if(subscriptions.size() > 0) {
            List<Object> subIds = []
            subIds.addAll(subscriptions.id)
            Sql sql = GlobalService.obtainSqlConnection()
            try {
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
            finally {
                sql.close()
            }
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
    @Deprecated
    Integer countPerpetualAccessTitlesBySubAndNotInIEGroup(Subscription subscription, SurveyConfig surveyConfig) {
        Integer count = 0
        Set<Subscription> contextSubscriptions = linksGenerationService.getSuccessionChain(subscription, 'sourceSubscription')
        contextSubscriptions << subscription

        IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(surveyConfig, subscription)

        if(contextSubscriptions.size() > 0 && issueEntitlementGroup) {
            Set<Subscription> subs = []
            contextSubscriptions.each { Subscription s ->
                subs << s
                if (s.instanceOf && auditService.getAuditConfig(s.instanceOf, 'holdingSelection'))
                    subs << s.instanceOf
            }
            Set<Subscription> otherSubscriptions = subscriptionService.getSubscriptionsWithPossiblePerpetualTitles(subscription.getSubscriber())
            otherSubscriptions.removeAll(contextSubscriptions)
            Set<String> subscribedHostPlatformURLs = TitleInstancePackagePlatform.executeQuery("select tipp.hostPlatformURL from TitleInstancePackagePlatform tipp, SubscriptionPackage sp where sp.pkg = tipp.pkg and sp.subscription in (:subs)", [subs: contextSubscriptions])
            List ieDirectCount = IssueEntitlement.executeQuery("select count(*) from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subs) and ie.perpetualAccessBySub in (:subs) and ie.status = :tippStatus and ie not in (select igi.ie from IssueEntitlementGroupItem igi where igi.ieGroup = :ieGroup)", [subs: subs, tippStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
            List ieIndirectCount = PermanentTitle.executeQuery("select count(*) from PermanentTitle pt join pt.tipp tipp where tipp.hostPlatformURL in (:subscribedHostPlatformURLs) and pt.subscription in (:otherSubs)", [subscribedHostPlatformURLs: subscribedHostPlatformURLs, otherSubs: otherSubscriptions])
            if(ieDirectCount)
                count += ieDirectCount[0]
            if(ieIndirectCount)
                count += ieIndirectCount[0]
                /*
                List<GroovyRowResult> titles = sql.rows("select count(*) from issue_entitlement ie2 join title_instance_package_platform tipp2 on tipp2.tipp_id = ie2.ie_tipp_fk " +
                        " where ie2.ie_subscription_fk = any(:subs) and ie2.ie_perpetual_access_by_sub_fk = any(:subs)" +
                        " and ie2.ie_status_rv_fk = :tippStatus " +
                        " and ie2.ie_id not in (select igi_ie_fk from issue_entitlement_group_item where igi_ie_group_fk = :ieGroup) group by tipp2.tipp_host_platform_url", [subs: connection.createArrayOf('bigint', subIds.toArray()), tippStatus: RDStore.TIPP_STATUS_CURRENT.id, ieGroup: issueEntitlementGroup.id])

                if(titles)
                    count += titles[0]['count']
                List<GroovyRowResult> otherConsortia = sql.rows("select count(*) from permanent_title join title_instance_package_platform on pt_tipp_fk = tipp_id join subscription s on pt_subscription_fk = sub_id where " +
                        "tipp_host_platform_url in (select tipp.tipp_host_platform_url from title_instance_package_platform as tipp join subscription_package on sp_pkg_fk = tipp.tipp_pkg_fk where sp_sub_fk = any(:contextSubs)) and" +
                        "(pt_owner_fk = :subscriber and sub_parent_sub_fk is null) or (sub_id in (select os.sub_id from org_role join subscription as os on or_org_fk = os.sub_id where or_org_fk = :subscriber and or_roletype_fk = :subscrCons and os.sub_parent_sub_fk in (select auc_reference_id from audit_config where auc_reference_field = 'holdingSelection') and os.sub_parent_sub_fk != any(:contextSubs)))", [subscriber: subscription.getSubscriber().id, subscrCons: RDStore.OR_SUBSCRIBER_CONS.id, contextSubs: connection.createArrayOf('bigint', subIds.toArray())])
                if(otherConsortia)
                    count += otherConsortia[0]['count']
                */
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
                IssueEntitlementGroupItem.executeQuery("select count(*) from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status != :status",
                        [ieGroup: issueEntitlementGroup, status: RDStore.TIPP_STATUS_REMOVED])[0]
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
            BigDecimal sumListPrice = 0.0
            //sql bridge
            Sql sql = GlobalService.obtainSqlConnection()
            try {
                List<GroovyRowResult> tippIdRows = sql.rows("select ie_tipp_fk from issue_entitlement join issue_entitlement_group_item on ie_id = igi_ie_fk where igi_ie_group_fk = :ieGroup and ie_status_rv_fk <> :status", [ieGroup: issueEntitlementGroup.id, status: RDStore.TIPP_STATUS_REMOVED.id])
                if(tippIdRows) {
                    List<GroovyRowResult> result = batchQueryService.longArrayQuery("select sum(pi.pi_list_price) as sum_list_price from (select pi_tipp_fk, pi_list_price, row_number() over (partition by pi_tipp_fk order by pi_date_created desc) as rn, count(*) over (partition by pi_tipp_fk) as cn from price_item where pi_list_price is not null and pi_list_currency_rv_fk = :currency and pi_tipp_fk = any(:tippIDs)) as pi where pi.rn = 1", [tippIDs: tippIdRows['ie_tipp_fk']], [currency: currency.id])
                    if(result)
                        sumListPrice = result[0]['sum_list_price']
                }
                /*
                BigDecimal sumListPrice = issueEntitlementGroup ?
                        IssueEntitlementGroupItem.executeQuery("select sum(p.listPrice) from PriceItem p where p.listPrice is not null and p.listCurrency = :currency and p.tipp in (select igi.ie.tipp from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup and igi.ie.status != :status)",
                                [ieGroup: issueEntitlementGroup, currency: currency, status: RDStore.TIPP_STATUS_REMOVED])[0]
                        : 0.0
                sumListPrice
                */
            }
            finally {
                sql.close()
            }
            sumListPrice
        }
        else 0.0
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
                'OR longValue is not null ' +
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
                case ["laser-id", "laser-id (einrichtung)", "laser-id (institution)", "laser-id (einrichtungslizenz)", "laser-id (institution subscription)"]: colMap.uuidCol = c
                    break
                case "gnd-id": colMap.gndCol = c
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
                if (colMap.uuidCol >= 0 && cols[colMap.uuidCol] != null && !cols[colMap.uuidCol].trim().isEmpty()) {
                    match = Org.findByGlobalUIDAndArchiveDateIsNull(cols[colMap.uuidCol].trim())
                }
                if (!match && colMap.wibCol >= 0 && cols[colMap.wibCol] != null && !cols[colMap.wibCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.wibCol].trim(), ns: namespaces.wib])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.isilCol >= 0 && cols[colMap.isilCol] != null && !cols[colMap.isilCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.isilCol].trim(), ns: namespaces.isil])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.gndCol >= 0 && cols[colMap.gndCol] != null && !cols[colMap.gndCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.gndCol].trim(), ns: namespaces.gnd])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.rorCol >= 0 && cols[colMap.rorCol] != null && !cols[colMap.rorCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.rorCol].trim(), ns: namespaces.ror])
                    if (matchList.size() == 1)
                        match = matchList[0] as Org
                }
                if (!match && colMap.dealCol >= 0 && cols[colMap.dealCol] != null && !cols[colMap.dealCol].trim().isEmpty()) {
                    List matchList = Org.executeQuery('select org from Identifier id join id.org org where id.value = :value and id.ns = :ns and org.archiveDate is null', [value: cols[colMap.dealCol].trim(), ns: namespaces.dealId])
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

    boolean modificationToContactInformation(SurveyOrg surveyOrg) {
        boolean modification = false
        Subscription subscription = surveyOrg.surveyConfig.subscription
        SurveyConfig surveyConfig = subscription ? SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(surveyOrg.surveyConfig.subscription, true) : null
        Subscription prevSub = subscription ? subscription._getCalculatedPreviousForSurvey() : null
        if(surveyConfig && surveyConfig.invoicingInformation && prevSub){
            int countModification = 0
            Date renewalDate = DocContext.executeQuery('select dateCreated from DocContext as dc where dc.subscription = :subscription and dc.owner.type = :docType and dc.owner.owner = :owner and dc.status is null order by dc.dateCreated desc', [subscription: prevSub, docType: RDStore.DOC_TYPE_RENEWAL, owner: surveyConfig.surveyInfo.owner])[0]
            if(renewalDate) {
                countModification = SurveyOrg.executeQuery('select count(*) from SurveyOrg as surOrg where surOrg = :surOrg ' +
                        'and (exists(select addr from Address as addr where addr = surOrg.address and addr.lastUpdated > :renewalDate) ' +
                        'or exists(select ct from Contact as ct where ct.prs in (select spr.person from SurveyPersonResult as spr where spr.participant = surOrg.org and spr.surveyConfig = surOrg.surveyConfig and spr.billingPerson = true) and ct.lastUpdated > :renewalDate) ' +
                        'or exists(select pr from Person as pr where pr in (select spr.person from SurveyPersonResult as spr where spr.participant = surOrg.org and spr.surveyConfig = surOrg.surveyConfig and spr.billingPerson = true) and pr.lastUpdated > :renewalDate))', [renewalDate: renewalDate, surOrg: surveyOrg])[0]
                modification = countModification > 0 ? true : false
            }
        }

        return modification
    }
    int countModificationToContactInformationAfterRenewalDoc(Subscription subscription){
        int countModification = 0
        SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)
        Subscription prevSub = subscription._getCalculatedPreviousForSurvey()
        if(surveyConfig && surveyConfig.invoicingInformation && prevSub){
            Date renewalDate = DocContext.executeQuery('select dateCreated from DocContext as dc where dc.subscription = :subscription and dc.owner.type = :docType and dc.owner.owner = :owner and dc.status is null order by dc.dateCreated desc', [subscription: prevSub, docType: RDStore.DOC_TYPE_RENEWAL, owner: surveyConfig.surveyInfo.owner])[0]
            if(renewalDate) {
                countModification = SurveyOrg.executeQuery('select count(*) from SurveyOrg as surOrg where surveyConfig = :surveyConfig ' +
                        'and (exists(select addr from Address as addr where addr = surOrg.address and addr.lastUpdated > :renewalDate) ' +
                        'or exists(select ct from Contact as ct where ct.prs in (select spr.person from SurveyPersonResult as spr where spr.participant = surOrg.org and spr.surveyConfig = surOrg.surveyConfig and spr.billingPerson = true) and ct.lastUpdated > :renewalDate) ' +
                        'or exists(select pr from Person as pr where pr in (select spr.person from SurveyPersonResult as spr where spr.participant = surOrg.org and spr.surveyConfig = surOrg.surveyConfig and spr.billingPerson = true) and pr.lastUpdated > :renewalDate))', [renewalDate: renewalDate, surveyConfig: surveyConfig])[0]
            }

        }

        return countModification

    }

    List generatePropertyDataForCharts(SurveyConfig surveyConfig, List<Org> orgList){
        //List chartSource = [['property', 'value']]
        List chartSource = []

        List<PropertyDefinition> propList = SurveyConfigProperties.executeQuery("select scp.surveyProperty from SurveyConfigProperties scp where scp.surveyConfig = :surveyConfig", [surveyConfig: surveyConfig])
        propList.sort {it.getI10n('name')}.eachWithIndex {PropertyDefinition prop, int i ->
            if (prop.isRefdataValueType()) {
                def refDatas = SurveyResult.executeQuery("select sr.refValue from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and sr.refValue is not null and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList]).groupBy {it.getI10n('value')}
                refDatas.each {
                    chartSource << ["${prop.getI10n('name')}: ${it.key}", it.value.size()]
                }
            }
            if (prop.isLongType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.longValue is not null) and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isStringType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.stringValue is not null or sr.stringValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isBigDecimalType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.decValue is not null) and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isDateType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.dateValue is not null) and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
            else if (prop.isURLType()) {
                chartSource << ["${prop.getI10n('name')}", SurveyResult.executeQuery("select count(*) from SurveyResult sr where sr.surveyConfig = :surveyConfig and sr.participant in (:participants) and (sr.urlValue is not null or sr.urlValue != '') and sr.type = :propType", [propType: prop, surveyConfig: surveyConfig, participants: orgList])[0]]
            }
        }
        chartSource = chartSource.reverse()
        return chartSource
    }

    List generateSurveyPackageDataForCharts(SurveyConfig surveyConfig, List<Org> orgList){
        //List chartSource = [['property', 'value']]
        List chartSource = []

        List<Package> packages = SurveyConfigPackage.executeQuery("select scp.pkg from SurveyConfigPackage scp where scp.surveyConfig = :surveyConfig order by scp.pkg.name asc", [surveyConfig: surveyConfig])

        packages.each {Package pkg ->
            int countPackages = SurveyPackageResult.executeQuery("select count(*) from SurveyPackageResult spr where spr.surveyConfig = :surveyConfig and spr.participant in (:participants) and spr.pkg = :pkg", [pkg: pkg, surveyConfig: surveyConfig, participants: orgList])[0]
            if(countPackages > 0) {
                chartSource << ["${pkg.name.replace('"', '')}", countPackages]
            }
        }

        chartSource = chartSource.reverse()
        return chartSource
    }

    List generateSurveySubscriptionDataForCharts(SurveyConfig surveyConfig, List<Org> orgList){
        //List chartSource = [['property', 'value']]
        List chartSource = []

        List<Subscription> subs = SurveyConfigSubscription.executeQuery("select scp.subscription from SurveyConfigSubscription scp where scp.surveyConfig = :surveyConfig order by scp.subscription.name asc", [surveyConfig: surveyConfig])

        subs.each {Subscription sub ->
            int countSubs = SurveySubscriptionResult.executeQuery("select count(*) from SurveySubscriptionResult spr where spr.surveyConfig = :surveyConfig and spr.participant in (:participants) and spr.subscription = :subscription", [subscription: sub, surveyConfig: surveyConfig, participants: orgList])[0]
            if(countSubs > 0) {
                chartSource << ["${sub.name.replace('"', '')}", countSubs]
            }
        }

        chartSource = chartSource.reverse()
        return chartSource
    }

    List generateSurveyVendorDataForCharts(SurveyConfig surveyConfig, List<Org> orgList){
        //List chartSource = [['property', 'value']]
        List chartSource = []

        List<Vendor> vendors = SurveyConfigVendor.executeQuery("select scv.vendor from SurveyConfigVendor scv where scv.surveyConfig = :surveyConfig order by scv.vendor.name asc", [surveyConfig: surveyConfig])

        vendors.each {Vendor vendor ->
            int countVendors = SurveyVendorResult.executeQuery("select count(*) from SurveyVendorResult svr where svr.surveyConfig = :surveyConfig and svr.participant in (:participants) and svr.vendor = :vendor", [vendor: vendor, surveyConfig: surveyConfig, participants: orgList])[0]
            if(countVendors > 0) {
                chartSource << ["${vendor.name.replace('"', '')}", countVendors]
            }
        }

        chartSource = chartSource.reverse()
        return chartSource.reverse()
    }

    Map getCostItemSumBySelectSurveyPackageOfParticipant(SurveyConfig surveyConfig, Org participant){
        Double sumCostInBillingCurrency = 0.0
        Double sumCostInBillingCurrencyAfterTax = 0.0

        List<SurveyPackageResult> surveyPackageResultList = SurveyPackageResult.findAllBySurveyConfigAndParticipant(surveyConfig, participant)

        if(surveyPackageResultList){
            List<CostItem> costItemList = CostItem.findAllBySurveyOrgAndPkgInList(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant), surveyPackageResultList.pkg)
            costItemList.each { CostItem costItem ->

                sumCostInBillingCurrency = sumCostInBillingCurrency+ costItem.costInBillingCurrency
                sumCostInBillingCurrencyAfterTax = sumCostInBillingCurrencyAfterTax+ costItem.costInBillingCurrencyAfterTax
            }
        }

        [sumCostInBillingCurrency: sumCostInBillingCurrency, sumCostInBillingCurrencyAfterTax: sumCostInBillingCurrencyAfterTax]

    }

    Map getCostItemSumBySelectSurveySubscriptionsOfParticipant(SurveyConfig surveyConfig, Org participant){
        Double sumCostInBillingCurrency = 0.0
        Double sumCostInBillingCurrencyAfterTax = 0.0

        List<SurveySubscriptionResult> surveySubscriptionResultList = SurveySubscriptionResult.findAllBySurveyConfigAndParticipant(surveyConfig, participant)

        if(surveySubscriptionResultList){
            List<CostItem> costItemList = CostItem.findAllBySurveyOrgAndSurveyConfigSubscriptionInList(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant), SurveyConfigSubscription.findAllBySurveyConfig(surveyConfig))
            costItemList.each { CostItem costItem ->

                sumCostInBillingCurrency = sumCostInBillingCurrency+ costItem.costInBillingCurrency
                sumCostInBillingCurrencyAfterTax = sumCostInBillingCurrencyAfterTax+ costItem.costInBillingCurrencyAfterTax
            }
        }

        [sumCostInBillingCurrency: sumCostInBillingCurrency, sumCostInBillingCurrencyAfterTax: sumCostInBillingCurrencyAfterTax]

    }


    Map participantResultGenerics(Map result, Org participant, GrailsParameterMap params){
        result.participant = participant
        if (params.viewTab == 'overview') {
            if (result.surveyConfig.subscription) {
                result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)
                if (result.subscription) {
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
                        if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                            result.countCurrentPermanentTitles = countPerpetualAccessTitlesBySubAndNotInIEGroup(result.subscription, result.surveyConfig)
                        }
                        else {
                            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
                            result.countCurrentPermanentTitles = issueEntitlementGroup ? subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(result.subscription, issueEntitlementGroup) : 0
                        }
//                    if (result.surveyConfig.pickAndChoosePerpetualAccess) {
//                        result.countCurrentIEs = countPerpetualAccessTitlesBySub(result.subscription)
//                    } else {
//                        result.countCurrentIEs = (result.previousSubscription ? subscriptionService.countCurrentIssueEntitlements(result.previousSubscription) : 0) + subscriptionService.countCurrentIssueEntitlements(result.subscription)
//                    }

                        result.subscriber = participant
                    }
                }
            }
        }
        else if (params.viewTab == 'additionalInformation') {
            if (result.surveyConfig.subscription) {
                result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)
                // restrict visible for templates/links/orgLinksAsList
                result.visibleOrgRelations = []
                if (result.subscription) {
                    result.subscription.orgRelations.each { OrgRole or ->
                        if (!(or.org.id == contextService.getOrg().id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS])) {
                            result.visibleOrgRelations << or
                        }
                    }
                    result.visibleOrgRelations.sort { it.org.sortname }

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
                if (params.personId && params.setPreferredBillingPerson) {
                    if (params.setPreferredBillingPerson == 'true') {
                        Person person = Person.get(Long.valueOf(params.personId))
                        if (person && !SurveyPersonResult.findByParticipantAndSurveyConfigAndBillingPerson(participant, result.surveyConfig, true)) {
                            new SurveyPersonResult(participant: participant, surveyConfig: result.surveyConfig, person: person, billingPerson: true, owner: result.surveyInfo.owner).save()
                        }else {
                            result.error = messageSource.getMessage('person.preferredBillingPerson.fail', null, LocaleUtils.getCurrentLocale())
                        }
                    }
                    if (params.setPreferredBillingPerson == 'false') {
                        Person person = Person.get(Long.valueOf(params.personId))
                        SurveyPersonResult surveyPersonResult = SurveyPersonResult.findByParticipantAndSurveyConfigAndPersonAndBillingPerson(participant, result.surveyConfig, person, true)
                        if (person && surveyPersonResult) {
                            surveyPersonResult.delete()
                        }
                    }
                    params.remove('setPreferredBillingPerson')
                    params.remove('personId')
                }

                if (params.addressId && params.setAddress) {
                    if (params.setAddress == 'true') {
                        if(result.surveyOrg.address){
                            result.error = messageSource.getMessage('person.preferredBillingPerson.fail', null, LocaleUtils.getCurrentLocale())
                        }else {
                            result.surveyOrg.address = Address.get(Long.valueOf(params.addressId))
                        }

                    }
                    if (params.setAddress == 'false') {
                        result.surveyOrg.address = null
                    }
                    result.surveyOrg.save()
                    params.remove('setAddress')
                    params.remove('addressId')
                }

                if(params.setEInvoiceValuesFromOrg) {
                    result.surveyOrg.eInvoicePortal = participant.eInvoicePortal
                    result.surveyOrg.eInvoiceLeitwegId = participant.getLeitID()?.value
                    result.surveyOrg.peppolReceiverId = participant.getPeppolReceiverID()?.value
                    result.surveyOrg.eInvoiceLeitkriterium = participant.getLeitkriteriums() ? participant.getLeitkriteriums()[0].value : ''
                    result.surveyOrg.save()
                    params.remove('setEInvoiceValuesFromOrg')
                }

                if(params.setContactAndAddressFromOrg) {
                    setDefaultPreferredConcatsForSurvey(result.surveyConfig, participant)
                    params.remove('setContactAndAddressFromOrg')
                }

                if(params.setEInvoiceLeitkriteriumFromOrg) {
                    result.surveyOrg.eInvoiceLeitkriterium = params.setEInvoiceLeitkriteriumFromOrg
                    result.surveyOrg.save()
                    params.remove('setEInvoiceLeitkriteriumFromOrg')
                }

            }

            params.sort = params.sort ?: 'sortname'
            params.org = participant
            params.function = [RDStore.PRS_FUNC_INVOICING_CONTACT.id]
            result.visiblePersons = addressbookService.getVisiblePersons("contacts", params)

            params.type = RDStore.ADDRESS_TYPE_BILLING.id
            result.addresses = addressbookService.getVisibleAddresses("contacts", params)

        }
        else if (params.viewTab == 'surveyContacts') {
            if (result.editable) {
                    if (params.personId && params.setPreferredSurveyPerson) {
                        if (params.setPreferredSurveyPerson == 'true') {
                            Person person = Person.get(Long.valueOf(params.personId))
                            if(person && !SurveyPersonResult.findByParticipantAndSurveyConfigAndPersonAndSurveyPerson(participant, result.surveyConfig, person, true)){
                                new SurveyPersonResult(participant: participant, surveyConfig: result.surveyConfig, person: person, surveyPerson: true, owner: result.surveyInfo.owner).save()
                            }
                        }
                        if (params.setPreferredSurveyPerson == 'false') {
                            Person person = Person.get(Long.valueOf(params.personId))
                            SurveyPersonResult surveyPersonResult = SurveyPersonResult.findByParticipantAndSurveyConfigAndPersonAndSurveyPerson(participant, result.surveyConfig, person, true)
                            if(person && surveyPersonResult){
                                surveyPersonResult.delete()
                            }
                        }

                        params.remove('setPreferredSurveyPerson')
                        params.remove('personId')
                    }
            }

            params.sort = params.sort ?: 'sortname'
            params.org = participant
            params.function = [RDStore.PRS_FUNC_SURVEY_CONTACT.id]
            result.visiblePersons = addressbookService.getVisiblePersons("contacts", params)

        }
        else if (params.viewTab == 'stats') {
            result.subscription = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)

            if(result.subscription){
                if (params.error)
                    result.error = params.error
                if (params.reportType)
                    result.putAll(subscriptionControllerService.loadFilterList(params))

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
                    Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: platformInstance.gokbId])
                    if (queryResult.error && queryResult.error == 404) {
                        result.wekbServerUnavailable = message(code: 'wekb.error.404')
                    } else if (queryResult) {
                        List records = queryResult.result
                        if (records[0]) {
                            records[0].lastRun = platformInstance.counter5LastRun ?: platformInstance.counter4LastRun
                            records[0].id = platformInstance.id
                            result.platformInstanceRecords[platformInstance.gokbId] = records[0]
                            result.platformInstanceRecords[platformInstance.gokbId].wekbUrl = Wekb.getResourceShowURL() + "/${platformInstance.gokbId}"
                            if (records[0].statisticsFormat == 'COUNTER' && records[0].counterR4SushiServerUrl == null && records[0].counterR5SushiServerUrl == null) {
                                result.error = 'noSushiSource'
                                ArrayList<Object> errorArgs = ["${Wekb.getResourceShowURL()}/${platformInstance.gokbId}", platformInstance.name]
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
                            Set allAvailableReports = subscriptionControllerService.getAvailableReports(result, true, true)
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

            result.ddcs = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.DDC)

            params.subTab = params.subTab ?: 'allPackages'

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
                        //Fallback with fake UUID
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
        }else if (params.viewTab == 'subscriptionSurvey') {
            if (result.editable) {
                /*switch (params.actionsForSurveySubscriptions) {
                    case "addSurveySubscription":
                        Subscription surveySubscription = Subscription.findById(params.XXX)
                        if (SurveyConfigSubscription.findBySurveyConfigAndSubscription(result.surveyConfig, surveySubscription) && !SurveySubscriptionResult.findBySurveyConfigAndParticipantAndSubscription(result.surveyConfig, participant, surveySubscription)) {
                            SurveySubscriptionResult surveySubscriptionResult = new SurveySubscriptionResult(surveyConfig: result.surveyConfig, participant: participant, subscription: surveySubscription, owner: result.surveyInfo.owner)
                            surveySubscriptionResult.save()
                        }

                        break
                    case "removeSurveySubscription":
                        Subscription surveySubscription = Subscription.findById(params.XXX)
                        SurveySubscriptionResult surveySubscriptionResult = SurveySubscriptionResult.findBySurveyConfigAndParticipantSubscription(result.surveyConfig, participant, surveySubscription)
                        if (SurveyConfigSubscription.findBySurveyConfigAndSubscription(result.surveyConfig, surveySubscription) && surveySubscriptionResult) {
                            surveySubscriptionResult.delete()
                        }
                        break
                }*/

                if (params.selectedSubs || params.subListToggler) {

                    List selectedSubs = []
                    if (params.subListToggler == 'on') {
                        if (params.processOption == 'unlinkSubscriptions') {
                            List<Subscription> subscriptions = SurveySubscriptionResult.executeQuery("select scs.subscription from SurveySubscriptionResult scs where scs.surveyConfig = :surveyConfig and scs.participant = :participant", [surveyConfig: result.surveyConfig, participant: participant])
                            result.putAll(getMySubscriptions(params, result.user, result.surveyInfo.owner), subscriptions)
                        } else {
                            result.putAll(getMySubscriptions(params, result.user, result.surveyInfo.owner))
                        }

                        result.subscriptions.each {
                            selectedSubs << it.id
                        }
                    } else selectedSubs = params.list("selectedSubs")


                    if (selectedSubs) {
                        selectedSubs.each {
                            Subscription subscription = Subscription.get(it)

                            if (params.processOption == 'unlinkSubscriptions') {
                                SurveySubscriptionResult surveySubscriptionResult = SurveySubscriptionResult.findBySurveyConfigAndParticipantAndSubscription(result.surveyConfig, participant, subscription)
                                if (subscription && SurveyConfigSubscription.findBySurveyConfigAndSubscription(result.surveyConfig, subscription) && surveySubscriptionResult) {
                                    surveySubscriptionResult.delete()
                                }
                            }
                            if (params.processOption == 'linkSubscriptions') {
                                if (subscription && SurveyConfigSubscription.findBySurveyConfigAndSubscription(result.surveyConfig, subscription) && !SurveySubscriptionResult.findBySurveyConfigAndParticipantAndSubscription(result.surveyConfig, participant, subscription)) {
                                    SurveySubscriptionResult surveySubscriptionResult = new SurveySubscriptionResult(surveyConfig: result.surveyConfig, participant: participant, subscription: subscription, owner: result.surveyInfo.owner)
                                    surveySubscriptionResult.save()
                                }
                            }
                        }
                        params.remove("selectedSubs")
                    }
                }
            }

            params.subTab = params.subTab ?: 'allSubscriptions'

            List<Subscription> subscriptions


            if(params.subTab == 'allSubscriptions') {
                subscriptions = SurveyConfigSubscription.executeQuery("select scs.subscription from SurveyConfigSubscription scs where scs.surveyConfig = :surveyConfig", [surveyConfig: result.surveyConfig])
            }

            if(params.subTab == 'selectSubscriptions') {
                subscriptions = SurveySubscriptionResult.executeQuery("select scs.subscription from SurveySubscriptionResult scs where scs.surveyConfig = :surveyConfig and scs.participant = :participant", [surveyConfig: result.surveyConfig, participant: participant])
            }



            result.putAll(getMySubscriptions(params, result.user, result.surveyInfo.owner, subscriptions))

            result.subscriptions = result.subscriptions.drop((int) result.offset).take((int) result.max)
            if(result.subscriptions)
                result.allLinkedLicenses = Links.findAllByDestinationSubscriptionInListAndSourceLicenseIsNotNullAndLinkType(result.subscriptions, RDStore.LINKTYPE_LICENSE)

            if(result.surveySubscriptionsCount == 0){
                result.num_sub_rows = 0
                result.subscriptions = []
            }

        }
        else if (params.viewTab == 'vendorSurvey') {
            if (result.editable) {
                switch (params.actionsForSurveyVendors) {
                    case "addSurveyVendor":
                        Vendor vendor = Vendor.findById(params.vendorId)
                        if (SurveyConfigVendor.findBySurveyConfigAndVendor(result.surveyConfig, vendor) && !SurveyVendorResult.findBySurveyConfigAndParticipant(result.surveyConfig, participant)) {
                            SurveyVendorResult surveyVendorResult = new SurveyVendorResult(surveyConfig: result.surveyConfig, participant: participant, vendor: vendor, owner: result.surveyInfo.owner)
                            surveyVendorResult.save()
                        } else {
                        result.error = messageSource.getMessage('surveyVendors.selectedVendor.fail', null, LocaleUtils.getCurrentLocale())
                        }

                        break
                    case "removeSurveyVendor":
                        Vendor vendor = Vendor.findById(params.vendorId)
                        SurveyVendorResult surveyVendorResult = SurveyVendorResult.findBySurveyConfigAndParticipantAndVendor(result.surveyConfig, participant, vendor)
                        if (surveyVendorResult) {
                            surveyVendorResult.delete()
                        }
                        break
                }
            }

            result.propList    = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.VEN_PROP], contextService.getOrg())

            List configVendorIds = SurveyConfigVendor.executeQuery("select scv.vendor.id from SurveyConfigVendor scv where scv.surveyConfig = :surveyConfig ", [surveyConfig: result.surveyConfig])
            params.ids = configVendorIds
            params.max = configVendorIds.size()

            result.putAll(vendorService.getWekbVendors(params))

            if (params.isMyX) {
                List<String> xFilter = params.list('isMyX')
                Set<Long> f1Result = [], f2Result = []
                boolean   f1Set = false, f2Set = false
                result.currentVendorIdList = Vendor.executeQuery('select vr.vendor.id from VendorRole vr, OrgRole oo join oo.sub s where s = vr.subscription and oo.org = :context and s.status = :current', [current: RDStore.SUBSCRIPTION_CURRENT, context: contextService.getOrg()])
                if (xFilter.contains('ismyx_exclusive')) {
                    f1Result.addAll( result.vendorTotal.findAll { result.currentVendorIdList.contains( it.id ) }.collect{ it.id } )
                    f1Set = true
                }
                if (xFilter.contains('ismyx_not')) {
                    f1Result.addAll( result.vendorTotal.findAll { ! result.currentVendorIdList.contains( it.id ) }.collect{ it.id }  )
                    f1Set = true
                }
                if (xFilter.contains('wekb_exclusive')) {
                    f2Result.addAll( result.vendorTotal.findAll { it.gokbId != null && it.gokbId in result.wekbRecords.keySet() }.collect{ it.id } )
                    f2Set = true
                }
                if (xFilter.contains('wekb_not')) {
                    f2Result.addAll( result.vendorTotal.findAll { it.gokbId == null }.collect{ it.id }  )
                    f2Set = true
                }

                if (f1Set) { result.vendorTotal = result.vendorTotal.findAll { f1Result.contains(it.id) } }
                if (f2Set) { result.vendorTotal = result.vendorTotal.findAll { f2Result.contains(it.id) } }
            }

        }

        return result

    }

    void setDefaultPreferredConcatsForSurvey(SurveyConfig surveyConfig, Org org){
        SurveyOrg surOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, org)
        if(surOrg && surveyConfig.invoicingInformation) {
            Map parameterMap = [:]
            parameterMap.org = org

            parameterMap.sort = 'sortname'
            parameterMap.preferredBillingPerson = true
            parameterMap.function = [RDStore.PRS_FUNC_INVOICING_CONTACT.id]
            List<Person> visiblePersons = addressbookService.getVisiblePersons("contacts", parameterMap)

            visiblePersons.each { Person person ->
                if (person.preferredBillingPerson && !SurveyPersonResult.findByParticipantAndSurveyConfigAndPersonAndBillingPerson(org, surveyConfig, person, true)) {
                    new SurveyPersonResult(participant: org, surveyConfig: surveyConfig, person: person, billingPerson: true, owner: surveyConfig.surveyInfo.owner).save()
                }
            }


            parameterMap.remove('preferredBillingPerson')
            parameterMap.preferredSurveyPerson = true
            parameterMap.function = [RDStore.PRS_FUNC_SURVEY_CONTACT.id]
            visiblePersons = addressbookService.getVisiblePersons("contacts", parameterMap)
            visiblePersons.each { Person person ->
                if (person.preferredSurveyPerson && !SurveyPersonResult.findByParticipantAndSurveyConfigAndPersonAndSurveyPerson(org, surveyConfig, person, true)) {
                    new SurveyPersonResult(participant: org, surveyConfig: surveyConfig, person: person, surveyPerson: true, owner: surveyConfig.surveyInfo.owner).save()
                }
            }


            Map parameterMap2 = [:]
            parameterMap2.org = org
            parameterMap2.sort = 'sortname'
            parameterMap2.type = RDStore.ADDRESS_TYPE_BILLING.id
            List<Address> addresses = addressbookService.getVisibleAddresses("contacts", parameterMap2)

            addresses.each { Address address ->
                if (address.preferredForSurvey) {
                    surOrg.address = address
                    surOrg.save()
                }
            }


        }
    }

    Map<String, Object> financeEnrichment(MultipartFile tsvFile, String encoding, RefdataValue pickedElement, SurveyConfig surveyConfig, Package pkg = null, SurveyConfigSubscription surveyConfigSubscription = null) {
        Map<String, Object> result = [:]
        List<String> wrongIdentifiers = [] // wrongRecords: downloadable file
        Org contextOrg = contextService.getOrg()
        //List<String> rows = tsvFile.getInputStream().getText(encoding).split('\n')
        //rows.remove(0).split('\t') we should consider every row
        //needed because cost item updates are not flushed immediately
        Set<Long> updatedIDs = []
        //preparatory for an eventual variable match; for the moment: hard coded to 0 and 1
        int idCol = 0, valueCol = 1, noRecordCounter = 0, wrongIdentifierCounter = 0, missingCurrencyCounter = 0, totalRows = 0
        Set<IdentifierNamespace> namespaces = [IdentifierNamespace.findByNs('ISIL'), IdentifierNamespace.findByNs('wibid')]
        tsvFile.getInputStream().withReader(encoding) { reader ->
            char tab = '\t'
            ICSVParser csvp = new CSVParserBuilder().withSeparator(tab).build() // csvp.DEFAULT_SEPARATOR, csvp.DEFAULT_QUOTE_CHARACTER, csvp.DEFAULT_ESCAPE_CHARACTER
            CSVReader csvr = new CSVReaderBuilder( reader ).withCSVParser( csvp ).build()
            String[] line
            while (line = csvr.readNext()) {
                if (line[0]) {
                    //wrong separator
                    if (line.size() > 1) {
                        totalRows++
                        //rows.each { String row ->
                        //List<String> cols = row.split('\t')
                        String idStr = line[idCol].trim(), valueStr = line[valueCol].trim()
                        //try to match the survey
                        if (valueStr) {
                            //first: get the org
                            Org match = null
                            Set<Org> check = Org.executeQuery('select ci.customer from CustomerIdentifier ci where ci.value = :number', [number: idStr])
                            if (check.size() == 1)
                                match = check[0]
                            if (!match)
                                match = Org.findByGlobalUID(idStr)
                            if (!match) {
                                check = Org.executeQuery('select id.org from Identifier id where id.value = :value and id.ns in (:namespaces)', [value: idStr, namespaces: namespaces])
                                if (check.size() == 1)
                                    match = check[0]
                            }
                            //match success
                            if (match) {
                                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, match)
                                    if (surveyOrg) {
                                        CostItem ci
                                        if(pkg){
                                            ci = CostItem.findBySurveyOrgAndOwnerAndCostItemElementAndPkg(surveyOrg, contextOrg, pickedElement, pkg)
                                        }else if(subscription){
                                            ci = CostItem.findBySurveyOrgAndOwnerAndCostItemElementAndSurveyConfigSubscription(surveyOrg, contextOrg, pickedElement, surveyConfigSubscription)
                                        }
                                        else {
                                            ci = CostItem.findBySurveyOrgAndOwnerAndCostItemElement(surveyOrg, contextOrg, pickedElement)
                                        }

                                        if (ci) {
                                            //Regex to parse different sum entries
                                            //Pattern nonNumericRegex = Pattern.compile("([\$€£]|EUR|USD|GBP)")
                                            Pattern numericRegex = Pattern.compile("([\\d'.,]+)")
                                            //step 1: strip the non-numerical part and try to parse a currency
                                            //skipped as of comment of March 12th, '25
                                            //Matcher billingCurrencyMatch = nonNumericRegex.matcher(valueStr)
                                            //step 2: pass the numerical part to the value parser
                                            Matcher costMatch = numericRegex.matcher(valueStr)
                                            //if(costMatch.find() && billingCurrencyMatch.find()) {
                                            if (costMatch.find()) {
                                                String input = costMatch.group(1)//, currency = billingCurrencyMatch.group(1)
                                                BigDecimal parsedCost = escapeService.parseFinancialValue(input)
                                                ci.costInBillingCurrency = parsedCost
                                                /*
                                            switch(currency) {
                                                case ['€', 'EUR']: ci.billingCurrency = RDStore.CURRENCY_EUR
                                                    break
                                                case ['£', 'GBP']: ci.billingCurrency = RDStore.CURRENCY_GBP
                                                    break
                                                case ['$', 'USD']: ci.billingCurrency = RDStore.CURRENCY_USD
                                                    break
                                            }
                                            */
                                                if (ci.save()) {
                                                    updatedIDs << ci.id
                                                }
                                                else
                                                    log.error(ci.getErrors().getAllErrors().toListString())
                                            }
                                            /*
                                        else if(!billingCurrencyMatch.find())
                                            missingCurrencyCounter++
                                        */
                                        }
                                        else {
                                            noRecordCounter++
                                            wrongIdentifiers << idStr
                                        }
                                    }
                            }
                            else {
                                wrongIdentifierCounter++
                                wrongIdentifiers << idStr
                            }
                        }
                    }
                    else if(line.size() == 1) {
                        result.wrongSeparator = true
                        result.afterEnrichment = true
                        result
                    }
                }
            }
        }
        result.missingCurrencyCounter = missingCurrencyCounter
        result.wrongIdentifiers = wrongIdentifiers
        result.matchCounter = updatedIDs.size()
        result.wrongIdentifierCounter = wrongIdentifierCounter
        result.noRecordCounter = noRecordCounter
        result.totalRows = totalRows
        result.afterEnrichment = true
        result
    }

    boolean existsMultiYearTermBySurveyUseForTransfer(SurveyConfig surveyConfig, Org org) {
        boolean existsMultiYearTerm = false
        Subscription sub = surveyConfig.subscription

        if (surveyConfig.subSurveyUseForTransfer && sub) {
            Subscription subMuliYear = Subscription.executeQuery("select sub" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.org = :org and orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub" +
                    " and sub.isMultiYear = true and sub.endDate != null and (EXTRACT (DAY FROM (sub.endDate - NOW())) > 366)",
                    [org          : org,
                     roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: sub])[0]

            if (subMuliYear) {
                return true
            }
        }

        return existsMultiYearTerm
    }

    /**
     * Checks if this subscription is a multi-year subscription and if we are in the time range spanned by the parent subscription
     * @return true if we are within the given multi-year range, false otherwise
     */


    boolean existsCurrentMultiYearTermBySurveyUseForTransfer(SurveyConfig surveyConfig, Org org) {
        boolean existsMultiYearTerm = false
        Subscription sub = surveyConfig.subscription

        if (surveyConfig.subSurveyUseForTransfer && sub) {
            Subscription subMuliYear = Subscription.executeQuery("select sub" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.org = :org and orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub" +
                    " and sub.isMultiYear = true and sub.endDate != null and (EXTRACT (DAY FROM (sub.endDate - sub.instanceOf.startDate)) > 366)",
                    [org          : org,
                     roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: sub])[0]

            if (subMuliYear) {
                return true
            }
        }

        return existsMultiYearTerm
    }

    boolean selectableDespiteMultiYearTerm(SurveyConfig surveyConfig, Org org) {
        boolean selectable = true
        Subscription sub = surveyConfig.subscription

        if (surveyConfig.subSurveyUseForTransfer && sub) {
            Subscription subMuliYear = Subscription.executeQuery("select sub" +
                    " from Subscription sub " +
                    " join sub.orgRelations orgR " +
                    " where orgR.org = :org and orgR.roleType in :roleTypes " +
                    " and sub.instanceOf = :instanceOfSub" +
                    " and sub.isMultiYear = true and sub.endDate != null and (EXTRACT (DAY FROM (sub.endDate - sub.instanceOf.startDate)) > 549)",
                    [org          : org,
                     roleTypes    : [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS],
                     instanceOfSub: sub])[0]

            if (subMuliYear) {
                selectable = false
            }
        }

        return selectable
    }

    Map<String,Object> getMySubscriptions(GrailsParameterMap params, User contextUser, Org contextOrg, List<Subscription> filteredSubs = null) {
        Map<String,Object> result = [:]
        EhcacheWrapper cache = contextService.getUserCache("/subscriptions/filter/")
        if(cache && cache.get('subscriptionFilterCache')) {
            if(!params.resetFilter && !params.isSiteReloaded)
                params.putAll((GrailsParameterMap) cache.get('subscriptionFilterCache'))
            else params.remove('resetFilter')
            cache.remove('subscriptionFilterCache') //has to be executed in any case in order to enable cache updating
        }
        SwissKnife.setPaginationParams(result, params, contextUser)


    /*    result.availableConsortia = Combo.executeQuery(
                'select c.toOrg from Combo as c where c.fromOrg = :fromOrg and c.type = :type',
                [fromOrg: contextOrg, type: RDStore.COMBO_TYPE_CONSORTIUM]
        )*/

/*        List<Role> consRoles = Role.findAll { authority in ['ORG_CONSORTIUM_BASIC', 'ORG_CONSORTIUM_PRO'] }

        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSetting os_ct where 
                        os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and os_ct.roleValue in (:roles) 
                        order by lower(o.name)""",
                [roles: consRoles]
        )*/

       /* def viableOrgs = []

        if ( result.availableConsortia ){
            result.availableConsortia.each {
                viableOrgs.add(it)
            }
        }

        viableOrgs.add(contextOrg)*/

        String consortiaFilter = ''
        if(contextOrg.isCustomerType_Consortium() || contextOrg.isCustomerType_Support())
            consortiaFilter = 'and s.instanceOf = null'

        Set<Year> availableReferenceYears = Subscription.executeQuery('select s.referenceYear from OrgRole oo join oo.sub s where s.referenceYear != null and oo.org = :contextOrg '+consortiaFilter+' order by s.referenceYear desc', [contextOrg: contextOrg])
        result.referenceYears = availableReferenceYears

        def date_restriction = null
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        if (! params.status) {
            if (params.isSiteReloaded != "yes") {
                String[] defaultStatus = [RDStore.SUBSCRIPTION_CURRENT.id]
                params.status = defaultStatus
                //params.hasPerpetualAccess = RDStore.YN_YES.id.toString() as you wish, myladies ... as of May 16th, '22, the setting should be reverted
                result.defaultSet = true
            }
            else {
                params.status = 'FETCH_ALL'
            }
        }
        if(params.isSiteReloaded == "yes") {
            params.remove('isSiteReloaded')
            cache.put('subscriptionFilterCache', params)
        }

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, '', contextOrg)
        result.filterSet = tmpQ[2]
        List<Subscription> subscriptions

        if(filteredSubs){
            tmpQ[0] = tmpQ[0].replace("order by", "and s in (:subList) order by ")
            Map query
            tmpQ[1] << [subList: filteredSubs]
        }

        subscriptions = Subscription.executeQuery( "select s " + tmpQ[0], tmpQ[1] )
        //impossible to sort in nothing ...
        if(params.sort == "provider") {
            subscriptions.sort { Subscription s1, Subscription s2 ->
                String sortname1 = s1.getSortedProviders(params.order)[0]?.sortname?.toLowerCase(), sortname2 = s2.getSortedProviders(params.order)[0]?.sortname?.toLowerCase()
                int cmp
                if(params.order == "asc") {
                    if(!sortname1) {
                        if(!sortname2)
                            cmp = 0
                        else cmp = 1
                    }
                    else {
                        if(!sortname2)
                            cmp = -1
                        else cmp = sortname1 <=> sortname2
                    }
                }
                else cmp = sortname2 <=> sortname1
                if(!cmp)
                    cmp = params.order == 'asc' ? s1.name <=> s2.name : s2.name <=> s1.name
                if(!cmp)
                    cmp = params.order == 'asc' ? s1.startDate <=> s2.startDate : s2.startDate <=> s1.startDate
                cmp
            }
        }
        else if(params.sort == "vendor") {
            subscriptions.sort { Subscription s1, Subscription s2 ->
                String sortname1 = s1.getSortedVendors(params.order)[0]?.sortname?.toLowerCase(), sortname2 = s2.getSortedVendors(params.order)[0]?.sortname?.toLowerCase()
                int cmp
                if(params.order == "asc") {
                    if(!sortname1) {
                        if(!sortname2)
                            cmp = 0
                        else cmp = 1
                    }
                    else {
                        if(!sortname2)
                            cmp = -1
                        else cmp = sortname1 <=> sortname2
                    }
                }
                else cmp = sortname2 <=> sortname1
                if(!cmp) {
                    cmp = params.order == 'asc' ? s1.name <=> s2.name : s2.name <=> s1.name
                }
                if(!cmp) {
                    cmp = params.order == 'asc' ? s1.startDate <=> s2.startDate : s2.startDate <=> s1.startDate
                }
                cmp
            }
        }
        result.num_sub_rows = subscriptions.size()

        result.date_restriction = date_restriction

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextOrg)

        result.subscriptions = subscriptions

        result
    }

}
