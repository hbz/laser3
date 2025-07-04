<%@ page import="de.laser.survey.SurveySubscriptionResult; de.laser.survey.SurveyTransfer; de.laser.Subscription; de.laser.ui.Btn" %>
<g:if test="${listTransferredSub && listTransferredSub.size() > 0}">
    <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.period.label')}</th>
            <th>${message(code: 'subscription.label')}</th>
            <g:if test="${surveyConfig.subscriptionSurvey}">
                <th>
                    ${message(code: 'subscriptionSurvey.label')}
                </th>
            </g:if>
            <th>${message(code: 'surveyTransfer.transferred')}</th>
            <th>${message(code: 'default.action.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${listTransferredSub}" var="sub" status="i">
            <g:set var="subResultCount"
                   value="${SurveySubscriptionResult.executeQuery('select count(*) from SurveySubscriptionResult ssr where ssr.subscription = :parent and ssr.surveyConfig = :surveyConfig and exists (select so from SurveyOrg as so where so.finishDate is not null and so.surveyConfig = ssr.surveyConfig and so.org = ssr.participant)', [parent: sub, surveyConfig: surveyConfig])[0]}"/>
            <g:set var="transferredSubCount"
                   value="${SurveyTransfer.executeQuery('select count(*) from SurveyTransfer st where st.subscription.instanceOf = :parent and st.surveyConfig = :surveyConfig', [parent: sub, surveyConfig: surveyConfig])[0]}"/>

            <tr class="${(params.surveySubscriptions && sub.id.toString() == params.surveySubscriptions) ? 'positive' : ''}">
                <td>
                    ${i + 1}
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${sub.startDate}"/>
                    -
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${sub.endDate}"/>
                </td>
                <td>
                    <g:link controller="subscription" action="show" id="${sub.id}">${sub.getLabel()}</g:link>
                </td>
                <g:if test="${surveyConfig.subscriptionSurvey}">
                    <td class="${subResultCount > 0 ? 'positive' : ''}">
                        <g:link controller="survey" action="surveySubscriptionsEvaluation" id="${surveyInfo.id}" params="${[surveyConfigID: surveyConfig.id, surveySubscriptions: [sub.id]]}">${subResultCount}</g:link>
                    </td>
                </g:if>
                <td>
                    <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}" params="${[surveyConfigID: surveyConfig.id, subs: [sub.id]]}">${transferredSubCount}</g:link>
                </td>
                <td>
                    <g:if test="${surveyConfig.subscriptionSurvey && subResultCount > 0}">
                            <g:if test="${!(params.surveySubscriptions && sub.id == params.surveySubscriptions)}">
                                <g:link controller="survey" action="$actionName"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, surveySubscriptions: sub.id]"
                                        class="${Btn.SIMPLE}">
                                    <g:message code="default.select2.label" args="[message(code: 'subscription.label')]"/>
                                </g:link>
                                <br>
                                <br>
                            </g:if>
                    </g:if>


                    <g:if test="${transferredSubCount > 0}">
                            <g:link controller="survey" action="compareMembersOfTwoSubs" class="${Btn.SIMPLE} "
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: sub.id]">
                                ${message(code: 'surveyInfo.transferMembers')}
                            </g:link>
                    </g:if>
                </td>
            </tr>

        </g:each>
        </tbody>
    </table>

    <br>
    <br>
</g:if>