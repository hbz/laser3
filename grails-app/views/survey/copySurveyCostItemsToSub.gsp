<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.storage.RDConstants; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem" %>
<laser:htmlStart message="surveyInfo.copySurveyCostItems" />

<ui:breadcrumbs>
    <ui:crumb controller="survey" action="workflowsSurveysConsortia" text="${message(code: 'menu.my.surveys')}"/>

    <g:if test="${surveyInfo}">
        <ui:crumb controller="survey" action="show" id="${surveyInfo.id}"
                     params="[surveyConfigID: surveyConfig.id]" text="${surveyInfo.name}"/>
    </g:if>
    <ui:crumb message="surveyInfo.copySurveyCostItems" class="active"/>
</ui:breadcrumbs>

%{--<ui:controlButtons>
    <ui:actionsDropdown>
        <ui:actionsDropdownItem controller="survey" action="setStatus" params="[id: params.id, newStatus: 'setCompleted']"
                                   message="surveyInfo.completed.action"/>
    </ui:actionsDropdown>
</ui:controlButtons>--}%

<ui:h1HeaderWithIcon text="${surveyInfo.name}" type="Survey">
    <uiSurvey:status object="${surveyInfo}"/>
</ui:h1HeaderWithIcon>

<g:if test="${surveyConfig.subscription}">
 <ui:buttonWithIcon style="vertical-align: super;" message="${message(code: 'button.message.showLicense')}" variation="tiny" icon="${Icon.SUBSCRIPTION}" href="${createLink(action: 'show', controller: 'subscription', id: surveyConfig.subscription.id)}"/>
</g:if>

<ui:objectStatus object="${surveyInfo}" />

<ui:messages data="${flash}"/>

<h2 class="ui header">
    ${message(code: 'surveyInfo.copySurveyCostItems')}
</h2>

<g:render template="costItemsByCostItemElementTable" model="${[costItemsByCTE: costItemsByCostItemElement, header: g.message(code: 'costItem.label')+' in '+ g.message(code: 'survey.label')]}"/>


<ui:greySegment>
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
                        <ui:totalNumber total="${parentSubscription.getDerivedNonHiddenSubscribers().size()}"/>
                    </g:if>
                </h3>
            </div>
        </div>
    </div>
</ui:greySegment>

<ui:greySegment>

    <g:form action="proccessCopySurveyCostItemsToSub" controller="survey" id="${surveyInfo.id}"
            params="[surveyConfigID: surveyConfig.id]"
            method="post" class="ui form">

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
                    %{--<br>
                    <g:set var="costItemElements"
                           value="${costItemsByCostItemElement.collect {RefdataValue.findByValueAndOwner(it.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))}}"/>

                    <ui:select name="selectedCostItemElementID"
                               from="${costItemElements}"
                               optionKey="id"
                               optionValue="value"
                               value="${selectedCostItemElementID}"
                               class="ui dropdown clearable"
                               id="selectedCostItemElementID"/>--}%
                </th>
                <th>${message(code: 'copySurveyCostItems.newCostItem')}</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${participantsList}" var="participant" status="i">

                <tr class="">
                    <td>
                        <g:if test="${participant.surveyCostItem && !CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNull(participant.newSub, contextService.getOrg(), selectedCostItemElement, RDStore.COST_ITEM_DELETED)}">
                            <g:checkBox name="selectedSurveyCostItem" value="${participant.surveyCostItem.id}" checked="false"/>
                        </g:if>
                    </td>
                    <td>${i + 1}</td>
                    <td class="titleCell">
                        <g:if test="${participant.newSub && participant.newSub.isMultiYear}">
                            <ui:multiYearIcon isConsortial="true" color="orange" />
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
                            <table class="ui very basic compact table">
                                <tbody><tr>
                                    <td>
                                        <strong><g:formatNumber number="${participant.surveyCostItem.costInBillingCurrencyAfterTax}"
                                                                minFractionDigits="2"
                                                                maxFractionDigits="2" type="number"/></strong>

                                        (<g:formatNumber number="${participant.surveyCostItem.costInBillingCurrency}" minFractionDigits="2"
                                                         maxFractionDigits="2" type="number"/>)
                                    </td>
                                    <td>
                                        ${participant.surveyCostItem.billingCurrency?.getI10n('value')}
                                    </td>
                                    <g:set var="sumSurveyCostItem"
                                           value="${sumSurveyCostItem + participant.surveyCostItem.costInBillingCurrency ?: 0}"/>
                                    <g:set var="sumSurveyCostItemAfterTax"
                                           value="${sumSurveyCostItemAfterTax + participant.surveyCostItem.costInBillingCurrencyAfterTax ?: 0}"/>
                                </tr>
                                </tbody>
                            </table>
                        </g:if>
                    </td>

                    <td>
                        <g:if test="${participant.newSub}">
                            <table class="ui very basic compact table">
                                <tbody>
                                <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, contextService.getOrg(), selectedCostItemElement, RDStore.COST_ITEM_DELETED)}"
                                        var="costItemParticipantSub">
                                    <tr>
                                        <td>
                                            <strong><g:formatNumber
                                                    number="${costItemParticipantSub.costInBillingCurrencyAfterTax}"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" type="number"/></strong>

                                            (<g:formatNumber number="${costItemParticipantSub.costInBillingCurrency}"
                                                             minFractionDigits="2"
                                                             maxFractionDigits="2" type="number"/>)
                                        </td>
                                        <td>
                                            ${costItemParticipantSub.billingCurrency?.getI10n('value')}
                                        </td>

                                        <g:set var="sumNewCostItem"
                                               value="${sumNewCostItem + costItemParticipantSub.costInBillingCurrency ?: 0}"/>
                                        <g:set var="sumNewCostItemAfterTax"
                                               value="${sumNewCostItemAfterTax + costItemParticipantSub.costInBillingCurrencyAfterTax ?: 0}"/>
                                    </tr>

                                </g:each>
                                </tbody>
                            </table>
                        </g:if>
                    </td>
                    <td>
                        <g:if test="${participant.newSub}">
                            <g:link mapping="subfinance" controller="finance" action="index" params="${[sub:participant.newSub.id]}"
                                    class="${Btn.ICON.SIMPLE}"><i class="${Icon.SUBSCRIPTION}"></i></g:link>
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
                <button class="${Btn.POSITIVE}" type="submit">${message(code: 'copySurveyCostItems.copyCostItems')}</button>
                <br />
                <br />
                <button class="${Btn.POSITIVE}" name="isVisibleForSubscriber" value="true" type="submit">${message(code: 'copySurveyCostItems.copyCostItems')} (${message(code:'financials.isVisibleForSubscriber')})</button>
            </div>
        </div>
    </g:form>
</ui:greySegment>

<laser:script file="${this.getGroovyPageFileName()}">
    $('#costItemsToggler').click(function () {
        if ($(this).prop('checked')) {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', true)
        } else {
            $("tr[class!=disabled] input[name=selectedSurveyCostItem]").prop('checked', false)
        }
    });

        $('#selectedCostItemElementID').on('change', function() {
        var selectedCostItemElementID = $(this).val()
        var url = "<g:createLink controller="survey" action="$actionName" params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID]}"/>&selectedCostItemElementID="+selectedCostItemElementID;
            location.href = url;
         });
</laser:script>
<laser:htmlEnd />
