<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.PropertyStore; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg; de.laser.AuditConfig" %>
<laser:htmlStart message="surveyInfo.copyProperties" serviceInjection="true" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.transferOverView" class="active"/>
</ui:breadcrumbs>


<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey">
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<g:if test="${surveyConfig.subscription}">
    <ui:linkWithIcon icon="${Icon.SUBSCRIPTION} bordered inverted orange la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<ui:messages data="${flash}"/>

<br/>

<g:if test="${surveyConfig.subSurveyUseForTransfer && !(surveyInfo.status in [RDStore.SURVEY_IN_EVALUATION, RDStore.SURVEY_COMPLETED])}">
    <div class="ui segment">
        <strong>${message(code: 'renewalEvaluation.notInEvaliation')}</strong>
    </div>
</g:if>
<g:else>

    <g:render template="multiYearsSubs"/>

    <g:render template="navCompareMembers"/>

    <ui:messages data="${flash}"/>

    <h2 class="ui header">
        ${message(code: 'copyProperties.copyProperties', args: [message(code: 'copyProperties.' + params.tab)])}
    </h2>

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
                    </h3>
                </div>
            </div>
        </div>
    </ui:greySegment>

%{--<div class="ui icon positive message">
    <i class="${Icon.UI.INFO}"></i>

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

    <g:if test="${properties}">
        <div class="ui segment">
            <h3>
                <g:message code="propertyDefinition.plural"/>
            </h3>
            <table class="ui sortable celled la-js-responsive-table la-table table">
                <thead>
                <tr>
                    <th>${message(code: 'sidewide.number')}</th>
                    <th>${message(code: 'propertyDefinition.label')}</th>
                    <g:if test="${params.tab == 'surveyProperties'}">
                        <th><g:if test="${surveyConfig.subSurveyUseForTransfer}">
                            ${message(code: 'default.count.label')} <g:message code="renewalEvaluation.parentSubscription"/>
                        </g:if><g:else>
                            ${message(code: 'default.count.label')} <g:message code="copyElementsIntoObject.sourceObject.name"
                                                                               args="[message(code: 'subscription.label')]"/>
                        </g:else></th>
                        <th>${message(code: 'default.count.label')}</th>
                    </g:if>
                    <g:else>
                        <th><g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        ${message(code: 'default.count.label')} <g:message code="renewalEvaluation.parentSubscription"/>
                    </g:if><g:else>
                        ${message(code: 'default.count.label')} <g:message code="copyElementsIntoObject.sourceObject.name"
                                                                           args="[message(code: 'subscription.label')]"/>
                    </g:else>
                        ${message(code: 'default.count.label')}
                    </g:else>
                    <th><g:if test="${surveyConfig.subSurveyUseForTransfer}">
                        ${message(code: 'default.count.label')} <g:message code="renewalEvaluation.parentSuccessorSubscription"/>
                    </g:if><g:else>
                        ${message(code: 'default.count.label')} <g:message code="copyElementsIntoObject.targetObject.name"
                                                                           args="[message(code: 'subscription.label')]"/>
                    </g:else></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${properties.sort { it.getI10n('name') }}" var="property" status="i">

                    <tr>
                        <td>${i + 1}</td>
                        <td><g:link controller="survey" action=" $actionName"
                                    params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID, selectedProperty: property.id]}">${property.getI10n('name')}</g:link></td>
                        <td>${subscriptionService.countCustomSubscriptionPropertyOfMembersByParentSub(contextOrg, parentSubscription, property)}</td>
                        <g:if test="${params.tab == 'surveyProperties'}">
                            <td>${participantsList ? surveyService.countSurveyPropertyWithValueByMembers(surveyConfig, property, participantsList.org) : 0}</td>
                        </g:if>
                        <td>${subscriptionService.countCustomSubscriptionPropertyOfMembersByParentSub(contextOrg, parentSuccessorSubscription, property)}</td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:if>


    <ui:greySegment>
        <g:if test="${properties}">

            <g:form action="proccessCopyProperties" controller="survey" id="${surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id, tab: params.tab, targetSubscriptionId: targetSubscription?.id]"
                    method="post" class="ui form ">
                <g:hiddenField name="copyProperty" value="${selectedProperty}"/>

                <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
                    <thead>
                    <tr>
                        <th>
                            <g:checkBox name="propertiesToggler" id="propertiesToggler" checked="false"/>
                        </th>
                        <th>${message(code: 'sidewide.number')}</th>
                        <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>

                        <g:if test="${params.tab == 'surveyProperties'}">
                            <th><g:if test="${surveyConfig.subSurveyUseForTransfer}">
                                <g:message code="renewalEvaluation.parentSubscription"/>
                            </g:if><g:else>
                                <g:message code="copyElementsIntoObject.sourceObject.name"
                                           args="[message(code: 'subscription.label')]"/>
                            </g:else></th>
                            <th>
                                <g:form id="selectedPropertyForm" action="copyProperties" method="post"
                                        params="${[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, targetSubscriptionId: targetSubscription?.id]}">
                                    <ui:select name="selectedProperty"
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
                            <th><g:if test="${surveyConfig.subSurveyUseForTransfer}">
                                <g:message code="renewalEvaluation.parentSubscription"/>
                            </g:if><g:else>
                                <g:message code="copyElementsIntoObject.sourceObject.name"
                                           args="[message(code: 'subscription.label')]"/>
                            </g:else>
                            <g:form action="copyProperties" method="post"
                                    params="${[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: params.tab, targetSubscriptionId: targetSubscription?.id]}">
                                <ui:select name="selectedProperty"
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
                            <g:message code="renewalEvaluation.parentSuccessorSubscription"/>
                        </g:if><g:else>
                            <g:message code="copyElementsIntoObject.targetObject.name"
                                       args="[message(code: 'subscription.label')]"/>
                        </g:else></th>
                        <th></th>
                    </tr>
                    </thead>
                    <tbody>

                    <g:each in="${participantsList}" var="participant" status="i">

                        <tr class="">
                            <td>
                                <g:if test="${params.tab == 'customProperties' && (participant.oldSub && ((!participant.newCustomProperty && participant.oldCustomProperty) || (participant.newCustomProperty && participant.oldCustomProperty && participant.oldCustomProperty.type.multipleOccurrence)))}">
                                    <g:checkBox name="selectedSub" value="${participant.newSub.id}" checked="false"/>
                                </g:if>
                                <g:elseif
                                        test="${params.tab == 'privateProperties' && (participant.oldSub && ((!participant.newPrivateProperty && participant.oldPrivateProperty) || (participant.newPrivateProperty && participant.oldPrivateProperty && participant.oldPrivateProperty.type.multipleOccurrence)))}">
                                    <g:checkBox name="selectedSub" value="${participant.newSub.id}" checked="false"/>
                                </g:elseif>
                                <g:elseif
                                        test="${params.tab == 'surveyProperties' && ((participant.surveyProperty && participant.surveyProperty.getValue() && (!participant.newCustomProperty && participant.surveyProperty) || (participant.newCustomProperty && participant.newCustomProperty.type.multipleOccurrence)))}">
                                    <g:checkBox name="selectedSub" value="${participant.newSub.id}" checked="false"/>
                                </g:elseif>
                            </td>
                            <td>${i + 1}</td>
                            <td class="titleCell">
                                <g:if test="${participant.newSub.isMultiYear}">
                                    <ui:multiYearIcon isConsortial="true" color="orange" />
                                </g:if>
                                <g:link controller="myInstitution" action="manageParticipantSurveys"
                                        id="${participant.id}">
                                    ${participant.sortname}
                                </g:link>
                                <br/>
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
                                            <ui:xEditable owner="${participant.oldCustomProperty}" type="number"
                                                             overwriteEditable="${false}"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${participant.oldCustomProperty.type.isStringType()}">
                                            <ui:xEditable owner="${participant.oldCustomProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.oldCustomProperty.type.isBigDecimalType()}">
                                            <ui:xEditable owner="${participant.oldCustomProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.oldCustomProperty.type.isDateType()}">
                                            <ui:xEditable owner="${participant.oldCustomProperty}" type="date"
                                                             overwriteEditable="${false}"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.oldCustomProperty.type.isURLType()}">
                                            <ui:xEditable owner="${participant.oldCustomProperty}" type="url"
                                                             field="urlValue"
                                                             overwriteEditable="${false}"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${participant.oldCustomProperty.value}">
                                                <ui:linkWithIcon href="${participant.oldCustomProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.oldCustomProperty.type.isRefdataValueType()}">
                                            <ui:xEditableRefData owner="${participant.oldCustomProperty}" type="text"
                                                                    overwriteEditable="${false}"
                                                                    field="refValue"
                                                                    config="${participant.oldCustomProperty.type.refdataCategory}"/>
                                        </g:elseif>

                                        <g:if test="${participant.oldCustomProperty.hasProperty('instanceOf') && participant.oldCustomProperty.instanceOf && AuditConfig.getConfig(participant.oldCustomProperty.instanceOf)}">
                                            <g:if test="${participant.oldSub.isSlaved}">
                                                <ui:auditIcon type="auto" />
                                            </g:if>
                                            <g:else>
                                                <ui:auditIcon type="default" />
                                            </g:else>
                                        </g:if>

                                    </g:elseif><g:else>

                                    ${message(code: 'subscriptionsManagement.noCustomProperty')}

                                </g:else>
                                </g:if>

                                <g:if test="${params.tab == 'privateProperties'}">
                                    <g:if test="${!participant.oldSub}">
                                        ${message(code: 'copyProperties.copyProperties.noSubscription')}
                                    </g:if>
                                    <g:elseif test="${participant.oldSub && participant.oldPrivateProperty}">

                                        <g:if test="${participant.oldPrivateProperty.type.isIntegerType()}">
                                            <ui:xEditable owner="${participant.oldPrivateProperty}" type="number"
                                                             overwriteEditable="${false}"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${participant.oldPrivateProperty.type.isStringType()}">
                                            <ui:xEditable owner="${participant.oldPrivateProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.oldPrivateProperty.type.isBigDecimalType()}">
                                            <ui:xEditable owner="${participant.oldPrivateProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.oldPrivateProperty.type.isDateType()}">
                                            <ui:xEditable owner="${participant.oldPrivateProperty}" type="date"
                                                             overwriteEditable="${false}"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.oldPrivateProperty.type.isURLType()}">
                                            <ui:xEditable owner="${participant.oldPrivateProperty}" type="url"
                                                             field="urlValue"
                                                             overwriteEditable="${false}"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${participant.oldPrivateProperty.value}">
                                                <ui:linkWithIcon href="${participant.oldPrivateProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.oldPrivateProperty.type.isRefdataValueType()}">
                                            <ui:xEditableRefData owner="${participant.oldPrivateProperty}"
                                                                    type="text"
                                                                    overwriteEditable="${false}"
                                                                    field="refValue"
                                                                    config="${participant.oldPrivateProperty.type.refdataCategory}"/>
                                        </g:elseif>

                                    </g:elseif><g:else>

                                    ${message(code: 'subscriptionsManagement.noPrivateProperty')}

                                </g:else>
                                </g:if>

                            </td>

                            <g:if test="${params.tab == 'surveyProperties'}">
                                <td class="center aligned">
                                    <g:if test="${participant.surveyProperty && participant.surveyProperty.getValue()}">

                                        <g:if test="${participant.surveyProperty.type.isIntegerType()}">
                                            <ui:xEditable owner="${participant.surveyProperty}" type="number"
                                                             overwriteEditable="${false}"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${participant.surveyProperty.type.isStringType()}">
                                            <ui:xEditable owner="${participant.surveyProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.surveyProperty.type.isBigDecimalType()}">
                                            <ui:xEditable owner="${participant.surveyProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.surveyProperty.type.isDateType()}">
                                            <ui:xEditable owner="${participant.surveyProperty}" type="date"
                                                             overwriteEditable="${false}"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.surveyProperty.type.isURLType()}">
                                            <ui:xEditable owner="${participant.surveyProperty}" type="url"
                                                             field="urlValue"
                                                             overwriteEditable="${false}"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${participant.surveyProperty.value}">
                                                <ui:linkWithIcon href="${participant.surveyProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif test="${participant.surveyProperty.type.isRefdataValueType()}">
                                            <ui:xEditableRefData owner="${participant.surveyProperty}" type="text"
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
                                            <ui:xEditable owner="${participant.newCustomProperty}" type="number"
                                                             overwriteEditable="${false}"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${participant.newCustomProperty.type.isStringType()}">
                                            <ui:xEditable owner="${participant.newCustomProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.newCustomProperty.type.isBigDecimalType()}">
                                            <ui:xEditable owner="${participant.newCustomProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.newCustomProperty.type.isDateType()}">
                                            <ui:xEditable owner="${participant.newCustomProperty}" type="date"
                                                             overwriteEditable="${false}"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.newCustomProperty.type.isURLType()}">
                                            <ui:xEditable owner="${participant.newCustomProperty}" type="url"
                                                             field="urlValue"
                                                             overwriteEditable="${false}"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${participant.newCustomProperty.value}">
                                                <ui:linkWithIcon href="${participant.newCustomProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.newCustomProperty.type.isRefdataValueType()}">
                                            <ui:xEditableRefData owner="${participant.newCustomProperty}" type="text"
                                                                    overwriteEditable="${false}"
                                                                    field="refValue"
                                                                    config="${participant.newCustomProperty.type.refdataCategory}"/>
                                        </g:elseif>

                                        <g:if test="${participant.newCustomProperty.hasProperty('instanceOf') && participant.newCustomProperty.instanceOf && AuditConfig.getConfig(participant.newCustomProperty.instanceOf)}">
                                            <g:if test="${participant.newSub.isSlaved}">
                                                <ui:auditIcon type="auto" />
                                            </g:if>
                                            <g:else>
                                                <ui:auditIcon type="default" />
                                            </g:else>
                                        </g:if>

                                    </g:if><g:else>

                                    ${message(code: 'subscriptionsManagement.noCustomProperty')}

                                </g:else>
                                </g:if>

                                <g:if test="${params.tab == 'privateProperties'}">
                                    <g:if test="${participant.newPrivateProperty}">

                                        <g:if test="${participant.newPrivateProperty.type.isIntegerType()}">
                                            <ui:xEditable owner="${participant.newPrivateProperty}" type="number"
                                                             overwriteEditable="${false}"
                                                             field="intValue"/>
                                        </g:if>
                                        <g:elseif test="${participant.newPrivateProperty.type.isStringType()}">
                                            <ui:xEditable owner="${participant.newPrivateProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="stringValue"/>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.newPrivateProperty.type.isBigDecimalType()}">
                                            <ui:xEditable owner="${participant.newPrivateProperty}" type="text"
                                                             overwriteEditable="${false}"
                                                             field="decValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.newPrivateProperty.type.isDateType()}">
                                            <ui:xEditable owner="${participant.newPrivateProperty}" type="date"
                                                             overwriteEditable="${false}"
                                                             field="dateValue"/>
                                        </g:elseif>
                                        <g:elseif test="${participant.newPrivateProperty.type.isURLType()}">
                                            <ui:xEditable owner="${participant.newPrivateProperty}" type="url"
                                                             field="urlValue"
                                                             overwriteEditable="${false}"

                                                             class="la-overflow la-ellipsis"/>
                                            <g:if test="${participant.newPrivateProperty.value}">
                                                <ui:linkWithIcon href="${participant.newPrivateProperty.value}"/>
                                            </g:if>
                                        </g:elseif>
                                        <g:elseif
                                                test="${participant.newPrivateProperty.type.isRefdataValueType()}">
                                            <ui:xEditableRefData owner="${participant.newPrivateProperty}"
                                                                    type="text"
                                                                    overwriteEditable="${false}"
                                                                    field="refValue"
                                                                    config="${participant.newPrivateProperty.type.refdataCategory}"/>
                                        </g:elseif>

                                    </g:if><g:else>

                                    ${message(code: 'subscriptionsManagement.noPrivateProperty')}

                                </g:else>
                                </g:if>

                            </td>

                            <td>
                                <g:if test="${participant.newSub}">
                                    <g:link controller="subscription" action="show" id="${participant.newSub.id}"
                                            class="${Btn.ICON.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                                </g:if>

                                <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                                    <g:set var="multiYearResultProperties"
                                           value="${surveyService.getMultiYearResultProperties(surveyConfig, Org.get(participant.id))}"/>
                                    <g:if test="${multiYearResultProperties.size () > 0}">
                                        <br>
                                        <br>

                                        <div class="ui icon"
                                             data-tooltip="${message(code: 'surveyProperty.label') + ': ' + multiYearResultProperties.collect { it.getI10n('name') }.join(', ') + ' = ' + message(code: 'refdata.Yes')}">
                                            <i class="bordered colored info icon"></i>
                                        </div>
                                    </g:if>
                                </g:if>

                                <g:if test="${SurveyOrg.findByOrgAndSurveyConfig(Org.get(participant.id), surveyConfig)}">
                                    <br>
                                    <br>

                                    <div class="ui icon"
                                         data-tooltip="${message(code: 'surveyParticipants.selectedParticipants')}">
                                        <i class="${Icon.SURVEY} bordered colored"></i>
                                    </div>
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
                        <g:if test="${params.tab == 'surveyProperties' && surveyConfig.subSurveyUseForTransfer && selectedProperty &&
                                (PropertyDefinition.get(selectedProperty) == PropertyStore.SURVEY_PROPERTY_PUBLISHING_COMPONENT || PropertyDefinition.get(selectedProperty) == PropertyStore.SURVEY_PROPERTY_SUBSCRIPTION_FORM)}">
                            <div class="ui large toggle checkbox">
                                <input type="checkbox" name="copyToSubAttribut">
                                <label>${message(code: 'copyProperties.surveyProperties.copyToSubAttr')}</label>
                            </div>

                            <br>
                            <br>
                        </g:if>

                        <button class="${Btn.POSITIVE}"
                                type="submit">${message(code: 'copyProperties.copyProperties', args: [message(code: 'copyProperties.' + params.tab)])}</button>
                    </div>
                </div>
            </g:form>
        </g:if>
        <g:else>
            <g:message code="copyProperties.noCopyProperties" args="[message(code: 'copyProperties.' + params.tab)]"/>
        </g:else>
    </ui:greySegment>

    <div class="sixteen wide field" style="text-align: center;">
        <g:if test="${params.tab != 'privateProperties'}">
            <g:link class="${Btn.SIMPLE}" controller="survey" action="copyProperties"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: ((params.tab == 'customProperties') ? 'privateProperties' : ((params.tab == 'surveyProperties') ? 'customProperties' : 'surveyProperties')), targetSubscriptionId: targetSubscription?.id]">
                ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
            </g:link>
        </g:if>

        <g:if test="${params.tab == 'privateProperties'}">
            <g:link class="${Btn.SIMPLE}" controller="survey" action="copySurveyCostItems"
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

</g:else>

<laser:htmlEnd />
