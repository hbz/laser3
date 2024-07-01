<%@ page import="de.laser.helper.Icons; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.Subscription; de.laser.Links; de.laser.interfaces.CalculatedType; de.laser.OrgRole; de.laser.Org; de.laser.storage.RDStore; de.laser.RefdataValue" %>

<laser:serviceInjection />
<g:set var="actionStart" value="${System.currentTimeMillis()}"/>

    <g:if test="${actionName == 'show'}">
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="show" target="_blank" params="[id: subscription.id, export: 'pdf']">Export PDF</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </g:if>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionDropdownItems: true]]}" />

        <div class="divider"></div>

        <g:if test="${editable}">

%{--            <g:if test="${subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL}">--}%
            <g:if test="${subscription._getCalculatedType() in [Subscription.TYPE_CONSORTIAL, Subscription.TYPE_ADMINISTRATIVE]}">
                <ui:actionsDropdownItem controller="subscription" action="copySubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription), copyObject: true]}" message="myinst.copySubscription" />
            </g:if>

            <ui:actionsDropdownItem controller="subscription" action="copyElementsIntoSubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription)]}" message="myinst.copyElementsIntoSubscription" />
        </g:if>

            <g:if test="${editable}">
%{--                <div class="divider"></div>--}%

%{--                <g:if test="${subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE]}">--}%
%{--                    <div class="divider"></div>--}%
%{--                    <ui:actionsDropdownItem controller="subscription" action="manageDiscountScale" params="${[id:params.id]}" message="subscription.details.manageDiscountScale.label" />--}%
%{--                    <g:if test="${subscription.discountScales.size() > 0}">--}%
%{--                        <ui:actionsDropdownItem controller="subscription" action="copyDiscountScales" params="${[id:params.id]}" message="subscription.details.copyDiscountScales.label" />--}%
%{--                    </g:if>--}%
%{--                </g:if>--}%


                <g:if test="${subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE]}">
                    <div class="divider"></div>
                    <g:if test="${hasNext}">
                        <ui:actionsDropdownItemDisabled tooltip="${message(code: 'subscription.details.renewals.isAlreadyRenewed')}" message="subscription.details.renewalsConsortium.label"/>
                    </g:if>
                    <g:else>
                        <ui:actionsDropdownItem controller="subscription" action="renewSubscription"
                                                params="${[id: params.id]}" message="subscription.details.renewalsConsortium.label"/>
                    </g:else>
                </g:if>

            <g:if test="${showConsortiaFunctions || subscription.administrative || subscription._getCalculatedType() in [Subscription.TYPE_CONSORTIAL, Subscription.TYPE_ADMINISTRATIVE]}">
                <div class="divider"></div>
            </g:if>

            <g:if test="${showConsortiaFunctions || subscription.administrative}">
                <ui:actionsDropdownItem controller="subscription" action="addMembers" params="${[id:params.id]}" text="${message(code:'subscription.details.addMembers.label',args:[message(code:'subscription.details.consortiaMembers.label')])}" />
            </g:if>

%{--            <g:if test="${subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL}">--}%
            <g:if test="${subscription._getCalculatedType() in [Subscription.TYPE_CONSORTIAL, Subscription.TYPE_ADMINISTRATIVE]}">

                  <ui:actionsDropdownItem controller="subscription" action="membersSubscriptionsManagement"
                                           params="${[id: params.id]}"
                                           text="${message(code:'subscriptionsManagement.subscriptions.members')}"/>
            </g:if>

            <g:if test="${actionName == 'members'}">
                <g:if test="${subscriptionService.getValidSubChilds(subscription)}">
                    <div class="divider"></div>
                    <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
                </g:if>
            </g:if>
            <g:elseif test="${actionName == 'show'}">
                <%-- the editable setting needs to be the same as for the properties themselves -> override! --%>
                <%-- the second clause is to prevent the menu display for consortia at member subscriptions --%>
                <g:if test="${!(contextOrg.id == subscriptionConsortia?.id && subscription.instanceOf)}">
                    <div class="divider"></div>
                    <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />
                </g:if>

                <g:if test="${editable}">
                    <div class="divider"></div>
                    <g:link class="item" action="delete" id="${params.id}"><i class="${Icons.CMD_DELETE} icon"></i> ${message(code:'deletion.subscription')}</g:link>
                </g:if>
            </g:elseif>
        </g:if>
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <g:if test="${actionName == 'members' && subscriptionService.getValidSubChilds(subscription)}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </ui:actionsDropdown>
    </g:if>
</g:elseif>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionModals: true, ownobj: subscription, owntp: 'subscription']]}" />
</g:if>

