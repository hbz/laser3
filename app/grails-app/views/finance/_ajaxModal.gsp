<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.*;" %>
<laser:serviceInjection />

<g:render template="vars" /><%-- setting vars --%>

<g:set var="modalText" value="${message(code:'financials.addNewCost')}" />
<g:set var="org" value="${contextService.getOrg()}" />

<%
    if (costItem) {
        modalText = g.message(code: 'financials.editCost')

        def subscriberExists = OrgRole.findBySubAndRoleType(costItem.sub, RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role'));
        if ( subscriberExists ) {
            modalText = subscriberExists.org?.toString()
        }
    }
%>

<semui:modal id="costItem_ajaxModal" text="${modalText}">
    <g:if test="${costItem?.globalUID}">
        <g:if test="${costItem?.isVisibleForSubscriber}">
            <div class="ui orange ribbon label">
                <strong>${message(code:'financials.isVisibleForSubscriber')}</strong>
            </div>
        </g:if>
        <g:else><%--
            <div class="ui blue ribbon label">
                <strong>${message(code:'financials.newCosts.UID')}: </strong>${costItem?.globalUID}
            </div>--%>
        </g:else>
    </g:if>
    <g:form class="ui small form" id="editCost" url="${formUrl}">

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

        <div class="fields">
            <div class="nine wide field">
                <g:if test="${OrgRole.findBySubAndOrgAndRoleType(fixedSubscription, contextService.getOrg(), RefdataValue.getByValueAndCategory('Subscription Consortia', 'Organisational Role')) || OrgRole.findBySubAndOrgAndRoleType(currentSubscription, contextService.getOrg(), RefdataValue.getByValueAndCategory('Subscription Consortia', 'Organisational Role')) }">
                    <div class="two fields la-fields-no-margin-button">
                        <div class="field">
                            <label>${message(code:'financials.newCosts.costTitle')}</label>
                            <input type="text" name="newCostTitle" id="newCostTitle" value="${costItem?.costTitle}" />
                        </div><!-- .field -->
                        <div class="field">
                            <label>${message(code:'financials.isVisibleForSubscriber')}</label>
                            <g:set var="newIsVisibleForSubscriberValue" value="${costItem?.isVisibleForSubscriber ? RefdataValue.getByValueAndCategory('Yes', 'YN').id : RefdataValue.getByValueAndCategory('No', 'YN').id}" />
                            <laser:select name="newIsVisibleForSubscriber" class="ui dropdown"
                                      id="newIsVisibleForSubscriber"
                                      from="${RefdataValue.findAllByOwner(RefdataCategory.findByDesc('YN'))}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${['':'']}"
                                      value="${newIsVisibleForSubscriberValue}" />
                        </div><!-- .field -->
                    </div>
                </g:if>
                <g:else>
                    <div class="field">
                        <label>${message(code:'financials.newCosts.costTitle')}</label>
                        <input type="text" name="newCostTitle" id="newCostTitle" value="${costItem?.costTitle}" />
                    </div><!-- .field -->
                </g:else>

                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label>${message(code:'financials.budgetCode')}</label>
                        <select name="newBudgetCodes" class="ui fluid search dropdown" multiple="multiple">
                            <g:each in="${BudgetCode.findAllByOwner(contextService.getOrg())}" var="bc">
                                <g:if test="${costItem?.getBudgetcodes()?.contains(bc)}">
                                    <option selected="selected" value="${bc.class.name}:${bc.id}">${bc.value}</option>
                                </g:if>
                                <g:else>
                                    <option value="${bc.class.name}:${bc.id}">${bc.value}</option>
                                </g:else>
                            </g:each>
                        </select>
                        <%--
                        <input type="text" name="newBudgetCode" id="newBudgetCode" class="select2 la-full-width"
                               placeholder="${CostItemGroup.findByCostItem(costItem)?.budgetCode?.value}"/>
                               --%>

                    </div><!-- .field -->

                    <div class="field">
                        <label>Reference/Codes</label>
                        <input type="text" name="newReference" id="newCostItemReference" placeholder="" value="${costItem?.reference}"/>
                    </div><!-- .field -->
                </div>

            </div>

            <div class="seven wide field">
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
                <div class="two fields">
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
                        <label>${message(code:'financials.costItemConfiguration')}</label>
                        <%
                            def ciec = null
                            if(costItem) {
                                if(costItem.costItemElementConfiguration)
                                    ciec = costItem.costItemElementConfiguration.class.name+":"+costItem.costItemElementConfiguration.id
                                else if(!costItem.costItemElementConfiguration && costItem.costItemElement) {
                                    def config = CostItemElementConfiguration.findByCostItemElementAndForOrganisation(costItem.costItemElement,contextService.getOrg())
                                    if(config)
                                        ciec = config.elementSign.class.name+":"+config.elementSign.id
                                }
                            }
                        %>
                        <g:select name="ciec" class="ui dropdown" from="${costItemElementConfigurations}"
                        optionKey="id" optionValue="value" value="${ciec}"
                        noSelection="${[null:message(code:'financials.costItemConfiguration.notSet')]}"/>
                    </div>
                </div>


                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label>${message(code:'financials.newCosts.controllable')}</label>
                        <laser:select name="newCostTaxType" title="${g.message(code: 'financials.addNew.taxCateogry')}" class="ui dropdown"
                                      from="${taxType}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${['':'']}"
                                      value="${costItem?.taxCode?.id}" />
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
                </div><!-- .fields two -->

            </div> <!-- 2/2 field -->
        </div><!-- two fields -->

        <div class="fields">
            <fieldset class="nine wide field la-modal-fieldset-margin-right la-account-currency">
                <label>${g.message(code:'financials.newCosts.amount')}</label>

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="number" class="calc" style="width:50%"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency"
                               placeholder="${g.message(code:'financials.invoice_total')}"
                               value="${costItem?.costInBillingCurrency}" step="0.01"/>

                        <div class="ui icon button" id="costButton3" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>

                        <g:select class="ui dropdown dk-width-auto" name="newCostCurrency" title="${g.message(code: 'financials.addNew.currencyType')}"
                                  from="${currency}"
                                  optionKey="id"
                                  optionValue="${{(it.text.split('-')).first()}}"
                                  value="${costItem?.billingCurrency?.id}" />
                    </div><!-- .field -->
                    <div class="field">
                        <label>Endpreis</label>
                        <input title="Rechnungssumme nach Steuer (in EUR)" type="text" readonly="readonly"
                               name="newCostInBillingCurrencyAfterTax" id="newCostInBillingCurrencyAfterTax"
                               value="${costItem?.costInBillingCurrencyAfterTax}" step="0.01"/>

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
                        <label>${g.message(code:'financials.newCosts.exchangeRate')}</label>
                        <input title="${g.message(code:'financials.addNew.currencyRate')}" type="number" class="calc"
                               name="newCostCurrencyRate" id="newCostCurrencyRate"
                               placeholder="${g.message(code:'financials.newCosts.exchangeRate')}"
                               value="${costItem?.currencyRate}" step="0.000000001" />

                        <div class="ui icon button" id="costButton2" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label>Steuersatz (in %)</label>
                        <g:select class="ui dropdown calc" name="newTaxRate" title="TaxRate"
                              from="${CostItem.TAX_RATES}"
                              optionKey="${{it}}"
                              optionValue="${{it}}"
                              value="${costItem?.taxRate}" />

                    </div><!-- .field -->
                </div>

                <div class="two fields">
                    <div class="field">
                        <label>${g.message(code:'financials.newCosts.valueInEuro')}</label>
                        <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="number" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency"
                               placeholder="${message(code:'financials.newCosts.valueInEuro')}"
                               value="${costItem?.costInLocalCurrency}" step="0.01"/>

                        <div class="ui icon button" id="costButton1" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label>Endpreis (in EUR)</label>
                        <input title="Wert nach Steuer (in EUR)" type="text" readonly="readonly"
                               name="newCostInLocalCurrencyAfterTax" id="newCostInLocalCurrencyAfterTax"
                               value="${costItem?.costInLocalCurrencyAfterTax}" step="0.01"/>
                    </div><!-- .field -->
                </div>

                <div class="field">
                    <div class="ui checkbox">
                        <label>Finalen Preis runden</label>
                        <input name="newFinalCostRounding" class="hidden calc" type="checkbox"
                               <g:if test="${costItem?.finalCostRounding}"> checked="checked" </g:if>
                        />
                    </div>
                </div><!-- .field -->
            </fieldset> <!-- 1/2 field |  .la-account-currency -->

            <fieldset class="seven wide field la-modal-fieldset-no-margin">
                <label>${message(code:'financials.newCosts.constsReferenceOn')}</label>

                <div class="field">
                    <label>${message(code:'subscription.label')}</label>

                    <g:if test="${costItem?.sub}">
                        <input class="la-full-width la-select2-fixed-width"
                               readonly='readonly'
                               value="${costItem.sub.getName()}" />
                        <input name="newSubscription" id="pickedSubscription"
                               type="hidden"
                               value="${'com.k_int.kbplus.Subscription:' + costItem.sub.id}" />
                    </g:if>
                    <g:else>
                        <g:if test="${inSubMode}">
                            <input class="la-full-width la-select2-fixed-width"
                                   readonly='readonly'
                                   value="${fixedSubscription?.getName()}" />
                            <input name="newSubscription" id="pickedSubscription"
                                   type="hidden"
                                   value="${'com.k_int.kbplus.Subscription:' + fixedSubscription?.id}" />
                        </g:if>
                        <g:else>
                            <input name="newSubscription" id="newSubscription" class="la-full-width select2 la-select2-fixed-width"
                                   data-filterMode="${'com.k_int.kbplus.Subscription:' + fixedSubscription?.id}"
                                   data-subfilter=""
                                   placeholder="${message(code:'financials.newCosts.newLicence')}" />
                        </g:else>
                    </g:else>
                </div><!-- .field -->

                <div class="field">

                    <g:if test="${inSubMode}">
                        <%
                            def validSubChilds = com.k_int.kbplus.Subscription.findAllByInstanceOfAndStatusNotEqual(
                                    fixedSubscription,
                                    com.k_int.kbplus.RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
                            )
                        %>

                        <g:if test="${validSubChilds && ! costItem}">
                            <label>Teilnehmer</label>
                            <g:select name="newLicenseeTarget" id="newLicenseeTarget" class="ui dropdown"
                                      from="${[[id:'forConsortia', label:'Gilt für die Konsortiallizenz'], [id:'forAllSubscribers', label:'Für alle Teilnehmer']] + validSubChilds}"
                                      optionValue="${{it?.name ? it.getAllSubscribers().join(', ') : it.label}}"
                                      optionKey="${{"com.k_int.kbplus.Subscription:" + it?.id}}"
                                      noSelection="['':'']"
                                      value="${'com.k_int.kbplus.Subscription:' + it?.id}" />

                            <script>
                                $(function() {
                                    $('#newLicenseeTarget').on('change', function () {
                                        var $elems = $('#newPackageWrapper select, #newPackageWrapper .dropdown')
                                        if ( [
                                                'com.k_int.kbplus.Subscription:forConsortia',
                                                'com.k_int.kbplus.Subscription:forAllSubscribers'
                                            ].includes($(this).val())
                                        ) {
                                            $elems.removeAttr('disabled')
                                            $elems.removeClass('disabled')
                                        } else {
                                            $elems.attr('disabled', 'disabled')
                                            $elems.addClass('disabled')
                                        }
                                    })
                                })
                            </script>
                        </g:if>
                    </g:if>

                </div><!-- .field -->

                <div class="field" id="newPackageWrapper">

                    <label>${message(code:'package.label')}</label>
                    <g:if test="${costItem?.sub}">
                        <g:select name="newPackage" id="newPackage" class="ui dropdown"
                                  from="${[{}] + costItem?.sub?.packages}"
                                  optionValue="${{it?.pkg?.name ?: 'Keine Verknüpfung'}}"
                                  optionKey="${{"com.k_int.kbplus.SubscriptionPackage:" + it?.id}}"
                                  noSelection="['':'']"
                                  value="${'com.k_int.kbplus.SubscriptionPackage:' + costItem?.subPkg?.id}" />
                    </g:if>
                    <g:elseif test="${inSubMode}">
                        <g:select name="newPackage" id="newPackage" class="ui dropdown"
                                  from="${[{}] + fixedSubscription?.packages}"
                                  optionValue="${{it?.pkg?.name ?: 'Keine Verknüpfung'}}"
                                  optionKey="${{"com.k_int.kbplus.SubscriptionPackage:" + it?.id}}"
                                  noSelection="['':'']"
                                  value="${'com.k_int.kbplus.SubscriptionPackage:' + costItem?.subPkg?.id}" />
                    </g:elseif>
                    <g:else>
                        <input name="newPackage" id="newPackage" class="ui" disabled="disabled" data-subFilter="" data-disableReset="true" />
                    </g:else>


                    <%-- the distinction between subMode (= fixedSubscription) and general view is done already in the controller! --%>
                    <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                    <input name="newIe" id="newIE" class="select2 la-select2-fixed-width" />
                    <%--<g:if test="${! inSubMode}">
                        <input name="newIe" id="newIE" data-subFilter="" data-disableReset="true" class="la-full-width" value="${params.newIe}">
                    </g:if>
                    <g:else>
                        <input name="newIe" id="newIE" data-subFilter="${fixedSubscription?.id}" data-disableReset="true" class="select2 la-full-width" value="${params.newIe}">
                    </g:else>--%>

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
                    <semui:datepicker label="financials.invoiceDate" name="newInvoiceDate" placeholder="financials.invoiceDate" value="${costItem?.invoiceDate}" />

                    <label>${message(code:'financials.newCosts.description')}</label>
                    <input type="text" name="newDescription" id="newDescription"
                           placeholder="${message(code:'default.description.label')}" value="${costItem?.costDescription}"/>
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
        <%
            def costItemElementConfigurations = "{"
            StringJoiner sj = new StringJoiner(",")
            orgConfigurations.each { orgConf ->
                sj.add('"'+orgConf.id+'":"'+orgConf.value+'"')
            }
            costItemElementConfigurations += sj.toString()+"}"
        %>
        var costItemElementConfigurations = ${raw(costItemElementConfigurations)}

        $("#costButton1").click(function() {
            if (! isError("#newCostInBillingCurrency") && ! isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInBillingCurrency").val() * $("#newCostCurrencyRate").val()).toFixed(2));

                $(".la-account-currency").find(".field").removeClass("error");
                calcTaxResults()
            }
        })
        $("#costButton2").click(function() {
            if (! isError("#newCostInLocalCurrency") && ! isError("#newCostInBillingCurrency")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInLocalCurrency").val() / $("#newCostInBillingCurrency").val()).toFixed(9));

                $(".la-account-currency").find(".field").removeClass("error");
                calcTaxResults()
            }
        })
        $("#costButton3").click(function() {
            if (! isError("#newCostInLocalCurrency") && ! isError("#newCostCurrencyRate")) {
                var input = $(this).siblings("input");
                input.transition('glow');
                input.val(($("#newCostInLocalCurrency").val() / $("#newCostCurrencyRate").val()).toFixed(2));

                $(".la-account-currency").find(".field").removeClass("error");
                calcTaxResults()
            }
        });
        $("#newCostItemElement").change(function() {
            if(typeof(costItemElementConfigurations[$(this).val()]) !== 'undefined')
                $("[name='ciec']").dropdown('set selected',costItemElementConfigurations[$(this).val()]);
            else
                $("[name='ciec']").dropdown('set selected','null');
        });
        var isError = function(cssSel)  {
            if ($(cssSel).val().length <= 0 || $(cssSel).val() < 0) {
                $(".la-account-currency").children(".field").removeClass("error");
                $(cssSel).parent(".field").addClass("error");
                return true
            }
            return false
        }

        $('.calc').on('change', function() {
            calcTaxResults()
        })

        var calcTaxResults = function() {
            var roundF = $('*[name=newFinalCostRounding]').prop('checked')
            var taxF = 1.0 + (0.01 * $("*[name=newTaxRate]").val())

            $('#newCostInBillingCurrencyAfterTax').val(
                roundF ? Math.round($("#newCostInBillingCurrency").val() * taxF) : ($("#newCostInBillingCurrency").val() * taxF).toFixed(2)
            )
            $('#newCostInLocalCurrencyAfterTax').val(
                roundF ? Math.round( $("#newCostInLocalCurrency").val() * taxF ) : ( $("#newCostInLocalCurrency").val() * taxF ).toFixed(2)
            )
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

            <g:if test="${issueEntitlement}">
                var data = {id : "${issueEntitlement.class.name}:${issueEntitlement.id}",
                            text : "${issueEntitlement.tipp.title.title}"};
            </g:if>

            $('#newIE').select2({
                placeholder: "${message(code:'financials.newCosts.singleEntitlement')}",
                <%--minimumInputLength: 1,
                formatInputTooShort: function () {
                    return "${message(code:'select2.minChars.note')}";
                },--%>
                global: false,
                ajax: {
                    url: "<g:createLink controller='ajax' action='lookupIssueEntitlements' params='${params}'/>",
                    data: function (term, page) {
                        return {
                            hideDeleted: 'true',
                            hideIdent: 'false',
                            inclSubStartDate: 'false',
                            q: '%' + term + '%',
                            page_limit: 20,
                            baseClass: 'com.k_int.kbplus.IssueEntitlement',
                            sub: $("#pickedSubscription,#newSubscription").val()
                        };
                    },
                    results: function (data, page) {
                        return {results: data.values};
                    },
                    allowClear: true,
                    formatSelection: function(data) {
                        return data.text;
                    }
                }
            });
            //duplicated call needed to preselect data
            if(typeof(data) !== 'undefined')
                $('#newIE').select2('data',data);
        }
    </script>

</semui:modal>
<!-- _ajaxModal.gsp -->
