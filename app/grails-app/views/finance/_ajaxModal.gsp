<!-- _editAjax.gsp -->
<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:render template="vars" /><%-- setting vars --%>

<g:set var="modalText" value="${message(code:'financials.addNewCost')}" />

<g:if test="${costItem}">
    <g:set var="modalText" value="${message(code:'financials.editCost')} #${costItem?.id}" />
</g:if>

<semui:modal id="costItem_ajaxModal" text="${modalText}">
    <g:form class="ui small form" id="editCost" url="[controller:'finance', action:'newCostItem']">

        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}" />
        <g:if test="${costItem}">
            <g:hiddenField name="oldCostItem" value="${costItem.class.getName()}:${costItem.id}" />
        </g:if>

        <!-- DEBUG ${inSubMode} ${fixedSubscription} -->

        <div class="two fields">
            <div class="field">
                <div class="field">
                    <label>${message(code:'financials.newCosts.costTitle')}</label>
                    <input type="text" name="newCostTitle" id="newCostTitle" value="${costItem?.costTitle}" />
                </div><!-- .field -->

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.budgetCode')}</label>
                        <input type="text" name="newBudgetCode" id="newBudgetCode" class="select2 la-full-width" placeholder=""/>
                    </div><!-- .field -->

                    <div class="field">
                        <label>Reference/Codes</label>
                        <input type="text" name="newReference" id="newCostItemReference" placeholder="" value="${costItem?.reference}"/>
                    </div><!-- .field -->
                </div>
            </div>
            <div class="field">
                <div class="field ">
                    <label>${message(code:'financials.newCosts.UID')}</label>
                    <input type="text" readonly value="${costItem?.globalUID}" />
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.costItemStatus')}</label>
                    <laser:select name="newCostItemStatus" title="${g.message(code: 'financials.addNew.costState')}" class="ui dropdown"
                                  id="newCostItemStatus"
                                  from="${costItemStatus}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${['':'']}"
                                  value="${costItem?.costItemStatus?.id}" />
                </div><!-- .field -->
            </div>
        </div><!-- two fields -->

        <div class="three fields">
            <fieldset class="field la-modal-fieldset-no-margin la-account-currency">
                <label>${g.message(code:'financials.newCosts.amount')}</label>

                    <div class="field">
                        <label>${g.message(code:'financials.newCosts.valueInEuro')}</label>
                        <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="number" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency"
                               placeholder="${message(code:'financials.newCosts.valueInEuro')}" value="${costItem?.costInLocalCurrency}" step="0.01"/>

                        <div class="ui icon button" id="costButton1" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="right center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->

                    <div class="field la-exchange-rate">
                        <label>${g.message(code:'financials.newCosts.exchangeRate')}</label>
                        <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc"
                               name="newCostCurrencyRate" id="newCostCurrencyRate"
                               placeholder="${g.message(code:'financials.newCosts.exchangeRate')}" value="${costItem?.currencyRate}" step="0.000000001" />

                        <div class="ui icon button" id="costButton2" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="right center" data-variation="tiny">
                        <i class="calculator icon"></i>
                    </div>
                    </div><!-- .field -->

                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="number" class="calc"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency"
                               placeholder="${g.message(code:'financials.invoice_total')}" value="${costItem?.costInBillingCurrency}" step="0.01"/>

                        <div class="ui icon button" id="costButton3" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="right center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->

                    <div class="field">
                        <g:select class="ui dropdown" name="newCostCurrency" title="${g.message(code: 'financials.addNew.currencyType')}"
                                  from="${currency}"
                                  optionKey="id"
                                  optionValue="text"
                                  value="${costItem?.billingCurrency?.id}" />
                    </div><!-- .field -->

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
                                  noSelection="${['':'']}"
                                  value="${costItem?.costItemCategory?.id}" />
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.costItemElement')}</label>
                    <laser:select name="newCostItemElement" class="ui dropdown"
                                  from="${costItemElement}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${['':'']}"
                                  value="${costItem?.costItemElement?.id}" />
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.newCosts.controllable')}</label>
                    <laser:select name="newCostTaxType" title="${g.message(code: 'financials.addNew.taxCateogry')}" class="ui dropdown"
                                  from="${taxType}"
                                  optionKey="id"
                                  optionValue="value"
                                  noSelection="${['':'']}"
                                  value="${costItem?.taxCode?.id}" />
                </div><!-- .field -->
            </fieldset> <!-- 2/3 field -->

            <fieldset class="field la-modal-fieldset-no-margin">
                <label>${message(code:'financials.newCosts.constsReferenceOn')}</label>

                <div class="field">
                    <label>${message(code:'subscription.label')}</label>

                    <input ${inSubMode ? "disabled='disabled' data-filterMode='${fixedSubscription?.class.getName()}:${fixedSubscription?.id}'" : '' }
                            name="newSubscription" id="newSubscription"
                            class="la-full-width select2"
                            data-subfilter=""
                            placeholder="${message(code:'financials.newCosts.newLicence')}"
                            ${inSubMode ? " value='${fixedSubscription?.name}' " : " value='${params.newSubscription}' "}
                             />
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'package.label')}</label>
                    <g:if test="${! inSubMode}">
                        <input name="newPackage" id="newPackage" class="la-full-width" disabled='disabled' data-subFilter="" data-disableReset="true" />
                    </g:if>
                    <g:else>
                        <input name="newPackage" id="newPackage" class="select2 la-full-width" data-subFilter="${fixedSubscription?.id}" data-disableReset="true" />
                    </g:else>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                    <g:if test="${! inSubMode}">
                        <input name="newIe" id="newIE" disabled='disabled' data-subFilter="" data-disableReset="true" class="la-full-width" value="${params.newIe}">
                    </g:if>
                    <g:else>
                        <input name="newIe" id="newIE" disabled="disabled" data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="select2 la-full-width" value="${params.newIe}">
                    </g:else>
                </div><!-- .field -->
            </fieldset> <!-- 3/3 field -->

        </div><!-- three fields -->

        <div class="three fields">
            <fieldset class="field la-modal-fieldset-no-margin">
                <semui:datepicker label="financials.datePaid" name="newDatePaid" placeholder="financials.datePaid" value="${costItem?.datePaid}" />

                <semui:datepicker label="financials.dateFrom" name="newStartDate" placeholder="default.date.label" value="${costItem?.startDate}" />

                <semui:datepicker label="financials.dateTo" name="newEndDate" placeholder="default.date.label" value="${costItem?.endDate}" />
            </fieldset> <!-- 1/3 field -->

            <fieldset class="field la-modal-fieldset-margin">
                <div class="field">
                    <label>${message(code:'financials.newCosts.description')}</label>
                    <textarea name="newDescription" id="newDescription" placeholder="${message(code:'default.description.label')}">${costItem?.costDescription}</textarea>
                </div><!-- .field -->
            </fieldset> <!-- 2/3 field -->

            <fieldset class="field la-modal-fieldset-no-margin">
                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <input type="text" name="newInvoiceNumber" id="newInvoiceNumber"
                           placeholder="${message(code:'financials.invoice_number')}" value="${costItem?.invoice?.invoiceNumber}"/>
                </div><!-- .field -->

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <input type="text" name="newOrderNumber" id="newOrderNumber"
                           placeholder="${message(code:'financials.order_number')}" value="${costItem?.order?.orderNumber}"/>
                </div><!-- .field -->
            </fieldset> <!-- 3/3 field -->

        </div><!-- three fields -->

    </g:form>

    <script type="text/javascript">
        $("#costButton1").click(function() {
            if (! isError("#newCostInBillingCurrency") && ! isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInBillingCurrency").val() / $("#newCostCurrencyRate").val()).toFixed(2));

                $(".la-account-currency").children(".field").removeClass("error");
            }
        })
        $("#costButton2").click(function() {
            if (! isError("#newCostInLocalCurrency") && ! isError("#newCostInBillingCurrency")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInBillingCurrency").val() / $("#newCostInLocalCurrency").val()).toFixed(9));

                $(".la-account-currency").children(".field").removeClass("error");
            }
        })
        $("#costButton3").click(function() {
            if (! isError("#newCostInLocalCurrency") && ! isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInLocalCurrency").val() * $("#newCostCurrencyRate").val()).toFixed(2));

                $(".la-account-currency").children(".field").removeClass("error");
            }
        });
        var isError = function(cssSel)  {
            if ($(cssSel).val().length <= 0 || $(cssSel).val() < 0) {
                $(".la-account-currency").children(".field").removeClass("error");
                $(cssSel).parent(".field").addClass("error");
                return true
            }
            return false
        }

        var ajaxPostFunc = function () {

            $('#newCostCurrency').dropdown('setting', 'onChange', function(value, text, $selectedItem) {
                $('#newCostCurrency').dropdown('set text', text.split('-')[0].trim());
            }).dropdown('change');

            $('#costItem_ajaxModal #newBudgetCode').select2({
                minimumInputLength: 1,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note')}";
                },
                initSelection: function(element, callback) {
                    console.log( "GSP: _ajaxModal > JS: initSelection")
                    /*
                    $.ajax({
                        url: "<g:createLink controller='ajax' action='lookup'/>",
                        data: {
                            format: 'json',
                            q:'',
                            shortcode: "${contextService.getOrg()?.shortcode}",
                            baseClass: 'com.k_int.kbplus.CostItemGroup'
                            },
                        dataType: 'json',
                        results: function (data, page) {
                            return {results: data.values};
                        }
                    }).done( function(data) {
                        callback(data)
                    })
                    */
                },
                allowClear: true,
                tags: true,
                tokenSeparators: [',', ' '],
                ajax: {
                    url: "<g:createLink controller='ajax' action='lookup'/>",
                    dataType: 'json',
                    data: function (term, page) {
                        return {
                            format: 'json',
                            q: term,
                            shortcode: "${contextService.getOrg()?.shortcode}",
                            baseClass: 'com.k_int.kbplus.CostItemGroup'
                        };
                    },
                    results: function (data, page) {
                        return {results: data.values};
                    }
                },
                createSearchChoice: function(term, data) {
                    var existsAlready = false;
                    for (var i = 0; i < data.length; i++) {
                        if(term.toLowerCase() == data[i].text.toLowerCase()) {
                            existsAlready = true;
                            break;
                        }
                    }
                    if(! existsAlready)
                        return {id: -1 + term, text: "${message(code: 'default.newValue.label')}: " + term};
                }
            })

            <g:if test="${! inSubMode}">

            $('#costItem_ajaxModal #newSubscription').select2({
                placeholder: "${message(code:'financials.newCosts.enterSubName')}",
                minimumInputLength: 1,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note')}";
                },
                global: false,
                ajax: {
                    url: "<g:createLink controller='ajax' action='lookup'/>",
                    dataType: 'json',
                    data: function (term, page) {
                        return {
                            hideDeleted: 'true',
                            hideIdent: 'false',
                            inclSubStartDate: 'false',
                            inst_shortcode: "${contextService.getOrg()?.shortcode}",
                            q: '%' + term , // contains search term
                            page_limit: 20,
                            baseClass:'com.k_int.kbplus.Subscription'
                        };
                    },
                    results: function (data, page) {
                        return {results: data.values};
                    }
                },
                allowClear: true,
                formatSelection: function(data) {
                    return data.text;
                }
            });

            </g:if>

            <g:if test="${inSubMode}">

            $('#costItem_ajaxModal #newPackage').select2({
                placeholder: "${message(code:'financials.newCosts.enterPkgName')}",
                minimumInputLength: 1,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note')}";
                },
                global: false,
                ajax: {
                    url: "<g:createLink controller='ajax' action='lookup'/>",
                    dataType: 'json',
                    data: function (term, page) {
                        return {
                            hideDeleted: 'true',
                            hideIdent: 'false',
                            inclSubStartDate: 'false',
                            inst_shortcode: "${contextService.getOrg()?.shortcode}",
                            q: '%' + term , // contains search term
                            page_limit: 20,
                            subFilter:$(s.ft.filterSubscription).data().filtermode.split(":")[1],
                            baseClass:'com.k_int.kbplus.SubscriptionPackage'
                        };
                    },
                    results: function (data, page) {
                        return {results: data.values};
                    }
                },
                allowClear: true,
                formatSelection: function(data) {
                    return data.text;
                }
            });

            </g:if>
        }
    </script>

</semui:modal>
<!-- _editAjax.gsp -->
