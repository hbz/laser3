<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDConstants; de.laser.utils.DateUtils; de.laser.CustomerTypeService; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="subscription.details.compareSubMemberCostItems.label"/>

<laser:serviceInjection/>

<ui:breadcrumbs>
    <ui:crumb controller="myInstitution" action="currentSubscriptions" message="myinst.currentSubscriptions.label"/>
    <ui:crumb controller="subscription" action="show" id="${params.id}" text="${subscription}"/>
    <ui:crumb controller="subscription" action="subfinance" id="${params.id}" message="subscription.details.financials.label"/>
    <ui:crumb class="active" message="subscription.details.compareSubMemberCostItems.label"/>

</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="${customerTypeService.getActionsTemplatePath()}"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon referenceYear="${subscription?.referenceYear}" type="subscription"
                     visibleProviders="${providerRoles}">
    <laser:render template="/subscription/iconSubscriptionIsChild"/>

    ${message(code: 'subscription.details.compareSubMemberCostItems.label')} ${message(code: 'default.for')} ${subscription}
</ui:h1HeaderWithIcon>
<ui:totalNumber class="la-numberHeader" total="${0}"/>
<ui:anualRings object="${subscription}" action="compareSubMemberCostItems"
               navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

<br/>

<ui:filter>
    <g:form action="compareSubMemberCostItems" params="${[id: params.id]}" method="get" class="ui form">
        <laser:render template="/templates/filter/orgFilter"
                      model="[
                              tmplConfigShow      : [['name', 'identifier', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], ['discoverySystemsFrontend', 'discoverySystemsIndex'], ['subRunTimeMultiYear']],
                              tmplConfigFormFilter: true
                      ]"/>
    </g:form>
</ui:filter>

<g:if test="${editable}">
    <div class="field" style="text-align: right;">
        <g:if test="${showBulkCostItems == null || showBulkCostItems == 'false'}">
            <g:if test="${subscription}">
                <g:link class="${Btn.SIMPLE}" action="compareSubMemberCostItems" id="${subscription.id}"
                         params="${params + [showView: showView, showBulkCostItems: 'true']}">
                    ${g.message(code: 'financials.bulkCostItems.show')}
                </g:link>
            </g:if>
        </g:if>
        <g:else>
            <g:if test="${subscription}">
                <g:link class="${Btn.SIMPLE}" action="compareSubMemberCostItems" id="${subscription.id}"
                         params="${params + [showView: showView, showBulkCostItems: 'false']}">
                    ${g.message(code: 'financials.bulkCostItems.hidden')}
                </g:link>
            </g:if>
            <br>
            <br>
        </g:else>

    </div>
</g:if>

<g:form action="compareSubMemberCostItems" name="editCost_${idSuffix}" method="post" class="ui form" params="[processBulkCostItems: true]">
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
                    <button class="${Btn.SIMPLE}"
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

    <g:set var="costItemElements"
           value="${costItemsByCostItemElement.collect {RefdataValue.findByValueAndOwner(it.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))}}"/>



    <table id="costTable_${customerType}"
           class="ui celled monitor stackable sortable  table la-js-responsive-table la-table la-ignore-fixed">
        <thead>
        <tr>
            <g:if test="${showBulkCostItems && editable}">
                <th scope="col" rowspan="3">
                        <g:checkBox name="subListToggler" id="subListToggler" checked="false"/>
                </th>
            </g:if>

            <th scope="col" rowspan="3">${message(code: 'sidewide.number')}</th>
            <g:sortableColumn title="${message(code: 'org.sortname.label')}" property="lower(o.sortname)" rowspan="3"
                              class="center aligned"
                              params="${request.getParameterMap()}"/>
            <g:sortableColumn title="${message(code: 'org.fullName.label')}" property="lower(o.name)" rowspan="3"
                              class="center aligned"
                              params="${request.getParameterMap()}"/>

            <th rowspan="3" class="center aligned la-no-uppercase">
                <ui:multiYearIcon isConsortial="true"/>
            </th>
            <th colspan="10" class="center aligned"><ui:select name="selectedCostItemElementID"
                                                              from="${costItemElements}"
                                                              optionKey="id"
                                                              optionValue="value"
                                                              value="${selectedCostItemElementID}"
                                                              class="ui dropdown clearable"
                                                              id="selectedCostItemElementID"/>
            </th>
            <g:if test="${contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
                <th class="center aligned" scope="col" rowspan="3">
                    <ui:optionsIcon />
                </th>
            </g:if>
        </tr>
        <tr>
            <th colspan="5" class="center aligned"><g:message
                    code="subscription.details.compareSubMemberCostItems.oldCostItem"/></th>

            <th colspan="5" class="center aligned"><g:message
                    code="subscription.details.compareSubMemberCostItems.newCostItem"/></th>

        </tr>
        <tr>
            <th scope="col" rowspan="2">${message(code: 'default.date.label')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.invoice_total')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.taxRate')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.amountFinal')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.newCosts.value')}</th>


            <th scope="col" rowspan="2">${message(code: 'default.date.label')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.invoice_total')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.taxRate')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.amountFinal')}</th>
            <th scope="col" rowspan="2">${message(code: 'financials.newCosts.value')}</th>

        </tr>
        </thead>
        <tbody>
        <g:each in="${filteredSubChilds}" status="i" var="row">
            <g:set var="sub" value="${row.sub}"/>
            <g:set var="oldCostItem" value="${0.0}"/>
            <g:set var="oldCostItemAfterTax" value="${0.0}"/>
            <g:set var="previousSub" value="${sub._getCalculatedPreviousForSurvey()}"/>
            <g:set var="previousSubCostItems" value="${null}"/>
            <g:if test="${previousSub}">
                <g:set var="previousSubCostItems"
                       value="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(previousSub, institution, selectedCostItemElement, RDStore.COST_ITEM_DELETED)}"/>
            </g:if>

            <g:set var="currentSubCostItems"
                   value="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(sub, institution, selectedCostItemElement, RDStore.COST_ITEM_DELETED)}"/>

            <tr class="${!currentSubCostItems ? 'red' : ''}">
                <g:set var="subscr" value="${row.orgs}"/>
                <g:if test="${showBulkCostItems && editable}">
                    <td>
                        <g:if test="${currentSubCostItems && currentSubCostItems.size() == 1 && ((previousSubCostItems && previousSubCostItems.size() <= 1) || previousSubCostItems == null )}">
                            <g:checkBox id="selectedSubs_${sub.id}" name="selectedSubs" value="${sub.id}"
                                        checked="false"/>
                        </g:if>
                    </td>
                </g:if>
                <td>${i + 1}</td>
                <td>
                    <g:link controller="subscription" action="show" id="${sub.id}"> ${subscr.sortname}</g:link>
                </td>
                <td>
                    <g:link controller="organisation" action="show" id="${subscr.id}">${subscr}</g:link>
                    <ui:customerTypeOnlyProIcon org="${subscr}"/>
                </td>
                <td>
                    <g:if test="${sub.isMultiYear}">
                        <ui:multiYearIcon isConsortial="true" color="orange"/>
                    </g:if>
                </td>

                <td>
                    <g:each in="${previousSubCostItems}"
                            var="costItemParticipantSub">
                        <g:if test="${costItemParticipantSub.startDate || costItemParticipantSub.endDate}">
                            ${costItemParticipantSub.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItemParticipantSub.startDate) : ''} - ${costItemParticipantSub.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItemParticipantSub.endDate) : ''}
                        </g:if>
                    </g:each>
                </td>

                <td>
                    <g:each in="${previousSubCostItems}"
                            var="costItemParticipantSub">
                        <g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/>

                        ${costItemParticipantSub.billingCurrency?.getI10n('value')}
                        <g:set var="sumOldCostInBillingCurrency"
                               value="${sumOldCostInBillingCurrency + costItemParticipantSub.costInBillingCurrency ?: 0}"/>

                        <g:set var="oldCostItem"
                               value="${costItemParticipantSub.costInBillingCurrency ?: null}"/>
                    </g:each>
                </td>
                <td>
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
                </td>
                <td>
                    <g:each in="${previousSubCostItems}"
                            var="costItemParticipantSub">

                        <g:formatNumber
                                number="${costItemParticipantSub.costInBillingCurrencyAfterTax}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>

                        ${costItemParticipantSub.billingCurrency?.getI10n('value')}
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
                <g:set var="wrongStartDateEndDate"
                       value=""/>
                <g:each in="${currentSubCostItems}"
                        var="costItemParticipantSub">
                    <g:if test="${costItemParticipantSub.startDate != sub.startDate || costItemParticipantSub.endDate != sub.endDate}">
                        <g:set var="wrongStartDateEndDate"
                               value="red"/>
                    </g:if>
                </g:each>

                <td class="${wrongStartDateEndDate}">
                    <g:each in="${currentSubCostItems}"
                            var="costItemParticipantSub">
                        <g:if test="${costItemParticipantSub.startDate || costItemParticipantSub.endDate}">
                            ${costItemParticipantSub.startDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItemParticipantSub.startDate) : ''} - ${costItemParticipantSub.endDate ? DateUtils.getLocalizedSDF_noTimeShort().format(costItemParticipantSub.endDate) : ''}
                        </g:if>
                    </g:each>
                </td>

                <td>
                    <g:each in="${currentSubCostItems}"
                            var="costItemParticipantSub">
                        <g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/>

                        ${costItemParticipantSub.billingCurrency?.getI10n('value')}
                        <g:set var="sumNewCostInBillingCurrency"
                               value="${sumNewCostInBillingCurrency + costItemParticipantSub.costInBillingCurrency ?: 0}"/>

                        <g:if test="${oldCostItem}">
                            <strong>
                                <g:formatNumber
                                        number="${((costItemParticipantSub.costInBillingCurrency - oldCostItem) / oldCostItem) * 100}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/>%
                            </strong>
                        </g:if>

                    </g:each>
                </td>
                <td>
                    <g:each in="${currentSubCostItems}"
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
                </td>
                <td>
                    <g:each in="${currentSubCostItems}"
                            var="costItemParticipantSub">

                        <g:formatNumber
                                number="${costItemParticipantSub.costInBillingCurrencyAfterTax}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>

                        ${costItemParticipantSub.billingCurrency?.getI10n('value')}
                        <g:set var="sumNewCostInBillingCurrencyAfterTax"
                               value="${sumNewCostInBillingCurrencyAfterTax + costItemParticipantSub.costInBillingCurrencyAfterTax ?: 0}"/>

                        <g:if test="${oldCostItemAfterTax}">
                            <br/><strong><g:formatNumber
                                number="${((costItemParticipantSub.costInBillingCurrencyAfterTax - oldCostItemAfterTax) / oldCostItemAfterTax) * 100}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>%</strong>
                        </g:if>
                    </g:each>
                </td>
                <td>
                    <g:each in="${currentSubCostItems}"
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

                <g:if test="${editable}">
                    <td class="x">
                        <g:each in="${currentSubCostItems}"
                                var="costItemParticipantSub">
                            <g:link controller="finance" action="editCostItem"
                                    params='[sub: "${costItemParticipantSub.sub?.id}", id: "${costItemParticipantSub.id}"]'
                                    class="${Btn.MODERN.SIMPLE} trigger-modal"
                                    data-id_suffix="edit_${costItemParticipantSub.id}"
                                    role="button"
                                    aria-label="${message(code: 'ariaLabel.edit.universal')}">
                                <i aria-hidden="true" class="${Icon.CMD.EDIT}"></i>
                            </g:link>
                        </g:each>
                    </td>
                </g:if>
            </tr>
        </g:each>
        </tbody>
        <tfoot>
        <tr>
            <td></td>
            <td><strong><g:message code="financials.export.sums"/></strong></td>
            <td></td>
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
            <td></td>
        </tr>
        </tfoot>
    </table>

</g:form>



<g:if test="${editable}">
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#subListToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
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
                            transition: 'scale',
                            onApprove : function() {
                                $(this).find('.ui.form').submit();
                                return false;
                            }
                        }).modal('show');
                    })
                });

         $('#selectedCostItemElementID').on('change', function() {
            var selectedCostItemElementID = $(this).val()
            var url = "<g:createLink controller="${controllerName}" action="${actionName}" id="${params.id}"/>?selectedCostItemElementID="+selectedCostItemElementID;
            location.href = url;
         });
    </laser:script>

</g:if>

<laser:htmlEnd/>
