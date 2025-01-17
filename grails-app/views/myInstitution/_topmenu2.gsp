<%@ page import="de.laser.CustomerTypeService; de.laser.ui.Icon" %>

<div class="ui fluid card" style="margin-bottom: 2em">
    <div class="content" style="padding-bottom: 0">
        <div class="ui four column grid">

            <div class="column">
                <div class="ui vertical fluid secondary menu">
                    <g:link class="item" controller="myInstitution" action="currentSubscriptions">
                        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i> ${message(code:'menu.my.subscriptions')}
                    </g:link>
                    <g:link class="item" controller="myInstitution" action="currentLicenses">
                        <i class="${Icon.LICENSE} la-list-icon"></i> ${message(code:'menu.my.licenses')}
                    </g:link>

                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" icon="${Icon.ORG} la-list-icon" />
                    </g:if>
                    <g:else>
                        <g:link class="item" controller="myInstitution" action="currentProviders">
                            <i class="${Icon.PROVIDER} la-list-icon"></i> ${message(code:'menu.my.providers')}
                        </g:link>
                    %{--  <g:link class="item" controller="myInstitution" action="currentVendors">
%{--                    <i class="${Icon.VENDOR} icon la-list-icon"></i> ${message(code:'menu.my.vendors')}</g:link>--}%
                    </g:else>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui vertical fluid secondary menu">
                    <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="tasks" message="menu.my.tasks" icon="${Icon.TASK} la-list-icon" />

                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                    <ui:securedMainNavItem controller="myInstitution" action="documents" message="menu.my.documents" icon="${Icon.DOCUMENT} la-list-icon" />
                    <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" icon="${Icon.WORKFLOW} la-list-icon" />
                    </g:if>
                    <g:else>
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" icon="${Icon.WORKFLOW} la-list-icon" />
                    %{--   <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="documents" message="menu.my.documents" icon="${Icon.DOCUMENT} icon la-list-icon" />--}%

                        <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
                            <g:link class="item" controller="myInstitution" action="currentSurveys">
                                <i class="${Icon.SURVEY} la-list-icon"></i> ${message(code:'menu.my.surveys')}
                            </g:link>
                        </g:if>
                        <g:else>
                            <ui:securedMainNavItem orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" icon="${Icon.SURVEY} la-list-icon" />
                        </g:else>
                    </g:else>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui vertical fluid secondary menu">
                    <g:link class="item" controller="org" action="show" id="${contextService.getOrg().id}">
                        <i class="${Icon.ORG} la-list-icon"></i> ${message(code: 'menu.institutions.org.show')}
                    </g:link>
                    <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="addressbook" message="menu.institutions.addressbook" icon="${Icon.ACP_PUBLIC} la-list-icon" />
                    <ui:securedMainNavItem controller="myInstitution" action="finance" message="menu.institutions.finance" icon="${Icon.FINANCE} la-list-icon" />
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui vertical fluid secondary menu">
                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem specRole="ROLE_ADMIN" controller="myInstitution" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" icon="${Icon.ORG} la-list-icon" />
                        <ui:securedMainNavItem controller="myInstitution" action="subscriptionsManagement" message="menu.institutions.subscriptionsManagement" icon="${Icon.SUBSCRIPTION} la-list-icon" />
                    </g:if>
                    <g:else>
                        <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                            <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" icon="${Icon.ORG} la-list-icon" />
                        </g:if>
                        <g:elseif test="${contextService.getOrg().isCustomerType_Inst()}">
                            <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" icon="${Icon.ORG} la-list-icon" />
                        </g:elseif>
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="reporting" message="myinst.reporting" icon="${Icon.REPORTING} la-list-icon" />
                    </g:else>
                    <g:link class="item" controller="public" action="help">
                        <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i> ${message(code:'menu.user.help')}
                    </g:link>
                </div>
            </div><!-- .column -->

        </div><!-- .grid -->
    </div>
</div>

<style>
    .list .item .content .disabled { color:lightgrey }
</style>