<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<g:set var="surveyService" bean="surveyService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} :  ${message(code: 'surveyInfo.transferOverView')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
        <semui:crumb controller="survey" action="renewalWithSurvey" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" message="surveyInfo.renewalOverView"/>
    </g:if>
    <semui:crumb message="surveyInfo.transferOverView" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="survey" action="renewalWithSurvey" params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.renewalOverView"/>

            <semui:actionsDropdownItem controller="survey" action="copySurveyCostItems" params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.copySurveyCostItems"/>

    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<h2>
    ${message(code: 'surveyInfo.transferOverView')}
</h2>



<semui:form>
    <div class="ui grid">

        <div class="row">
            <div class="eight wide column">
                <h3 class="ui header center aligned">

                    <g:message code="renewalWithSurvey.parentSubscription"/>:<br>
                    <g:if test="${parentSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSubscription?.id}">${parentSubscription?.dropdownNamingConvention()}</g:link>
                        <br>
                        <g:link controller="subscription" action="members"
                                id="${parentSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSubscription.getDerivedSubscribers().size() ?: 0}"/>
                    </g:if>
                </h3>
            </div>

            <div class="eight wide column">
                <h3 class="ui header center aligned">
                    <g:message code="renewalWithSurvey.parentSuccessorSubscription"/>:<br>
                    <g:if test="${parentSuccessorSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSuccessorSubscription?.id}">${parentSuccessorSubscription?.dropdownNamingConvention()}</g:link>
                        <br>
                        <g:link controller="subscription" action="members"
                                id="${parentSuccessorSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSuccessorSubscription.getDerivedSubscribers().size() ?: 0}"/>

                    </g:if>
                    <g:else>
                        <g:message code="renewalWithSurvey.noParentSuccessorSubscription"/>
                    %{--<br>
                    <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey"
                            id="${surveyInfo?.id}"
                            params="[surveyConfig: surveyConfig?.id, parentSub: parentSubscription?.id]"
                            class="ui button ">
                        <g:message code="renewalWithSurvey.newSub"/>
                    </g:link>--}%
                    </g:else>
                </h3>
            </div>
        </div>
    </div>
</semui:form>
<semui:form>

        <div class="ui grid">

            <div class="row">

                <div class="eight wide column">

                    <table class="ui celled sortable table la-table" id="parentSubscription">
                        <thead>
                        <tr>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${participantsList}" var="participant" status="i">
                            <g:if test="${participant in parentParticipantsList}">
                                <g:set var="termination" value="${!(participant in parentSuccessortParticipantsList)}"/>
                                <g:set var="participantSub" value="${parentSubscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <tr class=" ${termination ? 'negative' : ''}">
                                    <td>${i + 1}</td>
                                    <td class="titleCell">
                                        <g:if test="${participantSub && participantSub.isMultiYear}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                <i class="map orange icon"></i>
                                            </span>
                                        </g:if>
                                        <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                id="${participant.id}">
                                            ${participant?.sortname}
                                        </g:link>
                                        <br>
                                        <g:link controller="organisation" action="show"
                                                id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>
                                        <g:if test="${participantSub}">
                                            <div class="la-icon-list">
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.startDate}"/>
                                                -
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.endDate}"/>
                                                <div class="right aligned wide column">
                                                    <b>${participantSub.status.getI10n('value')}</b>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td>
                                        <g:if test="${participantSub}">
                                            <g:link controller="subscription" action="show" id="${participantSub.id}"
                                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td>${i + 1}</td>
                                    <td class="titleCell"></td>
                                    <td></td>
                                </tr>
                            </g:else>
                        </g:each>
                        </tbody>
                    </table>

                </div>

                <div class="eight wide column">

                    <table class="ui celled sortable table la-table" id="parentSuccessorSubscription">
                        <thead>
                        <tr>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                            <th></th>
                        </tr>
                        </thead>
                        <g:each in="${participantsList}" var="participant" status="j">
                            <g:if test="${participant in parentSuccessortParticipantsList}">
                                <g:set var="participantSub" value="${parentSuccessorSubscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <tr class=" ${participant in parentParticipantsList ? '' : 'positive'}">
                                    <td>${j+1}</td>
                                    <td class="titleCell">
                                        <g:if test="${participantSub && participantSub.isMultiYear}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                <i class="map orange icon"></i>
                                            </span>
                                        </g:if>
                                        <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                id="${participant.id}">
                                            ${participant?.sortname}
                                        </g:link>
                                        <br>
                                        <g:link controller="organisation" action="show"
                                                id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>

                                        <g:if test="${participantSub}">
                                            <div class="la-icon-list">
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.startDate}"/>
                                                -
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSub.endDate}"/>

                                                <div class="right aligned wide column">
                                                    <b>${participantSub.status.getI10n('value')}</b>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td>
                                        <g:if test="${participantSub}">
                                            <g:link controller="subscription" action="show" id="${participantSub.id}"
                                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                                        </g:if>
                                    </td>
                                </tr>
                            </g:if>
                            <g:else>
                                <tr>
                                    <td>${j+1}</td>
                                    <td class="titleCell"></td>
                                </tr>
                            </g:else>
                        </g:each>

                    </table>
                </div>
            </div>
        </div>


</semui:form>

<r:script>
    $(document).ready(function() {

        $("#parentSubscription .titleCell").each(function(k) {
            var v = $(this).height();
            $("#parentSuccessorSubscription .titleCell").eq(k).height(v);
        });

        $("#parentSuccessorSubscription .titleCell").each(function(k) {
            var v = $(this).height();
            $("#parentSubscription .titleCell").eq(k).height(v);
        });

    });
</r:script>

</body>
</html>
