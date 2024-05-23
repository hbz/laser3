<%@ page import="de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.storage.RDStore; de.laser.utils.LocaleUtils" %>
<laser:serviceInjection/>
<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_TITLE_SELECTION.id]}">
    <g:set var="costItemsSurvey"
           value="${surveyOrg ? CostItem.findAllBySurveyOrgAndPkgIsNull(surveyOrg) : null}"/>

    <g:set var="costItemsSubsc"
           value="${CostItem.executeQuery('select ci from CostItem as ci left join ci.costItemElement cie where ci.owner in :owner and ci.sub = :sub and ci.isVisibleForSubscriber = true and ci.surveyOrg = null and ci.costItemStatus != :deleted and ci.pkg is null' +
                   ' order by cie.value_' + LocaleUtils.getCurrentLang(),
                   [owner: [subscription.getConsortia()], sub: subscription, deleted: RDStore.COST_ITEM_DELETED])}"/>

    <% Set<RefdataValue> costItemElementsNotInSurveyCostItems = [] %>

    <%
        def elementSign = 'notSet'
        String icon = ''
        String dataTooltip = ""
    %>

    <g:if test="${surveyInfo.owner.id != institution.id && (costItemsSubsc || costItemsSurvey)}">

        <div class="ui card la-time-card">

            <div class="content">
                <div class="header"><g:message code="surveyConfigsInfo.costItems"/></div>
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
                                <%
                                    elementSign = 'notSet'
                                    icon = ''
                                    dataTooltip = ""
                                    if (costItem.costItemElementConfiguration) {
                                        elementSign = costItem.costItemElementConfiguration
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
                                            icon = '<i class="question circle icon"></i>'
                                            break
                                    }
                                %>
                                <span class="la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${dataTooltip}">${raw(icon)}</span>

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
                                   value="${CostItem.findAllBySurveyOrgAndCostItemStatusNotEqualAndCostItemElementAndPkgIsNull(surveyOrg, RDStore.COST_ITEM_DELETED, costItem.costItemElement)}"/>


                            <g:if test="${surveyCostItems && !(costItem.costItemElement in (costItemElementsNotInSurveyCostItems))}">
                                <g:each in="${surveyCostItems}"
                                        var="costItemSurvey">
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
                                                    icon = '<i class="question circle icon"></i>'
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
                           value="${surveyOrg && costItemElementsNotInSurveyCostItems ? CostItem.findAllBySurveyOrgAndCostItemElementNotInListAndPkgIsNull(surveyOrg, costItemElementsNotInSurveyCostItems) : []}"/>
                    <g:if test="${costItemsWithoutSubCostItems}">
                        <g:each in="${costItemsWithoutSubCostItems}" var="costItemSurvey">
                            <tr>
                                <td></td>
                                <td></td>
                                <td></td>
                                <td></td>
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
                                                icon = '<i class="question circle icon"></i>'
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

    <g:if test="${surveyInfo.owner.id == institution.id}">
        <g:set var="consCostItems"
               value="${CostItem.executeQuery('select ci from CostItem ci right join ci.sub sub join sub.orgRelations oo left join ci.costItemElement cie ' +
                       'where ci.owner = :owner and sub.instanceOf = :sub and oo.roleType in (:roleTypes)  and ci.surveyOrg = null and ci.costItemStatus != :deleted' +
                       ' order by cie.value_' + LocaleUtils.getCurrentLang(),
                       [owner: [subscription.getConsortia()], sub: subscription, deleted: RDStore.COST_ITEM_DELETED, roleTypes: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])}"/>
        <g:set var="consCosts" value="${consCostItems ? financeService.calculateResults(consCostItems.id) : null}"/>
        <g:if test="${consCosts}">
            <div class="ui card la-dl-no-table">
                <div class="content">
                %{-- <g:if test="${costItemSums.ownCosts}">
                     <g:if test="${(contextOrg.id != subscription.getConsortia()?.id && subscription.instanceOf) || !subscription.instanceOf}">
                         <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.ownCosts')} </h2>
                         <laser:render template="/subscription/financials" model="[data: costItemSums.ownCosts]"/>
                     </g:if>
                 </g:if>--}%
                    <g:if test="${consCosts}">
                        <h2 class="ui header">${message(code: 'financials.label')} : ${message(code: 'financials.tab.consCosts')} ${message(code: 'surveyCostItem.info')}</h2>
                        <laser:render template="/subscription/financials" model="[data: consCosts]"/>
                    </g:if>
                </div>
            </div>
        </g:if>
    </g:if>
</g:if>