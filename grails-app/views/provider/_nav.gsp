<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="provider" action="show" params="${[id: provider.id]}" message="org.nav.details"/>

    <ui:subNavItem controller="provider" action="notes" params="${[id: provider.id]}" counts="${notesCount}" message="default.notes.label"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="provider" action="tasks" params="${[id: provider.id]}" counts="${tasksCount}" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="provider" params="${[id: provider.id]}" action="documents" counts="${docsCount}" message="default.documents.label" />

    <g:if test="${workflowService.hasREAD()}">
        <ui:subNavItem controller="provider" action="workflows" counts="${checklistCount}" params="${[id: provider.id]}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="provider" action="workflows" counts="${checklistCount}" params="${[id: provider.id]}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

    <g:if test="${contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="provider" action="addressbook" params="${[id: provider.id]}" message="menu.institutions.myAddressbook"/>
    </g:if>
</ui:subNav>