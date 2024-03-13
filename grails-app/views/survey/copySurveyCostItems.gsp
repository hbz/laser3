<%@ page import="de.laser.RefdataValue; de.laser.storage.RDStore; de.laser.properties.PropertyDefinition;de.laser.RefdataCategory;de.laser.Org;de.laser.survey.SurveyOrg;de.laser.finance.CostItem; de.laser.storage.RDConstants;" %>
<laser:htmlStart message="surveyInfo.copySurveyCostItems" serviceInjection="true" />

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

    <div class="ui tablet stackable steps">

        <div class="${(actionName == 'compareMembersOfTwoSubs') ? 'active' : ''} step">
            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="compareMembersOfTwoSubs"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'surveyInfo.transferMembers')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="exchange icon"></i>${message(code: 'surveyInfo.transferMembers')}
                </div>
            </div>

            <g:if test="${transferWorkflow && transferWorkflow.transferMembers == 'true'}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferMembers: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferMembers: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>

        </div>

        <g:if test="${surveyConfig.subSurveyUseForTransfer}">
            <div class="${(actionName == 'copySubPackagesAndIes') ? 'active' : ''} step">

                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copySubPackagesAndIes"
                                params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]">
                            ${message(code: 'copySubPackagesAndIes.label')}
                        </g:link>
                    </div>

                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'copySubPackagesAndIes.label')}
                    </div>
                </div>
            &nbsp;&nbsp;
                <g:if test="${transferWorkflow && transferWorkflow.transferSubPackagesAndIes == 'true'}">
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSubPackagesAndIes: false]">
                        <i class="check bordered large green icon"></i>
                    </g:link>
                </g:if>
                <g:else>
                    <g:link controller="survey" action="setSurveyTransferConfig"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSubPackagesAndIes: true]">
                        <i class="close bordered large red icon"></i>
                    </g:link>
                </g:else>

            </div>
        </g:if>

        <div class="${(actionName == 'copyProperties' && params.tab == 'surveyProperties') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copyProperties"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'copyProperties.surveyProperties.short')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="tags icon"></i>${message(code: 'properties')}
                </div>
            </div>

            <g:if test="${transferWorkflow && transferWorkflow.transferSurveyProperties == 'true'}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyProperties: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyProperties: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>
        </div>

        <div class="${(actionName == 'copyProperties' && params.tab == 'customProperties') ? 'active' : ''}  step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copyProperties"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'customProperties', targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'copyProperties.customProperties.short')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="tags icon"></i>${message(code: 'properties')}
                </div>
            </div>

            <g:if test="${transferWorkflow && transferWorkflow.transferCustomProperties == 'true'}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferCustomProperties: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferCustomProperties: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>

        </div>

        <div class="${(actionName == 'copyProperties' && params.tab == 'privateProperties') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copyProperties"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, tab: 'privateProperties', targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'copyProperties.privateProperties.short')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="tags icon"></i>${message(code: 'properties')}
                </div>
            </div>

            <g:if test="${transferWorkflow && transferWorkflow.transferPrivateProperties == 'true'}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferPrivateProperties: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferPrivateProperties: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>

        </div>

        <div class="${(actionName == 'copySurveyCostItems') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySurveyCostItems"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'copySurveyCostItems.surveyCostItems')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="money bill alternate outline icon"></i>${message(code: 'copySurveyCostItems.surveyCostItem')}
                </div>
            </div>

            <g:if test="${transferWorkflow && transferWorkflow.transferSurveyCostItems == 'true'}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyCostItems: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, transferSurveyCostItems: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>

        </div>



    </div>

    <h2 class="ui header">
        ${message(code: 'surveyInfo.copySurveyCostItems')}
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

                        </g:if>
                    </h3>
                </div>
            </div>
        </div>
    </ui:greySegment>

    <ui:greySegment>

        <g:form action="proccessCopySurveyCostItems" controller="survey" id="${surveyInfo.id}"
                params="[surveyConfigID: surveyConfig.id, targetSubscriptionId: targetSubscription?.id]"
                method="post" class="ui form ">

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
                    <th>${message(code: 'copySurveyCostItems.surveyCostItem')}<br>
                        <g:set var="costItemElements"
                               value="${CostItem.executeQuery('from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig) and ct.costItemElement is not null', [status: RDStore.COST_ITEM_DELETED, surveyConfig: surveyConfig]).groupBy {it.costItemElement}.collect {RefdataValue.findByValueAndOwner(it.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))}}"/>

                        <ui:select name="selectedCostItemElement"
                                   from="${costItemElements}"
                                   optionKey="id"
                                   optionValue="value"
                                   value="${selectedCostItemElementID}"
                                   class="ui dropdown"
                                   id="selectedCostItemElement"/>
                    </th>
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
                            <g:if test="${participant.surveyCostItem && !CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, institution, selectedCostItemElement, RDStore.COST_ITEM_DELETED)}">
                                <g:checkBox name="selectedSurveyCostItem" value="${participant.surveyCostItem.id}"
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
                                    <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.oldSub, institution, selectedCostItemElement, RDStore.COST_ITEM_DELETED)}"
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
                                    <g:each in="${CostItem.findAllBySubAndOwnerAndCostItemElementAndCostItemStatusNotEqual(participant.newSub, institution, selectedCostItemElement, RDStore.COST_ITEM_DELETED)}"
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
                    <button class="ui button positive"
                            type="submit">${message(code: 'copySurveyCostItems.copyCostItems')}</button>
                    <br/>
                    <br/>
                    <button class="ui button positive" name="isVisibleForSubscriber" value="true"
                            type="submit">${message(code: 'copySurveyCostItems.copyCostItems')} (${message(code: 'financials.isVisibleForSubscriber')})</button>
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
        })
    </laser:script>

    <g:form action="setSurveyCompleted" method="post" class="ui form"
            params="[id: surveyInfo.id, surveyConfigID: params.surveyConfigID]">

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
    $('#selectedCostItemElement').on('change', function() {
        var selectedCostItemElement = $("#selectedCostItemElement").val()
        var url = "<g:createLink controller="survey" action="$actionName" params="${params + [id: surveyInfo.id, surveyConfigID: params.surveyConfigID]}"/>&selectedCostItemElement="+selectedCostItemElement;
            location.href = url;
         });
</laser:script>
<laser:htmlEnd />
