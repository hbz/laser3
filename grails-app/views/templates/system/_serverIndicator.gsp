%{-- model: currentServer --}%
<%@ page import="de.laser.utils.AppUtils" %>

<g:if test="${currentServer == AppUtils.LOCAL}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#contextBar').addClass('la-local');
        $('#login .card').addClass('la-local');
    </laser:script>
</g:if>
<g:if test="${currentServer == AppUtils.DEV}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#contextBar').addClass('la-dev');
        $('#login .card').addClass('la-dev');
    </laser:script>
</g:if>
<g:if test="${currentServer == AppUtils.QA}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#contextBar').addClass('la-qa');
        $('#login .card').addClass('la-qa');
    </laser:script>
</g:if>
<g:if test="${currentServer == AppUtils.TEST}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#contextBar').addClass('la-test');
        $('#login .card').addClass('la-test');
    </laser:script>
</g:if>

