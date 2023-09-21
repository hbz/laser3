<%@ page import="de.laser.utils.AppUtils; de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: my objects --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.my')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />

        <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" />
            <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
        </g:if>
        <g:elseif test="${contextService.hasPerm(CustomerTypeService.ORG_INST_BASIC)}">
            <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" />
        </g:elseif>

        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentProviders" message="menu.my.providers" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentPlatforms" message="menu.my.platforms" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentPackages" message="menu.my.packages" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentTitles" message="menu.my.titles" />
        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentPermanentTitles" message="menu.my.permanentTitles" />

        <div class="divider"></div>
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="tasks" message="menu.my.tasks" />
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="documents" message="menu.my.documents" />
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />

        <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_INST_BASIC)}">
            <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentSurveys" message="menu.my.surveys" />
        </g:if>
        <g:elseif test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" />
        </g:elseif>

        <div class="divider"></div>

        <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="myInstitution" action="currentSubscriptionsTransfer" message="menu.my.currentSubscriptionsTransfer" />
        </g:if>

        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentMarkers" message="menu.my.markers" />

        %{--                                <g:if test="${contextService.hasPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)}">--}%
        %{--                                    <div class="divider"></div>--}%
        %{--                                    <ui:securedMainNavItem addItemAttributes="true" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" />--}%
        %{--                                </g:if>--}%

        <div class="divider"></div>
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="compare" action="compareSubscriptions" message="menu.my.comp_sub" />
        <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="compare" action="compareLicenses" message="menu.my.comp_lic" />
    </div>
</div>
