<%@ page import="de.laser.SurveyConfig; de.laser.RefdataCategory;de.laser.properties.PropertyDefinition;de.laser.RefdataValue;de.laser.helper.RDStore;" %>
<laser:serviceInjection/>
<g:set var="subscriptionService" bean="subscriptionService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'surveyTitlesEvaluation.label')}</title>
</head>

<body>

<g:render template="breadcrumb" model="${[params: params]}"/>

<semui:controlButtons>
    <g:if test="${surveyInfo.status != RDStore.SURVEY_IN_PROCESSING}">
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="surveyTitlesEvaluation" id="${surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id, exportXLSX: true]">${message(code: 'survey.exportSurvey')}</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
    </g:if>

    <g:render template="actions"/>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
<semui:xEditable owner="${surveyInfo}" field="name"/>
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<g:render template="nav"/>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<br />

<div class="ui grid">

    <div class="sixteen wide stretched column">
        <div class="ui top attached tabular menu">

            <a class="item ${surveyConfig.surveyProperties?.size() > 0 ? 'active' : ''}" data-tab="participantsViewAllFinish">
                ${message(code: 'surveyEvaluation.participantsViewAllFinish')}
                <div class="ui floating circular label">${participantsFinish.size() ?: 0}</div>
            </a>

            <a class="item" data-tab="participantsViewAllNotFinish">
                ${message(code: 'surveyEvaluation.participantsViewAllNotFinish')}
                <div class="ui floating circular label">${participantsNotFinish.size() ?: 0}</div>
            </a>

            <a class="item" data-tab="participantsView">
                ${message(code: 'surveyEvaluation.participantsView')}
                <div class="ui floating circular label">${participants?.size() ?: 0}</div>
            </a>

        </div>

        <div class="ui bottom attached tab segment ${surveyConfig.surveyProperties?.size() > 0 ? 'active' : ''}" data-tab="participantsViewAllFinish">

                <h2 class="ui icon header la-clear-before la-noMargin-top"><g:message code="surveyEvaluation.participants"/><semui:totalNumber
                        total="${participantsFinish.size()}"/></h2>
                <g:if test="${surveyInfo && surveyInfo.status?.id == RDStore.SURVEY_IN_EVALUATION.id}">
                                <g:link controller="survey" action="completeIssueEntitlementsSurvey" id="${surveyConfig.id}"
                                        class="ui icon button right floated">
                                    <g:message code="completeIssueEntitlementsSurvey.forFinishParticipant.label"/>
                                </g:link>
                </g:if>
                <br />
                <br />
                <semui:form>

                    <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

                    <g:set var="surveyParticipantsHasAccess"
                           value="${participantsFinish.findAll { it?.hasAccessOrg() }?.sort {
                               it?.sortname
                           }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                        </a>
                    </g:if>
                    </div>

                    <br />
                    <br />

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

                                    <g:link action="surveyTitlesSubscriber"
                                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant?.id]"
                                            class="ui icon button"><i
                                            class="write icon"></i>
                                    </g:link>

                                </td>

                            </tr>

                        </g:each>
                        </tbody>
                    </table>

                    <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

                    <g:set var="surveyParticipantsHasNotAccess" value="${participantsFinish.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

                    <div class="four wide column">
                    <g:if test="${surveyParticipantsHasNotAccess}">
                        <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                        </a>
                    </g:if>
                    </div>

                    <br />
                    <br />

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

                                    <g:link action="surveyTitlesSubscriber"
                                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant?.id]"
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
            <br />

            <semui:form>

                <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

                <g:set var="surveyParticipantsHasAccess"
                       value="${participantsNotFinish?.findAll { it?.hasAccessOrg() }?.sort {
                           it?.sortname
                       }}"/>

                <div class="four wide column">
                <g:if test="${surveyParticipantsHasAccess}">
                    <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                        <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                    </a>
                </g:if>
                </div>

                <br />
                <br />

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

                                <g:link action="surveyTitlesSubscriber"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant?.id]"
                                        class="ui icon button"><i
                                        class="write icon"></i>
                                </g:link>

                            </td>

                        </tr>

                    </g:each>
                    </tbody>
                </table>

                <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

                <g:set var="surveyParticipantsHasNotAccess" value="${participantsNotFinish.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

                <div class="four wide column">
                <g:if test="${surveyParticipantsHasNotAccess}">
                    <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                        <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                    </a>
                </g:if>
                </div>

                <br />
                <br />

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

                                <g:link action="surveyTitlesSubscriber"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant?.id]"
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
            <br />

            <semui:form>

                <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

                <g:set var="surveyParticipantsHasAccess"
                       value="${participants?.findAll { it?.hasAccessOrg() }?.sort {
                           it?.sortname
                       }}"/>

                <div class="four wide column">
                <g:if test="${surveyParticipantsHasAccess}">
                    <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                        <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
                    </a>
                </g:if>
                </div>

                <br />
                <br />

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

                                <g:link action="surveyTitlesSubscriber"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant?.id]"
                                        class="ui icon button"><i
                                        class="write icon"></i>
                                </g:link>

                            </td>

                        </tr>

                    </g:each>
                    </tbody>
                </table>

                <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

                <g:set var="surveyParticipantsHasNotAccess" value="${participants.findAll { !it?.hasAccessOrg() }.sort { it?.sortname }}"/>

                <div class="four wide column">
                <g:if test="${surveyParticipantsHasNotAccess}">
                    <a data-semui="modal" class="ui icon button right floated" data-orgIdList="${(surveyParticipantsHasNotAccess.id)?.join(',')}" href="#copyEmailaddresses_static">
                        <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
                    </a>
                </g:if>
                </div>

                <br />
                <br />

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

                                <g:link action="surveyTitlesSubscriber"
                                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant?.id]"
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

</body>
</html>
