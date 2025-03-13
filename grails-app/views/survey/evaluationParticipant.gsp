<%@ page import="de.laser.survey.SurveyPersonResult; de.laser.ui.Icon; de.laser.survey.SurveyConfig;de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org" %>

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
    <ui:msg class="success" showIcon="true" hideClose="true" text="${message(code:"surveyResult.finish.info.consortia")}." />
</g:if>

<g:if test="${participant}">
    <div class="ui error message">
        <div class="header">
            <g:message code="myinst.message.attention"/>
            <g:message code="survey.message.participantView"/>
            <span class="ui label"><g:link controller="organisation" action="show" id="${participant.id}">${participant.getDesignation()}</g:link></span>.
        </div>

        <p>

            <g:message code="myinst.subscriptionDetails.message.hereLink"/>
            <g:link controller="survey" action="show" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]">
                <g:message code="survey.message.backToSurvey"/>
            </g:link>


            <g:message code="myinst.subscriptionDetails.message.and"/>

            <g:link controller="survey" action="surveyEvaluation" id="${subscription.instanceOf.id}">
                <g:message code="survey.message.backToParticipants"/>
            </g:link>.
        </p>
    </div>


    <ui:greySegment>
    <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>
    <g:set var="surveyPersons"
               value="${SurveyPersonResult.executeQuery('select spr.person from SurveyPersonResult spr where spr.surveyPerson = true and spr.participant = :participant and spr.surveyConfig = :surveyConfig', [participant: choosenOrg, surveyConfig: surveyConfig])}"/>
    <table class="ui table la-js-responsive-table la-table compact">
        <tbody>
        <tr>
            <td>
                <p><strong><g:link controller="organisation" action="show" id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                ${choosenOrg.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${surveyPersons}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <laser:render template="/addressbook/person_details"
                                      model="${[person: gcp, tmplHideLinkToAddressbook: true, showFunction: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
                <g:elseif test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <laser:render template="/addressbook/person_details"
                                  model="${[person: gcp, tmplHideLinkToAddressbook: true, showFunction: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:elseif>
            </td>
        </tr>
        </tbody>
    </table>

        <div class="ui form">
            <div class="field">
                <label>
                    <g:message code="surveyInfo.comment.label"/>
                </label>
                <g:if test="${surveyInfo.comment}">
                    <textarea class="la-textarea-resize-vertical" readonly="readonly" rows="3">${surveyInfo.comment}</textarea>
                </g:if>
                <g:else>
                    <g:message code="surveyConfigsInfo.comment.noComment"/>
                </g:else>
            </div>
        </div>

    </ui:greySegment>
</g:if>

<laser:render template="/templates/survey/participantView"/>

<laser:htmlEnd />
