<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}" />
      <semui:crumb text="${message(code:'datamanager.deletedTitleManagement.label', default:'Deleted Title management')}" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header">${message(code:'datamanager.deletedTitleManagement.label', default:'Deleted Title Management')} : ${titleInstanceTotal} ${message(code:'datamanager.deletedTitleManagement.del_titles', default:'Deleted Titles')}</h1>

    <semui:messages data="${flash}" />

      <table class="ui sortable celled la-table table">
        <thead>
          <tr>
            <g:sortableColumn property="title" title="${message(code: 'title.label', default: 'Title')}" />
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


        <semui:paginate  action="deletedTitleManagement" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${titleInstanceTotal}" />

  </body>
</html>
