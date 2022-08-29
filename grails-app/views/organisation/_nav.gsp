<laser:serviceInjection/>
<ui:subNav actionName="${actionName}">
    <%
        Map<String, Object> breadcrumbParams = [id: orgInstance.id]
        if(params.my)
            breadcrumbParams.my = true
    %>
    <ui:subNavItem controller="organisation" action="show" params="${breadcrumbParams}" message="org.nav.details"/>
    <ui:subNavItem controller="organisation" action="ids" params="${breadcrumbParams}" message="org.nav.ids"/>
    <g:if test="${inContextOrg}">
        <ui:subNavItem controller="organisation" action="myPublicContacts" params="${[id: institution.id]}" message="menu.institutions.publicContactsHyphen" />
    </g:if>
    <g:elseif test="${(contextCustomerType == 'ORG_CONSORTIUM' && !isProviderOrAgency)}">
        <ui:subNavItem controller="organisation" action="myPublicContacts" params="${[id: orgInstance.id]}" message="menu.institutions.publicContactsHyphen" />
    </g:elseif>
    <g:else>
        <g:if test="${!isProviderOrAgency}">
            <ui:subNavItem disabled="true" controller="organisation" action="myPublicContacts" message="menu.institutions.publicContactsHyphen" />
        </g:if>
    </g:else>
    <g:if test="${!isProviderOrAgency}">
        <ui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}" message="menu.institutions.readerNumbers"/>

        <g:if test="${tmplAccessPointsActive}">
            <ui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="accessPoints" class="active" params="${[id: orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:if>
        <g:else>
            <ui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="accessPoints" params="${[id: orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:else>
    </g:if>
    <sec:ifAnyGranted roles="ROLE_ADMIN"><!-- TODO: workflows-permissions -->
        <g:if test="${contextService.getOrg().getCustomerType() in ['ORG_CONSORTIUM']}">
            <ui:subNavItem controller="organisation" action="workflows" counts="${workflowCount}" params="${[id:params.id]}" message="workflow.plural" />
        </g:if>
    </sec:ifAnyGranted>

    <ui:securedSubNavItem controller="organisation" action="tasks" params="${breadcrumbParams}" counts="${tasksCount}" affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM" message="menu.institutions.tasks"/>
    <ui:securedSubNavItem controller="organisation" action="documents" params="${breadcrumbParams}" affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM" message="menu.my.documents" />
    <ui:subNavItem controller="organisation" action="notes" params="${breadcrumbParams}" counts="${notesCount}" message="default.notes.label"/>
    <g:if test="${!inContextOrg}">
        <ui:securedSubNavItem controller="organisation" action="addressbook" params="${breadcrumbParams}" affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM" message="menu.institutions.myAddressbook"/>
    </g:if>
    <g:if test="${!isProviderOrAgency}">
        <g:if test="${inContextOrg}">
            <ui:securedSubNavItem controller="myInstitution" action="users" message="org.nav.users" affiliation="INST_ADM"/>
        </g:if>
        <%-- deactivated as of ERMS-2934
        <g:elseif test="${ accessService.checkForeignOrgComboPermAffiliation([
                org: orgInstance,
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_ADM"
        ]) && !instAdmService.hasInstAdmin(orgInstance) }">
            <ui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
        </g:elseif>--%>
        <g:else>
            <%-- this kind of check is necessary because it should not be displayed at all if user has no specRoles --%>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                <ui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
            </sec:ifAnyGranted>
        </g:else>
        <g:if test="${inContextOrg}">
            <ui:securedSubNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}"
                                     message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        </g:if>
        <g:elseif test="${accessService.checkForeignOrgComboPermAffiliationX([
                    org: orgInstance,
                    comboPerm: "ORG_CONSORTIUM",
                    comboAffiliation: "INST_ADM",
                    specRoles: "ROLE_ORG_EDITOR, ROLE_ADMIN"
        ])}">
            <ui:subNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}" message="org.nav.options"/>
        </g:elseif>
        <g:else>
            <ui:subNavItem disabled="disabled" message="org.nav.options"/>
        </g:else>
    </g:if>
</ui:subNav>