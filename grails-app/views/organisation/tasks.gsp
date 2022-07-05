<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'task.plural')}</title>
</head>
<body>

    <semui:breadcrumbs>
        <g:if test="${!inContextOrg}">
            <semui:crumb text="${orgInstance.getDesignation()}" class="active"/>
        </g:if>
    </semui:breadcrumbs>
    <semui:controlButtons>
        <laser:render template="actions" model="${[org:org]}"/>
    </semui:controlButtons>

    <semui:h1HeaderWithIcon text="${orgInstance.name}" />

    <laser:render template="nav" />

    <semui:messages data="${flash}" />

    <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList, taskInstanceCount:taskInstanceCount]}"/>
    <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList, taskInstanceCount:myTaskInstanceCount]}"/>
    <laser:render template="/templates/tasks/js_taskedit"/>

</body>
</html>

