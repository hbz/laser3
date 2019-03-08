<!-- _result_tab_cons.gsp -->
<%@ page import="org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.RDStore; com.k_int.kbplus.CostItemElementConfiguration;com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>

<laser:serviceInjection />


<%--
    def jsonSource = []

    cost_items.each{ ci ->
        def or = OrgRole.findBySubAndRoleType(ci.sub, RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role'))

        jsonSource << [
                "name": "${or.org.sortname}",
                "type": "${or.org.orgType}",
                "federal": "${or.org.federalState}",
                "cost": "${ci.costInLocalCurrency}"
        ]
    }

    //println jsonSource

    def jb = new groovy.json.JsonBuilder(jsonSource)
    println jb.toPrettyString()
--%>

<table id="costTable_${i}" data-queryMode="${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">
    <thead>
        <tr>
            <th>${message(code:'sidewide.number')}</th>
            <th>${message(code:'financials.newCosts.costParticipants')}</th>
            <th>${message(code:'financials.newCosts.costTitle')}</th>
            <g:if test="${!fixedSubscription}">
                <th>${message(code:'financials.newCosts.subscriptionHeader')}</th>
            </g:if>
            <th><span data-tooltip="${message(code:'financials.costItemConfiguration')}" data-position="top center"><i class="money bill alternate icon"></i></span></th>
            <th>${message(code:'financials.currency')}</th>
            <th>${message(code:'financials.invoice_total')}</th>
            <th>${message(code:'financials.taxRate')}</th>
            <th>${message(code:'financials.amountFinal')}</th>
            <th>${message(code:'financials.newCosts.valueInEuro')}</th>
            <th>${message(code:'financials.dateFrom')}<br/>${message(code:'financials.dateTo')}</th>
            <th>${message(code:'financials.costItemElement')}</th>
            <th></th>
        </tr>
    </thead>
    <tbody>
        %{--Empty result set--}%
        <g:if test="${data.count == 0}">
            <tr>
                <td colspan="12" style="text-align:center">
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
                        <% int offset = consOffset ? consOffset : 0 %>
                        ${ jj + 1 + offset }
                    </td>
                    <td>
                        <g:each in="${ci.sub.orgRelations}" var="or">
                            <g:if test="${or.roleType.equals(RDStore.OR_SUBSCRIBER_CONS)}">
                                ${or.org.sortname}
                            </g:if>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${ci.sub.orgRelations}" var="or">
                           <g:if test="${or.roleType.equals(RDStore.OR_SUBSCRIBER_CONS)}">
                               <g:link mapping="subfinance" params="[sub:ci.sub.id]">${or.org.designation}</g:link>

                               <g:if test="${ci.isVisibleForSubscriber}">
                                   <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                                       <i class="ui icon eye orange"></i>
                                   </span>
                               </g:if>
                           </g:if>
                        </g:each>
                        <br />
                        <g:if test="${editable}">
                            <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
                        </g:if>
                        <g:else>
                            ${ci.costTitle}
                        </g:else>
                    </td>
                    <g:if test="${!fixedSubscription}">
                        <td>
                            <g:if test="${ci.sub}">${ci.sub.name} (${formatDate(date:ci.sub.startDate,format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.endDate, format: message(code: 'default.date.format.notime'))})</g:if>
                            <g:else>${message(code:'financials.clear')}</g:else>
                        </td>
                    </g:if>
                    <td>
                        <span data-position="right center" data-tooltip="${dataTooltip}">${raw(icon)}</span>
                    </td>
                    <td>
                        ${ci.billingCurrency ?: 'EUR'}
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencySymbol="" />
                    </td>
                    <td>
                        ${ci.taxKey ? ci.taxKey.taxRate : 0}%
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="" />
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInLocalCurrency}" type="currency" currencyCode="EUR" currencySymbol="" />
                        <br />
                        <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" currencySymbol="" />
                    </td>
                    <td>
                        <g:if test="${editable}">
                            <semui:xEditable owner="${ci}" type="date" field="startDate" />
                            <br />
                            <semui:xEditable owner="${ci}" type="date" field="endDate" />
                        </g:if>
                        <g:else>
                            ${ci.startDate}
                            <br>
                            ${ci.endDate}
                        </g:else>
                    </td>
                    <td>
                        <g:if test="${editable}">
                            <semui:xEditableRefData config="CostItemElement" emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costItemElement" />
                        </g:if>
                        <g:else>
                            ${ci.costItemElement}
                        </g:else>
                    </td>
                    <td class="x">
                        <g:if test="${editable}">
                            <g:if test="${fixedSubscription}">
                                <g:link mapping="subfinanceEditCI" params='[sub:"${fixedSubscription.id}", id:"${ci.id}", tab:"cons"]' class="ui icon button trigger-modal">
                                    <i class="write icon"></i>
                                </g:link>
                                <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                                    <g:link mapping="subfinanceCopyCI" params='[sub:"${fixedSubscription.id}", id:"${ci.id}", tab:"cons"]' class="ui icon button trigger-modal">
                                        <i class="copy icon"></i>
                                    </g:link>
                                </span>
                            </g:if>
                            <g:else>
                                <g:link controller="finance" action="editCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", tab:"cons"]' class="ui icon button trigger-modal">
                                    <i class="write icon"></i>
                                </g:link>
                                <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                                    <g:link controller="finance" action="copyCostItem" params='[sub:"${ci.sub?.id}", id:"${ci.id}", tab:"cons"]' class="ui icon button trigger-modal">
                                        <i class="copy icon"></i>
                                    </g:link>
                                </span>
                            </g:else>
                            <g:link controller="finance" action="deleteCostItem" id="${ci.id}" params="[ tab:'cons']" class="ui icon negative button" onclick="return confirm('${message(code: 'default.button.confirm.delete')}')">
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
                int colspan1 = 5
                int colspan2 = 7
                if(fixedSubscription) {
                    colspan1 = 4
                    colspan2 = 6
                }
            %>
            <tr>
                <th colspan="13">
                    ${message(code:'financials.totalCost')}
                </th>
            </tr>
            <g:each in="${data.sums.billingSums}" var="entry">
                <tr>
                    <td colspan="${colspan1}">

                    </td>
                    <td>
                        ${message(code:'financials.sum.billing')} ${entry.currency}<br>
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
                <td colspan="2">
                    ${message(code:'financials.sum.local')}<br>
                    ${message(code:'financials.sum.localAfterTax')}
                </td>
                <td class="la-exposed-bg">
                    <g:formatNumber number="${data.sums.localSums.localSum}" type="currency" currencySymbol="" currencyCode="EUR"/><br>
                    <g:formatNumber number="${data.sums.localSums.localSumAfterTax}" type="currency" currencySymbol="" currencyCode="EUR"/>
                </td>
                <td colspan="4">

                </td>
            </tr>
        </g:if>
        <g:elseif test="${data.count > 0 && !data.sums.billingSums}">
            <tr>
                <td colspan="13">
                    ${message(code:'financials.noCostsConsidered')}
                </td>
            </tr>
        </g:elseif>
        <tr>
            <td colspan="13">
                <div class="ui fluid accordion">
                    <div class="title">
                        <i class="dropdown icon"></i>
                        <strong>${message(code: 'financials.calculationBase')}</strong>
                    </div>
                    <div class="content">
                        <p>
                            <%
                                def argv0 = contextService.getOrg().costConfigurationPreset ? contextService.getOrg().costConfigurationPreset.getI10n('value') : message(code:'financials.costItemConfiguration.notSet')
                            %>
                            ${message(code: 'financials.calculationBase.paragraph1', args: [argv0])}
                        </p>
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
        <semui:paginate mapping="subfinance" params="${params+[view:'cons']}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}" offset="${consOffset ? consOffset : '1'}" total="${data.count}"/>
    </g:if>
    <g:else>
        <semui:paginate action="finance" controller="myInstitution" params="${params+[view:'cons']}"
                        next="${message(code: 'default.paginate.next', default: 'Next')}"
                        prev="${message(code: 'default.paginate.prev', default: 'Prev')}"
                        max="${max}" offset="${consOffset ? consOffset : '1'}" total="${data.count}"/>
    </g:else>
</g:if>
<!-- _result_tab_cons.gsp -->
