<%@ page import="de.laser.config.ConfigMapper; de.laser.survey.SurveyConfig; de.laser.survey.SurveyResult; de.laser.Org; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.properties.PropertyDefinition;de.laser.storage.RDStore;de.laser.RefdataCategory; de.laser.survey.SurveyOrg" %>
<laser:serviceInjection/>
<%
    if(showOpenParticipantsAgainButtons) {
        String mailSubject = escapeService.replaceUmlaute(g.message(code: 'email.subject.surveys', args: ["${surveyConfig.surveyInfo.type.getI10n('value')}"]) + " " + surveyConfig.surveyInfo.name + "")

        String mailBody = surveyService.notificationSurveyAsString(surveyConfig.surveyInfo)

        String mailString = ""
    }
%>


<g:if test="${surveyConfig}">

    <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>

    <g:if test="${surveyConfig.subscription}">

        <g:link class="ui right floated button la-inline-labeled" controller="subscription" action="members" id="${subscription.id}">
            <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

            <div class="ui blue circular label">
                ${countParticipants.subMembers}
            </div>
        </g:link>

        <g:link class="ui right floated button la-inline-labeled" controller="survey" action="surveyParticipants"
                id="${surveyConfig.surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id]">
            <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

            <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
        </g:link>

        <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
            ( ${countParticipants.subMembersWithMultiYear}
            ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
        </g:if>

    </g:if>

    <g:if test="${!surveyConfig.subscription}">
        <g:link  class="ui right floated button la-inline-labeled" controller="survey" action="surveyParticipants"
                id="${surveyConfig.surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id]">
            <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>
            <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
        </g:link>

    </g:if>
<br/><br/>



<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_RENEWAL.id] }">

    <div class="la-inline-lists">

        <h3 class="ui header">${message(code:'renewalEvaluation.propertiesChanged')}</h3>

        <g:if test="${propertiesChanged}">
            <g:link class="ui right floated button" controller="survey" action="showPropertiesChanged"
                    id="${surveyConfig.surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id, tab: params.tab, exportXLSX: true]">
                Export ${message(code: 'renewalEvaluation.propertiesChanged')}
            </g:link>
            <br/>
            <br/>
        </g:if>


        <table class="ui la-js-responsive-table la-table table">
                        <thead>
                        <tr>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'propertyDefinition.label')}</th>
                            <th>${message(code:'renewalEvaluation.propertiesChanged')}</th>
                            <th>${message(code: 'default.actions.label')}</th>
                        </tr>
                        </thead>
                        <tbody>

                        <g:each in="${propertiesChanged}" var="property" status="i">
                            <g:set var="propertyDefinition"
                                   value="${PropertyDefinition.findById(property.key)}"/>
                            <tr>
                                <td class="center aligned">
                                    ${i + 1}
                                </td>
                                <td>
                                    ${propertyDefinition.getI10n('name')}
                                </td>
                                <td>${property.value.size()}</td>
                                <td>
                                    <button type="button" onclick="JSPC.app.propertiesChanged(${property.key});">
                                        <g:message code="default.button.show.label"/>
                                    </button>
                                </td>
                            </tr>

                        </g:each>
                        </tbody>
                    </table>



    </div>
</g:if>

</g:if>

<ui:filter>
<g:form action="${actionName}" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, tab: params.tab]">
    <laser:render template="/templates/filter/orgFilter"
              model="[
                      tmplConfigShow      : [['name', 'libraryType', 'subjectGroup'], ['country&region', 'libraryNetwork', 'property&value'], surveyConfig.subscription ? ['hasSubscription'] : []],
                      tmplConfigFormFilter: true
              ]"/>
</g:form>
</ui:filter>



<g:form action="${processAction}" controller="survey" method="post" class="ui form"
        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab]">
    <br/><br/>

    <div class="ui blue large label">
        <g:message code="surveyEvaluation.participants"/>: <div class="detail">${participants.size()}</div>
    </div>

    <h4 class="ui header"><g:message code="surveyParticipants.hasAccess"/></h4>

    <g:set var="surveyParticipantsHasAccess"
           value="${participants.findAll { it.org.hasAccessOrg() }}"/>


        <g:if test="${surveyParticipantsHasAccess}">
            <a data-ui="modal" class="ui icon button right floated"
               data-orgIdList="${(surveyParticipantsHasAccess.org.id)?.join(',')}"
               href="#copyEmailaddresses_static">
                <g:message code="survey.copyEmailaddresses.participantsHasAccess"/>
            </a>
        </g:if>

<br/><br/>



    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <g:if test="${showCheckbox}">
                <th>
                    <g:if test="${surveyParticipantsHasAccess}">
                        <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                    </g:if>
                </th>
            </g:if>

            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">

                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <th>${message(code: 'sidewide.number')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <th>${message(code: 'default.name.label')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                    <g:each in="${surveyConfig.getSortedSurveyProperties()}" var="surveyProperty">
                        <th>${surveyProperty.getI10n('name')}
                            <g:if test="${surveyProperty.getI10n('expl')}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${surveyProperty.getI10n('expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </th>
                    </g:each>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                    <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                <th>
                    ${message(code: 'surveyEvaluation.titles.currentAndFixedEntitlements')}
                </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                <th>
                    ${message(code: 'surveyInfo.finishedDate')}
                </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                    <th>
                        ${message(code: 'surveyOrg.reminderMailDate')}
                    </th>
                </g:if>
            </g:each>
            <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${surveyParticipantsHasAccess}" var="surveyOrg" status="i">

            <g:set var="participant"
                   value="${surveyOrg.org}"/>
            <g:set var="surResults" value="[]"/>
            <g:each in="${surveyConfig.getSortedSurveyProperties()}" var="surveyProperty">
                <% surResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participant, surveyConfig, surveyProperty) %>
            </g:each>
            <tr>
                <g:if test="${showCheckbox}">
                    <td>
                        <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                    </td>
                </g:if>
                <g:each in="${tmplConfigShow}" var="tmplConfigItem">

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                        <td>
                            ${i + 1}
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                        <td>
                            <g:link controller="myInstitution" action="manageParticipantSurveys"
                                    id="${participant.id}">
                                ${participant.sortname}
                            </g:link>
                            <br/>
                            <g:link controller="organisation" action="show" id="${participant.id}">
                                (${fieldValue(bean: participant, field: "name")})
                            </g:link>


                        <g:if test="${surveyConfig.surveyProperties}">
                            <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.processedOrg')}">
                                    <i class="edit green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                    <i class="edit red icon"></i>
                                </span>
                            </g:else>
                        </g:if>

                            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.finishOrg')}">
                                    <i class="check green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                    <i class="x red icon"></i>
                                </span>
                            </g:else>

                            <g:if test="${propertiesChangedByParticipant && participant.id in propertiesChangedByParticipant.id}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                                    <i class="exclamation triangle yellow large icon"></i>
                                </span>
                            </g:if>


                            <g:if test="${!surveyConfig.hasOrgSubscription(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyOrg.orgInsertedItself}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyLinks.newParticipate')}">
                                    <i class="paper plane outline large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${showOpenParticipantsAgainButtons}">
                                <% mailString =
                                        "mailto:${surveyOrg.org.getMailsOfGeneralContactPersons(false).join(';')}?subject=" + mailSubject +
                                                "&body=" + mailBody
                                %>

                                <a href="${mailString}">
                                    <span data-position="right center"
                                          class="la-popup-tooltip la-delay"
                                          data-content="Mail senden an Hauptkontakte">
                                        <i class="ui icon envelope outline la-list-icon"></i>
                                    </span>
                                </a>
                            </g:if>

                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                            <g:each in="${surResults}" var="resultProperty">
                                <td>
                                    <laser:render template="surveyResult"
                                              model="[surResult: resultProperty, surveyOrg: surveyOrg]"/>
                                </td>
                            </g:each>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                        <td class="center aligned">
                            <g:set var="subParticipant"
                                   value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                            <div class="ui circular label">
                            <g:if test="${surveyConfig.pickAndChoosePerpetualAccess}">
                                ${surveyService.countPerpetualAccessTitlesBySub(subParticipant)} / ${subscriptionService.countIssueEntitlementsNotFixed(subParticipant)}
                            </g:if>
                            <g:else>
                                ${subscriptionService.countIssueEntitlementsFixed(subParticipant)} / ${subscriptionService.countIssueEntitlementsNotFixed(subParticipant)}
                            </g:else>
                            </div>

                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="finishDate"/>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="reminderMailDate"/>
                        </td>
                    </g:if>
                </g:each>
                <td>
                    <g:link controller="survey" action="evaluationParticipant"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                            class="ui button blue icon la-modern-button la-popup-tooltip la-delay"
                            data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                        <i class="chart pie icon"></i>
                    </g:link>
                </td>

            </tr>

        </g:each>
        </tbody>
    </table>

    <g:if test="${showOpenParticipantsAgainButtons}">
        <div class="content">
            <div class="ui form twelve wide column">
                <div class="two fields">
                    <g:if test="${params.tab == 'participantsViewAllNotFinish' ? 'active' : ''}">
                        <div class="eight wide field" style="text-align: left;">
                            <button name="openOption" type="submit" value="ReminderMail" class="ui button">
                                ${message(code: 'openParticipantsAgain.reminder.participantsHasAccess')}
                            </button>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>
    </g:if>
    <br/><br/>
    <h4 class="ui header"><g:message code="surveyParticipants.hasNotAccess"/></h4>

    <g:set var="surveyParticipantsHasNotAccess"
           value="${participants.findAll { !it.org.hasAccessOrg() }}"/>


    <g:if test="${surveyParticipantsHasNotAccess}">
        <a data-ui="modal" class="ui icon button right floated"
           data-orgIdList="${(surveyParticipantsHasNotAccess.org.id)?.join(',')}"
           href="#copyEmailaddresses_static">
            <g:message code="survey.copyEmailaddresses.participantsHasNoAccess"/>
        </a>
    </g:if>


    <table class="ui celled sortable table la-js-responsive-table la-table">
        <thead>
        <tr>
            <g:if test="${showCheckbox}">
                <th>
                    <g:if test="${surveyParticipantsHasNotAccess && actionName != 'openParticipantsAgain' && params.tab != 'participantsViewAllNotFinish'}">
                        <g:checkBox name="orgListToggler" id="orgListToggler" checked="false"/>
                    </g:if>
                </th>
            </g:if>

            <g:each in="${tmplConfigShow}" var="tmplConfigItem" status="i">

                <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                    <th>${message(code: 'sidewide.number')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                    <th>${message(code: 'default.name.label')}</th>
                </g:if>

                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                    <g:each in="${surveyConfig.getSortedSurveyProperties()}" var="surveyProperty">
                        <th>${surveyProperty.getI10n('name')}
                            <g:if test="${surveyProperty.getI10n('expl')}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                      data-content="${surveyProperty.getI10n('expl')}">
                                    <i class="question circle icon"></i>
                                </span>
                            </g:if>
                        </th>
                    </g:each>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                    <th>${message(code: 'surveyResult.commentOnlyForOwner')}
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${message(code: 'surveyResult.commentOnlyForOwner.info')}">
                            <i class="question circle icon"></i>
                        </span>
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                    <th>
                        ${message(code: 'surveyEvaluation.titles.currentAndFixedEntitlements')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                    <th>
                        ${message(code: 'surveyInfo.finishedDate')}
                    </th>
                </g:if>
                <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                    <th>
                        ${message(code: 'surveyOrg.reminderMailDate')}
                    </th>
                </g:if>

            </g:each>
            <th scope="col" rowspan="2" class="two">${message(code:'default.actions.label')}</th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${surveyParticipantsHasNotAccess}" var="surveyOrg" status="i">

            <g:set var="participant"
                   value="${surveyOrg.org}"/>

            <g:set var="surResults" value="[]"/>
            <g:each in="${surveyConfig.getSortedSurveyProperties()}" var="surveyProperty">
                <% surResults << SurveyResult.findByParticipantAndSurveyConfigAndType(participant, surveyConfig, surveyProperty) %>
            </g:each>

            <tr>
                <g:if test="${showCheckbox}">
                    <g:if test="${actionName != 'openParticipantsAgain' && params.tab != 'participantsViewAllNotFinish'}">
                    <td>
                        <g:checkBox name="selectedOrgs" value="${participant.id}" checked="false"/>
                    </td>
                    </g:if>
                </g:if>
                <g:each in="${tmplConfigShow}" var="tmplConfigItem">

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('lineNumber')}">
                        <td>
                            ${i + 1}
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('name')}">
                        <td>
                            <g:link controller="myInstitution" action="manageParticipantSurveys"
                                    id="${participant.id}">
                                ${participant.sortname}
                            </g:link>
                            <br/>
                            <g:link controller="organisation" action="show" id="${participant.id}">
                                (${fieldValue(bean: participant, field: "name")})
                            </g:link>

                        <g:if test="${surveyConfig.surveyProperties}">
                            <g:if test="${surveyConfig.checkResultsEditByOrg(participant) == SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.processedOrg')}">
                                    <i class="edit green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notprocessedOrg')}">
                                    <i class="edit red icon"></i>
                                </span>
                            </g:else>
                        </g:if>

                            <g:if test="${surveyConfig.isResultsSetFinishByOrg(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.finishOrg')}">
                                    <i class="check green icon"></i>
                                </span>
                            </g:if>
                            <g:else>
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.notfinishOrg')}">
                                    <i class="x red icon"></i>
                                </span>
                            </g:else>

                            <g:if test="${propertiesChangedByParticipant && participant.id in propertiesChangedByParticipant.id}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'renewalEvaluation.propertiesChanged')}">
                                    <i class="exclamation triangle yellow large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${!surveyConfig.hasOrgSubscription(participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyOrg.orgInsertedItself}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyLinks.newParticipate')}">
                                    <i class="paper plane outline large icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${showOpenParticipantsAgainButtons}">
                                <% mailString =
                                        "mailto:${surveyOrg.org.getMailsOfGeneralContactPersons(false).join(';')}?subject=" + mailSubject +
                                                "&body=" + mailBody
                                %>

                                <a href="${mailString}">
                                    <span data-position="right center"
                                          class="la-popup-tooltip la-delay"
                                          data-content="Mail senden an Hauptkontakte">
                                        <i class="ui icon envelope outline la-list-icon"></i>
                                    </span>
                                </a>
                            </g:if>

                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyProperties')}">
                        <g:each in="${surResults}" var="resultProperty">
                            <td>
                                <laser:render template="surveyResult"
                                          model="[surResult: resultProperty, surveyOrg: surveyOrg]"/>
                            </td>
                        </g:each>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('commentOnlyForOwner')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="text" field="ownerComment"/>
                        </td>
                    </g:if>

                    <g:if test="${tmplConfigItem.equalsIgnoreCase('surveyTitlesCount')}">
                        <td class="center aligned">
                            <g:set var="subParticipant"
                                   value="${surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                            <div class="ui circular label">
                                ${subscriptionService.countIssueEntitlementsFixed(subParticipant)} / ${subscriptionService.countIssueEntitlementsNotFixed(subParticipant)}
                            </div>

                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('finishedDate')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="finishDate"/>
                        </td>
                    </g:if>
                    <g:if test="${tmplConfigItem.equalsIgnoreCase('reminderMailDate')}">
                        <td>
                            <ui:xEditable owner="${surveyOrg}" type="date" field="reminderMailDate"/>
                        </td>
                    </g:if>

                </g:each>
                <td>
                    <g:link controller="survey" action="evaluationParticipant"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, participant: participant.id]"
                            class="ui button blue icon la-modern-button la-popup-tooltip la-delay"
                            data-content="${message(code: 'surveyInfo.toSurveyInfos')}">
                        <i class="chart pie icon"></i>
                    </g:link>
                </td>
            </tr>

        </g:each>
        </tbody>
    </table>

    <g:if test="${showTransferFields}">
        <br />
        <br />
        <ui:greySegment>
        <div class="ui form">
        <h3 class="ui header">${message(code: 'surveyTransfer.info.label')}:</h3>
            <div class="two fields">
                <div class="ui field">
                     <div class="field">
                        <label>${message(code: 'filter.status')}</label>
                        <ui:select class="ui dropdown" name="status" id="status"
                                      from="${ RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_STATUS) }"
                                      optionKey="id"
                                      optionValue="value"
                                      multiple="true"
                                      value="${RDStore.SUBSCRIPTION_CURRENT.id}"
                                      noSelection="${['' : message(code:'default.select.choose.label')]}"
                                      onchange="JSPC.app.adjustDropdown()"/>
                    </div>
                    <br />
                    <br id="element-vor-target-dropdown" />
                    <br />

                </div>
                <div class="field">
                    <ui:datepicker label="subscription.startDate.label" id="startDate" name="startDate" value=""/>

                    <ui:datepicker label="subscription.endDate.label" id="endDate" name="endDate" value=""/>
                </div>
            </div>

            <input class="ui button" type="submit" value="${message(code: 'surveyTransfer.button')}">
        </ui:greySegment>
        </div>

    </g:if>

    <g:if test="${showOpenParticipantsAgainButtons}">
        <div class="content">
            <div class="ui form twelve wide column">
                <div class="two fields">
                    <g:if test="${params.tab == 'participantsViewAllFinish' ? 'active' : ''}">

                        <div class="eight wide field" style="text-align: left;">
                            <button name="openOption" type="submit" value="OpenWithoutMail" class="ui button">
                                ${message(code: 'openParticipantsAgain.openWithoutMail.button')}
                            </button>
                        </div>

                        <div class="eight wide field" style="text-align: right;">
                            <button name="openOption" type="submit" value="OpenWithMail" class="ui button">
                                ${message(code: 'openParticipantsAgain.openWithMail.button')}
                            </button>
                        </div>
                    </g:if>
                </div>
            </div>
        </div>
    </g:if>

</g:form>



<laser:script file="${this.getGroovyPageFileName()}">
<g:if test="${showCheckbox}">
    $('#orgListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedOrgs]").prop('checked', false)
        }
    })
</g:if>
<g:if test="${showTransferFields}">
JSPC.app.adjustDropdown = function () {

    var url = '<g:createLink controller="ajaxJson" action="adjustSubscriptionList"/>'

    var status = $("select#status").serialize()
    if (status) {
        url = url + '?' + status
    }

    $.ajax({
        url: url,
        success: function (data) {
            var select = '';
            for (var index = 0; index < data.length; index++) {
                var option = data[index];
                var optionText = option.text;
                var optionValue = option.value;
                var count = index + 1
                // console.log(optionValue +'-'+optionText)

                select += '<div class="item" data-value="' + optionValue + '">'+ count + ': ' + optionText + '</div>';
            }

            select = ' <div class="ui fluid search selection dropdown la-filterProp">' +
'   <input type="hidden" id="subscription" name="targetSubscriptionId">' +
'   <i class="dropdown icon"></i>' +
'   <div class="default text">${message(code: 'default.select.choose.label')}</div>' +
'   <div class="menu">'
+ select +
'</div>' +
'</div>';

            $('#element-vor-target-dropdown').next().replaceWith(select);

            $('.la-filterProp').dropdown({
                duration: 150,
                transition: 'fade',
                clearable: true,
                forceSelection: false,
                selectOnKeydown: false,
                onChange: function (value, text, $selectedItem) {
                    value.length === 0 ? $(this).removeClass("la-filter-selected") : $(this).addClass("la-filter-selected");
                }
            });
        }, async: false
    });
}

JSPC.app.adjustDropdown()
</g:if>
JSPC.app.propertiesChanged = function (propertyDefinitionId) {
    $.ajax({
        url: '<g:createLink controller="survey" action="showPropertiesChanged" params="[tab: params.tab, surveyConfigID: surveyConfig.id, id: surveyInfo.id]"/>&propertyDefinitionId='+propertyDefinitionId,
            success: function(result){
                $("#dynamicModalContainer").empty();
                $("#modalPropertiesChanged").remove();

                $("#dynamicModalContainer").html(result);
                $("#dynamicModalContainer .ui.modal").modal('show');
            }
        });
    }

</laser:script>

