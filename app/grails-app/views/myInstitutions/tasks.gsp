<!doctype html>
<r:require module="annotations" />

<html>
    <head>
        <meta name="layout" content="semanticUI"/>
        <title>${message(code:'laser', default:'LAS:eR')} ${institution.name} - ${message(code:'task.plural', default:'Tasks')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitutions" action="dashboard" params="${[shortcode:params.shortcode]}" text="${institution.name}" />
            <semui:crumb message="menu.institutions.tasks" class="active"/>
        </semui:breadcrumbs>

        <h1 class="ui header">${institution.name} - ${message(code:'task.plural', default:'Tasks')}</h1>

        <semui:messages data="${flash}" />

        <g:render template="/templates/tasks/table" model="${[ownobj:institution, owntp:'org', taskInstanceList:taskInstanceList]}"/>

        <g:render template="/templates/tasks/table2" model="${[ownobj:institution, owntp:'org', taskInstanceList:myTaskInstanceList]}"/>

  </body>
</html>
