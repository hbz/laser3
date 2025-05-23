<!-- _costItemInput.gsp -->
<%@ page import="de.laser.wekb.Package; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.finance.BudgetCode; de.laser.finance.CostInformationDefinition; de.laser.finance.CostItem; de.laser.IssueEntitlement; de.laser.IssueEntitlementGroup; de.laser.Subscription; de.laser.SubscriptionPackage; de.laser.UserSetting; de.laser.storage.RDStore; de.laser.storage.RDConstants; de.laser.*; de.laser.interfaces.CalculatedType; de.laser.finance.CostItemElementConfiguration" %>
<laser:serviceInjection />

        <g:if test="${costItem}">
            <g:hiddenField id="costItemId_${idSuffix}" name="costItemId" value="${costItem.id}"/>
        </g:if>
        <g:if test="${copyCostsFromConsortia}">
            <g:hiddenField id="copyBase_${idSuffix}" name="copyBase" value="${genericOIDService.getOID(costItem)}" />
        </g:if>
        <g:if test="${subscription}">
            <g:hiddenField id="sub_${idSuffix}" name="sub" value="${subscription.id}"/>
        </g:if>

        <div class="fields la-forms-grid">
            <div class="eight la-modal-fieldset-no-margin  wide field  la-forms-grid">
                <g:if test="${showVisibilitySettings}">
                    <div class="two fields">
                        <div class="field">
                            <label><g:message code="financials.newCosts.costTitle"/></label>
                            <input type="text" name="newCostTitle" value="${costItem?.costTitle}" maxlength="255"/>
                        </div><!-- .field -->
                        <div class="field">
                            <label><g:message code="financials.isVisibleForSubscriber"/></label>
                            <g:set var="newIsVisibleForSubscriberValue" value="${costItem?.isVisibleForSubscriber ? RDStore.YN_YES.id : RDStore.YN_NO.id}" />
                            <g:if test="${idSuffix == 'bulk'}">
                                <ui:select name="newIsVisibleForSubscriber" id="newIsVisibleForSubscriber_${idSuffix}" class="ui dropdown clearable"
                                              from="${yn}"
                                              optionKey="id"
                                              optionValue="value"
                                              noSelection="${[null:message(code:'default.select.choose.label')]}"
                                              value="" />
                            </g:if>
                            <g:else>
                                <ui:select name="newIsVisibleForSubscriber" id="newIsVisibleForSubscriber_${idSuffix}" class="ui dropdown clearable"
                                              from="${yn}"
                                              optionKey="id"
                                              optionValue="value"
                                              value="${newIsVisibleForSubscriberValue}" />
                            </g:else>

                        </div><!-- .field -->
                    </div>
                </g:if>
                <g:else>
                    <div class="field">
                        <label><g:message code="financials.newCosts.costTitle"/></label>
                        <input type="text" name="newCostTitle" value="${costItem?.costTitle}" maxlength="255"/>
                    </div><!-- .field -->
                </g:else>
                <div class="two fields">
                    <div class="field">
                        <label><g:message code="financials.budgetCode"/></label>
                        <select name="newBudgetCodes" class="ui fluid search dropdown multiple" multiple="multiple">
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
            <div class="eight la-modal-fieldset-no-margin wide field la-forms-grid">
                <div class="two fields">
                    <div class="field">
                        <label><g:message code="financials.costItemElement"/></label>
                        <g:if test="${costItemElements}">
                            <ui:select name="newCostItemElement" id="newCostItemElement_${idSuffix}" class="ui dropdown clearable"
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
                        <ui:select name="ciec" id="ciec_${idSuffix}" class="ui dropdown clearable"
                                      from="${costItemSigns}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${[null:message(code:'default.select.choose.label')]}"
                                      value="${costItem?.costItemElementConfiguration?.id}"/>
                    </div>
                </div>
                <div class="field">
                    <label>${message(code:'default.status.label')}</label>
                    <ui:select name="newCostItemStatus" id="newCostItemStatus_${idSuffix}" title="${g.message(code: 'financials.addNew.costState')}" class="ui dropdown clearable"
                                  from="${costItemStatus}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${[(RDStore.GENERIC_NULL_VALUE.id):message(code:'default.select.choose.label')]}"
                                  value="${costItem?.costItemStatus?.id}" />
                </div><!-- .field -->

            </div> <!-- 2/2 field -->
        </div><!-- two fields -->

        <div class="fields">
            <fieldset class="<g:if test="${idSuffix != 'bulk' && !(mode == 'copy' && copyToOtherSub)}"> nine la-modal-fieldset-margin-right </g:if> <g:else> sixteen </g:else> wide field  la-account-currency la-forms-grid">
                <label>${g.message(code:'financials.newCosts.amount')}</label>

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="text" class="calc" style="width:50%"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency_${idSuffix}" placeholder="${g.message(code:'financials.invoice_total')}"
                               value="<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2" maxFractionDigits="2" />"/>

                        <div id="calculateBillingCurrency_${idSuffix}" class="${Btn.ICON.SIMPLE} la-long-tooltip calcButton" data-tooltip="${message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
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
                        <%
                            String value
                            if(idSuffix != 'bulk') {
                                if(costItem) {
                                    if(contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE,null).value == 'de')
                                        value = costItem.currencyRate.toString().replace('.', ',')
                                    else value = costItem.currencyRate.toString()
                                }
                                else {
                                    if(contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE,null).value == 'de')
                                        value = '1,0'
                                    else value = '1.0'
                                }
                            }
                        %>
                        <input title="${g.message(code:'financials.addNew.currencyRate')}" type="text" class="calc la-82Percent"
                               name="newCostCurrencyRate" id="newCostCurrencyRate_${idSuffix}"
                               placeholder="${g.message(code:'financials.newCosts.exchangeRate')}"
                               value="${value}" />

                        <div  id="calculateExchangeRate_${idSuffix}" class="${Btn.ICON.SIMPLE} la-long-tooltip calcButton" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code:'financials.newCosts.taxTypeAndRate')}</label>
                        <g:select class="ui dropdown clearable calc" name="newTaxRate" id="newTaxRate_${idSuffix}" title="TaxRate"
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
                        <input title="<g:message code="financials.newCosts.valueInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/>" type="text" class="calc la-82Percent"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency_${idSuffix}"
                               placeholder="${message(code:'financials.newCosts.value')}"
                               value="<g:formatNumber number="${costItem?.costInLocalCurrency}" minFractionDigits="2" maxFractionDigits="2"/>" />

                        <div id="calculateLocalCurrency_${idSuffix}" class="${Btn.ICON.SIMPLE} la-long-tooltip calcButton" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
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

                <div class="two fields">
                    <div class="field">
                        <div class="ui checkbox">
                            <label><g:message code="financials.newCosts.roundBillingSum"/></label>
                            <input name="newBillingSumRounding" id="newBillingSumRounding_${idSuffix}" class="hidden calc" type="checkbox"
                                <g:if test="${costItem?.billingSumRounding}"> checked="checked" </g:if>
                            />
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <div class="ui checkbox">
                            <label><g:message code="financials.newCosts.roundFinalSum"/></label>
                            <input name="newFinalCostRounding" id="newFinalCostRounding_${idSuffix}" class="hidden calc" type="checkbox"
                                <g:if test="${costItem?.finalCostRounding}"> checked="checked" </g:if>
                            />
                        </div>
                    </div><!-- .field -->
                </div>
            </fieldset> <!-- 1/2 field |  .la-account-currency -->


            <g:if test="${idSuffix != 'bulk' && !(mode == 'copy' && copyToOtherSub)}">
                <fieldset class="seven wide field la-modal-fieldset-no-margin la-forms-grid">
                    <label>${message(code:'financials.newCosts.costsReferenceOn')}</label>

                    <div class="field">
                        <label>${message(code:'default.subscription.label')}</label>

                        <g:if test="${costItem?.sub}">
                            <input class="la-full-width"
                                   readonly='readonly'
                                   value="${costItem.sub.getName()}" />
                            <input name="newSubscription" id="newSubscription_${idSuffix}"
                                   type="hidden"
                                   value="${costItem.sub.id}" />
                        </g:if>
                        <g:else>
                            <g:if test="${subscription}">
                                <input class="la-full-width"
                                       readonly='readonly'
                                       value="${subscription.getName()}" />
                                <input name="newSubscription" id="newSubscription_${idSuffix}"
                                       type="hidden"
                                       value="${subscription.id}" />
                            </g:if>
                            <g:else>
                                <div class="ui search selection dropdown newCISelect" id="newSubscription_${idSuffix}">
                                    <input type="hidden" name="newSubscription">
                                    <i class="dropdown icon"></i>
                                    <input type="text" class="search">
                                    <div class="default text">${message(code:'default.subscription.label')}</div>
                                </div>
                            </g:else>
                        </g:else>
                    </div><!-- .field -->

                    <div class="field">
                        <g:if test="${validSubChilds}">
                            <label>${licenseeLabel}</label>
                            <g:if test="${(mode != 'copy') && costItem && costItem.sub && costItem.sub.instanceOf}">
                                <input class="la-full-width" readonly="readonly" value="${costItem.sub.getSubscriberRespConsortia().sortname}" />
                            </g:if>
                            <g:elseif test="${costItem?.sub == subscription && subscription._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL}">
                                <input type="button" name="toggleLicenseeTarget" id="toggleLicenseeTarget_${idSuffix}" class="${Btn.SIMPLE} la-full-width" value="${message(code:'financials.newCosts.toggleLicenseeTarget')}">
                                <g:select name="newLicenseeTarget" id="newLicenseeTarget_${idSuffix}" class="ui dropdown clearable multiple search"
                                          from="${validSubChilds}" multiple="multiple"
                                          optionValue="${{it.name ? it.getSubscriberRespConsortia().dropdownNamingConvention(institution) : it.label}}"
                                          optionKey="id"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          value="forParent"
                                />
                            </g:elseif>
                            <g:else>
                                <input type="button" name="toggleLicenseeTarget" id="toggleLicenseeTarget_${idSuffix}" class="${Btn.SIMPLE} la-full-width" value="${message(code:'financials.newCosts.toggleLicenseeTarget')}">
                                <g:select name="newLicenseeTarget" id="newLicenseeTarget_${idSuffix}" class="ui dropdown clearable multiple search"
                                          from="${validSubChilds}" multiple="multiple"
                                          optionValue="${{it.name ? it.getSubscriberRespConsortia().dropdownNamingConvention(institution) : it.label}}"
                                          optionKey="id"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          value="${costItem?.sub?.id}"
                                />
                            </g:else>
                        </g:if>

                    </div><!-- .field -->

                    <div class="newPackageWrapper">
                        <div class="field">
                            <label>${message(code:'financials.newCosts.package')}</label>
                            <g:if test="${costItem?.sub}">
                                <g:select name="newPackage" id="newPackage_${idSuffix}" class="ui dropdown clearable search"
                                          from="${costItem?.sub?.packages?.pkg}"
                                          optionValue="${{it?.name ?: message(code:'financials.newCosts.noPackageLink')}}"
                                          optionKey="id"
                                          noSelection="${['' : message(code:'default.select.choose.label')]}"
                                          value="${costItem?.pkg?.id}" />
                            </g:if>
                            <g:else>
                            <%--<input name="newPackage" class="ui" disabled="disabled" data-subFilter="" data-disableReset="true" />--%>
                                <div class="ui search selection dropdown newCISelect" id="newPackage_${idSuffix}">
                                    <input type="hidden" name="newPackage" value="${costItem?.pkg ? "${costItem.pkg.id}" : params.newPackage}">
                                    <i class="dropdown icon"></i>
                                    <input type="text" class="search">
                                    <div class="default text"></div>
                                </div>
                            </g:else>
                        </div>
                        <div class="field">
                            <%-- the distinction between subMode (= sub) and general view is done already in the controller! --%>
                            <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                            <div class="ui search selection dropdown newCISelect" id="newIE_${idSuffix}">
                                <input type="hidden" name="newIE" value="${costItem?.issueEntitlement ? "${costItem.issueEntitlement.id}" : params.newIE}">
                                <i class="dropdown icon"></i>
                                <input type="text" class="search">
                                <div class="default text"></div>
                            </div>
                        </div>

                        <div class="field">
                            <label>${message(code:'financials.newCosts.titleGroup')}</label>
                            <div class="ui search selection dropdown newCISelect" id="newTitleGroup_${idSuffix}" >
                                <input type="hidden" name="newTitleGroup" value="${costItem?.issueEntitlementGroup ? "${costItem.issueEntitlementGroup.id}" : params.newTitleGroup}">
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
            <fieldset class="field la-modal-fieldset-no-margin la-forms-grid">
                <div class="two fields">
                    <ui:datepicker label="financials.datePaid" name="newDatePaid" id="newDatePaid_${idSuffix}" placeholder="financials.datePaid" value="${costItem?.datePaid}" />

                    <%-- to restrict upon year: https://jsbin.com/ruqakehefa/1/edit?html,js,output , cf. example 8! --%>
                    <ui:datepicker type="year" label="financials.financialYear" name="newFinancialYear" id="newFinancialYear_${idSuffix}" placeholder="financials.financialYear" value="${costItem?.financialYear}" />
                </div>
                <div class="two fields">
                    <ui:datepicker label="financials.dateFrom" name="newStartDate" id="newStartDate_${idSuffix}" placeholder="default.date.label" value="${costItem?.startDate}" />

                    <ui:datepicker label="financials.dateTo" name="newEndDate" id="newEndDate_${idSuffix}" placeholder="default.date.label" value="${costItem?.endDate}" />
                </div>
            </fieldset> <!-- 1/3 field -->

            <fieldset class="field la-modal-fieldset-margin la-forms-grid">
                <div class="field">
                    <ui:datepicker label="financials.invoiceDate" name="newInvoiceDate" id="newInvoiceDate_${idSuffix}" placeholder="financials.invoiceDate" value="${costItem?.invoiceDate}" />
                </div>
                <div class="field">
                    <label>${message(code:'default.description.label')}</label>
                    <input type="text" name="newDescription" placeholder="${message(code:'default.description.label')}" value="${costItem?.costDescription}"/>
                </div><!-- .field -->
            </fieldset> <!-- 2/3 field -->

            <fieldset class="field la-modal-fieldset-no-margin la-forms-grid">
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

        <div class="two fields">
            <fieldset class="field la-modal-fieldset-no-margin la-forms-grid">
                <div class="field">
                    <label>${g.message(code: 'financials.costInformationDefinition')}</label>
                    <ui:dropdown name="newCostInformationDefinition"
                                 class="newCostInformationDefinition_${idSuffix} clearable"
                                 from="${costInformationDefinitions}"
                                 iconWhich="${Icon.PROP.IS_PRIVATE}"
                                 optionKey="${{it.id}}"
                                 optionValue="${{ it.getI10n('name') }}"
                                 noSelection="${message(code: 'default.select.choose.label')}"/>
                </div><!-- .field -->
            </fieldset>
            <fieldset class="field la-modal-fieldset-no-margin la-forms-grid">
                <div class="field">
                    <label></label>
                    <input type="text" name="newCostInformationStringValue" id="newCostInformationValueText_${idSuffix}" hidden="hidden">
                    <select id="newCostInformationValueDropdown_${idSuffix}" name="newCostInformationRefValue" class="ui search selection fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>
                    </select>
                </div>
            </fieldset>
        </div><!-- two fields -->

        <g:if test="${mode == 'copy' && copyToOtherSub}">
        <div class="fields">
            <fieldset class="sixteen wide field la-modal-fieldset-margin-right">
                <label>${g.message(code: 'financials.copyCostItem.toOtherSub')}</label>

                <div class="ui field">
                    <div class="field">
                        <label>${message(code: 'filter.status')}</label>
                        <select id="status" name="status" multiple="" class="ui search selection fluid multiple dropdown" onchange="JSPC.app.adjustDropdown()">
                            <option value=""><g:message code="default.select.choose.label"/></option>
                            <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }" var="status">
                                <option <%=(status.id in params.list('status')) ? 'selected="selected"' : ''%> value="${status.id}">${status.getI10n('value')}</option>
                            </g:each>
                        </select>

                    </div>
                    <g:if test="${contextService.getOrg().isCustomerType_Consortium()}">
                        <div class="ui checkbox">
                            <g:checkBox name="show.subscriber" value="true" checked="true"
                                        onchange="JSPC.app.adjustDropdown()"/>
                            <label for="show.subscriber">${message(code: 'default.compare.show.subscriber.name')}</label>
                        </div><br />
                    </g:if>
                   <div class="ui checkbox">
                        <g:checkBox name="show.connectedObjects" value="true" checked="false"
                                    onchange="JSPC.app.adjustDropdown()"/>
                        <label for="show.connectedObjects">${message(code: 'default.compare.show.connectedObjects.name')}</label>
                    </div>
                    <br />
                    <select id="selectedSubs" name="selectedSubs" multiple="" class="ui search selection fluid dropdown">
                        <option value="">${message(code: 'default.select.choose.label')}</option>
                    </select>
                </div>

            </fieldset> <!-- 1/2 field |  .la-account-currency -->
        </div>
        </g:if>

<laser:script file="${this.getGroovyPageFileName()}">
    <%
        String contextSub = ""
        if(costItem && costItem.sub)
            contextSub = costItem.sub.id
        else if(subscription)
            contextSub = subscription.id
    %>
    JSPC.app.ajaxDropdown = function(selector, url, valuesString) {
        let values = [];
        if(valuesString.includes(',')) {
            values = valuesString.split(',');
        }
        else if(valuesString.length > 0) {
            values.push(valuesString);
        }
        selector.dropdown({
            apiSettings: {
                url: url,
                cache: false
            },
            clearable: true,
            minCharacters: 0
        });
        if(values.length > 0) {
            selector.dropdown('queryRemote', '', () => {
                selector.dropdown('set selected', values);
            });
        }
    }
    JSPC.app.finance${idSuffix} = {
        userLang: "${contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE,null)}",
        currentForm: $("#editCost_${idSuffix}"),
        newSubscription: $("#newSubscription_${idSuffix}"),
        newPackage: $("#newPackage_${idSuffix}"),
        newIE: $("#newIE_${idSuffix}"),
        newTitleGroup: $("#newTitleGroup_${idSuffix}"),
        toggleLicenseeTarget: $("#toggleLicenseeTarget_${idSuffix}"),
        newLicenseeTarget: $("#newLicenseeTarget_${idSuffix}"),
        newLicenseeDiv: $("#newLicenseeTarget_${idSuffix}").parent('div'),
        costBillingCurrency: $("#newCostInBillingCurrency_${idSuffix}"),
        costBillingCurrencyAfterTax: $("#newCostInBillingCurrencyAfterTax_${idSuffix}"),
        calculateBillingCurrency: $("#calculateBillingCurrency_${idSuffix}"),
        costCurrencyRate: $("#newCostCurrencyRate_${idSuffix}"),
        calculateCurrencyRate: $("#calculateExchangeRate_${idSuffix}"),
        costLocalCurrency: $("#newCostInLocalCurrency_${idSuffix}"),
        costLocalCurrencyAfterTax: $("#newCostInLocalCurrencyAfterTax_${idSuffix}"),
        calculateLocalCurrency: $("#calculateLocalCurrency_${idSuffix}"),
        costCurrency: $("#newCostCurrency_${idSuffix}"),
        costItemElement: $("#newCostItemElement_${idSuffix}"),
        costInformationDefinition: $(".newCostInformationDefinition_${idSuffix}"),
        costInformationValueDropdown: $("#newCostInformationValueDropdown_${idSuffix}"),
        costInformationValueText: $("#newCostInformationValueText_${idSuffix}"),
        billingSumRounding: $("#newBillingSumRounding_${idSuffix}"),
        finalCostRounding: $("#newFinalCostRounding_${idSuffix}"),
        taxRate: $("#newTaxRate_${idSuffix}"),
        ciec: $("#ciec_${idSuffix}"),
        costElems: $("#newCostInLocalCurrency_${idSuffix}, #newCostCurrencyRate_${idSuffix}, #newCostInBillingCurrency_${idSuffix}"),
        calc: $(".calc"),
        newSubscription: $("#newSubscription_${idSuffix}"),
        isVisibleForSubscriber: $("#newIsVisibleForSubscriber_${idSuffix}"),
        elementChangeable: false,
        refdataDefs: [
        <%
            Set<Long> refdataDefs = CostInformationDefinition.executeQuery("select cif.id from CostInformationDefinition cif where cif.type = '"+RefdataValue.class.name+"' and (cif.tenant = null or cif.tenant = :ctx)", [ctx: contextService.getOrg()])
            refdataDefs.eachWithIndex { Long cifId, int i ->
                String tmp = cifId
                if(i < refdataDefs.size() - 1)
                    tmp += ','
                print tmp
            }
        %>
        ],
        costItemElementConfigurations: {
        <%
            costItemElements.eachWithIndex { CostItemElementConfiguration ciec, int i ->
                String tmp = "${ciec.costItemElement.id}: ${ciec.elementSign.id}"
                if(i < costItemElements.size() - 1)
                    tmp += ','
                println tmp
            }
        %>
        },
        selLinks: {
            newSubscription_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupSubscriptions"])}?query={query}",
        <g:if test="${costItem?.sub || subscription}">
            newPackage_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}&ctx=${contextSub}",
            newIE_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub=${contextSub}",
            newTitleGroup_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}&sub=${contextSub}"
        </g:if>
        <g:else>
            newPackage_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}",
            newIE_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}",
            newTitleGroup_${idSuffix}: "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}"
        </g:else>
        },
        eurVal: "${RDStore.CURRENCY_EUR.id}",
        isError: function(elem)  {
            if (elem.val().length <= 0 || elem.val() < 0) {
                $(".la-account-currency").children(".field").removeClass("error");
                elem.parent(".field").addClass("error");
                return true
            }
            return false
        },
        updateTitleDropdowns: function() {
            $(".newCISelect").each(function(k,v){
                //console.log(JSPC.app.finance${idSuffix}.selLinks[$(this).attr("id")]);
                JSPC.app.ajaxDropdown($(this), JSPC.app.finance${idSuffix}.selLinks[$(this).attr("id")], '')
            });
        <% if(costItem?.issueEntitlement) {
            String ieTitleName = costItem.issueEntitlement.tipp.name
            String ieTitleTypeString = costItem.issueEntitlement.tipp.titleType %>
        JSPC.app.finance${idSuffix}.newIE.dropdown('set text',"${ieTitleName} (${ieTitleTypeString}) (${costItem.sub.dropdownNamingConvention()})");
        <%  }
        if(costItem?.issueEntitlementGroup) {
            String issueEntitlementGroupName = costItem.issueEntitlementGroup.name %>
        JSPC.app.finance${idSuffix}.newTitleGroup.dropdown('set text',"${issueEntitlementGroupName} (${costItem.sub.dropdownNamingConvention()})");
        <%  }  %>
        },
        collect: function (fields) {
            let values = [];
            for(let i = 0;i < fields.length;i++) {
                let value = fields[i];
                values.push(value.getAttribute("data-value"));
            }
            //console.log(values);
            return values;
        },
        preselectMembers: function () {
        <g:if test="${pickedSubscriptions}">
            let licenseeTargets = [];
            <g:each in="${pickedSubscriptions}" var="pickedSub">
                licenseeTargets.push(${pickedSub});
            </g:each>
            JSPC.app.finance${idSuffix}.newLicenseeTarget.dropdown("set selected",licenseeTargets);
            <g:if test="${pickedSubscriptions.size() > 9}">
                JSPC.app.finance${idSuffix}.newLicenseeTarget.parent('div').toggle();
            </g:if>
        </g:if>
        },
        onSubscriptionUpdate: function () {
            let context;
            selectedMembers = $("[name='newLicenseeTarget']").val();
            console.log(selectedMembers);
            if(selectedMembers.length === 1){
                let values = selectedMembers;
                if(!values[0].match(/:null|:for/)) {
                     context = values[0];
                }
                else context = "${contextSub}";
            }
            else if(JSPC.app.finance${idSuffix}.newLicenseeTarget.length === 0)
                context = JSPC.app.finance${idSuffix}.newSubscription.dropdown('get value');
            JSPC.app.finance${idSuffix}.selLinks.newIE_${idSuffix} = "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="+context;
            JSPC.app.finance${idSuffix}.selLinks.newTitleGroup_${idSuffix} = "${createLink([controller:"ajaxJson", action:"lookupTitleGroups"])}?query={query}&sub="+context;
            JSPC.app.finance${idSuffix}.selLinks.newPackage_${idSuffix} = "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}&ctx="+context;
            JSPC.app.finance${idSuffix}.newIE.dropdown('clear');
            JSPC.app.finance${idSuffix}.newTitleGroup.dropdown('clear');
            JSPC.app.finance${idSuffix}.newPackage.dropdown('clear');
            JSPC.app.finance${idSuffix}.updateTitleDropdowns();
        },
        checkPackageBelongings: function () {
            var subscription = $("#newSubscription_${idSuffix}, #pickedSubscription_${idSuffix}").val();
            let selectedMembers = $("[name='newLicenseeTarget']~a");
            let values = JSPC.app.finance${idSuffix}.collect(selectedMembers);
            if(values.length === 1 && !values[0].match(/:null|:for/)) {
                subscription = values[0];
                $.ajax({
                    url: "<g:createLink controller="ajaxJson" action="checkCascade"/>?subscription="+subscription+"&package="+JSPC.app.finance${idSuffix}.newPackage.val()+"&issueEntitlement="+JSPC.app.finance${idSuffix}.newIE.val(),
                }).done(function (response) {
                    //console.log("function ran through w/o errors, please continue implementing! Response from server is: "+JSON.stringify(response))
                    if(!response.sub)
                        JSPC.app.finance${idSuffix}.newSubscription.addClass("error");
                    else
                        JSPC.app.finance${idSuffix}.newSubscription.removeClass("error");
                    if(!response.pkg)
                        JSPC.app.finance${idSuffix}.newPackage.addClass("error");
                    else
                        JSPC.app.finance${idSuffix}.newPackage.removeClass("error");
                    if(!response.ie)
                        JSPC.app.finance${idSuffix}.newIE.addClass("error");
                    else
                        JSPC.app.finance${idSuffix}.newIE.removeClass("error");
                }).fail(function () {
                    console.log("AJAX error! Please check logs!");
                });
            }
        },
        checkValues: function () {
            if(JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val()) > 0) {
                if ( (JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val()) * JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val(), true)).toFixed(2) !== JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costLocalCurrency.val()).toFixed(2) ) {
                    //console.log("inserted values are: "+JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val())+" * "+JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val(), true)+" = "+JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costLocalCurrency.val()).toFixed(2)+", correct would be: "+(JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val()) * JSPC.app.finance${idSuffix}.costCurrencyRate.val(), true).toFixed(2));
                    JSPC.app.finance${idSuffix}.costElems.parent('.field').addClass('error');
                    return false;
                }
                else {
                    JSPC.app.finance${idSuffix}.costElems.parent('.field').removeClass('error');
                    return true;
                }
            }
            else {
                JSPC.app.finance${idSuffix}.costElems.parent('.field').removeClass('error');
                return true;
            }
        },
        calcTaxResults: function (localHandInput) {
            let roundB = JSPC.app.finance${idSuffix}.billingSumRounding.prop('checked');
            let roundF = JSPC.app.finance${idSuffix}.finalCostRounding.prop('checked');
            //console.log(taxRate.val());
            let taxF = 1.0 + (0.01 * JSPC.app.finance${idSuffix}.taxRate.val().split("§")[1]);
            let parsedBillingCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val().trim());
            let parsedCurrencyRate = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val().trim(), true); //calcualted but NOT set yet!
            let parsedLocalCurrency = parsedBillingCurrency * parsedCurrencyRate; //calcualted but NOT set yet!
            if(localHandInput === true) {
                //manipulate iff localCurrency has been changed by user
                parsedLocalCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costLocalCurrency.val().trim());
                if(parsedLocalCurrency != 0 && parsedBillingCurrency != 0)
                    JSPC.app.finance${idSuffix}.costCurrencyRate.val(JSPC.app.finance${idSuffix}.doubleToString((parsedLocalCurrency / parsedBillingCurrency), true));
            }
            let billingCurrencyAfterRounding = roundB ? Math.round(parsedBillingCurrency) : parsedBillingCurrency
            let localCurrencyAfterRounding = roundB ? Math.round(parsedLocalCurrency) : parsedLocalCurrency
            JSPC.app.finance${idSuffix}.costBillingCurrency.val(JSPC.app.finance${idSuffix}.doubleToString(billingCurrencyAfterRounding));
            if(parsedCurrencyRate > 0) {
                JSPC.app.finance${idSuffix}.costLocalCurrency.val(JSPC.app.finance${idSuffix}.doubleToString(localCurrencyAfterRounding));
            }
            let billingAfterTax = roundF ? Math.round(billingCurrencyAfterRounding * taxF) : billingCurrencyAfterRounding * taxF;
            JSPC.app.finance${idSuffix}.costBillingCurrencyAfterTax.val(
                 JSPC.app.finance${idSuffix}.doubleToString(billingAfterTax)
            );
            if(parsedCurrencyRate > 0) {
                let localAfterTax = roundF ? Math.round(localCurrencyAfterRounding * taxF ) : localCurrencyAfterRounding * taxF;
                JSPC.app.finance${idSuffix}.costLocalCurrencyAfterTax.val(
                    JSPC.app.finance${idSuffix}.doubleToString(localAfterTax)
                );
            }
        },
        stringToDouble: function (input, currencyRate = false) {
            let output = 0.0;
            //determine locale from server
            if(JSPC.app.finance${idSuffix}.userLang === 'en') {
                if(currencyRate) {
                    output = parseFloat(input.replaceAll(/,/g,"."));
                }
                else {
                    output = parseFloat(input.replaceAll(/,/g,""));
                }
            }
            else {
                if(currencyRate) {
                    output = parseFloat(input.replaceAll(/,/g,"."));
                }
                else {
                    output = parseFloat(input.replaceAll(/[.']/g,"").replaceAll(",","."));
                    //else console.log("Please check over regex!");
                }
            }
            return output;
        },
        doubleToString: function (input, currencyRate = false) {
            let rndInput = currencyRate ? input : input.toFixed(2)
            if(currencyRate)
                console.log(input);
            if(!isNaN(input)) {
                let output;
                if(JSPC.app.finance${idSuffix}.userLang !== 'en')
                    output = rndInput.toString().replace(".",",");
                else output = rndInput.toString();
                return output;
            }
        },
        init: function(elem) {
            //console.log(this);
            this.newSubscription.change(function(){
                JSPC.app.finance${idSuffix}.onSubscriptionUpdate();
            });
            this.newLicenseeTarget.change(function(){
                JSPC.app.finance${idSuffix}.onSubscriptionUpdate();
            });
            this.toggleLicenseeTarget.click( function() {
                JSPC.app.finance${idSuffix}.newLicenseeTarget.parent('div').toggle();
            });
            this.costInformationDefinition.change( function() {
                let numberVal = parseInt($(this).dropdown('get value'));
                let withRefdata = JSPC.app.finance${idSuffix}.refdataDefs.indexOf(numberVal) > -1;
                JSPC.app.finance${idSuffix}.costInformationValueDropdown.dropdown('clear');
                JSPC.app.adjustCostInformationDropdown(withRefdata, <g:if test="${costItem?.costInformationRefValue}">${costItem.costInformationRefValue.id}</g:if><g:else>false</g:else>);
                if(withRefdata) {
                    JSPC.app.finance${idSuffix}.costInformationValueDropdown.parent('div').show();
                    JSPC.app.finance${idSuffix}.costInformationValueText.hide();
                }
                else {
                    JSPC.app.finance${idSuffix}.costInformationValueDropdown.parent('div').hide();
                    JSPC.app.finance${idSuffix}.costInformationValueText.show();
                }
            });
            this.newPackage.change(function(){
                let context;
                let selectedMembers = $("[name='newLicenseeTarget']~a");
                if(selectedMembers.length === 1) {
                    let values = JSPC.app.finance${idSuffix}.collect(selectedMembers);
                    if(!values[0].match(/:null|:for/)) {
                        context = values[0];
                    }
                    else context = "${contextSub}";
                }
                else if(selectedMembers.length === 0)
                    context = JSPC.app.finance${idSuffix}.newSubscription.val();
                JSPC.app.finance${idSuffix}.selLinks.newIE = "${createLink([controller:"ajaxJson", action:"lookupIssueEntitlements"])}?query={query}&sub="+context+"&pkg="+JSPC.app.finance${idSuffix}.newPackage.val();
                JSPC.app.finance${idSuffix}.newIE.dropdown('clear');
                JSPC.app.finance${idSuffix}.updateTitleDropdowns();
            });
            this.newIE.change(function(){
                JSPC.app.finance${idSuffix}.checkPackageBelongings();
            });
            this.newTitleGroup.change(function(){
                JSPC.app.finance${idSuffix}.updateTitleDropdowns();
            });
            this.calculateBillingCurrency.click( function() {
                if (! JSPC.app.finance${idSuffix}.isError(JSPC.app.finance${idSuffix}.costLocalCurrency) && ! JSPC.app.finance${idSuffix}.isError(JSPC.app.finance${idSuffix}.costCurrencyRate)) {
                    let parsedLocalCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costLocalCurrency.val().trim());
                    let parsedCurrencyRate = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val().trim(), true);
                    if (parsedCurrencyRate > 0) {
                        JSPC.app.finance${idSuffix}.costBillingCurrency.val(JSPC.app.finance${idSuffix}.doubleToString((parsedLocalCurrency / parsedCurrencyRate)));
                    }
                    $(".la-account-currency").find(".field").removeClass("error");
                    JSPC.app.finance${idSuffix}.calcTaxResults();
                }
            });
            this.calculateCurrencyRate.click( function() {
                if (! JSPC.app.finance${idSuffix}.isError(JSPC.app.finance${idSuffix}.costLocalCurrency) && ! JSPC.app.finance${idSuffix}.isError(JSPC.app.finance${idSuffix}.costBillingCurrency)) {
                    let parsedLocalCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costLocalCurrency.val().trim());
                    let parsedBillingCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val().trim());
                    JSPC.app.finance${idSuffix}.costCurrencyRate.val(JSPC.app.finance${idSuffix}.doubleToString((parsedLocalCurrency / parsedBillingCurrency), true));
                    $(".la-account-currency").find(".field").removeClass("error");
                    JSPC.app.finance${idSuffix}.calcTaxResults();
                }
            });
            this.calculateLocalCurrency.click( function() {
                if (! JSPC.app.finance${idSuffix}.isError(JSPC.app.finance${idSuffix}.costBillingCurrency) && ! JSPC.app.finance${idSuffix}.isError(JSPC.app.finance${idSuffix}.costCurrencyRate)) {
                    let parsedBillingCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val().trim());
                    let parsedCurrencyRate = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val().trim(), true);
                    if (parsedCurrencyRate === 0) {
                        JSPC.app.finance${idSuffix}.costLocalCurrency.val("");
                    }
                    else {
                        JSPC.app.finance${idSuffix}.costLocalCurrency.val((parsedBillingCurrency * parsedCurrencyRate));
                    }
                    $(".la-account-currency").find(".field").removeClass("error");
                    JSPC.app.finance${idSuffix}.calcTaxResults();
                }
            });
            this.costBillingCurrency.change(function(){
                if(!JSPC.app.finance${idSuffix}.costElems.hasClass("focused")) {
                    if(JSPC.app.finance${idSuffix}.costCurrency.val() == JSPC.app.finance${idSuffix}.eurVal) {
                        JSPC.app.finance${idSuffix}.calculateLocalCurrency.click();
                    }
                }
            });
            this.costItemElement.change(function() {
                //console.log(JSPC.app.finance${idSuffix}.ciec);
                if(typeof(JSPC.app.finance${idSuffix}.costItemElementConfigurations[JSPC.app.finance${idSuffix}.costItemElement.val()]) !== 'undefined')
                    JSPC.app.finance${idSuffix}.ciec.dropdown('set selected', JSPC.app.finance${idSuffix}.costItemElementConfigurations[JSPC.app.finance${idSuffix}.costItemElement.val()]);
                else
                    JSPC.app.finance${idSuffix}.ciec.dropdown('set selected','null');
            });
            this.costElems.focus(function(e) {
                JSPC.app.finance${idSuffix}.costElems.addClass('focused');
                JSPC.app.finance${idSuffix}.elementChangeable = false;
            });
            this.costElems.blur(function(e) {
                let parsedCurrencyRate = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costCurrencyRate.val().trim(), true);
                let parsedBillingCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val().trim());
                let parsedLocalCurrency = JSPC.app.finance${idSuffix}.stringToDouble(JSPC.app.finance${idSuffix}.costLocalCurrency.val().trim());
                let allSet = JSPC.app.finance${idSuffix}.costBillingCurrency.val().length > 0 && JSPC.app.finance${idSuffix}.costLocalCurrency.val().length > 0 && JSPC.app.finance${idSuffix}.costCurrencyRate.val().length > 0 && parsedCurrencyRate > 0;
                if(JSPC.app.finance${idSuffix}.elementChangeable === true){
                    JSPC.app.finance${idSuffix}.costElems.removeClass('focused');
                    if(allSet) {
                        JSPC.app.finance${idSuffix}.calcTaxResults($(this).attr("id") === JSPC.app.finance${idSuffix}.costLocalCurrency.attr("id")); //will set boolean localHandInput
                    }
                    else if(parsedCurrencyRate == 0) {
                        JSPC.app.finance${idSuffix}.calcTaxResults()
                    }
                    else {
                        if(JSPC.app.finance${idSuffix}.costLocalCurrency.val().length > 0 && JSPC.app.finance${idSuffix}.costCurrencyRate.val().length > 0 && parsedCurrencyRate > 0) {
                            JSPC.app.finance${idSuffix}.calculateBillingCurrency.click();
                        }
                        if(JSPC.app.finance${idSuffix}.costBillingCurrency.val().length > 0 && JSPC.app.finance${idSuffix}.costLocalCurrency.val().length > 0 && parsedBillingCurrency > 0 && parsedLocalCurrency > 0) {
                            JSPC.app.finance${idSuffix}.calculateCurrencyRate.click();
                        }
                        if(JSPC.app.finance${idSuffix}.costBillingCurrency.val().length > 0 && JSPC.app.finance${idSuffix}.costCurrencyRate.val().length > 0 && parsedCurrencyRate > 0) {
                            JSPC.app.finance${idSuffix}.calculateLocalCurrency.click();
                        }
                    }
                }
            });
            this.costElems.keydown(function(e) {
                if(e.keyCode === 27 || e.keyCode === 9) {
                    JSPC.app.finance${idSuffix}.elementChangeable = true;
                    JSPC.app.finance${idSuffix}.costElems.blur();
                }
            });
            $('html').mousedown(function(e) {
                JSPC.app.finance${idSuffix}.elementChangeable = true;
                window.setTimeout(function() {
                    JSPC.app.finance${idSuffix}.elementChangeable = false;
                }, 10);
            });
            //is to bridge the html-mousedown event listener which fires before the click event
            $('.calcButton').mousedown(function(e) {
                $(this).click();
            });
            this.calc.change( function() {
                if(!$(this).hasClass("focused")) {
                    JSPC.app.finance${idSuffix}.calcTaxResults();
                }
            });
            this.costElems.change(function(){
                if(!$(this).hasClass("focused")) {
                    JSPC.app.finance${idSuffix}.checkValues();
                    if(JSPC.app.finance${idSuffix}.costCurrency.val() != 0) {
                        JSPC.app.finance${idSuffix}.costCurrency.parent(".field").removeClass("error");
                    }
                    else {
                        JSPC.app.finance${idSuffix}.costCurrency.parent(".field").addClass("error");
                    }
                }
            });
            this.costCurrency.change(function(){
                //console.log("event listener succeeded, picked value is: "+$(this).val());
                if($(this).val() === JSPC.app.finance${idSuffix}.eurVal)
                    JSPC.app.finance${idSuffix}.costCurrencyRate.val(1.0);
                else JSPC.app.finance${idSuffix}.costCurrencyRate.val(0.0);
                JSPC.app.finance${idSuffix}.calculateLocalCurrency.click();
            });
            this.currentForm.submit(function(e){
                e.preventDefault();
                if('${idSuffix}' === 'bulk') {
                    //var isValueSetForVisibleForSubscriber = $("#percentOnOldPrice").val() > 0 ? true : (${showVisibilitySettings} ? (JSPC.app.finance${idSuffix}.isVisibleForSubscriber.val() != 'null') : true)

                    if((JSPC.app.finance${idSuffix}.costBillingCurrency.val() && JSPC.app.finance${idSuffix}.costLocalCurrency.val()) ||
                        (JSPC.app.finance${idSuffix}.costBillingCurrency.val() && JSPC.app.finance${idSuffix}.costCurrencyRate.val()) ||
                        (JSPC.app.finance${idSuffix}.costLocalCurrency.val() && JSPC.app.finance${idSuffix}.costCurrencyRate.val())) {
                        let valuesCorrect = JSPC.app.finance${idSuffix}.checkValues();
                        //if(valuesCorrect && isValueSetForVisibleForSubscriber) {
                        if(valuesCorrect) {
                            JSPC.app.finance${idSuffix}.costCurrency.parent(".field").removeClass("error");
                            JSPC.app.finance${idSuffix}.currentForm.unbind('submit').submit();
                        }
                        else {
                            //if(!isValueSetForVisibleForSubscriber) {
                                //alert("${message(code:'financials.newCosts.noIsVisibleForSubscriberPicked')}");
                            //}
                            //else{
                                alert("${message(code:'financials.newCosts.calculationError')}");
                            //}
                        }
                    }
                    else {
                        //if(!isValueSetForVisibleForSubscriber) {
                            //alert("${message(code:'financials.newCosts.noIsVisibleForSubscriberPicked')}");
                        //}
                        //else{
                            //modifications in only one of the fields
                            JSPC.app.finance${idSuffix}.currentForm.unbind('submit').submit();
                        //}
                    }
                }
                else if(JSPC.app.finance${idSuffix}.costCurrency.val() != 0) {
                    let valuesCorrect = JSPC.app.finance${idSuffix}.checkValues();
                    if(valuesCorrect) {
                        JSPC.app.finance${idSuffix}.costCurrency.parent(".field").removeClass("error");
                        if(JSPC.app.finance${idSuffix}.newSubscription.hasClass('error') || JSPC.app.finance${idSuffix}.newPackage.hasClass('error') || JSPC.app.finance${idSuffix}.newIE.hasClass('error'))
                            alert("${message(code:'financials.newCosts.entitlementError')}");
                        else {
                            if(JSPC.app.finance${idSuffix}.newLicenseeTarget.length === 1 && JSPC.app.finance${idSuffix}.newLicenseeTarget.val().length === 0) {
                                let alertText = "${institution.isCustomerType_Consortium() ? message(code:'financials.newCosts.noSubscriptionErrorConsortia') : message(code:'financials.newCosts.noSubscriptionError')}"
                                alert(alertText);
                            }
                            else {
                                //console.log(JSPC.app.finance${idSuffix}.newLicenseeTarget.val());
                                if(JSPC.app.finance${idSuffix}.newLicenseeTarget.val() && JSPC.app.finance${idSuffix}.newLicenseeTarget.val().join(";").indexOf('forParent') > -1) {
                                    if(confirm("${message(code:'financials.newCosts.confirmForParent')}")) JSPC.app.finance${idSuffix}.currentForm.unbind('submit').submit();
                                }
                                else JSPC.app.finance${idSuffix}.currentForm.unbind('submit').submit();
                            }
                        }
                    }
                    else {
                         alert("${message(code:'financials.newCosts.calculationError')}");
                    }
                }
                else {
                    alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
                    JSPC.app.finance${idSuffix}.costCurrency.parent(".field").addClass("error");
                }
            });
        }
    }
    JSPC.app.finance${idSuffix}.init();

    JSPC.app.adjustDropdown = function () {

        var showSubscriber = $("input[name='show.subscriber']").prop('checked');
        var showConnectedObjs = $("input[name='show.connectedObjects']").prop('checked');
        var url = '<g:createLink controller="ajaxJson" action="adjustCompareSubscriptionList"/>?showSubscriber=' + showSubscriber + '&showConnectedObjs=' + showConnectedObjs

        var status = $("select#status")
        if (status) {
            url = url + '&' + status.serialize()
        }

        var dropdownSelectedSubs = $('#selectedSubs');

        dropdownSelectedSubs.empty();
        dropdownSelectedSubs.append('<option selected="true" disabled>${message(code: 'default.select.choose.label')}</option>');
        dropdownSelectedSubs.prop('selectedIndex', 0);

        $.ajax({
                url: url,
                success: function (data) {
                    $.each(data, function (key, entry) {
                        if(entry.value == "${costItem?.sub?.id}" && status && status[0].value == "${costItem?.sub?.status?.id}"){
                            dropdownSelectedSubs.append($('<option></option>').attr('value', entry.value).attr('selected', 'selected').text(entry.text));
                        }else{
                            dropdownSelectedSubs.append($('<option></option>').attr('value', entry.value).text(entry.text));
                            }
                     });
                }
        });
    }

    JSPC.app.adjustCostInformationDropdown = function (withRefdata, preselect) {

        var dropdownCostInformationValues = JSPC.app.finance${idSuffix}.costInformationValueDropdown;

        dropdownCostInformationValues.empty();

        if(withRefdata) {
            var url = '<g:createLink controller="ajaxJson" action="adjustCostInformationValueList"/>?costInformationDefinition='+JSPC.app.finance${idSuffix}.costInformationDefinition.dropdown('get value');
            if(!preselect) {
                dropdownCostInformationValues.append('<option selected="selected" disabled>${message(code: 'default.select.choose.label')}</option>');
                dropdownCostInformationValues.prop('selectedIndex', 0);
            }
            else dropdownCostInformationValues.append('<option selected="selected" disabled>${message(code: 'default.select.choose.label')}</option>');

            $.ajax({
                url: url,
                success: function (data) {
                    $.each(data, function (key, entry) {
                        if(entry.value === preselect)
                            dropdownCostInformationValues.append($('<option selected="selected"></option>').attr('value', entry.value).text(entry.text));
                        else
                            dropdownCostInformationValues.append($('<option></option>').attr('value', entry.value).text(entry.text));
                    });
                }
            });
        }
    }
    <g:if test="${costItem?.costInformationDefinition}">
        JSPC.app.finance${idSuffix}.costInformationDefinition.dropdown("set selected","${costItem?.costInformationDefinition.id}");

        // set filterProp by params
        <g:if test="${costItem?.costInformationRefValue}">
            JSPC.app.finance${idSuffix}.costInformationValueText.hide();
            JSPC.app.finance${idSuffix}.costInformationValueDropdown.dropdown("set selected", "${costItem.costInformationRefValue.id}");
        </g:if>
        <g:elseif test="${costItem?.costInformationStringValue}">
            JSPC.app.finance${idSuffix}.costInformationValueDropdown.parent('div').hide();
            JSPC.app.finance${idSuffix}.costInformationValueText.val("${costItem.costInformationStringValue}").show();
        </g:elseif>
    </g:if>

    <g:if test="${mode == 'copy' && copyToOtherSub}">
        JSPC.app.adjustDropdown();
    </g:if>

</laser:script>


