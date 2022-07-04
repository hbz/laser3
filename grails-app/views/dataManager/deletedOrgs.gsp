<!doctype html>
<html>
  <head>
    <meta name="layout" content="laser">
    <title>${message(code:'laser')} : ${message(code:'datamanager.deletedOrgManagement.label')}</title>
  </head>

  <body>

    <semui:breadcrumbs>
      <semui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager')}" />
      <semui:crumb text="${message(code:'datamanager.deletedOrgManagement.label')}" class="active"/>
    </semui:breadcrumbs>

    <semui:headerWithIcon message="datamanager.deletedOrgManagement.label">
      <semui:totalNumber total="${orgTotal}"/>
    </semui:headerWithIcon>

    <semui:messages data="${flash}" />

    <table class="ui sortable celled la-js-responsive-table la-table table">
      <thead>
        <tr>
          <g:sortableColumn property="sortname" title="${message(code: 'org.sortname.label')}" />
          <g:sortableColumn property="name" title="${message(code: 'default.name.label')}" />
          <th></th>
        </tr>
      </thead>
      <tbody>
        <g:each in="${orgList}" var="orgInstance">
          <tr>
            <td>${fieldValue(bean: orgInstance, field: "sortname")}</td>
            <td>${fieldValue(bean: orgInstance, field: "name")}</td>
            <td>
                <semui:xEditableRefData owner="${orgInstance}" field="status" config="${de.laser.storage.RDConstants.ORG_STATUS}"/>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>


        <semui:paginate action="deletedOrgs" controller="dataManager" params="${params}" next="${message(code:'default.paginate.next')}" prev="${message(code:'default.paginate.prev')}" max="${max}" total="${titleInstanceTotal}" />

  </body>
</html>
