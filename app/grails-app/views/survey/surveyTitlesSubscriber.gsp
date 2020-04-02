<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.Subscription; com.k_int.kbplus.ApiSource; com.k_int.kbplus.Platform; com.k_int.kbplus.BookInstance; com.k_int.kbplus.Org" %>
<laser:serviceInjection/>
<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.renewEntitlements.label')}</title>
</head>

<body>
<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
        <semui:crumb class="active" controller="survey" action="surveyTitlesEvaluation" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" message="surveyTitlesEvaluation.label"/>
    </g:if>

</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
        <g:if test="${surveyOrg.finishDate && surveyInfo && surveyInfo.status.id == de.laser.helper.RDStore.SURVEY_SURVEY_STARTED.id}">
            <semui:actionsDropdownItem controller="survey" action="openIssueEntitlementsSurveyAgain"
                                       params="[id: surveyConfig.id, participant: participant.id]"
                                       message="openIssueEntitlementsSurveyAgain.label"/>
        </g:if>

        <g:if test="${surveyInfo && surveyInfo.status.id in [de.laser.helper.RDStore.SURVEY_IN_EVALUATION.id, de.laser.helper.RDStore.SURVEY_COMPLETED.id]}">
            <semui:actionsDropdownItem controller="survey" action="completeIssueEntitlementsSurveyforParticipant"
                                       params="[id: surveyConfig.id, participant: participant.id]"
                                       message="completeIssueEntitlementsSurvey.forParticipant.label"/>
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled tooltip="${message(code: 'renewEntitlementsWithSurvey.noCompleted')}"
                                               controller="survey"
                                               action="completeIssueEntitlementsSurveyforParticipant"
                                               message="completeIssueEntitlementsSurvey.forParticipant.label"/>
        </g:else>

    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerTitleIcon type="Survey"/>
<g:message code="issueEntitlementsSurvey.label"/>: <g:link controller="subscription" action="index"
                                                           id="${subscriptionInstance.id}">${surveyConfig.getConfigNameShort()}</g:link>
</h1>


<g:if test="${flash}">
    <semui:messages data="${flash}"/>
</g:if>

<g:if test="${participant}">
    <semui:form>
        <g:set var="choosenOrg" value="${Org.findById(participant.id)}"/>
        <g:set var="choosenOrgCPAs" value="${choosenOrg.getGeneralContactPersons(false)}"/>

        <g:if test="${choosenOrg}">
        <table class="ui table la-table la-table-small">
            <tbody>
            <tr>
                <td>
                    <p><strong>${choosenOrg.name} (${choosenOrg.shortname})</strong></p>

                    ${choosenOrg.libraryType.getI10n('value')}
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
        </g:if>

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

<br>


<div class="la-inline-lists">
    <g:render template="/templates/survey/subscriptionSurvey" model="[surveyConfig        : surveyConfig,
                                                                      subscriptionInstance: subscriptionInstance,
                                                                      visibleOrgRelations : visibleOrgRelations,
    ]"/>
</div>

<div class="ui stackable grid">

    <div class="twelve wide column">
        <div class="la-inline-lists">
            <div class="ui card la-time-card">
                <div class="content">
                    <div class="header"><g:message code="subscription.entitlement.plural"/></div>
                </div>

                <div class="content">
                    <div class="header"><g:message code="renewEntitlementsWithSurvey.currentFixedEntitlements"/></div>
                    <dl>
                        <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                        <dd>${iesFix.size() ?: 0}</dd>
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
                        <dd>${ies.size() ?: 0}</dd>
                    </dl>
                    <dl>
                        <dt class="control-label">${message(code: 'tipp.listPrice')}</dt>
                        <dd><g:formatNumber number="${iesListPriceSum}" type="currency"/></dd>
                    </dl>
                </div>

                <div class="content">
                    <div class="ui la-vertical buttons">
                        <g:link action="showEntitlementsRenew"
                            id="${surveyConfig.id}" params="[participant: participant.id]"
                                class="ui button">
                            <g:message code="renewEntitlementsWithSurvey.toCurrentEntitlements"/>
                        </g:link>
                    </div>
                </div>
            </div>

        </div>

    </div>

</div><!-- .grid -->

<br>
<br>



<br>
<br>

</body>
</html>
