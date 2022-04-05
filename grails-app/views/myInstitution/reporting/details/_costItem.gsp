<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:render template="/myInstitution/reporting/details/top" />

<div class="ui segment">
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th></th>
            <th>
                ${message(code:'default.subscription.label')}
            </th>
            <th>
                ${message(code:'financials.newCosts.costParticipants')}
            </th>
            <th>
                ${message(code:'financials.costItemElement')}
            </th>
            <th></th>
            %{--
            <th>3</th>
            <th>4</th>
            --}%
            <th>
                ${message(code:'financials.taxRate')}
            </th>
            <th>
                ${message(code:'financials.sum.billing')}
                <br />
                %{-- ${message(code:'financials.sum.billingAfterTax')} --}%
            </th>
            <th>
                ${message(code:'financials.sum.local')}
                <br/>
                ${message(code:'financials.sum.billingAfterTax')} (nach Steuern)
            </th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${list}" var="costItem" status="i">
                <tr>
                    <td>${i + 1}.</td>
                    <td>
                        <g:link controller="subscription" action="show" id="${costItem.sub.instanceOf.id}" target="_blank">${costItem.sub.name}</g:link>
                    </td>
                    <td>
                        <g:each in="${costItem.sub.orgRelations}" var="ciSubscr">
                            <g:if test="${[RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id].contains(ciSubscr.roleType.id)}">
                                <g:link controller="org" action="show" id="${ciSubscr.org.id}">${ciSubscr.org.sortname ?: ciSubscr.org.name}</g:link>
                            </g:if>
                        </g:each>
                    </td>
                    <td>${costItem.costItemElement?.getI10n('value')}</td>
                    <td>${costItem.costTitle}</td>
                    %{--
                    <td>${costItem.costItemCategory}</td>
                    <td>${costItem.type?.getI10n('value')}</td>
                    --}%
                    <td>
                        ${costItem.taxKey?.taxRate}%
                    </td>
                    <td>
                        <g:if test="${costItem.costInBillingCurrency}">
                            <g:formatNumber number="${costItem.costInBillingCurrency}" type="currency" currencySymbol="${costItem.billingCurrency?.getI10n('value')}"/>
                            <br />
                            <g:formatNumber number="${costItem.costInBillingCurrencyAfterTax}" type="currency" currencySymbol="${costItem.billingCurrency?.getI10n('value')}"/>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${costItem.costInLocalCurrency}">
                            <g:formatNumber number="${costItem.costInLocalCurrency}" type="currency" currencySymbol="EUR"/>
                            <br />
                            <g:formatNumber number="${costItem.costInLocalCurrencyAfterTax}" type="currency" currencySymbol="EUR"/>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </tbody>
    </table>
</div>
