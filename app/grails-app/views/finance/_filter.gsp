<!-- _filter.gsp -->
<%@ page import="de.laser.helper.DateUtil; java.text.SimpleDateFormat;de.laser.helper.RDStore;de.laser.helper.RDConstants;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.FinanceController;com.k_int.kbplus.CostItem" %>
<laser:serviceInjection />


    <%--normal semui:filter comes along with more functionality which conflicts with ajax dropdown initialisation, see ERMS-1420--%>
    <g:render template="../templates/filter/javascript" />
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
                        <input type="hidden" name="filterSubProviders" value="${filterPresets?.filterSubProviders}">
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
                            <input type="hidden" name="filterCISub" value="${filterPresets?.filterCISub}">
                            <i class="dropdown icon"></i>
                            <input type="text" class="search">
                            <div class="default text"><g:message code="default.select.all.label"/></div>
                        </div>
                    </div>
                </g:if>
                <div class="field fieldcontain"><!--NEW -->
                    <label>${message(code:'package.label')}</label>
                    <div class="ui search selection multiple dropdown newFilter" id="filterCISPkg">
                        <input type="hidden" name="filterCISPkg" value="${filterPresets?.filterCISPkg}">
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
                        <input type="hidden" name="filterCIBudgetCode" value="${filterPresets?.filterCIBudgetCode}">
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
                        <input type="hidden" name="filterCIInvoiceNumber" value="${filterPresets?.filterCIInvoiceNumber}">
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
                        <input type="hidden" name="filterCIOrderNumber" value="${filterPresets?.filterCIOrderNumber}">
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
                        <input type="hidden" name="filterCIReference" value="${filterPresets?.filterCIReference}">
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
                    <laser:select id="filterCIElement" class="ui dropdown selection search"
                                  name="filterCIElement"
                                  from="${RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_ELEMENT)}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${filterPresets?.filterCIElement}"
                                  noSelection="${['':message(code:'default.select.all.label')]}"/>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIStatus">${message(code:'default.status.label')}</label>
                    <laser:select id="filterCIStatus" class="ui dropdown selection search"
                                  name="filterCIStatus"
                                  from="${[de.laser.helper.RDStore.GENERIC_NULL_VALUE]+RefdataCategory.getAllRefdataValues(RDConstants.COST_ITEM_STATUS)-de.laser.helper.RDStore.COST_ITEM_DELETED}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${filterPresets?.filterCIStatus}"
                                  noSelection="${['':message(code:'default.select.all.label')]}"/>
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