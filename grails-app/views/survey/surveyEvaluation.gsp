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
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:statusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="${actionName}"/>

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_TITLE_SELECTION]}">
    <ui:linkWithIcon icon="bordered inverted orange clipboard la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.isTypeSubscriptionOrIssueEntitlement()}">
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
        <span class="ui floating blue circular label">${participantsFinishTotal}</span>
    </g:link>

    <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
        ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
        <span class="ui floating blue circular label">${participantsNotFinishTotal}</span>
    </g:link>

    <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
            controller="survey" action="surveyEvaluation"
            params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsView']">
        ${message(code: 'surveyEvaluation.participantsView')}
        <span class="ui floating blue circular label">${participantsTotal}</span>
    </g:link>

</div>
<div class="ui bottom attached tab segment active">
    <laser:render template="evaluationParticipantsView" model="[showCheckbox: false,
                                                        tmplConfigShow   : ['lineNumber', 'name', (surveyConfig.pickAndChoose ? 'finishedDate' : ''), (surveyConfig.pickAndChoose ? 'surveyTitlesCount' : ''), (surveyConfig.pickAndChoose ? 'uploadTitleListDoc' : ''), 'surveyProperties', 'commentOnlyForOwner', (surveyConfig.pickAndChoose ? 'downloadTitleList' : '')]]"/>
</div>


</g:else>

<laser:htmlEnd />
