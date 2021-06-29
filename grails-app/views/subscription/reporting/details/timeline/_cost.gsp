<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<div class="ui segment">
    <g:if test="${billingSums.size() == 1}">
        <table class="ui table la-table compact">
            <thead>
            <tr>
                <th style="width:25%"></th>
                <th style="width:25%"></th>
                <th style="width:25%">${message(code:'financials.sum.local')}</th>
                <th style="width:25%">${message(code:'financials.sum.billingAfterTax')} (nach Steuern)</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${billingSums}" var="entry" status="i">
                <tr>
                    <td style="width:25%"></td>
                    <td style="width:25%"></td>
                    <td style="width:25%"><g:formatNumber number="${localSums.localSum}" type="currency" currencySymbol="EUR"/></td>
                    <td style="width:25%"><g:formatNumber number="${localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:elseif test="${localSums}">
        <table class="ui table la-table compact">
            <thead>
            <tr>
                <th style="width:25%">${message(code:'financials.sum.billing')}</th>
                <th style="width:25%">${message(code:'financials.sum.billingAfterTax')}</th>
                <th style="width:25%">${message(code:'financials.sum.local')}</th>
                <th style="width:25%">${message(code:'financials.sum.billingAfterTax')} (nach Steuern)</th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${billingSums}" var="entry" status="i">
                    <tr>
                        <td style="width:25%"><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                        <td style="width:25%"><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                        <td style="width:25%"></td>
                        <td style="width:25%"></td>
                    </tr>
                </g:each>
                <tr>
                    <td style="width:25%"></td>
                    <td style="width:25%"></td>
                    <td style="width:25%"><strong><g:formatNumber number="${localSums.localSum}" type="currency" currencySymbol="EUR"/></strong></td>
                    <td style="width:25%"><strong><g:formatNumber number="${localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/></strong></td>
                </tr>
            </tbody>
        </table>
    </g:elseif>
    <g:else>
        Es wurden keine Kosten gefunden.
    </g:else>
</div>



