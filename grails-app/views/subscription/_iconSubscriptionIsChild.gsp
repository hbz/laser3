<%@ page import="de.laser.interfaces.CalculatedType;" %>
<g:set var="checkCons" value="${contextService.getOrg().id == subscription.getConsortia()?.id && subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}" />

<g:if test="${checkCons}">

    <ui:childSubscriptionIcon/>

    <laser:script file="${this.getGroovyPageFileName()}">
      $(document).ready(function() {
        $('.la-subscriptionIsChild').visibility({
          type   : 'fixed',
          offset : 55,
          zIndex: 102
        })
      })
    </laser:script>
</g:if>