<%@ page import="de.laser.CustomerTypeService; de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.Platform; de.laser.Subscription; de.laser.SubscriptionPackage;" %>
<laser:serviceInjection />

<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="subscription" action="show" params="${[id:params.id]}" message="subscription.details.details.label" />

    <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
        <ui:subNavItem controller="subscription" action="members" counts="${currentMembersCounts}" params="${[id:params.id]}" message="${"subscription.details.consortiaMembers.label"}" />
    </g:if>

    %{--Custom URL mapping for re-use of index--}%
    <g:link class="item${controllerName == 'finance' ? ' active':''}" mapping="subfinance" controller="finance" action="index" params="${[sub:params.id]}">
        ${message(code:'subscription.details.financials.label')}<span class="ui floating blue circular label">${currentCostItemCounts}</span>
    </g:link>

%{--    <ui:subNavItem controller="subscription" action="reporting" params="${[id:params.id]}" message="myinst.reporting" />--}%

    <ui:subNavItem controller="subscription" action="notes" params="${[id:params.id]}" counts="${notesCount}" message="default.notes.label" />
    <ui:subNavItem controller="subscription" action="tasks" params="${[id:params.id]}" counts="${tasksCount}" message="task.plural" />
    <ui:subNavItem controller="subscription" action="documents" params="${[id:params.id]}" message="default.documents.label" />

    <ui:subNavItem controller="subscription" action="workflows" counts="${checklistCount}" params="${[id:params.id]}" message="workflow.plural"/>

</ui:subNav>
