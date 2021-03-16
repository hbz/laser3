<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<div class="ui message success">
    <p>${label}</p>
</div>

<g:if test="${billingSums.size() == 1}">
    <div class="ui segment">
        <table class="ui table la-table compact">
            <thead>
            <tr>
                <th style="width:25%"></th>
                <th style="width:25%"></th>
                <th style="width:25%">Wert</th>
                <th style="width:25%">Endpreis (nach Steuern)</th>
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
    </div>
</g:if>
<g:else>
    <div class="ui segment">
        <table class="ui table la-table compact">
            <thead>
            <tr>
                <th style="width:25%">Rechnungssumme</th>
                <th style="width:25%">Endpreis</th>
                <th style="width:25%">Wert</th>
                <th style="width:25%">Endpreis (nach Steuern)</th>
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
    </div>
</g:else>



