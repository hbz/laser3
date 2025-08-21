<%@ page import="de.laser.CustomerTypeService; de.laser.IssueEntitlement; de.laser.storage.RDStore; de.laser.wekb.Platform; de.laser.Subscription; de.laser.SubscriptionPackage;" %>
<laser:serviceInjection />

%{--<pre>--}%
%{--    _nav.gsp--}%
%{--    contextOrg: ${contextOrg}--}%
%{--    institution: ${institution}--}%
%{--    orgInstance: ${orgInstance}--}%
%{--    inContextOrg: ${inContextOrg}--}%
%{--</pre>--}%

<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="subscription" action="show" params="${[id:params.id]}" message="subscription.details.details.label" />

    <g:if test="${controllerName != 'finance'}">%{-- template is used by subscriptionDetails/* and finance/index --}%
        <ui:subNavItem controller="subscription" counts="${subscription.packages.size()}" action="index" params="${[id:params.id]}" message="subscription.details.current_ent" />
    </g:if>
    <g:else>%{-- prevent two active items with action 'index' due url mapping 'subfinance' --}%
        <g:link controller="subscription" action="index" params="${[id:params.id]}" class="item">
            ${message('code': 'subscription.details.current_ent')} <ui:bubble float="true" count="${subscription.packages.size()}"/>
        </g:link>
    </g:else>

    <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
        <ui:subNavItem controller="subscription" action="members" counts="${currentMembersCounts}" params="${[id:params.id]}" message="subscription.details.consortiaMembers.label" />
    </g:if>

    %{--Custom URL mapping for re-use of index--}%
    <g:link class="item${controllerName == 'finance' ? ' active':''}" mapping="subfinance" controller="finance" action="index" params="${[sub:params.id]}">
        ${message(code:'subscription.details.financials.label')} <ui:bubble float="true" count="${currentCostItemCounts}"/>
    </g:link>

        <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
            <ui:securedSubNavItem orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="subscription" action="surveysConsortia" counts="${currentSurveysCounts}" params="${[id:params.id]}" message="subscription.details.surveys.label" />
        </g:if>
        <g:if test="${(contextService.getOrg().isCustomerType_Consortium_Pro() && subscription.instanceOf)}">
            <ui:securedSubNavItem orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="subscription" action="surveys" counts="${currentSurveysCounts}" params="${[id:params.id]}" message="subscription.details.surveys.label" />
        </g:if>
        <g:if test="${contextService.getOrg().isCustomerType_Inst() && subscription.type == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL}">
            <ui:securedSubNavItem orgPerm="${CustomerTypeService.ORG_INST_BASIC}" controller="subscription" action="surveys" counts="${currentSurveysCounts}" params="${[id:params.id]}" message="subscription.details.surveys.label" />
        </g:if>

        <g:if test="${subscription.packages}">
            <g:if test="${subscriptionService.areStatsAvailable(subscription)}">
                <ui:subNavItem controller="subscription" action="stats" params="${[id:params.id]}" message="default.stats.label" />
            </g:if>
            <g:else>
                <ui:subNavItem message="default.stats.label" tooltip="${message(code: 'default.stats.noStatsForSubscription')}" disabled="disabled" />
            </g:else>
        </g:if>
        <g:else>
            <ui:subNavItem message="default.stats.label" tooltip="${message(code: 'default.stats.noPackage')}" disabled="disabled" />
        </g:else>

%{--        <g:if test="${contextService.getOrg().isCustomerType_Pro()}">--}%
            <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="subscription" action="reporting" params="${[id:params.id]}" message="myinst.reporting" />
%{--        </g:if>--}%

        <ui:subNavItem controller="subscription" action="notes" params="${[id:params.id]}" counts="${notesCount}" message="default.notes.label" />
        <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="subscription" action="tasks" params="${[id:params.id]}" counts="${tasksCount}" message="task.plural" />
        <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="subscription" action="documents" params="${[id:params.id]}" counts="${docsCount}" message="default.documents.label" />

        <g:if test="${workflowService.hasREAD()}">
            <ui:subNavItem controller="subscription" action="workflows" counts="${checklistCount}" params="${[id:params.id]}" message="workflow.plural"/>
        </g:if>
        <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
            <ui:subNavItem controller="subscription" action="workflows" counts="${checklistCount}" params="${[id:params.id]}" message="workflow.plural" disabled="disabled"/>
        </g:elseif>

        <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
            <ui:securedSubNavItem orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="subscription" action="subTransfer" params="${[id:params.id]}" message="subscription.details.subTransfer.label" />
        </g:if>

</ui:subNav>
