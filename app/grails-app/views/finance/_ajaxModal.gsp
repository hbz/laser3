<!-- _ajaxModal.gsp -->
<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.*;org.springframework.context.i18n.LocaleContextHolder" %>
<laser:serviceInjection />

<g:render template="vars" model="[org:contextService.getOrg()]"/><%-- setting vars --%>

<g:set var="modalText" value="${message(code:'financials.addNewCost')}" />
<g:set var="submitButtonLabel" value="${message(code:'default.button.create_new.label')}" />
<g:set var="org" value="${contextService.getOrg()}" />

<%
    if (costItem) {
        if(mode && mode.equals("edit")) {
            modalText = g.message(code: 'financials.editCost')
            submitButtonLabel = g.message(code:'default.button.save.label')
        }
        else if(mode && mode.equals("copy")) {
            modalText = g.message(code: 'financials.costItem.copy.tooltip')
            submitButtonLabel = g.message(code:'default.button.copy.label')
        }

        def subscriberExists = OrgRole.findBySubAndRoleType(costItem.sub, RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role'));
        if ( subscriberExists ) {
            modalText = subscriberExists.org?.toString()
        }
    }

%>

<semui:modal id="costItem_ajaxModal" text="${modalText}" msgSave="${submitButtonLabel}">
    <g:if test="${costItem?.globalUID}">
        <g:if test="${costItem?.isVisibleForSubscriber && tab == "cons"}">
            <div class="ui orange ribbon label">
                <strong>${message(code:'financials.isVisibleForSubscriber')}</strong>
            </div>
        </g:if>
        <g:elseif test="${costItem?.isVisibleForSubscriber && tab == "subscr"}">
            <div class="ui blue ribbon label">
                <strong>${message(code:'financials.transferConsortialCosts')}: </strong>
            </div>
        </g:elseif>
    </g:if>
    <g:form class="ui small form" id="editCost" url="${formUrl}">
        <g:hiddenField name="showView" value="${tab}" />
        <g:hiddenField name="shortcode" value="${contextService.getOrg()?.shortcode}" />
        <g:if test="${costItem && (mode && mode.equals("edit"))}">
            <g:hiddenField name="oldCostItem" value="${costItem.class.getName()}:${costItem.id}" />
        </g:if>
        <g:elseif test="${costItem && (mode && mode.equals("copy"))}">
            <g:hiddenField name="copyBase" value="${costItem.class.getName()}:${costItem.id}" />
        </g:elseif>

        <!--
        Ctx.Sub: ${sub}
        CI.Sub: ${costItem?.sub}
        CI.SubPkg: ${costItem?.subPkg}
        -->

        <div class="fields">
            <div class="nine wide field">
                <%
                    OrgRole consortialRole = sub?.orgRelations?.find{it.org.id == org.id && it.roleType.id == RDStore.OR_SUBSCRIPTION_CONSORTIA.id}
                %>
                <g:if test="${consortialRole && !sub.administrative}">
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
                                      from="${RefdataCategory.getAllRefdataValues('YN')}"
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
                                    <option value="${BudgetCode.class.name}:${bc.id}">${bc.value}</option>
                                </g:else>
                            </g:each>
                        </select>
                        <%--
                        <input type="text" name="newBudgetCode" id="newBudgetCode" class="select2 la-full-width"
                               placeholder="${CostItemGroup.findByCostItem(costItem)?.budgetCode?.value}"/>
                               --%>

                    </div><!-- .field -->

                    <div class="field">
                        <label>${message(code:'financials.referenceCodes')}</label>
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
                <div class="two fields la-fields-no-margin-button">
                    <div class="field">
                        <label>${message(code:'financials.costItemElement')}</label>
                        <g:if test="${costItemElement}">
                            <laser:select name="newCostItemElement" class="ui dropdown"
                                          from="${costItemElement}"
                                          optionKey="id"
                                          optionValue="value"
                                          noSelection="${['':'']}"
                                          value="${costItem?.costItemElement?.id}" />
                        </g:if>
                        <g:else>
                            ${message(code:'financials.costItemElement.noneDefined')}
                        </g:else>
                    </div><!-- .field -->
                    <div class="field">
                        <label>${message(code:'financials.costItemConfiguration')}</label>
                        <%
                            def ciec = [id:null,value:'financials.costItemConfiguration.notSet']
                            if(costItem && !tab.equals("subscr")) {
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
                               value="<g:formatNumber number="${consCostTransfer ? costItem?.costInBillingCurrencyAfterTax : costItem?.costInBillingCurrency}" minFractionDigits="2" maxFractionDigits="2" />"/>

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
                               value="<g:formatNumber number="${consCostTransfer ? 0.0 : costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2" maxFractionDigits="2" />" />

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

                        <div class="ui icon button" id="costButton2" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <%--
                    <div class="field">
                        <label>${message(code:'financials.newCosts.controllable')}</label>
                        <laser:select name="newCostTaxType" title="${g.message(code: 'financials.addNew.taxCategory')}" class="ui dropdown"
                                      from="${taxType}"
                                      optionKey="id"
                                      optionValue="value"
                                      noSelection="${['':'']}"
                                      value="${costItem?.taxCode?.id}" />
                    </div><!-- .field -->
                    --%>
                    <div class="field">
                        <label>${message(code:'financials.newCosts.taxTypeAndRate')}</label>
                        <%
                            CostItem.TAX_TYPES taxKey
                            if(costItem?.taxKey && tab != "subscr")
                                taxKey = costItem.taxKey
                        %>
                        <g:select class="ui dropdown calc" name="newTaxRate" title="TaxRate"
                              from="${CostItem.TAX_TYPES}"
                              optionKey="${{it.taxType.class.name+":"+it.taxType.id+"§"+it.taxRate}}"
                              optionValue="${{it.taxType.getI10n("value")+" ("+it.taxRate+"%)"}}"
                              value="${taxKey?.taxType?.class?.name}:${taxKey?.taxType?.id}§${taxKey?.taxRate}"
                              noSelection="${['null§0':'']}"/>

                    </div><!-- .field -->
                </div>

                <div class="two fields">
                    <div class="field">
                        <label>${g.message(code:'financials.newCosts.valueInEuro')}</label>
                        <input title="${g.message(code:'financials.addNew.LocalCurrency')}" type="text" class="calc"
                               name="newCostInLocalCurrency" id="newCostInLocalCurrency"
                               placeholder="${message(code:'financials.newCosts.valueInEuro')}"
                               value="<g:formatNumber number="${consCostTransfer ? costItem?.costInLocalCurrencyAfterTax : costItem?.costInLocalCurrency}" minFractionDigits="2" maxFractionDigits="2"/>" />

                        <div class="ui icon button" id="costButton1" data-tooltip="${g.message(code: 'financials.newCosts.buttonExplanation')}" data-position="top center" data-variation="tiny">
                            <i class="calculator icon"></i>
                        </div>
                    </div><!-- .field -->
                    <div class="field">
                        <label>Endpreis (in EUR)</label>
                        <input title="Wert nach Steuer (in EUR)" type="text" readonly="readonly"
                               name="newCostInLocalCurrencyAfterTax" id="newCostInLocalCurrencyAfterTax"
                               value="<g:formatNumber number="${consCostTransfer ? 0.0 : costItem?.costInLocalCurrencyAfterTax}" minFractionDigits="2" maxFractionDigits="2"/>"/>
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
                        <input class="la-full-width"
                               readonly='readonly'
                               value="${costItem.sub.getName()}" />
                        <input name="newSubscription" id="pickedSubscription"
                               type="hidden"
                               value="${'com.k_int.kbplus.Subscription:' + costItem.sub.id}" />
                    </g:if>
                    <g:else>
                        <g:if test="${sub}">
                            <input class="la-full-width"
                                   readonly='readonly'
                                   value="${sub.getName()}" />
                            <input name="newSubscription" id="pickedSubscription"
                                   type="hidden"
                                   value="${'com.k_int.kbplus.Subscription:' + sub.id}" />
                        </g:if>
                        <g:else>
                            <div class="ui search selection dropdown newCISelect" id="newSubscription">
                                <input type="hidden" name="newSubscription">
                                <i class="dropdown icon"></i>
                                <input type="text" class="search">
                                <div class="default text">${message(code:'financials.newCosts.newLicence')}</div>
                            </div>
                            <%--<input name="newSubscription" id="newSubscription" class="la-full-width"
                                   data-subfilter=""
                                   placeholder="${message(code:'financials.newCosts.newLicence')}" />--%>
                        </g:else>
                    </g:else>
                </div><!-- .field -->

                <div class="field">

                    <g:if test="${tab == "cons" && (sub || (costItem && costItem.sub))}">
                        <%
                            def validSubChilds
                            Subscription contextSub
                            if(costItem && costItem.sub) contextSub = costItem.sub
                            else if(sub) contextSub = sub
                            //consortial subscription
                            if(!contextSub.instanceOf)
                                validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(contextSub, RDStore.SUBSCRIPTION_DELETED)
                            //consortial member subscription
                            else if(contextSub.instanceOf)
                                validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(contextSub.instanceOf, RDStore.SUBSCRIPTION_DELETED)
                        %>

                        <g:if test="${validSubChilds}">
                            <label>Teilnehmer</label>
                            <g:if test="${contextSub && contextSub.instanceOf()}">
                                <input class="la-full-width" readonly="readonly" value="${modalText}" />
                            </g:if>
                            <g:else>
                                <g:select name="newLicenseeTarget" id="newLicenseeTarget" class="ui dropdown search"
                                          from="${[[id:'forConsortia', label:'Gilt für die Konsortiallizenz'], [id:'forAllSubscribers', label:'Für alle Teilnehmer']] + validSubChilds}"
                                          optionValue="${{it?.name ? it.getAllSubscribers().join(', ') : it.label}}"
                                          optionKey="${{"com.k_int.kbplus.Subscription:" + it?.id}}"
                                          noSelection="['':'']"
                                          value="${'com.k_int.kbplus.Subscription:' + contextSub.id}"
                                          onchange="onSubscriptionUpdate()"
                                />
                            </g:else>
                        </g:if>
                    </g:if>

                </div><!-- .field -->

                <div id="newPackageWrapper">
                    <div class="field">
                        <label>${message(code:'package.label')}</label>
                        <g:if test="${costItem?.sub}">
                            <g:select name="newPackage" id="newPackage" class="ui dropdown search"
                                      from="${[{}] + costItem?.sub?.packages}"
                                      optionValue="${{it?.pkg?.name ?: 'Keine Verknüpfung'}}"
                                      optionKey="${{"com.k_int.kbplus.SubscriptionPackage:" + it?.id}}"
                                      noSelection="['':'']"
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

        <%
            def costItemElementConfigurations = "{"
            StringJoiner sj = new StringJoiner(",")
            orgConfigurations.each { orgConf ->
                sj.add('"'+orgConf.id+'":"'+orgConf.value+'"')
            }
            costItemElementConfigurations += sj.toString()+"}"
        %>
            var costItemElementConfigurations = ${raw(costItemElementConfigurations)};
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
            });

            $("#editCost").submit(function(e){
                e.preventDefault();
                var valuesCorrect = checkValues();
                if(valuesCorrect) {
                    if($("#newSubscription").hasClass('error') || $("#newPackage").hasClass('error') || $("#newIE").hasClass('error'))
                        alert("${message(code:'financials.newCosts.entitlementError')}");
                    else $(this).unbind('submit').submit();
                }
                else {
                    alert("${message(code:'financials.newCosts.calculationError')}");
                }
            });

            $("#newCostCurrency").change(function(){
                //console.log("event listener succeeded, picked value is: "+$(this).val());
                if($(this).val() === eurVal)
                    $("#newCostCurrencyRate").val(1.0);
                else $("#newCostCurrencyRate").val(0.0);
                $("#costButton1").click();
            });

            $("[name='newSubscription'][name='newLicenseeTarget']").change(function(){
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
                $("#newIE").dropdown('set text',"${costItem?.issueEntitlement ? "${costItem.issueEntitlement.tipp.title.title} (${costItem.issueEntitlement.tipp.title.type.getI10n('value')}) (${costItem.sub.dropdownNamingConvention(contextService.getOrg())})" : ''}");
            }

            function setupCalendar() {
                $("[name='newFinancialYear']").parents(".datepicker").calendar({
                    type: 'year'
                });
            }

            function checkValues() {
                if ( convertDouble($("#newCostInBillingCurrency").val()) * $("#newCostCurrencyRate").val() !== convertDouble($("#newCostInLocalCurrency").val()) ) {
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
                var locale = "${LocaleContextHolder.getLocale()}";
                if(typeof(input) === 'number') {
                    output = input.toFixed(2);
                    if(locale.indexOf("de") > -1)
                        output = output.replace(".",",");
                }
                else if(typeof(input) === 'string') {
                    output = 0.0;
                    if(input.match(/(\d{1-3}\.?)*\d+(,\d{2})?/g))
                        output = parseFloat(input.replace(/\./g,"").replace(/,/g,"."));
                    else if(input.match(/(\d{1-3},?)*\d+(\.\d{2})?/g)) {
                        output = parseFloat(input.replace(/,/g, ""));
                    }
                    else console.log("Please check over regex!");
                    //console.log("string input parsed, output is: "+output);
                }
                return output;
            }

    </script>

</semui:modal>
<!-- _ajaxModal.gsp -->
