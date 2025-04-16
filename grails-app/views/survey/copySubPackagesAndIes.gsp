<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="copySubPackagesAndIes.titel" />

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

<g:if test="${(surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])}">
    <div class="ui segment">
        <strong>${message(code: 'survey.notStarted')}</strong>
    </div>
</g:if>
<g:else>

    <g:render template="multiYearsSubs"/>

    <g:if test="${isLinkingRunning}">
        <ui:msg class="warning" showIcon="true" hideClose="true" header="Info" message="subscriptionsManagement.isLinkingRunning.info" />
    </g:if>

    <g:render template="navCompareMembers"/>

   %{-- <h2 class="ui header">
        ${message(code: 'copySubPackagesAndIes.titel')}
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

                            <br>

                            <div class="ui middle aligned selection list">
                                <strong><g:message code="package.plural"/>:</strong>
                                <g:each in="${parentSubscription.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index" id="${parentSubscription.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/><ui:ieAndPkgSize sp="${sp}" />
                                        </g:link>
                                    </div>
                                    </div>
                                </g:each>
                            </div>
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
                            <br>

                            <div class="ui middle aligned selection list">
                                <strong><g:message code="package.plural"/>:</strong>
                                <g:each in="${parentSuccessorSubscription.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index"
                                                id="${parentSuccessorSubscription.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/><ui:ieAndPkgSize sp="${sp}" />
                                        </g:link>
                                    </div>
                                    </div>
                                </g:each>
                            </div>

                        </g:if>
                    </h3>
                </div>
            </div>
        </div>
    </ui:greySegment>

    <ui:greySegment>

        <g:form action="proccessCopySubPackagesAndIes" controller="survey" id="${surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]"
                method="post" class="ui form">

            <div class="ui segment">
                <div class="field required">

                    <label>
                        <g:message code="package.plural"/>
                    <g:message code="renewalEvaluation.parentSuccessorSubscription"/></th>
                        <g:message code="messageRequiredField"/>
                    </label>

                    <g:if test="${validPackages}">
                        <g:select class="ui multiple search dropdown"
                                  optionKey="${{ it.pkg.id }}"
                                  optionValue="${{ it.getPackageNameWithCurrentTippsCount() }}"
                                  from="${validPackages}" name="selectedPackages" value=""
                                  required=""
                                  noSelection='["all": "${message(code: 'subscriptionsManagement.all.package')}"]'/>
                    </g:if>
                    <g:else>
                        <g:message code="subscriptionsManagement.noValidPackages"/>
                    </g:else>

                </div>

                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <div class="ui buttons">
                            <g:if test="${parentSuccessorSubscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}">
                                <button class="${Btn.POSITIVE}" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                        name="processOption"
                                        value="linkwithoutIE">${message(code: 'subscriptionsManagement.linkGeneral')}</button>
                            </g:if>
                            <g:else>
                                <button class="${Btn.POSITIVE}" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                        name="processOption"
                                        value="linkwithoutIE">${message(code: 'subscriptionsManagement.linkwithoutIE')}</button>

                                <div class="or" data-text="${message(code: 'default.or')}"></div>
                                <button class="${Btn.POSITIVE}" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''} type="submit"
                                        name="processOption"
                                        value="linkwithIE">${message(code: 'subscriptionsManagement.linkwithIE')}</button>
                            </g:else>
                        </div>
                    </div>
                </div>
            </div>

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
                                <%-- This whole construct is necessary for that the form validation works!!! --%>
                                <div class="field">
                                    <div class="ui checkbox">
                                        <g:checkBox id="selectedSubs_${participant.newSub.id}" name="selectedSubs" value="${participant.newSub.id}"
                                                    checked="false"/>
                                    </div>
                                </div>
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
                                    <div class="item">
                                        <div class="content">
                                            <g:if test="${participant.oldSub?.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}">
                                                <g:link controller="subscription" action="index" id="${participant.oldSub.id}"
                                                        params="[pkgfilter: sp.pkg.id]">
                                                    ${sp.pkg.name}<br/><i class="${Icon.SIG.INHERITANCE_AUTO}"></i>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="subscription" action="index" id="${participant.oldSub.id}"
                                                        params="[pkgfilter: sp.pkg.id]">
                                                    ${sp.pkg.name}<br/><ui:ieAndPkgSize sp="${sp}" />
                                                </g:link>
                                            </g:else>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <div class="ui middle aligned selection list">
                                <g:each in="${participant.newSub.packages}" var="sp">
                                    <div class="item">
                                        <div class="content">
                                            <g:if test="${participant.newSub.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE}">
                                                <g:link controller="subscription" action="index" id="${participant.newSub.id}"
                                                        params="[pkgfilter: sp.pkg.id]">
                                                    ${sp.pkg.name}<br/><i class="${Icon.SIG.INHERITANCE_AUTO}"></i>
                                                </g:link>
                                            </g:if>
                                            <g:else>
                                                <g:link controller="subscription" action="index" id="${participant.newSub.id}"
                                                        params="[pkgfilter: sp.pkg.id]">
                                                    ${sp.pkg.name}<br/><ui:ieAndPkgSize sp="${sp}" />
                                                </g:link>
                                            </g:else>
                                        </div>
                                    </div>
                                </g:each>
                            </div>
                        </td>
                        <td>
                            <g:if test="${participant.newSub}">
                                <g:link controller="subscription" action="index"
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

        </g:form>
    </ui:greySegment>

    <div class="sixteen wide field" style="text-align: center;">
        <g:if test="${surveyConfig.packageSurvey}">
            <g:link class="ui button" controller="survey" action="copySurveyPackages"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription.id]">
                ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
            </g:link>
        </g:if>
        <g:elseif test="${surveyConfig.vendorSurvey}">
            <g:link class="ui button" controller="survey" action="copySurveyVendors"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription.id]">
                ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
            </g:link>
        </g:elseif>
        <g:else>
            <g:link class="ui button" controller="survey" action="copyProperties"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription.id]">
                ${message(code: 'copySurveyCostItems.workFlowSteps.nextStep')}
            </g:link>
        </g:else>
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
