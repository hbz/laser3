<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<form class="ui form" id="createCost">
    <table id="newCosts" class="ui striped celled la-rowspan table table-tworow">
        <thead>
            <tr>
                <th rowspan="2" style="vertical-align: top;">Cost Item#</th>
                <th>Invoice#</th>
                <th>Order#</th>
                <th>Subscription</th>
                <th>Package</th>
                <th colspan="2" style="vertical-align: top;">Issue Entitlement</th>
            </tr>
            <tr>
                <th>Date</th>
                <th>Amount [billing] * [Exchange] = [local]</th>
                <th>Reference</th>
                <th colspan="3">Description</th>
            </tr>
        </thead>

        <tbody>
        <tr><td colspan="9">&nbsp;</td></tr>
        <tr>
            <td rowspan="2">Add a new cost item</td>
            <td><input type="text" name="newInvoiceNumber" class="input-medium"
                       placeholder="New item invoice #" id="newInvoiceNumber" value="${params.newInvoiceNumber}"/></td>
            <td><input type="text" name="newOrderNumber" class="input-medium"
                       placeholder="New Order #" id="newOrderNumber" value="${params.newOrderNumber}"/></td>
            <td>
                <input ${inSubMode ? "disabled='disabled' data-filterMode='${fixedSubscription?.class.getName()}:${fixedSubscription?.id}'" : '' }
                        name="newSubscription" class="input-xlarge select2" placeholder="New Subscription" id="newSubscription"
                        value="${inSubMode ? fixedSubscription?.name : params.newSubscription}" data-subfilter=""/>
                <g:if test="${inSubMode}">
                    <g:hiddenField data-subfilter="" name="newSubscription" value="${fixedSubscription?.class.getName()}:${fixedSubscription?.id}"></g:hiddenField>
                </g:if>
            </td>
            <td>
                <g:if test="${inSubMode}">
                  <input class="select2 input-xlarge"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" name="newPackage" id="newPackage" />
                </g:if>
                <g:else>
                  <input class="select2 input-xlarge" disabled='disabled' data-subFilter="" data-disableReset="true" name="newPackage" id="newPackage" />
                </g:else>
            </td>
            <td>
                <g:if test="${inSubMode}">
                  <input name="newIe"  data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="input-large select2" id="newIE" value="${params.newIe}">
                </g:if>
                <g:else>
                  <input name="newIe" disabled='disabled' data-subFilter="" data-disableReset="true" class="input-large select2" id="newIE" value="${params.newIe}">
                </g:else>
            </td>
            <td rowspan="2">
                <g:submitToRemote data-action="create" onSuccess="Finance.updateResults('create');Finance.clearCreate()"
                                  onFailure="errorHandling(textStatus,'create',errorThrown)"
                                  url="[controller:'finance', action: 'newCostItem']" type="submit"
                                  name="Add" value="add"
                                  class="ui button">
                </g:submitToRemote> </br></br>
            </td>
        </tr>
        <tr>
            <td>
                <div class="fields">
                    <semui:datepicker label="financials.datePaid" name="newDate" placeholder ="financials.datePaid" value="${params.newDate}" >
                    </semui:datepicker>
                </div>
                <h4 class="ui header">Statuses</h4>
                <g:select name="newCostItemStatus"
                          id="newCostItemStatus"
                          from="${costItemStatus}"
                          optionKey="id"
                          title="${g.message(code: 'financials.addNew.costState')}"
                          noSelection="${['':'No Status']}"/> <br/>

                <g:select name="newCostItemCategory"
                          id="newCostItemCategory"
                          from="${costItemCategory}"
                          optionKey="id"
                          title="${g.message(code: 'financials.addNew.costCategory')}"
                          noSelection="${['':'No Category']}"/> <br/>

                <g:select name="newCostItemElement"
                          from="${costItemElement}"
                          optionKey="id"
                          noSelection="${['':'No Element']}"/> <br/>

                <g:select name="newCostTaxType"
                          from="${taxType}"
                          optionKey="id"
                          title="${g.message(code: 'financials.addNew.taxCateogry')}"
                          noSelection="${['':'No Tax Type']}"/> <br/>
            </td>
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
                <input type="text" class="select2" style="width: 220px; border-radius: 4px;" placeholder="New code or lookup code" name="newBudgetCode" id="newBudgetCode" ><br/><br/><br/>
                <h4 class="ui header">Validity Period (Dates)</h4>
                <div class="fields">
                    <semui:datepicker label ="datamanager.changeLog.from_date" name="newStartDate" placeholder ="default.date.label" >
                    </semui:datepicker>
                    <semui:datepicker label ="datamanager.changeLog.to_date" name="newEndDate" placeholder ="default.date.label" value ="${params.endDate}">
                    </semui:datepicker>
                </div>
            </td>
            <td colspan="2">
                <h4 class="ui header">Description</h4>
                <textarea name="newDescription" placeholder="New Item Description" id="newCostItemDescription"/></textarea>
            </td>
        </tr>
        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}"></g:hiddenField>
        </tbody>
    </table>
</form>
