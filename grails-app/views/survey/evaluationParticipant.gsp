<%@ page import="de.laser.helper.Icons; de.laser.survey.SurveyConfig;de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org" %>

<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyResult.label')}-${message(code: 'surveyParticipants.label')})" serviceInjection="true"/>

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
    <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id]}">
        <ui:actionsDropdown>
            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
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
    <ui:linkWithIcon icon="${Icons.SUBSCRIPTION} bordered inverted orange la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>


<laser:render template="nav"/>


<ui:messages data="${flash}"/>

<g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
    <ui:msg class="success" showIcon="true" hideClose="true" text="${message(code:"surveyResult.finish.info.consortia")}." />
</g:if>

<g:if test="${participant}">

    <ui:greySegment>
    <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <table class="ui table la-js-responsive-table la-table compact">
        <tbody>
        <tr>
            <td>
                <p><strong><g:link controller="organisation" action="show" id="${choosenOrg.id}">${choosenOrg.name} (${choosenOrg.sortname})</g:link></strong></p>

                ${choosenOrg.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <laser:render template="/templates/cpa/person_details"
                                  model="${[person: gcp, tmplHideLinkToAddressbook: true]}"/>
                    </g:each>
                    <g:set var="editable" value="${oldEditable ?: false}" scope="request"/>
                </g:if>
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
