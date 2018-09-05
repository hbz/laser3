<!doctype html>
<html>
<head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'subscription.label', default:'Subscription')}</title>
</head>
<body>

    <g:render template="breadcrumb" model="${[ params:params ]}"/>
    <semui:controlButtons>
        <g:render template="actions" />
    </semui:controlButtons>
    <h1 class="ui header"><semui:headerIcon />

        <semui:xEditable owner="${subscriptionInstance}" field="name" />
    </h1>

    <g:render template="nav" />

    <g:if test="${subscriptionInstance.instanceOf && (contextOrg == subscriptionInstance.getConsortia())}">
        <div class="ui negative message">
            <div class="header"><g:message code="myinst.message.attention" /></div>
            <p>
                <g:message code="myinst.subscriptionDetails.message.ChildView" />
                <span class="ui label">${subscriptionInstance.getAllSubscribers()?.collect{itOrg -> itOrg.name}.join(',')}</span>.
            <g:message code="myinst.subscriptionDetails.message.ConsortialView" />
            <g:link controller="subscriptionDetails" action="show" id="${subscriptionInstance.instanceOf.id}"><g:message code="myinst.subscriptionDetails.message.here" /></g:link>.
            </p>
        </div>
    </g:if>

    <semui:messages data="${flash}" />

    <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList]}"/>

</body>
</html>

