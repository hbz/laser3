<%@page import="de.laser.interfaces.TemplateSupport" %>
<g:if test="${data.billingSums}">
    <table class="ui la-table-small la-table-inCard table">
        <thead>
        <tr>
            <th>${message(code:'financials.costInBillingCurrency')}</th>
            <th>${message(code:'financials.billingCurrency')}</th>
            <th>${message(code:'financials.newCosts.valueInEuro')}</th>
            <g:if test="${subscriptionInstance.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_CONSORTIAL)}">
                <th>${message(code:'financials.costInBillingCurrencyAfterTax')}</th>
                <th>${message(code:'financials.billingCurrency')}</th>
                <th>${message(code:'financials.newCosts.valueInEuro')}</th>
            </g:if>
        </tr>
        </thead>
        <tbody>
        <g:set var="sums" value="${data.billingSums}" />
        <g:each in="${sums}" var="entry">
            <g:set var="currency" value="${entry.currency}" />
            <tr>
                <g:if test="${subscriptionInstance.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_CONSORTIAL)}">
                    <td><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol=""/></td>
                    <td>${entry.currency}</td>
                    <td><g:formatNumber number="${data.localSums.localSum}" type="currency" currencySymbol=""/></td>
                    <td><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol=""/></td>
                    <td>${entry.currency}</td>
                    <td><g:formatNumber number="${data.localSums.localSumAfterTax}" type="currency" currencyCode="EUR" currencySymbol=""/></td>
                </g:if>
                <g:else>
                    <td><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol=""/></td>
                    <td>${entry.currency}</td>
                    <td><g:formatNumber number="${data.localSums.localSumAfterTax}" type="currency" currencyCode="EUR" currencySymbol=""/></td>
                </g:else>
            </tr>
        </g:each>
        </tbody>
    </table>
</g:if>
<g:else>
    <dl>
        <dd>${message(code:'financials.noCostsConsidered')}</dd>
    </dl>
</g:else>