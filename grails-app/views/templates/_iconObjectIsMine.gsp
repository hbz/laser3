<%@ page import="de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection />
<g:if test="${isMyPlatform}">
  <ui:objectMineIcon myObject="${message(code: 'license.relationship.platform')}"/>
</g:if>
<g:if test="${isMyPkg}">
  <ui:objectMineIcon myObject="${message(code: 'license.relationship.pkg')}"/>
</g:if>
<g:if test="${isMyOrg}">
  <ui:objectMineIcon myObject="${message(code: 'license.relationship.org')}"/>
</g:if>
<laser:script file="${this.getGroovyPageFileName()}">
  $(document).ready(function() {
    $('.la-objectIsMine').visibility({
      type   : 'fixed',
      offset : 55,
      zIndex: 101
    })
  })
</laser:script>
