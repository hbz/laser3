<%@page import="de.laser.interfaces.CalculatedType" %>
<g:if test="${data.billingSums}">
    <table class="ui compact la-table-inCard table">
        <thead>
        <tr>
            <th>${message(code:'financials.sum.billing')}</th>
            <th>${message(code:'financials.sum.billingAfterTax')}</th>
            <th>${message(code:'financials.sum.local')}</th>
            <th>${message(code:'financials.sum.localAfterTax')}</th>
        </tr>
        </thead>
        <tbody>
        <g:set var="sums" value="${data.billingSums}" />
        <g:each in="${sums}" var="entry">
            <g:set var="currency" value="${entry.currency}" />
            <tr>
                <td><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                <td><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                <td><g:formatNumber number="${data.localSums.localSum}" type="currency" currencySymbol="EUR"/></td>
                <td><g:formatNumber number="${data.localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/></td>
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