<!-- _createModal.gsp -->
<style>
    .fields {
        background: #fffaf5;
        border: 1px solid grey;
    }
</style>
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<semui:modal id="costItem_create_modal" text="${message(code:'financials.addNewCost')}">
    <g:form class="ui small form" id="createCost" url="[controller:'finance', action:'newCostItem']">

        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}"></g:hiddenField>

        <!-- div class="ui grid" -->
            <div class="two fields">
                <div class="field">
                    <div class="field">
                        <label>${message(code:'financials.newCosts.description')}</label>
                        <input type="text" value="placeholder" />
                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code:'financials.budgetCode')}</label>
                        <input type="text" class="select2 la-full-width" placeholder="New code or lookup code" name="newBudgetCode" id="newBudgetCode" />
                    </div><!-- .field -->
                </div>
                <div class="field">
                    <div class="field">
                        <label>${message(code:'financials.newCosts.UID')}</label>
                        <input type="text" value="placeholder" />
                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code:'financials.costItemStatus')}</label>
                        <laser:select name="newCostItemStatus" title="${g.message(code: 'financials.addNew.costState')}" class="ui dropdown"
                                      id="newCostItemStatus"
                                      from="${costItemStatus}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${['':'']}"/>
                    </div><!-- .field -->
                </div>
            </div><!-- two fields -->

            <div class="three fields">
                <div class="field">
                    <label>${g.message(code:'financials.newCosts.amount')}</label>
                    <div class="field">
                        <div class="field">
                            <label>${g.message(code:'financials.newCosts.valueInEuro')}</label>
                            <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="number" class="calc" name="newCostInBillingCurrency" placeholder="New Cost Ex-Tax - Billing Currency" id="newCostInBillingCurrency" value="1" step="0.01"/> <br/>
                        </div><!-- .field -->
                        <div class="field">
                            <label>Umrechnungsfaktor</label>
                            <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc" name="newCostCurrencyRate" placeholder="Exchange Rate" id="newCostCurrencyRate" value="1" step="0.01" /> <br/>
                        </div><!-- .field -->
                        <div class="two fields">
                            <div class="field">
                                <label>${message(code:'financials.invoice_total')}</label>
                                <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="number" class="calc" name="newCostInLocalCurrency" placeholder="New Cost Ex-Tax - Local Currency" id="newCostInLocalCurrency" value="1" step="0.01"/> <br/>
                            </div>
                            <div class="field">
                                <label>&nbsp;</label>
                                <g:select name="newCostCurrency" title="${g.message(code: 'financials.addNew.currencyType')}"
                                          from="${currency}"
                                          optionKey="id"
                                          optionValue="text"/>
                            </div>
                        </div><!-- fields -->
                    </div>
                </div> <!-- 1/3 field -->


                <div class="field">

                    <div class="field">

                    </div>

                </div> <!-- 2/3 field -->


                <div class="field">

                    <div class="field">

                    </div>

                </div> <!-- 3/3 field -->

            </div><!-- three fields -->


            <div class="four wide column">

                <semui:datepicker label="financials.datePaid" name="newDate" placeholder="financials.datePaid" value="${params.newDate}" />

                <semui:datepicker label="financials.dateFrom" name="newStartDate" placeholder="default.date.label" value="${params.newStartDate}" />

                <semui:datepicker label="financials.dateTo" name="newEndDate" placeholder="default.date.label" value ="${params.newEndDate}" />

            </div><!-- .column -->

            <div class="four wide column">
                <div class="field">
                    <label>${message(code:'financials.costItemCategory')}</label>
                    <laser:select name="newCostItemCategory" title="${g.message(code: 'financials.addNew.costCategory')}" class="ui dropdown"
                          id="newCostItemCategory"
                          from="${costItemCategory}"
                          optionKey="id"
                          optionValue="value"
                          noSelection="${['':'']}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.costItemElement')}</label>
                    <laser:select name="newCostItemElement" class="ui dropdown"
                              from="${costItemElement}"
                              optionKey="id"
                              optionValue="value"
                              noSelection="${['':'']}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>Steuerbar</label>
                    <laser:select name="newCostTaxType" title="${g.message(code: 'financials.addNew.taxCateogry')}" class="ui dropdown"
                              from="${taxType}"
                              optionKey="id"
                              optionValue="value"
                              noSelection="${['':'']}"/>
                </div><!-- .field -->
            </div><!-- .column -->


            <div class="four wide column">


                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <input type="text" name="newInvoiceNumber" class="input-medium"
                           placeholder="New item invoice #" id="newInvoiceNumber" value="${params.newInvoiceNumber}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <input type="text" name="newOrderNumber" class="input-medium"
                           placeholder="New Order #" id="newOrderNumber" value="${params.newOrderNumber}"/>
                </div><!-- .field -->
            </div><!-- .column -->

            <div class="four wide column">
                <div class="field">
                    <label>${message(code:'subscription.label')}</label>
                    <input ${inSubMode ? "disabled='disabled' data-filterMode='${fixedSubscription?.class.getName()}:${fixedSubscription?.id}'" : '' }
                            name="newSubscription" class="la-full-width select2" placeholder="New Subscription" id="newSubscription"
                            value="${inSubMode ? fixedSubscription?.name : params.newSubscription}" data-subfilter=""/>
                    <g:if test="${inSubMode}">
                        <g:hiddenField data-subfilter="" name="newSubscription" value="${fixedSubscription?.class.getName()}:${fixedSubscription?.id}"></g:hiddenField>
                    </g:if>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'package.label')}</label>
                    <g:if test="${inSubMode}">
                        <input class="select2 la-full-width"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" name="newPackage" id="newPackage" />
                    </g:if>
                    <g:else>
                        <input class="select2 la-full-width" disabled='disabled' data-subFilter="" data-disableReset="true" name="newPackage" id="newPackage" />
                    </g:else>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'issueEntitlement.label')}</label>
                    <g:if test="${inSubMode}">
                        <input name="newIe"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="la-full-width select2" id="newIE" value="${params.newIe}">
                    </g:if>
                    <g:else>
                        <input name="newIe" disabled='disabled' data-subFilter="" data-disableReset="true" class="la-full-width select2" id="newIE" value="${params.newIe}">
                    </g:else>
                </div><!-- .field -->
            </div><!-- .column -->

            <div class="eight wide column">
                <div class="field">
                    <label>${message(code:'default.description.label')}</label>
                    <textarea name="newDescription" placeholder="New Item Description" id="newCostItemDescription"/></textarea>
                </div>
            </div><!-- .column -->

            <div class="four wide column">
                <div class="field">
                    <label>Reference/Codes</label>
                    <input type="text" name="newReference" placeholder="New Item Reference" id="newCostItemReference" value="${params.newReference}"/>
                </div>
            </div><!-- .column -->


            <%-- <div class="twelve wide column">
                <div class="field">
                    <label>&nbsp;</label>
                    <g:submitToRemote data-action="create" onSuccess="Finance.updateResults('create');Finance.clearCreate()"
                                      onFailure="errorHandling(textStatus,'create',errorThrown)"
                                      url="[controller:'finance', action: 'newCostItem']" type="submit"
                                      name="Add" value="${message(code:'default.button.create_new.label')}"
                                      class="ui button">
                    </g:submitToRemote>
                </div>
            </div><!-- .column --> --%>

        <!-- /div --><!-- .grid -->
    </g:form>
</semui:modal>

<!-- _createModal.gsp -->
