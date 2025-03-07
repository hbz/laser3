<%@ page import="de.laser.ui.Icon; de.laser.survey.SurveyConfig; de.laser.RefdataValue; de.laser.properties.PropertyDefinition; de.laser.survey.SurveyOrg; de.laser.storage.RDStore; de.laser.RefdataCategory; de.laser.Org" %>


<laser:htmlStart text="${message(code: 'survey.label')}  ${message(code: 'openParticipantsAgain.label')}" />

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
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
        ${message(code: "openParticipantsAgain.label")} <ui:bubble count="${participantsFinishTotal}" />
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
