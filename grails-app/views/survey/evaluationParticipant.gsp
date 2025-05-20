<%@ page import="de.laser.survey.SurveyOrg; de.laser.survey.SurveyPersonResult; de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org" %>

<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyResult.label')}-${message(code: 'surveyParticipants.label')})" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyResult.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    %{--<ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="survey" action="generatePdfForParticipant"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]">PDF-Export
            </g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>--}%
    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_SURVEY_COMPLETED.id, RDStore.SURVEY_IN_EVALUATION.id]}">
        <ui:actionsDropdown>
            <g:if test="${surveyInfo.status.id == RDStore.SURVEY_SURVEY_STARTED.id && surveyConfig.isResultsSetFinishByOrg(participant)}">
                <ui:actionsDropdownItem controller="survey" action="actionsForParticipant"
                                               params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, actionForParticipant: 'openSurveyAgainForParticipant']"
                                               message="openSurveyAgainForParticipant.button"/>

            </g:if>
            <g:if test="${!surveyConfig.isResultsSetFinishByOrg(participant)}">
                <ui:actionsDropdownItem controller="survey" action="actionsForParticipant"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id, actionForParticipant: 'finishSurveyForParticipant']"
                                               message="finishSurveyForParticipant.button"/>

            </g:if>
        </ui:actionsDropdown>
    </g:if>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:status object="${surveyInfo}"/>


<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>


<laser:render template="nav"/>


<ui:messages data="${flash}"/>

<g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
    <ui:msg class="success" showIcon="true" hideClose="true" text="${message(code:"surveyResult.finish.info.consortia")}. ${formatDate(format: message(code: 'default.date.format.notime'), date: SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant).finishDate)}" />
</g:if>

    <g:render template="/survey/participantMessage"/>
    <g:render template="/survey/participantInfos"/>

<laser:render template="/templates/survey/participantView"/>

<laser:htmlEnd />
