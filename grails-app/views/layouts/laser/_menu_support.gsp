<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: public --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.public')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:link addItemAttributes="true" controller="organisation" action="listInstitution">${message(code:'menu.public.all_insts')}</ui:link>

        <div class="divider"></div>

        <ui:link addItemAttributes="true" target="_blank" onclick="JSPC.app.workaround_targetBlank(event)" controller="gasco">${message(code:'menu.public.gasco_monitor')}</ui:link>
        <a href="${message(code:'url.wekb.' + currentServer)}" id="wekb" class="item" role="menuitem" target="_blank" onclick="JSPC.app.workaround_targetBlank(event)"><i class="ui icon la-gokb"></i> we:kb</a>
    </div>
</div>

%{-- menu: my objects --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.my')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />

        <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" />
        <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />

        <div class="divider"></div>
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="tasks" message="menu.my.tasks" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="documents" message="menu.my.documents" />

        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />
    </div>
</div>

%{-- menu: my institution --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.institutions.myInst')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="dashboard" message="menu.institutions.dash" />

        <ui:link addItemAttributes="true" controller="organisation" action="show" params="[id: contextOrg?.id]">${message(code:'menu.institutions.org_info')}</ui:link>

        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="addressbook" message="menu.institutions.addressbook" />
        <div class="divider"></div>

        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="finance" message="menu.institutions.finance" />
        <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="costConfiguration" action="index" message="menu.institutions.costConfiguration" />
        <ui:securedMainNavItem addItemAttributes="true" instRole="INST_EDITOR" specRole="ROLE_ADMIN" controller="myInstitution" action="financeImport" message="menu.institutions.financeImport" />
        <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="budgetCodes" message="menu.institutions.budgetCodes" />

        <div class="divider"></div>

%{--        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="subscriptionsManagement" message="menu.institutions.subscriptionsManagement" />--}%
        <ui:securedMainNavItem addItemAttributes="true" instRole="INST_ADM" controller="myInstitution" action="users" message="menu.institutions.users" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="managePrivatePropertyDefinitions" message="menu.institutions.manage_props" />
    </div>
</div>

