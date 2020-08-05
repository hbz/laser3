<%@ page import="com.k_int.kbplus.Person; com.k_int.kbplus.RefdataValue; com.k_int.kbplus.SubscriptionController" %>
<laser:serviceInjection/>

<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code: 'laser')} : ${message(code: 'subscription.details.copyMyElements.label')}</title>
</head>

<body>
<g:render template="breadcrumb" model="${[params: params]}"/>
<br>

<h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon/>${message(code: 'subscription.details.copyMyElements.label')}</h1>

<semui:messages data="${flash}"/>

<% Map params = [id: params.id];
if (sourceSubscriptionId) params << [sourceSubscriptionId: sourceSubscriptionId];
if (targetSubscriptionId) params << [targetSubscriptionId: targetSubscriptionId];
%>

<semui:subNav>

    <semui:complexSubNavItem class="${workFlowPart == SubscriptionController.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS ? 'active' : ''}"
                             controller="subscription" action="copyMyElements"
                             params="${params << [workFlowPart: SubscriptionController.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS]}">
        <div class="content">
            <div class="title">${message(code: 'subscription.details.copyElementsIntoSubscription.attachements.label')}</div>

            <div class="description">
                <i class="file outline icon"></i>${message(code: 'default.documents.label')}
                <i class="sticky note outline icon"></i>${message(code: 'default.notes.label')}
                <i class="checked calendar icon"></i>${message(code: 'menu.institutions.tasks')}
            </div>
        </div>
    </semui:complexSubNavItem>

    <semui:complexSubNavItem class="${workFlowPart == SubscriptionController.WORKFLOW_PROPERTIES ? 'active' : ''}" controller="subscription"
                             action="copyMyElements" params="${params << [workFlowPart: SubscriptionController.WORKFLOW_PROPERTIES]}">
        <div class="content">
            <div class="title">${message(code: 'properties')}</div>

            <div class="description">
                <i class="tags icon"></i>${message(code: 'properties')}
            </div>

        </div>
    </semui:complexSubNavItem>
</semui:subNav>

<br>

<div class="la-legend">
    <span class="la-key"><strong>${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.key')}:</strong>
    </span>
    <span class="la-added">${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.willStay')}</span>
    <span class="la-removed">${message(code: 'subscription.renewSubscriptionConsortia.workFlowSteps.legend.willBeReplaced')}</span>
</div>
<g:if test="${workFlowPart == SubscriptionController.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS}">
    <g:render template="/templates/subscription/copyDocsAndTasks"/>
</g:if>
<g:elseif test="${workFlowPart == SubscriptionController.WORKFLOW_PROPERTIES}">
    <g:render template="/templates/subscription/copyPrivatePropertiesCompare"/>
</g:elseif>
<g:javascript src="copyPropertiesCompare.js"/>

</body>
</html>
