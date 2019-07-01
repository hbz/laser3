<%--
  Created by IntelliJ IDEA.
  User: galffy
  Date: 07.02.2019
  Time: 08:56
--%>
<!doctype html>
<html xmlns="http://www.w3.org/1999/html">
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.details.financials.label')}</title>
    </head>
    <body>
        <laser:serviceInjection />
        <g:set var="own" value="${financialData.own}"/>
        <g:set var="cons" value="${financialData.cons}"/>
        <g:set var="subscr" value="${financialData.subscr}"/>
        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution.name}" />
            <semui:crumb class="active" text="${message(code:'subscription.details.financials.label')}" />
        </semui:breadcrumbs>

        <semui:controlButtons>
            <semui:exportDropdown>
                <g:if test="${(params.submit && params.filterSubStatus) || params.filterSubStatus}">
                    <semui:exportDropdownItem>
                        <g:link  class="item js-open-confirm-modal"
                                 data-confirm-term-content = "${message(code: 'confirmation.content.exportPartial', default: 'Achtung!  Dennoch fortfahren?')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls', default:'XLS Export')}
                        </g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item exportCSV js-open-confirm-modal"
                                 data-confirm-term-content = "${message(code: 'confirmation.content.exportPartialCSV')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:if>
                <g:else>
                    <semui:exportDropdownItem>
                        <g:link class="item" controller="finance" action="financialsExport" params="${params+[exportXLS:true]}">${message(code:'default.button.exports.xls', default:'XLS Export')}</g:link>
                    </semui:exportDropdownItem>
                    <semui:exportDropdownItem>
                        <g:link class="item exportCSV js-open-confirm-modal"
                                 data-confirm-term-content = "${message(code: 'confirmation.content.exportCSV')}"
                                 data-confirm-term-how="ok"
                                 controller="finance"
                                 action="financialsExport"
                                 params="${params+[format:'csv']}">${message(code:'default.button.exports.csv')}
                        </g:link>
                    </semui:exportDropdownItem>
                </g:else>
            <%--
            <semui:exportDropdownItem>
                <a data-mode="sub" class="disabled export" style="cursor: pointer">CSV Costs by Subscription</a>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <a data-mode="code" class="disabled export" style="cursor: pointer">CSV Costs by Code</a>
            </semui:exportDropdownItem>
            --%>
            </semui:exportDropdown>

            <g:if test="${editable}">
                <semui:actionsDropdown>
                    <semui:actionsDropdownItem id="btnAddNewCostItem" message="financials.addNewCost" />
                <%--<semui:actionsDropdownItemDisabled message="financials.action.financeImport" />--%>
                    <semui:actionsDropdownItem controller="myInstitution" action="financeImport" message="financials.action.financeImport" />
                </semui:actionsDropdown>
            </g:if>
        </semui:controlButtons>

        <g:if test="${showView.equals("cons")}">
            <g:set var="totalString" value="${own.count ? own.count : 0} ${message(code:'financials.header.ownCosts')} / ${cons.count} ${message(code:'financials.header.consortialCosts')}"/>
        </g:if>
        <g:elseif test="${showView.equals("consAtSubscr")}">
            <g:set var="totalString" value="${cons.count ? cons.count : 0} ${message(code:'financials.header.consortialCosts')}"/>
        </g:elseif>
        <g:elseif test="${showView.equals("subscr") && accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_USER")}">
            <g:set var="totalString" value="${own.count ? own.count : 0} ${message(code:'financials.header.ownCosts')} / ${subscr.count} ${message(code:'financials.header.subscriptionCosts')}"/>
        </g:elseif>
        <g:elseif test="${accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_USER")}">
            <g:set var="totalString" value="${own.count ? own.count : 0} ${message(code:'financials.header.ownCosts')}"/>
        </g:elseif>
        <g:else>
            <g:set var="totalString" value="${subscr.count} ${message(code:'financials.header.subscriptionCosts')}"/>
        </g:else>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'subscription.details.financials.label')}<semui:totalNumber total="${totalString}"/></h1>
        <g:render template="result" model="[own:own,cons:cons,subscr:subscr,view:view,showView:showView,filterPresets:filterPresets]" />
    </body>
</html>