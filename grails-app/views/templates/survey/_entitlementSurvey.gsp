<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon" %>
<div class="ui card la-time-card">
    <div class="content">
        <div class="header"><g:message code="renewEntitlementsWithSurvey.selectableTitles"/></div>
    </div>

    <div class="content">
        <div class="ui form twelve wide column">
            <div class="two fields">

                <div class="eight wide field" style="text-align: left;">
                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                            id="${subscription.id}"
                            params="${[surveyConfigID: surveyConfig.id]}"
                            class="${Btn.SIMPLE}">
                        <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                    </g:link>
                </div>

            </div>
        </div>

    </div>
</div>


<div class="ui top attached stackable tabular la-tab-with-js menu">
    <a class="active item" data-tab="currentTitlesSelect"><g:message
            code="renewEntitlementsWithSurvey.currentTitlesSelect"/></a>

    <a class="item" data-tab="currentTitles"><g:message code="renewEntitlementsWithSurvey.currentTitles"/>
        <span class="la-long-tooltip la-popup-tooltip" data-position="right center"
              data-content="${message(code: 'renewEntitlementsWithSurvey.currentTitles.mouseover')}">
            <i class="${Icon.TOOLTIP.HELP}"></i>
        </span>
    </a>

%{--    <g:if test="${surveyService.showStatisticByParticipant(surveyConfig.subscription, subscriber)}">
        <a class="item" data-tab="stats"><g:message code="default.stats.label"/></a>
    </g:if>--}%
</div>

<div class="ui bottom attached active tab segment" data-tab="currentTitlesSelect">
    <div class="item">
        <div class="content">
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
            </dl>
        </div>
    </div>


    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
            id="${subscription.id}"
            params="${[surveyConfigID: surveyConfig.id,
                       tab           : 'selectedIEs']}"
            class="${Btn.SIMPLE}">
        <g:message code="renewEntitlementsWithSurvey.currentTitlesSelect.button"/>
    </g:link>

</div>

<div class="ui bottom attached tab segment" data-tab="currentTitles">
    <div class="item">
        <div class="content">
            <dl>
                <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                <dd>${countCurrentPermanentTitles}</dd>
            </dl>
           %{-- <dl>
                <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
                <dd><g:formatNumber number="${iesFixListPriceSum}" type="currency"/></dd>
            </dl>--}%

        </div>
    </div>

    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
            id="${subscription.id}"
            params="${[surveyConfigID: surveyConfig.id,
                       tab           : 'currentPerpetualAccessIEs']}"
            class="${Btn.SIMPLE}">
        <g:message code="renewEntitlementsWithSurvey.currentTitles.button"/>
    </g:link>

</div>

%{--
<g:if test="${surveyService.showStatisticByParticipant(surveyConfig.subscription, subscriber)}">
    <div class="ui bottom attached tab segment" data-tab="stats">

        <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                id="${subscription.id}"
                params="${[surveyConfigID: surveyConfig.id,
                           tab           : 'topUsed']}"
                class="${Btn.SIMPLE}">
            <g:message code="renewEntitlementsWithSurvey.stats.button"/>
        </g:link>

    </div>

</g:if>--}%
