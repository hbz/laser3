<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'datamanager.deletedTitleManagement.label')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager.dash')}" />
      <semui:crumb text="${message(code:'datamanager.deletedTitleManagement.label')}" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header la-noMargin-top">
      ${message(code:'datamanager.deletedTitleManagement.label')}
      <semui:totalNumber total="${titleInstanceTotal}"/>
    </h1>

    <semui:messages data="${flash}" />

      <table class="ui sortable celled la-js-responsive-table la-table table">
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
