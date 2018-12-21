<laser:serviceInjection />

<g:each in="${cost_items}" var="ci" status="jj">
    <tr id="bulkdelete-b${ci.id}">
        <td>
            ${ jj + 1 }
        </td>
        <td>
            <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />

            <g:if test="${ci.isVisibleForSubscriber}">
                <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                    <i class="ui icon eye orange"></i>
                </span>
            </g:if>
        </td>
        <g:if test="${!forSingleSubscription}">
            <td>
                <g:if test="${ci.sub}">${ci.sub} (${formatDate(date:ci.sub.startDate,format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.endDate, format: message(code: 'default.date.format.notime'))})</g:if>
                <g:else>${message(code:'financials.clear')}</g:else>
            </td>
        </g:if>
        <td>
            <span class="costData"
                  data-costInLocalCurrency="<g:formatNumber number="${ci.costInLocalCurrency}" locale="en" maxFractionDigits="2"/>"
                  data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                  data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                  data-costInBillingCurrency="<g:formatNumber number="${ci.costInBillingCurrency}" locale="en" maxFractionDigits="2"/>"
                  data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
            >
                <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>
                <br />
                <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>  (${ci.taxRate ?: 0}%)
            </span>
        </td>
        <td>
            <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" />
            <br />
            <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" />  (${ci.taxRate ?: 0}%)
        </td>

        <td>
            <semui:xEditableRefData config="CostItemStatus" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemStatus" />
        </td>
        <td>
            <semui:xEditable owner="${ci}" type="date" field="startDate" />
            <br />
            <semui:xEditable owner="${ci}" type="date" field="endDate" />
        </td>

        <td>
            <semui:xEditableRefData config="CostItemElement" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemElement" />
        </td>

        <td class="x">
            <g:if test="${editable}">
                <g:if test="${inSubMode}">
                    <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}", tab:"owner"]' class="ui icon button trigger-modal">
                        <i class="write icon"></i>
                    </g:link>
                    <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                        <g:link mapping="subfinanceCopyCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}", tab:"owner"]' class="ui icon button trigger-modal">
                            <i class="copy icon"></i>
                        </g:link>
                    </span>
                </g:if>
                <g:else>
                    <g:link controller="finance" action="editCostItem" id="${ci.id}" class="ui icon button trigger-modal">
                        <i class="write icon"></i>
                    </g:link>
                </g:else>

            </g:if>
            <g:if test="${editable}">
                <g:link controller="finance" action="deleteCostItem" id="${ci.id}" params="[ tab:'owner']" class="ui icon negative button" onclick="return confirm('${message(code: 'default.button.confirm.delete')}')">
                    <i class="trash alternate icon"></i>
                </g:link>
            </g:if>
        </td>

    </tr>
</g:each>