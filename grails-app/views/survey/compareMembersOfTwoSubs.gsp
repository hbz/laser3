<%@ page import="de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org;de.laser.survey.SurveyOrg" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyInfo.transferMembers')})" serviceInjection="true"/>

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.transferMembers" class="active"/>
</ui:breadcrumbs>

%{--<ui:controlButtons>
    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <ui:actionsDropdown>
            <g:if test="${parentSuccessorSubscription}">

                <ui:actionsDropdownItem data-semui="modal" href="#transferParticipantsModal"
                                           message="surveyInfo.transferParticipants"/>
            </g:if>
            <ui:actionsDropdownItem controller="survey" action="renewalEvaluation"
                                       params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.evaluation"/>

        --}%%{--<ui:actionsDropdownItem controller="survey" action="setCompleted"
                                   params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                   message="surveyInfo.completed.action"/>--}%%{--
        </ui:actionsDropdown>
    </g:if>
</ui:controlButtons>--}%

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey" />
<ui:surveyStatus object="${surveyInfo}"/>


<g:if test="${surveyConfig.subSurveyUseForTransfer}">
    <laser:render template="nav"/>
</g:if>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br/>

<g:if test="${surveyConfig.subSurveyUseForTransfer && !(surveyInfo.status in [RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED])}">
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:if>
<g:else>


    <g:if test="${!parentSuccessorSubscription}">
        <h3 class="ui header">
        <g:message code="renewalEvaluation.parentSuccessorSubscription"/>:
        <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo.id}"
                params="[surveyConfig: surveyConfig.id, parentSub: parentSubscription.id]"
                class="ui button ">
            <g:message code="renewalEvaluation.newSub"/>
        </g:link>
        </h3>

    </g:if>
    <g:else>
        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <g:if test="${parentSuccessorSubscription.getAllSubscribers().size() > 0}">
            <g:link controller="subscription" action="copyElementsIntoSubscription" id="${parentSubscription.id}"
                    params="[sourceObjectId: genericOIDService.getOID(parentSubscription), targetObjectId: genericOIDService.getOID(parentSuccessorSubscription), isRenewSub: true, fromSurvey: true]"
                    class="ui button ">
                <g:message code="renewalEvaluation.newSub.change"/>
            </g:link>
        </g:if>
        <g:else>
                <a class="ui button" data-semui="modal" href="#transferParticipantsModal"><g:message code="surveyInfo.transferParticipants"/></a>
        </g:else>
        <br>
        </g:if>

        <div class="ui tablet stackable steps">

            <div class="${(actionName == 'compareMembersOfTwoSubs') ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="compareMembersOfTwoSubs"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]">
                            ${message(code: 'surveyInfo.transferMembers')}
                        </g:link>
                    </div>

                    <div class="description">
                        <i class="exchange icon"></i>${message(code: 'surveyInfo.transferMembers')}
                    </div>
                </div>
            &nbsp;&nbsp;
                <g:if test="${transferWorkflow && transferWorkflow.transferMembers == 'true'}">
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferMembers: false]">
                        <i class="check bordered large green icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferMembers: true]">
                        <i class="close bordered large red icon"></i>
                    </g:link>
                </g:else>

            </div>

            <div class="${(actionName == 'copyProperties' && params.tab == 'surveyProperties') ? 'active' : ''} step">

                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copyProperties"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription?.id]">
                            ${message(code: 'copyProperties.surveyProperties.short')}
                        </g:link>
                    </div>

                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>
                </div>
            &nbsp;&nbsp;
                <g:if test="${transferWorkflow && transferWorkflow.transferSurveyProperties == 'true'}">
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyProperties: false]">
                        <i class="check bordered large green icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyProperties: true]">
                        <i class="close bordered large red icon"></i>
                    </g:link>
                </g:else>
            </div>

            <div class="${(actionName == 'copyProperties' && params.tab == 'customProperties') ? 'active' : ''}  step">

                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copyProperties"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'customProperties', targetSubscriptionId: targetSubscription?.id]">
                            ${message(code: 'copyProperties.customProperties.short')}
                        </g:link>
                    </div>

                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>
                </div>
            &nbsp;&nbsp;
                <g:if test="${transferWorkflow && transferWorkflow.transferCustomProperties == 'true'}">
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferCustomProperties: false]">
                        <i class="check bordered large green icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferCustomProperties: true]">
                        <i class="close bordered large red icon"></i>
                    </g:link>
                </g:else>

            </div>

            <div class="${(actionName == 'copyProperties' && params.tab == 'privateProperties') ? 'active' : ''} step">

                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copyProperties"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'privateProperties', targetSubscriptionId: targetSubscription?.id]">
                            ${message(code: 'copyProperties.privateProperties.short')}
                        </g:link>
                    </div>

                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>
                </div>
            &nbsp;&nbsp;
                <g:if test="${transferWorkflow && transferWorkflow.transferPrivateProperties == 'true'}">
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferPrivateProperties: false]">
                        <i class="check bordered large green icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferPrivateProperties: true]">
                        <i class="close bordered large red icon"></i>
                    </g:link>
                </g:else>

            </div>

            <div class="${(actionName == 'copySurveyCostItems') ? 'active' : ''} step">

                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copySurveyCostItems"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]">
                            ${message(code: 'copySurveyCostItems.surveyCostItems')}
                        </g:link>
                    </div>

                    <div class="description">
                        <i class="money bill alternate outline icon"></i>${message(code: 'copySurveyCostItems.surveyCostItem')}
                    </div>
                </div>
            &nbsp;&nbsp;
                <g:if test="${transferWorkflow && transferWorkflow.transferSurveyCostItems == 'true'}">
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyCostItems: false]">
                        <i class="check bordered large green icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyCostItems: true]">
                        <i class="close bordered large red icon"></i>
                    </g:link>
                </g:else>

            </div>

        </div>

        <h2 class="ui header">
            ${message(code: 'surveyInfo.transferMembers')}
        </h2>



        <ui:form>
            <div class="ui grid">

                <div class="row">
                    <div class="eight wide column">
                        <h3 class="ui header center aligned">

                            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                                <g:message code="renewalEvaluation.parentSubscription"/>:
                            </g:if><g:else>
                            <g:message code="copyElementsIntoObject.sourceObject.name"
                                       args="[message(code: 'subscription.label')]"/>:
                        </g:else><br/>
                            <g:if test="${parentSubscription}">
                                <g:link controller="subscription" action="show"
                                        id="${parentSubscription.id}">${parentSubscription.dropdownNamingConvention()}</g:link>
                                <br/>
                                <g:link controller="subscription" action="members"
                                        id="${parentSubscription.id}">${message(code: 'renewalEvaluation.orgsInSub')}</g:link>
                                <ui:totalNumber total="${parentSubscription.getDerivedSubscribers().size()}"/>
                            </g:if>
                        </h3>
                    </div>

                    <div class="eight wide column">
                        <h3 class="ui header center aligned">
                            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                                <g:message code="renewalEvaluation.parentSuccessorSubscription"/>:
                            </g:if><g:else>
                            <g:message code="copyElementsIntoObject.targetObject.name"
                                       args="[message(code: 'subscription.label')]"/>:
                        </g:else><br/>
                            <g:if test="${parentSuccessorSubscription}">
                                <g:link controller="subscription" action="show"
                                        id="${parentSuccessorSubscription.id}">${parentSuccessorSubscription.dropdownNamingConvention()}</g:link>
                                <br/>
                                <g:link controller="subscription" action="members"
                                        id="${parentSuccessorSubscription.id}">${message(code: 'renewalEvaluation.orgsInSub')}</g:link>
                                <ui:totalNumber
                                        total="${parentSuccessorSubscription.getDerivedSubscribers().size()}"/>

                            </g:if>
                            <g:else>
                                <g:message code="renewalEvaluation.noParentSuccessorSubscription"/>
                            %{--<br />
                            <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey"
                                    id="${surveyInfo.id}"
                                    params="[surveyConfig: surveyConfig.id, parentSub: parentSubscription.id]"
                                    class="ui button ">
                                <g:message code="renewalEvaluation.newSub"/>
                            </g:link>--}%
                            </g:else>
                        </h3>
                    </div>
                </div>
            </div>
        </ui:form>
        <ui:form>
            <g:set var="count" value="${0}"/>
            <g:set var="count2" value="${0}"/>
            <div class="ui grid">

                <div class="row">

                    <div class="eight wide column">

                        <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
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
                                    <g:set var="termination"
                                           value="${!(participant in parentSuccessortParticipantsList)}"/>
                                    <g:set var="participantSub"
                                           value="${parentSubscription.getDerivedSubscriptionBySubscribers(participant)}"/>
                                    <tr class=" ${termination ? 'negative' : ''}">
                                        <g:set var="count" value="${count + 1}"/>
                                        <td>${count}</td>
                                        <td class="titleCell">
                                            <g:if test="${participantSub && participantSub.isMultiYear}">
                                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                      data-position="bottom center"
                                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                    <i class="map orange icon"></i>
                                                </span>
                                            </g:if>
                                            <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                    id="${participant.id}">
                                                ${participant.sortname}
                                            </g:link>
                                            <br/>
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
                                                        <strong>${participantSub.status.getI10n('value')}</strong>
                                                    </div>
                                                </div>
                                            </g:if>

                                        </td>
                                        <td>
                                            <g:if test="${participantSub}">
                                                <g:link controller="subscription" action="show"
                                                        id="${participantSub.id}"
                                                        class="ui button icon"><i class="icon clipboard"></i></g:link>
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:if>
                                <g:else>
                                    <tr>
                                        <td></td>
                                        <td class="titleCell"></td>
                                        <td></td>
                                    </tr>
                                </g:else>
                            </g:each>
                            </tbody>
                        </table>

                    </div>

                    <div class="eight wide column">

                        <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSuccessorSubscription">
                            <thead>
                            <tr>
                                <th>${message(code: 'sidewide.number')}</th>
                                <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                                <th></th>
                            </tr>
                            </thead>
                            <g:each in="${participantsList}" var="participant" status="j">
                                <g:if test="${participant in parentSuccessortParticipantsList}">
                                    <g:set var="participantSub"
                                           value="${parentSuccessorSubscription.getDerivedSubscriptionBySubscribers(participant)}"/>
                                    <tr class=" ${participant in parentParticipantsList ? '' : 'positive'}">
                                        <g:set var="count2" value="${count2 + 1}"/>
                                        <td>${count2}</td>
                                        <td class="titleCell">
                                            <g:if test="${participantSub && participantSub.isMultiYear}">
                                                <span class="la-long-tooltip la-popup-tooltip la-delay"
                                                      data-position="bottom center"
                                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                    <i class="map orange icon"></i>
                                                </span>
                                            </g:if>
                                            <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                    id="${participant.id}">
                                                ${participant.sortname}
                                            </g:link>
                                            <br/>
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
                                                        <strong>${participantSub.status.getI10n('value')}</strong>
                                                    </div>
                                                </div>
                                            </g:if>

                                        </td>
                                        <td>
                                            <g:if test="${participantSub}">
                                                <g:link controller="subscription" action="show"
                                                        id="${participantSub.id}"
                                                        class="ui button icon"><i class="icon clipboard"></i></g:link>
                                            </g:if>
                                        </td>
                                    </tr>
                                </g:if>
                                <g:else>
                                    <tr>
                                        <td></td>
                                        <td class="titleCell"></td>
                                    </tr>
                                </g:else>
                            </g:each>

                        </table>
                    </div>
                </div>
            </div>

        </ui:form>

        <div class="sixteen wide field" style="text-align: center;">
            <g:link class="ui button" controller="survey" action="copyProperties"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription?.id]">
                ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
            </g:link>
        </div>

        <laser:script file="${this.getGroovyPageFileName()}">
            $("#parentSubscription .titleCell").each(function(k) {
                var v = $(this).height();
                $("#parentSuccessorSubscription .titleCell").eq(k).height(v);
            });

            $("#parentSuccessorSubscription .titleCell").each(function(k) {
                var v = $(this).height();
                $("#parentSubscription .titleCell").eq(k).height(v);
            });
        </laser:script>

        <g:if test="${surveyConfig.subSurveyUseForTransfer && parentSuccessorSubscription}">

            <laser:render template="transferParticipantsModal"/>

        </g:if>

        <g:form action="setSurveyCompleted" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

            <div class="ui right floated compact segment">
                <div class="ui checkbox">
                    <input type="checkbox" onchange="this.form.submit()"
                           name="surveyCompleted" ${surveyInfo.status.id == RDStore.SURVEY_COMPLETED.id ? 'checked' : ''}>
                    <label><g:message code="surveyInfo.status.completed"/></label>
                </div>
            </div>

        </g:form>

    </g:else>

</g:else>

<laser:htmlEnd />
