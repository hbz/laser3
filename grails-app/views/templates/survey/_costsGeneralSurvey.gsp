<%@ page import="de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.storage.RDStore; de.laser.utils.LocaleUtils" %>
<laser:serviceInjection/>
<g:set var="costItemsSurvey"
       value="${surveyOrg ? CostItem.findAllBySurveyOrgAndPkgIsNull(surveyOrg) : null}"/>

<%
    def elementSign = 'notSet'
    String icon = ''
    String dataTooltip = ""
%>

<g:if test="${surveyInfo.owner.id != institution.id && costItemsSurvey}">

    <div class="ui card la-time-card">

        <div class="content">
            <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
        </div>

        <div class="content">
            <table class="ui celled compact la-js-responsive-table la-table-inCard table">
                <thead>
                <tr>
                    <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                    <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                    <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                    <th class="la-smaller-table-head"><g:message
                            code="financials.newCosts.totalAmount"/></th>
                </tr>
                </thead>


                <tbody class="top aligned">

                <g:each in="${costItemsSurvey}" var="costItemSurvey">
                    <tr>
                        <td>
                            <%
                                elementSign = 'notSet'
                                icon = ''
                                dataTooltip = ""
                                if (costItemSurvey.costItemElementConfiguration) {
                                    elementSign = costItemSurvey.costItemElementConfiguration
                                }
                                switch (elementSign) {
                                    case RDStore.CIEC_POSITIVE:
                                        dataTooltip = message(code: 'financials.costItemConfiguration.positive')
                                        icon = '<i class="plus green circle icon"></i>'
                                        break
                                    case RDStore.CIEC_NEGATIVE:
                                        dataTooltip = message(code: 'financials.costItemConfiguration.negative')
                                        icon = '<i class="minus red circle icon"></i>'
                                        break
                                    case RDStore.CIEC_NEUTRAL:
                                        dataTooltip = message(code: 'financials.costItemConfiguration.neutral')
                                        icon = '<i class="circle yellow icon"></i>'
                                        break
                                    default:
                                        dataTooltip = message(code: 'financials.costItemConfiguration.notSet')
                                        icon = '<i class="grey question circle icon"></i>'
                                        break
                                }
                            %>

                            <span class="la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${dataTooltip}">${raw(icon)}</span>

                            ${costItemSurvey.costItemElement?.getI10n('value')}

                        </td>
                        <td>
                            <strong>
                                <g:formatNumber
                                        number="${costItemSurvey.costInBillingCurrency}"
                                        minFractionDigits="2" maxFractionDigits="2"
                                        type="number"/>
                            </strong>

                            ${costItemSurvey.billingCurrency?.getI10n('value')}
                        </td>
                        <td>
                            <g:if test="${costItemSurvey.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                            </g:if>
                            <g:elseif test="${costItemSurvey.taxKey}">
                                ${costItemSurvey.taxKey.taxType?.getI10n("value") + " (" + costItemSurvey.taxKey.taxRate + "%)"}
                            </g:elseif>
                        </td>
                        <td>
                            <strong>
                                <g:formatNumber
                                        number="${costItemSurvey.costInBillingCurrencyAfterTax}"
                                        minFractionDigits="2" maxFractionDigits="2"
                                        type="number"/>
                            </strong>

                            ${costItemSurvey.billingCurrency?.getI10n('value')}

                            <g:set var="newCostItem"
                                   value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                            <g:if test="${costItemSurvey.startDate || costItemSurvey.endDate}">
                                <br/>(${formatDate(date: costItemSurvey.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItemSurvey.endDate, format: message(code: 'default.date.format.notime'))})
                            </g:if>

                            <g:if test="${costItemSurvey.costDescription}">
                                <br/>

                                <div class="ui icon la-popup-tooltip la-delay"
                                     data-position="right center"
                                     data-variation="tiny"
                                     data-content="${costItemSurvey.costDescription}">
                                    <i class="question small circular inverted icon"></i>
                                </div>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </div>
</g:if>
