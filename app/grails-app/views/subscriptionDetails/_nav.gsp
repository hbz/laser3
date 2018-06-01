<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="subscriptionDetails" action="show" params="${[id:params.id]}" message="subscription.details.details.label" />

    <g:if test="${controllerName != 'finance'}">%{-- template is used by subscriptionDetails/* and finance/index --}%
        <semui:subNavItem controller="subscriptionDetails" action="index" params="${[id:params.id]}" message="subscription.details.current_ent" />
    </g:if>
    <g:else>%{-- prevent two active items with action 'index' due url mapping 'subfinance' --}%
        <g:link controller="subscriptionDetails" action="index" params="${[id:params.id]}" class="item">${message('code': 'subscription.details.current_ent')}</g:link>
    </g:else>

    <g:if test="${(subscriptionInstance?.getConsortia()?.id == contextService.getOrg()?.id) && !subscriptionInstance.instanceOf}">
        <semui:subNavItem controller="subscriptionDetails" action="members" params="${[id:params.id]}" message="subscription.details.members.label" />
    </g:if>

    <semui:subNavItem controller="subscriptionDetails" action="tasks" params="${[id:params.id]}" message="task.plural" />

    <%-- <semui:subNavItem controller="subscriptionDetails" action="renewals" params="${[id:params.id]}" message="subscription.details.renewals.label" /> --%>
    <%--
        <semui:subNavItem controller="subscriptionDetails" action="previous" params="${[id:params.id]}" message="subscription.details.previous.label" />
        <semui:subNavItem controller="subscriptionDetails" action="expected" params="${[id:params.id]}" message="subscription.details.expected.label" />
    --%>
    <%--
        <g:if test="${grailsApplication.config.feature_finance}">
            <semui:subNavItem controller="subscriptionDetails" action="costPerUse" params="${[id:params.id]}" message="subscription.details.costPerUse.label" />
        </g:if>
    --%>

    <semui:subNavItem controller="subscriptionDetails" action="documents" params="${[id:params.id]}" message="default.documents.label" />
    <semui:subNavItem controller="subscriptionDetails" action="notes" params="${[id:params.id]}" message="default.notes.label" />

    <g:if test="${grailsApplication.config.feature_finance}">
    %{--Custom URL mapping for re-use of index--}%

        <g:link class="item${controllerName == 'finance' ? ' active':''}" mapping="subfinance" controller="finance" action="index" params="${[sub:params.id]}">
            ${message(code:'subscription.details.financials.label', default:'Subscription Financials')}
        </g:link>

    </g:if>

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="subscriptionDetails" action="changes" params="${[id:params.id]}" class="la-role-admin" message="license.nav.todo_history" />
        <semui:subNavItem controller="subscriptionDetails" action="history" params="${[id:params.id]}" class="la-role-admin" message="license.nav.edit_history" />
        <semui:subNavItem controller="subscriptionDetails" action="permissionInfo" params="${[id:params.id]}" class="la-role-admin" message="default.permissionInfo.label" />
    </sec:ifAnyGranted>
</semui:subNav>
