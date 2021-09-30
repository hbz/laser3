<%@page import="de.laser.interfaces.CalculatedType; de.laser.helper.RDStore" %>


<g:if test="${data.billingSums}">
    <table class="ui compact table">
        <g:set var="sums" value="${data.billingSums}" />
        <g:each in="${sums}" var="entry">
            <g:set var="currency" value="${entry.currency}" />

            <g:if test="${entry.currency == RDStore.CURRENCY_USD.value}">
                <g:set var="iconCurrency" value="dollar sign" />
            </g:if>
            <g:elseif test="${entry.currency == RDStore.CURRENCY_GBP.value}">
                <g:set var="iconCurrency" value="pound sign" />
            </g:elseif>
            <g:else>
                <g:set var="iconCurrency" value="money bill alternate" />
            </g:else>

            <g:if test="${entry.currency != RDStore.CURRENCY_EUR.value}">
                <tr>
                    <td rowspan="2">
                        <i class="bordered large ${iconCurrency} icon a-timeLineIcon la-timeLineIcon-contact"></i>
                    </td>
                    <th class="control-label">${message(code:'financials.sum.billing')}</th>
                    <td class="right aligned"><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                </tr>
                <tr>
                    <th class="control-label">${message(code:'financials.sum.billingAfterTax')}</th>
                    <td class="right aligned"><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                </tr>
            </g:if>
            <g:if test="${entry.localSum > 0}">
                <tr>
                    <td rowspan="2">
                        <i class="bordered large euro sign icon a-timeLineIcon la-timeLineIcon-contact"></i>
                    </td>
                    <th class="control-label">${message(code:'financials.sum.local')}</th>
                    <td class="right aligned"><g:formatNumber number="${entry.localSum}" type="currency" currencySymbol="EUR"/></td>
                </tr>
                <tr>
                    <th class="control-label">${message(code:'financials.sum.localAfterTax')}</th>
                    <td class="right aligned" ><g:formatNumber number="${entry.localSumAfterTax}" type="currency" currencySymbol="EUR"/></td>
                </tr>
            </g:if>


        </g:each>

    </table>
</g:if>

<g:else>
    <dl>
        <dd>${message(code:'financials.noCostsConsidered')}</dd>
    </dl>
</g:else>