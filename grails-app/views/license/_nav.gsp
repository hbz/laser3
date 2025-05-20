<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{--<pre>--}%
%{--    _nav.gsp--}%
%{--    contextOrg: ${contextOrg}--}%
%{--    institution: ${institution}--}%
%{--    orgInstance: ${orgInstance}--}%
%{--    inContextOrg: ${inContextOrg}--}%
%{--</pre>--}%

<g:set var="license" value="${de.laser.License.get(params.id)}"/>

<ui:subNav actionName="${actionName}">
    <ui:subNavItem controller="license" action="show" params="${[id:params.id]}" message="license.nav.details" />
    <ui:subNavItem controller="license" action="linkedSubs" params="${[id:params.id]}" message="subscription.plural" counts="${subsCount}"/>
    <g:if test="${license._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}">
        <ui:subNavItem controller="license" action="members" params="${[id:params.id]}" text="${message(code:'license.details.incoming.childs')}"/>
    </g:if>

    <ui:subNavItem controller="license" action="notes" params="${[id:params.id]}" counts="${notesCount}" message="license.nav.notes" />
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="license" action="tasks" params="${[id:params.id]}" counts="${tasksCount}" message="task.plural" />
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="license" action="documents" params="${[id:params.id]}" counts="${docsCount}" message="license.nav.docs" />

    <g:if test="${workflowService.hasREAD()}">
        <ui:subNavItem controller="license" action="workflows" counts="${checklistCount}" params="${[id:params.id]}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="license" action="workflows" counts="${checklistCount}" params="${[id:params.id]}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

</ui:subNav>