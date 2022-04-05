<%@ page import="de.laser.SurveyConfig; de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                     text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>
    <semui:crumb message="surveyTransfer.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey"
                             action="surveyEvaluation"/>
</h1>

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
    : ${message(code: 'surveyInfo.transfer')}
</h2>

<g:if test="${(surveyInfo.status in [RDStore.SURVEY_SURVEY_STARTED, RDStore.SURVEY_SURVEY_COMPLETED, RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED])}">
    <semui:form>

        <g:render template="evaluationParticipantsView" model="[showCheckbox: true,
                                                                showTransferFields: true,
                                                                processAction: 'processTransferParticipants',
                                                                tmplConfigShow   : ['lineNumber', 'name', (surveyConfig.pickAndChoose ? 'finishedDate' : ''), (surveyConfig.pickAndChoose ? 'surveyTitlesCount' : ''), 'surveyProperties', 'commentOnlyForOwner']]"/>

    </semui:form>
</g:if>
<g:else>
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:else>



</body>
</html>
