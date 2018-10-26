<!-- _result_tab_subscr.gsp -->
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>

<laser:serviceInjection />

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <th class="two wide">${message(code:'financials.invoice_total')}</th>
        <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
        <th>${message(code:'financials.costItemElement')}</th>
        <th>Lizenz</th>
        <th>Paket</th>
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

        <g:each in="${cost_items}" var="ci" status="jj">
            <tr id="bulkdelete-b${ci.id}">
                <td>
                    ${ jj + 1 }
                </td>
                <td>
                    <span class="costData"
                          data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                          data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                          data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                    >
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}" />
                    </span>
                </td>
                <td>
                    <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" />
                </td>
                <td>
                    ${ci.costItemElement?.getI10n('value')}
                </td>
                <td>
                    <g:link controller="subscriptionDetails" action="show" id="${ci.sub?.id}">${ci.sub}</g:link>
                </td>
                <td>
                    <g:link controller="packageDetails" action="show" id="${ci.subPkg?.pkg?.id}">${ci.subPkg?.pkg}</g:link>
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

<!-- _result_tab_subscr.gsp -->
