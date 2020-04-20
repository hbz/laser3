<%@ page import="de.laser.helper.RDStore; com.k_int.properties.PropertyDefinition;com.k_int.kbplus.RefdataCategory;com.k_int.kbplus.RefdataValue;com.k_int.kbplus.Org;com.k_int.kbplus.SurveyOrg" %>
<laser:serviceInjection/>

<g:set var="surveyService" bean="surveyService"/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} :  ${message(code: 'surveyInfo.copySurveyCostItems')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}" text="${surveyInfo.name}"/>
    </g:if>
    <semui:crumb message="surveyInfo.copySurveyCostItems" class="active"/>
</semui:breadcrumbs>

%{--<semui:controlButtons>
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="survey" action="setCompleted"
                                   params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                   message="surveyInfo.completed.action"/>
    </semui:actionsDropdown>
</semui:controlButtons>--}%

<br>

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
    <div class="ui grid">
        <div class="row">
            <div class="sixteen wide column">
                <h3 class="ui header center aligned">
                    <g:if test="${parentSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSubscription?.id}">${parentSubscription?.dropdownNamingConvention()}</g:link>
                        <br>
                        <g:link controller="subscription" action="members"
                                id="${parentSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSubscription.getDerivedSubscribers().size() ?: 0}"/>
                    </g:if>
                </h3>
            </div>
        </div>
    </div>
</semui:form>

<semui:form>

    <g:form action="proccessCopySurveyCostItemsToSub" controller="survey" id="${surveyInfo?.id}"
            params="[surveyConfigID: surveyConfig?.id]"
            method="post" class="ui form ">

        <g:set var="sumNewCostItem" value="${0.0}"/>
        <g:set var="sumSurveyCostItem" value="${0.0}"/>
        <g:set var="sumNewCostItemAfterTax" value="${0.0}"/>
        <g:set var="sumSurveyCostItemAfterTax" value="${0.0}"/>
        <table class="ui celled sortable table la-table" id="parentSubscription">
            <thead>
            <tr>
                <th>
                    <g:checkBox name="costItemsToggler" id="costItemsToggler" checked="false"/>
                </th>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                <th>${message(code: 'copySurveyCostItems.surveyCostItem')}
                    <g:if test="${surveyConfig.comment}">
                        <span class="la-long-tooltip la-popup-tooltip la-delay" data-position="bottom center"
                              data-content="${surveyConfig.comment}">
                            <i class="question circle icon"></i>
                        </span>
                    </g:if>
                </th>
                <th>${message(code: 'copySurveyCostItems.newCostItem')}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${participantsList}" var="participant" status="i">
                <g:set var="costElement"
                       value="${RefdataValue.getByValueAndCategory('price: consortial price', de.laser.helper.RDConstants.COST_ITEM_ELEMENT)}"/>

                <tr class="">
                    <td>
                        <g:if test="${participant.surveyCostItem && !com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, institution, costElement, RDStore.COST_ITEM_DELETED)}">
                            <g:checkBox name="selectedSurveyCostItem" value="${participant.surveyCostItem.id}" checked="false"/>
                        </g:if>
                    </td>
                    <td>${i + 1}</td>
                    <td class="titleCell">
                        <g:if test="${participant.newSub && participant.newSub.isMultiYear}">
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
                                id="${participant.id}">(${participant.name})</g:link>
                        <g:if test="${participant.newSub}">
                            <div class="la-icon-list">
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${participant.newSub.startDate}"/>
                                -
                                <g:formatDate formatName="default.date.format.notime"
                                              date="${participant.newSub.endDate}"/>
                                <div class="right aligned wide column">
                                    <b>${participant.newSub.status.getI10n('value')}</b>
                                </div>
                            </div>
                        </g:if>

                    </td>

                    <td>

                        <g:if test="${participant.surveyCostItem}">
                            ${participant.surveyCostItem.costItemElement?.getI10n('value')}<br>
                            <b><g:formatNumber number="${participant.surveyCostItem.costInBillingCurrencyAfterTax}"
                                               minFractionDigits="2"
                                               maxFractionDigits="2" type="number"/></b>

                            (<g:formatNumber number="${participant.surveyCostItem.costInBillingCurrency}" minFractionDigits="2"
                                             maxFractionDigits="2" type="number"/>)

                            ${(participant.surveyCostItem?.billingCurrency?.getI10n('value')?.split('-')).first()}

                            <g:set var="sumSurveyCostItem"
                                   value="${sumSurveyCostItem + participant.surveyCostItem.costInBillingCurrency?:0}"/>
                            <g:set var="sumSurveyCostItemAfterTax"
                                   value="${sumSurveyCostItemAfterTax + participant.surveyCostItem.costInBillingCurrencyAfterTax?:0}"/>


                        </g:if>
                    </td>

                    <td>
                        <g:if test="${participant.newSub}">
                            <g:each in="${com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, institution, costElement, RDStore.COST_ITEM_DELETED)}"
                                    var="costItemParticipantSuccessorSub">

                                ${costItemParticipantSuccessorSub.costItemElement?.getI10n('value')}<br>
                                <b><g:formatNumber
                                        number="${costItemParticipantSuccessorSub?.costInBillingCurrencyAfterTax}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/></b>

                                (<g:formatNumber number="${costItemParticipantSuccessorSub?.costInBillingCurrency}"
                                                 minFractionDigits="2"
                                                 maxFractionDigits="2" type="number"/>)

                                ${(costItemParticipantSuccessorSub?.billingCurrency?.getI10n('value')?.split('-')).first()}
                                <g:set var="sumNewCostItem"
                                       value="${sumNewCostItem + costItemParticipantSuccessorSub?.costInBillingCurrency?:0}"/>
                                <g:set var="sumNewCostItemAfterTax"
                                       value="${sumNewCostItemAfterTax + costItemParticipantSuccessorSub?.costInBillingCurrencyAfterTax?:0}"/>


                            </g:each>
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
            <tfoot>
            <tr>
                <td></td>
                <td><b><g:message code="financials.export.sums"/></b></td>
                <td></td>
                <td>
                    <b><g:formatNumber number="${sumSurveyCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></b>
                    (<g:formatNumber number="${sumSurveyCostItem}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)

                </td>
                <td>
                    <b><g:formatNumber number="${sumNewCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></b>
                    (<g:formatNumber number="${sumNewCostItem}" minFractionDigits="2"
                                    maxFractionDigits="2" type="number"/>)


                </td>
                <td></td>
            </tr>
            </tfoot>
        </table>

        <div class="two fields">
            <div class="eight wide field" style="text-align: left;">

            </div>

            <div class="eight wide field" style="text-align: right;">
                <button class="ui button positive" type="submit">${message(code: 'copySurveyCostItems.copyCostItems')}</button>
                <br>
                <br>
                <button class="ui button positive" name="isVisibleForSubscriber" value="true" type="submit">${message(code: 'copySurveyCostItems.copyCostItems')} (${message(code:'financials.isVisibleForSubscriber')})</button>
            </div>
        </div>
    </g:form>
</semui:form>

<script language="JavaScript">
    $('#costItemsToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', false)
        }
    })
</script>
</body>
</html>
