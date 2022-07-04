<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'datamanager.deletedTitleManagement.label')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager')}" />
      <semui:crumb text="${message(code:'datamanager.deletedTitleManagement.label')}" class="active"/>
    </semui:breadcrumbs>

    <semui:headerWithIcon message="datamanager.deletedTitleManagement.label">
      <semui:totalNumber total="${titleInstanceTotal}"/>
    </semui:headerWithIcon>

    <semui:messages data="${flash}" />

      <table class="ui sortable celled la-js-responsive-table la-table table">
        <thead>
          <tr>
            <g:sortableColumn property="title" title="${message(code: 'title.label')}" />
            <th></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${titleList}" var="titleInstance">
            <tr>
              <td>${fieldValue(bean: titleInstance, field: "title")}</td>
            </tr>
          </g:each>
        </tbody>
      </table>

      <semui:paginate action="deletedTitles" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${titleInstanceTotal}" />

  </body>
</html>
