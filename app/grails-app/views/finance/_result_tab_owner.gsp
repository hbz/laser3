<!-- _result_tab_owner.gsp -->
<laser:serviceInjection />

<table id="costTable_${i}" class="ui celled sortable table table-tworow la-table ignore-floatThead">

<thead>
    <tr>
        <th>${message(code:'sidewide.number')}</th>
        <th>${message(code:'financials.newCosts.costTitle')}</th>
        <g:if test="${!forSingleSubscription}">
            <th>${message(code:'financials.newCosts.subscriptionHeader')}</th>
        </g:if>
        <th class="two wide">${message(code:'financials.invoice_total')}</th>
        <th class="two wide">${message(code:'financials.newCosts.valueInEuro')}</th>
        <th>${message(code:'financials.costItemStatus')}</th>
        <th>${message(code:'financials.dateFrom')}<br />${message(code:'financials.dateTo')}</th>
        <th>${message(code:'financials.costItemElement')}</th>
        <th></th>
    </tr>
</thead>
<tbody>
    %{--Empty result set--}%
    <g:if test="${cost_items?.size() == 0}">
        <tr>
            <td colspan="9" style="text-align:center">
                <br />
                <g:if test="${msg}">${msg}</g:if>
                <g:else>${message(code:'finance.result.filtered.empty')}</g:else>
                <br />
            </td>
        </tr>
    </g:if>
    <g:else>
        <% int counterHelper = params.offset ? Integer.parseInt(params.offset) : 0 %>
        <g:each in="${cost_items}" var="subListItem">
            <g:render template="result_tab_owner_table" model="[cost_items: subListItem.value, forSingleSubscription: forSingleSubscription, counterHelper: counterHelper]" />
            <% counterHelper += subListItem.value.size() %>
        </g:each>
    </g:else>
</tbody>
    <tfoot>
        <tr>
            <td colspan="8">
                <strong>${g.message(code: 'financials.totalcost', default: 'Total Cost')} </strong>
                <br/>
                <span class="sumOfCosts_${i}"></span>
            </td>
        </tr>
        <tr>
            <td colspan="8">
                <div class="ui fluid accordion">
                    <div class="title">
                        <i class="dropdown icon"></i>
                        <strong>${message(code: 'financials.calculationBase')}</strong>
                    </div>
                    <div class="content">
                        <%
                            def argv0 = contextService.getOrg().costConfigurationPreset ? contextService.getOrg().costConfigurationPreset.getI10n('value') : message(code:'financials.costItemConfiguration.notSet')
                        %>
                        ${message(code: 'financials.calculationBase.paragraph1', args: [argv0])}
                        <p>
                            ${message(code: 'financials.calculationBase.paragraph2')}
                        </p>
                    </div>
                </div>
            </td>
        </tr>
    </tfoot>
</table>
    <g:if test="${cost_items}">
        <g:if test="${inSubMode}">
            <semui:paginate mapping="subfinance" action="index" controller="finance" params="${params}"
                            next="${message(code: 'default.paginate.next', default: 'Next')}"
                            prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                            total="${cost_items_count}"/>
        </g:if>
        <g:else>
            <semui:paginate action="finance" controller="myInstitution" params="${params}"
                            next="${message(code: 'default.paginate.next', default: 'Next')}"
                            prev="${message(code: 'default.paginate.prev', default: 'Prev')}" max="${max}"
                            total="${cost_items_count}"/>
        </g:else>

    </g:if>

<!-- _result_tab_owner.gsp -->
