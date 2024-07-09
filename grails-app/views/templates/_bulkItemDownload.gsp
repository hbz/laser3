<%@ page import="de.laser.helper.Icons" %>
<g:if test="${token}">
    <ui:msg class="success" icon="${Icons.SYM.SUCCESS}" message="default.file.success"/>
    <g:link class="ui icon button la-modern-button" controller="package" action="downloadLargeFile" params="[token: token, filenameDisplay: filenameDisplay, fileformat: fileformat]"><i class="${Icons.CMD.DOWNLOAD}"></i></g:link>
</g:if>