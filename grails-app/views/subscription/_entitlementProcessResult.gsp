<%@ page import="de.laser.helper.Icons" %>
<g:if test="${success}">
    <ui:msg icon="ui check icon" class="success" message="subscription.details.addEntitlements.matchingSuccessful" args="[processCount, processRows, countSelectTipps, countNotSelectTipps, g.createLink(controller: 'subscription', action: 'index', params: [id: subscription.id])]"/>
</g:if>
<g:if test="${tippSelectForSurveySuccess}">
    <ui:msg class="positive" header="${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.label')}">
        <g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.selectProcess"
                   args="[processCount, processRows, countSelectTipps, countNotSelectTipps, g.createLink(controller: 'subscription', action: 'renewEntitlementsWithSurvey', params: [id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'selectedIEs'])]"/>
    </ui:msg>
</g:if>
<g:if test="${truncatedRows}">
    <ui:msg icon="ui exclamation icon" class="error" message="subscription.details.addEntitlements.truncatedRows" args="[truncatedRows]"/>
</g:if>
<g:if test="${errorKBART}">
    <ui:msg icon="ui exclamation icon" class="error" message="subscription.details.addEntitlements.titleNotMatched" args="[errorCount]"/>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadLargeFile" params="[token: token, fileformat: fileformat]"><i class="${Icons.CMD_DOWNLOAD}"></i></g:link>
</g:if>
<g:elseif test="${errMess}">
    <ui:msg icon="ui exclamation icon" class="error" message="${errMess}" args="[errorCount]"/>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadLargeFile" params="[token: token, fileformat: 'txt']"><i class="${Icons.CMD_DOWNLOAD}"></i></g:link>
</g:elseif>