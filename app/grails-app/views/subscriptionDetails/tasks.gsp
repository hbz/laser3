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

    <semui:messages data="${flash}" />

    <g:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList]}"/>
    <g:render template="/templates/tasks/js_taskedit"/>

</body>
</html>

