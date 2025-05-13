<%@ page import="de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.Subscription; de.laser.Links; de.laser.interfaces.CalculatedType; de.laser.OrgRole; de.laser.Org; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.SubscriptionPackage" %>
<laser:serviceInjection />
<g:set var="actionStart" value="${System.currentTimeMillis()}"/>
<%
    List menuArgs
    if(showConsortiaFunctions)
        menuArgs = [message(code:'subscription.details.consortiaMembers.label')]
%>
    <g:if test="${actionName == 'show'}">
        <ui:exportDropdown>
            <ui:exportDropdownItem>
                <g:link class="item" action="show" target="_blank" params="[id: subscription.id, export: 'pdf']">Export PDF</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
    </g:if>
    <g:elseif test="${actionName in ['index','addEntitlements']}">
        <ui:exportDropdown>
            <g:if test="${actionName == 'index'}">
                <g:if test="${currentTitlesCounts < 1000000}">
                    <ui:exportDropdownItem>
                        <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.ISSUE_ENTITLEMENTS]"/>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
                </g:else>
            </g:if>
            <g:elseif test="${actionName == 'addEntitlements'}">
                <g:if test="${currentTitlesCounts < 1000000}">
                    <ui:exportDropdownItem>
                        <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.TIPPS]"/>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
                </g:else>
            </g:elseif>
            <ui:exportDropdownItem>
                <g:link class="item kbartExport  js-no-wait-wheel" params="${params + [exportKBart: true]}">KBART Export</g:link>
            </ui:exportDropdownItem>
        </ui:exportDropdown>
</g:elseif>

<g:if test="${contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/actions" />

        <div class="divider"></div>

        <g:if test="${editable}">

            <g:if test="${(contextCustomerType == CustomerTypeService.ORG_INST_PRO && subscription._getCalculatedType() == Subscription.TYPE_LOCAL) || (customerTypeService.isConsortium( contextCustomerType ) && subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL)}">
                <ui:actionsDropdownItem controller="subscription" action="copySubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription), copyObject: true]}" message="myinst.copySubscription" />
            </g:if>
            <g:else>
                <ui:actionsDropdownItemDisabled message="myinst.copySubscription" />
            </g:else>

            <g:if test="${(contextCustomerType == CustomerTypeService.ORG_INST_PRO && !subscription.instanceOf) || customerTypeService.isConsortium( contextCustomerType )}">
                <ui:actionsDropdownItem controller="subscription" action="copyElementsIntoSubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription)]}" message="myinst.copyElementsIntoSubscription" />
            </g:if>
            <%-- removed as of ERMS-6441
            <g:if test="${customerTypeService.isConsortium( contextCustomerType ) && !subscription.instanceOf}">
                <div class="divider"></div>
                <ui:actionsDropdownItem data-ui="modal" id="generateFinanceImportWorksheet" href="#financeImportTemplate" message="myinst.financeImport.subscription.template"/>
                <ui:actionsDropdownItem controller="myInstitution" action="financeImport" params="${[id:subscription.id]}" message="menu.institutions.financeImport" />
            </g:if>--%>
        </g:if>

            <g:if test="${contextCustomerType == CustomerTypeService.ORG_INST_PRO && subscription.instanceOf}">
                <ui:actionsDropdownItem controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(subscription)]}" message="myinst.copyMyElements" />
                <g:if test="${navPrevSubscription}">
                    <ui:actionsDropdownItem controller="subscription" action="copyMyElements" params="${[sourceObjectId: genericOIDService.getOID(navPrevSubscription[0]), targetObjectId: genericOIDService.getOID(subscription)]}" message="myinst.copyMyElementsFromPrevSubscription" />
                </g:if>
            </g:if>

            <g:if test="${editable}">
                <div class="divider"></div>
                <ui:actionsDropdownItem controller="subscription" action="linkPackage" params="${[id:params.id]}" message="subscription.details.linkPackage.label" />
                <ui:actionsDropdownItem controller="subscription" action="linkTitle" params="${[id:params.id]}" message="subscription.details.linkTitle.label.subscription" />
                <g:if test="${subscription.packages}">
                    <g:if test="${titleManipulation}">
                        <ui:actionsDropdownItem controller="subscription" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />
                        <ui:actionsDropdownItem id="selectEntitlementsWithIDOnly" href="${createLink(action: 'kbartSelectionUpload', controller: 'ajaxHtml', id: subscription.id, params: [referer: actionName, headerToken: 'subscription.details.addEntitlements.menuID', withIDOnly: true, progressCacheKey: '/subscription/addEntitlements/'])}" message="subscription.details.addEntitlements.menuID"/>
                        <ui:actionsDropdownItem id="selectEntitlementsWithKBART" href="${createLink(action: 'kbartSelectionUpload', controller: 'ajaxHtml', id: subscription.id, params: [referer: actionName, headerToken: 'subscription.details.addEntitlements.menu', progressCacheKey: '/subscription/addEntitlements/'])}" message="subscription.details.addEntitlements.menu"/>
                        <ui:actionsDropdownItem id="selectEntitlementsWithPick" href="${createLink(action: 'kbartSelectionUpload', controller: 'ajaxHtml', id: subscription.id, params: [referer: actionName, headerToken: 'subscription.details.addEntitlements.menuPick', withPick: true, progressCacheKey: '/subscription/addEntitlements/'])}" message="subscription.details.addEntitlements.menuPick"/>
                    </g:if>
                    <g:else>
                        <ui:actionsDropdownItemDisabled message="subscription.details.addEntitlements.label" tooltip="${message(code:'subscription.details.addEntitlements.holdingEntire')}"/>
                        <ui:actionsDropdownItemDisabled message="subscription.details.addEntitlements.menu" tooltip="${message(code:'subscription.details.addEntitlements.holdingEntire')}"/>
                    </g:else>
                    <ui:actionsDropdownItem controller="subscription" action="manageEntitlementGroup" params="${[id:params.id]}" message="subscription.details.manageEntitlementGroup.label" />
                    <g:if test="${titleManipulation}">
                        <ui:actionsDropdownItem controller="subscription" action="index" notActive="true" params="${[id:params.id, issueEntitlementEnrichment: true]}" message="subscription.details.issueEntitlementEnrichment.label" />
                    </g:if>
                    <g:else>
                        <ui:actionsDropdownItemDisabled message="subscription.details.issueEntitlementEnrichment.label" tooltip="${message(code:'subscription.details.addEntitlements.holdingEntire')}"/>
                    </g:else>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="subscription.details.addEntitlements.label" tooltip="${message(code:'subscription.details.addEntitlements.noPackagesYetAdded')}"/>
                </g:else>

            <%-- TODO: once the hookup has been decided, the ifAnyGranted securing can be taken down --%>
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <g:if test="${subscription.instanceOf}">
                    <g:if test="${params.pkgfilter}">
                        <g:set var="pkg" value="${SubscriptionPackage.executeQuery("select sp from SubscriptionPackage sp where sp.pkg.gokbId = :filter",[filter:params.pkgfilter])}"/>
                        <g:if test="${pkg && !pkg.finishDate}">
                            <ui:actionsDropdownItem controller="subscription" action="renewEntitlements" params="${[targetObjectId:params.id,packageId:params.pkgfilter]}" message="subscription.details.renewEntitlements.label"/>
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="subscription.details.renewEntitlements.label" tooltip="${message(code:'subscription.details.renewEntitlements.packageRenewalAlreadySubmitted')}"/>
                        </g:else>
                    </g:if>
                    <g:else>
                        <ui:actionsDropdownItemDisabled message="subscription.details.renewEntitlements.label" tooltip="${message(code:'subscription.details.renewEntitlements.packageMissing')}"/>
                    </g:else>
                </g:if>
            </sec:ifAnyGranted>
                <g:if test="${subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE] && contextService.getOrg().isCustomerType_Consortium_Pro()}">
                    <div class="divider"></div>
                    <ui:actionsDropdownItem controller="subscription" action="manageDiscountScale" params="${[id:params.id]}" message="subscription.details.manageDiscountScale.label" />
                    <g:if test="${subscription.discountScales.size() > 0}">
                        <ui:actionsDropdownItem controller="subscription" action="copyDiscountScales" params="${[id:params.id]}" message="subscription.details.copyDiscountScales.label" />
                    </g:if>
                </g:if>
                    <g:if test="${subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE] && contextService.getOrg().isCustomerType_Consortium()}">
                <div class="divider"></div>
                <g:if test="${hasNext}">
                    <ui:actionsDropdownItemDisabled tooltip="${message(code: 'subscription.details.renewals.isAlreadyRenewed')}" message="subscription.details.renewalsConsortium.label"/>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItem controller="subscription" action="renewSubscription"
                                           params="${[id: params.id]}" message="subscription.details.renewalsConsortium.label"/>
                </g:else>
            </g:if>
            <g:if test ="${subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL}">
                <g:if test ="${hasNext}">
                    <ui:actionsDropdownItemDisabled tooltip="${message(code: 'subscription.details.renewals.isAlreadyRenewed')}" message="subscription.details.renewals.label"/>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItem controller="subscription" action="renewSubscription"
                                           params="${[id: params.id]}" message="subscription.details.renewals.label"/>
                </g:else>
            </g:if>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium_Pro() && showConsortiaFunctions && subscription.instanceOf == null }">
                <ui:actionsDropdownItem controller="survey" action="addSubtoSubscriptionSurvey"
                                               params="${[sub:params.id]}" text="${message(code:'createSubscriptionSurvey.label')}" />
                <g:if test="${titleManipulationBlocked}">
                    <ui:actionsDropdownItemDisabled message="createIssueEntitlementsSurvey.label" tooltip="${message(code: 'subscription.details.addEntitlements.holdingInherited')}" />
                </g:if>
                <g:else>
                    <ui:actionsDropdownItem controller="survey" action="addSubtoIssueEntitlementsSurvey" params="${[sub:params.id]}" text="${message(code:'createIssueEntitlementsSurvey.label')}" />
                </g:else>
                <div class="divider"></div>
            </g:if>

            <g:if test="${showConsortiaFunctions || subscription.administrative}">
                <ui:actionsDropdownItem controller="subscription" action="addMembers" params="${[id:params.id]}" text="${message(code:'subscription.details.addMembers.label',args:menuArgs)}" />
            </g:if>

            <g:if test="${subscription._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL && customerTypeService.isConsortium( contextCustomerType )}">

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
                <g:if test="${!(contextService.getOrg().id == subscriptionConsortia?.id && subscription.instanceOf)}">
                    <div class="divider"></div>
                    <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />
                </g:if>

                <g:if test="${editable}">
                    <div class="divider"></div>
                    <g:link class="item" action="delete" id="${params.id}"><i class="${Icon.CMD.DELETE}"></i> ${message(code:'deletion.subscription')}</g:link>
                </g:if>
            </g:elseif>
        </g:if>
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${contextService.isInstEditor()}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem message="template.addNote" data-ui="modal" href="#modalCreateNote" />

        <g:if test="${actionName == 'members' && subscriptionService.getValidSubChilds(subscription)}">
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            <div class="divider"></div>
        </g:if>
    </ui:actionsDropdown>
</g:elseif>
<g:elseif test="${contextService.isInstUser()}">
    <g:if test="${actionName == 'members' && subscriptionService.getValidSubChilds(subscription)}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </ui:actionsDropdown>
    </g:if>
</g:elseif>

<g:if test="${contextService.isInstEditor()}">
    <laser:render template="/templates/sidebar/modals" model="${[tmplConfig: [ownobj: subscription, owntp: 'subscription', inContextOrg: inContextOrg]]}" />
    <laser:render template="financeImportTemplate" />
</g:if>

<g:if test="${subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL}">
    <g:set var="previous" value="${subscription._getCalculatedPrevious()}"/>
    <g:set var="successor" value="${subscription._getCalculatedSuccessor()}"/>
    <laser:render template="subscriptionTransferInfo" model="${[calculatedSubList: successor + [subscription] + previous]}"/>
</g:if>

<g:if test="${editable && subscription.getConsortium()?.id == contextService.getOrg().id}">
    <g:if test="${!(actionName.startsWith('copy') || actionName in ['renewEntitlementsWithSurvey', 'renewSubscription', 'emptySubscription'])}">
        <laser:render template="/templates/flyouts/subscriptionMembers" model="[subscription: subscription]"/>
    </g:if>
</g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#selectEntitlementsWithKBART, #selectEntitlementsWithPick, #selectEntitlementsWithIDOnly').on('click', function(e) {
            e.preventDefault();

            $.ajax({
                url: $(this).attr('href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #KBARTUploadForm').remove();
                $('#dynamicModalContainer').empty().html(data);
                let keyboardHandler = function(e) {
                    if (e.keyCode === 27) {
                        $('#KBARTUploadForm').modal('hide');
                    }
                };
                $('#dynamicModalContainer .ui.modal').modal({
                   onShow: function () {
                        r2d2.initDynamicUiStuff('#KBARTUploadForm');
                        r2d2.initDynamicXEditableStuff('#KBARTUploadForm');
                        $("html").css("cursor", "auto");
                    },
                    onVisible: function () {
                        document.addEventListener('keyup', keyboardHandler);
                    },
                    detachable: true,
                    autofocus: false,
                    closable: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    },
                    onHide : function() {
                        document.removeEventListener('keyup', keyboardHandler);
                    }
                }).modal('show');
            })
        });
</laser:script>


