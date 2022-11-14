<%@ page import="de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection />

<g:set var="checkCons" value="${contextService.getOrg().id == subscription.getConsortia()?.id && subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}" />

<g:if test="${checkCons}">

    <semui:childSubscriptionIcon/>

    <laser:script>
      $(document).ready(function() {
        $('.la-subscriptionIsChild').visibility({
          type   : 'fixed',
          offset : 55,
          zIndex: 102
        })
      })
    </laser:script>
</g:if>