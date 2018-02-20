<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${editable}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="subscriptionDetails" action="linkPackage" params="${[id:params.id]}" message="subscription.details.linkPackage.label" />
        <semui:actionsDropdownItem controller="subscriptionDetails" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />

        <g:if test="${(subscriptionInstance?.getConsortia()?.id == contextService.getOrg()?.id) && !subscriptionInstance.instanceOf}">
            <semui:actionsDropdownItem controller="subscriptionDetails" action="addMembers" params="${[id:params.id]}" message="subscription.details.addMembers.label" />
        </g:if>

        <semui:actionsDropdownItem controller="subscriptionDetails" action="renewals" params="${[id:params.id]}" message="subscription.details.renewals.label" />
    </semui:actionsDropdown>
</g:if>