<!-- _result_tab_owner.gsp -->
<%@page import="de.laser.helper.RDStore"%>
<laser:serviceInjection />

<table id="costTable_${i}" data-queryMode="${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'financials.newCosts.costTitle')}</th>
            <g:if test="${!forSingleSubscription}">
                <th>${message(code:'financials.newCosts.subscriptionHeader')}</th>
            </g:if>
            <th><span data-tooltip="${message(code:'financials.costItemConfiguration')}" data-position="top center"><i class="money bill alternate icon"></i></span></th>
            <th class="two wide">${message(code:'financials.invoice_total')}</th>
            <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
            <th>${message(code:'financials.costItemStatus')}</th>
            <th>${message(code:'financials.dateFrom')}<br />${message(code:'financials.dateTo')}</th>
            <th>${message(code:'financials.costItemElement')}</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        %{--Empty result set--}%
        <g:if test="${!data.count || data.count == 0}">
            <tr>
                <td colspan="10" style="text-align:center">
                    <br />
                    <g:if test="${msg}">${msg}</g:if>
                    <g:else>${message(code:'finance.result.filtered.empty')}</g:else>
                    <br />
                </td>
            </tr>
        </g:if>
        <g:else>
            <g:each in="${data.costItems}" var="ci" status="jj">
                <%
                    def elementSign = 'notSet'
                    String icon = ''
                    String dataTooltip = ""
                    if(ci.costItemElementConfiguration) {
                        elementSign = ci.costItemElementConfiguration
                    }
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
                        <% int offset = ownOffset ? ownOffset : 0 %>
                        ${ jj + 1 + offset }
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
                        <span data-position="right center" data-tooltip="${dataTooltip}">${raw(icon)}</span>
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>
                        <br />
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>  (${ci.taxRate ?: 0}%)
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
        </g:else>
    </tbody>
    <tfoot>
        <g:if test="${data.count > 0 && data.pageSums.billingSums}">
            <%
                int colspan = 2
                if(forSingleSubscription) {
                    colspan = 1
                }
            %>
            <tr>
                <th colspan="10">
                    ${message(code:'financials.totalCostOnPage')}
                </th>
            </tr>
            <g:each in="${data.pageSums.billingSums}" var="entry">
                <tr>
                    <td colspan="${colspan}">

                    </td>
                    <td colspan="2">
                        ${message(code:'financials.sum.billing')} ${entry.currency}<br>
                        ${message(code:'financials.sum.billingAfterTax')}
                    </td>
                    <td class="la-exposed-bg">
                        <g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/><br>
                        <g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/>
                    </td>
                    <td colspan="5">

                    </td>
                </tr>
            </g:each>
            <tr>
                <td colspan="${colspan}">

                </td>
                <td colspan="3">
                    ${message(code:'financials.sum.local')}<br>
                    ${message(code:'financials.sum.localAfterTax')}
                </td>
                <td class="la-exposed-bg">
                    <g:formatNumber number="${data.pageSums.localSums.localSum}" type="currency" currencySymbol="EUR"/><br>
                    <g:formatNumber number="${data.pageSums.localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/>
                </td>
                <td colspan="4">

                </td>
            </tr>
            <tr>
                <th colspan="10">
                    ${message(code:'financials.totalCost')}
                </th>
            </tr>
            <g:each in="${data.sums.billingSums}" var="entry">
                <tr>
                    <td colspan="${colspan}">

                    </td>
                    <td colspan="2">
                        ${message(code:'financials.sum.billing')} ${entry.currency}<br>
                        ${message(code:'financials.sum.billingAfterTax')}
                    </td>
                    <td class="la-exposed-bg">
                        <g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/><br>
                        <g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/>
                    </td>
                    <td colspan="5">

                    </td>
                </tr>
            </g:each>
            <tr>
                <td colspan="${colspan}">

                </td>
                <td colspan="3">
                    ${message(code:'financials.sum.local')}<br>
                    ${message(code:'financials.sum.localAfterTax')}
                </td>
                <td class="la-exposed-bg">
                    <g:formatNumber number="${data.sums.localSums.localSum}" type="currency" currencySymbol="EUR"/><br>
                    <g:formatNumber number="${data.sums.localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/>
                </td>
                <td colspan="4">

                </td>
            </tr>
        </g:if>
        <g:elseif test="${data.count > 0 && !data.pageSums.billingSums}">
            <tr>
                <td colspan="10">
                    ${message(code:'financials.noCostsConsidered')}
                </td>
            </tr>
        </g:elseif>
        <tr>
            <td colspan="10">
                <div class="ui fluid accordion">
                    <div class="title">
                        <i class="dropdown icon"></i>
                        <strong>${message(code: 'financials.calculationBase')}</strong>
                    </div>
                    <div class="content">
                        <%
                            def argv0 = contextService.getOrg().costConfigurationPreset ? contextService.getOrg().costConfigurationPreset.getI10n('value') : message(code:'financials.costItemConfiguration.notSet')
                        %>
                        ${message(code: 'financials.calculationBase.paragraph1', args: [argv0])}
                        <p>
                            ${message(code: 'financials.calculationBase.paragraph2')}
                        </p>
                    </div>
                </div>
            </td>
        </tr>
    </tfoot>
</table>
<g:if test="${data.costItems}">
    <g:if test="${inSubMode}">
        <semui:paginate mapping="subfinance" action="index" controller="finance" params="${params+[view:'owner']}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}" offset="${ownerOffset ? ownerOffset : 1}" total="${data.count}"/>
    </g:if>
    <g:else>
        <semui:paginate action="finance" controller="myInstitution" params="${params+[view:'owner']}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}" offset="${ownerOffset ? ownerOffset : 1}" total="${data.count}"/>
    </g:else>
</g:if>
<!-- _result_tab_owner.gsp -->
