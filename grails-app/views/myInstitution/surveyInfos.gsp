<%@ page import="de.laser.survey.SurveyConfig;de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue; de.laser.Org" %>
<laser:htmlStart text="${surveyInfo.type.getI10n('value')}" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <ui:crumb text="${surveyInfo.type.getI10n('value')} - ${surveyInfo.name}" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <ui:exportDropdown>
        <ui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="surveyInfos"
                    params="${params + [exportXLSX: true, surveyConfigID: surveyConfig.id]}">${message(code: 'survey.exportSurvey')}</g:link>
        </ui:exportDropdownItem>
    </ui:exportDropdown>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.type.getI10n('value')} - ${surveyInfo.name}" type="Survey">
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<ui:messages data="${flash}"/>

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


    <ui:greySegment>
        <h3 class="ui header"><g:message code="surveyInfo.owner.label"/>:</h3>

        <g:if test="${choosenOrg}">
            <g:set var="choosenOrgCPAs" value="${choosenOrg.getGeneralContactPersons(false)}"/>
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
        </g:if>

        <div class="ui form la-padding-left-07em">
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

<br />

<div class="ui stackable grid">
    <div class="sixteen wide column">

        <div class="la-inline-lists">
            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">

                <laser:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                                  costItemSums        : costItemSums,
                                                                                  subscription        : subscription,
                                                                                  visibleOrgRelations : visibleOrgRelations,
                                                                                  surveyResults       : surveyResults]"/>

            </g:if>

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_GENERAL_SURVEY}">

                <laser:render template="/templates/survey/generalSurvey" model="[surveyConfig : surveyConfig,
                                                                             surveyResults: surveyResults]"/>
            </g:if>

            <g:if test="${surveyInfo && surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT}">

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

<br />

<g:if test="${editable}">
    <g:link class="ui button green js-open-confirm-modal"
            data-confirm-messageUrl="${g.createLink(controller: 'ajaxHtml', action: 'getSurveyFinishMessage', params: [id: surveyInfo.id, surveyConfigID: surveyConfig.id])}"
            data-confirm-term-how="concludeBinding"
            data-confirm-replaceHeader="true"
            controller="myInstitution"
            action="surveyInfoFinish"
            data-targetElement="surveyInfoFinish"
            id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]">
        <g:message
                code="${surveyInfo.isMandatory ? 'surveyResult.finish.mandatory.info2' : 'surveyResult.finish.info2'}"/>
    </g:link>
</g:if>
<br />
<br />
<laser:htmlEnd />
