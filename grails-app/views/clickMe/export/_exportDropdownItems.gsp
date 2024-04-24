<%@ page import="de.laser.ClickMeConfig; de.laser.storage.RDStore;" %>

<g:if test="${controllerName == 'survey'}">
    <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                            params="[exportController: 'survey', exportAction: 'surveyEvaluation', exportParams: params, clickMeType: 'surveyEvaluation', id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                            text="Export"/>

    <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
        <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: 'survey', exportAction: 'exportSurCostItems', exportParams: params, clickMeType: 'surveyCostItems', id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                message="survey.exportSurveyCostItems"/>

    </g:if>

    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: 'survey', exportAction: 'renewalEvaluation', exportParams: params, clickMeType: 'surveyRenewalEvaluation', id: params.id, surveyConfigID: surveyConfig.id, exportFileName: exportFileName]"
                                message="renewalEvaluation.exportRenewal"/>
    </g:if>
</g:if>
<g:else>

    <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                            params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: clickMeType, id: params.id, exportFileName: exportFileName]"
                            text="Export"/>

    <g:set var="clickMeConfigs" value="${ClickMeConfig.findAllByContextOrgAndClickMeType(contextOrg, clickMeType, [sort: 'configOrder'])}"/>
    <g:if test="${clickMeConfigs}">
        <div class="ui divider"></div>
    </g:if>
    <g:each in="${clickMeConfigs}" var="clickMeConfig">
        <ui:actionsDropdownItem class="triggerClickMeExport" controller="clickMe" action="exportClickMeModal"
                                params="[exportController: controllerName, exportAction: actionName, exportParams: params, clickMeType: clickMeType, id: params.id, clickMeConfigId: clickMeConfig.id, exportFileName: exportFileName]"
                                text="Export: ${clickMeConfig.name}"/>
    </g:each>

</g:else>