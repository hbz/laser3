<%@ page import="de.laser.ui.Icon; de.laser.ui.Btn" %>
<g:if test="${success}">
    <ui:msg showIcon="true" class="success">
        <g:message code="default.usage.sushiCallCheck.success"/>
    </ui:msg>
</g:if>
<g:elseif test="${wekbUnavailable}">
    <ui:msg showIcon="true" class="error">
        <g:message code="default.usage.sushiCallCheck.wekbUnavailable"/><br>
    </ui:msg>
</g:elseif>
<g:elseif test="${token}">
    <ui:msg showIcon="true" class="error">
        <g:message code="default.usage.sushiCallCheck.error"/><br>
        <g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, fileformat: 'xlsx']"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
    </ui:msg>
</g:elseif>