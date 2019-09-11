<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} : ${message(code: 'surveyInfo.renewal')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyInfo.renewal" class="active"/>
</semui:breadcrumbs>


<semui:controlButtons>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<h2>
    ${message(code: 'surveyInfo.renewal')}
</h2>


<semui:form>

    <g:form action="proccessRenewalwithSurvey" controller="survey" id="${surveyInfo?.id}"
            params="[surveyConfigID: surveyConfig?.id]"
            method="post" class="ui form newLicence">

        <h4>
        <g:message code="renewalwithSurvey.parentSubscription"/>:
        <g:if test="${parentSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSubscription?.id}">${parentSubscription?.dropdownNamingConvention()}</g:link>
        </g:if>

        <br>
        <br>
        <g:message code="renewalwithSurvey.parentSuccessorSubscription"/>:
        <g:if test="${parentSuccessorSubscription}">
            <g:link controller="subscription" action="show"
                    id="${parentSuccessorSubscription?.id}">${parentSuccessorSubscription?.dropdownNamingConvention()}</g:link>

            <g:link controller="survey" action="copyElementsIntoRenewalSubscription" id="${parentSubscription?.id}"
                    params="[sourceSubscriptionId: parentSubscription?.id, targetSubscriptionId: parentSuccessorSubscription?.id, isRenewSub: true, isCopyAuditOn: true]"
                    class="ui button ">
                <g:message code="renewalwithSurvey.newSub.change"/>
            </g:link>

        </g:if>
        <g:else>
            <g:message code="renewalwithSurvey.noParentSuccessorSubscription"/>
            <g:link controller="survey" action="renewSubscriptionConsortiaWithSurvey" id="${surveyInfo?.id}"
                    params="[surveyConfig: surveyConfig?.id, parentSub: parentSubscription?.id]"
                    class="ui button ">
                <g:message code="renewalwithSurvey.newSub"/>
            </g:link>
        </g:else>
        </br>
        </h4>


        <br>
        <br>

        <h4 class="ui left aligned icon header">${message(code: 'renewalwithSurvey.continuetoSubscription.label')} <semui:totalNumber
                total="${orgsContinuetoSubscription?.size()}"/></h4>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'org.name.label')}</th>

                <th>
                    ${participationProperty?.getI10n('name')}

                    <g:if test="${participationProperty?.getI10n('explain')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participationProperty?.getI10n('explain')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>

                <g:each in="${properties}" var="surveyProperty">
                    <th>
                        ${surveyProperty?.getI10n('name')}

                        <g:if test="${surveyProperty?.getI10n('explain')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyProperty?.getI10n('explain')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                    </th>
                </g:each>
                <th>${message(code: 'renewalwithSurvey.costItem.label')}</th>
            </tr>
            </thead>
            <g:each in="${orgsContinuetoSubscription}" var="participantResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${participantResult?.participant?.sortname}<br>
                        <g:link controller="organisation" action="show"
                                id="${participantResult?.participant.id}">(${fieldValue(bean: participantResult?.participant, field: "name")})</g:link>

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

                            ${(costItem?.billingCurrency?.getI10n('value')?.split('-'))?.first()}
                        </g:if>
                    </td>

                </tr>
            </g:each>
        </table>


        <br>
        <br>

        <h4 class="ui left aligned icon header">${message(code: 'renewalwithSurvey.withMultiYearTermSub.label')} <semui:totalNumber
                total="${orgsWithMultiYearTermSub?.size()}"/></h4>

        <table class="ui celled la-table table">
            <thead>
            <tr>

                <th>${message(code: 'default.sortname.label')}</th>
                <th>${message(code: 'default.startDate.label')}</th>
                <th>${message(code: 'default.endDate.label')}</th>
                <th>${message(code: 'subscription.details.status')}</th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${orgsWithMultiYearTermSub}" var="sub">
                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <g:each in="${sub.getAllSubscribers()}" var="subscriberOrg">
                        <td>${subscriberOrg.sortname}</td>
                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.startDate}"/></td>
                        <td><g:formatDate formatName="default.date.format.notime" date="${sub.endDate}"/></td>
                        <td>${sub.status.getI10n('value')}</td>
                    </g:each>
                </tr>
            </g:each>
            </tbody>
        </table>

        <br>
        <br>

        <h4 class="ui left aligned icon header">${message(code: 'renewalwithSurvey.withTermination.label')} <semui:totalNumber
                total="${orgsWithTermination?.size()}"/></h4>

        <table class="ui celled sortable table la-table">
            <thead>
            <tr>
                <th class="center aligned">${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'org.name.label')}</th>

                <th>
                    ${participationProperty?.getI10n('name')}

                    <g:if test="${participationProperty?.getI10n('explain')}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                              data-content="${participationProperty?.getI10n('explain')}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>

                <g:each in="${properties}" var="surveyProperty">
                    <th>
                        ${surveyProperty?.getI10n('name')}

                        <g:if test="${surveyProperty?.getI10n('explain')}">
                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="right center"
                                  data-content="${surveyProperty?.getI10n('explain')}">
                                <i class="question circle icon"></i>
                            </span>
                        </g:if>
                    </th>
                </g:each>
                <th>${message(code: 'renewalwithSurvey.costItem.label')}</th>
            </tr>
            </thead>
            <g:each in="${orgsWithTermination}" var="participantResult" status="i">

                <tr>
                    <td class="center aligned">
                        ${i + 1}
                    </td>
                    <td>
                        ${participantResult?.participant?.sortname}<br>
                        <g:link controller="organisation" action="show"
                                id="${participantResult?.participant.id}">(${fieldValue(bean: participantResult?.participant, field: "name")})</g:link>

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

                        <b><g:formatNumber number="${costItem?.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                           maxFractionDigits="2" type="number"/></b>

                        (<g:formatNumber number="${costItem?.costInBillingCurrency}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        ${(costItem?.billingCurrency?.getI10n('value').split('-')).first()}
                    </td>
                </tr>
            </g:each>
        </table>

    </g:form>
</semui:form>

</body>
</html>
