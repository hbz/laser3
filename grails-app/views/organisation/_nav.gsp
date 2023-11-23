<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">
    <%
        Map<String, Object> breadcrumbParams = [id: orgInstance.id]
    %>

    <ui:subNavItem controller="organisation" action="show" params="${breadcrumbParams}" message="org.nav.details"/>
    <ui:subNavItem controller="organisation" action="ids" params="${breadcrumbParams}" message="${isProviderOrAgency ? 'org.nav.ids' : 'org.nav.idsCids.shy'}"/>
    <g:if test="${inContextOrg}">
        <ui:subNavItem controller="organisation" action="myPublicContacts" params="${[id: institution.id]}" message="menu.institutions.publicContacts.shy" />
    </g:if>
    <g:elseif test="${(customerTypeService.isConsortium( contextCustomerType ) && !isProviderOrAgency)}">
        <ui:subNavItem controller="organisation" action="myPublicContacts" params="${breadcrumbParams}" message="menu.institutions.publicContacts.shy" />
    </g:elseif>
    <g:else>
        <g:if test="${!isProviderOrAgency}">
            <ui:subNavItem controller="organisation" action="myPublicContacts" message="menu.institutions.publicContacts.shy" disabled="true" />
        </g:if>
    </g:else>
    <g:if test="${!isProviderOrAgency}">
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
    <ui:securedSubNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="organisation" action="documents" params="${breadcrumbParams}" message="default.documents.label" />

    <g:if test="${contextService.getOrg().isCustomerType_Pro()}"><!-- TODO: workflows-permissions -->
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural"/>
    </g:if>
    <g:elseif test="${contextService.getOrg().isCustomerType_Basic()}">
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural" disabled="disabled"/>
    </g:elseif>

    <g:if test="${!inContextOrg && contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" message="menu.institutions.myAddressbook"/>
    </g:if>
    <g:if test="${!isProviderOrAgency}">
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
        <g:elseif test="${accessService.otherOrgAndComboCheckPermAffiliation_or_ROLEADMIN(orgInstance, CustomerTypeService.ORG_CONSORTIUM_BASIC, 'INST_ADM')}">
            <ui:subNavItem controller="organisation" action="settings" params="${breadcrumbParams}" message="org.nav.dataTransfer.shy"/>
        </g:elseif>
        <g:else>
            <ui:subNavItem message="org.nav.dataTransfer.shy" disabled="disabled" />
        </g:else>
    </g:if>
</ui:subNav>