<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService" %>
<laser:serviceInjection/>

%{--<pre>--}%
%{--    _nav.gsp--}%
%{--    contextOrg: ${contextOrg}--}%
%{--    institution: ${institution}--}%
%{--    orgInstance: ${orgInstance}--}%
%{--    inContextOrg: ${inContextOrg}--}%
%{--    institutionalView: ${institutionalView}--}%
%{--    consortialView: ${consortialView}--}%
%{--</pre>--}%

<ui:subNav actionName="${actionName}">
    <%
        Map<String, Object> breadcrumbParams = [id: orgInstance.id]
    %>

    <g:if test="${orgInstance.isInfoAccessible()}">
%{--            <ui:subNavItem controller="organisation" action="info" params="${breadcrumbParams}" message="org.nav.info"/>--}%
        <g:link controller="org" action="dataviz" params="${breadcrumbParams}" class="item ${actionName == 'dataviz' ? 'active' : ''}" role="tab"><i class="${Icon.DATA_DASHBOARD} circular" style="margin:0"></i></g:link>
    </g:if>

    <ui:subNavItem controller="organisation" action="show" params="${breadcrumbParams}" message="org.nav.details"/>
    <ui:subNavItem controller="organisation" action="ids" params="${breadcrumbParams}" message="${isProviderOrAgency ? 'org.nav.ids' : 'org.nav.idsCids.shy'}"/>
    <g:if test="${inContextOrg}">
        <ui:subNavItem controller="organisation" action="contacts" params="${[id: institution.id]}" message="menu.institutions.publicContacts" />
    </g:if>
    <g:elseif test="${(customerTypeService.isConsortium( contextCustomerType ))}">
        <ui:subNavItem controller="organisation" action="contacts" params="${breadcrumbParams}" message="menu.institutions.publicContacts" />
    </g:elseif>
    <g:else>
        <ui:subNavItem controller="organisation" action="contacts" message="menu.institutions.publicContacts" disabled="true" />
    </g:else>
    <g:if test="${orgInstance.getCustomerType() in [CustomerTypeService.ORG_INST_PRO,CustomerTypeService.ORG_INST_BASIC]}">
        <ui:securedSubNavItem controller="organisation" action="readerNumber" params="${breadcrumbParams}" message="menu.institutions.readerNumbers"/>

        <g:if test="${tmplAccessPointsActive}">
            <ui:securedSubNavItem controller="organisation" action="accessPoints" class="active" params="${breadcrumbParams}" message="org.nav.accessPoints.shy"/>
        </g:if>
        <g:else>
            <ui:securedSubNavItem controller="organisation" action="accessPoints" params="${breadcrumbParams}" message="org.nav.accessPoints.shy"/>
        </g:else>
    </g:if>

    <ui:subNavItem controller="organisation" action="notes" params="${breadcrumbParams}" counts="${notesCount}" message="default.notes.label"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="organisation" action="tasks" params="${breadcrumbParams}" counts="${tasksCount}" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="organisation" action="documents" params="${breadcrumbParams}" counts="${docsCount}" message="default.documents.label" />

    <g:if test="${workflowService.hasREAD()}">
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

    <g:if test="${!inContextOrg && contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" message="menu.institutions.myAddressbook"/>
    </g:if>
    <g:if test="${inContextOrg}">
        <ui:securedSubNavItem instRole="INST_ADM" controller="myInstitution" action="users" message="org.nav.users.shy" />
    </g:if>
    <g:else>
    <%-- this kind of check is necessary because it should not be displayed at all if user has no specRole --%>
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <ui:subNavItem controller="organisation" action="users" params="${breadcrumbParams}" message="org.nav.users.shy"/>
        </sec:ifAnyGranted>
    </g:else>
    <g:if test="${inContextOrg}">
        <ui:securedSubNavItem instRole="INST_ADM" affiliationOrg="${orgInstance}"
                              controller="organisation" action="settings" params="${breadcrumbParams}" message="org.nav.dataTransfer.shy" />
    </g:if>
    <%-- to exclude other consortium admins --%>
    <g:elseif test="${contextService.getUser().isComboInstAdminOf(orgInstance) && !orgInstance.hasInstAdminEnabled()}">
        <ui:subNavItem controller="organisation" action="settings" params="${breadcrumbParams}" message="org.nav.dataTransfer.shy"/>
    </g:elseif>
    <g:elseif test="${contextService.getUser().isAdmin() || contextService.getUser().isYoda()}">
        <ui:subNavItem controller="organisation" action="settings" params="${breadcrumbParams}" message="org.nav.dataTransfer.shy"/>
    </g:elseif>
</ui:subNav>