<%@ page import="de.laser.helper.Icons" %>
<g:if test="${token}">
    <ui:msg icon="ui check icon" class="success" message="default.file.success"/>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadLargeFile" params="[token: token, filenameDisplay: filenameDisplay, fileformat: fileformat]"><i class="${Icons.CMD_DOWNLOAD}"></i></g:link>
</g:if>