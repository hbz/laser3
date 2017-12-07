<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'subscription.label', default:'Subscription')}</title>
</head>
<body>

    <semui:breadcrumbs>
        <g:if test="${params.shortcode}">
            <semui:crumb controller="myInstitutions" action="currentSubscriptions" params="${[shortcode:params.shortcode]}" text="${params.shortcode} - ${message(code:'myinst.currentSubscriptions.label', default:'Current Subscriptions')}" />
        </g:if>
        <semui:crumb controller="subscriptionDetails" action="index" id="${subscriptionInstance.id}"  text="${subscriptionInstance.name}" />
        <semui:crumb class="active" text="${message(code:'task.plural', default:'Tasks')}" />
    </semui:breadcrumbs>

    <g:if test="${editable}">
        <semui:crumbAsBadge message="default.editable" class="orange" />
    </g:if>

    <h1 class="ui header">${subscriptionInstance?.name}</h1>

    <g:render template="nav" />

    <g:render template="/templates/tasks/table" model="${[ownobj:subscriptionInstance, owntp:'subscription', taskInstanceList:taskInstanceList]}"/>

</body>
</html>

