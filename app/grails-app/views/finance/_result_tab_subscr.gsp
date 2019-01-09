<!-- _result_tab_subscr.gsp -->
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>

<laser:serviceInjection />

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <th class="two wide">${message(code:'financials.invoice_total')}</th>
        <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
        <th>${message(code:'financials.costItemElement')}</th>
        <th>${message(code:'financials.forSubscription')}</th>
        <th>${message(code:'financials.forPackage')}</th>
    </tr>
</thead>
<tbody>
    %{--Empty result set--}%
    <g:if test="${cost_items?.size() == 0}">
        <tr>
            <td colspan="6" style="text-align:center">
                <br />
                <g:if test="${msg}">${msg}</g:if>
                <g:else>${message(code:'finance.result.filtered.empty')}</g:else>
                <br />
            </td>
        </tr>
    </g:if>
    <g:else>

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
            %>
            <tr id="bulkdelete-b${ci.id}">
                <td>
                    <% int offset = params.offset ? Integer.parseInt(params.offset) : 0 %>
                    ${ jj + 1 + offset }
                </td>
                <td>
                    <span class="costData"
                          data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                          data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                          data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                          ${cieString}
                    >
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="${ci.billingCurrency ?: 'EUR'}" />
                    </span>
                </td>
                <td>
                    <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencyCode="EUR" />
                </td>
                <td>
                    ${ci.costItemElement?.getI10n('value')}
                    <span data-position="right center" data-tooltip="${dataTooltip}">${raw(icon)}</span>
                </td>
                <td>
                    <g:link controller="subscriptionDetails" action="show" id="${ci.sub?.id}">${ci.sub}</g:link>
                </td>
                <td>
                    <g:link controller="packageDetails" action="show" id="${ci.subPkg?.pkg?.id}">${ci.subPkg?.pkg}</g:link>
                </td>
            </tr>
        </g:each>

    </g:else>
</tbody>
    <tfoot>
        <tr>
            <td colspan="7">
                <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong>
                <br/>
                <span class="sumOfCosts_${i}"></span>
            </td>
        </tr>
        <tr>
            <td colspan="7">
                <div class="ui fluid accordion">
                    <div class="title">
                        <i class="dropdown icon"></i>
                        <strong>${message(code: 'financials.calculationBase')}</strong>
                    </div>
                    <div class="content">
                        <p>
                            ${message(code: 'financials.calculationBase.paragraph1', args: [contextService.getOrg().costConfigurationPreset.getI10n('value')])}
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
    <g:if test="${cost_items}">
        <g:if test="${inSubMode}">
            <semui:paginate mapping="subfinance" action="index" controller="finance" params="${params}"
                            next="${message(code: 'default.paginate.next', default: 'Next')}"
                            prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                            total="${cost_items_count}"/>
        </g:if>
        <g:else>
            <semui:paginate action="finance" controller="myInstitution" params="${params}"
                            next="${message(code: 'default.paginate.next', default: 'Next')}"
                            prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                            total="${cost_items_count}"/>
        </g:else>
    </g:if>

<!-- _result_tab_subscr.gsp -->
