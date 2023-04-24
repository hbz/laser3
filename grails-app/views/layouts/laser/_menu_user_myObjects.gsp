<%@ page import="de.laser.CustomerTypeService" %>
<laser:serviceInjection />

%{-- menu: my objects --}%

<div class="ui dropdown item" role="menuitem" aria-haspopup="true">
    <a class="title">
        ${message(code:'menu.my')} <i class="dropdown icon"></i>
    </a>
    <div class="menu" role="menu">
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSubscriptions" message="menu.my.subscriptions" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentLicenses" message="menu.my.licenses" />

        <g:if test="${accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN" action="manageMembers" message="menu.my.insts" />
            <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" specRole="ROLE_ADMIN" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
        </g:if>
        <g:elseif test="${accessService.ctxPerm(CustomerTypeService.ORG_INST_BASIC)}">
            <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" />
        </g:elseif>

        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentProviders" message="menu.my.providers" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPlatforms" message="menu.my.platforms" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentPackages" message="menu.my.packages" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentTitles" message="menu.my.titles" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" affiliation="INST_USER" controller="myInstitution" action="documents" message="menu.my.documents" />

        <div class="divider"></div>

        <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" affiliation="INST_USER" controller="myInstitution" action="subscriptionsManagement" message="menu.my.subscriptionsManagement" />

        <g:if test="${accessService.ctxPerm(CustomerTypeService.ORG_INST_BASIC)}">
            <div class="divider"></div>
            <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="myInstitution" action="currentSurveys" message="menu.my.surveys" />
        </g:if>
        <g:elseif test="${accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
            <div class="divider"></div>
            <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" affiliation="INST_USER" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" />
        </g:elseif>

        <div class="divider"></div>
        <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="${CustomerTypeService.PERMS_PRO}" affiliation="INST_USER" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />

        %{--                                <g:if test="${accessService.ctxPerm(CustomerTypeService.ORG_CONSORTIUM_PRO)}">--}%
        %{--                                    <div class="divider"></div>--}%
        %{--                                    <ui:securedMainNavItem generateElementId="true" role="menuitem" affiliation="INST_USER" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" />--}%
        %{--                                </g:if>--}%

        <div class="divider"></div>
        <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" affiliation="INST_USER" controller="compare" action="compareSubscriptions" message="menu.my.comp_sub" />
        <ui:securedMainNavItem generateElementId="true" role="menuitem" orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" affiliation="INST_USER" controller="compare" action="compareLicenses" message="menu.my.comp_lic" />
    </div>
</div>
