<%@ page import="de.laser.ui.Btn; de.laser.ui.Icon; de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem; de.laser.storage.RDConstants;" %>
<laser:htmlStart message="surveyCostItemsPackages.label" />

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

<g:if test="${!(surveyInfo.status in [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY])}">
    <div class="ui segment">
        <strong>${message(code: 'survey.notStarted ')}</strong>
    </div>
</g:if>
<g:else>

    <g:render template="multiYearsSubs"/>

    <g:render template="navCompareMembers"/>

   %{-- <h2 class="ui header">
        ${message(code: 'surveyCostItemsPackages.label')}
    </h2>--}%

    <g:render template="costItemsByCostItemElementAndPkgTable"/>


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

        <g:form action="proccessCopySurveyCostItemPackage" controller="survey" id="${surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]"
                method="post" class="ui form">

            <g:set var="sumOldCostItem" value="${0.0}"/>
            <g:set var="sumNewCostItem" value="${0.0}"/>
            <g:set var="sumSurveyCostItem" value="${0.0}"/>
            <g:set var="sumOldCostItemAfterTax" value="${0.0}"/>
            <g:set var="sumNewCostItemAfterTax" value="${0.0}"/>
            <g:set var="sumSurveyCostItemAfterTax" value="${0.0}"/>
            <g:set var="oldCostItem" value="${0.0}"/>
            <g:set var="oldCostItemAfterTax" value="${0.0}"/>
            <table class="ui celled sortable table la-js-responsive-table la-table" id="parentSubscription">
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
                    <g:set var="oldCostItem" value="${0.0}"/>
                    <g:set var="oldCostItemAfterTax" value="${0.0}"/>

                    <tr class="">
                        <td>
                            <g:if test="${participant.surveyCostItem && !CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNotNull(participant.newSub, contextService.getOrg(), selectedCostItemElement, RDStore.COST_ITEM_DELETED)}">
                                <g:checkBox name="selectedSurveyCostItemPackage" value="${participant.surveyCostItem.id}"
                                            checked="false"/>
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
                            <g:if test="${participant.oldSub}">
                                <table class="ui very basic compact table">
                                    <tbody>
                                    <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkg(participant.oldSub, contextService.getOrg(), selectedCostItemElement, RDStore.COST_ITEM_DELETED, pkg)}"
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

                                        </tr>
                                        <g:set var="sumOldCostItem"
                                               value="${sumOldCostItem + costItemParticipantSub.costInBillingCurrency ?: 0}"/>
                                        <g:set var="sumOldCostItemAfterTax"
                                               value="${sumOldCostItemAfterTax + costItemParticipantSub.costInBillingCurrencyAfterTax ?: 0}"/>

                                        <g:set var="oldCostItem"
                                               value="${oldCostItem + (costItemParticipantSub.costInBillingCurrency ?: 0.0)}"/>
                                        <g:set var="oldCostItemAfterTax"
                                               value="${oldCostItemAfterTax + (costItemParticipantSub.costInBillingCurrencyAfterTax ?: 0.0)}"/>
                                    </g:each>
                                    </tbody>
                                </table>
                            </g:if>
                        </td>

                        <td>

                            <g:if test="${participant.surveyCostItem}">
                                <table class="ui very basic compact table">
                                    <tbody>
                                    <tr>
                                        <td>
                                            <strong><g:formatNumber
                                                    number="${participant.surveyCostItem.costInBillingCurrencyAfterTax}"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" type="number"/></strong>

                                            (<g:formatNumber number="${participant.surveyCostItem.costInBillingCurrency}"
                                                             minFractionDigits="2"
                                                             maxFractionDigits="2" type="number"/>)

                                        </td>
                                        <td>
                                            ${participant.surveyCostItem.billingCurrency?.getI10n('value')}
                                        </td>
                                        <g:set var="sumSurveyCostItem"
                                               value="${sumSurveyCostItem + participant.surveyCostItem.costInBillingCurrency ?: 0}"/>
                                        <g:set var="sumSurveyCostItemAfterTax"
                                               value="${sumSurveyCostItemAfterTax + participant.surveyCostItem.costInBillingCurrencyAfterTax ?: 0}"/>
                                        <td>
                                            <g:if test="${oldCostItem || oldCostItemAfterTax}">
                                                <strong><g:formatNumber
                                                        number="${((participant.surveyCostItem.costInBillingCurrencyAfterTax - oldCostItemAfterTax) / oldCostItemAfterTax) * 100}"
                                                        minFractionDigits="2"
                                                        maxFractionDigits="2" type="number"/>%</strong>

                                                (<g:formatNumber
                                                    number="${((participant.surveyCostItem.costInBillingCurrency - oldCostItem) / oldCostItem) * 100}"
                                                    minFractionDigits="2"
                                                    maxFractionDigits="2" type="number"/>%)
                                            </g:if>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </g:if>
                        </td>

                        <td>
                            <g:if test="${participant.newSub}">
                                <table class="ui very basic compact table">
                                    <tbody>
                                    <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqualAndPkg(participant.newSub, contextService.getOrg(), selectedCostItemElement, RDStore.COST_ITEM_DELETED, pkg)}"
                                            var="costItemParticipantSuccessorSub">

                                        <tr>
                                            <td>
                                                <strong><g:formatNumber
                                                        number="${costItemParticipantSuccessorSub.costInBillingCurrencyAfterTax}"
                                                        minFractionDigits="2"
                                                        maxFractionDigits="2" type="number"/></strong>

                                                (<g:formatNumber number="${costItemParticipantSuccessorSub.costInBillingCurrency}"
                                                                 minFractionDigits="2"
                                                                 maxFractionDigits="2" type="number"/>)
                                            </td>
                                            <td>
                                                ${costItemParticipantSuccessorSub.billingCurrency?.getI10n('value')}
                                            </td>
                                            <td>
                                                <g:set var="sumNewCostItem"
                                                       value="${sumNewCostItem + costItemParticipantSuccessorSub.costInBillingCurrency ?: 0}"/>
                                                <g:set var="sumNewCostItemAfterTax"
                                                       value="${sumNewCostItemAfterTax + costItemParticipantSuccessorSub.costInBillingCurrencyAfterTax ?: 0}"/>

                                                <g:if test="${oldCostItem || oldCostItemAfterTax}">
                                                    <strong><g:formatNumber
                                                            number="${((costItemParticipantSuccessorSub.costInBillingCurrencyAfterTax - oldCostItemAfterTax) / oldCostItemAfterTax) * 100}"
                                                            minFractionDigits="2"
                                                            maxFractionDigits="2" type="number"/>%</strong>

                                                    (<g:formatNumber
                                                        number="${((costItemParticipantSuccessorSub.costInBillingCurrency - oldCostItem) / oldCostItem) * 100}"
                                                        minFractionDigits="2"
                                                        maxFractionDigits="2" type="number"/>%)
                                                </g:if>
                                            </td>
                                        </tr>
                                    </g:each>
                                    </tbody>
                                </table>
                            </g:if>
                        </td>
                        <td>
                            <g:if test="${participant.newSub}">
                                <g:link mapping="subfinance" controller="finance" action="index"
                                        params="${[sub: participant.newSub.id]}"
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
                <tfoot>
                <tr>
                    <td></td>
                    <td><strong><g:message code="financials.export.sums"/></strong></td>
                    <td></td>
                    <td>
                        <strong><g:formatNumber number="${sumOldCostItemAfterTax}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/></strong>
                        (<g:formatNumber number="${sumOldCostItem}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)
                    </td>
                    <td>
                        <strong><g:formatNumber number="${sumSurveyCostItemAfterTax}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/></strong>
                        (<g:formatNumber number="${sumSurveyCostItem}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        <g:if test="${sumOldCostItemAfterTax || sumOldCostItem}">
                            <strong><g:formatNumber
                                number="${((sumSurveyCostItemAfterTax - sumOldCostItemAfterTax) / sumOldCostItemAfterTax) * 100}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>%</strong>

                            (<g:formatNumber number="${((sumSurveyCostItem - sumOldCostItem) / sumOldCostItem) * 100}"
                                             minFractionDigits="2"
                                             maxFractionDigits="2" type="number"/>%)
                        </g:if>
                    </td>
                    <td>
                        <strong><g:formatNumber number="${sumNewCostItemAfterTax}" minFractionDigits="2"
                                                maxFractionDigits="2" type="number"/></strong>
                        (<g:formatNumber number="${sumNewCostItem}" minFractionDigits="2"
                                         maxFractionDigits="2" type="number"/>)

                        <g:if test="${sumOldCostItemAfterTax || sumOldCostItem}">
                            <strong><g:formatNumber
                                number="${((sumNewCostItemAfterTax - sumOldCostItemAfterTax) / sumOldCostItemAfterTax) * 100}"
                                minFractionDigits="2"
                                maxFractionDigits="2" type="number"/>%</strong>

                            (<g:formatNumber
                                number="${((sumNewCostItem - sumOldCostItem) / sumOldCostItem) * 100}"
                                minFractionDigits="2"
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
                    <button class="${Btn.POSITIVE}"
                            type="submit">${message(code: 'copySurveyCostItems.copyCostItems')}</button>
                    <br/>
                    <br/>
                    <button class="${Btn.POSITIVE}" name="isVisibleForSubscriber" value="true"
                            type="submit">${message(code: 'copySurveyCostItems.copyCostItems')} (${message(code: 'financials.isVisibleForSubscriber')})</button>
                </div>
            </div>
        </g:form>
    </ui:greySegment>
    <laser:script file="${this.getGroovyPageFileName()}">
        $('#costItemsToggler').click(function () {
            if ($(this).prop('checked')) {
                $("tr[class!=disabled] input[name=selectedSurveyCostItemPackage]").prop('checked', true)
            } else {
                $("tr[class!=disabled] input[name=selectedSurveyCostItemPackage]").prop('checked', false)
            }
        })
    </laser:script>

    <g:form action="setStatus" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID, newStatus: 'setSurveyCompleted']">

        <div class="ui right floated compact segment">
            <div class="ui checkbox">
                <input type="checkbox" onchange="this.form.submit()"
                       name="surveyCompleted" ${surveyInfo.status.id == RDStore.SURVEY_COMPLETED.id ? 'checked' : ''}>
                <label><g:message code="surveyInfo.status.completed"/></label>
            </div>
        </div>

    </g:form>
</g:else>


<laser:script file="${this.getGroovyPageFileName()}">
    $('#selectedCostItemElementID').on('change', function() {
        var selectedCostItemElementID = $(this).val()
        var url = "<g:createLink controller="survey" action="$actionName" params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID]}"/>&selectedCostItemElementID="+selectedCostItemElementID;
            location.href = url;
         });
</laser:script>
<laser:htmlEnd />
