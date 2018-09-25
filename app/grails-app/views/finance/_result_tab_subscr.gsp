<!-- _result_tab_subscr.gsp -->
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>

<laser:serviceInjection />

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <th>Teilnehmer / ${message(code:'financials.newCosts.costTitle')}</th>
        <th class="two wide">${message(code:'financials.invoice_total')}</th>
        <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
        <th>${message(code:'financials.costItemElement')}</th>
        <th>${message(code:'financials.costItemStatus')}</th>
    </tr>
</thead>
<tbody>
    %{--Empty result set--}%
    <g:if test="${cost_items?.size() == 0}">
        <tr>
            <td colspan="8" style="text-align:center">
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
                    <g:set var="orgRoles" value="${OrgRole.findBySubAndRoleType(ci.sub, RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role'))}" />
                    <g:each in="${orgRoles}" var="or">
                        ${or.org}
                    </g:each>

                    <br />
                    ${ci.costTitle}
                </td>
                <td>
                    <span class="costData"
                          data-costInLocalCurrency="<g:formatNumber number="${ci.costInLocalCurrency}" locale="en" maxFractionDigits="2"/>"
                          data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                          data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                          data-costInBillingCurrency="<g:formatNumber number="${ci.costInBillingCurrency}" locale="en" maxFractionDigits="2"/>"
                          data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                    >
                        <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}" />
                        <br />
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}" /> (${ci.taxRate ?: 0}%)
                    </span>
                </td>
                <td>
                    <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" />
                    <br />
                    <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" /> (${ci.taxRate ?: 0}%)
                </td>
                <td>
                    ${ci.costItemElement.getI10n('value')}
                </td>
                <td>
                    ${ci.costItemStatus?.getI10n('value')}
                </td>

            </tr>
        </g:each>

    </g:else>
</tbody>
    <tfoot>
    <tr>
        <td colspan="8">
            <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong>
            <br/>
            <span class="sumOfCosts_${i}"></span>
        </td>
    </tr>
    </tfoot>
</table>

<!-- _result_tab_subscr.gsp -->
