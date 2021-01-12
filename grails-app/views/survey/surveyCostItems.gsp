<%@ page import="de.laser.RefdataValue; de.laser.finance.CostItem; de.laser.SurveyConfig; de.laser.RefdataCategory;de.laser.properties.PropertyDefinition; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.RDStore;" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'survey.label')}</title>
</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="myInstitution" action="dashboard" text="${contextService.getOrg().getDesignation()}"/>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>
    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.getConfigNameShort()}"/>
    </g:if>
    <semui:crumb message="surveyCostItems.label" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="exportSurCostItems" id="${surveyInfo.id}"
                    params="[exportXLSX: true, surveyConfigID: surveyConfig.id]">${message(code: 'survey.exportSurveyCostItems')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatusWithRings object="${surveyInfo}" surveyConfig="${surveyConfig}" controller="survey" action="surveyCostItems"/>
</h1>



<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type == SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription?.id}">
            ${surveyConfig.subscription?.name}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyCostItems.label')}
</h2>

<br />

<g:if test="${surveyConfigs}">
    <div class="ui grid">
        %{--<div class="four wide column">
            <div class="ui vertical fluid menu">
        <g:each in="${surveyConfigs.sort { it.configOrder }}" var="config" status="i">

        <g:link class="item ${params.surveyConfigID == config?.id.toString() ? 'active' : ''}"
                style="${config?.costItemsFinish ? 'background-color: Lime' : ''}"
                controller="survey" action="surveyCostItems"
                id="${config?.surveyInfo.id}" params="[surveyConfigID: config?.id]">

            <h5 class="ui header">${config?.getConfigNameShort()}</h5>
            ${SurveyConfig.getLocalizedValue(config?.type)}


            <div class="ui floating circular label">${config?.orgs?.size() ?: 0}</div>
        </g:link>
        </g:each>
        </div>
        </div>--}%

        <div class="sixteen wide stretched column">
            <div class="ui top attached tabular menu">
                <g:link class="item ${params.tab == 'selectedSubParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'selectedSubParticipants']">
                    ${message(code: 'surveyParticipants.selectedSubParticipants')}
                    <div class="ui floating circular label">${selectedSubParticipants?.size() ?: 0}</div>
                </g:link>

                <g:link class="item ${params.tab == 'selectedParticipants' ? 'active' : ''}"
                        controller="survey" action="surveyCostItems"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, tab: 'selectedParticipants']">
                    ${message(code: 'surveyParticipants.selectedParticipants')}
                    <div class="ui floating circular label">${selectedParticipants?.size() ?: 0}</div>
                </g:link>

            </div>

        <div class="ui bottom attached tab segment active">

            <div class="four wide column">

                <g:if test="${params.tab == 'selectedSubParticipants'}">
                    <g:link onclick="JSPC.app.addForAllSurveyCostItem([${(selectedSubParticipants?.id)}])"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="surveyCostItems.createInitialCostItem"/>
                    </g:link>
                </g:if>

                <g:if test="${params.tab == 'selectedParticipants'}">
                    <g:link onclick="JSPC.app.addForAllSurveyCostItem([${(selectedParticipants?.id)}])"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="surveyCostItems.createInitialCostItem"/>
                    </g:link>
                </g:if>
            </div>

            <br />
            <br />

            <semui:filter>
            <g:form action="surveyCostItems" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
            <g:render template="/templates/filter/orgFilter"
            model="[
                              tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value']],
                              tmplConfigFormFilter: true
                      ]"/>
            </g:form>
            </semui:filter>


            <g:form action="processSurveyCostItemsBulk" name="surveyCostItemsBulk" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']">

                <h3 class="ui header"><span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center" data-content="${message(code: 'surveyCostItems.bulkOption.info')}">
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

                            <div class="field">
                                <div class="ui checkbox">
                                    <label><g:message code="financials.newCosts.finalSumRounded"/></label>
                                    <input name="newFinalCostRounding2" class="hidden calc" type="checkbox"
                                           <g:if test="${costItem?.finalCostRounding}">checked="checked"</g:if>/>
                                </div>
                            </div><!-- .field -->

                        </fieldset> <!-- 1/2 field |  .la-account-currency -->

                    </div><!-- three fields -->
                    <g:if test="${params.tab == 'selectedSubParticipants' }">
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
                                       value="" step="0.01"/>
                                <div class="ui basic label">%</div>
                                </div>
                            </div>
                        </fieldset>
                    </div>
                    </g:if>

                </div>

                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <button class="ui button" type="submit">${message(code: 'default.button.save_changes')}</button>
                    </div>

                    <div class="eight wide field" style="text-align: right;">
                    </div>
                </div>

                <g:if test="${params.tab == 'selectedSubParticipants'}">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedSubParticipants?.findAll { it?.hasAccessOrg() }?.sort {
                               it?.sortname
                           }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>

                        <br />
                        <br />

                        <g:render template="/templates/filter/orgFilterTable"
                                  model="[orgList         : surveyParticipantsHasAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveySubInfoStartEndDate', 'surveySubCostItem', 'surveyCostItem'],
                                          tableID         : 'costTable'
                                  ]"/>

                    </div>

                    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedSubParticipants?.findAll { !it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>

                        <br />
                        <br />

                        <g:render template="/templates/filter/orgFilterTable"
                                  model="[orgList       : surveyParticipantsHasNotAccess,
                                          tmplShowCheckbox: true,
                                          tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveySubInfoStartEndDate', 'surveySubCostItem', 'surveyCostItem'],
                                          tableID       : 'costTable'
                                  ]"/>

                    </div>

                </g:if>


                <g:if test="${params.tab == 'selectedParticipants'}">

                    <h3 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h3>


                    <g:set var="surveyParticipantsHasAccess"
                           value="${selectedParticipants?.findAll { it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>

                    </div>

                    <br />
                    <br />

                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList       : surveyParticipantsHasAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow: ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                      tableID       : 'costTable'
                              ]"/>


                    <h3 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h3>

                    <g:set var="surveyParticipantsHasNotAccess"
                           value="${selectedParticipants?.findAll { !it?.hasAccessOrg() }?.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>
                    </div>


                    <br />
                    <br />

                    <g:render template="/templates/filter/orgFilterTable"
                              model="[orgList         : surveyParticipantsHasNotAccess,
                                      tmplShowCheckbox: true,
                                      tmplConfigShow  : ['lineNumber', 'sortname', 'name', 'surveyCostItem'],
                                      tableID         : 'costTable'
                              ]"/>

                    </div>

                </g:if>

                <br />
                <br />
                <button name="deleteCostItems" value="true" type="submit" class="ui icon negative button" onclick="return confirm('${message(code:'confirm.dialog.delete.surveyCostItems')}')">
                    <i class="trash alternate icon"></i> ${message(code: "surveyCostItems.bulkOption.delete")}
                </button>

            </g:form>
            <br />
            <br />

            <g:form action="surveyCostItemsFinish" method="post" class="ui form"
                    params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

                <div class="ui right floated compact segment">
                    <div class="ui checkbox">
                        <input type="checkbox" onchange="this.form.submit()"
                               name="costItemsFinish" ${surveyConfig.costItemsFinish ? 'checked' : ''}>
                        <label><g:message code="surveyconfig.costItemsFinish.label"/></label>
                    </div>
                </div>

            </g:form>

        </div>
    </div>
</g:if>
<g:else>
    <p><strong>${message(code: 'surveyConfigs.noConfigList')}</strong></p>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">

JSPC.app.isClicked = false;

JSPC.app.addForAllSurveyCostItem = function(orgsIDs) {
                        event.preventDefault();

                        // prevent 2 Clicks open 2 Modals
                        if (!JSPC.app.isClicked) {
                            JSPC.app.isClicked = true;
                            $('.ui.dimmer.modals > #modalSurveyCostItem').remove();
                            $('#dynamicModalContainer').empty()

                           $.ajax({
                                url: "<g:createLink controller='survey' action='addForAllSurveyCostItem'/>",
                                traditional: true,
                                data: {
                                    id: "${params.id}",
                                    surveyConfigID: "${surveyConfig.id}",
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
                                JSPC.app.isClicked = false;
                            }, 800);
                        }
                    }

    JSPC.app.costItemElementConfigurations = ${raw(orgConfigurations as String)};

    JSPC.app.eurVal = "${RefdataValue.getByValueAndCategory('EUR','Currency').id}";

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


        $('#newCostInBillingCurrencyAfterTax2').val(
            roundF ? Math.round(parsedBillingCurrency * taxF) : JSPC.app.convertDouble(parsedBillingCurrency * taxF)
        );

    };

    JSPC.app.costElems = $("#newCostInBillingCurrency2");

    JSPC.app.costElems.on('change', function () {
        if ($("[name='newCostCurrency2']").val() != 0) {
            $("#newCostCurrency2").parent(".field").removeClass("error");
        } else {
            $("#newCostCurrency2").parent(".field").addClass("error");
        }
    });

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

</body>
</html>
