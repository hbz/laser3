<%@ page import="de.laser.survey.SurveyConfig;de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org" %>

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
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" controller="survey" action="generatePdfForParticipant"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]">PDF-Export
                </g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    <ui:actionsDropdown>
        <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id]}">

            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                <ui:actionsDropdownItem controller="survey" action="openSurveyAgainForParticipant"
                                           params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                           message="openSurveyAgainForParticipant.button"/>

            </g:if>
            <g:if test="${!surveyConfig.isResultsSetFinishByOrg(participant)}">
                <ui:actionsDropdownItem controller="survey" action="finishSurveyForParticipant"
                                           params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                           message="finishSurveyForParticipant.button"/>

            </g:if>
        </g:if>
    </ui:actionsDropdown>

</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey">
    <ui:xEditable owner="${surveyInfo}" field="name"/>
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>


<ui:messages data="${flash}"/>

<g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
    <div class="ui icon positive message">
        <i class="info icon"></i>
        <div class="content">
            <div class="header"></div>
            <p>
                <g:message code="surveyResult.finish.info.consortia"/>.
            </p>
        </div>
    </div>
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

<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">

            <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">

                <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                                  costItemSums        : costItemSums,
                                                                                  subscription        : subscription,
                                                                                  visibleOrgRelations : visibleOrgRelations,
                                                                                  surveyResults       : surveyResults]"/>
            </g:if>

            <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                <laser:render template="/templates/survey/generalSurvey" model="[surveyConfig        : surveyConfig,
                                                                             costItemSums        : costItemSums,
                                                                             subscription        : surveyConfig.subscription,
                                                                             tasks               : tasks,
                                                                             visibleOrgRelations : visibleOrgRelations]"/>
            </g:if>

            <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">

                <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                                  costItemSums        : costItemSums,
                                                                                  subscription        : subscription,
                                                                                  visibleOrgRelations : visibleOrgRelations,
                                                                                  surveyResults       : surveyResults]"/>

                <laser:render template="/templates/survey/entitlementSurvey"/>

            </g:if>

        </div>
    </div>
</div>

<laser:htmlEnd />
