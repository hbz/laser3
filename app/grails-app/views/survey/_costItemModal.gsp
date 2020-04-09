<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.*;org.springframework.context.i18n.LocaleContextHolder" %>
<laser:serviceInjection/>

<g:if test="${setting == 'bulkForAll'}">
    <g:set var="modalText" value="${message(code: 'financials.addNewCostForAll') + " ("+ surveyOrgList.size()+ ") "}"/>
</g:if>
<g:else>
    <g:set var="modalText" value="${message(code: 'financials.addNewCostFor', args: [surveyOrg.org.name])}"/>
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

<semui:modal id="${modalID ?: 'modalSurveyCostItem'}" text="${modalText + (surveyOrg.surveyConfig.subscription ? ' ('+ surveyOrg.surveyConfig.subscription+ ')' : '')}" msgSave="${submitButtonLabel}">
    <g:form class="ui small form" name="editCost" action="newSurveyCostItem">

        <g:hiddenField name="shortcode" value="${contextService.getOrg().shortcode}"/>
        <g:if test="${setting == 'bulkForAll'}">
            <g:hiddenField name="surveyConfig" value="${surveyConfig.class.getName()}:${surveyConfig.id}"/>
            <g:hiddenField name="surveyOrgs" value="${surveyOrgList.join(",")}"/>
        </g:if>
        <g:else>
            <g:if test="${costItem && (mode && mode.equals("edit"))}">
                <g:hiddenField name="oldCostItem" value="${costItem.class.getName()}:${costItem.id}"/>
            </g:if>
                <g:hiddenField name="surveyOrg" value="${surveyOrg.class.getName()}:${surveyOrg.id}"/>
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
                        <label><g:message code="financials.costItemElement"/></label>
                        <g:if test="${costItemElements}">
                            <laser:select name="newCostItemElement" class="ui dropdown"
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
                        <laser:select name="ciec" class="ui dropdown"
                                      from="${costItemSigns}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${[null:message(code:'default.select.choose.label')]}"
                                      value="${costItem?.costItemElementConfiguration?.id}"/>
                    </div>
                </div>
                <div class="field">
                    <label>${message(code:'default.status.label')}</label>
                    <laser:select name="newCostItemStatus" title="${g.message(code: 'financials.addNew.costState')}" class="ui dropdown"
                                  id="newCostItemStatus"
                                  from="${costItemStatus}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${[(RDStore.GENERIC_NULL_VALUE.id):message(code:'default.select.choose.label')]}"
                                  value="${costItem?.costItemStatus?.id}" />
                </div><!-- .field -->

            </div> <!-- 2/2 field -->
        </div><!-- two fields -->

        <div class="fields">
            <fieldset class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
                <label>${g.message(code: 'financials.newCosts.amount')}</label>

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="text" class="calc" style="width:50%"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency"
                               placeholder="${g.message(code: 'financials.invoice_total')}"
                               value="<g:formatNumber
                                       number="${costItem?.costInBillingCurrency}"
                                       minFractionDigits="2" maxFractionDigits="2"/>"/>

                        <g:select class="ui dropdown dk-width-auto" name="newCostCurrency" title="${message(code: 'financials.addNew.currencyType')}"
                                  from="${currency}"
                                  optionKey="id"
                                  optionValue="${{it.text.contains('-') ? it.text.split('-').first() : it.text}}"
                                  value="${costItem?.billingCurrency?.id}" />
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.newCosts.totalAmount"/></label>
                        <input title="${g.message(code:'financials.newCosts.totalAmount')}" type="text" readonly="readonly"
                               name="newCostInBillingCurrencyAfterTax" id="newCostInBillingCurrencyAfterTax"
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


                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code:'financials.newCosts.taxTypeAndRate')}</label>
                        <g:select class="ui dropdown calc" name="newTaxRate" title="TaxRate"
                              from="${CostItem.TAX_TYPES}"
                              optionKey="${{it.taxType.class.name+":"+it.taxType.id+"§"+it.taxRate}}"
                              optionValue="${{it.display ? it.taxType.getI10n("value")+" ("+it.taxRate+"%)" : it.taxType.getI10n("value")}}"
                              value="${taxKey?.taxType?.class?.name}:${taxKey?.taxType?.id}§${taxKey?.taxRate}"
                              noSelection="${['null§0':'']}"/>

                    </div><!-- .field -->
                </div>

                <div class="field">
                    <div class="ui checkbox">
                        <label><g:message code="financials.newCosts.finalSumRounded"/></label>
                        <input name="newFinalCostRounding" class="hidden calc" type="checkbox"
                               <g:if test="${costItem?.finalCostRounding}">checked="checked"</g:if>/>
                    </div>
                </div><!-- .field -->

            </fieldset> <!-- 1/2 field |  .la-account-currency -->

        </div><!-- three fields -->


        <div class="two fields">
            <semui:datepicker label="financials.dateFrom" id="newStartDate" name="newStartDate" placeholder="default.date.label" value="${costItem?.startDate}" />

            <semui:datepicker label="financials.dateTo" id="newEndDate" name="newEndDate" placeholder="default.date.label" value="${costItem?.endDate}" />
        </div>


        <div class="one fields">
            <fieldset class="sixteen wide field la-modal-fieldset-no-margin">
                <div class="field">
                    <label>${message(code: 'survey.costItemModal.descriptionfor', args:[surveyOrg.org.name ?: 'alle'])}</label>
                    <textarea name="newDescription" id="newDescription"
                              placeholder="${message(code: 'default.description.label')}">${costItem?.costDescription}</textarea>
                </div><!-- .field -->
            </fieldset>
        </div>

    </g:form>

    <script>

        var costItemElementConfigurations = ${raw(orgConfigurations as String)};
        console.log(costItemElementConfigurations);
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

            $('#newCostInBillingCurrencyAfterTax').val(
                roundF ? Math.round(parsedBillingCurrency * taxF) : convertDouble(parsedBillingCurrency * taxF)
            );
        };

        var costElems = $("#newCostInBillingCurrency");

        costElems.on('change', function () {
            if($("[name='newCostCurrency']").val() != 0) {
                $("#newCostCurrency").parent(".field").removeClass("error");
            }
            else {
                $("#newCostCurrency").parent(".field").addClass("error");
            }
        });

        $("#editCost").submit(function(e){
            e.preventDefault();
            if($("[name='newCostCurrency']").val() != 0) {
                $(this).unbind('submit').submit();
            }
            else {
                alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
                $("#newCostCurrency").parent(".field").addClass("error");
            }
        });


        function convertDouble(input) {
            //console.log("input: "+input+", typeof: "+typeof(input));
            var output;
            //determine locale from server
            var userLang = "${contextService.user.getSettingsValue(UserSettings.KEYS.LANGUAGE,null)}";
            //console.log(userLang);
            if(typeof(input) === 'number') {
                output = input.toFixed(2);
                if(userLang !== 'en')
                    output = output.replace(".",",");
            }
            else if(typeof(input) === 'string') {
                output = 0.0;
                if(userLang === 'en') {
                    output = parseFloat(input);
                }
                else {
                    if(input.match(/(\d{1-3}\.?)*\d+(,\d{2})?/g))
                        output = parseFloat(input.replace(/\./g,"").replace(/,/g,"."));
                    else if(input.match(/(\d{1-3},?)*\d+(\.\d{2})?/g)) {
                        output = parseFloat(input.replace(/,/g, ""));
                    }
                    else console.log("Please check over regex!");
                }
                //console.log("string input parsed, output is: "+output);
            }
            return output;
        }

    </script>

</semui:modal>
<!-- _ajaxModal.gsp -->
