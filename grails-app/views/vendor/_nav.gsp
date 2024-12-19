<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="vendor" action="show" params="${[id: vendor.id]}" message="org.nav.details"/>

    <ui:subNavItem controller="vendor" action="notes" params="${[id: vendor.id]}" counts="${notesCount}" message="default.notes.label"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="vendor" action="tasks" params="${[id: vendor.id]}" counts="${tasksCount}" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="vendor" action="documents" params="${[id: vendor.id]}" counts="${docsCount}" message="default.documents.label" />

    <g:if test="${workflowService.hasREAD()}">
        <ui:subNavItem controller="vendor" action="workflows" counts="${checklistCount}" params="${[id: vendor.id]}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="vendor" action="workflows" counts="${checklistCount}" params="${[id: vendor.id]}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

    <g:if test="${contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="vendor" action="addressbook" params="${[id: vendor.id]}" message="menu.institutions.myAddressbook"/>
    </g:if>
</ui:subNav>