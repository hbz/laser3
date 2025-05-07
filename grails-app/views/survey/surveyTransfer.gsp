<%@ page import="de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyTransfer.label')})"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
    %{--        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]"--}%
    %{--                     text="${surveyConfig.getConfigNameShort()}"/>--}%
        <ui:crumb class="active" text="${surveyConfig.getConfigNameShort()}"/>
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
    <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}"
                       href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<g:if test="${!surveyConfig.subSurveyUseForTransfer}">
    <laser:render template="nav"/>
</g:if>
<g:else>
    <h2 class="ui header">
    <g:message code="surveyTransfer.action"/>:
    </h2>
</g:else>

<ui:objectStatus object="${surveyInfo}"/>

<ui:messages data="${flash}"/>

<br/>

<g:if test="${(surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])}">
    <div class="ui segment">
        <strong>${message(code: 'survey.notStarted')}</strong>
    </div>
</g:if>
<g:else>

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
                <g:set var="tmplConfigShowList"
                       value="${['lineNumber', 'name', 'finishedDate', 'surveyTitlesCount', 'surveyProperties', 'commentOnlyForOwner']}"/>
            </g:if>
            <g:elseif test="${surveyConfig.pickAndChoose && surveyConfig.vendorSurvey}">
                <g:set var="tmplConfigShowList"
                       value="${['lineNumber', 'name', 'finishedDate', 'surveyTitlesCount', 'surveyProperties', 'surveyVendor', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:elseif test="${surveyConfig.packageSurvey && surveyConfig.vendorSurvey}">
                <g:set var="tmplConfigShowList"
                       value="${['lineNumber', 'name', 'surveyProperties', 'surveyPackages', 'surveyCostItemsPackages', 'surveyVendor', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:elseif test="${surveyConfig.packageSurvey}">
                <g:set var="tmplConfigShowList"
                       value="${['lineNumber', 'name', 'surveyProperties', 'surveyPackages', 'surveyCostItemsPackages', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:elseif test="${surveyConfig.vendorSurvey}">
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'surveyVendor', 'commentOnlyForOwner']}"/>
            </g:elseif>
            <g:else>
                <g:set var="tmplConfigShowList" value="${['lineNumber', 'name', 'surveyProperties', 'commentOnlyForOwner']}"/>
            </g:else>


            <laser:render template="evaluationParticipantsView" model="[showCheckboxForParticipantsHasAccess  : editable,
                                                                        showCheckboxForParticipantsHasNoAccess: editable,
                                                                        showTransferFields                    : editable,
                                                                        processAction                         : 'processTransferParticipants',
                                                                        tmplConfigShow                        : tmplConfigShowList,
                                                                        showIcons: params.tab == 'participantsView']"/>
        </div>


</g:else>

<laser:htmlEnd/>
