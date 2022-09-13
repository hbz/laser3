<%@ page import="de.laser.finance.CostItem; de.laser.UserSetting; de.laser.storage.RDStore; de.laser.*; de.laser.finance.CostItemElementConfiguration;" %>
<laser:serviceInjection/>

<g:hiddenField name="shortcode" value="${contextService.getOrg().shortcode}"/>
<g:if test="${setting == 'bulkForAll'}">
    <g:hiddenField name="surveyConfig" value="${surveyConfig.class.getName()}:${surveyConfig.id}"/>
    <g:hiddenField name="surveyOrgs" value="${surveyOrgList.join(",")}"/>
</g:if>
<g:else>
    <g:if test="${costItem && (mode && mode.equals("edit"))}">
        <g:hiddenField name="oldCostItem" value="${costItem.class.getName()}:${costItem.id}"/>
    </g:if>
    <g:if test="${surveyOrg}">
        <g:hiddenField name="surveyOrg" value="${surveyOrg.class.getName()}:${surveyOrg.id}"/>
    </g:if>
</g:else>

<div class="fields la-forms-grid">
    <div class="eight wide field">
        <div class="field">
            <label for="newCostTitle">${message(code: 'financials.newCosts.costTitle')}</label>
            <input type="text" name="newCostTitle" id="newCostTitle" value="${costItem?.costTitle}"/>
        </div><!-- .field -->

    </div>

    <div class="eight wide field">
        <div class="two fields la-fields-no-margin-button">
            <div class="field">
                <label><g:message code="financials.costItemElement"/></label>
                <g:if test="${costItemElements}">
                    <ui:select name="newCostItemElement" id="newCostItemElement_${idSuffix}" class="ui fluid dropdown"
                                  from="${costItemElements.collect { ciec -> ciec.costItemElement }}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${[null: message(code: 'default.select.choose.label')]}"
                                  value="${costItem?.costItemElement?.id}"/>
                </g:if>
                <g:else>
                    ${message(code: 'financials.costItemElement.noneDefined')}
                </g:else>
            </div><!-- .field -->
            <div class="field">
                <label><g:message code="financials.costItemConfiguration"/></label>
                <ui:select name="ciec" id="ciec_${idSuffix}" class="ui fluid  dropdown"
                              from="${costItemSigns}"
                              optionKey="id"
                              optionValue="value"
                              noSelection="${[null: message(code: 'default.select.choose.label')]}"
                              value="${costItem?.costItemElementConfiguration?.id}"/>
            </div>
        </div>

        <div class="field">
            <label>${message(code: 'default.status.label')}</label>
            <ui:select name="newCostItemStatus" id="newCostItemStatus_${idSuffix}" title="${g.message(code: 'financials.addNew.costState')}"
                          class="ui dropdown"
                          from="${costItemStatus}"
                          optionKey="id"
                          optionValue="value"
                          noSelection="${[(RDStore.GENERIC_NULL_VALUE.id): message(code: 'default.select.choose.label')]}"
                          value="${costItem?.costItemStatus?.id}"/>
        </div><!-- .field -->

    </div> <!-- 2/2 field -->
</div><!-- two fields -->

<div class="fields la-forms-grid">
    <fieldset class="sixteen wide field la-account-currency">
        <label>${g.message(code: 'financials.newCosts.amount')}</label>

        <div class="two fields">
            <div class="field">
                <label>${message(code: 'financials.invoice_total')}</label>
                <input title="${g.message(code: 'financials.addNew.BillingCurrency')}" type="text" class="calc"
                       style="width:50%"
                       name="newCostInBillingCurrency" id="newCostInBillingCurrency_${idSuffix}"
                       placeholder="${g.message(code: 'financials.invoice_total')}"
                       value="<g:formatNumber
                               number="${costItem?.costInBillingCurrency}"
                               minFractionDigits="2" maxFractionDigits="2"/>"/>

                <g:select class="ui dropdown la-small-dropdown la-not-clearable" name="newCostCurrency" id="newCostCurrency_${idSuffix}"
                          title="${message(code: 'financials.addNew.currencyType')}"
                          from="${currency}"
                          optionKey="id"
                          optionValue="${{ it.text.contains('-') ? it.text.split('-').first() : it.text }}"
                          value="${costItem?.billingCurrency?.id}"/>
            </div><!-- .field -->
            <div class="field">
                <label><g:message code="financials.newCosts.totalAmount"/></label>
                <input title="${g.message(code: 'financials.newCosts.totalAmount')}" type="text" readonly="readonly"
                       name="newCostInBillingCurrencyAfterTax" id="newCostInBillingCurrencyAfterTax_${idSuffix}"
                       value="<g:formatNumber
                               number="${costItem?.costInBillingCurrencyAfterTax}"
                               minFractionDigits="2" maxFractionDigits="2"/>"/>

            </div><!-- .field -->

        </div>

        <div class="two fields">
            <div class="field la-exchange-rate">

            </div><!-- .field -->
            <div class="field">
                <label>${message(code: 'financials.newCosts.taxTypeAndRate')}</label>
                <g:select class="ui dropdown calc" name="newTaxRate" id="newTaxRate_${idSuffix}" title="TaxRate"
                          from="${CostItem.TAX_TYPES}"
                          optionKey="${{ it.taxType.class.name + ":" + it.taxType.id + "§" + it.taxRate }}"
                          optionValue="${{ it.display ? it.taxType.getI10n("value") + " (" + it.taxRate + "%)" : it.taxType.getI10n("value") }}"
                          value="${taxKey?.taxType?.class?.name}:${taxKey?.taxType?.id}§${taxKey?.taxRate}"
                          noSelection="${['null§0': '']}"/>

            </div><!-- .field -->
        </div>

        <div class="two fields">
            <div class="field">
                <div class="ui checkbox">
                    <label for="newBillingSumRounding_${idSuffix}"><g:message code="financials.newCosts.roundBillingSum"/></label>
                    <input name="newBillingSumRounding" id="newBillingSumRounding_${idSuffix}" class="hidden calc" type="checkbox"
                        <g:if test="${costItem?.billingSumRounding}">checked="checked"</g:if>/>
                </div>
            </div><!-- .field -->
            <div class="field">
                <div class="ui checkbox">
                    <label for="newFinalCostRounding_${idSuffix}"><g:message code="financials.newCosts.roundFinalSum"/></label>
                    <input name="newFinalCostRounding" id="newFinalCostRounding_${idSuffix}" class="hidden calc" type="checkbox"
                        <g:if test="${costItem?.finalCostRounding}">checked="checked"</g:if>/>
                </div>
            </div><!-- .field -->
        </div>

    </fieldset> <!-- 1/2 field |  .la-account-currency -->

</div><!-- three fields -->

<div class="field ">
    <div class="two fields la-forms-grid">
        <ui:datepicker label="financials.dateFrom" id="newStartDate" name="newStartDate" placeholder="default.date.label"
                          value="${costItem?.startDate}"/>

        <ui:datepicker label="financials.dateTo" id="newEndDate" name="newEndDate" placeholder="default.date.label"
                          value="${costItem?.endDate}"/>
    </div>


    <div class="one fields la-forms-grid">
        <fieldset class="sixteen wide field">
            <div class="field">
                <label for="newDescription">${message(code: 'survey.costItemModal.descriptionfor', args: [surveyOrg ? surveyOrg.org.name : 'alle'])}</label>
                <textarea class="la-textarea-resize-vertical" name="newDescription" id="newDescription"
                          placeholder="${message(code: 'default.description.label')}">${costItem?.costDescription}</textarea>
            </div><!-- .field -->
        </fieldset>
    </div>
</div>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.finance${idSuffix} = {
        userLang: "${contextService.getUser().getSettingsValue(UserSetting.KEYS.LANGUAGE,null)}",
        currentForm: $("#editCost_${idSuffix}"),
        costBillingCurrency: $("#newCostInBillingCurrency_${idSuffix}"),
        costBillingCurrencyAfterTax: $("#newCostInBillingCurrencyAfterTax_${idSuffix}"),
        costCurrency: $("#newCostCurrency_${idSuffix}"),
        costItemElement: $("#newCostItemElement_${idSuffix}"),
        billingSumRounding: $("#newBillingSumRounding_${idSuffix}"),
        finalCostRounding: $("#newFinalCostRounding_${idSuffix}"),
        taxRate: $("#newTaxRate_${idSuffix}"),
        ciec: $("#ciec_${idSuffix}"),
        costElems: $("#newCostInBillingCurrency_${idSuffix}"),
        calc: $(".calc"),
        percentOnOldPrice: $("#percentOnOldPrice"),
        elementChangeable: false,
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
        eurVal: "${RDStore.CURRENCY_EUR.id}",
        isError: function(elem)  {
            if (elem.val().length <= 0 || elem.val() < 0) {
                $(".la-account-currency").children(".field").removeClass("error");
                elem.parent(".field").addClass("error");
                return true
            }
            return false
        },
        calcTaxResults: function () {
            let roundB = JSPC.app.finance${idSuffix}.billingSumRounding.prop('checked');
            let roundF = JSPC.app.finance${idSuffix}.finalCostRounding.prop('checked');
            //console.log(taxRate.val());
            let taxF = 1.0 + (0.01 * JSPC.app.finance${idSuffix}.taxRate.val().split("§")[1]);
            let parsedBillingCurrency = JSPC.app.finance${idSuffix}.convertDouble(JSPC.app.finance${idSuffix}.costBillingCurrency.val().trim());
            let billingCurrencyAfterRounding = roundB ? Math.round(parsedBillingCurrency) : JSPC.app.finance${idSuffix}.convertDouble(parsedBillingCurrency);
            JSPC.app.finance${idSuffix}.costBillingCurrency.val(JSPC.app.finance${idSuffix}.outputValue(billingCurrencyAfterRounding));
            let billingAfterTax = roundF ? Math.round(billingCurrencyAfterRounding * taxF) : JSPC.app.finance${idSuffix}.convertDouble(billingCurrencyAfterRounding * taxF);
            JSPC.app.finance${idSuffix}.costBillingCurrencyAfterTax.val(
                JSPC.app.finance${idSuffix}.outputValue(billingAfterTax)
            );
        },
        convertDouble: function (input) {
            let output;
            //determine locale from server
            if(typeof(input) === 'number') {
                output = input.toFixed(2);
                //console.log("input: "+input+", typeof: "+typeof(input));
            }
            else if(typeof(input) === 'string') {
                output = 0.0;
                if(JSPC.app.finance${idSuffix}userLang === 'en') {
                    output = parseFloat(input);
                }
                else {
                    if(input.match(/(\d+\.?)*\d+(,\d{2})?/g))
                        output = parseFloat(input.replace(/\./g,"").replace(/,/g,"."));
                    else if(input.match(/(\d+,?)*\d+(\.\d{2})?/g))
                        output = parseFloat(input.replace(/,/g, ""));
                    //else console.log("Please check over regex!");
                }
                //console.log("string input parsed, output is: "+output);
            }
            return output;
        },
        outputValue: function(input) {
            //console.log(userLang);
            let output;
            if(JSPC.app.finance${idSuffix}.userLang !== 'en')
                output = input.toString().replace(".",",");
            else output = input.toString();
            //console.log("output: "+output+", typeof: "+typeof(output));
            return output;
        },
        init: function(elem) {
            //console.log(this);
            this.costItemElement.change(function() {
                console.log(JSPC.app.finance${idSuffix}.ciec);
                if(typeof(JSPC.app.finance${idSuffix}.costItemElementConfigurations[JSPC.app.finance${idSuffix}.costItemElement.val()]) !== 'undefined')
                    JSPC.app.finance${idSuffix}.ciec.dropdown('set selected', JSPC.app.finance${idSuffix}.costItemElementConfigurations[JSPC.app.finance${idSuffix}.costItemElement.val()]);
                else
                    JSPC.app.finance${idSuffix}.ciec.dropdown('set selected','null');
            });
            this.calc.change( function() {
                if('${idSuffix}' !== 'bulk' && !$(this).hasClass("focused"))
                    JSPC.app.finance${idSuffix}.calcTaxResults();
            });
            this.costElems.focus(function(e) {
                JSPC.app.finance${idSuffix}.costElems.addClass('focused');
                JSPC.app.finance${idSuffix}.elementChangeable = false;
            });
            this.costElems.blur(function(e) {
                if(JSPC.app.finance${idSuffix}.elementChangeable === true){
                    JSPC.app.finance${idSuffix}.costElems.removeClass('focused');
                    JSPC.app.finance${idSuffix}.calcTaxResults();
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
            this.costElems.change(function(){
                if(!$(this).hasClass("focused")) {
                    if(JSPC.app.finance${idSuffix}.costCurrency.val() != 0) {
                        JSPC.app.finance${idSuffix}.costCurrency.parent(".field").removeClass("error");
                    }
                    else {
                        JSPC.app.finance${idSuffix}.costCurrency.parent(".field").addClass("error");
                    }
                }
            });
            this.currentForm.submit(function(e){
                e.preventDefault();
                if(JSPC.app.finance${idSuffix}.percentOnOldPrice.val() >= 0){
                    JSPC.app.finance${idSuffix}.currentForm.unbind('submit').submit();
                } else if(JSPC.app.finance${idSuffix}.costCurrency.val() != 0) {
                    JSPC.app.finance${idSuffix}.currentForm.unbind('submit').submit();
                }
                else {
                    alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
                    JSPC.app.finance${idSuffix}.costCurrency.parent(".field").addClass("error");
                }
            });
        }
    }
    JSPC.app.finance${idSuffix}.init();

</laser:script>

