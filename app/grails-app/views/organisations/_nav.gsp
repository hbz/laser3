<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="organisations" action="show" params="${[id: params.id]}" message="org.nav.details"/>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">
    <semui:securedSubNavItem controller="organisations" action="numbers" params="${[id: params.id]}"
                             affiliation="INST_EDITOR" affiliationOrg="${orgInstance}"
                      message="menu.institutions.numbers" specRoleCheck="ROLE_ORG_EDITOR"/>
    </g:if>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">
        <semui:securedSubNavItem controller="organisations" action="users" params="${[id: params.id]}"
                                 message="org.nav.users" affiliation="INST_USER" affiliationOrg="${orgInstance}"
        />
    </g:if>

    <semui:securedSubNavItem controller="organisations" action="config" params="${[id: params.id]}"
                             message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>

    <semui:subNavItem controller="organisations" action="addressbook" params="${[id: params.id]}"
                      message="menu.institutions.myAddressbook"/>
    <semui:subNavItem controller="organisations" action="accessPoints" params="${[id:params.id]}"
                      message="org.nav.accessPoints" />

</semui:subNav>