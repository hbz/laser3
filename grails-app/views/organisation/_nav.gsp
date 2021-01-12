<laser:serviceInjection/>
<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="organisation" action="show" params="${[id: orgInstance.id]}" message="org.nav.details"/>
    <semui:subNavItem controller="organisation" action="ids" params="${[id: orgInstance.id]}" message="org.nav.ids"/>
    <g:if test="${inContextOrg}">
        <semui:subNavItem controller="organisation" action="myPublicContacts" params="${[id: institution.id]}" message="menu.institutions.publicContacts" />
    </g:if>
    <g:elseif test="${(contextCustomerType == 'ORG_CONSORTIUM' && !isProviderOrAgency)}">
        <semui:subNavItem controller="organisation" action="myPublicContacts" params="${[id: orgInstance.id]}" message="menu.institutions.publicContacts" />
    </g:elseif>
    <g:else>
        <g:if test="${!isProviderOrAgency}">
            <semui:subNavItem disabled="true" controller="organisation" action="myPublicContacts" message="menu.institutions.publicContacts" />
        </g:if>
    </g:else>
    <g:if test="${!isProviderOrAgency}">
        <semui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}" message="menu.institutions.readerNumbers"/>

        <g:if test="${tmplAccessPointsActive}">
            <semui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="accessPoints" class="active" params="${[id: orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:if>
        <g:else>
            <semui:securedSubNavItem affiliation="INST_USER" controller="organisation" action="accessPoints" params="${[id: orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:else>
    </g:if>
    <semui:securedSubNavItem controller="organisation" action="tasks" params="${[id: orgInstance.id]}" affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM" message="menu.institutions.tasks"/>
    <semui:securedSubNavItem controller="organisation" action="documents" params="${[id: orgInstance.id]}" affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM" message="menu.my.documents" />
    <semui:subNavItem controller="organisation" action="notes" params="${[id: orgInstance.id]}" message="default.notes.label"/>
    <g:if test="${!inContextOrg}">
        <semui:securedSubNavItem controller="organisation" action="addressbook" params="${[id: orgInstance.id]}" affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM" message="menu.institutions.myAddressbook"/>
    </g:if>
    <g:if test="${!isProviderOrAgency}">
        <g:if test="${inContextOrg}">
            <semui:securedSubNavItem controller="myInstitution" action="userList" message="org.nav.users" affiliation="INST_ADM"/>
        </g:if>
        <%-- deactivated as of ERMS-2934
        <g:elseif test="${ accessService.checkForeignOrgComboPermAffiliation([
                org: orgInstance,
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_ADM"
        ]) && !instAdmService.hasInstAdmin(orgInstance) }">
            <semui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
        </g:elseif>--%>
        <g:else>
            <%-- this kind of check is necessary because it should not be displayed at all if user has no specRoles --%>
            <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
                <semui:subNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}" message="org.nav.users"/>
            </sec:ifAnyGranted>
        </g:else>
        <g:if test="${inContextOrg}">
            <semui:securedSubNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}"
                                     message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        </g:if>
        <g:elseif test="${accessService.checkForeignOrgComboPermAffiliationX([
                    org: orgInstance,
                    comboPerm: "ORG_INST_COLLECTIVE, ORG_CONSORTIUM",
                    comboAffiliation: "INST_ADM",
                    specRoles: "ROLE_ORG_EDITOR, ROLE_ADMIN"
        ])}">
            <semui:subNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}" message="org.nav.options"/>
        </g:elseif>
        <g:else>
            <semui:subNavItem disabled="disabled" message="org.nav.options"/>
        </g:else>
    </g:if>
</semui:subNav>