<%@ page import="de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="copySubPackagesAndIes.titel" serviceInjection="true"/>

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

<g:if test="${surveyInfo.type.id in [RDStore.SURVEY_TYPE_RENEWAL.id, RDStore.SURVEY_TYPE_SUBSCRIPTION.id, RDStore.SURVEY_TYPE_TITLE_SELECTION]}">
    <ui:linkWithIcon icon="bordered inverted orange clipboard la-object-extended" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
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

    <g:if test="${isLinkingRunning}">
        <div class="ui icon warning message">
            <i class="info icon"></i>
            <div class="content">
                <div class="header">Info</div>

                <p>${message(code: 'subscriptionsManagement.isLinkingRunning.info')}</p>
            </div>
        </div>
    </g:if>

    <g:render template="navCompareMembers"/>

    <h2 class="ui header">
        ${message(code: 'copySubPackagesAndIes.titel')}
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
                            <ui:totalNumber total="${parentSubscription.getDerivedSubscribers().size()}"/>

                            <br>

                            <div class="ui middle aligned selection list">
                                <strong><g:message code="package.plural"/>:</strong>
                                <g:each in="${parentSubscription.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index" id="${parentSubscription.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/>${raw(sp.getIEandPackageSize())}
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
                                    total="${parentSuccessorSubscription.getDerivedSubscribers().size()}"/>
                            <br>

                            <div class="ui middle aligned selection list">
                                <strong><g:message code="package.plural"/>:</strong>
                                <g:each in="${parentSuccessorSubscription.packages}" var="sp">
                                    <div class="item"><div class="content">
                                        <g:link controller="subscription" action="index"
                                                id="${parentSuccessorSubscription.id}"
                                                params="[pkgfilter: sp.pkg.id]">
                                            ${sp.pkg.name}<br/>${raw(sp.getIEandPackageSize())}
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
                method="post" class="ui form ">

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
                            <button class="ui green button" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''}
                                    type="submit"
                                    name="processOption"
                                    value="linkwithoutIE">${message(code: 'subscriptionsManagement.linkwithoutIE')}</button>

                            <div class="or" data-text="${message(code: 'default.or')}"></div>
                            <button class="ui green button" ${!editable || isLinkingRunning ? 'disabled="disabled"' : ''}
                                    type="submit"
                                    name="processOption"
                                    value="linkwithIE">${message(code: 'subscriptionsManagement.linkwithIE')}</button>
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
                                        class="ui button icon"><i class="icon clipboard"></i></g:link>
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
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>

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
