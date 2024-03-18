<%@ page import="de.laser.storage.RDStore; de.laser.finance.CostItem" %>
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

        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferMembers)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferMembers: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferMembers: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <g:if test="${surveyConfig.subSurveyUseForTransfer}">
        <div class="${(actionName == 'copySubPackagesAndIes') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySubPackagesAndIes"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'copySubPackagesAndIes.label')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="gift icon"></i>${message(code: 'copySubPackagesAndIes.label')}
                </div>
            </div>
        &nbsp;&nbsp;
            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSubPackagesAndIes)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSubPackagesAndIes: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSubPackagesAndIes: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>

        </div>
    </g:if>


    <div class="${(actionName == 'copyProperties' && params.tab == 'surveyProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, tab: 'surveyProperties', targetSubscriptionId: targetSubscription?.id]">
                    ${message(code: 'copyProperties.surveyProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyProperties)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>
    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'customProperties') ? 'active' : ''}  step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, tab: 'customProperties', targetSubscriptionId: targetSubscription?.id]">
                    ${message(code: 'copyProperties.customProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferCustomProperties)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferCustomProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferCustomProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'privateProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, tab: 'privateProperties', targetSubscriptionId: targetSubscription?.id]">
                    ${message(code: 'copyProperties.privateProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>
        </div>

        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferPrivateProperties)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferPrivateProperties: false]">
                <i class="check bordered large green icon"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferPrivateProperties: true]">
                <i class="close bordered large red icon"></i>
            </g:link>
        </g:else>

    </div>

    <g:if test="${CostItem.executeQuery('select count(*) from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and costItem.costItemStatus != :status', [survConfig: surveyConfig, status: RDStore.COST_ITEM_DELETED])[0] > 0}">
        <div class="${(actionName == 'copySurveyCostItems') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySurveyCostItems"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, targetSubscriptionId: targetSubscription?.id]">
                        ${message(code: 'copySurveyCostItems.surveyCostItems')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="money bill alternate outline icon"></i>${message(code: 'copySurveyCostItems.surveyCostItem')}
                </div>
            </div>

            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyCostItems)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyCostItems: false]">
                    <i class="check bordered large green icon"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyCostItems: true]">
                    <i class="close bordered large red icon"></i>
                </g:link>
            </g:else>

        </div>
    </g:if>

</div>