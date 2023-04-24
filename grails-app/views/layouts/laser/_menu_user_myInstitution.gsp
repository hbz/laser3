<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: my institution --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.institutions.myInst')} <i class="dropdown icon"></i>
    </a>

    <div class="menu" role="menu">
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />
        <div class="divider"></div>

        <ui:link generateElementId="true" class="item" role="menuitem" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</ui:link>

        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="addressbook" message="menu.institutions.myAddressbook" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="tasks" message="task.plural" />
        <%--<ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_ORG_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="changes" message="menu.institutions.changes" />--%>
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="finance" message="menu.institutions.finance" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_EDITOR" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />

        <div class="divider"></div>
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_ADM" controller="myInstitution" action="users" message="menu.institutions.users" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="reporting" message="menu.institutions.reporting" />
    </div>
</div>