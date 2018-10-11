 <%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
    <head>
        <meta name="layout" content="semanticUI">
        <g:set var="entityName" value="${message(code: 'default.provider.label', default: 'Provider')}" />
        <title>${message(code:'laser', default:'LAS:eR')} : <g:message code="menu.institutions.all_provider" /></title>
    </head>
    <body>

        <semui:breadcrumbs>
            <semui:crumb message="menu.institutions.all_provider" class="active" />
        </semui:breadcrumbs>

        <sec:ifAnyGranted roles="ROLE_ADMIN,ROLE_ORG_EDITOR,ROLE_ORG_COM_EDITOR">
            <semui:controlButtons>
                <g:render template="actions" />
            </semui:controlButtons>
        </sec:ifAnyGranted>

        <h1 class="ui left aligned icon header"><semui:headerIcon /><g:message code="menu.institutions.all_provider" /> - ${orgListTotal} Treffer</h1>

        <semui:messages data="${flash}" />
        <semui:filter>
            <g:form action="listProvider" method="get" class="ui form">
                <g:render template="/templates/filter/orgFilter"
                          model="[tmplConfigShow: ['property', 'name', 'country']
                          ]"/>
            </g:form>
        </semui:filter>
        <g:render template="/templates/filter/orgFilterTable"
              model="[orgList: orgList,
                      tmplShowCheckbox: false,
                      tmplConfigShow: ['lineNumber', 'shortname', 'name', 'publicContacts', 'country']
              ]"/>
        <semui:paginate total="${orgListTotal}" params="${params}" />

    </body>
</html>
