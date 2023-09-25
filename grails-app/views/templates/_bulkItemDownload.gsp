<g:if test="${token}">
    <g:if test="${errorKBART}">
        <ui:msg icon="ui exclamation icon" class="error" message="subscription.details.addEntitlements.titleNotMatched" args="[errorCount]"/>
    </g:if>
    <g:else>
        <ui:msg icon="ui check icon" class="success" message="default.file.success"/>
    </g:else>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadKBART" params="[token: token]"><i class="ui icon download"></i></g:link>
</g:if>