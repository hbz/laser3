<%@ page import="de.laser.SurveyConfig; de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.helper.RDStore" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')} (${message(code: 'surveyResult.label')})</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code:'menu.my.surveys')}" />
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
    <semui:crumb message="surveyResult.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:if test="${surveyInfo.status != RDStore.SURVEY_IN_PROCESSING}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <a class="item" data-semui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
            </semui:exportDropdownItem>

            %{--<semui:exportDropdownItem>
                <g:link class="item" action="surveyEvaluation" id="${surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, exportXLSX: true]">${message(code: 'survey.exportSurvey')}</g:link>
            </semui:exportDropdownItem>

            <g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id]}">
            <semui:exportDropdownItem>
                <g:link class="item" action="surveyEvaluation" id="${surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, exportXLSX: true, surveyCostItems: true]">${message(code: 'survey.exportSurveyCostItems')}</g:link>
            </semui:exportDropdownItem>
            </g:if>--}%
        </semui:exportDropdown>
    </g:if>

    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
</h1>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyEvaluation"/>




<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
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
        <div class="ui floating circular label">${participantsFinishTotal}</div>
    </g:link>

    <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
        <div class="ui floating circular label">${participantsNotFinishTotal}</div>
    </g:link>

    <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsView']">
        ${message(code: 'surveyEvaluation.participantsView')}
        <div class="ui floating circular label">${participantsTotal}</div>
    </g:link>

</div>
<div class="ui bottom attached tab segment active">
    <g:render template="evaluationParticipantsView" model="[showCheckbox: false,
                                                        tmplConfigShow   : ['lineNumber', 'name', (surveyConfig.pickAndChoose ? 'finishedDate' : ''), (surveyConfig.pickAndChoose ? 'surveyTitlesCount' : ''), 'surveyProperties', 'commentOnlyForOwner']]"/>
</div>
<g:render template="export/individuallyExportModal" model="[modalID: 'individuallyExportModal']" />

</g:else>

</body>
</html>
