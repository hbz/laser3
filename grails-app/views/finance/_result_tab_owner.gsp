<!-- _result_tab_owner.gsp -->
<%@page import="de.laser.storage.RDStore;de.laser.finance.CostItem"%>
<laser:serviceInjection />
<%
    int colspan = 2
    int wideColspan = 10
    if(fixedSubscription) {
        colspan = 1
        wideColspan = 9
    }
%>
<table id="costTable_${customerType}" class="ui celled sortable monitor stackable table table-tworow la-js-responsive-table la-table la-ignore-fixed">
    <thead>
        <tr>
            <g:if test="${!fixedSubscription}">
                <th>${message(code:'sidewide.number')}</th>
                <g:sortableColumn property="ci.costTitle" title="${message(code:'financials.newCosts.costTitle')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.sub.name" title="${message(code:'default.subscription.label')}" params="[ownSort: true]"/>
                <th class="la-no-uppercase"><span class="la-popup-tooltip la-delay" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center"><i class="money bill alternate icon"></i></span></th>
                <g:sortableColumn property="ci.costInBillingCurrency" title="${message(code:'financials.invoice_total')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.costInLocalCurrency" title="${message(code:'financials.newCosts.value')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.costItemStatus" title="${message(code:'default.status.label')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.startDate" title="${message(code:'financials.dateFrom')}" params="[ownSort: true]"/>
                <g:sortableColumn property="ci.costItemElement" title="${message(code:'financials.costItemElement')}" params="[ownSort: true]"/>
                <th class="la-action-info"><g:message code="default.actions.label"/></th>
            </g:if>
            <g:else>
                <th>${message(code:'sidewide.number')}</th>
                <g:sortableColumn property="costTitle" title="${message(code:'financials.newCosts.costTitle')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <th class="la-no-uppercase"><span class="la-popup-tooltip la-delay" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center"><i class="money bill alternate icon"></i></span></th>
                <g:sortableColumn property="costInBillingCurrency" title="${message(code:'financials.invoice_total')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="costInLocalCurrency" title="${message(code:'financials.newCosts.value')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="costItemStatus" title="${message(code:'default.status.label')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="startDate" title="${message(code:'financials.dateFrom')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <g:sortableColumn property="costItemElement" title="${message(code:'financials.costItemElement')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <th class="la-action-info"><g:message code="default.actions.label"/></th>
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
                <g:sortableColumn property="ci.endDate" title="${message(code:'financials.dateTo')}" params="[ownSort: true, sub: fixedSubscription.id]" mapping="subfinance"/>
                <th colspan="2"></th>
            </g:else>
        </tr>
    </thead>
    <tbody>
        %{--Empty result set--}%
        <g:if test="${!data.count || data.count == 0}">
            <tr>
                <td colspan="${wideColspan}" style="text-align:center">
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
                        <% int offset = offsets.ownOffset ?: 0 %>
                        ${ jj + 1 + offset }
                    </td>
                    <td>
                        ${raw(ci.costTitle?.replaceAll(/(.{50})/,'$1&shy;'))}
                    </td>
                    <g:if test="${!fixedSubscription}">
                        <td>
                            <g:if test="${ci.sub}"><g:link controller="subscription" action="show" id="${ci.sub.id}">${ci.sub}</g:link> (${formatDate(date:ci.sub.startDate,format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.endDate, format: message(code: 'default.date.format.notime'))})</g:if>
                            <g:else>${message(code:'financials.clear')}</g:else>
                        </td>
                    </g:if>
                    <td>
                        <span class="la-popup-tooltip la-delay" data-position="right center" data-content="${dataTooltip}">${raw(icon)}</span>
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>
                        <br />
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}"/>
                        <g:if test="${ci.taxKey && ci.taxKey.display}">
                            (${ci.taxKey.taxRate ?: 0}%)
                        </g:if>
                        <g:elseif test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7,CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                            ${ci.taxKey.taxType.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                            (${RDStore.TAX_REVERSE_CHARGE.getI10n("value")})
                        </g:elseif>
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" />
                        <br />
                        <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" />
                        <g:if test="${ci.taxKey && ci.taxKey.display}">
                            (${ci.taxKey.taxRate ?: 0}%)
                        </g:if>
                        <g:elseif test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7,CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                            ${ci.taxKey.taxType.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                            (${RDStore.TAX_REVERSE_CHARGE.getI10n("value")})
                        </g:elseif>
                    </td>
                    <td>
                        <ui:xEditableRefData config="${de.laser.storage.RDConstants.COST_ITEM_STATUS}" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemStatus" />
                    </td>
                    <td>
                        <ui:xEditable owner="${ci}" type="date" field="startDate" validation="datesCheck"/>
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code:'financials.dateTo')}:">
                            <ui:xEditable owner="${ci}" type="date" field="endDate" validation="datesCheck"/>
                        </span>
                    </td>
                    <td>
                        ${ci.costItemElement?.getI10n("value")}
                    </td>
                    <td class="x">
                        <g:if test="${editable}">
                            <g:if test="${fixedSubcription}">
                                <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}", showView:"own", offset: params.offset]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="edit_${ci.id}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                                <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'financials.costItem.copy.tooltip')}">
                                    <g:link mapping="subfinanceCopyCI" params='[sub:"${fixedSubscription?.id}", id:"${ci.id}", showView:"own"]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="copy_${ci.id}">
                                        <i class="copy icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <g:link controller="finance" action="editCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", showView:"own", offset: params.offset]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="edit_${ci.id}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                    <i aria-hidden="true" class="write icon"></i>
                                </g:link>
                                <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'financials.costItem.copy.tooltip')}">
                                    <g:link controller="finance" action="copyCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", showView:"own"]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="copy_${ci.id}">
                                        <i class="copy icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                        </g:if>
                        <g:if test="${editable}">
                            <g:link controller="finance" action="deleteCostItem" id="${ci.id}" params="[ showView:'own', offset: params.offset]" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.costItem",args: [ci.costTitle])}"
                                        data-confirm-term-how="delete"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                <i class="trash alternate outline icon"></i>
                            </g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
        </g:else>
    </tbody>
    <tfoot>
        <g:if test="${data.count > 0 && data.sums.billingSums}">

            <tr>
                <th colspan="${wideColspan}">
                    ${message(code:'financials.totalCost')}
                </th>
            </tr>
            <g:each in="${data.sums.billingSums}" var="entry">
                <tr>
                    <td colspan="${colspan}">

                    </td>
                    <td colspan="2">
                        ${message(code:'financials.sum.billing')} ${entry.currency}<br />
                        ${message(code:'financials.sum.billingAfterTax')}
                    </td>
                    <td class="la-exposed-bg">
                        <g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/><br />
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
                    ${message(code:'financials.sum.local')}<br />
                    ${message(code:'financials.sum.localAfterTax')}
                </td>
                <td class="la-exposed-bg">
                    <g:formatNumber number="${data.sums.localSums.localSum}" type="currency" currencySymbol="EUR"/><br />
                    <g:formatNumber number="${data.sums.localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/>
                </td>
                <td colspan="4">

                </td>
            </tr>
        </g:if>
        <g:elseif test="${data.count > 0 && !data.sums.billingSums}">
            <tr>
                <td colspan="${wideColspan}">
                    ${message(code:'financials.noCostsConsidered')}
                </td>
            </tr>
        </g:elseif>
        <tr>
            <td colspan="${wideColspan}">
                <div class="ui fluid accordion">
                    <div class="title">
                        <i aria-hidden="true" class="dropdown icon" ></i>
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
    <g:if test="${fixedSubscription}">
        <ui:paginate mapping="subfinance" params="${params+[showView:'own']}"
                        max="${max}" offset="${ownOffset ? ownOffset : 0}" total="${data.count}"/>
    </g:if>
    <g:else>
        <ui:paginate action="finance" controller="myInstitution" params="${params+[showView:'own']}"
                        max="${max}" offset="${ownOffset ? ownOffset : 0}" total="${data.count}"/>
    </g:else>
</g:if>
<!-- _result_tab_owner.gsp -->
