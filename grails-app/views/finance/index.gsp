<laser:htmlStart message="subscription.details.financials.label" serviceInjection="true"/>

        <g:set var="own" value="${financialData.own}"/>
        <g:set var="cons" value="${financialData.cons}"/>
        <g:set var="subscr" value="${financialData.subscr}"/>
        <ui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </ui:debugInfo>
        <ui:breadcrumbs>
            <ui:crumb controller="org" action="show" id="${institution.id}" text="${institution.getDesignation()}"/>
            <ui:crumb class="active" text="${message(code:'subscription.details.financials.label')}" />
        </ui:breadcrumbs>

        <ui:controlButtons>
            <ui:exportDropdown>
                <ui:exportDropdownItem>
                    <a class="item" data-ui="modal" href="#individuallyExportModal">Export</a>
                </ui:exportDropdownItem>
            </ui:exportDropdown>

            <g:if test="${editable}">
                <ui:actionsDropdown>
                    <ui:actionsDropdownItem id="btnAddNewCostItem" message="financials.addNewCost" />
                <%--<ui:actionsDropdownItemDisabled message="financials.action.financeImport" />--%>
                    <ui:actionsDropdownItem controller="myInstitution" action="financeImport" message="financials.action.financeImport" />
                </ui:actionsDropdown>
            </g:if>
        </ui:controlButtons>

        <%
            List<GString> total = []
            dataToDisplay.each { view ->
                switch(view) {
                    case 'own': total << "${own.count} ${message(code:'financials.header.ownCosts')}"
                        break
                    case 'cons': total << "${cons.count} ${message(code:'financials.header.consortialCosts')}"
                        break
                    case 'subscr': total << "${subscr.count} ${message(code:'financials.header.subscriptionCosts')}"
                        break
                }
            }
        %>

        <ui:h1HeaderWithIcon message="subscription.details.financials.label" type="finance" total="${total.join(' / ')}" floated="true" />

        <laser:render template="result" model="[own:own,cons:cons,subscr:subscr,showView:showView,filterPresets:filterPresets,ciTitles:ciTitles]" />

        <laser:render template="export/individuallyExportModal" model="[modalID: 'individuallyExportModal', subscription: null]" />
<laser:htmlEnd />