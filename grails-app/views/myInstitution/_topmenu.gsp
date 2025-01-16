<%@ page import="de.laser.CustomerTypeService; de.laser.ui.Icon" %>

<div class="ui fluid card" style="margin-bottom: 2em">
    <div class="content" style="padding-bottom: 0">
        <div class="ui four column relaxed divided grid">

            <div class="column">
                <div class="ui relaxed list">
                    <div class="item">
                        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                        <div class="content">
                            <g:link controller="myInstitution" action="currentSubscriptions">${message(code:'menu.my.subscriptions')}</g:link>
                        </div>
                    </div>
                    <div class="item">
                        <i class="${Icon.LICENSE} la-list-icon"></i>
                        <div class="content">
                            <g:link controller="myInstitution" action="currentLicenses">${message(code:'menu.my.licenses')}</g:link>
                        </div>
                    </div>
                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <div class="item">
                            <i class="${Icon.ORG} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" />
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="item">
                            <i class="${Icon.PROVIDER} la-list-icon"></i>
                            <div class="content">
                                <g:link controller="myInstitution" action="currentProviders">${message(code:'menu.my.providers')}</g:link>
                            </div>
                        </div>
                    %{--                        <div class="item">--}%
                    %{--                            <i class="${Icon.VENDOR} icon la-list-icon"></i>--}%
                    %{--                            <div class="content">--}%
                    %{--                                <g:link controller="myInstitution" action="currentVendors">${message(code:'menu.my.vendors')}</g:link>--}%
                    %{--                            </div>--}%
                    %{--                        </div>--}%
                    </g:else>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui relaxed list">
                    <div class="item">
                        <i class="${Icon.TASK} la-list-icon"></i>
                        <div class="content">
                            <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="tasks" message="menu.my.tasks" />
                        </div>
                    </div>
                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <div class="item">
                            <i class="${Icon.DOCUMENT} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem controller="myInstitution" action="documents" message="menu.my.documents" />
                            </div>
                        </div>
                        <div class="item">
                            <i class="${Icon.WORKFLOW} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="item">
                            <i class="${Icon.WORKFLOW} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" />
                            </div>
                        </div>
                    %{--                        <div class="item">--}%
                    %{--                            <i class="${Icon.DOCUMENT} icon la-list-icon"></i>--}%
                    %{--                            <div class="content">--}%
                    %{--                                <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="documents" message="menu.my.documents" />--}%
                    %{--                            </div>--}%
                    %{--                        </div>--}%
                        <div class="item">
                            <i class="${Icon.SURVEY} la-list-icon"></i>
                            <div class="content">
                                <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
                                    <g:link controller="myInstitution" action="currentSurveys">${message(code:'menu.my.surveys')}</g:link>
                                </g:if>
                                <g:else>
                                    <ui:securedMainNavItem orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys"/>
                                </g:else>
                            </div>
                        </div>
                    </g:else>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui relaxed list">
                    <div class="item">
                        <i class="${Icon.ORG} la-list-icon"></i>
                        <div class="content">
                            <g:link controller="org" action="show" id="${contextService.getOrg().id}">${message(code: 'menu.institutions.org.show')}</g:link>
                        </div>
                    </div>
                    <div class="item">
                        <i class="${Icon.ACP_PUBLIC} la-list-icon"></i>
                        <div class="content">
                            <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="addressbook" message="menu.institutions.addressbook" />
                        </div>
                    </div>
                    <div class="item">
                        <i class="${Icon.FINANCE} la-list-icon"></i>
                        <div class="content">
                            <ui:securedMainNavItem controller="myInstitution" action="finance" message="menu.institutions.finance" />
                        </div>
                    </div>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui relaxed list">
                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <div class="item">
                            <i class="${Icon.ORG} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem specRole="ROLE_ADMIN" controller="myInstitution" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" />
                            </div>
                        </div>
                        <div class="item">
                            <i class="${Icon.SUBSCRIPTION} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem controller="myInstitution" action="subscriptionsManagement" message="menu.institutions.subscriptionsManagement" />
                            </div>
                        </div>
                    </g:if>
                    <g:else>
                        <div class="item">
                            <i class="${Icon.ORG} la-list-icon"></i>
                            <div class="content">
                                <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                                    <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" />
                                </g:if>
                                <g:elseif test="${contextService.getOrg().isCustomerType_Inst()}">
                                    <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" />
                                </g:elseif>
                            </div>
                        </div>
                        <div class="item">
                            <i class="${Icon.REPORTING} la-list-icon"></i>
                            <div class="content">
                                <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="reporting" message="myinst.reporting" />
                            </div>
                        </div>
                    </g:else>
                    <div class="item">
                        <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i>
                        <div class="content">
                            <g:link controller="public" action="help">${message(code:'menu.user.help')}</g:link>
                        </div>
                    </div>
                </div>
            </div><!-- .column -->

        </div>
    </div>
</div>

<style>
    .list .item .content .disabled { color:lightgrey }
</style>