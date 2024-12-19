<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<g:if test="${token}">
    <ui:msg class="success" showIcon="true" message="default.file.success"/>
    <g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, filenameDisplay: filenameDisplay, fileformat: fileformat]"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link>
</g:if>
<g:elseif test="${error}">
    <g:if test="${error == 401}">
        Sie haben keinen Zugriff auf das angeforderte Objekt!
    </g:if>
    <g:else>
        ${error}
    </g:else>
</g:elseif>