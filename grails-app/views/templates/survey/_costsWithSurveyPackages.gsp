<%@ page import="de.laser.ui.Icon; de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.storage.RDStore; de.laser.utils.LocaleUtils; de.laser.survey.SurveyPackageResult" %>

<%
    def elementSign = 'notSet'
    String icon = ''
    String dataTooltip = ""
%>

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_TITLE_SELECTION.id]}">

    <g:set var="surveyPackages"
           value="${SurveyPackageResult.executeQuery("select spr.pkg from SurveyPackageResult spr where spr.participant = :participant and spr.surveyConfig = :surveyConfig", [participant: participant, surveyConfig: surveyConfig])}"/>

    <g:set var="costItemsSurvey"
           value="${surveyOrg && surveyPackages ? CostItem.findAllBySurveyOrgAndPkgInList(surveyOrg, surveyPackages) : null}"/>

    <g:set var="costItemsSubsc"
           value="${subscription ? CostItem.executeQuery('select ci from CostItem as ci left join ci.costItemElement cie where ci.owner in :owner and ci.sub = :sub and ci.isVisibleForSubscriber = true and ci.surveyOrg = null and ci.costItemStatus != :deleted and ci.pkg in (:pkgs)' +
                   ' order by cie.value_' + LocaleUtils.getCurrentLang(),
                   [owner: [subscription.getConsortium()], sub: subscription, deleted: RDStore.COST_ITEM_DELETED, pkgs: surveyPackages]) : null}"/>

    <% Set<RefdataValue> costItemElementsNotInSurveyCostItems = [] %>

    <g:if test="${(costItemsSubsc || costItemsSurvey)}">

        <div class="ui card la-time-card">

            <div class="content">
                <div class="header"><g:message code="surveyPackages.selectedPackages"/>: <g:message code="surveyCostItemsPackages.label"/></div>
            </div>

            <div class="content">
                <table class="ui celled compact la-js-responsive-table la-table-inCard table">
                    <thead>
                    <tr>
                        <th colspan="4" class="center aligned">
                            <g:message code="surveyConfigsInfo.oldPrice"/>
                        </th>
                        <th colspan="4" class="center aligned">
                            <g:message code="surveyConfigsInfo.newPrice"/>
                        </th>
                        <th rowspan="2">Diff.</th>
                    </tr>
                    <tr>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.newCosts.totalAmount"/></th>

                        <th class="la-smaller-table-head"><g:message code="financials.costItemElement"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.invoice_total"/></th>
                        <th class="la-smaller-table-head"><g:message code="financials.newCosts.taxTypeAndRate"/></th>
                        <th class="la-smaller-table-head"><g:message
                                code="financials.newCosts.totalAmount"/></th>

                    </tr>
                    </thead>


                    <tbody class="top aligned">
                    <g:each in="${costItemsSubsc}" var="costItem">
                        <tr>
                            <td>
                                <ui:costSign ci="${costItem}"/>

                                ${costItem.costItemElement?.getI10n('value')}
                            </td>
                            <td>
                                <strong>
                                    <g:formatNumber
                                            number="${costItem.costInBillingCurrency}"
                                            minFractionDigits="2" maxFractionDigits="2"
                                            type="number"/>
                                </strong>

                                ${costItem.billingCurrency?.getI10n('value')}
                            </td>
                            <td>
                                <g:if test="${costItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                    ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                </g:if>
                                <g:elseif test="${costItem.taxKey}">
                                    ${costItem.taxKey.taxType?.getI10n("value") + " (" + costItem.taxKey.taxRate + "%)"}
                                </g:elseif>
                            </td>
                            <td>
                                <strong>
                                    <g:formatNumber
                                            number="${costItem.costInBillingCurrencyAfterTax}"
                                            minFractionDigits="2" maxFractionDigits="2"
                                            type="number"/>
                                </strong>

                                ${costItem.billingCurrency?.getI10n('value')}

                                <g:if test="${costItem.startDate || costItem.endDate}">
                                    <br/>(${formatDate(date: costItem.startDate, format: message(code: 'default.date.format.notime'))} - ${formatDate(date: costItem.endDate, format: message(code: 'default.date.format.notime'))})
                                </g:if>
                            </td>

                            <g:set var="surveyCostItems" scope="request"
                                   value="${CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgInList(surveyOrg, RDStore.COST_ITEM_DELETED, costItem.costItemElement, surveyPackages)}"/>


                            <g:if test="${surveyCostItems && !(costItem.costItemElement in (costItemElementsNotInSurveyCostItems))}">
                                <g:each in="${surveyCostItems}"
                                        var="costItemSurvey">
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
                                    <td>
                                        <g:set var="oldCostItem"
                                               value="${costItem.costInBillingCurrency ?: 0.0}"/>

                                        <g:set var="newCostItem"
                                               value="${costItemSurvey.costInBillingCurrency ?: 0.0}"/>

                                        <strong><g:formatNumber
                                                number="${(newCostItem - oldCostItem)}"
                                                minFractionDigits="2" maxFractionDigits="2" type="number"/>
                                            <br/>
                                            (<g:formatNumber
                                                    number="${((newCostItem - oldCostItem) / oldCostItem) * 100}"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" type="number"/>%)</strong>
                                    </td>
                                </g:each>
                            </g:if>
                            <g:else>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
                            </g:else>
                        </tr>

                        <% costItemElementsNotInSurveyCostItems << costItem.costItemElement %>
                    </g:each>

                    <g:set var="costItemsWithoutSubCostItems"
                           value="${surveyOrg && costItemElementsNotInSurveyCostItems ? CostItem.findAllBySurveyOrgAndCostItemElementNotInListAndPkgInList(surveyOrg, costItemElementsNotInSurveyCostItems, surveyPackages) : []}"/>
                    <g:if test="${costItemsWithoutSubCostItems}">
                        <g:each in="${costItemsWithoutSubCostItems}" var="costItemSurvey">
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
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

                                <td>
                                </td>
                            </tr>
                        </g:each>
                    </g:if>

                    </tbody>
                </table>
            </div>
        </div>
    </g:if>
</g:if>

<g:if test="${surveyInfo.type.id == RDStore.SURVEY_TYPE_INTEREST.id}">
    <g:set var="surveyPackages"
           value="${SurveyPackageResult.executeQuery("select spr.pkg from SurveyPackageResult spr where spr.participant = :participant and spr.surveyConfig = :surveyConfig", [participant: participant, surveyConfig: surveyConfig])}"/>

    <g:set var="costItemsSurvey"
           value="${surveyOrg && surveyPackages ? CostItem.findAllBySurveyOrgAndPkgInList(surveyOrg, surveyPackages) : null}"/>

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