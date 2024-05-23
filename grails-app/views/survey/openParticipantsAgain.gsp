<%@ page import="de.laser.survey.SurveyConfig; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.Org" %>


<laser:htmlStart text="${message(code: 'survey.label')}  ${message(code: 'openParticipantsAgain.label')}" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"
                     text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>

    <ui:crumb message="openParticipantsAgain.label" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>

<uiSurvey:status object="${surveyInfo}"/>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="bordered inverted orange clipboard la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.subscription}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>
    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>:

        ${message(code: "openParticipantsAgain.label")} <div class="ui blue circular label">${participantsFinishTotal}</div>
</h2>
<br />

<div class="ui grid">

    <div class="sixteen wide stretched column">

        <ui:greySegment>
            <g:if test="${surveyConfig.pickAndChoose}">
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'finishedDate', 'surveyTitlesCount', 'surveyProperties', 'commentOnlyForOwner', 'reminderMailDate']}"/>
            </g:if>
            <g:else>
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'commentOnlyForOwner', 'reminderMailDate']}"/>
            </g:else>

                <laser:render template="evaluationParticipantsView" model="[showCheckboxForParticipantsHasAccess: true,
                                                                            showCheckboxForParticipantsHasNoAccess: false,
                                                                        showOpenParticipantsAgainButtons: true,
                                                                        processAction: 'createOwnMail',
                                                                        processController: 'mail',
                                                                        tmplConfigShow   : tmplConfigShowList]"/>

        </ui:greySegment>

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
