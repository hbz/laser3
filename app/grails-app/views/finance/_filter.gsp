<!-- _filter.gsp -->
<%@ page import="com.k_int.kbplus.OrgRole;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.properties.PropertyDefinition" %>

<% def contextService = grailsApplication.mainContext.getBean("contextService") %>

<g:if test="${false}"><!-- TMP::IGNORE LEGACY FILTER -->

%{--AJAX rendered messages--}%
<g:if test="${info}">
    <div id="info" >
        <table id="financeErrors" class="ui striped celled table">
            <thead>
            <tr>
                <th>Problem/Update</th>
                <th>Info</th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${info}" var="i">
                <tr>
                    <td>${i.status.encodeAsHTML()}</td>
                    <td>${i.msg.encodeAsHTML()}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</g:if>

%{--Basic static help text--}%
<g:render template="help" />

<semui:filter>
    <g:form id="filterView" class="ui form" action="index" method="post">
        <input type="hidden" name="shortcode" value="${contextService.getOrg()?.shortcode}"/>

        <div class="three fields">
            <div class="field">
                <label for="adv_codes">${message(code:'financials.budgetCode')}</label>
                <input id="adv_codes" name="adv_codes" type="text"/>
            </div>
            <div class="field">
                <label for="adv_costItemCategory">${message(code:'financials.costItemCategory')}</label>

                <laser:select id="adv_costItemCategory" class="ui dropdown"
                          name="adv_costItemCategory"
                          from="${costItemCategory}"
                          optionKey="id"
                          optionValue="value"
                          noSelection="${['':'Alle ..']}"/>
            </div>
            <div class="field required">

                <label>${message(code:'subscription.label')}</label>
                <g:if test="${inSubMode == true}">
                    <input name="subscriptionFilter" id="subscriptionFilter" class="la-full-width" value="${fixedSubscription?.name}" disabled="disabled"
                           data-filterMode="${fixedSubscription.class.name}:${fixedSubscription.id}"  />
                </g:if>
                <g:else>
                    <input type="text" name="subscriptionFilter" class="la-full-width" data-filterMode="" id="subscriptionFilter" value="${params.subscriptionFilter}" />
                </g:else>

                <g:hiddenField name="sub" value="${fixedSubscription?.id}"></g:hiddenField>
            </div>

        </div><!-- row1 -->

        <div class="three fields">
            <div class="field required">
                <label>${message(code:'financials.invoice_number')}</label><!-- invoice -->
                <input id="filterInvoiceNumber" name="invoiceNumberFilter"
                       type="text" class="filterUpdated"
                       value="${params.invoiceNumberFilter}" />
            </div>

            <div class="field">
                <label for="adv_costItemStatus">${message(code:'financials.costItemStatus')}</label>
                <laser:select id="adv_costItemStatus" class="ui dropdown"
                          name="adv_costItemStatus"
                          from="${costItemStatus}"
                          optionKey="id"
                          optionValue="value"
                          noSelection="${['':'Alle ..']}"/>
            </div>
            <div class="field required">
                <label>${message(code:'package.label')}</label>
                <input type="text" name="packageFilter" class="filterUpdated la-full-width" id="packageFilter" value="${params.packageFilter}" />
            </div>

        </div><!-- row2 -->

        <div class="three fields">

            <div class="field required">
                <label>${message(code:'financials.order_number')}</label>
                <input type="text" name="orderNumberFilter"
                       class="filterUpdated"
                       id="filterOrderNumber"  value="${params.orderNumberFilter}" data-type="select"/>
            </div>

            <div class="field">
                <label>Steuer</label>
                <laser:select id="taxCode" name="taxCode" class="ui dropdown" disabled="disabled"
                          from="${taxType}"
                          optionKey="id"
                          optionValue="value"
                          noSelection="${['':'Alle ..']}"/>
            </div>

            <div class="field">
                <label for="adv_ie">${message(code:'financials.newCosts.singleEntitlement')}</label>
                <input id="adv_ie" name="adv_ie" class="input-large" type="text" disabled="disabled" />
            </div>
        </div><!-- row3 -->

        <div class="three fields">
            <div class="field">
            </div>
            <div class="field">
            </div>
            <div class="two fields">
                <div class="field">
                    <%--<span ${wildcard && filterMode=='ON'? hidden="hidden" : ''}>
                        (${g.message(code: 'financials.help.wildcard')} : <g:checkBox name="wildcard" title="${g.message(code: 'financials.wildcard.title')}" type="checkbox" value="${wildcard}"></g:checkBox> )
                    </span>--%>
                    <input type="hidden" name="wildcard" value="on" />
                    <label>&nbsp;</label>
                    <div id="filtering" data-toggle="buttons-radio">
                        <g:if test="${filterMode=='OFF'}">
                            <g:select name="filterMode" from="['OFF','ON']" type="button" class="ui button"></g:select>
                        </g:if>
                        <g:hiddenField type="hidden" name="resetMode" value="${params.resetMode}"></g:hiddenField>
                        <%--<g:submitButton name="submitFilterMode" id="submitFilterMode" class="ui button"  value="${filterMode=='ON'?'reset':'search'}" title="${g.message(code: 'financials.pagination.title')}"></g:submitButton>--%>
                    </div>
                </div>
                <div class="field">
                    <label>&nbsp;</label>
                    <g:submitButton name="submitFilterMode" id="submitFilterMode" class="ui secondary button" value="${filterMode=='ON'?'reset':'search'}" title="${g.message(code: 'financials.pagination.title')}"></g:submitButton>
                </div>
            </div>
        </div><!-- row4 -->

        <%-- advanced legacy filter fields here --%>
        <%-- advanced legacy filter fields here --%>
        <%-- advanced legacy filter fields here --%>

        <%--
        <div class="three fields">
            <div class="two fields">
                <div class="field">
                    <label for="adv_datePaid">Date Paid</label>
                    <select name="_adv_datePaidType" class="input-mini"  id="adv_datePaidType">
                        <option value="">N/A</option>
                        <option value="eq">==</option>
                        <option value="gt">&gt;</option>
                        <option value="gt">&lt;</option>
                    </select>
                </div>
                <semui:datepicker label="financials.datePaid" name="newDate" placeholder ="financials.datePaid" value="${params.newDate}" />
            </div>
        </div>

        <div class="three fields">
            <div class="two fields">
                <div class="field">
                    <label for="adv_amount">Local Amount </label>
                    <select name="_adv_amountType" class="input-mini"  id="adv_amountType">
                        <option value="">N/A</option>
                        <option value="eq">==</option>
                        <option value="gt">&gt;</option>
                        <option value="gt">&lt;</option>
                    </select>
                </div>
                <div class="field">
                    <label>&nbsp;</label>
                    <input id="adv_amount" name="adv_amount" type="number" step="0.01" />
                </div>
            </div>
        </div>

        <div class="three fields">
            <div class="two fields">
                <semui:datepicker label ="datamanager.changeLog.from_date" name="newStartDate" placeholder ="default.date.label" />
                <semui:datepicker label ="datamanager.changeLog.to_date" name="newEndDate" placeholder ="default.date.label" value ="${params.endDate}" />
            </div>

            <div class="field">
                <label for="adv_ref">Cost Reference</label>
                <input id="adv_ref" name="adv_ref" />
            </div>
        </div>
        --%>

    </g:form>

</semui:filter>

</g:if><!-- TMP::IGNORE LEGACY FILTER -->

    <semui:filter>
        <%
            def formUrl = [controller: 'myInstitution', action: 'finance']

            if (fixedSubscription) {
                formUrl = [mapping: 'subfinance', params: [sub: "${fixedSubscription?.id}"]]
            }
        %>

        <g:form url="${formUrl}" method="get" class="ui form">

            <div class="three fields">
                <div class="field">
                    <label for="filterCITitle">${message(code:'financials.newCosts.costTitle')}</label>
                    <input id="filterCITitle" name="filterCITitle" type="text" value="${params.filterCITitle}"/>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIElement">${message(code:'financials.costItemElement')}</label>
                    <laser:select id="filterCIElement" class="ui dropdown selection"
                                  name="filterCIElement"
                                  from="${costItemElement}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${params.filterCIElement}"
                                  noSelection="${['':'Alle ..']}"/>
                </div>

                <div class="field fieldcontain">
                    <label for="filterCIStatus">${message(code:'financials.costItemStatus')}</label>
                    <laser:select id="filterCIStatus" class="ui dropdown selection"
                                  name="filterCIStatus"
                                  from="${costItemStatus}"
                                  optionKey="${{it.class.getName() + ":" + it.id}}"
                                  optionValue="value"
                                  value="${params.filterCIStatus}"
                                  noSelection="${['':'Alle ..']}"/>
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <label for="filterCIBudgetCode">${message(code:'financials.budgetCode')}</label>
                    <g:select id="filterCIBudgetCode" class="ui dropdown search selection"
                              name="filterCIBudgetCode"
                              from="${allCIBudgetCodes}"
                              value="${params.filterCIBudgetCode}"
                              noSelection="${['':'Alle ..']}"
                        />
                </div>

                <div class="field">
                    <label>${message(code:'financials.invoice_number')}</label>
                    <g:select id="filterCIInvoiceNumber" class="ui dropdown search selection"
                              name="filterCIInvoiceNumber"
                              from="${allCIInvoiceNumbers}"
                              value="${params.filterCIInvoiceNumber}"
                              noSelection="${['':'Alle ..']}"
                        />
                </div>

                <div class="field">
                    <label>${message(code:'financials.order_number')}</label>
                    <g:select id="filterCIOrderNumber" class="ui dropdown search selection"
                              name="filterCIOrderNumber"
                              from="${allCIOrderNumbers}"
                              value="${params.filterCIOrderNumber}"
                              noSelection="${['':'Alle ..']}"
                        />
                </div>
            </div><!-- .three -->

            <div class="three fields">
                <div class="field">
                    <label>Unscharfe Suche zulassen (im Feld Bezeichnung)</label>
                    <input type="checkbox" name="wildcard" value="on" <g:if test="${wildcard != 'off'}"> checked="checked"</g:if> />
                </div>

                <div class="field">
                </div>

                <div class="field la-filter-search ">
                    <a href="${request.forwardURI}" class="ui reset primary button">${message(code:'default.button.reset.label')}</a>
                    <input type="submit" name="submit" class="ui secondary button" value="${message(code:'default.button.filter.label', default:'Filter')}">
                </div>
            </div>

            <input type="hidden" name="shortcode" value="${contextService.getOrg()?.shortcode}"/> %{-- TODO: REMOVE --}%
        </g:form>
    </semui:filter>

<g:if test="${costItemSubList.size() > 1}">
    <div class="ui styled fluid accordion">
</g:if>

    <g:each in="${costItemSubList}" var="subListItem" status="i">

        <g:if test="${costItemSubList.size() > 1}">
            <div class="title">
                <i class="dropdown icon"></i>
                ${subListItem.key != 'clean' ? subListItem.key : 'Ohne konkrete Zuordnung'}
                ( ${subListItem.value?.size()} )
                <span class="sumOfCosts_${i}" style="position:absolute;right:30px"></span>
            </div>

            <div class="content">
        </g:if>

            <g:set var="cost_items" value="${subListItem.value}" />

            <table id="costTable_${i}" class="ui celled sortable table table-tworow la-table floatThead">

                <thead>
                    <tr>
                        <th>${message(code:'financials.costInLocalCurrency')}</th>
                        <th class="three wide">${message(code:'financials.newCosts.costTitle')}</th>
                        <%-- <th>${message(code:'financials.costItemCategory')}</th> --%>
                        <th>${message(code:'financials.costItemElement')}</th>
                        <%-- <th>${message(code:'financials.costItemComponent')}</th> --%>
                        <th>${message(code:'financials.costItemStatus')}</th>
                        <th>${message(code:'financials.dateFrom')}</th>
                        <th>${message(code:'financials.dateTo')}</th>
                        <th>Aktionen</th>
                    </tr>
                </thead>
                <tbody>
                    %{--Empty result set--}%
                    <g:if test="${cost_item_count == 0}">
                        <tr><td colspan="7" style="text-align:center">&nbsp;<br/>
                            <g:if test="${msg}">${msg}</g:if><g:else>${message(code:'finance.result.filtered.empty')}</g:else><br/>&nbsp;
                        </td></tr>
                    </g:if>
                    <g:else>
                        <g:render template="filter_data" model="[editable: editable, cost_items: cost_items]"></g:render>
                    </g:else>
                </tbody>
                <tfoot>
                    <tr>
                        <th>
                            <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}: <span class="sumOfCosts_${i}"></span></strong>
                        </th>
                    </tr>
                </tfoot>
            </table>

        <g:if test="${costItemSubList.size() > 1}">
            </div><!-- .content -->
        </g:if>
    </g:each>

    <g:if test="${costItemSubList.size() > 1}">
        <div class="title">
            <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')}:<span id="totalCost" style="position:absolute;right:30px"></span></strong>
        </div>
    </g:if>

<g:if test="${costItemSubList.size() > 1}">
    </div>
</g:if>

<%--
        <table id="costTable" class="ui striped celled la-rowspan table table-tworow">

            <thead>
                <tr>
                    <th rowspan="2" style="vertical-align: top; cursor: pointer;">
                        <a data-order="id"  class="sortable ${order=="Cost Item#"? "sorted ${sort}":''}">Cost Item#</a>*
                    </th>
                    <th>
                        <a style="cursor: pointer;" class="sortable ${order=="invoice#"? "sorted ${sort}":''}"  data-order="invoice#">${message(code:'financials.invoice_number')}</a>*
                    </th>
                    <th>
                       <a style="cursor: pointer;" class="sortable ${order=="order#"? "sorted ${sort}":''}"  data-order="order#">${message(code:'financials.order_number')}</a>*<br/>
                    </th>
                    <th>
                        <a data-order="Subscription" style="cursor: pointer;" class="sortable ${order=="Subscription"? "sorted ${sort}":''}">${message(code:'subscription.label')}</a>*
                    </th>
                    <th>
                        <a data-order="Package" style="cursor: pointer;" class="sortable ${order=="Package"? "sorted ${sort}":''}">${message(code:'package.label')}</a>*
                    </th>
                    <th style="vertical-align: top">${message(code:'issueEntitlement.label')}</th>


                %-- {--If has editable rights, allow delete column to be shown--}%
                    <g:if test="${editable}">
                        <th rowspan="2" colspan="1" style="vertical-align: top;">Delete
                            <br/><br/> <input title="${g.message(code: 'financials.deleteall.title')}" id="selectAll" type="checkbox" value=""/>
                        </th>
                    </g:if> --%
                </tr>
                %{--End of table row one of headers--}%


                <tr style="width: 100%;">
                    <th></th>
                    <th>
                        <a style="color: #990100; vertical-align: top; cursor: pointer;" data-order="datePaid" class="sortable ${order=="datePaid"? "sorted ${sort}":''}">Date Paid</a>*<br/><br/>

                        <ul style="list-style-type:none; margin: 0">
                            <li>Status</li>
                            <li>Category</li>
                            <li>Element</li>
                            <li>Tax Type</li>
                        </ul>
                    </th>
                    <th>Billing Amount<br/>Billing Currency<br/>Local Amount</th>
                    <th>
                        Cost Reference </br></br> Codes </br></br>
                        <a style="color: #990100; cursor: pointer;" data-order="startDate" class="sortable ${order=="startDate"? "sorted ${sort}":''}">Start Period</a>* &nbsp;<i>to</i>&nbsp;
                        <a style="color: #990100; cursor: pointer;" data-order="endDate" class="sortable ${order=="endDate"? "sorted ${sort}":''}">End Period</a>*
                    </th>
                    <th colspan="2">Cost Description</th>
                </tr>
            %{--End of table row two of headers--}%
            </thead>
            <tbody>
            --%>

            <%--

            %{--Empty result set--}%
            <g:if test="${cost_item_count==0}">
                <tr><td colspan="8" style="text-align:center">&nbsp;<br/><g:if test="${msg}">${msg}</g:if><g:else>No Cost Items Found</g:else><br/>&nbsp;</td></tr>
            </g:if>
            <g:else>
            %{--Two rows of data per CostItem, separated for readability--}%
                <g:render template="filter_data" model="[editable: editable, cost_items: cost_items]"></g:render>
            </g:else>
            </tbody>

        </table>
 --%>

<%--
<div id="paginationWrapper" class="pagination">
    <div id="paginateInfo" hidden="true" data-offset="${offset!=null?offset:params.offset}" data-max="${max!=null?max:params.max}"
         data-wildcard="${wildcard!=null?wildcard:params.wildcard}" data-insubmode="${inSubMode}" data-sub="${fixedSubscription?.id}"
         data-sort="${sort!=null?sort:params.sort}" data-order="${order!=null?order:params.order}" data-relation="${isRelation!=null?isRelation:params.orderRelation}"
         data-filterMode="${filterMode}" data-total="${cost_item_count}" data-resetMode="${params.resetMode}" data-subscriptionFilter="${params.subscriptionFilter}"
         data-invoiceNumberFilter="${params.invoiceNumberFilter}" data-orderNumberFilter="${params.orderNumberFilter}" data-packageFilter="${params.packageFilter}">
    </div>

    <util:remotePaginate title="${g.message(code: 'financials.pagination.title')}"
          onFailure="errorHandling(textStatus,'Pagination',errorThrown)" onComplete="Finance.rebind();Finance.scrollTo(null,'#costTable');" update="filterTemplate"
          offset='0'  total="${cost_item_count}"  max="20" pageSizes="[10, 20, 50, 100, 200]" alwaysShowPageSizes="true" controller="finance" action="index"
           params="${params+["filterMode": "${filterMode}", "sort":"${sort}", "order":"${order}", "format":"frag", "inSubMode":"${inSubMode}", "sub":"${fixedSubscription?.id}"]}" />
</div>
--%>
<!-- _filter.gsp -->