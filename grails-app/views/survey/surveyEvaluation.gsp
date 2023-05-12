<%@ page import="de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyResult.label')})" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyResult.label" class="active"/>--}%
</ui:breadcrumbs>

<ui:controlButtons>
    <g:if test="${surveyInfo.status != RDStore.SURVEY_IN_PROCESSING}">
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
            </ui:exportDropdownItem>

            %{--<ui:exportDropdownItem>
                <g:link class="item" action="surveyEvaluation" id="${surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, exportXLSX: true]">${message(code: 'survey.exportSurvey')}</g:link>
            </ui:exportDropdownItem>

            <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
            <ui:exportDropdownItem>
                <g:link class="item" action="surveyEvaluation" id="${surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, exportXLSX: true, surveyCostItems: true]">${message(code: 'survey.exportSurveyCostItems')}</g:link>
            </ui:exportDropdownItem>
            </g:if>--}%
        </ui:exportDropdown>
    </g:if>

    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon type="Survey">
<ui:xEditable owner="${surveyInfo}" field="name"/>
</ui:h1HeaderWithIcon>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyEvaluation"/>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyResult.label')}
</h2>

<g:if test="${surveyInfo.status == RDStore.SURVEY_IN_PROCESSING}">
    <div class="ui segment">
        <strong>${message(code: 'surveyEvaluation.notOpenSurvey')}</strong>
    </div>
</g:if>
<g:else>
<div class="ui top attached stackable tabular la-tab-with-js menu">

    <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
        <div class="ui floating blue circular label">${participantsFinishTotal}</div>
    </g:link>

    <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
        <div class="ui floating blue circular label">${participantsNotFinishTotal}</div>
    </g:link>

    <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsView']">
        ${message(code: 'surveyEvaluation.participantsView')}
        <div class="ui floating blue circular label">${participantsTotal}</div>
    </g:link>

</div>
<div class="ui bottom attached tab segment active">
    <laser:render template="evaluationParticipantsView" model="[showCheckbox: false,
                                                        tmplConfigShow   : ['lineNumber', 'name', (surveyConfig.pickAndChoose ? 'finishedDate' : ''), (surveyConfig.pickAndChoose ? 'surveyTitlesCount' : ''), 'surveyProperties', 'commentOnlyForOwner']]"/>
</div>
<laser:render template="export/individuallyExportModal" model="[modalID: 'individuallyExportModal', contactSwitch: true]" />

</g:else>

<laser:htmlEnd />
