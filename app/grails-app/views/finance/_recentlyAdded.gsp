<!-- _recentlyAdded.gsp -->
%{--<%@page defaultCodec="HTML" %>--}%
<p>Quickly see costing items that have been recently added/updated</p>
<p>From: ${from}  -> To: ${to}</p>

<g:if test="${recentlyUpdated}">

    <table class="ui celled la-table table" id="recentUpdatesTable" data-resultsTo="${to}" data-resultsFrom="${from}">
        <thead>
            <tr>
                <th>Cost#</th>
                <th>Invoice#</th>
                <th>Order#</th>
                <th>Subscription</th>
            </tr>
            <tr>
                <th>Package</th>
                <th>IE</th>
                <th>Amount [billing]/<br/>[local]</th>
                <th>Updated</th>
            </tr>
        </thead>
        <tbody>
            <g:each in="${recentlyUpdated}" var="item">
                <tr>
                    <td>${item?.id}</td>
                    <td>${item?.invoice?.invoiceNumber.encodeAsHTML()}</td>
                    <td>${item?.order?.orderNumber.encodeAsHTML()}</td>
                    <td>${item?.sub?.name.encodeAsHTML()}</td>
                </tr>
                <tr>
                    <td>${item?.subPkg?.pkg?.name.encodeAsHTML()}</td>
                    <td>${item?.issueEntitlement?.tipp?.title?.title.encodeAsHTML()}</td>
                    <td>${item?.costInBillingCurrency.encodeAsHTML()}</td>
                    <td><g:formatDate format="dd-MM-yy" date="${item?.lastUpdated}"/></td>
                </tr>
            </g:each>
        </tbody>
    </table>

</g:if>
<g:else>
    <p>No recent cost items...</p>
    <p>Table automatically updates</p>
</g:else>

<!-- _recentlyAdded.gsp -->