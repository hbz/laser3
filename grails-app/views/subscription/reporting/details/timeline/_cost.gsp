<%@ page import="de.laser.helper.RDStore; de.laser.Org;" %>
<laser:serviceInjection />

<g:render template="/subscription/reporting/details/timeline/base.part1" />

<g:if test="${neutralCostItems && relevantCostItems}">
    <div class="ui top attached stackable tabular la-tab-with-js menu">
        <a data-tab="summary" class="item active">Zusammenfassung</a>
        <a data-tab="neutral" class="item">Neutrale Kosten</a>
        <a data-tab="relevant" class="item">Preise</a>
    </div>

    <div data-tab="summary" class="ui bottom attached tab segment active">
</g:if>
<g:else>
    <div class="ui segment">
</g:else>

        <g:if test="${billingSums.size() == 1}">
            <table class="ui table la-js-responsive-table la-table compact">
                <thead>
                <tr>
                    <th style="width:25%"></th>
                    <th style="width:25%"></th>
                    <th style="width:25%">${message(code:'financials.sum.local')}</th>
                    <th style="width:25%">${message(code:'financials.sum.billingAfterTax')} (nach Steuern)</th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${billingSums}" var="entry" status="i">
                    <tr>
                        <td style="width:25%"></td>
                        <td style="width:25%"></td>
                        <td style="width:25%"><g:formatNumber number="${localSums.localSum}" type="currency" currencySymbol="EUR"/></td>
                        <td style="width:25%"><g:formatNumber number="${localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </g:if>
        <g:elseif test="${localSums}">
            <table class="ui table la-js-responsive-table la-table compact">
                <thead>
                <tr>
                    <th style="width:25%">${message(code:'financials.sum.billing')}</th>
                    <th style="width:25%">${message(code:'financials.sum.billingAfterTax')}</th>
                    <th style="width:25%">${message(code:'financials.sum.local')}</th>
                    <th style="width:25%">${message(code:'financials.sum.billingAfterTax')} (nach Steuern)</th>
                </tr>
                </thead>
                <tbody>
                    <g:each in="${billingSums}" var="entry" status="i">
                        <tr>
                            <td style="width:25%"><g:formatNumber number="${entry.billingSum}" type="currency" currencySymbol="${entry.currency}"/></td>
                            <td style="width:25%"><g:formatNumber number="${entry.billingSumAfterTax}" type="currency" currencySymbol="${entry.currency}"/></td>
                            <td style="width:25%"></td>
                            <td style="width:25%"></td>
                        </tr>
                    </g:each>
                    <tr>
                        <td style="width:25%"></td>
                        <td style="width:25%"></td>
                        <td style="width:25%"><strong><g:formatNumber number="${localSums.localSum}" type="currency" currencySymbol="EUR"/></strong></td>
                        <td style="width:25%"><strong><g:formatNumber number="${localSums.localSumAfterTax}" type="currency" currencySymbol="EUR"/></strong></td>
                    </tr>
                </tbody>
            </table>
        </g:elseif>
        <g:else>
            Es wurden keine Kosten gefunden.
        </g:else>

    </div><!-- .segment -->

<g:if test="${relevantCostItems && relevantCostItems}">
    <div data-tab="neutral" class="ui bottom attached tab segment">
</g:if>

<g:if test="${neutralCostItems}">
    <g:if test="${!relevantCostItems}"><div class="ui segment"></g:if>
    <table class="ui table la-js-responsive-table la-table compact">
        <thead>
        <tr>
            <th rowspan="2"></th>
            <th rowspan="2">${message(code:'reporting.local.subscription.timeline.timeline-member')}</th>
            <th rowspan="2">${message(code:'financials.costItemElement')}</th>
            <th rowspan="2">${message(code:'financials.taxRate')}</th>
            <th rowspan="2"></th>
            <th class="la-smaller-table-head">${message(code:'financials.sum.billing')}</th>
            <th class="la-smaller-table-head">${message(code:'financials.sum.local')}</th>
            <th class="la-smaller-table-head">${message(code:'financials.dateFrom')}</th>
        </tr>
        <tr>
            <th class="la-smaller-table-head">${message(code:'financials.sum.billingAfterTax')}</th>
            <th class="la-smaller-table-head">${message(code:'financials.sum.localAfterTax')}</th>
            <th class="la-smaller-table-head">${message(code:'financials.dateTo')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${neutralCostItems}" var="ci" status="i">
            <tr>
                <td>${i+1}.</td>
                <td>
                    <g:each in="${ci.sub.orgRelations}" var="ciSubscr">
                        <g:if test="${[RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id].contains(ciSubscr.roleType.id)}">
                            <g:link controller="org" action="show" id="${ciSubscr.org.id}">${ciSubscr.org.sortname ?: ciSubscr.org.name}</g:link>
                        </g:if>
                    </g:each>
                </td>
                <td>
                    ${ci.costItemElement?.getI10n("value")}
                </td>
                <td>
                    <g:if test="${ci.taxKey && ci.taxKey.display}">
                        ${ci.taxKey.taxRate + '%'}
                    </g:if>
                    <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                        ${RDStore.TAX_REVERSE_CHARGE.getI10n("value")}
                    </g:elseif>
                    <g:elseif test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                        ${ci.taxKey.taxType.getI10n("value")}
                    </g:elseif>
                    <g:elseif test="${! ci.taxKey}">
                        <g:message code="financials.taxRate.notSet"/>
                    </g:elseif>
                </td>
                <td>
                    <%
                        switch (ci.costItemElementConfiguration) {
                            case RDStore.CIEC_POSITIVE:
                                print '<i class="plus green circle icon"></i>'
                                break
                            case RDStore.CIEC_NEGATIVE:
                                print '<i class="minus red circle icon"></i>'
                                break
                            case RDStore.CIEC_NEUTRAL:
                                print '<i class="circle yellow icon"></i>'
                                break
                            default:
                                print'<i class="question circle icon"></i>'
                                break
                        }
                    %>
                </td>
                <td>
                    <span style="color:darkgrey"><g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencySymbol="${ci.billingCurrency ?: 'EUR'}" /></span>
                    <br />
                    <span class="la-secondHeaderRow" data-label="${message(code:'financials.sum.billingAfterTax')}:">
                        <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="${ci.billingCurrency ?: 'EUR'}" />
                    </span>
                </td>
                <td>
                    <span style="color:darkgrey"><g:formatNumber number="${ci.costInLocalCurrency ?: 0.0}" type="currency" currencySymbol="EUR" /></span>
                    <br />
                    <span class="la-secondHeaderRow" data-label="${message(code:'financials.sum.localAfterTax')}:">
                        <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="EUR" />
                    </span>
                </td>
                <td>
                    <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.startDate}"/>
                    <br />
                    <span class="la-secondHeaderRow" data-label="${message(code:'financials.dateTo')}:">
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.endDate}"/>
                    </span>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>

    </div><!-- .segment -->
</g:if>

<g:if test="${relevantCostItems && neutralCostItems}">
    <div data-tab="relevant" class="ui bottom attached tab segment">
</g:if>

<g:if test="${relevantCostItems}">
    <g:if test="${!neutralCostItems}"><div class="ui segment"></g:if>
        <table class="ui table la-js-responsive-table la-table compact">
            <thead>
                <tr>
                    <th rowspan="2"></th>
                    <th rowspan="2">${message(code:'reporting.local.subscription.timeline.timeline-member')}</th>
                    <th rowspan="2">${message(code:'financials.costItemElement')}</th>
                    <th rowspan="2">${message(code:'financials.taxRate')}</th>
                    <th rowspan="2"></th>
                    <th class="la-smaller-table-head">${message(code:'financials.sum.billing')}</th>
                    <th class="la-smaller-table-head">${message(code:'financials.sum.local')}</th>
                    <th class="la-smaller-table-head">${message(code:'financials.dateFrom')}</th>
                </tr>
                <tr>
                    <th class="la-smaller-table-head">${message(code:'financials.sum.billingAfterTax')}</th>
                    <th class="la-smaller-table-head">${message(code:'financials.sum.localAfterTax')}</th>
                    <th class="la-smaller-table-head">${message(code:'financials.dateTo')}</th>
                </tr>
            </thead>
            <tbody>
            <g:each in="${relevantCostItems}" var="ci" status="i">
                <tr>
                    <td>${i+1}</td>
                    <td>
                        <g:each in="${ci.sub.orgRelations}" var="ciSubscr">
                            <g:if test="${[RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id].contains(ciSubscr.roleType.id)}">
                                <g:link controller="org" action="show" id="${ciSubscr.org.id}">${ciSubscr.org.sortname ?: ciSubscr.org.name}</g:link>
                            </g:if>
                        </g:each>
                    </td>
                    <td>
                        ${ci.costItemElement?.getI10n("value")}
                    </td>
                    <td>
                        <g:if test="${ci.taxKey && ci.taxKey.display}">
                            ${ci.taxKey.taxRate + '%'}
                        </g:if>
                        <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                            ${RDStore.TAX_REVERSE_CHARGE.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                            ${ci.taxKey.taxType.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${! ci.taxKey}">
                            <g:message code="financials.taxRate.notSet"/>
                        </g:elseif>
                    </td>
                    <td>
                        <%
                            switch (ci.costItemElementConfiguration) {
                                case RDStore.CIEC_POSITIVE:
                                    print '<i class="plus green circle icon"></i>'
                                    break
                                case RDStore.CIEC_NEGATIVE:
                                    print '<i class="minus red circle icon"></i>'
                                    break
                                case RDStore.CIEC_NEUTRAL:
                                    print '<i class="circle yellow icon"></i>'
                                    break
                                default:
                                    print'<i class="question circle icon"></i>'
                                    break
                            }
                        %>
                    </td>
                    <td>
                        <span style="color:darkgrey"><g:formatNumber number="${ci.costInBillingCurrency ?: 0.0}" type="currency" currencySymbol="${ci.billingCurrency ?: 'EUR'}" /></span>
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code:'financials.sum.billingAfterTax')}:">
                            <g:formatNumber number="${ci.costInBillingCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="${ci.billingCurrency ?: 'EUR'}" />
                        </span>
                    </td>
                    <td>
                        <span style="color:darkgrey"><g:formatNumber number="${ci.costInLocalCurrency ?: 0.0}" type="currency" currencySymbol="EUR" /></span>
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code:'financials.sum.localAfterTax')}:">
                            <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency" currencySymbol="EUR" />
                        </span>
                    </td>
                    <td>
                        <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.startDate}"/>
                        <br />
                        <span class="la-secondHeaderRow" data-label="${message(code:'financials.dateTo')}">
                            <g:formatDate format="${message(code:'default.date.format.notime')}" date="${ci.endDate}"/>
                        </span>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>

    </div><!-- .segment -->
</g:if>

<g:render template="/subscription/reporting/details/loadJavascript"  />
<g:render template="/subscription/reporting/export/detailsModal" model="[modalID: 'detailsExportModal', token: token]" />



