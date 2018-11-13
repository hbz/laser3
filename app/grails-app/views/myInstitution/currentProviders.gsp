<%@ page import="com.k_int.kbplus.RefdataValue" %>
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

    <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.institutions.myProviders" /></h1>

    <semui:messages data="${flash}" />
    <semui:filter>
        <g:form action="currentProviders" method="get" class="ui form">
            <g:render template="/templates/filter/orgFilter"
                      model="[
                              propList: propList,
                              orgRoles: orgRoles,
                              tmplConfigShow: ['role', 'property', 'name', 'country'],
                              tmplConfigFormFilter: true
                      ]"/>
        </g:form>
    </semui:filter>

    <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'shortname', 'name', 'publicContacts', 'privateContacts', 'licenses', 'identifier']
              ]"/>
    <semui:paginate total="${orgListTotal}" params="${params}" />
  </body>
</html>
