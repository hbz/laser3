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

        <!--
        Sub.Mode: ${inSubMode}
        Ctx.Sub: ${fixedSubscription}
        CI.Sub: ${costItem?.sub}
        CI.SubPkg: ${costItem?.subPkg}
        -->

        <div class="three fields">
            <div class="field">
                <div class="field">
                    <label>${message(code:'financials.newCosts.costTitle')}</label>
                    <input type="text" name="newCostTitle" id="newCostTitle" value="${costItem?.costTitle}" />
                </div><!-- .field -->

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.budgetCode')}</label>
                        <input type="text" name="newBudgetCode" id="newBudgetCode" class="select2 la-full-width"
                               placeholder="${com.k_int.kbplus.CostItemGroup.findByCostItem(costItem)?.budgetCode?.value}"/>
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
            <div class="field">
                <%--
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
                --%>
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
            </div> <!-- 3/3 field -->
        </div><!-- three fields -->

        <div class="fields">
            <fieldset class="twelve wide field la-modal-fieldset-margin-right la-account-currency">
                <label>${g.message(code:'financials.newCosts.amount')}</label>
                <div class="two fields">
                    <div class="field">
                        <label>${g.message(code:'financials.newCosts.valueInEuro')}</label>
                        <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="number" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency"
                               placeholder="${message(code:'financials.newCosts.valueInEuro')}" value="${costItem?.costInLocalCurrency}" step="0.01"/>

                        <div class="ui icon button" id="costButton1" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->

                    <div class="field la-exchange-rate">
                        <label>${g.message(code:'financials.newCosts.exchangeRate')}</label>
                        <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc"
                               name="newCostCurrencyRate" id="newCostCurrencyRate"
                               placeholder="${g.message(code:'financials.newCosts.exchangeRate')}" value="${costItem?.currencyRate}" step="0.000000001" />

                        <div class="ui icon button" id="costButton2" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                </div>
                <div class="two fields">

                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="number" class="calc"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency"
                               placeholder="${g.message(code:'financials.invoice_total')}" value="${costItem?.costInBillingCurrency}" step="0.01"/>

                        <div class="ui icon button" id="costButton3" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->

                    <div class="field">
                        <label>&nbsp;</label>
                        <g:select class="ui dropdown la-currency" name="newCostCurrency" title="${g.message(code: 'financials.addNew.currencyType')}"
                                  from="${currency}"
                                  optionKey="id"
                                  optionValue="text"
                                  value="${costItem?.billingCurrency?.id}" />
                    </div><!-- .field -->
                </div>
            </fieldset> <!-- 1/2 field -->



            <fieldset class="four wide field la-modal-fieldset-no-margin">
                <label>${message(code:'financials.newCosts.constsReferenceOn')}</label>

                <div class="field">
                    <label>${message(code:'subscription.label')}</label>

                    <g:if test="${costItem?.sub}">
                        <input class="la-full-width"
                               readonly='readonly'
                               value="${costItem.sub.getName()}" />
                        <input name="newSubscription"
                               type="hidden"
                               value="${'com.k_int.kbplus.Subscription:' + costItem.sub.id}" />
                    </g:if>
                    <g:else>
                        <g:if test="${inSubMode}">
                            <input class="la-full-width"
                                   readonly='readonly'
                                   value="${fixedSubscription?.getName()}" />
                            <input name="newSubscription"
                                   type="hidden"
                                   value="${'com.k_int.kbplus.Subscription:' + fixedSubscription?.id}" />
                        </g:if>
                        <g:else>
                            <input name="newSubscription" id="newSubscription" class="la-full-width select2"
                                   data-filterMode="${'com.k_int.kbplus.Subscription:' + fixedSubscription?.id}"
                                   data-subfilter=""
                                   placeholder="${message(code:'financials.newCosts.newLicence')}" />
                        </g:else>
                    </g:else>
                </div><!-- .field -->

                <div class="field">
                    <g:if test="${costItem?.sub}">
                        <label>${message(code:'package.label')}</label>
                        <g:select name="newPackage" id="newPackage" class="ui dropdown"
                                  from="${[{}] + costItem?.sub?.packages}"
                                  optionValue="${{it?.pkg?.name ?: 'Keine Verknüpfung'}}"
                                  optionKey="${{"com.k_int.kbplus.SubscriptionPackage:" + it?.id}}"
                                  noSelection="['':'']"
                                  value="${'com.k_int.kbplus.SubscriptionPackage:' + costItem?.subPkg?.id}" />
                    </g:if>
                    <g:elseif test="${inSubMode}">
                        <label>${message(code:'package.label')}</label>
                        <g:select name="newPackage" id="newPackage" class="ui dropdown"
                                  from="${[{}] + fixedSubscription?.packages}"
                                  optionValue="${{it?.pkg?.name ?: 'Keine Verknüpfung'}}"
                                  optionKey="${{"com.k_int.kbplus.SubscriptionPackage:" + it?.id}}"
                                  noSelection="['':'']"
                                  value="${'com.k_int.kbplus.SubscriptionPackage:' + costItem?.subPkg?.id}" />
                    </g:elseif>
                    <g:else>
                        <input name="newPackage" id="newPackage" class="la-full-width" disabled="disabled" data-subFilter="" data-disableReset="true" />
                    </g:else>

                </div><!-- .field -->

                <div class="field">
                    <%--
                    <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                    <g:if test="${! inSubMode}">
                        <input name="newIe" id="newIE" disabled='disabled' data-subFilter="" data-disableReset="true" class="la-full-width" value="${params.newIe}">
                    </g:if>
                    <g:else>
                        <input name="newIe" id="newIE" disabled="disabled" data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="select2 la-full-width" value="${params.newIe}">
                    </g:else>
                    --%>
                </div><!-- .field -->
            </fieldset> <!-- 2/2 field -->

        </div><!-- three fields -->

        <div class="three fields">
            <fieldset class="field la-modal-fieldset-no-margin">
                <semui:datepicker label="financials.datePaid" name="newDatePaid" placeholder="financials.datePaid" value="${costItem?.datePaid}" />
                <div class="two fields">
                    <semui:datepicker label="financials.dateFrom" name="newStartDate" placeholder="default.date.label" value="${costItem?.startDate}" />

                    <semui:datepicker label="financials.dateTo" name="newEndDate" placeholder="default.date.label" value="${costItem?.endDate}" />
                </div>
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
        /*var costSelectors = {
            lc:   "#newCostInLocalCurrency",
            rate: "#newCostCurrencyRate",
            bc:   "#newCostInBillingCurrency"
        }*/

        $("#costButton1").click(function() {
            if (! isError("#newCostInBillingCurrency") && ! isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInBillingCurrency").val() / $("#newCostCurrencyRate").val()).toFixed(2));

                $(".la-account-currency").find(".field").removeClass("error");
            }
        })
        $("#costButton2").click(function() {
            if (! isError("#newCostInLocalCurrency") && ! isError("#newCostInBillingCurrency")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInBillingCurrency").val() / $("#newCostInLocalCurrency").val()).toFixed(9));

                $(".la-account-currency").find(".field").removeClass("error");
            }
        })
        $("#costButton3").click(function() {
            if (! isError("#newCostInLocalCurrency") && ! isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInLocalCurrency").val() * $("#newCostCurrencyRate").val()).toFixed(2));

                $(".la-account-currency").find(".field").removeClass("error");
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

        var costElems = $("#newCostInLocalCurrency, #newCostCurrencyRate, #newCostInBillingCurrency")

        costElems.on('change', function(){
            if ( $("#newCostInLocalCurrency").val() * $("#newCostCurrencyRate").val() != $("#newCostInBillingCurrency").val() ) {
                costElems.parent('.field').addClass('error')
            }
            else {
                costElems.parent('.field').removeClass('error')
            }
        })

        var ajaxPostFunc = function () {

            console.log( "ajaxPostFunc")
            /*
            $('#newCostCurrency').dropdown('setting', 'onChange', function(value, text, $selectedItem) {
                $('#newCostCurrency').dropdown('set text', text.split('-')[0].trim());
            }).dropdown('change');
            */

            $('#costItem_ajaxModal #newBudgetCode').select2({
                minimumInputLength: 0,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note')}";
                },
                allowClear: true,
                /*
                tags: true,
                tokenSeparators: [',', ' '],
                */
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
                        return {results:
                            data.values};
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

            /*
            $.ajax({
                url: "<g:createLink controller='ajax' action='lookup'/>",
                dataType: 'json',
                data: {
                    format: 'json',
                    shortcode: "${contextService.getOrg()?.shortcode}",
                    baseClass: 'com.k_int.kbplus.CostItemGroup'
                },
                success: function(data) {
                    $('#costItem_ajaxModal #newBudgetCode').select2({
                        tags: data.values
                    })
                    for (var i = 0; i < data.values.length; i++) {
                        $('#costItem_ajaxModal #newBudgetCode').val(data.values[i].id).trigger('change')
                    }
                }
            });
*/

/*
            $('#s2test').select2({
                tags: [{id:123, text: 'blah'},{id:36, text: 'blubb'}]
            })
            $('#s2test').val('123', '36').trigger('change')
*/

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
        }
    </script>

</semui:modal>
<!-- _editAjax.gsp -->
