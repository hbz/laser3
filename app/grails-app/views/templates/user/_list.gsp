<%@ page import="com.k_int.kbplus.*; com.k_int.kbplus.auth.*" %>
<laser:serviceInjection />
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
    <body>

        <g:render template="breadcrumb" model="${[ params:params ]}"/>

        <semui:controlButtons>
            <g:render template="actions" />
        </semui:controlButtons>

        <h1 class="ui left aligned icon header"><semui:headerIcon />${message(code:'user.show_all.label')}
            <semui:totalNumber total="${total}"/>
        </h1>

            <g:render template="userFilterFragment" model="${filterConfig}"/>

            <g:if test="${consortiaWarning}">
                <div class="ui info message">${message(code:'user.edit.info')}</div>
            </g:if>

            <semui:messages data="${flash}" />

            <table class="ui sortable celled la-table la-table-small table">
                <thead>
                    <tr>
                        <%--<g:sortableColumn property="u.username" params="${params}" title="${message(code: 'user.name.label', default: 'User Name')}" />
                        <g:sortableColumn property="u.display" params="${params}" title="${message(code: 'user.display.label', default: 'Display Name')}" />
                        <g:sortableColumn property="uo.org.instname" params="${params}" title="${message(code: 'user.instname.label', default: 'Institution')}" />
                        --%>
                        <th>${message(code:'user.username.label')}</th>
                        <th>${message(code:'user.displayName.label')}</th>
                        <th>${message(code:'user.org')}</th>
                        <th>${message(code:'user.enabled.label')}</th>
                        <th class="la-action-info">${message(code:'default.actions')}</th>
                    </tr>
                </thead>
                <tbody>
                    <g:render template="userListFragment" model="${tableConfig}"/>
                </tbody>
            </table>

          <%-- <semui:paginate total="${total}" params="${params}" /> --%>

    </body>
</html>
