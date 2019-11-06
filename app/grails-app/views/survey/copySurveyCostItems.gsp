<%@ page import="de.laser.helper.RDStore; com.k_int.kbplus.SurveyProperty;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<g:set var="surveyService" bean="surveyService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser', default: 'LAS:eR')} :  ${message(code: 'surveyInfo.copySurveyCostItems')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
        <semui:crumb controller="survey" action="renewalWithSurvey" id="${surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" message="surveyInfo.renewalOverView"/>
    </g:if>
    <semui:crumb message="surveyInfo.transferOverView" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="survey" action="renewalWithSurvey" params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.renewalOverView"/>

            <semui:actionsDropdownItem controller="survey" action="copySurveyCostItems" params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                       message="surveyInfo.copySurveyCostItems"/>

    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<h2>
    ${message(code: 'surveyInfo.copySurveyCostItems')}
</h2>


<semui:form>

    <g:form action="proccessCopySurveyCostItems" controller="survey" id="${surveyInfo?.id}"
            params="[surveyConfigID: surveyConfig?.id]"
            method="post" class="ui form ">

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

        <g:set var="surveyParticipants" value="${surveyConfig?.orgs?.size()}"/>

        <h3 class="ui left aligned icon header">
            <g:link action="evaluationConfigsInfo" id="${surveyInfo?.id}"
                    params="[surveyConfigID: surveyConfig?.id]">${message(code: 'survey.label')} ${message(code: 'surveyParticipants.label')}</g:link>
            <semui:totalNumber total="${surveyParticipants}"/>
        </h3>

        <br>
        <g:set var="sumOldCostItem" value="${0.0}"/>
        <g:set var="sumNewCostItem" value="${0.0}"/>
        <g:set var="sumSurveyCostItem" value="${0.0}"/>
                    <table class="ui celled sortable table la-table" id="parentSubscription">
                        <thead>
                        <tr>
                            <th>
                                <g:checkBox name="costItemsToggler" id="costItemsToggler" checked="false"/>
                            </th>
                            <th>${message(code: 'sidewide.number')}</th>
                            <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                            <th>${message(code: 'copySurveyCostItems.oldCostItem')}</th>
                            <th>${message(code: 'copySurveyCostItems.surveyCostItem')}</th>
                            <th>${message(code: 'copySurveyCostItems.newCostItem')}</th>
                            <th></th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${participantsList}" var="participant" status="i">
                            <g:set var="costElement" value="${RefdataValue.getByValueAndCategory('price: consortial price', 'CostItemElement')}"/>
                            <g:set var="surveyOrg" value="${SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, participant)}"/>
                            <g:set var="surveyCostItem" value="${surveyOrg ? com.k_int.kbplus.CostItem.findBySurveyOrg(surveyOrg) : null}"/>

                                <g:set var="newSub" value="${(participant in parentSuccessortParticipantsList) && !(participant in parentParticipantsList) ? 'positive' : null}"/>
                                <g:set var="terminatedSub" value="${!(participant in parentSuccessortParticipantsList) && (participant in parentParticipantsList) ? 'negative' : null}"/>
                                <g:set var="participantSub" value="${parentSubscription?.getDerivedSubscriptionBySubscribers(participant)}"/>
                                <g:set var="participantSuccessorSub" value="${parentSuccessorSubscription?.getDerivedSubscriptionBySubscribers(participant)}"/>

                                <tr class=" ${terminatedSub ?: (newSub ?: '')}">
                                    <td>
                                    <g:if test="${!terminatedSub && surveyCostItem}">
                                        <g:checkBox name="selectedSurveyCostItem" value="${surveyCostItem.id}" checked="false"/>
                                    </g:if>
                                </td>
                                    <td>${i + 1} </td>
                                    <td class="titleCell">
                                        <g:if test="${participantSuccessorSub && participantSuccessorSub.isMultiYear}">
                                            <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                                                  data-content="${message(code: 'subscription.isMultiYear.consortial.label')}">
                                                <i class="map orange icon"></i>
                                            </span>
                                        </g:if>
                                        <g:link controller="myInstitution" action="manageParticipantSurveys"
                                                id="${participant.id}">
                                            ${participant?.sortname}
                                        </g:link>
                                        <br>
                                        <g:link controller="organisation" action="show"
                                                id="${participant.id}">(${fieldValue(bean: participant, field: "name")})</g:link>
                                        <g:if test="${participantSuccessorSub}">
                                            <div class="la-icon-list">
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSuccessorSub.startDate}"/>
                                                -
                                                <g:formatDate formatName="default.date.format.notime"
                                                              date="${participantSuccessorSub.endDate}"/>
                                                <div class="right aligned wide column">
                                                    <b>${participantSuccessorSub.status.getI10n('value')}</b>
                                                </div>
                                            </div>
                                        </g:if>

                                    </td>
                                    <td>
                                        <g:if test="${participantSub}">
                                            <g:each in="${com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElement(participantSub, institution, costElement)}"
                                                    var="costItemParticipantSub">

                                                    ${costItemParticipantSub.costItemElement?.getI10n('value')}<br>
                                                    <b><g:formatNumber number="${costItemParticipantSub.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                                                       maxFractionDigits="2" type="number"/></b>

                                                    (<g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}" minFractionDigits="2"
                                                                     maxFractionDigits="2" type="number"/>)

                                                    ${(costItemParticipantSub?.billingCurrency?.getI10n('value')?.split('-')).first()}
                                                <g:set var="sumOldCostItem" value="${sumOldCostItem + costItemParticipantSub.costInBillingCurrencyAfterTax}"/>
                                            </g:each>
                                        </g:if>
                                    </td>

                                    <td>

                                        <g:if test="${surveyCostItem}">
                                            ${surveyCostItem.costItemElement?.getI10n('value')}<br>
                                        <b><g:formatNumber number="${surveyCostItem.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                                           maxFractionDigits="2" type="number"/></b>

                                        (<g:formatNumber number="${surveyCostItem.costInBillingCurrency}" minFractionDigits="2"
                                                         maxFractionDigits="2" type="number"/>)

                                        ${(surveyCostItem?.billingCurrency?.getI10n('value')?.split('-')).first()}

                                            <g:set var="sumSurveyCostItem" value="${sumSurveyCostItem + surveyCostItem.costInBillingCurrencyAfterTax}"/>
                                        </g:if>
                                    </td>

                                    <td>
                                        <g:if test="${participantSuccessorSub}">
                                            <g:each in="${com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElement(participantSuccessorSub, institution, costElement)}"
                                                    var="costItemParticipantSuccessorSub">

                                                    ${costItemParticipantSuccessorSub.costItemElement?.getI10n('value')}<br>
                                                    <b><g:formatNumber number="${costItemParticipantSuccessorSub?.costInBillingCurrencyAfterTax}" minFractionDigits="2"
                                                                       maxFractionDigits="2" type="number"/></b>

                                                    (<g:formatNumber number="${costItemParticipantSuccessorSub?.costInBillingCurrency}" minFractionDigits="2"
                                                                     maxFractionDigits="2" type="number"/>)

                                                    ${(costItemParticipantSuccessorSub?.billingCurrency?.getI10n('value')?.split('-')).first()}
                                                <g:set var="sumNewCostItem" value="${sumNewCostItem + costItemParticipantSuccessorSub?.costInBillingCurrencyAfterTax}"/>
                                            </g:each>
                                        </g:if>
                                    </td>
                                    <td>
                                        <g:if test="${participantSuccessorSub}">
                                            <g:link controller="subscription" action="show" id="${participantSuccessorSub.id}"
                                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                                        </g:if>
                                    </td>
                                </tr>
                        </g:each>
                        </tbody>
                        <tfoot>
                        <tr>
                            <td></td>
                            <td><b><g:message code="financials.export.sums"/></b> </td>
                            <td></td>
                            <td>
                                <g:formatNumber number="${sumOldCostItem}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/>
                            </td>
                            <td>
                                <g:formatNumber number="${sumSurveyCostItem}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/>
                            </td>
                            <td>
                                <g:formatNumber number="${sumNewCostItem}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/>
                            </td>
                            <td></td>
                        </tr>
                        </tfoot>
                    </table>


        <button class="ui button" type="submit">Umfrage Kosten übertragen</button>
    </g:form>
</semui:form>
<script language="JavaScript">
    $('#costItemsToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', true)
        }
        else {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', false)
        }
    })
</script>
</body>
</html>
