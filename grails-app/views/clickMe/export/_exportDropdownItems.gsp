<%@ page import="de.laser.ExportClickMeService; de.laser.ClickMeConfig; de.laser.storage.RDStore;" %>
<laser:serviceInjection/>
<g:if test="${controllerName == 'survey'}">
    <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                            params="[exportController: 'survey', exportAction: 'surveyEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_EVALUATION, id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                            text="Export"/>

    <g:set var="clickMeConfigs"
           value="${ClickMeConfig.findAllByContextOrgAndClickMeType(contextService.getOrg(), ExportClickMeService.SURVEY_EVALUATION, [sort: 'configOrder'])}"/>
    <g:each in="${clickMeConfigs}" var="clickMeConfig">
        <ui:actionsDropdownItem tooltip="${clickMeConfig.note}" class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: ExportClickMeService.SURVEY_EVALUATION, id: params.id, clickMeConfigId: clickMeConfig.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                text="Export: ${clickMeConfig.name}"/>
    </g:each>

    <g:if test="${clickMeConfigs}">
        <div class="ui divider"></div>
    </g:if>

    <g:if test="${surveyInfo.type.id != RDStore.SURVEY_TYPE_TITLE_SELECTION.id}">
        <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: 'survey', exportAction: 'exportSurCostItems', exportParams: params, clickMeType: ExportClickMeService.SURVEY_COST_ITEMS, id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                message="survey.exportSurveyCostItems"/>


        <g:set var="clickMeConfigsCostItems"
               value="${ClickMeConfig.findAllByContextOrgAndClickMeType(contextService.getOrg(), ExportClickMeService.SURVEY_COST_ITEMS, [sort: 'configOrder'])}"/>

        <g:each in="${clickMeConfigsCostItems}" var="clickMeConfig">
            <ui:actionsDropdownItem tooltip="${clickMeConfig.note}" class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                    params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: ExportClickMeService.SURVEY_COST_ITEMS, id: params.id, clickMeConfigId: clickMeConfig.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                    text="${message(code: 'survey.exportSurveyCostItems')}: ${clickMeConfig.name}"/>
        </g:each>

        <g:if test="${clickMeConfigsCostItems}">
            <div class="ui divider"></div>
        </g:if>

    </g:if>

    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION, id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                message="renewalEvaluation.exportRenewal"/>

        <g:set var="clickMeConfigsRenewal"
               value="${ClickMeConfig.findAllByContextOrgAndClickMeType(contextService.getOrg(), ExportClickMeService.SURVEY_RENEWAL_EVALUATION, [sort: 'configOrder'])}"/>
        <g:each in="${clickMeConfigsRenewal}" var="clickMeConfig">
            <ui:actionsDropdownItem tooltip="${clickMeConfig.note}" class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                    params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: ExportClickMeService.SURVEY_RENEWAL_EVALUATION, id: params.id, clickMeConfigId: clickMeConfig.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                    text="${message(code: 'renewalEvaluation.exportRenewal')}: ${clickMeConfig.name}"/>
        </g:each>

    </g:if>
</g:if>
<g:else>

    <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                            params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: clickMeType, id: params.id, exportFileName: exportFileName]"
                            text="Export"/>

    <g:set var="clickMeConfigs" value="${ClickMeConfig.findAllByContextOrgAndClickMeType(contextService.getOrg(), clickMeType, [sort: 'configOrder'])}"/>
    <g:if test="${clickMeConfigs}">
        <div class="ui divider"></div>
    </g:if>
    <g:each in="${clickMeConfigs}" var="clickMeConfig">
        <ui:actionsDropdownItem tooltip="${clickMeConfig.note}" class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: clickMeType, id: params.id, clickMeConfigId: clickMeConfig.id, exportFileName: exportFileName]"
                                text="Export: ${clickMeConfig.name}"/>
    </g:each>
    <g:if test="${clickMeConfigs}">
        <div class="ui divider"></div>
    </g:if>

</g:else>