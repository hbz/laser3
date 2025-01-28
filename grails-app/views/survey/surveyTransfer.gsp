<%@ page import="de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyTransfer.label')})" />

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

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br />
<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.subscription}">
        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
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

        <div class="ui top attached stackable tabular la-tab-with-js menu">

            <g:link class="item ${params.tab == 'participantsViewAllFinish' ? 'active' : ''}"
                    controller="survey" action="surveyEvaluation"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllFinish']">
                ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                <ui:bubble float="true" count="${participantsFinishTotal}"/>
            </g:link>

            <g:link class="item ${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}"
                    controller="survey" action="surveyEvaluation"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsViewAllNotFinish']">
                ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                <ui:bubble float="true" count="${participantsNotFinishTotal}"/>
            </g:link>

            <g:link class="item ${params.tab == 'participantsView' ? 'active' : ''}"
                    controller="survey" action="surveyEvaluation"
                    params="[id: params.id, surveyConfigID: surveyConfig.id, tab: 'participantsView']">
                ${message(code: 'surveyEvaluation.participantsView')}
                <ui:bubble float="true" count="${participantsTotal}"/>
            </g:link>

        </div>
        <div class="ui bottom attached tab segment active">

            <g:if test="${surveyConfig.pickAndChoose}">
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'finishedDate', 'surveyTitlesCount', 'surveyProperties', 'commentOnlyForOwner']}"/>
            </g:if>
            <g:elseif test="${surveyConfig.packageSurvey && surveyConfig.vendorSurvey}">
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'surveyPackages', 'surveyCostItemsPackages', 'surveyVendors', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:elseif test="${surveyConfig.packageSurvey}">
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'surveyPackages', 'surveyCostItemsPackages', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:elseif test="${surveyConfig.vendorSurvey}">
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'surveyVendors', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:else>
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'commentOnlyForOwner']}"/>
            </g:else>


            <laser:render template="evaluationParticipantsView" model="[showCheckboxForParticipantsHasAccess: true,
                                                                        showCheckboxForParticipantsHasNoAccess: true,
                                                                        showTransferFields: true,
                                                                        processAction: 'processTransferParticipants',
                                                                        tmplConfigShow   : tmplConfigShowList]"/>
        </div>

    </ui:greySegment>
</g:if>
<g:else>
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:else>

<laser:htmlEnd />
