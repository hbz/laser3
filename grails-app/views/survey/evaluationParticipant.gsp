<%@ page import="de.laser.SurveyConfig; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org" %>
<laser:serviceInjection/>
<!doctype html>



<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'myinst.currentSubscriptions.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="myinst.currentSubscriptions.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
        %{--<semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" controller="survey" action="generatePdfForParticipant"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]">PDF-Export
                </g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>--}%
    <semui:actionsDropdown>
        <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">
            <semui:actionsDropdownItem action="renewEntitlements" controller="survey"
                                       id="${surveyConfig.id}" params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                       message="renewEntitlementsWithSurvey.renewEntitlements"/>
        </g:if>
        <g:if test="${surveyInfo.status.id in [RDStore.SURVEY_SURVEY_STARTED.id, RDStore.SURVEY_SURVEY_COMPLETED.id]}">

            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                <semui:actionsDropdownItem controller="survey" action="openSurveyAgainForParticipant"
                                           params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                           message="openSurveyAgainForParticipant.button"/>

            </g:if>
            <g:if test="${!surveyConfig.isResultsSetFinishByOrg(participant)}">
                <semui:actionsDropdownItem controller="survey" action="finishSurveyForParticipant"
                                           params="[surveyConfigID: surveyConfig.id, participant: participant.id]"
                                           message="finishSurveyForParticipant.button"/>

            </g:if>
        </g:if>
    </semui:actionsDropdown>

</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>


<semui:messages data="${flash}"/>

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

    <semui:form>
    <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <table class="ui table la-js-responsive-table la-table compact">
        <tbody>
        <tr>
            <td>
                <p><strong>${choosenOrg?.name} (${choosenOrg?.shortname})</strong></p>

                ${choosenOrg?.libraryType?.getI10n('value')}
            </td>
            <td>
                <g:if test="${choosenOrgCPAs}">
                    <g:set var="oldEditable" value="${editable}"/>
                    <g:set var="editable" value="${false}" scope="request"/>
                    <g:each in="${choosenOrgCPAs}" var="gcp">
                        <g:render template="/templates/cpa/person_details"
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
                    <textarea readonly="readonly" rows="3">${surveyInfo.comment}</textarea>
                </g:if>
                <g:else>
                    <g:message code="surveyConfigsInfo.comment.noComment"/>
                </g:else>
            </div>
        </div>

    </semui:form>
</g:if>

<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">

            <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">

                <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                                  costItemSums        : costItemSums,
                                                                                  subscription        : subscription,
                                                                                  visibleOrgRelations : visibleOrgRelations,
                                                                                  surveyResults       : surveyResults]"/>
            </g:if>

            <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                <g:render template="/templates/survey/generalSurvey" model="[surveyConfig        : surveyConfig,
                                                                             costItemSums        : costItemSums,
                                                                             subscription        : surveyConfig.subscription,
                                                                             tasks               : tasks,
                                                                             visibleOrgRelations : visibleOrgRelations]"/>
            </g:if>

            <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">

                <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                                  costItemSums        : costItemSums,
                                                                                  subscription        : subscription,
                                                                                  visibleOrgRelations : visibleOrgRelations,
                                                                                  surveyResults       : surveyResults]"/>

                <g:render template="/templates/survey/entitlementSurvey"/>

            </g:if>

        </div>
    </div>
</div>
</body>
</html>
