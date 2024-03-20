<laser:serviceInjection />

<g:if test="${subscription.instanceOf && contextService.getOrg().id == subscription.getConsortia()?.id}">
    <i class="icon circular orange child la-subscriptionIsChild"></i>
</g:if>