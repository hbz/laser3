<%@page import="com.k_int.kbplus.Subscription"%>
<%@page import="static com.k_int.kbplus.SubscriptionController.WORKFLOW_END"%>
<laser:serviceInjection/>
<!doctype html>
<html>
    %{--<head>--}%
        %{--<meta name="layout" content="semanticUI" />--}%
        %{--<title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.my.comp_lic')}</title>--}%
    %{--</head>--}%
    <body>
    <semui:form>

        <g:form action="copyElementsIntoRenewalSubscription" controller="survey" id="${params.id ?: params.sourceSubscriptionId}"
                params="[workFlowPart: WORKFLOW_END, sourceSubscriptionId: sourceSubscriptionId, targetSubscriptionId: targetSubscriptionId, isRenewSub: isRenewSub]"
                method="post" class="ui form newLicence">

                <%
                    List subscriptions = [Subscription.get(sourceSubscriptionId)]
                    if (targetSubscriptionId) subscriptions.add(Subscription.get(targetSubscriptionId))
                %>

                <g:set var="subscriptionsCount" value="${subscriptions?.size()}"/>

                    <g:if test="${customProperties?.size() > 0}">
                        <table class="ui celled table la-table">
                            <g:render template="propertyComparisonTableRow" model="[group:customProperties,key:message(code:'license.properties'),subscriptions:subscriptions]" />
                        </table>
                    </g:if>

                    <g:if test="${privateProperties?.size() > 0}">
                        <table class="ui celled table la-table">
                            <g:render template="propertyComparisonTableRow" model="[group:privateProperties,key:message(code:'license.properties.private')+' '+contextService.getOrg().name,subscriptions:subscriptions]" />
                        </table>
                    </g:if>
            <g:set var="submitDisabled" value="${(sourceSubscription && targetSubscription)? '' : 'disabled'}"/>
            <g:if test="${customProperties || privateProperties}">
                <g:set var="submitButtonText" value="${isRenewSub?
                        message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStep') :
                        message(code: 'subscription.details.copyElementsIntoSubscription.copyProperties.button')}" />
                <div class="sixteen wide field" style="text-align: right;">
                    <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()" ${submitDisabled}/>
                </div>
            </g:if>
            <g:else>
                ${message(code: 'subscription.details.copyElementsIntoSubscription.copyProperties.empty')}
                <br><br>

                <div class="two fields">
                    <div class="eight wide field" style="text-align: left;">
                        <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndIsSubscriptionSurveyFix(Subscription.get(sourceSubscriptionId), true)}" />
                        <g:link action="renewalWithSurvey" id="${surveyConfig?.surveyInfo?.id}" params="[surveyConfigID: surveyConfig?.id]" class="ui button js-click-control">
                            <g:message code="renewalWithSurvey.back"/>
                        </g:link>
                    </div>
                    <div class="eight wide field" style="text-align: right;">
                        <g:set var="submitButtonText" value="${isRenewSub?
                                message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.lastStepWithoutSaveDate') :
                                message(code: 'subscription.details.copyElementsIntoSubscription.lastStepWithoutSaveDate')}" />
                        <input type="submit" class="ui button js-click-control" value="${submitButtonText}" onclick="return jsConfirmation()" ${submitDisabled}/>
                    </div>
                </div>

            </g:else>
    </g:form>
    </semui:form>
    </body>
</html>
