<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="provider" action="show" message="org.nav.details"/>

    <ui:subNavItem controller="provider" action="notes" counts="${notesCount}" message="default.notes.label"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="provider" action="tasks" counts="${tasksCount}" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="provider" action="documents" message="default.documents.label" />

    <g:if test="${contextService.getOrg().isCustomerType_Pro()}"><!-- TODO: workflows-permissions -->
        <ui:subNavItem controller="provider" action="workflows" counts="${checklistCount}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="provider" action="workflows" counts="${checklistCount}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

    <g:if test="${contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" message="menu.institutions.myAddressbook"/>
    </g:if>
</ui:subNav>