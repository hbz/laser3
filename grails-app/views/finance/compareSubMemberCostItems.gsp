<%@ page import="de.laser.CustomerTypeService; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="subscription.details.compareSubMemberCostItems.label"/>

<laser:serviceInjection/>
<g:set var="own" value="${financialData.own}"/>
<g:set var="cons" value="${financialData.cons}"/>
<g:set var="subscr" value="${financialData.subscr}"/>
<ui:debugInfo>
    <laser:render template="/templates/debug/benchMark" model="[debug: benchMark]"/>
</ui:debugInfo>
<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <ui:crumb class="active" text="${subscription.name}"/>
</ui:breadcrumbs>

<%
    List<GString> total = []
    dataToDisplay.each { view ->
        switch (view) {
            case 'own': total << "${own.count} ${message(code: 'financials.header.ownCosts')}"
                break
            case ['cons', 'consAtSubscr']: total << "${cons.count} ${message(code: 'financials.header.consortialCosts')}"
                break
        }
    }
%>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" type="subscription"
                     visibleOrgRelations="${visibleOrgRelations}">
    <laser:render template="/subscription/iconSubscriptionIsChild"/>

    ${message(code: 'subscription.details.compareSubMemberCostItems.label')} ${message(code: 'default.for')} ${subscription}
</ui:h1HeaderWithIcon>
<ui:totalNumber class="la-numberHeader" total="${total.join(' / ')}"/>
<ui:anualRings mapping="subfinance" object="${subscription}" controller="finance" action="index"
               navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<laser:render template="/subscription/${customerTypeService.getNavTemplatePath()}"
              model="${[subscription: subscription, params: (params << [id: subscription.id, showConsortiaFunctions: showConsortiaFunctions])]}"/>

<g:if test="${showConsortiaFunctions}">
    <laser:render template="/subscription/message" model="${[contextOrg: institution, subscription: subscription]}"/>
</g:if>

<br/>



<g:if test="${editable}">
    <div class="field" style="text-align: right;">
        <g:if test="${showBulkCostItems == null || showBulkCostItems == 'false'}">
            <g:if test="${fixedSubscription}">
                <g:link class="ui button" action="compareSubMemberCostItems" id="${fixedSubscription.id}"
                        controller="finance" params="${params + [showView: showView, showBulkCostItems: 'true']}">
                    ${g.message(code: 'financials.bulkCostItems.show')}
                </g:link>
            </g:if>
        </g:if>
        <g:else>
            <g:if test="${fixedSubscription}">
                <g:link class="ui button" action="compareSubMemberCostItems" id="${fixedSubscription.id}"
                        controller="finance" params="${params + [showView: showView, showBulkCostItems: 'false']}">
                    ${g.message(code: 'financials.bulkCostItems.hidden')}
                </g:link>
            </g:if>
            <br>
            <br>
        </g:else>

    </div>
</g:if>

<g:form action="processCostItemsBulk" name="editCost_${idSuffix}" method="post" class="ui form">
    <g:if test="${showBulkCostItems == 'true'}">
        <div>

            <div class="fields la-forms-grid">
                <fieldset
                        class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
                    <div class="field center aligned">

                        <label>${message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}</label>

                        <div class="ui right labeled input">
                            <input type="number"
                                   name="percentOnOldPrice"
                                   id="percentOnOldPrice"
                                   placeholder="${g.message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}"
                                   value="" step="0.01"/>

                            <div class="ui basic label">%</div>
                        </div>
                    </div>
                </fieldset>
            </div>

            <div class="ui horizontal divider"><g:message
                    code="search.advancedSearch.option.OR"/></div>

            <div class="fields la-forms-grid">
                <fieldset
                        class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
                    <div class="field center aligned">

                        <label>${message(code: 'surveyCostItems.bulkOption.percentOnCurrentPrice')}</label>

                        <div class="ui right labeled input">
                            <input type="number"
                                   name="percentOnCurrentPrice"
                                   id="percentOnCurrentPrice"
                                   placeholder="${g.message(code: 'surveyCostItems.bulkOption.percentOnCurrentPrice')}"
                                   value="" step="0.01"/>

                            <div class="ui basic label">%</div>
                        </div>
                    </div>
                </fieldset>
            </div>

            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                    <button class="ui button"
                            type="submit">${message(code: 'financials.bulkCostItems.submit')}</button>
                </div>

                <div class="eight wide field" style="text-align: right;">
                </div>
            </div>
        </div>
    </g:if>


    <g:set var="sumOldCostInBillingCurrency" value="${0.0}"/>
    <g:set var="sumNewCostInBillingCurrency" value="${0.0}"/>
    <g:set var="sumOldCostInBillingCurrencyAfterTax" value="${0.0}"/>
    <g:set var="sumNewCostInBillingCurrencyAfterTax" value="${0.0}"/>
    <g:set var="oldCostItem" value="${0.0}"/>
    <g:set var="oldCostItemAfterTax" value="${0.0}"/>

    <%
        int colspan1 = 4
        int colspan2 = 8
        int wideColspan2 = 13
        Map sorting
        int offset

        sorting = [consSort: true]
        offset = offsets.consOffset

    %>
    <table id="costTable_${customerType}"
           class="ui celled monitor stackable sortable  table la-js-responsive-table la-table la-ignore-fixed">
        <thead>
        <tr>
            <g:if test="${showBulkCostItems && editable}">
                <th scope="col" rowspan="2">
                    <g:if test="${data.costItems}">
                        <g:checkBox name="costItemListToggler" id="costItemListToggler" checked="false"/>
                    </g:if>
                </th>
            </g:if>
            <th scope="col" rowspan="2">${message(code: 'sidewide.number')}</th>
            <g:sortableColumn property="oo.org.sortname"
                              title="${message(code: 'financials.newCosts.costParticipants')}"
                              params="${sorting + [sub: fixedSubscription.id]}" action="compareSubMemberCostItems"
                              scope="col"
                              rowspan="2"/>
            <g:sortableColumn property="costItemElement" title="${message(code: 'financials.costItemElement')}"
                              params="${sorting + [sub: fixedSubscription.id]}" action="compareSubMemberCostItems"
                              scope="col"
                              rowspan="2"/>
            <th colspan="4" class="center aligned"><g:message
                    code="subscription.details.compareSubMemberCostItems.oldCostItem"/></th>

            <th colspan="4" class="center aligned"><g:message
                    code="subscription.details.compareSubMemberCostItems.newCostItem"/></th>

            <g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
                <th class="la-action-info" scope="col" rowspan="2"><g:message code="default.actions.label"/></th>
            </g:if>
        </tr>
        <tr>
            <th scope="col" rowspan="2">${message(code: 'financials.invoice_total')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.taxRate')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.amountFinal')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.newCosts.value')}</th>


            <g:sortableColumn property="costInBillingCurrency" title="${message(code: 'financials.invoice_total')}"
                              params="${sorting + [sub: fixedSubscription.id]}" action="compareSubMemberCostItems"
                              scope="col"
                              rowspan="2"/>
            <th scope="col" rowspan="2">${message(code: 'financials.taxRate')}</th>

            <th scope="col" rowspan="2">${message(code: 'financials.amountFinal')}</th>

            <g:sortableColumn property="costInLocalCurrency" title="${message(code: 'financials.newCosts.value')}"
                              params="${sorting + [sub: fixedSubscription.id]}" action="compareSubMemberCostItems"
                              scope="col"
                              rowspan="2"/>
        </tr>
        </thead>
        <tbody>
        %{--Empty result set--}%
        <g:if test="${data.count == 0}">
            <tr>
                <td colspan="${wideColspan2}" style="text-align:center">
                    <br/>
                    <g:if test="${msg}">${msg}</g:if>
                    <g:else>${message(code: 'finance.result.filtered.empty')}</g:else>
                    <br/>
                </td>
            </tr>
        </g:if>
        <g:else>
            <g:each in="${data.costItems}" var="ci" status="jj">
                <g:set var="oldCostItem" value="${0.0}"/>
                <g:set var="oldCostItemAfterTax" value="${0.0}"/>
                <g:set var="previousSub" value="${ci.sub._getCalculatedPreviousForSurvey()}"/>
                <g:set var="previousSubCostItems" value="${null}"/>
                <g:if test="${previousSub}">
                    <g:set var="previousSubCostItems"
                           value="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(previousSub, institution, ci.costItemElement, RDStore.COST_ITEM_DELETED)}"/>
                </g:if>

                <tr id="bulkdelete-b${ci.id}" class="${!previousSubCostItems ? 'red' : ''}">
                    <g:if test="${showBulkCostItems && editable}">
                        <td>
                            <g:checkBox id="selectedCostItems_${ci.id}" name="selectedCostItems" value="${ci.id}"
                                        checked="false"/>
                        </td>
                    </g:if>
                    <td>
                        <%
                            Set<Long> memberRoles = [RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id]
                        %>
                        ${jj + 1 + offset}
                    </td>
                    <td>
                        <g:each in="${ci.sub.orgRelations}" var="or">
                            <g:if test="${memberRoles.contains(or.roleType.id)}">
                                <g:link controller="org" action="show" id="${or.org.id}"><span
                                        class="la-popup-tooltip la-delay"
                                        data-content="${or.org.name}">${or.org.sortname}</span></g:link>
                            </g:if>
                        </g:each>
                    </td>
                    <td>
                        ${ci.costItemElement?.getI10n("value")}
                    </td>
                    <td>
                        <g:each in="${previousSubCostItems}"
                                var="costItemParticipantSub">
                            <g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}"
                                            minFractionDigits="2"
                                            maxFractionDigits="2" type="number"/>

                            ${(costItemParticipantSub.billingCurrency?.getI10n('value')?.split('-')).first()}
                            <g:set var="sumOldCostInBillingCurrency"
                                   value="${sumOldCostInBillingCurrency + costItemParticipantSub.costInBillingCurrency ?: 0}"/>

                            <g:set var="oldCostItem"
                                   value="${costItemParticipantSub.costInBillingCurrency ?: null}"/>
                        </g:each>
                    </td>
                    <td>
                        <g:if test="${previousSubCostItems}">
                            <g:each in="${previousSubCostItems}"
                                    var="costItemParticipantSub">

                                <g:if test="${costItemParticipantSub.taxKey && costItemParticipantSub.taxKey.display}">
                                    ${costItemParticipantSub.taxKey.taxRate + '%'}
                                </g:if>
                                <g:elseif
                                        test="${costItemParticipantSub.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                                    ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                                </g:elseif>
                                <g:elseif
                                        test="${costItemParticipantSub.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                                    ${costItemParticipantSub.taxKey.taxType.getI10n("value")}
                                </g:elseif>
                                <g:elseif test="${!costItemParticipantSub.taxKey}">
                                    <g:message code="financials.taxRate.notSet"/>
                                </g:elseif>
                            </g:each>
                        </g:if>

                    </td>
                    <td>
                        <g:each in="${previousSubCostItems}"
                                var="costItemParticipantSub">

                            <g:formatNumber
                                    number="${costItemParticipantSub.costInBillingCurrencyAfterTax}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2" type="number"/>

                            ${(costItemParticipantSub.billingCurrency?.getI10n('value')?.split('-')).first()}
                            <g:set var="sumOldCostInBillingCurrencyAfterTax"
                                   value="${sumOldCostInBillingCurrencyAfterTax + costItemParticipantSub.costInBillingCurrencyAfterTax ?: 0}"/>
                            <g:set var="oldCostItemAfterTax"
                                   value="${costItemParticipantSub.costInBillingCurrencyAfterTax ?: null}"/>
                        </g:each>
                    </td>
                    <td>
                        <g:each in="${previousSubCostItems}"
                                var="costItemParticipantSub">
                            <g:formatNumber number="${costItemParticipantSub.costInLocalCurrency ?: 0.0}"
                                            type="currency" currencySymbol="EUR"/>
                            <br/>
                            <span class="la-secondHeaderRow"
                                  data-label="${message(code: 'costItem.costInLocalCurrencyAfterTax.label')}:">
                                <g:formatNumber number="${costItemParticipantSub.costInLocalCurrencyAfterTax ?: 0.0}"
                                                type="currency" currencySymbol="EUR"/>
                            </span>
                        </g:each>
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInBillingCurrency}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/>

                        ${(ci.billingCurrency?.getI10n('value')?.split('-')).first()}
                        <g:set var="sumNewCostInBillingCurrency"
                               value="${sumNewCostInBillingCurrency + ci.costInBillingCurrency ?: 0}"/>

                        <g:if test="${oldCostItem}">
                            <strong>
                                <g:formatNumber
                                    number="${((ci.costInBillingCurrency - oldCostItem) / oldCostItem) * 100}"
                                    minFractionDigits="2"
                                    maxFractionDigits="2" type="number"/>%
                            </strong>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${ci.taxKey && ci.taxKey.display}">
                            ${ci.taxKey.taxRate + '%'}
                        </g:if>
                        <g:elseif test="${ci.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE}">
                            ${RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n("value")}
                        </g:elseif>
                        <g:elseif
                                test="${ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19]}">
                            ${ci.taxKey.taxType.getI10n("value")}
                        </g:elseif>
                        <g:elseif test="${!ci.taxKey}">
                            <g:message code="financials.taxRate.notSet"/>
                        </g:elseif>
                    </td>
                    <td>
                        <g:formatNumber
                                number="${ci.costInBillingCurrencyAfterTax}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>

                        ${(ci.billingCurrency?.getI10n('value')?.split('-')).first()}
                        <g:set var="sumNewCostInBillingCurrencyAfterTax"
                               value="${sumNewCostInBillingCurrencyAfterTax + ci.costInBillingCurrencyAfterTax ?: 0}"/>

                        <g:if test="${oldCostItemAfterTax}">
                            <br/><strong><g:formatNumber
                                number="${((ci.costInBillingCurrencyAfterTax - oldCostItemAfterTax) / oldCostItemAfterTax) * 100}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>%</strong>
                        </g:if>
                    </td>
                    <td>
                        <g:formatNumber number="${ci.costInLocalCurrency ?: 0.0}" type="currency" currencySymbol="EUR"/>
                        <br/>
                        <span class="la-secondHeaderRow"
                              data-label="${message(code: 'costItem.costInLocalCurrencyAfterTax.label')}:">
                            <g:formatNumber number="${ci.costInLocalCurrencyAfterTax ?: 0.0}" type="currency"
                                            currencySymbol="EUR"/>
                        </span>
                    </td>
                    <g:if test="${editable}">
                        <td class="x">
                            <g:link controller="finance" action="editCostItem"
                                    params='[sub: "${ci.sub?.id}", id: "${ci.id}", showView: "cons", offset: params.offset]'
                                    class="ui icon button blue la-modern-button trigger-modal"
                                    data-id_suffix="edit_${ci.id}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="write icon"></i>
                            </g:link>
                        </td>
                    </g:if>
                </tr>
            </g:each>
        </g:else>
        </tbody>
        <tfoot>
        <tr>
            <td></td>
            <td><strong><g:message code="financials.export.sums"/></strong></td>
            <td></td>
            <td></td>
            <td><g:formatNumber number="${sumOldCostInBillingCurrency}" minFractionDigits="2"
                                maxFractionDigits="2" type="number"/></td>
            <td></td>
            <td>
                <g:formatNumber number="${sumOldCostInBillingCurrencyAfterTax}" minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>
            </td>
            <td></td>
            <td>
                <g:formatNumber number="${sumNewCostInBillingCurrency}" minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>

                <g:if test="${sumOldCostInBillingCurrency}">
                    <br/>
                    <strong><g:formatNumber
                            number="${((sumNewCostInBillingCurrency - sumOldCostInBillingCurrency) / sumOldCostInBillingCurrency) * 100}"
                            minFractionDigits="2"
                            maxFractionDigits="2" type="number"/>%
                    </strong>
                </g:if>
            </td>
            <td></td>
            <td>
                <g:formatNumber number="${sumNewCostInBillingCurrencyAfterTax}" minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>

                <g:if test="${sumOldCostInBillingCurrencyAfterTax}">

                    <strong>
                        <g:formatNumber
                            number="${((sumNewCostInBillingCurrencyAfterTax - sumOldCostInBillingCurrencyAfterTax) / sumOldCostInBillingCurrencyAfterTax) * 100}"
                            minFractionDigits="2"
                            maxFractionDigits="2" type="number"/>%
                    </strong>
                </g:if>
            </td>
            <td></td>
        </tr>
        </tfoot>
    </table>

</g:form>

<g:if test="${data.costItems}">
    <ui:paginate action="compareSubMemberCostItems" params="${params + [showView: showView]}"
                 max="${max}" offset="${offset}" total="${data.count}"/>
</g:if>

<g:if test="${editable}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#costItemListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedCostItems]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedCostItems]").prop('checked', false)
            }
        });

        $('table[id^=costTable] .x .trigger-modal').on('click', function(e) {
                    e.preventDefault();
                    let idSuffix = $(this).attr("data-id_suffix");
                    $.ajax({
                        url: $(this).attr('href')
                    }).done( function(data) {
                        $('.ui.dimmer.modals > #costItem_ajaxModal').remove();
                        $('#dynamicModalContainer').empty().html(data);

                        $('#dynamicModalContainer .ui.modal').modal({
                            onVisible: function () {
                                r2d2.initDynamicUiStuff('#costItem_ajaxModal');
                                r2d2.initDynamicXEditableStuff('#costItem_ajaxModal');
                                JSPC.app['finance'+idSuffix].updateTitleDropdowns();

                                r2d2.helper.focusFirstFormElement(this);
                            },
                            detachable: true,
                            autofocus: false,
                            closable: false,
                            transition: 'scale',
                            onApprove : function() {
                                $(this).find('.ui.form').submit();
                                return false;
                            }
                        }).modal('show');
                    })
                });
    </laser:script>
</g:if>

<laser:htmlEnd/>
