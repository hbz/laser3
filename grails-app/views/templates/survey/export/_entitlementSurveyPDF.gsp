<g:set bean="surveyService" var="surveyService"/>

<h2><g:message
        code="renewEntitlementsWithSurvey.currentTitlesSelect"/></h2>
<dl>
    <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
    <dd>${countSelectedIEs}</dd>
</dl>
<dl>
    <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
    <dd><g:formatNumber number="${iesListPriceSum}" type="currency"/></dd>
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
    <dd>${countCurrentIEs}</dd>
</dl>

<g:link absolute="true" controller="subscription" action="renewEntitlementsWithSurvey"
        id="${subscription.id}"
        params="${[surveyConfigID: surveyConfig.id,
                   tab           : 'currentIEs']}"
        class="ui button">
    <g:message code="renewEntitlementsWithSurvey.currentTitles.button"/>
</g:link>

<g:if test="${surveyService.showStatisticByParticipant(surveyConfig.subscription, subscriber)}">
    <h2><g:message code="default.stats.label"/></h2>

    <g:link absolute="true" controller="subscription" action="renewEntitlementsWithSurvey"
            id="${subscription.id}"
            params="${[surveyConfigID: surveyConfig.id,
                       tab           : 'allIEsStats']}"
            class="ui button">
        <g:message code="renewEntitlementsWithSurvey.stats.button"/>
    </g:link>

</g:if>