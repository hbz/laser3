<%@ page import="de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyTransfer.label')})" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
%{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"--}%
%{--                     text="${surveyConfig.getConfigNameShort()}"/>--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}" />
    </g:if>
%{--    <ui:crumb message="surveyTransfer.label" class="active"/>--}%
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
    : ${message(code: 'surveyInfo.transfer')}
</h2>

<g:if test="${(surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED, RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED])}">
    <ui:greySegment>
        <g:if test="${surveyConfig.pickAndChoose}">
            <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'finishedDate', 'surveyTitlesCount', 'surveyProperties', 'commentOnlyForOwner']}"/>
        </g:if>
        <g:else>
            <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'commentOnlyForOwner']}"/>
        </g:else>


        <laser:render template="evaluationParticipantsView" model="[showCheckboxForParticipantsHasAccess: true,
                                                                    showCheckboxForParticipantsHasNoAccess: true,
                                                                showTransferFields: true,
                                                                processAction: 'processTransferParticipants',
                                                                tmplConfigShow   : tmplConfigShowList]"/>

    </ui:greySegment>
</g:if>
<g:else>
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:else>

<laser:htmlEnd />
