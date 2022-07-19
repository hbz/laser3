<%@ page import="de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.SurveyOrg; de.laser.SurveyConfig; de.laser.helper.RDStore; de.laser.RefdataCategory; de.laser.Org" %>

<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')} (<g:if test="${params.tab == 'participantsViewAllFinish'}">
        ${message(code:"openParticipantsAgain.label")}
    </g:if>
    <g:else>
        ${message(code:"openParticipantsAgain.reminder")}
    </g:else>)</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                     text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>
    <g:if test="${params.tab == 'participantsViewAllFinish'}">
        <semui:crumb message="openParticipantsAgain.label" class="active"/>
    </g:if>
    <g:else>
        <semui:crumb message="openParticipantsAgain.reminder" class="active"/>
    </g:else>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
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
    </g:else>:

    <g:if test="${params.tab == 'participantsViewAllFinish'}">
        ${message(code: 'openParticipantsAgain.label')}
    </g:if>
    <g:else>
        ${message(code: 'openParticipantsAgain.reminder')}
    </g:else>
</h2>
<br />

<div class="ui grid">

    <div class="sixteen wide stretched column">
        <div class="ui top attached stackable tabular la-tab-with-js menu">

            <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
                    controller="survey" action="openParticipantsAgain"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllFinish']">
                ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                <div class="ui floating circular label">${participantsFinishTotal}</div>
            </g:link>

            <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
                    controller="survey" action="openParticipantsAgain"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
                ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                <div class="ui floating circular label">${participantsNotFinishTotal}</div>
            </g:link>

        </div>

        <semui:form>

                <g:render template="evaluationParticipantsView" model="[showCheckbox: true,
                                                                        showOpenParticipantsAgainButtons: true,
                                                                        processAction: 'processOpenParticipantsAgain',
                                                                        tmplConfigShow   : ['lineNumber', 'name', (surveyConfig.pickAndChoose ? 'finishedDate' : ''), (surveyConfig.pickAndChoose ? 'surveyTitlesCount' : ''), 'surveyProperties', 'commentOnlyForOwner']]"/>

        </semui:form>

    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#orgListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
        }
    })
</laser:script>

</body>
</html>
