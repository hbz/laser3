<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'task.plural')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
    <h1 class="ui  icon header la-noMargin-top"><semui:headerIcon />
        <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
            <g:render template="iconSubscriptionIsChild"/>
        </g:if>
        <semui:xEditable owner="${subscription}" field="name" />
    </h1>
    <semui:anualRings object="${subscription}" controller="subscription" action="tasks" navNext="${navNextSubscription}" navPrev="${navPrevSubscription}"/>

    <g:render template="nav" />

    <g:if test="${subscription.instanceOf && contextOrg.id == subscription.getConsortia()?.id}">
        <g:render template="message" />
    </g:if>

    <semui:messages data="${flash}" />

    <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList, taskInstanceCount:taskInstanceCount]}"/>
    <g:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList, taskInstanceCount:myTaskInstanceCount]}"/>
    <g:render template="/templates/tasks/js_taskedit"/>

</body>
</html>

