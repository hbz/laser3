<!-- _createModal.gsp -->

<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<semui:modal id="costItem_create_modal" text="${message(code:'financials.addNewCost')}">
    <g:form class="ui small form" id="createCost" url="[controller:'finance', action:'newCostItem']">

        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}"></g:hiddenField>

        <p>DEBUG ${inSubMode} ${fixedSubscription}</p>

        <!-- div class="ui grid" -->
            <div class="two fields">
                <div class="field">
                    <div class="field">
                        <label>${message(code:'financials.newCosts.description')}</label>
                        <input type="text" value="${message(code:'financials.newCosts.description')}" />
                    </div><!-- .field -->
                    <div class="two fields">
                        <div class="field">
                            <label>${message(code:'financials.budgetCode')}</label>
                            <input type="text" class="select2 la-full-width" placeholder="${message(code:'financials.budgetCode')}" name="newBudgetCode" id="newBudgetCode" />
                        </div><!-- .field -->

                        <div class="field">
                            <label>Reference/Codes</label>
                            <input type="text" name="newReference" placeholder="New Item Reference" id="newCostItemReference" value="${params.newReference}"/>
                        </div>
                    </div>
                </div>
                <div class="field">
                    <div class="field ">
                        <label>${message(code:'financials.newCosts.UID')}</label>
                        <input type="text" readonly value="${message(code:'financials.newCosts.UID')}" />
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
                <fieldset class="field la-modal-fieldset-no-margin">
                    <label>${g.message(code:'financials.newCosts.amount')}</label>

                        <div class="field">
                            <label>${g.message(code:'financials.newCosts.valueInEuro')}</label>
                            <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="number" class="calc" name="newCostInBillingCurrency" placeholder="${g.message(code:'financials.newCosts.valueInEuro')}" id="newCostInBillingCurrency" value="1" step="0.01"/> <br/>
                        </div><!-- .field -->
                        <div class="field">
                            <label>${g.message(code:'financials.newCosts.exchangeRate')}</label>
                            <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc" name="newCostCurrencyRate" placeholder="${g.message(code:'financials.newCosts.exchangeRate')}" id="newCostCurrencyRate" value="1" step="0.01" /> <br/>
                        </div><!-- .field -->

                        <div class="field">
                            <label>${message(code:'financials.invoice_total')}</label>
                            <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="number" class="calc" name="newCostInLocalCurrency" placeholder="${message(code:'financials.invoice_total')}" id="newCostInLocalCurrency" value="1" step="0.01"/> <br/>
                        </div>
                        <div class="field">
                            <g:select class="ui dropdown" name="newCostCurrency" title="${g.message(code: 'financials.addNew.currencyType')}"
                                      from="${currency}"
                                      optionKey="id"
                                      optionValue="text"/>
                        </div>


                </fieldset> <!-- 1/3 field -->


                <fieldset class="field la-modal-fieldset-margin">
                <label>&nbsp;</label>
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
                        <label>${message(code:'financials.newCosts.controllable')}</label>
                        <laser:select name="newCostTaxType" title="${g.message(code: 'financials.addNew.taxCateogry')}" class="ui dropdown"
                                      from="${taxType}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${['':'']}"/>
                    </div><!-- .field -->

                </fieldset> <!-- 2/3 field -->


                <fieldset class="field la-modal-fieldset-no-margin">
                <label>${message(code:'financials.newCosts.constsReferenceOn')}</label>
                    <div class="field">
                        <label>${message(code:'subscription.label')}</label>
                        <input ${inSubMode ? "disabled='disabled' data-filterMode='${fixedSubscription?.class.getName()}:${fixedSubscription?.id}'" : '' }
                                name="newSubscription" class="la-full-width select2" placeholder="${message(code:'financials.newCosts.newLicence')}" id="newSubscription"
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
                        <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                        <g:if test="${inSubMode}">
                            <input name="newIe"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="la-full-width select2" id="newIE" value="${params.newIe}">
                        </g:if>
                        <g:else>
                            <input name="newIe" disabled='disabled' data-subFilter="" data-disableReset="true" class="la-full-width select2" id="newIE" value="${params.newIe}">
                        </g:else>
                    </div><!-- .field -->

                </fieldset> <!-- 3/3 field -->

            </div><!-- three fields -->
            <div class="three fields">
                <fieldset class="field la-modal-fieldset-no-margin">

                    <semui:datepicker label="financials.datePaid" name="newDatePaid" placeholder="financials.datePaid" value="${params.newDatePaid}" />

                    <semui:datepicker label="financials.dateFrom" name="newStartDate" placeholder="default.date.label" value="${params.newStartDate}" />

                    <semui:datepicker label="financials.dateTo" name="newEndDate" placeholder="default.date.label" value ="${params.newEndDate}" />

                </fieldset> <!-- 1/3 field -->


                <fieldset class="field la-modal-fieldset-margin">


                    <div class="field">
                        <label>${message(code:'default.description.label')}</label>
                        <textarea name="newDescription" placeholder="${message(code:'default.description.label')}" id="newCostItemDescription"/></textarea>
                    </div>

                </fieldset> <!-- 2/3 field -->


                <fieldset class="field la-modal-fieldset-no-margin">
                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <input type="text" name="newInvoiceNumber" class="input-medium"
                           placeholder="${message(code:'financials.invoice_number')}" id="newInvoiceNumber" value="${params.newInvoiceNumber}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <input type="text" name="newOrderNumber" class="input-medium"
                           placeholder="${message(code:'financials.order_number')}" id="newOrderNumber" value="${params.newOrderNumber}"/>
                </div><!-- .field -->

            </fieldset> <!-- 3/3 field -->

        </div><!-- three fields -->





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
