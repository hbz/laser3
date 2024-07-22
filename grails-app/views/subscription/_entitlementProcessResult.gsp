<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${success}">
    <ui:msg class="success" showIcon="true" message="subscription.details.addEntitlements.matchingSuccessful" args="[processCount, processRows, countSelectTipps, countNotSelectTipps, g.createLink(controller: 'subscription', action: 'index', params: [id: subscription.id])]"/>
</g:if>
<g:if test="${tippSelectForSurveySuccess}">
    <ui:msg class="success" header="${message(code:'renewEntitlementsWithSurvey.issueEntitlementSelect.label')}">
        <g:message code="renewEntitlementsWithSurvey.issueEntitlementSelect.selectProcess"
                   args="[processCount, processRows, countSelectTipps, countNotSelectTipps, g.createLink(controller: 'subscription', action: 'renewEntitlementsWithSurvey', params: [id: subscriberSub.id, surveyConfigID: surveyConfig.id, tab: 'selectedIEs'])]"/>
    </ui:msg>
</g:if>
<g:if test="${truncatedRows}">
    <ui:msg class="error" showIcon="true" message="subscription.details.addEntitlements.truncatedRows" args="[truncatedRows]"/>
</g:if>
<g:if test="${errorKBART}">
    <ui:msg class="error" showIcon="true" message="subscription.details.addEntitlements.titleNotMatched" args="[errorCount]"/>
    <g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, fileformat: fileformat]"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
</g:if>
<g:elseif test="${errMess}">
    <ui:msg class="error" showIcon="true" message="${errMess}" args="[errorCount]"/>
    <g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, fileformat: 'txt']"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
</g:elseif>