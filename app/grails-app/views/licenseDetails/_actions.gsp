<g:if test="${editable}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="subscriptionDetails" action="linkPackage" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.linkPackage.label" />
        <semui:actionsDropdownItem controller="subscriptionDetails" action="addEntitlements" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.addEntitlements.label" />
        <semui:actionsDropdownItem controller="subscriptionDetails" action="renewals" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.renewals.label" />
    </semui:actionsDropdown>

    <% /* <semui:crumbAsBadge message="default.editable" class="orange" /> */ %>
</g:if>