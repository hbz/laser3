<!-- _create.gsp -->
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<semui:form>
<form class="ui form" id="createCost">
    <table id="newCosts" class="ui striped celled la-rowspan table table-tworow">
        <tbody>
        <tr>
            <td>
                <h4 class="ui header">Cost values and Currency</h4>
                <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="number" class="calc" name="newCostInBillingCurrency" placeholder="New Cost Ex-Tax - Billing Currency" id="newCostInBillingCurrency" value="1" step="0.01"/> <br/>

                <g:select name="newCostCurrency"
                          from="${currency}"
                          optionKey="id"
                          title="${g.message(code: 'financials.addNew.currencyType')}"
                          optionValue="text"/>

                <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc" name="newCostCurrencyRate" placeholder="Exchange Rate" id="newCostCurrencyRate" value="1" step="0.01" /> <br/>
                <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="number" class="calc" name="newCostInLocalCurrency" placeholder="New Cost Ex-Tax - Local Currency" id="newCostInLocalCurrency" value="1" step="0.01"/> <br/>
            </td>
            <td>
                <h4 class="ui header">Reference/Codes</h4>
                <input type="text" name="newReference" placeholder="New Item Reference" id="newCostItemReference" value="${params.newReference}"/><br/>

            </td>
        </tr>
        </tbody>
    </table>

    <div style="clear:both"></div>

    <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}"></g:hiddenField>



    <div class="three fields">

        <div class="field">
            <label>Budgetcode</label>
            <input type="text" class="select2" placeholder="New code or lookup code" name="newBudgetCode" id="newBudgetCode" >
        </div><!-- .field -->

        <div class="field">
            <label>Kategorie</label>
            <g:select name="newCostItemCategory"
                  id="newCostItemCategory"
                  from="${costItemCategory}"
                  optionKey="id"
                  title="${g.message(code: 'financials.addNew.costCategory')}"
                  noSelection="${['':'No Category']}"/>
        </div><!-- .field -->

    <div class="field">
            <label>Subscription</label>
            <input ${inSubMode ? "disabled='disabled' data-filterMode='${fixedSubscription?.class.getName()}:${fixedSubscription?.id}'" : '' }
                    name="newSubscription" class="input-xlarge select2" placeholder="New Subscription" id="newSubscription"
                    value="${inSubMode ? fixedSubscription?.name : params.newSubscription}" data-subfilter=""/>
            <g:if test="${inSubMode}">
                <g:hiddenField data-subfilter="" name="newSubscription" value="${fixedSubscription?.class.getName()}:${fixedSubscription?.id}"></g:hiddenField>
            </g:if>
        </div><!-- .field -->

    </div><!-- row -->

    <div class="three fields">

        <div class="field">
            <label>Rechnungsnr.</label>
            <input type="text" name="newInvoiceNumber" class="input-medium"
                   placeholder="New item invoice #" id="newInvoiceNumber" value="${params.newInvoiceNumber}"/>
        </div><!-- .field -->

        <div class="field">
            <label>Komponente</label>
            <g:select name="newCostItemElement"
                      from="${costItemElement}"
                      optionKey="id"
                      noSelection="${['':'No Element']}"/>
        </div><!-- .field -->

        <div class="field">
            <label>Package</label>
            <g:if test="${inSubMode}">
                <input class="select2 input-xlarge"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" name="newPackage" id="newPackage" />
            </g:if>
            <g:else>
                <input class="select2 input-xlarge" disabled='disabled' data-subFilter="" data-disableReset="true" name="newPackage" id="newPackage" />
            </g:else>
        </div><!-- .field -->
    </div><!-- row -->

    <div class="three fields">

        <div class="field">
            <label>Bestellnummer</label>
            <input type="text" name="newOrderNumber" class="input-medium"
                placeholder="New Order #" id="newOrderNumber" value="${params.newOrderNumber}"/>
        </div><!-- .field -->

        <div class="field">
            <label>Steuerbar</label>
            <g:select name="newCostTaxType"
                  from="${taxType}"
                  optionKey="id"
                  title="${g.message(code: 'financials.addNew.taxCateogry')}"
                  noSelection="${['':'No Tax Type']}"/>
        </div><!-- .field -->

        <div class="field">
            <label>Issue Entitlement</label>
            <g:if test="${inSubMode}">
                <input name="newIe"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="input-large select2" id="newIE" value="${params.newIe}">
            </g:if>
            <g:else>
                <input name="newIe" disabled='disabled' data-subFilter="" data-disableReset="true" class="input-large select2" id="newIE" value="${params.newIe}">
            </g:else>
        </div><!-- .field -->
    </div><!-- row -->

    <div class="three fields">

        <div class="field">
            <label>Beschreibung</label>
            <textarea name="newDescription" placeholder="New Item Description" id="newCostItemDescription"/></textarea>
        </div>
    </div><!-- row -->





    <g:submitToRemote data-action="create" onSuccess="Finance.updateResults('create');Finance.clearCreate()"
                      onFailure="errorHandling(textStatus,'create',errorThrown)"
                      url="[controller:'finance', action: 'newCostItem']" type="submit"
                      name="Add" value="${message(code:'default.button.create_new.label')}"
                      class="ui button">
    </g:submitToRemote>

</form>
</semui:form>


<semui:form>
    <form class="ui form" id="createCost">

        <div class="ui grid">

            <div class="four wide column">

                <semui:datepicker label="financials.datePaid" name="newDate" placeholder="financials.datePaid" value="${params.newDate}" />

                <semui:datepicker label="financials.dateFrom" name="newStartDate" placeholder="default.date.label" value="${params.newStartDate}" />

                <semui:datepicker label="financials.dateTo" name="newEndDate" placeholder="default.date.label" value ="${params.newEndDate}" />

            </div><!-- .column -->

            <div class="four wide column">

                <div class="field">
                    <label>Bezeichnung</label>
                    <input type="text" value="placeholder" />
                </div><!-- .field -->

                <div class="field">
                    <label>Status</label>
                <g:select name="newCostItemStatus"
                          id="newCostItemStatus"
                          from="${costItemStatus}"
                          optionKey="id"
                          title="${g.message(code: 'financials.addNew.costState')}"
                          noSelection="${['':'No Status']}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>UID</label>
                    <input type="text" value="placeholder" />
                </div><!-- .field -->

            </div><!-- .column -->

        </div><!-- .grid -->

    </form>
</semui:form>

<!-- _create.gsp -->
