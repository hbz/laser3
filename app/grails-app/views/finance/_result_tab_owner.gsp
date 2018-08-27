<!-- _result_tab_owner.gsp -->
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'financials.newCosts.costTitle')}</th>
        <th class="two wide">${message(code:'financials.invoice_total')}</th>
        <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
        <th>${message(code:'financials.costItemElement')}</th>
        <th>${message(code:'financials.costItemStatus')}</th>
        <th>${message(code:'financials.dateFrom')} - ${message(code:'financials.dateTo')}</th>
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
                <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
            </td>
            <td>
                <span class="costData"
                      data-costInLocalCurrency="<g:formatNumber number="${ci.costInLocalCurrency}" locale="en" maxFractionDigits="2"/>"
                      data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                      data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                      data-costInBillingCurrency="<g:formatNumber number="${ci.costInBillingCurrency}" locale="en" maxFractionDigits="2"/>"
                      data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                >
                    <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>
                    <br />
                    <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>  (${ci.taxRate ?: 0}%)
                </span>
            </td>
            <td>
                <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" />
                <br />
                <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" />  (${ci.taxRate ?: 0}%)
            </td>
            <td>
                <semui:xEditableRefData config="CostItemElement" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemElement" />
            </td>
            <td>
                <semui:xEditableRefData config="CostItemStatus" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemStatus" />
            </td>
            <td>
                <semui:xEditable owner="${ci}" type="date" field="startDate" />
                <br />
                <semui:xEditable owner="${ci}" type="date" field="endDate" />
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
                <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')} </strong>
                <br/>
                <span class="sumOfCosts_${i}"></span>
            </td>
        </tr>
    </tfoot>
</table>

<!-- _result_tab_owner.gsp -->
