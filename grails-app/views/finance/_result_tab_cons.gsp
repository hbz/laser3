<!-- _result_tab_cons.gsp -->
<%@ page import="de.laser.CustomerTypeService; de.laser.storage.RDStore; de.laser.finance.CostItemElementConfiguration;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.properties.PropertyDefinition;de.laser.FinanceController;de.laser.finance.CostItem" %>

<laser:serviceInjection />

<%
    int colspan1 = 3
    int colspan2 = 7
    int wideColspan2 = 15
    Map sorting
    int offset
    if(showView == "cons") {
        colspan1 = 5
        colspan2 = 9
        wideColspan2 = 15
        sorting = [consSort: true]
        offset = offsets.consOffset
    }
    else {
        sorting = [subscrSort: true]
        offset = offsets.subscrOffset
    }

    if(fixedSubscription) {
        colspan1 = 2
        colspan2 = 6
        wideColspan2 = 13
        if(showView == "cons") {
            colspan1 = 4
            colspan2 = 8
            wideColspan2 = 13
            sorting = [consSort: true]
            offset = offsets.consOffset
        }
        else if(showView == "consAtSubscr") {
            colspan1 = 3
            colspan2 = 7
            wideColspan2 = 13
            sorting = [consSort: true]
            offset = offsets.consOffset
        }
        else {
            sorting = [subscrSort: true]
            offset = offsets.subscrOffset
        }
    }
%>
<table id="costTable_${customerType}" class="ui celled monitor stackable sortable  table la-js-responsive-table la-table la-ignore-fixed">
    <thead>
        <tr>
            <g:if test="${tmplShowCheckbox && editable}">
                <th scope="col" rowspan="2">
                    <g:if test="${data.costItems}">
                        <g:checkBox name="costItemListToggler" id="costItemListToggler" checked="false"/>
                    </g:if>
                </th>
            </g:if>
            <g:if test="${!fixedSubscription}">
                <th scope="col" rowspan="2">${message(code:'sidewide.number')}</th>
                <g:if test="${showView == "cons"}">
                    <g:sortableColumn property="oo.org.sortname" title="${message(code:'financials.newCosts.costParticipants')}" params="${sorting}" scope="col" rowspan="2"/>
                </g:if>
                <g:sortableColumn property="ci.costTitle" title="${message(code:'financials.newCosts.costTitle')}" params="${sorting}" scope="col" rowspan="2"/>
                <g:sortableColumn property="sub.name" title="${message(code:'default.subscription.label')}" params="${sorting}" scope="col" class="la-smaller-table-head"/>
                <th class="la-no-uppercase" scope="col" rowspan="2">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center"><i class="money bill alternate icon"></i></span>
                </th>
                <g:sortableColumn property="ci.billingCurrency" title="${message(code:'default.currency.label')}" params="${sorting}" scope="col" rowspan="2"/>
                <g:sortableColumn property="ci.costInBillingCurrency" title="${message(code:'financials.invoice_total')}" params="${sorting}" scope="col" rowspan="2"/>
                <th scope="col" rowspan="2">${message(code:'financials.taxRate')}</th>
                <th scope="col" rowspan="2">${message(code:'financials.amountFinal')}</th>
                <g:sortableColumn property="ci.costInLocalCurrency" title="${message(code:'financials.newCosts.value')}" params="${sorting}" scope="col" rowspan="2"/>
                <g:sortableColumn property="ci.startDate" title="${message(code:'financials.dateFrom')}" params="${sorting}" scope="col" class="la-smaller-table-head" />
                <g:sortableColumn property="ci.costItemElement" title="${message(code:'financials.costItemElement')}" params="${sorting}" scope="col" rowspan="2"/>
                <%-- editable must be checked here as well because of the consortia preview! --%>
                <g:if test="${editable && contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
                    <th class="la-action-info" scope="col" rowspan="2"><g:message code="default.actions.label"/></th>
                </g:if>
            </g:if>
            <g:else>
                <th scope="col" rowspan="2">${message(code:'sidewide.number')}</th>
                <g:if test="${showView == "cons"}">
                    <g:sortableColumn property="oo.org.sortname" title="${message(code:'financials.newCosts.costParticipants')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" rowspan="2"/>
                </g:if>
                <g:sortableColumn property="costTitle" title="${message(code:'financials.newCosts.costTitle')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" rowspan="2"/>
                <th class="la-no-uppercase" scope="col" rowspan="2">
                    <span class="la-popup-tooltip la-delay" data-content="${message(code:'financials.costItemConfiguration')}" data-position="left center"><i class="money bill alternate icon"></i></span>
                </th>
                <g:sortableColumn property="billingCurrency" title="${message(code:'default.currency.label')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" rowspan="2"/>
                <g:sortableColumn property="costInBillingCurrency" title="${message(code:'financials.invoice_total')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" rowspan="2"/>
                <th scope="col" rowspan="2">${message(code:'financials.taxRate')}</th>
                <th scope="col" rowspan="2">${message(code:'financials.amountFinal')}</th>
                <g:sortableColumn property="costInLocalCurrency" title="${message(code:'financials.newCosts.value')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" rowspan="2"/>
                <g:sortableColumn property="startDate" title="${message(code:'financials.dateFrom')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" class="la-smaller-table-head" />
                <g:sortableColumn property="costItemElement" title="${message(code:'financials.costItemElement')}" params="${sorting+[sub: fixedSubscription.id]}" mapping="subfinance" scope="col" rowspan="2"/>
                <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
                    <th class="la-action-info" scope="col" rowspan="2"><g:message code="default.actions.label"/></th>
                </g:if>
            </g:else>
        </tr>
        <tr>
            <g:if test="${!fixedSubscription}">
                <g:if test="${showView == "cons"}">
                    <g:sortableColumn property="sub.startDate" title="${message(code:'financials.subscriptionRunningTime')}" params="[consSort: true]" scope="col" class="la-smaller-table-head" />
                </g:if>
                <g:elseif test="${showView == "subscr"}">
                    <g:sortableColumn property="sub.startDate" title="${message(code:'financials.subscriptionRunningTime')}" params="[consSort: true]" scope="col" class="la-smaller-table-head" />
                </g:elseif>
                <g:sortableColumn property="ci.endDate" title="${message(code:'financials.dateTo')}" params="[consSort: true]" scope="col" class="la-smaller-table-head" />
            </g:if>
            <g:else>
                <g:sortableColumn property="ci.endDate" title="${message(code:'financials.dateTo')}" params="[consSort: true, sub: fixedSubscription.id]" mapping="subfinance" scope="col" class="la-smaller-table-head" />
            </g:else>
        </tr>
    </thead>
    <tbody>
        %{--Empty result set--}%
        <g:if test="${data.count == 0}">
            <tr>
                <td colspan="${wideColspan2}" style="text-align:center">
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
                    <g:if test="${tmplShowCheckbox && editable}">
                        <td>
                            <g:checkBox id="selectedCostItems_${ci.id}" name="selectedCostItems" value="${ci.id}" checked="false"/>
                        </td>
                    </g:if>
                    <td>
                        <%
                            Set<Long> memberRoles = [RDStore.OR_SUBSCRIBER_CONS.id,RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id]
                        %>
                        ${ jj + 1 + offset }
                    </td>
                    <g:if test="${showView == "cons"}">
                        <td>
                            <g:each in="${ci.sub.orgRelations}" var="or">
                                <g:if test="${memberRoles.contains(or.roleType.id)}">
                                    <g:link controller="org" action="show" id="${or.org.id}"><span class="la-popup-tooltip la-delay" data-content="${or.org.name}">${or.org.sortname}</span></g:link>
                                </g:if>
                            </g:each>
                        </td>
                    </g:if>
                    <td>
                        <g:if test="${showView == "cons"}">
                            <g:each in="${ci.sub.orgRelations}" var="or">
                                <g:if test="${memberRoles.contains(or.roleType.id)}">
                                    <g:link mapping="subfinance" params="[sub:ci.sub.id]">${or.org.designation}</g:link>
                                    <g:if test="${ci.isVisibleForSubscriber}">
                                        <span data-position="top right" class="la-popup-tooltip la-delay" data-content="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                                            <i class="ui icon eye orange"></i>
                                        </span>
                                    </g:if>
                                </g:if>
                            </g:each>
                            <br />
                        </g:if>
                        ${raw(ci.costTitle?.replaceAll(/(.{50})/,'$1&shy;'))}
                    </td>
                    <g:if test="${!fixedSubscription}">
                        <td>
                            <g:if test="${ci.sub}">
                                <g:if test="${ci.sub.instanceOf && showView == "cons"}">
                                    <g:link controller="subscription" action="show" id="${ci.sub.instanceOf.id}">${ci.sub.name}</g:link>
                                </g:if>
                                <g:else>
                                    <g:link controller="subscription" action="show" id="${ci.sub.id}">${ci.sub.name}</g:link>
                                </g:else>
                                (${formatDate(date:ci.sub.startDate,format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.endDate, format: message(code: 'default.date.format.notime'))})</g:if>
                            <g:else>${message(code:'financials.clear')}</g:else>
                        </td>
                    </g:if>
                    <td>
                        <span class="la-popup-tooltip la-delay" data-position="right center" data-content="${dataTooltip}">${raw(icon)}</span>
                    </td>
                    <td>
                        ${ci.billingCurrency ?: 'EUR'}
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencySymbol="" />
                    </td>
                    <td>
                        <g:if test="${ci.taxKey && ci.taxKey.display}">
                            ${ci.taxKey.taxRate+'%'}
                        </g:if>
                        <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                            ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7,CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                            ${ci.taxKey.taxType.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${!ci.taxKey}">
                            <g:message code="financials.taxRate.notSet"/>
                        </g:elseif>
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="" />
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInLocalCurrency ?: 0.0}" type="currency" currencySymbol="EUR" />
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code:'costItem.costInLocalCurrencyAfterTax.label')}:">
                            <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="EUR" />
                        </span>
                    </td>
                    <td>
                        <ui:xEditable owner="${ci}" type="date" field="startDate" overwriteEditable="${editable}" validation="datesCheck"/>
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code:'financials.dateTo')}:">
                            <ui:xEditable owner="${ci}" type="date" field="endDate" overwriteEditable="${editable}" validation="datesCheck"/>
                        </span>
                    </td>
                    <td>
                        ${ci.costItemElement?.getI10n("value")}
                    </td>
                        <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_CONSORTIUM_BASIC)}">
                            <td class="x">
                                <g:if test="${fixedSubscription}">
                                    <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription.id}", id:"${ci.id}", showView:"cons", offset: params.offset]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="edit_${ci.id}"
                                            role="button"
                                            aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                        <i aria-hidden="true" class="write icon"></i>
                                    </g:link>
                                    <span data-position="top right la-popup-tooltip la-delay" data-content="${message(code:'financials.costItem.copy.tooltip')}">
                                        <g:link mapping="subfinanceCopyCI" params='[sub:"${fixedSubscription.id}", id:"${ci.id}", showView:"cons", offset: params.offset]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="copy_${ci.id}">
                                            <i class="copy icon"></i>
                                        </g:link>
                                    </span>
                                </g:if>
                                <g:else>
                                    <g:link controller="finance" action="editCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", showView:"cons", offset: params.offset]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="edit_${ci.id}"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                        <i aria-hidden="true" class="write icon"></i>
                                    </g:link>
                                    <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'financials.costItem.copy.tooltip')}">
                                        <g:link controller="finance" action="copyCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", showView:"cons", offset: params.offset]' class="ui icon button blue la-modern-button trigger-modal" data-id_suffix="copy_${ci.id}">
                                            <i class="copy icon"></i>
                                        </g:link>
                                    </span>
                                </g:else>
                                <g:link controller="finance" action="deleteCostItem" id="${ci.id}" params="[ showView:'cons', offset: params.offset]" class="ui icon negative button la-modern-button js-open-confirm-modal"
                                        data-confirm-tokenMsg="${message(code: "confirm.dialog.delete.costItem.participant")}"
                                        data-confirm-term-how="delete"
                                        role="button"
                                        aria-label="${message(code: 'ariaLabel.delete.universal')}">
                                    <i class="trash alternate outline icon"></i>
                                </g:link>
                            </td>
                        </g:if>
                        <g:elseif test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_INST_PRO)}">
                            <td class="x">
                                <g:if test="${fixedSubscription}">
                                    <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'financials.costItem.transfer.tooltip')}">
                                        <g:link mapping="subfinanceCopyCI" params='[sub:"${fixedSubscription.id}", id:"${ci.id}", showView:"own"]' class="ui icon blue button la-modern-button trigger-modal" data-id_suffix="copy_${ci.id}">
                                            <i class="la-copySend icon"></i>
                                        </g:link>
                                    </span>
                                </g:if>
                                <g:else>
                                    <span class="la-popup-tooltip la-delay" data-position="top right" data-content="${message(code:'financials.costItem.transfer.tooltip')}">
                                        <g:link controller="finance" action="copyCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", showView:"own"]' class="ui icon blue button la-modern-button trigger-modal" data-id_suffix="copy_${ci.id}">
                                            <i class="la-copySend icon"></i>
                                        </g:link>
                                    </span>
                                </g:else>
                            </td>
                        </g:elseif>
                </tr>
            </g:each>
        </g:else>
    </tbody>
    <tfoot>
        <g:if test="${data.count > 0 && data.sums.billingSums}">
            <tr>
                <th class="control-label" colspan="${wideColspan2}">
                    ${message(code:'financials.totalCost')}
                </th>
            </tr>
            <g:each in="${data.sums.billingSums}" var="entry">
                <tr>
                    <td colspan="${colspan1}">

                    </td>
                    <td colspan="2">
                        ${message(code:'financials.sum.billing')} ${entry.currency}<br />
                    </td>
                    <td class="la-exposed-bg">
                        <g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/>
                    </td>
                    <td>
                        ${message(code:'financials.sum.billingAfterTax')}
                    </td>
                    <td class="la-exposed-bg">
                        <g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/>
                    </td>
                    <td colspan="4">

                    </td>
                </tr>
            </g:each>
            <tr>
                <td colspan="${colspan2}">

                </td>
                <td>
                    ${message(code:'financials.sum.local')}<br />
                    ${message(code:'financials.sum.localAfterTax')}
                </td>
                <td class="la-exposed-bg">
                    <g:formatNumber number="${data.sums.localSums.localSum}" type="currency" currencySymbol="" currencyCode="EUR"/><br />
                    <g:formatNumber number="${data.sums.localSums.localSumAfterTax}" type="currency" currencySymbol="" currencyCode="EUR"/>
                </td>
                <td colspan="3">

                </td>
            </tr>
        </g:if>
        <g:elseif test="${data.count > 0 && !data.sums.billingSums}">
            <tr>
                <th class="control-label" colspan="${wideColspan2}">
                    ${message(code:'financials.noCostsConsidered')}
                </td>
            </tr>
        </g:elseif>
        <tr>
            <td colspan="${wideColspan2}">
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
    <g:if test="${fixedSubscription}">
        <ui:paginate mapping="subfinance" params="${params+[showView:showView]}"
                        max="${max}" offset="${offset}" total="${data.count}"/>
    </g:if>
    <g:else>
        <ui:paginate action="finance" controller="myInstitution" params="${params+[showView:showView]}"
                        max="${max}" offset="${offset}" total="${data.count}"/>
    </g:else>
</g:if>
<!-- _result_tab_cons.gsp -->

<g:if test="${tmplShowCheckbox}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#costItemListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedCostItems]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedCostItems]").prop('checked', false)
            }
        })
    </laser:script>
</g:if>
