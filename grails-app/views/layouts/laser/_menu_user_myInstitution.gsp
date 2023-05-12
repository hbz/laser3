<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: my institution --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.institutions.myInst')} <i class="dropdown icon"></i>
    </a>

    <div class="menu" role="menu">
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />
%{--        <div class="divider"></div>--}%

        <ui:link addItemAttributes="true" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</ui:link>

        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="addressbook" message="menu.institutions.addressbook" />
        <%--<ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_ORG_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="changes" message="menu.institutions.changes" />--%>

        <div class="divider"></div>

        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="finance" message="menu.institutions.finance" />
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
        <ui:securedMainNavItem addItemAttributes="true" affiliation="INST_EDITOR" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />

        <div class="divider"></div>

        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="subscriptionsManagement" message="menu.institutions.subscriptionsManagement" />
        <ui:securedMainNavItem addItemAttributes="true" affiliation="INST_ADM" controller="myInstitution" action="users" message="menu.institutions.users" />
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />

        <div class="divider"></div>
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="reporting" message="menu.institutions.reporting" />
    </div>
</div>