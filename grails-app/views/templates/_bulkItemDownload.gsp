<g:if test="${token}">
    <ui:msg icon="ui check icon" class="success" message="default.file.success"/><g:link class="ui icon button la-modern-button" controller="package" action="downloadKBART" params="[token: token]"><i class="ui icon download"></i></g:link>
</g:if>