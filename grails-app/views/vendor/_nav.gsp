<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="vendor" action="show" params="${breadcrumbParams}" message="org.nav.details"/>
    <ui:subNavItem controller="vendor" action="ids" params="${breadcrumbParams}" message="org.nav.ids"/>

    <ui:subNavItem controller="organisation" action="notes" params="${breadcrumbParams}" counts="${notesCount}" message="default.notes.label"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="organisation" action="tasks" params="${breadcrumbParams}" counts="${tasksCount}" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="organisation" action="documents" params="${breadcrumbParams}" message="default.documents.label" />

    <g:if test="${contextService.getOrg().isCustomerType_Pro()}"><!-- TODO: workflows-permissions -->
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

    <g:if test="${contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" message="menu.institutions.myAddressbook"/>
    </g:if>
    <ui:subNavItem controller="subscription" action="notes" params="${[id:params.id]}" counts="${notesCount}" message="default.notes.label" />
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="subscription" action="tasks" params="${[id:params.id]}" counts="${tasksCount}" message="task.plural" />
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="subscription" action="documents" params="${[id:params.id]}" counts="${docsCount}" message="default.documents.label" />
</ui:subNav>