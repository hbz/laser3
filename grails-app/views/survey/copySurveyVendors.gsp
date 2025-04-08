<%@ page import="de.laser.wekb.VendorRole; de.laser.ui.Btn; de.laser.ui.Icon; de.laser.AuditConfig; de.laser.storage.RDConstants; de.laser.SubscriptionPackage; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="copySurveyVendors.transfer" />

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
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<laser:render template="nav"/>

<ui:objectStatus object="${surveyInfo}" />

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

    <h2 class="ui header">
        ${message(code: 'copySurveyVendors.transfer')}
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

    <ui:greySegment>

        <g:form action="proccessCopySurveyVendors" controller="survey" id="${surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]"
                method="post" class="ui form">


            <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
                <thead>
                <tr>
                    <g:if test="${editable}">
                        <th class="center aligned" rowspan="2">
                            <g:checkBox name="membersListToggler" id="membersListToggler" checked="false"/>
                        </th>
                    </g:if>
                    <th rowspan="2">${message(code: 'sidewide.number')}</th>
                    <th rowspan="2">${message(code: 'subscription.details.consortiaMembers.label')}</th>
                    <th><g:message code="renewalEvaluation.parentSubscription"/></th>
                    <th rowspan="2"><g:message code="copySurveyVendors.label"/></th>
                    <th><g:message code="renewalEvaluation.parentSuccessorSubscription"/></th>
                    <th rowspan="2"></th>
                </tr>
                <tr>
                    <th><g:message code="vendor.plural"/></th>
                    <th><g:message code="vendor.plural"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${participantsList}" var="participant" status="i">

                    <tr class="">
                        <g:if test="${editable}">
                            <td>
                            <g:if test="${editable && participant.surveyVendors && !VendorRole.findByVendorInListAndSubscription(participant.surveyVendors, participant.newSub)}">
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${participant.newSub.id}" name="selectedSubs" value="${participant.newSub.id}"
                                                    checked="false"/>
                                    </div>
                                </div>
                            </g:if>
                            </td>
                        </g:if>
                        <td>${i + 1}</td>
                        <td class="titleCell">
                            <g:if test="${participant.newSub && participant.newSub.isMultiYear}">
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
                            <div class="ui middle aligned selection list">
                                <g:each in="${participant.oldSub?.getVendors()}" var="vendor">
                                    <div class="item"><div class="content">
                                        <g:link controller="vendor" action="show" id="${vendor.id}">
                                            ${vendor.name}
                                        </g:link>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <div class="ui middle aligned selection list">
                                <g:each in="${participant.surveyVendors.sort{it.name}}" var="vendor">
                                    <div class="item">
                                        <div class="content">
                                            <g:link controller="vendor" action="show" id="${vendor.id}">
                                                ${vendor.name}
                                            </g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <div class="ui middle aligned selection list">
                                <g:each in="${participant.newSub.getVendors()}" var="vendor">
                                    <div class="item"><div class="content">
                                        <g:link controller="vendor" action="show" id="${vendor.id}">
                                            ${vendor.name}
                                        </g:link>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <g:if test="${participant.newSub}">
                                <g:link controller="subscription" action="show"
                                        params="${[id: participant.newSub.id]}"
                                        class="${Btn.ICON.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
                            </g:if>

                            <g:if test="${surveyConfig.subSurveyUseForTransfer}">
                                <g:set var="multiYearResultProperties"
                                       value="${surveyService.getMultiYearResultProperties(surveyConfig, Org.get(participant.id))}"/>
                                <g:if test="${multiYearResultProperties.size () > 0}">
                                    <br>
                                    <br>

                                    <div data-tooltip="${message(code: 'surveyProperty.label') + ': ' + multiYearResultProperties.collect { it.getI10n('name') }.join(', ') + ' = ' + message(code: 'refdata.Yes')}">
                                        <i class="bordered colored info icon"></i>
                                    </div>
                                </g:if>
                            </g:if>

                            <g:if test="${SurveyOrg.findByOrgAndSurveyConfig(Org.get(participant.id), surveyConfig)}">
                                <br>
                                <br>

                                <div data-tooltip="${message(code: 'surveyParticipants.selectedParticipants')}">
                                    <i class="${Icon.SURVEY} bordered colored"></i>
                                </div>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>


                    <div class="field" style="text-align: right;">
                        <button class="${Btn.POSITIVE}" ${!editable ? 'disabled="disabled"' : ''}
                                name="processOption"
                                type="submit">${message(code: 'copySurveyVendors.transfer')}</button>
                    </div>


        </g:form>
    </ui:greySegment>

    <div class="sixteen wide field" style="text-align: center;">
        <g:link class="ui button" controller="survey" action="copyProperties"
                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription.id]">
            ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
        </g:link>
    </div>

    <laser:script file="${this.getGroovyPageFileName()}">
        $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        }
        else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });

    $("input[name=selectedSubs]").checkbox({
        onChange: function() {
            $('#membersListToggler').prop('checked', false);
        }
    });
    </laser:script>

</g:else>
<laser:htmlEnd/>
