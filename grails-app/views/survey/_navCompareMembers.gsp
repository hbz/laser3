<%@ page import="de.laser.AuditConfig; de.laser.ui.Icon; de.laser.storage.RDStore; de.laser.finance.CostItem" %>
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
    &nbsp;
        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferMembers)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferMembers: false]">
                <i class="${Icon.SYM.YES} bordered large green"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferMembers: true]">
                <i class="${Icon.SYM.NO} bordered large red"></i>
            </g:link>
        </g:else>

    </div>

    <g:if test="${surveyConfig.subSurveyUseForTransfer && parentSuccessorSubscription.holdingSelection != RDStore.SUBSCRIPTION_HOLDING_ENTIRE}">
        <div class="${(actionName == 'copySubPackagesAndIes') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySubPackagesAndIes"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id]">
                        ${message(code: 'copySubPackagesAndIes.label')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="${Icon.PACKAGE}"></i>${message(code: 'copySubPackagesAndIes.label')}
                </div>
            </div>
        &nbsp;&nbsp;
            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSubPackagesAndIes)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSubPackagesAndIes: false]">
                    <i class="${Icon.SYM.YES} bordered large green"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSubPackagesAndIes: true]">
                    <i class="${Icon.SYM.NO} bordered large red"></i>
                </g:link>
            </g:else>

        </div>
    </g:if>

    <g:if test="${surveyConfig.packageSurvey}">
        <div class="${(actionName == 'copySurveyPackages') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySurveyPackages"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id]">
                        ${message(code: 'copySurveyPackages.label')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="${Icon.PACKAGE}"></i>${message(code: 'copySurveyPackages.label')}
                </div>
            </div>
        &nbsp;&nbsp;
            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyPackages)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyPackages: false]">
                    <i class="${Icon.SYM.YES} bordered large green"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyPackages: true]">
                    <i class="${Icon.SYM.NO} bordered large red"></i>
                </g:link>
            </g:else>

        </div>
    </g:if>

    <g:if test="${surveyConfig.vendorSurvey}">
        <div class="${(actionName == 'copySurveyVendors') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySurveyVendors"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id]">
                        ${message(code: 'copySurveyVendors.label')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="${Icon.PACKAGE}"></i>${message(code: 'copySurveyVendors.label')}
                </div>
            </div>
        &nbsp;&nbsp;
            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyVendors)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyVendors: false]">
                    <i class="${Icon.SYM.YES} bordered large green"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyVendors: true]">
                    <i class="${Icon.SYM.NO} bordered large red"></i>
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
                <i class="${Icon.SYM.PROPERTIES}"></i>${message(code: 'properties')}
            </div>
        </div>
        &nbsp;
        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyProperties)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyProperties: false]">
                <i class="${Icon.SYM.YES} bordered large green"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyProperties: true]">
                <i class="${Icon.SYM.NO} bordered large red"></i>
            </g:link>
        </g:else>
    </div>


<g:if test="${surveyConfig.subscription}">

    <div class="${(actionName == 'copyProperties' && params.tab == 'customProperties') ? 'active' : ''}  step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, tab: 'customProperties', targetSubscriptionId: targetSubscription?.id]">
                    ${message(code: 'copyProperties.customProperties.short')}
                </g:link>
            </div>

            <div class="description">
                <i class="${Icon.SYM.PROPERTIES}"></i>${message(code: 'properties')}
            </div>
        </div>
        &nbsp;
        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferCustomProperties)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferCustomProperties: false]">
                <i class="${Icon.SYM.YES} bordered large green"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferCustomProperties: true]">
                <i class="${Icon.SYM.NO} bordered large red"></i>
            </g:link>
        </g:else>

    </div>

    <div class="${(actionName == 'copyProperties' && params.tab == 'privateProperties') ? 'active' : ''} step">

        <div class="content">
            <div class="title">
                <g:link controller="survey" action="copyProperties"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, tab: 'privateProperties', targetSubscriptionId: targetSubscription?.id]">
                    ${message(code: 'copyProperties.privateProperties')}
                </g:link>
            </div>

            <div class="description">
                <i class="${Icon.SYM.PROPERTIES}"></i>${message(code: 'properties')}
            </div>
        </div>
    &nbsp;
        <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferPrivateProperties)}">
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferPrivateProperties: false]">
                <i class="${Icon.SYM.YES} bordered large green"></i>
            </g:link>
        </g:if>
        <g:else>
            <g:link controller="survey" action="setSurveyTransferConfig"
                    params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferPrivateProperties: true]">
                <i class="${Icon.SYM.NO} bordered large red"></i>
            </g:link>
        </g:else>

    </div>

</g:if>

    <g:if test="${CostItem.executeQuery('select count(*) from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and costItem.costItemStatus != :status and costItem.pkg is null', [survConfig: surveyConfig, status: RDStore.COST_ITEM_DELETED])[0] > 0}">
        <div class="${(actionName == 'copySurveyCostItems') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySurveyCostItems"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id]">
                        ${message(code: 'copySurveyCostItems.surveyCostItems')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="money bill alternate outline icon"></i>${message(code: 'copySurveyCostItems.surveyCostItem')}
                </div>
            </div>
        &nbsp;
            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyCostItems)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyCostItems: false]">
                    <i class="${Icon.SYM.YES} bordered large green"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyCostItems: true]">
                    <i class="${Icon.SYM.NO} bordered large red"></i>
                </g:link>
            </g:else>

        </div>
    </g:if>


    <g:if test="${CostItem.executeQuery('select count(*) from CostItem costItem join costItem.surveyOrg surOrg where surOrg.surveyConfig = :survConfig and costItem.costItemStatus != :status and costItem.pkg is not null', [survConfig: surveyConfig, status: RDStore.COST_ITEM_DELETED])[0] > 0}">
        <div class="${(actionName == 'copySurveyCostItemPackage') ? 'active' : ''} step">

            <div class="content">
                <div class="title">
                    <g:link controller="survey" action="copySurveyCostItemPackage"
                            params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id]">
                        ${message(code: 'surveyCostItemsPackages.label')}
                    </g:link>
                </div>

                <div class="description">
                    <i class="money bill alternate outline icon"></i>${message(code: 'surveyCostItemsPackages.label')}
                </div>
            </div>
        &nbsp;
            <g:if test="${transferWorkflow && Boolean.valueOf(transferWorkflow.transferSurveyCostItems)}">
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyCostItemPackage: false]">
                    <i class="${Icon.SYM.YES} bordered large green"></i>
                </g:link>
            </g:if>
            <g:else>
                <g:link controller="survey" action="setSurveyTransferConfig"
                        params="[id: surveyInfo.id, surveyConfigID: surveyConfig.id, targetSubscriptionId: parentSuccessorSubscription.id, transferSurveyCostItemPackage: true]">
                    <i class="${Icon.SYM.NO} bordered large red"></i>
                </g:link>
            </g:else>

        </div>
    </g:if>

</div>