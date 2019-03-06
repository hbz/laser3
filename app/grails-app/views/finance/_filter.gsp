<!-- _filter.gsp -->
<%@ page import="java.text.SimpleDateFormat; de.laser.helper.RDStore; com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.FinanceController" %>
<laser:serviceInjection />

    <semui:filter>
        <%
            def formUrl = [controller: 'myInstitution', action: 'finance']
            SimpleDateFormat sdf = new SimpleDateFormat(message(code:'default.date.format.notime'))
            if (fixedSubscription) {
                formUrl = [mapping: 'subfinance', params: [sub: "${fixedSubscription?.id}"]]
            }
        %>

        <g:form url="${formUrl}" method="get" class="ui form">

            <div class="three fields">
                <%-- this test includes the check if the filter is called for a subscription consortia --%>
                <g:if test="${subscriptionParticipants && !showView.equals("consAtSubscr")}">
                    <div class="field">
                        <label for="filterSubMembers">${message(code:'subscription.details.members.label')}</label>
                        <g:select id="filterSubMembers" name="filterSubMembers" multiple="" value="${filterPreset?.filterSubMembers}"
                                  class="ui fluid search dropdown" from="${subscriptionParticipants}" optionKey="id" optionValue="name"
                                  noSelection="${['':'Alle ..']}"
                        />
                    </div>
                </g:if>
                <div class="field">
                    <label>${message(code:'default.provider.label')}</label>
                    <div class="ui multiple search selection dropdown newFilter" id="filterSubProviders">
                        <input type="hidden" name="filterSubProviders" value="${params.filterSubProviders}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text">Alle ...</div>
                    </div>
                    <%--<g:select id="filterSubProviders" from="" name="filterSubProviders" multiple="" value="${params.filterSubProviders}"
                        class="ui fluid search dropdown" optionKey="id" optionValue="name" noSelection="${['' : 'Alle ..']}"
                    />--%>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field">
                        <%
                            List fakeList = []
                            RefdataCategory.getAllRefdataValues('Subscription Status').each { rdv ->
                                if(!rdv.equals(RDStore.SUBSCRIPTION_DELETED))
                                    fakeList.add(rdv)
                            }
                            fakeList.add(RefdataValue.getByValueAndCategory('subscription.status.no.status.set.but.null', 'filter.fake.values'))
                        %>
                        <label for="filterSubStatus">${message(code:'subscription.status.label')}</label>
                        <laser:select id="filterSubStatus" class="ui fluid dropdown" name="filterSubStatus"
                                      from="${ fakeList }"
                                      optionKey="id"
                                      optionValue="value"
                                      value="${params.filterSubStatus}"
                                      noSelection="${['' : 'Alle ..']}"
                        />
                    </div>
                </g:if>
            </div>

            <div class="three fields">
                <div class="field">
                    <label for="filterCITitle">${message(code:'financials.newCosts.costTitle')}</label>
                    <input id="filterCITitle" name="filterCITitle" type="text" value="${params.filterCITitle}"/>
                </div>
                <g:if test="${!fixedSubscription}">
                    <div class="field fieldcontain"><!--NEW -->
                        <label>${message(code:'subscription.label')}</label>
                        <div class="ui search selection multiple dropdown newFilter" id="filterCISub">
                            <input type="hidden" name="filterCISub" value="${params.filterCISub}">
                            <i class="dropdown icon"></i>
                            <input type="text" class="search">
                            <div class="default text">Alle ...</div>
                        </div>
                    </div>
                </g:if>
                <div class="field fieldcontain"><!--NEW -->
                    <label>${message(code:'package.label')}</label>
                    <div class="ui search selection multiple dropdown newFilter" id="filterCISPkg">
                        <input type="hidden" name="filterCISPkg" value="${params.filterCISPkg}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text">Alle ...</div>
                    </div>
                    <%--<g:select id="filterCISPkg" class="ui fluid search dropdown" multiple=""
                              name="filterCISPkg"
                              from=""
                              optionValue="text"
                              optionKey="id"
                              noSelection="['':'Alle ..']"
                              value="${params.filterCISPkg}" />--%>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <label>${message(code:'financials.budgetCode')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIBudgetCode">
                        <input type="hidden" name="filterCIBudgetCode" value="${params.filterCIBudgetCode}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text">Alle ...</div>
                    </div>
                    <%--<g:select id="filterCIBudgetCode" class="ui dropdown search selection"
                              name="filterCIBudgetCode"
                              from=""
                              optionKey="id" optionValue="value"
                              value="${params.filterCIBudgetCode}"
                              noSelection="${['':'Alle ..']}" /> --%>
                </div>

                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIInvoiceNumber">
                        <input type="hidden" name="filterCIInvoiceNumber" value="${params.filterCIInvoiceNumber}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text">Alle ...</div>
                    </div>
                    <%--<g:select id="filterCIInvoiceNumber" class="ui dropdown search selection"
                              name="filterCIInvoiceNumber"
                              from=""
                              value="${params.filterCIInvoiceNumber}"
                              noSelection="${['':'Alle ..']}" />--%>
                </div>

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIOrderNumber">
                        <input type="hidden" name="filterCIOrderNumber" value="${params.filterCIOrderNumber}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text">Alle ...</div>
                    </div>
                    <%--<g:select id="filterCIOrderNumber" class="ui dropdown search selection"
                              name="filterCIOrderNumber"
                              from=""
                              value="${params.filterCIOrderNumber}"
                              noSelection="${['':'Alle ..']}" />--%>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field fieldcontain">
                    <label>${message(code:'financials.referenceCodes')}</label>
                    <div class="ui search selection dropdown newFilter" id="filterCIReference">
                        <input type="hidden" name="filterCIReference" value="${params.filterCIReference}">
                        <i class="dropdown icon"></i>
                        <input type="text" class="search">
                        <div class="default text">Alle ...</div>
                    </div>
                    <%--<g:select id="filterCIReference" class="ui dropdown search selection"
                              name="filterCIReference"
                              from=""
                              value="${params.filterCIReference}"
                              noSelection="${['':'Alle ..']}" />--%>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIElement">${message(code:'financials.costItemElement')}</label>
                    <laser:select id="filterCIElement" class="ui dropdown selection"
                                  name="filterCIElement"
                                  from="${allCIElements}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${params.filterCIElement}"
                                  noSelection="${['':'Alle ..']}"/>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIStatus">${message(code:'financials.costItemStatus')}</label>
                    <laser:select id="filterCIStatus" class="ui dropdown selection"
                                  name="filterCIStatus"
                                  from="${RefdataCategory.getAllRefdataValues("CostItemStatus")}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${params.filterCIStatus}"
                                  noSelection="${['':'Alle ..']}"/>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <semui:datepicker label="financials.financialYear" name="filterCIFinancialYear" placeholder="filter.placeholder"
                                      value="${params.filterCIFinancialYear}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.invoice_from" name="filterCIInvoiceFrom" placeholder="filter.placeholder"
                                      value="${params.filterCIInvoiceFrom}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.invoice_to" name="filterCIInvoiceTo" placeholder="filter.placeholder"
                                      value="${params.filterCIInvoiceTo}"/>
                </div>
            </div>

            <div class="three fields">
                <div class="field">
                    <semui:datepicker label="default.valid_on.label" name="filterCIValidOn" placeholder="filter.placeholder"
                                      value="${params.filterCIValidOn}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.paid_from" name="filterCIPaidFrom" placeholder="filter.placeholder"
                                      value="${params.filterCIPaidFrom}"/>
                </div>

                <div class="field">
                    <semui:datepicker label="financials.paid_to" name="filterCIPaidTo" placeholder="filter.placeholder"
                                      value="${params.filterCIPaidTo}"/>
                </div>
            </div>

            <div class="three fields">
                <div class="field fieldcontain"><!-- here comes the new field for tax rate, see ERMS-1046 -->
                <%--
                <label for="filterCICategory">${message(code:'financials.costItemCategory')}</label>
                <laser:select id="filterCICategory" class="ui dropdown selection"
                              name="filterCICategory"
                              from="${costItemCategory}"
                              optionKey="${{it.class.getName() + ":" + it.id}}"
                              optionValue="value"
                              value="${params.filterCICategory}"
                              noSelection="${['':'Alle ..']}"/>

                    <label for="filterCITaxType">${message(code:'financials.newCosts.controllable')}</label>
                    <% println params.taxType %>
                    <laser:select id="filterCITaxType" class="ui dropdown selection"
                                  name="filterCITaxType"
                                  from="${RefdataCategory.getAllRefdataValues("TaxType")}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${params.taxType}"
                                  noSelection="${['':'Alle ..']}"/>--%>
                </div>
                <div class="field">
                    <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                    <input type="submit" name="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                </div>
            </div>

            <g:hiddenField name="orgId" value="${contextService.getOrg()?.id}"/>
        </g:form>
    </semui:filter>

<!-- _filter.gsp -->

<r:script>
    $(document).ready(function(){
        var links = {
            "filterSubProviders": "${createLink([controller:"ajax",action:"lookupProviders"])}?query={query}",
            "filterCISub": "${createLink([controller:"ajax",action:"lookupSubscriptions"])}?query={query}",
            "filterCISPkg": "${createLink([controller:"ajax",action:"lookupSubscriptionPackages"])}?query={query}${fixedSubscription ? '&ctx='+fixedSubscription.class.name+':'+fixedSubscription.id : ''}",
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
        $("[name='filterCIFinancialYear']").parents(".datepicker").calendar({
            type: 'year'
        });
    });
</r:script>