<!-- _costItemInput.gsp -->
<%@ page import="de.laser.finance.BudgetCode; de.laser.finance.CostItem; de.laser.IssueEntitlement; de.laser.IssueEntitlementGroup; de.laser.Subscription; de.laser.SubscriptionPackage; de.laser.UserSetting; de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.*; de.laser.*; org.springframework.context.i18n.LocaleContextHolder; de.laser.interfaces.CalculatedType" %>
<laser:serviceInjection />

    <g:form class="ui small form" name="editCost_${idSuffix}" url="${formUrl}">
        <g:if test="${costItem}">
            <g:hiddenField id="costItemId_${idSuffix}" name="costItemId" value="${costItem.id}"/>
        </g:if>
        <g:if test="${copyCostsFromConsortia}">
            <g:hiddenField id="copyBase_${idSuffix}" name="copyBase" value="${genericOIDService.getOID(costItem)}" />
        </g:if>
        <div class="fields">
            <div class="nine wide field">
                <g:if test="${showVisibilitySettings}">
                    <div class="two fields la-fields-no-margin-button">
                        <div class="field">
                            <label><g:message code="financials.newCosts.costTitle"/></label>
                            <input type="text" name="newCostTitle" value="${costItem?.costTitle}" />
                        </div><!-- .field -->
                        <div class="field">
                            <label><g:message code="financials.isVisibleForSubscriber"/></label>
                            <g:set var="newIsVisibleForSubscriberValue" value="${costItem?.isVisibleForSubscriber ? RDStore.YN_YES.id : RDStore.YN_NO.id}" />
                            <laser:select name="newIsVisibleForSubscriber" id="newIsVisibleForSubscriber_${idSuffix}" class="ui dropdown"
                                      from="${yn}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${newIsVisibleForSubscriberValue}" />
                        </div><!-- .field -->
                    </div>
                </g:if>
                <g:else>
                    <div class="field">
                        <label><g:message code="financials.newCosts.costTitle"/></label>
                        <input type="text" name="newCostTitle" value="${costItem?.costTitle}" />
                    </div><!-- .field -->
                </g:else>
                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label><g:message code="financials.budgetCode"/></label>
                        <select name="newBudgetCodes" class="ui fluid search dropdown" multiple="multiple">
                            <g:each in="${budgetCodes}" var="bc">
                                <g:if test="${costItem?.getBudgetcodes()?.contains(bc)}">
                                    <option selected="selected" value="${bc.class.name}:${bc.id}">${bc.value}</option>
                                </g:if>
                                <g:else>
                                    <option value="${BudgetCode.class.name}:${bc.id}">${bc.value}</option>
                                </g:else>
                            </g:each>
                        </select>
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.referenceCodes"/></label>
                        <input type="text" name="newReference" value="${costItem?.reference}"/>
                    </div><!-- .field -->
                </div>
            </div>
            <div class="seven wide field">
                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label><g:message code="financials.costItemElement"/></label>
                        <g:if test="${costItemElements}">
                            <laser:select name="newCostItemElement" id="newCostItemElement_${idSuffix}" class="ui dropdown"
                                          from="${costItemElements.collect{ ciec -> ciec.costItemElement }}"
                                          optionKey="id"
                                          optionValue="value"
                                          noSelection="${[null:message(code:'default.select.choose.label')]}"
                                          value="${costItem?.costItemElement?.id}" />
                        </g:if>
                        <g:else>
                            ${message(code:'financials.costItemElement.noneDefined')}
                        </g:else>
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.costItemConfiguration"/></label>
                        <laser:select name="ciec" id="ciec_${idSuffix}" class="ui dropdown"
                                      from="${costItemSigns}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${[null:message(code:'default.select.choose.label')]}"
                                      value="${costItem?.costItemElementConfiguration?.id}"/>
                    </div>
                </div>
                <div class="field">
                    <label>${message(code:'default.status.label')}</label>
                    <laser:select name="newCostItemStatus" id="newCostItemStatus_${idSuffix}" title="${g.message(code: 'financials.addNew.costState')}" class="ui dropdown"
                                  from="${costItemStatus}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${[(RDStore.GENERIC_NULL_VALUE.id):message(code:'default.select.choose.label')]}"
                                  value="${costItem?.costItemStatus?.id}" />
                </div><!-- .field -->

            </div> <!-- 2/2 field -->
        </div><!-- two fields -->

        <div class="fields">
            <fieldset class="nine wide field la-modal-fieldset-margin-right la-account-currency">
                <label>${g.message(code:'financials.newCosts.amount')}</label>

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="text" class="calc" style="width:50%"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency_${idSuffix}" placeholder="${g.message(code:'financials.invoice_total')}"
                               value="<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2" maxFractionDigits="2" />"/>

                        <div id="calculateBillingCurrency_${idSuffix}" class="ui icon button la-popup-tooltip la-delay" data-content="${message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>

                        <g:select class="ui dropdown la-small-dropdown la-not-clearable" name="newCostCurrency" id="newCostCurrency_${idSuffix}" title="${message(code: 'financials.addNew.currencyType')}"
                                  from="${currency}"
                                  optionKey="id"
                                  optionValue="${{it.text.contains('-') ? it.text.split('-').first() : it.text}}"
                                  value="${costItem?.billingCurrency?.id}" />
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.newCosts.totalAmount"/></label>
                        <input title="${g.message(code:'financials.newCosts.totalAmount')}" type="text" readonly="readonly"
                               name="newCostInBillingCurrencyAfterTax" id="newCostInBillingCurrencyAfterTax_${idSuffix}"
                               value="<g:formatNumber number="${costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2" maxFractionDigits="2" />" />

                    </div><!-- .field -->
                </div>

                <div class="two fields">
                    <div class="field la-exchange-rate">
                        <label>${g.message(code:'financials.newCosts.exchangeRate')}</label>
                        <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc"
                               name="newCostCurrencyRate" id="newCostCurrencyRate_${idSuffix}"
                               placeholder="${g.message(code:'financials.newCosts.exchangeRate')}"
                               value="${costItem ? costItem.currencyRate : 1.0}" step="0.001" />

                        <div id="calculateExchangeRate_${idSuffix}" class="ui icon button la-popup-tooltip la-delay" data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code:'financials.newCosts.taxTypeAndRate')}</label>
                        <g:select class="ui dropdown calc" name="newTaxRate" id="newTaxRate_${idSuffix}" title="TaxRate"
                              from="${CostItem.TAX_TYPES}"
                              optionKey="${{it.taxType.class.name+":"+it.taxType.id+"§"+it.taxRate}}"
                              optionValue="${{it.display ? it.taxType.getI10n("value")+" ("+it.taxRate+"%)" : it.taxType.getI10n("value")}}"
                              value="${taxKey?.taxType?.class?.name}:${taxKey?.taxType?.id}§${taxKey?.taxRate}"
                              noSelection="${['null§0':'']}"/>

                    </div><!-- .field -->
                </div>

                <div class="two fields">
                    <div class="field">
                        <label><g:message code="financials.newCosts.valueInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/></label><%-- TODO once we may configure local currency as OrgSetting, this arg has to be replaced! --%>
                        <input title="<g:message code="financials.newCosts.valueInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/>" type="text" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency_${idSuffix}"
                               placeholder="${message(code:'financials.newCosts.value')}"
                               value="<g:formatNumber number="${costItem?.costInLocalCurrency}" minFractionDigits="2" maxFractionDigits="2"/>" />

                        <div id="calculateLocalCurrency_${idSuffix}" class="ui icon button la-popup-tooltip la-delay" data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.newCosts.finalSumInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/></label><%-- TODO once we may configure local currency as OrgSetting, this arg has to be replaced! --%>
                        <input title="<g:message code="financials.newCosts.finalSumInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/>" type="text" readonly="readonly"
                               name="newCostInLocalCurrencyAfterTax" id="newCostInLocalCurrencyAfterTax_${idSuffix}"
                               value="<g:formatNumber number="${costItem?.costInLocalCurrencyAfterTax}" minFractionDigits="2" maxFractionDigits="2"/>"/>
                    </div><!-- .field -->
                </div>

                <div class="field">
                    <div class="ui checkbox">
                        <label><g:message code="financials.newCosts.finalSumRounded"/></label>
                        <input name="newFinalCostRounding" id="newFinalCostRounding_${idSuffix}" class="hidden calc" type="checkbox"
                               <g:if test="${costItem?.finalCostRounding}"> checked="checked" </g:if>
                        />
                    </div>
                </div><!-- .field -->
            </fieldset> <!-- 1/2 field |  .la-account-currency -->

            <g:if test="${idSuffix != 'bulk'}">
                <fieldset class="seven wide field la-modal-fieldset-no-margin">
                    <label>${message(code:'financials.newCosts.costsReferenceOn')}</label>

                    <div class="field">
                        <label>${message(code:'default.subscription.label')}</label>

                        <g:if test="${costItem?.sub}">
                            <input class="la-full-width"
                                   readonly='readonly'
                                   value="${costItem.sub.getName()}" />
                            <input name="newSubscription" id="newSubscription_${idSuffix}"
                                   type="hidden"
                                   value="${Subscription.class.name + ':' + costItem.sub.id}" />
                        </g:if>
                        <g:else>
                            <g:if test="${subscription}">
                                <input class="la-full-width"
                                       readonly='readonly'
                                       value="${subscription.getName()}" />
                                <input name="newSubscription" id="newSubscription_${idSuffix}"
                                       type="hidden"
                                       value="${Subscription.class.name + ':' + subscription.id}" />
                            </g:if>
                            <g:else>
                                <div class="ui search selection dropdown newCISelect">
                                    <input type="hidden" name="newSubscription" id="newSubscription_${idSuffix}">
                                    <i class="dropdown icon"></i>
                                    <input type="text" class="search">
                                    <div class="default text">${message(code:'financials.newCosts.newLicence')}</div>
                                </div>
                            </g:else>
                        </g:else>
                    </div><!-- .field -->

                    <div class="field">
                        <g:if test="${validSubChilds}">
                            <label>${licenseeLabel}</label>
                            <g:if test="${contextSub && contextSub.instanceOf()}">
                                <input class="la-full-width" readonly="readonly" value="${contextSub.getSubscriber().sortname}" />
                            </g:if>
                            <g:else>
                                <input type="button" name="toggleLicenseeTarget" id="toggleLicenseeTarget_${idSuffix}" class="ui button la-full-width" value="${message(code:'financials.newCosts.toggleLicenseeTarget')}">
                                <g:select name="newLicenseeTarget" id="newLicenseeTarget_${idSuffix}" class="ui dropdown multiple search"
                                          from="${validSubChilds}"
                                          optionValue="${{it.name ? it.getSubscriber().dropdownNamingConvention(institution) : it.label}}"
                                          optionKey="${{Subscription.class.name + ':' + it.id}}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          value="${Subscription.class.name + ':' + costItem?.sub?.id}"
                                          onchange="JSPC.app.onSubscriptionUpdate()"
                                />
                            </g:else>
                        </g:if>

                    </div><!-- .field -->

                    <div class="newPackageWrapper">
                        <div class="field">
                            <label>${message(code:'financials.newCosts.package')}</label>
                            <g:if test="${costItem?.sub}">
                                <g:select name="newPackage" id="newPackage_${idSuffix}" class="ui dropdown search"
                                          from="${[{}] + costItem?.sub?.packages}"
                                          optionValue="${{it?.pkg?.name ?: message(code:'financials.newCosts.noPackageLink')}}"
                                          optionKey="${{SubscriptionPackage.class.name + ':' + it?.id}}"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          value="${SubscriptionPackage.class.name + ':' + costItem?.subPkg?.id}" />
                            </g:if>
                            <g:else>
                            <%--<input name="newPackage" class="ui" disabled="disabled" data-subFilter="" data-disableReset="true" />--%>
                                <div class="ui search selection dropdown newCISelect">
                                    <input type="hidden" name="newPackage" id="newPackage_${idSuffix}" value="${costItem?.subPkg ? "${SubscriptionPackage.class.name}:${costItem.subPkg.id}" : params.newPackage}">
                                    <i class="dropdown icon"></i>
                                    <input type="text" class="search">
                                    <div class="default text"></div>
                                </div>
                            </g:else>
                        </div>
                        <div class="field">
                            <%-- the distinction between subMode (= sub) and general view is done already in the controller! --%>
                            <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                            <div class="ui search selection dropdown newCISelect">
                                <input type="hidden" name="newIE" id="newIE_${idSuffix}" value="${costItem?.issueEntitlement ? "${IssueEntitlement.class.name}:${costItem.issueEntitlement.id}" : params.newIE}">
                                <i class="dropdown icon"></i>
                                <input type="text" class="search">
                                <div class="default text"></div>
                            </div>
                        </div>

                        <div class="field">
                            <label>${message(code:'financials.newCosts.titleGroup')}</label>
                            <div class="ui search selection dropdown newCISelect">
                                <input type="hidden" name="newTitleGroup" id="newTitleGroup_${idSuffix}" value="${costItem?.issueEntitlementGroup ? "${IssueEntitlementGroup.class.name}:${costItem.issueEntitlementGroup.id}" : params.newTitleGroup}">
                                <i class="dropdown icon"></i>
                                <input type="text" class="search">
                                <div class="default text"></div>
                            </div>
                        </div>

                    </div><!-- .field -->
                </fieldset> <!-- 2/2 field -->
            </g:if>


        </div><!-- three fields -->

        <div class="three fields">
            <fieldset class="field la-modal-fieldset-no-margin">
                <div class="two fields">
                    <semui:datepicker label="financials.datePaid" name="newDatePaid" id="newDatePaid_${idSuffix}" placeholder="financials.datePaid" value="${costItem?.datePaid}" />

                    <%-- to restrict upon year: https://jsbin.com/ruqakehefa/1/edit?html,js,output , cf. example 8! --%>
                    <semui:datepicker label="financials.financialYear" name="newFinancialYear" id="newFinancialYear_${idSuffix}" placeholder="financials.financialYear" value="${costItem?.financialYear}" />
                </div>
                <div class="two fields">
                    <semui:datepicker label="financials.dateFrom" name="newStartDate" id="newStartDate_${idSuffix}" placeholder="default.date.label" value="${costItem?.startDate}" />

                    <semui:datepicker label="financials.dateTo" name="newEndDate" id="newEndDate_${idSuffix}" placeholder="default.date.label" value="${costItem?.endDate}" />
                </div>
            </fieldset> <!-- 1/3 field -->

            <fieldset class="field la-modal-fieldset-margin">
                <div class="field">
                    <semui:datepicker label="financials.invoiceDate" name="newInvoiceDate" id="newInvoiceDate_${idSuffix}" placeholder="financials.invoiceDate" value="${costItem?.invoiceDate}" />

                    <label>${message(code:'financials.newCosts.description')}</label>
                    <input type="text" name="newDescription" placeholder="${message(code:'default.description.label')}" value="${costItem?.costDescription}"/>
                </div><!-- .field -->
            </fieldset> <!-- 2/3 field -->

            <fieldset class="field la-modal-fieldset-no-margin">
                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <input type="text" name="newInvoiceNumber" placeholder="${message(code:'financials.invoice_number')}" value="${costItem?.invoice?.invoiceNumber}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <input type="text" name="newOrderNumber" placeholder="${message(code:'financials.order_number')}" value="${costItem?.order?.orderNumber}"/>
                </div><!-- .field -->
            </fieldset> <!-- 3/3 field -->

        </div><!-- three fields -->

    </g:form>

<script>
    <g:render template="/templates/javascript/jspc.finance.js" />
</script>