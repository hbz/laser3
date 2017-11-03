<%@ page import="com.k_int.kbplus.Org" %>
<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI">
    <title>${ui.display}</title>
  </head>
  <body>
    <div class="container">
      <div class="row">
        <div class="span12">

          <div class="page-header">
             <h1>${ui.displayName?:'No username'}</h1>
          </div>

          <g:if test="${flash.message}">
            <bootstrap:alert class="alert-info">${flash.message}</bootstrap:alert>
          </g:if>

          <g:if test="${flash.error}">
            <bootstrap:alert class="alert-info">${flash.error}</bootstrap:alert>
          </g:if>

          <h3>${message(code:'user.affiliation.plural', default:'Affiliations')}</h3>

          <table class="ui celled table">
            <thead>
              <tr>
                <th>${message(code:'user.id', default:'Id')}</th>
                <th>${message(code:'user.org', default:'Org')}</th>
                <th>${message(code:'user.role', default:'Role')}</th>
                <th>${message(code:'user.status', default:'Status')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${ui.affiliations}" var="af">
                <tr>
                  <td>${af.id}</td>
                  <td>${af.org.name}</td>
                  <td>${af.formalRole?.authority}</td>
                  <td>${message(code:"cv.membership.status.${af.status}")}</td>
                </tr>
              </g:each>
            </tbody>
          </table>

          <h3>${message(code:'user.role.plural', default:'Roles')}</h3>

          <table class="ui celled table">
            <thead>
              <tr>
                <th>${message(code:'user.role', default:'Role')}</th>
              </tr>
            </thead>
            <tbody>
              <g:each in="${ui.roles}" var="rl">
                <tr>
                  <td>${rl.role.authority}</td>
                </tr>
              </g:each>
            </tbody>
          </table>

        </div>
      </div>
    </div>
  </body>
</html>
