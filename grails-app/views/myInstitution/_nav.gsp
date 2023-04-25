<ui:subNav actionName="${actionName}">
    <ui:subNavItem controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.private_props" />
    <ui:subNavItem controller="myInstitution" action="managePropertyDefinitions" message="menu.institutions.prop_defs" />
    <ui:subNavItem controller="myInstitution" action="managePropertyGroups" message="menu.institutions.prop_groups" />
    <ui:securedSubNavItem affiliation="INST_EDITOR" controller="myInstitution" action="manageProperties" message="menu.institutions.manage_private_props" />
</ui:subNav>
