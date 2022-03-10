<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.private_props" />
    <semui:subNavItem controller="myInstitution" action="managePropertyDefinitions" message="menu.institutions.prop_defs" />
    <semui:subNavItem controller="myInstitution" action="managePropertyGroups" message="menu.institutions.prop_groups" />
    <semui:securedSubNavItem controller="myInstitution" action="manageProperties" message="menu.institutions.manage_private_props" affiliation="INST_EDITOR" />
</semui:subNav>
