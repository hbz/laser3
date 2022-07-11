<laser:htmlStart message="datamanager.deletedOrgManagement.label" />

    <ui:breadcrumbs>
      <ui:crumb controller="dataManager" action="index" text="${message(code:'menu.datamanager')}" />
      <ui:crumb text="${message(code:'datamanager.deletedOrgManagement.label')}" class="active"/>
    </ui:breadcrumbs>

    <ui:h1HeaderWithIcon message="datamanager.deletedOrgManagement.label" total="${orgTotal}" />

    <ui:messages data="${flash}" />

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
                <ui:xEditableRefData owner="${orgInstance}" field="status" config="${de.laser.storage.RDConstants.ORG_STATUS}"/>
            </td>
          </tr>
        </g:each>
      </tbody>
    </table>


        <ui:paginate action="deletedOrgs" controller="dataManager" params="${params}" max="${max}" total="${titleInstanceTotal}" />

<laser:htmlEnd />
