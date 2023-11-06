<g:if test="${success}">
    <ui:msg icon="ui check icon" class="success" message="subscription.details.addEntitlements.matchingSuccessful" args="[g.createLink(controller: 'subscription', action: 'index', params: [id: subscription.id])]"/>
</g:if>
<g:if test="${truncatedRows}">
    <ui:msg icon="ui exclamation icon" class="error" message="subscription.details.addEntitlements.truncatedRows" args="[truncatedRows]"/>
</g:if>
<g:if test="${errorKBART}">
    <ui:msg icon="ui exclamation icon" class="error" message="subscription.details.addEntitlements.titleNotMatched" args="[errorCount]"/>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadLargeFile" params="[token: token, filenameDisplay: filenameDisplay, fileformat: fileformat]"><i class="ui icon download"></i></g:link>
</g:if>