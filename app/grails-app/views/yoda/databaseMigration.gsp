<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} : Database Migration</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb message="menu.yoda.dash" controller="yoda" action="index"/>
      <semui:crumb text="Database Migration" class="active"/>
    </semui:breadcrumbs>

    <semui:messages data="${flash}" />

    <h2 class="ui header">Private Properties (mandatory) without existing values</h2>

    <g:link controller="yoda" action="dbmFixPrivateProperties" params="[cmd:'doIt']" class="ui button negative">
        <i class="icon trash alternate"></i> &nbsp;  Delete all</g:link>

    <br/>

    <table class="ui table la-table-small">
        <tbody>
        <g:each in="${candidates}" var="cat, ppp">
            <tr>
                <td colspan="5">
                    <h3>${cat} - ${ppp.size()}</h3>
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
