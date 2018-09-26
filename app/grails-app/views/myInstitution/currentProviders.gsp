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
    %{--FILTER EINGEBAUT--}%
    <semui:filter>
        <g:form action="currentProviders" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              propList: propList,
                              tmplConfigShow: ['property', 'name', 'country']
                      ]"/>
        </g:form>
    </semui:filter>
    %{--FILTER EINGEBAUT--}%

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'shortname', 'name', 'publicContacts', 'privateContacts', 'licenses', 'identifier', 'type', 'sector']
              ]"/>

  </body>
</html>
