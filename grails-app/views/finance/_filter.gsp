<!-- _filter.gsp -->
<%@ page import="de.laser.helper.LocaleUtils; de.laser.helper.DateUtils; de.laser.RefdataValue; de.laser.I10nTranslation; org.springframework.context.i18n.LocaleContextHolder; de.laser.helper.DateUtils; java.text.SimpleDateFormat;de.laser.storage.RDStore;de.laser.storage.RDConstants;de.laser.properties.PropertyDefinition;de.laser.OrgRole;de.laser.RefdataCategory;de.laser.FinanceController;de.laser.finance.CostItem" %>
<laser:serviceInjection />

<%
    String locale = LocaleUtils.getCurrentLang()
    String getAllRefDataValuesForCategoryQuery = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv.value_" + locale
%>

    <%--normal semui:filter comes along with more functionality which conflicts with ajax dropdown initialisation, see ERMS-1420--%>
    <laser:render template="/templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <%
            Map<String,Object> formUrl = [controller: 'myInstitution', action: 'finance']
            SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
            if (fixedSubscription) {
                formUrl = [mapping: 'subfinance', params: [sub: "${fixedSubscription?.id}"]]
            }
        %>

        <g:form url="${formUrl}" method="get" class="ui form">

            <div class="three fields">
                <%-- this test includes the check if the filter is called for a subscription consortia --%>
                <g:if test="${subMembers}">
                    <div class="field">
                        <label for="filterSubMembers">
                            <g:message code="${subMemberLabel}"/>
                        </label>
                        <g:select id="filterSubMembers" name="filterSubMembers" multiple="" value="${filterPresets?.filterSubMembers?.collect{ sm -> sm.id }}"
                                  class="ui fluid search dropdown" from="${subMembers}" optionKey="id" optionValue="${{it.getSubscriber().dropdownNamingConvention(institution)}}"
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
                    <label>${message(code:'default.myProviderAgency.label')}</label>
                    <div class="ui multiple search selection dropdown newFilter" id="filterSubProviders">
                        <input type="hidden" name="filterSubProviders">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field">
                        <g:set var="subStatus" value="${RefdataValue.executeQuery(getAllRefDataValuesForCategoryQuery, [category: RDConstants.SUBSCRIPTION_STATUS])}" scope="request"/>
                        <label for="filterSubStatus">${message(code:'subscription.status.label')}</label>
                        <laser:select id="filterSubStatus" class="ui fluid dropdown search" name="filterSubStatus"
                                      from="${subStatus}"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${filterPresets?.filterSubStatus?.id}"
                                      noSelection="${['' : message(code:'default.select.all.label')]}"
                            onchange="JSPC.app.setupDropdowns()"
                        />
                    </div>
                </g:if>
            </div>

            <div class="three fields">
                <div class="field">
                    <label for="filterCITitle">${message(code:'financials.newCosts.costTitle')}
                        <span data-position="right center" class="la-popup-tooltip la-delay" data-content="${message(code:'financials.title.tooltip')}">
                            <i class="question circle icon"></i>
                        </span>
                    </label>
                    <div class="ui search selection dropdown allowAdditions" id="filterCITitle">
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
                    <div class="ui search selection multiple dropdown newFilter" id="filterCISPkg">
                        <input type="hidden" name="filterCISPkg">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <label>${message(code:'financials.budgetCode')}</label>
                    <div class="ui search selection multiple dropdown newFilter" id="filterCIBudgetCode">
                        <input type="hidden" name="filterCIBudgetCode">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
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
                    <select name="filterCIElement" id="filterCIElement" multiple="" class="ui dropdown search selection">
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
                    <select name="filterCIStatus" id="filterCIStatus" multiple="" class="ui dropdown search selection">
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
                    <semui:datepicker label="financials.financialYear" id="filterCIFinancialYear" name="filterCIFinancialYear" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIFinancialYear}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.invoice_from" id="filterCIInvoiceFrom" name="filterCIInvoiceFrom" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIInvoiceFrom}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.invoice_to" id="filterCIInvoiceTo" name="filterCIInvoiceTo" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIInvoiceTo}"/>
                </div>
            </div>

            <div class="three fields">
                <div class="field">
                    <semui:datepicker label="default.valid_on.label" id="filterCIValidOn" name="filterCIValidOn" placeholder="filter.placeholder" value="${filterPresets?.filterCIValidOn}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.paid_from" id="filterCIPaidFrom" name="filterCIPaidFrom" placeholder="filter.placeholder" value="${filterPresets?.filterCIPaidFrom}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.paid_to" id="filterCIPaidTo" name="filterCIPaidTo" placeholder="filter.placeholder" value="${filterPresets?.filterCIPaidTo}"/>
                </div>
            </div>
            <div class="three fields">

                <div class="field">
                    <semui:datepicker label="financials.dateFrom" id="filterCIDateFrom" name="filterCIDateFrom" placeholder="filter.placeholder" value="${filterPresets?.filterCIDateFrom}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.dateTo" id="filterCIDateTo" name="filterCIDateTo" placeholder="filter.placeholder" value="${filterPresets?.filterCIDateTo}"/>
                </div>
                <div class="field">
                    <div class="ui checkbox">
                        <label for="filterCIUnpaid"><g:message code="financials.costItemUnpaid"/></label>
                        <input id="filterCIUnpaid" name="filterCIUnpaid" type="checkbox" value="true" <g:if test="${params.filterCIUnpaid}">checked="checked"</g:if>>
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
                    <g:select id="filterCITaxType" class="ui dropdown selection search"
                              name="filterCITaxType"
                              from="${taxTypesList}"
                              optionKey="${{it.key}}"
                              optionValue="${{it.value}}"
                              value="${filterPresets?.filterCITaxType}"
                              noSelection="${['':message(code:'default.select.all.label')]}"/>
                </div>
                <div class="field">
                    <label for="filterCICurrency"><g:message code="default.currency.label"/></label>
                    <g:select id="filterCICurrency" class="ui dropdown selection search"
                              name="filterCICurrency"
                              from="${currenciesList}"
                              optionKey="${{it.id}}"
                              optionValue="${{it.text}}"
                              value="${filterPresets?.filterCICurrency?.id}"
                              noSelection="${['':message(code:'default.select.all.label')]}"/>
                </div>
                <div class="field la-field-right-aligned">
                    <a href="${request.forwardURI}?reset=true" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                    <g:hiddenField name="showView" value="${showView}"/>
                    <input type="submit" name="submit" class="ui secondary button" value="${message(code:'default.button.filter.label')}">
                </div>
            </div>

            <%--<g:hiddenField name="orgId" value="${institution.id}"/>--%>
        </g:form>
    </semui:filter>

<!-- _filter.gsp -->

<laser:script file="${this.getGroovyPageFileName()}">

    $.fn.dropdown.settings.message = {
        noResults: "<g:message code="select2.noMatchesFound" />"
    };
    $("#filterSubStatus, #filterCIStatus").dropdown({
        "clearable": true
    });
    JSPC.app.setupDropdowns = function () {
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
            fixedSubscriptionString = "&ctx=${fixedSubscription.class.name}:${fixedSubscription.id}"
        </g:if>
        const links = {
            "filterSubProviders": "${createLink([controller:"ajaxJson", action:"lookupProvidersAgencies"])}?query={query}&forFinanceView=true",
            "filterCISub": "${createLink([controller:"ajaxJson", action:"lookupSubscriptions"])}?status="+subStatus+"&query={query}",
            "filterCISPkg": "${createLink([controller:"ajaxJson", action:"lookupSubscriptionPackages"])}?status="+subStatus+fixedSubscriptionString+"&query={query}",
            "filterCIBudgetCode": "${createLink([controller:"ajaxJson", action:"lookupBudgetCodes"])}?query={query}",
            "filterCIInvoiceNumber": "${createLink([controller:"ajaxJson", action:"lookupInvoiceNumbers"])}?query={query}",
            "filterCIOrderNumber": "${createLink([controller:"ajaxJson", action:"lookupOrderNumbers"])}?query={query}",
            "filterCIReference": "${createLink([controller:"ajaxJson", action:"lookupReferences"])}?query={query}"
        };
        $(".newFilter").each(function(k,v){
            let values = []
            switch($(this).attr("id")) {
                case 'filterCISub':
                    values = [<g:each in="${filterPresets?.filterCISub}" var="ciSub" status="i">'${genericOIDService.getOID(ciSub)}'<g:if test="${i < filterPresets.filterCISub.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCISPkg':
                    values = [<g:each in="${filterPresets?.filterCISPkg}" var="ciSPkg" status="i">'${genericOIDService.getOID(ciSPkg)}'<g:if test="${i < filterPresets.filterCISPkg.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterSubProviders':
                    values = [<g:each in="${filterPresets?.filterSubProviders}" var="subProvider" status="i">'${genericOIDService.getOID(subProvider)}'<g:if test="${i < filterPresets.filterSubProviders.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCIBudgetCode':
                    values = [<g:each in="${filterPresets?.filterCIBudgetCode}" var="budgetCode" status="i">'${budgetCode.id}'<g:if test="${i < filterPresets.filterCIBudgetCode.size()-1}">,</g:if></g:each>];
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
            $(this).dropdown('set value',values);
            $(this).dropdown({
                apiSettings: {
                    url: links[$(this).attr("id")],
                    cache: false
                },
                clearable: true,
                minCharacters: 0,
                message: {noResults:JSPC.dict.get('select2.noMatchesFound', JSPC.currLanguage)},
                onChange: function (value, text, $selectedItem) {
                    value !== '' ? $(this).addClass("la-filter-selected") : $(this).removeClass("la-filter-selected");
                }
            });
        });
        $(".newFilter").keypress(function(e){
            if(e.keyCode == 8)
                console.log("backspace event!");
        });
    }

        <g:if test="${params.filterCIUnpaid}">
            $("#filterCIPaidFrom,#filterCIPaidTo").attr("disabled",true);
        </g:if>
        JSPC.app.setupDropdowns();

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

</laser:script>