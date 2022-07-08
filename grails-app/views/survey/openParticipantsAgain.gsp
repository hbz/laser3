<%@ page import="de.laser.survey.SurveyConfig; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.Org" %>

<g:set var="currenMsgCode" value="${params.tab == 'participantsViewAllFinish' ? 'openParticipantsAgain.label' : 'openParticipantsAgain.reminder'}" />

<laser:htmlStart text="${message(code: 'survey.label')}  ${message(code: currenMsgCode)}" serviceInjection="true" />

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                     text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>

    <semui:crumb message="${currenMsgCode}" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <laser:render template="actions"/>
</semui:controlButtons>

<semui:h1HeaderWithIcon type="Survey">
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</semui:h1HeaderWithIcon>



<laser:render template="nav"/>

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

        ${message(code: "${currenMsgCode}")}
</h2>
<br />

<div class="ui grid">

    <div class="sixteen wide stretched column">
        <div class="ui top attached stackable tabular menu">

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

                <laser:render template="evaluationParticipantsView" model="[showCheckbox: true,
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

<laser:htmlEnd />
