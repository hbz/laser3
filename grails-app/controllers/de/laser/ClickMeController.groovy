package de.laser

import de.laser.annotations.DebugInfo
import de.laser.storage.BeanStore
import de.laser.survey.SurveyConfig
import de.laser.utils.DateUtils
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured


@Secured(['IS_AUTHENTICATED_FULLY'])
class ClickMeController {

    ExportClickMeService exportClickMeService
    ContextService contextService
    EscapeService escapeService

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
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
        result.exportParams = [:]
        params.each { String k, v ->
            if(!(k in ['controller', 'action', 'exportParams']))
                result.exportParams[k] = v
        }

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
        result.enableClickMeConfigSave = BeanStore.getContextService().isInstEditor(CustomerTypeService.PERMS_PRO)
        result.multiMap = false

        result.editExportConfig = params.editExportConfig

        if(params.clickMeConfigId)
        {
            result.clickMeConfig = ClickMeConfig.get(params.clickMeConfigId)
            result.modalText = result.clickMeConfig ? "Export: $result.clickMeConfig.name" : result.modalText
            result.showClickMeConfigSave = result.clickMeConfig ? (result.editExportConfig ? true : false) : true
        }


        switch (params.clickMeType) {
            case ExportClickMeService.ADDRESSBOOK:
                Map<String, Object> fields = exportClickMeService.getExportAddressFieldsForUI()
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
            case ExportClickMeService.CONSORTIAS:
                result.formFields = exportClickMeService.getExportOrgFieldsForUI('consortium')
                result.contactSwitch = true
                result.accessPointNotice = true
                result.exportFileName = result.exportFileName ?: message(code: 'consortium.plural.label')
                break
            case ExportClickMeService.CONSORTIA_PARTICIPATIONS:
                result.formFields = exportClickMeService.getExportConsortiaParticipationFieldsForUI()
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'consortium.member.plural')
                break
            case ExportClickMeService.COST_ITEMS:
                result.subscription = Subscription.get(params.sub)
                result.formFields = exportClickMeService.getExportCostItemFieldsForUI(result.subscription)
                result.exportController = 'finance'
                result.exportAction = 'financialsExport'
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: (result.subscription ? (escapeService.escapeString(result.subscription.name) + "_" + message(code:'subscription.details.financials.label')) : message(code:'subscription.details.financials.label'))
                result.overrideFormat = [xlsx: 'XLSX']
                result.multiMap = true
                break
            case ExportClickMeService.INSTITUTIONS:
                result.formFields = exportClickMeService.getExportOrgFieldsForUI('institution')
                result.contactSwitch = true
                result.accessPointNotice = true
                result.exportFileName = result.exportFileName ?: message(code: 'subscription.details.consortiaMembers.label')
                break
            case ExportClickMeService.ISSUE_ENTITLEMENTS:
                result.subscription = Subscription.get(params.id)
                result.formFields = exportClickMeService.getExportIssueEntitlementFieldsForUI()
                result.exportController = 'subscription'
                result.exportAction = 'exportHolding'
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.subscription.name) + "_" + message(code:'default.ie')
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                break
            case ExportClickMeService.LICENSES:
                result.formFields = exportClickMeService.getExportLicenseFieldsForUI()
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'license.plural')
                break
            case ExportClickMeService.PROVIDERS:
                result.formFields = exportClickMeService.getExportProviderFieldsForUI()
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'default.provider.export.label')
                break
            case ExportClickMeService.SUBSCRIPTIONS:
                result.formFields = exportClickMeService.getExportSubscriptionFieldsForUI(false)
                result.exportFileName = result.exportFileName ?: message(code: 'subscription.plural')
                break
            case ExportClickMeService.SUBSCRIPTIONS_MEMBERS:
                result.subscription = Subscription.get(params.id)
                result.formFields = exportClickMeService.getExportSubscriptionMembersFieldsForUI(result.subscription)
                result.csvFieldSeparator = '\t'
                result.exportController = 'subscription'
                result.exportAction = 'members'
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.subscription.name) + "_" + message(code:'subscriptionDetails.members.members')
                break
            case ExportClickMeService.SUBSCRIPTIONS_TRANSFER:
                result.formFields = exportClickMeService.getExportSubscriptionFieldsForUI(true)
                result.exportFileName = result.exportFileName ?: message(code: 'export.my.currentSubscriptionsTransfer')
                break
            case ExportClickMeService.SURVEY_EVALUATION:
                result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
                result.formFields = exportClickMeService.getExportSurveyEvaluationFieldsForUI(result.surveyConfig)
                result.contactSwitch = true
                if(params.chartFilter){
                    result.modalText = result.modalText + " (${params.chartFilter})"
                }
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.surveyConfig.getSurveyName()) + "_" + message(code:'surveyResult.label')
                break
            case ExportClickMeService.SURVEY_RENEWAL_EVALUATION:
                result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
                result.formFields = exportClickMeService.getExportRenewalFieldsForUI(result.surveyConfig)
                result.contactSwitch = true
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.surveyConfig.getSurveyName()) + "_" + message(code:'renewalexport.renewals')
                result.exportExcelButtonName = message(code: 'renewalEvaluation.exportExcelRenewal')
                result.exportCSVButtonName = message(code: 'renewalEvaluation.exportCSVRenewal')
                break
            case ExportClickMeService.SURVEY_COST_ITEMS:
                result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
                result.formFields = exportClickMeService.getExportSurveyCostItemFieldsForUI()
                result.contactSwitch = true
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(result.surveyConfig.getSurveyName()) + "_" + message(code: 'financials.costItem')
                break
            case ExportClickMeService.TIPPS:
                if(params.exportAction == 'addEntitlements')
                    result.exportAction = 'exportPossibleEntitlements'
                else if(params.exportAction == 'currentPermanentTitles')
                    result.exportAction = 'exportPermanentTitles'
                result.formFields = exportClickMeService.getExportTippFieldsForUI()
                result.exportFileName = result.exportFileName ?: escapeService.escapeString(message(code:'menu.my.permanentTitles'))
                result.overrideFormat = [xlsx: 'XLSX', csv: 'CSV']
                break
            case ExportClickMeService.VENDORS:
                result.formFields = exportClickMeService.getExportVendorFieldsForUI()
                result.contactSwitch = true
                result.exportFileName = result.exportFileName ?: message(code: 'vendor.plural')
                break
        }

        if(result.clickMeConfig && result.formFields) {
            result.formFields = exportClickMeService.getClickMeFields(result.clickMeConfig, result.formFields)
        }

        if(result.editExportConfig){
            result.exportController = 'clickMe'
            result.exportAction = 'editExportConfig'
        }

        render(template: templateName, model: result)
    }

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    Map<String,Object> editExportConfig() {
        Map<String, Object> result = [:]

        if(params.clickMeConfigId)
        {
            result.clickMeConfig = ClickMeConfig.get(params.clickMeConfigId)
            result.modalText = result.clickMeConfig ? "Export: $result.clickMeConfig.name" : result.modalText
            result.showClickMeConfigSave = result.clickMeConfig ? (result.editExportConfig ? true : false) : true
        }

        if(result.clickMeConfig && params.saveClickMeConfig){
            Map<String, Object> selectedFields = [:]

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            selectedFieldsRaw.each { it ->
                if(it.value == 'on')
                selectedFields.put( it.key.replaceFirst('iex:', ''), it.value )
            }
            String jsonConfig = (new JSON(selectedFields)).toString()
            result.clickMeConfig.jsonConfig = jsonConfig
            result.clickMeConfig.name = params.clickMeConfigName
            result.clickMeConfig.note = params.clickMeConfigNote
            result.clickMeConfig.save()
        }

        redirect(url: request.getHeader('referer'))
    }


}
