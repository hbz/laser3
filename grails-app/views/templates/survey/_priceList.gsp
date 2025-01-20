<g:if test="${selectionListPriceEuro > 0}">
    <div class="item">
        <div class="content">
            <strong><g:message code="renewEntitlementsWithSurvey.totalCostSelected"/>:</strong> <g:formatNumber number="${selectionListPriceEuro}" type="currency" currencyCode="EUR"/><br/>
        </div>
    </div>
</g:if>
<g:if test="${selectionListPriceUSD > 0}">
    <div class="item">
        <div class="content">
            <strong><g:message code="renewEntitlementsWithSurvey.totalCostSelected"/>:</strong> <g:formatNumber number="${selectionListPriceUSD}" type="currency" currencyCode="USD"/><br/>
        </div>
    </div>
</g:if>
<g:if test="${selectionListPriceGBP > 0}">
    <div class="item">
        <div class="content">
            <strong><g:message code="renewEntitlementsWithSurvey.totalCostSelected"/>:</strong> <g:formatNumber number="${selectionListPriceGBP}" type="currency" currencyCode="GBP"/><br/>
        </div>
    </div>
</g:if>
