<!doctype html>
<html>
  <head>
    <meta name="layout" content="mmbootstrap"/>
    <title>${message(code:'laser', default:'LAS:eR')} Show Affiliations</title>
  </head>

  <body>

    <laser:breadcrumbs>
      <laser:crumb message="menu.admin.dash" controller="admin" action="index"/>
      <laser:crumb text="Show Affiliations" class="active"/>
    </laser:breadcrumbs>

    <laser:flash data="${flash}" />

    <div class="container">
        <h2>Affiliations</h2>
        <table class="table table-bordered table-striped">
          <tr>
            <th>Username</th><th>Affiliations</th>
          </tr>
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
