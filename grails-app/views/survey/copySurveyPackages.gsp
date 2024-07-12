<%@ page import="de.laser.helper.Icons; de.laser.AuditConfig; de.laser.storage.RDConstants; de.laser.SubscriptionPackage; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="copySurveyPackages.transfer" serviceInjection="true"/>

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
    <ui:linkWithIcon icon="${Icons.SUBSCRIPTION} bordered inverted orange la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
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

    <h2 class="ui header">
        ${message(code: 'copySurveyPackages.transfer')}
    </h2>

    <g:if test="${isLinkingRunning}">
        <ui:msg class="warning" showIcon="true" hideClose="true" header="Info" message="subscriptionsManagement.isLinkingRunning.info" />
    </g:if>


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

        <g:form action="proccessCopySurveyPackages" controller="survey" id="${surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]"
                method="post" class="ui form ">


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
                    <th rowspan="2"><g:message code="copySurveyPackages.label"/></th>
                    <th><g:message code="renewalEvaluation.parentSuccessorSubscription"/></th>
                    <th rowspan="2"></th>
                </tr>
                <tr>
                    <th><g:message code="package.plural"/></th>
                    <th><g:message code="package.plural"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${participantsList}" var="participant" status="i">

                    <tr class="">
                        <g:if test="${editable}">
                            <td>
                            <g:if test="${editable && participant.surveyPackages && SubscriptionPackage.countByPkgInList(participant.surveyPackages) < participant.surveyPackages.size()}">
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
                                <g:each in="${participant.oldSub?.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index" id="${participant.oldSub.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/>${raw(sp.getIEandPackageSize())}
                                        </g:link>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <div class="ui middle aligned selection list">
                                <g:each in="${participant.surveyPackages.sort{it.name}}" var="pkg">
                                    <div class="item">
                                        <div class="content">
                                            <g:link controller="package" action="show" id="${pkg.id}">
                                                ${pkg.name}<br/>${raw(pkg.getPackageSize())}
                                            </g:link>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <div class="ui middle aligned selection list">
                                <g:each in="${participant.newSub.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index" id="${participant.newSub.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/>${raw(sp.getIEandPackageSize())}
                                        </g:link>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <g:if test="${participant.newSub}">
                                <g:link controller="subscription" action="index"
                                        params="${[id: participant.newSub.id]}"
                                        class="ui button icon"><i class="${Icons.SUBSCRIPTION}"></i></g:link>
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
                                    <i class="${Icons.SURVEY} bordered colored"></i>
                                </div>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

            <ui:greySegment>
                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <div class="field">
                            <label for="holdingSelection">${message(code: 'subscription.holdingSelection.label')} <span
                                    class="la-long-tooltip la-popup-tooltip la-delay"
                                    data-content="${message(code: "subscription.holdingSelection.explanation")}"><i class="${Icons.TOOLTIP.HELP}"></i></span>
                            </label>
                        </div>

                        <div class="four fields">
                            <g:if test="${parentSuccessorSubscription && auditService.getAuditConfig(parentSuccessorSubscription, 'holdingSelection')}">
                                <div class="field">
                                    <ui:select class="ui dropdown search selection" id="holdingSelection" name="holdingSelection"
                                               from="${RefdataCategory.getAllRefdataValues(RDConstants.SUBSCRIPTION_HOLDING)}" optionKey="id"
                                               optionValue="value"/>
                                </div>
                            </g:if>
                            <g:else>
                                <div class="field">
                                    ${parentSuccessorSubscription.holdingSelection.getI10n('value')}
                                </div>
                            </g:else>

                            <div class="field">
                                <div class="ui checkbox toggle">
                                    <g:checkBox name="createEntitlements"/>
                                    <label><g:message code="subscription.details.link.with_ents"/></label>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="eight wide field" style="text-align: right;">
                        <button class="ui button positive" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''}
                                name="processOption"
                                type="submit">${message(code: 'copySurveyPackages.transfer')}</button>
                    </div>
                </div>
            </ui:greySegment>

        </g:form>
    </ui:greySegment>
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#membersListToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', true)
        }
        else {
            $("tr[class!=disabled] input[name=selectedSubs]").prop('checked', false)
        }
    });
    </laser:script>

</g:else>
<laser:htmlEnd/>
