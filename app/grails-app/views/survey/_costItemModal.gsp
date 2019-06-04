<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.*;org.springframework.context.i18n.LocaleContextHolder" %>
<laser:serviceInjection/>

<g:render template="vars" model="[org: contextService.getOrg()]"/><%-- setting vars --%>

<g:if test="${setting == 'bulkForAll'}">
    <g:set var="modalText" value="${message(code: 'financials.addNewCostForAll')}"/>
</g:if>
<g:else>
    <g:set var="modalText" value="${message(code: 'financials.addNewCostFor', args: [surveyOrg?.org?.name])}"/>
</g:else>
<g:set var="submitButtonLabel" value="${message(code: 'default.button.create_new.label')}"/>
<g:set var="org" value="${contextService.getOrg()}"/>


<%
    if (costItem) {
        if (mode && mode.equals("edit")) {
            modalText = g.message(code: 'financials.editCostFor', args: [surveyOrg.org.name])
            submitButtonLabel = g.message(code: 'default.button.save.label')
        }
    }

%>

<semui:modal id="${modalID ?: 'modalSurveyCostItem'}" text="${modalText + (surveyOrg?.surveyConfig?.subscription ? ' ('+ surveyOrg?.surveyConfig?.subscription+ ')' : '')}" msgSave="${submitButtonLabel}">
    <g:form class="ui small form" id="editCost" action="newSurveyCostItem">

        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}"/>
        <g:if test="${setting == 'bulkForAll'}">
            <g:hiddenField name="surveyConfig" value="${surveyConfig.class.getName()}:${surveyConfig.id}"/>
        </g:if>
        <g:else>
            <g:if test="${costItem && (mode && mode.equals("edit"))}">
                <g:hiddenField name="oldCostItem" value="${costItem.class.getName()}:${costItem.id}"/>
            </g:if>
            <g:else>
                <g:hiddenField name="surveyOrg" value="${surveyOrg.class.getName()}:${surveyOrg.id}"/>
            </g:else>
        </g:else>


        <div class="fields">
            <div class="nine wide field">
                <div class="field">
                    <label>${message(code: 'financials.newCosts.costTitle')}</label>
                    <input type="text" name="newCostTitle" id="newCostTitle" value="${costItem?.costTitle}"/>
                </div><!-- .field -->

            </div>

            <div class="seven wide field">

                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label>${message(code: 'financials.costItemElement')}</label>
                        <g:if test="${costItemElement}">
                            <laser:select name="newCostItemElement" class="ui dropdown"
                                          from="${costItemElement}"
                                          optionKey="id"
                                          optionValue="value"
                                          noSelection="${['': '']}"
                                          value="${costItem?.costItemElement?.id}"/>
                        </g:if>
                        <g:else>
                            ${message(code: 'financials.costItemElement.noneDefined')}
                        </g:else>
                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code: 'financials.costItemConfiguration')}</label>
                        <%
                            def ciec = [id: null, value: 'financials.costItemConfiguration.notSet']
                            if (costItem && !tab.equals("subscr")) {
                                if (costItem.costItemElementConfiguration)
                                    ciec = costItem.costItemElementConfiguration.class.name + ":" + costItem.costItemElementConfiguration.id
                                else if (!costItem.costItemElementConfiguration && costItem.costItemElement) {
                                    def config = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(costItem.costItemElement, contextService.getOrg())
                                    if (config)
                                        ciec = config.elementSign.class.name + ":" + config.elementSign.id
                                }
                            }
                        %>
                        <g:select name="ciec" class="ui dropdown" from="${costItemElementConfigurations}"
                                  optionKey="id" optionValue="value" value="${ciec}"
                                  noSelection="${[null: message(code: 'financials.costItemConfiguration.notSet')]}"/>
                    </div>
                </div>

                <div class="field">
                    <label>${message(code: 'financials.costItemStatus')}</label>
                    <laser:select name="newCostItemStatus" title="${g.message(code: 'financials.addNew.costState')}"
                                  class="ui dropdown"
                                  id="newCostItemStatus"
                                  from="${costItemStatus}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${['': '']}"
                                  value="${costItem?.costItemStatus?.id}"/>
                </div><!-- .field -->

            </div> <!-- 2/2 field -->
        </div><!-- two fields -->

        <div class="fields">
            <fieldset class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
                <label>${g.message(code: 'financials.newCosts.amount')}</label>

                <div class="two fields">
                    <div class="field">
                        <label>${message(code: 'financials.invoice_total')}</label>
                        <input title="${g.message(code: 'financials.addNew.BillingCurrency')}" type="text" class="calc"
                               style="width:50%"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency"
                               placeholder="${g.message(code: 'financials.invoice_total')}"
                               value="<g:formatNumber
                                       number="${consCostTransfer ? costItem?.costInBillingCurrencyAfterTax : costItem?.costInBillingCurrency}"
                                       minFractionDigits="2" maxFractionDigits="2"/>"/>

                        <div class="ui icon button" id="costButton3"
                             data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}"
                             data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>

                        <g:select class="ui dropdown dk-width-auto" name="newCostCurrency"
                                  title="${g.message(code: 'financials.addNew.currencyType')}"
                                  from="${currency}"
                                  optionKey="id"
                                  optionValue="${{ (it.text.split('-')).first() }}"
                                  value="${costItem?.billingCurrency?.id}"/>
                    </div><!-- .field -->
                    <div class="field">
                        <label>Endpreis</label>
                        <input title="Rechnungssumme nach Steuer (in EUR)" type="text" readonly="readonly"
                               name="newCostInBillingCurrencyAfterTax" id="newCostInBillingCurrencyAfterTax"
                               value="<g:formatNumber
                                       number="${consCostTransfer ? 0.0 : costItem?.costInBillingCurrencyAfterTax}"
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
                        <input title="${g.message(code: 'financials.addNew.currencyRate')}" type="number" class="calc"
                               name="newCostCurrencyRate" id="newCostCurrencyRate"
                               placeholder="${g.message(code: 'financials.newCosts.exchangeRate')}"
                               value="${costItem ? costItem.currencyRate : 1.0}" step="0.000000001"/>

                        <div class="ui icon button" id="costButton2"
                             data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}"
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
                        <g:select class="ui dropdown calc" name="newTaxRate" title="TaxRate"
                                  from="${CostItem.TAX_TYPES}"
                                  optionKey="${{ it.taxType.class.name + ":" + it.taxType.id + "§" + it.taxRate }}"
                                  optionValue="${{ it.taxType.getI10n("value") + " (" + it.taxRate + "%)" }}"
                                  value="${taxKey?.taxType?.class?.name}:${taxKey?.taxType?.id}§${taxKey?.taxRate}"
                                  noSelection="${['null§0': '']}"/>

                    </div><!-- .field -->
                </div>

                <div class="two fields">
                    <div class="field">
                        <label>${g.message(code: 'financials.newCosts.valueInEuro')}</label>
                        <input title="${g.message(code: 'financials.addNew.LocalCurrency')}" type="text" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency"
                               placeholder="${message(code: 'financials.newCosts.valueInEuro')}"
                               value="<g:formatNumber
                                       number="${consCostTransfer ? costItem?.costInLocalCurrencyAfterTax : costItem?.costInLocalCurrency}"
                                       minFractionDigits="2" maxFractionDigits="2"/>"/>

                        <div class="ui icon button" id="costButton1"
                             data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}"
                             data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label>Endpreis (in EUR)</label>
                        <input title="Wert nach Steuer (in EUR)" type="text" readonly="readonly"
                               name="newCostInLocalCurrencyAfterTax" id="newCostInLocalCurrencyAfterTax"
                               value="<g:formatNumber
                                       number="${consCostTransfer ? 0.0 : costItem?.costInLocalCurrencyAfterTax}"
                                       minFractionDigits="2" maxFractionDigits="2"/>"/>
                    </div><!-- .field -->
                </div>

                <div class="field">
                    <div class="ui checkbox">
                        <label>Finalen Preis runden</label>
                        <input name="newFinalCostRounding" class="hidden calc" type="checkbox"
                            <g:if test="${costItem?.finalCostRounding}">checked="checked"</g:if>/>
                    </div>
                </div><!-- .field -->

            </fieldset> <!-- 1/2 field |  .la-account-currency -->

        </div><!-- three fields -->

        <div class="one fields">
            <fieldset class="sixteen wide field la-modal-fieldset-no-margin">
                <div class="field">
                    <label>${message(code: 'survey.costItemModal.descriptionfor', args:[surveyOrg?.org?.name ?: 'alle'])}</label>
                    <textarea name="newDescription" id="newDescription"
                              placeholder="${message(code: 'default.description.label')}">${costItem?.costDescription}</textarea>
                </div><!-- .field -->
            </fieldset>
        </div>

    </g:form>

    <script>

        <%
            def costItemElementConfigurations = "{"
            StringJoiner sj = new StringJoiner(",")
            orgConfigurations.each { orgConf ->
                sj.add('"'+orgConf.id+'":"'+orgConf.value+'"')
            }
            costItemElementConfigurations += sj.toString()+"}"
        %>
        var costItemElementConfigurations = ${raw(costItemElementConfigurations)};

        var eurVal = "${RefdataValue.getByValueAndCategory('EUR','Currency').id}";

        $("#newCostInBillingCurrency").change(function(){
            var currencyEUR = ${RefdataValue.getByValueAndCategory('EUR','Currency').id};
            if($("#newCostCurrency").val() == currencyEUR) {
                $("#costButton1").click();
            }
        });

        $("#costButton1").click(function () {
            if (!isError("#newCostInBillingCurrency") && !isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency").val());
                input.val(convertDouble(parsedBillingCurrency * $("#newCostCurrencyRate").val()));

                $(".la-account-currency").find(".field").removeClass("error");
                calcTaxResults()
            }
        });
        $("#costButton2").click(function () {
            if (!isError("#newCostInLocalCurrency") && !isError("#newCostInBillingCurrency")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency").val());
                var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency").val());
                input.val((parsedLocalCurrency / parsedBillingCurrency));

                $(".la-account-currency").find(".field").removeClass("error");
                calcTaxResults()
            }
        });
        $("#costButton3").click(function () {
            if (!isError("#newCostInLocalCurrency") && !isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency").val());
                input.val(convertDouble(parsedLocalCurrency / $("#newCostCurrencyRate").val()));

                $(".la-account-currency").find(".field").removeClass("error");
                calcTaxResults()
            }
        });
        $("#newCostItemElement").change(function () {
            if (typeof(costItemElementConfigurations[$(this).val()]) !== 'undefined')
                $("[name='ciec']").dropdown('set selected', costItemElementConfigurations[$(this).val()]);
            else
                $("[name='ciec']").dropdown('set selected', 'null');
        });
        var isError = function (cssSel) {
            if ($(cssSel).val().length <= 0 || $(cssSel).val() < 0) {
                $(".la-account-currency").children(".field").removeClass("error");
                $(cssSel).parent(".field").addClass("error");
                return true
            }
            return false
        };

        $('.calc').on('change', function () {
            calcTaxResults()
        });

        var calcTaxResults = function () {
            var roundF = $('*[name=newFinalCostRounding]').prop('checked');
            console.log($("*[name=newTaxRate]").val());
            var taxF = 1.0 + (0.01 * $("*[name=newTaxRate]").val().split("§")[1]);

            var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency").val());
            var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency").val());

            $('#newCostInBillingCurrencyAfterTax').val(
                roundF ? Math.round(parsedBillingCurrency * taxF) : convertDouble(parsedBillingCurrency * taxF)
            );
            $('#newCostInLocalCurrencyAfterTax').val(
                roundF ? Math.round(parsedLocalCurrency * taxF) : convertDouble(parsedLocalCurrency * taxF)
            );
        };

        var costElems = $("#newCostInLocalCurrency, #newCostCurrencyRate, #newCostInBillingCurrency");

        costElems.on('change', function () {
            checkValues();
        });


        $("#newCostCurrency").change(function () {
            //console.log("event listener succeeded, picked value is: "+$(this).val());
            if ($(this).val() === eurVal)
                $("#newCostCurrencyRate").val(1.0);
            else $("#newCostCurrencyRate").val(0.0);
            $("#costButton1").click();
        });


        function checkValues() {
            if (convertDouble($("#newCostInBillingCurrency").val()) * $("#newCostCurrencyRate").val() !== convertDouble($("#newCostInLocalCurrency").val())) {
                costElems.parent('.field').addClass('error');
                return false;
            }
            else {
                costElems.parent('.field').removeClass('error');
                return true;
            }
        }

        function convertDouble(input) {
            console.log("input: " + input + ", typeof: " + typeof(input))
            var output;
            //determine locale from server
            var locale = "${LocaleContextHolder.getLocale()}";
            if (typeof(input) === 'number') {
                output = input.toFixed(2);
                if (locale.indexOf("de") > -1)
                    output = output.replace(".", ",");
            }
            else if (typeof(input) === 'string') {
                output = 0.0;
                if (input.match(/(\d{1-3}\.?)*\d+(,\d{2})?/g))
                    output = parseFloat(input.replace(/\./g, "").replace(/,/g, "."));
                else if (input.match(/(\d{1-3},?)*\d+(\.\d{2})?/g)) {
                    output = parseFloat(input.replace(/,/g, ""));
                }
                else console.log("Please check over regex!");
                console.log("string input parsed, output is: " + output);
            }
            return output;
        }

    </script>

</semui:modal>
<!-- _ajaxModal.gsp -->
