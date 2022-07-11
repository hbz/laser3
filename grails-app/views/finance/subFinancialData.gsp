<laser:htmlStart message="subscription.details.financials.label" />

        <laser:serviceInjection />
        <g:set var="own" value="${financialData.own}"/>
        <g:set var="cons" value="${financialData.cons}"/>
        <g:set var="subscr" value="${financialData.subscr}"/>
        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <ui:breadcrumbs>
            <%--<ui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}" />--%>
            <ui:crumb controller="myInstitution" action="currentSubscriptions" text="${message(code:'myinst.currentSubscriptions.label')}" />
            <ui:crumb class="active"  message="${subscription.name}" />
        </ui:breadcrumbs>
        <ui:controlButtons>
            <ui:exportDropdown>
                <g:if test="${filterSet}">
                    <ui:exportDropdownItem>
                        <g:link  class="item js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[exportXLS:true,sub:subscription.id]}">${message(code:'default.button.exports.xls')}
                        </g:link>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <a class="item" data-ui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
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
                    <ui:exportDropdownItem>
                        <g:link class="item" controller="finance" action="financialsExport" params="${params+[exportXLS:true,sub:subscription.id]}">${message(code:'default.button.exports.xls')}</g:link>
                    </ui:exportDropdownItem>
                    <ui:exportDropdownItem>
                        <a class="item" data-ui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
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
                </ui:actionsDropdown>
            </g:if>
        </ui:controlButtons>

        <%
            List<GString> total = []
            dataToDisplay.each { view ->
                switch(view) {
                    case 'own': total << "${own.count} ${message(code:'financials.header.ownCosts')}"
                        break
                    case 'cons':
                    case 'consAtSubscr': total << "${cons.count} ${message(code:'financials.header.consortialCosts')}"
                        break
                    case 'subscr': total << "${subscr.count} ${message(code:'financials.header.subscriptionCosts')}"
                        break
                }
            }
        %>

        <ui:h1HeaderWithIcon type="Subscription">
            <laser:render template="/subscription/iconSubscriptionIsChild"/>

            ${message(code:'subscription.details.financials.label')} ${message(code:'default.for')} ${subscription} <ui:totalNumber total="${total.join(' / ')}"/>
        </ui:h1HeaderWithIcon>
        <ui:anualRings mapping="subfinance" object="${subscription}" controller="finance" action="index" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

        <laser:render template="/subscription/nav" model="${[subscription:subscription, params:(params << [id:subscription.id, showConsortiaFunctions:showConsortiaFunctions])]}"/>

        <g:if test="${showConsortiaFunctions || params.orgBasicMemberView}">
            <laser:render template="/subscription/message" model="${[contextOrg: institution, subscription: subscription]}"/>
        </g:if>

        <laser:render template="result" model="[own:own,cons:cons,subscr:subscr,showView:showView,filterPresets:filterPresets,fixedSubscription:subscription,ciTitles:ciTitles]" />

        <laser:render template="export/individuallyExportModal" model="[modalID: 'individuallyExportModal']" />

<laser:htmlEnd />