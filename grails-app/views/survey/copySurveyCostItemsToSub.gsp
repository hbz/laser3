<%@ page import="de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code: 'laser')} : ${message(code: 'surveyInfo.copySurveyCostItems')}</title>

</head>

<body>

<semui:breadcrumbs>
    <semui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <semui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
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

<semui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey">
<semui:surveyStatus object="${surveyInfo}"/>
</semui:h1HeaderWithIcon>

<semui:objectStatus object="${surveyInfo}" status="${surveyInfo.status}"/>

<semui:messages data="${flash}"/>

<h2 class="ui header">
    ${message(code: 'surveyInfo.copySurveyCostItems')}
</h2>


<semui:form>
    <div class="ui grid">
        <div class="row">
            <div class="sixteen wide column">
                <h3 class="ui header center aligned">
                    <g:if test="${parentSubscription}">
                        <g:link controller="subscription" action="show"
                                id="${parentSubscription.id}">${parentSubscription.dropdownNamingConvention()}</g:link>
                        <br />
                        <g:link controller="subscription" action="members"
                                id="${parentSubscription.id}">${message(code: 'renewalEvaluation.orgsInSub')}</g:link>
                        <semui:totalNumber total="${parentSubscription.getDerivedSubscribers().size()}"/>
                    </g:if>
                </h3>
            </div>
        </div>
    </div>
</semui:form>

<semui:form>

    <g:form action="proccessCopySurveyCostItemsToSub" controller="survey" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]"
            method="post" class="ui form ">

        <g:set var="sumNewCostItem" value="${0.0}"/>
        <g:set var="sumSurveyCostItem" value="${0.0}"/>
        <g:set var="sumNewCostItemAfterTax" value="${0.0}"/>
        <g:set var="sumSurveyCostItemAfterTax" value="${0.0}"/>
        <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
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
                       value="${RefdataValue.getByValueAndCategory('price: consortial price', de.laser.storage.RDConstants.COST_ITEM_ELEMENT)}"/>

                <g:if test="${participant.surveyCostItem}">
                    <g:set var="costElement"
                           value="${participant.surveyCostItem.costItemElement}"/>
                </g:if>

                <tr class="">
                    <td>
                        <g:if test="${participant.surveyCostItem && !CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, institution, costElement, RDStore.COST_ITEM_DELETED)}">
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
                            ${participant.sortname}
                        </g:link>
                        <br />
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

                        <g:if test="${participant.surveyCostItem}">
                            ${participant.surveyCostItem.costItemElement?.getI10n('value')}<br />
                            <strong><g:formatNumber number="${participant.surveyCostItem.costInBillingCurrencyAfterTax}"
                                               minFractionDigits="2"
                                               maxFractionDigits="2" type="number"/></strong>

                            (<g:formatNumber number="${participant.surveyCostItem.costInBillingCurrency}" minFractionDigits="2"
                                             maxFractionDigits="2" type="number"/>)

                            ${(participant.surveyCostItem.billingCurrency?.getI10n('value')?.split('-')).first()}

                            <g:set var="sumSurveyCostItem"
                                   value="${sumSurveyCostItem + participant.surveyCostItem.costInBillingCurrency?:0}"/>
                            <g:set var="sumSurveyCostItemAfterTax"
                                   value="${sumSurveyCostItemAfterTax + participant.surveyCostItem.costInBillingCurrencyAfterTax?:0}"/>


                        </g:if>
                    </td>

                    <td>
                        <g:if test="${participant.newSub}">
                            <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, institution, costElement, RDStore.COST_ITEM_DELETED)}"
                                    var="costItemParticipantSub">

                                ${costItemParticipantSub.costItemElement?.getI10n('value')}<br />
                                <strong><g:formatNumber
                                        number="${costItemParticipantSub.costInBillingCurrencyAfterTax}"
                                        minFractionDigits="2"
                                        maxFractionDigits="2" type="number"/></strong>

                                (<g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}"
                                                 minFractionDigits="2"
                                                 maxFractionDigits="2" type="number"/>)

                                ${(costItemParticipantSub.billingCurrency?.getI10n('value')?.split('-')).first()}
                                <g:set var="sumNewCostItem"
                                       value="${sumNewCostItem + costItemParticipantSub.costInBillingCurrency?:0}"/>
                                <g:set var="sumNewCostItemAfterTax"
                                       value="${sumNewCostItemAfterTax + costItemParticipantSub.costInBillingCurrencyAfterTax?:0}"/>


                            </g:each>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${participant.newSub}">
                            <g:link mapping="subfinance" controller="finance" action="index" params="${[sub:participant.newSub.id]}"
                                    class="ui button icon"><i class="icon clipboard"></i></g:link>
                        </g:if>
                    </td>
                </tr>
            </g:each>
            </tbody>
            <tfoot>
            <tr>
                <td></td>
                <td><strong><g:message code="financials.export.sums"/></strong></td>
                <td></td>
                <td>
                    <strong><g:formatNumber number="${sumSurveyCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></strong>
                    (<g:formatNumber number="${sumSurveyCostItem}" minFractionDigits="2"
                                     maxFractionDigits="2" type="number"/>)

                </td>
                <td>
                    <strong><g:formatNumber number="${sumNewCostItemAfterTax}" minFractionDigits="2"
                                       maxFractionDigits="2" type="number"/></strong>
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
                <br />
                <br />
                <button class="ui button positive" name="isVisibleForSubscriber" value="true" type="submit">${message(code: 'copySurveyCostItems.copyCostItems')} (${message(code:'financials.isVisibleForSubscriber')})</button>
            </div>
        </div>
    </g:form>
</semui:form>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#costItemsToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', false)
        }
    })
</laser:script>
</body>
</html>
