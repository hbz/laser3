<%@ page import="de.laser.SurveyConfig; de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.Org" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${surveyInfo.type.getI10n('value')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb text="${surveyInfo.type.getI10n('value')}"/>
    <semui:crumb text="${surveyInfo.name}" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="surveyInfos"
                    params="${params + [exportXLSX: true, surveyConfigID: surveyConfig.id]}">${message(code: 'survey.exportSurvey')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
${surveyInfo.type.getI10n('value')} - ${surveyInfo.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:messages data="${flash}"/>

<br />

<g:if test="${surveyConfig.isResultsSetFinishByOrg(institution)}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message
                        code="${surveyInfo.isMandatory ? 'surveyResult.finish.mandatory.info' : 'surveyResult.finish.info'}"/>.
            </p>
        </div>
    </div>
</g:if>

<g:if test="${ownerId}">
    <g:set var="choosenOrg" value="${Org.findById(ownerId)}"/>
    <g:set var="choosenOrgCPAs" value="${choosenOrg?.getGeneralContactPersons(false)}"/>

    <semui:form>
        <h3 class="ui header"><g:message code="surveyInfo.owner.label"/>:</h3>

        <table class="ui table la-table compact">
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

<br />

<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">
            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">

                <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                                  costItemSums        : costItemSums,
                                                                                  subscription        : subscription,
                                                                                  visibleOrgRelations : visibleOrgRelations,
                                                                                  surveyResults       : surveyResults]"/>

            </g:if>

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                <g:render template="/templates/survey/generalSurvey" model="[surveyConfig : surveyConfig,
                                                                             surveyResults: surveyResults]"/>
            </g:if>

        </div>
    </div>
</div>

<br />

<g:if test="${editable}">
    <g:link class="ui button green js-open-confirm-modal"
            data-confirm-tokenMsg="${message(code: "confirm.dialog.concludeBinding.survey")}"
            data-confirm-term-how="concludeBinding"
            controller="myInstitution"
            action="surveyInfoFinish"
            id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]">
        <g:message
                code="${surveyInfo.isMandatory ? 'surveyResult.finish.mandatory.info2' : 'surveyResult.finish.info2'}"/>
    </g:link>
</g:if>
<br />
<br />
</body>
</html>
