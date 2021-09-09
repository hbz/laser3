<%@page import="de.laser.interfaces.CalculatedType; de.laser.helper.RDStore" %>

<style>
    th,.la-label {
        text-transform: uppercase;
        font-size: .8rem;
        color: #515151;
        font-weight: 700;
    }
</style>
<g:if test="${data.billingSums}">
    <table class="ui  table">
        <g:set var="sums" value="${data.billingSums}" />
        <g:each in="${sums}" var="entry">
            <g:set var="currency" value="${entry.currency}" />
            <g:if test="${entry.currency != RDStore.CURRENCY_EUR.value}">
                <tr>
                    <td rowspan="2" style="padding: 0.4em 1.4em;">
                        <i class="bordered large euro sign icon a-timeLineIcon la-timeLineIcon-contact"></i>
                    </td>
                    <th>${message(code:'financials.sum.billing')}</th>
                    <td class="right aligned"><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                </tr>
                <tr>
                    <th>${message(code:'financials.sum.billingAfterTax')}</th>
                    <td class="right aligned"><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                </tr>
            </g:if>
            <tr>
                <td rowspan="2" style="width:20px; padding: 0.4em 1.4em">
                    <i class="bordered large euro sign icon a-timeLineIcon la-timeLineIcon-contact"></i>
                </td>
                <td class="la-label" style="padding: 0.4em 1.4em;">${message(code:'financials.sum.local')}</td>
                <td class="right aligned" style="padding: 0.4em 1.4em;"><g:formatNumber number="${data.localSums.localSum}" type="currency" currencySymbol="EUR"/></td>
            </tr>
            <tr>

                <td class="la-label" style="padding: 0.4em 1.4em;">${message(code:'financials.sum.localAfterTax')}</th>
                <td class="right aligned" style="padding: 0.4em 1.4em;"><g:formatNumber number="${data.localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/></td>
            </tr>

        </g:each>

    </table>
</g:if>

<g:else>
    <dl>
        <dd>${message(code:'financials.noCostsConsidered')}</dd>
    </dl>
</g:else>