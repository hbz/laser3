<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${editable}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="subscriptionDetails" action="linkPackage" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.linkPackage.label" />
        <semui:actionsDropdownItem controller="subscriptionDetails" action="addEntitlements" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.addEntitlements.label" />

        <g:if test="${(subscriptionInstance?.getConsortia()?.id == contextService.getOrg()?.id) && !subscriptionInstance.instanceOf}">
            <semui:actionsDropdownItem controller="subscriptionDetails" action="addMembers" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.addMembers.label" />
        </g:if>

        <semui:actionsDropdownItem controller="subscriptionDetails" action="renewals" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.renewals.label" />
    </semui:actionsDropdown>
</g:if>
<g:if test="${actionName == 'index'}">
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" params="${params + [format:'json']}">JSON</g:link>
        </semui:exportDropdownItem>
        <semui:exportDropdownItem>
            <g:link class="item" controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}" params="${params + [format:'xml']}">XML</g:link>
        </semui:exportDropdownItem>
        <g:each in="${transforms}" var="transkey,transval">
            <semui:exportDropdownItem>
                <g:link class="item" action="index" id="${params.id}" params="${[format:'xml', transformId:transkey, mode: params.mode]}">${transval.name}</g:link>
            </semui:exportDropdownItem>
        </g:each>
    </semui:exportDropdown>
</g:if>