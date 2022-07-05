<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : Database Migration</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.yoda" controller="yoda" action="index"/>
      <semui:crumb text="Database Migration" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <semui:h1HeaderWithIcon text="Private Properties (mandatory) without existing values" />

    <g:link controller="yoda" action="dbmFixPrivateProperties" params="[cmd:'doIt']" class="ui button negative">
        <i class="icon trash alternate outline"></i> &nbsp;  Delete all</g:link>

    <br />

    <table class="ui table compact">
        <tbody>
        <g:each in="${candidates}" var="cat, ppp">
            <tr>
                <td colspan="5">
                    <h3 class="ui header">${cat} - ${ppp.size()}</h3>
                </td>
            </tr>
            <tr>
                <td><strong>id</strong></td>
                <td><strong>pd.id</strong></td>
                <td></td>
                <td><strong>owner.id</strong></td>
                <td></td>
            </tr>
            <g:each in="${ppp}" var="pp">
                <tr>
                    <td>${pp.id}</td>
                    <td>${pp.type.id}</td>
                    <td>${pp.type.name}</td>
                    <td>${pp.owner.id}</td>
                    <td>${pp.owner }</td>
                </tr>
            </g:each>
        </g:each>
        </tbody>
    </table>

  </body>
</html>
