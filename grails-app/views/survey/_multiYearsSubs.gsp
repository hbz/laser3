<%@ page import="de.laser.ui.Btn" %>
<g:if test="${listMuliYearsSub && listMuliYearsSub.size() > 0}">
    <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
        <thead>
        <tr>
            <th>${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'renewalEvaluation.period')}</th>
            <th>${message(code: 'subscription.label')}</th>
            <th>${message(code: 'subscription.isMultiYear.label')} IN <br> ${message(code: 'survey.label')}-${message(code: 'surveyInfo.members')} / ${message(code: 'subscription.label')}-${message(code: 'subscriptionDetails.members.members')}</th>
            <th>${message(code: 'default.action.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${listMuliYearsSub}" var="multiYear" status="i">
            <g:set var="countMultiYearMembers" value="${0}"/>
            <g:if test="${nextSubs && nextSubs[i]}">
                <g:set var="countMultiYearMembers" value="${subscriptionService.countMultiYearSubInParentSub(nextSubs[i])}"/>
            </g:if>
            <tr class="${(nextSubs && parentSuccessorSubscription == nextSubs[i]) ? 'positive' : ''}">
                <td>
                    ${i + 1}
                </td>
                <td>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${multiYear.startDate}"/>
                    -
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${multiYear.endDate}"/>
                </td>
                <td>
                    <g:if test="${nextSubs && nextSubs[i]}">
                        <g:link controller="subscription" action="show" id="${nextSubs[i].id}">${nextSubs[i].getLabel()}</g:link>
                    </g:if>
                </td>
                <td>
                    <g:link controller="survey" action="surveyEvaluation" id="${surveyInfo.id}"
                            params="${[surveyConfigID: surveyConfig.id, filterPropDefAllMultiYear: 'on']}">${surveyService.countMultiYearResult(surveyConfig, i + 1)}</g:link> /

                    <g:if test="${nextSubs && nextSubs[i]}">
                        <g:link controller="subscription" action="members" id="${nextSubs[i].id}"
                                params="[subRunTimeMultiYear: 'on']">${(nextSubs && nextSubs[i]) ? countMultiYearMembers : 0}</g:link>
                    </g:if>
                </td>
                <td>
                    <g:if test="${nextSubs && nextSubs[i]}">
                        <g:if test="${actionName == 'compareMembersOfTwoSubs'}">
                            <g:link class="${Btn.SIMPLE} openTransferParticipantsModal" controller="survey" action="openTransferParticipantsModal" params="${[surveyConfigID: surveyConfig.id, id: surveyInfo.id, targetSubscriptionId: nextSubs[i].id]}">
                                <g:message code="surveyInfo.transferParticipants"/>
                            </g:link>
                            <br>
                            <br>
                        </g:if>

                        <g:if test="${parentSuccessorSubscription != nextSubs[i]}">
                            <g:link controller="survey" action="$actionName"
                                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: nextSubs[i].id]"
                                    class="${Btn.SIMPLE}">
                                <g:message code="default.select2.label" args="[message(code: 'renewalEvaluation.parentSuccessorSubscription')]"/>
                            </g:link>
                        </g:if>
                    </g:if>
                    <g:else>
                        <g:if test="${(i == 0) || (nextSubs && nextSubs[i - 1])}">
                        <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo.id}"
                                params="${[surveyConfig: surveyConfig.id, sourceSubId: ((nextSubs && i > 0) ? nextSubs[i - 1].id : parentSubscription.id)]}"
                                class="${Btn.SIMPLE}">
                            <g:message code="renewalEvaluation.newSub"/>
                        </g:link>
                        </g:if>
                    </g:else>
                </td>
            </tr>

        </g:each>
        </tbody>
    </table>

    <br>
    <br>
</g:if>
<g:else>
    <g:if test="${surveyConfig.subSurveyUseForTransfer && actionName == 'compareMembersOfTwoSubs'}">
        <g:if test="${!parentSuccessorSubscription}">

            <h3 class="ui header">
                <g:message code="renewalEvaluation.parentSuccessorSubscription"/>:
                <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo.id}"
                        params="[surveyConfig: surveyConfig.id, sourceSubId: surveyConfig.subscription.id]"
                        class="${Btn.SIMPLE}">
                    <g:message code="renewalEvaluation.newSub"/>
                </g:link>
            </h3>

        </g:if>

        <g:if test="${parentSuccessorSubscription}">
            <br>
            <g:link class="${Btn.SIMPLE} openTransferParticipantsModal" controller="survey" action="openTransferParticipantsModal" params="${[surveyConfigID: surveyConfig.id, id: surveyInfo.id, targetSubscriptionId: parentSuccessorSubscription.id]}">
                <g:message code="surveyInfo.transferParticipants"/>
            </g:link>
            <br>
        </g:if>

        <g:if test="${parentSuccessorSubscription && parentSuccessorSubscription.getDerivedNonHiddenSubscribers().size() > 0}">
            <br>
            <g:link controller="subscription" action="copyElementsIntoSubscription" id="${parentSubscription.id}"
                    params="[sourceObjectId: genericOIDService.getOID(parentSubscription), targetObjectId: genericOIDService.getOID(parentSuccessorSubscription), isRenewSub: true, fromSurvey: surveyConfig.id]"
                    class="${Btn.SIMPLE}">
                <g:message code="renewalEvaluation.newSub.change"/>
            </g:link>
        </g:if>

        <br>
    </g:if>
</g:else>

<laser:script file="${this.getGroovyPageFileName()}">
    $('.openTransferParticipantsModal').on('click', function(e) {
            e.preventDefault();
console.log($(this).attr('href'))
            $.ajax({
                url: $(this).attr('href')
            }).done( function (data) {
                $('.ui.dimmer.modals > #transferParticipantsModal').remove();
                $('#dynamicModalContainer').empty().html(data);

                $('#dynamicModalContainer .ui.modal').modal({
                   onShow: function () {
                        r2d2.initDynamicUiStuff('#transferParticipantsModal');
                        r2d2.initDynamicXEditableStuff('#transferParticipantsModal');
                        $("html").css("cursor", "auto");
                    },
                    detachable: true,
                    autofocus: false,
                    transition: 'scale',
                    onApprove : function() {
                        $(this).find('.ui.form').submit();
                        return false;
                    }
                }).modal('show');
            })
        })
</laser:script>