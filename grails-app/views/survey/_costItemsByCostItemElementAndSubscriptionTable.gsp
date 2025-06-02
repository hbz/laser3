<%@ page import="de.laser.ui.Icon; de.laser.RefdataCategory; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.finance.CostItem" %>
<laser:serviceInjection/>

<g:set var="sumCostInBillingCurrencyAfterTax" value="${0}"/>
<g:set var="sumCostInBillingCurrency" value="${0}"/>
<g:set var="sumCostItems" value="${0}"/>

<div class="ui segment">
    <h3>
        <g:message code="surveyCostItemsSubscriptions.label"/> in <g:message code="survey.label"/>
    </h3>
    <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.name.label')}</th>
            <th>${message(code: 'financials.costItemElement')}</th>
            <th>${message(code: 'default.count.label')}</th>
            <th>${message(code: 'costItem.costInBillingCurrency.label')}</th>
            <th>${message(code: 'costItem.costInBillingCurrencyAfterTax.label')}</th>
            <th>${message(code: 'default.selected.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${costItemsBySubscriptions}" var="costItemsBySubscription">
            <g:each in="${costItemsBySubscription.costItemsByCostItemElement}" var="ctByCostItemElement" status="i">
                <g:set var="costItemElement"
                       value="${RefdataValue.findByValueAndOwner(ctByCostItemElement.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))}"/>
                <g:set var="sumCostInBillingCurrencyAfterTaxByElement" value="${0}"/>
                <g:set var="sumCostInBillingCurrencyByElement" value="${0}"/>
                <g:each in="${ctByCostItemElement.value}" var="costItem">
                    <g:set var="sumCostInBillingCurrencyAfterTaxByElement"
                           value="${sumCostInBillingCurrencyAfterTaxByElement + costItem.costInBillingCurrencyAfterTax}"/>
                    <g:set var="sumCostInBillingCurrencyByElement" value="${sumCostInBillingCurrencyByElement + costItem.costInBillingCurrency}"/>
                </g:each>

                <g:set var="sumCostInBillingCurrencyAfterTax" value="${sumCostInBillingCurrencyAfterTax + sumCostInBillingCurrencyAfterTaxByElement}"/>
                <g:set var="sumCostInBillingCurrency" value="${sumCostInBillingCurrency + sumCostInBillingCurrencyByElement}"/>
                <g:set var="sumCostItems" value="${sumCostItems + ctByCostItemElement.value.size()}"/>
                <tr>
                    <td>${i + 1}</td>
                    <td>${costItemsBySubscription.subscription.getLabel()}</td>
                    <td>${costItemElement.getI10n('value')}</td>
                    <td>${ctByCostItemElement.value.size()}</td>
                    <td><g:formatNumber number="${sumCostInBillingCurrencyByElement}" minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/></td>
                    <td><g:formatNumber number="${sumCostInBillingCurrencyAfterTaxByElement}" minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/></td>
                    <td>
                        <g:if test="${selectedCostItemElementID == costItemElement.id && costItemsBySubscription.surveyConfigSubscription.id == selectedSurveyConfigSubscriptionID}">
                            <g:link controller="survey" action="$actionName"
                                    params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID, selectedCostItemElementID: costItemElement.id, selectedSurveyConfigSubscriptionID: costItemsBySubscription.surveyConfigSubscription.id]}">
                                <i class="${Icon.SYM.CHECKBOX_CHECKED} large"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="survey" action="$actionName"
                                    params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID, selectedCostItemElementID: costItemElement.id, selectedSurveyConfigSubscriptionID:  costItemsBySubscription.surveyConfigSubscription.id]}">
                                <i class="${Icon.SYM.CHECKBOX} large"></i>
                            </g:link>
                        </g:else>
                    </td>
                </tr>
            </g:each>
        </g:each>
        </tbody>
        <g:if test="${costItemsBySubscriptions}">
            <tfoot>
            <tr>
                <td></td>
                <td></td>
                <td></td>
                <td>${sumCostItems}</td>
                <td>
                    <g:formatNumber number="${sumCostInBillingCurrency}" minFractionDigits="2"
                                    maxFractionDigits="2" type="number"/>
                </td>
                <td>
                    <g:formatNumber number="${sumCostInBillingCurrencyAfterTax}" minFractionDigits="2"
                                    maxFractionDigits="2" type="number"/>
                </td>
                <td></td>
            </tr>
            </tfoot>
        </g:if>
    </table>
</div>

