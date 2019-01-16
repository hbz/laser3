<!doctype html>
<html>
  <head>
    <meta name="layout" content="semanticUI"/>
    <title>${message(code:'laser', default:'LAS:eR')} ${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager.dash', default:'Data Manager Dashboard')}" />
      <semui:crumb text="${message(code:'datamanager.deletedOrgManagement.label', default:'Deleted Title management')}" class="active"/>
    </semui:breadcrumbs>

    <h1 class="ui header">${message(code:'datamanager.deletedOrgManagement.label', default:'Deleted Title Management')} : ${orgTotal} </h1>

    <semui:messages data="${flash}" />

    <table class="ui sortable celled la-table table">
      <thead>
        <tr>
          <g:sortableColumn property="sortname" title="${message(code: 'org.sortname.label')}" />
          <g:sortableColumn property="name" title="${message(code: 'org.name.label')}" />
          <th></th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${orgList}" var="orgInstance">
          <tr>
            <td>${fieldValue(bean: orgInstance, field: "sortname")}</td>
            <td>${fieldValue(bean: orgInstance, field: "name")}</td>
            <td>
                <semui:xEditableRefData owner="${orgInstance}" field="status" config='OrgStatus'/>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>


        <semui:paginate action="deletedOrgs" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next', default:'Next')}" prev="${message(code:'default.paginate.prev', default:'Prev')}" max="${max}" total="${titleInstanceTotal}" />

  </body>
</html>
