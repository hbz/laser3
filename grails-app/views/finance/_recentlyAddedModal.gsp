<!-- _recentlyAddedModal.gsp -->

<ui:modal id="recentlyAdded_modal" text="${message(code:'financials.recentCosts')}" hideSubmitButton="true">

    <p>Überblick über die zuletzt hinzugefügten Kosten.</p>
    <p>Von ${from} bis ${to}</p>

    <g:if test="${recentlyUpdated}">

        <table class="ui celled la-js-responsive-table la-table compact table" id="recentUpdatesTable" data-resultsTo="${to}" data-resultsFrom="${from}">
            <thead>
                <tr>
                    <th>${message(code:'financials.newCosts.costTitle')}</th>
                    <th>${message(code:'financials.invoice_number')}</th>
                    <th>${message(code:'financials.order_number')}</th>
                    <th>${message(code:'default.subscription.label')}</th>
                </tr>
                <tr>
                    <th>${message(code:'package.label')}</th>
                    <th>${message(code:'financials.newCosts.singleEntitlement')}</th>
                    <th>${message(code:'financials.invoice_total')} /<br />${message(code:'financials.costInLocalCurrency')}</th>
                    <th>Zuletzt aktualisiert</th>
                </tr>
            </thead>
            <tbody>
                <g:each in="${recentlyUpdated}" var="item">
                    <tr>
                        <td>${item?.costTitle}</td>
                        <td>${item?.invoice?.invoiceNumber}</td>
                        <td>${item?.order?.orderNumber}</td>
                        <td>${item?.sub?.name}</td>
                    </tr>
                    <tr>
                        <td>${item?.subPkg?.pkg?.name}</td>
                        <td>${item?.issueEntitlement?.tipp?.title?.title}</td>
                        <td>${item?.costInBillingCurrency}</td>
                        <td><g:formatDate format="${message(code:'default.date.format.notime')}" date="${item?.lastUpdated}"/></td>
                    </tr>
                </g:each>
            </tbody>
        </table>

    </g:if>
    <g:else>

        <p>${message(code:'finance.result.filtered.empty')}</p>
        <p>Diese Übersicht aktualisiert sich automatisch.</p>

    </g:else>

</ui:modal>

<!-- _recentlyAddedModal.gsp -->