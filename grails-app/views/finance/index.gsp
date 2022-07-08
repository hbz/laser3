<laser:htmlStart message="subscription.details.financials.label" serviceInjection="true"/>

        <g:set var="own" value="${financialData.own}"/>
        <g:set var="cons" value="${financialData.cons}"/>
        <g:set var="subscr" value="${financialData.subscr}"/>
        <semui:debugInfo>
            <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]" />
        </semui:debugInfo>
        <semui:breadcrumbs>
            <%--<semui:crumb controller="myInstitution" action="dashboard" text="${institution.name}" />--%>
            <semui:crumb class="active" text="${message(code:'subscription.details.financials.label')}" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <g:if test="${filterSet}">
                    <semui:exportDropdownItem>
                        <g:link  class="item js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartial')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <a class="item" data-semui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item exportCSV js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportPartialCSV')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:if>
                <g:else>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="finance" action="financialsExport" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls')}</g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <a class="item" data-semui="modal" href="#individuallyExportModal">Click Me Excel Export</a>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item exportCSV js-open-confirm-modal"
                                 data-confirm-tokenMsg = "${message(code: 'confirmation.content.exportCSV')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:else>
            </semui:exportDropdown>

            <g:if test="${editable}">
                <semui:actionsDropdown>
                    <semui:actionsDropdownItem id="btnAddNewCostItem" message="financials.addNewCost" />
                <%--<semui:actionsDropdownItemDisabled message="financials.action.financeImport" />--%>
                    <semui:actionsDropdownItem controller="myInstitution" action="financeImport" message="financials.action.financeImport" />
                </semui:actionsDropdown>
            </g:if>
        </semui:controlButtons>

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

        <semui:h1HeaderWithIcon message="subscription.details.financials.label" total="${total.join(' / ')}" floated="true" />

        <laser:render template="result" model="[own:own,cons:cons,subscr:subscr,showView:showView,filterPresets:filterPresets,ciTitles:ciTitles]" />

        <laser:render template="export/individuallyExportModal" model="[modalID: 'individuallyExportModal']" />
<laser:htmlEnd />