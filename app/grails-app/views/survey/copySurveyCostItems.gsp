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
        <semui:crumb controller="survey" action="renewalWithSurvey" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" message="surveyInfo.renewalOverView"/>
    </g:if>
    <semui:crumb message="surveyInfo.transferOverView" class="active"/>
</semui:breadcrumbs>

<semui:controlButtons>
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="survey" action="renewalWithSurvey"
                                   params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                   message="surveyInfo.renewalOverView"/>

        <semui:actionsDropdownItem controller="survey" action="setCompleted"
                                   params="[id: params.id, surveyConfigID: surveyConfig.id]"
                                   message="surveyInfo.completed.action"/>
    </semui:actionsDropdown>
</semui:controlButtons>

<h1 class="ui icon header"><semui:headerTitleIcon type="Survey"/>
${surveyInfo?.name}
<semui:surveyStatus object="${surveyInfo}"/>
</h1>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<div class="ui tablet stackable steps">

    <div class="${(actionName == 'compareMembersOfTwoSubs') ? 'active' : ''} step">
        <div class="content">
            <div class="title">
                <g:link controller="survey" action="compareMembersOfTwoSubs"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id]">
                    ${message(code: 'surveyInfo.transferMembers')}
                </g:link>
            </div>

            <div class="description">
                <i class="exchange icon"></i>${message(code: 'surveyInfo.transferMembers')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferMembers == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferMembers: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferMembers: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>


    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'surveyProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'surveyProperties']">
                    ${message(code: 'copyProperties.surveyProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferSurveyProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>
    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'customProperties') ? 'active' : ''}  step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'customProperties']">
                    ${message(code: 'copyProperties.customProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferCustomProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferCustomProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferCustomProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'privateProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, tab: 'privateProperties']">
                    ${message(code: 'copyProperties.privateProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferPrivateProperties == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferPrivateProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferPrivateProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <div class="${(actionName == 'copySurveyCostItems') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copySurveyCostItems"
                        params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id]">
                    ${message(code: 'copySurveyCostItems.surveyCostItems')}
                </g:link>
            </div>

            <div class="description">
                <i class="money bill alternate outline icon"></i>${message(code: 'copySurveyCostItems.surveyCostItem')}
            </div>
        </div>

        <g:if test="${transferWorkflow && transferWorkflow.transferSurveyCostItems == 'true'}">
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyCostItems: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="surveyTransferConfig"
                    params="[id: surveyInfo?.id, surveyConfigID: surveyConfig?.id, transferSurveyCostItems: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

</div>

<h2>
    ${message(code: 'surveyInfo.copySurveyCostItems')}
</h2>


<semui:form>
    <div class="ui grid">

        <div class="row">
            <div class="eight wide column">
                <h3 class="ui header center aligned">

                    <g:message code="renewalWithSurvey.parentSubscription"/>:<br>
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

            <div class="eight wide column">
                <h3 class="ui header center aligned">
                    <g:message code="renewalWithSurvey.parentSuccessorSubscription"/>:<br>
                    <g:if test="${parentSuccessorSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSuccessorSubscription?.id}">${parentSuccessorSubscription?.dropdownNamingConvention()}</g:link>
                        <br>
                        <g:link controller="subscription" action="members"
                                id="${parentSuccessorSubscription?.id}">${message(code: 'renewalWithSurvey.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSuccessorSubscription.getDerivedSubscribers().size() ?: 0}"/>

                    </g:if>
                </h3>
            </div>
        </div>
    </div>
</semui:form>

<semui:form>

    <g:form action="proccessCopySurveyCostItems" controller="survey" id="${surveyInfo?.id}"
            params="[surveyConfigID: surveyConfig?.id]"
            method="post" class="ui form ">

        <g:set var="sumOldCostItem" value="${0.0}"/>
        <g:set var="sumNewCostItem" value="${0.0}"/>
        <g:set var="sumSurveyCostItem" value="${0.0}"/>
        <g:set var="sumOldCostItemAfterTax" value="${0.0}"/>
        <g:set var="sumNewCostItemAfterTax" value="${0.0}"/>
        <g:set var="sumSurveyCostItemAfterTax" value="${0.0}"/>
        <g:set var="OldCostItem" value="${0.0}"/>
        <g:set var="OldCostItemAfterTax" value="${0.0}"/>
        <table class="ui celled sortable table la-table" id="parentSubscription">
            <thead>
            <tr>
                <th>
                    <g:checkBox name="costItemsToggler" id="costItemsToggler" checked="false"/>
                </th>
                <th>${message(code: 'sidewide.number')}</th>
                <th>${message(code: 'subscription.details.consortiaMembers.label')}</th>
                <th>${message(code: 'copySurveyCostItems.oldCostItem')}</th>
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
                       value="${RefdataValue.getByValueAndCategory('price: consortial price', 'CostItemElement')}"/>

                <tr class="">
                    <td>
                        <g:if test="${participant.surveyCostItem && !com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElement(participant.newSub, institution, costElement)}">
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
                        <g:if test="${participant.oldSub}">
                            <g:each in="${com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElement(participant.oldSub, institution, costElement)}"
                                    var="costItemParticipantSub">

                                ${costItemParticipantSub.costItemElement?.getI10n('value')}<br>
                                <b><g:formatNumber number="${costItemParticipantSub.costInBillingCurrencyAfterTax}"
                                                   minFractionDigits="2"
                                                   maxFractionDigits="2" type="number"/></b>

                                (<g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}"
                                                 minFractionDigits="2"
                                                 maxFractionDigits="2" type="number"/>)

                                ${(costItemParticipantSub?.billingCurrency?.getI10n('value')?.split('-')).first()}
                                <g:set var="sumOldCostItem"
                                       value="${sumOldCostItem + costItemParticipantSub.costInBillingCurrency?:0}"/>
                                <g:set var="sumOldCostItemAfterTax"
                                       value="${sumOldCostItemAfterTax + costItemParticipantSub.costInBillingCurrencyAfterTax?:0}"/>

                                <g:set var="OldCostItem" value="${costItemParticipantSub.costInBillingCurrency?:null}"/>
                                <g:set var="OldCostItemAfterTax" value="${costItemParticipantSub.costInBillingCurrencyAfterTax?:null}"/>
                            </g:each>
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

                            <g:if test="${OldCostItem || OldCostItemAfterTax}">
                                <br><b><g:formatNumber number="${((participant.surveyCostItem.costInBillingCurrencyAfterTax-OldCostItemAfterTax)/OldCostItemAfterTax)*100}"
                                                   minFractionDigits="2"
                                                   maxFractionDigits="2" type="number"/>%</b>

                                (<g:formatNumber number="${((participant.surveyCostItem.costInBillingCurrency-OldCostItem)/OldCostItem)*100}" minFractionDigits="2"
                                                 maxFractionDigits="2" type="number"/>%)
                            </g:if>

                        </g:if>
                    </td>

                    <td>
                        <g:if test="${participant.newSub}">
                            <g:each in="${com.k_int.kbplus.CostItem.findAllBySubAndOwnerAndCostItemElement(participant.newSub, institution, costElement)}"
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

                                <g:if test="${OldCostItem || OldCostItemAfterTax}">
                                    <br><b><g:formatNumber number="${((costItemParticipantSuccessorSub?.costInBillingCurrencyAfterTax-OldCostItemAfterTax)/OldCostItemAfterTax)*100}"
                                                           minFractionDigits="2"
                                                           maxFractionDigits="2" type="number"/>%</b>

                                    (<g:formatNumber number="${((costItemParticipantSuccessorSub?.costInBillingCurrency-OldCostItem)/OldCostItem)*100}" minFractionDigits="2"
                                                     maxFractionDigits="2" type="number"/>%)
                                </g:if>
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
                    <b><g:formatNumber number="${sumOldCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></b>
                    (<g:formatNumber number="${sumOldCostItem}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)
                </td>
                <td>
                    <b><g:formatNumber number="${sumSurveyCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></b>
                    (<g:formatNumber number="${sumSurveyCostItem}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)

                    <g:if test="${sumOldCostItemAfterTax || sumOldCostItem}">
                        <br><b><g:formatNumber number="${((sumSurveyCostItemAfterTax-sumOldCostItemAfterTax)/sumOldCostItemAfterTax)*100}"
                                               minFractionDigits="2"
                                               maxFractionDigits="2" type="number"/>%</b>

                        (<g:formatNumber number="${((sumSurveyCostItem-sumOldCostItem)/sumOldCostItem)*100}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>%)
                    </g:if>
                </td>
                <td>
                    <b><g:formatNumber number="${sumNewCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></b>
                    (<g:formatNumber number="${sumNewCostItem}" minFractionDigits="2"
                                    maxFractionDigits="2" type="number"/>)

                    <g:if test="${sumOldCostItemAfterTax || sumOldCostItem}">
                        <br><b><g:formatNumber number="${((sumNewCostItemAfterTax-sumOldCostItemAfterTax)/sumOldCostItemAfterTax)*100}"
                                               minFractionDigits="2"
                                               maxFractionDigits="2" type="number"/>%</b>

                        (<g:formatNumber number="${((sumNewCostItemAfterTax-sumOldCostItem)/sumOldCostItem)*100}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>%)
                    </g:if>
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
