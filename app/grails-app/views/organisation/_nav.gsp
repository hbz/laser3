<laser:serviceInjection />

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="organisation" action="show" params="${[id: orgInstance.id]}" message="org.nav.details"/>
    <g:if test="${inContextOrg}">
        <semui:subNavItem controller="myInstitution" action="myPublicContacts" message="menu.institutions.publicContacts" />
    </g:if>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">

        <g:if test="${accessService.checkForeignOrgComboPermAffiliationX([
                org: orgInstance,
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_EDITOR",
                specRoles: "ROLE_ORG_EDITOR,ROLE_ADMIN"])}">

                <semui:subNavItem controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}"
                          message="menu.institutions.readerNumbers"/>
        </g:if>
        <g:else>
            <semui:subNavItem controller="organisation" action="readerNumber" params="${[id: orgInstance.id]}"
                              message="menu.institutions.readerNumbers" disabled="disabled" />
        </g:else>
    </g:if>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">
        <semui:securedSubNavItem controller="organisation" action="users" params="${[id: orgInstance.id]}"
                                     message="org.nav.users" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
    </g:if>

    <%-- TODO: check ctx != foreign org --%>
    <semui:securedSubNavItem controller="organisation" action="settings" params="${[id: orgInstance.id]}"
                             orgPerm="ORG_BASIC_MEMBER,ORG_CONSORTIUM" specRole="ROLE_ADMIN,ROLE_ORG_EDITOR"
                             affiliation="INST_ADM" affiliationOrg="${orgInstance}"
                             message="org.nav.options" />

    <semui:securedSubNavItem controller="organisation" action="documents" params="${[id: orgInstance.id]}"
                             affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM"
                             message="menu.my.documents" />

    <g:if test="${!inContextOrg}">
        <semui:securedSubNavItem controller="organisation" action="addressbook" params="${[id: orgInstance.id]}"
                                 affiliation="INST_USER" orgPerm="ORG_INST,ORG_CONSORTIUM"
                                 message="menu.institutions.myAddressbook"/>
    </g:if>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">

        <g:if test="${accessService.checkForeignOrgComboPermAffiliationX([
                org: orgInstance,
                affiliation: "INST_USER",
                comboPerm: "ORG_CONSORTIUM",
                comboAffiliation: "INST_EDITOR",
                specRoles: "ROLE_ADMIN"])}">

            <semui:subNavItem controller="organisation" action="accessPoints" params="${[id:orgInstance.id]}" message="org.nav.accessPoints"/>
        </g:if>
        <g:else>
            <semui:subNavItem controller="organisation" action="accessPoints" params="${[id:orgInstance.id]}"
                              message="org.nav.accessPoints" disabled="disabled" />
        </g:else>

    </g:if>
</semui:subNav>