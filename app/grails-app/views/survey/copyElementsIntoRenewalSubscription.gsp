<%@ page import="com.k_int.kbplus.Person" %>
<%@ page import="com.k_int.kbplus.RefdataValue" %>
<%@ page import="static com.k_int.kbplus.SubscriptionController.*"%>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.copyElementsIntoSubscription.label')}</title>
</head>
<body>


<semui:breadcrumbs>
    <semui:crumb controller="survey" action="currentSurveysConsortia" text="${message(code:'menu.my.surveys')}" />

    <g:if test="${sourceSubscription}">
        <g:set var="surveyConfig" value="${com.k_int.kbplus.SurveyConfig.findBySubscriptionAndIsSubscriptionSurveyFix(sourceSubscription, true)}"/>
        <semui:crumb controller="survey" action="renewalWithSurvey" id="${surveyConfig.surveyInfo.id}" params="[surveyConfigID: surveyConfig.id]" text="${surveyConfig.surveyInfo.name}" />
    </g:if>

    <semui:crumb class="active" message="subscription.details.renewals.renew_sub.label" />

</semui:breadcrumbs>
<br>
    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        <g:if test="${isRenewSub}">
            ${message(code: 'subscription.details.renewals.renew_sub.label')}: <g:if test="${sourceSubscription}">${sourceSubscription.name}</g:if>
        </g:if>
        <g:else>
            ${message(code: 'subscription.details.copyElementsIntoSubscription.label')}
        </g:else>
    </h1>
    <semui:messages data="${flash}"/>

    <% Map params = [id: params.id];
        if (sourceSubscriptionId)   params << [sourceSubscriptionId: sourceSubscriptionId];
        if (targetSubscriptionId)   params << [targetSubscriptionId: targetSubscriptionId];
        if (isRenewSub)             params << [isRenewSub: isRenewSub];

    %>
    <g:if test="${isRenewSub}">
        <div class="ui tablet stackable steps la-clear-before">
            <div class="${workFlowPart == WORKFLOW_DATES_OWNER_RELATIONS ? 'active' : ''} step">
                <div class="content">
                    <div class="content" >
                        <div class="title">
                            <g:link controller="survey" action="copyElementsIntoRenewalSubscription" params="${params << [workFlowPart: null]}">
                                ${message(code: 'subscription.details.copyElementsIntoSubscription.general_data.label')}
                            </g:link>
                        </div>
                        <div class="description">
                            <i class="calendar alternate outline icon"></i>${message(code: 'subscription.periodOfValidity.label')}
                            <i class="balance scale icon"></i>${message(code: 'license.label')}
                            <i class="university icon"></i>${message(code: 'subscription.organisations.label')}
                        </div>
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_PACKAGES_ENTITLEMENTS ? 'active' : ''} step">
                <div class="content" >
                    <div class="title">
                        <g:link controller="survey" action="copyElementsIntoRenewalSubscription" params="${params << [workFlowPart: WORKFLOW_DATES_OWNER_RELATIONS]}">
                            ${message(code: 'subscription.details.copyElementsIntoSubscription.inventory.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="gift icon"></i>${message(code: 'package.label')}
                        <i class="book icon"></i>${message(code: 'title')}
                    </div>
                </div>
            </div>
            <div class="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copyElementsIntoRenewalSubscription" params="${params << [workFlowPart: WORKFLOW_PACKAGES_ENTITLEMENTS]}">
                            ${message(code: 'subscription.details.copyElementsIntoSubscription.attachements.label')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                        <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                        <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
                    </div>
                </div>
            </div>

            <div class="${workFlowPart == WORKFLOW_PROPERTIES ? 'active' : ''} step">
                <div class="content">
                    <div class="title">
                        <g:link controller="survey" action="copyElementsIntoRenewalSubscription" params="${params << [workFlowPart: WORKFLOW_SUBSCRIBER]}">
                            ${message(code: 'properties')}
                        </g:link>
                    </div>
                    <div class="description">
                        <i class="tags icon"></i>${message(code: 'properties')}
                    </div>
                </div>
            </div>
        </div>
    </g:if>

    <br>
    <div class="la-legend">
        <span class="la-key"><strong>${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.key')}: </strong></span>
        <span class="la-added">${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.willStay')}</span>
        <span class="la-removed">${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.willBeReplaced')}</span>
    </div>
    <g:if test="${workFlowPart == WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
        <g:render template="/templates/subscription/copyDocsAndTasks" />
    </g:if>
    <g:elseif test="${workFlowPart == WORKFLOW_PROPERTIES}">
        <g:render template="/templates/subscription/copyPropertiesCompare" />
    </g:elseif>
    <g:elseif test="${workFlowPart == WORKFLOW_PACKAGES_ENTITLEMENTS}">
        <g:render template="/templates/subscription/copyPackagesAndIEs" />
    </g:elseif>
    %{--<g:elseif test="${workFlowPart == WORKFLOW_DATES_OWNER_RELATIONS}">--}%
    <g:else>
        <g:render template="/templates/subscription/copyElements" />
    </g:else>

    <g:javascript src="copyPropertiesCompare.js"/>
</body>
</html>
