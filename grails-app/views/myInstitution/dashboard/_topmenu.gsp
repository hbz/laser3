<%@ page import="de.laser.CustomerTypeService; de.laser.ui.Icon" %>

<div class="ui fluid card" id="dashboard-topmenu">
    <div class="content">
        <div class="ui four column stackable grid">

            <div class="column">
                <div class="ui relaxed selection list la-dashboard">
                    <g:link class="item" controller="myInstitution" action="currentSubscriptions">
                        <i class="${Icon.SUBSCRIPTION} la-list-icon"></i> ${message(code:'menu.my.subscriptions')}
                    </g:link>
                    <g:link class="item" controller="myInstitution" action="currentLicenses">
                        <i class="${Icon.LICENSE} la-list-icon"></i> ${message(code:'menu.my.licenses')}
                    </g:link>

                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" icon="${Icon.AUTH.ORG_INST} la-list-icon" />
                    </g:if>
                    <g:else>
                        <g:link class="item" controller="myInstitution" action="currentProviders">
                            <i class="${Icon.PROVIDER} la-list-icon"></i> ${message(code:'menu.my.providers')}
                        </g:link>
                        <g:link class="item" controller="myInstitution" action="currentVendors">
                            <i class="${Icon.VENDOR} icon la-list-icon"></i> ${message(code:'menu.my.vendors')}
                        </g:link>
                    </g:else>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui relaxed selection list la-dashboard">
                    <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="tasks" message="menu.my.tasks" icon="${Icon.TASK} la-list-icon" />

                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem controller="myInstitution" action="documents" message="menu.my.documents" icon="${Icon.DOCUMENT} la-list-icon" />
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" icon="${Icon.WORKFLOW} la-list-icon" />
                    </g:if>
                    <g:else>
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="currentWorkflows" message="menu.my.workflows" icon="${Icon.WORKFLOW} la-list-icon" />

                        <g:if test="${contextService.getOrg().isCustomerType_Inst()}">
                            <g:link class="item" controller="myInstitution" action="currentSurveys">
                                <i class="${Icon.SURVEY} la-list-icon"></i> ${message(code:'menu.my.surveys')}
                            </g:link>
                        </g:if>
                        <g:else>
                            <ui:securedMainNavItem orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="survey" action="workflowsSurveysConsortia" message="menu.my.surveys" icon="${Icon.SURVEY} la-list-icon" />
                        </g:else>
                    </g:else>
                    <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentMarkers" message="menu.my.markers" icon="${Icon.MARKER} la-list-icon"/>
                    </g:if>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui relaxed selection list la-dashboard">

                    <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                        <g:link class="item" controller="org" action="show" id="${contextService.getOrg().id}">
                            <i class="${Icon.AUTH.ORG_CONSORTIUM} la-list-icon"></i> ${message(code: 'menu.institutions.org.show')}
                        </g:link>
                    </g:if>
                    <g:elseif test="${contextService.getOrg().isCustomerType_Inst()}">
                        <g:link class="item" controller="org" action="show" id="${contextService.getOrg().id}">
                            <i class="${Icon.AUTH.ORG_INST} la-list-icon"></i> ${message(code: 'menu.institutions.org.show')}
                        </g:link>
                    </g:elseif>
                    <g:elseif test="${contextService.getOrg().isCustomerType_Support()}">
                        <g:link class="item" controller="org" action="show" id="${contextService.getOrg().id}">
                            <i class="${Icon.AUTH.ORG_SUPPORT} la-list-icon"></i> ${message(code: 'menu.institutions.org.show')}
                        </g:link>
                    </g:elseif>

                    <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC}" controller="myInstitution" action="addressbook" message="menu.institutions.addressbook" icon="${Icon.ACP_PUBLIC} la-list-icon" />
                    <ui:securedMainNavItem controller="myInstitution" action="finance" message="menu.institutions.finance" icon="${Icon.FINANCE} la-list-icon" />

                    <g:if test="${!contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem orgPerm="${CustomerTypeService.PERMS_PRO}" controller="myInstitution" action="reporting" message="myinst.reporting" icon="${Icon.REPORTING} la-list-icon" />
                    </g:if>
                </div>
            </div><!-- .column -->
            <div class="column">
                <div class="ui relaxed selection list la-dashboard">
                    <g:if test="${contextService.getOrg().isCustomerType_Support()}">
                        <ui:securedMainNavItem specRole="ROLE_ADMIN" controller="myInstitution" action="manageConsortiaSubscriptions" message="menu.my.consortiaSubscriptions" icon="${Icon.AUTH.ORG_INST} la-list-icon" />
                        <ui:securedMainNavItem controller="myInstitution" action="subscriptionsManagement" message="menu.institutions.subscriptionsManagement" icon="${Icon.SUBSCRIPTION} la-list-icon" />
                    </g:if>
                    <g:else>
                        <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                            <ui:securedMainNavItem addItemAttributes="true" specRole="ROLE_ADMIN" controller="myInstitution" action="manageMembers" message="menu.my.insts" icon="${Icon.AUTH.ORG_INST} la-list-icon" />
                            <ui:securedMainNavItem addItemAttributes="true" orgPerm="${CustomerTypeService.ORG_CONSORTIUM_PRO}" controller="myInstitution" action="currentSubscriptionsTransfer" message="menu.my.currentSubscriptionsTransfer" icon="${Icon.SUBSCRIPTION} la-list-icon" />
                        </g:if>
                        <g:elseif test="${contextService.getOrg().isCustomerType_Inst()}">
                            <ui:securedMainNavItem addItemAttributes="true" controller="myInstitution" action="currentConsortia" message="menu.my.consortia" icon="${Icon.AUTH.ORG_CONSORTIUM} la-list-icon" />
                        </g:elseif>
                    </g:else>

                    <g:link class="item" controller="public" action="help">
                        <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i> ${message(code:'menu.user.help')}
                    </g:link>
%{--                    <g:link class="item" controller="public" action="faq">--}%
%{--                        <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i> ${message(code:'menu.user.faq')}--}%
%{--                    </g:link>--}%
%{--                    <g:link class="item" controller="public" action="releases">--}%
%{--                        <i class="${Icon.TOOLTIP.HELP} la-list-icon"></i> ${message(code:'releaseNotes')}--}%
%{--                    </g:link>--}%
                </div>
            </div><!-- .column -->

        </div><!-- .grid -->
    </div>
</div>

<style>
#dashboard-topmenu {
    margin-bottom: 1.5em;
}
#dashboard-topmenu > .content {
    padding-bottom: 0;
}
#dashboard-topmenu .list .item .la-list-icon {
    margin: .1rem .5rem 0 0;
}
</style>