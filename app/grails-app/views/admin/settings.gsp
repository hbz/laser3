<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Admin Settings</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
      <semui:crumb text="System Settings" class="active"/>
    </semui:breadcrumbs>

    <div class="container">
      <table class="ui celled table">
        <thead>
          <tr>
            <td>Setting</td>
            <td>Value</td>
          </tr>
        </thead>
        <tbody>
          <g:each in="${settings}" var="s">
            <tr>
              <td>${s.name}</td>
              <td>
                <g:if test="${s.tp==1}">
                  <g:link controller="admin" action="toggleBoolSetting" params="${[setting:s.name]}">${s.value}</g:link>
                </g:if>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
    </div>
  </body>
</html>
