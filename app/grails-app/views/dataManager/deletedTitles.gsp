<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser')} ${message(code:'menu.datamanager.dash')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager.dash')}" />
      <semui:crumb text="${message(code:'datamanager.deletedTitleManagement.label', default:'Deleted Title management')}" class="active"/>
    </semui:breadcrumbs>
    <br>
    <h1 class="ui header la-noMargin-top">${message(code:'datamanager.deletedTitleManagement.label', default:'Deleted Title Management')} : ${titleInstanceTotal}</h1>

    <semui:messages data="${flash}" />

      <table class="ui sortable celled la-table table">
        <thead>
          <tr>
            <g:sortableColumn property="title" title="${message(code: 'title.label')}" />
            <th></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${titleInstanceList}" var="titleInstance">
            <tr>
              <td>${fieldValue(bean: titleInstance, field: "title")}</td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <semui:paginate action="deletedTitles" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${titleInstanceTotal}" />

  </body>
</html>
