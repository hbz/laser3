<%@ page import="de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.FinanceService; de.laser.RefdataCategory; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.RDStore" %>
<br />
<semui:form>

<g:set var="financeService" bean="financeService"/>

<h3 class="ui header"><span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
          data-content="${message(code: 'surveyCostItems.bulkOption.info')}">
    ${message(code: 'surveyCostItems.bulkOption.label')}
    <i class="question circle icon"></i>
</span>:</h3>

<div class="ui basic segment">
    <div class="fields">
        <fieldset class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
            <label>${g.message(code: 'financials.newCosts.amount')}</label>

            <div class="two fields">
                <div class="field">
                    <label>${message(code: 'financials.invoice_total')}</label>
                    <input title="${g.message(code: 'financials.addNew.BillingCurrency')}" type="text"
                           class="calc"
                           style="width:75%"
                           name="newCostInBillingCurrency2" id="newCostInBillingCurrency2"
                           placeholder="${g.message(code: 'financials.invoice_total')}"
                           value="<g:formatNumber
                                   number="${costItem?.costInBillingCurrency}"
                                   minFractionDigits="2" maxFractionDigits="2"/>"/>

                    <div class="ui icon button la-popup-tooltip la-delay" id="costButton32"
                         data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}"
                         data-position="top center" data-variation="tiny">
                        <i class="calculator icon"></i>
                    </div>

                    <g:select class="ui dropdown dk-width-auto" name="newCostCurrency2"
                              title="${g.message(code: 'financials.addNew.currencyType')}"
                              from="${financeService.orderedCurrency()}"
                              optionKey="id"
                              optionValue="${{ it.text.contains('-') ? it.text.split('-').first() : it.text }}"
                              value="${costItem?.billingCurrency?.id}"/>
                </div><!-- .field -->
                <div class="field">
                    <label><g:message code="financials.newCosts.billingSum"/></label>
                    <input title="<g:message code="financials.newCosts.billingSum"/>" type="text"
                           readonly="readonly"
                           name="newCostInBillingCurrencyAfterTax2"
                           id="newCostInBillingCurrencyAfterTax2"
                           value="<g:formatNumber
                                   number="${costItem?.costInBillingCurrencyAfterTax}"
                                   minFractionDigits="2" maxFractionDigits="2"/>"/>

                </div><!-- .field -->
            <!-- TODO -->
                <style>
                .dk-width-auto {
                    width: auto !important;
                    min-width: auto !important;
                }
                </style>
            </div>

            <div class="two fields">
                <div class="field la-exchange-rate">
                    <label>${g.message(code: 'financials.newCosts.exchangeRate')}</label>
                    <input title="${g.message(code: 'financials.addNew.currencyRate')}" type="number"
                           class="calc"
                           name="newCostCurrencyRate2" id="newCostCurrencyRate2"
                           placeholder="${g.message(code: 'financials.newCosts.exchangeRate')}"
                           value="${costItem ? costItem.currencyRate : 1.0}" step="0.000000001"/>

                    <div class="ui icon button la-popup-tooltip la-delay" id="costButton22"
                         data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}"
                         data-position="top center" data-variation="tiny">
                        <i class="calculator icon"></i>
                    </div>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code: 'financials.newCosts.taxTypeAndRate')}</label>
                    <%
                        CostItem.TAX_TYPES taxKey
                        if (costItem?.taxKey && tab != "subscr")
                            taxKey = costItem.taxKey
                    %>
                    <g:select class="ui dropdown calc" name="newTaxRate2" title="TaxRate"
                              from="${CostItem.TAX_TYPES}"
                              optionKey="${{ it.taxType.class.name + ":" + it.taxType.id + "§" + it.taxRate }}"
                              optionValue="${{ it.taxType.getI10n("value") + " (" + it.taxRate + "%)" }}"
                              value="${taxKey?.taxType?.class?.name}:${taxKey?.taxType?.id}§${taxKey?.taxRate}"
                              noSelection="${['null§0': '']}"/>

                </div><!-- .field -->
            </div>

            <div class="two fields">
                <div class="field">
                    <label>${g.message(code: 'financials.newCosts.value')}</label>
                    <input title="${g.message(code: 'financials.addNew.LocalCurrency')}" type="text"
                           class="calc"
                           name="newCostInLocalCurrency2" id="newCostInLocalCurrency2"
                           placeholder="${message(code: 'financials.newCosts.value')}"
                           value="<g:formatNumber
                                   number="${costItem?.costInLocalCurrency}"
                                   minFractionDigits="2" maxFractionDigits="2"/>"/>

                    <div class="ui icon button la-popup-tooltip la-delay" id="costButton12"
                         data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}"
                         data-position="top center" data-variation="tiny">
                        <i class="calculator icon"></i>
                    </div>
                </div><!-- .field -->
                <div class="field">
                    <label><g:message code="financials.newCosts.finalSum"/></label>
                    <input title="<g:message code="financials.newCosts.finalSum"/>" type="text"
                           readonly="readonly"
                           name="newCostInLocalCurrencyAfterTax2" id="newCostInLocalCurrencyAfterTax2"
                           value="<g:formatNumber
                                   number="${costItem?.costInLocalCurrencyAfterTax}"
                                   minFractionDigits="2" maxFractionDigits="2"/>"/>
                </div><!-- .field -->
            </div>
            <div class="two fields">
                <div class="field">
                    <div class="ui checkbox">
                        <label><g:message code="financials.newCosts.finalSumRounded"/></label>
                        <input name="newFinalCostRounding2" class="hidden calc" type="checkbox"
                               <g:if test="${costItem?.finalCostRounding}">checked="checked"</g:if>/>
                    </div>
                </div><!-- .field -->

                <div class="field">
                    <label><g:message code="financials.isVisibleForSubscriber"/></label>
                    <g:set var="newIsVisibleForSubscriberValue2" value="${costItem?.isVisibleForSubscriber ? RDStore.YN_YES.id : RDStore.YN_NO.id}" />
                    <laser:select name="newIsVisibleForSubscriber2" class="ui dropdown"
                                  id="newIsVisibleForSubscriber2"
                                  from="${yn}"
                                  optionKey="id"
                                  optionValue="value"
                                  value="${newIsVisibleForSubscriberValue2}" />
                </div><!-- .field -->
            </div>

        </fieldset> <!-- 1/2 field |  .la-account-currency -->

    </div><!-- three fields -->

    <div class="two fields">
        <div class="eight wide field" style="text-align: left;">
            <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
        </div>

        <div class="eight wide field" style="text-align: right;">
        </div>
    </div>

</div>

</semui:form>

<laser:script file="${this.getGroovyPageFileName()}">

    JSPC.app.eurVal = "${RefdataValue.getByValueAndCategory('EUR','Currency').id}";

    $("#newCostInBillingCurrency2").change(function () {
        var currencyEUR = ${RefdataValue.getByValueAndCategory('EUR','Currency').id};
        if ($("#newCostCurrency2").val() == currencyEUR) {
            $("#costButton12").click();
        }
    });

    $("#costButton12").click(function () {
        if (! JSPC.app.isError("#newCostInBillingCurrency2") && ! JSPC.app.isError("#newCostCurrencyRate2")) {
            var input = $(this).siblings("input");
            input.transition('glow');
            var parsedBillingCurrency = JSPC.app.convertDouble($("#newCostInBillingCurrency2").val());
            input.val(JSPC.app.convertDouble(parsedBillingCurrency * $("#newCostCurrencyRate2").val()));

            $(".la-account-currency").find(".field").removeClass("error");
            JSPC.app.calcTaxResults()
        }
    });
    $("#costButton22").click(function () {
        if (! JSPC.app.isError("#newCostInLocalCurrency2") && ! JSPC.app.isError("#newCostInBillingCurrency2")) {
            var input = $(this).siblings("input");
            input.transition('glow');
            var parsedLocalCurrency = JSPC.app.convertDouble($("#newCostInLocalCurrency2").val());
            var parsedBillingCurrency = JSPC.app.convertDouble($("#newCostInBillingCurrency2").val());
            input.val((parsedLocalCurrency / parsedBillingCurrency));

            $(".la-account-currency").find(".field").removeClass("error");
            JSPC.app.calcTaxResults()
        }
    });
    $("#costButton32").click(function () {
        if (! JSPC.app.isError("#newCostInLocalCurrency2") && ! JSPC.app.isError("#newCostCurrencyRate2")) {
            var input = $(this).siblings("input");
            input.transition('glow');
            var parsedLocalCurrency = JSPC.app.convertDouble($("#newCostInLocalCurrency2").val());
            input.val(JSPC.app.convertDouble(parsedLocalCurrency / $("#newCostCurrencyRate2").val()));

            $(".la-account-currency").find(".field").removeClass("error");
            JSPC.app.calcTaxResults()
        }
    });
    JSPC.app.isError = function (cssSel) {
        if ($(cssSel).val().length <= 0 || $(cssSel).val() < 0) {
            $(".la-account-currency").children(".field").removeClass("error");
            $(cssSel).parent(".field").addClass("error");
            return true
        }
        return false
    };

    $('.calc').on('change', function () {
        JSPC.app.calcTaxResults()
    });

    JSPC.app.calcTaxResults = function () {
        var roundF = $('*[name=newFinalCostRounding2]').prop('checked');
        console.log($("*[name=newTaxRate2]").val());
        var taxF = 1.0 + (0.01 * $("*[name=newTaxRate2]").val().split("§")[1]);

        var parsedBillingCurrency = JSPC.app.convertDouble($("#newCostInBillingCurrency2").val());
        var parsedLocalCurrency = JSPC.app.convertDouble($("#newCostInLocalCurrency2").val());

        $('#newCostInBillingCurrencyAfterTax2').val(
            roundF ? Math.round(parsedBillingCurrency * taxF) : JSPC.app.convertDouble(parsedBillingCurrency * taxF)
        );
        $('#newCostInLocalCurrencyAfterTax2').val(
            roundF ? Math.round(parsedLocalCurrency * taxF) : JSPC.app.convertDouble(parsedLocalCurrency * taxF)
        );
    };

    JSPC.app.costElems = $("#newCostInLocalCurrency2, #newCostCurrencyRate2, #newCostInBillingCurrency2");

    JSPC.app.costElems.on('change', function () {
        JSPC.app.checkValues();
        if ($("[name='newCostCurrency2']").val() != 0) {
            $("#newCostCurrency2").parent(".field").removeClass("error");
        } else {
            $("#newCostCurrency2").parent(".field").addClass("error");
        }
    });

    $("#costItemsBulk").submit(function (e) {
        e.preventDefault();
        //if ($("[name='newCostCurrency2']").val() != 0 && $("[name='newCostCurrency2']").val() != null) {
            var valuesCorrect = JSPC.app.checkValues();
            if (valuesCorrect) {
                $("#newCostCurrency2").parent(".field").removeClass("error");
                if ($("#newSubscription").hasClass('error') || $("#newPackage").hasClass('error') || $("#newIE").hasClass('error'))
                    alert("${message(code:'financials.newCosts.entitlementError')}");
                else $(this).unbind('submit').submit();
            } else {
                alert("${message(code:'financials.newCosts.calculationError')}");
            }
        //}
        /*else {
            alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
            $("#newCostCurrency2").parent(".field").addClass("error");
        }*/
    });

    $("#newCostCurrency2").change(function () {
        //console.log("event listener succeeded, picked value is: "+$(this).val());
        if ($(this).val() === JSPC.app.eurVal)
            $("#newCostCurrencyRate2").val(1.0);
        else $("#newCostCurrencyRate2").val(0.0);
        $("#costButton12").click();
    });

    JSPC.app.checkValues = function() {
        if (JSPC.app.convertDouble($("#newCostInBillingCurrency2").val()) * $("#newCostCurrencyRate2").val() !== JSPC.app.convertDouble($("#newCostInLocalCurrency2").val())) {
            JSPC.app.costElems.parent('.field').addClass('error');
            return false;
        } else {
            JSPC.app.costElems.parent('.field').removeClass('error');
            return true;
        }
    }

    JSPC.app.convertDouble = function (input) {
        console.log("input: " + input + ", typeof: " + typeof (input))
        var output;
        //determine locale from server
        var locale = "${LocaleContextHolder.getLocale()}";
        if (typeof (input) === 'number') {
            output = input.toFixed(2);
            if (locale.indexOf("de") > -1)
                output = output.replace(".", ",");
        } else if (typeof (input) === 'string') {
            output = 0.0;
            if (input.match(/(\d{1-3}\.?)*\d+(,\d{2})?/g))
                output = parseFloat(input.replace(/\./g, "").replace(/,/g, "."));
            else if (input.match(/(\d{1-3},?)*\d+(\.\d{2})?/g)) {
                output = parseFloat(input.replace(/,/g, ""));
            } else console.log("Please check over regex!");
            console.log("string input parsed, output is: " + output);
        }
        return output;
    }

</laser:script>