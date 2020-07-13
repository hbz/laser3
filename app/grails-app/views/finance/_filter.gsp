<!-- _filter.gsp -->
<%@ page import="de.laser.helper.DateUtil; java.text.SimpleDateFormat;de.laser.helper.RDStore;de.laser.helper.RDConstants;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.FinanceController;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />


    <%--normal semui:filter comes along with more functionality which conflicts with ajax dropdown initialisation, see ERMS-1420--%>
    <g:render template="/templates/filter/javascript" />
    <semui:filter showFilterButton="true">
        <%
            Map<String,Object> formUrl = [controller: 'myInstitution', action: 'finance']
            SimpleDateFormat sdf = de.laser.helper.DateUtil.getSDF_NoTime()
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
                        <g:select id="filterSubMembers" name="filterSubMembers" multiple="" value="${filterPresets?.filterSubMembers}"
                                  class="ui fluid search dropdown" from="${subMembers}" optionKey="id" optionValue="${{it.getSubscriber().dropdownNamingConvention(institution)}}"
                                  noSelection="${['':message(code:'default.select.all.label')]}"
                        />
                    </div>
                </g:if>
                <g:if test="${consMembers}">
                    <div class="field">
                        <label for="filterSubMembers">
                            <g:message code="${subMemberLabel}"/>
                        </label>
                        <g:select id="filterConsMembers" name="filterConsMembers" multiple="" value="${filterPresets?.filterConsMembers}"
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
                    <%--<g:select id="filterSubProviders" from="" name="filterSubProviders" multiple="" value="${params.filterSubProviders}"
                        class="ui fluid search dropdown" optionKey="id" optionValue="name" noSelection="${['' : message(code:'default.select.all.label')]}"
                    />--%>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field">
                        <label for="filterSubStatus">${message(code:'subscription.status.label')}</label>
                        <laser:select id="filterSubStatus" class="ui fluid dropdown search" name="filterSubStatus"
                                      from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${filterPresets?.filterSubStatus?.id}"
                                      noSelection="${['' : message(code:'default.select.all.label')]}"
                            onchange="setupDropdowns()"
                        />
                    </div>
                </g:if>
            </div>

            <div class="three fields">
                <div class="field">
                    <label for="filterCITitle">${message(code:'financials.newCosts.costTitle')}</label>
                    <input id="filterCITitle" name="filterCITitle" type="text" value="${filterPresets?.filterCITitle}"/>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field fieldcontain"><!--NEW -->
                        <label>${message(code:'default.subscription.label')}</label>
                        <div class="ui search selection multiple dropdown newFilter" id="filterCISub">
                            <input type="hidden" name="filterCISub">
                            <i class="dropdown icon"></i>
                            <input type="text" class="search">
                            <div class="default text"><g:message code="default.select.all.label"/></div>
                        </div>
                    </div>
                </g:if>
                <div class="field fieldcontain"><!--NEW -->
                    <label>${message(code:'package.label')}</label>
                    <div class="ui search selection multiple dropdown newFilter" id="filterCISPkg">
                        <input type="hidden" name="filterCISPkg">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                    <%--<g:select id="filterCISPkg" class="ui fluid search dropdown" multiple=""
                              name="filterCISPkg"
                              from=""
                              optionValue="text"
                              optionKey="id"
                              noSelection="['':message(code:'default.select.all.label')]"
                              value="${filterPresets?.filterCISPkg}" />--%>
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
                    <%--<g:select id="filterCIBudgetCode" class="ui dropdown search selection"
                              name="filterCIBudgetCode"
                              from=""
                              optionKey="id" optionValue="value"
                              value="${filterPresets?.filterCIBudgetCode}"
                              noSelection="${['':message(code:'default.select.all.label')]}" /> --%>
                </div>

                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIInvoiceNumber">
                        <input type="hidden" name="filterCIInvoiceNumber">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                    <%--<g:select id="filterCIInvoiceNumber" class="ui dropdown search selection"
                              name="filterCIInvoiceNumber"
                              from=""
                              value="${filterPresets?.filterCIInvoiceNumber}"
                              noSelection="${['':message(code:'default.select.all.label')]}" />--%>
                </div>

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIOrderNumber">
                        <input type="hidden" name="filterCIOrderNumber">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                    <%--<g:select id="filterCIOrderNumber" class="ui dropdown search selection"
                              name="filterCIOrderNumber"
                              from=""
                              value="${filterPresets?.filterCIOrderNumber}"
                              noSelection="${['':message(code:'default.select.all.label')]}" />--%>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field fieldcontain">
                    <label>${message(code:'financials.referenceCodes')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIReference">
                        <input type="hidden" name="filterCIReference">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text"><g:message code="default.select.all.label"/></div>
                    </div>
                    <%--<g:select id="filterCIReference" class="ui dropdown search selection"
                              name="filterCIReference"
                              from=""
                              value="${filterPresets?.filterCIReference}"
                              noSelection="${['':message(code:'default.select.all.label')]}" />--%>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIElement">${message(code:'financials.costItemElement')}</label>
                    <select name="filterCIElement" id="filterCIElement" multiple="" class="ui dropdown search selection">
                        <option value=""><g:message code="default.select.all.label"/></option>
                        <g:each in="${RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT)}" var="rdv">
                            <option value="${"${rdv.class.getName()}:${rdv.id}"}" <%=(filterPresets?.filterCIElement?.contains(rdv)) ? 'selected="selected"' : '' %>>
                                ${rdv.getI10n("value")}
                            </option>
                        </g:each>
                    </select>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIStatus">${message(code:'default.status.label')}</label>
                    <select name="filterCIStatus" id="filterCIStatus" multiple="" class="ui dropdown search selection">
                        <option value=""><g:message code="default.select.all.label"/></option>
                        <g:each in="${[RDStore.GENERIC_NULL_VALUE]+RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS)-RDStore.COST_ITEM_DELETED}" var="rdv">
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
                    <semui:datepicker label="default.valid_on.label" id="filterCIValidOn" name="filterCIValidOn" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIValidOn}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.paid_from" id="filterCIPaidFrom" name="filterCIPaidFrom" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIPaidFrom}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.paid_to" id="filterCIPaidTo" name="filterCIPaidTo" placeholder="filter.placeholder"
                                      value="${filterPresets?.filterCIPaidTo}"/>
                </div>
            </div>

            <div class="three fields">
                <div class="field"><!-- here comes the new field for tax rate, see ERMS-1046 -->
                <%--
                <label for="filterCICategory">${message(code:'financials.costItemCategory')}</label>
                <laser:select id="filterCICategory" class="ui dropdown selection"
                              name="filterCICategory"
                              from="${costItemCategory}"
                              optionKey="${{it.class.getName() + ":" + it.id}}"
                              optionValue="value"
                              value="${filterPresets?.filterCICategory}"
                              noSelection="${['':message(code:'default.select.all.label')]}"/>
                --%>
                    <label for="filterCITaxType">${message(code:'financials.newCosts.taxTypeAndRate')}</label>
                    <%
                        List taxTypesList = [[key:'null',value:"${RDStore.GENERIC_NULL_VALUE.getI10n('value')}"]]
                        CostItem.TAX_TYPES.every { taxType ->
                            if(taxType == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                taxTypesList.add([key:taxType,value:taxType.taxType.getI10n("value")])
                            else
                                taxTypesList.add([key:taxType,value:taxType.taxType.getI10n("value")+" ("+taxType.taxRate+"%)"])
                        }
                    %>
                    <g:select id="filterCITaxType" class="ui dropdown selection search"
                              name="filterCITaxType"
                              from="${taxTypesList}"
                              optionKey="${{it.key}}"
                              optionValue="${{it.value}}"
                              value="${filterPresets?.filterCITaxType}"
                              noSelection="${['':message(code:'default.select.all.label')]}"/>
                </div>
                <div class="field"></div>
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

<r:script>
    $.fn.dropdown.settings.message = {
        noResults: "<g:message code="select2.noMatchesFound" />"
    };
    $("#filterSubStatus, #filterCIStatus").dropdown({
        "clearable": true
    });
    function setupDropdowns() {
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
            "filterSubProviders": "${createLink([controller:"ajax",action:"lookupProvidersAgencies"])}?query={query}&forFinanceView=true",
            "filterCISub": "${createLink([controller:"ajax",action:"lookupSubscriptions"])}?status="+subStatus+"&query={query}",
            "filterCISPkg": "${createLink([controller:"ajax",action:"lookupSubscriptionPackages"])}?status="+subStatus+fixedSubscriptionString+"&query={query}",
            "filterCIBudgetCode": "${createLink([controller:"ajax",action:"lookupBudgetCodes"])}?query={query}",
            "filterCIInvoiceNumber": "${createLink([controller:"ajax",action:"lookupInvoiceNumbers"])}?query={query}",
            "filterCIOrderNumber": "${createLink([controller:"ajax",action:"lookupOrderNumbers"])}?query={query}",
            "filterCIReference": "${createLink([controller:"ajax",action:"lookupReferences"])}?query={query}"
        };
        $(".newFilter").each(function(k,v){
            let values = []
            switch($(this).attr("id")) {
                case 'filterCISub':
                    values = [<g:each in="${filterPresets?.filterCISub}" var="ciSub" status="i">'${com.k_int.kbplus.GenericOIDService.getOID(ciSub)}'<g:if test="${i < filterPresets.filterCISub.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCISPkg':
                    values = [<g:each in="${filterPresets?.filterCISPkg}" var="ciSPkg" status="i">'${com.k_int.kbplus.GenericOIDService.getOID(ciSPkg)}'<g:if test="${i < filterPresets.filterCISPkg.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterSubProviders':
                    values = [<g:each in="${filterPresets?.filterSubProviders}" var="subProvider" status="i">'${com.k_int.kbplus.GenericOIDService.getOID(subProvider)}'<g:if test="${i < filterPresets.filterSubProviders.size()-1}">,</g:if></g:each>];
                    break;
                case 'filterCIBudgetCode':
                    values = [<g:each in="${filterPresets?.filterCIBudgetCode}" var="budgetCode" status="i">'${budgetCode}'<g:if test="${i < filterPresets.filterCIBudgetCode.size()-1}">,</g:if></g:each>];
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
                minCharacters: 0
            });
        });
        $(".newFilter").keypress(function(e){
            if(e.keyCode == 8)
                console.log("backspace event!");
        });
    }
    $(document).ready(function(){
        setupDropdowns();
        $("[name='filterCIFinancialYear']").parents(".datepicker").calendar({
            type: 'year'
        });
    });
</r:script>