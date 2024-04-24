package de.laser

import de.laser.storage.BeanStore
import de.laser.survey.SurveyConfig
import de.laser.utils.DateUtils
import grails.plugin.springsecurity.annotation.Secured


@Secured(['IS_AUTHENTICATED_FULLY'])
class ClickMeController {

    ExportClickMeService exportClickMeService
    ContextService contextService
    EscapeService escapeService

    @Secured(['ROLE_USER'])
    Map<String,Object> exportClickMeModal() {
        Map<String,Object> result = [:]

        String templateName = "export/generallyModal"
        String modalID = "exportClickMeModal"

        result.modalID = modalID
        result.modalText = params.modalText ?: "Export"
        result.modalSize = "large"

        result.clickMeConfig = null
        result.exportController = params.exportController
        result.exportAction = params.exportAction
        result.exportParams = params.exportParams
        result.clickMeType = params.clickMeType

        result.formFields = [:]
        result.filterFields = [:]
        result.exportFileName = params.exportFileName ?: ''
        result.contactSwitch = false
        result.csvFieldSeparator = '|'
        result.orgSwitch = false
        result.accessPointNotice = false
        result.currentTabNotice = false
        result.overrideFormat = null
        result.showClickMeConfigSave = true
        result.enableClickMeConfigSave = BeanStore.getContextService().isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)
        result.tab = null
        result.multiMap = false

        if(params.clickMeConfigId)
        {
            result.clickMeConfig = ClickMeConfig.get(params.clickMeConfigId)
            result.modalText = result.clickMeConfig ? "Export: $result.clickMeConfig.name" : result.modalText
            result.showClickMeConfigSave = result.clickMeConfig ? false : true
        }

        switch (params.clickMeType) {
            case "addressbook":
                Map<String, Object> fields = exportClickMeService.getExportAddressFieldsForUI(result.clickMeConfig)
                Map<String, Object> formFields = fields.exportFields as Map, filterFields = fields.filterFields as Map
                Map<String, Object> urlParams = params.clone()
                urlParams.remove('tab')
                result.formFields = formFields
                result.exportController = 'myInstitution'
                result.exportAction = 'addressbook'
                result.exportParams = urlParams
                result.exportFileName = result.exportFileName ?: escapeService.escapeString("${message(code: 'menu.institutions.myAddressbook')}_${DateUtils.getSDF_yyyyMMdd().format(new Date())}")
                result.orgSwitch = true
                result.currentTabNotice = true
                break
            case "consortias":
                result.formFields = exportClickMeService.getExportOrgFieldsForUI('consortium', result.clickMeConfig)
                result.contactSwitch = true
                result.accessPointNotice = true
                result.exportFileName = result.exportFileName ?: message(code: 'consortium.plural.label')
                break
            case "consortiaParticipations":
                result.formFields = exportClickMeService.getExportConsortiaParticipationFieldsForUI(result.clickMeConfig)
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'consortium.member.plural')
                break
            case "ies":
                result.subscription = Subscription.get(params.id)
                result.formFields = exportClickMeService.getExportIssueEntitlementFieldsForUI(result.clickMeConfig)
                result.exportController = 'subscription'
                result.exportAction = 'index'
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.subscription.name) + "_" + message(code:'default.ie')
                break
            case "financialsExport":
                result.subscription = Subscription.get(params.id)
                result.formFields = exportClickMeService.getExportCostItemFieldsForUI(result.subscription, result.clickMeConfig)
                result.exportController = 'finance'
                result.exportAction = 'financialsExport'
                result.exportParams = result.exportParams+[id:params.id]
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: (result.subscription ? (escapeService.escapeString(subscription.name) + "_" + message(code:'subscription.details.financials.label')) : message(code:'subscription.details.financials.label'))
                result.overrideFormat = [xlsx: 'XLSX']
                result.multiMap = true
                break
            case "institutions":
                result.formFields = exportClickMeService.getExportOrgFieldsForUI('institution', result.clickMeConfig)
                result.contactSwitch = true
                result.accessPointNotice = true
                result.exportFileName = result.exportFileName ?: message(code: 'subscription.details.consortiaMembers.label')
                break
            case "lics":
                result.formFields = exportClickMeService.getExportLicenseFieldsForUI(result.clickMeConfig)
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'license.plural')
                break
            case "providers":
                result.formFields = exportClickMeService.getExportOrgFieldsForUI('provider', result.clickMeConfig)
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'default.ProviderAgency.export.label')
                break
            case "subs":
                result.formFields = exportClickMeService.getExportSubscriptionFieldsForUI(false, result.clickMeConfig)
                result.exportFileName = result.exportFileName ?: message(code: 'subscription.plural')
                break
            case "subMembers":
                result.subscription = Subscription.get(params.id)
                result.formFields = exportClickMeService.getExportSubscriptionMembersFieldsForUI(result.subscription, result.clickMeConfig)
                result.exportController = 'subscription'
                result.exportAction = 'members'
                result.exportParams = result.exportParams+[id:params.id]
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.subscription.name) + "_" + message(code:'subscriptionDetails.members.members')
                break
            case "subsTransfer":
                result.formFields = exportClickMeService.getExportSubscriptionFieldsForUI(true, result.clickMeConfig)
                result.exportFileName = result.exportFileName ?: message(code: 'export.my.currentSubscriptionsTransfer')
                break
            case "surveyEvaluation":
                result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
                result.formFields = exportClickMeService.getExportSurveyEvaluationFieldsForUI(result.surveyConfig, result.clickMeConfig)
                result.contactSwitch = true
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.surveyConfig.getSurveyName()) + "_" + message(code:'surveyResult.label')
                break
            case "surveyRenewalEvaluation":
                result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
                result.formFields = exportClickMeService.getExportRenewalFieldsForUI(result.surveyConfig, result.clickMeConfig)
                result.contactSwitch = true
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code:'renewalexport.renewals')
                result.exportExcelButtonName = message(code: 'renewalEvaluation.exportExcelRenewal')
                result.exportCSVButtonName = message(code: 'renewalEvaluation.exportCSVRenewal')
                break
            case "surveyCostItems":
                result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
                result.formFields = exportClickMeService.getExportSurveyCostItemFieldsForUI(result.clickMeConfig)
                result.contactSwitch = true
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(surveyConfig.getSurveyName()) + "_" + message(code: 'financials.costItem')
                break
            case "tipps":
                result.formFields = exportClickMeService.getExportTippFieldsForUI(result.clickMeConfig)
                result.exportFileName = result.exportFileName ?: message(code:'default.title.label')
                break
            case "vendors":
                result.formFields = exportClickMeService.getExportVendorFieldsForUI(result.clickMeConfig)
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'vendor.plural')
                break
        }

        render(template: templateName, model: result)
    }
}
