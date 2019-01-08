<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.CostItemElementConfiguration;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection />

<g:each in="${cost_items}" var="ci" status="jj">
        <%
            def org = contextService.getOrg()
            def elementSign = 'notSet'
            def icon = ''
            def dataTooltip = ""
            if(ci.costItemElement) {
                def cie = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(ci.costItemElement, org)
                if(cie) {
                    elementSign = cie.elementSign
                }
            }
            String cieString = "data-elementSign=${elementSign}"
            switch(elementSign) {
                case RDStore.CIEC_POSITIVE:
                    dataTooltip = message(code:'financials.costItemConfiguration.positive')
                    icon = '<i class="plus green circle icon"></i>'
                    break
                case RDStore.CIEC_NEGATIVE:
                    dataTooltip = message(code:'financials.costItemConfiguration.negative')
                    icon = '<i class="minus red circle icon"></i>'
                    break
                case RDStore.CIEC_NEUTRAL:
                    dataTooltip = message(code:'financials.costItemConfiguration.neutral')
                    icon = '<i class="circle yellow icon"></i>'
                    break
                default:
                    dataTooltip = message(code:'financials.costItemConfiguration.notSet')
                    icon = '<i class="question circle icon"></i>'
                    break
            }
        %>
    <tr id="bulkdelete-b${ci.id}">
        <td>
            ${ jj + 1 + counterHelper}
        </td>
        <td>
            <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
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
                  ${cieString}
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
            <span data-position="right center" data-tooltip="${dataTooltip}">${raw(icon)}</span>
        </td>
        <td class="x">
            <g:if test="${editable}">
                <g:if test="${forSingleSubcription}">
                    <g:link mapping="subfinanceEditCI" params='[fixedSub:"${fixedSubscription?.id}", id:"${ci.id}", tab:"owner"]' class="ui icon button trigger-modal">
                        <i class="write icon"></i>
                    </g:link>
                    <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                        <g:link mapping="subfinanceCopyCI" params='[fixedSub:"${fixedSubscription?.id}", id:"${ci.id}", tab:"owner"]' class="ui icon button trigger-modal">
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