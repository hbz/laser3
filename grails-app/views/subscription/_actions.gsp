<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.Subscription; de.laser.Links; de.laser.interfaces.CalculatedType; de.laser.OrgRole; de.laser.Org; de.laser.storage.RDStore; de.laser.RefdataValue; de.laser.SubscriptionPackage" %>

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
            <%--
            <ui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" action="${actionName}"
                            params="${params + [format: 'csv']}">
                        <g:message code="default.button.exports.csv"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="${actionName}" params="${params + [format: 'csv']}">CSV Export</g:link>
                </g:else>
            </ui:exportDropdownItem>
            --%>
            <g:if test="${actionName == 'index'}">
                <g:if test="${currentTitlesCounts < 1000000}">
                    <ui:exportDropdownItem>
                        <a class="item" data-ui="modal" href="#individuallyExportIEsModal">Export</a>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
                </g:else>
            </g:if>
            <g:elseif test="${actionName == 'addEntitlements'}">
                <g:if test="${currentTitlesCounts < 1000000}">
                    <ui:exportDropdownItem>
                        <a class="item" data-ui="modal" href="#individuallyExportTippsModal">Export</a>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItemDisabled message="Export" tooltip="${message(code: 'export.titles.excelLimit')}"/>
                </g:else>
            </g:elseif>
            <%--
            <ui:exportDropdownItem>
                <g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                            data-confirm-tokenMsg="${message(code: 'confirmation.content.exportPartial')}"
                            data-confirm-term-how="ok" action="${actionName}"
                            params="${params + [exportXLSX: true]}">
                        <g:message code="default.button.exports.xls"/>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link class="item" action="${actionName}" params="${params+[exportXLSX: true]}">
                        <g:message code="default.button.exports.xls"/>
                    </g:link>
                </g:else>
            </ui:exportDropdownItem>
            --%>
            <ui:exportDropdownItem>
                <%--<g:if test="${filterSet}">
                    <g:link class="item js-open-confirm-modal"
                             data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                             data-confirm-term-how="ok"
                             action="${actionName}"
                             id="${params.id}"
                             params="${params + [exportKBart: true]}">KBART Export
                    </g:link>
                </g:if>
                <g:else>--%>
                    <g:link class="item kbartExport" params="${params + [exportKBart: true]}">KBART Export</g:link>
                <%--</g:else>--%>
            </ui:exportDropdownItem>
        <%--<ui:exportDropdownItem>
                <g:link class="item" controller="subscription" action="index" id="${subscription.id}" params="${params + [format:'json']}">JSON</g:link>
            </ui:exportDropdownItem>
            <ui:exportDropdownItem>
                <g:link class="item" controller="subscription" action="index" id="${subscription.id}" params="${params + [format:'xml']}">XML</g:link>
            </ui:exportDropdownItem>--%>
        </ui:exportDropdown>
</g:elseif>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionDropdownItems: true]]}" />

        <div class="divider"></div>

        <g:if test="${editable}">

            <g:if test="${(contextCustomerType == CustomerTypeService.ORG_INST_PRO && subscription._getCalculatedType() == Subscription.TYPE_LOCAL) || (customerTypeService.isConsortium( contextCustomerType ) && subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL)}">
                <ui:actionsDropdownItem controller="subscription" action="copySubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription), copyObject: true]}" message="myinst.copySubscription" />
            </g:if>
            <g:else>
                <ui:actionsDropdownItemDisabled controller="subscription" action="copySubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription), copyObject: true]}" message="myinst.copySubscription" />
            </g:else>

            <g:if test="${(contextCustomerType == CustomerTypeService.ORG_INST_PRO && !subscription.instanceOf) || customerTypeService.isConsortium( contextCustomerType )}">
                <ui:actionsDropdownItem controller="subscription" action="copyElementsIntoSubscription" params="${[sourceObjectId: genericOIDService.getOID(subscription)]}" message="myinst.copyElementsIntoSubscription" />
            </g:if>
            <g:if test="${customerTypeService.isConsortium( contextCustomerType ) && !subscription.instanceOf}">
                <div class="divider"></div>
                <ui:actionsDropdownItem data-ui="modal" id="generateFinanceImportWorksheet" href="#financeImportTemplate" message="myinst.financeImport.subscription.template"/>
                <ui:actionsDropdownItem controller="myInstitution" action="financeImport" params="${[id:subscription.id]}" message="menu.institutions.financeImport" />
            </g:if>
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
                <g:if test="${subscription.packages}">
                    <ui:actionsDropdownItem controller="subscription" action="addEntitlements" params="${[id:params.id]}" message="subscription.details.addEntitlements.label" />
                    <g:if test="${actionName == 'addEntitlements'}">
                        <ui:actionsDropdownItem data-ui="modal" id="selectEntitlementsWithKBART" href="#KBARTUploadForm" message="subscription.details.addEntitlements.menu"/>
                    </g:if>
                    <ui:actionsDropdownItem controller="subscription" action="manageEntitlementGroup" params="${[id:params.id]}" message="subscription.details.manageEntitlementGroup.label" />
                    <ui:actionsDropdownItem controller="subscription" action="index" notActive="true" params="${[id:params.id, issueEntitlementEnrichment: true]}" message="subscription.details.issueEntitlementEnrichment.label" />
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
                    <ui:actionsDropdownItemDisabled controller="subscription" action="renewSubscription"
                                                       params="${[id: params.id]}" tooltip="${message(code: 'subscription.details.renewals.isAlreadyRenewed')}" message="subscription.details.renewalsConsortium.label"/>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItem controller="subscription" action="renewSubscription"
                                           params="${[id: params.id]}" message="subscription.details.renewalsConsortium.label"/>
                </g:else>
            </g:if>
            <g:if test ="${subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL}">
                <g:if test ="${hasNext}">
                    <ui:actionsDropdownItemDisabled controller="subscription" action="renewSubscription"
                                                       params="${[id: params.id]}" tooltip="${message(code: 'subscription.details.renewals.isAlreadyRenewed')}" message="subscription.details.renewals.label"/>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItem controller="subscription" action="renewSubscription"
                                           params="${[id: params.id]}" message="subscription.details.renewals.label"/>
                </g:else>
            </g:if>
            <g:if test="${contextService.getOrg().isCustomerType_Consortium_Pro() && showConsortiaFunctions && subscription.instanceOf == null }">
                <ui:actionsDropdownItem controller="survey" action="addSubtoSubscriptionSurvey"
                                               params="${[sub:params.id]}" text="${message(code:'createSubscriptionSurvey.label')}" />

                <ui:actionsDropdownItem controller="survey" action="addSubtoIssueEntitlementsSurvey"
                                           params="${[sub:params.id]}" text="${message(code:'createIssueEntitlementsSurvey.label')}" />
            </g:if>
            <g:elseif test="${contextService.getOrg().isCustomerType_Consortium() && showConsortiaFunctions && subscription.instanceOf == null }">
                <ui:actionsDropdownItemDisabled controller="survey" action="addSubtoSubscriptionSurvey" params="${[sub:params.id]}" text="${message(code:'createSubscriptionSurvey.label')}"
                                                tooltip="${message(code: 'tooltip.onlyFullMembership')}" message="createSubscriptionSurvey.label"/>

                <ui:actionsDropdownItemDisabled controller="survey" action="addSubtoIssueEntitlementsSurvey" params="${[sub:params.id]}" text="${message(code:'createIssueEntitlementsSurvey.label')}"
                                                tooltip="${message(code: 'tooltip.onlyFullMembership')}" message="createIssueEntitlementsSurvey.label"/>
            </g:elseif>

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
                <g:if test="${!(contextOrg.id == subscriptionConsortia?.id && subscription.instanceOf)}">
                    <div class="divider"></div>
                    <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />
                </g:if>

                <g:if test="${editable}">
                    <div class="divider"></div>
                    <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.subscription')}</g:link>
                </g:if>
%{--                <g:else>--}%
%{--                    <a class="item disabled" href="#"><i class="trash alternate outline icon"></i> ${message(code:'deletion.subscription')}</a>--}%
%{--                </g:else>--}%
            </g:elseif>
        </g:if>
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${contextService.isInstEditor_or_ROLEADMIN()}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem message="template.addNote" data-ui="modal" href="#modalCreateNote" />

        <g:if test="${actionName == 'members' && subscriptionService.getValidSubChilds(subscription)}">
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            <div class="divider"></div>
        </g:if>
    </ui:actionsDropdown>
</g:elseif>
<g:elseif test="${contextService.isInstUser_or_ROLEADMIN()}">
    <g:if test="${actionName == 'members' && subscriptionService.getValidSubChilds(subscription)}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </ui:actionsDropdown>
    </g:if>
</g:elseif>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN()}">
    <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionModals: true, ownobj: subscription, owntp: 'subscription']]}" />
    <laser:render template="financeImportTemplate" />
</g:if>

<g:if test="${subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL}">
    <g:set var="previous" value="${subscription._getCalculatedPrevious()}"/>
    <g:set var="successor" value="${subscription._getCalculatedSuccessor()}"/>
    <laser:render template="subscriptionTransferInfo" model="${[calculatedSubList: successor + [subscription] + previous]}"/>
</g:if>


