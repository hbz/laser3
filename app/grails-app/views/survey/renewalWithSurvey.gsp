<%@ page import="de.laser.interfaces.TemplateSupport; de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyInfo.renewalOverView')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyInfo.renewalOverView" class="active"/>
</semui:breadcrumbs>


<semui:controlButtons>
    <semui:exportDropdown>
        <semui:exportDropdownItem>
            <g:link class="item" action="renewalWithSurvey" id="${surveyInfo.id}"
                    params="[surveyConfigID: surveyConfig.id, exportXLS: true]">${message(code: 'renewalWithSurvey.exportRenewal')}</g:link>
        </semui:exportDropdownItem>
    </semui:exportDropdown>
    <semui:actionsDropdown>
        <g:if test="${parentSuccessorSubscription}">

            <semui:actionsDropdownItem data-semui="modal" href="#transferParticipantsModal"
                                       message="surveyInfo.transferParticipants"/>


            <semui:actionsDropdownItem controller="survey" action="compareMembersOfTwoSubs" params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.transferOverView"/>
        </g:if>
        <g:else>
            <semui:actionsDropdownItemDisabled data-semui="modal" href="#transferParticipantsModal"
                                               message="surveyInfo.transferParticipants" tooltip="${message(code: 'renewalWithSurvey.noParentSuccessorSubscription')}"/>
        </g:else>

    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<h2>
    ${message(code: 'surveyInfo.renewalOverView')}
</h2>


<semui:form>

    <h3>
    <g:message code="renewalWithSurvey.parentSubscription"/>:
    <g:if test="${parentSubscription}">
        <g:link controller="subscription" action="show"
                id="${parentSubscription?.id}">${parentSubscription?.dropdownNamingConvention()}</g:link>
    </g:if>

    <br>
    <br>
    <g:message code="renewalWithSurvey.parentSuccessorSubscription"/>:
    <g:if test="${parentSuccessorSubscription}">
        <g:link controller="subscription" action="show"
                id="${parentSuccessorSubscription?.id}">${parentSuccessorSubscription?.dropdownNamingConvention()}</g:link>

        <g:if test="${parentSuccessorSubscription.getAllSubscribers().size() > 0}">
        <g:link controller="survey" action="copyElementsIntoRenewalSubscription" id="${parentSubscription?.id}"
                params="[sourceSubscriptionId: parentSubscription?.id, targetSubscriptionId: parentSuccessorSubscription?.id, isRenewSub: true, isCopyAuditOn: true]"
                class="ui button ">
            <g:message code="renewalWithSurvey.newSub.change"/>
        </g:link>
        </g:if>

    </g:if>
    <g:else>
        <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo?.id}"
                params="[surveyConfig: surveyConfig?.id, parentSub: parentSubscription?.id]"
                class="ui button ">
            <g:message code="renewalWithSurvey.newSub"/>
        </g:link>
    </g:else>
    </br>
    </h3>

    <br>

    <g:set var="consortiaSubscriptions"
           value="${com.k_int.kbplus.Subscription.findAllByInstanceOf(parentSubscription)?.size()}"/>
    <g:set var="surveyParticipants" value="${surveyConfig?.orgs?.size()}"/>
    <g:set var="totalOrgs"
           value="${(orgsContinuetoSubscription?.size() ?: 0) + (newOrgsContinuetoSubscription?.size() ?: 0) + (orgsWithMultiYearTermSub?.size() ?: 0) + (orgsLateCommers?.size() ?: 0) + (orgsWithTermination?.size() ?: 0) + (orgsWithoutResult?.size() ?: 0) + (orgsWithParticipationInParentSuccessor?.size() ?: 0)}"/>


    <h3 class="ui left floated aligned icon header la-clear-before">
        <g:link action="evaluationConfigsInfo" id="${surveyInfo?.id}"
                params="[surveyConfigID: surveyConfig?.id]">${message(code: 'survey.label')} ${message(code: 'surveyParticipants.label')}</g:link>
        <semui:totalNumber total="${surveyParticipants}"/>
        <br>
        <g:link controller="subscription" action="members"
                id="${parentSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
        <semui:totalNumber class="${surveyParticipants != consortiaSubscriptions ? 'red' : ''}"
                           total="${consortiaSubscriptions}"/>
        <br>
        ${message(code: 'renewalWithSurvey.orgsTotalInRenewalProcess')}
        <semui:totalNumber class="${totalOrgs != consortiaSubscriptions ? 'red' : ''}" total="${totalOrgs}"/>
    </h3>

    <br>
    <br>

    <h4 class="ui left floated aligned icon header la-clear-before">${message(code: 'renewalWithSurvey.continuetoSubscription.label')} <semui:totalNumber
            total="${orgsContinuetoSubscription?.size() ?: 0}"/></h4>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'org.name.label')}</th>

            <th>
                ${participationProperty?.getI10n('name')}

                <g:if test="${participationProperty?.getI10n('expl')}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${participationProperty?.getI10n('expl')}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </th>
            <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey}">
                <th>
                    <g:message code="renewalWithSurvey.period"/>
                </th>
            </g:if>


            <g:each in="${properties}" var="surveyProperty">
                <th>
                    ${surveyProperty?.getI10n('name')}

                    <g:if test="${surveyProperty?.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${surveyProperty?.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
            </g:each>
            <th>${message(code: 'renewalWithSurvey.costItem.label')}</th>
            <th>${message(code: 'default.actions')}</th>
        </tr>
        </thead>
        <g:each in="${orgsContinuetoSubscription}" var="participantResult" status="i">

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:link controller="myInstitution" action="manageParticipantSurveys"
                            id="${participantResult?.participant.id}">
                        ${participantResult?.participant?.sortname}
                    </g:link>
                    <br>
                    <g:link controller="organisation" action="show"
                            id="${participantResult?.participant.id}">(${fieldValue(bean: participantResult?.participant, field: "name")})</g:link>

                    <div class="ui grid">
                        <div class="right aligned wide column">
                            <g:if test="${!surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(participantResult?.participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyConfig?.checkResultsEditByOrg(participantResult?.participant) == com.k_int.kbplus.SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
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

                            <g:if test="${surveyConfig?.isResultsSetFinishByOrg(participantResult?.participant)}">
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
                        </div>
                    </div>
                </td>
                <td>
                    ${participantResult?.resultOfParticipation?.getResult()}

                    <g:if test="${participantResult?.resultOfParticipation?.comment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.comment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                    <g:if test="${participantResult?.resultOfParticipation?.ownerComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.ownerComment}">
                            <i class="info circle icon"></i>
                        </span>
                    </g:if>

                </td>

                <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey}">
                    <td>
                </g:if>

                <g:if test="${multiYearTermTwoSurvey}">
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodTwoStartDate}"/>
                    <br>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodTwoEndDate}"/>

                    <g:if test="${participantResult?.participantPropertyTwoComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.participantPropertyTwoComment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                </g:if>
                <g:if test="${multiYearTermThreeSurvey}">
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodThreeStartDate}"/>
                    <br>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodThreeEndDate}"/>

                    <g:if test="${participantResult?.participantPropertyThreeComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.participantPropertyThreeComment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </g:if>

                <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey}">
                    </td>
                </g:if>

                <g:each in="${participantResult?.properties.sort { it?.type?.name }}"
                        var="participantResultProperty">
                    <td>
                        ${participantResultProperty?.getResult()}

                        <g:if test="${participantResultProperty?.comment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.comment}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${participantResultProperty?.ownerComment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.ownerComment}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                </g:each>

                <td>

                    <g:set var="costItem" value="${participantResult?.resultOfParticipation?.getCostItem()}"/>

                    <g:if test="${costItem}">
                        <b><g:formatNumber number="${costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                           maxFractionDigits="2" type="number"/></b>

                        (<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        ${(costItem?.billingCurrency?.getI10n('value')?.split('-'))?.first()}
                    </g:if>
                </td>
                <td>

                    <g:link controller="myInstitution" action="surveyParticipantConsortiaNew"
                            id="${participantResult?.participant?.id}"
                            params="[surveyConfig: surveyConfig?.id]" class="ui button icon"><i
                            class="icon chart pie"></i></g:link>

                    <g:if test="${participantResult?.sub}">
                        <br>
                        <g:link controller="subscription" action="show" id="${participantResult?.sub?.id}"
                                class="ui button icon"><i class="icon clipboard"></i></g:link>
                    </g:if>
                </td>

            </tr>
        </g:each>
    </table>


    <br>
    <br>

    <h4 class="ui left floated aligned icon header la-clear-before">${message(code: 'renewalWithSurvey.newOrgstoSubscription.label')} <semui:totalNumber
            total="${newOrgsContinuetoSubscription?.size() ?: 0}"/></h4>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'org.name.label')}</th>

            <th>
                ${participationProperty?.getI10n('name')}

                <g:if test="${participationProperty?.getI10n('expl')}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${participationProperty?.getI10n('expl')}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </th>

            <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey}">
                <th>
                    <g:message code="renewalWithSurvey.period"/>
                </th>
            </g:if>

            <g:each in="${properties}" var="surveyProperty">
                <th>
                    ${surveyProperty?.getI10n('name')}

                    <g:if test="${surveyProperty?.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${surveyProperty?.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
            </g:each>
            <th>${message(code: 'renewalWithSurvey.costItem.label')}</th>
            <th>${message(code: 'default.actions')}</th>
        </tr>
        </thead>
        <g:each in="${newOrgsContinuetoSubscription}" var="participantResult" status="i">

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:link controller="myInstitution" action="manageParticipantSurveys"
                            id="${participantResult?.participant.id}">
                        ${participantResult?.participant?.sortname}
                    </g:link>
                    <br>
                    <g:link controller="organisation" action="show"
                            id="${participantResult?.participant.id}">(${fieldValue(bean: participantResult?.participant, field: "name")})</g:link>

                    <div class="ui grid">
                        <div class="right aligned wide column">
                            <g:if test="${!surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(participantResult?.participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyConfig?.checkResultsEditByOrg(participantResult?.participant) == com.k_int.kbplus.SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
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

                            <g:if test="${surveyConfig?.isResultsSetFinishByOrg(participantResult?.participant)}">
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
                        </div>
                    </div>
                </td>

                <td>
                    ${participantResult?.resultOfParticipation?.getResult()}

                    <g:if test="${participantResult?.resultOfParticipation?.comment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.comment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                    <g:if test="${participantResult?.resultOfParticipation?.ownerComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.ownerComment}">
                            <i class="info circle icon"></i>
                        </span>
                    </g:if>

                </td>

                <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey}">
                    <td>
                </g:if>

                <g:if test="${multiYearTermTwoSurvey}">
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodTwoStartDate}"/>
                    <br>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodTwoEndDate}"/>

                    <g:if test="${participantResult?.participantPropertyTwoComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.participantPropertyTwoComment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </g:if>
                <g:if test="${multiYearTermThreeSurvey}">
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodThreeStartDate}"/>
                    <br>
                    <g:formatDate formatName="default.date.format.notime"
                                  date="${participantResult?.newSubPeriodThreeEndDate}"/>

                    <g:if test="${participantResult?.participantPropertyThreeComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.participantPropertyThreeComment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </g:if>

                <g:if test="${multiYearTermTwoSurvey || multiYearTermThreeSurvey}">
                    </td>
                </g:if>



                <g:each in="${participantResult?.properties.sort { it?.type?.name }}"
                        var="participantResultProperty">
                    <td>
                        ${participantResultProperty?.getResult()}

                        <g:if test="${participantResultProperty?.comment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.comment}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${participantResultProperty?.ownerComment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.ownerComment}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                </g:each>

                <td>

                    <g:set var="costItem" value="${participantResult?.resultOfParticipation?.getCostItem()}"/>

                    <g:if test="${costItem}">
                        <b><g:formatNumber number="${costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                           maxFractionDigits="2" type="number"/></b>

                        (<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        ${(costItem?.billingCurrency?.getI10n('value')?.split('-'))?.first()}
                    </g:if>
                </td>
                <td>

                    <g:link controller="myInstitution" action="surveyParticipantConsortiaNew"
                            id="${participantResult?.participant?.id}"
                            params="[surveyConfig: surveyConfig?.id]" class="ui button icon"><i
                            class="icon chart pie"></i></g:link>

                    <g:if test="${participantResult?.sub}">
                        <br>
                        <g:link controller="subscription" action="show" id="${participantResult?.sub?.id}"
                                class="ui button icon"><i class="icon clipboard"></i></g:link>
                    </g:if>
                </td>

            </tr>
        </g:each>
    </table>


    <br>
    <br>

    <h4 class="ui left floated aligned icon header la-clear-before">${message(code: 'renewalWithSurvey.withMultiYearTermSub.label')} <semui:totalNumber
            total="${orgsWithMultiYearTermSub?.size() ?: 0}"/></h4>

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.sortname.label')}</th>
            <th>${message(code: 'default.startDate.label')}</th>
            <th>${message(code: 'default.endDate.label')}</th>
            <th>${message(code: 'subscription.details.status')}</th>
            <th>${message(code: 'default.actions')}</th>

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
                        ${subscriberOrg?.sortname}
                        <br>

                        <g:link controller="organisation" action="show"
                                id="${subscriberOrg.id}">(${fieldValue(bean: subscriberOrg, field: "name")})</g:link>
                    </td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <td>${sub.status.getI10n('value')}</td>
                    <td>
                        <g:if test="${sub}">
                            <g:link controller="subscription" action="show" id="${sub?.id}"
                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                        </g:if>
                        <g:if test="${sub?.getCalculatedSuccessor()}">
                            <br>
                            <g:link controller="subscription" action="show" id="${sub?.getCalculatedSuccessor()?.id}"
                                    class="ui button icon"><i class="icon yellow clipboard"></i></g:link>
                        </g:if>
                    </td>
                </g:each>
            </tr>
        </g:each>
        </tbody>
    </table>

    <br>
    <br>

    <h4 class="ui left floated aligned icon header la-clear-before">${message(code: 'renewalWithSurvey.orgsWithParticipationInParentSuccessor.label')} <semui:totalNumber
            total="${orgsWithParticipationInParentSuccessor?.size() ?: 0}"/></h4>

    <table class="ui celled la-table table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'default.sortname.label')}</th>
            <th>${message(code: 'default.startDate.label')}</th>
            <th>${message(code: 'default.endDate.label')}</th>
            <th>${message(code: 'subscription.details.status')}</th>
            <th>${message(code: 'default.actions')}</th>

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
                        ${subscriberOrg?.sortname}
                        <br>
                        <g:link controller="organisation" action="show"
                                id="${subscriberOrg.id}">(${fieldValue(bean: subscriberOrg, field: "name")})</g:link>
                    </td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                    <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                    <td>${sub.status.getI10n('value')}</td>
                    <td>
                        <g:if test="${sub}">
                            <g:link controller="subscription" action="show" id="${sub?.id}"
                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                        </g:if>
                        <g:if test="${sub?.getCalculatedSuccessor()}">
                            <br>
                            <g:link controller="subscription" action="show" id="${sub?.getCalculatedSuccessor()?.id}"
                                    class="ui button icon"><i class="icon yellow clipboard"></i></g:link>
                        </g:if>
                    </td>
                </g:each>
            </tr>
        </g:each>
        </tbody>
    </table>

    <br>
    <br>


    <h4 class="ui left floated aligned icon header la-clear-before">${message(code: 'renewalWithSurvey.withTermination.label')} <semui:totalNumber
            total="${orgsWithTermination?.size() ?: 0}"/></h4>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'org.name.label')}</th>

            <th>
                ${participationProperty?.getI10n('name')}

                <g:if test="${participationProperty?.getI10n('expl')}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${participationProperty?.getI10n('expl')}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </th>

            <g:each in="${properties}" var="surveyProperty">
                <th>
                    ${surveyProperty?.getI10n('name')}

                    <g:if test="${surveyProperty?.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyProperty?.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
            </g:each>
            <th>${message(code: 'renewalWithSurvey.costItem.label')}</th>
            <th>${message(code: 'default.actions')}</th>
        </tr>
        </thead>
        <g:each in="${orgsWithTermination}" var="participantResult" status="i">

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>
                    <g:link controller="myInstitution" action="manageParticipantSurveys"
                            id="${participantResult?.participant.id}">
                        ${participantResult?.participant?.sortname}
                    </g:link>
                    <br>
                    <g:link controller="organisation" action="show"
                            id="${participantResult?.participant.id}">(${fieldValue(bean: participantResult?.participant, field: "name")})</g:link>

                    <div class="ui grid">
                        <div class="right aligned wide column">
                            <g:if test="${!surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(participantResult?.participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyConfig?.checkResultsEditByOrg(participantResult?.participant) == com.k_int.kbplus.SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
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

                            <g:if test="${surveyConfig?.isResultsSetFinishByOrg(participantResult?.participant)}">
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
                        </div>
                    </div>

                </td>
                <td>
                    ${participantResult?.resultOfParticipation?.getResult()}

                    <g:if test="${participantResult?.resultOfParticipation?.comment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.comment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                    <g:if test="${participantResult?.resultOfParticipation?.ownerComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.ownerComment}">
                            <i class="info circle icon"></i>
                        </span>
                    </g:if>

                </td>

                <g:each in="${participantResult?.properties.sort { it?.type?.name }}"
                        var="participantResultProperty">
                    <td>
                        ${participantResultProperty?.getResult()}

                        <g:if test="${participantResultProperty?.comment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.comment}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${participantResultProperty?.ownerComment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.ownerComment}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                </g:each>
                <td>

                    <g:set var="costItem" value="${participantResult?.resultOfParticipation?.getCostItem()}"/>
                    <g:if test="${costItem}">
                        <b><g:formatNumber number="${costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                           maxFractionDigits="2" type="number"/></b>

                        (<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        ${(costItem?.billingCurrency?.getI10n('value')?.split('-')).first()}
                    </g:if>
                </td>
                <td>

                    <g:link controller="myInstitution" action="surveyParticipantConsortiaNew"
                            id="${participantResult?.participant?.id}"
                            params="[surveyConfig: surveyConfig?.id]" class="ui button icon"><i
                            class="icon chart pie"></i></g:link>

                    <g:if test="${participantResult?.sub}">
                        <br>
                        <g:link controller="subscription" action="show" id="${participantResult?.sub?.id}"
                                class="ui button icon"><i class="icon clipboard"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>

    <br>
    <br>

    <h4 class="ui left floated aligned icon header la-clear-before">${message(code: 'renewalWithSurvey.orgsWithoutResult.label')} <semui:totalNumber
            total="${orgsWithoutResult?.size() ?: 0}"/></h4>

    <table class="ui celled sortable table la-table">
        <thead>
        <tr>
            <th class="center aligned">${message(code: 'sidewide.number')}</th>
            <th>${message(code: 'org.name.label')}</th>

            <th>
                ${participationProperty?.getI10n('name')}

                <g:if test="${participationProperty?.getI10n('expl')}">
                    <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                          data-content="${participationProperty?.getI10n('expl')}">
                        <i class="question circle icon"></i>
                    </span>
                </g:if>
            </th>

            <g:each in="${properties}" var="surveyProperty">
                <th>
                    ${surveyProperty?.getI10n('name')}

                    <g:if test="${surveyProperty?.getI10n('expl')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${surveyProperty?.getI10n('expl')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
            </g:each>
            <th>${message(code: 'renewalWithSurvey.costItem.label')}</th>
            <th>${message(code: 'default.actions')}</th>
        </tr>
        </thead>
        <g:each in="${orgsWithoutResult}" var="participantResult" status="i">

            <tr>
                <td class="center aligned">
                    ${i + 1}
                </td>
                <td>

                    <g:link controller="myInstitution" action="manageParticipantSurveys"
                            id="${participantResult?.participant.id}">
                        ${participantResult?.participant?.sortname}
                    </g:link>

                    <br>

                    <g:link controller="organisation" action="show"
                            id="${participantResult?.participant.id}">(${fieldValue(bean: participantResult?.participant, field: "name")})</g:link>

                    <div class="ui grid">
                        <div class="right aligned wide column">
                            <g:if test="${!surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(participantResult?.participant)}">
                                <span data-position="top right" class="la-popup-tooltip la-delay"
                                      data-content="${message(code: 'surveyResult.newOrg')}">
                                    <i class="star black large  icon"></i>
                                </span>
                            </g:if>

                            <g:if test="${surveyConfig?.checkResultsEditByOrg(participantResult?.participant) == com.k_int.kbplus.SurveyConfig.ALL_RESULTS_PROCESSED_BY_ORG}">
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

                            <g:if test="${surveyConfig?.isResultsSetFinishByOrg(participantResult?.participant)}">
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
                        </div>
                    </div>
                </td>
                <td>
                    ${participantResult?.resultOfParticipation?.getResult()}

                    <g:if test="${participantResult?.resultOfParticipation?.comment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.comment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>

                    <g:if test="${participantResult?.resultOfParticipation?.ownerComment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participantResult?.resultOfParticipation?.ownerComment}">
                            <i class="info circle icon"></i>
                        </span>
                    </g:if>

                </td>

                <g:each in="${participantResult?.properties.sort { it?.type?.name }}"
                        var="participantResultProperty">
                    <td>
                        ${participantResultProperty?.getResult()}

                        <g:if test="${participantResultProperty?.comment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.comment}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>

                        <g:if test="${participantResultProperty?.ownerComment}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${participantResultProperty?.ownerComment}">
                                <i class="info circle icon"></i>
                            </span>
                        </g:if>

                    </td>
                </g:each>
                <td>

                    <g:set var="costItem" value="${participantResult?.resultOfParticipation?.getCostItem()}"/>
                    <g:if test="${costItem}">
                        <b><g:formatNumber number="${costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                           maxFractionDigits="2" type="number"/></b>

                        (<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        ${(costItem?.billingCurrency?.getI10n('value')?.split('-')).first()}
                    </g:if>
                </td>
                <td>

                    <g:link controller="myInstitution" action="surveyParticipantConsortiaNew"
                            id="${participantResult?.participant?.id}"
                            params="[surveyConfig: surveyConfig?.id]" class="ui button icon"><i
                            class="icon chart pie"></i></g:link>

                    <g:if test="${participantResult?.sub}">
                        <br>
                        <g:link controller="subscription" action="show" id="${participantResult?.sub?.id}"
                                class="ui button icon"><i class="icon clipboard"></i></g:link>
                    </g:if>
                </td>
            </tr>
        </g:each>
    </table>

</semui:form>

<g:if test="${parentSuccessorSubscription}">

    <g:set var="auditConfigProvidersAgencies" value="${parentSuccessorSubscription.orgRelations?.findAll {it.isShared}}" />

    <semui:modal id="transferParticipantsModal" message="surveyInfo.transferParticipants"
                 msgSave="${message(code: 'surveyInfo.transferParticipants.button')}">

        <h3><g:message code="surveyInfo.transferParticipants.option"/>:</h3>

        <g:form class="ui form"
                url="[controller: 'survey', action: 'processTransferParticipants', params: [id: params.id, surveyConfigID: surveyConfig?.id]]">
            <div class="field">
                <g:set var="properties" value="${de.laser.AuditConfig.getConfigs(parentSuccessorSubscription)}"></g:set>
                <g:if test="${properties}">

                    <label><g:message code="subscription.details.copyElementsIntoSubscription.auditConfig" />:</label>
                    <div class="ui bulleted list">
                        <g:each in="${properties}" var="prop" >
                            <div class="item">
                                <b><g:message code="subscription.${prop.referenceField}.label" /></b>:
                            <g:if test="${parentSuccessorSubscription.getProperty(prop.referenceField) instanceof com.k_int.kbplus.RefdataValue}">
                                ${parentSuccessorSubscription.getProperty(prop.referenceField).getI10n('value')}
                            </g:if>
                            <g:else>
                                ${parentSuccessorSubscription.getProperty(prop.referenceField)}
                            </g:else>
                            </div>
                        </g:each>
                    </div>
                </g:if>
                <g:else>
                    <g:message code="subscription.details.copyElementsIntoSubscription.noAuditConfig"/>
                </g:else>

                <g:if test="${auditConfigProvidersAgencies}">
                    <label><g:message code="property.share.tooltip.on" />:</label>
                    <div class="ui bulleted list">
                        <g:each in="${auditConfigProvidersAgencies}" var="role" >
                            <div class="item">
                                <b> ${role.roleType.getI10n("value")}</b>:
                                    ${role.org.name}
                            </div>
                        </g:each>
                    </div>

                </g:if>

            </div>
            <div class="two fields">
                <g:set var="validPackages" value="${parentSuccessorSubscription.packages?.sort { it.pkg.name }}"/>
                <div class="field">

                    <label><g:message code="myinst.addMembers.linkPackages"/></label>
                    <g:if test="${validPackages}">
                        <div class="ui checkbox">
                            <input type="checkbox" id="linkAllPackages" name="linkAllPackages">
                            <label for="linkAllPackages"><g:message code="myinst.addMembers.linkAllPackages"
                                                                    args="${superOrgType}"/>
                            (<g:each in="${validPackages}" var="pkg">
                                ${pkg.getPackageName()},
                            </g:each>)
                            </label>
                        </div>

                        <div class="ui checkbox">
                            <input type="checkbox" id="linkWithEntitlements" name="linkWithEntitlements">
                            <label for="linkWithEntitlements"><g:message
                                    code="myinst.addMembers.withEntitlements"/></label>
                        </div>

                        <div class="field">
                            <g:select class="ui search multiple dropdown"
                                      optionKey="id" optionValue="${{ it.getPackageName() }}"
                                      from="${validPackages}" name="packageSelection" value=""
                                      noSelection='["": "${message(code: 'subscription.linkPackagesMembers.noSelection')}"]'/>
                        </div>
                    </g:if>
                    <g:else>
                        <g:message code="subscription.linkPackagesMembers.noValidLicenses" args="${superOrgType}"/>
                    </g:else>
                </div>

                <g:if test="${!auditConfigProvidersAgencies}">
                <div class="field">
                    <g:set var="providers" value="${parentSuccessorSubscription.getProviders()?.sort { it.name }}"/>
                    <g:set var="agencies" value="${parentSuccessorSubscription.getAgencies()?.sort { it.name }}"/>

                    <g:if test="${(providers || agencies)}">
                        <label><g:message code="surveyInfo.transferParticipants.moreOption"/></label>

                        <div class="ui checkbox">
                            <input type="checkbox" id="transferProviderAgency" name="transferProviderAgency" checked>
                            <label for="transferProviderAgency"><g:message
                                    code="surveyInfo.transferParticipants.transferProviderAgency"
                                    args="${superOrgType}"/>
                            <g:set var="providerAgency" value="${providers + agencies}"/>
                            (${providerAgency ? providerAgency.name.join(', ') : ''})
                            </label>
                        </div>

                        <div class="field">

                            <g:if test="${providers}">
                                <label><g:message code="surveyInfo.transferParticipants.transferProvider"
                                                  args="${superOrgType}"/>:</label>
                                <g:select class="ui search multiple dropdown"
                                          optionKey="id" optionValue="name"
                                          from="${providers}" name="providersSelection" value=""
                                          noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferProvider')}"]'/>
                            </g:if>
                        </div>

                        <div class="field">
                            <g:set var="agencies"
                                   value="${parentSuccessorSubscription.getAgencies()?.sort { it.name }}"/>
                            <g:if test="${agencies}">
                                <label><g:message code="surveyInfo.transferParticipants.transferAgency"
                                                  args="${superOrgType}"/>:</label>
                                <g:select class="ui search multiple dropdown"
                                          optionKey="id" optionValue="name"
                                          from="${agencies}" name="agenciesSelection" value=""
                                          noSelection='["": "${message(code: 'surveyInfo.transferParticipants.noSelectionTransferAgency')}"]'/>
                            </g:if>
                        </div>
                    </g:if>
                    <g:else>
                        <g:message code="surveyInfo.transferParticipants.noTransferProviderAgency"
                                   args="${superOrgType}"/>
                    </g:else>
                </div>
                </g:if>
            </div>

            <div class="ui two fields">
                <div class="field">
                    <label><g:message code="myinst.copyLicense"/></label>
                    <g:if test="${parentSuccessorSubscription.owner}">
                        <g:if test="${parentSuccessorSubscription.getCalculatedType() == de.laser.interfaces.TemplateSupport.CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE && institution.id == parentSuccessorSubscription.getCollective().id}">
                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" name="attachToParticipationLic" value="true">
                                <label><g:message code="myinst.attachToParticipationLic"/></label>
                            </div>

                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" name="attachToParticipationLic" value="false"
                                       checked="checked">
                                <label><g:message code="myinst.noAttachmentToParticipationLic"/></label>
                            </div>
                        </g:if>
                        <g:else>
                            <div class="ui radio checkbox">
                                <g:if test="${parentSuccessorSubscription.owner.derivedLicenses}">
                                    <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics"
                                           value="no">
                                </g:if>
                                <g:else>
                                    <input class="hidden" type="radio" id="generateSlavedLics" name="generateSlavedLics"
                                           value="no"
                                           checked="checked">
                                </g:else>
                                <label for="generateSlavedLics">${message(code: 'myinst.separate_lics_no')}</label>
                            </div>

                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" id="generateSlavedLics1" name="generateSlavedLics"
                                       value="shared">
                                <label for="generateSlavedLics1">${message(code: 'myinst.separate_lics_shared', args: superOrgType)}</label>
                            </div>

                            <div class="ui radio checkbox">
                                <input class="hidden" type="radio" id="generateSlavedLics2" name="generateSlavedLics"
                                       value="explicit">
                                <label for="generateSlavedLics2">${message(code: 'myinst.separate_lics_explicit', args: superOrgType)}</label>
                            </div>

                            <g:if test="${parentSuccessorSubscription.owner.derivedLicenses}">
                                <div class="ui radio checkbox">
                                    <input class="hidden" type="radio" id="generateSlavedLics3"
                                           name="generateSlavedLics"
                                           value="reference" checked="checked">
                                    <label for="generateSlavedLics3">${message(code: 'myinst.separate_lics_reference')}</label>
                                </div>

                                <div class="generateSlavedLicsReference-wrapper hidden">
                                    <br/>
                                    <g:select from="${parentSuccessorSubscription.owner?.derivedLicenses}"
                                              class="ui search dropdown hide"
                                              optionKey="${{ 'com.k_int.kbplus.License:' + it.id }}"
                                              optionValue="${{ it.reference }}"
                                              name="generateSlavedLicsReference"/>
                                </div>
                                <r:script>
                                    $('*[name=generateSlavedLics]').change(function () {
                                        $('*[name=generateSlavedLics][value=reference]').prop('checked') ?
                                                $('.generateSlavedLicsReference-wrapper').removeClass('hidden') :
                                                $('.generateSlavedLicsReference-wrapper').addClass('hidden');
                                    })
                                    $('*[name=generateSlavedLics]').trigger('change')
                                </r:script>
                            </g:if>
                        </g:else>
                    </g:if>
                    <g:else>
                        <semui:msg class="info" text="${message(code: 'myinst.noSubscriptionOwner')}"/>
                    </g:else>
                </div>
            </div>

        </g:form>

    </semui:modal>
</g:if>

</body>
</html>
