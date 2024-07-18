<%@ page import="de.laser.ui.Icon" %>
<g:if test="${token}">
    <ui:msg class="success" showIcon="true" message="default.stats.success"/>
    <g:link class="ui icon button la-modern-button" controller="subscription" action="downloadReport" params="[token: token]"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
</g:if>
<g:if test="${error}">
    <ui:msg class="error" showIcon="true" text="${message(code: "default.stats.error.${error}")}"/>
</g:if>