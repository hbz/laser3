<%@ page import="de.laser.ui.Icon" %>
<g:if test="${token}">
    <ui:msg class="success" showIcon="true" message="default.file.success"/>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadLargeFile" params="[token: token, filenameDisplay: filenameDisplay, fileformat: fileformat]"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
</g:if>