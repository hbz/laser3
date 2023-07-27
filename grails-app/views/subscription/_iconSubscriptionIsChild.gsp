<%@ page import="de.laser.interfaces.CalculatedType;" %>
<laser:serviceInjection />
%{--<g:set var="checkCons" value="${contextService.getOrg().id == subscription.getConsortia()?.id && subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION}" />--}%

<g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortia()?.id}">
    <i class="icon circular orange child la-subscriptionIsChild"></i>

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