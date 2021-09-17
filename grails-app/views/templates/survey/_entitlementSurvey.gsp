<div class="ui top attached tabular menu">
    <a class="active item" data-tab="currentTitles"><g:message code="renewEntitlementsWithSurvey.currentTitles"/></a>
    <a class="item" data-tab="currentTitlesSelect"><g:message
            code="renewEntitlementsWithSurvey.currentTitlesSelect"/></a>
    <g:if test="${previousSubscription}">
        <a class="item" data-tab="previousTitles"><g:message code="renewEntitlementsWithSurvey.previousTitles"/></a>
    </g:if>

    <g:if test="${surveyService.showStatisticByParticipant(surveyConfig.subscription, subscriber)}">
        <a class="item" data-tab="stats"><g:message code="default.stats.label"/></a>
    </g:if>
</div>

<div class="ui bottom attached active tab segment" data-tab="currentTitles">
    <div class="item">
        <div class="content">
            <dl>
                <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                <dd>${iesFix.size()}</dd>
            </dl>
            <dl>
                <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
                <dd><g:formatNumber number="${iesFixListPriceSum}" type="currency"/></dd>
            </dl>

        </div>
    </div>

    <div class="ui form twelve wide column">
        <div class="two fields">

            <div class="eight wide field" style="text-align: left;">
                <g:if test="${subscription}">
                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                            id="${subscription.id}"
                            params="${[targetObjectId: subscription.id,
                                       surveyConfigID: surveyConfig.id]}"
                            class="ui button">
                        <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                    </g:link>
                </g:if>
            </div>


            <div class="eight wide field" style="text-align: right;">

                <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">

                    <g:link action="renewEntitlements" controller="surveys"
                            id="${surveyConfig.id}" params="[participant: participant.id]"
                            class="ui button">
                        <g:message code="renewEntitlementsWithSurvey.renewEntitlements"/>
                    </g:link>

                </g:if>

                <g:link action="index" controller="subscription"
                        id="${subscription.id}"
                        class="ui button">
                    <g:message code="renewEntitlementsWithSurvey.currentTitles"/>
                </g:link>

            </div>
        </div>
    </div>

</div>

<div class="ui bottom attached tab segment" data-tab="currentTitlesSelect">
    <div class="item">
        <div class="content">
            <dl>
                <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                <dd>${ies.size()}</dd>
            </dl>
            <dl>
                <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
                <dd><g:formatNumber number="${iesListPriceSum}" type="currency"/></dd>
            </dl>
        </div>
    </div>

    <div class="ui form twelve wide column">
        <div class="two fields">

            <div class="eight wide field" style="text-align: left;">
                <g:if test="${subscription}">
                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                            id="${subscription.id}"
                            params="${[targetObjectId: subscription.id,
                                       surveyConfigID: surveyConfig.id]}"
                            class="ui button">
                        <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                    </g:link>
                </g:if>
            </div>


            <div class="eight wide field" style="text-align: right;">

                <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">

                    <g:link action="renewEntitlements" controller="surveys"
                            id="${surveyConfig.id}" params="[participant: participant.id]"
                            class="ui button">
                        <g:message code="renewEntitlementsWithSurvey.renewEntitlements"/>
                    </g:link>

                </g:if>

                <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                        id="${subscription.id}"
                        params="${[targetObjectId: subscription.id,
                                   surveyConfigID: surveyConfig.id,
                                   tab           : 'selectedIEs']}"
                        class="ui button">
                    <g:message code="renewEntitlementsWithSurvey.currentTitlesSelect"/>
                </g:link>

            </div>
        </div>
    </div>
</div>

<g:if test="${previousSubscription}">
    <div class="ui bottom attached tab segment" data-tab="previousTitles">
        <div class="item">
            <div class="content">
                <dl>
                    <dt class="control-label">${message(code: 'myinst.selectPackages.pkg_titles')}</dt>
                    <dd>${previousIes.size()}</dd>
                </dl>
                <dl>
                    <dt class="control-label">${message(code: 'tipp.price.listPrice')}</dt>
                    <dd><g:formatNumber number="${previousIesListPriceSum}" type="currency"/></dd>
                </dl>
            </div>
        </div>

        <div class="ui form twelve wide column">
            <div class="two fields">

                <div class="eight wide field" style="text-align: left;">
                    <g:if test="${subscription}">
                        <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                                id="${subscription.id}"
                                params="${[targetObjectId: subscription.id,
                                           surveyConfigID: surveyConfig.id]}"
                                class="ui button">
                            <g:message code="surveyInfo.toIssueEntitlementsSurvey"/>
                        </g:link>
                    </g:if>
                </div>


                <div class="eight wide field" style="text-align: right;">

                    <g:if test="${contextOrg.id == surveyConfig.surveyInfo.owner.id}">

                        <g:link action="renewEntitlements" controller="surveys"
                                id="${surveyConfig.id}" params="[participant: participant.id]"
                                class="ui button">
                            <g:message code="renewEntitlementsWithSurvey.renewEntitlements"/>
                        </g:link>

                    </g:if>

                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                            id="${subscription.id}"
                            params="${[targetObjectId: subscription.id,
                                       surveyConfigID: surveyConfig.id,
                                       tab           : 'previousIEs']}"
                            class="ui button">
                        <g:message code="renewEntitlementsWithSurvey.previousTitles"/>
                    </g:link>

                </div>
            </div>
        </div>
    </div>
</g:if>

<g:if test="${surveyService.showStatisticByParticipant(surveyConfig.subscription, subscriber)}">
    <div class="ui bottom attached tab segment" data-tab="stats">
        <br>
        <div class="ui form twelve wide column">
            <div class="two fields">

                <div class="eight wide field" style="text-align: left;">
                    <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                            id="${subscription.id}"
                            params="${[targetObjectId: subscription.id,
                                       surveyConfigID: surveyConfig.id,
                                       tab           : 'allIEsStats']}"
                            class="ui button">
                        <g:message code="renewEntitlementsWithSurvey.allIEsStatsStats"/>
                    </g:link>

                </div>

                <div class="eight wide field" style="text-align: right;">

                    <g:if test="${previousSubscription}">
                        <g:link controller="subscription" action="renewEntitlementsWithSurvey"
                                id="${subscription.id}"
                                params="${[targetObjectId: subscription.id,
                                           surveyConfigID: surveyConfig.id,
                                           tab           : 'previousIEsStats']}"
                                class="ui button">
                            <g:message code="renewEntitlementsWithSurvey.previousIEsStats"/>
                        </g:link>
                    </g:if>

                </div>
            </div>
        </div>
    </div>
</g:if>