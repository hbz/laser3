<laser:serviceInjection />

<g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortia()?.id}">
    <i class="icon circular orange child la-subscriptionIsChild"></i>

%{--<g:if test="${! AppUtils.isPreviewOnly()}">--}%
    <laser:script file="${this.getGroovyPageFileName()}">
      $(document).ready(function() {
        $('.la-subscriptionIsChild').visibility({
          type   : 'fixed',
          offset : 55,
          zIndex: 101
        })
      })
    </laser:script>
%{--</g:if>--}%
</g:if>