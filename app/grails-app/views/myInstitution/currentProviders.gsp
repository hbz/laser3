<!doctype html>

<html>
    <head>
        <meta name="layout" content="semanticUI" />
        <title>${message(code:'laser', default:'LAS:eR')} : ${message(code:'menu.institutions.myProviders')}</title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb controller="myInstitution" action="dashboard" text="${institution?.getDesignation()}" />
            <semui:crumb message="menu.institutions.myProviders" class="active" />
        </semui:breadcrumbs>

    <h1 class="ui header"><semui:headerIcon /><g:message code="menu.institutions.myProviders" /></h1>

    <semui:messages data="${flash}" />

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'name', 'identifier', 'type', 'sector']
              ]"/>

  </body>
</html>
