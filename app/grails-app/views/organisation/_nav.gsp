<laser:serviceInjection />
<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="organisation" action="show" params="${[id: params.id]}" message="org.nav.details"/>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">
    <semui:securedSubNavItem controller="organisation" action="readerNumber" params="${[id: params.id]}"
                             affiliation="INST_EDITOR" affiliationOrg="${orgInstance}"
                      message="menu.institutions.readerNumbers" specRoleCheck="ROLE_ORG_EDITOR"/>
    </g:if>

    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">
        <g:if test="${instAdmService.hasInstAdmPivileges(contextService.getUser(), orgInstance)}">
            <semui:subNavItem controller="organisation" action="users" params="${[id: params.id]}"
                            message="org.nav.users" affiliation="INST_USER" affiliationOrg="${orgInstance}"/>
        </g:if>
        <g:else>
            <semui:securedSubNavItem controller="organisation" action="users" params="${[id: params.id]}"
                                     message="org.nav.users" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
        </g:else>
    </g:if>

    <semui:securedSubNavItem controller="organisation" action="settings" params="${[id: params.id]}"
                             message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}" specRoleCheck="ROLE_ORG_EDITOR"/>

    <semui:subNavItem controller="organisation" action="documents" params="${[id: params.id]}" message="menu.my.documents" />


    <semui:subNavItem controller="organisation" action="addressbook" params="${[id: params.id]}"
                      message="menu.institutions.myAddressbook"/>
    <g:if test="${orgInstance.sector != com.k_int.kbplus.RefdataValue.getByValueAndCategory('Publisher', 'OrgSector')}">
    <semui:securedSubNavItem controller="organisation" action="accessPoints" params="${[id:params.id]}"
                      message="org.nav.accessPoints" affiliation="INST_ADM" affiliationOrg="${orgInstance}"/>
    </g:if>
</semui:subNav>