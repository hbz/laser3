<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.SurveyConfig; com.k_int.kbplus.CostItem; com.k_int.kbplus.RefdataValue; org.springframework.context.i18n.LocaleContextHolder;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg()?.getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig]" text="${surveyConfig?.getConfigNameShort()}"/>
    </g:if>
    <semui:crumb message="surveyCostItems.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="exportSurCostItems" id="${surveyInfo?.id}"
                    params="[exportXLS: true, surveyConfigID: surveyConfig?.id]">${message(code: 'survey.exportCostItems')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br>

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig?.type == 'Subscription'}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig?.subscription?.id}">
            ${surveyConfig?.subscription?.name}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig?.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyCostItems.label')}
</h2>

<br>

<g:if test="${surveyConfigs}">
    <div class="ui grid">
        %{--<div class="four wide column">
            <div class="ui vertical fluid menu">
        <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

        <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                style="${config?.costItemsFinish ? 'background-color: Lime' : ''}"
                controller="survey" action="surveyCostItems"
                id="${config?.surveyInfo?.id}" params="[surveyConfigID: config?.id]">

            <h5 class="ui header">${config?.getConfigNameShort()}</h5>
            ${com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type)}


            <div class="ui floating circular label">${config?.orgs?.size() ?: 0}</div>
        </g:link>
        </g:each>
        </div>
        </div>--}%

        <div class="sixteen wide stretched column">
            <div class="ui top attached tabular menu">
                <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig?.surveyInfo?.id}"
                        params="[surveyConfigID: surveyConfig?.id, tab: 'selectedSubParticipants']">
                    ${message(code: 'surveyParticipants.selectedSubParticipants')}
                    <div class="ui floating circular label">${selectedSubParticipants?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig?.surveyInfo?.id}"
                        params="[surveyConfigID: surveyConfig?.id, tab: 'selectedParticipants']">
                    ${message(code: 'surveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants?.size() ?: 0}</div>
                </g:link>

            </div>

        <div class="ui bottom attached tab segment active">

            <div class="four wide column">

                <g:link onclick="addForAllSurveyCostItem([${(selectedSubParticipants?.id)}])"
                        class="ui icon button right floated trigger-modal">
                    <g:message code="surveyCostItems.createInitialCostItem"/>
                </g:link>
            </div>

            <br>
            <br>

            <semui:filter>
            <g:form action="surveyCostItems" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']">
            <g:render template="/templates/filter/orgFilter"
            model="[
                              tmplConfigShow      : [['name', 'libraryType'], ['federalState', 'libraryNetwork', 'property'], ['customerType']],
                              tmplConfigFormFilter: true,
                              useNewLayouter      : true
                      ]"/>
            </g:form>
            </semui:filter>


            <g:form action="processSurveyCostItemsBulk" name="surveyCostItemsBulk" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']">

                <h3><span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'surveyCostItems.bulkOption.info')}">
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
                                           style="width:50%"
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
                                              from="${currency}"
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
                                           class="disabled"
                                           name="newCostCurrencyRate2" id="newCostCurrencyRate2"
                                           placeholder="${g.message(code: 'financials.newCosts.exchangeRate')}"
                                           value="${costItem ? costItem.currencyRate : 1.0}" step="0.000000001" readonly="readonly"/>


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
                                           class="disabled"
                                           name="newCostInLocalCurrency2" id="newCostInLocalCurrency2"
                                           placeholder="${message(code: 'financials.newCosts.value')}"
                                           value="<g:formatNumber
                                                   number="${costItem?.costInLocalCurrency}"
                                                   minFractionDigits="2" maxFractionDigits="2"/>" readonly="readonly"/>

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

                            <div class="field">
                                <div class="ui checkbox">
                                    <label><g:message code="financials.newCosts.finalSumRounded"/></label>
                                    <input name="newFinalCostRounding2" class="hidden calc" type="checkbox"
                                           <g:if test="${costItem?.finalCostRounding}">checked="checked"</g:if>/>
                                </div>
                            </div><!-- .field -->

                        </fieldset> <!-- 1/2 field |  .la-account-currency -->

                    </div><!-- three fields -->
                    <div class="ui horizontal divider"><g:message code="search.advancedSearch.option.OR"/></div>

                    <div class="fields">
                        <fieldset class="sixteen wide field la-modal-fieldset-margin-right la-account-currency">
                            <div class="field center aligned">

                                <label>${message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}</label>
                                <div class="ui right labeled input">
                                <input type="number"
                                       style="width:50%"
                                       name="percentOnOldPrice"
                                       placeholder="${g.message(code: 'surveyCostItems.bulkOption.percentOnOldPrice')}"
                                       value="${0.0}" step="0.01"/>
                                <div class="ui basic label">%</div>
                                </div>
                            </div>
                        </fieldset>
                    </div>

                </div>

                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
                    </div>

                    <div class="eight wide field" style="text-align: right;">
                    </div>
                </div>

                <g:if test="${params.tab == 'selectedSubParticipants'}">

                    <h3><g:message code="surveyParticipants.hasAccess"/></h3>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedSubParticipants?.findAll { it?.hasAccessOrg() }?.sort {
                               it?.sortname
                           }}"/>

                    <div class="four wide column">

                        <g:link data-orgIdList="${(surveyParticipantsHasAccess.id).join(',')}"
                                data-targetId="copyEmailaddresses_ajaxModal2"
                                class="ui icon button right floated trigger-modal">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </g:link>

                        <br>
                        <br>

                        <g:render template="/templates/filter/orgFilterTable"
                                  model="[orgList         : surveyParticipantsHasAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveySubInfoStartEndDate', 'surveySubCostItem', 'surveyCostItem'],
                                          tableID         : 'costTable'
                                  ]"/>

                    </div>

                    <h3><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedSubParticipants?.findAll { !it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">

                        <g:link data-orgIdList="${(surveyParticipantsHasNotAccess.id).join(',')}"
                                data-targetId="copyEmailaddresses_ajaxModal3"
                                class="ui icon button right floated trigger-modal">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </g:link>

                        <br>
                        <br>

                        <g:render template="/templates/filter/orgFilterTable"
                                  model="[orgList       : surveyParticipantsHasNotAccess,
                                          tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveySubInfoStartEndDate', 'surveySubCostItem', 'surveyCostItem'],
                                          tableID       : 'costTable'
                                  ]"/>

                    </div>

                </g:if>


                <g:if test="${params.tab == 'selectedParticipants'}">

                    <h3><g:message code="surveyParticipants.hasAccess"/></h3>


                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedParticipants?.findAll { it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">

                        <g:link data-orgIdList="${(surveyParticipantsHasAccess.id).join(',')}"
                                data-targetId="copyEmailaddresses_ajaxModal4"
                                class="ui icon button right floated trigger-modal">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </g:link>

                    </div>

                    <br>
                    <br>

                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList       : surveyParticipantsHasAccess,
                                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                      tableID       : 'costTable'
                              ]"/>


                    <h3><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedParticipants?.findAll { !it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                        <g:link data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}"
                                data-targetId="copyEmailaddresses_ajaxModal6"
                                class="ui icon button right floated trigger-modal">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </g:link>
                    </div>


                    <br>
                    <br>

                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList         : surveyParticipantsHasNotAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                      tableID         : 'costTable'
                              ]"/>

                    </div>

                </g:if>
            </g:form>
            <br>
            <br>

            <g:form action="surveyCostItemsFinish" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

                <div class="ui right floated compact segment">
                    <div class="ui checkbox">
                        <input type="checkbox" onchange="this.form.submit()"
                               name="costItemsFinish" ${surveyConfig?.costItemsFinish ? 'checked' : ''}>
                        <label><g:message code="surveyConfig.costItemsFinish.label"/></label>
                    </div>
                </div>

            </g:form>

        </div>
    </div>
</g:if>
<g:else>
    <p><b>${message(code: 'surveyConfigs.noConfigList')}</b></p>
</g:else>

<g:javascript>

var isClicked = false;

function addForAllSurveyCostItem(orgsIDs) {
                        event.preventDefault();

                        // prevent 2 Clicks open 2 Modals
                        if (!isClicked) {
                            isClicked = true;
                            $('.ui.dimmer.modals > #modalSurveyCostItem').remove();
                            $('#dynamicModalContainer').empty()

                           $.ajax({
                                url: "<g:createLink controller='survey' action='addForAllSurveyCostItem'/>",
                                traditional: true,
                                data: {
                                    id: "${params.id}",
                                    surveyConfigID: "${surveyConfig?.id}",
                                    orgsIDs: orgsIDs
                                }
                            }).done(function (data) {
                                $('#dynamicModalContainer').html(data);

                                $('#dynamicModalContainer .ui.modal').modal({
                                    onVisible: function () {
                                        r2d2.initDynamicSemuiStuff('#modalSurveyCostItem');
                                        r2d2.initDynamicXEditableStuff('#modalSurveyCostItem');

                                    },
                                    detachable: true,
                                    closable: false,
                                    transition: 'scale',
                                    onApprove: function () {
                                        $(this).find('.ui.form').submit();
                                        return false;
                                    }
                                }).modal('show');
                            })
                            setTimeout(function () {
                                isClicked = false;
                            }, 800);
                        }
                    }

</g:javascript>

<script>

    var costItemElementConfigurations = ${raw(orgConfigurations as String)};

    var eurVal = "${RefdataValue.getByValueAndCategory('EUR','Currency').id}";

    $("#newCostInBillingCurrency2").change(function () {
        var currencyEUR = ${RefdataValue.getByValueAndCategory('EUR','Currency').id};
        if ($("#newCostCurrency2").val() == currencyEUR) {
            $("#costButton12").click();
        }
    });

    $("#costButton12").click(function () {
        if (!isError("#newCostInBillingCurrency2") && !isError("#newCostCurrencyRate2")) {
            var input = $(this).siblings("input");
            input.transition('glow');
            var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency2").val());
            input.val(convertDouble(parsedBillingCurrency * $("#newCostCurrencyRate2").val()));

            $(".la-account-currency").find(".field").removeClass("error");
            calcTaxResults()
        }
    });
    $("#costButton22").click(function () {
        if (!isError("#newCostInLocalCurrency2") && !isError("#newCostInBillingCurrency2")) {
            var input = $(this).siblings("input");
            input.transition('glow');
            var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency2").val());
            var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency2").val());
            input.val((parsedLocalCurrency / parsedBillingCurrency));

            $(".la-account-currency").find(".field").removeClass("error");
            calcTaxResults()
        }
    });
    $("#costButton32").click(function () {
        if (!isError("#newCostInLocalCurrency2") && !isError("#newCostCurrencyRate2")) {
            var input = $(this).siblings("input");
            input.transition('glow');
            var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency2").val());
            input.val(convertDouble(parsedLocalCurrency / $("#newCostCurrencyRate2").val()));

            $(".la-account-currency").find(".field").removeClass("error");
            calcTaxResults()
        }
    });
    $("#newCostItemElement2").change(function () {
        if (typeof (costItemElementConfigurations[$(this).val()]) !== 'undefined')
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
        var roundF = $('*[name=newFinalCostRounding2]').prop('checked');
        console.log($("*[name=newTaxRate2]").val());
        var taxF = 1.0 + (0.01 * $("*[name=newTaxRate2]").val().split("§")[1]);

        var parsedBillingCurrency = convertDouble($("#newCostInBillingCurrency2").val());
        var parsedLocalCurrency = convertDouble($("#newCostInLocalCurrency2").val());

        $('#newCostInBillingCurrencyAfterTax2').val(
            roundF ? Math.round(parsedBillingCurrency * taxF) : convertDouble(parsedBillingCurrency * taxF)
        );
        $('#newCostInLocalCurrencyAfterTax2').val(
            roundF ? Math.round(parsedLocalCurrency * taxF) : convertDouble(parsedLocalCurrency * taxF)
        );
    };

    var costElems = $("#newCostInLocalCurrency2, #newCostCurrencyRate2, #newCostInBillingCurrency2");

    costElems.on('change', function () {
        checkValues();
        if ($("[name='newCostCurrency2']").val() != 0) {
            $("#newCostCurrency2").parent(".field").removeClass("error");
        } else {
            $("#newCostCurrency2").parent(".field").addClass("error");
        }
    });

    /*    $("#surveyCostItemsBulk").submit(function (e) {
            e.preventDefault();
            if ($("[name='newCostCurrency2']").val() != 0) {
                var valuesCorrect = checkValues();
                if (valuesCorrect) {
                    $("#newCostCurrency2").parent(".field").removeClass("error");
                    if ($("#newSubscription").hasClass('error') || $("#newPackage").hasClass('error') || $("#newIE").hasClass('error'))
                        alert("${message(code:'financials.newCosts.entitlementError')}");
                else $(this).unbind('submit').submit();
            } else {
                alert("${message(code:'financials.newCosts.calculationError')}");
            }
        } else {
            alert("${message(code:'financials.newCosts.noCurrencyPicked')}");
            $("#newCostCurrency2").parent(".field").addClass("error");
        }
    });*/

    $("#newCostCurrency2").change(function () {
        //console.log("event listener succeeded, picked value is: "+$(this).val());
        if ($(this).val() === eurVal)
            $("#newCostCurrencyRate2").val(1.0);
        else $("#newCostCurrencyRate2").val(0.0);
        $("#costButton12").click();
    });


    function checkValues() {
        if (convertDouble($("#newCostInBillingCurrency2").val()) * $("#newCostCurrencyRate2").val() !== convertDouble($("#newCostInLocalCurrency2").val())) {
            costElems.parent('.field').addClass('error');
            return false;
        } else {
            costElems.parent('.field').removeClass('error');
            return true;
        }
    }

    function convertDouble(input) {
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

</script>

</body>
</html>
