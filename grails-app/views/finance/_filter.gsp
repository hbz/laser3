<!-- _filter.gsp -->
<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.utils.LocaleUtils; de.laser.utils.DateUtils; de.laser.RefdataValue; de.laser.I10nTranslation; java.text.SimpleDateFormat;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.properties.PropertyDefinition;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.FinanceController;de.laser.finance.CostItem" %>
<laser:serviceInjection />

<%
    String lang = LocaleUtils.getCurrentLang()
    String getAllRefDataValuesForCategoryQuery = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv.value_" + lang
%>

    <%--normal ui:filter comes along with more functionality which conflicts with ajax dropdown initialisation, see ERMS-1420--%>
    <ui:filter>
        <%
            Map<String,Object> formUrl = [controller: 'myInstitution', action: 'finance']
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            if (fixedSubscription) {
                formUrl = [mapping: 'subfinance', params: [sub: "${fixedSubscription?.id}"]]
            }
        %>

        <g:form url="${formUrl}" method="get" class="ui form">

            <div class="four fields">
                <%-- this test includes the check if the filter is called for a subscription consortia --%>
                <g:if test="${subMembers}">
                    <div class="field">
                        <label for="filterSubMembers">
                            <g:message code="${subMemberLabel}"/>
                        </label>
                        <g:select id="filterSubMembers" name="filterSubMembers" multiple="" value="${filterPresets?.filterSubMembers?.collect{ sm -> sm.id }}"
                                  class="ui fluid search dropdown" from="${subMembers}" optionKey="id" optionValue="${{it.getSubscriberRespConsortia().dropdownNamingConvention(institution)}}"
                                  noSelection="${['':message(code:'default.select.all.label')]}"
                        />
                    </div>
                </g:if>
                <g:if test="${consMembers}">
                    <div class="field">
                        <label for="filterConsMembers">
                            <g:message code="${subMemberLabel}"/>
                        </label>
                        <g:select id="filterConsMembers" name="filterConsMembers" multiple="" value="${filterPresets?.filterConsMembers?.collect{ cm -> cm.id }}"
                                  class="ui fluid search dropdown" from="${consMembers}" optionKey="id" optionValue="${{it.dropdownNamingConvention(institution)}}"
                                  noSelection="${['':message(code:'default.select.all.label')]}"
                        />
                    </div>
                </g:if>
                <div class="field">
                    <label>${message(code:'menu.my.providers')}</label>
                    <div class="ui multiple search selection dropdown newFilter" id="filterSubProviders">
                        <input type="hidden" name="filterSubProviders">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
                <div class="field">
                    <label>${message(code:'menu.my.vendors')}</label>
                    <div class="ui multiple search selection dropdown newFilter" id="filterSubVendors">
                        <input type="hidden" name="filterSubVendors">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field">
                        <g:set var="subStatus" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.SUBSCRIPTION_STATUS])}" scope="request"/>
                        <label for="filterSubStatus">${message(code:'subscription.status.label')}</label>
                        <ui:select id="filterSubStatus" class="ui fluid dropdown search clearable" name="filterSubStatus" multiple=""
                                      from="${subStatus}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${filterPresets?.filterSubStatus?.id}"
                                      noSelection="${['' : message(code:'default.select.all.label')]}"
                            onchange="JSPC.app.updateSubscriptionDropdown()"
                        />
                    </div>
                </g:if>
            </div>

            <div class="three fields">
                <div class="field">
                    <label for="filterCITitle">${message(code:'financials.newCosts.costTitle')}
                        <span data-position="right center" class="la-popup-tooltip" data-content="${message(code:'financials.title.tooltip')}">
                            <i class="${Icon.TOOLTIP.HELP}"></i>
                        </span>
                    </label>
                    <div class="ui search selection dropdown <g:if test="${ciTitles}">allowAdditions</g:if>" id="filterCITitle">
                        <input type="hidden" name="filterCITitle" value="${filterPresets?.filterCITitle}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                        <div class="menu">
                        <g:each in="${ciTitles}" var="ciTitle">
                                <div class="item" data-value="${ciTitle}">${ciTitle}</div>
                        </g:each>
                        </div>
                    </div>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field"><!--NEW -->
                        <label>${message(code:'default.subscription.label')}</label>
                        <div class="ui search selection multiple dropdown newFilter" id="filterCISub">
                            <input type="hidden" name="filterCISub">
                            <i class="dropdown icon"></i>
                            <input type="text" class="search">
                            <div class="default text"><g:message code="default.select.all.label"/></div>
                        </div>
                    </div>
                </g:if>
                <div class="field"><!--NEW -->
                    <label>${message(code:'package.label')}</label>
                    <div class="ui search selection multiple dropdown newFilter" id="filterCIPkg">
                        <input type="hidden" name="filterCIPkg">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <label>${message(code:'financials.budgetCode')}</label>
                    <g:select id="filterCIBudgetCode" class="ui multiple dropdown search selection"
                              multiple="multiple"
                              name="filterCIBudgetCode"
                              from="${budgetCodes}"
                              optionKey="id" optionValue="value"
                              value="${filterPresets?.filterCIBudgetCode?.id}"
                              noSelection="${['':message(code:'default.select.all.label')]}" />
                </div>

                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIInvoiceNumber">
                        <input type="hidden" name="filterCIInvoiceNumber">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIOrderNumber">
                        <input type="hidden" name="filterCIOrderNumber">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <label>${message(code:'financials.referenceCodes')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIReference">
                        <input type="hidden" name="filterCIReference">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>

                <div class="field">
                    <label for="filterCIElement">${message(code:'financials.costItemElement')}</label>
                    <select name="filterCIElement" id="filterCIElement" multiple="" class="ui dropdown clearable search selection">
                        <option value=""><g:message code="default.select.all.label"/></option>
                        <g:set var="costItemElementsForFilter" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.COST_ITEM_ELEMENT])}" scope="request"/>
                        <g:each in="${costItemElementsForFilter}" var="rdv">
                            <option value="${"${rdv.class.getName()}:${rdv.id}"}" <%=(filterPresets?.filterCIElement?.contains(rdv)) ? 'selected="selected"' : '' %>>
                                ${rdv.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field">
                    <label for="filterCIStatus">${message(code:'default.status.label')}</label>
                    <select name="filterCIStatus" id="filterCIStatus" multiple="" class="ui dropdown clearable search selection">
                        <option value=""><g:message code="default.select.all.label"/></option>
                        <g:set var="costItemStatusForFilter" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.COST_ITEM_STATUS])}" scope="request"/>
                        <g:each in="${costItemStatusForFilter-RDStore.COST_ITEM_DELETED+RDStore.GENERIC_NULL_VALUE}" var="rdv">
                            <option value="${"${rdv.class.getName()}:${rdv.id}"}" <%=(filterPresets?.filterCIStatus?.contains(rdv)) ? 'selected="selected"' : '' %>>
                                ${rdv.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <ui:datepicker label="financials.financialYear" id="filterCIFinancialYear" name="filterCIFinancialYear" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIFinancialYear}"/>
                </div>

                <div class="field">
                    <ui:datepicker label="financials.invoice_from" id="filterCIInvoiceFrom" name="filterCIInvoiceFrom" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIInvoiceFrom}"/>
                </div>

                <div class="field">
                    <ui:datepicker label="financials.invoice_to" id="filterCIInvoiceTo" name="filterCIInvoiceTo" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIInvoiceTo}"/>
                </div>
            </div>

            <div class="three fields">
                <div class="field">
                    <ui:datepicker label="default.valid_on.label" id="filterCIValidOn" name="filterCIValidOn" placeholder="filter.placeholder" value="${filterPresets?.filterCIValidOn}"/>
                </div>

                <div class="field">
                    <ui:datepicker label="financials.paid_from" id="filterCIPaidFrom" name="filterCIPaidFrom" placeholder="filter.placeholder" value="${filterPresets?.filterCIPaidFrom}"/>
                </div>

                <div class="field">
                    <ui:datepicker label="financials.paid_to" id="filterCIPaidTo" name="filterCIPaidTo" placeholder="filter.placeholder" value="${filterPresets?.filterCIPaidTo}"/>
                </div>
            </div>
            <div class="three fields">

                <div class="field">
                    <ui:datepicker label="financials.dateFrom" id="filterCIDateFrom" name="filterCIDateFrom" placeholder="filter.placeholder" value="${filterPresets?.filterCIDateFrom}"/>
                </div>

                <div class="field">
                    <ui:datepicker label="financials.dateTo" id="filterCIDateTo" name="filterCIDateTo" placeholder="filter.placeholder" value="${filterPresets?.filterCIDateTo}"/>
                </div>
                <div class="field">
                    <div class="ui checkbox">
                        <label for="filterCIUnpaid"><g:message code="financials.costItemUnpaid"/></label>
                        <input id="filterCIUnpaid" name="filterCIUnpaid" type="checkbox" value="true" <g:if test="${filterPresets?.filterCIUnpaid}">checked="checked"</g:if>>
                    </div>
                </div>
            </div>
            <div class="three fields">
                <div class="field">
                    <label for="filterCITaxType"><g:message code="financials.newCosts.taxTypeAndRate"/></label>
                    <%
                        List taxTypesList = []
                        CostItem.TAX_TYPES.every { taxType ->
                            if(taxType == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                taxTypesList.add([key:taxType,value:taxType.taxType.getI10n("value")])
                            else
                                taxTypesList.add([key:taxType,value:taxType.taxType.getI10n("value")+" ("+taxType.taxRate+"%)"])
                        }
                        taxTypesList.add([key:'null',value:"${RDStore.GENERIC_NULL_VALUE.getI10n('value')}"])
                    %>
                    <g:select id="filterCITaxType" class="ui dropdown clearable selection search"
                              name="filterCITaxType"
                              from="${taxTypesList}"
                              optionKey="${{it.key}}"
                              optionValue="${{it.value}}"
                              value="${filterPresets?.filterCITaxType}"
                              noSelection="${['':message(code:'default.select.all.label')]}"/>
                </div>
                <div class="field">
                    <label for="filterCICurrency"><g:message code="default.currency.label"/></label>
                    <g:select id="filterCICurrency" class="ui dropdown clearable selection search"
                              name="filterCICurrency"
                              from="${currenciesList}"
                              optionKey="${{it.id}}"
                              optionValue="${{it.text}}"
                              value="${filterPresets?.filterCICurrency?.id}"
                              noSelection="${['':message(code:'default.select.all.label')]}"/>
                </div>
            </div>
            <div class="three fields">
                <div class="field">
                    <label>${g.message(code: 'financials.costInformationDefinition')}</label>
                    <ui:dropdown name="filterCIIDefinition"
                                 class="filterCIIDefinition clearable"
                                 from="${costInformationDefinitions}"
                                 iconWhich="${Icon.PROP.IS_PRIVATE}"
                                 optionKey="${{genericOIDService.getOID(it)}}"
                                 optionValue="${{ it.getI10n('name') }}"
                                 noSelection="${message(code: 'default.select.choose.label')}"/>
                </div>
                <div class="field" id="filterCIIValueWrapper">
                    <label></label>
                    <select id="filterCIIValue" name="filterCIIValue" multiple="multiple" class="ui fluid multiple search selection dropdown"></select>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}?reset=true" class="${Btn.SECONDARY} reset">${message(code:'default.button.reset.label')}</a>
                    <g:hiddenField name="showView" value="${showView}"/>
                    <input type="submit" name="submit" class="${Btn.PRIMARY}" value="${message(code:'default.button.filter.label')}">
                </div>
            </div>

            <%--<g:hiddenField name="orgId" value="${institution.id}"/>--%>
        </g:form>
    </ui:filter>

<!-- _filter.gsp -->

<laser:script file="${this.getGroovyPageFileName()}">

    $.fn.dropdown.settings.message = {
        noResults: "<g:message code="select2.noMatchesFound" />"
    };
    $("#filterSubStatus, #filterCIStatus").dropdown({
        "clearable": true
    });
    JSPC.app.links = {
        "filterSubProviders": "${createLink([controller:"ajaxJson", action:"lookupProviders"])}?query={query}&forFinanceView=true",
        "filterSubVendors": "${createLink([controller:"ajaxJson", action:"lookupVendors"])}?query={query}&forFinanceView=true",
        "filterCISub": "${createLink([controller:"ajaxJson", action:"lookupSubscriptions"])}?query={query}",
        "filterCIPkg": "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?query={query}",
        "filterCIInvoiceNumber": "${createLink([controller:"ajaxJson", action:"lookupInvoiceNumbers"])}?query={query}",
        "filterCIOrderNumber": "${createLink([controller:"ajaxJson", action:"lookupOrderNumbers"])}?query={query}",
        "filterCIReference": "${createLink([controller:"ajaxJson", action:"lookupReferences"])}?query={query}"
    };
    JSPC.app.setupDropdowns = function () {
        $(".newFilter").each(function(k,v){
            let values = []
            let minCharacters = 0;
            switch($(this).attr("id")) {
                case 'filterCISub':
                    values = [<g:each in="${filterPresets?.filterCISub}" var="ciSub" status="i">'${genericOIDService.getOID(ciSub)}'<g:if test="${i < filterPresets.filterCISub.size()-1}">,</g:if></g:each>];
                    minCharacters = 1; //due to high amount of data
                    break;
                case 'filterCIPkg':
                    values = [<g:each in="${filterPresets?.filterCIPkg}" var="ciPkg" status="i">'${ciPkg.id}'<g:if test="${i < filterPresets.filterCIPkg.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterSubProviders':
                    values = [<g:each in="${filterPresets?.filterSubProviders}" var="subProvider" status="i">'${genericOIDService.getOID(subProvider)}'<g:if test="${i < filterPresets.filterSubProviders.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterSubVendors':
                    values = [<g:each in="${filterPresets?.filterSubVendors}" var="subVendor" status="i">'${genericOIDService.getOID(subVendor)}'<g:if test="${i < filterPresets.filterSubVendors.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCIInvoiceNumber':
                    values = [<g:each in="${filterPresets?.filterCIInvoiceNumber}" var="invoiceNumber" status="i">'${invoiceNumber}'<g:if test="${i < filterPresets.filterCIInvoiceNumber.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCIOrderNumber':
                    values = [<g:each in="${filterPresets?.filterCIOrderNumber}" var="orderNumber" status="i">'${orderNumber}'<g:if test="${i < filterPresets.filterCIOrderNumber.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCIReference':
                    values = [<g:each in="${filterPresets?.filterCIReference}" var="reference" status="i">'${reference}'<g:if test="${i < filterPresets.filterCIReference.size()-1}">,</g:if></g:each>];
                    break;
            }
            $(this).dropdown({
                apiSettings: {
                    url: JSPC.app.links[$(this).attr("id")],
                    cache: false
                },
                clearable: true,
                minCharacters: minCharacters,
                message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.config.language)},
                onChange: function (value, text, $selectedItem) {
                    value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                    if($.inArray($(this).attr("id"), ["filterSubProviders", "filterSubVendors"]) > -1) {
                        JSPC.app.updateSubscriptionDropdown();
                    }
                }
            });
            $(this).dropdown('queryRemote', '', () => {
                $(this).dropdown('set selected', values);
                //if($.inArray($(this).attr("id"), ["filterSubProviders", "filterSubVendors"]) > -1) {
                    //JSPC.app.updateSubscriptionDropdown();
                //}
            });
        });
        $(".newFilter").keypress(function(e){
            if(e.keyCode == 8)
                console.log("backspace event!");
        });
    }
    JSPC.app.updateSubscriptionDropdown = function () {
        if($("#filterSubStatus").length > 0) {
            var subStatus = $("#filterSubStatus").val();
            if(subStatus.length === 0) {
                subStatus = "FETCH_ALL";
            }
        }
        else {
            subStatus = "FETCH_ALL";
        }
        let fixedSubscriptionString = "";
        <g:if test="${fixedSubscription}">
            fixedSubscriptionString = "${fixedSubscription.class.name}:${fixedSubscription.id}"
        </g:if>
        console.log($("#filterSubProviders").dropdown("get values"));
        console.log($("#filterSubVendors").dropdown("get values"));
        $("#filterCISub").dropdown({
            minCharacters: 1,
            apiSettings: {
                url: JSPC.app.links["filterCISub"],
                data: {
                    providerFilter: $("#filterSubProviders").dropdown("get values"),
                    vendorFilter: $("#filterSubVendors").dropdown("get values"),
                    ctx: fixedSubscriptionString,
                    status: subStatus
                },
                cache: false
            }
        });
    }
        $(".filterCIIDefinition").change(function() {
            $("#filterCIIValueWrapper").empty();
            let url = '<g:createLink controller="ajaxJson" action="getPropValues"/>' + '?oid=' + $(this).dropdown('get value');
            <g:if test="${fixedSubscription}">
                url += '&subscription=${fixedSubscription.id}'
            </g:if>
            $.ajax({
                url: url,
                success: function (data) {
                    $("#filterCIIValueWrapper").append('<label></label><select id="filterCIIValue" name="filterCIIValue" multiple="multiple" class="ui fluid multiple search selection dropdown"></select>');
                    $.each(data, function (key, entry) {
                        let filterCIIValue = null;
                        <g:if test="${filterPresets?.filterCIIValue}">
                            filterCIIValue = ${filterPresets?.filterCIIValue};
                        </g:if>
                        if(entry.value == filterCIIValue)
                            $("#filterCIIValue").append($('<option></option>').attr('value', entry.value).attr('selected', 'selected').text(entry.text));
                        else
                            $("#filterCIIValue").append($('<option></option>').attr('value', entry.value).text(entry.text));
                    });
                    $("#filterCIIValue").dropdown();
                }
            });
        });

        <g:if test="${filterPresets?.filterCIUnpaid}">
            $("#filterCIPaidFrom,#filterCIPaidTo").attr("disabled",true);
        </g:if>
        JSPC.app.setupDropdowns();
        <g:if test="${filterPresets?.filterCIIDefinition}">
            $(".filterCIIDefinition").dropdown("set selected","${genericOIDService.getOID(filterPresets?.filterCIIDefinition)}");
        </g:if>

        $("[name='filterCIFinancialYear']").parents(".datepicker").calendar({
            type: 'year',
            onChange: function(date, text, mode) {
                // deal with colored input field only when in filter context
                if ($(this).parents('.la-filter').length) {
                    if (!text) {
                        $(this).removeClass("la-calendar-selected");
                    } else {
                        if( ! $(this).hasClass("la-calendar-selected") ) {
                            $(this).addClass("la-calendar-selected");
                        }
                    }
                }
            },
        });
        $("#filterCIUnpaid").change(function() {
            if($(this).is(":checked"))
                $("#filterCIPaidFrom,#filterCIPaidTo").attr("disabled",true);
            else
                $("#filterCIPaidFrom,#filterCIPaidTo").attr("disabled",false);
        });
        $("#filterCIUnpaid").change(function() {
            if($(this).is(":checked"))
                $("#filterCIPaidFrom,#filterCIPaidTo").attr("disabled",true);
            else
                $("#filterCIPaidFrom,#filterCIPaidTo").attr("disabled",false);
        });

</laser:script>