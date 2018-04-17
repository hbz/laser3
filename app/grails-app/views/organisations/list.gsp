 <%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'org.label', default: 'Org')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="menu.institutions.all_orgs" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.all_orgs" class="active" />
        </semui:breadcrumbs>

        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR">
            <semui:controlButtons>
                <g:render template="actions" />
            </semui:controlButtons>
        </sec:ifAnyGranted>

        <h1 class="ui header"><semui:headerIcon /><g:message code="menu.institutions.all_orgs" /> - ${orgListTotal} Treffer</h1>

        <semui:messages data="${flash}" />

        <semui:filter>
            <g:form action="list" method="get" class="ui form">
                <g:render template="/templates/filter/orgFilter"
                          model="[tmplConfigShow: ['name', 'type', 'sector', 'federalState', 'libraryNetwork', 'libraryType']
                          ]"/>
            </g:form>
        </semui:filter>

        <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['name', 'wib', 'isil', 'type', 'sector', 'federalState', 'libraryNetwork', 'libraryType']
              ]"/>

        <semui:paginate total="${orgListTotal}" params="${params}" />

    </body>
</html>
