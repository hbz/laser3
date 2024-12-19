<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">
    <%
        Map<String, Object> breadcrumbParams = [id: orgInstance.id]
    %>

    <ui:subNavItem controller="organisation" action="show" params="${breadcrumbParams}" message="org.nav.details"/>
    <ui:subNavItem controller="organisation" action="ids" params="${breadcrumbParams}" message="${isProviderOrAgency ? 'org.nav.ids' : 'org.nav.idsCids.shy'}"/>

    <g:if test="${inContextOrg}">
        <ui:subNavItem controller="organisation" action="contacts" params="${[id: institution.id]}" message="menu.institutions.publicContacts" />
    </g:if>
    <g:elseif test="${!isProviderOrAgency}">
        <ui:subNavItem controller="organisation" action="contacts" params="${breadcrumbParams}" message="menu.institutions.publicContacts" />
    </g:elseif>

    <g:if test="${!isProviderOrAgency}">
        <ui:subNavItem controller="organisation" action="readerNumber" params="${breadcrumbParams}" message="menu.institutions.readerNumbers"/>

        <g:if test="${tmplAccessPointsActive}">
            <ui:subNavItem controller="organisation" action="accessPoints" class="active" params="${breadcrumbParams}" message="org.nav.accessPoints.shy"/>
        </g:if>
        <g:else>
            <ui:subNavItem controller="organisation" action="accessPoints" params="${breadcrumbParams}" message="org.nav.accessPoints.shy"/>
        </g:else>
    </g:if>

    <ui:subNavItem controller="organisation" action="notes" params="${breadcrumbParams}" counts="${notesCount}" message="default.notes.label"/>
    <ui:subNavItem controller="organisation" action="tasks" params="${breadcrumbParams}" counts="${tasksCount}" message="menu.institutions.tasks"/>
    <ui:subNavItem controller="organisation" action="documents" params="${breadcrumbParams}" message="default.documents.label" />
    <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural"/>

    <g:if test="${!inContextOrg}">
        <ui:subNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" message="menu.institutions.myAddressbook"/>
    </g:if>
    <g:if test="${!isProviderOrAgency}">
        <g:if test="${inContextOrg}">
            <ui:securedSubNavItem instRole="INST_ADM" controller="myInstitution" action="users" message="org.nav.users.shy" />
        </g:if>
        <g:else>
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <ui:subNavItem controller="organisation" action="users" params="${breadcrumbParams}" message="org.nav.users.shy"/>
            </sec:ifAnyGranted>
        </g:else>
    </g:if>
</ui:subNav>