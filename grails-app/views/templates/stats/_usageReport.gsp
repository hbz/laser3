<%@ page import="de.laser.helper.Icons" %>
<g:if test="${token}">
    <ui:msg class="success" icon="${Icons.SUCCESS}" message="default.stats.success"/><g:link class="ui icon button la-modern-button" controller="subscription" action="downloadReport" params="[token: token]"><i class="${Icons.CMD.DOWNLOAD}"></i></g:link>
</g:if>
<g:if test="${error}">
    <ui:msg class="error" icon="${Icons.ERROR}" text="${message(code: "default.stats.error.${error}")}"/>
</g:if>