<%@ page import="de.laser.SubscriptionPackage; de.laser.IssueEntitlement; de.laser.helper.RDStore; de.laser.Platform; de.laser.Subscription" %>
<laser:serviceInjection />

<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="subscription" action="show" params="${[id:params.id]}" message="subscription.details.details.label" />

    <g:if test="${controllerName != 'finance'}">%{-- template is used by subscriptionDetails/* and finance/index --}%
        <semui:subNavItem controller="subscription" counts="${currentTitlesCounts}" action="index" params="${[id:params.id]}" message="subscription.details.current_ent" />
    </g:if>
    <g:else>%{-- prevent two active items with action 'index' due url mapping 'subfinance' --}%
        <g:link controller="subscription" action="index" params="${[id:params.id]}" class="item">${message('code': 'subscription.details.current_ent')}<div class="ui floating blue circular label">${currentTitlesCounts}</div></g:link>
    </g:else>

    <semui:subNavItem controller="subscription" action="entitlementChanges" params="${[id:params.id]}" message="myinst.menu.changes.label" />

    <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
        <semui:subNavItem controller="subscription" action="members" counts="${currentMembersCounts}" params="${[id:params.id]}" message="${"subscription.details.consortiaMembers.label"}" />
        %{-- <semui:subNavItem controller="subscription" action="pendingChanges" params="${[id:params.id]}" message="pendingChange.plural" /> --}%
    </g:if>

    %{--Custom URL mapping for re-use of index--}%
    <g:link class="item${controllerName == 'finance' ? ' active':''}" mapping="subfinance" controller="finance" action="index" params="${[sub:params.id]}">
        ${message(code:'subscription.details.financials.label')}<div class="ui floating blue circular label">${currentCostItemCounts}</div>
    </g:link>

    <g:if test="${showConsortiaFunctions && !subscription.instanceOf}">
        <semui:securedSubNavItem orgPerm="ORG_CONSORTIUM" controller="subscription" action="surveysConsortia" counts="${currentSurveysCounts}" params="${[id:params.id]}" message="subscription.details.surveys.label" />
    </g:if>
    <g:if test="${((contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM']) && subscription.instanceOf)}">
        <semui:securedSubNavItem orgPerm="ORG_CONSORTIUM" controller="subscription" action="surveys" counts="${currentSurveysCounts}" params="${[id:params.id]}" message="subscription.details.surveys.label" />
    </g:if>
    <g:if test="${((contextService.getOrg().getCustomerType() in ['ORG_INST', 'ORG_BASIC_MEMBER']) || params.orgBasicMemberView)&& subscription?.type == de.laser.helper.RDStore.SUBSCRIPTION_TYPE_CONSORTIAL}">
        <semui:securedSubNavItem orgPerm="ORG_BASIC_MEMBER" controller="subscription" action="surveys" counts="${currentSurveysCounts}" params="${[id:params.id]}" message="subscription.details.surveys.label" />
    </g:if>
    <g:if test="${subscription.packages}">
        <%
            Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: subscription])
            if(!subscribedPlatforms) {
                subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription", [subscription: subscription])
            }
            Set<Long> reportingInstitutions = [institution.id]
            reportingInstitutions.addAll(Subscription.executeQuery('select oo.org.id from OrgRole oo join oo.sub s where s.instanceOf = :subscription and oo.roleType in (:roleTypes)', [subscription: subscription, roleTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]))
            boolean statsAvailable = subscriptionService.areStatsAvailable(subscribedPlatforms, subscription.packages.collect { SubscriptionPackage sp -> sp.pkg.id }, reportingInstitutions)
        %>
        <g:if test="${statsAvailable}">
            <semui:subNavItem controller="subscription" action="stats" params="${[id:params.id]}" message="default.stats.label" />
        </g:if>
        <g:else>
            <semui:subNavItem disabled="disabled" message="default.stats.label" tooltip="${message(code: 'default.stats.noStatsForSubscription')}"/>
        </g:else>
    </g:if>
    <g:else>
        <semui:subNavItem disabled="disabled" message="default.stats.label" tooltip="${message(code: 'default.stats.noPackage')}"/>
    </g:else>


    <g:if test="${contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM', 'ORG_INST']}">
        <semui:subNavItem controller="subscription" action="reporting" params="${[id:params.id]}" message="myinst.reporting" />
    </g:if>
    <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: reporting-permissions -->
        <g:if test="${contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM']}">
            <semui:subNavItem controller="subscription" action="workflows" counts="${workflowCount}" params="${[id:params.id]}" message="workflow.plural" />
        </g:if>
    </sec:ifAnyGranted>

    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="subscription" action="tasks" params="${[id:params.id]}" message="task.plural" />
    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="subscription" action="documents" params="${[id:params.id]}" message="default.documents.label" />
    <semui:subNavItem controller="subscription" action="notes" params="${[id:params.id]}" message="default.notes.label" />

    <%--
    <semui:subNavItem controller="subscription" action="changes" params="${[id:params.id]}" message="license.nav.todo_history" />

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="subscription" action="history" params="${[id:params.id]}" class="la-role-admin" message="license.nav.edit_history" />
    </sec:ifAnyGranted>
    --%>
</semui:subNav>
