<!-- _result_tab_cons.gsp -->
<%@ page import="com.k_int.kbplus.CostItemElementConfiguration;com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>

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

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <th>${message(code:'financials.newCosts.costParticipants')}</th>
        <th>${message(code:'financials.newCosts.costTitle')}</th>
        <g:if test="${!forSingleSubscription}">
            <th>${message(code:'financials.newCosts.subscriptionHeader')}</th>
        </g:if>
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
    <g:if test="${cost_items?.size() == 0}">
        <tr>
            <td colspan="11" style="text-align:center">
                <br />
                <g:if test="${msg}">${msg}</g:if>
                <g:else>${message(code:'finance.result.filtered.empty')}</g:else>
                <br />
            </td>
        </tr>
    </g:if>
    <g:else>
        <g:each in="${cost_items}" var="ci" status="jj">
            <g:set var="orgRoles" value="${OrgRole.findBySubAndRoleType(ci.sub, RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role'))}" />
            <%
                def org = contextService.getOrg()
                def elementSign = org.costConfigurationPreset
                def icon = ''
                def dataTooltip = ""
                if(elementSign == null) {
                    elementSign = RefdataValue.getByValueAndCategory('positive', 'Cost configuration')
                }
                def consider = org.considerationPreset
                if(consider == null) {
                    consider = RefdataValue.getByValueAndCategory('Yes', 'YN')
                }
                if(ci.costItemElement) {
                    def cie = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(ci.costItemElement, org)
                    if(cie) {
                        elementSign = cie.elementSign
                        consider = cie.consider
                    }
                }
                String cieString = "data-elementSign=${elementSign} data-consider=${consider}"
                switch(elementSign) {
                    case RefdataValue.getByValueAndCategory('positive','Cost configuration'):
                        dataTooltip = message(code:'financials.costItemConfiguration.positive')
                        icon = '<i class="check circle'
                        break
                    case RefdataValue.getByValueAndCategory('negative','Cost configuration'):
                        dataTooltip = message(code:'financials.costItemConfiguration.negative')
                        icon = '<i class="minus circle'
                        break
                    case RefdataValue.getByValueAndCategory('neutral','Cost configuration'):
                        dataTooltip = message(code:'financials.costItemConfiguration.neutral')
                        icon = '<i class="circle'
                        break
                    default:
                        dataTooltip = message(code:'financials.costItemConfiguration.notSet')
                        icon = '<i class="question circle'
                        break
                }
                switch(consider) {
                    case RefdataValue.getByValueAndCategory('Yes','YN'):
                        dataTooltip += ', '+message(code: 'financials.costItemConfiguration.considered')
                        icon += ' icon"></i>'
                        break
                    case RefdataValue.getByValueAndCategory('No','YN'):
                        dataTooltip += ', '+message(code:'financials.costItemConfiguration.notConsidered')
                        icon += ' outline icon"></i>'
                        break
                    default:
                        dataTooltip += ', '+message(code:'financials.costItemConfiguration.considerationNotSet')
                        icon += ' outline icon"></i><i class="question circle outline icon"></i>'
                        break
                }
            %>
            <tr id="bulkdelete-b${ci.id}">
                <td>
                    <% int offset = params.offset ? Integer.parseInt(params.offset) : 0 %>
                    ${ jj + 1 + offset }
                </td>
                <td>
                    <g:each in="${orgRoles}" var="or">
                        ${or.org.sortname}
                    </g:each>
                </td>
                <td>
                   <g:each in="${orgRoles}" var="or">
                        <g:link mapping="subfinance" params="[sub:or.sub.id]">${or.org.getDesignation()}</g:link>

                        <g:if test="${ci.isVisibleForSubscriber}">
                            <span data-position="top right" data-tooltip="${message(code:'financials.isVisibleForSubscriber')}" style="margin-left:10px">
                                <i class="ui icon eye orange"></i>
                            </span>
                        </g:if>
                    </g:each>

                    <br />
                    <semui:xEditable emptytext="${message(code:'default.button.edit.label')}" owner="${ci}" field="costTitle" />
                </td>
                <g:if test="${!forSingleSubscription}">
                    <td>
                        <g:if test="${ci.sub}">${ci.sub} (${formatDate(date:ci.sub.startDate,format:message(code: 'default.date.format.notime'))} - ${formatDate(date: ci.sub.endDate, format: message(code: 'default.date.format.notime'))})</g:if>
                        <g:else>${message(code:'financials.clear')}</g:else>
                    </td>
                </g:if>
                <td>
                    ${ci.billingCurrency ?: 'EUR'}
                </td>
                <td>
                    <span class="costData"
                          data-costInLocalCurrency="<g:formatNumber number="${ci.costInLocalCurrency}" locale="en" maxFractionDigits="2"/>"
                          data-costInLocalCurrencyAfterTax="<g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                          data-billingCurrency="${ci.billingCurrency ?: 'EUR'}"
                          data-costInBillingCurrency="<g:formatNumber number="${ci.costInBillingCurrency}" locale="en" maxFractionDigits="2"/>"
                          data-costInBillingCurrencyAfterTax="<g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" locale="en" maxFractionDigits="2"/>"
                          ${cieString}
                    >
                        <g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencySymbol="" />
                    </span>
                </td>
                <td>
                    ${ci.taxRate ?: 0}%
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
                        <g:if test="${forSingleSubscription}">
                            <g:link mapping="subfinanceEditCI" params='[fixedSub:"${fixedSubscription.id}", id:"${ci.id}", tab:"sc"]' class="ui icon button trigger-modal">
                                <i class="write icon"></i>
                            </g:link>
                            <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                                <g:link mapping="subfinanceCopyCI" params='[fixedSub:"${fixedSubscription.id}", id:"${ci.id}", tab:"sc"]' class="ui icon button trigger-modal">
                                    <i class="copy icon"></i>
                                </g:link>
                            </span>
                        </g:if>
                        <g:else>
                            <g:link controller="finance" action="editCostItem" params='[currSub:"${ci.sub?.id}", id:"${ci.id}", tab:"sc"]' class="ui icon button trigger-modal">
                                <i class="write icon"></i>
                            </g:link>
                            <span data-position="top right" data-tooltip="${message(code:'financials.costItem.copy.tooltip')}">
                                <g:link controller="finance" action="copyCostItem" params='[currSub:"${ci.sub?.id}", id:"${ci.id}", tab:"sc"]' class="ui icon button trigger-modal">
                                    <i class="copy icon"></i>
                                </g:link>
                            </span>
                        </g:else>
                        <g:link controller="finance" action="deleteCostItem" id="${ci.id}" params="[ tab:'sc']" class="ui icon negative button" onclick="return confirm('${message(code: 'default.button.confirm.delete')}')">
                            <i class="trash alternate icon"></i>
                        </g:link>
                    </g:if>
                </td>

            </tr>
        </g:each>

    </g:else>
</tbody>
    <tfoot>
    <tr>
        <td colspan="11">
            <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}</strong>
            <br/>
            <span class="sumOfCosts_${i}"></span>
        </td>
    </tr>
    <tr>
        <td colspan="11">
            <div class="ui fluid accordion">
                <div class="title">
                    <i class="dropdown icon"></i>
                    <strong>${message(code: 'financials.calculationBase')}</strong>
                </div>
                <div class="content">
                    <p>
                        ${message(code: 'financials.calculationBase.paragraph1', args: [contextService.getOrg().costConfigurationPreset.getI10n('value'),contextService.getOrg().considerationPreset.getI10n('value')])}
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

<!-- _result_tab_cons.gsp -->
