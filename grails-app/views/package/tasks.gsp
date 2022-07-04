<%@ page import="de.laser.Package" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'task.plural')}</title>
</head>

<body>

    <semui:breadcrumbs>
        <semui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
        <semui:crumb text="${packageInstance?.name}" id="${packageInstance?.id}" class="active"/>
    </semui:breadcrumbs>
    <semui:controlButtons>
        <semui:exportDropdown>
            <semui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'json']}">JSON</g:link>
            </semui:exportDropdownItem>
            <semui:exportDropdownItem>
                <g:link class="item" action="show" params="${params+[format:'xml']}">XML</g:link>
            </semui:exportDropdownItem>
        </semui:exportDropdown>
        <laser:render template="actions" />
    </semui:controlButtons>
    <semui:modeSwitch controller="package" action="show" params="${params}"/>

    <semui:headerWithIcon text="${packageInstance?.name}" />

    <laser:render template="nav"/>

    <semui:messages data="${flash}" />

    <laser:render template="/templates/tasks/table" model="${[taskInstanceList:taskInstanceList,taskInstanceCount:taskInstanceCount]}"/>
    <laser:render template="/templates/tasks/table2" model="${[taskInstanceList:myTaskInstanceList,taskInstanceCount:myTaskInstanceCount]}"/>

    <laser:render template="/templates/tasks/js_taskedit"/>

</body>
</html>
