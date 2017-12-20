<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} Show Affiliations</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.admin.dash" controller="admin" action="index"/>
      <semui:crumb text="Show Affiliations" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <div>
        <h2 class="ui header">Affiliations</h2>
        <table class="ui celled striped table">
          <thead>
            <tr>
              <th>Username</th><th>Affiliations</th>
            </tr>
          </thead>
          <g:each in="${users}" var="u">
            <tr>
              <td>${u.displayName} / ${u.username}</td>
              <td>
                <ul>
                  <g:each in="${u.affiliations}" var="ua">
                    <li>${ua.org.shortcode}:${ua.status}:${ua.formalRole?.authority}</li>
                  </g:each>
                </ul>
              </td>
          </g:each>
        </table>
    </div>




  </body>
</html>
