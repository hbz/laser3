<%@ page import="de.laser.survey.SurveyConfig; de.laser.survey.SurveyOrg; de.laser.interfaces.CalculatedType; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition; de.laser.RefdataCategory; de.laser.RefdataValue; de.laser.Org; de.laser.Subscription" %>
<laser:htmlStart text="${message(code: 'survey.label')} (${message(code: 'surveyInfo.evaluation')})" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.evaluation" class="active"/>
</ui:breadcrumbs>


<ui:controlButtons>
    <ui:exportDropdown>
        <ui:actionsDropdownItem data-ui="modal" href="#individuallyExportModal" message="renewalEvaluation.exportExcelRenewal"/>
    </ui:exportDropdown>
    <laser:render template="actions"/>
</ui:controlButtons>

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey">
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br />

<h2 class="ui icon header la-clear-before la-noMargin-top">
    <g:if test="${surveyConfig.type in [SurveyConfig.SURVEY_CONFIG_TYPE_SUBSCRIPTION, SurveyConfig.SURVEY_CONFIG_TYPE_ISSUE_ENTITLEMENT]}">
        <i class="icon clipboard outline la-list-icon"></i>
        <g:link controller="subscription" action="show" id="${surveyConfig.subscription.id}">
            ${surveyConfig.getConfigNameShort()}
        </g:link>

    </g:if>
    <g:else>
        ${surveyConfig.getConfigNameShort()}
    </g:else>
    : ${message(code: 'surveyInfo.evaluation')}
</h2>

<g:if test="${!(surveyInfo.status in [RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED])}">
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:if>
<g:else>

    <ui:messages data="${[message: message(code: 'renewalEvaluation.dynamicSite')]}"/>

    <ui:greySegment>

        %{--<h3 class="ui header">
        <g:message code="renewalEvaluation.parentSubscription"/>:
        <g:if test="${parentSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSubscription.id}">${parentSubscription.dropdownNamingConvention()}</g:link>
        </g:if>

        <br/>
        <br/>
        <g:message code="renewalEvaluation.parentSuccessorSubscription"/>:
        <g:if test="${parentSuccessorSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSuccessorSubscription.id}">${parentSuccessorSubscription.dropdownNamingConvention()}</g:link>

            <g:if test="${parentSuccessorSubscription.getAllSubscribers().size() > 0}">
                <g:link controller="subscription" action="copyElementsIntoSubscription" id="${parentSubscription.id}"
                        params="[sourceObjectId: genericOIDService.getOID(parentSubscription), targetObjectId: genericOIDService.getOID(parentSuccessorSubscription), isRenewSub: true, fromSurvey: true]"
                        class="ui button ">
                    <g:message code="renewalEvaluation.newSub.change"/>
                </g:link>
            </g:if>

        </g:if>
        <g:else>
            <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo.id}"
                    params="[surveyConfig: surveyConfig.id, parentSub: parentSubscription.id]"
                    class="ui button ">
                <g:message code="renewalEvaluation.newSub"/>
            </g:link>
        </g:else>
        <br />
        </h3>--}%

        <g:set var="countParticipants" value="${surveyConfig.countParticipants()}"/>
        <div class="ui horizontal segments">
            <div class="ui segment center aligned">
                <g:link controller="subscription" action="members" id="${subscription.id}">
                    <strong>${message(code: 'surveyconfig.subOrgs.label')}:</strong>

                    <div class="ui blue circular label">
                        ${countParticipants.subMembers}
                    </div>
                </g:link>
            </div>

            <div class="ui segment center aligned">
                <g:link controller="survey" action="surveyParticipants"
                        id="${surveyConfig.surveyInfo.id}"
                        params="[surveyConfigID: surveyConfig.id]">
                    <strong>${message(code: 'surveyconfig.orgs.label')}:</strong>

                    <div class="ui blue circular label">${countParticipants.surveyMembers}</div>
                </g:link>

                <g:if test="${countParticipants.subMembersWithMultiYear > 0}">
                    ( ${countParticipants.subMembersWithMultiYear}
                    ${message(code: 'surveyconfig.subOrgsWithMultiYear.label')} )
                </g:if>
            </div>

            <div class="ui segment center aligned">
                <strong>${message(code: 'renewalEvaluation.orgsTotalInRenewalProcess')}:</strong>
                <ui:totalNumber class="${totalOrgs != countParticipants.subMembers ? 'red' : ''}"
                                   total="${totalOrgs}"/>

            </div>
        </div>

    </ui:greySegment>

    <div class="la-inline-lists">
            <div class="ui card">
                <div class="content">
                    <h2 class="ui header">${message(code:'renewalEvaluation.propertiesChanged')}</h2>

                    <g:if test="${propertiesChanged}">
                        <g:link class="ui right floated button" controller="survey" action="showPropertiesChanged"
                                id="${surveyConfig.surveyInfo.id}"
                                params="[surveyConfigID: surveyConfig.id, tab: params.tab, exportXLSX: true]">
                            Export ${message(code: 'renewalEvaluation.propertiesChanged')}
                        </g:link>
                        <br>
                        <br>
                    </g:if>
                    <div>
                        <table class="ui la-js-responsive-table la-table table">
                            <thead>
                            <tr>
                                <th class="center aligned">${message(code: 'sidewide.number')}</th>
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
                                        <button class="ui button" onclick="JSPC.app.propertiesChanged(${property.key});">
                                            <g:message code="default.button.show.label"/>
                                        </button>
                                    </td>
                                </tr>

                            </g:each>
                            </tbody>
                        </table>

                    </div>
                </div>
            </div>
    </div>


    <ui:greySegment>

        <div class="ui top attached stackable tabular la-tab-with-js menu">
            <a class="active item" data-tab="orgsContinuetoSubscription">
                ${message(code: 'renewalEvaluation.continuetoSubscription.label')} <ui:totalNumber
                        total="${orgsContinuetoSubscription.size()}"/>
            </a>

            <a class="item" data-tab="newOrgsContinuetoSubscription">
                ${message(code: 'renewalEvaluation.newOrgstoSubscription.label')} <ui:totalNumber
                        total="${newOrgsContinuetoSubscription.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithMultiYearTermSub">
                ${message(code: 'renewalEvaluation.withMultiYearTermSub.label')} <ui:totalNumber
                        total="${orgsWithMultiYearTermSub.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithParticipationInParentSuccessor">
                ${message(code: 'renewalEvaluation.orgsWithParticipationInParentSuccessor.label')} <ui:totalNumber
                        total="${orgsWithParticipationInParentSuccessor.size()}"/>
            </a>

            <a class="item" data-tab="orgsWithTermination">
                ${message(code: 'renewalEvaluation.withTermination.label')} <ui:totalNumber
                        total="${orgsWithTermination.size()}"/>
            </a>

           <a class="item" data-tab="orgsWithoutResult">
                ${message(code: 'renewalEvaluation.orgsWithoutResult.label')} <ui:totalNumber
                        total="${orgsWithoutResult.size()}"/>
            </a>

            <a class="item" data-tab="orgInsertedItself">
                ${message(code: 'renewalEvaluation.orgInsertedItself.label')} <ui:totalNumber
                        total="${orgInsertedItself.size()}"/>
            </a>
        </div>

        <div class="ui bottom attached active tab segment" data-tab="orgsContinuetoSubscription">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.continuetoSubscription.label')} <ui:totalNumber
                    total="${orgsContinuetoSubscription.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgsContinuetoSubscription]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="newOrgsContinuetoSubscription">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.newOrgstoSubscription.label')} <ui:totalNumber
                    total="${newOrgsContinuetoSubscription.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: newOrgsContinuetoSubscription]"/>
        </div>

        <div class="ui bottom attached tab segment" data-tab="orgsWithTermination">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.withTermination.label')} <ui:totalNumber
                    total="${orgsWithTermination.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgsWithTermination]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="orgsWithoutResult">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgsWithoutResult.label')} (${message(code: 'surveys.tabs.termination')})<ui:totalNumber
                    total="${orgsWithoutResult.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgsWithoutResult]"/>
        </div>

        <div class="ui bottom attached tab segment" data-tab="orgInsertedItself">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgInsertedItself.label')}<ui:totalNumber
                    total="${orgInsertedItself.size()}"/></h4>

            <laser:render template="renewalResult" model="[participantResults: orgInsertedItself]"/>
        </div>


        <div class="ui bottom attached tab segment" data-tab="orgsWithMultiYearTermSub">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.withMultiYearTermSub.label')} <ui:totalNumber
                    total="${orgsWithMultiYearTermSub.size()}"/></h4>

            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'default.actions.label')}</th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${orgsWithMultiYearTermSub}" var="sub" status="i">
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                            <td>
                                ${subscriberOrg.sortname}
                                <br/>

                                <g:link controller="organisation" action="show"
                                        id="${subscriberOrg.id}">(${fieldValue(bean: subscriberOrg, field: "name")})</g:link>
                            </td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                            <td>${sub.status.getI10n('value')}</td>
                            <td>
                                <g:if test="${sub}">
                                    <g:link controller="subscription" action="show" id="${sub.id}"
                                            class="ui button icon"><i class="icon clipboard"></i></g:link>
                                </g:if>
                                <g:if test="${sub._getCalculatedPreviousForSurvey()}">
                                    <br/>
                                    <br/>
                                    <%-- TODO Moe --%>
                                    <g:link controller="subscription" action="show"
                                            id="${sub._getCalculatedPreviousForSurvey()?.id}"
                                            class="ui button icon"><i class="icon yellow clipboard"></i></g:link>
                                </g:if>
                            </td>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>

        <div class="ui bottom attached tab segment" data-tab="orgsWithParticipationInParentSuccessor">
            <h4 class="ui icon header la-clear-before la-noMargin-top">${message(code: 'renewalEvaluation.orgsWithParticipationInParentSuccessor.label')} <ui:totalNumber
                    total="${orgsWithParticipationInParentSuccessor.size() }"/></h4>

            <table class="ui celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th class="center aligned">${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'default.sortname.label')}</th>
                    <th>${message(code: 'default.startDate.label')}</th>
                    <th>${message(code: 'default.endDate.label')}</th>
                    <th>${message(code: 'default.status.label')}</th>
                    <th>${message(code: 'default.actions.label')}</th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${orgsWithParticipationInParentSuccessor}" var="sub" status="i">
                    <tr>
                        <td class="center aligned">
                            ${i + 1}
                        </td>
                        <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                            <td>
                                ${subscriberOrg.sortname}
                                <br/>
                                <g:link controller="organisation" action="show"
                                        id="${subscriberOrg.id}">(${fieldValue(bean: subscriberOrg, field: "name")})</g:link>
                            </td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                            <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                            <td>${sub.status.getI10n('value')}</td>
                            <td>
                                <g:if test="${sub}">
                                    <g:link controller="subscription" action="show" id="${sub.id}"
                                            class="ui button icon"><i class="icon clipboard"></i></g:link>
                                </g:if>
                                <g:if test="${sub._getCalculatedPreviousForSurvey()}">
                                    <br/>
                                    <br/>
                                    <%-- TODO Moe --%>
                                    <g:link controller="subscription" action="show"
                                            id="${sub._getCalculatedPreviousForSurvey()?.id}"
                                            class="ui button icon"><i class="icon yellow clipboard"></i></g:link>
                                </g:if>
                            </td>
                        </g:each>
                    </tr>
                </g:each>
                </tbody>
            </table>

        </div>

    </ui:greySegment>


    <g:form action="workflowRenewalSent" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

        <div class="ui right floated compact segment">
            <div class="ui checkbox">
                <input type="checkbox" onchange="this.form.submit()"
                       name="renewalSent" ${surveyInfo.isRenewalSent ? 'checked' : ''}>
                <label><g:message code="surveyInfo.isRenewalSent.label"/></label>
            </div>
        </div>

    </g:form>

    <laser:render template="export/individuallyExportRenewModal" model="[modalID: 'individuallyExportModal']" />


    <laser:script file="${this.getGroovyPageFileName()}">
        JSPC.app.propertiesChanged = function (propertyDefinitionId) {
            $.ajax({
                url: '<g:createLink controller="survey" action="showPropertiesChanged" params="[surveyConfigID: surveyConfig.id, id: surveyInfo.id]"/>&propertyDefinitionId='+propertyDefinitionId,
                success: function(result){
                    $("#dynamicModalContainer").empty();
                    $("#modalPropertiesChanged").remove();

                    $("#dynamicModalContainer").html(result);
                    $("#dynamicModalContainer .ui.modal").modal('show');
                }
            });
        }
    </laser:script>

</g:else>

<laser:htmlEnd />
