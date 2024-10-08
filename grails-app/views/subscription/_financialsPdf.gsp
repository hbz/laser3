<%@page import="de.laser.ui.Icon; de.laser.interfaces.CalculatedType; de.laser.storage.RDStore" %>


<g:if test="${data.billingSums}">
    <table>
        <g:set var="sums" value="${data.billingSums}" />
        <g:each in="${sums}" var="entry">
            <g:set var="currency" value="${entry.currency}" />

            <g:if test="${entry.currency == RDStore.CURRENCY_USD.value}">
                <g:set var="iconCurrency" value="dollar sign icon" />
            </g:if>
            <g:elseif test="${entry.currency == RDStore.CURRENCY_GBP.value}">
                <g:set var="iconCurrency" value="pound sign icon" />
            </g:elseif>
            <g:else>
                <g:set var="iconCurrency" value="${Icon.FNC.COST_CONFIG}" />
            </g:else>

            <g:if test="${entry.currency != RDStore.CURRENCY_EUR.value}">
                <tr>
                    <th>${message(code:'financials.sum.billing')}</th>
                    <td><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                </tr>
                <tr>
                    <th>${message(code:'financials.sum.billingAfterTax')}</th>
                    <td><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                </tr>
            </g:if>
            <g:if test="${entry.localSum > 0}">
                <tr>
                    <th>${message(code:'financials.sum.local')}</th>
                    <td><g:formatNumber number="${entry.localSum}" type="currency" currencySymbol="EUR"/></td>
                </tr>
                <tr>
                    <th>${message(code:'financials.sum.localAfterTax')}</th>
                    <td><g:formatNumber number="${entry.localSumAfterTax}" type="currency" currencySymbol="EUR"/></td>
                </tr>
            </g:if>


        </g:each>

    </table>
</g:if>

<g:else>
    <section>
        ${message(code:'financials.noCostsConsidered')}
    </section>
</g:else>