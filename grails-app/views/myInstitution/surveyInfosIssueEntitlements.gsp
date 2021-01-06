<%@ page import="de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue;de.laser.Org" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'issueEntitlementsSurvey.label')}</title>
</head>

<body>

<semui:breadcrumbs>

    <semui:crumb controller="myInstitution" action="currentSurveys" message="currentSurveys.label"/>
    <semui:crumb message="issueEntitlementsSurvey.label"/>
    <semui:crumb controller="subscription" action="index" id="${subscription.id}"
                 text="${subscription.name}" class="active"/>
</semui:breadcrumbs>

%{--<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="myInstitution" action="surveyInfos"
                    params="${params + [exportXLS: true]}">${message(code: 'survey.exportSurvey')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
</semui:controlButtons>--}%

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>
${message(code: 'issueEntitlementsSurvey.label')} - ${surveyInfo.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:messages data="${flash}"/>

<br />

<g:if test="${de.laser.SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, institution)?.finishDate != null}">
    <div class="ui icon positive message">
        <i class="info icon"></i>

        <div class="content">
            <div class="header"></div>

            <p>
                <%-- <g:message code="surveyInfo.finishOrSurveyCompleted"/> --%>
                <g:message code="renewEntitlementsWithSurvey.finish.info"/>
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


    <div class="la-inline-lists">
        <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                          subscription        : subscription,
                                                                          visibleOrgRelations : visibleOrgRelations,
                                                                          surveyResults       : surveyResults        ]"/>
    </div>

<div class="ui stackable grid">

    <div class="sixteen wide column">
        <div class="la-inline-lists">
            <div class="ui card la-time-card">
                <div class="content">
                    <div class="header"><g:message code="subscription.entitlement.plural"/></div>
                </div>

                <div class="content">
                    <div class="header"><g:message code="renewEntitlementsWithSurvey.currentFixedEntitlements"/></div>
                    <dl>
                        <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                        <dd>${iesFix?.size() ?: 0}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'tipp.listPrice')}</dt>
                        <dd><g:formatNumber number="${iesFixListPriceSum}" type="currency"/></dd>
                    </dl>
                </div>

                <div class="content">
                    <div class="header"><g:message code="renewEntitlementsWithSurvey.currentEntitlements"/></div>
                    <dl>
                        <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                        <dd>${ies?.size() ?: 0}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'tipp.listPrice')}</dt>
                        <dd><g:formatNumber number="${iesListPriceSum}" type="currency"/></dd>
                    </dl>
                </div>

                <div class="content">
                    <div class="ui form twelve wide column">
                        <div class="two fields">

                            <div class="eight wide field" style="text-align: left;">
                                <g:if test="${subscription}">
                                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                                            id="${subscription.id}"
                                            params="${[targetObjectId: subscription.id,
                                                       surveyConfigID      : surveyConfig.id]}"
                                            class="ui button">
                                        <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                                    </g:link>
                                </g:if>
                            </div>


                            <div class="eight wide field" style="text-align: right;">
                                <g:link controller="subscription" action="showEntitlementsRenewWithSurvey"
                                        id="${surveyConfig.id}"
                                        class="ui button">
                                    <g:message code="renewEntitlementsWithSurvey.toCurrentEntitlements"/>
                                </g:link>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>

    </div>

    <div class="ui form twelve wide column">
                <g:if test="${subscription && editable}">
                    <g:link class="ui button green js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: "confirm.dialog.concludeBinding.renewalEntitlements")}"
                            data-confirm-term-how="concludeBinding"
                            controller="myInstitution" action="surveyInfoFinish"
                            id="${surveyInfo.id}"
                            params="[surveyConfigID: surveyConfig.id]">
                        <g:message code="renewEntitlementsWithSurvey.submit"/>
                    </g:link>
                </g:if>
    </div>

</div><!-- .grid -->

<br />
<br />



<br />
<br />

</body>
</html>
