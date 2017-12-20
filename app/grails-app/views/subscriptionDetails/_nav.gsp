<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="subscriptionDetails" action="details" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.details.label" />
    <semui:subNavItem controller="subscriptionDetails" action="index" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.current_ent" />
    <semui:subNavItem controller="subscriptionDetails" action="tasks" params="${[id:params.id, shortcode: (params.shortcode ?: null)]}" message="task.plural" />

    <!-- <semui:subNavItem controller="subscriptionDetails" action="renewals" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.renewals.label" />-->
    <!--
        <semui:subNavItem controller="subscriptionDetails" action="previous" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.previous.label" />
        <semui:subNavItem controller="subscriptionDetails" action="expected" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.expected.label" />

        <g:if test="${grailsApplication.config.feature_finance}">
            <semui:subNavItem controller="subscriptionDetails" action="costPerUse" params="${[id:params.id, shortcode:(params.shortcode ?: null)]}" message="subscription.details.costPerUse.label" />
        </g:if>
    -->

    <semui:subNavItem controller="subscriptionDetails" action="documents" params="${[id:params.id, shortcode: (params.shortcode ?: null)]}" message="default.documents.label" />
    <semui:subNavItem controller="subscriptionDetails" action="notes" params="${[id:params.id, shortcode: (params.shortcode ?: null)]}" message="default.notes.label" />

    <g:if test="${grailsApplication.config.feature_finance}">
    %{--Custom URL mapping for re-use of index--}%
        <g:link class="item" mapping="subfinance" controller="finance" action="index" params="${[sub:params.id, shortcode: (params.shortcode ?: null)]}">${message(code:'subscription.details.financials.label', default:'Subscription Financials')}</g:link>
    </g:if>
    <semui:subNavItem controller="subscriptionDetails" action="todoHistory" params="${[id:params.id, shortcode: (params.shortcode ?: null)]}" message="license.nav.todo_history" />
    <semui:subNavItem controller="subscriptionDetails" action="editHistory" params="${[id:params.id, shortcode: (params.shortcode ?: null)]}" message="license.nav.edit_history" />
    <semui:subNavItem controller="subscriptionDetails" action="permissionInfo" params="${[id:params.id, shortcode: (params.shortcode ?: null)]}" message="default.permissionInfo.label" />
</semui:subNav>
