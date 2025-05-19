<%@ page import="de.laser.survey.SurveySubscriptionResult; de.laser.survey.SurveyConfigSubscription; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.storage.RDStore; de.laser.utils.LocaleUtils; de.laser.survey.SurveyPackageResult" %>

<%
    def elementSign = 'notSet'
    String icon = ''
    String dataTooltip = ""
%>

<g:if test="${surveyInfo.type.id == RDStore.SURVEY_TYPE_INTEREST.id}">
    <g:set var="surveySubscriptions"
           value="${SurveySubscriptionResult.executeQuery("select ssr.subscription from SurveySubscriptionResult ssr where ssr.participant = :participant and ssr.surveyConfig = :surveyConfig", [participant: participant, surveyConfig: surveyConfig])}"/>

    <g:set var="costItemsSurvey"
           value="${surveyOrg && surveySubscriptions ? CostItem.findAllBySurveyOrgAndSurveyConfigSubscriptionInList(surveyOrg, SurveyConfigSubscription.findAllBySurveyConfigAndSubscriptionInList(surveyConfig, surveySubscriptions)) : null}"/>

    <g:if test="${costItemsSurvey}">

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
                                <ui:costSign ci="${costItemSurvey}"/>

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

                                    <div class="ui icon la-popup-tooltip"
                                         data-position="right center"
                                         data-variation="tiny"
                                         data-content="${costItemSurvey.costDescription}">
                                        <i class="${Icon.TOOLTIP.HELP}"></i>
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
</g:if>