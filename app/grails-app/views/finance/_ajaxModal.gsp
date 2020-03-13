<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.helper.RDStore; de.laser.helper.RDConstants; com.k_int.kbplus.*;org.springframework.context.i18n.LocaleContextHolder; de.laser.interfaces.TemplateSupport" %>
<laser:serviceInjection />

<semui:modal id="costItem_ajaxModal" text="${modalText}" msgSave="${submitButtonLabel}">
    <g:if test="${costItem}">
        <g:if test="${editConf.showVisiblitySettings && costItem.isVisibleForSubscriber}">
            <div class="content la-twoSided-ribbon">
                <div class="ui orange ribbon label">
                    <strong><g:message code="financials.isVisibleForSubscriber"/>: ${costItem.sub.getSubscriber()}</strong>
                </div>
            </div>
        </g:if>
        <g:elseif test="${copyCostsfromConsortia}">
            <div class="content la-twoSided-ribbon">
                <div class="ui blue ribbon label">
                    <strong><g:message code="financials.transferConsortialCosts"/>: </strong>
                </div>
            </div>
        </g:elseif>
        <g:elseif test="${subscription}">
            <div class="content la-twoSided-ribbon">
                <div class="ui orange ribbon label">
                    <strong>${subscription.getSubscriber().name}</strong>
                </div>
            </div>
        </g:elseif>
        <div class="content la-twoSided-ribbon">
            <div class="ui orange right ribbon label">
                <strong><g:message code="globalUID.label"/>: ${costItem.globalUID}</strong>
            </div>
        </div>
    </g:if>
    <g:form class="ui small form" id="editCost" url="${formUrl}">
        <g:if test="${costItem}">
            <g:hiddenField name="costItemId" value="${costItem.id}"/>
        </g:if>
        <g:if test="${copyCostsfromConsortia}">
            <g:hiddenField name="copyBase" value="${costItem.class.getName()}:${costItem.id}" />
        </g:if>
        <div class="fields">
            <div class="nine wide field">
                <g:if test="${editConf.showVisibilitySettings}">
                    <div class="two fields la-fields-no-margin-button">
                        <div class="field">
                            <label><g:message code="financials.newCosts.costTitle"/></label>
                            <input type="text" name="newCostTitle" value="${costItem?.costTitle}" />
                        </div><!-- .field -->
                        <div class="field">
                            <label><g:message code="financials.isVisibleForSubscriber"/></label>
                            <g:set var="newIsVisibleForSubscriberValue" value="${costItem?.isVisibleForSubscriber ? RDStore.YN_YES.id : RDStore.YN_NO.id}" />
                            <laser:select name="newIsVisibleForSubscriber" class="ui dropdown"
                                      id="newIsVisibleForSubscriber"
                                      from="${yn}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${newIsVisibleForSubscriberValue}" />
                        </div><!-- .field -->
                    </div>
                </g:if>
                <g:else>
                    <div class="field">
                        <label><g:message code="financials.newCosts.costTitle"/></label>
                        <input type="text" name="newCostTitle" value="${costItem?.costTitle}" />
                    </div><!-- .field -->
                </g:else>
                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label><g:message code="financials.budgetCode"/></label>
                        <select name="newBudgetCodes" class="ui fluid search dropdown" multiple="multiple">
                            <g:each in="${budgetCodes}" var="bc">
                                <g:if test="${costItem?.getBudgetcodes()?.contains(bc)}">
                                    <option selected="selected" value="${bc.class.name}:${bc.id}">${bc.value}</option>
                                </g:if>
                                <g:else>
                                    <option value="${BudgetCode.class.name}:${bc.id}">${bc.value}</option>
                                </g:else>
                            </g:each>
                        </select>
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.referenceCodes"/></label>
                        <input type="text" name="newReference" id="newCostItemReference" value="${costItem?.reference}"/>
                    </div><!-- .field -->
                </div>
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
            <fieldset class="nine wide field la-modal-fieldset-margin-right la-account-currency">
                <label>${g.message(code:'financials.newCosts.amount')}</label>

                <div class="two fields">
                    <div class="field">
                        <label>${message(code:'financials.invoice_total')}</label>
                        <input title="${g.message(code:'financials.addNew.BillingCurrency')}" type="text" class="calc" style="width:50%"
                               name="newCostInBillingCurrency" id="newCostInBillingCurrency"
                               placeholder="${g.message(code:'financials.invoice_total')}"
                               value="<g:formatNumber number="${copyCostsFromConsortia ? costItem?.costInBillingCurrencyAfterTax : costItem?.costInBillingCurrency}" minFractionDigits="2" maxFractionDigits="2" />"/>

                        <div class="ui icon button la-popup-tooltip la-delay" id="costButton3" data-content="${message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>

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
                               value="<g:formatNumber number="${copyCostsFromConsortia ? 0.0 : costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2" maxFractionDigits="2" />" />

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
                               value="${costItem ? costItem.currencyRate : 1.0}" step="0.000000001" />

                        <div class="ui icon button la-popup-tooltip la-delay" id="costButton2" data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
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

                <div class="two fields">
                    <div class="field">
                        <label><g:message code="financials.newCosts.valueInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/></label><%-- TODO once we may configure local currency as OrgSetting, this arg has to be replaced! --%>
                        <input title="<g:message code="financials.newCosts.valueInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/>" type="text" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency"
                               placeholder="${message(code:'financials.newCosts.value')}"
                               value="<g:formatNumber number="${fromConsortia ? costItem?.costInLocalCurrencyAfterTax : costItem?.costInLocalCurrency}" minFractionDigits="2" maxFractionDigits="2"/>" />

                        <div class="ui icon button la-popup-tooltip la-delay" id="costButton1" data-content="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label><g:message code="financials.newCosts.finalSumInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/></label><%-- TODO once we may configure local currency as OrgSetting, this arg has to be replaced! --%>
                        <input title="<g:message code="financials.newCosts.finalSumInLocalCurrency" args="${[RDStore.CURRENCY_EUR.value]}"/>" type="text" readonly="readonly"
                               name="newCostInLocalCurrencyAfterTax" id="newCostInLocalCurrencyAfterTax"
                               value="<g:formatNumber number="${fromConsortia ? 0.0 : costItem?.costInLocalCurrencyAfterTax}" minFractionDigits="2" maxFractionDigits="2"/>"/>
                    </div><!-- .field -->
                </div>

                <div class="field">
                    <div class="ui checkbox">
                        <label><g:message code="financials.newCosts.finalSumRounded"/></label>
                        <input name="newFinalCostRounding" class="hidden calc" type="checkbox"
                               <g:if test="${costItem?.finalCostRounding}"> checked="checked" </g:if>
                        />
                    </div>
                </div><!-- .field -->
            </fieldset> <!-- 1/2 field |  .la-account-currency -->

            <fieldset class="seven wide field la-modal-fieldset-no-margin">
                <label>${message(code:'financials.newCosts.constsReferenceOn')}</label>

                <div class="field">
                    <label>${message(code:'default.subscription.label')}</label>

                    <g:if test="${costItem?.sub}">
                        <input class="la-full-width"
                               readonly='readonly'
                               value="${costItem.sub.getName()}" />
                        <input name="newSubscription" id="pickedSubscription"
                               type="hidden"
                               value="${'com.k_int.kbplus.Subscription:' + costItem.sub.id}" />
                    </g:if>
                    <g:else>
                        <g:if test="${subscription}">
                            <input class="la-full-width"
                                   readonly='readonly'
                                   value="${subscription.getName()}" />
                            <input name="newSubscription" id="pickedSubscription"
                                   type="hidden"
                                   value="${'com.k_int.kbplus.Subscription:'+subscription.id}" />
                        </g:if>
                        <g:else>
                            <div class="ui search selection dropdown newCISelect" id="newSubscription">
                                <input type="hidden" name="newSubscription">
                                <i class="dropdown icon"></i>
                                <input type="text" class="search">
                                <div class="default text">${message(code:'financials.newCosts.newLicence')}</div>
                            </div>
                        </g:else>
                    </g:else>
                </div><!-- .field -->

                <div class="field">
                    <g:if test="${validSubChilds}">
                        <label>${licenseeLabel}</label>
                        <g:if test="${contextSub && contextSub.instanceOf()}">
                            <input class="la-full-width" readonly="readonly" value="${contextSub.getSubscriber().sortname}" />
                        </g:if>
                        <g:else>
                            <g:select name="newLicenseeTarget" id="newLicenseeTarget" class="ui dropdown search"
                                      from="${validSubChilds}"
                                      optionValue="${{it.name ? it.getSubscriber().dropdownNamingConvention(institution) : it.label}}"
                                      optionKey="${{"com.k_int.kbplus.Subscription:" + it.id}}"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                      value="${'com.k_int.kbplus.Subscription:' + costItem?.sub?.id}"
                                      onchange="onSubscriptionUpdate()"
                            />
                        </g:else>
                    </g:if>

                </div><!-- .field -->

                <div id="newPackageWrapper">
                    <div class="field">
                        <label>${message(code:'package.label')}</label>
                        <g:if test="${costItem?.sub}">
                            <g:select name="newPackage" id="newPackage" class="ui dropdown search"
                                      from="${[{}] + costItem?.sub?.packages}"
                                      optionValue="${{it?.pkg?.name ?: message(code:'financials.newCosts.noPackageLink')}}"
                                      optionKey="${{"com.k_int.kbplus.SubscriptionPackage:" + it?.id}}"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                      value="${'com.k_int.kbplus.SubscriptionPackage:' + costItem?.subPkg?.id}" />
                        </g:if>
                        <g:else>
                            <%--<input name="newPackage" id="newPackage" class="ui" disabled="disabled" data-subFilter="" data-disableReset="true" />--%>
                            <div class="ui search selection dropdown newCISelect" id="newPackage">
                                <input type="hidden" name="newPackage" value="${costItem?.subPkg ? "com.k_int.kbplus.SubscriptionPackage:${costItem.subPkg.id}" : params.newPackage}">
                                <i class="dropdown icon"></i>
                                <input type="text" class="search">
                                <div class="default text"></div>
                            </div>
                        </g:else>
                    </div>
                    <div class="field">
                        <%-- the distinction between subMode (= sub) and general view is done already in the controller! --%>
                        <label>${message(code:'financials.newCosts.singleEntitlement')}</label>
                        <div class="ui search selection dropdown newCISelect" id="newIE">
                            <input type="hidden" name="newIE" value="${costItem?.issueEntitlement ? "com.k_int.kbplus.IssueEntitlement:${costItem.issueEntitlement.id}" : params.newIE}">
                            <i class="dropdown icon"></i>
                            <input type="text" class="search">
                            <div class="default text"></div>
                        </div>
                    </div>

                </div><!-- .field -->
            </fieldset> <!-- 2/2 field -->

        </div><!-- three fields -->

        <div class="three fields">
            <fieldset class="field la-modal-fieldset-no-margin">
                <div class="two fields">
                    <semui:datepicker label="financials.datePaid" id="newDatePaid" name="newDatePaid" placeholder="financials.datePaid" value="${costItem?.datePaid}" />

                    <%-- to restrict upon year: https://jsbin.com/ruqakehefa/1/edit?html,js,output , cf. example 8! --%>
                    <semui:datepicker label="financials.financialYear" id="newFinancialYear" name="newFinancialYear" placeholder="financials.financialYear" value="${costItem?.financialYear}" />
                </div>
                <div class="two fields">
                    <semui:datepicker label="financials.dateFrom" id="newStartDate" name="newStartDate" placeholder="default.date.label" value="${costItem?.startDate}" />

                    <semui:datepicker label="financials.dateTo" id="newEndDate" name="newEndDate" placeholder="default.date.label" value="${costItem?.endDate}" />
                </div>
            </fieldset> <!-- 1/3 field -->

            <fieldset class="field la-modal-fieldset-margin">
                <div class="field">
                    <semui:datepicker label="financials.invoiceDate" id="newInvoiceDate" name="newInvoiceDate" placeholder="financials.invoiceDate" value="${costItem?.invoiceDate}" />

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

    <script>
        /*var costSelectors = {
            lc:   "#newCostInLocalCurrency",
            rate: "#newCostCurrencyRate",
            bc:   "#newCostInBillingCurrency"
        }*/

            var costItemElementConfigurations = ${raw(orgConfigurations as String)};
            console.log(costItemElementConfigurations);
            var selLinks = {
                "newSubscription": "${createLink([controller:"ajax",action:"lookupSubscriptions"])}?query={query}",
                "newPackage": "${createLink([controller:"ajax",action:"lookupSubscriptionPackages"])}?query={query}",
                "newIE": "${createLink([controller:"ajax",action:"lookupIssueEntitlements"])}?query={query}"
            };
            var eurVal = "${RefdataValue.getByValueAndCategory('EUR','Currency').id}";
            if($("[name='newSubscription']").val().length > 0) {
                selLinks.newPackage += "&sub="+$("[name='newSubscription']").val();
                selLinks.newIE += "&sub="+$("[name='newSubscription']").val();
            }
            $("#costButton1").click(function() {
                if (! isError("#newCostInBillingCurrency") && ! isError("#newCostCurrencyRate")) {
                    var input = $(this).siblings("input");
                    input.transition('glow');
                    var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency").val());
                    input.val(convertDouble(parsedBillingCurrency * $("#newCostCurrencyRate").val()));

                    $(".la-account-currency").find(".field").removeClass("error");
                    calcTaxResults()
                }
            });
            $("#newCostInBillingCurrency").change(function(){
                var currencyEUR = ${RefdataValue.getByValueAndCategory('EUR','Currency').id};
                if($("#newCostCurrency").val() == currencyEUR) {
                    $("#costButton1").click();
                }
            });
            $("#costButton2").click(function() {
                if (! isError("#newCostInLocalCurrency") && ! isError("#newCostInBillingCurrency")) {
                    var input = $(this).siblings("input");
                    input.transition('glow');
                    var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency").val());
                    var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency").val());
                    input.val((parsedLocalCurrency / parsedBillingCurrency));

                    $(".la-account-currency").find(".field").removeClass("error");
                    calcTaxResults()
                }
            });
            $("#costButton3").click(function() {
                if (! isError("#newCostInLocalCurrency") && ! isError("#newCostCurrencyRate")) {
                    var input = $(this).siblings("input");
                    input.transition('glow');
                    var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency").val());
                    input.val(convertDouble(parsedLocalCurrency / $("#newCostCurrencyRate").val()));

                    $(".la-account-currency").find(".field").removeClass("error");
                    calcTaxResults()
                }
            });
            $("#newCostItemElement").change(function() {
                console.log(costItemElementConfigurations[$(this).val()]);
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
            };

            $('.calc').on('change', function() {
                calcTaxResults()
            });

            var calcTaxResults = function() {
                var roundF = $('*[name=newFinalCostRounding]').prop('checked');
                console.log($("*[name=newTaxRate]").val());
                var taxF = 1.0 + (0.01 * $("*[name=newTaxRate]").val().split("§")[1]);

                var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency").val());
                var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency").val());

                $('#newCostInBillingCurrencyAfterTax').val(
                    roundF ? Math.round(parsedBillingCurrency * taxF) : convertDouble(parsedBillingCurrency * taxF)
                );
                $('#newCostInLocalCurrencyAfterTax').val(
                    roundF ? Math.round(parsedLocalCurrency * taxF ) : convertDouble(parsedLocalCurrency * taxF )
                );
            };

            var costElems = $("#newCostInLocalCurrency, #newCostCurrencyRate, #newCostInBillingCurrency");

            costElems.on('change', function(){
                checkValues();
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
                    var valuesCorrect = checkValues();
                    if(valuesCorrect) {
                        $("#newCostCurrency").parent(".field").removeClass("error");
                        if($("#newSubscription").hasClass('error') || $("#newPackage").hasClass('error') || $("#newIE").hasClass('error'))
                            alert("${message(code:'financials.newCosts.entitlementError')}");
                        else {
                            if($("[name='newLicenseeTarget']").length > 0 && $("[name='newLicenseeTarget']").val() === '') {
                                alert("${message(code:'financials.newCosts.noSubscriptionError')}") //continue here: a confirm if the consortial user wants really to attach cost items to the parent subscription
                            }
                            else {
                                if($("[name='newLicenseeTarget").val().indexOf('forParent') > -1) {
                                    if(confirm("${message(code:'financials.newCosts.confirmForParent')}")) $(this).unbind('submit').submit();
                                }
                                else $(this).unbind('submit').submit();
                            }
                        }
                    }
                    else {
                        alert("${message(code:'financials.newCosts.calculationError')}");
                    }
                }
                else {
                    alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
                    $("#newCostCurrency").parent(".field").addClass("error");
                }
            });

            $("#newCostCurrency").change(function(){
                //console.log("event listener succeeded, picked value is: "+$(this).val());
                if($(this).val() === eurVal)
                    $("#newCostCurrencyRate").val(1.0);
                else $("#newCostCurrencyRate").val(0.0);
                $("#costButton1").click();
            });

            $("[name='newSubscription']").change(function(){
                onSubscriptionUpdate();
                ajaxPostFunc();
            });

            function onSubscriptionUpdate() {
                var context = $("[name='newSubscription']").val();
                if($("[name='newLicenseeTarget']").length > 0 && !$("[name='newLicenseeTarget']").val().match(/:null|:for/))
                    context = $("[name='newLicenseeTarget']").val();
                selLinks.newIE = "${createLink([controller:"ajax",action:"lookupIssueEntitlements"])}?query={query}&sub="+context;
                selLinks.newPackage = "${createLink([controller:"ajax",action:"lookupSubscriptionPackages"])}?query={query}&ctx="+context;
                $("#newIE").dropdown('clear');
                $("#newPackage").dropdown('clear');
                ajaxPostFunc();
            }

            $("#newPackage").change(function(){
                var context = $("[name='newSubscription']").val();
                if($("[name='newLicenseeTarget']").length > 0 && !$("[name='newLicenseeTarget']").val().match(/:null|:for/))
                    context = $("[name='newLicenseeTarget']").val();
                selLinks.newIE = "${createLink([controller:"ajax",action:"lookupIssueEntitlements"])}?query={query}&sub="+context+"&pkg="+$("[name='newPackage']").val();
                $("#newIE").dropdown('clear');
                ajaxPostFunc();
            });

            $("#newIE").change(function(){
                checkPackageBelongings();
            });

            function ajaxPostFunc() {
                $(".newCISelect").each(function(k,v){
                    $(this).dropdown({
                        apiSettings: {
                            url: selLinks[$(this).attr("id")],
                            cache: false
                        },
                        clearable: true,
                        minCharacters: 0
                    });
                });
                <%
                    if(costItem?.issueEntitlement) {
                        String ieTitleName = costItem.issueEntitlement.tipp.title.title
                        String ieTitleTypeString = costItem.issueEntitlement.tipp.title.printTitleType()
                        %>
                    $("#newIE").dropdown('set text',"${ieTitleName} (${ieTitleTypeString}) (${costItem.sub.dropdownNamingConvention(contextService.getOrg())})");
                <%  }  %>
            }

            function setupCalendar() {
                $("[name='newFinancialYear']").parents(".datepicker").calendar({
                    type: 'year'
                });
            }

            function checkValues() {
                if ( (convertDouble($("#newCostInBillingCurrency").val()) * $("#newCostCurrencyRate").val()).toFixed(2) !== convertDouble($("#newCostInLocalCurrency").val()).toFixed(2) ) {
                    console.log("inserted values are: "+convertDouble($("#newCostInBillingCurrency").val())+" * "+$("#newCostCurrencyRate").val()+" = "+convertDouble($("#newCostInLocalCurrency").val()).toFixed(2)+", correct would be: "+(convertDouble($("#newCostInBillingCurrency").val()) * $("#newCostCurrencyRate").val()).toFixed(2));
                    costElems.parent('.field').addClass('error');
                    return false;
                }
                else {
                    costElems.parent('.field').removeClass('error');
                    return true;
                }
            }

            function checkPackageBelongings() {
                var subscription = $("[name='newSubscription'], #pickedSubscription").val();
                if($("[name='newLicenseeTarget']").length > 0 && !$("[name='newLicenseeTarget']").val().match(/:null|:for/)) {
                    subscription = $("[name='newLicenseeTarget']").val();
                }
                $.ajax({
                    url: "<g:createLink controller="ajax" action="checkCascade"/>?subscription="+subscription+"&package="+$("[name='newPackage']").val()+"&issueEntitlement="+$("[name='newIE']").val(),
                }).done(function (response) {
                    //console.log("function ran through w/o errors, please continue implementing! Response from server is: "+JSON.stringify(response))
                    if(!response.sub) $("#newSubscription").addClass("error");
                    else $("#newSubscription").removeClass("error");
                    if(!response.subPkg) $("#newPackage").addClass("error");
                    else $("#newPackage").removeClass("error");
                    if(!response.ie) $("#newIE").addClass("error");
                    else $("#newIE").removeClass("error");
                }).fail(function () {
                    console.log("AJAX error! Please check logs!");
                });
            }

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
