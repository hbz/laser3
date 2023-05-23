%{-- model: currentServer --}%
<%@ page import="de.laser.utils.AppUtils" %>

<g:if test="${currentServer == AppUtils.LOCAL}">
    <div class="ui yellow label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.local')}"></div>
</g:if>
<g:if test="${currentServer == AppUtils.DEV}">
    <div class="ui green label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.dev')}"></div>
</g:if>
<g:if test="${currentServer == AppUtils.QA}">
    <div class="ui red label big la-server-label" aria-label="${message(code:'ariaLabel.serverIdentification.qa')}"></div>
</g:if>
