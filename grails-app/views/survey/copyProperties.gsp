<%@ page import="de.laser.RefdataValue; de.laser.helper.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.SurveyOrg; de.laser.AuditConfig" %>
<laser:serviceInjection/>

<g:set var="surveyService" bean="surveyService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} :  ${message(code: 'surveyInfo.copyProperties')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
            <semui:crumb controller="survey" action="renewalWithSurvey" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" message="surveyInfo.renewalOverView"/>
        </g:if>
    </g:if>
    <semui:crumb message="surveyInfo.transferOverView" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="survey" action="renewalWithSurvey"
                                       params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.renewalOverView"/>

            %{--<semui:actionsDropdownItem controller="survey" action="setCompleted"
                                       params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.completed.action"/>--}%

        </semui:actionsDropdown>
    </g:if>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

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

        <g:if test="${transferWorkflow && transferWorkflow.transferMembers == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferMembers: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
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

        <g:if test="${transferWorkflow && transferWorkflow.transferSurveyProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
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

        <g:if test="${transferWorkflow && transferWorkflow.transferCustomProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferCustomProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
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

        <g:if test="${transferWorkflow && transferWorkflow.transferPrivateProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferPrivateProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
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

        <g:if test="${transferWorkflow && transferWorkflow.transferSurveyCostItems == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyCostItems: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyCostItems: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

</div>

<semui:messages data="${flash}"/>

<h2 class="ui header">
    ${message(code: 'copyProperties.copyProperties', args: [message(code: 'copyProperties.' + params.tab)])}
</h2>

<semui:form>
    <div class="ui grid">

        <div class="row">
            <div class="eight wide column">
                <h3 class="ui header center aligned">

                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <g:message code="renewalWithSurvey.parentSubscription"/>:
                    </g:if><g:else>
                        <g:message code="copyElementsIntoObject.sourceObject.name" args="[message(code: 'subscription.label')]"/>:
                    </g:else><br />
                    <g:if test="${parentSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSubscription.id}">${parentSubscription.dropdownNamingConvention()}</g:link>
                        <br />
                        <g:link controller="subscription" action="members"
                                id="${parentSubscription.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSubscription.getDerivedSubscribers().size() ?: 0}"/>
                    </g:if>
                </h3>
            </div>

            <div class="eight wide column">
                <h3 class="ui header center aligned">
                    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <g:message code="renewalWithSurvey.parentSuccessorSubscription"/>:
                    </g:if><g:else>
                        <g:message code="copyElementsIntoObject.targetObject.name" args="[message(code: 'subscription.label')]"/>:
                    </g:else><br />
                    <g:if test="${parentSuccessorSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSuccessorSubscription.id}">${parentSuccessorSubscription.dropdownNamingConvention()}</g:link>
                        <br />
                        <g:link controller="subscription" action="members"
                                id="${parentSuccessorSubscription.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSuccessorSubscription.getDerivedSubscribers().size() ?: 0}"/>

                    </g:if>
                </h3>
            </div>
        </div>
    </div>
</semui:form>

%{--<div class="ui icon positive message">
    <i class="info icon"></i>

    <div class="content">
        <div class="header"></div>

        <p>
            <g:if test="${params.tab == 'surveyProperties'}">
                <g:message code="copyProperties.surveyProperties.info"/>
            </g:if>
            <g:if test="${params.tab == 'customProperties'}">
                <g:message code="copyProperties.customProperties.info"/>
            </g:if>
            <g:if test="${params.tab == 'privateProperties'}">
                <g:message code="copyProperties.privateProperties.info"/>
            </g:if>
        </p>
    </div>
</div>--}%

<semui:form>
    <g:if test="${properties}">

        <g:form action="proccessCopyProperties" controller="survey" id="${surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id, tab: params.tab, targetSubscriptionId: targetSubscription?.id]"
                method="post" class="ui form ">
            <g:hiddenField name="copyProperty" value="${selectedProperty}"/>

            <table class="ui celled sortable table la-table" id="parentSubscription">
                <thead>
                <tr>
                    <th>
                        <g:checkBox name="propertiesToggler" id="propertiesToggler" checked="false"/>
                    </th>
                    <th>${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>

                    <g:if test="${params.tab == 'surveyProperties'}">
                        <th> <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                            <g:message code="renewalWithSurvey.parentSubscription"/>
                        </g:if><g:else>
                            <g:message code="copyElementsIntoObject.sourceObject.name" args="[message(code: 'subscription.label')]"/>
                        </g:else></th>
                        <th>
                            <g:form id="selectedPropertyForm" action="copyProperties" method="post"
                                    params="${[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, targetSubscriptionId: targetSubscription?.id]}">
                                <laser:select name="selectedProperty"
                                              from="${properties}"
                                              optionKey="id"
                                              optionValue="name"
                                              value="${selectedProperty}"
                                              class="ui dropdown"
                                              onchange="this.form.submit()"/>
                            </g:form>
                        </th>
                    </g:if>
                    <g:else>
                        <th> <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                            <g:message code="renewalWithSurvey.parentSubscription"/>
                        </g:if><g:else>
                            <g:message code="copyElementsIntoObject.sourceObject.name" args="[message(code: 'subscription.label')]"/>
                        </g:else>
                            <g:form action="copyProperties" method="post"
                                    params="${[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, targetSubscriptionId: targetSubscription?.id]}">
                                <laser:select name="selectedProperty"
                                              from="${properties.sort { it.getI10n('name') }}"
                                              optionKey="id"
                                              optionValue="name"
                                              value="${selectedProperty}"
                                              class="ui dropdown"
                                              onchange="this.form.submit()"/>
                            </g:form>
                        </th>
                    </g:else>
                    <th><g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        <g:message code="renewalWithSurvey.parentSuccessorSubscription"/>
                    </g:if><g:else>
                        <g:message code="copyElementsIntoObject.targetObject.name" args="[message(code: 'subscription.label')]"/>
                    </g:else></th>
                    <th></th>
                </tr>
                </thead>
                <tbody>

                <g:each in="${participantsList}" var="participant" status="i">

                    <tr class="">
                        <td>
                            <g:if test="${!participant.newCustomProperty && !participant.newPrivateProperty}">
                                <g:checkBox name="selectedSub" value="${participant.newSub.id}" checked="false"/>
                            </g:if>
                        </td>
                        <td>${i + 1}</td>
                        <td class="titleCell">
                            <g:if test="${participant.newSub.isMultiYear}">
                                <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                      data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                    <i class="map orange icon"></i>
                                </span>
                            </g:if>
                            <g:link controller="myInstitution" action="manageParticipantSurveys"
                                    id="${participant.id}">
                                ${participant.sortname}
                            </g:link>
                            <br />
                            <g:link controller="organisation" action="show"
                                    id="${participant.id}">(${participant.name})</g:link>
                            <g:if test="${participant.newSub}">
                                <div class="la-icon-list">
                                    <g:formatDate formatName="default.date.format.notime"
                                                  date="${participant.newSub.startDate}"/>
                                    -
                                    <g:formatDate formatName="default.date.format.notime"
                                                  date="${participant.newSub.endDate}"/>
                                    <div class="right aligned wide column">
                                        <strong>${participant.newSub.status.getI10n('value')}</strong>
                                    </div>
                                </div>
                            </g:if>

                        </td>

                        <td>
                            <g:if test="${params.tab in ['surveyProperties', 'customProperties']}">
                                <g:if test="${!participant.oldSub}">
                                    ${message(code: 'copyProperties.copyProperties.noSubscription')}
                                </g:if>
                                <g:elseif test="${participant.oldSub && participant.oldCustomProperty}">

                                    <g:if test="${participant.oldCustomProperty.type.isIntegerType()}">
                                        <semui:xEditable owner="${participant.oldCustomProperty}" type="number"
                                                         overwriteEditable="${false}"
                                                         field="intValue"/>
                                    </g:if>
                                    <g:elseif test="${participant.oldCustomProperty.type.isStringType()}">
                                        <semui:xEditable owner="${participant.oldCustomProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="stringValue"/>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.oldCustomProperty.type.isBigDecimalType()}">
                                        <semui:xEditable owner="${participant.oldCustomProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="decValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.oldCustomProperty.type.isDateType()}">
                                        <semui:xEditable owner="${participant.oldCustomProperty}" type="date"
                                                         overwriteEditable="${false}"
                                                         field="dateValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.oldCustomProperty.type.isURLType()}">
                                        <semui:xEditable owner="${participant.oldCustomProperty}" type="url"
                                                         field="urlValue"
                                                         overwriteEditable="${false}"

                                                         class="la-overflow la-ellipsis"/>
                                        <g:if test="${participant.oldCustomProperty.value}">
                                            <semui:linkIcon href="${participant.oldCustomProperty.value}"/>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.oldCustomProperty.type.isRefdataValueType()}">
                                        <semui:xEditableRefData owner="${participant.oldCustomProperty}" type="text"
                                                                overwriteEditable="${false}"
                                                                field="refValue"
                                                                config="${participant.oldCustomProperty.type.refdataCategory}"/>
                                    </g:elseif>

                                    <g:if test="${participant.oldCustomProperty.hasProperty('instanceOf') && participant.oldCustomProperty.instanceOf && AuditConfig.getConfig(participant.oldCustomProperty.instanceOf)}">
                                        <g:if test="${participant.oldSub.isSlaved}">
                                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="large icon thumbtack blue"></i></span>
                                        </g:if>
                                        <g:else>
                                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit')}" data-position="top right"><i class="large icon thumbtack grey"></i></span>
                                        </g:else>
                                    </g:if>


                                </g:elseif><g:else>

                                ${message(code: 'subscription.propertiesMembers.noCustomProperty')}

                            </g:else>
                            </g:if>

                            <g:if test="${params.tab == 'privateProperties'}">
                                <g:if test="${!participant.oldSub}">
                                    ${message(code: 'copyProperties.copyProperties.noSubscription')}
                                </g:if>
                                <g:elseif test="${participant.oldSub && participant.oldPrivateProperty}">

                                    <g:if test="${participant.oldPrivateProperty.type.isIntegerType()}">
                                        <semui:xEditable owner="${participant.oldPrivateProperty}" type="number"
                                                         overwriteEditable="${false}"
                                                         field="intValue"/>
                                    </g:if>
                                    <g:elseif test="${participant.oldPrivateProperty.type.isStringType()}">
                                        <semui:xEditable owner="${participant.oldPrivateProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="stringValue"/>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.oldPrivateProperty.type.isBigDecimalType()}">
                                        <semui:xEditable owner="${participant.oldPrivateProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="decValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.oldPrivateProperty.type.isDateType()}">
                                        <semui:xEditable owner="${participant.oldPrivateProperty}" type="date"
                                                         overwriteEditable="${false}"
                                                         field="dateValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.oldPrivateProperty.type.isURLType()}">
                                        <semui:xEditable owner="${participant.oldPrivateProperty}" type="url"
                                                         field="urlValue"
                                                         overwriteEditable="${false}"

                                                         class="la-overflow la-ellipsis"/>
                                        <g:if test="${participant.oldPrivateProperty.value}">
                                            <semui:linkIcon href="${participant.oldPrivateProperty.value}"/>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.oldPrivateProperty.type.isRefdataValueType()}">
                                        <semui:xEditableRefData owner="${participant.oldPrivateProperty}" type="text"
                                                                overwriteEditable="${false}"
                                                                field="refValue"
                                                                config="${participant.oldPrivateProperty.type.refdataCategory}"/>
                                    </g:elseif>

                                </g:elseif><g:else>

                                ${message(code: 'subscription.propertiesMembers.noPrivateProperty')}

                            </g:else>
                            </g:if>

                        </td>

                        <g:if test="${params.tab == 'surveyProperties'}">
                            <td class="center aligned">
                                <g:if test="${participant.surveyProperty}">

                                    <g:if test="${participant.surveyProperty.type.isIntegerType()}">
                                        <semui:xEditable owner="${participant.surveyProperty}" type="number"
                                                         overwriteEditable="${false}"
                                                         field="intValue"/>
                                    </g:if>
                                    <g:elseif test="${participant.surveyProperty.type.isStringType()}">
                                        <semui:xEditable owner="${participant.surveyProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="stringValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.surveyProperty.type.isBigDecimalType()}">
                                        <semui:xEditable owner="${participant.surveyProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="decValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.surveyProperty.type.isDateType()}">
                                        <semui:xEditable owner="${participant.surveyProperty}" type="date"
                                                         overwriteEditable="${false}"
                                                         field="dateValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.surveyProperty.type.isURLType()}">
                                        <semui:xEditable owner="${participant.surveyProperty}" type="url"
                                                         field="urlValue"
                                                         overwriteEditable="${false}"

                                                         class="la-overflow la-ellipsis"/>
                                        <g:if test="${participant.surveyProperty.value}">
                                            <semui:linkIcon href="${participant.surveyProperty.value}"/>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif test="${participant.surveyProperty.type.isRefdataValueType()}">
                                        <semui:xEditableRefData owner="${participant.surveyProperty}" type="text"
                                                                overwriteEditable="${false}"
                                                                field="refValue"
                                                                config="${participant.surveyProperty.type.refdataCategory}"/>
                                    </g:elseif>

                                </g:if><g:else>

                                    ${message(code: 'copyProperties.surveyProperties.noProperty')}

                                </g:else>
                            </td>
                        </g:if>
                        <td>
                            <g:if test="${params.tab in ['surveyProperties', 'customProperties']}">
                                <g:if test="${participant.newCustomProperty}">

                                    <g:if test="${participant.newCustomProperty.type.isIntegerType()}">
                                        <semui:xEditable owner="${participant.newCustomProperty}" type="number"
                                                         overwriteEditable="${false}"
                                                         field="intValue"/>
                                    </g:if>
                                    <g:elseif test="${participant.newCustomProperty.type.isStringType()}">
                                        <semui:xEditable owner="${participant.newCustomProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="stringValue"/>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.newCustomProperty.type.isBigDecimalType()}">
                                        <semui:xEditable owner="${participant.newCustomProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="decValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.newCustomProperty.type.isDateType()}">
                                        <semui:xEditable owner="${participant.newCustomProperty}" type="date"
                                                         overwriteEditable="${false}"
                                                         field="dateValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.newCustomProperty.type.isURLType()}">
                                        <semui:xEditable owner="${participant.newCustomProperty}" type="url"
                                                         field="urlValue"
                                                         overwriteEditable="${false}"

                                                         class="la-overflow la-ellipsis"/>
                                        <g:if test="${participant.newCustomProperty.value}">
                                            <semui:linkIcon href="${participant.newCustomProperty.value}"/>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.newCustomProperty.type.isRefdataValueType()}">
                                        <semui:xEditableRefData owner="${participant.newCustomProperty}" type="text"
                                                                overwriteEditable="${false}"
                                                                field="refValue"
                                                                config="${participant.newCustomProperty.type.refdataCategory}"/>
                                    </g:elseif>

                                    <g:if test="${participant.newCustomProperty.hasProperty('instanceOf') && participant.newCustomProperty.instanceOf && AuditConfig.getConfig(participant.newCustomProperty.instanceOf)}">
                                        <g:if test="${participant.newSub.isSlaved}">
                                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit.auto')}" data-position="top right"><i class="large icon thumbtack blue"></i></span>
                                        </g:if>
                                        <g:else>
                                            <span class="la-popup-tooltip la-delay" data-content="${message(code:'property.audit.target.inherit')}" data-position="top right"><i class="large icon thumbtack grey"></i></span>
                                        </g:else>
                                    </g:if>

                                </g:if><g:else>

                                ${message(code: 'subscription.propertiesMembers.noCustomProperty')}

                            </g:else>
                            </g:if>

                            <g:if test="${params.tab == 'privateProperties'}">
                                <g:if test="${participant.newPrivateProperty}">

                                    <g:if test="${participant.newPrivateProperty.type.isIntegerType()}">
                                        <semui:xEditable owner="${participant.newPrivateProperty}" type="number"
                                                         overwriteEditable="${false}"
                                                         field="intValue"/>
                                    </g:if>
                                    <g:elseif test="${participant.newPrivateProperty.type.isStringType()}">
                                        <semui:xEditable owner="${participant.newPrivateProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="stringValue"/>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.newPrivateProperty.type.isBigDecimalType()}">
                                        <semui:xEditable owner="${participant.newPrivateProperty}" type="text"
                                                         overwriteEditable="${false}"
                                                         field="decValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.newPrivateProperty.type.isDateType()}">
                                        <semui:xEditable owner="${participant.newPrivateProperty}" type="date"
                                                         overwriteEditable="${false}"
                                                         field="dateValue"/>
                                    </g:elseif>
                                    <g:elseif test="${participant.newPrivateProperty.type.isURLType()}">
                                        <semui:xEditable owner="${participant.newPrivateProperty}" type="url"
                                                         field="urlValue"
                                                         overwriteEditable="${false}"

                                                         class="la-overflow la-ellipsis"/>
                                        <g:if test="${participant.newPrivateProperty.value}">
                                            <semui:linkIcon href="${participant.newPrivateProperty.value}"/>
                                        </g:if>
                                    </g:elseif>
                                    <g:elseif
                                            test="${participant.newPrivateProperty.type.isRefdataValueType()}">
                                        <semui:xEditableRefData owner="${participant.newPrivateProperty}" type="text"
                                                                overwriteEditable="${false}"
                                                                field="refValue"
                                                                config="${participant.newPrivateProperty.type.refdataCategory}"/>
                                    </g:elseif>

                                </g:if><g:else>

                                ${message(code: 'subscription.propertiesMembers.noPrivateProperty')}

                            </g:else>
                            </g:if>

                        </td>

                        <td>
                            <g:if test="${participant.newSub}">
                                <g:link controller="subscription" action="show" id="${participant.newSub.id}"
                                        class="ui button icon"><i class="icon clipboard"></i></g:link>
                            </g:if>
                        </td>
                    </tr>
                </g:each>

                </tbody>
            </table>

            <div class="two fields">
                <div class="eight wide field" style="text-align: left;">
                </div>

                <div class="eight wide field" style="text-align: right;">
                    <button class="ui button positive"
                            type="submit">${message(code: 'copyProperties.copyProperties', args: [message(code: 'copyProperties.' + params.tab)])}</button>
                </div>
            </div>
        </g:form>
    </g:if>
    <g:else>
        <g:message code="copyProperties.noCopyProperties" args="[message(code: 'copyProperties.' + params.tab)]"/>
    </g:else>
</semui:form>

<div class="sixteen wide field" style="text-align: center;">
<g:if test="${params.tab != 'privateProperties'}">
    <g:link class="ui button" controller="survey" action="copyProperties"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: ((params.tab == 'customProperties') ? 'privateProperties' : ((params.tab == 'surveyProperties') ? 'customProperties' : 'surveyProperties')), targetSubscriptionId: targetSubscription?.id]">
        ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
    </g:link>
</g:if>

<g:if test="${params.tab == 'privateProperties'}">
    <g:link class="ui button" controller="survey" action="copySurveyCostItems"
            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]">
        ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
    </g:link>
</g:if>
</div>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#propertiesToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSub]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSub]").prop('checked', false)
        }
    })
</laser:script>
</body>
</html>
