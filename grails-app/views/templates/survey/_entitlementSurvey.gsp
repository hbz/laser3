<div class="ui card la-time-card">
    <div class="content">
        <div class="header"><g:message code="subscription.entitlement.plural"/></div>
    </div>

    <div class="content">
        <div class="header"><g:message code="renewEntitlementsWithSurvey.currentFixedEntitlements"/></div>
        <dl>
            <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
            <dd>${iesFix.size()}</dd>
        </dl>
        <dl>
            <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
            <dd><g:formatNumber number="${iesFixListPriceSum}" type="currency"/></dd>
        </dl>

        <div class="ui la-vertical buttons">
            <g:link action="index" controller="subscription"
                    id="${subscription.id}"
                    class="ui button">
                <g:message code="renewEntitlementsWithSurvey.toCurrentFixedEntitlements"/>
            </g:link>
        </div>

    </div>

    <div class="content">
        <div class="header"><g:message code="renewEntitlementsWithSurvey.currentEntitlements"/></div>
        <dl>
            <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
            <dd>${ies.size()}</dd>
        </dl>
        <dl>
            <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
            <dd><g:formatNumber number="${iesListPriceSum}" type="currency"/></dd>
        </dl>
    </div>

    <div class="content">
        <div class="ui form twelve wide column">
            <div class="two fields">

                <div class="eight wide field" style="text-align: left;">
                    <g:if test="${subscription}">
                        <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                                id="${subscription.id}"
                                params="${[targetObjectId: subscription.id,
                                           surveyConfigID      : surveyConfig.id]}"
                                class="ui button">
                            <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                        </g:link>
                    </g:if>
                </div>


                <div class="eight wide field" style="text-align: right;">

                    <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">

                        <g:link action="renewEntitlements"
                                id="${surveyConfig.id}" params="[participant: participant.id]"
                                class="ui button">
                            <g:message code="renewEntitlementsWithSurvey.renewEntitlements"/>
                        </g:link>

                    </g:if>

                    <g:link controller="subscription" action="showEntitlementsRenewWithSurvey"
                            id="${surveyConfig.id}"
                            class="ui button">
                        <g:message code="renewEntitlementsWithSurvey.toCurrentEntitlements"/>
                    </g:link>


                </div>
            </div>
        </div>
    </div>
</div>