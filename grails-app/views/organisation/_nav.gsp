<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">
    <%
        Map<String, Object> breadcrumbParams = [id: orgInstance.id]
    %>

    <ui:subNavItem controller="organisation" action="show" params="${breadcrumbParams}" message="org.nav.details"/>
    <ui:subNavItem controller="organisation" action="ids" params="${breadcrumbParams}" message="${isProviderOrAgency ? 'org.nav.ids' : 'org.nav.idsCidsHyphen'}"/>
    <g:if test="${inContextOrg}">
        <ui:subNavItem controller="organisation" action="myPublicContacts" params="${[id: institution.id]}" message="menu.institutions.publicContactsHyphen" />
    </g:if>
    <g:elseif test="${(customerTypeService.isConsortium( contextCustomerType ) && !isProviderOrAgency)}">
        <ui:subNavItem controller="organisation" action="myPublicContacts" params="${breadcrumbParams}" message="menu.institutions.publicContactsHyphen" />
    </g:elseif>
    <g:else>
        <g:if test="${!isProviderOrAgency}">
            <ui:subNavItem disabled="true" controller="organisation" action="myPublicContacts" message="menu.institutions.publicContactsHyphen" />
        </g:if>
    </g:else>
    <g:if test="${!isProviderOrAgency}">
        <ui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="readerNumber" params="${breadcrumbParams}" message="menu.institutions.readerNumbers"/>

        <g:if test="${tmplAccessPointsActive}">
            <ui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="accessPoints" class="active" params="${breadcrumbParams}" message="org.nav.accessPoints"/>
        </g:if>
        <g:else>
            <ui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="accessPoints" params="${breadcrumbParams}" message="org.nav.accessPoints"/>
        </g:else>
    </g:if>
    <g:if test="${(inContextOrg || isProviderOrAgency) && workflowService.hasUserPerm_read()}"><!-- TODO: workflows-permissions -->
        <ui:subNavItem controller="organisation" action="workflows" counts="${checklistCount}" params="${breadcrumbParams}" message="workflow.plural" />
    </g:if>

    <ui:securedSubNavItem controller="organisation" action="tasks" params="${breadcrumbParams}" counts="${tasksCount}" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem controller="organisation" action="documents" params="${breadcrumbParams}" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" message="menu.my.documents" />
    <ui:subNavItem controller="organisation" action="notes" params="${breadcrumbParams}" counts="${notesCount}" message="default.notes.label"/>
    <g:if test="${!inContextOrg && contextCustomerType in ['ORG_INST_PRO','ORG_CONSORTIUM_BASIC','ORG_CONSORTIUM_PRO']}">
        <ui:subNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" message="menu.institutions.myAddressbook"/>
    </g:if>
    <g:if test="${!isProviderOrAgency}">
        <g:if test="${inContextOrg}">
            <ui:securedSubNavItem controller="myInstitution" action="users" message="org.nav.users" affiliation="INST_ADM"/>
        </g:if>
        <%-- deactivated as of ERMS-2934
        <g:elseif test="${ accessService.checkForeignOrgComboPermAffiliation([
                org: orgInstance,
                comboPerm: "ORG_CONSORTIUM_BASIC",
                comboAffiliation: "INST_ADM"
        ]) && !instAdmService.hasInstAdmin(orgInstance) }">
            <ui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
        </g:elseif>--%>
        <g:else>
            <%-- this kind of check is necessary because it should not be displayed at all if user has no specRoles --%>
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <ui:subNavItem controller="organisation" action="users" params="${breadcrumbParams}" message="org.nav.users"/>
            </sec:ifAnyGranted>
        </g:else>
        <g:if test="${inContextOrg}">
            <ui:securedSubNavItem controller="organisation" action="settings" params="${breadcrumbParams}"
                                     message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        </g:if>
        <g:elseif test="${accessService.checkForeignOrgComboPermAffiliationX([
                    org: orgInstance,
                    comboPerm: "ORG_CONSORTIUM_BASIC",
                    comboAffiliation: "INST_ADM",
                    specRoles: "ROLE_ADMIN"
        ])}">
            <ui:subNavItem controller="organisation" action="settings" params="${breadcrumbParams}" message="org.nav.options"/>
        </g:elseif>
        <g:else>
            <ui:subNavItem disabled="disabled" message="org.nav.options"/>
        </g:else>
    </g:if>
</ui:subNav>