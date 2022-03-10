<%@ page import="de.laser.Subscription; de.laser.SubscriptionController; de.laser.CopyElementsService"%>
<laser:serviceInjection/>
<!doctype html>
<html>
%{--<head>--}%
%{--<meta name="layout" content="laser" />--}%
%{--<title>${message(code:'laser')} : ${message(code:'menu.my.comp_lic')}</title>--}%
%{--</head>--}%
<body>
<semui:form>
    <g:if test="${!fromSurvey && !isRenewSub}">
        <g:render template="/templates/copyElements/selectSourceAndTargetObject" model="[
                sourceObject          : sourceObject,
                targetObject          : targetObject,
                allObjects_readRights : allObjects_readRights,
                allObjects_writeRights: allObjects_writeRights]"/>
    </g:if>

    <g:form controller="${controllerName}" action="${actionName}" id="${params.id ?: params.sourceObjectId}" data-confirm-id="copyElements_form"
            params="[workFlowPart: CopyElementsService.WORKFLOW_END, sourceObjectId: genericOIDService.getOID(sourceObject), targetObjectId: genericOIDService.getOID(targetObject), isRenewSub: isRenewSub, fromSurvey: fromSurvey]"
            method="post" class="ui form newLicence">


        <g:if test="${privateProperties?.size() > 0}">
            <table class="ui celled table la-js-responsive-table la-table">
                <g:render template="/templates/copyElements/propertyComparisonTableRow"
                          model="[group: privateProperties, key: message(code: 'subscription.properties.private') + ' ' + contextService.getOrg().name, sourceObject: sourceObject]"/>
            </table>
        </g:if>
        <g:set var="submitDisabled" value="${(sourceObject && targetObject) ? '' : 'disabled'}"/>

        <g:if test="${privateProperties}">
            <g:set var="submitButtonText" value="${isRenewSub ?
                    message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStep') :
                    message(code: 'copyElementsIntoObject.copyProperties.button')}"/>
            <div class="sixteen wide field" style="text-align: right;">
                <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}"
                       data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete" ${submitDisabled}/>
            </div>
        </g:if>
        <g:else>
            <strong>${message(code: 'copyElementsIntoObject.copyProperties.empty')}</strong>
            <br /><br />

            <div class="sixteen wide field" style="text-align: right;">
                <g:set var="submitButtonText" value="${isRenewSub ?
                        message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStepWithoutSaveDate') :
                        message(code: 'copyElementsIntoObject.lastStepWithoutSaveDate')}"/>
                <input type="submit" id="copyElementsSubmit" class="ui button js-click-control" value="${submitButtonText}"
                       data-confirm-id="copyElements"
                       data-confirm-tokenMsg="${message(code: 'copyElementsIntoObject.delete.elements', args: [g.message(code:  "${sourceObject.getClass().getSimpleName().toLowerCase()}.label")])}"
                       data-confirm-term-how="delete" ${submitDisabled}/>
            </div>
        </g:else>

    </g:form>
</semui:form>
</body>
</html>
