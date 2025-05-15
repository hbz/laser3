<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.PropertyStore; de.laser.storage.RDConstants; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.RefdataValue;de.laser.Org;de.laser.survey.SurveyOrg" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyInfo.transferMembers')})" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                  params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.transferMembers" class="active"/>
</ui:breadcrumbs>

<ui:controlButtons>
    <laser:render template="exports"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey"/>
<uiSurvey:status object="${surveyInfo}"/>


<g:if test="${surveyConfig.subSurveyUseForTransfer}">
    <laser:render template="nav"/>
</g:if>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<br/>

<g:if test="${(surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])}">
    <div class="ui segment">
        <strong>${message(code: 'survey.notStarted')}</strong>
    </div>
</g:if>
<g:else>

    <g:render template="multiYearsSubs"/>

    <g:if test="${parentSuccessorSubscription}">

        <g:render template="navCompareMembers"/>

     %{--   <h2 class="ui header">
            ${message(code: 'surveyInfo.transferMembers')}
        </h2>--}%



        <ui:greySegment>
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
                                <ui:totalNumber total="${parentSubscription.getDerivedNonHiddenSubscribers().size()}"/>
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
                                        total="${parentSuccessorSubscription.getDerivedNonHiddenSubscribers().size()}"/>

                            </g:if>
                            <g:else>
                                <g:message code="renewalEvaluation.noParentSuccessorSubscription"/>
                            </g:else>
                        </h3>
                    </div>
                </div>
            </div>
        </ui:greySegment>
        <ui:greySegment>
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
                                <g:if test="${participant.id in parentParticipantsList?.id}">
                                    <g:set var="termination"
                                           value="${!(participant.id in parentSuccessortParticipantsList?.id)}"/>
                                    <g:set var="participantSub"
                                           value="${parentSubscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
                                    <tr class="${termination ? 'negative' : ''}">
                                        <g:set var="count" value="${count + 1}"/>
                                        <td>${count}</td>
                                        <td class="titleCell">
                                            <g:if test="${participantSub && participantSub.isMultiYear}">
                                                <ui:multiYearIcon isConsortial="true" color="orange"/>
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
                                                        class="${Btn.ICON.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                                            </g:if>

                                            <g:set var="multiYearResultProperties"
                                                   value="${surveyService.getMultiYearResultProperties(surveyConfig, participant)}"/>
                                            <g:if test="${surveyConfig.subSurveyUseForTransfer && multiYearResultProperties.size() > 0}">
                                                <br>
                                                <br>

                                                <div data-tooltip="${message(code: 'surveyProperty.label') + ': ' + multiYearResultProperties.collect { it.getI10n('name') }.join(', ') + ' = ' + message(code: 'refdata.Yes')}">
                                                    <i class="bordered colored info icon"></i>
                                                </div>
                                            </g:if>



                                            <g:if test="${SurveyOrg.findByOrgAndSurveyConfig(participant, surveyConfig)}">
                                                <br>
                                                <br>

                                                <div data-tooltip="${message(code: 'surveyParticipants.selectedParticipants')}">
                                                    <i class="${Icon.SURVEY} bordered colored"></i>
                                                </div>
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
                                <g:if test="${participant.id in parentSuccessortParticipantsList?.id}">
                                    <g:set var="surveyOrg" value="${SurveyOrg.findByOrgAndSurveyConfig(participant, surveyConfig)}"/>
                                    <g:set var="participantSub"
                                           value="${parentSuccessorSubscription.getDerivedSubscriptionForNonHiddenSubscriber(participant)}"/>
                                    <tr class="${(participant.id in parentParticipantsList?.id || !surveyOrg) ? '' : 'positive'}">
                                        <g:set var="count2" value="${count2 + 1}"/>
                                        <td>${count2}</td>
                                        <td class="titleCell">
                                            <g:if test="${participantSub && participantSub.isMultiYear}">
                                                <ui:multiYearIcon isConsortial="true" color="orange"/>
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
                                                        class="${Btn.ICON.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                                            </g:if>

                                            <g:set var="multiYearResultProperties"
                                                   value="${surveyService.getMultiYearResultProperties(surveyConfig, participant)}"/>
                                            <g:if test="${surveyConfig.subSurveyUseForTransfer && multiYearResultProperties.size() > 0}">
                                                <br>
                                                <br>

                                                <div data-tooltip="${message(code: 'surveyProperty.label') + ': ' + multiYearResultProperties.collect { it.getI10n('name') }.join(', ') + ' = ' + message(code: 'refdata.Yes')}">
                                                    <i class="bordered colored info icon"></i>
                                                </div>
                                            </g:if>



                                            <g:if test="${surveyOrg}">
                                                <br>
                                                <br>

                                                <div data-tooltip="${message(code: 'surveyParticipants.selectedParticipants')}">
                                                    <i class="${Icon.SURVEY} bordered colored"></i>
                                                </div>
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

        </ui:greySegment>

        <div class="sixteen wide field" style="text-align: center;">
            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                <g:link class="${Btn.SIMPLE}" controller="survey" action="copySubPackagesAndIes"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription.id]">
                    ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
                </g:link>
            </g:if>
            <g:elseif test="${surveyConfig.packageSurvey}">
                <g:link class="${Btn.SIMPLE}" controller="survey" action="copySurveyPackages"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription.id]">
                    ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
                </g:link>
            </g:elseif>
            <g:elseif test="${surveyConfig.vendorSurvey}">
                <g:link class="${Btn.SIMPLE}" controller="survey" action="copySurveyVendors"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription.id]">
                    ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
                </g:link>
            </g:elseif>
            <g:else>
                <g:link class="${Btn.SIMPLE}" controller="survey" action="copyProperties"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription.id]">
                    ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
                </g:link>
            </g:else>
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

        <g:form action="setStatus" method="post" class="ui form"
                params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, newStatus: 'setSurveyCompleted']">

            <div class="ui right floated compact segment">
                <div class="ui checkbox">
                    <input type="checkbox" onchange="this.form.submit()"
                           name="surveyCompleted" ${surveyInfo.status.id == RDStore.SURVEY_COMPLETED.id ? 'checked' : ''}>
                    <label><g:message code="surveyInfo.status.completed"/></label>
                </div>
            </div>

        </g:form>

    </g:if>

</g:else>

<laser:htmlEnd/>
