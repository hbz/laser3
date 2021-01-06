<%@ page import="de.laser.Package" %>
<!doctype html>
<html>
<head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'package.show.nav.notes')}</title>
</head>
<body>

    <semui:breadcrumbs>
        <semui:crumb controller="package" action="index" text="${message(code:'package.show.all')}" />
        <semui:crumb text="${packageInstance.name}" id="${packageInstance.id}" class="active"/>
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
        <g:render template="actions" />
    </semui:controlButtons>

    <semui:modeSwitch controller="package" action="show" params="${params}"/>

    <h1 class="ui icon header la-clear-before la-noMargin-top"><semui:headerIcon />
        ${packageInstance?.name}
    </h1>

    <semui:messages data="${flash}" />

    <g:render template="nav"/>

    <g:render template="/templates/notes/table" model="${[instance: packageInstance, redirect: 'notes']}"/>

</body>
</html>
