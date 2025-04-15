<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.ExportClickMeService; de.laser.storage.RDStore; de.laser.Subscription" %>
<laser:htmlStart message="subscription.details.financials.label" />

<laser:render template="/templates/flyouts/dateCreatedLastUpdated" model="[obj: subscription]"/>
        <laser:serviceInjection />
        <g:set var="own" value="${financialData.own}"/>
        <g:set var="cons" value="${financialData.cons}"/>
        <g:set var="subscr" value="${financialData.subscr}"/>
        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <ui:breadcrumbs>
            <%--<ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}" />--%>
            <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label" />
            <ui:crumb class="active" text="${subscription.name}" />
        </ui:breadcrumbs>
        <ui:controlButtons>
            <ui:exportDropdown>
                <g:if test="${filterSet}">
                    <%--<ui:exportDropdownItem>
                        <g:link class="item js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[exportXLS:true,sub:subscription.id]}">${message(code:'default.button.exports.xls')}
                        </g:link>
                    </ui:exportDropdownItem>--%>
                    <ui:exportDropdownItem>
                        <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.COST_ITEMS]"/>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <g:link class="item exportCSV js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartialCSV')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[format:'csv',sub:subscription.id]}">${message(code:'default.button.exports.csv')}
                        </g:link>
                    </ui:exportDropdownItem>
                </g:if>
                <g:else>
                    <%--<ui:exportDropdownItem>
                        <g:link class="item" controller="finance" action="financialsExport" params="${params+[exportXLS:true,sub:subscription.id]}">${message(code:'default.button.exports.xls')}</g:link>
                    </ui:exportDropdownItem>--%>
                    <ui:exportDropdownItem>
                        <g:render template="/clickMe/export/exportDropdownItems" model="[clickMeType: ExportClickMeService.COST_ITEMS]"/>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <g:link class="item exportCSV js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportCSV')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[format:'csv',sub:subscription.id]}">${message(code:'default.button.exports.csv')}
                        </g:link>
                    </ui:exportDropdownItem>
                </g:else>
            </ui:exportDropdown>

            <g:if test="${editable}">
                <ui:actionsDropdown>
                    <ui:actionsDropdownItem id="btnAddNewCostItem" message="financials.addNewCost" />
                    <g:if test="${customerTypeService.isConsortium( institution.getCustomerType() ) && !subscription.instanceOf}">
                        <g:if test="${costItemElements}">
                            <ui:actionsDropdownItem data-ui="modal" id="openFinanceEnrichment" href="#financeEnrichment" message="financials.enrichment.menu" />
                        </g:if>
                        <g:else>
                            <ui:actionsDropdownItemDisabled message="financials.enrichment.menu" tooltip="${message(code:'financials.enrichment.menu.disabled')}" />
                        </g:else>
                        <div class="divider"></div>
                        <%-- merged into myInstitution/financeImport as of ERMS-6441
                        <ui:actionsDropdownItem data-ui="modal" id="generateFinanceImportWorksheet" href="#financeImportTemplate" message="myinst.financeImport.subscription.template"/>
                        --%>
                        <ui:actionsDropdownItem controller="myInstitution" action="financeImport" params="${[id:subscription.id]}" message="menu.institutions.financeImport" />
                        <ui:actionsDropdownItem controller="subscription" action="compareSubMemberCostItems" params="${[id:subscription.id]}" message="subscription.details.compareSubMemberCostItems.label" />
                    </g:if>
                </ui:actionsDropdown>
            </g:if>
        </ui:controlButtons>

        <%
            List<GString> total = []
            dataToDisplay.each { view ->
                switch(view) {
                    case 'own': total << "${own.count} ${message(code:'financials.header.ownCosts')}"
                        break
                    case [ 'cons', 'consAtSubscr' ]: total << "${cons.count} ${message(code:'financials.header.consortialCosts')}"
                        break
                    case 'subscr': total << "${subscr.count} ${message(code:'financials.header.subscriptionCosts')}"
                        break
                }
            }
        %>

        <ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" type="subscription" visibleProviders="${providerRoles}">
            <laser:render template="/subscription/iconSubscriptionIsChild"/>

            ${message(code:'subscription.details.financials.label')} ${message(code:'default.for')} ${subscription}
        </ui:h1HeaderWithIcon>
        <ui:totalNumber class="la-numberHeader" total="${total.join(' / ')}"/>
        <ui:anualRings mapping="subfinance" object="${subscription}" controller="finance" action="index" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>
        <g:if test="${subscription._getCalculatedType() == Subscription.TYPE_CONSORTIAL}">
            <g:set var="previous" value="${subscription._getCalculatedPrevious()}"/>
            <g:set var="successor" value="${subscription._getCalculatedSuccessor()}"/>
            <laser:render template="/subscription/subscriptionTransferInfo" model="${[calculatedSubList: successor + [subscription] + previous]}"/>
        </g:if>

        <g:if test="${editable && subscription.getConsortium()?.id == contextService.getOrg().id}">
            <laser:render template="/templates/flyouts/subscriptionMembers" model="[subscription: subscription]"/>
        </g:if>
        <laser:render template="/subscription/${customerTypeService.getNavTemplatePath()}" model="${[subscription:subscription, params:(params << [id:subscription.id, showConsortiaFunctions:showConsortiaFunctions])]}"/>

        <g:if test="${showConsortiaFunctions}">
            <laser:render template="/subscription/message" model="${[subscription: subscription]}"/>%{-- ERMS-6070 --}%
        </g:if>

        <g:if test="${afterEnrichment}">
            <g:if test="${wrongSeparator}">
                <ui:msg showIcon="true" class="error" message="financials.enrichment.wrongSeparator"/>
            </g:if>
            <g:else>
                <g:if test="${matchCounter > 0}">
                    <ui:msg showIcon="true" class="success" message="financials.enrichment.result" args="[matchCounter, totalRows]"/>
                </g:if>
                <g:else>
                    <ui:msg showIcon="true" class="warning" message="financials.enrichment.emptyResult" args="[totalRows]"/>
                </g:else>
                <g:if test="${missing || wrongIdentifiers}">
                    <ui:msg showIcon="true" class="error">
                        <g:if test="${missing}">
                            <p><g:message code="financials.enrichment.missingPrices"/></p>
                        </g:if>
                        <g:if test="${wrongIdentifiers}">
                            <p><g:message code="financials.enrichment.invalidIDs" args="[wrongIdentifierCounter]"/></p>
                            <p><g:link class="${Btn.ICON.SIMPLE}" controller="package" action="downloadLargeFile" params="[token: token, fileformat: 'txt']"><i class="${Icon.CMD.DOWNLOAD}"></i></g:link></p>
                        </g:if>
                    </ui:msg>
                </g:if>
            </g:else>
        </g:if>

        <laser:render template="result" model="[own:own,cons:cons,subscr:subscr,showView:showView,filterPresets:filterPresets,fixedSubscription:subscription,ciTitles:ciTitles,missing:missing]" />

        <laser:render template="/subscription/financeImportTemplate" />

        <laser:render template="/finance/financeEnrichment" />

        <g:render template="/clickMe/export/js"/>

<laser:htmlEnd />