<%@ page import="com.k_int.kbplus.RefdataCategory;com.k_int.properties.PropertyDefinition;com.k_int.kbplus.SurveyConfig;com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection/>
<g:set var="subscriptionService" bean="subscriptionService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'surveyTitlesEvaluation.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br>

<div class="ui grid">

    <div class="sixteen wide stretched column">
        <div class="ui top attached tabular menu">

            <a class="item active" data-tab="participantsViewAllFinish">
                ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                <div class="ui floating circular label">${participantsFinish?.size() ?: 0}</div>
            </a>

            <a class="item" data-tab="participantsViewAllNotFinish">
                ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                <div class="ui floating circular label">${participantsNotFinish?.size() ?: 0}</div>
            </a>

            <a class="item" data-tab="participantsView">
                ${message(code: 'surveyEvaluation.participantsView')}
                <div class="ui floating circular label">${participants?.size() ?: 0}</div>
            </a>

        </div>


        <div class="ui bottom attached tab segment active" data-tab="participantsViewAllFinish">

                <h2 class="ui icon header la-clear-before la-noMargin-top"><g:message code="surveyEvaluation.participants"/><semui:totalNumber
                        total="${participantsFinish?.size()}"/></h2>
                <g:if test="${surveyInfo && surveyInfo.status?.id == de.laser.helper.RDStore.SURVEY_IN_EVALUATION?.id}">
                                <g:link controller="survey" action="completeIssueEntitlementsSurvey" id="${surveyConfig?.id}"
                                        class="ui icon button right floated">
                                    <g:message code="completeIssueEntitlementsSurvey.forFinishParticipant.label"/>
                                </g:link>
                </g:if>
                <br>
                <br>
                <semui:form>

                    <h4><g:message code="surveyParticipants.hasAccess"/></h4>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${participantsFinish?.findAll { it?.hasAccessOrg() }?.sort {
                               it?.sortname
                           }}"/>

                    <div class="four wide column">
                        <g:link data-orgIdList="${(surveyParticipantsHasAccess?.id).join(',')}"
                                data-targetId="copyEmailaddresses_ajaxModal2"
                                class="ui icon button right floated trigger-modal">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </g:link>
                    </div>

                    <br>
                    <br>

                    <table class="ui celled sortable table la-table">
                        <thead>
                        <tr>
                            <th class="center aligned">
                                ${message(code: 'sidewide.number')}
                            </th>
                            <th>
                                ${message(code: 'org.sortname.label')}
                            </th>
                            <th>
                                ${message(code: 'default.name.label')}
                            </th>
                            <th>
                                ${message(code: 'surveyInfo.finishedDate')}
                            </th>
                            <th>
                                ${message(code: 'surveyTitlesEvaluation.currentAndFixedEntitlements')}
                            </th>
                            <th>
                                ${message(code: 'default.actions.label')}
                            </th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${surveyParticipantsHasAccess}" var="participant" status="i">
                            <tr>
                                <td>
                                    ${i + 1}
                                </td>
                                <td>
                                    <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant?.id}">
                                        ${participant?.sortname}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="organisation" action="show" id="${participant.id}">
                                        ${fieldValue(bean: participant, field: "name")}
                                    </g:link>
                                </td>
                                <td>
                                    <semui:surveyFinishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                                </td>
                                <td class="center aligned">
                                    <g:set var="subParticipant" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                    <div class="ui circular label">
                                    ${subscriptionService.getIssueEntitlementsFixed(subParticipant)?.size()?:0 } / ${subscriptionService.getIssueEntitlementsNotFixed(subParticipant)?.size()?:0 }
                                    </div>

                                </td>
                                <td>

                                    <g:link action="showEntitlementsRenew"
                                            id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                            class="ui icon button"><i
                                            class="write icon"></i>
                                    </g:link>

                                </td>

                            </tr>

                        </g:each>
                        </tbody>
                    </table>

                    <h4><g:message code="surveyParticipants.hasNotAccess"/></h4>

                    <g:set var="surveyParticipantsHasNotAccess" value="${participantsFinish.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

                    <div class="four wide column">
                        <g:link data-orgIdList="${(surveyParticipantsHasNotAccess?.id).join(',')}"
                                data-targetId="copyEmailaddresses_ajaxModal3"
                                class="ui icon button right floated trigger-modal">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </g:link>
                    </div>

                    <br>
                    <br>

                    <table class="ui celled sortable table la-table">
                        <thead>
                        <tr>
                            <th class="center aligned">
                                ${message(code: 'sidewide.number')}
                            </th>
                            <th>
                                ${message(code: 'org.sortname.label')}
                            </th>
                            <th>
                                ${message(code: 'default.name.label')}
                            </th>
                            <th>
                                ${message(code: 'surveyInfo.finishedDate')}
                            </th>
                            <th>
                                ${message(code: 'surveyTitlesEvaluation.currentAndFixedEntitlements')}
                            </th>
                            <th> ${message(code: 'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${surveyParticipantsHasNotAccess}" var="participant" status="i">
                            <tr>
                                <td>
                                    ${i + 1}
                                </td>
                                <td>
                                    <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant?.id}">
                                        ${participant?.sortname}
                                    </g:link>
                                </td>
                                <td>
                                    <g:link controller="organisation" action="show" id="${participant.id}">
                                        ${fieldValue(bean: participant, field: "name")}
                                    </g:link>
                                </td>
                                <td>
                                    <semui:surveyFinishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                                </td>
                                <td class="center aligned">
                                    <g:set var="subParticipant" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                    <div class="ui circular label">
                                        ${subscriptionService.getIssueEntitlementsFixed(subParticipant)?.size()?:0 } / ${subscriptionService.getIssueEntitlementsNotFixed(subParticipant)?.size()?:0 }
                                    </div>
                                </td>
                                <td>

                                    <g:link action="showEntitlementsRenew"
                                            id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                            class="ui icon button"><i
                                            class="write icon"></i>
                                    </g:link>

                                </td>
                            </tr>

                        </g:each>
                        </tbody>
                    </table>

                </semui:form>

        </div>

        <div class="ui bottom attached tab segment" data-tab="participantsViewAllNotFinish">

            <h2 class="ui icon header la-clear-before la-noMargin-top"><g:message code="surveyEvaluation.participants"/><semui:totalNumber
                    total="${participantsNotFinish?.size()}"/></h2>
            <br>

            <semui:form>

                <h4><g:message code="surveyParticipants.hasAccess"/></h4>

                <g:set var="surveyParticipantsHasAccess"
                       value="${participantsNotFinish?.findAll { it?.hasAccessOrg() }?.sort {
                           it?.sortname
                       }}"/>

                <div class="four wide column">
                    <g:link data-orgIdList="${(surveyParticipantsHasAccess?.id).join(',')}"
                            data-targetId="copyEmailaddresses_ajaxModal4"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                    </g:link>
                </div>

                <br>
                <br>

                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <th>
                            ${message(code: 'org.sortname.label')}
                        </th>
                        <th>
                            ${message(code: 'default.name.label')}
                        </th>
                        <th>
                            ${message(code: 'surveyInfo.finishedDate')}
                        </th>
                        <th>
                            ${message(code: 'surveyTitlesEvaluation.currentAndFixedEntitlements')}
                        </th>
                        <th>
                            ${message(code: 'default.actions.label')}
                        </th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyParticipantsHasAccess}" var="participant" status="i">
                        <tr>
                            <td>
                                ${i + 1}
                            </td>
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant?.id}">
                                    ${participant?.sortname}
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    ${fieldValue(bean: participant, field: "name")}
                                </g:link>
                            </td>
                            <td>
                                <semui:surveyFinishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                            </td>
                            <td class="center aligned">
                                <g:set var="subParticipant" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <div class="ui circular label">
                                    ${subscriptionService.getIssueEntitlementsFixed(subParticipant)?.size()?:0 } / ${subscriptionService.getIssueEntitlementsNotFixed(subParticipant)?.size()?:0 }
                                </div>
                            </td>
                            <td>

                                <g:link action="showEntitlementsRenew"
                                        id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                        class="ui icon button"><i
                                        class="write icon"></i>
                                </g:link>

                            </td>

                        </tr>

                    </g:each>
                    </tbody>
                </table>

                <h4><g:message code="surveyParticipants.hasNotAccess"/></h4>

                <g:set var="surveyParticipantsHasNotAccess" value="${participantsNotFinish.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

                <div class="four wide column">
                    <g:link data-orgIdList="${(surveyParticipantsHasNotAccess?.id).join(',')}"
                            data-targetId="copyEmailaddresses_ajaxModal5"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                    </g:link>
                </div>

                <br>
                <br>

                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <th>
                            ${message(code: 'org.sortname.label')}
                        </th>
                        <th>
                            ${message(code: 'default.name.label')}
                        </th>
                        <th>
                            ${message(code: 'surveyInfo.finishedDate')}
                        </th>
                        <th>
                            ${message(code: 'surveyTitlesEvaluation.currentAndFixedEntitlements')}
                        </th>
                        <th> ${message(code: 'default.actions.label')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyParticipantsHasNotAccess}" var="participant" status="i">
                        <tr>
                            <td>
                                ${i + 1}
                            </td>
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant?.id}">
                                    ${participant?.sortname}
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    ${fieldValue(bean: participant, field: "name")}
                                </g:link>
                            </td>
                            <td>
                                <semui:surveyFinishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                            </td>
                            <td class="center aligned">
                                <g:set var="subParticipant" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <div class="ui circular label">
                                    ${subscriptionService.getIssueEntitlementsFixed(subParticipant)?.size()?:0 } / ${subscriptionService.getIssueEntitlementsNotFixed(subParticipant)?.size()?:0 }
                                </div>
                            </td>
                            <td>

                                <g:link action="showEntitlementsRenew"
                                        id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                        class="ui icon button"><i
                                        class="write icon"></i>
                                </g:link>

                            </td>
                        </tr>

                    </g:each>
                    </tbody>
                </table>

            </semui:form>

        </div>

        <div class="ui bottom attached tab segment" data-tab="participantsView">

            <h2 class="ui icon header la-clear-before la-noMargin-top"><g:message code="surveyEvaluation.participants"/><semui:totalNumber
                    total="${participants?.size()}"/></h2>
            <br>

            <semui:form>

                <h4><g:message code="surveyParticipants.hasAccess"/></h4>

                <g:set var="surveyParticipantsHasAccess"
                       value="${participants?.findAll { it?.hasAccessOrg() }?.sort {
                           it?.sortname
                       }}"/>

                <div class="four wide column">
                    <g:link data-orgIdList="${(surveyParticipantsHasAccess?.id).join(',')}"
                            data-targetId="copyEmailaddresses_ajaxModal6"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                    </g:link>
                </div>

                <br>
                <br>

                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <th>
                            ${message(code: 'org.sortname.label')}
                        </th>
                        <th>
                            ${message(code: 'default.name.label')}
                        </th>
                        <th>
                            ${message(code: 'surveyInfo.finishedDate')}
                        </th>
                        <th>
                            ${message(code: 'surveyTitlesEvaluation.currentAndFixedEntitlements')}
                        </th>
                        <th>
                            ${message(code: 'default.actions.label')}
                        </th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyParticipantsHasAccess}" var="participant" status="i">
                        <tr>
                            <td>
                                ${i + 1}
                            </td>
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant?.id}">
                                    ${participant?.sortname}
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    ${fieldValue(bean: participant, field: "name")}
                                </g:link>
                            </td>
                            <td>
                                <semui:surveyFinishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                            </td>
                            <td class="center aligned">
                                <g:set var="subParticipant" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <div class="ui circular label">
                                    ${subscriptionService.getIssueEntitlementsFixed(subParticipant)?.size()?:0 } / ${subscriptionService.getIssueEntitlementsNotFixed(subParticipant)?.size()?:0 }
                                </div>
                            </td>
                            <td>

                                <g:link action="showEntitlementsRenew"
                                        id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                        class="ui icon button"><i
                                        class="write icon"></i>
                                </g:link>

                            </td>

                        </tr>

                    </g:each>
                    </tbody>
                </table>

                <h4><g:message code="surveyParticipants.hasNotAccess"/></h4>

                <g:set var="surveyParticipantsHasNotAccess" value="${participants.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

                <div class="four wide column">
                    <g:link data-orgIdList="${(surveyParticipantsHasNotAccess?.id).join(',')}"
                            data-targetId="copyEmailaddresses_ajaxModal7"
                            class="ui icon button right floated trigger-modal">
                        <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                    </g:link>
                </div>

                <br>
                <br>

                <table class="ui celled sortable table la-table">
                    <thead>
                    <tr>
                        <th class="center aligned">
                            ${message(code: 'sidewide.number')}
                        </th>
                        <th>
                            ${message(code: 'org.sortname.label')}
                        </th>
                        <th>
                            ${message(code: 'default.name.label')}
                        </th>
                        <th>
                            ${message(code: 'surveyInfo.finishedDate')}
                        </th>
                        <th>
                            ${message(code: 'surveyTitlesEvaluation.currentAndFixedEntitlements')}
                        </th>
                        <th> ${message(code: 'default.actions.label')}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${surveyParticipantsHasNotAccess}" var="participant" status="i">
                        <tr>
                            <td>
                                ${i + 1}
                            </td>
                            <td>
                                <g:link controller="myInstitution" action="manageParticipantSurveys" id="${participant?.id}">
                                    ${participant?.sortname}
                                </g:link>
                            </td>
                            <td>
                                <g:link controller="organisation" action="show" id="${participant.id}">
                                    ${fieldValue(bean: participant, field: "name")}
                                </g:link>
                            </td>
                            <td>
                                <semui:surveyFinishDate participant="${participant}" surveyConfig="${surveyConfig}"/>
                            </td>
                            <td class="center aligned">
                                <g:set var="subParticipant" value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <div class="ui circular label">
                                    ${subscriptionService.getIssueEntitlementsFixed(subParticipant)?.size()?:0 } / ${subscriptionService.getIssueEntitlementsNotFixed(subParticipant)?.size()?:0 }
                                </div>
                            </td>
                            <td>

                                <g:link action="showEntitlementsRenew"
                                        id="${surveyConfig?.id}" params="[participant: participant?.id]"
                                        class="ui icon button"><i
                                        class="write icon"></i>
                                </g:link>

                            </td>
                        </tr>

                    </g:each>
                    </tbody>
                </table>

            </semui:form>

        </div>

    </div>
</div>

<r:script>
    $(document).ready(function () {
        $('.tabular.menu .item').tab()
    });
</r:script>

</body>
</html>
