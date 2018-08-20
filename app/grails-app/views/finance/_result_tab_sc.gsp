<!-- _result_tab_sc.gsp -->
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>

<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'financials.newCosts.costTitle')}</th>
        <th class="two wide">${message(code:'financials.costInBillingCurrency')}</th>
        <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
        <th>${message(code:'financials.costItemElement')}</th>
        <th>${message(code:'financials.costItemStatus')}</th>
        <th></th>
    </tr>
</thead>
<tbody>
    %{--Empty result set--}%
    <g:if test="${cost_items?.size() == 0}">
        <tr>
            <td colspan="7" style="text-align:center">
                <br />
                <g:if test="${msg}">${msg}</g:if>
                <g:else>${message(code:'finance.result.filtered.empty')}</g:else>
                <br />
            </td>
        </tr>
    </g:if>
    <g:else>

        <g:each in="${cost_items}" var="ci">
            <tr id="bulkdelete-b${ci.id}">
                <td>
                    <g:set var="orgRoles" value="${OrgRole.findBySubAndRoleType(ci.sub, RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role'))}" />
                    <g:each in="${orgRoles}" var="or">
                        <g:link mapping="subfinance" params="[sub:or.sub.id]">${or.org}</g:link>
                    </g:each>

                    <br />
                    &nbsp; &nbsp; <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
                </td>
                <td>
                    <span class="costData"
                          data-costInLocalCurrency="<g:formatNumber number="${ci.costInLocalCurrency}" locale="en" maxFractionDigits="2"/>"
                          data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax}" locale="en" maxFractionDigits="2"/>"
                          data-billingCurrency="${ci.billingCurrency}"
                          data-costInBillingCurrency="<g:formatNumber number="${ci.costInBillingCurrency}" locale="en" maxFractionDigits="2"/>"
                          data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax}" locale="en" maxFractionDigits="2"/>"
                    >
                        <g:formatNumber number="${ci.costInBillingCurrency}" type="currency" currencyCode="${ci.billingCurrency}" />
                        <br />
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax}" type="currency" currencyCode="${ci.billingCurrency}" />
                    </span>
                </td>
                <td>
                    <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" />
                    <br />
                    <g:formatNumber number="${ci.costInLocalCurrencyAfterTax}" type="currency" currencyCode="EUR" />
                </td>
                <td>
                    <semui:xEditableRefData config="CostItemElement" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemElement" />
                </td>
                <td>
                    <semui:xEditableRefData config="CostItemStatus" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemStatus" />
                </td>

                <td class="x">
                    <g:if test="${editable}">
                        <g:if test="${inSubMode}">
                            <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}"]' class="ui icon button">
                                <i class="write icon"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="finance" action="editCostItem" id="${ci.id}" class="ui icon button">
                                <i class="write icon"></i>
                            </g:link>
                        </g:else>
                    </g:if>
                    <g:if test="${editable}">
                        <g:link controller="finance" action="deleteCostItem" id="${ci.id}" class="ui icon negative button" onclick="return confirm('${message(code: 'default.button.confirm.delete')}')">
                            <i class="trash alternate icon"></i>
                        </g:link>
                    </g:if>
                </td>

            </tr>
        </g:each>

    </g:else>
</tbody>
    <tfoot>
    <tr>
        <td colspan="7">
            <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong>
            <br/>
            <span class="sumOfCosts_${i}"></span>
        </td>
    </tr>
    </tfoot>
</table>

<!-- _result_tab_sc.gsp -->
