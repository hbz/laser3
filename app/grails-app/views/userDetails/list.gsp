<%@ page import="com.k_int.kbplus.*" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <g:set var="entityName" value="${message(code: 'user.label', default: 'User')}" />
    <title><g:message code="default.list.label" args="[entityName]" /></title>
  </head>
  <body>
    <div>

          <h1 class="ui header">Users</h1>

          <div class="well form-horizontal">

          <g:form action="list" method="get">
            Name Contains: <input type="text" name="name" value="${params.name}"/>
            Role:    
             <g:set value="${com.k_int.kbplus.auth.Role.findAll()}" var="auth_values"/>
             <g:select from="${auth_values}" noSelection="${['null':'-Any role-']}" value="authority "optionKey="id" optionValue="authority" name="authority" />
            <input type="submit" value="Search" class="ui primary button"/>
          </g:form>
        </div>

        <g:if test="${flash.message}">
          <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
        </g:if>
        
        <table class="ui celled striped table">
          <thead>
            <tr>
                <g:sortableColumn property="username" params="${params}" title="${message(code: 'user.name.label', default: 'User Name')}" />
                <g:sortableColumn property="display" params="${params}" title="${message(code: 'user.display.label', default: 'Display Name')}" />
                <g:sortableColumn property="instname" params="${params}" title="${message(code: 'user.instname.label', default: 'Institution')}" />
                <td>Enabled</td>
            </tr>
          </thead>
          <tbody>
          <g:each in="${users}" var="user">
            <tr>
              <td><g:link  action="edit" id="${user.id}">${user.displayName}</g:link></td>
              <td>${fieldValue(bean: user, field: "display")}</td>
              <td>${fieldValue(bean: user, field: "instname")}</td>
              <td>${fieldValue(bean: user, field: "enabled")}</td>
            </tr>
          </g:each>
          </tbody>
        </table>

        <div class="pagination">
          <bootstrap:paginate total="${total}" params="${params}" />
        </div>

    </div>
  </body>
</html>
