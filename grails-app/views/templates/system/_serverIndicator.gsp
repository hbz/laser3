%{-- model: currentServer --}%
<%@ page import="de.laser.utils.AppUtils" %>

<g:if test="${currentServer != AppUtils.PROD}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#contextBar').addClass('la-${currentServer.toLowerCase()}');
        $('.la-top-menu').addClass('la-${currentServer.toLowerCase()}');
        <g:if test="${currentServer == (AppUtils.QA || AppUtils.TEST || AppUtils.DEV)}">
            $('#mainContent').addClass('la-${currentServer.toLowerCase()}');
        </g:if>

    </laser:script>
</g:if>
