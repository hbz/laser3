<g:set bean="surveyService" var="surveyService"/>

<h2><g:message
        code="renewEntitlementsWithSurvey.currentTitlesSelect"/></h2>
<dl>
    <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
    <dd>${countSelectedIEs}</dd>
</dl>
<dl>
    <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
    <dd>
        <g:if test="${sumListPriceSelectedIEsEUR > 0}">
            <g:formatNumber
                    number="${sumListPriceSelectedIEsEUR}" type="currency" currencyCode="EUR"/><br/>
        </g:if>
        <g:if test="${sumListPriceSelectedIEsUSD > 0}">
            <g:formatNumber
                    number="${sumListPriceSelectedIEsUSD}" type="currency" currencyCode="USD"/><br/>
        </g:if>
        <g:if test="${sumListPriceSelectedIEsGBP > 0}">
            <g:formatNumber
                    number="${sumListPriceSelectedIEsGBP}" type="currency" currencyCode="GBP"/><br/>
        </g:if>
    </dd>
</dl>



<g:link absolute="true" controller="subscription" action="renewEntitlementsWithSurvey"
        id="${subscription.id}"
        params="${[surveyConfigID: surveyConfig.id,
                   tab           : 'selectedIEs']}"
        class="ui button">
    <g:message code="renewEntitlementsWithSurvey.currentTitlesSelect.button"/>
</g:link>

<h2><g:message code="renewEntitlementsWithSurvey.currentTitles"/>
</h2>
<dl>
    <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
    <dd>${countCurrentPermanentTitles}</dd>
</dl>

<g:link absolute="true" controller="subscription" action="renewEntitlementsWithSurvey"
        id="${subscription.id}"
        params="${[surveyConfigID: surveyConfig.id,
                   tab           : 'currentPerpetualAccessIEs']}"
        class="ui button">
    <g:message code="renewEntitlementsWithSurvey.currentTitles.button"/>
</g:link>

%{--
<g:if test="${surveyService.showStatisticByParticipant(surveyConfig.subscription, subscriber)}">
    <h2><g:message code="default.stats.label"/></h2>

    <g:link absolute="true" controller="subscription" action="renewEntitlementsWithSurvey"
            id="${subscription.id}"
            params="${[surveyConfigID: surveyConfig.id,
                       tab           : 'topUsed']}"
            class="ui button">
        <g:message code="renewEntitlementsWithSurvey.stats.button"/>
    </g:link>

</g:if>--}%
