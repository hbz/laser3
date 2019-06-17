<!-- _result_tab_owner.gsp -->
<%@page import="de.laser.helper.RDStore"%>
<laser:serviceInjection />

<table id="costTable_${i}" data-queryMode="${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">
    <thead>
        <tr>
            <g:if test="${!fixedSubscription}">
                <th>${message(code:'sidewide.number')}</th>
                <g:sortableColumn property="ci.costTitle" title="${message(code:'financials.newCosts.costTitle')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.sub.name" title="${message(code:'financials.forSubscription')}" params="[ownSort: true]"/>
                <th><span data-tooltip="${message(code:'financials.costItemConfiguration')}" data-position="top center"><i class="money bill alternate icon"></i></span></th>
                <g:sortableColumn property="ci.costInBillingCurrency" title="${message(code:'financials.invoice_total')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.costInLocalCurrency" title="${message(code:'financials.newCosts.valueInEuro')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.costItemStatus" title="${message(code:'financials.costItemStatus')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.startDate" title="${message(code:'financials.dateFrom')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.costItemElement" title="${message(code:'financials.costItemElement')}" params="[ownSort: true]"/>
                <th></th>
            </g:if>
            <g:else>
                <th>${message(code:'sidewide.number')}</th>
                <g:sortableColumn property="ci.costTitle" title="${message(code:'financials.newCosts.costTitle')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <th><span data-tooltip="${message(code:'financials.costItemConfiguration')}" data-position="top center"><i class="money bill alternate icon"></i></span></th>
                <g:sortableColumn property="ci.costInBillingCurrency" title="${message(code:'financials.invoice_total')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="ci.costInLocalCurrency" title="${message(code:'financials.newCosts.valueInEuro')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="ci.costItemStatus" title="${message(code:'financials.costItemStatus')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="ci.startDate" title="${message(code:'financials.dateFrom')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="ci.costItemElement" title="${message(code:'financials.costItemElement')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <th></th>
            </g:else>
        </tr>
        <tr>
            <g:if test="${!fixedSubscription}">
                <th colspan="2"></th>
                <g:sortableColumn property="ci.sub.startDate" title="${message(code:'financials.subscriptionRunningTime')}" params="[ownSort: true]"/>
                <th colspan="4"></th>
                <g:sortableColumn property="ci.endDate" title="${message(code:'financials.dateTo')}" params="[ownSort: true]"/>
                <th colspan="2"></th>
            </g:if>
            <g:else>
                <th colspan="6"></th>
                <g:sortableColumn property="ci.endDate" title="${message(code:'financials.dateTo')}" params="[ownSort: true]"/>
                <th colspan="2"></th>
            </g:else>
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
                        <g:if test="${editable}">
                            <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
                        </g:if>
                        <g:else>
                            ${ci.costTitle}
                        </g:else>
                    </td>
                    <g:if test="${!fixedSubscription}">
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
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>  (${ci.taxKey ? "${ci.taxKey.taxRate}%" : message(code:'financials.taxRate.notSet')})
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" />
                        <br />
                        <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" />  (${ci.taxKey ? "${ci.taxKey.taxRate}%" : message(code:'financials.taxRate.notSet')})
                    </td>
                    <td>
                        <semui:xEditableRefData config="CostItemStatus" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemStatus" />
                    </td>
                    <td>
                        <semui:xEditable owner="${ci}" type="date" field="startDate" />
                        <br>
                        <semui:xEditable owner="${ci}" type="date" field="endDate" />
                    </td>
                    <td>
                        <semui:xEditableRefData config="CostItemElement" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemElement" />
                    </td>
                    <td class="x">
                        <g:if test="${editable}">
                            <g:if test="${fixedSubcription}">
                                <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}", tab:"own"]' class="ui icon button trigger-modal">
                                    <i class="write icon"></i>
                                </g:link>
                                <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                                    <g:link mapping="subfinanceCopyCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}", tab:"own"]' class="ui icon button trigger-modal">
                                        <i class="copy icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <g:link controller="finance" action="editCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", tab:"own"]' class="ui icon button trigger-modal">
                                    <i class="write icon"></i>
                                </g:link>
                                <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                                    <g:link controller="finance" action="copyCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", tab:"own"]' class="ui icon button trigger-modal">
                                        <i class="copy icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>
                        <g:if test="${editable}">
                            <g:link controller="finance" action="deleteCostItem" id="${ci.id}" params="[ tab:'own']" class="ui icon negative button" onclick="return confirm('${message(code: 'default.button.confirm.delete')}')">
                                <i class="trash alternate icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </g:else>
    </tbody>
    <tfoot>
        <g:if test="${data.count > 0 && data.sums.billingSums}">
            <%
                int colspan = 2
                if(fixedSubscription) {
                    colspan = 1
                }
            %>
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
        <g:elseif test="${data.count > 0 && !data.sums.billingSums}">
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
    <g:if test="${fixedSubcription}">
        <semui:paginate mapping="subfinance" params="${params+[view:'own']}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}" offset="${ownOffset ? ownOffset : 0}" total="${data.count}"/>
    </g:if>
    <g:else>
        <semui:paginate action="finance" controller="myInstitution" params="${params+[view:'own']}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}" offset="${ownOffset ? ownOffset : 0}" total="${data.count}"/>
    </g:else>
</g:if>
<!-- _result_tab_owner.gsp -->
